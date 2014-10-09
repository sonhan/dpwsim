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

import org.ws4d.java.message.FaultMessage;
import org.ws4d.java.message.InvokeMessage;
import org.ws4d.java.message.Message;
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
import org.ws4d.java.message.eventing.UnsubscribeMessage;
import org.ws4d.java.message.eventing.UnsubscribeResponseMessage;
import org.ws4d.java.message.metadata.GetMessage;
import org.ws4d.java.message.metadata.GetMetadataMessage;
import org.ws4d.java.message.metadata.GetMetadataResponseMessage;
import org.ws4d.java.message.metadata.GetResponseMessage;
import org.ws4d.java.service.OperationDescription;
import org.ws4d.java.types.XAddressInfo;

/**
 * This interface is used to receive answers to request messages sent via
 * {@link CommunicationManager#send(Message, ProtocolDomain, ResponseCallback)}
 * . Incoming response messages are delivered to an appropriate
 * <code>handle</code> method, e.g.
 * {@link #handle(Message, ProbeMatchesMessage, ProtocolData)} for responses to
 * a sent {@link ProbeMessage}.
 */
public interface ResponseCallback {

	/**
	 * Receives a {@link ProbeMatchesMessage} in response to a previously
	 * {@link CommunicationManager#send(Message, ProtocolDomain, ResponseCallback)
	 * sent} {@link ProbeMessage}. This receiving <code>ResponseCallback</code>
	 * instance is the one passed as third argument to the
	 * {@link CommunicationManager#send(Message, ProtocolDomain, ResponseCallback)
	 * send method}.<br />
	 * Note that it is possible (depending on the underlying technology
	 * implemented by the concrete {@link CommunicationManager} in use) to
	 * receive <em>none</em>, <em>exactly one</em> or <em>multiple</em>
	 * {@link ProbeMatchesMessage}s in response to a single sent
	 * {@link ProbeMessage}. This method would therefore accordingly either not
	 * get called at all, or get called once or multiple times..
	 * <p>
	 * If no responses to the sent resolve message are received within a
	 * protocol-specific time period, the method {@link #handleTimeout(Message)}
	 * will be called.
	 * </p>
	 * 
	 * @param request the original request message
	 * @param probeMatches a message containing a response to the previously
	 *            sent request
	 * @param protocolData protocol (aka. technology) specific addressing
	 *            information attached to the received message; this is
	 *            especially useful when
	 *            {@link CommunicationManager#send(Message, ProtocolDomain, ResponseCallback)
	 *            sending} probe request messages over multiple different
	 *            technologies like DPWS, Bluetooth, ZigBee, etc. (which can be
	 *            done by specifying respective {@link ProtocolDomain}s); the
	 *            supplied <code>protocolData</code> enables in such cases the
	 *            requester to distinguish which technology the search results
	 *            were obtained over
	 */
	public void handle(Message request, ProbeMatchesMessage probeMatches, ProtocolData protocolData);

	/**
	 * Receives a {@link ResolveMatchesMessage} in response to a previously
	 * {@link CommunicationManager#send(Message, ProtocolDomain, ResponseCallback)
	 * sent} {@link ResolveMessage}. This receiving
	 * <code>ResponseCallback</code> instance is the one passed as third
	 * argument to the
	 * {@link CommunicationManager#send(Message, ProtocolDomain, ResponseCallback)
	 * send method}.<br />
	 * Note that it is possible (depending on the underlying technology
	 * implemented by the concrete {@link CommunicationManager} in use) to
	 * receive <em>none</em> or <em>exactly one</em>
	 * {@link ResolveMatchesMessage} in response to a single sent
	 * {@link ResolveMessage}. This method would thus accordingly either not get
	 * called at all, or get called once.
	 * <p>
	 * If no responses to the sent resolve message are received within a
	 * protocol-specific time period, method {@link #handleTimeout(Message)}
	 * will be called.
	 * </p>
	 * 
	 * @param request the original request message
	 * @param resolveMatches a message containing a response to the previously
	 *            sent request
	 * @param protocolData protocol (aka. technology) specific addressing
	 *            information attached to the received message; this is
	 *            especially useful when
	 *            {@link CommunicationManager#send(Message, ProtocolDomain, ResponseCallback)
	 *            sending} resolve request messages over multiple different
	 *            technologies like DPWS, Bluetooth, ZigBee, etc. (which can be
	 *            done by specifying respective {@link ProtocolDomain}s); the
	 *            supplied <code>protocolData</code> enables in such cases the
	 *            requester to distinguish which technology the result was
	 *            obtained over
	 */
	public void handle(Message request, ResolveMatchesMessage resolveMatches, ProtocolData protocolData);

