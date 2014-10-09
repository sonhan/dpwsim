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

import java.io.IOException;
import java.io.OutputStream;

import org.ws4d.java.DPWSFramework;
import org.ws4d.java.communication.monitor.MonitorStreamFactory;
import org.ws4d.java.communication.monitor.MonitoredMessageReceiver;
import org.ws4d.java.communication.monitor.MonitoringContext;
import org.ws4d.java.communication.protocol.http.HTTPResponse;
import org.ws4d.java.communication.protocol.mime.MIMEBodyHeader;
import org.ws4d.java.communication.protocol.mime.MIMEEntityInput;
import org.ws4d.java.communication.protocol.mime.MIMEEntityOutput;
import org.ws4d.java.communication.protocol.mime.MIMEHandler;
import org.ws4d.java.communication.protocol.soap.SOAPResponse;
import org.ws4d.java.communication.protocol.soap.generator.MessageReceiver;
import org.ws4d.java.communication.protocol.soap.generator.SOAPMessageGeneratorFactory;
import org.ws4d.java.constants.SOAPConstants;
import org.ws4d.java.dispatch.MessageInformer;
import org.ws4d.java.message.FaultMessage;
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
import org.ws4d.java.service.parameter.ParameterValue;
import org.ws4d.java.structures.HashMap;
import org.ws4d.java.structures.Queue;
import org.ws4d.java.util.ParameterUtil;

/**
 *
 */
final class IncomingMIMEReceiver implements MIMEHandler, MessageReceiver {

	private static abstract class SimpleMIMEEntityOutput implements MIMEEntityOutput {

		/*
		 * (non-Javadoc)
		 * @see
		 * org.ws4d.java.communication.protocol.mime.MIMEEntityOutput#serialize
		 * (java.io.OutputStream)
		 */
		public void serialize(OutputStream out) throws IOException {
			// do nothing, getHTTPResponse() takes care of everything
		}

		/*
		 * (non-Javadoc)
		 * @see
		 * org.ws4d.java.communication.protocol.mime.MIMEBase#getEntityHeader()
		 */
		public MIMEBodyHeader getEntityHeader() {
			// do nothing, getHTTPResponse() takes care of everything
			return null;
		}

	}

	private static final MessageInformer	MESSAGE_INFORMER	= MessageInformer.getInstance();

	private final IncomingMessageListener	listener;

	// key = thread, value = MIMEEntity
	private final HashMap					responses			= new HashMap();

	/**
	 * @param content
	 * @param protocolData
	 */
	static void storeMIMEContext(ParameterValue content, ProtocolData protocolData) {
		if (content != null && ParameterUtil.hasAttachment(content)) {
			ParameterUtil.setAttachmentScope(content, protocolData.getCurrentMIMEContext());
		}
	}

