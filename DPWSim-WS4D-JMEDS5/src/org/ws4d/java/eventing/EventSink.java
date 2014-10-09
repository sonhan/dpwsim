/*******************************************************************************
 * Copyright (c) 2009 MATERNA Information & Communications. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html. For further
 * project-related information visit http://www.ws4d.org. The most recent
 * version of the JMEDS framework can be obtained from
 * http://sourceforge.net/projects/ws4d-javame.
 ******************************************************************************/

package org.ws4d.java.eventing;

import org.ws4d.java.communication.Bindable;
import org.ws4d.java.communication.CommunicationBinding;
import org.ws4d.java.communication.CommunicationManager;
import org.ws4d.java.service.Service;
import org.ws4d.java.service.parameter.ParameterValue;
import org.ws4d.java.types.URI;

/**
 * Interface used by a client to receive events. Incoming events/ solicit
 * responses will be transmitted to it.
 * <p>
 * To receive events/ solicit responses, the event sink needs to have at least
 * one {@link CommunicationBinding}. A communication binding can be added by
 * {@link #addBinding(CommunicationBinding)} to the event sink. Afterwards the
 * event sink must be opened by {@link #open()}, so that all bindings are bound
 * to their {@link CommunicationManager}s.
 * </p>
 * <strong>Important</strong> Subscribing to events is done via
 * {@link Service#subscribe(EventSink, String, org.ws4d.java.types.URISet, long)}
 * .
 */
public interface EventSink extends Bindable {

	/**
	 * Get associated event listener of this event sink.
	 * 
	 * @return EventListener associated with this event sink.
	 */
	public EventListener getEventListener();

	/**
	 * Callback method for receiving local notifications/ solicit responses.
	 * 
	 * @param clientSubscriptionId Subscription id the client sent to the
	 *            service for subscription.
	 * @param actionUri Action of event fired.
	 * @param outputParameter The parameter value the service transmitted with
	 *            the event message.
	 * @return Case event message: <code>null</code><BR>
	 *         Case solicit response message: parameter value to be transmitted
	 *         with the response message.
	 */
	public ParameterValue receiveLocalEvent(String clientSubscriptionId, URI actionUri, ParameterValue outputParameter);

	// ------------------- STATUS ---------------------

	/**
	 * Returns if event sink is open to receive notifications/ solicit
	 * responses.
	 * 
	 * @return <code>true</code> - if event sink is open to receive events/
	 *         solicit responses, else <code>false</code>.
	 */
	public boolean isOpen();

	/**
	 * Opens event receiving for this event endpoint. All contained bindings
	 * will be bound to their communication managers and start listening to
	 * incoming event/ solicit response messages.
	 * 
	 * @throws EventingException in case opening this event sink fails for any
	 *             reason (e.g. binding to a specified local address fails)
	 */
	public void open() throws EventingException;

	/**
	 * Closes event receiving for this event endpoint. All bindings of the event
	 * sink will be unbound.
	 */
	public void close();

	// ------------------- SUBCRIPTION HANDLING -------------------

	/**
	 * Internal method used within the subscription process.<BR>
	 * Subscribing to an event is done by
	 * {@link Service#subscribe(EventSink, String, org.ws4d.java.types.URISet, long)}
	 * .
	 * 
	 * @param clientSubId client subscription id
	 * @param subscription client subscription
	 */
	public void addSubscription(String clientSubId, ClientSubscription subscription);

	/**
	 * @param clientSubId
	 * @return the client subscription with the given ID
	 */
	public ClientSubscription getSubscription(String clientSubId);

}
