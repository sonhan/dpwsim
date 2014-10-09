/*******************************************************************************
 * Copyright (c) 2009 MATERNA Information & Communications. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html. For further
 * project-related information visit http://www.ws4d.org. The most recent
 * version of the JMEDS framework can be obtained from
 * http://sourceforge.net/projects/ws4d-javame.
 ******************************************************************************/
package org.ws4d.java.communication.protocol.soap;

import org.ws4d.java.DPWSFramework;
import org.ws4d.java.communication.ProtocolData;
import org.ws4d.java.communication.ResponseCallback;
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
 * This is a special implementation of the {@link ResponseCallback} which allows
 * to continue handling further messages, without waiting for the callback to be
 * finished.
 */
class NonBlockingResponseCallback implements ResponseCallback {

	private ResponseCallback	to					= null;

	private XAddressInfo		targetXAddresInfo	= null;

	/**
	 * Creates a non-blocking callback for SOAP messages.
	 * 
	 * @param to the origin callback.
	 */
	NonBlockingResponseCallback(XAddressInfo targetXAddressInfo, ResponseCallback to) {
		this.targetXAddresInfo = targetXAddressInfo;
		this.to = to;
	}

	/*
	 * (non-Javadoc)
	 * @seeorg.ws4d.java.communication.ResponseCallback#handle(org.ws4d.java.
	 * communication.message.Message,
	 * org.ws4d.java.message.discovery.ProbeMatchesMessage,
	 * org.ws4d.java.communication.ProtocolData)
	 */
	public void handle(Message request, ProbeMatchesMessage probeMatches, final ProtocolData protocolData) {
		final ProbeMatchesMessage res = probeMatches;
		final Message req = request;
		final ResponseCallback callback = to;
		DPWSFramework.getThreadPool().execute(new Runnable() {

			/*
			 * (non-Javadoc)
			 * @see java.lang.Runnable#run()
			 */
			public void run() {
				callback.handle(req, res, protocolData);
			}

		});
	}

	/*
	 * (non-Javadoc)
	 * @seeorg.ws4d.java.communication.ResponseCallback#handle(org.ws4d.java.
	 * communication.message.Message,
	 * org.ws4d.java.message.discovery.ResolveMatchesMessage,
	 * org.ws4d.java.communication.ProtocolData)
	 */
	public void handle(Message request, ResolveMatchesMessage resolveMatches, final ProtocolData protocolData) {
		final ResolveMatchesMessage res = resolveMatches;
		final Message req = request;
		final ResponseCallback callback = to;
		DPWSFramework.getThreadPool().execute(new Runnable() {

			/*
			 * (non-Javadoc)
			 * @see java.lang.Runnable#run()
			 */
			public void run() {
				callback.handle(req, res, protocolData);
			}

		});
	}

	/*
	 * (non-Javadoc)
	 * @seeorg.ws4d.java.communication.ResponseCallback#handle(org.ws4d.java.
	 * communication.message.Message,
	 * org.ws4d.java.message.metadata.GetResponseMessage,
	 * org.ws4d.java.communication.ProtocolData)
	 */
	public void handle(Message request, GetResponseMessage getResponse, ProtocolData protocolData) {
		final GetResponseMessage res = getResponse;
		final Message req = request;
		final ResponseCallback callback = to;
		final ProtocolData data = protocolData;
		DPWSFramework.getThreadPool().execute(new Runnable() {

			/*
			 * (non-Javadoc)
			 * @see java.lang.Runnable#run()
			 */
			public void run() {
				callback.handle(req, res, data);
			}

		});
	}

