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

import org.ws4d.java.communication.ProtocolData;
import org.ws4d.java.communication.TimeoutException;
import org.ws4d.java.dispatch.DefaultDeviceReference;
import org.ws4d.java.dispatch.DeviceServiceRegistry;
import org.ws4d.java.dispatch.ServiceReferenceInternal;
import org.ws4d.java.message.metadata.GetResponseMessage;
import org.ws4d.java.service.reference.DeviceReference;
import org.ws4d.java.service.reference.ServiceReference;
import org.ws4d.java.structures.DataStructure;
import org.ws4d.java.structures.EmptyStructures;
import org.ws4d.java.structures.HashMap;
import org.ws4d.java.structures.HashSet;
import org.ws4d.java.structures.Iterator;
import org.ws4d.java.structures.ReadOnlyIterator;
import org.ws4d.java.structures.Set;
import org.ws4d.java.types.DiscoveryData;
import org.ws4d.java.types.EndpointReference;
import org.ws4d.java.types.EprInfo;
import org.ws4d.java.types.HostedMData;
import org.ws4d.java.types.QNameSet;
import org.ws4d.java.types.URI;
import org.ws4d.java.util.Log;

/**
 * Proxy class of a (remote) dpws device
 */
public class ProxyDevice extends DeviceCommons {

	/** Device reference of this device */
	private DeviceReference	deviceReference		= null;

	/** List of service references attached to this device */
	private Set				serviceReferences	= null;

	private boolean			valid				= true;

	/**
	 * Constructor. Constructs device proxy by get response message.
	 * 
	 * @param message Holds information about discovered device.
	 */
	public ProxyDevice(GetResponseMessage message, DefaultDeviceReference devRef, Device oldDevice, ProtocolData protocolData) {
		super(message.getThisModel(), message.getThisDevice());
		deviceReference = devRef;

		this.setSecure(devRef.isSecureDevice());

		// host block updated in dev ref handler
		DataStructure hostedList = message.getHosted();
		if (hostedList == null) {
			return;
		}

		serviceReferences = new HashSet(hostedList.size());
		// HostMData host = message.getRelationship().getHost();

		HashMap oldServiceRefsMap = null;
		if (oldDevice != null) {
			Iterator it;
			if (oldDevice instanceof ProxyDevice) {
				ProxyDevice proxy = (ProxyDevice) oldDevice;
				Set oldRefs = proxy.serviceReferences;
				oldServiceRefsMap = new HashMap(oldRefs.size());
				it = oldRefs.iterator();
			} else {
				it = oldDevice.getServiceReferences();
				oldServiceRefsMap = new HashMap();
			}
			while (it.hasNext()) {
				ServiceReference serviceRef = (ServiceReference) it.next();
				oldServiceRefsMap.put(serviceRef.getServiceId(), serviceRef);
			}
		}

		for (Iterator hostedMDataIter = hostedList.iterator(); hostedMDataIter.hasNext();) {
			/*
			 * build up services, references
			 */
			HostedMData hosted = (HostedMData) hostedMDataIter.next();

			for (Iterator eprImfoIter = hosted.getEprInfoSet().iterator(); eprImfoIter.hasNext();) {
				EprInfo serviceEpr = (EprInfo) eprImfoIter.next();
				if (serviceEpr.getProtocolInfo() == null || serviceEpr.isProtocolInfoNotDependable()) {
					serviceEpr.mergeProtocolInfo(protocolData.getProtocolInfo());
					serviceEpr.setProtocolInfoNotDependable(true);
				}
			}
			
			ServiceReferenceInternal servRef;
			if (oldServiceRefsMap != null) {
				URI serviceId = hosted.getServiceId();
				servRef = (ServiceReferenceInternal) oldServiceRefsMap.remove(serviceId);
				if (servRef == null) {
					servRef = (ServiceReferenceInternal) DeviceServiceRegistry.getUpdatedServiceReference(hosted, devRef, protocolData.getCommunicationManagerId(), protocolData);
				} else {
					servRef.update(hosted, devRef, protocolData);
				}
			} else {
				servRef = (ServiceReferenceInternal) DeviceServiceRegistry.getUpdatedServiceReference(hosted, devRef, protocolData.getCommunicationManagerId(), protocolData);
			}
			serviceReferences.add(servRef);
		}
		if (oldServiceRefsMap != null) {
			for (Iterator it = oldServiceRefsMap.values().iterator(); it.hasNext();) {
				ServiceReferenceInternal serviceRef = (ServiceReferenceInternal) it.next();
				serviceRef.disconnectFromDevice();
			}
		}
	}

