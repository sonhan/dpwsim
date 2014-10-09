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
import org.ws4d.java.communication.TimeoutException;
import org.ws4d.java.service.OperationDescription;
import org.ws4d.java.structures.DataStructure;
import org.ws4d.java.wsdl.WSDLOperation;

/**
 * Events sources are the source of server-side notifications, which interested
 * clients may subscribe to.
 * <p>
 * The DPWS framework supports two types of <a
 * href="http://www.w3.org/TR/wsdl">WSDL 1.1</a> and <a
 * href="http://www.w3.org/Submission/WS-Eventing/">WS-Eventing</a> compliant
 * events: <strong>notifications</strong> and <strong>solicit-response</strong>
 * operations. While the first ones represent one-way messages sent from the
 * event source to its subscribers, the later additionally includes response
 * messages sent back from the subscribers to the source.
 * <p>
 * Clients willing to receive notifications from this event source may simply
 * {@link #subscribe(EventListener, long, DataStructure) subscribe} to it. A
 * subscription can be defined to expire after a certain amount of time (
 * <em>duration</em>) or it may last &quot;forever&quot;, i.e. until either the
 * event source or the subscriber explicitly cancels it or it terminates due to
 * shutdown or missing network reachability.
 * </p>
 */
public interface EventSource extends OperationDescription {

	/**
	 * Returns <code>true</code>, if the transmission type of this event source
	 * is {@link WSDLOperation#TYPE_NOTIFICATION}. Returns <code>false</code> in
	 * any other case.
	 * 
	 * @return checks whether this is a {@link WSDLOperation#TYPE_NOTIFICATION
	 *         notification} event source
	 */
	public boolean isNotification();

	/**
	 * Returns <code>true</code>, if the transmission type of this event source
	 * is {@link WSDLOperation#TYPE_SOLICIT_RESPONSE}. Returns
	 * <code>false</code> in any other case.
	 * 
	 * @return checks whether this is a
	 *         {@link WSDLOperation#TYPE_SOLICIT_RESPONSE solicit-response}
	 *         event source
	 */
	public boolean isSolicitResponse();

	/**
	 * Allows a client to subscribe to this event source. The subscription will
	 * last for <code>duration</code> milliseconds or it will not expire at all
	 * when the value of <code>duration</code> is <code>0</code>.
	 * <p>
	 * This method will generate auto-bindings when subscribing to an event
	 * source which belongs to a <em>remotely</em> deployed DPWS service.
	 * This/These binding(s) will be used to open a local {@link EventSink event
	 * sink} and listen for incoming notifications.
	 * </p>
	 * <p>
	 * This method returns a {@link ClientSubscription} instance, which can be
	 * used to explore and manage the state of the subscription (e.g. renew or
	 * unsubscribe, etc.).
	 * </p>
	 * 
	 * @param client the client to which notification messages from this event
	 *            source are to be sent
	 * @param duration time until subscription expires in milliseconds;
	 *            <code>0</code> means subscription never expires
	 * @return a client subscription allowing management of the subscription
	 *         state
	 * @throws TimeoutException if this event source belongs to a remote service
	 *             and sending the subscription to it timed out
	 * @throws EventingException if an error occurs during subscription
	 */
	public ClientSubscription subscribe(EventListener client, long duration) throws EventingException, TimeoutException;

	/**
	 * Allows a listener to subscribe to this event source. The subscription
	 * will last <code>duration</code> milliseconds or it will not expire at
	 * all, in case the value of <code>duration</code> is <code>0</code>.
	 * <p>
	 * When subscribing to an event source, which belongs to a <em>remotely</em>
	 * deployed DPWS service, it is important to include at least one
	 * {@link CommunicationBinding} within the argument <code>bindings</code>.
	 * This/These binding(s) will be used to open a local {@link EventSink event
	 * sink} and listen for incoming notifications.
	 * </p>
	 * <p>
	 * This method returns a {@link ClientSubscription} instance, which can be
	 * used to explore and manage the state of the subscription (e.g. renew or
	 * unsubscribe, etc.).
	 * </p>
	 * 
	 * @param eventListener the listener to which notification messages from
	 *            this event source are to be sent
	 * @param duration time until subscription expires in milliseconds;
	 *            <code>0</code> means subscription never expires
	 * @param bindings a data structure consisting of one or more
	 *            {@link CommunicationBinding} instances; those are used to bind
	 *            a local {@link EventSink event sink} to and allow it to listen
	 *            for event notifications from a remote service
	 * @return a client subscription allowing management of the subscription
	 *         state
	 * @throws TimeoutException if this event source belongs to a remote service
	 *             and sending the subscription to it timed out
	 * @throws EventingException if an error occurs during subscription
	 */
	public ClientSubscription subscribe(EventListener eventListener, long duration, DataStructure bindings) throws EventingException, TimeoutException;

}
