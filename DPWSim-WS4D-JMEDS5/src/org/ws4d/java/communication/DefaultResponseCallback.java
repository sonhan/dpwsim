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
import org.ws4d.java.message.discovery.ResolveMatchesMessage;
import org.ws4d.java.message.eventing.GetStatusResponseMessage;
import org.ws4d.java.message.eventing.RenewResponseMessage;
import org.ws4d.java.message.eventing.SubscribeResponseMessage;
import org.ws4d.java.message.eventing.UnsubscribeResponseMessage;
import org.ws4d.java.message.metadata.GetMetadataResponseMessage;
import org.ws4d.java.message.metadata.GetResponseMessage;
import org.ws4d.java.service.OperationDescription;
import org.ws4d.java.types.XAddressInfo;
import org.ws4d.java.util.Log;

/**
 * A default implementation of a {@link ResponseCallback}. All
 * <code>handle</code> methods of this class simply log their arguments to
 * standard output.
 */
public class DefaultResponseCallback implements ResponseCallback {

	private XAddressInfo	targetXAddressInfo	= null;

	private static void logResponse(Message request, Message response) {
		Log.info("Unhandled response: " + response + ". Request was: " + request);
	}

	/**
	 * 
	 */
	public DefaultResponseCallback(XAddressInfo targetXAddressInfo) {
		this.targetXAddressInfo = targetXAddressInfo;
	}

	/*
	 * (non-Javadoc)
	 * @seeorg.ws4d.java.communication.ResponseCallback#handle(org.ws4d.java.
	 * communication.message.Message,
	 * org.ws4d.java.message.discovery.ProbeMatchesMessage,
	 * org.ws4d.java.communication.ProtocolData)
	 */
	public void handle(Message request, ProbeMatchesMessage probeMatches, ProtocolData protocolData) {
		logResponse(request, probeMatches);
	}

	/*
	 * (non-Javadoc)
	 * @seeorg.ws4d.java.communication.ResponseCallback#handle(org.ws4d.java.
	 * communication.message.Message,
	 * org.ws4d.java.message.discovery.ResolveMatchesMessage,
	 * org.ws4d.java.communication.ProtocolData)
	 */
	public void handle(Message request, ResolveMatchesMessage resolveMatches, ProtocolData protocolData) {
		logResponse(request, resolveMatches);
	}

	/*
	 * (non-Javadoc)
	 * @seeorg.ws4d.java.communication.ResponseCallback#handle(org.ws4d.java.
	 * communication.message.Message,
	 * org.ws4d.java.message.metadata.GetResponseMessage,
	 * org.ws4d.java.communication.ProtocolData)
	 */
	public void handle(Message request, GetResponseMessage getResponse, ProtocolData protocolData) {
		logResponse(request, getResponse);
	}

	/*
	 * (non-Javadoc)
	 * @seeorg.ws4d.java.communication.ResponseCallback#handle(org.ws4d.java.
	 * communication.message.Message, org.ws4d.java.message.metadata
	 * .GetMetadataResponseMessage, org.ws4d.java.communication.ProtocolData)
	 */
	public void handle(Message request, GetMetadataResponseMessage getMetadataResponse, ProtocolData protocolData) {
		logResponse(request, getMetadataResponse);
	}

	/*
	 * (non-Javadoc)
	 * @seeorg.ws4d.java.communication.ResponseCallback#handle(org.ws4d.java.
	 * communication.message.Message,
	 * org.ws4d.java.message.eventing.SubscribeResponseMessage,
	 * org.ws4d.java.communication.ProtocolData)
	 */
	public void handle(Message request, SubscribeResponseMessage subscribeResponse, ProtocolData protocolData) {
		logResponse(request, subscribeResponse);
	}

	/*
	 * (non-Javadoc)
	 * @seeorg.ws4d.java.communication.ResponseCallback#handle(org.ws4d.java.
	 * communication.message.Message,
	 * org.ws4d.java.message.eventing.GetStatusResponseMessage,
	 * org.ws4d.java.communication.ProtocolData)
	 */
	public void handle(Message request, GetStatusResponseMessage getStatusResponse, ProtocolData protocolData) {
		logResponse(request, getStatusResponse);
	}

	/*
	 * (non-Javadoc)
	 * @seeorg.ws4d.java.communication.ResponseCallback#handle(org.ws4d.java.
	 * communication.message.Message,
	 * org.ws4d.java.message.eventing.RenewResponseMessage,
	 * org.ws4d.java.communication.ProtocolData)
	 */
	public void handle(Message request, RenewResponseMessage renewResponse, ProtocolData protocolData) {
		logResponse(request, renewResponse);
	}

	/*
	 * (non-Javadoc)
	 * @seeorg.ws4d.java.communication.ResponseCallback#handle(org.ws4d.java.
	 * communication.message.Message,
	 * org.ws4d.java.message.eventing.UnsubscribeResponseMessage,
	 * org.ws4d.java.communication.ProtocolData)
	 */
	public void handle(Message request, UnsubscribeResponseMessage unsubscribeResponse, ProtocolData protocolData) {
		logResponse(request, unsubscribeResponse);
	}

	/*
	 * (non-Javadoc)
	 * @seeorg.ws4d.java.communication.ResponseCallback#handle(org.ws4d.java.
	 * communication.message.Message,
	 * org.ws4d.java.message.invocation.InvokeMessage,
	 * org.ws4d.java.communication.ProtocolData)
	 */
	public void handle(Message request, InvokeMessage invokeResponse, ProtocolData protocolData) {
		logResponse(request, invokeResponse);
	}

	/*
	 * (non-Javadoc)
	 * @seeorg.ws4d.java.communication.ResponseCallback#handle(org.ws4d.java.
	 * communication.message.Message, org.ws4d.java.message.FaultMessage,
	 * org.ws4d.java.communication.ProtocolData)
	 */
	public void handle(Message request, FaultMessage fault, ProtocolData protocolData) {
		logResponse(request, fault);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.ws4d.java.communication.ResponseCallback#handleTransmissionException
	 * (org.ws4d.java.message.Message, java.lang.Exception,
	 * org.ws4d.java.communication.ProtocolData)
	 */
	public void handleTransmissionException(Message request, Exception exception, ProtocolData protocolData) {
		Log.warn("Unhandled transmission exception: " + exception + ". Request was: " + request);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.ws4d.java.communication.ResponseCallback#handleMalformedResponseException
	 * (org.ws4d.java.message.Message, java.lang.Exception,
	 * org.ws4d.java.communication.ProtocolData)
	 */
	public void handleMalformedResponseException(Message request, Exception exception, ProtocolData protocolData) {
		Log.warn("Unhandled malformed response exception: " + exception + ". Request was: " + request);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.ws4d.java.communication.ResponseCallback#handleTimeout(org.ws4d.java
	 * .communication.message.Message)
	 */
	public void handleTimeout(Message request) {
		Log.warn("Unhandled request timeout. Request was: " + request);
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.communication.ResponseCallback#getOperation()
	 */
	public OperationDescription getOperation() {
		return null;
	}

	public void setTargetAddress(XAddressInfo targetXAddressInfo) {
		this.targetXAddressInfo = targetXAddressInfo;
	}

	public XAddressInfo getTargetAddress() {
		return targetXAddressInfo;
	}

}
