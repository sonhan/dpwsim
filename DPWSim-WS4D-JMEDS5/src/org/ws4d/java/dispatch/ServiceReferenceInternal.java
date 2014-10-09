package org.ws4d.java.dispatch;

import org.ws4d.java.communication.ProtocolData;
import org.ws4d.java.communication.TimeoutException;
import org.ws4d.java.configuration.DispatchingProperties;
import org.ws4d.java.service.LocalService;
import org.ws4d.java.service.Service;
import org.ws4d.java.service.reference.DeviceReference;
import org.ws4d.java.service.reference.Reference;
import org.ws4d.java.service.reference.ServiceReference;
import org.ws4d.java.structures.DataStructure;
import org.ws4d.java.types.EndpointReferenceSet;
import org.ws4d.java.types.HostedMData;
import org.ws4d.java.types.URI;
import org.ws4d.java.types.URISet;
import org.ws4d.java.types.XAddressInfo;
import org.ws4d.java.wsdl.WSDLRepository;

/**
 * Interface covers the methods used internally to manage service references.
 */
public interface ServiceReferenceInternal extends ServiceReference {

	/**
	 * Gets present service of reference. If the service is remote and its proxy
	 * not built up, the proxy may be built by an existing WSDL within the
	 * {@link WSDLRepository} or by sending a get metadata message.
	 * 
	 * @param doBuildUp If <code>false</code> and service does not exist,
	 *            service will not be build up. In this case, returned service
	 *            is <code>null</code>
	 * @return service The present service of this reference. The returned
	 *         service may change.
	 * @throws TimeoutException This exception is thrown if the remote service
	 *             doesn't answer to the get metadata message, which tries to
	 *             receive the necessary data to build up the service. The
	 *             timeout value can be configured in the
	 *             {@link DispatchingProperties} via the method
	 *             {@link DispatchingProperties#setResponseWaitTime(int)}.
	 */
	public Service getService(boolean doBuildUp) throws TimeoutException;

	/**
	 * Sets local service, replaces present service. Used to set local services.
	 * 
	 * @param service replacement service.
	 * @param hosted the hosted block of the service
	 * @return replaced service.
	 */
	public Service setService(LocalService service, HostedMData hosted);

	/**
	 * Update service references with hosted metadata. If new metadata lacks of
	 * previous transmitted port types, the associated service is removed. If
	 * new metadata includes new port types, service is updated.
	 * 
	 * @param endpoint Endpoint references to set.
	 */
	public void update(HostedMData newHostedBlock, DeviceReference devRef, ProtocolData protocolData);

	/**
	 * Removes the parent device reference from this service reference.
	 */
	public void disconnectFromDevice();

	/**
	 * @param devRef
	 */
	public void setParentDeviceReference(DeviceReference devRef);

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
	public void setLocation(int location);

	/**
	 * @param metaLocs
	 */
	public void setMetaDataLocations(URISet metaLocs);

	/**
	 * Updates metadata references.
	 * 
	 * @param metaRefs
	 */
	public void setMetadataReferences(EndpointReferenceSet metaRefs);

	/**
	 * Updates WSDLs linked to this service
	 * 
	 * @param wsdls
	 */
	public void setWSDLs(DataStructure wsdls);

	public XAddressInfo getPreferredXAddressInfo() throws TimeoutException;

	public XAddressInfo getNextXAddressInfoAfterFailure(URI transportAddress) throws TimeoutException;

}
