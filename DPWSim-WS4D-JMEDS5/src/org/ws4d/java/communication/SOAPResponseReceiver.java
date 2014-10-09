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

import org.ws4d.java.communication.protocol.soap.generator.MessageReceiver;
import org.ws4d.java.communication.protocol.soap.generator.UnexpectedMessageException;
import org.ws4d.java.dispatch.MessageInformer;
import org.ws4d.java.message.FaultMessage;
import org.ws4d.java.message.InvokeMessage;
import org.ws4d.java.message.Message;
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
import org.ws4d.java.util.Log;

/**
 *
 */
final class SOAPResponseReceiver implements MessageReceiver {

	private static final MessageInformer	MESSAGE_INFORMER	= MessageInformer.getInstance();

	private final Message					request;

	private final ResponseCallback			callback;

	/**
	 * @param request
	 * @param callback
	 */
	SOAPResponseReceiver(Message request, ResponseCallback callback) {
		super();
		this.request = request;
		this.callback = callback;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.ws4d.java.communication.protocol.soap.generator.MessageReceiver#receive
	 * (org.ws4d.java.message.discovery.HelloMessage,
	 * org.ws4d.java.communication.ProtocolData)
	 */
	public void receive(HelloMessage hello, ProtocolData protocolData) {
		receiveUnexpectedMessage(hello, protocolData);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.ws4d.java.communication.protocol.soap.generator.MessageReceiver#receive
	 * (org.ws4d.java.message.discovery.ByeMessage,
	 * org.ws4d.java.communication.ProtocolData)
	 */
	public void receive(ByeMessage bye, ProtocolData protocolData) {
		receiveUnexpectedMessage(bye, protocolData);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.ws4d.java.communication.protocol.soap.generator.MessageReceiver#receive
	 * (org.ws4d.java.message.discovery.ProbeMessage,
	 * org.ws4d.java.communication.ProtocolData)
	 */
	public void receive(ProbeMessage probe, ProtocolData protocolData) {
		receiveUnexpectedMessage(probe, protocolData);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.ws4d.java.communication.protocol.soap.generator.MessageReceiver#receive
	 * (org.ws4d.java.message.discovery.ProbeMatchesMessage,
	 * org.ws4d.java.communication.ProtocolData)
	 */
	public void receive(ProbeMatchesMessage probeMatches, ProtocolData protocolData) {
		IncomingSOAPReceiver.markIncoming(probeMatches);
		callback.handle(request, probeMatches, protocolData);
		MESSAGE_INFORMER.forwardMessage(probeMatches, protocolData);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.ws4d.java.communication.protocol.soap.generator.MessageReceiver#receive
	 * (org.ws4d.java.message.discovery.ResolveMessage,
	 * org.ws4d.java.communication.ProtocolData)
	 */
	public void receive(ResolveMessage resolve, ProtocolData protocolData) {
		receiveUnexpectedMessage(resolve, protocolData);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.ws4d.java.communication.protocol.soap.generator.MessageReceiver#receive
	 * (org.ws4d.java.message.discovery.ResolveMatchesMessage,
	 * org.ws4d.java.communication.ProtocolData)
	 */
	public void receive(ResolveMatchesMessage resolveMatches, ProtocolData protocolData) {
		receiveUnexpectedMessage(resolveMatches, protocolData);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.ws4d.java.communication.protocol.soap.generator.MessageReceiver#receive
	 * (org.ws4d.java.message.metadata.GetMessage,
	 * org.ws4d.java.communication.ProtocolData)
	 */
	public void receive(GetMessage get, ProtocolData protocolData) {
		receiveUnexpectedMessage(get, protocolData);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.ws4d.java.communication.protocol.soap.generator.MessageReceiver#receive
	 * (org.ws4d.java.message.metadata.GetResponseMessage,
	 * org.ws4d.java.communication.ProtocolData)
	 */
	public void receive(GetResponseMessage getResponse, ProtocolData protocolData) {
		IncomingSOAPReceiver.markIncoming(getResponse);
		callback.handle(request, getResponse, protocolData);
		MESSAGE_INFORMER.forwardMessage(getResponse, protocolData);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.ws4d.java.communication.protocol.soap.generator.MessageReceiver#receive
	 * (org.ws4d.java.message.metadata.GetMetadataMessage,
	 * org.ws4d.java.communication.ProtocolData)
	 */
	public void receive(GetMetadataMessage getMetadata, ProtocolData protocolData) {
		receiveUnexpectedMessage(getMetadata, protocolData);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.ws4d.java.communication.protocol.soap.generator.MessageReceiver#receive
	 * (org.ws4d.java.message.metadata. GetMetadataResponseMessage,
	 * org.ws4d.java.communication.ProtocolData)
	 */
	public void receive(GetMetadataResponseMessage getMetadataResponse, ProtocolData protocolData) {
		IncomingSOAPReceiver.markIncoming(getMetadataResponse);
		callback.handle(request, getMetadataResponse, protocolData);
		MESSAGE_INFORMER.forwardMessage(getMetadataResponse, protocolData);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.ws4d.java.communication.protocol.soap.generator.MessageReceiver#receive
	 * (org.ws4d.java.message.eventing.SubscribeMessage,
	 * org.ws4d.java.communication.ProtocolData)
	 */
	public void receive(SubscribeMessage subscribe, ProtocolData protocolData) {
		receiveUnexpectedMessage(subscribe, protocolData);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.ws4d.java.communication.protocol.soap.generator.MessageReceiver#receive
	 * (org.ws4d.java.message.eventing.SubscribeResponseMessage,
	 * org.ws4d.java.communication.ProtocolData)
	 */
	public void receive(SubscribeResponseMessage subscribeResponse, ProtocolData protocolData) {
		IncomingSOAPReceiver.markIncoming(subscribeResponse);
		callback.handle(request, subscribeResponse, protocolData);
		MESSAGE_INFORMER.forwardMessage(subscribeResponse, protocolData);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.ws4d.java.communication.protocol.soap.generator.MessageReceiver#receive
	 * (org.ws4d.java.message.eventing.GetStatusMessage,
	 * org.ws4d.java.communication.ProtocolData)
	 */
	public void receive(GetStatusMessage getStatus, ProtocolData protocolData) {
		receiveUnexpectedMessage(getStatus, protocolData);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.ws4d.java.communication.protocol.soap.generator.MessageReceiver#receive
	 * (org.ws4d.java.message.eventing.GetStatusResponseMessage,
	 * org.ws4d.java.communication.ProtocolData)
	 */
	public void receive(GetStatusResponseMessage getStatusResponse, ProtocolData protocolData) {
		IncomingSOAPReceiver.markIncoming(getStatusResponse);
		callback.handle(request, getStatusResponse, protocolData);
		MESSAGE_INFORMER.forwardMessage(getStatusResponse, protocolData);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.ws4d.java.communication.protocol.soap.generator.MessageReceiver#receive
	 * (org.ws4d.java.message.eventing.RenewMessage,
	 * org.ws4d.java.communication.ProtocolData)
	 */
	public void receive(RenewMessage renew, ProtocolData protocolData) {
		receiveUnexpectedMessage(renew, protocolData);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.ws4d.java.communication.protocol.soap.generator.MessageReceiver#receive
	 * (org.ws4d.java.message.eventing.RenewResponseMessage,
	 * org.ws4d.java.communication.ProtocolData)
	 */
	public void receive(RenewResponseMessage renewResponse, ProtocolData protocolData) {
		IncomingSOAPReceiver.markIncoming(renewResponse);
		callback.handle(request, renewResponse, protocolData);
		MESSAGE_INFORMER.forwardMessage(renewResponse, protocolData);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.ws4d.java.communication.protocol.soap.generator.MessageReceiver#receive
	 * (org.ws4d.java.message.eventing.UnsubscribeMessage,
	 * org.ws4d.java.communication.ProtocolData)
	 */
	public void receive(UnsubscribeMessage unsubscribe, ProtocolData protocolData) {
		receiveUnexpectedMessage(unsubscribe, protocolData);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.ws4d.java.communication.protocol.soap.generator.MessageReceiver#receive
	 * (org.ws4d.java.message.eventing.UnsubscribeResponseMessage,
	 * org.ws4d.java.communication.ProtocolData)
	 */
	public void receive(UnsubscribeResponseMessage unsubscribeResponse, ProtocolData protocolData) {
		IncomingSOAPReceiver.markIncoming(unsubscribeResponse);
		callback.handle(request, unsubscribeResponse, protocolData);
		MESSAGE_INFORMER.forwardMessage(unsubscribeResponse, protocolData);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.ws4d.java.communication.protocol.soap.generator.MessageReceiver#receive
	 * (org.ws4d.java.message.eventing.SubscriptionEndMessage,
	 * org.ws4d.java.communication.ProtocolData)
	 */
	public void receive(SubscriptionEndMessage subscriptionEnd, ProtocolData protocolData) {
		receiveUnexpectedMessage(subscriptionEnd, protocolData);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.ws4d.java.communication.protocol.soap.generator.MessageReceiver#receive
	 * (org.ws4d.java.message.invocation.InvokeMessage,
	 * org.ws4d.java.communication.ProtocolData)
	 */
	public void receive(InvokeMessage invoke, ProtocolData protocolData) {
		IncomingMIMEReceiver.storeMIMEContext(invoke.getContent(), protocolData);
		IncomingSOAPReceiver.markIncoming(invoke);
		callback.handle(request, invoke, protocolData);
		MESSAGE_INFORMER.forwardMessage(invoke, protocolData);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.ws4d.java.communication.protocol.soap.generator.MessageReceiver#receive
	 * (org.ws4d.java.message.FaultMessage,
	 * org.ws4d.java.communication.ProtocolData)
	 */
	public void receive(FaultMessage fault, ProtocolData protocolData) {
		IncomingSOAPReceiver.markIncoming(fault);
		callback.handle(request, fault, protocolData);
		MESSAGE_INFORMER.forwardMessage(fault, protocolData);
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.communication.protocol.soap.generator.MessageReceiver#
	 * receiveFailed(java.lang.Exception,
	 * org.ws4d.java.communication.ProtocolData)
	 */
	public void receiveFailed(Exception e, ProtocolData protocolData) {
		callback.handleMalformedResponseException(request, e, protocolData);
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.communication.protocol.soap.generator.MessageReceiver#
	 * sendFailed(java.lang.Exception, org.ws4d.java.communication.ProtocolData)
	 */
	public void sendFailed(Exception e, ProtocolData protocolData) {
		callback.handleTransmissionException(request, e, protocolData);
	}

	private void receiveUnexpectedMessage(Message message, ProtocolData protocolData) {
		IncomingSOAPReceiver.markIncoming(message);
		String actionName = message.getAction().toString();
		Log.error("<I> Unexpected SOAP response message: " + actionName);
		if (Log.isDebug()) {
			Log.error(message.toString());
		}
		callback.handleMalformedResponseException(request, new UnexpectedMessageException(actionName), protocolData);
		MESSAGE_INFORMER.forwardMessage(message, protocolData);
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.communication.protocol.soap.generator.MessageReceiver#
	 * getOperation(java.lang.String)
	 */
	public OperationDescription getOperation(String action) {
		return callback.getOperation();
	}

}
