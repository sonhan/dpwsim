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

import java.io.IOException;

import org.ws4d.java.client.AppSequenceBuffer;
import org.ws4d.java.communication.CommunicationManager;
import org.ws4d.java.communication.CommunicationManagerRegistry;
import org.ws4d.java.communication.DefaultIncomingMessageListener;
import org.ws4d.java.communication.DiscoveryBinding;
import org.ws4d.java.communication.IncomingMessageListener;
import org.ws4d.java.communication.ProtocolData;
import org.ws4d.java.concurrency.DeadlockException;
import org.ws4d.java.configuration.DispatchingProperties;
import org.ws4d.java.constants.DPWSMessageConstants;
import org.ws4d.java.message.Message;
import org.ws4d.java.message.discovery.ByeMessage;
import org.ws4d.java.message.discovery.HelloMessage;
import org.ws4d.java.service.LocalDevice;
import org.ws4d.java.service.LocalService;
import org.ws4d.java.service.Service;
import org.ws4d.java.service.reference.DeviceReference;
import org.ws4d.java.service.reference.Reference;
import org.ws4d.java.service.reference.ServiceReference;
import org.ws4d.java.structures.ArrayList;
import org.ws4d.java.structures.DataStructure;
import org.ws4d.java.structures.HashSet;
import org.ws4d.java.structures.Iterator;
import org.ws4d.java.structures.LinkedSet;
import org.ws4d.java.structures.LockedList;
import org.ws4d.java.structures.LockedMap;
import org.ws4d.java.types.AppSequence;
import org.ws4d.java.types.DiscoveryData;
import org.ws4d.java.types.EndpointReference;
import org.ws4d.java.types.EprInfo;
import org.ws4d.java.types.EprInfoSet;
import org.ws4d.java.types.HostedMData;
import org.ws4d.java.types.ProbeScopeSet;
import org.ws4d.java.types.QNameSet;
import org.ws4d.java.types.XAddressInfoSet;
import org.ws4d.java.util.Log;

/**
 * Registry class which manages deviceInvokers, serviceInvokers and their
 * references. Many methods of this class register service and service reference
 * listeners.
 * 
 * @author mspies
 */
public abstract class DeviceServiceRegistry {

	private static final int[]						DEVICE_LIFECYCLE_MESSAGE_TYPES	= { DPWSMessageConstants.HELLO_MESSAGE, DPWSMessageConstants.BYE_MESSAGE };

	private static final IncomingMessageListener	DEVICE_LIFECYCLE_LISTENER		= new IncomingHelloByeListener();

	private static final int						MAX_CACHE_SIZE					= DispatchingProperties.getInstance().getServiceReferenceCacheSize();

	// epr -> devRef
	static final LockedMap							DEVICE_REFS						= new LockedMap();

	// epr -> servRef
	static final LockedMap							SERVICE_REFS					= new LockedMap();

	// default device instances
	private static final LockedList					DEVICES							= new LockedList();

	// default service instances
	private static final LockedList					SERVICES						= new LockedList();

	private static AppSequenceBuffer				appSequenceBuffer				= null;

	private static int								appSequenceBufferUser			= 0;

	/**
	 * Set of service refs ordered by access. Used to determine the eldest
	 * service ref. Holds only references, which are not local and not assigned
	 * to a device.
	 */
	private static final LinkedSet					SERVICE_REFS_GARBAGE_LIST		= new LinkedSet(MAX_CACHE_SIZE, true);

	// ------------------- CONSTRUCTOR -------------------------

	/**
	 * Package-private constructor.
	 */
	private DeviceServiceRegistry() {
		super();
	}

	// public static void init() {
	// GLOBAL_LOCK.exclusiveLock();
	// try {
	// for (Iterator it = CommunicationManagerRegistry.getLoadedManagers();
	// it.hasNext();) {
	// CommunicationManager manager = (CommunicationManager) it.next();
	// try {
	// manager.registerDeviceReference(DEVICE_LIFECYCLE_MESSAGE_TYPES, null,
	// DEVICE_LIFECYCLE_LISTENER);
	// } catch (IOException e) {
	// Log.printStackTrace(e);
	// }
	// }
	// } finally {
	// GLOBAL_LOCK.releaseExclusiveLock();
	// }
	// }

