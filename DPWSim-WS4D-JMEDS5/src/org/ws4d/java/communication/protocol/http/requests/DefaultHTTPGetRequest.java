/*******************************************************************************
 * Copyright (c) 2009 MATERNA Information & Communications. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html. For further
 * project-related information visit http://www.ws4d.org. The most recent
 * version of the JMEDS framework can be obtained from
 * http://sourceforge.net/projects/ws4d-javame.
 ******************************************************************************/
package org.ws4d.java.communication.protocol.http.requests;

import java.io.IOException;
import java.io.OutputStream;

import org.ws4d.java.communication.ProtocolData;
import org.ws4d.java.communication.monitor.MonitoringContext;
import org.ws4d.java.communication.protocol.http.HTTPRequest;
import org.ws4d.java.communication.protocol.http.HTTPResponseHandler;
import org.ws4d.java.communication.protocol.http.header.HTTPRequestHeader;
import org.ws4d.java.constants.HTTPConstants;
import org.ws4d.java.types.InternetMediaType;

public class DefaultHTTPGetRequest implements HTTPRequest {

	private HTTPRequestHeader	header	= null;

	public DefaultHTTPGetRequest(String request) {
		header = new HTTPRequestHeader(HTTPConstants.HTTP_METHOD_GET, request, HTTPConstants.HTTP_VERSION11);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.ws4d.java.communication.protocol.http.HTTPRequest#serializeRequestBody
	 * (java.io.OutputStream, org.ws4d.java.communication.ProtocolData,
	 * org.ws4d.java.io.monitor.MonitoringContext)
	 */
	public void serializeRequestBody(OutputStream out, ProtocolData protocolData, MonitoringContext context) throws IOException {}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.ws4d.java.communication.protocol.httpx.HTTPRequest#getRequestHeader()
	 */
	public HTTPRequestHeader getRequestHeader() {
		return header;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.ws4d.java.communication.protocol.http.HTTPRequest#requestSendFailed
	 * (java.lang.Exception, org.ws4d.java.communication.ProtocolData)
	 */
	public void requestSendFailed(Exception e, ProtocolData pd) {
		// TODO Auto-generated method stub
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.ws4d.java.communication.protocol.http.HTTPRequest#responseReceiveFailed
	 * (java.lang.Exception, org.ws4d.java.communication.ProtocolData)
	 */
	public void responseReceiveFailed(Exception e, ProtocolData pd) {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.ws4d.java.communication.protocol.http.HTTPRequest#getResponseHandler
	 * (org.ws4d.java.communication.InternetMediaType)
	 */
	public HTTPResponseHandler getResponseHandler(InternetMediaType mediaType) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

}
