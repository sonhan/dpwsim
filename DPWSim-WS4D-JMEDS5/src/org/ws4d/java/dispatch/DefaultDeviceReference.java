/*******************************************************************************
 * Copyright (c) 2009 MATERNA Information & Communications. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html. For further
 * project-related information visit http://www.ws4d.org. The most recent
 * version of the JMEDS framework can be obtained from
 * http://sourceforge.net/projects/ws4d-javame.
 ******************************************************************************/

package org.ws4d.java.dispatch;

import java.util.NoSuchElementException;

import org.ws4d.java.DPWSFramework;
import org.ws4d.java.communication.CommunicationManager;
import org.ws4d.java.communication.DefaultResponseCallback;
import org.ws4d.java.communication.Discovery;
import org.ws4d.java.communication.ProtocolData;
import org.ws4d.java.communication.TimeoutException;
import org.ws4d.java.configuration.DispatchingProperties;
import org.ws4d.java.constants.DPWSMessageConstants;
import org.ws4d.java.constants.WSDConstants;
import org.ws4d.java.constants.WSSecurityConstants;
import org.ws4d.java.dispatch.DeviceListenerQueue.DeviceEvent;
import org.ws4d.java.message.FaultMessage;
import org.ws4d.java.message.Message;
import org.ws4d.java.message.discovery.ByeMessage;
import org.ws4d.java.message.discovery.HelloMessage;
import org.ws4d.java.message.discovery.ProbeMatch;
import org.ws4d.java.message.discovery.ProbeMatchesMessage;
import org.ws4d.java.message.discovery.ProbeMessage;
import org.ws4d.java.message.discovery.ResolveMatchesMessage;
import org.ws4d.java.message.discovery.ResolveMessage;
import org.ws4d.java.message.metadata.GetMessage;
import org.ws4d.java.message.metadata.GetResponseMessage;
import org.ws4d.java.service.Device;
import org.ws4d.java.service.LocalDevice;
import org.ws4d.java.service.ProxyFactory;
import org.ws4d.java.service.reference.DeviceListener;
import org.ws4d.java.service.reference.DeviceReference;
import org.ws4d.java.structures.AppSequenceTracker;
import org.ws4d.java.structures.DataStructure;
import org.ws4d.java.structures.EmptyStructures;
import org.ws4d.java.structures.HashMap;
import org.ws4d.java.structures.HashSet;
import org.ws4d.java.structures.Iterator;
import org.ws4d.java.structures.LockedMap;
import org.ws4d.java.structures.ReadOnlyIterator;
import org.ws4d.java.types.AppSequence;
import org.ws4d.java.types.AttributedURI;
import org.ws4d.java.types.DiscoveryData;
import org.ws4d.java.types.EndpointReference;
import org.ws4d.java.types.HostMData;
import org.ws4d.java.types.QName;
import org.ws4d.java.types.QNameSet;
import org.ws4d.java.types.ScopeSet;
import org.ws4d.java.types.URI;
import org.ws4d.java.types.URISet;
import org.ws4d.java.types.XAddressInfo;
import org.ws4d.java.types.XAddressInfoSet;
import org.ws4d.java.util.Log;
import org.ws4d.java.util.TimedEntry;
import org.ws4d.java.util.WatchDog;

/**
 * Class holds listeners of device reference. Also manages creating and
 * disposing of devices.
 */
public class DefaultDeviceReference extends TimedEntry implements DeviceReference {

	/**
	 * This sequence number should only be used to compare the first incoming
	 * message to proxy devices
	 */
	public static final AppSequence					APP_SEQUENCE_ZERO					= new AppSequence(-1, 0);

	public static final int							EVENT_DEVICE_SEEN					= 0;

	public static final int							EVENT_DEVICE_BYE					= 1;

	public static final int							EVENT_DEVICE_GET_RSP				= 2;

	public static final int							EVENT_DEVICE_CHANGED				= 3;

	public static final int							EVENT_DEVICE_COMPLETELY_DISCOVERED	= 4;

	public static final int							EVENT_DEVICE_FAULT_RESET			= 5;

	private static final GetRequestSynchronizer		UP_TO_DATE_GET_SYNCHRONIZER			= new GetRequestSynchronizer();

	private static final RequestSynchronizer		UP_TO_DATE_PROBE_SYNCHRONIZER		= new RequestSynchronizer();

	private static final ResolveRequestSynchronizer	UP_TO_DATE_RESOLVE_SYNCHRONIZER		= new ResolveRequestSynchronizer();

	private static final int						SYNC_WAITTIME						= 5000;

	private static final int						SYNC_WAITRETRY						= 5;

	private boolean									isSecure							= false;

	// ------------------------------------------------------------------------------------

	private Device									device								= null;

	// DeviceListener --> ListenerQueue
	private LockedMap								listeners							= new LockedMap();

	/** Changes to local device discovery data must not occur */
	private DiscoveryData							discoveryData						= null;

	private String									customMData							= null;

	private XAddressInfo							preferredXAddressInfo				= null;

	private int										location							= LOCATION_UNKNOWN;

	private AppSequenceTracker						appSequenceTracker					= null;

	private StateManager							proxyReferenceState					= new StateManager();

	private GetRequestSynchronizer					getSynchronizer						= null;

	private RequestSynchronizer						probeSynchronizer					= null;

	private ResolveRequestSynchronizer				resolveSynchronizer					= null;

	// Message --> [Get|Resolve]RequestSynchronizer
	private final HashMap							synchronizers						= new HashMap();

	private boolean									autoUpdateDevice					= false;

	// -------------------------- CONSTRUCTOR ---------------------------

	/**
	 * Constructor, device is not initialized. This constructor is used for a
	 * proxy device.
	 * 
	 * @param data discovery data.
	 * @param protocolData
	 */
	DefaultDeviceReference(AppSequence appSeq, DiscoveryData data, ProtocolData protocolData) {
		super();
		DiscoveryData dataClone = new DiscoveryData(data);

		XAddressInfoSet xAddressInfoSet = dataClone.getXAddressInfoSet();
		if (xAddressInfoSet != null) {
			xAddressInfoSet.mergeProtocolInfo(protocolData.getProtocolInfo());
		}
		setDiscoveryData(dataClone);
		this.location = LOCATION_REMOTE;
		appSequenceTracker = new AppSequenceTracker(appSeq);
		setPreferredXAddress(dataClone, protocolData);
		setPreferredVersion(protocolData.getCommunicationManagerId());

		// Condition: we must not use Bye-Messages with metadata version to init
		if (dataClone.getMetadataVersion() > DiscoveryData.UNKNOWN_METADATA_VERSION) {
			proxyReferenceState.setState(STATE_RUNNING);
		}

		WatchDog.getInstance().register(this, DispatchingProperties.getInstance().getReferenceCachingTime());
	}

	/**
	 * Constructor. Location of device is unknown.
	 * 
	 * @param epr
	 */
	DefaultDeviceReference(EndpointReference epr) {
		super();
		if (epr == null) {
			throw new IllegalArgumentException("endpoint reference must not be null");
		}
		setDiscoveryData(new DiscoveryData(epr));
		appSequenceTracker = new AppSequenceTracker();
		WatchDog.getInstance().register(this, DispatchingProperties.getInstance().getReferenceCachingTime());
	}

	/**
	 * Constructor. Location of device is unknown.
	 * 
	 * @param epr
	 */
	DefaultDeviceReference(EndpointReference epr, XAddressInfoSet addresses) {
		super();
		DiscoveryData d = new DiscoveryData(epr);
		d.setXAddresInfoSet(addresses);
		setDiscoveryData(d);
		appSequenceTracker = new AppSequenceTracker();
		preferredXAddressInfo = addresses.toArray()[0];
		WatchDog.getInstance().register(this, DispatchingProperties.getInstance().getReferenceCachingTime());
	}