	public static void tearDown() {
		// unregister device lifecycle listener
		for (Iterator it = CommunicationManagerRegistry.getLoadedManagers(); it.hasNext();) {
			CommunicationManager manager = (CommunicationManager) it.next();
			try {
				manager.unregisterDeviceReference(DEVICE_LIFECYCLE_MESSAGE_TYPES, null, DEVICE_LIFECYCLE_LISTENER);
			} catch (IOException e) {
				Log.printStackTrace(e);
			}
		}
		DEVICES.exclusiveLock();
		try {
			int count = DEVICES.size();
			while (count-- > 0) {
				LocalDevice device = (LocalDevice) DEVICES.get(0);
				try {
					// this stops also all service of this device
					device.stop();
				} catch (IOException e) {
					Log.printStackTrace(e);
				}
			}
		} finally {
			DEVICES.releaseExclusiveLock();
		}

		// now stop services, which are NOT on top of device
		SERVICES.exclusiveLock();
		try {
			int count = SERVICES.size();
			while (count-- > 0) {
				LocalService service = (LocalService) SERVICES.get(0);
				try {
					service.stop();
				} catch (IOException e) {
					Log.printStackTrace(e);
				}
			}
		} finally {
			SERVICES.releaseExclusiveLock();
		}
	}

	/**
	 * Get device reference of a remote device. If no device reference
	 * registered for this device, a new one will be created.
	 * 
	 * @param helloData
	 * @return
	 */
	public static DeviceReference getDeviceReference(HelloData helloData) {
		if (helloData == null || helloData.getDiscoveryData() == null) {
			return null;
		}
		DEVICE_REFS.sharedLock();
		boolean sharedLockHold = true;
		try {
			Object o = DEVICE_REFS.get(helloData.getEndpointReference());
			if (o != null) {
				// kkkneu if the device is known, merge the
				// ProtocolVersionInfo!(to set the best fitting DPWSVersion
				DefaultDeviceReference d = (DefaultDeviceReference) o;
				if (d.getPreferredXAddressInfo() != null && d.getPreferredXAddressInfo().getProtocolInfo() != null && helloData.getProtocolData() != null) {
					d.getPreferredXAddressInfo().getProtocolInfo().merge(helloData.getProtocolData().getProtocolInfo());
				}
				return d;
			}

			try {
				DEVICE_REFS.exclusiveLock();
			} catch (DeadlockException e) {
				DEVICE_REFS.releaseSharedLock();
				sharedLockHold = false;
				return getDeviceReference(helloData);
			}
			try {
				// no device reference available, create one
				DefaultDeviceReference devRef = new DefaultDeviceReference(helloData.getAppSequence(), helloData.getDiscoveryData(), helloData.getProtocolData());
				DEVICE_REFS.put(helloData.getEndpointReference(), devRef);
				return devRef;
			} finally {
				DEVICE_REFS.releaseExclusiveLock();
			}

		} finally {
			if (sharedLockHold) {
				DEVICE_REFS.releaseSharedLock();
			}
		}
	}

	/**
	 * Get device reference of a device location is unknown. If no device
	 * reference registered for this device, a new will be created.
	 * 
	 * @param endpointReference
	 * @return
	 */
	public static DeviceReference getDeviceReference(EndpointReference endpointReference) {
		return getDeviceReference(endpointReference, true);
	}

	/**
	 * Returns the device reference to the specified endpoint reference.
	 * 
	 * @param endpointReference Endpoint reference of the device being looked
	 *            for.
	 * @param doCreate If <code>true</code>, reference will be created if not
	 *            already existing.
	 * @return Device reference being looked for.
	 */
	public static DeviceReference getDeviceReference(EndpointReference epr, boolean doCreate) {
		if (epr == null) {
			return null;
		}
		DEVICE_REFS.sharedLock();
		boolean sharedLockHold = true;
		try {
			Object o = DEVICE_REFS.get(epr);
			if (o != null || !doCreate) {
				return (DefaultDeviceReference) o;
			}

			try {
				DEVICE_REFS.exclusiveLock();
			} catch (DeadlockException e) {
				DEVICE_REFS.releaseSharedLock();
				sharedLockHold = false;
				return getDeviceReference(epr, doCreate);
			}
			try {
				// no device reference available, create one
				DefaultDeviceReference devRef = new DefaultDeviceReference(epr);
				DEVICE_REFS.put(epr, devRef);
				return devRef;
			} finally {
				DEVICE_REFS.releaseExclusiveLock();
			}

		} finally {
			if (sharedLockHold) {
				DEVICE_REFS.releaseSharedLock();
			}
		}
	}

