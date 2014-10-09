/*******************************************************************************
 * Copyright (c) 2009 MATERNA Information & Communications. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html. For further
 * project-related information visit http://www.ws4d.org. The most recent
 * version of the JMEDS framework can be obtained from
 * http://sourceforge.net/projects/ws4d-javame.
 ******************************************************************************/

package org.ws4d.java.service;

import java.io.IOException;
import java.util.Date;

import org.ws4d.java.DPWSFramework;
import org.ws4d.java.client.DefaultClient;
import org.ws4d.java.communication.CommunicationBinding;
import org.ws4d.java.communication.CommunicationManager;
import org.ws4d.java.communication.CommunicationManagerRegistry;
import org.ws4d.java.communication.CommunicationUtil;
import org.ws4d.java.communication.DefaultIncomingMessageListener;
import org.ws4d.java.communication.Discovery;
import org.ws4d.java.communication.DiscoveryBinding;
import org.ws4d.java.communication.ProtocolData;
import org.ws4d.java.communication.ProtocolDomain;
import org.ws4d.java.communication.protocol.http.HTTPGroup;
import org.ws4d.java.communication.protocol.http.HTTPUser;
import org.ws4d.java.concurrency.LockSupport;
import org.ws4d.java.concurrency.Lockable;
import org.ws4d.java.configuration.DeviceProperties;
import org.ws4d.java.configuration.DevicesPropertiesHandler;
import org.ws4d.java.configuration.Properties;
import org.ws4d.java.constants.ConstantsHelper;
import org.ws4d.java.constants.DPWSMessageConstants;
import org.ws4d.java.dispatch.DefaultDeviceReference;
import org.ws4d.java.dispatch.DeviceServiceRegistry;
import org.ws4d.java.dispatch.OutDispatcher;
import org.ws4d.java.message.SOAPException;
import org.ws4d.java.message.discovery.ByeMessage;
import org.ws4d.java.message.discovery.HelloMessage;
import org.ws4d.java.message.discovery.ProbeMatch;
import org.ws4d.java.message.discovery.ProbeMatchesMessage;
import org.ws4d.java.message.discovery.ProbeMessage;
import org.ws4d.java.message.discovery.ResolveMatch;
import org.ws4d.java.message.discovery.ResolveMatchesMessage;
import org.ws4d.java.message.discovery.ResolveMessage;
import org.ws4d.java.message.metadata.GetMessage;
import org.ws4d.java.message.metadata.GetResponseMessage;
import org.ws4d.java.presentation.Presentation;
import org.ws4d.java.service.reference.DeviceReference;
import org.ws4d.java.service.reference.ServiceReference;
import org.ws4d.java.structures.ArrayList;
import org.ws4d.java.structures.DataStructure;
import org.ws4d.java.structures.EmptyStructures;
import org.ws4d.java.structures.HashMap;
import org.ws4d.java.structures.HashSet;
import org.ws4d.java.structures.Iterator;
import org.ws4d.java.structures.MessageIdBuffer;
import org.ws4d.java.structures.ReadOnlyIterator;
import org.ws4d.java.structures.Set;
import org.ws4d.java.types.AppSequence;
import org.ws4d.java.types.CustomizeMData;
import org.ws4d.java.types.DiscoveryData;
import org.ws4d.java.types.EndpointReference;
import org.ws4d.java.types.EprInfo;
import org.ws4d.java.types.EprInfoSet;
import org.ws4d.java.types.HostMData;
import org.ws4d.java.types.HostedMData;
import org.ws4d.java.types.LocalizedString;
import org.ws4d.java.types.ProbeScopeSet;
import org.ws4d.java.types.QName;
import org.ws4d.java.types.QNameSet;
import org.ws4d.java.types.RelationshipMData;
import org.ws4d.java.types.ScopeSet;
import org.ws4d.java.types.ThisDeviceMData;
import org.ws4d.java.types.ThisModelMData;
import org.ws4d.java.types.URI;
import org.ws4d.java.types.URISet;
import org.ws4d.java.types.XAddressInfo;
import org.ws4d.java.types.XAddressInfoSet;
import org.ws4d.java.util.IDGenerator;
import org.ws4d.java.util.Log;
import org.ws4d.java.util.StringUtil;
import org.ws4d.java.util.WS4DIllegalStateException;

/**
 * Implementation of a local DPWS device. A device in DPWS is a web service with
 * specific functions. It can be discovered via probing by clients and it
 * supports resolving of its endpoint. A device bears metadata information and
 * services.
 * <p>
 * This class implements a local device within the framework. Its metadata can
 * be configured and services can be added. The configuration can be done by use
 * of a configuration file/stream via the {@link Properties} class. In this
 * case, the constructor {@link #DefaultDevice(int configurationId)} must be
 * used with the configuration id of the matching device properties.
 * </p>
 * To receive messages, a communication binding {@link CommunicationBinding}
 * must be added to the device. In DPWS, this binding must be a
 * <code>org.ws4d.java.communication.HTTPBinding</code>, so that the device can
 * receive get messages.
 * <p>
 * A DefaultDevice has to be started before becoming fully functional. Starting
 * the device will establish the binding, i. e. a socket will be opened and the
 * http server will listen to the address of the binding. In DPWS, a multicast
 * listener will also be bound to the device. A hello message will then be sent
 * to all connected networks. Residing services will also be started. Stopping
 * the device will initiate the sending of a bye message, its services will be
 * stopped and the binding will be removed.
 * </p>
 * Example code: <code>
 * <pre>
 * DPWSFramework.start(args);
 * ...
 * DefaultDevice device = new DefaultDevice();
 * HTTPBinding binding = new HTTPBinding( ip, port, "SimpleExampleDevice" );
 * 
 * device.addBinding( binding );
 * device.addService( service );
 * device.addFriendlyName( "en-US", "JMEDS Simple Device" );
 * 
 * device.start();
 * </pre>
 * </code>
 * <p>
 * <strong>Important:</strong> Setting/Adding device data includes getting the
 * exclusive lock (({@link Lockable}) for the device.<BR>
 * If the device is running, each change will cause a hello message to be sent
 * with an incremented metadata version. To combine multiple device data changes
 * with sending only one hello message, the exclusive lock has to be first be
 * obtained by {@link #exclusiveLock()}. After the last device data change,
 * releasing the exclusive lock through {@link #releaseExclusiveLock()} will
 * send a single hello with an incremented metadata version.
 * </p>
 * <p>
 * A DefaultDevice will respond to the following request message types:
 * <ul>
 * <li>Probe Message - {@link ProbeMessage}
 * <li>Resolve Message - {@link ResolveMessage}
 * <li>Get Message - {@link GetMessage}
 * </ul>
 * with the appropriate response message types:
 * <ul>
 * <li>Probe Matches Message - {@link ProbeMatchesMessage}
 * <li>Resolve Matches Message - {@link ResolveMatchesMessage}
 * <li>Get Response Message - {@link GetResponseMessage}
 * </ul>
 * Additionally the device initiates the sending of the following message types:
 * <ul>
 * <li>Hello Message - {@link HelloMessage}
 * <li>Bye Message - {@link ByeMessage}
 * </ul>
 * </p>
 * The DefaultDevice class implements the functionality of a Target Service
 * described in the WSDD-Discovery specification. This version supports only the
 * Ad hoc operational mode.
 */
public class DefaultDevice extends DeviceCommons implements LocalDevice {

	private static final int[]				DISCOVERY_MESSAGE_TYPES			= { DPWSMessageConstants.PROBE_MESSAGE, DPWSMessageConstants.RESOLVE_MESSAGE };

	private static final int[]				DEVICE_MESSAGE_TYPES			= { DPWSMessageConstants.GET_MESSAGE, DPWSMessageConstants.PROBE_MESSAGE };

	/** Configuration id */
	protected final int						configurationId;

	protected final DeviceMessageListener	incomingListener				= new DeviceMessageListener(this);

	/** Lock */
	private final Lockable					lockSupport						= new LockSupport();

	/** Device reference of this device */
	protected DefaultDeviceReference		myDeviceRef						= null;

	protected DiscoveryData					discoveryData;

	/** Set of services attached to this device. */
	protected final Set						services						= new HashSet();

	protected boolean						running							= false;

	protected boolean						changed							= false;

	protected boolean						discoveryDataChanged			= true;

	protected boolean						isMetadataVersionSet			= false;

	protected final AppSequenceManager		appSequencer					= new AppSequenceManager();

	protected DataStructure					transportBindings;

	protected DataStructure					inputDiscoveryBindings;

	protected final DataStructure			outputDiscoveryDomains			= new HashSet();

	protected final DeviceProperties		deviceProp;

	protected boolean						usingDefaultDiscoveryDomains	= false;

	/** DiscoveryProxy */
	private boolean							isDiscoveryProxy				= false;

	// DEFAULT VALUES
	protected String						defaultLanugaugeString			= "en-EN";

	protected LocalizedString				defaultFriendlyName				= new LocalizedString(StringUtil.simpleClassName(getClass()), defaultLanugaugeString);

	protected LocalizedString				defaultModelName				= defaultFriendlyName;

	protected LocalizedString				defaultManufacturer				= new LocalizedString("MATERNA GmbH", null);

	private String							namespace						= "http://ws4d.org";

	private final MessageIdBuffer			messageIdBuffer					= new MessageIdBuffer();

	public static final int					MAX_QNAME_SERIALIZATION			= 10;

	/** Authentication */
	private HTTPGroup						userGroup						= null;

	public static boolean					hasCustomizeMData				= false;

	private HashMap							mdata							= new HashMap();

	public CustomizeMData					custom							= new CustomizeMData();