	// --------------------------------------------------------

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.service.ServiceModifiableImpl#isRemote()
	 */
	public boolean isRemote() {
		return true;
	}

	// --------------------- DISCOVERY DATA --------------------

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.service.Device#getEndpointReferences()
	 */
	public EndpointReference getEndpointReference() {
		return deviceReference.getEndpointReference();
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.service.Device#getPortTypes()
	 */
	public Iterator getPortTypes() {
		try {
			return deviceReference.getDevicePortTypes(false);
		} catch (TimeoutException e) {
			Log.printStackTrace(e);
		}
		return EmptyStructures.EMPTY_ITERATOR;

	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.service.Device#getMetadataVersion()
	 */
	public long getMetadataVersion() {
		try {
			return deviceReference.getMetadataVersion(false);
		} catch (TimeoutException e) {
			Log.printStackTrace(e);
		}
		return DiscoveryData.UNKNOWN_METADATA_VERSION;
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.service.Device#getDeviceReference()
	 */
	public DeviceReference getDeviceReference() {
		return deviceReference;
	}


	/* (non-Javadoc)
	 * @see org.ws4d.java.service.Device#getXAddressInfos()
	 */
	public Iterator getXAddressInfos() {
		try {
			return deviceReference.getXAddressInfos(false);
		} catch (TimeoutException e) {
			Log.printStackTrace(e);
		}
		return EmptyStructures.EMPTY_ITERATOR;
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.service.Device#getScopes()
	 */
	public Iterator getScopes() {
		try {
			return deviceReference.getScopes(false);
		} catch (TimeoutException e) {
			Log.printStackTrace(e);
		}
		return EmptyStructures.EMPTY_ITERATOR;
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.service.Device#getServiceReferences()
	 */
	public Iterator getServiceReferences() {
		return serviceReferences == null ? EmptyStructures.EMPTY_ITERATOR : new ReadOnlyIterator(serviceReferences);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.ws4d.java.service.Device#getServiceReferences(org.ws4d.java.types
	 * .QNameSet)
	 */
	public Iterator getServiceReferences(QNameSet servicePortTypes) {
		if (serviceReferences == null || serviceReferences.size() == 0) {
			return EmptyStructures.EMPTY_ITERATOR;
		}

		Set matchingServRefs = new HashSet(serviceReferences.size());
		addServiceReferences(matchingServRefs, servicePortTypes);
		return new ReadOnlyIterator(matchingServRefs);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.ws4d.java.service.Device#addServiceReferences(org.ws4d.java.structures
	 * .DataStructure, org.ws4d.java.types.QNameSet)
	 */
	public void addServiceReferences(DataStructure to, QNameSet servicePortTypes) {
		if (serviceReferences == null || serviceReferences.size() == 0) {
			return;
		}
		for (Iterator it = serviceReferences.iterator(); it.hasNext();) {
			ServiceReference servRef = (ServiceReference) it.next();
			if (servicePortTypes.isContainedBy(servRef.getPortTypes())) {
				to.add(servRef);
			}
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

		for (Iterator it = serviceReferences.iterator(); it.hasNext();) {
			ServiceReference servRef = (ServiceReference) it.next();
			if (searchedServiceId.equals(servRef.getServiceId().toString())) {
				return servRef;
			}
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

		for (Iterator it = serviceReferences.iterator(); it.hasNext();) {
			ServiceReference servRef = (ServiceReference) it.next();
			for (Iterator it2 = servRef.getEprInfos(); it2.hasNext();) {
				EprInfo eprInfo = (EprInfo) it2.next();
				if (serviceEpr.equals(eprInfo.getEndpointReference())) {
					return servRef;
				}
			}
		}

		return null;
	}

	public String getDefaultNamespace() {
		return null;
	}

	public boolean isValid() {
		return valid;
	}

	public void invalidate() {
		this.valid = false;
	}

	/**
	 * @see org.ws4d.java.service.Device#readCustomizeMData()
	 */
	public String readCustomizeMData() {
		return deviceReference.getCustomMData();

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
