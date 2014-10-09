/*******************************************************************************
 * Copyright (c) 2009 MATERNA Information & Communications. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html. For further
 * project-related information visit http://www.ws4d.org. The most recent
 * version of the JMEDS framework can be obtained from
 * http://sourceforge.net/projects/ws4d-javame.
 ******************************************************************************/

package org.ws4d.java.service.reference;

import org.ws4d.java.communication.TimeoutException;
import org.ws4d.java.configuration.DispatchingProperties;
import org.ws4d.java.dispatch.DeviceServiceRegistry;
import org.ws4d.java.dispatch.ServiceReferenceEventRegistry;
import org.ws4d.java.service.Service;
import org.ws4d.java.structures.Iterator;
import org.ws4d.java.types.EndpointReference;
import org.ws4d.java.types.QNameSet;
import org.ws4d.java.types.URI;
import org.ws4d.java.wsdl.WSDL;
import org.ws4d.java.wsdl.WSDLRepository;

/**
 * Interface of service reference. The service within it can be replaced. Get
 * present service by calling getService() method.
 * <p>
 * Service references are registered in the {@link DeviceServiceRegistry}. There
 * should only be one service reference for each service.
 * </p>
 * Global service listening can be initiated by registration via
 * {@link ServiceReferenceEventRegistry#registerServiceListening(ServiceListener)}
 * , the method
 * {@link ServiceReferenceEventRegistry#unregisterServiceListening(ServiceListener)}
 * removes service listening.
 */
public interface ServiceReference extends Reference {

	/**
	 * Gets present service of reference. If the service is remote and its proxy
	 * not built up, the proxy may be built by an existing WSDL within the
	 * {@link WSDLRepository} or by sending a get metadata message.
	 * 
	 * @return service The present service of this reference. The returned
	 *         service may change.
	 * @throws TimeoutException This exception is thrown if the remote service
	 *             doesn't answer to the get metadata message, which tries to
	 *             receive the necessary data to build up the service. The
	 *             timeout value can be configured in the
	 *             {@link DispatchingProperties} via the method
	 *             {@link DispatchingProperties#setResponseWaitTime(int)}.
	 */
	public abstract Service getService() throws TimeoutException;

	/**
	 * Gets endpoint references. In DPWS the address contained in an endpoint
	 * reference is a transport address.
	 * 
	 * @return endpoint references.
	 */
	public Iterator getEprInfos();

	/**
	 * Gets service port types. The port types defines the operations the
	 * service provides.
	 * 
	 * @return service port types.
	 */
	public Iterator getPortTypes();

	/**
	 * Returns the number of port types for this service reference.
	 * 
	 * @return the number of port types
	 */
	public int getPortTypeCount();

	/**
	 * Returns <code>true</code> only in case this service reference provides
	 * all port types listed within argument<code>portTypes</code>.
	 * 
	 * @param portTypes the port types to check this service reference for
	 * @return <code>true</code> if all port types are provided by this service
	 *         reference, <code>false</code> otherwise
	 */
	public boolean containsAllPortTypes(QNameSet portTypes);

	/**
	 * Returns the service ID (unique within the scope of its parent device).
	 * 
	 * @return the service ID
	 */
	public URI getServiceId();

	/**
	 * Returns an iterator over the set of {@link EndpointReference} instances
	 * pointing at the locations of the target service's metadata descriptions
	 * (i.e. usually its WSDL files).
	 * 
	 * @return an iterator over {@link EndpointReference}s to the service's
	 *         metadata
	 */
	public Iterator getMetadataReferences();

	/**
	 * Returns an iterator over the set of {@link URI} instances pointing at the
	 * addresses of the target service's metadata description locations (i.e.
	 * usually its WSDL files).
	 * 
	 * @return an iterator over {@link URI}s to the service's metadata
	 */
	public Iterator getMetadataLocations();

	/**
	 * Returns an iterator over the set of {@link WSDL} instances describing the
	 * target service.
	 * 
	 * @return an iterator over {@link WSDL}s containing the service's metadata
	 */
	public Iterator getWSDLs();

	/**
	 * Returns the parent device reference of the device which hosts the
	 * service, if the device is known.
	 * 
	 * @return The device reference of the device which hosts the service.
	 */
	public DeviceReference getParentDeviceRef();

	/**
	 * Returns whether or not the service is secured
	 * 
	 * @return
	 */
	public boolean isSecureService();

	/**
	 * Returns whether the service is build up or not.
	 * 
	 * @return <code>true</code>, if service object exists.
	 */
	boolean isServiceObjectExisting();

	/**
	 * Resets this service reference, i.e. makes it loose all internal state
	 * information but the endpoint references of the service.
	 */
	public void reset();

}