	/*
	 * (non-Javadoc)
	 * @seeorg.ws4d.java.communication.ResponseCallback#handle(org.ws4d.java.
	 * communication.message.Message, org.ws4d.java.message.metadata
	 * .GetMetadataResponseMessage, org.ws4d.java.communication.ProtocolData)
	 */
	public void handle(Message request, GetMetadataResponseMessage getMetadataResponse, ProtocolData protocolData) {
		final GetMetadataResponseMessage res = getMetadataResponse;
		final Message req = request;
		final ResponseCallback callback = to;
		final ProtocolData data = protocolData;
		DPWSFramework.getThreadPool().execute(new Runnable() {

			/*
			 * (non-Javadoc)
			 * @see java.lang.Runnable#run()
			 */
			public void run() {
				callback.handle(req, res, data);
			}

		});
	}

	/*
	 * (non-Javadoc)
	 * @seeorg.ws4d.java.communication.ResponseCallback#handle(org.ws4d.java.
	 * communication.message.Message,
	 * org.ws4d.java.message.eventing.SubscribeResponseMessage,
	 * org.ws4d.java.communication.ProtocolData)
	 */
	public void handle(Message request, SubscribeResponseMessage subscribeResponse, ProtocolData protocolData) {
		final SubscribeResponseMessage res = subscribeResponse;
		final Message req = request;
		final ResponseCallback callback = to;
		final ProtocolData data = protocolData;
		DPWSFramework.getThreadPool().execute(new Runnable() {

			/*
			 * (non-Javadoc)
			 * @see java.lang.Runnable#run()
			 */
			public void run() {
				callback.handle(req, res, data);
			}

		});
	}

	/*
	 * (non-Javadoc)
	 * @seeorg.ws4d.java.communication.ResponseCallback#handle(org.ws4d.java.
	 * communication.message.Message,
	 * org.ws4d.java.message.eventing.GetStatusResponseMessage,
	 * org.ws4d.java.communication.ProtocolData)
	 */
	public void handle(Message request, GetStatusResponseMessage getStatusResponse, ProtocolData protocolData) {
		final GetStatusResponseMessage res = getStatusResponse;
		final Message req = request;
		final ResponseCallback callback = to;
		final ProtocolData data = protocolData;
		DPWSFramework.getThreadPool().execute(new Runnable() {

			/*
			 * (non-Javadoc)
			 * @see java.lang.Runnable#run()
			 */
			public void run() {
				callback.handle(req, res, data);
			}

		});
	}

	/*
	 * (non-Javadoc)
	 * @seeorg.ws4d.java.communication.ResponseCallback#handle(org.ws4d.java.
	 * communication.message.Message,
	 * org.ws4d.java.message.eventing.RenewResponseMessage,
	 * org.ws4d.java.communication.ProtocolData)
	 */
	public void handle(Message request, RenewResponseMessage renewResponse, ProtocolData protocolData) {
		final RenewResponseMessage res = renewResponse;
		final Message req = request;
		final ResponseCallback callback = to;
		final ProtocolData data = protocolData;
		DPWSFramework.getThreadPool().execute(new Runnable() {

			/*
			 * (non-Javadoc)
			 * @see java.lang.Runnable#run()
			 */
			public void run() {
				callback.handle(req, res, data);
			}

		});
	}

	/*
	 * (non-Javadoc)
	 * @seeorg.ws4d.java.communication.ResponseCallback#handle(org.ws4d.java.
	 * communication.message.Message,
	 * org.ws4d.java.message.eventing.UnsubscribeResponseMessage,
	 * org.ws4d.java.communication.ProtocolData)
	 */
	public void handle(Message request, UnsubscribeResponseMessage unsubscribeResponse, ProtocolData protocolData) {
		final UnsubscribeResponseMessage res = unsubscribeResponse;
		final Message req = request;
		final ResponseCallback callback = to;
		final ProtocolData data = protocolData;
		DPWSFramework.getThreadPool().execute(new Runnable() {

			/*
			 * (non-Javadoc)
			 * @see java.lang.Runnable#run()
			 */
			public void run() {
				callback.handle(req, res, data);
			}

		});
	}

