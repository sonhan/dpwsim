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
import org.ws4d.java.dispatch.MessageInformer;
import org.ws4d.java.dispatch.RequestResponseCoordinator;
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
final class UDPResponseReceiver implements MessageReceiver {

	private static final MessageInformer		MESSAGE_INFORMER	= MessageInformer.getInstance();

	private final RequestResponseCoordinator	rrc;

	/**
	 * @param rrc
	 */
	UDPResponseReceiver(RequestResponseCoordinator rrc) {
		super();
		this.rrc = rrc;
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

		if (probeMatches.getHeader().isValidated()) {
			rrc.handleResponse(probeMatches, protocolData);
			MESSAGE_INFORMER.forwardMessage(probeMatches, protocolData);
		}
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
		IncomingSOAPReceiver.markIncoming(resolveMatches);

		if (resolveMatches.getHeader().isValidated()) {
			rrc.handleResponse(resolveMatches, protocolData);
			MESSAGE_INFORMER.forwardMessage(resolveMatches, protocolData);
		}
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
		receiveUnexpectedMessage(getResponse, protocolData);
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
		receiveUnexpectedMessage(getMetadataResponse, protocolData);
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
		receiveUnexpectedMessage(subscribeResponse, protocolData);
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
		receiveUnexpectedMessage(getStatusResponse, protocolData);
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
		receiveUnexpectedMessage(renewResponse, protocolData);
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
		receiveUnexpectedMessage(unsubscribeResponse, protocolData);
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
		receiveUnexpectedMessage(invoke, protocolData);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.ws4d.java.communication.protocol.soap.generator.MessageReceiver#receive
	 * (org.ws4d.java.message.FaultMessage,
	 * org.ws4d.java.communication.ProtocolData)
	 */
	public void receive(FaultMessage fault, ProtocolData protocolData) {
		receiveUnexpectedMessage(fault, protocolData);
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.communication.protocol.soap.generator.MessageReceiver#
	 * receiveFailed(java.lang.Exception,
	 * org.ws4d.java.communication.ProtocolData)
	 */
	public void receiveFailed(Exception e, ProtocolData protocolData) {
		Log.error("Unable to receive SOAP-over-UDP response from " + protocolData.getDestinationAddress());
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.communication.protocol.soap.generator.MessageReceiver#
	 * sendFailed(java.lang.Exception, org.ws4d.java.communication.ProtocolData)
	 */
	public void sendFailed(Exception e, ProtocolData protocolData) {
		// void
	}

	private void receiveUnexpectedMessage(Message message, ProtocolData protocolData) {
		IncomingSOAPReceiver.markIncoming(message);
		String actionName = message.getAction().toString();
		Log.error("<I> Unexpected unicast SOAP-over-UDP response message from " + protocolData.getDestinationAddress() + ": " + actionName);
		if (Log.isDebug()) {
			Log.error(message.toString());
		}
		MESSAGE_INFORMER.forwardMessage(message, protocolData);
	}

	public OperationDescription getOperation(String action) {
		return null;
	}
}
