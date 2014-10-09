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
import java.io.OutputStream;

import org.ws4d.java.DPWSFramework;
import org.ws4d.java.communication.CommunicationManager;
import org.ws4d.java.communication.DPWSProtocolInfo;
import org.ws4d.java.communication.ProtocolData;
import org.ws4d.java.communication.ProtocolInfo;
import org.ws4d.java.communication.monitor.MonitoringContext;
import org.ws4d.java.communication.protocol.http.HTTPResponse;
import org.ws4d.java.communication.protocol.http.HTTPResponseUtil;
import org.ws4d.java.communication.protocol.http.header.HTTPRequestHeader;
import org.ws4d.java.communication.protocol.http.header.HTTPResponseHeader;
import org.ws4d.java.communication.protocol.mime.DefaultMIMEHandler;
import org.ws4d.java.configuration.DPWSProperties;
import org.ws4d.java.constants.HTTPConstants;
import org.ws4d.java.constants.MIMEConstants;
import org.ws4d.java.dispatch.MessageInformer;
import org.ws4d.java.message.FaultMessage;
import org.ws4d.java.message.InvokeMessage;
import org.ws4d.java.message.Message;
import org.ws4d.java.service.parameter.ParameterValue;
import org.ws4d.java.structures.List;
import org.ws4d.java.types.InternetMediaType;
import org.ws4d.java.types.URI;
import org.ws4d.java.util.ParameterUtil;

/**
 *
 */
public class SOAPResponse extends HTTPResponse {

	private static final MessageInformer	MESSAGE_INFORMER	= MessageInformer.getInstance();

	private final Message					response;

	private final HTTPResponseHeader		header;

	private String							mimeBoundary		= null;

	private List							attachments			= null;

	/**
	 * @param httpStatus
	 * @param response
	 */
	public SOAPResponse(int httpStatus, Message response) {
		super();
		this.response = response;

		header = HTTPResponseUtil.getResponseHeader(httpStatus);

		int chunkedMode = -1;

		/*
		 * Check for HTTP chunk coding global settings.
		 */
		if (response != null) {
			ProtocolInfo pvi = response.getProtocolInfo();
			if (pvi instanceof DPWSProtocolInfo) {
				DPWSProtocolInfo dpvi = (DPWSProtocolInfo) pvi;
				chunkedMode = dpvi.getHttpResponseChunkedMode();
			}
		}

		if (chunkedMode == DPWSProperties.HTTP_CHUNKED_ON) {
			header.addHeaderFieldValue(HTTPConstants.HTTP_HEADER_TRANSFER_ENCODING, HTTPConstants.HTTP_HEADERVALUE_TRANSFERCODING_CHUNKED);
		} else if (chunkedMode == DPWSProperties.HTTP_CHUNKED_ON_FOR_INVOKE) {
			if (response.getType() == Message.INVOKE_MESSAGE) {
				header.addHeaderFieldValue(HTTPConstants.HTTP_HEADER_TRANSFER_ENCODING, HTTPConstants.HTTP_HEADERVALUE_TRANSFERCODING_CHUNKED);
			}
		}

		String contentType = InternetMediaType.getSOAPXML().getMediaType();
		if (response instanceof InvokeMessage) {
			InvokeMessage invoke = (InvokeMessage) response;
			contentType = inspectAttachments(contentType, invoke.getContent());
		} else if (response instanceof FaultMessage) {
			FaultMessage fault = (FaultMessage) response;
			contentType = inspectAttachments(contentType, fault.getDetail());
		}
		header.addHeaderFieldValue(HTTPConstants.HTTP_HEADER_CONTENT_TYPE, contentType);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.ws4d.java.communication.protocol.http.HTTPResponse#getResponseHeader
	 * ()
	 */
	public HTTPResponseHeader getResponseHeader() {
		return header;
	}

	public void serializeResponseBody(URI request, HTTPRequestHeader header, OutputStream out, ProtocolData protocolData, MonitoringContext context) throws IOException {
		if (response == null) {
			// omit one-ways
			return;
		}
		CommunicationManager comMan = DPWSFramework.getCommunicationManager(protocolData.getCommunicationManagerId());
		comMan.serializeMessageWithAttachments(response, mimeBoundary, attachments, out, protocolData);
		MESSAGE_INFORMER.forwardMessage(response, protocolData);

		if (context != null) {
			context.setMessage(response);
		}
	}

	/**
	 * @param contentType
	 * @param pv
	 * @return
	 */
	private String inspectAttachments(String contentType, ParameterValue pv) {
		if (pv != null && ParameterUtil.hasAttachment(pv)) {
			mimeBoundary = DefaultMIMEHandler.createMimeBoundary();
			InternetMediaType mimeType = InternetMediaType.cloneAndSetParameter(InternetMediaType.getMultipartRelated(), MIMEConstants.PARAMETER_BOUNDARY, mimeBoundary);
			contentType = mimeType.toString();
			attachments = ParameterUtil.getAttachments(pv);
		}
		return contentType;
	}

}