	/**
	 * Returns the device reference to the specified endpoint reference.
	 * 
	 * @param endpointReference Endpoint reference of the device being looked
	 *            for.
	 * @param doCreate If <code>true</code>, reference will be created if not
	 *            already existing.
	 * @return Device reference being looked for.
	 */
	public static DeviceReference getDeviceReference(EndpointReference epr, XAddressInfoSet addresses, boolean doCreate) {
		if (epr == null) {
			return null;
		}
		DEVICE_REFS.sharedLock();
		boolean sharedLockHold = true;
		try {
			Object o = DEVICE_REFS.get(epr);
			if (o != null || !doCreate) {
				return (DefaultDeviceReference) o;
			}

			try {
				DEVICE_REFS.exclusiveLock();
			} catch (DeadlockException e) {
				DEVICE_REFS.releaseSharedLock();
				sharedLockHold = false;
				return getDeviceReference(epr, addresses, doCreate);
			}
			try {
				// no device reference available, create one
				DefaultDeviceReference devRef = new DefaultDeviceReference(epr, addresses);
				DEVICE_REFS.put(epr, devRef);
				return devRef;
			} finally {
				DEVICE_REFS.releaseExclusiveLock();
			}

		} finally {
			if (sharedLockHold) {
				DEVICE_REFS.releaseSharedLock();
			}
		}
	}

	/**
	 * Get device reference of a local device. If no device reference registered
	 * for this device, a new will be created.
	 * 
	 * @param device
	 * @return
	 */
	public static DefaultDeviceReference getDeviceReference(LocalDevice device) {
		EndpointReference epr = device.getEndpointReference();
		if (epr == null) {
			return null;
		}

		DEVICE_REFS.sharedLock();
		boolean sharedLockHold = true;
		try {
			Object o = DEVICE_REFS.get(epr);
			if (o != null) {
				DEVICE_REFS.releaseSharedLock();
				sharedLockHold = false;
				DefaultDeviceReference devRef = (DefaultDeviceReference) o;
				/*
				 * if somebody has created a dev ref to an unknown device, and
				 * this local device is now registering.
				 */
				devRef.setLocalDevice(device);
				return devRef;
			}
			try {
				DEVICE_REFS.exclusiveLock();
			} catch (DeadlockException e) {
				DEVICE_REFS.releaseSharedLock();
				sharedLockHold = false;
				return getDeviceReference(device);
			}
			try {
				// no device reference available, create one
				DefaultDeviceReference devRef = new DefaultDeviceReference(device);
				DEVICE_REFS.put(epr, devRef);
				return devRef;
			} finally {
				DEVICE_REFS.releaseExclusiveLock();
			}
		} finally {
			if (sharedLockHold) {
				DEVICE_REFS.releaseSharedLock();
			}
		}
	}

	public static ServiceReference createServiceReference(EndpointReference epr, QNameSet portTypes, String comManId, ProtocolData protocolData) throws DuplicateServiceReferenceException {
		SERVICE_REFS.exclusiveLock();
		try {
			Object o = SERVICE_REFS.get(epr);
			if (o != null) {
				throw new DuplicateServiceReferenceException("Existing service reference with equal endpoint reference found: " + o);
			}

			HostedMData hosted = new HostedMData();
			EprInfoSet eprs = new EprInfoSet();
			eprs.add(new EprInfo(epr, comManId));
			hosted.setEprInfoSet(eprs);
			hosted.setTypes(portTypes);
			ServiceReference serviceRef = ServiceReferenceFactory.getInstance().newServiceReference(hosted, comManId, protocolData);
			addServiceReferenceToGarbageList(serviceRef);
			SERVICE_REFS.put(epr, serviceRef);
			return serviceRef;
		} finally {
			SERVICE_REFS.releaseExclusiveLock();
		}
	}

