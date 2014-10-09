/*******************************************************************************
 * Copyright (c) 2009 MATERNA Information & Communications. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html. For further
 * project-related information visit http://www.ws4d.org. The most recent
 * version of the JMEDS framework can be obtained from
 * http://sourceforge.net/projects/ws4d-javame.
 ******************************************************************************/
package org.ws4d.java.communication;

import org.ws4d.java.message.InvokeMessage;
import org.ws4d.java.message.SOAPException;
import org.ws4d.java.message.discovery.ByeMessage;
import org.ws4d.java.message.discovery.HelloMessage;
import org.ws4d.java.message.discovery.ProbeMatchesMessage;
import org.ws4d.java.message.discovery.ProbeMessage;
import org.ws4d.java.message.discovery.ResolveMatchesMessage;
import org.ws4d.java.message.discovery.ResolveMessage;
import org.ws4d.java.message.eventing.GetStatusMessage;
import org.ws4d.java.message.eventing.GetStatusResponseMessage;
import org.ws4d.java.message.eventing.RenewMessage;
import org.ws4d.java.message.eventing.RenewResponseMessage;
import org.ws4d.java.message.eventing.SubscribeMessage;
import org.ws4d.java.message.eventing.SubscribeResponseMessage;
import org.ws4d.java.message.eventing.SubscriptionEndMessage;
import org.ws4d.java.message.eventing.UnsubscribeMessage;
import org.ws4d.java.message.eventing.UnsubscribeResponseMessage;
import org.ws4d.java.message.metadata.GetMessage;
import org.ws4d.java.message.metadata.GetMetadataMessage;
import org.ws4d.java.message.metadata.GetMetadataResponseMessage;
import org.ws4d.java.message.metadata.GetResponseMessage;
import org.ws4d.java.service.OperationDescription;

/**
 * Used to
 * {@link CommunicationManager#register(int[], CommunicationBinding, IncomingMessageListener)
 * register} interest for incoming (request) messages of a certain type. When a
 * desired message is received, it is delivered to the matching
 * <code>handle</code> method, e.g. {@link #handle(GetMessage, ProtocolData)}
 * for a {@link GetMessage}.
 */
public interface IncomingMessageListener {

	/**
	 * Receives an incoming hello message.
	 * 
	 * @param hello the message
	 * @param protocolData protocol (aka. technology) specific data associated
	 *            with the incoming message, e.g. source and target addresses or
	 *            other transport-related information
	 * @throws OldAppSequenceException in case <code>hello</code> has an older
	 *             app sequence as expected
	 */
	public void handle(HelloMessage hello, ProtocolData protocolData);

	/**
	 * Receives an incoming bye message.
	 * 
	 * @param bye the message
	 * @param protocolData protocol (aka. technology) specific data associated
	 *            with the incoming message, e.g. source and target addresses or
	 *            other transport-related information
	 * @throws OldAppSequenceException in case <code>bye</code> has an older app
	 *             sequence as expected
	 */
	public void handle(ByeMessage bye, ProtocolData protocolData);

	/**
	 * Receives an incoming probe message. The returned value depends on the
	 * content and type (multicast vs. directed) of the probe. For a multicast
	 * probe that matches, a corresponding non-empty {@link ProbeMatchesMessage}
	 * must be returned. Otherwise, if the multicast probe doesn't match, this
	 * method must return <code>null</code>. If the received probe is directed,
	 * then this method must always return a <code>ProbeMatchesMessage</code>
	 * which, in the case of a match, must be not empty.
	 * <p>
	 * When the incoming probe message is a directed one and for some reason
	 * this method fails (e.g. this implementation doesn't support handling of
	 * probe messages, unexpected probe content, etc.), a {@link SOAPException}
	 * describing the failure reason must be thrown. It will then be forwarded
	 * to the original sender of the probe.
	 * </p>
	 * 
	 * @param probe the probe message to respond to
	 * @param protocolData protocol (aka. technology) specific data associated
	 *            with the incoming message, e.g. source and target addresses or
	 *            other transport-related information
	 * @return <code>null</code> or either an empty or a proper probe matches
	 *         message, depending on the type and content of the received probe
	 *         message
	 * @throws SOAPException this exception may be raised only if a directed
	 *             probe was received and its processing caused a failure
	 */
	public ProbeMatchesMessage handle(ProbeMessage probe, ProtocolData protocolData) throws SOAPException;

