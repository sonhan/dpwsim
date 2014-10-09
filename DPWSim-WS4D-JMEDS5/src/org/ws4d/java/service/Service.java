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

import org.ws4d.java.communication.TimeoutException;
import org.ws4d.java.eventing.ClientSubscription;
import org.ws4d.java.eventing.EventSink;
import org.ws4d.java.eventing.EventSource;
import org.ws4d.java.eventing.EventingException;
import org.ws4d.java.service.reference.DeviceReference;
import org.ws4d.java.service.reference.ServiceReference;
import org.ws4d.java.structures.HashMap;
import org.ws4d.java.structures.Iterator;
import org.ws4d.java.types.CustomAttributeValue;
import org.ws4d.java.types.EprInfo;
import org.ws4d.java.types.QName;
import org.ws4d.java.types.URI;
import org.ws4d.java.types.URISet;
import org.ws4d.java.wsdl.WSDL;

/**
 * Interface of service classes representing "DPWS Hosted Services"
 * 
 */
public interface Service {

	/**
	 * Gets service id
	 * 
	 * @return service id
	 */
	public URI getServiceId();

	/**
	 * Returns an iterator of {@link EprInfo}.
	 * 
	 * @return an iterator of {@link EprInfo}.
	 */
	public Iterator getEprInfos();

	/**
	 * Gets service port types.
	 * 
	 * @return service port types, data structure is read only.
	 */
	public Iterator getPortTypes();

	/**
	 * Gets the service reference.
	 * 
	 * @return service reference
	 */
	public ServiceReference getServiceReference();

	/**
	 * Gets device reference of parent device. This method may return
	 * <code>null</code> in case this service doesn't reside on a device or its
	 * underlying device is not known at this time.
	 * 
	 * @return device reference of parent device, may be <code>null</code>
	 */
	public DeviceReference getParentDeviceReference();

	/**
	 * Gets all operations of the specified service port type.
	 * 
	 * @param portType specific port type
	 * @return the operations belonging to the specified port type
	 */
	public Iterator getOperations(QName portType);

	/**
	 * Gets all operations.
	 * 
	 * @return all operations
	 */
	public Iterator getOperations();

	/**
	 * Gets an operation of specified <code>portType</code> with given
	 * <code>opName</code>. The <code>inputName</code> and/or
	 * <code>outputName</code> may be <code>null</code> only if the requested
	 * operation doesn't have any input/output. Otherwise, this method won't get
	 * any results.
	 * <p>
	 * When an operation is added to a service, and it doesn't provide an
	 * explicit name for either its input or output elements (see
	 * {@link Operation#setInputName(String)} and
	 * {@link Operation#setOutputName(String)}), a default name is generated
	 * according to the WSDL 1.1 Specification. As long as there are other
	 * operations with the same input/output name which have already been added
	 * to this service, an incremental index is appended to the default
	 * generated name.
	 * </p>
	 * If you are certain that the operation of the specified
	 * <code>portType</code> and with the given <code>opName</code> is
	 * <em>NOT</em> overloaded (i.e. there is no other operation within the same
	 * port type with the same operation name), then you can use method
	 * {@link #getAnyOperation(QName, String)} instead of this one.
	 * 
	 * @param portType specific port type of operation
	 * @param opName name of operation
	 * @param inputName the name of the input element which belongs to this
	 *            operation according to its WSDL; must be <code>null</code>, if
	 *            operation has no input
	 * @param outputName the name of the output element which belongs to this
	 *            operation according to its WSDL; must be <code>null</code>, if
	 *            operation has no output
	 * @return requested operation or <code>null</code>, if not found
	 * @see #getAnyOperation(QName, String)
	 */
	public Operation getOperation(QName portType, String opName, String inputName, String outputName);

	/**
	 * Gets operation with specified unique <code>inputAction</code>.
	 * 
	 * @param inputAction the WS-Addressing action URI of the requested
	 *            operation according to its WSDL
	 * @return requested operation or <code>null</code>, if not found
	 */
	public Operation getOperation(String inputAction);