	public static ServiceReference getUpdatedServiceReference(HostedMData hosted, DeviceReference devRef, String comManId, ProtocolData protocolData) {
		ServiceReferenceInternal servRef = null;
		SERVICE_REFS.sharedLock();
		try {
			servRef = getFirstMatchingServiceReferenceForReuse(hosted);
		} finally {
			SERVICE_REFS.releaseSharedLock();
		}
		if (servRef != null) {
			servRef.update(hosted, devRef, protocolData);
			return servRef;
		}

		SERVICE_REFS.exclusiveLock();
		try {
			servRef = getFirstMatchingServiceReferenceForReuse(hosted);
			if (servRef != null) {
				servRef.update(hosted, devRef, protocolData);
				return servRef;
			}
			servRef = ServiceReferenceFactory.getInstance().newServiceReference(hosted, comManId, protocolData);
			for (Iterator it = hosted.getEprInfoSet().iterator(); it.hasNext();) {
				EprInfo serviceEpr = (EprInfo) it.next();
				SERVICE_REFS.put(serviceEpr.getEndpointReference(), servRef);
			}
			if (devRef != null && servRef.getParentDeviceRef() == null) {
				servRef.setParentDeviceReference(devRef);
			}
			return servRef;
		} finally {
			SERVICE_REFS.releaseExclusiveLock();
		}
	}

	/**
	 * @param hosted
	 * @param servRef
	 * @return
	 */
	private static ServiceReferenceInternal getFirstMatchingServiceReferenceForReuse(HostedMData hosted) {
		for (Iterator it = hosted.getEprInfoSet().iterator(); it.hasNext();) {
			EprInfo serviceEpr = (EprInfo) it.next();
			Object o = SERVICE_REFS.get(serviceEpr.getEndpointReference());
			if (o != null) {
				ServiceReferenceInternal servRef = (ServiceReferenceInternal) o;
				removeServiceReferenceFromGarbageList(servRef);
				return servRef;
			}
		}
		return null;
	}

	/**
	 * Returns the service reference to the specified endpoint reference.
	 * 
	 * @param endpointReference Endpoint reference of the service being looked
	 *            for.
	 * @param doCreate If <code>true</code>, reference will be created if not
	 *            already existing.
	 * @return Service reference being looked for.
	 */
	/**
	 * @param endpointReference
	 * @return
	 */
	public static ServiceReference getServiceReference(EndpointReference endpointReference, String comManId, boolean doCreate) {
		if (endpointReference == null) {
			return null;
		}

		SERVICE_REFS.sharedLock();
		boolean sharedLockHold = true;
		try {
			Object o = SERVICE_REFS.get(endpointReference);
			if (o != null || !doCreate) {
				return (ServiceReference) SERVICE_REFS.get(endpointReference);
			}

			try {
				SERVICE_REFS.exclusiveLock();
			} catch (DeadlockException e) {
				SERVICE_REFS.releaseSharedLock();
				sharedLockHold = false;
				return getServiceReference(endpointReference, comManId, doCreate);
			}
			try {
				ServiceReference serviceRef = ServiceReferenceFactory.getInstance().newServiceReference(endpointReference, comManId);
				addServiceReferenceToGarbageList(serviceRef);
				SERVICE_REFS.put(endpointReference, serviceRef);
				return serviceRef;
			} finally {
				SERVICE_REFS.releaseExclusiveLock();
			}
		} finally {
			if (sharedLockHold) {
				SERVICE_REFS.releaseSharedLock();
			}
		}
	}

	/**
	 * Update the registered endpoint addresses of the service reference.
	 * 
	 * @param newHosted
	 * @param servRef
	 * @return
	 */
	static ServiceReferenceInternal updateServiceReferenceRegistration(HostedMData newHosted, ServiceReferenceInternal servRef) {
		// ServiceReferenceHandler servRef = null;
		EprInfoSet newEprs = newHosted.getEprInfoSet();

		SERVICE_REFS.exclusiveLock();
		try {
			/*
			 * remove all eprs from registry, which are not transmitted
			 */
			for (Iterator it = servRef.getEprInfos(); it.hasNext();) {
				EprInfo epr = (EprInfo) it.next();
				if (!newEprs.contains(epr)) {
					SERVICE_REFS.remove(epr.getEndpointReference());
				}
			}

			/*
			 * add all transmitted eprs
			 */
			for (Iterator it = newEprs.iterator(); it.hasNext();) {
				EprInfo serviceEpr = (EprInfo) it.next();
				SERVICE_REFS.put(serviceEpr.getEndpointReference(), servRef);
			}
		} finally {
			SERVICE_REFS.releaseExclusiveLock();
		}

		return servRef;
	}