	/**
	 * Constructor local DPWS device. No device properties of the properties
	 * file/stream {@link Properties} are used to build up the device.
	 * <p>
	 * <strong>Important:</strong> It is necessary to
	 * {@link #addBinding(CommunicationBinding binding) add a binding} to a
	 * device before it can be started.
	 * </p>
	 */
	public DefaultDevice() {
		this(-1);
	}

	/**
	 * Constructor of local DPWS device. The given configuration id should map
	 * to the device property entries in the configuration file/stream
	 * {@link Properties}. The property entries of this device will be gathered
	 * in a {@link DeviceProperties} object and used to build up the device and
	 * its metadata.
	 * <p>
	 * <strong>Important:</strong> It is necessary to
	 * {@link #addBinding(CommunicationBinding binding) add a binding} to a
	 * device before it can be started. The binding may be specified within the
	 * configuration file/stream.
	 * </p>
	 * 
	 * @param configurationId The configuration id that map to the device
	 *            properties within the configuration file/stream.
	 */
	public DefaultDevice(int configurationId) {
		super();
		this.configurationId = configurationId;
		if (this.configurationId != -1) {
			DevicesPropertiesHandler propHandler = DevicesPropertiesHandler.getInstance();
			deviceProp = propHandler.getDeviceProperties(new Integer(configurationId));

			/*
			 * Reads configuration
			 */
			discoveryData = deviceProp.getDiscoveryData();
			deviceMetadata = deviceProp.getDeviceData();
			modelMetadata = deviceProp.getModelData();

			transportBindings = deviceProp.getBindings();
			// if (transportBindings.size() > 0) {
			// for (Iterator it = transportBindings.iterator(); it.hasNext();) {
			// CommunicationBinding binding = (CommunicationBinding) it.next();
			// addXAddress(binding.getTransportAddress());
			// }
			// }

			inputDiscoveryBindings = deviceProp.getDiscoveryBindings();

			if (getEndpointReference() == null) {
				// sets random UUID.
				EndpointReference epr = new EndpointReference(IDGenerator.getUUIDasURI());
				setEndpointReference(epr);
			}

			if (getMetadataVersion() < 0) {
				/*
				 * sets metadata version based on system time.
				 */
				setMetadataVersion((int) ((new Date()).getTime() / 1000));
			}

			if (deviceProp.useSecurity()) {
				setSecureDevice();
			}

			// propHandler.
		} else {
			deviceProp = null;

			transportBindings = new ArrayList(2);
			inputDiscoveryBindings = new ArrayList(2);
			discoveryData = new DiscoveryData();

			/*
			 * sets random UUID.
			 */
			EndpointReference epr = new EndpointReference(IDGenerator.getUUIDasURI());
			setEndpointReference(epr);

			/*
			 * sets metadata version based on system time.
			 */
			setMetadataVersion((int) ((new Date()).getTime() / 1000));

		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.service.Device#isRemote()
	 */
	public boolean isRemote() {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.service.LocalDevice#isRunning()
	 */
	public boolean isRunning() {
		sharedLock();
		try {
			return running;
		} finally {
			releaseSharedLock();
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.communication.Bindable#hasBindings()
	 */
	public boolean hasBindings() {
		return (transportBindings.size() > 0);
	}

	public boolean hasDiscoveryBindings() {
		return (inputDiscoveryBindings != null && inputDiscoveryBindings.size() > 0);
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.communication.Bindable#getBindings()
	 */
	public Iterator getBindings() {
		return new ReadOnlyIterator(transportBindings);
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.communication.Bindable#getBindings()
	 */
	public Iterator getDiscoveryBindings() {
		return new ReadOnlyIterator(inputDiscoveryBindings);
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.communication.Bindable#supportsBindingChanges()
	 */
	public boolean supportsBindingChanges() {
		lockSupport.sharedLock();
		try {
			return !isRunning();
		} finally {
			lockSupport.releaseSharedLock();
		}
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.ws4d.java.communication.Bindable#addBinding(org.ws4d.java.communication
	 * .CommunicationBinding)
	 */
	public void addBinding(CommunicationBinding binding) throws WS4DIllegalStateException {
		lockSupport.exclusiveLock();
		try {
			if (isRunning()) {
				throw new WS4DIllegalStateException("Device is already running, unable to add binding");
			}
			transportBindings.add(binding);
			// addXAddress(binding.getTransportAddress());
		} finally {
			lockSupport.releaseExclusiveLock();
		}

	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.ws4d.java.communication.Bindable#addBinding(org.ws4d.java.communication
	 * .CommunicationBinding)
	 */
	public void addBinding(DiscoveryBinding binding) throws WS4DIllegalStateException {
		lockSupport.exclusiveLock();
		try {
			if (isRunning()) {
				throw new WS4DIllegalStateException("Device is already running, unable to add binding");
			}
			if (binding instanceof DiscoveryBinding) {
				inputDiscoveryBindings.add(binding);
			} else {
				throw new WS4DIllegalStateException("Unsupported binding type: " + binding);
			}
		} finally {
			lockSupport.releaseExclusiveLock();
		}

	}

	/*
	 * (non-Javadoc)
	 * @seeorg.ws4d.java.communication.Bindable#removeBinding(org.ws4d.java.
	 * communication.CommunicationBinding)
	 */
	public boolean removeBinding(CommunicationBinding binding) throws WS4DIllegalStateException {
		lockSupport.exclusiveLock();
		try {
			if (isRunning()) {
				throw new WS4DIllegalStateException("Device is already running, unable to remove binding");
			}
			// removeXAddress(binding.getTransportAddress());
			return transportBindings.remove(binding);
		} finally {
			lockSupport.releaseExclusiveLock();
		}
	}

	/*
	 * (non-Javadoc)
	 * @seeorg.ws4d.java.communication.Bindable#removeBinding(org.ws4d.java.
	 * communication.CommunicationBinding)
	 */
	public boolean removeBinding(DiscoveryBinding binding) throws WS4DIllegalStateException {
		lockSupport.exclusiveLock();
		try {
			if (isRunning()) {
				throw new WS4DIllegalStateException("Device is already running, unable to remove binding");
			}
			if (binding instanceof DiscoveryBinding) {
				return inputDiscoveryBindings.remove(binding);
			} else {
				throw new WS4DIllegalStateException("Unsupported binding type: " + binding);
			}
		} finally {
			lockSupport.releaseExclusiveLock();
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.communication.Bindable#clearBindings()
	 */
	public void clearBindings() throws WS4DIllegalStateException {
		lockSupport.exclusiveLock();
		try {
			if (isRunning()) {
				throw new WS4DIllegalStateException("Device is already running, unable to clear bindings");
			}
			// for (Iterator it = transportBindings.iterator(); it.hasNext();) {
			// CommunicationBinding binding = (CommunicationBinding) it.next();
			// removeXAddress(binding.getTransportAddress());
			// it.remove();
			// }
			transportBindings.clear();
			inputDiscoveryBindings.clear();
		} finally {
			lockSupport.releaseExclusiveLock();
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.service.Device#getDeviceReference()
	 */
	public DeviceReference getDeviceReference() {
		if (myDeviceRef == null) {
			myDeviceRef = DeviceServiceRegistry.getDeviceReference(this);
			myDeviceRef.setLocalDevice(this);
		}
		return myDeviceRef;
	}

	/**
	 * Starts the device. Starting the device will:
	 * <ul>
	 * <li>start its services,
	 * <li>bind each {@link CommunicationBinding} to the matching
	 * {@link CommunicationManager}, i.e. start listening to incoming messages
	 * for the specified address,
	 * <li>registers the device to the {@link DeviceServiceRegistry}.
	 * </ul>
	 * 
	 * @throws IOException is thrown, if a binding couldn't be bound to the
	 *             communication manager or if starting a service will throw the
	 *             exception.
	 */
	public final void start() throws IOException {
		if (!DPWSFramework.isRunning()) {
			throw new RuntimeException("DPWSFramework not running, please start it in advance!");
		}

		lockSupport.exclusiveLock();
		try {
			if (isRunning()) {
				Log.warn("Cannot start device. Device already running.");
				return;
			}
			QNameSet collectedDeviceTypes = new QNameSet();
			/*
			 * Add default values of mandatory device metadata if necessary
			 */
			if (deviceMetadata.getFriendlyNames().size() == 0) {
				deviceMetadata.addFriendlyName(defaultFriendlyName);
			}
			if (modelMetadata.getManufacturerNames().size() == 0) {
				modelMetadata.addManufacturerName(defaultManufacturer);
			}
			if (modelMetadata.getModelNames().size() == 0) {
				modelMetadata.addModelName(defaultModelName);
			}

			if (Log.isDebug()) {
				Log.debug("Start Device: " + deviceMetadata.getFriendlyNames().iterator().next());
			}
			boolean hasInputDiscoveryBindings = hasDiscoveryBindings();
			boolean hasTransportBindings = hasBindings();
			boolean hasOutputDiscoveryDomain = !isUsingDefaultDiscoveryDomains();

			if (!hasTransportBindings) {
				String descriptor = StringUtil.simpleClassName(getClass());
				if (Log.isDebug()) {
					Log.info("No bindings found for Device. Autobinding device " + descriptor);
				}
				DataStructure autoBindings = new HashSet();
				for (Iterator it = CommunicationManagerRegistry.getLoadedManagers(); it.hasNext();) {
					CommunicationManager manager = (CommunicationManager) it.next();
					manager.getAutobindings(descriptor, autoBindings);
				}
				for (Iterator it = autoBindings.iterator(); it.hasNext();) {
					CommunicationBinding binding = (CommunicationBinding) it.next();
					addBinding(binding);
				}
			}
			if (hasInputDiscoveryBindings && hasOutputDiscoveryDomain) {
				// everything is ok. Nothing to do.
			} else if (!hasInputDiscoveryBindings && hasOutputDiscoveryDomain) {

				for (Iterator it = CommunicationManagerRegistry.getLoadedManagers(); it.hasNext();) {
					CommunicationManager manager = (CommunicationManager) it.next();

					for (Iterator it2 = getOutputDiscoveryDomains().iterator(); it2.hasNext();) {
						ProtocolDomain domain = (ProtocolDomain) it2.next();
						inputDiscoveryBindings.add(manager.getDiscoveryBindingForDomain(domain));
					}
				}

			} else if (hasInputDiscoveryBindings && !hasOutputDiscoveryDomain) {
				for (Iterator it = CommunicationManagerRegistry.getLoadedManagers(); it.hasNext();) {
					CommunicationManager manager = (CommunicationManager) it.next();
					for (Iterator it2 = getDiscoveryBindings(); it2.hasNext();) {
						DataStructure d = manager.getDiscoveryDomainForBinding((DiscoveryBinding) it2.next());
						for (Iterator itpd = d.iterator(); itpd.hasNext();) {
							addOutputDiscoveryDomain((ProtocolDomain) itpd.next());
						}
					}
				}
			} else {
				// if has no input and output Discovery information -> take
				// informations from HTTP-Binding!
				for (Iterator it = CommunicationManagerRegistry.getLoadedManagers(); it.hasNext();) {
					DataStructure domains = new HashSet();
					CommunicationManager manager = (CommunicationManager) it.next();
					manager.getProtocolDomains(getBindings(), domains);
					if (domains.size() > 0) {
						for (Iterator iti = domains.iterator(); iti.hasNext();) {
							ProtocolDomain domain = (ProtocolDomain) iti.next();
							inputDiscoveryBindings.add(manager.getDiscoveryBindingForDomain(domain));
							addOutputDiscoveryDomain(domain);
						}
					}
				}
			}
			for (Iterator it = getBindings(); it.hasNext();) {
				CommunicationBinding binding = (CommunicationBinding) it.next();
				CommunicationManager manager = CommunicationManagerRegistry.getManager(binding.getCommunicationManagerId());
				collectedDeviceTypes.addAll(manager.getDeviceTypes());
				manager.registerDevice(DEVICE_MESSAGE_TYPES, binding, incomingListener, userGroup);
				addXAddressInfo(new XAddressInfo(binding.getTransportAddress(), manager.getCommunicationManagerId()));
			}
			for (Iterator it = getDiscoveryBindings(); it.hasNext();) {
				DiscoveryBinding binding = (DiscoveryBinding) it.next();
				CommunicationManager manager = CommunicationManagerRegistry.getManager(binding.getCommunicationManagerId());
				collectedDeviceTypes.addAll(manager.getDeviceTypes());
				manager.registerDiscovery(DISCOVERY_MESSAGE_TYPES, binding, incomingListener);
			}

			for (Iterator it = services.iterator(); it.hasNext();) {
				LocalService service = (LocalService) it.next();
				service.setParentDevice(this);
				service.start();
			}
			if (Log.isInfo()) {
				StringBuffer sb = new StringBuffer();
				for (Iterator it = getXAddressInfos(); it.hasNext();) {
					sb.append(((XAddressInfo) it.next()).getXAddress());
					if (it.hasNext()) {
						sb.append(", ");
					}
				}
				Log.info("Device [ UUID=" + this.getEndpointReference().getAddress() + ", XAddresses={ " + sb.toString() + " } ] online.");
			}

			discoveryData.addTypes(collectedDeviceTypes);

			appSequencer.reset();

			// flag must be reseted, else initial started stack won't
			// updates metadata version with the first change
			isMetadataVersionSet = false;

			Presentation p = DPWSFramework.getPresentation();
			if (p != null) {
				try {
					URI presentationURL = p.register(this);
					this.setPresentationUrl(presentationURL.toString());
				} catch (RuntimeException e) {
					Log.printStackTrace(e);
				}
			}

			// / Registers device reference in framework
			getDeviceReference();
			running = true;
			changed = false;

		} finally {
			lockSupport.releaseExclusiveLock();
		}

		DeviceServiceRegistry.register(this);

		HelloMessage hello = createHelloMessage();

		OutDispatcher.getInstance().send(hello, null, getOutputDiscoveryDomains());
		if (myDeviceRef != null) {
			if (changed) {
				myDeviceRef.announceDeviceChangedAndBuildUp();
			} else {
				myDeviceRef.announceDeviceRunningAndBuildUp();
			}
		}
	}

	/**
	 * Stops the device. Stopping the device will:
	 * <ul>
	 * <li>stop its services,
	 * <li>unbind each {@link CommunicationBinding} to the matching
	 * {@link CommunicationManager},
	 * <li>unregisters the device from the {@link DeviceServiceRegistry}.
	 * </ul>
	 * 
	 * @throws IOException is thrown if a binding couldn't be unbound or if
	 *             stopping a service will throw the exception.
	 */
	public final void stop() throws IOException {
		stop(true);
	}

	/**
	 * Stops the device. Stopping the device will:
	 * <ul>
	 * <li>unbind each {@link CommunicationBinding} to the matching
	 * {@link CommunicationManager},
	 * <li>unregisters the device from the {@link DeviceServiceRegistry}.
	 * </ul>
	 * 
	 * @param stopServices If true, stops services too.
	 * @throws IOException is thrown if a binding couldn't be unbound or if
	 *             stopping a service will throw the exception.
	 */
	public final void stop(boolean stopServices) throws IOException {
		lockSupport.exclusiveLock();
		try {
			if (!isRunning()) {
				Log.warn("Cannot stop device. Device not running.");
				return;
			}
			DeviceServiceRegistry.unregister(this);
			for (Iterator it = getBindings(); it.hasNext();) {
				CommunicationBinding binding = (CommunicationBinding) it.next();
				CommunicationManager manager = CommunicationManagerRegistry.getManager(binding.getCommunicationManagerId());
				removeXAddressInfo(new XAddressInfo(binding.getTransportAddress(), manager.getCommunicationManagerId()));
				manager.unregisterDevice(DEVICE_MESSAGE_TYPES, binding, incomingListener);
			}
			for (Iterator it = getDiscoveryBindings(); it.hasNext();) {
				DiscoveryBinding binding = (DiscoveryBinding) it.next();
				CommunicationManager manager = CommunicationManagerRegistry.getManager(binding.getCommunicationManagerId());
				manager.unregisterDiscovery(DISCOVERY_MESSAGE_TYPES, binding, incomingListener);
			}
			if (stopServices) {
				for (Iterator it = services.iterator(); it.hasNext();) {
					LocalService service = (LocalService) it.next();
					service.stop();
				}
			}
			Log.info("Device [ UUID=" + this.getEndpointReference().getAddress() + " ] offline.");

			sendBye();

			if (myDeviceRef != null) {
				myDeviceRef.announceDeviceBye();
			}
			running = false;
		} finally {
			lockSupport.releaseExclusiveLock();
		}
	}

	/**
	 * Sends hello message. Simple method to announce the device is in the
	 * network.
	 * <p>
	 * <strong>Important:</strong> This method won't start the device. But
	 * starting this device will automatically send a hello message.
	 * </p>
	 */
	public void sendHello() {
		HelloMessage hello = createHelloMessage();
		OutDispatcher.getInstance().send(hello, null, getOutputDiscoveryDomains());
	}

	/**
	 * Sends Bye Message. Simple method to send a bye message to the network.
	 * <p>
	 * <strong>Important:</strong> This method won't stop the device. But
	 * stopping this device will automatically send a bye message.
	 * </p>
	 */
	public void sendBye() {
		DiscoveryData data = new DiscoveryData();
		data.setEndpointReference(discoveryData.getEndpointReference());
		data.setXAddresInfoSet(discoveryData.getXAddressInfoSet());
		ByeMessage bye = new ByeMessage(data, CommunicationManager.ID_NULL);
		bye.getHeader().setAppSequence(appSequencer.getNext());

		OutDispatcher.getInstance().send(bye, null, getOutputDiscoveryDomains());
	}

	/**
	 * Increments metadata version by one and send hello, inform local device
	 * update listener.
	 */
	private void deviceUpdated() {
		lockSupport.exclusiveLock();
		HelloMessage hello = null;
		try {
			// if (!isRunning()) {
			// // fire hellos only if currently up
			// return;
			// }
			if (!isMetadataVersionSet) {
				/*
				 * We only increment version, if not set by user.
				 */
				copyDiscoveryDataIfRunning();

				long metadataVersion = discoveryData.getMetadataVersion();
				metadataVersion++;
				discoveryData.setMetadataVersion(metadataVersion);
			} else {
				isMetadataVersionSet = false;
			}
			if (running) {
				hello = createHelloMessage();
			}
		} finally {
			discoveryDataChanged = false;
			lockSupport.releaseExclusiveLock();
			if (hello != null) {
				OutDispatcher.getInstance().send(hello, null, getOutputDiscoveryDomains());
				if (myDeviceRef != null) {
					myDeviceRef.announceDeviceChangedAndBuildUp();
				}
				changed = false;
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.concurrency.locks.Lockable#sharedLock()
	 */
	public void sharedLock() {
		lockSupport.sharedLock();
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.concurrency.locks.Lockable#exclusiveLock()
	 */
	public void exclusiveLock() {
		lockSupport.exclusiveLock();
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.concurrency.locks.Lockable#releaseSharedLock()
	 */
	public void releaseSharedLock() {
		lockSupport.releaseSharedLock();
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.concurrency.locks.Lockable#releaseExclusiveLock()
	 */
	public boolean releaseExclusiveLock() {
		boolean isLastLockReleased = lockSupport.releaseExclusiveLock();
		if (isLastLockReleased && changed) {
			changed = false;
			deviceUpdated();
		}
		return isLastLockReleased;
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.concurrency.locks.Lockable#tryExclusiveLock()
	 */
	public boolean tryExclusiveLock() {
		return lockSupport.tryExclusiveLock();
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.concurrency.locks.Lockable#trySharedLock()
	 */
	public boolean trySharedLock() {
		return lockSupport.trySharedLock();
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.service.Device#getEndpointReferences()
	 */
	public EndpointReference getEndpointReference() {
		lockSupport.sharedLock();
		try {
			return discoveryData.getEndpointReference();
		} finally {
			lockSupport.releaseSharedLock();
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.service.Device#getPortTypes()
	 */
	public Iterator getPortTypes() {
		lockSupport.sharedLock();
		try {
			QNameSet types = discoveryData.getTypes();
			return types == null ? EmptyStructures.EMPTY_ITERATOR : new ReadOnlyIterator(types.iterator());
		} finally {
			lockSupport.releaseSharedLock();
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.service.Device#getScopes()
	 */
	public Iterator getScopes() {
		lockSupport.sharedLock();
		try {
			ScopeSet scopes = discoveryData.getScopes();
			URISet uriScopes = (scopes == null) ? null : scopes.getScopesAsUris();
			return (uriScopes == null) ? EmptyStructures.EMPTY_ITERATOR : new ReadOnlyIterator(uriScopes.iterator());
		} finally {
			lockSupport.releaseSharedLock();
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.service.Device#getManufacturer(java.lang.String)
	 */
	public String getManufacturer(String lang) {
		lockSupport.sharedLock();
		try {
			return super.getManufacturer(lang);
		} finally {
			lockSupport.releaseSharedLock();
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.service.Device#getManufacturers()
	 */
	public Iterator getManufacturers() {
		lockSupport.sharedLock();
		try {
			return super.getManufacturers();
		} finally {
			lockSupport.releaseSharedLock();
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.service.Device#getManufacturerUrl()
	 */
	public String getManufacturerUrl() {
		lockSupport.sharedLock();
		try {
			return super.getManufacturerUrl();
		} finally {
			lockSupport.releaseSharedLock();
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.service.Device#getModelName(java.lang.String)
	 */
	public String getModelName(String lang) {
		lockSupport.sharedLock();
		try {
			return super.getModelName(lang);
		} finally {
			lockSupport.releaseSharedLock();
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.service.Device#getModelNames()
	 */
	public Iterator getModelNames() {
		lockSupport.sharedLock();
		try {
			return super.getModelNames();
		} finally {
			lockSupport.releaseSharedLock();
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.service.Device#getModelNumber()
	 */
	public String getModelNumber() {
		lockSupport.sharedLock();
		try {
			return super.getModelNumber();
		} finally {
			lockSupport.releaseSharedLock();
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.service.Device#getModelUrl()
	 */
	public String getModelUrl() {
		lockSupport.sharedLock();
		try {
			return super.getModelUrl();
		} finally {
			lockSupport.releaseSharedLock();
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.service.Device#getPresentationUrl()
	 */
	public String getPresentationUrl() {
		lockSupport.sharedLock();
		try {
			return super.getPresentationUrl();
		} finally {
			lockSupport.releaseSharedLock();
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.service.Device#getFriendlyName(java.lang.String)
	 */
	public String getFriendlyName(String lang) {
		lockSupport.sharedLock();
		try {
			return super.getFriendlyName(lang);
		} finally {
			lockSupport.releaseSharedLock();
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.service.Device#getFriendlyNames()
	 */
	public Iterator getFriendlyNames() {
		lockSupport.sharedLock();
		try {
			return super.getFriendlyNames();
		} finally {
			lockSupport.releaseSharedLock();
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.service.Device#getFirmwareVersion()
	 */
	public String getFirmwareVersion() {
		lockSupport.sharedLock();
		try {
			return super.getFirmwareVersion();
		} finally {
			lockSupport.releaseSharedLock();
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.service.Device#getSerialNumber()
	 */
	public String getSerialNumber() {
		lockSupport.sharedLock();
		try {
			return super.getSerialNumber();
		} finally {
			lockSupport.releaseSharedLock();
		}
	}

	/**
	 * Sets the {@link EndpointReference} of this device. The endpoint reference
	 * bears a stable globally-unique identifier of the device. This address
	 * part is typically not a physical address. <BR>
	 * If not set, the framework generates it automatically. The address part of
	 * the endpoint reference can be configured via the {@link Properties}.
	 * 
	 * @param endpoint The endpoint reference to set.
	 */
	public void setEndpointReference(EndpointReference endpoint) {
		if (endpoint == null) {
			throw new IllegalArgumentException("endpoint reference must not be null");
		}
		lockSupport.exclusiveLock();
		try {
			copyDiscoveryDataIfRunning();
			discoveryData.setEndpointReference(endpoint);
			changed = true;
		} finally {
			if (lockSupport.releaseExclusiveLock()) {
				deviceUpdated();
			}
		}
	}

	/**
	 * Sets the port types of the device. This port types should show clients in
	 * the network which services the device may hold. Clients (see
	 * {@link DefaultClient}) can search for the specific device port types.
	 * <p>
	 * The port types are communicated via the hello, probe matches, resolve
	 * matches, get response and the get metadata response messages (the
	 * "wsdd:Types" elements and the be "dpws:Types" elements of host metadata).
	 * </p>
	 * <p>
	 * The "dpws:Device" port type is added by default.
	 * </p>
	 * 
	 * @param qnsPortTypes Device port types to set.
	 */
	public void setPortTypes(QNameSet qnsPortTypes) {
		lockSupport.exclusiveLock();
		try {
			if (qnsPortTypes == null) {
				qnsPortTypes = new QNameSet();
			}
			copyDiscoveryDataIfRunning();
			discoveryData.setTypes(qnsPortTypes);
			changed = true;
		} finally {
			if (lockSupport.releaseExclusiveLock()) {
				deviceUpdated();
			}
		}
	}

	/**
	 * Adds {@link XAddressInfo} to device.
	 * 
	 * @param xAdrInfo
	 */
	private void addXAddressInfo(XAddressInfo xAdrInfo) {
		lockSupport.exclusiveLock();
		try {
			copyDiscoveryDataIfRunning();
			XAddressInfoSet xAddresses = discoveryData.getXAddressInfoSet();
			if (xAddresses == null) {
				xAddresses = new XAddressInfoSet();
				discoveryData.setXAddresInfoSet(xAddresses);
			}
			xAddresses.add(xAdrInfo);
		} finally {
			lockSupport.releaseExclusiveLock();
		}
	}

	// /**
	// * Sets list of xaddresses.
	// *
	// * @param xAddresses xaddresses to set
	// */
	// public void setXAddresses(URISet xAddresses) {
	// lockSupport.exclusiveLock();
	// try {
	// discoveryData.setXAddrs(xAddresses);
	// } finally {
	// lockSupport.releaseExclusiveLock();
	// }
	// }
	//
	/**
	 * Removes {@link XAddressInfo} from device.
	 * 
	 * @param xAdrInfo
	 */
	private void removeXAddressInfo(XAddressInfo xAdrInfo) {
		lockSupport.exclusiveLock();
		try {
			copyDiscoveryDataIfRunning();
			XAddressInfoSet xAddresses = discoveryData.getXAddressInfoSet();
			if (xAddresses != null && xAdrInfo != null) {
				xAddresses.remove(xAdrInfo);
			}
		} finally {
			lockSupport.releaseExclusiveLock();
		}
	}

	/**
	 * Sets a list of scopes. Scopes are used within the discovery of devices. A
	 * client may search for devices with specific scopes. <BR>
	 * Scopes are part of the hello, probe matches, resolve matches messages.
	 * <p>
	 * Setting the scopes includes getting the exclusive lock (({@link Lockable}
	 * ) of the device.<BR>
	 * If the device is running, each change will initiate the sending of a
	 * hello message with an incremented metadata version. To combine multiple
	 * device data changes with sending only one hello, the exclusive lock has
	 * to be obtained through {@link #exclusiveLock()}. After the last device
	 * data change, releasing the exclusive lock with
	 * {@link #releaseExclusiveLock()} will send a single hello with an
	 * incremented metadata version.
	 * </p>
	 * 
	 * @param scopes List of scopes to set.
	 */
	public void setScopes(ScopeSet scopes) {
		lockSupport.exclusiveLock();
		try {
			copyDiscoveryDataIfRunning();
			discoveryData.setScopes(scopes);
			changed = true;
		} finally {
			if (lockSupport.releaseExclusiveLock()) {
				deviceUpdated();
			}
		}
	}

	/**
	 * Adds manufacturer name to the device which is used as value of the
	 * "dpws:Manufacturer" element in the model metadata. The manufacturer name
	 * is language specific.
	 * <p>
	 * Adding the manufacturer name includes getting the exclusive lock ((
	 * {@link Lockable}) for the device.<BR>
	 * If the device is running, each change will initiate the sending of a
	 * hello message with an incremented metadata version. To combine multiple
	 * device data changes with sending only one hello, the exclusive lock has
	 * to be obtained through {@link #exclusiveLock()}. After the last device
	 * data change, releasing the exclusive lock with
	 * {@link #releaseExclusiveLock()} will send a single hello with an
	 * incremented metadata version.
	 * </p>
	 * 
	 * @param lang Language attribute, i. e. "en-US or "de-DE":
	 *            <ul>
	 *            <li>The syntax of the language tags is described in RFC 5646.
	 *            <li>All language subtags are registered to the IANA Language
	 *            Subtag Registry.
	 *            <li>All region subtags are specified in
	 *            "ISO 3166: Codes for Country Names".
	 *            </ul>
	 * @param manufacturer The manufacturer name to set in the specified
	 *            language.
	 */
	public void addManufacturer(String lang, String manufacturer) {
		lockSupport.exclusiveLock();
		try {
			modelMetadata.addManufacturerName(new LocalizedString(manufacturer, lang));
			changed = true;
		} finally {
			if (lockSupport.releaseExclusiveLock()) {
				deviceUpdated();
			}
		}
	}

	/**
	 * Sets the url of the manufacturer. It used as the value of the
	 * "dpws:ManufacturerUrl" element of the model metadata.
	 * <p>
	 * Setting the manufacturer url includes getting the exclusive lock ((
	 * {@link Lockable}) for the device.<BR>
	 * If the device is running, each change will initiate the sending of a
	 * hello message with an incremented metadata version. To combine multiple
	 * device data changes with sending only one hello, the exclusive lock has
	 * to be obtained through {@link #exclusiveLock()}. After the last device
	 * data change, releasing the exclusive lock with
	 * {@link #releaseExclusiveLock()} will send a single hello with an
	 * incremented metadata version.
	 * </p>
	 * 
	 * @param manufacturerUrl The url of the manufacturer to set.
	 */
	public void setManufacturerUrl(String manufacturerUrl) {
		lockSupport.exclusiveLock();
		try {
			modelMetadata.setManufacturerUrl(new URI(manufacturerUrl));
			changed = true;
		} finally {
			if (lockSupport.releaseExclusiveLock()) {
				deviceUpdated();
			}
		}
	}

	/**
	 * Adds a model name to the device. The model name is used as value of the
	 * "dpws:ModelName" element in the model metadata. The model name is
	 * language specific.
	 * <p>
	 * Adding a model name includes getting the exclusive lock ((
	 * {@link Lockable}) for the device.<BR>
	 * If the device is running, each change will initiate the sending of a
	 * hello message with an incremented metadata version. To combine multiple
	 * device data changes with sending only one hello, the exclusive lock has
	 * to be obtained through {@link #exclusiveLock()}. After the last device
	 * data change releasing the exclusive lock with
	 * {@link #releaseExclusiveLock()} will send a single hello with an
	 * incremented metadata version.
	 * </p>
	 * 
	 * @param lang Language attribute, i. e. "en-US or "de-DE":
	 *            <ul>
	 *            <li>The syntax of the language tags is described in RFC 5646.
	 *            <li>All language subtags are registered to the IANA Language
	 *            Subtag Registry.
	 *            <li>All region subtags are specified in
	 *            "ISO 3166: Codes for Country Names".
	 *            </ul>
	 * @param modelName The model name to set in the specified language.
	 */
	public void addModelName(String lang, String modelName) {
		lockSupport.exclusiveLock();
		try {
			modelMetadata.addModelName(new LocalizedString(modelName, lang));
			changed = true;
		} finally {
			if (lockSupport.releaseExclusiveLock()) {
				deviceUpdated();
			}
		}
	}

	/**
	 * Sets the model number of the device. The model number is used as value of
	 * the "dpws:ModelNumber" element in the model metadata.
	 * <p>
	 * Setting the model number includes getting the exclusive lock ((
	 * {@link Lockable}) for the device.<BR>
	 * If the device is running, each change will initiate the sending of a
	 * hello message with an incremented metadata version. To combine multiple
	 * device data changes with sending only one hello, the exclusive lock has
	 * to be obtained through{@link #exclusiveLock()}. After the last device
	 * data change the release of the exclusive lock by
	 * {@link #releaseExclusiveLock()} will send a single hello with an
	 * incremented metadata version.
	 * </p>
	 * 
	 * @param modelNumber The model number of the device to set.
	 */
	public void setModelNumber(String modelNumber) {
		lockSupport.exclusiveLock();
		try {
			modelMetadata.setModelNumber(modelNumber);
			changed = true;
		} finally {
			if (lockSupport.releaseExclusiveLock()) {
				deviceUpdated();
			}
		}
	}

	/**
	 * Sets the model url of the device. The model url is used as value of the
	 * "dpws:ModelUrl" element of the model metadata.
	 * <p>
	 * Setting the model url includes getting the exclusive lock ((
	 * {@link Lockable}) for the device.<BR>
	 * If the device is running, each change will initiate the sending of a
	 * hello message with an incremented metadata version. To combine multiple
	 * device data changes with sending only one hello, the exclusive lock has
	 * to be taken by {@link #exclusiveLock()}. After the last device data
	 * change the release of the exclusive lock by
	 * {@link #releaseExclusiveLock()} will send a single hello with an
	 * incremented metadata version.
	 * </p>
	 * 
	 * @param modelUrl The model url of the device to set.
	 */
	public void setModelUrl(String modelUrl) {
		lockSupport.exclusiveLock();
		try {
			modelMetadata.setModelUrl(new URI(modelUrl));
			changed = true;
		} finally {
			if (lockSupport.releaseExclusiveLock()) {
				deviceUpdated();
			}
		}
	}

	/**
	 * Sets the presentation url of the device. It is used as value of the
	 * "dpws:PresentationUrl" element of the model metadata.
	 * <p>
	 * Setting the presentation url includes getting the exclusive lock ((
	 * {@link Lockable}) for the device.<BR>
	 * If the device is running, each change will initiate the sending of a
	 * hello message with an incremented metadata version. To combine multiple
	 * device data changes with sending only one hello, the exclusive lock has
	 * to be obtained through {@link #exclusiveLock()}. After the last device
	 * data change releasing the exclusive lock with
	 * {@link #releaseExclusiveLock()} will send a single hello with an
	 * incremented metadata version.
	 * </p>
	 * 
	 * @param presentationUrl The presentation url to set.
	 */
	public void setPresentationUrl(String presentationUrl) {
		lockSupport.exclusiveLock();
		try {
			modelMetadata.setPresentationUrl(new URI(presentationUrl));
			changed = true;
		} finally {
			if (lockSupport.releaseExclusiveLock()) {
				deviceUpdated();
			}
		}
	}

	/**
	 * Adds a friendly name to the device. It is used as the value of the
	 * "dpws:FriendlyName" element of the device metadata. The friendly name is
	 * language specific.
	 * <p>
	 * Adding a friendly name includes getting the exclusive lock ((
	 * {@link Lockable}) for the device.<BR>
	 * If the device is running, each change will initiate the sending of a
	 * hello message with an incremented metadata version. To combine multiple
	 * device data changes with sending only one hello, the exclusive lock has
	 * to be obtained through {@link #exclusiveLock()}. After the last device
	 * data change releasing the exclusive lock with
	 * {@link #releaseExclusiveLock()} will send a single hello with an
	 * incremented metadata version.
	 * </p>
	 * 
	 * @param lang Language attribute, i. e. "en-US or "de-DE":
	 *            <ul>
	 *            <li>The syntax of the language tags is described in RFC 5646.
	 *            <li>All language subtags are registered to the IANA Language
	 *            Subtag Registry.
	 *            <li>All region subtags are specified in
	 *            "ISO 3166: Codes for Country Names".
	 *            </ul>
	 * @param friendlyName The friendly name of the device in the specified
	 *            language to be set.
	 */
	public void addFriendlyName(String lang, String friendlyName) {
		lockSupport.exclusiveLock();
		try {
			deviceMetadata.addFriendlyName(new LocalizedString(friendlyName, lang));
			changed = true;
		} finally {
			if (lockSupport.releaseExclusiveLock()) {
				deviceUpdated();
			}
		}
	}

	/**
	 * Sets the firmware version to the device. It is used as the value of the
	 * "dpws:FirmwareVersion" element of the device metadata.
	 * <p>
	 * Setting the firmware version includes getting the exclusive lock ((
	 * {@link Lockable}) for the device.<BR>
	 * If the device is running, each change will initiate the sending of a
	 * hello message with an incremented metadata version. To combine multiple
	 * device data changes with sending only one hello, the exclusive lock has
	 * to be obtained through {@link #exclusiveLock()}. After the last device
	 * data change releasing the exclusive lock with
	 * {@link #releaseExclusiveLock()} will send a single hello with an
	 * incremented metadata version.
	 * </p>
	 * 
	 * @param firmware The firmware version of the device to set.
	 */
	public void setFirmwareVersion(String firmware) {
		lockSupport.exclusiveLock();
		try {
			deviceMetadata.setFirmwareVersion(firmware);
			changed = true;
		} finally {
			if (lockSupport.releaseExclusiveLock()) {
				deviceUpdated();
			}
		}
	}

	/**
	 * Sets the serial number of the device. It is used as the value of the
	 * "wsdp:SerialNumber" element of the device metadata.
	 * <p>
	 * Setting the serial number version includes getting the exclusive lock ((
	 * {@link Lockable}) for the device.<BR>
	 * If the device is running, each change will initiate the sending of a
	 * hello message with an incremented metadata version. To combine multiple
	 * device data changes with sending only one hello, the exclusive lock has
	 * to be obtained through {@link #exclusiveLock()}. After the last device
	 * data change releasing the exclusive lock with
	 * {@link #releaseExclusiveLock()} will send a single hello with an
	 * incremented metadata version.
	 * </p>
	 * 
	 * @param serialNumber The serial number of the device to set.
	 */
	public void setSerialNumber(String serialNumber) {
		lockSupport.exclusiveLock();
		try {
			deviceMetadata.setSerialNumber(serialNumber);
			changed = true;
		} finally {
			if (lockSupport.releaseExclusiveLock()) {
				deviceUpdated();
			}
		}
	}

	/**
	 * Adds service to device.
	 * <p>
	 * NOTICE: If the device is already running, you must start the service with
	 * the start() method, or use the addService(LocalService, boolean) method.
	 * </p>
	 * <p>
	 * Adding a service to the device includes getting the exclusive lock ((
	 * {@link Lockable}) for the device.<BR>
	 * If the device is running, each change will initiate the sending of a
	 * hello message with an incremented metadata version. To combine multiple
	 * device data changes with sending only one hello, the exclusive lock has
	 * to be obtained through {@link #exclusiveLock()}. After the last device
	 * data change releasing the exclusive lock with
	 * {@link #releaseExclusiveLock()} will send a single hello with an
	 * incremented metadata version.
	 * </p>
	 * 
	 * @see org.ws4d.java.service.LocalDevice#addService(org.ws4d.java.service.LocalService,
	 *      boolean)
	 * @param service service to add to this device.
	 */
	public void addService(LocalService service) {
		try {
			addService(service, true);
		} catch (IOException e) {
			// THIS should NEVER happen! Because we don't start the service!
			Log.error("Oh shit! I got an exception while adding a service. Shit should NEVER happen here!");
		}
	}

	/**
	 * Adds a service to the device.
	 * <p>
	 * Adding a service to the device includes getting the exclusive lock ((
	 * {@link Lockable}) for the device.<BR>
	 * If the device is running, each change will initiate the sending of a
	 * hello message with an incremented metadata version. To combine multiple
	 * device data changes with sending only one hello, the exclusive lock has
	 * to be obtained through {@link #exclusiveLock()}. After the last device
	 * data change, releasing the exclusive lock with
	 * {@link #releaseExclusiveLock()} will send a single hello with an
	 * incremented metadata version.
	 * </p>
	 * 
	 * @param service service to add to this device.
	 * @param startIfRunning <code>true</code> the service is started if the
	 *            device is already running, <code>false</code> the service has
	 *            not been not started, we just add it.
	 */
	public void addService(LocalService service, boolean startIfRunning) throws IOException {
		lockSupport.exclusiveLock();
		try {
			service.setParentDevice(this);
			services.add(service);
			if (isRunning() && startIfRunning) {
				service.start();
			}
			changed = true;
		} finally {
			if (lockSupport.releaseExclusiveLock()) {
				deviceUpdated();
			}
		}
	}

	/**
	 * Removes service from device. The service will be removed from the device,
	 * but won't be stopped.
	 * <p>
	 * Removing a service from the device includes getting the exclusive lock ((
	 * {@link Lockable}) for the device.<BR>
	 * If the device is running, each change will initiate the sending of a
	 * hello message with an incremented metadata version. To combine multiple
	 * device data changes with sending only one hello, the exclusive lock has
	 * to be obtained through {@link #exclusiveLock()}. After the last device
	 * data change releasing the exclusive lock with
	 * {@link #releaseExclusiveLock()} will send a single hello with an
	 * incremented metadata version.
	 * </p>
	 * 
	 * @param service The service to remove from this device.
	 */
	public void removeService(LocalService service) {
		try {
			removeService(service, false);
		} catch (IOException e) {
			// THIS should NEVER happen! Because we don't start the service!
			Log.error("Oh shit! I got an exception while adding a service. Shit should NEVER happen here!");
		}
	}

	/**
	 * Removes a service from the device. If stopIfRunning is
	 * <code>true<code> the service to remove is stopped if running, else not.
	 * <p>
	 * Removing a service from the device includes getting the exclusive lock ((
	 * {@link Lockable}) for the device.<BR>
	 * If the device is running, each change will initiate the sending of a
	 * hello message with an incremented metadata version. To combine multiple
	 * device data changes with sending only one hello, the exclusive lock has
	 * to be obtained through{@link #exclusiveLock()}. After the last device data
	 * change releasing the exclusive lock with
	 * {@link #releaseExclusiveLock()} will send a single hello with an
	 * incremented metadata version.
	 * </p>
	 * 
	 * @param service The service to remove from the device.
	 * @param stopIfRunning <code>true</code> the service is stopped if the
	 *            service is running, <code>false</code> just remove.
	 */
	public void removeService(LocalService service, boolean stopIfRunning) throws IOException {
		lockSupport.exclusiveLock();
		try {
			services.remove(service);
			if (service.isRunning() && stopIfRunning) {
				service.stop();
			}
		} finally {
			if (lockSupport.releaseExclusiveLock()) {
				if (isRunning()) {
					deviceUpdated();
				} else {
					changed = true;
				}
			}
		}
	}

	/**
	 * Sets the device metadata of the device. It contains different device
	 * metadata and is transmitted to the "dpws:ThisDevice" metadata.
	 * <p>
	 * Setting the device metadata includes getting the exclusive lock ((
	 * {@link Lockable}) for the device.<BR>
	 * If the device is running, each change will initiate the sending of a
	 * hello message with an incremented metadata version. To combine multiple
	 * device data changes with sending only one hello, the exclusive lock has
	 * to obtained through {@link #exclusiveLock()}. After the last device data
	 * change, releasing the exclusive lock with {@link #releaseExclusiveLock()}
	 * will send a single hello with an incremented metadata version.
	 * </p>
	 * 
	 * @param deviceMetadata
	 */
	public void setDeviceMetadata(ThisDeviceMData deviceMetadata) {
		lockSupport.exclusiveLock();
		try {
			this.deviceMetadata = deviceMetadata;
			changed = true;
		} finally {
			if (lockSupport.releaseExclusiveLock()) {
				deviceUpdated();
			}
		}

	}

	/**
	 * Sets the metadata version of the device. The metadata version is part of
	 * some discovery messages of the device. If it is incremented, clients
	 * receiving this new metadata version have to update the device's
	 * information.
	 * <p>
	 * Setting the metadata version includes getting the exclusive lock ((
	 * {@link Lockable}) for the device.<BR>
	 * If the device is running, each change will initiate the sending of a
	 * hello message with an incremented metadata version. To combine multiple
	 * device data changes with sending only one hello, the exclusive lock has
	 * to be obtained through {@link #exclusiveLock()}. After the last device
	 * data change, releasing the exclusive lock with
	 * {@link #releaseExclusiveLock()} will send a single hello with the new
	 * metadata version.
	 * </p>
	 * 
	 * @param metadataVersion The metadata version to set is of type unsigned
	 *            int.
	 */
	public void setMetadataVersion(long metadataVersion) {
		lockSupport.exclusiveLock();
		try {
			copyDiscoveryDataIfRunning();
			this.discoveryData.setMetadataVersion(metadataVersion);
			isMetadataVersionSet = true;
			changed = true;
		} finally {
			if (lockSupport.releaseExclusiveLock()) {
				deviceUpdated();
			}
		}
	}

	/**
	 * Sets the model metadata of the device. It contains different model meta
	 * data and is transmitted via the "dpws:ThisModel" metadata.
	 * <p>
	 * Setting the model metadata version includes getting the exclusive lock ((
	 * {@link Lockable}) for the device.<BR>
	 * If the device is running, each change will initiate the sending of a
	 * hello message with an incremented metadata version. To combine multiple
	 * device data changes with sending only one hello, the exclusive lock has
	 * to be obtained through {@link #exclusiveLock()}. After the last device
	 * data change releasing the exclusive lock with
	 * {@link #releaseExclusiveLock()} will send a single hello with an
	 * incremented metadata version.
	 * </p>
	 * 
	 * @param modelMetadata The model metadata of the device to set.
	 */
	public void setModelMetadata(ThisModelMData modelMetadata) {
		lockSupport.exclusiveLock();
		try {
			this.modelMetadata = modelMetadata;
			changed = true;
		} finally {
			if (lockSupport.releaseExclusiveLock()) {
				deviceUpdated();
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.service.Device#getDeviceMetadata()
	 */
	public ThisDeviceMData getDeviceMetadata() {
		lockSupport.sharedLock();
		try {
			return deviceMetadata;
		} finally {
			lockSupport.releaseSharedLock();
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.service.Device#getMetadataVersion()
	 */
	public long getMetadataVersion() {
		lockSupport.sharedLock();
		try {
			return discoveryData.getMetadataVersion();
		} finally {
			lockSupport.releaseSharedLock();
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.service.Device#getModelMetadata()
	 */
	public ThisModelMData getModelMetadata() {
		lockSupport.sharedLock();
		try {
			return modelMetadata;
		} finally {
			lockSupport.releaseSharedLock();
		}
	}

	/**
	 * Gets iterator over all services. A service is of type {@link Service}.
	 * 
	 * @return Iterator over all services of type {@link Service}.
	 */
	public Iterator getServices() {
		return new ReadOnlyIterator(services);
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.service.Device#getServiceReferences()
	 */
	public Iterator getServiceReferences() {
		lockSupport.sharedLock();
		try {
			Set servRefs = new HashSet(services.size());
			for (Iterator it = services.iterator(); it.hasNext();) {
				Service service = (Service) it.next();
				servRefs.add(service.getServiceReference());
			}
			return new ReadOnlyIterator(servRefs);
		} finally {
			lockSupport.releaseSharedLock();
		}
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.ws4d.java.service.Device#getServiceReferences(org.ws4d.java.types
	 * .QNameSet)
	 */
	public Iterator getServiceReferences(QNameSet servicePortTypes) {
		Set matchingServRefs = new HashSet();
		addServiceReferences(matchingServRefs, servicePortTypes);
		return new ReadOnlyIterator(matchingServRefs);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.ws4d.java.service.LocalDevice#addMatchingServiceRefs(org.ws4d.java
	 * .structures.DataStructure, org.ws4d.java.types.QNameSet)
	 */
	public void addServiceReferences(DataStructure to, QNameSet servicePortTypes) {
		lockSupport.sharedLock();
		try {
			for (Iterator it = services.iterator(); it.hasNext();) {
				Service service = (Service) it.next();
				if (servicePortTypes.isContainedBy(service.getPortTypes())) {
					to.add(service.getServiceReference());
				}
			}
		} finally {
			lockSupport.releaseSharedLock();
		}
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.ws4d.java.service.Device#getServiceReference(org.ws4d.java.types.URI)
	 */
	public ServiceReference getServiceReference(URI serviceId) {
		if (serviceId == null) {
			return null;
		}
		String searchedServiceId = serviceId.toString();

		lockSupport.sharedLock();
		try {
			for (Iterator it = services.iterator(); it.hasNext();) {
				Service service = (Service) it.next();
				if (searchedServiceId.equals(service.getServiceId().toString())) {
					return service.getServiceReference();
				}
			}
		} finally {
			lockSupport.releaseSharedLock();
		}

		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.ws4d.java.service.Device#getServiceReference(org.ws4d.java.types.
	 * EndpointReference)
	 */
	public ServiceReference getServiceReference(EndpointReference serviceEpr) {
		if (serviceEpr == null) {
			return null;
		}

		lockSupport.sharedLock();
		try {
			for (Iterator it = services.iterator(); it.hasNext();) {
				Service service = (Service) it.next();
				for (Iterator it2 = service.getEprInfos(); it2.hasNext();) {
					EprInfo eprInfo = (EprInfo) it2.next();
					if (serviceEpr.equals(eprInfo.getEndpointReference())) {
						return service.getServiceReference();
					}
				}
			}
		} finally {
			lockSupport.releaseSharedLock();
		}

		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.service.Device#getXAddresses()
	 */
	public Iterator getXAddressInfos() {
		lockSupport.sharedLock();
		try {
			XAddressInfoSet xAddrs = discoveryData.getXAddressInfoSet();
			return xAddrs == null ? EmptyStructures.EMPTY_ITERATOR : new ReadOnlyIterator(xAddrs.iterator());
		} finally {
			lockSupport.releaseSharedLock();
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.service.LocalDevice#getDiscoveryData()
	 */
	public DiscoveryData getDiscoveryData() {
		return discoveryData;
	}

	/**
	 * Does the device use the default discovery domains for send multicast
	 * discovery messages. If <code>true</code> => hello and bye messages will
	 * be sent via the static output domains in {@link Discovery}. If
	 * <code>false</code> => hello and bye will be sent to this device's
	 * {@link #addOutputDiscoveryDomain(ProtocolDomain) explicitly configured
	 * domains}.
	 * 
	 * @return If <code>true</code> => hello and bye messages will be sent via
	 *         the static output domains in {@link Discovery}. If
	 *         <code>false</code> => hello and bye will be sent to this device's
	 *         configured output domains.
	 */
	public boolean isUsingDefaultDiscoveryDomains() {
		return outputDiscoveryDomains.size() == 0;
	}

	/**
	 * Adds the specified protocol domain to this device. The domain will be
	 * used for sending discovery messages (hellos and byes), in case
	 * {@link #isUsingDefaultDiscoveryDomains()} returns <code>false</code>.
	 * 
	 * @param domain the new protocol domain to add to this device
	 * @see #isUsingDefaultDiscoveryDomains()
	 * @see #setUsingDefaultDiscoveryDomains(boolean)
	 */
	public void addOutputDiscoveryDomain(ProtocolDomain domain) {
		if (domain == null) {
			return;
		}
		outputDiscoveryDomains.add(domain);
	}

	/**
	 * Removes a previously {@link #addOutputDiscoveryDomain(ProtocolDomain)
	 * added} output domain from this device.
	 * 
	 * @param domain the output domain to remove
	 * @see #isUsingDefaultDiscoveryDomains()
	 * @see #setUsingDefaultDiscoveryDomains(boolean)
	 */
	public void removeOutputDiscoveryDomain(ProtocolDomain domain) {
		outputDiscoveryDomains.remove(domain);
	}

	/**
	 * Gets device configuration properties. The device properties are built up
	 * while reading a configuration file/stream by the {@link Properties}
	 * class.
	 * <p>
	 * While constructing this device, the device properties were used to set
	 * the device data. Changes of the device data afterwards will not be
	 * transmitted to the properties.
	 * </p>
	 * 
	 * @return properties The properties of device created whilst reading the
	 *         configuration file/stream.
	 */
	public DeviceProperties getDeviceProperties() {
		return deviceProp;
	}

	/**
	 * Gets the configuration id. The configuration id maps to the device
	 * properties within the configuration file/stream. The device can be
	 * constructed by {@link #DefaultDevice(int)} which specifies the
	 * configuration id. The default id is -1, which doesn't map to any
	 * configuration.
	 * 
	 * @return The configuration id of the device. If it is -1, no configuration
	 *         id was specified.
	 */
	public int getConfigurationID() {
		return configurationId;
	}

	/**
	 * Checks if this device matches the searched device port types and scopes.
	 * To match the device both the port types and the scopes must be part of
	 * the device.
	 * 
	 * @param searchTypes Searched device port types to match the device.
	 * @param searchScopes Searched scopes to match the device.
	 * @return <code>true</code> - if both the given device port types and
	 *         scopes are part of the device.
	 */
	public boolean deviceMatches(QNameSet searchTypes, ProbeScopeSet searchScopes) {
		QNameSet deviceTypes = discoveryData.getTypes();
		if (searchTypes == null || searchTypes.isEmpty() || (deviceTypes != null && deviceTypes.containsAll(searchTypes))) {
			// check scopes
			if (searchScopes != null && !searchScopes.isEmpty()) {
				ScopeSet scopes = discoveryData.getScopes();
				if (scopes == null || scopes.isEmpty() || !scopes.containsAll(searchScopes)) {
					return false;
				}
			}
			return true;
		}
		return false;
	}

	/**
	 * 
	 */
	private void copyDiscoveryDataIfRunning() {
		if (running && !discoveryDataChanged) {
			discoveryData = new DiscoveryData(discoveryData);
			myDeviceRef.setDiscoveryData(discoveryData);
			discoveryDataChanged = true;
		}
	}

	/**
	 * Creates a wsa:Hello message for the given device.
	 * 
	 * @return the wsa:Hello message.
	 */
	private HelloMessage createHelloMessage() {
		// Copy discovery data! And filter types with priorities.
		DiscoveryData d = new DiscoveryData(discoveryData);
		QName[] qarray = QNameSet.sortPrioritiesAsArray(d.getTypes());
		if (qarray != null) {
			int j = Math.min(qarray.length, MAX_QNAME_SERIALIZATION);
			QNameSet nTypes = new QNameSet(j);
			for (int i = 0; i < j; i++) {
				nTypes.add(qarray[i]);
			}
			d.setTypes(nTypes);
		} else {
			Log.warn("Sending wsd:Hello message without any types (e.g DPWS)! Maybe nobody will accept this message, set correct types!");
		}
		HelloMessage hello = new HelloMessage(d, CommunicationManager.ID_NULL);
		hello.getHeader().setAppSequence(appSequencer.getNext());
		if (isSecure()) {
			hello.setSecure(true);
			hello.setCertificate(this.getCertificate());
			hello.setPrivateKey(this.getPrivateKey());
		}
		return hello;
	}

	private DataStructure getOutputDiscoveryDomains() {
		if (isUsingDefaultDiscoveryDomains()) {
			return Discovery.getDefaultOutputDomains();
		} else {
			return outputDiscoveryDomains;
		}
	}

	public void setDiscoveryProxy(boolean isDiscoveryProxy) {
		this.isDiscoveryProxy = isDiscoveryProxy;
	}

	public boolean isDiscoveryProxy() {
		return isDiscoveryProxy;
	}

	private final class DeviceMessageListener extends DefaultIncomingMessageListener {

		Device	ownerDevice	= null;

		private DeviceMessageListener(Device dd) {
			super();
			ownerDevice = dd;
		}

		/*
		 * (non-Javadoc)
		 * @see
		 * org.ws4d.java.communication.DefaultIncomingMessageListener#handle
		 * (org.ws4d.java.message.metadata.GetMessage,
		 * org.ws4d.java.communication.ProtocolData)
		 */
		public GetResponseMessage handle(GetMessage get, ProtocolData protocolData) throws SOAPException {
			lockSupport.sharedLock();
			try {
				GetResponseMessage response = new GetResponseMessage(protocolData.getCommunicationManagerId());
				response.setResponseTo(get);

				// set DPWSVersion from the Request to the Response
				response.setProtocolInfo(get.getProtocolInfo());

				response.setThisModel(modelMetadata);
				response.setThisDevice(deviceMetadata);
				RelationshipMData relationship = new RelationshipMData();

				// the host part
				HostMData host = new HostMData();
				host.setEndpointReference(getEndpointReference());
				host.setTypes(discoveryData.getTypes());
				relationship.setHost(host);

				// the hosted parts
				Iterator it = getServices();
				while (it.hasNext()) {
					HostedMData hosted = new HostedMData();
					Service service = (Service) it.next();

					/*
					 * Filter endpoint references which are not transport
					 * addresses. DPWS specification 2.5 R0042
					 */
					Iterator eprsCurrent = service.getEprInfos();
					EprInfoSet eprsFiltered = new EprInfoSet();
					while (eprsCurrent.hasNext()) {
						EprInfo epr = (EprInfo) eprsCurrent.next();
						if (epr.getXAddress() != null) {
							eprsFiltered.add(epr);
						}
					}
					hosted.setEprInfoSet(eprsFiltered);
					Iterator typesCurrent = service.getPortTypes();
					QNameSet typesFilled = new QNameSet();
					while (typesCurrent.hasNext()) {
						QName name = (QName) typesCurrent.next();
						typesFilled.add(name);
					}
					hosted.setTypes(typesFilled);
					hosted.setServiceId(service.getServiceId());
					relationship.addHosted(hosted);
				}

				CommunicationManager comMan = DPWSFramework.getCommunicationManager(protocolData.getCommunicationManagerId());
				CommunicationUtil comUtil = comMan.getCommunicationUtil();
				ConstantsHelper helper = comUtil.getHelper(get.getProtocolInfo().getVersion());

				response.addRelationship(relationship, helper);
				if (hasCustomizeMData) {
					response.addCustomizeMetaData(CustomizeMData.getInstance());
				}
				return response;
			} finally {
				lockSupport.releaseSharedLock();
			}
		}

		/*
		 * (non-Javadoc)
		 * @see
		 * org.ws4d.java.communication.DefaultIncomingMessageListener#handle
		 * (org.ws4d.java.message.discovery.ProbeMessage,
		 * org.ws4d.java.communication.ProtocolData)
		 */
		public ProbeMatchesMessage handle(ProbeMessage probe, ProtocolData protocolData) throws SOAPException {
			if (messageIdBuffer.containsOrEnqueue(probe.getMessageId())) {
				if (Log.isDebug()) {
					Log.debug("Discarding probe message! Already saw this one!", Log.DEBUG_LAYER_APPLICATION);
				}
				return null;
			}
			lockSupport.sharedLock();
			try {

				if (deviceMatches(probe.getTypes(), probe.getScopes())) {
					ProbeMatchesMessage response = new ProbeMatchesMessage(protocolData.getCommunicationManagerId());
					response.setResponseTo(probe);
					response.getHeader().setAppSequence(appSequencer.getNext());

					// set DPWSVersion from the Request to the Response
					response.setProtocolInfo(probe.getProtocolInfo());

					ProbeMatch probeMatch = new ProbeMatch();
					probeMatch.setEndpointReference(getEndpointReference());
					probeMatch.setMetadataVersion(getMetadataVersion());
					QNameSet matchTypes;
					ScopeSet matchScopes;

					if (DPWSFramework.hasModule(DPWSFramework.SECURITY_MODULE) && ownerDevice.isSecure()) {
						response.setSecure(true);
						response.setPrivateKey(ownerDevice.getPrivateKey());
						response.setCertificate(ownerDevice.getCertificate());
					}

					QNameSet supportedDeviceTypes = DPWSFramework.getCommunicationManager(protocolData.getCommunicationManagerId()).getDeviceTypes();
					if (probe.isDirected()) {
						/*
						 * directed probe probe! Add all known types and scopes.
						 */
						matchTypes = discoveryData.getTypes();
						if (matchTypes != null) {
							matchTypes.addAll(supportedDeviceTypes);
						} else {
							matchTypes = new QNameSet(supportedDeviceTypes);
						}
						matchScopes = discoveryData.getScopes();
					} else {
						/*
						 * for general UDP probes, we may reduce the number of
						 * included types, scopes and xAddresses. At this point
						 * its necessary to answer with types which are requests
						 * by the search. So we need to check the matches and
						 * priorities here. We do not answer with ALL types
						 * anymore.
						 */
						QNameSet searchedTypes = probe.getTypes();
						QName[] discoveryDataTypes = QNameSet.sortPrioritiesAsArray(discoveryData.getTypes());
						// add all device types and searched types we matched
						matchTypes = new QNameSet(supportedDeviceTypes);
						if (searchedTypes != null) {
							for (int i = 0; i < discoveryDataTypes.length; i++) {
								if (searchedTypes.contains(discoveryDataTypes[i])) {
									matchTypes.add(discoveryDataTypes[i]);
								}
							}
						}
						// add other types by priority
						for (int i = 0; i < discoveryDataTypes.length && matchTypes.size() <= MAX_QNAME_SERIALIZATION; i++) {
							matchTypes.add(discoveryDataTypes[i]);
						}

						// add scopes that matched
						matchScopes = new ProbeScopeSet();
						ProbeScopeSet searchedScopes = probe.getScopes();
						ScopeSet discoveryDataScopeSet = discoveryData.getScopes();
						if (discoveryDataScopeSet != null && !discoveryDataScopeSet.isEmpty()) {
							String[] discoveryDataScopes = discoveryData.getScopes().getScopesAsStringArray();
							if (searchedScopes != null) {

								for (int k = 0; k < discoveryDataScopes.length; k++) {
									// TODO: scope matching rule (MatchBy) has
									// to be considered
									if (searchedScopes.contains(discoveryDataScopes[k])) {
										matchScopes.addScope(discoveryDataScopes[k]);
									}
								}
							}
							for (int k = 0; k < discoveryDataScopes.length && matchScopes.size() <= MAX_QNAME_SERIALIZATION; k++) {
								matchScopes.addScope(discoveryDataScopes[k]);
							}
						}
					}

					probeMatch.setTypes(matchTypes);
					// TODO: scopes with prio
					probeMatch.setScopes(matchScopes);
					probeMatch.setXAddresInfoSet(discoveryData.getXAddressInfoSet());
					response.addProbeMatch(probeMatch);
					return response;
				} else if (probe.isDirected()) {
					// always return empty ProbeMatches message when directed
					ProbeMatchesMessage matches = new ProbeMatchesMessage(protocolData.getCommunicationManagerId());
					matches.setResponseTo(probe);

					// set DPWSVersion from the Request to the Response
					matches.setProtocolInfo(probe.getProtocolInfo());

					return matches;
				}
				return null;
			} finally {
				lockSupport.releaseSharedLock();
			}
		}

		/*
		 * (non-Javadoc)
		 * @see
		 * org.ws4d.java.communication.DefaultIncomingMessageListener#handle
		 * (org.ws4d.java.message.discovery.ResolveMessage,
		 * org.ws4d.java.communication.ProtocolData)
		 */
		public ResolveMatchesMessage handle(ResolveMessage resolve, ProtocolData protocolData) {
			lockSupport.sharedLock();
			try {
				if (resolve.getEndpointReference() != null && resolve.getEndpointReference().equals(getEndpointReference())) {
					ResolveMatchesMessage response = new ResolveMatchesMessage(protocolData.getCommunicationManagerId());
					response.setResponseTo(resolve);
					response.getHeader().setAppSequence(appSequencer.getNext());

					// set DPWSVersion from the Request to the Response
					response.setProtocolInfo(resolve.getProtocolInfo());

					ResolveMatch match = new ResolveMatch();
					match.setEndpointReference(getEndpointReference());
					match.setMetadataVersion(getMetadataVersion());
					match.setTypes(discoveryData.getTypes());
					match.setScopes(discoveryData.getScopes());
					match.setXAddresInfoSet(discoveryData.getXAddressInfoSet());
					response.setResolveMatch(match);
					return response;
				}
				return null;
			} finally {
				lockSupport.releaseSharedLock();
			}
		}

	}

	/**
	 * Manages the application sequence of device.
	 */
	public class AppSequenceManager {

		/** Seconds till era when device started */
		private long	instanceId		= 0;

		// private URI sequenceId; // optional

		/** last send message number */
		private long	messageNumber	= 0;

		/**
		 * Resets application sequence
		 */
		public void reset() {
			instanceId = System.currentTimeMillis() / 1000;
			messageNumber = 0;
		}

		/**
		 * Increments message number by one and returns AppSequence with this;
		 * 
		 * @return
		 */
		public AppSequence getNext() {
			messageNumber++;
			return new AppSequence(instanceId, messageNumber);
		}

	}

	public void setDefaultNamespace(String ns) {
		namespace = ns;
	}

	public String getDefaultNamespace() {
		return namespace;
	}

	public boolean isValid() {
		return true;
	}

	public void invalidate() {
		// void
	}

	public HTTPGroup getGroup() {
		return userGroup;
	}

	public void addUser(HTTPUser user) {
		if (userGroup == null) {
			userGroup = new HTTPGroup();
		}
		userGroup.addUser(user);
	}

	public void addGroup(HTTPGroup group) {
		if (userGroup == null) {
			userGroup = group;
		} else {
			// TODO mehrere Gruppen hinzufügen können
		}
	}

	/**
	 * @see org.ws4d.java.service.Device#readCustomizeMData()
	 */
	public String readCustomizeMData() {
		return myDeviceRef.getCustomMData();

	}

	/**
	 * @see org.ws4d.java.service.LocalDevice#writeCustomizeMData(HashMap)
	 */
	public void writeCustomizeMData(HashMap metaData) {

		hasCustomizeMData = true;

		this.mdata = metaData;

		Iterator keys = mdata.keySet().iterator();
		while (keys.hasNext()) {
			QName element = (QName) keys.next();
			Object value = mdata.get(element);
			CustomizeMData.getInstance().addNewElement(element, value);
		}

	}

	/**
	 * @see org.ws4d.java.service.Device#hasCustomizeMData()
	 */
	public boolean hasCustomizeMData() {

		if (readCustomizeMData() != null)
			return true;
		else
			return false;
	}

}