	/**
	 * Receives a {@link GetResponseMessage}. The corresponding
	 * {@link GetMessage} has previously been sent by passing this
	 * <code>ResponseCallback</code> instance as the third argument to the
	 * {@link CommunicationManager#send(Message, ProtocolDomain, ResponseCallback)
	 * send method}.
	 * <p>
	 * Each {@link GetMessage} may result in a call to either this method (in
	 * case of successful two-way communication), or to methods:
	 * <ul>
	 * <li>
	 * {@link #handleTransmissionException(Message, Exception, ProtocolData)} in
	 * case of a failure while sending the request message
	 * <li>
	 * <li>{@link #handle(Message, FaultMessage, ProtocolData)} in case of a
	 * failure during request processing on the server side</li>
	 * <li>
	 * {@link #handleMalformedResponseException(Message, Exception, ProtocolData)}
	 * in case of a failure during response processing on this side</li>
	 * <li>{@link #handleTimeout(Message)} in case of a timeout while waiting
	 * for responses to the sent request.</li>
	 * </ul>
	 * </p>
	 * 
	 * @param request the original request message
	 * @param getResponse a message containing the response to the previously
	 *            sent request
	 * @param protocolData protocol (aka. technology) specific addressing
	 *            information attached to the received message
	 */
	public void handle(Message request, GetResponseMessage getResponse, ProtocolData protocolData);

	/**
	 * Receives a {@link GetMetadataResponseMessage}. The corresponding
	 * {@link GetMetadataMessage} has previously been sent by passing this
	 * <code>ResponseCallback</code> instance as the third argument to the
	 * {@link CommunicationManager#send(Message, ProtocolDomain, ResponseCallback)
	 * send method}.
	 * <p>
	 * Each {@link GetMetadataMessage} may result in a call to either this
	 * method (in case of successful two-way communication), or to methods:
	 * <ul>
	 * <li>
	 * {@link #handleTransmissionException(Message, Exception, ProtocolData)} in
	 * case of a failure while sending the request message
	 * <li>
	 * <li>{@link #handle(Message, FaultMessage, ProtocolData)} in case of a
	 * failure during request processing on the server side</li>
	 * <li>
	 * {@link #handleMalformedResponseException(Message, Exception, ProtocolData)}
	 * in case of a failure during response processing on this side</li>
	 * <li>{@link #handleTimeout(Message)} in case of a timeout while waiting
	 * for responses to the sent request.</li>
	 * </ul>
	 * </p>
	 * 
	 * @param request the original request message
	 * @param getMetadataResponse a message containing the response to the
	 *            previously sent request
	 * @param protocolData protocol (aka. technology) specific addressing
	 *            information attached to the received message
	 */
	public void handle(Message request, GetMetadataResponseMessage getMetadataResponse, ProtocolData protocolData);

	/**
	 * Receives a {@link SubscribeResponseMessage}. The corresponding
	 * {@link SubscribeMessage} has been sent previously by passing this
	 * <code>ResponseCallback</code> instance as the third argument to the
	 * {@link CommunicationManager#send(Message, ProtocolDomain, ResponseCallback)
	 * send method}.
	 * <p>
	 * Each {@link SubscribeMessage} may result in a call to either this method
	 * (in case of successful two-way communication), or to methods:
	 * <ul>
	 * <li>
	 * {@link #handleTransmissionException(Message, Exception, ProtocolData)} in
	 * case of a failure while sending the request message
	 * <li>
	 * <li>{@link #handle(Message, FaultMessage, ProtocolData)} in case of a
	 * failure during request processing on the server side</li>
	 * <li>
	 * {@link #handleMalformedResponseException(Message, Exception, ProtocolData)}
	 * in case of a failure during response processing on this side</li>
	 * <li>{@link #handleTimeout(Message)} in case of a timeout while waiting
	 * for responses to the sent request.</li>
	 * </ul>
	 * </p>
	 * 
	 * @param request the original request message
	 * @param subscribeResponse a message containing the response to the
	 *            previously sent request
	 * @param protocolData protocol (aka. technology) specific addressing
	 *            information attached to the received message
	 */
	public void handle(Message request, SubscribeResponseMessage subscribeResponse, ProtocolData protocolData);

