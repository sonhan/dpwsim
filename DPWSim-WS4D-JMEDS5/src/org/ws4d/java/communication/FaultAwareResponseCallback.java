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

/**
 *
 */
public class FaultAwareResponseCallback implements ResponseCallback {

	private final ResponseCallback	callback;

	private final int				supportedVersionsCount;

	private volatile boolean		responseReceived	= false;

	private int						receivedFaultCount	= 0;

	private XAddressInfo			targetXAddressInfo	= null;

	FaultAwareResponseCallback(XAddressInfo targetXAddressInfo, ResponseCallback callback, int supportedVersionsCount) {
		this.targetXAddressInfo = targetXAddressInfo;
		this.callback = callback;
		this.supportedVersionsCount = supportedVersionsCount;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.ws4d.java.communication.ResponseCallback#handle(org.ws4d.java.message
	 * .Message, org.ws4d.java.message.discovery.ProbeMatchesMessage,
	 * org.ws4d.java.communication.ProtocolData)
	 */
	public synchronized void handle(Message request, ProbeMatchesMessage probeMatches, ProtocolData protocolData) {
		responseReceived = true;
		callback.handle(request, probeMatches, protocolData);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.ws4d.java.communication.ResponseCallback#handle(org.ws4d.java.message
	 * .Message, org.ws4d.java.message.discovery.ResolveMatchesMessage,
	 * org.ws4d.java.communication.ProtocolData)
	 */
	public synchronized void handle(Message request, ResolveMatchesMessage resolveMatches, ProtocolData protocolData) {
		responseReceived = true;
		callback.handle(request, resolveMatches, protocolData);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.ws4d.java.communication.ResponseCallback#handle(org.ws4d.java.message
	 * .Message, org.ws4d.java.message.metadata.GetResponseMessage,
	 * org.ws4d.java.communication.ProtocolData)
	 */
	public synchronized void handle(Message request, GetResponseMessage getResponse, ProtocolData protocolData) {
		responseReceived = true;
		callback.handle(request, getResponse, protocolData);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.ws4d.java.communication.ResponseCallback#handle(org.ws4d.java.message
	 * .Message, org.ws4d.java.message.metadata.GetMetadataResponseMessage,
	 * org.ws4d.java.communication.ProtocolData)
	 */
	public synchronized void handle(Message request, GetMetadataResponseMessage getMetadataResponse, ProtocolData protocolData) {
		responseReceived = true;
		callback.handle(request, getMetadataResponse, protocolData);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.ws4d.java.communication.ResponseCallback#handle(org.ws4d.java.message
	 * .Message, org.ws4d.java.message.eventing.SubscribeResponseMessage,
	 * org.ws4d.java.communication.ProtocolData)
	 */
	public synchronized void handle(Message request, SubscribeResponseMessage subscribeResponse, ProtocolData protocolData) {
		responseReceived = true;
		callback.handle(request, subscribeResponse, protocolData);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.ws4d.java.communication.ResponseCallback#handle(org.ws4d.java.message
	 * .Message, org.ws4d.java.message.eventing.GetStatusResponseMessage,
	 * org.ws4d.java.communication.ProtocolData)
	 */
	public synchronized void handle(Message request, GetStatusResponseMessage getStatusResponse, ProtocolData protocolData) {
		responseReceived = true;
		callback.handle(request, getStatusResponse, protocolData);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.ws4d.java.communication.ResponseCallback#handle(org.ws4d.java.message
	 * .Message, org.ws4d.java.message.eventing.RenewResponseMessage,
	 * org.ws4d.java.communication.ProtocolData)
	 */
	public synchronized void handle(Message request, RenewResponseMessage renewResponse, ProtocolData protocolData) {
		responseReceived = true;
		callback.handle(request, renewResponse, protocolData);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.ws4d.java.communication.ResponseCallback#handle(org.ws4d.java.message
	 * .Message, org.ws4d.java.message.eventing.UnsubscribeResponseMessage,
	 * org.ws4d.java.communication.ProtocolData)
	 */
	public synchronized void handle(Message request, UnsubscribeResponseMessage unsubscribeResponse, ProtocolData protocolData) {
		responseReceived = true;
		callback.handle(request, unsubscribeResponse, protocolData);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.ws4d.java.communication.ResponseCallback#handle(org.ws4d.java.message
	 * .Message, org.ws4d.java.message.InvokeMessage,
	 * org.ws4d.java.communication.ProtocolData)
	 */
	public synchronized void handle(Message request, InvokeMessage invokeResponse, ProtocolData protocolData) {
		responseReceived = true;
		callback.handle(request, invokeResponse, protocolData);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.ws4d.java.communication.ResponseCallback#handle(org.ws4d.java.message
	 * .Message, org.ws4d.java.message.FaultMessage,
	 * org.ws4d.java.communication.ProtocolData)
	 */
	public synchronized void handle(Message request, FaultMessage fault, ProtocolData protocolData) {
		/*
		 * TODO check whether the fault is related to an unsupported protocol
		 * version we used for our request; if not, it should be forwarded to
		 * the callback immediately
		 */
		if (!responseReceived && ++receivedFaultCount == supportedVersionsCount) {
			callback.handle(request, fault, protocolData);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.ws4d.java.communication.ResponseCallback#handleTimeout(org.ws4d.java
	 * .message.Message)
	 */
	public synchronized void handleTimeout(Message request) {
		responseReceived = true;
		callback.handleTimeout(request);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.ws4d.java.communication.ResponseCallback#handleTransmissionException
	 * (org.ws4d.java.message.Message, java.lang.Exception,
	 * org.ws4d.java.communication.ProtocolData)
	 */
	public synchronized void handleTransmissionException(Message request, Exception exception, ProtocolData protocolData) {
		responseReceived = true;
		callback.handleTransmissionException(request, exception, protocolData);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.ws4d.java.communication.ResponseCallback#handleMalformedResponseException
	 * (org.ws4d.java.message.Message, java.lang.Exception,
	 * org.ws4d.java.communication.ProtocolData)
	 */
	public synchronized void handleMalformedResponseException(Message request, Exception exception, ProtocolData protocolData) {
		responseReceived = true;
		callback.handleMalformedResponseException(request, exception, protocolData);
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.communication.ResponseCallback#getOperation()
	 */
	public OperationDescription getOperation() {
		return (callback != null) ? callback.getOperation() : null;
	}

	/* (non-Javadoc)
	 * @see org.ws4d.java.communication.ResponseCallback#setTargetAddress(org.ws4d.java.types.XAddressInfo)
	 */
	public void setTargetAddress(XAddressInfo targetXAddressInfo) {
		this.targetXAddressInfo = targetXAddressInfo;
	}

	/* (non-Javadoc)
	 * @see org.ws4d.java.communication.ResponseCallback#getTargetAddress()
	 */
	public XAddressInfo getTargetAddress() {
		return targetXAddressInfo;
	}

}