	/*
	 * (non-Javadoc)
	 * @seeorg.ws4d.java.communication.ResponseCallback#handle(org.ws4d.java.
	 * communication.message.Message,
	 * org.ws4d.java.message.invocation.InvokeMessage,
	 * org.ws4d.java.communication.ProtocolData)
	 */
	public void handle(Message request, InvokeMessage invokeResponse, ProtocolData protocolData) {
		final InvokeMessage res = invokeResponse;
		final Message req = request;
		final ResponseCallback callback = to;
		final ProtocolData data = protocolData;
		DPWSFramework.getThreadPool().execute(new Runnable() {

			/*
			 * (non-Javadoc)
			 * @see java.lang.Runnable#run()
			 */
			public void run() {
				callback.handle(req, res, data);
			}

		});
	}

	/*
	 * (non-Javadoc)
	 * @seeorg.ws4d.java.communication.ResponseCallback#handle(org.ws4d.java.
	 * communication.message.Message, org.ws4d.java.message.FaultMessage,
	 * org.ws4d.java.communication.ProtocolData)
	 */
	public void handle(Message request, FaultMessage fault, ProtocolData protocolData) {
		final FaultMessage res = fault;
		final Message req = request;
		final ResponseCallback callback = to;
		final ProtocolData data = protocolData;
		DPWSFramework.getThreadPool().execute(new Runnable() {

			/*
			 * (non-Javadoc)
			 * @see java.lang.Runnable#run()
			 */
			public void run() {
				callback.handle(req, res, data);
			}

		});
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.ws4d.java.communication.ResponseCallback#handleTransmissionException
	 * (org.ws4d.java.message.Message, java.lang.Exception,
	 * org.ws4d.java.communication.ProtocolData)
	 */
	public void handleTransmissionException(Message request, Exception exception, final ProtocolData protocolData) {
		final Exception res = exception;
		final Message req = request;
		final ResponseCallback callback = to;
		DPWSFramework.getThreadPool().execute(new Runnable() {

			/*
			 * (non-Javadoc)
			 * @see java.lang.Runnable#run()
			 */
			public void run() {
				callback.handleTransmissionException(req, res, protocolData);
			}

		});
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.ws4d.java.communication.ResponseCallback#handleMalformedResponseException
	 * (org.ws4d.java.message.Message, java.lang.Exception,
	 * org.ws4d.java.communication.ProtocolData)
	 */
	public void handleMalformedResponseException(Message request, Exception exception, final ProtocolData protocolData) {
		final Exception res = exception;
		final Message req = request;
		final ResponseCallback callback = to;
		DPWSFramework.getThreadPool().execute(new Runnable() {

			/*
			 * (non-Javadoc)
			 * @see java.lang.Runnable#run()
			 */
			public void run() {
				callback.handleMalformedResponseException(req, res, protocolData);
			}

		});
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.ws4d.java.communication.ResponseCallback#handleTimeout(org.ws4d.java
	 * .communication.message.Message)
	 */
	public void handleTimeout(Message request) {
		final Message req = request;
		final ResponseCallback callback = to;
		DPWSFramework.getThreadPool().execute(new Runnable() {

			/*
			 * (non-Javadoc)
			 * @see java.lang.Runnable#run()
			 */
			public void run() {
				callback.handleTimeout(req);
			}

		});
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.communication.ResponseCallback#getOperation()
	 */
	public OperationDescription getOperation() {
		return to.getOperation();
	}

	/* (non-Javadoc)
	 * @see org.ws4d.java.communication.ResponseCallback#setTargetAddress(org.ws4d.java.types.XAddressInfo)
	 */
	public void setTargetAddress(XAddressInfo targetXAddressInfo) {
		this.targetXAddresInfo = targetXAddressInfo;
	}

	/* (non-Javadoc)
	 * @see org.ws4d.java.communication.ResponseCallback#getTargetAddress()
	 */
	public XAddressInfo getTargetAddress() {
		return targetXAddresInfo;
	}

}