	static void addServiceReferenceToGarbageList(ServiceReference servRef) {
		if (servRef == null) {
			return;
		}
		ServiceReference eldest = null;
		synchronized (SERVICE_REFS_GARBAGE_LIST) {
			if (SERVICE_REFS_GARBAGE_LIST.size() >= MAX_CACHE_SIZE) {
				eldest = (ServiceReference) SERVICE_REFS_GARBAGE_LIST.removeFirst();
			}
			SERVICE_REFS_GARBAGE_LIST.add(servRef);
		}
		if (eldest != null) {
			unregisterServiceReference(eldest);
		}
	}

	static void updateServiceReferenceInGarbageList(ServiceReference servRef) {
		synchronized (SERVICE_REFS_GARBAGE_LIST) {
			SERVICE_REFS_GARBAGE_LIST.touch(servRef);
		}
	}

	public static void removeServiceReferenceFromGarbageList(ServiceReference servRef) {
		synchronized (SERVICE_REFS_GARBAGE_LIST) {
			SERVICE_REFS_GARBAGE_LIST.remove(servRef);
		}
	}

	/**
	 * @param deviceTypes
	 * @param scopes
	 * @return
	 */
	public static DataStructure getLocalDeviceReferences(QNameSet deviceTypes, ProbeScopeSet scopes) {
		DEVICES.sharedLock();
		try {
			DataStructure matchingDeviceRefs = new HashSet();

			for (Iterator it = DEVICES.iterator(); it.hasNext();) {
				LocalDevice device = (LocalDevice) it.next();
				if (device.deviceMatches(deviceTypes, scopes)) {
					matchingDeviceRefs.add(device.getDeviceReference());
				}
			}

			return matchingDeviceRefs;
		} finally {
			DEVICES.releaseSharedLock();
		}
	}

	/**
	 * @param serviceTypes
	 * @param deviceTypes
	 * @param scopes
	 * @return
	 */
	public static DataStructure getLocalServiceReferences(QNameSet serviceTypes, QNameSet deviceTypes, ProbeScopeSet scopes) {
		DataStructure matchingServiceRefs = new ArrayList();
		if ((deviceTypes != null && deviceTypes.size() > 0) || scopes != null && scopes.size() > 0) {
			DEVICES.sharedLock();
			try {
				for (Iterator it = DEVICES.iterator(); it.hasNext();) {
					LocalDevice device = (LocalDevice) it.next();
					if (device.deviceMatches(deviceTypes, scopes)) {
						device.addServiceReferences(matchingServiceRefs, serviceTypes);
					}
				}
			} finally {
				DEVICES.releaseSharedLock();
			}
		} else {
			SERVICES.sharedLock();
			try {
				for (Iterator it = SERVICES.iterator(); it.hasNext();) {
					Service service = (Service) it.next();
					ServiceReference servRef = service.getServiceReference();
					if (serviceTypes.isContainedBy(servRef.getPortTypes())) {
						matchingServiceRefs.add(servRef);
					}
				}
			} finally {
				SERVICES.releaseSharedLock();
			}
		}
		return matchingServiceRefs;
	}

	/**
	 * Removes proxy device reference.
	 * 
	 * @param deviceReference proxy device reference to remove.
	 */
	static void unregisterDeviceReference(DefaultDeviceReference deviceReference) {
		if (deviceReference == null) {
			return;
		}
		EndpointReference epr = deviceReference.getEndpointReference();

		DEVICE_REFS.exclusiveLock();
		try {
			if (DEVICE_REFS.remove(epr) == null) {
				return;
			}
		} finally {
			DEVICE_REFS.releaseExclusiveLock();
		}
		// disconnect service references
		deviceReference.disconnectAllServiceReferences(false);
	}

