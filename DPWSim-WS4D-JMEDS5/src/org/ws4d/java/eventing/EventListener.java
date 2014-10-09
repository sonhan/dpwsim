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

import org.ws4d.java.communication.CommunicationBinding;
import org.ws4d.java.service.parameter.ParameterValue;
import org.ws4d.java.structures.DataStructure;
import org.ws4d.java.types.URI;
import org.ws4d.java.wsdl.WSDLOperation;

/**
 * The primary callback interface for handling events and other <a
 * href="http://www.w3.org/Submission/WS-Eventing/">WS-Eventing</a> related
 * notifications.
 */
public interface EventListener {

	/**
	 * Gets an event sink, which can be used when registering for event
	 * notifications from a service. The supplied data structure
	 * <code>bindings</code> is supposed to contain at least one
	 * {@link CommunicationBinding} instance denoting a local transport address,
	 * at which incoming notifications shall be delivered to. The EventSink is
	 * associated with the EventListener.
	 * 
	 * @param bindings a data structure of {@link CommunicationBinding}
	 *            instances to expose the created event sink over
	 * @return the event sink
	 */
	public EventSink getEventSink(DataStructure bindings);

	/**
	 * Callback method to receive events. Override this to receive and handle
	 * events.
	 * <p>
	 * In case the received event corresponds to a
	 * {@link WSDLOperation#TYPE_SOLICIT_RESPONSE} operation, the response to
	 * send back to the event source is taken from the return value of this
	 * method.
	 * </p>
	 * 
	 * @param subscription this is the subscription because of which the event
	 *            was received
	 * @param actionURI URI of the subscribed action
	 * @param parameterValue parameter value of notification message containing
	 *            the user data
	 * @return if not null, solicit response message: parameter values will be
	 *         sent as response to notification sender
	 */
	public ParameterValue eventReceived(ClientSubscription subscription, URI actionURI, ParameterValue parameterValue);

	/**
	 * Callback method to receive unexpected subscription ends from event
	 * sources. If this method is called, the subscription has been removed from
	 * the event sink. Override this to receive and handle subscription ends.
	 * 
	 * @param subscription subscription ended.
	 * @param reason Reason why the subscription ends unexpectedly
	 */
	public void subscriptionEndReceived(ClientSubscription subscription, URI reason);

	/**
	 * Callback method to receive timeouts of subscriptions. Override this to
	 * receive and handle timeouts of subscriptions.
	 * 
	 * @param subscription Ending subscription.
	 */
	public void subscriptionTimeoutReceived(ClientSubscription subscription);

}