	/**
	 * @param listener
	 */
	IncomingMIMEReceiver(IncomingMessageListener listener) {
		super();
		this.listener = listener;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.ws4d.java.communication.protocol.mime.MIMEHandler#handleRequest(org
	 * .ws4d.java.communication.protocol.mime.MIMEEntityInput,
	 * org.ws4d.java.structures.Queue, org.ws4d.java.communication.ProtocolData,
	 * org.ws4d.java.io.monitor.MonitoringContext)
	 */
	public void handleRequest(MIMEEntityInput part, Queue responseContainer, ProtocolData protocolData, MonitoringContext context) {
		final MessageReceiver r;

		MonitorStreamFactory monFac = DPWSFramework.getMonitorStreamFactory();
		if (monFac != null) {
			r = new MonitoredMessageReceiver(this, context);
		} else {
			r = this;
		}

		SOAPMessageGeneratorFactory.getInstance().getSOAP2MessageGeneratorForCurrentThread().deliverMessage(part.getBodyInputStream(), r, protocolData);
		MIMEEntityOutput response;
		synchronized (this.responses) {
			response = (MIMEEntityOutput) this.responses.remove(Thread.currentThread());
		}
		if (response != null) {
			responseContainer.enqueue(response);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.ws4d.java.communication.protocol.mime.MIMEHandler#handleResponse(
	 * org.ws4d.java.communication.protocol.mime.MIMEEntityInput,
	 * org.ws4d.java.communication.ProtocolData,
	 * org.ws4d.java.io.monitor.MonitoringContext)
	 */
	public void handleResponse(MIMEEntityInput part, ProtocolData protocolData, MonitoringContext context) throws IOException {
		// not needed on the server side
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.ws4d.java.communication.protocol.soap.generator.MessageReceiver#receive
	 * (org.ws4d.java.message.discovery.HelloMessage,
	 * org.ws4d.java.communication.ProtocolData)
	 */
	public void receive(HelloMessage hello, ProtocolData protocolData) {
		sendBadRequest();
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.ws4d.java.communication.protocol.soap.generator.MessageReceiver#receive
	 * (org.ws4d.java.message.discovery.ByeMessage,
	 * org.ws4d.java.communication.ProtocolData)
	 */
	public void receive(ByeMessage bye, ProtocolData protocolData) {
		sendBadRequest();
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.ws4d.java.communication.protocol.soap.generator.MessageReceiver#receive
	 * (org.ws4d.java.message.discovery.ProbeMessage,
	 * org.ws4d.java.communication.ProtocolData)
	 */
	public void receive(ProbeMessage probe, ProtocolData protocolData) {
		sendBadRequest();
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.ws4d.java.communication.protocol.soap.generator.MessageReceiver#receive
	 * (org.ws4d.java.message.discovery.ProbeMatchesMessage,
	 * org.ws4d.java.communication.ProtocolData)
	 */
	public void receive(ProbeMatchesMessage probeMatches, ProtocolData protocolData) {
		sendBadRequest();
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.ws4d.java.communication.protocol.soap.generator.MessageReceiver#receive
	 * (org.ws4d.java.message.discovery.ResolveMessage,
	 * org.ws4d.java.communication.ProtocolData)
	 */
	public void receive(ResolveMessage resolve, ProtocolData protocolData) {
		sendBadRequest();
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.ws4d.java.communication.protocol.soap.generator.MessageReceiver#receive
	 * (org.ws4d.java.message.discovery.ResolveMatchesMessage,
	 * org.ws4d.java.communication.ProtocolData)
	 */
	public void receive(ResolveMatchesMessage resolveMatches, ProtocolData protocolData) {
		sendBadRequest();
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.ws4d.java.communication.protocol.soap.generator.MessageReceiver#receive
	 * (org.ws4d.java.message.metadata.GetMessage,
	 * org.ws4d.java.communication.ProtocolData)
	 */
	public void receive(GetMessage get, ProtocolData protocolData) {
		sendBadRequest();
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.ws4d.java.communication.protocol.soap.generator.MessageReceiver#receive
	 * (org.ws4d.java.message.metadata.GetResponseMessage,
	 * org.ws4d.java.communication.ProtocolData)
	 */
	public void receive(GetResponseMessage getResponse, ProtocolData protocolData) {
		sendBadRequest();
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.ws4d.java.communication.protocol.soap.generator.MessageReceiver#receive
	 * (org.ws4d.java.message.metadata.GetMetadataMessage,
	 * org.ws4d.java.communication.ProtocolData)
	 */
	public void receive(GetMetadataMessage getMetadata, ProtocolData protocolData) {
		sendBadRequest();
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.ws4d.java.communication.protocol.soap.generator.MessageReceiver#receive
	 * (org.ws4d.java.message.metadata. GetMetadataResponseMessage,
	 * org.ws4d.java.communication.ProtocolData)
	 */
	public void receive(GetMetadataResponseMessage getMetadataResponse, ProtocolData protocolData) {
		sendBadRequest();
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.ws4d.java.communication.protocol.soap.generator.MessageReceiver#receive
	 * (org.ws4d.java.message.eventing.SubscribeMessage,
	 * org.ws4d.java.communication.ProtocolData)
	 */
	public void receive(SubscribeMessage subscribe, ProtocolData protocolData) {
		sendBadRequest();
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.ws4d.java.communication.protocol.soap.generator.MessageReceiver#receive
	 * (org.ws4d.java.message.eventing.SubscribeResponseMessage,
	 * org.ws4d.java.communication.ProtocolData)
	 */
	public void receive(SubscribeResponseMessage subscribeResponse, ProtocolData protocolData) {
		sendBadRequest();
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.ws4d.java.communication.protocol.soap.generator.MessageReceiver#receive
	 * (org.ws4d.java.message.eventing.GetStatusMessage,
	 * org.ws4d.java.communication.ProtocolData)
	 */
	public void receive(GetStatusMessage getStatus, ProtocolData protocolData) {
		sendBadRequest();
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.ws4d.java.communication.protocol.soap.generator.MessageReceiver#receive
	 * (org.ws4d.java.message.eventing.GetStatusResponseMessage,
	 * org.ws4d.java.communication.ProtocolData)
	 */
	public void receive(GetStatusResponseMessage getStatusResponse, ProtocolData protocolData) {
		sendBadRequest();
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.ws4d.java.communication.protocol.soap.generator.MessageReceiver#receive
	 * (org.ws4d.java.message.eventing.RenewMessage,
	 * org.ws4d.java.communication.ProtocolData)
	 */
	public void receive(RenewMessage renew, ProtocolData protocolData) {
		sendBadRequest();
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.ws4d.java.communication.protocol.soap.generator.MessageReceiver#receive
	 * (org.ws4d.java.message.eventing.RenewResponseMessage,
	 * org.ws4d.java.communication.ProtocolData)
	 */
	public void receive(RenewResponseMessage renewResponse, ProtocolData protocolData) {
		sendBadRequest();
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.ws4d.java.communication.protocol.soap.generator.MessageReceiver#receive
	 * (org.ws4d.java.message.eventing.UnsubscribeMessage,
	 * org.ws4d.java.communication.ProtocolData)
	 */
	public void receive(UnsubscribeMessage unsubscribe, ProtocolData protocolData) {
		sendBadRequest();
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.ws4d.java.communication.protocol.soap.generator.MessageReceiver#receive
	 * (org.ws4d.java.message.eventing.UnsubscribeResponseMessage,
	 * org.ws4d.java.communication.ProtocolData)
	 */
	public void receive(UnsubscribeResponseMessage unsubscribeResponse, ProtocolData protocolData) {
		sendBadRequest();
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.ws4d.java.communication.protocol.soap.generator.MessageReceiver#receive
	 * (org.ws4d.java.message.eventing.SubscriptionEndMessage,
	 * org.ws4d.java.communication.ProtocolData)
	 */
	public void receive(SubscriptionEndMessage subscriptionEnd, ProtocolData protocolData) {
		sendBadRequest();
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.ws4d.java.communication.protocol.soap.generator.MessageReceiver#receive
	 * (org.ws4d.java.message.invocation.InvokeMessage,
	 * org.ws4d.java.communication.ProtocolData)
	 */
	public void receive(InvokeMessage invoke, ProtocolData protocolData) {
		storeMIMEContext(invoke.getContent(), protocolData);
		IncomingSOAPReceiver.markIncoming(invoke);
		try {
			InvokeMessage responseMessage = listener.handle(invoke, protocolData);
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
		sendBadRequest();
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.communication.protocol.soap.generator.MessageReceiver#
	 * receiveFailed(java.lang.Exception,
	 * org.ws4d.java.communication.ProtocolData)
	 */
	public void receiveFailed(Exception e, ProtocolData protocolData) {
		sendBadRequest();
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
	private void respondWithMessage(final InvokeMessage responseMessage) {
		MIMEEntityOutput response;
		if (responseMessage != null) {
			IncomingSOAPReceiver.markOutgoing(responseMessage);

			response = new SimpleMIMEEntityOutput() {

				/*
				 * (non-Javadoc)
				 * @seeorg.ws4d.java.communication.protocol.mime.MIMEEntity#
				 * getHTTPResponse()
				 */
				public HTTPResponse getHTTPResponse() {
					return new SOAPResponse(200, responseMessage); // TODO
				}

			};
			synchronized (responses) {
				responses.put(Thread.currentThread(), response);
			}
		}
		// DefaultMIMEHandler will send an empty 202 response in that case
	}

	/**
	 * @param e
	 */
	private void respondWithFault(SOAPException e) {
		final FaultMessage fault = e.getFault();
		IncomingSOAPReceiver.markOutgoing(fault);

		MIMEEntityOutput response = new SimpleMIMEEntityOutput() {

			/*
			 * (non-Javadoc)
			 * @see
			 * org.ws4d.java.communication.protocol.mime.MIMEEntity#getHTTPResponse
			 * ()
			 */
			public HTTPResponse getHTTPResponse() {
				if (SOAPConstants.SOAP_FAULT_SENDER.equals(fault.getCode())) {
					return new SOAPResponse(400, fault); // TODO
				} else {
					return new SOAPResponse(500, fault); // TODO
				}
			}

		};

		synchronized (responses) {
			responses.put(Thread.currentThread(), response);
		}
	}

	private void sendBadRequest() {
		/*
		 * send a HTTP 400 Bad Request, as we don't support MIME packages
		 * containing other SOAP envelopes than operation invocations
		 */
		synchronized (responses) {
			responses.put(Thread.currentThread(), new SOAPResponse(400, null)); // TODO
		}
	}

}