	/**
	 * Returns the first (or last, or <em>ANY</em> other) operation with the
	 * specified <code>portType</code> and <code>operationName</code>. Note that
	 * there might be more than one operation with the same name and port type
	 * in the case of overloading. In such situations, it is the user's
	 * responsibility to determine which of the overloaded versions was returned
	 * by this method. This method returns <code>null</code>, if there are no
	 * operations matching the <code>portType</code> and
	 * <code>operationName</code> arguments.
	 * <p>
	 * This method is useful in case it is known that there is only one
	 * operation with a given name within a port type.
	 * 
	 * @param portType the port type to which the desired operation
	 * @param operationName the name of the operation
	 * @return any (maybe randomly determined) operation with the given port
	 *         type and name
	 * @see #getOperation(QName, String, String, String)
	 */
	public Operation getAnyOperation(QName portType, String operationName);

	/**
	 * Gets all event sources of the specified service port type.
	 * 
	 * @param portType specific port type
	 * @return all event sources belonging to the specified service port type
	 */
	public Iterator getEventSources(QName portType);

	/**
	 * Gets all events sources.
	 * 
	 * @return all event sources
	 */
	public Iterator getEventSources();

	/**
	 * Gets event source of specified <code>portType</code> with given
	 * <code>eventName</code>. The <code>inputName</code> and/or
	 * <code>outputName</code> may be <code>null</code> only if the requested
	 * event source doesn't have any input/output. Otherwise, this method won't
	 * find any results. *
	 * <p>
	 * When an event source is added to a service, and it doesn't provide an
	 * explicit name for either its input or output elements (see
	 * {@link DefaultEventSource#setInputName(String)} and
	 * {@link DefaultEventSource#setOutputName(String)}), a default name is
	 * therefore generated according to the WSDL 1.1 Specification. If there are
	 * other event sources with the same input/output name already added to this
	 * service, an incremental index is appended to the default generated name.
	 * </p>
	 * If you are certain that the event source of the specified
	 * <code>portType</code> and with the given <code>eventName</code> is
	 * <em>NOT</em> overloaded (i.e. there is no other event source within the
	 * same port type with the same event name), then you can use method
	 * {@link #getAnyEventSource(QName, String)} instead of this one.
	 * 
	 * @param portType specific port type of operation
	 * @param eventName name of event source
	 * @param inputName the name of the input element that belongs to this event
	 *            source according to its WSDL; must be <code>null</code>, if
	 *            event source has no input
	 * @param outputName the name of the output element that belongs to this
	 *            event source according to its WSDL; must be <code>null</code>,
	 *            if event source has no output
	 * @return requested event source or <code>null</code>, if not found
	 * @see #getAnyEventSource(QName, String)
	 */
	public EventSource getEventSource(QName portType, String eventName, String inputName, String outputName);

	/**
	 * Gets event source with specified unique <code>outputAction</code>.
	 * 
	 * @param outputAction the WS-Addressing action URI of the requested event
	 *            source according to its WSDL
	 * @return requested event or <code>null</code> if not found
	 */
	public EventSource getEventSource(String outputAction);

	/**
	 * Returns the first (or last, or <em>ANY</em> other) event source with the
	 * specified <code>portType</code> and <code>eventName</code>. Note that
	 * there might be more than one event source with the same name and port
	 * type in the case of overloading. In such situations, it is the user's
	 * responsibility to determine which of the overloaded versions was returned
	 * by this method. This method returns <code>null</code>, if there are no
	 * event source matching the <code>portType</code> and
	 * <code>eventName</code> arguments.
	 * <p>
	 * This method is useful in case it is known that there is only one event
	 * source with a given name within a port type.
	 * 
	 * @param portType the port type the desired event source belongs to
	 * @param eventName the name of the event source
	 * @return any (maybe randomly determined) event source with the given port
	 *         type and name
	 * @see #getEventSource(QName, String, String, String)
	 */
	public EventSource getAnyEventSource(QName portType, String eventName);

	/**
	 * Is the service remote (proxy) or local?
	 * 
	 * @return whether this is a remote service (proxy) or not
	 */
	public boolean isRemote();

	// ---------------------------- EVENTING RELATED ---------------------------

	/**
	 * Initializes event receiving from specified event sender.
	 * 
	 * @param sink event sink which will receive the notifications.
	 * @param clientSubscriptionId
	 * @param eventActionURIs a set of action URIs to subscribe to
	 * @param duration duration in millis of subscription. If 0, subscription
	 *            does not expire.
	 * @return subscription id (wse:identifier)
	 * @throws EventingException
	 * @throws TimeoutException
	 */
	// FIXME make slim
	public ClientSubscription subscribe(EventSink sink, String clientSubscriptionId, URISet eventActionURIs, long duration) throws EventingException, TimeoutException;

