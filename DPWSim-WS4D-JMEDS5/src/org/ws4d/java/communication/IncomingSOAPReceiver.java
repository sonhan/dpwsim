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

import org.ws4d.java.DPWSFramework;
import org.ws4d.java.communication.DPWS2006.DefaultDPWSCommunicatonUtil;
import org.ws4d.java.communication.protocol.soap.server.SOAPServer.SOAPHandler;
import org.ws4d.java.configuration.DPWSProperties;
import org.ws4d.java.constants.DPWSConstants;
import org.ws4d.java.constants.DPWSConstants2006;
import org.ws4d.java.constants.SOAPConstants;
import org.ws4d.java.dispatch.MessageInformer;
import org.ws4d.java.message.FaultMessage;
import org.ws4d.java.message.InvokeMessage;
import org.ws4d.java.message.Message;
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
import org.ws4d.java.structures.HashSet;
import org.ws4d.java.util.Log;

/**
 * 
 */
final class IncomingSOAPReceiver extends SOAPHandler {

	private static final MessageInformer		MESSAGE_INFORMER	= MessageInformer.getInstance();

	private final IncomingMessageListener		listener;

	private final DefaultDPWSCommunicatonUtil	util				= DefaultDPWSCommunicatonUtil.getInstance();

	static void markIncoming(Message message) {
		message.setInbound(true);
		if (Log.isDebug()) {
			Log.debug("<I> " + message, Log.DEBUG_LAYER_FRAMEWORK);
		}
	}

	static void markOutgoing(Message message) {
		message.setInbound(false);
		if (Log.isDebug()) {
			Log.debug("<O> " + message, Log.DEBUG_LAYER_FRAMEWORK);
		}
	}

	/**
	 * This method simply returns straightaway as long as the eventing module is
	 * present within the current runtime. If the eventing module is
	 * <em>not</em> present, it throws a <code>SOAPException</code> with a
	 * corresponding fault message describing the problem.
	 * 
	 * @param msg the message received
	 * @throws SOAPException if the eventing module is not present and
	 */
	private static void checkEventingPresence(Message msg) throws SOAPException {
		if (DPWSFramework.hasModule(DPWSFramework.EVENTING_MODULE)) {
			return;
		}
		throw new SOAPException(FaultMessage.createActionNotSupportedFault(msg));
	}