	private static void unregisterServiceReference(ServiceReference servRef) {
		Iterator eprs = servRef.getEprInfos();
		if (!eprs.hasNext()) {
			Log.error("ERROR: DeviceServiceRegistry.unregisterServiceReference0: no epr in service");
			return;
		}

		SERVICE_REFS.exclusiveLock();
		try {
			while (eprs.hasNext()) {
				EprInfo eprInfo = (EprInfo) eprs.next();
				SERVICE_REFS.remove(eprInfo.getEndpointReference());
			}
		} finally {
			SERVICE_REFS.releaseExclusiveLock();
		}
		// invalidate service of servRef
		ServiceReferenceInternal servRefHandler = (ServiceReferenceInternal) servRef;
		// invalidate the service
		servRefHandler.setService(null, null);
		removeServiceReferenceFromGarbageList(servRefHandler);
	}

	// ----------------------------------------------------------

	public static DeviceReference getUpdatedDeviceReference(DiscoveryData newData, Message msg, ProtocolData protocolData) {
		EndpointReference epr = newData.getEndpointReference();

		DEVICE_REFS.sharedLock();
		boolean sharedLockHold = true;
		try {
			DefaultDeviceReference devRef = (DefaultDeviceReference) DEVICE_REFS.get(epr);
			if (devRef != null) {
				DEVICE_REFS.releaseSharedLock();
				sharedLockHold = false;
				if (devRef.getLocation() == Reference.LOCATION_LOCAL || !devRef.checkAppSequence(msg.getAppSequence())) {
					/*
					 * It's our own device or message out of date => nothing to
					 * handle
					 */
					return devRef;
				}

				devRef.updateDiscoveryData(newData, protocolData);
			} else {
				// devRef == null
				try {
					DEVICE_REFS.exclusiveLock();
				} catch (DeadlockException e) {
					DEVICE_REFS.releaseSharedLock();
					sharedLockHold = false;
					return getUpdatedDeviceReference(newData, msg, protocolData);
				}
				try {
					devRef = new DefaultDeviceReference(msg.getAppSequence(), newData, protocolData);
					if (devRef.getPreferredXAddressInfo().getProtocolInfo() != null) {
						devRef.getPreferredXAddressInfo().getProtocolInfo().merge(msg.getProtocolInfo());
					} else {
						devRef.getPreferredXAddressInfo().setProtocolInfo(msg.getProtocolInfo());
					}
					DEVICE_REFS.put(epr, devRef);
				} finally {
					DEVICE_REFS.releaseExclusiveLock();
				}
			}

			devRef.setSecureDevice(msg.isSecure());

			return devRef;
		} finally {
			if (sharedLockHold) {
				DEVICE_REFS.releaseSharedLock();
			}
		}
	}

	public static void register(LocalDevice device) {
		DEVICES.exclusiveLock();
		try {
			if (DEVICES.contains(device)) {
				return;
			}
			DEVICES.add(device);
		} finally {
			DEVICES.releaseExclusiveLock();
		}
	}

	public static void unregister(LocalDevice device) {
		DEVICES.exclusiveLock();
		try {
			DEVICES.remove(device);
		} finally {
			DEVICES.releaseExclusiveLock();
		}
	}

	public static void register(LocalService service) {
		SERVICES.exclusiveLock();
		try {
			if (SERVICES.contains(service)) {
				return;
			}
			SERVICES.add(service);
		} finally {
			SERVICES.releaseExclusiveLock();
		}
	}

	public static void unregister(LocalService service) {
		SERVICES.exclusiveLock();
		try {
			SERVICES.remove(service);
		} finally {
			SERVICES.releaseExclusiveLock();
		}
	}

	public static void register(DiscoveryBinding binding) {
		for (Iterator it = CommunicationManagerRegistry.getLoadedManagers(); it.hasNext();) {
			CommunicationManager manager = (CommunicationManager) it.next();
			try {
				manager.registerDeviceReference(DEVICE_LIFECYCLE_MESSAGE_TYPES, binding, DEVICE_LIFECYCLE_LISTENER);
			} catch (IOException e) {
				Log.printStackTrace(e);
			}
		}
	}

