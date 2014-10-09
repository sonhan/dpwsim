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

import org.ws4d.java.communication.ProtocolData;
import org.ws4d.java.communication.TimeoutException;
import org.ws4d.java.message.SOAPException;
import org.ws4d.java.message.eventing.GetStatusMessage;
import org.ws4d.java.message.eventing.GetStatusResponseMessage;
import org.ws4d.java.message.eventing.RenewMessage;
import org.ws4d.java.message.eventing.RenewResponseMessage;
import org.ws4d.java.message.eventing.SubscribeMessage;
import org.ws4d.java.message.eventing.SubscribeResponseMessage;
import org.ws4d.java.message.eventing.UnsubscribeMessage;
import org.ws4d.java.message.eventing.UnsubscribeResponseMessage;
import org.ws4d.java.types.URISet;

public interface SubscriptionManager {

	/**
	 * This method should be called each time a local service receives a
	 * subscription request from a remote client.
	 * 
	 * @param msg the subscribe message describing the request
	 * @return a corresponding subscribe response message suitable to confirm
	 *         the subscription
	 * @throws SOAPException in case the subscription failed for any reason, a
	 *             fault message with further details can be obtained from this
	 *             <code>SOAPException</code> (see
	 *             {@link SOAPException#getFault()})
	 */
	public SubscribeResponseMessage subscribe(SubscribeMessage msg, ProtocolData protocolData) throws SOAPException;

	/**
	 * Subscribe to subscription for local client.
	 * 
	 * @param sink
	 * @param clientSubscriptionId
	 * @param eventActionURIs
	 * @param duration
	 * @return a client subscription instance describing the status and allowing
	 *         management of the subscription
	 * @throws EventingException
	 */
	public ClientSubscription subscribe(EventSink sink, String clientSubscriptionId, URISet eventActionURIs, long duration) throws EventingException;

	/**
	 * This method should be called each time a local service receives a request
	 * to cancel a subscription from a remote client.
	 * 
	 * @param msg the unsubscribe message describing the request
	 * @return a corresponding unsubscribe response message suitable to confirm
	 *         the subscription cancellation
	 * @throws SOAPException in case the cancellation failed for any reason, a
	 *             fault message with further details can be obtained from this
	 *             <code>SOAPException</code> (see
	 *             {@link SOAPException#getFault()})
	 */
	public UnsubscribeResponseMessage unsubscribe(UnsubscribeMessage msg, ProtocolData protocolData) throws SOAPException;

	/**
	 * This method should be called each time a service receives a request to
	 * cancel a subscription from a local client.
	 * 
	 * @param subscription Client subscription.
	 * @throws EventingException
	 */
	public void unsubscribe(ClientSubscription subscription) throws EventingException, TimeoutException;

	/**
	 * This method should be called each time a local service receives a request
	 * to renew a subscription from a client.
	 * 
	 * @param msg the renew message describing the request
	 * @return a corresponding renew response message suitable to confirm the
	 *         subscription renewal
	 * @throws SOAPException in case the renewal failed for any reason, a fault
	 *             message with further details can be obtained from this
	 *             <code>SOAPException</code> (see
	 *             {@link SOAPException#getFault()})
	 */
	public RenewResponseMessage renew(RenewMessage msg, ProtocolData protocolData) throws SOAPException;

	/**
	 * Renews an existing subscription with new duration. If duration is "0",
	 * subscription never terminates. This method should be called each time a
	 * service receives a request to renew a subscription from a client.
	 * 
	 * @param subscription
	 * @param duration
	 * @throws EventingException
	 * @throws TimeoutException
	 */
	public long renew(ClientSubscription subscription, long duration) throws EventingException, TimeoutException;

	/**
	 * This method should be called each time a local service receives a request
	 * to obtain the status of a subscription from a client.
	 * 
	 * @param msg the get status message describing the request
	 * @return a corresponding get status response message suitable to confirm
	 *         the subscription status query
	 * @throws SOAPException in case the status query failed for any reason, a
	 *             fault message with further details can be obtained from this
	 *             <code>SOAPException</code> (see
	 *             {@link SOAPException#getFault()})
	 */
	public GetStatusResponseMessage getStatus(GetStatusMessage msg, ProtocolData protocolData) throws SOAPException;

	/**
	 * This method should be called each time a service receives a request to
	 * obtain the status of a subscription from a client.
	 * 
	 * @param subscription
	 * @return the current status of the subscription in terms of milliseconds
	 *         until its expiration
	 * @throws EventingException
	 * @throws TimeoutException
	 */
	public long getStatus(ClientSubscription subscription) throws EventingException, TimeoutException;

	/**
	 * Notifies each subscribed event sink / event listener that the service has
	 * terminated the subscription.
	 */
	public void sendSubscriptionEnd();

	public boolean isRemote();

}
