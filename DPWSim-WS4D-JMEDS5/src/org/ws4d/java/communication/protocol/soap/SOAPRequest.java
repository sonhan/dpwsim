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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.ws4d.java.DPWSFramework;
import org.ws4d.java.communication.AttachmentStoreHandler;
import org.ws4d.java.communication.CommunicationManager;
import org.ws4d.java.communication.DPWSProtocolInfo;
import org.ws4d.java.communication.ProtocolData;
import org.ws4d.java.communication.ProtocolInfo;
import org.ws4d.java.communication.monitor.MonitorStreamFactory;
import org.ws4d.java.communication.monitor.MonitoredMessageReceiver;
import org.ws4d.java.communication.monitor.MonitoringContext;
import org.ws4d.java.communication.protocol.http.HTTPRequest;
import org.ws4d.java.communication.protocol.http.HTTPResponseHandler;
import org.ws4d.java.communication.protocol.http.header.HTTPRequestHeader;
import org.ws4d.java.communication.protocol.http.header.HTTPResponseHeader;
import org.ws4d.java.communication.protocol.mime.DefaultMIMEHandler;
import org.ws4d.java.communication.protocol.mime.MIMEEntityInput;
import org.ws4d.java.communication.protocol.mime.MIMEHandler;
import org.ws4d.java.communication.protocol.soap.generator.MessageReceiver;
import org.ws4d.java.communication.protocol.soap.generator.SOAPMessageGeneratorFactory;
import org.ws4d.java.configuration.DPWSProperties;
import org.ws4d.java.configuration.HTTPProperties;
import org.ws4d.java.constants.DPWSMessageConstants;
import org.ws4d.java.constants.HTTPConstants;
import org.ws4d.java.constants.MIMEConstants;
import org.ws4d.java.dispatch.MessageInformer;
import org.ws4d.java.message.FaultMessage;
import org.ws4d.java.message.InvokeMessage;
import org.ws4d.java.message.Message;
import org.ws4d.java.service.parameter.ParameterValue;
import org.ws4d.java.structures.List;
import org.ws4d.java.structures.Queue;
import org.ws4d.java.types.InternetMediaType;
import org.ws4d.java.util.ParameterUtil;

/**
 *
 */
public class SOAPRequest implements HTTPRequest {

	private static final MessageInformer	MESSAGE_INFORMER	= MessageInformer.getInstance();

	private final Message					request;

	private final MessageReceiver			receiver;

	private final HTTPRequestHeader			header;

	private String							mimeBoundary		= null;

	private List							mimeEntities		= null;