	/**
	 * Receives a resolve message. If the incoming <code>resolve</code> matches,
	 * a corresponding (non-empty) {@link ResolveMatchesMessage} must be
	 * returned. Otherwise, this method must return <code>null</code>.
	 * 
	 * @param resolve the resolve message to respond to
	 * @param protocolData protocol (aka. technology) specific data associated
	 *            with the incoming message, e.g. source and target addresses or
	 *            other transport-related information
	 * @return <code>null</code> in case the incoming resolve doesn't match,
	 *         otherwise return the corresponding message
	 */
	public ResolveMatchesMessage handle(ResolveMessage resolve, ProtocolData protocolData);

	/**
	 * Receives an incoming get message. Must return a valid
	 * {@link GetResponseMessage} if processing the get request succeeded. Must
	 * throw a {@link SOAPException} describing the failure otherwise.
	 * 
	 * @param get the incoming get request
	 * @param protocolData protocol (aka. technology) specific data associated
	 *            with the incoming message, e.g. source and target addresses or
	 *            other transport-related information
	 * @return the response to the incoming get request
	 * @throws SOAPException only if processing the get request failed for some
	 *             reason (e.g. when the receiving instance is not capable of
	 *             processing get requests, the request message is malformed,
	 *             etc.)
	 */
	public GetResponseMessage handle(GetMessage get, ProtocolData protocolData) throws SOAPException;

	/**
	 * Receives an incoming get metadata message. Must return a valid
	 * {@link GetMetadataResponseMessage} if processing the get metadata request
	 * succeeded. Must throw a {@link SOAPException} describing the failure
	 * otherwise.
	 * 
	 * @param getMetadata the incoming request to process
	 * @param protocolData protocol (aka. technology) specific data associated
	 *            with the incoming message, e.g. source and target addresses or
	 *            other transport-related information
	 * @return the response to the issued <code>getMetadata</code> request
	 * @throws SOAPException if processing the incoming metadata request fails,
	 *             e.g. because this instance can not handle get metadata
	 *             messages or the request includes unexpected content, etc.
	 */
	public GetMetadataResponseMessage handle(GetMetadataMessage getMetadata, ProtocolData protocolData) throws SOAPException;

	/**
	 * Receives an incoming subscribe message. Returns a suitable
	 * {@link SubscribeResponseMessage} or throws a {@link SOAPException} if
	 * processing the incoming request fails for any reason.
	 * 
	 * @param subscribe the incoming subscribe request to process
	 * @param protocolData protocol (aka. technology) specific data associated
	 *            with the incoming message, e.g. source and target addresses or
	 *            other transport-related information
	 * @return a valid response to the incoming subscribe request
	 * @throws SOAPException if processing the subscribe request fails for any
	 *             reason, e.g. when this endpoint can not handle subscribe
	 *             requests, or the request's content is malformed, etc.
	 */
	public SubscribeResponseMessage handle(SubscribeMessage subscribe, ProtocolData protocolData) throws SOAPException;

	/**
	 * Receives an incoming get-status message (WS-Eventing). Returns a suitable
	 * {@link GetStatusResponseMessage} or throws a {@link SOAPException} if
	 * processing the incoming request fails for some reason.
	 * 
	 * @param getStatus the incoming get-status request to process
	 * @param protocolData protocol (aka. technology) specific data associated
	 *            with the incoming message, e.g. source and target addresses or
	 *            other transport-related information
	 * @return a valid response to the incoming get-status request
	 * @throws SOAPException if processing the get-status request fails for some
	 *             reason, e.g. when this endpoint cannot handle get-status
	 *             requests, or the request's content is malformed, etc.
	 */
	public GetStatusResponseMessage handle(GetStatusMessage getStatus, ProtocolData protocolData) throws SOAPException;