	public static void unregister(DiscoveryBinding binding) {
		// unregister device lifecycle listener
		for (Iterator it = CommunicationManagerRegistry.getLoadedManagers(); it.hasNext();) {
			CommunicationManager manager = (CommunicationManager) it.next();
			try {
				manager.unregisterDeviceReference(DEVICE_LIFECYCLE_MESSAGE_TYPES, binding, DEVICE_LIFECYCLE_LISTENER);
			} catch (IOException e) {
				Log.printStackTrace(e);
			}
		}
	}

	/**
	 * Returns the buffer for the application sequence counter.
	 * 
	 * @return
	 */
	public static synchronized boolean checkAndUpdateAppSequence(EndpointReference ref, AppSequence seq) {
		if (appSequenceBufferUser == 0) {
			return true;
		}
		return appSequenceBuffer.checkAndUpdate(ref, seq);
	}

	public static synchronized void incAppSequenceUser() {
		if (appSequenceBufferUser++ == 0) {
			appSequenceBuffer = new AppSequenceBuffer();
		}
	}

	public static synchronized void decAppSequenceUser() {
		if (appSequenceBufferUser-- == 1) {
			appSequenceBuffer = null;
		} else if (appSequenceBufferUser == -1) {
			appSequenceBufferUser++;
			throw new RuntimeException("Cannot decrease Application Sequence Buffer User.");
		}

	}

	private static class IncomingHelloByeListener extends DefaultIncomingMessageListener {

		/*
		 * (non-Javadoc)
		 * @see
		 * org.ws4d.java.communication.DefaultIncomingMessageListener#handle
		 * (org.ws4d.java.message.discovery.HelloMessage,
		 * org.ws4d.java.communication.CommunicationID)
		 */
		public void handle(HelloMessage hello, ProtocolData protocolData) {
			DiscoveryData newData = hello.getDiscoveryData();
			EndpointReference epr;
			if (newData == null || (epr = newData.getEndpointReference()) == null) {
				return;
			}

			DEVICE_REFS.sharedLock();
			boolean sharedLockHold = true;
			try {
				DefaultDeviceReference devRef = (DefaultDeviceReference) DEVICE_REFS.get(epr);
				if (devRef != null) {
					DEVICE_REFS.releaseSharedLock();
					sharedLockHold = false;
					if (devRef.getLocation() == Reference.LOCATION_LOCAL) {
						/*
						 * It's our own device => nothing to handle
						 */
						return;
					}

					devRef.updateFromHello(hello, protocolData);
				} else {
					// devRef == null
					if (DispatchingProperties.getInstance().isDeviceReferenceAutoBuild()) {
						try {
							DEVICE_REFS.exclusiveLock();
						} catch (DeadlockException e) {
							DEVICE_REFS.releaseSharedLock();
							sharedLockHold = false;
							handle(hello, protocolData);
							return;
						}

						try {
							/*
							 * Build device reference
							 */
							devRef = new DefaultDeviceReference(hello.getAppSequence(), newData, protocolData);
							DEVICE_REFS.put(epr, devRef);
						} finally {
							DEVICE_REFS.releaseExclusiveLock();
						}
						if (Log.isInfo()) {
							Log.info("Set DPWS Version for " + devRef.getEndpointReference().toString() + " to : " + devRef.getPreferredXAddressInfo().getProtocolInfo());
						}
						devRef.setSecureDevice(hello.getHeader().getSignature() != null);

					} else {
						/*
						 * We don't add automatically device references.
						 */
						return;
					}
				}
			} finally {
				if (sharedLockHold) {
					DEVICE_REFS.releaseSharedLock();
				}
			}
		}

		/*
		 * (non-Javadoc)
		 * @see
		 * org.ws4d.java.communication.DefaultIncomingMessageListener#handle
		 * (org.ws4d.java.message.discovery.ByeMessage,
		 * org.ws4d.java.communication.CommunicationID)
		 */
		public void handle(ByeMessage bye, ProtocolData protocolData) {
			EndpointReference epr;
			if (bye == null || (epr = bye.getEndpointReference()) == null) {
				return;
			}

			Object o = DEVICE_REFS.get(epr);
			if (o != null) {
				DefaultDeviceReference devRef = (DefaultDeviceReference) o;

				if (devRef.getLocation() == Reference.LOCATION_LOCAL) {
					/*
					 * local device stops are transmitted by local device
					 */
					return;
				}

				devRef.updateFromBye(bye, protocolData);
			}
		}

	}

}
