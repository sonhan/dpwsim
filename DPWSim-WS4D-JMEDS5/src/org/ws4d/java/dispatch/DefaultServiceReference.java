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

import org.ws4d.java.DPWSFramework;
import org.ws4d.java.communication.CommunicationManager;
import org.ws4d.java.communication.Discovery;
import org.ws4d.java.communication.ProtocolData;
import org.ws4d.java.communication.ProtocolInfo;
import org.ws4d.java.communication.ResponseCallback;
import org.ws4d.java.communication.TimeoutException;
import org.ws4d.java.message.discovery.ResolveMessage;
import org.ws4d.java.message.metadata.GetMetadataMessage;
import org.ws4d.java.service.LocalService;
import org.ws4d.java.service.ProxyFactory;
import org.ws4d.java.service.Service;
import org.ws4d.java.service.reference.DeviceReference;
import org.ws4d.java.service.reference.Reference;
import org.ws4d.java.service.reference.ServiceListener;
import org.ws4d.java.service.reference.ServiceReference;
import org.ws4d.java.structures.ArrayList;
import org.ws4d.java.structures.DataStructure;
import org.ws4d.java.structures.EmptyStructures;
import org.ws4d.java.structures.HashMap;
import org.ws4d.java.structures.HashSet;
import org.ws4d.java.structures.Iterator;
import org.ws4d.java.structures.LinkedList;
import org.ws4d.java.structures.List;
import org.ws4d.java.structures.ReadOnlyIterator;
import org.ws4d.java.types.EndpointReference;
import org.ws4d.java.types.EndpointReferenceSet;
import org.ws4d.java.types.EprInfo;
import org.ws4d.java.types.EprInfoSet;
import org.ws4d.java.types.HostedMData;
import org.ws4d.java.types.QNameSet;
import org.ws4d.java.types.URI;
import org.ws4d.java.types.URISet;
import org.ws4d.java.types.XAddressInfo;
import org.ws4d.java.util.IDGenerator;
import org.ws4d.java.util.Log;
import org.ws4d.java.wsdl.WSDL;

/**
 * Class holds service reference.
 */
public class DefaultServiceReference implements ServiceReferenceInternal {

	private static final int				STATE_NEW				= 0;

	private static final int				STATE_NEEDS_UPDATE		= 1;

	private static final int				STATE_UP_TO_DATE		= 2;

	private static final int				SYNC_WAITTIME			= 5000;

	private static final int				SYNC_WAITRETRY			= 5;

	private int								currentState			= STATE_NEW;

	private Service							service					= null;

	private HostedMData						hosted					= null;

	int										hostedBlockVersion		= 0;

	// list of EprInfos
	List									resolvedEprInfos		= null;

	// list of EndpointReferences
	List									unresolvedEPRs			= null;

	int										currentXAddressIndex	= -1;

	private DeviceReference					parentDevRef			= null;

	// a set of EndpointReferences pointing at the metadata locations
	private DataStructure					metadataReferences		= null;

	// a set of URIs pointing at the metadata locations
	private DataStructure					metadataLocations		= null;

	// a set of WSDLs belonging to the service
	private DataStructure					wsdls					= null;

	EprInfo									preferredXAddressInfo;

	private int								location				= LOCATION_UNKNOWN;

	private boolean							secure					= false;

	private ServiceReferenceEventRegistry	eventAnnouncer			= ServiceReferenceEventRegistry.getInstance();

	GetMetadataRequestSynchronizer			getMetadataSynchronizer	= null;

	ResolveRequestSynchronizer				resolveSynchronizer		= null;

	HashMap									synchronizers			= new HashMap();

	/**
	 * Constructor, used for proxy services.
	 * 
	 * @param service
	 */
	protected DefaultServiceReference(HostedMData hosted, String comManId, ProtocolData protocolData) {
		if (DPWSFramework.hasModule(DPWSFramework.SECURITY_MODULE)) {
			Iterator iter = hosted.getEprInfoSet().iterator();
			while (iter.hasNext()) {
				URI address = ((EprInfo) iter.next()).getXAddress();
				if (address != null && DPWSFramework.getSecurityManager().isHTTPS(address)) {
					this.setSecureService(true);
					break;
				}
			}
		}
		setHostedFromDevice(hosted, comManId, protocolData);
	}