	/**
	 * Unsubscribe specified subscription.
	 * 
	 * @param subscription subscription to terminate.
	 * @throws EventingException
	 * @throws TimeoutException
	 */
	public void unsubscribe(ClientSubscription subscription) throws EventingException, TimeoutException;

	/**
	 * Renews an existing subscription with a new duration. If duration is "0",
	 * subscription never terminates.
	 * 
	 * @param subscription
	 * @param duration
	 * @throws EventingException
	 * @throws TimeoutException
	 */
	public long renew(ClientSubscription subscription, long duration) throws EventingException, TimeoutException;

	/**
	 * Returns the duration in milliseconds until expiration of the specified
	 * client subscription.
	 * 
	 * @param subscription
	 * @return
	 * @throws EventingException
	 * @throws TimeoutException
	 */
	public long getStatus(ClientSubscription subscription) throws EventingException, TimeoutException;

	/**
	 * Returns an iterator over all WSDLs directly attached to this service.
	 * 
	 * @return an iterator over all WSDLs directly attached to this service
	 */
	public Iterator getDescriptions();

	/**
	 * Returns a WSDL document describing this service by the given namespace.
	 * This method makes a recursive search within all WSDLs directly attached
	 * to that service.
	 * 
	 * @param targetNamespace the namespace.
	 * @return the WSDL document describing this service by the given namespace.
	 */
	public WSDL getDescription(String targetNamespace);

	/**
	 * Returns the value of the port type attribute with the given
	 * <code>name</code> for the port type with the specified unique
	 * <code>portTypeName</code> or with <code>null</code> if this attribute is
	 * not available (or if its value is explicitly set to <code>null</code>).
	 * <p>
	 * This method throws a <code>java.lang.IllegalArgumentException</code> if a
	 * port type with the given <code>portTypeName</code> is not found within
	 * this service instance.
	 * </p>
	 * 
	 * @param portTypeName the unique name of the port type within the scope of
	 *            this service instance, see {@link #getPortTypes()}
	 * @param attributeName the name of the port type attribute to query the
	 *            value of
	 * @return the value for the named port type attribute or <code>null</code>
	 * @throws IllegalArgumentException if no port type with the given
	 *             <code>portTypeName</code> is found
	 */
	public CustomAttributeValue getPortTypeAttribute(QName portTypeName, QName attributeName);

	/**
	 * Returns all port type attributes explicitly set on this service instance
	 * for the port type with the given unique <code>portTypeName</code>. Note
	 * that depending on the actual implementation the returned reference may
	 * point at the 'life map', i .e. the actual storage for the port type
	 * attributes. Thus, modifications to that map should be performed with care
	 * and keeping this in mind.
	 * <p>
	 * This method throws a <code>java.lang.IllegalArgumentException</code> if a
	 * port type with the given <code>portTypeName</code> is not found within
	 * this instance.
	 * </p>
	 * 
	 * @param portTypeName the unique name of the port type within the scope of
	 *            this instance, see {@link #getPortTypes()}
	 * @return all already set port type attributes
	 * @throws IllegalArgumentException if no port type with the given
	 *             <code>portTypeName</code> is found
	 */
	public HashMap getPortTypeAttributes(QName portTypeName);

	/**
	 * Returns <code>true</code> only if this service instance has at least one
	 * port type attribute set for the port type with the specified unique
	 * <code>portTypeName</code>. Returns <code>false</code> in any other case,
	 * including when there is no port type with the given
	 * <code>portTypeName</code>.
	 * 
	 * @param portTypeName the unique name of the port type within the scope of
	 *            this service instance, see {@link #getPortTypes()}
	 * @return <code>true</code> only if there is at least one port type
	 *         attribute set for the named port type within this service
	 *         instance
	 */
	public boolean hasPortTypeAttributes(QName portTypeName);

	/**
	 * This certificate is used to validate signatures. Used in the security
	 * package.
	 * 
	 * @return the java.security.cert.Certificate;
	 */
	public Object getCertificate();

	/**
	 * Sets the certificate of this service. Used only in the security package.
	 * 
	 * @param cert the java.security.cert.Certificate
	 */
	public void setCertificate(Object cert);

	/**
	 * return weather or not a service uses the security techniques.
	 * 
	 * @return
	 */
	public boolean isSecure();

	/**
	 * @param sec
	 */
	public void setSecure(boolean sec);
}