	/**
	 * @param listener
	 */
	IncomingSOAPReceiver(IncomingMessageListener listener) {
		super();
		this.listener = listener;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.ws4d.java.communication.protocol.soap.generator.MessageReceiver#receive
	 * (org.ws4d.java.message.discovery.HelloMessage,
	 * org.ws4d.java.communication.ProtocolData)
	 */
	public void receive(HelloMessage hello, ProtocolData protocolData) {
		respondWithActionNotSupported(hello, protocolData);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.ws4d.java.communication.protocol.soap.generator.MessageReceiver#receive
	 * (org.ws4d.java.message.discovery.ByeMessage,
	 * org.ws4d.java.communication.ProtocolData)
	 */
	public void receive(ByeMessage bye, ProtocolData protocolData) {
		respondWithActionNotSupported(bye, protocolData);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.ws4d.java.communication.protocol.soap.generator.MessageReceiver#receive
	 * (org.ws4d.java.message.discovery.ProbeMessage,
	 * org.ws4d.java.communication.ProtocolData)
	 */
	public void receive(ProbeMessage probe, ProtocolData protocolData) {
		// this is for directed probes to a device
		probe.setDirected(true);
		markIncoming(probe);
		try {
			Message responseMessage = listener.handle(probe, protocolData);
			MESSAGE_INFORMER.forwardMessage(probe, protocolData);
			if (responseMessage == null) {
				return;
				
			}
			// Check for Messageversion, if Version = 2006 the Namespaces and
			// some attributs must be changed
			util.changeOutgoingMessage(responseMessage.getProtocolInfo().getVersion(), responseMessage);

			respondWithMessage(responseMessage);
		} catch (SOAPException e) {
			MESSAGE_INFORMER.forwardMessage(probe, protocolData);
			respondWithFault(e);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.ws4d.java.communication.protocol.soap.generator.MessageReceiver#receive
	 * (org.ws4d.java.message.discovery.ProbeMatchesMessage,
	 * org.ws4d.java.communication.ProtocolData)
	 */
	public void receive(ProbeMatchesMessage probeMatches, ProtocolData protocolData) {
		respondWithActionNotSupported(probeMatches, protocolData);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.ws4d.java.communication.protocol.soap.generator.MessageReceiver#receive
	 * (org.ws4d.java.message.discovery.ResolveMessage,
	 * org.ws4d.java.communication.ProtocolData)
	 */
	public void receive(ResolveMessage resolve, ProtocolData protocolData) {
		respondWithActionNotSupported(resolve, protocolData);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.ws4d.java.communication.protocol.soap.generator.MessageReceiver#receive
	 * (org.ws4d.java.message.discovery.ResolveMatchesMessage,
	 * org.ws4d.java.communication.ProtocolData)
	 */
	public void receive(ResolveMatchesMessage resolveMatches, ProtocolData protocolData) {
		respondWithActionNotSupported(resolveMatches, protocolData);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.ws4d.java.communication.protocol.soap.generator.MessageReceiver#receive
	 * (org.ws4d.java.message.metadata.GetMessage,
	 * org.ws4d.java.communication.ProtocolData)
	 */
	public void receive(GetMessage get, ProtocolData protocolData) {
		markIncoming(get);
		try {
			Message responseMessage = listener.handle(get, protocolData);
			util.changeOutgoingMessage(responseMessage.getProtocolInfo().getVersion(), responseMessage);

			MESSAGE_INFORMER.forwardMessage(get, protocolData);
			respondWithMessage(responseMessage);
		} catch (SOAPException e) {
			MESSAGE_INFORMER.forwardMessage(get, protocolData);
			respondWithFault(e);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.ws4d.java.communication.protocol.soap.generator.MessageReceiver#receive
	 * (org.ws4d.java.message.metadata.GetResponseMessage,
	 * org.ws4d.java.communication.ProtocolData)
	 */
	public void receive(GetResponseMessage getResponse, ProtocolData protocolData) {
		respondWithActionNotSupported(getResponse, protocolData);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.ws4d.java.communication.protocol.soap.generator.MessageReceiver#receive
	 * (org.ws4d.java.message.metadata.GetMetadataMessage,
	 * org.ws4d.java.communication.ProtocolData)
	 */
	public void receive(GetMetadataMessage getMetadata, ProtocolData protocolData) {
		markIncoming(getMetadata);
		try {
			MESSAGE_INFORMER.forwardMessage(getMetadata, protocolData);
			Message responseMessage = listener.handle(getMetadata, protocolData);

			// Check for Messageversion, if Version = 2006 the Namespaces and
			// some attributs must be changed
			util.changeOutgoingMessage(responseMessage.getProtocolInfo().getVersion(), responseMessage);

			respondWithMessage(responseMessage);
		} catch (SOAPException e) {
			MESSAGE_INFORMER.forwardMessage(getMetadata, protocolData);
			respondWithFault(e);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.ws4d.java.communication.protocol.soap.generator.MessageReceiver#receive
	 * (org.ws4d.java.message.metadata. GetMetadataResponseMessage,
	 * org.ws4d.java.communication.ProtocolData)
	 */
	public void receive(GetMetadataResponseMessage getMetadataResponse, ProtocolData protocolData) {
		respondWithActionNotSupported(getMetadataResponse, protocolData);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.ws4d.java.communication.protocol.soap.generator.MessageReceiver#receive
	 * (org.ws4d.java.message.eventing.SubscribeMessage,
	 * org.ws4d.java.communication.ProtocolData)
	 */
	public void receive(SubscribeMessage subscribe, ProtocolData protocolData) {
		markIncoming(subscribe);
		try {
			checkEventingPresence(subscribe);
			Message responseMessage = listener.handle(subscribe, protocolData);
			MESSAGE_INFORMER.forwardMessage(subscribe, protocolData);
			respondWithMessage(responseMessage);
		} catch (SOAPException e) {
			MESSAGE_INFORMER.forwardMessage(subscribe, protocolData);
			respondWithFault(e);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.ws4d.java.communication.protocol.soap.generator.MessageReceiver#receive
	 * (org.ws4d.java.message.eventing.SubscribeResponseMessage,
	 * org.ws4d.java.communication.ProtocolData)
	 */
	public void receive(SubscribeResponseMessage subscribeResponse, ProtocolData protocolData) {
		respondWithActionNotSupported(subscribeResponse, protocolData);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.ws4d.java.communication.protocol.soap.generator.MessageReceiver#receive
	 * (org.ws4d.java.message.eventing.GetStatusMessage,
	 * org.ws4d.java.communication.ProtocolData)
	 */
	public void receive(GetStatusMessage getStatus, ProtocolData protocolData) {
		markIncoming(getStatus);
		try {
			checkEventingPresence(getStatus);
			Message responseMessage = listener.handle(getStatus, protocolData);
			MESSAGE_INFORMER.forwardMessage(getStatus, protocolData);
			respondWithMessage(responseMessage);
		} catch (SOAPException e) {
			MESSAGE_INFORMER.forwardMessage(getStatus, protocolData);
			respondWithFault(e);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.ws4d.java.communication.protocol.soap.generator.MessageReceiver#receive
	 * (org.ws4d.java.message.eventing.GetStatusResponseMessage,
	 * org.ws4d.java.communication.ProtocolData)
	 */
	public void receive(GetStatusResponseMessage getStatusResponse, ProtocolData protocolData) {
		respondWithActionNotSupported(getStatusResponse, protocolData);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.ws4d.java.communication.protocol.soap.generator.MessageReceiver#receive
	 * (org.ws4d.java.message.eventing.RenewMessage,
	 * org.ws4d.java.communication.ProtocolData)
	 */
	public void receive(RenewMessage renew, ProtocolData protocolData) {
		markIncoming(renew);
		try {
			checkEventingPresence(renew);
			Message responseMessage = listener.handle(renew, protocolData);
			MESSAGE_INFORMER.forwardMessage(renew, protocolData);
			respondWithMessage(responseMessage);
		} catch (SOAPException e) {
			MESSAGE_INFORMER.forwardMessage(renew, protocolData);
			respondWithFault(e);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.ws4d.java.communication.protocol.soap.generator.MessageReceiver#receive
	 * (org.ws4d.java.message.eventing.RenewResponseMessage,
	 * org.ws4d.java.communication.ProtocolData)
	 */
	public void receive(RenewResponseMessage renewResponse, ProtocolData protocolData) {
		respondWithActionNotSupported(renewResponse, protocolData);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.ws4d.java.communication.protocol.soap.generator.MessageReceiver#receive
	 * (org.ws4d.java.message.eventing.UnsubscribeMessage,
	 * org.ws4d.java.communication.ProtocolData)
	 */
	public void receive(UnsubscribeMessage unsubscribe, ProtocolData protocolData) {
		markIncoming(unsubscribe);
		try {
			checkEventingPresence(unsubscribe);
			Message responseMessage = listener.handle(unsubscribe, protocolData);
			MESSAGE_INFORMER.forwardMessage(unsubscribe, protocolData);
			respondWithMessage(responseMessage);
		} catch (SOAPException e) {
			MESSAGE_INFORMER.forwardMessage(unsubscribe, protocolData);
			respondWithFault(e);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.ws4d.java.communication.protocol.soap.generator.MessageReceiver#receive
	 * (org.ws4d.java.message.eventing.UnsubscribeResponseMessage,
	 * org.ws4d.java.communication.ProtocolData)
	 */
	public void receive(UnsubscribeResponseMessage unsubscribeResponse, ProtocolData protocolData) {
		respondWithActionNotSupported(unsubscribeResponse, protocolData);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.ws4d.java.communication.protocol.soap.generator.MessageReceiver#receive
	 * (org.ws4d.java.message.eventing.SubscriptionEndMessage,
	 * org.ws4d.java.communication.ProtocolData)
	 */
	public void receive(SubscriptionEndMessage subscriptionEnd, ProtocolData protocolData) {
		markIncoming(subscriptionEnd);
		listener.handle(subscriptionEnd, protocolData);
		MESSAGE_INFORMER.forwardMessage(subscriptionEnd, protocolData);
		respond(202, null);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.ws4d.java.communication.protocol.soap.generator.MessageReceiver#receive
	 * (org.ws4d.java.message.invocation.InvokeMessage,
	 * org.ws4d.java.communication.ProtocolData)
	 */
	public void receive(InvokeMessage invoke, ProtocolData protocolData) {
		markIncoming(invoke);
		try {
			Message responseMessage = listener.handle(invoke, protocolData);
			MESSAGE_INFORMER.forwardMessage(invoke, protocolData);
			respondWithMessage(responseMessage);
		} catch (SOAPException e) {
			MESSAGE_INFORMER.forwardMessage(invoke, protocolData);
			respondWithFault(e);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.communication.protocol.soap.generator.MessageReceiver#
	 * getOperation(java.lang.String)
	 */
	public OperationDescription getOperation(String action) {
		return listener.getOperation(action);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.ws4d.java.communication.protocol.soap.generator.MessageReceiver#receive
	 * (org.ws4d.java.message.FaultMessage,
	 * org.ws4d.java.communication.ProtocolData)
	 */
	public void receive(FaultMessage fault, ProtocolData protocolData) {
		respondWithActionNotSupported(fault, protocolData);
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.communication.protocol.soap.generator.MessageReceiver#
	 * receiveFailed(java.lang.Exception,
	 * org.ws4d.java.communication.ProtocolData)
	 */
	public void receiveFailed(Exception e, ProtocolData protocolData) {
		if (e instanceof VersionMismatchException) {

			VersionMismatchException ex = (VersionMismatchException) e;
			if (ex.getType() == VersionMismatchException.TYPE_WRONG_ADDRESSING_VERSION) {
				FaultMessage fault = FaultMessage.createMessageAddressingHeaderRequired(protocolData.getCommunicationManagerId());

				HashSet supportedDPWSVersions = DPWSProperties.getInstance().getSupportedDPWSVersions();
				if (supportedDPWSVersions.size() == 1 && ((Integer) supportedDPWSVersions.iterator().next()).intValue() == DPWSConstants2006.DPWS_VERSION2006) {
					fault.setProtocolInfo(new DPWSProtocolInfo(DPWSConstants2006.DPWS_VERSION2006));
					util.changeOutgoingMessage(fault.getProtocolInfo().getVersion(), fault);
				} else {
					fault.setProtocolInfo(new DPWSProtocolInfo(DPWSConstants.DPWS_VERSION2009));
				}

				respond(400, fault);
			}
		} else {
			respond(400, null);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.communication.protocol.soap.generator.MessageReceiver#
	 * sendFailed(java.lang.Exception, org.ws4d.java.communication.ProtocolData)
	 */
	public void sendFailed(Exception e, ProtocolData protocolData) {
		/*
		 * as this receiver will always be used on the server side, it never
		 * sends requests, thus this method can not get called
		 */
	}

	/**
	 * @param responseMessage
	 */
	private void respondWithMessage(Message responseMessage) {
		if (responseMessage == null) {
			respond(202, null);
		} else {
			markOutgoing(responseMessage);
			respond(200, responseMessage);
		}
	}

	/**
	 * @param e
	 */
	private void respondWithFault(SOAPException e) {
		FaultMessage fault = e.getFault();
		markOutgoing(fault);
		if (SOAPConstants.SOAP_FAULT_SENDER.equals(fault.getCode())) {
			respond(400, fault);
		} else {
			respond(500, fault);
		}
	}

	/**
	 * @param message
	 */
	private void respondWithActionNotSupported(Message message, ProtocolData protocolData) {
		markIncoming(message);
		String actionName = message.getAction().toString();
		Log.error("<I> Unexpected SOAP request message: " + actionName);
		if (Log.isDebug()) {
			Log.error(message.toString());
		}
		MESSAGE_INFORMER.forwardMessage(message, protocolData);
		FaultMessage fault = FaultMessage.createActionNotSupportedFault(message);
		markOutgoing(fault);
		respond(400, fault);
	}
}