	/**
	 * Constructor. Unknown location type of service.
	 * 
	 * @param epr
	 */
	protected DefaultServiceReference(EndpointReference epr, String comManId) {
		this.hosted = new HostedMData();
		EprInfo eprInfo = new EprInfo(epr, comManId);
		EprInfoSet eprInfoSet = new EprInfoSet();
		eprInfoSet.add(eprInfo);
		hosted.setEprInfoSet(eprInfoSet);
		if (eprInfo.getXAddress() != null) {
			resolvedEprInfos = new ArrayList();
			resolvedEprInfos.add(eprInfo);
			if (DPWSFramework.hasModule(DPWSFramework.SECURITY_MODULE)) {
				this.secure = DPWSFramework.getSecurityManager().isHTTPS(eprInfo.getXAddress());
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public synchronized String toString() {
		StringBuffer sb = new StringBuffer("DefaultServiceReference [ hosted=");
		sb.append(hosted);
		String loc = (location == LOCATION_UNKNOWN ? "unknown" : (location == LOCATION_REMOTE ? "remote" : "local"));
		sb.append(", location=").append(loc);
		if (location != LOCATION_LOCAL) {
			sb.append(", address=").append(preferredXAddressInfo);
		}
		sb.append(", service=").append(service);
		sb.append(" ]");
		return sb.toString();
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.service.reference.ServiceReference#getService()
	 */
	public Service getService() throws TimeoutException {
		return getService(true);
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.service.reference.ServiceReference#getService(boolean)
	 */
	public Service getService(boolean doBuildUp) throws TimeoutException {
		GetMetadataRequestSynchronizer sync = null;
		boolean havePendingSync = false;
		EprInfo xAddressInfo = null;
		synchronized (this) {
			if (location == LOCATION_LOCAL) {
				return service;
			}
			DeviceServiceRegistry.updateServiceReferenceInGarbageList(this);
			if (!doBuildUp || currentState == STATE_UP_TO_DATE) {
				return service;
			}

			if (getMetadataSynchronizer != null) {
				sync = getMetadataSynchronizer;
				havePendingSync = true;
			} else {
				// createProxyServiceFromLocalMetadata();

				sync = getMetadataSynchronizer = new GetMetadataRequestSynchronizer(hostedBlockVersion);
			}
		}

		if (havePendingSync) {
			return waitForService(sync);
		}

		xAddressInfo = (EprInfo) getPreferredXAddressInfo();

		// check whether there is a newer GetMetadata attempt
		GetMetadataRequestSynchronizer newerSync;
		synchronized (this) {
			newerSync = getMetadataSynchronizer;
		}
		if (newerSync != sync) {
			try {
				sync.service = getService(true);
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
			return sync.service;
		}

		synchronized (this) {
			synchronizers.put(sendGetMetadata(xAddressInfo).getMessageId(), sync);
		}

		return waitForService(sync);
	}

	/**
	 * 
	 */
	synchronized Service createProxyServiceFromLocalMetadata() {
		QNameSet types = hosted == null ? null : hosted.getTypes();
		if (types != null && !types.isEmpty()) {
			// hosted block is not empty, we can try a local WSDL load
			try {
				ProxyFactory pFac = DPWSFramework.getProxyFactory();
				service = pFac.createProxyService(this, null);
				// nice! :-) now let's see whether we have a service ID
				URI serviceId = hosted.getServiceId();
				if (serviceId == null) {
					// set to a "faked" one
					serviceId = IDGenerator.getUUIDasURI();
					hosted.setServiceId(serviceId);
				}
				eventAnnouncer.announceServiceCreated(this, service);

				return service;
			} catch (MissingMetadataException e) {
				/*
				 * some port types not found within local repo :( try obtaining
				 * service metadata
				 */
			} catch (IOException e) {
				Log.error("Cannot create service proxy from local metadata. " + e.getMessage());
			}
		}
		return null;
	}

	/**
	 * Instructs this service reference to asynchronously send a GetMetadata
	 * message to the service and create a new proxy, if required. The new proxy
	 * service is than announced asynchronously via
	 * {@link ServiceListener#serviceCreated(ServiceReference, Service)} method.
	 * <p>
	 * Note that in order to reduce network traffic a GetMetadata message will
	 * actually be sent only if it is detected that the service within this
	 * device reference instance is not up to date anymore.
	 */
	public void buildUpService() {
		GetMetadataRequestSynchronizer sync;
		synchronized (this) {
			if (getMetadataSynchronizer != null) {
				return;
			}
			sync = getMetadataSynchronizer = new GetMetadataRequestSynchronizer(hostedBlockVersion);
		}
		buildUpService(sync);
	}

	private void buildUpService(final GetMetadataRequestSynchronizer newSynchronizer) {
		EprInfo xAddressInfo = null;
		synchronized (this) {
			if (getMetadataSynchronizer != newSynchronizer) {
				return;
			}
			xAddressInfo = preferredXAddressInfo;
			if (xAddressInfo != null) {
				synchronizers.put(sendGetMetadata(xAddressInfo).getMessageId(), newSynchronizer);
				return;
			}
		}

		// start new thread for resolving
		DPWSFramework.getThreadPool().execute(new Runnable() {

			public void run() {
				try {
					EprInfo xAddressInfo = (EprInfo) getPreferredXAddressInfo();
					boolean callNotify = true;
					synchronized (DefaultServiceReference.this) {
						if (newSynchronizer == getMetadataSynchronizer) {
							synchronizers.put(sendGetMetadata(xAddressInfo).getMessageId(), newSynchronizer);
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
					Log.warn("Unablte to resolve remote service: " + e.getMessage());
				}
			}

		});
	}

	private Service waitForService(GetMetadataRequestSynchronizer sync) throws TimeoutException {
		while (true) {
			synchronized (sync) {
				int i = 0;
				while (sync.pending) {
					try {
						sync.wait(SYNC_WAITTIME);
						i++;
						if (i >= SYNC_WAITRETRY) {
							throw new TimeoutException("Service has not send an answer within " + (SYNC_WAITTIME * SYNC_WAITRETRY) + "ms.");
						}
					} catch (InterruptedException e) {
						Log.printStackTrace(e);
					}
				}

				if (sync.exception != null) {
					throw sync.exception;
				} else if (sync.service != null) {
					return sync.service;
				}
				/*
				 * else { this means we had a concurrent update and someone was
				 * started to obtain a newer device }
				 */
			}

			synchronized (this) {
				if (currentState == STATE_UP_TO_DATE) {
					return service;
				} else if (getMetadataSynchronizer != null) {
					sync = getMetadataSynchronizer;
				} else {
					throw new TimeoutException("Unknown communication error with service.");
				}
			}
		}
	}

	/**
	 * 
	 */
	GetMetadataMessage sendGetMetadata(EprInfo xAddress) {
		/*
		 * must be called while we hold the lock on this service reference
		 * instance
		 */
		GetMetadataMessage getMetadata = new GetMetadataMessage(xAddress.getComManId());
		getMetadata.getHeader().setEndpointReference(xAddress.getEndpointReference());
		getMetadata.setTargetXAddressInfo(xAddress);
		ProtocolInfo version = xAddress.getProtocolInfo();

		if (version == null && parentDevRef != null) {
			version = parentDevRef.getPreferredXAddressInfo().getProtocolInfo();
		}
		getMetadata.setProtocolInfo(version);

		ResponseCallback handler = ServiceReferenceFactory.getInstance().newResponseCallbackForServiceReference(this, xAddress);
		OutDispatcher.getInstance().send(getMetadata, xAddress, handler);
		return getMetadata;
	}

	private ResolveMessage sendResolve(EndpointReference eprToResolve) {
		/*
		 * communication manager ID is null, because we must resolve that
		 * endpoint reference and don't which communication manager will be
		 * used.
		 */
		ResolveMessage resolve = new ResolveMessage(CommunicationManager.ID_NULL);

		// resolve.setProtocolVersionInfo(ProtocolVersionInfoRegistry.get(eprToResolve));
		resolve.setEndpointReference(eprToResolve);
		ResponseCallback handler = ServiceReferenceFactory.getInstance().newResponseCallbackForServiceReference(this, null);
		OutDispatcher.getInstance().send(resolve, null, Discovery.getDefaultOutputDomains(), handler);
		return resolve;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.ws4d.java.dispatch.ServiceReferenceInternal#setService(org.ws4d.java
	 * .service.LocalService, org.ws4d.java.types.HostedMData)
	 */
	public synchronized Service setService(LocalService service, HostedMData hosted) {
		Service oldService = this.service;
		this.service = service;
		if (service != null) {
			this.hosted = hosted;
			if (location == LOCATION_UNKNOWN) {
				location = LOCATION_LOCAL;
			}
			eventAnnouncer.announceServiceCreated(this, service);
		} else {
			eventAnnouncer.announceServiceDisposed(this);
		}

		return oldService;
	}

	public Service rebuildService() throws TimeoutException {
		reset();
		return getService();
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.service.reference.ServiceReference#reset()
	 */
	public synchronized void reset() {
		if (location == LOCATION_LOCAL) {
			Log.warn("DefaultServiceReference.reset: Not allowed to reset references to local services!");
			return;
		}
		/*
		 * add reset() method, which drops the old service proxy and other
		 * metadata
		 */
		if (hosted != null) {
			// remove all service-related metadata but the EPRs
			hosted.setServiceId(null);
			hosted.setTypes(null);
		}
		hostedBlockVersion = 0;
		// xAddresses reset to initial state
		resolvedEprInfos = null;
		currentXAddressIndex = -1;
		unresolvedEPRs = null;
		currentState = STATE_NEEDS_UPDATE;

		parentDevRef = null;
		metadataReferences = null;
		metadataLocations = null;
		wsdls = null;
		preferredXAddressInfo = null;
		location = LOCATION_UNKNOWN;
		secure = false;
		DeviceServiceRegistry.addServiceReferenceToGarbageList(this);
		if (service != null) {
			service = null;
			eventAnnouncer.announceServiceDisposed(this);
		}
	}

	/**
	 * Update service references with hosted metadata. If new metadata lacks of
	 * previous transmitted port types, the associated service is removed. If
	 * new metadata includes new port types, service is updated.
	 * 
	 * @param endpoint Endpoint references to set.
	 */
	public void update(HostedMData newHostedBlock, DeviceReference devRef, ProtocolData protocolData) {
		synchronized (this) {
			if (newHostedBlock == hosted) {
				parentDevRef = devRef;
				return;
			}
			if (location == LOCATION_LOCAL) {
				Log.error("ServiceReferenceHandler.update: location is local");
				return;
			}
			location = LOCATION_REMOTE;
			parentDevRef = devRef;
			DeviceServiceRegistry.updateServiceReferenceRegistration(newHostedBlock, this);
		}

		if (!newHostedBlock.getServiceId().equals(hosted.getServiceId())) {
			Log.info("ServiceReferenceHandler.update: Updating a service reference with a different service id: " + newHostedBlock.getServiceId());
		}

		setHostedFromDevice(newHostedBlock, protocolData.getCommunicationManagerId(), protocolData);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.ws4d.java.dispatch.ServiceReferenceInternal#disconnectFromDevice()
	 */
	public synchronized void disconnectFromDevice() {
		if (parentDevRef != null) {
			parentDevRef = null;
			DeviceServiceRegistry.addServiceReferenceToGarbageList(this);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.service.reference.ServiceReference#getPortTypes()
	 */
	public synchronized Iterator getPortTypes() {
		QNameSet names = hosted.getTypes();
		return names == null ? EmptyStructures.EMPTY_ITERATOR : new ReadOnlyIterator(names.iterator());
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.service.reference.ServiceReference#getPortTypeCount()
	 */
	public synchronized int getPortTypeCount() {
		QNameSet names = hosted.getTypes();
		return names == null ? 0 : names.size();
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.ws4d.java.service.reference.ServiceReference#containsAllPortTypes
	 * (org.ws4d.java.types.QNameSet)
	 */
	public synchronized boolean containsAllPortTypes(QNameSet newTypes) {
		if (newTypes == null || newTypes.size() == 0) {
			return true;
		}
		QNameSet ownPortTypes = hosted == null ? null : hosted.getTypes();
		if (ownPortTypes == null) {
			return false;
		}
		if (newTypes.size() > ownPortTypes.size()) {
			return false;
		}
		return ownPortTypes.containsAll(newTypes);
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.service.reference.Reference#getLocation()
	 */
	public synchronized int getLocation() {
		return location;
	}

	/**
	 * Location of service, which this reference is linked to. Allowed values:
	 * <nl>
	 * <li> {@link Reference#LOCATION_LOCAL},
	 * <li> {@link Reference#LOCATION_REMOTE} or
	 * <li> {@link Reference#LOCATION_UNKNOWN}
	 * </nl>
	 * 
	 * @param location {@link Reference#LOCATION_LOCAL},
	 *            {@link Reference#LOCATION_REMOTE} or
	 *            {@link Reference#LOCATION_UNKNOWN}.
	 */
	public synchronized void setLocation(int location) {
		this.location = location;
	}


	/* (non-Javadoc)
	 * @see org.ws4d.java.service.reference.ServiceReference#getEprInfos()
	 */
	public synchronized Iterator getEprInfos() {
		EprInfoSet eprs = hosted.getEprInfoSet();
		return eprs == null ? EmptyStructures.EMPTY_ITERATOR : new ReadOnlyIterator(eprs.iterator());
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.service.reference.ServiceReference#getServiceId()
	 */
	public synchronized URI getServiceId() {
		return hosted.getServiceId();
	}

	/**
	 * Returns an iterator over the set of {@link EndpointReference} instances
	 * pointing at the locations of the target service's metadata descriptions
	 * (i.e. usually its WSDL files).
	 * 
	 * @return an iterator over {@link EndpointReference}s to the service's
	 *         metadata
	 */
	public synchronized Iterator getMetadataReferences() {
		return metadataReferences == null ? EmptyStructures.EMPTY_ITERATOR : new ReadOnlyIterator(metadataReferences);
	}

	/**
	 * Returns an iterator over the set of {@link URI} instances pointing at the
	 * addresses of the target service's metadata description locations (i.e.
	 * usually its WSDL files).
	 * 
	 * @return an iterator over {@link URI}s to the service's metadata
	 */
	public synchronized Iterator getMetadataLocations() {
		return metadataLocations == null ? EmptyStructures.EMPTY_ITERATOR : new ReadOnlyIterator(metadataLocations);
	}

	/**
	 * Returns an iterator over the set of {@link WSDL} instances describing the
	 * target service.
	 * 
	 * @return an iterator over {@link WSDL}s containing the service's metadata
	 */
	public synchronized Iterator getWSDLs() {
		return wsdls == null ? EmptyStructures.EMPTY_ITERATOR : new ReadOnlyIterator(wsdls);
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.service.reference.Reference#getPreferredXAddress()
	 */
	public synchronized URI getPreferredXAddress() throws TimeoutException {
		return getPreferredXAddressInfo().getXAddress();
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.ws4d.java.service.reference.Reference#getPreferredXAddressProtocol()
	 */
	public synchronized String getPreferredCommunicationManagerID() throws TimeoutException {
		return getPreferredXAddressInfo().getComManId();
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.ws4d.java.dispatch.ServiceReferenceInternal#getPreferredXAddressInfo
	 * ()
	 */
	public XAddressInfo getPreferredXAddressInfo() throws TimeoutException {
		ResolveRequestSynchronizer sync;
		synchronized (this) {
			if (preferredXAddressInfo != null) {
				return preferredXAddressInfo;
			}
			if (resolvedEprInfos != null && currentXAddressIndex < resolvedEprInfos.size() - 1) {
				return preferredXAddressInfo = (EprInfo) resolvedEprInfos.get(++currentXAddressIndex);
			}
			sync = resolveSynchronizer;
			if (sync == null) {
				if (unresolvedEPRs == null || unresolvedEPRs.size() == 0) {
					if (hosted != null) {
						for (Iterator it = hosted.getEprInfoSet().iterator(); it.hasNext();) {
							EprInfo eprInfo = (EprInfo) it.next();
							EndpointReference epr = eprInfo.getEndpointReference();
							if (epr.getAddress().isURN() || eprInfo.getXAddress() == null) {
								if (unresolvedEPRs == null) {
									unresolvedEPRs = new LinkedList();
								}
								unresolvedEPRs.add(epr);
							}
						}
					}
					currentXAddressIndex = -1;
					throw new TimeoutException("No more options to obtain transport address for service.");
				}
				sync = resolveSynchronizer = new ResolveRequestSynchronizer(hostedBlockVersion);
				synchronizers.put(sendResolve((EndpointReference) unresolvedEPRs.remove(0)).getMessageId(), sync);
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
				} else if (sync.xAddress != null) {
					return sync.xAddress;
				}
				/*
				 * else { this means we had a concurrent update and someone was
				 * started to obtain a newer address }
				 */
			}

			synchronized (this) {
				if (preferredXAddressInfo != null) {
					return preferredXAddressInfo;
				} else if (resolveSynchronizer != null) {
					sync = resolveSynchronizer;
				} else {
					break;
				}
			}
		}
		return getPreferredXAddressInfo();
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.dispatch.ServiceReferenceInternal#
	 * getNextXAddressInfoAfterFailure(org.ws4d.java.types.URI)
	 */
	public XAddressInfo getNextXAddressInfoAfterFailure(URI transportAddress) throws TimeoutException {
		synchronized (this) {
			if (preferredXAddressInfo != null) {
				URI address = preferredXAddressInfo.getXAddress();
				if (transportAddress.equals(address)) {
					preferredXAddressInfo = null;
				}
			}
		}
		return getPreferredXAddressInfo();
	}

	/**
	 * @param devRef
	 */
	public synchronized void setParentDeviceReference(DeviceReference devRef) {
		this.parentDevRef = devRef;
	}

	public synchronized DeviceReference getParentDeviceRef() {
		return parentDevRef;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.ws4d.java.service.reference.ServiceReference#isServiceObjectExisting
	 * ()
	 */
	public synchronized boolean isServiceObjectExisting() {
		return service != null;
	}

	public synchronized boolean isSecureService() {
		return secure;
	}

	/**
	 * If this service uses security techniques this should be used to set the
	 * state
	 * 
	 * @param sec
	 */
	synchronized void setSecureService(boolean sec) {
		this.secure = sec;
	}

	/**
	 * This method intentionally only creates new or updates existing port types
	 * and never deletes obsolete ones.
	 * 
	 * @throws MissingMetadataException
	 */
	void checkAndUpdateService(ProtocolData protocolData) throws MissingMetadataException {
		ProxyFactory pFac;
		try {
			pFac = DPWSFramework.getProxyFactory();

			if (service == null) {
				// service gets filled from WSDL(s) referenced within msg
				Service newService = pFac.createProxyService(this, protocolData);
				service = newService;
				currentState = STATE_UP_TO_DATE;
				ServiceReferenceEventRegistry.getInstance().announceServiceCreated(this, newService);
			} else if (currentState == STATE_NEEDS_UPDATE) {
				// update existing service.
				QNameSet portTypes = hosted.getTypes();
				if (portTypes != null) {
					currentState = STATE_UP_TO_DATE;
					if (pFac.checkServiceUpdate(service, portTypes)) {
						eventAnnouncer.announceServiceChanged(this, service);
					}
				} else {
					currentState = STATE_UP_TO_DATE;
				}
			}
		} catch (IOException e) {
			Log.error(e.getMessage());
		}
	}

	private synchronized void setHostedFromDevice(HostedMData newHosted, String comManId, ProtocolData protocolData) {
		if (newHosted.isEqualTo(hosted)) {
			return;
		}
		checkPortTypeIncompatibilityAndUpdate(newHosted);

		hosted = newHosted;
		resetTransportAddresses(comManId, protocolData);
	}

	public synchronized void setHostedFromService(HostedMData newHosted, String comManId, ProtocolData protocolData) {
		if (newHosted.isEqualTo(hosted)) {
			return;
		}
		if (hosted == null) {
			hosted = newHosted;
			currentState = STATE_NEEDS_UPDATE;
		} else {
			if (checkPortTypeIncompatibilityAndUpdate(newHosted)) {
				hosted = newHosted;
				resetTransportAddresses(comManId, protocolData);
			} else {
				EprInfoSet oldEprs = hosted.getEprInfoSet();
				hosted = newHosted;
				OUTER: for (Iterator it = newHosted.getEprInfoSet().iterator(); it.hasNext();) {
					EprInfo eprInfo = (EprInfo) it.next();
					if (oldEprs.contains(eprInfo)) {
						continue;
					}
					EndpointReference epr = eprInfo.getEndpointReference();
					URI address = epr.getAddress();
					if (address.isURN() || eprInfo.getXAddress() == null) {
						if (unresolvedEPRs == null) {
							unresolvedEPRs = new LinkedList();
						}
						if (!unresolvedEPRs.contains(epr)) {
							unresolvedEPRs.add(epr);
						}
					} else {
						if (resolvedEprInfos == null) {
							resolvedEprInfos = new ArrayList();
						}
						for (Iterator it2 = resolvedEprInfos.iterator(); it2.hasNext();) {
							EprInfo oldInfo = (EprInfo) it2.next();
							if (oldInfo.getXAddress().equals(address)) {
								continue OUTER;
							}
						}
						resolvedEprInfos.add(eprInfo);
					}
				}
			}
		}
	}

	/**
	 * @param newHosted
	 * @return <code>true</code> only if there are incompatible changes to the
	 *         service's port types, i.e. some previously existing port types
	 *         are gone now, <code>false</code> in any other case
	 */
	private boolean checkPortTypeIncompatibilityAndUpdate(HostedMData newHosted) {
		QNameSet newTypes = newHosted.getTypes();
		if (hosted != null && ((hosted.getTypes() != null && newTypes == null) || (newTypes != null && !newTypes.containsAll(hosted.getTypes())))) {
			// CASE: some types are no more supported => discard service
			service = null;
			currentState = STATE_NEEDS_UPDATE;
			eventAnnouncer.announceServiceDisposed(this);
			return true;
		} else {
			QNameSet oldTypes = hosted == null ? null : hosted.getTypes();
			int oldTypesCount = oldTypes == null ? 0 : oldTypes.size();
			if (oldTypesCount < (newTypes == null ? 0 : newTypes.size())) {
				currentState = STATE_NEEDS_UPDATE;
			}
			return false;
		}	
	}

	/**
	 * @param comManId
	 * @param protocolData
	 */
	private void resetTransportAddresses(String comManId, ProtocolData protocolData) {
		hostedBlockVersion++;
		currentXAddressIndex = -1;
		resolvedEprInfos = null;
		unresolvedEPRs = null;
		resolveSynchronizer = null;
		getMetadataSynchronizer = null;
		for (Iterator it = hosted.getEprInfoSet().iterator(); it.hasNext();) {
			EprInfo eprInfo = (EprInfo) it.next();
			URI address = eprInfo.getEndpointReference().getAddress();
			if (address.isURN() || eprInfo.getXAddress() == null) {
				if (unresolvedEPRs == null) {
					unresolvedEPRs = new LinkedList();
				}
				unresolvedEPRs.add(eprInfo.getEndpointReference());
			} else {
				if (resolvedEprInfos == null) {
					resolvedEprInfos = new ArrayList();
				}
				if (protocolData != null && protocolData.sourceMatches(address)) {
					resolvedEprInfos.add(0, eprInfo);
				} else {
					// TODO: protocolData is null ... is it okay for us? :D
					resolvedEprInfos.add(eprInfo);
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.ws4d.java.dispatch.ServiceReferenceInternal#setMetaDataLocations(org
	 * .ws4d.java.types.URISet)
	 */
	public synchronized void setMetaDataLocations(URISet metaLocs) {
		if (metadataLocations == null) {
			metadataLocations = new HashSet();
		} else {
			metadataLocations.clear();
		}
		if (metaLocs != null) {
			for (Iterator it = metaLocs.iterator(); it.hasNext();) {
				URI location = (URI) it.next();
				metadataLocations.add(location);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.ws4d.java.dispatch.ServiceReferenceInternal#setMetadataReferences
	 * (org.ws4d.java.types.EndpointReferenceSet)
	 */
	public synchronized void setMetadataReferences(EndpointReferenceSet metaRefs) {
		if (metadataReferences == null) {
			metadataReferences = new HashSet();
		} else {
			metadataReferences.clear();
		}
		if (metaRefs != null) {
			for (Iterator it = metaRefs.iterator(); it.hasNext();) {
				EndpointReference epr = (EndpointReference) it.next();
				metadataReferences.add(epr);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.ws4d.java.dispatch.ServiceReferenceInternal#setWSDLs(org.ws4d.java
	 * .structures.DataStructure)
	 */
	public synchronized void setWSDLs(DataStructure wsdls) {
		if (this.wsdls == null) {
			this.wsdls = new HashSet();
		} else {
			this.wsdls.clear();
		}
		if (wsdls != null) {
			for (Iterator it = wsdls.iterator(); it.hasNext();) {
				WSDL wsdl = (WSDL) it.next();
				this.wsdls.add(wsdl);
			}
		}
	}

	static class RequestSynchronizer {

		final int			hostedBlockVersion;

		TimeoutException	exception;

		volatile boolean	pending	= true;

		RequestSynchronizer(int hostedBlockVersion) {
			this.hostedBlockVersion = hostedBlockVersion;
		}

	}

	static class ResolveRequestSynchronizer extends RequestSynchronizer {

		EprInfo	xAddress;

		ResolveRequestSynchronizer(int hostedBlockVersion) {
			super(hostedBlockVersion);
		}

	}

	static class GetMetadataRequestSynchronizer extends RequestSynchronizer {

		Service	service;

		GetMetadataRequestSynchronizer(int hostedBlockVersion) {
			super(hostedBlockVersion);
		}

	}

}