	/**
	 * @param targetADdress
	 * @param request
	 * @param receiver
	 */
	public SOAPRequest(String requestPath, Message request, MessageReceiver receiver) {
		super();
		this.request = request;
		this.receiver = receiver;
		header = new HTTPRequestHeader(HTTPConstants.HTTP_METHOD_POST, requestPath, HTTPConstants.HTTP_VERSION11);

		int chunkedMode = -1;

		/*
		 * Check for HTTP chunk coding global settings.
		 */
		if (request != null) {
			ProtocolInfo pvi = request.getProtocolInfo();
			if (pvi instanceof DPWSProtocolInfo) {
				DPWSProtocolInfo dpvi = (DPWSProtocolInfo) pvi;
				chunkedMode = dpvi.getHttpRequestChunkedMode();

			}
		}

		/*
		 * Check for HTTP chunk coding address settings.
		 */
		String adr = request.getTargetAddress().toString();
		chunkedMode = HTTPProperties.getInstance().getChunkMode(adr);
		if (chunkedMode > -1) {
			System.out.println("chunk mode changed to " + chunkedMode + "!");
		}

		if (chunkedMode == DPWSProperties.HTTP_CHUNKED_ON) {
			header.addHeaderFieldValue(HTTPConstants.HTTP_HEADER_TRANSFER_ENCODING, HTTPConstants.HTTP_HEADERVALUE_TRANSFERCODING_CHUNKED);
		} else if (chunkedMode == DPWSProperties.HTTP_CHUNKED_ON_FOR_INVOKE) {
			if (request.getType() == Message.INVOKE_MESSAGE) {
				header.addHeaderFieldValue(HTTPConstants.HTTP_HEADER_TRANSFER_ENCODING, HTTPConstants.HTTP_HEADERVALUE_TRANSFERCODING_CHUNKED);
			}
		}

		String contentType = InternetMediaType.getSOAPXML().getMediaType();
		if (request.getType() == DPWSMessageConstants.INVOKE_MESSAGE) {
			InvokeMessage invoke = (InvokeMessage) request;
			ParameterValue pv = invoke.getContent();
			if (pv != null && ParameterUtil.hasAttachment(pv)) {
				mimeBoundary = DefaultMIMEHandler.createMimeBoundary();
				InternetMediaType mimeType = InternetMediaType.cloneAndSetParameter(InternetMediaType.getMultipartRelated(), MIMEConstants.PARAMETER_BOUNDARY, mimeBoundary);
				contentType = mimeType.toString();
				mimeEntities = ParameterUtil.getAttachments(pv);
			}
		}
		header.addHeaderFieldValue(HTTPConstants.HTTP_HEADER_CONTENT_TYPE, contentType);
		header.addHeaderFieldValue(HTTPConstants.HTTP_HEADER_TE, HTTPConstants.HTTP_HEADERVALUE_TE_TRAILERS);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.ws4d.java.communication.protocol.http.HTTPRequest#getRequestHeader()
	 */
	public HTTPRequestHeader getRequestHeader() {
		return header;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.ws4d.java.communication.protocol.http.HTTPRequest#getResponseHandler
	 * (org.ws4d.java.communication.InternetMediaType)
	 */
	public HTTPResponseHandler getResponseHandler(InternetMediaType mediaType) throws IOException {
		if (InternetMediaType.getSOAPXML().equals(mediaType)) {
			return new HTTPResponseHandler() {

				/*
				 * (non-Javadoc)
				 * @see
				 * org.ws4d.java.communication.protocol.http.HTTPResponseHandler
				 * #handle(org.ws4d.java.communication.protocol.http.header.
				 * HTTPResponseHeader, java.io.InputStream,
				 * org.ws4d.java.communication.protocol.http.HTTPRequest,
				 * org.ws4d.java.communication.DPWSProtocolData,
				 * org.ws4d.java.io.monitor.MonitoringContext)
				 */
				public void handle(HTTPResponseHeader header, InputStream body, HTTPRequest request, ProtocolData protocolData, MonitoringContext context) throws IOException {
					int httpStatus = header.getStatus();
					// TODO filter other potentially empty HTTP responses
					if (httpStatus == 202 || httpStatus == 204) {
						return;
					}

					MessageReceiver r;
					MonitorStreamFactory monFac = DPWSFramework.getMonitorStreamFactory();
					if (monFac != null) {
						r = new MonitoredMessageReceiver(receiver, context);
					} else {
						r = receiver;
					}

					boolean hasBody = false;
					String strLen = header.getHeaderFieldValue(HTTPConstants.HTTP_HEADER_CONTENT_LENGTH);
					if (strLen != null) {
						int len = Integer.parseInt(strLen.trim());
						hasBody = len > 0;
					} else {
						/*
						 * find out whether there are at least some bytes within
						 * the body
						 */
						hasBody = body.available() > 0;
					}

					if (hasBody) {
						SOAPMessageGeneratorFactory.getInstance().getSOAP2MessageGeneratorForCurrentThread().deliverMessage(body, r, protocolData);
					} else {
						/*
						 * regardless of the actual HTTP status code (be it a
						 * 4xx, 5xx or another one), if we get here, this means
						 * the other side is responding with a content type of
						 * application/soap+xml, but without a SOAP message
						 * within the HTTP body; so we can safely assume this is
						 * a faulty condition and deliver a dummy SOAP fault
						 * instead
						 */
						r.receive(FaultMessage.createEndpointUnavailableFault(SOAPRequest.this.request), protocolData);
					}
				}

			};

		} else if (InternetMediaType.getMultipartRelated().equals(mediaType)) {
			DefaultMIMEHandler mimeHandler = new DefaultMIMEHandler();
			mimeHandler.register(InternetMediaType.getApplicationXOPXML(), new MIMEHandler() {

				/*
				 * (non-Javadoc)
				 * @seeorg.ws4d.java.communication.protocol.mime.MIMEHandler#
				 * handleResponse
				 * (org.ws4d.java.communication.protocol.mime.MIMEEntityInput,
				 * org.ws4d.java.communication.DPWSProtocolData,
				 * org.ws4d.java.io.monitor.MonitoringContext)
				 */
				public void handleResponse(MIMEEntityInput part, ProtocolData protocolData, MonitoringContext context) throws IOException {
					MessageReceiver r;
					MonitorStreamFactory monFac = DPWSFramework.getMonitorStreamFactory();
					if (monFac != null) {
						r = new MonitoredMessageReceiver(receiver, context);
					} else {
						r = receiver;
					}

					SOAPMessageGeneratorFactory.getInstance().getSOAP2MessageGeneratorForCurrentThread().deliverMessage(part.getBodyInputStream(), r, protocolData);
				}

				/*
				 * (non-Javadoc)
				 * @seeorg.ws4d.java.communication.protocol.mime.MIMEHandler#
				 * handleRequest
				 * (org.ws4d.java.communication.protocol.mime.MIMEEntityInput,
				 * org.ws4d.java.structures.Queue,
				 * org.ws4d.java.communication.DPWSProtocolData,
				 * org.ws4d.java.io.monitor.MonitoringContext)
				 */
				public void handleRequest(MIMEEntityInput part, Queue responses, ProtocolData protocolData, MonitoringContext context) throws IOException {
					// void
				}

			});
			mimeHandler.register(2, -1, AttachmentStoreHandler.getInstance());
			return mimeHandler;
		} else if (InternetMediaType.getTextHTML().equals(mediaType)) {
			/*
			 * we may get text/html response e.g. when other side sends a
			 * HTTP-level error like 404, etc.
			 */
			return new HTTPResponseHandler() {

				/*
				 * (non-Javadoc)
				 * @see
				 * org.ws4d.java.communication.protocol.http.HTTPResponseHandler
				 * #handle(org.ws4d.java.communication.protocol.http.header.
				 * HTTPResponseHeader, java.io.InputStream,
				 * org.ws4d.java.communication.protocol.http.HTTPRequest,
				 * org.ws4d.java.communication.DPWSProtocolData,
				 * org.ws4d.java.io.monitor.MonitoringContext)
				 */
				public void handle(HTTPResponseHeader header, InputStream body, HTTPRequest request, ProtocolData protocolData, MonitoringContext context) throws IOException {
					MessageReceiver r;
					MonitorStreamFactory monFac = DPWSFramework.getMonitorStreamFactory();
					if (monFac != null) {
						r = new MonitoredMessageReceiver(receiver, context);
					} else {
						r = receiver;
					}

					if (header.getStatus() == 401) {
						r.receive(FaultMessage.createAuthenticationFault(SOAPRequest.this.request), protocolData);
					} else {
						r.receive(FaultMessage.createEndpointUnavailableFault(SOAPRequest.this.request), protocolData);
					}
				}

			};
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.ws4d.java.communication.protocol.http.HTTPRequest#requestSendFailed
	 * (java.lang.Exception, org.ws4d.java.communication.ProtocolData)
	 */
	public void requestSendFailed(Exception e, ProtocolData protocolData) {
		receiver.sendFailed(e, protocolData);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.ws4d.java.communication.protocol.http.HTTPRequest#responseReceiveFailed
	 * (java.lang.Exception, org.ws4d.java.communication.ProtocolData)
	 */
	public void responseReceiveFailed(Exception e, ProtocolData protocolData) {
		receiver.receiveFailed(e, protocolData);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.ws4d.java.communication.protocol.http.HTTPRequest#serializeRequestBody
	 * (java.io.OutputStream, org.ws4d.java.communication.ProtocolData,
	 * org.ws4d.java.io.monitor.MonitoringContext)
	 */
	public void serializeRequestBody(OutputStream out, ProtocolData protocolData, MonitoringContext context) throws IOException {
		CommunicationManager comMan = DPWSFramework.getCommunicationManager(protocolData.getCommunicationManagerId());
		comMan.serializeMessageWithAttachments(request, mimeBoundary, mimeEntities, out, protocolData);
		MESSAGE_INFORMER.forwardMessage(request, protocolData);
		if (context != null) {
			context.setMessage(request);
		}
	}

}