	/**
	 * Constructor. Only to be used by local devices.
	 * 
	 * @param device Local device.
	 */
	DefaultDeviceReference(LocalDevice device) {
		super();
		setDiscoveryData(device.getDiscoveryData());
		setLocalDevice(device);
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.management.TimedEntry#toString()
	 */
	public synchronized String toString() {
		StringBuffer sb = new StringBuffer("DeviceReference [ discoveryData=");
		sb.append(discoveryData);
		String loc = (location == LOCATION_UNKNOWN ? "unknown" : (location == LOCATION_REMOTE ? "remote" : "local"));
		sb.append(", location=").append(loc);
		if (location != LOCATION_LOCAL) {
			sb.append(", address=").append(preferredXAddressInfo);
		}
		sb.append(", device=").append(device);
		sb.append(" ]");
		return sb.toString();
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.service.reference.DeviceReference#getState()
	 */
	public int getState() {
		if (location == LOCATION_LOCAL) {
			LocalDevice device = (LocalDevice) this.device;
			if (device != null) {
				return device.isRunning() ? STATE_BUILD_UP : STATE_STOPPED;
			} else {
				Log.error("DefaultDeviceReference.getState: Location is local, but no device specified");
				return STATE_UNKNOWN;
			}
		}

		synchronized (this) {
			return proxyReferenceState.getState();
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.service.reference.DeviceReference#getDevice()
	 */
	public Device getDevice() throws TimeoutException {
		return getDevice(true);
	}

	/**
	 * Returns device. If doBuildUp is <code>false</code>, no proxy device will
	 * be created, i.e. no resolve and get messages will be sent.
	 * 
	 * @param doBuildUp Specifies that a proxy device should be built up, if no
	 *            device already exists.
	 * @return May be <code>null</code>, if build up was not requested.
	 * @throws TimeoutException
	 */
	protected Device getDevice(boolean doBuildUp) throws TimeoutException {
		boolean stoppedRemoteDevice = false;
		GetRequestSynchronizer sync = null;
		boolean havePendingSync = false;
		XAddressInfo xAddressInfo = null;
		synchronized (this) {
			if (location == LOCATION_LOCAL) {
				return device;
			}
			if (!doBuildUp || getSynchronizer == UP_TO_DATE_GET_SYNCHRONIZER) {
				return device;
			}

			if (getSynchronizer != null) {
				sync = getSynchronizer;
				havePendingSync = true;
			} else {
				sync = getSynchronizer = new GetRequestSynchronizer(this);
			}
			if (proxyReferenceState.getState() == STATE_STOPPED) {
				stoppedRemoteDevice = true;
				resolveSynchronizer = null;
			}
			xAddressInfo = preferredXAddressInfo;
		}

		if (havePendingSync) {
			return waitForDevice(sync);
		}

		if (xAddressInfo == null || xAddressInfo.getXAddress() == null) {
			xAddressInfo = resolveRemoteDevice();
		} else if (stoppedRemoteDevice) {
			fetchCompleteDiscoveryDataSync(resolveRemoteDevice());
		}

		// check whether there is a newer Get attempt
		GetRequestSynchronizer newerSync;
		synchronized (this) {
			newerSync = getSynchronizer;
			if (newerSync == sync) {
				sync.metadataVersion = discoveryData.getMetadataVersion();
			}
		}
		if (newerSync != sync) {
			try {
				sync.device = getDevice(true);
			} catch (TimeoutException e) {
				sync.exception = e;
			}
			synchronized (sync) {
				sync.pending = false;
				sync.notifyAll();
			}
			if (sync.exception != null) {
				throw sync.exception;
			}
			return sync.device;
		}

		synchronized (this) {
			synchronizers.put(sendGet(xAddressInfo).getMessageId(), sync);
		}

		return waitForDevice(sync);
	}

	private Device waitForDevice(GetRequestSynchronizer sync) throws TimeoutException {
		while (true) {
			synchronized (sync) {
				int i = 0;
				while (sync.pending) {
					try {
						sync.wait(SYNC_WAITTIME);
						i++;
						if (i >= SYNC_WAITRETRY) {
							throw new TimeoutException("Device has not send an answer within " + (SYNC_WAITTIME * SYNC_WAITRETRY) + "ms.");
						}
					} catch (InterruptedException e) {
						Log.printStackTrace(e);
					}
				}

				if (sync.exception != null) {
					throw sync.exception;
				} else if (sync.device != null) {
					return sync.device;
				}
				/*
				 * else { this means we had a concurrent update and someone was
				 * started to obtain a newer device }
				 */
			}

			synchronized (this) {
				if (getSynchronizer == UP_TO_DATE_GET_SYNCHRONIZER) {
					return device;
				} else if (getSynchronizer != null) {
					sync = getSynchronizer;
				} else {
					throw new TimeoutException("Unknown communication error with device.");
				}
			}
		}
	}

	/**
	 * Rebuilds device. Removes all service references from registry. Should
	 * only be used for remote devices.
	 * 
	 * @return Rebuild device.
	 * @throws TimeoutException
	 */
	public Device rebuildDevice() throws TimeoutException {
		reset(true);
		return getDevice();
	}

	/**
	 * Instructs this device reference to asynchronously send a Get message to
	 * the device and create a new proxy, if required. The new proxy device is
	 * than announced asynchronously via
	 * {@link DeviceListener#deviceBuildUp(DeviceReference, Device)} method.
	 * <p>
	 * Note that in oder to reduce network traffic a Get message will actually
	 * be sent only if it is detected that the device within this device
	 * reference instance is not up to date anymore.
	 */
	public void buildUpDevice() {
		GetRequestSynchronizer sync;
		synchronized (this) {
			if (getSynchronizer != null) {
				return;
			}
			sync = getSynchronizer = new GetRequestSynchronizer(this);
		}
		buildUpDevice(sync);
	}

	private void buildUpDevice(final GetRequestSynchronizer newSynchronizer) {
		XAddressInfo xAddressInfo = null;
		synchronized (this) {
			if (getSynchronizer != newSynchronizer) {
				return;
			}
			xAddressInfo = preferredXAddressInfo;
			if (xAddressInfo != null) {
				newSynchronizer.metadataVersion = discoveryData.getMetadataVersion();
				synchronizers.put(sendGet(xAddressInfo).getMessageId(), newSynchronizer);
				return;
			}
		}

		// start new thread for resolving
		DPWSFramework.getThreadPool().execute(new Runnable() {

			public void run() {
				try {
					XAddressInfo xAddressInfo = resolveRemoteDevice();
					boolean callNotify = true;
					synchronized (DefaultDeviceReference.this) {
						if (newSynchronizer == getSynchronizer) {
							newSynchronizer.metadataVersion = discoveryData.getMetadataVersion();
							synchronizers.put(sendGet(xAddressInfo).getMessageId(), newSynchronizer);
							callNotify = false;
						}
					}
					if (callNotify) {
						synchronized (newSynchronizer) {
							newSynchronizer.pending = false;
							newSynchronizer.notifyAll();
						}
					}
				} catch (TimeoutException e) {
					Log.warn("Unablte to resolve remote device: " + e.getMessage());
				}
			}

		});
	}

	/**
	 * Sets device, replaces present device. Only to be used for local devices.
	 * 
	 * @param device Replacement device (local).
	 * @return replaced device.
	 */
	public Device setLocalDevice(LocalDevice device) {
		if (this.device == device) {
			return device;
		}

		if (device == null) {
			/*
			 * CASE: somebody want to move local device to remote location or
			 * remove device from device ref.
			 */
			location = LOCATION_UNKNOWN;
			Device oldDevice = this.device;
			this.device = null;
			discoveryData = new DiscoveryData(discoveryData.getEndpointReference(), discoveryData.getMetadataVersion());
			getSynchronizer = null;
			probeSynchronizer = null;
			resolveSynchronizer = null;
			return oldDevice;
		}

		if (location == LOCATION_REMOTE) {
			// XXX think about moving remote device to local location
			Log.error("DefaultDeviceReference.setDevice: Setting local device to remote reference: Two devices using the same endpoint reference!");
			throw new RuntimeException("Setting local device to a remote reference!");
		}

		location = LOCATION_LOCAL;
		getSynchronizer = UP_TO_DATE_GET_SYNCHRONIZER;
		resolveSynchronizer = UP_TO_DATE_RESOLVE_SYNCHRONIZER;
		probeSynchronizer = UP_TO_DATE_PROBE_SYNCHRONIZER;
		preferredXAddressInfo = null;

		LocalDevice oldDevice = (LocalDevice) this.device;
		this.device = device;

		WatchDog.getInstance().unregister(this);
		// copy device metadata from device
		setDiscoveryData(device.getDiscoveryData());

		if (oldDevice == null || !device.equals(oldDevice)) {
			if (device.isRunning()) {
				announceDeviceChangedAndBuildUp();
			}
		}

		return oldDevice;
	}

	public void setDiscoveryData(DiscoveryData newDiscoveryData) {
		if (newDiscoveryData == null) {
			throw new IllegalArgumentException("discoverData must not be null");
		}
		if (newDiscoveryData.getEndpointReference() == null) {
			throw new IllegalArgumentException("endpoint reference within discoverData must not be null");
		}
		this.discoveryData = newDiscoveryData;
	}

	/**
	 * Resets all state information of the device reference except the endpoint
	 * reference. Removes the association between the device and services. This
	 * method has the same effect as calling {@link #reset(boolean)} with an
	 * argument of <code>false</code>.
	 */
	public synchronized void reset() {
		reset(false);
	}

	/**
	 * Resets all state information of the device reference except the endpoint
	 * reference. Removes the association between the device and services. If
	 * parameter <code>recurse</code> is set to <code>true</code>, than all
	 * service references currently associated with this device reference will
	 * be reset prior to removing them, too.
	 * 
	 * @param recurse if service references associated with this device
	 *            reference shell be reset, too
	 */
	public synchronized void reset(boolean recurse) {
		if (location == LOCATION_LOCAL) {
			Log.warn("DefaultDeviceReference.reset: Not allowed to reset references to local devices!");
			return;
		}

		if (Log.isInfo()) {
			Log.info("DefaultDeviceReference.reset: Resetting device reference with endpoint reference " + discoveryData.getEndpointReference());
		}

		disconnectAllServiceReferences(recurse);
		device = null;
		discoveryData = new DiscoveryData(discoveryData.getEndpointReference(), DiscoveryData.UNKNOWN_METADATA_VERSION);
		changeProxyReferenceState(EVENT_DEVICE_FAULT_RESET);
		location = LOCATION_UNKNOWN;

		appSequenceTracker = new AppSequenceTracker();

		preferredXAddressInfo = null;

		getSynchronizer = null;
		probeSynchronizer = null;
		resolveSynchronizer = null;
	}

	/**
	 * Uses directed Probe to refresh discovery data. A previously built up
	 * device will be disposed of.
	 * 
	 * @return
	 * @throws TimeoutException
	 */
	public void fetchCompleteDiscoveryDataSync() throws TimeoutException {
		fetchCompleteDiscoveryDataSync(resolveRemoteDevice());
	}

	/**
	 * Sends directed probe to device
	 */
	private void fetchCompleteDiscoveryDataSync(XAddressInfo xAddressInfo) throws TimeoutException {
		// we MUST NOT have locked DefaultDeviceReference.this up to now!
		RequestSynchronizer sync;
		synchronized (this) {
			if (probeSynchronizer == UP_TO_DATE_PROBE_SYNCHRONIZER) {
				return;
			}

			sync = probeSynchronizer;
			if (sync == null) {
				sync = probeSynchronizer = new RequestSynchronizer(this);

				sendDirectedProbe(xAddressInfo);
				ProbeMessage probe = new ProbeMessage(xAddressInfo.getComManId());
				synchronizers.put(probe.getMessageId(), sync);
				probe.setProtocolInfo(xAddressInfo.getProtocolInfo());
				probe.getHeader().setTo(new AttributedURI(WSDConstants.WSD_TO));
				probe.setTargetXAddressInfo(xAddressInfo);
				OutDispatcher.getInstance().send(probe, xAddressInfo, new DefaultDeviceReferenceCallback(xAddressInfo));
			}
		}

		while (true) {
			synchronized (sync) {
				while (sync.pending) {
					try {
						sync.wait();
					} catch (InterruptedException e) {
						Log.printStackTrace(e);
					}
				}

				if (sync.exception != null) {
					throw sync.exception;
				}
				/*
				 * else { this means we had a concurrent update and someone was
				 * started to obtain a newer device }
				 */
			}

			synchronized (this) {
				if (probeSynchronizer == UP_TO_DATE_PROBE_SYNCHRONIZER) {
					return;
				} else if (probeSynchronizer != null) {
					sync = probeSynchronizer;
				} else {
					throw new TimeoutException("Unknown communication error with device.");
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.service.reference.DeviceReference#
	 * fetchCompleteDiscoveryDataAsync()
	 */
	public void fetchCompleteDiscoveryDataAsync() {
		RequestSynchronizer sync;
		synchronized (this) {
			if (probeSynchronizer != null) {
				return;
			}
			sync = probeSynchronizer = new RequestSynchronizer(this);
		}
		fetchCompleteDiscoveryDataAsync(sync);
	}

	private void fetchCompleteDiscoveryDataAsync(final RequestSynchronizer newSynchronizer) {
		XAddressInfo xAddressInfo = null;
		synchronized (this) {
			if (probeSynchronizer != newSynchronizer) {
				return;
			}
			xAddressInfo = preferredXAddressInfo;
			if (xAddressInfo != null) {
				synchronizers.put(sendDirectedProbe(xAddressInfo).getMessageId(), newSynchronizer);
				return;
			}
		}

		// start new thread for resolving
		DPWSFramework.getThreadPool().execute(new Runnable() {

			public void run() {
				try {
					XAddressInfo xAddressInfo = resolveRemoteDevice();
					boolean callNotify = true;
					synchronized (DefaultDeviceReference.this) {
						if (newSynchronizer == probeSynchronizer) {
							synchronizers.put(sendDirectedProbe(xAddressInfo).getMessageId(), newSynchronizer);
							callNotify = false;
						}
					}
					if (callNotify) {
						synchronized (newSynchronizer) {
							newSynchronizer.pending = false;
							newSynchronizer.notifyAll();
						}
					}
				} catch (TimeoutException e) {
					Log.warn("Unablte to resolve remote device: " + e.getMessage());
				}
			}

		});
	}

	/**
	 * Sends Resolve message to remote device. ResolveMatches message will be
	 * handled by callback handler, which updates discovery data. Only one
	 * Resolve message will be sent each time.
	 * 
	 * @return preferred transport address of device and corresponding protocol
	 * @throws TimeoutException
	 */
	public XAddressInfo resolveRemoteDevice() throws TimeoutException {
		// we MUST NOT have locked DefaultDeviceReference.this up to now!
		ResolveRequestSynchronizer sync;
		synchronized (this) {
			sync = resolveSynchronizer;
			if (sync == UP_TO_DATE_RESOLVE_SYNCHRONIZER) {
				// means we have current discovery metadata from resolve
				if (preferredXAddressInfo == null) {
					/*
					 * fatal: resolve didn't provide us with actually usable
					 * addresses
					 */
					throw new TimeoutException("No usable transport addresses found!");
				}
				return preferredXAddressInfo;
			}

			if (sync == null) {
				sync = resolveSynchronizer = new ResolveRequestSynchronizer(this);
				synchronizers.put(sendResolve(preferredXAddressInfo).getMessageId(), sync);
			}
		}

		while (true) {
			synchronized (sync) {
				while (sync.pending) {
					try {
						sync.wait();
					} catch (InterruptedException e) {
						Log.printStackTrace(e);
					}
				}

				if (sync.exception != null) {
					throw sync.exception;
				} else if (sync.xAddressInfo != null) {
					return sync.xAddressInfo;
				}
				/*
				 * else { this means we had a concurrent update and someone was
				 * started to obtain a newer device }
				 */
			}

			synchronized (this) {
				if (preferredXAddressInfo != null) {
					return preferredXAddressInfo;
				}
				if (resolveSynchronizer == UP_TO_DATE_RESOLVE_SYNCHRONIZER) {
					if (preferredXAddressInfo == null) {
						/*
						 * fatal: resolve didn't provide us with actually usable
						 * addresses
						 */
						throw new TimeoutException("No usable transport addresses found!");
					}
					return preferredXAddressInfo;
				}
				if (resolveSynchronizer != null) {
					sync = resolveSynchronizer;
				} else {
					throw new TimeoutException("Unknown communication error with device.");
				}
			}
		}
	}

	private void resolveRemoteDeviceAsync(final ResolveRequestSynchronizer newSynchronizer) {
		XAddressInfo xAddressInfo = null;
		synchronized (this) {
			if (resolveSynchronizer != newSynchronizer) {
				return;
			}
			xAddressInfo = preferredXAddressInfo;
			if (xAddressInfo != null) {
				return;
			}
		}

		// start new thread for resolving
		DPWSFramework.getThreadPool().execute(new Runnable() {

			public void run() {
				boolean callNotify = true;
				synchronized (DefaultDeviceReference.this) {
					if (newSynchronizer != resolveSynchronizer) {
						synchronizers.put(sendResolve(preferredXAddressInfo).getMessageId(), newSynchronizer);
						callNotify = false;
					}
				}
				if (callNotify) {
					synchronized (newSynchronizer) {
						newSynchronizer.pending = false;
						newSynchronizer.notifyAll();
					}
				}
			}

		});
	}

	private GetMessage sendGet(XAddressInfo xAddressInfo) {
		GetMessage get = new GetMessage(xAddressInfo.getComManId());
		EndpointReference epr = getEndpointReference();
		get.setProtocolInfo(xAddressInfo.getProtocolInfo());
		// TODO Credentials in GETMessage

		/*
		 * we set the wsa:to property to the EPR address (usually a URN) of this
		 * device instead of to an xAddress of that device
		 */
		get.getHeader().setEndpointReference(epr);
		get.setTargetXAddressInfo(xAddressInfo);

		OutDispatcher.getInstance().send(get, xAddressInfo, new DefaultDeviceReferenceCallback(xAddressInfo));
		return get;
	}

	private ProbeMessage sendDirectedProbe(XAddressInfo xAddressInfo) {
		ProbeMessage probe = new ProbeMessage(xAddressInfo.getComManId());
		probe.setProtocolInfo(xAddressInfo.getProtocolInfo());
		probe.getHeader().setTo(new AttributedURI(WSDConstants.WSD_TO));
		probe.setTargetXAddressInfo(xAddressInfo);

		OutDispatcher.getInstance().send(probe, xAddressInfo, new DefaultDeviceReferenceCallback(xAddressInfo));
		return probe;
	}

	private ResolveMessage sendResolve(XAddressInfo xAddressInfo) {
		// Send resolve to discover xAddress(es)
		ResolveMessage resolve = new ResolveMessage(CommunicationManager.ID_NULL);
		EndpointReference epr = getEndpointReference();
		// we dont know the version, if we send an resolve
		// resolve.setProtocolInfo(xAddressInfo.getProtocolVersionInfo());
		resolve.setEndpointReference(epr);
		OutDispatcher.getInstance().send(resolve, xAddressInfo, Discovery.getDefaultOutputDomains(), new DefaultDeviceReferenceCallback(xAddressInfo));
		return resolve;
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.service.reference.Reference#getLocation()
	 */
	public int getLocation() {
		return location;
	}

	// --------------------- DISCOVERY DATA -----------------------

	/*
	 * (non-Javadoc)
	 * @see
	 * org.ws4d.java.service.reference.DeviceReference#getDevicePortTypes(boolean
	 * )
	 */
	public Iterator getDevicePortTypes(boolean doDiscovery) throws TimeoutException {
		synchronized (this) {
			if (doDiscovery) {
				doDiscovery = (discoveryData.getTypes() == null || discoveryData.getTypes().size() == 0) && preferredXAddressInfo == null;
			}
		}
		if (doDiscovery) {
			resolveRemoteDevice();
		}

		QNameSet types = discoveryData.getTypes();
		return types == null ? EmptyStructures.EMPTY_ITERATOR : new ReadOnlyIterator(types.iterator());
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.ws4d.java.service.reference.DeviceReference#getDevicePortTypesAsArray
	 * (boolean)
	 */
	public QName[] getDevicePortTypesAsArray(boolean doDiscovery) throws TimeoutException {
		synchronized (this) {
			if (doDiscovery) {
				doDiscovery = (discoveryData.getTypes() == null || discoveryData.getTypes().size() == 0) && preferredXAddressInfo == null;
			}
		}
		if (doDiscovery) {
			resolveRemoteDevice();
		}

		QNameSet types = discoveryData.getTypes();
		return types == null ? (QName[]) EmptyStructures.EMPTY_OBJECT_ARRAY : types.toArray();
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.service.reference.DeviceReference#getScopes(boolean)
	 */
	public Iterator getScopes(boolean doDiscovery) throws TimeoutException {
		synchronized (this) {
			if (doDiscovery) {
				doDiscovery = (discoveryData.getScopes() == null || discoveryData.getScopes().size() == 0) && preferredXAddressInfo == null;
			}
		}
		if (doDiscovery) {
			resolveRemoteDevice();
		}

		ScopeSet scopes = discoveryData.getScopes();
		URISet uriScopes = (scopes == null) ? null : scopes.getScopesAsUris();
		return (uriScopes == null) ? EmptyStructures.EMPTY_ITERATOR : new ReadOnlyIterator(uriScopes.iterator());
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.ws4d.java.service.reference.DeviceReference#getScopesAsArray(boolean)
	 */
	public URI[] getScopesAsArray(boolean doDiscovery) throws TimeoutException {
		synchronized (this) {
			if (doDiscovery) {
				doDiscovery = (discoveryData.getScopes() == null || discoveryData.getScopes().size() == 0) && preferredXAddressInfo == null;
			}
		}
		if (doDiscovery) {
			resolveRemoteDevice();
		}

		ScopeSet scopes = discoveryData.getScopes();
		URISet uriScopes = (scopes == null) ? null : scopes.getScopesAsUris();
		return (uriScopes == null) ? (URI[]) EmptyStructures.EMPTY_OBJECT_ARRAY : uriScopes.toArray();
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.ws4d.java.service.reference.DeviceReference#getMetadataVersion(boolean
	 * )
	 */
	public long getMetadataVersion(boolean doDiscovery) throws TimeoutException {
		synchronized (this) {
			if (doDiscovery) {
				doDiscovery = discoveryData.getMetadataVersion() == DiscoveryData.UNKNOWN_METADATA_VERSION && preferredXAddressInfo == null;
			}
		}
		if (doDiscovery) {
			resolveRemoteDevice();
		}

		return discoveryData.getMetadataVersion();
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.ws4d.java.service.reference.DeviceReference#getXAddresses(boolean)
	 */
	public Iterator getXAddressInfos(boolean doDiscovery) throws TimeoutException {
		synchronized (this) {
			if (doDiscovery) {
				doDiscovery = (discoveryData.getXAddressInfoSet() == null || discoveryData.getXAddressInfoSet().size() == 0) && preferredXAddressInfo == null;
			}
		}
		if (doDiscovery) {
			resolveRemoteDevice();
		}

		XAddressInfoSet xAddrs = discoveryData.getXAddressInfoSet();
		return xAddrs == null ? EmptyStructures.EMPTY_ITERATOR : new ReadOnlyIterator(xAddrs.iterator());
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.service.reference.Reference#getPreferredXAddress()
	 */
	public synchronized URI getPreferredXAddress() {
		return preferredXAddressInfo == null ? null : preferredXAddressInfo.getXAddress();
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.ws4d.java.service.reference.Reference#getPreferredXAddressProtocol()
	 */
	public synchronized String getPreferredCommunicationManagerID() {
		return preferredXAddressInfo == null ? CommunicationManager.ID_NULL : preferredXAddressInfo.getComManId();
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.ws4d.java.service.reference.DeviceReference#getEndpointReference()
	 */
	public EndpointReference getEndpointReference() {
		return discoveryData.getEndpointReference();
	}

	/**
	 * Give the customize metadata
	 * 
	 * @return String which contains the user added metadata.
	 */
	public String getCustomMData() {
		return customMData;
	}

	/**
	 * Set the customize metadata
	 * 
	 * @param customMData String which contains the new customize metadata.
	 */
	public void setCustomMData(String customMData) {
		this.customMData = customMData;
	}

	// -----------------------------------------------------------------

	/**
	 * Checks if the specified application sequence is newer than the latest. If
	 * the specified sequence is newer, the latest sequence replaced by the
	 * newer and <code>true</code> will be returned. If new sequence is
	 * <code>null</code>, method returns <code>true</code>;
	 * 
	 * @param newSequence
	 * @return <code>true</code>, if the specified sequence is newer than the
	 *         latest sequence or if new sequence is null.
	 */
	protected synchronized boolean checkAppSequence(AppSequence appSeq) {
		if (location == LOCATION_LOCAL) {
			Log.error("DefaultDeviceReference.checkAppSequence is not available for local devices.");
			throw new RuntimeException("checkAppSequence is not available for local devices!");
		}

		return appSequenceTracker.checkAndUpdate(appSeq);
	}

	protected synchronized int changeProxyReferenceState(int event) {
		return proxyReferenceState.transit(event);
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.management.TimedEntry#timedOut()
	 */
	protected void timedOut() {
		// Reference has no listeners, WatchDog timed out, delete this.
		listeners.sharedLock();
		try {
			if (listeners.size() == 0 && location != LOCATION_LOCAL) {
				// nobody needs this reference => remove it from registry
				if (discoveryData.getEndpointReference() != null) {
					DeviceServiceRegistry.unregisterDeviceReference(this);
				}
			}
			// else {
			// Log.error("Temporary DeviceReference timed out! " +
			// "Listener counter should be 0, was " + listeners.size());
			// }
		} finally {
			listeners.releaseSharedLock();
		}
	}

	// ------------------ LISTENERS--------------------------

	/*
	 * (non-Javadoc)
	 * @see
	 * org.ws4d.java.service.reference.DeviceReference#addListener(org.ws4d.
	 * java.service.reference.DeviceListener)
	 */
	public void addListener(DeviceListener listener) {
		if (listener == null) {
			return;
		}
		listeners.exclusiveLock();
		try {
			if (listeners.size() == 0 && location != LOCATION_LOCAL) {
				// only remote devices may have been registered
				WatchDog.getInstance().unregister(this);
			}
			if (listeners.containsKey(listener)) {
				// no need to create new listener queue
				return;
			}
			listeners.put(listener, new DeviceListenerQueue(listener, this));
		} finally {
			listeners.releaseExclusiveLock();
		}
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.ws4d.java.service.reference.DeviceReference#removeListener(org.ws4d
	 * .java.service.reference.DeviceListener)
	 */
	public void removeListener(DeviceListener listener) {
		if (listener == null) {
			return;
		}
		listeners.exclusiveLock();
		try {
			listeners.remove(listener);
			if (listeners.size() == 0 && location != LOCATION_LOCAL) {
				// only remote device will be removed
				WatchDog.getInstance().register(this, DispatchingProperties.getInstance().getReferenceCachingTime());
			}
		} finally {
			listeners.releaseExclusiveLock();
		}
	}

	/**
	 * Returns amount of listeners for this reference.
	 * 
	 * @return Amount of listeners for this reference.
	 */
	protected int sizeOfListeners() {
		listeners.sharedLock();
		try {
			return listeners.size();
		} finally {
			listeners.releaseSharedLock();
		}
	}

	/**
	 * Set the preferredProtocolVersion to the DPWSVersion of the DPWSProperties
	 * if just one is defined, else it will set to "null" for both.
	 */
	private void setPreferredVersion(String communicationManagerId) {
		CommunicationManager comMan = DPWSFramework.getCommunicationManager(communicationManagerId);
		HashSet supportedVersions = comMan.getSupportedVersions();
		if (supportedVersions.size() == 1) {
			if (preferredXAddressInfo == null) {
				preferredXAddressInfo = new XAddressInfo();
			}
			Integer ver = (Integer) (supportedVersions.toArray()[0]);
			preferredXAddressInfo.setProtocolInfo(comMan.createProtocolInfo(ver.intValue()));
		}
	}

	/**
	 * @param address the address to set
	 * @param comManId the protocol ID the new preferred XAddress belongs to
	 */
	private void setPreferredXAddress(DiscoveryData data, ProtocolData protocolData) {
		/*
		 * store address for target device within the device reference (handler)
		 */
		XAddressInfoSet xAddresses = data.getXAddressInfoSet();

		if (xAddresses == null || xAddresses.size() == 0) {
			return;
		}

		if (preferredXAddressInfo != null && xAddresses.contains(preferredXAddressInfo)) {
			return;
		}

		for (Iterator it = xAddresses.iterator(); it.hasNext();) {
			XAddressInfo xAddr = (XAddressInfo) it.next();
			if (protocolData.sourceMatches(xAddr.getXAddress())) {
				preferredXAddressInfo = xAddr;
				return;
			}
		}
		// take the first (or any other) one
		preferredXAddressInfo = (XAddressInfo) xAddresses.iterator().next();
	}

	public boolean isDeviceObjectExisting() {
		return device != null;
	}

	public boolean isDiscovered() {
		if (location == LOCATION_LOCAL) {
			return true;
		}

		return discoveryData.getMetadataVersion() == DiscoveryData.UNKNOWN_METADATA_VERSION ? false : true;
	}

	public synchronized boolean isCompleteDiscovered() {
		return probeSynchronizer == UP_TO_DATE_PROBE_SYNCHRONIZER;
	}

	public synchronized void setAutoUpdateDevice(boolean autoUpdateDevice) {
		this.autoUpdateDevice = autoUpdateDevice;
	}

	public synchronized boolean isAutoUpdateDevice() {
		return autoUpdateDevice;
	}

	synchronized void disconnectAllServiceReferences(boolean resetServiceRefs) {
		if (device == null) {
			return;
		}
		Iterator servRefs = device.getServiceReferences();
		while (servRefs.hasNext()) {
			ServiceReferenceInternal servRef = (ServiceReferenceInternal) servRefs.next();
			servRef.disconnectFromDevice();
			if (resetServiceRefs) {
				servRef.reset();
			}
		}
	}

	/**
	 * Updates discovery data of device reference (hence the discovery data of
	 * the proxy device).
	 * 
	 * @param data new discovery data to check old data against.
	 * @return true if endpoint reference was set.
	 */
	private boolean updateDiscoveryData(HostMData host) {
		if (location == LOCATION_LOCAL) {
			throw new RuntimeException("Updating Discovery Data for a local device is prohibited outside of the device");
		}

		if (host == null) {
			return false;
		}

		QNameSet types = discoveryData.getTypes();
		if (types == null) {
			types = new QNameSet(host.getTypes());
		} else {
			types.addAll(host.getTypes());
		}
		if (discoveryData.getEndpointReference() == null) {
			discoveryData.setEndpointReference(host.getEndpointReference());
			return true;
		}

		return false;
	}

	synchronized void updateFromHello(HelloMessage hello, ProtocolData protocolData) {
		if (checkAppSequence(hello.getAppSequence())) {
			if (Log.isInfo()) {
				Log.info("Set DPWS Version for " + getEndpointReference().toString() + " to : " + protocolData.getProtocolInfo().getDisplayName());
			}
			setSecureDevice(hello.getHeader().getSignature() != null);

			updateDiscoveryData(hello.getDiscoveryData(), protocolData);
		} else if (Log.isDebug()) {
			Log.debug("DefaultDeviceReference.updateFromHello: old AppSequence in HelloMessage (msgId = " + hello.getMessageId() + ")", Log.DEBUG_LAYER_FRAMEWORK);
		}
	}

	synchronized void updateFromBye(ByeMessage bye, ProtocolData protocolData) {
		if (checkAppSequence(bye.getAppSequence())) {
			preferredXAddressInfo = null;
			resolveSynchronizer = null;
			changeProxyReferenceState(DefaultDeviceReference.EVENT_DEVICE_BYE);
		} else if (Log.isDebug()) {
			Log.debug("DefaultDeviceReference.updateFromBye: old AppSequence in ByeMessage (msgId = " + bye.getMessageId() + ")", Log.DEBUG_LAYER_FRAMEWORK);
		}
	}

	/**
	 * Updates discovery data. Only used for references to remote devices.
	 * Returns <code>true</code>, if metadata version of the new discovery data
	 * is newer. Preferred xaddress will be set if unset or metadata version is
	 * newer. Changes proxy reference state.
	 * 
	 * @param newData
	 * @param protocolData
	 * @return <code>true</code>, if metadata version of the new discovery data
	 *         is newer.
	 */
	boolean updateDiscoveryData(DiscoveryData newData, ProtocolData protocolData) {
		GetRequestSynchronizer newGetSynchronizer = null;
		RequestSynchronizer newProbeSynchronizer = null;
		ResolveRequestSynchronizer newResolveSynchronizer = null;
		boolean updated = false;
		synchronized (this) {
			// handler.communicationState = CallbackHandler.COM_OK;

			XAddressInfoSet xAddressInfoSet = discoveryData.getXAddressInfoSet();
			if (xAddressInfoSet != null) {
				xAddressInfoSet.mergeProtocolInfo(protocolData.getProtocolInfo());
			}

			updated = discoveryData.update(newData);
			if (updated) {
				if (getSynchronizer != null) {
					if (getSynchronizer != UP_TO_DATE_GET_SYNCHRONIZER) {
						getSynchronizer = newGetSynchronizer = new GetRequestSynchronizer(this);
					} else {
						getSynchronizer = null;
					}
				}

				if (probeSynchronizer != null) {
					if (probeSynchronizer != UP_TO_DATE_PROBE_SYNCHRONIZER) {
						probeSynchronizer = newProbeSynchronizer = new RequestSynchronizer(this);
					} else {
						probeSynchronizer = null;
					}
				}
			}

			setPreferredXAddress(newData, protocolData);
			if (preferredXAddressInfo == null) {
				if (resolveSynchronizer != null) {
					if (resolveSynchronizer != UP_TO_DATE_RESOLVE_SYNCHRONIZER) {
						resolveSynchronizer = newResolveSynchronizer = new ResolveRequestSynchronizer(this);
					} else {
						resolveSynchronizer = null;
					}
				}
			}

			if (updated) {
				if (autoUpdateDevice) {
					buildUpDevice();
				}
				/*
				 * We do not remove the service from framework, we only remove
				 * association to the device.
				 */
				changeProxyReferenceState(EVENT_DEVICE_CHANGED);
			} else {
				changeProxyReferenceState(EVENT_DEVICE_SEEN);
			}
		}
		if (newResolveSynchronizer != null) {
			resolveRemoteDeviceAsync(newResolveSynchronizer);
		}
		if (newGetSynchronizer != null) {
			buildUpDevice(newGetSynchronizer);
		}
		if (newProbeSynchronizer != null) {
			fetchCompleteDiscoveryDataAsync(newProbeSynchronizer);
		}
		return updated;
	}

	private void announceDeviceListenerEvent(byte eventType, Device device) {
		listeners.sharedLock();
		try {
			DeviceEvent event = new DeviceEvent(eventType, device);
			for (Iterator it = listeners.values().iterator(); it.hasNext();) {
				DeviceListenerQueue queue = (DeviceListenerQueue) it.next();
				queue.announce(event);
			}
		} finally {
			listeners.releaseSharedLock();
		}
	}

	/**
	 * Informs listeners on this device reference in separate thread about
	 * change.
	 */
	private void announceDeviceRunning() {
		announceDeviceListenerEvent(DeviceListenerQueue.DEVICE_RUNNING_EVENT, null);
	}

	/**
	 * Informs listeners on this device reference in separate thread about
	 * change.
	 */
	private void announceDeviceCompletelyDiscovered() {
		announceDeviceListenerEvent(DeviceListenerQueue.DEVICE_COMPLETELY_DISCOVERED_EVENT, null);
	}

	/**
	 * Informs listeners on this device reference in separate thread about
	 * change.
	 */
	private void announceDeviceBuildUp() {
		announceDeviceListenerEvent(DeviceListenerQueue.DEVICE_BUILT_UP_EVENT, device);
	}

	/**
	 * Informs listeners on this device reference in separate thread about stop.
	 */
	public void announceDeviceBye() {
		announceDeviceListenerEvent(DeviceListenerQueue.DEVICE_BYE_EVENT, null);
	}

	/**
	 * Informs listeners on this device reference in separate thread about
	 * change.
	 */
	private void announceDeviceChanged() {
		announceDeviceListenerEvent(DeviceListenerQueue.DEVICE_CHANGED_EVENT, null);
	}

	/**
	 * Informs listeners on this device reference in separate thread about
	 * change.
	 */
	private void announceDeviceCommunicationErrorOrReset() {
		announceDeviceListenerEvent(DeviceListenerQueue.DEVICE_COMMUNICATION_ERROR_OR_RESET_EVENT, null);
	}

	/**
	 * Clones device listeners and informs everyone in separate thread about
	 * change.
	 */
	public void announceDeviceChangedAndBuildUp() {
		announceDeviceListenerEvent(DeviceListenerQueue.DEVICE_CHANGED_AND_BUILT_UP_EVENT, device);
	}

	/**
	 * Clones device listeners and informs everyone in separate thread about
	 * change.
	 */
	public void announceDeviceRunningAndBuildUp() {
		announceDeviceListenerEvent(DeviceListenerQueue.DEVICE_RUNNING_AND_BUILT_UP_EVENT, device);
	}

	private class DefaultDeviceReferenceCallback extends DefaultResponseCallback {

		DefaultDeviceReferenceCallback(XAddressInfo targetXAddressInfo) {
			super(targetXAddressInfo);
		}

		/*
		 * (non-Javadoc)
		 * @see
		 * org.ws4d.java.communication.DefaultResponseCallback#handle(org.ws4d
		 * .java.communication.message.Message,
		 * org.ws4d.java.message.discovery.ProbeMatchesMessage,
		 * org.ws4d.java.communication.ProtocolData)
		 */
		public void handle(Message request, ProbeMatchesMessage response, ProtocolData protocolData) {
			/*
			 * handles directed Probe
			 */
			RequestSynchronizer sync = null;
			try {
				synchronized (DefaultDeviceReference.this) {
					location = LOCATION_REMOTE;

					// set the preferred
					protocolData.setProtocolInfo(response.getProtocolInfo());

					checkAppSequence(response.getAppSequence());
					sync = (RequestSynchronizer) synchronizers.remove(request.getMessageId());
					if (sync == null) {
						/*
						 * this shouldn't ever happen, as it would mean we
						 * receive a response to a request we never sent...
						 */
						Log.warn("DefaultDeviceReference: ignoring unexpected ProbeMatches message " + response);
						return;
					}

					long currentMetadataVersion = discoveryData.getMetadataVersion();
					if (sync.metadataVersion == currentMetadataVersion) {
						setSecureDevice(response.getHeader().getSignature() != null);

						DataStructure data = response.getProbeMatches();
						boolean matched = false;
						for (Iterator it = data.iterator(); it.hasNext();) {
							ProbeMatch match = (ProbeMatch) it.next();
							if (discoveryData.getEndpointReference().equals(match.getEndpointReference())) {
								matched = true;
								updateDiscoveryData(match, protocolData);
								break;
							}
						}

						if (matched) {
							if (sync == probeSynchronizer) {
								probeSynchronizer = UP_TO_DATE_PROBE_SYNCHRONIZER;
								changeProxyReferenceState(EVENT_DEVICE_COMPLETELY_DISCOVERED);
							}
						} else {
							sync.exception = new TimeoutException("No matching endpoint reference in directed probe result found!");
							if (sync == probeSynchronizer) {
								// make next call create a new directed probe
								probeSynchronizer = null;
							}
						}

					} else {
						sync.exception = new TimeoutException("Device update detected while probing device directly");
					}
					/*
					 * don't make any changes on this devRef if the response is
					 * outdated!
					 */
				}
			} catch (Throwable e) {
				sync.exception = new TimeoutException("Unexpected exception during probe matches processing: " + e);
			}

			synchronized (sync) {
				sync.pending = false;
				sync.notifyAll();
			}
		}

		/*
		 * (non-Javadoc)
		 * @see
		 * org.ws4d.java.communication.DefaultResponseCallback#handle(org.ws4d
		 * .java.communication.message.Message,
		 * org.ws4d.java.message.discovery.ResolveMatchesMessage,
		 * org.ws4d.java.communication.ProtocolData)
		 */
		public void handle(Message request, ResolveMatchesMessage response, ProtocolData protocolData) {
			// XXX Discovery Proxy handling not implemented
			ResolveRequestSynchronizer sync = null;

			try {
				synchronized (DefaultDeviceReference.this) {
					location = LOCATION_REMOTE;
					protocolData.setProtocolInfo(response.getProtocolInfo());
					boolean appSequenceCheckPassed = checkAppSequence(response.getAppSequence());

					sync = (ResolveRequestSynchronizer) synchronizers.remove(request.getMessageId());
					if (sync == null) {
						/*
						 * this shouldn't ever happen, as it would mean we
						 * receive a response to a request we never sent...
						 */
						Log.warn("DefaultDeviceReference: ignoring unexpected ResolveMatches message " + response);
						return;
					}

					if (appSequenceCheckPassed) {
						long currentMetadataVersion = discoveryData.getMetadataVersion();
						if (sync.metadataVersion == currentMetadataVersion) {
							setSecureDevice(response.getHeader().getSignature() != null);

							DiscoveryData newDiscoData = response.getResolveMatch();
							updateDiscoveryData(newDiscoData, protocolData);
							sync.xAddressInfo = preferredXAddressInfo;
						}

						if (sync == resolveSynchronizer) {
							resolveSynchronizer = UP_TO_DATE_RESOLVE_SYNCHRONIZER;
						}
					} else if (Log.isDebug()) {
						Log.debug("DefaultDeviceReference.handle: old AppSequence in ResolveMatches message (msgId = " + response.getMessageId() + ")", Log.DEBUG_LAYER_FRAMEWORK);
					}
				}
			} catch (Throwable e) {
				sync.exception = new TimeoutException("Unexpected exception during resolve matches processing: " + e);
			}

			synchronized (sync) {
				sync.pending = false;
				sync.notifyAll();
			}
		}

		/*
		 * (non-Javadoc)
		 * @see
		 * org.ws4d.java.communication.DefaultResponseCallback#handle(org.ws4d
		 * .java.communication.message.Message, org.ws4d.java.message
		 * .metadataexchange.GetResponseMessage,
		 * org.ws4d.java.communication.ProtocolData)
		 */
		public void handle(Message request, GetResponseMessage response, ProtocolData protocolData) {
			GetRequestSynchronizer sync = null;
			try {
				synchronized (DefaultDeviceReference.this) {
					// Only message from remote devices are handled
					if (location == LOCATION_LOCAL) {
						// may occur if location of dev ref was unknown before
						return;
					}
					location = LOCATION_REMOTE;

					// update devRef data and schedule listener notifications
					protocolData.setProtocolInfo(response.getProtocolInfo());

					sync = (GetRequestSynchronizer) synchronizers.remove(request.getMessageId());
					if (sync == null) {
						/*
						 * this shouldn't ever happen, as it would mean we
						 * receive a response to a request we never sent...
						 */
						Log.warn("DefaultDeviceReference: ignoring unexpected GetResponse message " + response);
						return;
					}

					long currentMetadataVersion = discoveryData.getMetadataVersion();
					if (sync.metadataVersion == currentMetadataVersion) {
						setCustomMData(response.getCustomMdata());
						updateDiscoveryData(response.getHost());

						ProxyFactory pFac = DPWSFramework.getProxyFactory();
						boolean doChangeState = false;
						if (device == null) {
							// device was not build up until now
							device = pFac.createProxyDevice(response, DefaultDeviceReference.this, null, protocolData);
							doChangeState = true;
						} else if (!device.isValid()) {

							// device has been changed, create new device
							device = pFac.createProxyDevice(response, DefaultDeviceReference.this, device, protocolData);
							doChangeState = true;
						}

						if (doChangeState) {
							changeProxyReferenceState(EVENT_DEVICE_GET_RSP);
						}

						sync.device = device;

						if (sync == getSynchronizer) {
							// this was the only currently pending get request
							getSynchronizer = UP_TO_DATE_GET_SYNCHRONIZER;
						}
					} else {
						if (Log.isDebug()) {
							Log.debug("Concurrent device update detected, rebuilding device proxy", Log.DEBUG_LAYER_FRAMEWORK);
						}

						// sync.exception = new
						// TimeoutException("Device update detected while trying to build up device");
					}
				}
			} catch (Throwable e) {
				sync.exception = new TimeoutException("Unexpected exception during get response processing: " + e);
			}

			synchronized (sync) {
				sync.pending = false;
				sync.notifyAll();
			}
		}

		/*
		 * (non-Javadoc)
		 * @see
		 * org.ws4d.java.communication.DefaultResponseCallback#handle(org.ws4d
		 * .java.communication.message.Message,
		 * org.ws4d.java.message.FaultMessage,
		 * org.ws4d.java.communication.ProtocolData)
		 */
		public void handle(Message request, FaultMessage fault, ProtocolData protocolData) {
			boolean noMoreXAddrs = false;
			boolean unauthorized = false;
			RequestSynchronizer sync = null;
			try {
				synchronized (DefaultDeviceReference.this) {
					protocolData.setProtocolInfo(fault.getProtocolInfo());

					sync = (RequestSynchronizer) synchronizers.remove(request.getMessageId());
					if (sync == null) {
						/*
						 * this shouldn't ever happen, as it would mean we
						 * receive a response to a request we never sent...
						 */
						Log.warn("DefaultDeviceReference.handle(FaultMessage): no synchronizer found for request message " + request);
						return;
					}

					Log.warn("Fault returned for xaddress " + request.getTargetAddress() + ": " + fault);

					if (sync == resolveSynchronizer) {
						resolveSynchronizer = null;
					} else if (sync == probeSynchronizer) {
						try {
							// try to retransmit the message to another
							// XAddress!

							if (retransmitRequest(request)) {
								return;
							}
						} catch (NoSuchElementException e) {
							noMoreXAddrs = true;
						}

						probeSynchronizer = null;
					} else if (sync == getSynchronizer) {

						// check fault code
						if (fault.getSubcode().equals(WSSecurityConstants.WSSE_FAULT_AUTHENTICATION)) {
							unauthorized = true;
						} else {

							try {
								// try to retransmit the message to another
								// XAddress!

								if (retransmitRequest(request)) {
									return;
								}
							} catch (NoSuchElementException e) {
								noMoreXAddrs = true;
							}
						}
						getSynchronizer = null;
					}

					synchronizers.remove(request.getMessageId());

					changeProxyReferenceState(EVENT_DEVICE_FAULT_RESET);
				}
			} catch (Throwable e) {
				Log.warn("Unexpected exception during fault processing: " + e);
			}

			synchronized (sync) {

				if (noMoreXAddrs) {
					sync.exception = new TimeoutException("No further xaddress to communicate with.");
				} else if (unauthorized) {
					sync.exception = new TimeoutException("Authorization Required.");
				} else {
					switch (request.getType()) {
						case (DPWSMessageConstants.PROBE_MESSAGE): {
							sync.exception = new TimeoutException("Device send fault, probably doesn't support directed probing: " + fault);
							break;
						}
						default: {
							sync.exception = new TimeoutException("Device send fault, probably WSDAPI Device: " + fault);
						}
					}
				}
				sync.pending = false;
				sync.notifyAll();
			}

			if (Log.isDebug()) {
				URI msgId = fault.getRelatesTo();
				Log.debug("DefaultDeviceReference.CallbackHandler.receivedFault: get, msgId = " + msgId, Log.DEBUG_LAYER_FRAMEWORK);
			}
		}

		/*
		 * (non-Javadoc)
		 * @see org.ws4d.java.communication.DefaultResponseCallback#
		 * handleMalformedResponseException(org.ws4d.java.message.Message,
		 * java.lang.Exception, org.ws4d.java.communication.ProtocolData)
		 */
		public void handleMalformedResponseException(Message request, Exception exception, ProtocolData protocolData) {
			// same as for timeouts, but we additionally log the exception
			Log.warn("DefaultDeviceReference.handleMalformedResponseException: malformed response exception: " + exception + ". Request was: " + request);
			handleMalformedResponseOrTimeout(request, "handleMalformedResponseException");
		}

		/*
		 * (non-Javadoc)
		 * @see org.ws4d.java.communication.DefaultResponseCallback#
		 * handleTransmissionException(org.ws4d.java.message.Message,
		 * java.lang.Exception, org.ws4d.java.communication.ProtocolData)
		 */
		public void handleTransmissionException(Message request, Exception exception, ProtocolData protocolData) {
			boolean noMoreXAddrs = false;
			RequestSynchronizer sync = null;
			try {
				synchronized (DefaultDeviceReference.this) {
					sync = (RequestSynchronizer) synchronizers.get(request.getMessageId());
					if (sync == null) {
						/*
						 * this shouldn't ever happen, as it would mean we
						 * receive a response to a request we never sent...
						 */
						Log.warn("DefaultDeviceReference.handleTransmissionException: no synchronizer found for request message " + request);
						return;
					}

					Log.warn("Transmission error with xaddress " + request.getTargetAddress() + ": " + exception);

					if (sync == resolveSynchronizer) {
						resolveSynchronizer = null;
					} else if (sync == getSynchronizer || sync == probeSynchronizer) {
						try {
							// try to retransmit the message to another
							// XAddress!
							if (retransmitRequest(request)) {
								return;
							}
						} catch (NoSuchElementException e) {
							noMoreXAddrs = true;
						}

						if (sync == getSynchronizer) {
							getSynchronizer = null;
						} else {
							probeSynchronizer = null;
						}
					}

					synchronizers.remove(request.getMessageId());

					changeProxyReferenceState(EVENT_DEVICE_FAULT_RESET);
				}
			} catch (Throwable e) {
				Log.warn("Unexpected exception: " + e);
			}

			synchronized (sync) {
				if (noMoreXAddrs) {
					sync.exception = new TimeoutException("No further xaddress to communicate with.");
				} else {
					sync.exception = new TimeoutException("Unable to send request message " + request);
				}
				sync.pending = false;
				sync.notifyAll();
			}
		}

		/*
		 * (non-Javadoc)
		 * @see
		 * org.ws4d.java.dispatch.ResponseCallback#receivedTimeout(org.ws4d.java
		 * .data.uri.URI)
		 */
		public void handleTimeout(Message request) {
			// timeout of messages send
			handleMalformedResponseOrTimeout(request, "handleTimeout");
		}

		private void handleMalformedResponseOrTimeout(Message request, String methodName) {
			boolean noMoreXAddrs = false;
			RequestSynchronizer sync = null;
			try {
				synchronized (DefaultDeviceReference.this) {
					sync = (RequestSynchronizer) synchronizers.remove(request.getMessageId());
					if (sync == null) {
						if (request.getType() == DPWSMessageConstants.RESOLVE_MESSAGE) {
							/*
							 * it is normal to receive timeouts from some
							 * resolve messages as we usually send resolve
							 * request over multiple network cards and IP
							 * addresses and some of them may be left unanswered
							 */
							if (Log.isDebug()) {
								Log.debug("DefaultDeviceReference." + methodName + ": no synchronizer found for request message " + request, Log.DEBUG_LAYER_FRAMEWORK);
							}
						} else {
							Log.warn("DefaultDeviceReference." + methodName + ": no synchronizer found for request message " + request);
						}
						return;
					}

					if (sync == resolveSynchronizer) {
						resolveSynchronizer = null;
					} else if (sync == getSynchronizer || sync == probeSynchronizer) {
						try {
							// try to retransmit the message to another
							// XAddress!
							if (retransmitRequest(request)) {
								return;
							}
						} catch (NoSuchElementException e) {
							noMoreXAddrs = true;
						}

						if (sync == getSynchronizer) {
							getSynchronizer = null;
						} else {
							probeSynchronizer = null;
						}
					}

					synchronizers.remove(request.getMessageId());

					changeProxyReferenceState(EVENT_DEVICE_FAULT_RESET);
				}
			} catch (Throwable e) {
				Log.warn("Unexpected exception: " + e);
			}

			synchronized (sync) {
				if (noMoreXAddrs) {
					sync.exception = new TimeoutException("No further xaddress to communicate with.");
				} else {
					sync.exception = new TimeoutException("Device state unknown, probably offline");
				}
				sync.pending = false;
				sync.notifyAll();
			}
		}

		/**
		 * @param request
		 * @param noMoreXAddrs
		 * @return
		 */
		private boolean retransmitRequest(Message request) {
			XAddressInfo failedXAddress = request.getTargetXAddressInfo();
			XAddressInfoSet xaddresses = discoveryData.getXAddressInfoSet();
			xaddresses.remove(failedXAddress);
			if (xaddresses.size() == 0) {
				preferredXAddressInfo = null;
				resolveSynchronizer = null;
			} else {
				if (preferredXAddressInfo != null && failedXAddress.equals(preferredXAddressInfo)) {
					preferredXAddressInfo = (XAddressInfo) xaddresses.iterator().next();

					// retransmit original request message!
					request.setTargetXAddressInfo(preferredXAddressInfo);
					if (request.getType() == DPWSMessageConstants.PROBE_MESSAGE) {
						OutDispatcher.getInstance().send((ProbeMessage) request, preferredXAddressInfo, this);
						return true;
					} else if (request.getType() == DPWSMessageConstants.GET_MESSAGE) {
						OutDispatcher.getInstance().send((GetMessage) request, preferredXAddressInfo, this);
						return true;
					} else {
						// shouldn't ever happen
						throw new IllegalArgumentException("Unable to retransmit unrecognized message type: " + request);
					}
				}
			}
			return false;
		}

	}

	private class StateManager {

		private int	state	= STATE_UNKNOWN;

		private void setState(int state) {
			this.state = state;
		}

		private int getState() {
			return state;
		}

		private int transit(int event) {
			if (location == LOCATION_LOCAL) {
				throw new RuntimeException("Use of StateManager is dedicated to proxy devices!");
			}

			/*
			 * change state and announce state change
			 */
			switch (getState()) {
				case STATE_UNKNOWN:
					changeUnknownState(event);
					break;

				case STATE_RUNNING:
					changeRunningState(event);
					break;

				case STATE_BUILD_UP:
					changeBuildUpState(event);
					break;

				case STATE_STOPPED:
					changeStoppedState(event);
					break;
			}

			switch (event) {
				case EVENT_DEVICE_CHANGED:
					probeSynchronizer = null;
					if (device != null) {
						device.invalidate();
					}
					// removeDeviceServiceAssociation();
					// device = null;
					break;
				case EVENT_DEVICE_COMPLETELY_DISCOVERED:
					announceDeviceCompletelyDiscovered();
					break;
			}

			return getState();
		}

		private void changeUnknownState(int event) {
			switch (event) {
				case EVENT_DEVICE_BYE:
					setState(STATE_STOPPED);
					announceDeviceBye();
					break;

				case EVENT_DEVICE_CHANGED:
					setState(STATE_RUNNING);
					announceDeviceRunning();
					break;

				case EVENT_DEVICE_GET_RSP:
					setState(STATE_BUILD_UP);
					announceDeviceBuildUp();
					break;

				case EVENT_DEVICE_SEEN:
					setState(STATE_RUNNING);
					announceDeviceRunning();
					break;
			}
		}

		private void changeRunningState(int event) {
			switch (event) {
				case EVENT_DEVICE_BYE:
					setState(STATE_STOPPED);
					announceDeviceBye();
					break;

				case EVENT_DEVICE_CHANGED:
					// state: running => running
					announceDeviceChanged();
					break;

				case EVENT_DEVICE_GET_RSP:
					setState(STATE_BUILD_UP);
					announceDeviceBuildUp();
					break;

				case EVENT_DEVICE_FAULT_RESET:
					setState(STATE_UNKNOWN);
					announceDeviceCommunicationErrorOrReset();
					break;
			}
		}

		private void changeBuildUpState(int event) {
			switch (event) {
				case EVENT_DEVICE_BYE:
					setState(STATE_STOPPED);
					announceDeviceBye();
					break;

				case EVENT_DEVICE_CHANGED:
					setState(STATE_RUNNING);
					announceDeviceChanged();
					break;

				case EVENT_DEVICE_FAULT_RESET:
					setState(STATE_UNKNOWN);
					announceDeviceCommunicationErrorOrReset();
					break;
			}
		}

		private void changeStoppedState(int event) {
			switch (event) {
				case EVENT_DEVICE_CHANGED:
					setState(STATE_RUNNING);
					announceDeviceChanged();
					break;

				case EVENT_DEVICE_GET_RSP:
					setState(STATE_BUILD_UP);
					announceDeviceBuildUp();
					break;

				case EVENT_DEVICE_SEEN:
					/*
					 * case: device has send bye, framework didn't receive a new
					 * hello and somebody sends get.
					 */
					if (device != null) {
						setState(STATE_BUILD_UP);
						announceDeviceBuildUp();
					} else {
						setState(STATE_RUNNING);
						announceDeviceRunning();
					}
					break;
			}
		}

	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.service.reference.DeviceReference#isSecureDevice()
	 */
	public boolean isSecureDevice() {
		return isSecure;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.ws4d.java.service.reference.DeviceReference#setSecureDevice(boolean)
	 */
	public void setSecureDevice(boolean sec) {
		this.isSecure = sec;
	}

	private static class RequestSynchronizer {

		long				metadataVersion;

		volatile boolean	pending	= true;

		TimeoutException	exception;

		RequestSynchronizer() {
			super();
			this.metadataVersion = DiscoveryData.UNKNOWN_METADATA_VERSION;
		}

		RequestSynchronizer(DefaultDeviceReference parent) {
			super();
			metadataVersion = parent.discoveryData.getMetadataVersion();
		}

	}

	private static class ResolveRequestSynchronizer extends RequestSynchronizer {

		XAddressInfo	xAddressInfo;

		ResolveRequestSynchronizer() {
			super();
		}

		ResolveRequestSynchronizer(DefaultDeviceReference parent) {
			super(parent);
		}

	}

	private static class GetRequestSynchronizer extends RequestSynchronizer {

		Device	device;

		GetRequestSynchronizer() {
			super();
		}

		GetRequestSynchronizer(DefaultDeviceReference parent) {
			super(parent);
		}

	}

	public XAddressInfo getPreferredXAddressInfo() {
		return preferredXAddressInfo;
	}

}
