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
import org.ws4d.java.util.Log;

/**
 * A default implementation of a {@link IncomingMessageListener}. All
 * <code>handle</code> methods of this class simply log their arguments to
 * standard output and return either nothing (when allowed by DPWS
 * specification), an empty message of the proper type or throw a
 * {@link SOAPException}.
 */
public class DefaultIncomingMessageListener implements IncomingMessageListener {

	private static void logRequest(Message request) {
		Log.info("Unhandled request: " + request);
	}

	private static SOAPException actionNotSupportedException(Message request) {
		logRequest(request);
		return new SOAPException(FaultMessage.createActionNotSupportedFault(request));
	}

	/**
	 * 
	 */
	public DefaultIncomingMessageListener() {
		super();
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.ws4d.java.communication.IncomingMessageListener#handle(org.ws4d.java
	 * .communication.message.discovery.HelloMessage,
	 * org.ws4d.java.communication.ProtocolData)
	 */
	public void handle(HelloMessage hello, ProtocolData protocolData) {
		logRequest(hello);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.ws4d.java.communication.IncomingMessageListener#handle(org.ws4d.java
	 * .communication.message.discovery.ByeMessage,
	 * org.ws4d.java.communication.ProtocolData)
	 */
	public void handle(ByeMessage bye, ProtocolData protocolData) {
		logRequest(bye);

	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.ws4d.java.communication.IncomingMessageListener#handle(org.ws4d.java
	 * .communication.message.discovery.ProbeMessage,
	 * org.ws4d.java.communication.ProtocolData)
	 */
	public ProbeMatchesMessage handle(ProbeMessage probe, ProtocolData protocolData) throws SOAPException {
		logRequest(probe);
		if (probe.isDirected()) {
			ProbeMatchesMessage probeMatches = new ProbeMatchesMessage(probe.getHeader().getCommunicationManagerID());
			probeMatches.setResponseTo(probe);
			return probeMatches;
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.ws4d.java.communication.IncomingMessageListener#handle(org.ws4d.java
	 * .communication.message.discovery.ResolveMessage,
	 * org.ws4d.java.communication.ProtocolData)
	 */
	public ResolveMatchesMessage handle(ResolveMessage resolve, ProtocolData protocolData) {
		logRequest(resolve);
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.ws4d.java.communication.IncomingMessageListener#handle(org.ws4d.java
	 * .communication.message.metadataexchange.GetMessage,
	 * org.ws4d.java.communication.ProtocolData)
	 */
	public GetResponseMessage handle(GetMessage get, ProtocolData protocolData) throws SOAPException {
		throw actionNotSupportedException(get);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.ws4d.java.communication.IncomingMessageListener#handle(org.ws4d.java
	 * .communication.message.metadataexchange.GetMetadataMessage,
	 * org.ws4d.java.communication.ProtocolData)
	 */
	public GetMetadataResponseMessage handle(GetMetadataMessage getMetadata, ProtocolData protocolData) throws SOAPException {
		throw actionNotSupportedException(getMetadata);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.ws4d.java.communication.IncomingMessageListener#handle(org.ws4d.java
	 * .communication.message.eventing.SubscribeMessage,
	 * org.ws4d.java.communication.ProtocolData)
	 */
	public SubscribeResponseMessage handle(SubscribeMessage subscribe, ProtocolData protocolData) throws SOAPException {
		throw actionNotSupportedException(subscribe);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.ws4d.java.communication.IncomingMessageListener#handle(org.ws4d.java
	 * .communication.message.eventing.GetStatusMessage,
	 * org.ws4d.java.communication.ProtocolData)
	 */
	public GetStatusResponseMessage handle(GetStatusMessage getStatus, ProtocolData protocolData) throws SOAPException {
		throw actionNotSupportedException(getStatus);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.ws4d.java.communication.IncomingMessageListener#handle(org.ws4d.java
	 * .communication.message.eventing.RenewMessage,
	 * org.ws4d.java.communication.ProtocolData)
	 */
	public RenewResponseMessage handle(RenewMessage renew, ProtocolData protocolData) throws SOAPException {
		throw actionNotSupportedException(renew);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.ws4d.java.communication.IncomingMessageListener#handle(org.ws4d.java
	 * .communication.message.eventing.UnsubscribeMessage,
	 * org.ws4d.java.communication.ProtocolData)
	 */
	public UnsubscribeResponseMessage handle(UnsubscribeMessage unsubscribe, ProtocolData protocolData) throws SOAPException {
		throw actionNotSupportedException(unsubscribe);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.ws4d.java.communication.IncomingMessageListener#handle(org.ws4d.java
	 * .communication.message.eventing.SubscriptionEndMessage,
	 * org.ws4d.java.communication.ProtocolData)
	 */
	public void handle(SubscriptionEndMessage subscriptionEnd, ProtocolData protocolData) {
		logRequest(subscriptionEnd);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.ws4d.java.communication.IncomingMessageListener#handle(org.ws4d.java
	 * .communication.message.invocation.InvokeMessage,
	 * org.ws4d.java.communication.ProtocolData)
	 */
	public InvokeMessage handle(InvokeMessage invokeRequest, ProtocolData protocolData) throws SOAPException {
		throw actionNotSupportedException(invokeRequest);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.ws4d.java.communication.IncomingMessageListener#getOperation(java
	 * .lang.String)
	 */
	public OperationDescription getOperation(String action) {
		return null;
	}

}