	/**
	 * Receives a {@link GetStatusResponseMessage}. The corresponding
	 * {@link GetStatusMessage} has been sent previously by passing this
	 * <code>ResponseCallback</code> instance as the third argument to the
	 * {@link CommunicationManager#send(Message, ProtocolDomain, ResponseCallback)
	 * send method}.
	 * <p>
	 * Each {@link GetStatusMessage} may result in a call to either this method
	 * (in case of successful two-way communication), or to methods:
	 * <ul>
	 * <li>
	 * {@link #handleTransmissionException(Message, Exception, ProtocolData)} in
	 * case of a failure while sending the request message
	 * <li>
	 * <li>{@link #handle(Message, FaultMessage, ProtocolData)} in case of a
	 * failure during request processing on the server side</li>
	 * <li>
	 * {@link #handleMalformedResponseException(Message, Exception, ProtocolData)}
	 * in case of a failure during response processing on this side</li>
	 * <li>{@link #handleTimeout(Message)} in case of a timeout while waiting
	 * for responses to the sent request.</li>
	 * </ul>
	 * </p>
	 * 
	 * @param request the original request message
	 * @param getStatusResponse a message containing the response to the
	 *            previously sent request
	 * @param protocolData protocol (aka. technology) specific addressing
	 *            information attached to the received message
	 */
	public void handle(Message request, GetStatusResponseMessage getStatusResponse, ProtocolData protocolData);

	/**
	 * Receives a {@link RenewResponseMessage}. The corresponding
	 * {@link RenewMessage} has been sent previously by passing this
	 * <code>ResponseCallback</code> instance as the third argument to the
	 * {@link CommunicationManager#send(Message, ProtocolDomain, ResponseCallback)
	 * send method}.
	 * <p>
	 * Each {@link RenewMessage} may result in a call to either this method (in
	 * case of successful two-way communication), or to methods:
	 * <ul>
	 * <li>
	 * {@link #handleTransmissionException(Message, Exception, ProtocolData)} in
	 * case of a failure while sending the request message
	 * <li>
	 * <li>{@link #handle(Message, FaultMessage, ProtocolData)} in case of a
	 * failure during request processing on the server side</li>
	 * <li>
	 * {@link #handleMalformedResponseException(Message, Exception, ProtocolData)}
	 * in case of a failure during response processing on this side</li>
	 * <li>{@link #handleTimeout(Message)} in case of a timeout while waiting
	 * for responses to the sent request.</li>
	 * </ul>
	 * </p>
	 * 
	 * @param request the original request message
	 * @param renewResponse a message containing the response to the previously
	 *            sent request
	 * @param protocolData protocol (aka. technology) specific addressing
	 *            information attached to the received message
	 */
	public void handle(Message request, RenewResponseMessage renewResponse, ProtocolData protocolData);

	/**
	 * Receives an {@link UnsubscribeResponseMessage}. The corresponding
	 * {@link UnsubscribeMessage} has been sent previously by passing this
	 * <code>ResponseCallback</code> instance as the third argument to the
	 * {@link CommunicationManager#send(Message, ProtocolDomain, ResponseCallback)
	 * send method}.
	 * <p>
	 * Each {@link UnsubscribeMessage} may result in a call to either this
	 * method (in case of successful two-way communication), or to methods:
	 * <ul>
	 * <li>
	 * {@link #handleTransmissionException(Message, Exception, ProtocolData)} in
	 * case of a failure while sending the request message
	 * <li>
	 * <li>{@link #handle(Message, FaultMessage, ProtocolData)} in case of a
	 * failure during request processing on the server side</li>
	 * <li>
	 * {@link #handleMalformedResponseException(Message, Exception, ProtocolData)}
	 * in case of a failure during response processing on this side</li>
	 * <li>{@link #handleTimeout(Message)} in case of a timeout while waiting
	 * for responses to the sent request.</li>
	 * </ul>
	 * </p>
	 * 
	 * @param request the original request message
	 * @param unsubscribeResponse a message containing the response to the
	 *            previously sent request
	 * @param protocolData protocol (aka. technology) specific addressing
	 *            information attached to the received message
	 */
	public void handle(Message request, UnsubscribeResponseMessage unsubscribeResponse, ProtocolData protocolData);