	/**
	 * Receives an incoming renew message (WS-Eventing). Returns a suitable
	 * {@link RenewResponseMessage} or throws a {@link SOAPException} if
	 * processing the incoming request fails for any reason.
	 * 
	 * @param renew the incoming renew request to process
	 * @param protocolData protocol (aka. technology) specific data associated
	 *            with the incoming message, e.g. source and target addresses or
	 *            other transport-related information
	 * @return a valid response to the incoming renew request
	 * @throws SOAPException if processing the renew request fails for some
	 *             reason, e.g. when this endpoint cannot handle renew requests,
	 *             or the request's content is malformed, etc.
	 */
	public RenewResponseMessage handle(RenewMessage renew, ProtocolData protocolData) throws SOAPException;

	/**
	 * Receives an incoming unsubscribe message (WS-Eventing). Returns a
	 * suitable {@link UnsubscribeResponseMessage} or throws a
	 * {@link SOAPException} if processing the incoming request fails for any
	 * reason.
	 * 
	 * @param unsubscribe the incoming unsubscribe request to process
	 * @param protocolData protocol (aka. technology) specific data associated
	 *            with the incoming message, e.g. source and target addresses or
	 *            other transport-related information
	 * @return a valid response to the incoming unsubscribe request
	 * @throws SOAPException if processing the unsubscribe request fails for
	 *             some reason, e.g. when this endpoint can not handle
	 *             unsubscribe requests, or the request's content is malformed,
	 *             etc.
	 */
	public UnsubscribeResponseMessage handle(UnsubscribeMessage unsubscribe, ProtocolData protocolData) throws SOAPException;

	/**
	 * Receives an incoming subscription end message (WS-Eventing).
	 * <p>
	 * <strong>NOTE:</strong> As this type of message is a simple one-way
	 * notification, this method neither has a return value, nor is declared to
	 * throw a {@link SOAPException} in case of a failure. That is, errors
	 * occurring during processing of this message must be silently ignored
	 * (this includes the case when this <code>IncomingMessageListener</code>
	 * instance doesn't process subscription end messages at all).
	 * 
	 * @param subscriptionEnd the incoming subscription end message
	 * @param protocolData protocol (aka. technology) specific data associated
	 *            with the incoming message, e.g. source and target addresses or
	 *            other transport-related information
	 */
	public void handle(SubscriptionEndMessage subscriptionEnd, ProtocolData protocolData);

	/**
	 * Receives an incoming invoke request. The returned value depends on the
	 * type of the web service operation the request belongs to (one-way vs.
	 * request-response). For a one-way operation, this method must return
	 * <code>null</code>, whereas for a request-response operation, the returned
	 * {@link InvokeMessage} must contain a suitable response to the incoming
	 * request.
	 * <p>
	 * This method may throw a {@link SOAPException} under two distinct
	 * circumstances:
	 * <ul>
	 * <li>the web service operation to which the incoming request belongs
	 * declares one or more faults: in such a case, the thrown
	 * <code>SOAPException</code> is expected to correspond to either one of
	 * those declared faults</li>
	 * <li>processing the invoke request fails for any other reason, e.g.
	 * malformed content or this <code>IncomingMessageListener</code> instance
	 * being unable to handle invoke requests: then, the
	 * <code>SOAPException</code> must describe the exact failure reason in a
	 * way independent from the actual declaration of the involved web service
	 * operation</li>
	 * </ul>
	 * </p>
	 * 
	 * @param invokeRequest the incoming invoke request to process
	 * @param protocolData protocol (aka. technology) specific data associated
	 *            with the incoming message, e.g. source and target addresses or
	 *            other transport-related information
	 * @return either <code>null</code> for a one-way operation, or a response
	 *         to the incoming request
	 * @throws SOAPException either because processing the invoke request failed
	 *             with an user-defined failure reason (i.e. a fault declared by
	 *             the concerned web service operation) or because of a more
	 *             generic error such as this instance being unable to process
	 *             incoming invoke requests, or malformed request content, etc.
	 */
	public InvokeMessage handle(InvokeMessage invokeRequest, ProtocolData protocolData) throws SOAPException;

	/**
	 * Returns an operation for a given <strong>wsa:Action</strong>. <h3>
	 * Important</h3>
	 * <p>
	 * The result can be <code>null</code> for an implementation which does not
	 * use an operation at all.
	 * </p>
	 * 
	 * @param action the <strong>wsa:Action</strong> which should be used to
	 *            retrieve the operation.
	 * @return the operation.
	 */
	public OperationDescription getOperation(String action);

}