	/**
	 * Receives a {@link InvokeMessage} as a response to another
	 * <em>request</em> {@link InvokeMessage} that has previously been sent by
	 * passing this <code>ResponseCallback</code> instance as the third argument
	 * to the
	 * {@link CommunicationManager#send(Message, ProtocolDomain, ResponseCallback)
	 * send method}.
	 * <p>
	 * Each request {@link InvokeMessage} may result in a call to either this
	 * method (in case of successful two-way communication), or to methods:
	 * <ul>
	 * <li>
	 * {@link #handleTransmissionException(Message, Exception, ProtocolData)} in
	 * case of a failure while sending the request message
	 * <li>
	 * <li>{@link #handle(Message, FaultMessage, ProtocolData)} in case of a
	 * failure during request processing on the server side</li>
	 * <li>
	 * {@link #handleMalformedResponseException(Message, Exception, ProtocolData)}
	 * in case of a failure during response processing on this side</li>
	 * <li>{@link #handleTimeout(Message)} in case of a timeout while waiting
	 * for responses to the sent request.</li>
	 * </ul>
	 * In case of successful one-way communication, no method of this interface
	 * is called.
	 * </p>
	 * 
	 * @param request the original request message
	 * @param invokeResponse a message containing the response to the previously
	 *            sent request
	 * @param protocolData protocol (aka. technology) specific addressing
	 *            information attached to the received message
	 */
	public void handle(Message request, InvokeMessage invokeResponse, ProtocolData protocolData);

	/**
	 * Receives a {@link FaultMessage} in response to a previously
	 * {@link CommunicationManager#send(Message, ProtocolDomain, ResponseCallback)
	 * sent} request message.
	 * <p>
	 * A fault is raised on the responding side while processing the request
	 * message and then sent back to the requester (aka. this side). It is
	 * explicitly <strong>NOT</strong> created on this side while processing the
	 * received response (see method
	 * {@link #handleMalformedResponseException(Message, Exception, ProtocolData)}
	 * for the latter case).
	 * <p>
	 * 
	 * @param request the original request message
	 * @param fault the fault generated by the responding side
	 * @param protocolData protocol (aka. technology) specific addressing
	 *            information attached to the received message
	 */
	public void handle(Message request, FaultMessage fault, ProtocolData protocolData);

	/**
	 * Called when an attempt to receive responses to a previously sent request
	 * message passed to
	 * {@link CommunicationManager#send(Message, ProtocolDomain, ResponseCallback)}
	 * timeouted. This can only happen when using an unreliable connectionless
	 * transport layer for message transmission, such as UDP. Note that any
	 * other error related to sending the original request message is notified
	 * by the callback method
	 * {@link #handleTransmissionException(Message, Exception, ProtocolData)}.
	 * The original (request) message that was meant to be sent is passed as
	 * argument <code>request</code>.
	 * 
	 * @param request the message meant to be sent, which resulted in a timeout
	 */
	public void handleTimeout(Message request);

	/**
	 * Called when an exception arises while sending the <code>request</code>
	 * message. This could be e.g. due to network reachability reasons, an IO
	 * problem within the transport connection, an invalid message construct,
	 * etc.
	 * 
	 * @param request the request message which was meant to be sent when the
	 *            exception occurred
	 * @param exception the exception cause while sending the request message
	 * @param protocolData protocol (aka. technology) specific addressing
	 *            information related to the transmission exception
	 */
	public void handleTransmissionException(Message request, Exception exception, ProtocolData protocolData);

	/**
	 * Called when an exception occurs while receiving, decoding or processing
	 * the response message to a previously sent <code>request</code>. The
	 * exception is passed to argument <code>exception</code>
	 * 
	 * @param request the original request message
	 * @param exception the exception caused during processing the response
	 * @param protocolData protocol (aka. technology) specific addressing
	 *            information related to the malformed response
	 */
	public void handleMalformedResponseException(Message request, Exception exception, ProtocolData protocolData);

	public OperationDescription getOperation();
	
	public void setTargetAddress(XAddressInfo targetXAddressInfo);
	
	public XAddressInfo getTargetAddress();

}
