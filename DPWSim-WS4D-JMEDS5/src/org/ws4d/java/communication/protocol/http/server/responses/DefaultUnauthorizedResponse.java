/*******************************************************************************
 * Copyright (c) 2009 MATERNA Information & Communications. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html. For further
 * project-related information visit http://www.ws4d.org. The most recent
 * version of the JMEDS framework can be obtained from
 * http://sourceforge.net/projects/ws4d-javame.
 ******************************************************************************/
package org.ws4d.java.communication.protocol.http.server.responses;

import java.io.IOException;
import java.io.OutputStream;

import org.ws4d.java.communication.ProtocolData;
import org.ws4d.java.communication.monitor.MonitoringContext;
import org.ws4d.java.communication.protocol.http.HTTPResponse;
import org.ws4d.java.communication.protocol.http.HTTPResponseUtil;
import org.ws4d.java.communication.protocol.http.header.HTTPRequestHeader;
import org.ws4d.java.communication.protocol.http.header.HTTPResponseHeader;
import org.ws4d.java.constants.HTTPConstants;
import org.ws4d.java.html.SimpleHTML;
import org.ws4d.java.types.InternetMediaType;
import org.ws4d.java.types.URI;

public class DefaultUnauthorizedResponse extends HTTPResponse {

	private HTTPRequestHeader	requestHeader	= null;

	private byte[]				b				= null;

	public DefaultUnauthorizedResponse(HTTPRequestHeader requestHeader) {
		this.requestHeader = requestHeader;

		SimpleHTML html = new SimpleHTML("401 Authorization Required");
		html.addHeading("Authorization Required");
		html.addHorizontalRule();
		html.addParagraph("<i>Java Multi Edition DPWS Framework</i>");

		b = html.getData();
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.ws4d.java.communication.protocol.http.HTTPResponse#getResponseHeader
	 * ()
	 */
	public HTTPResponseHeader getResponseHeader() {
		HTTPResponseHeader responseHeader = HTTPResponseUtil.getResponseHeader(401);
		responseHeader.addHeaderFieldValue(HTTPConstants.HTTP_HEADER_CONTENT_LENGTH, String.valueOf(b.length));
		responseHeader.addHeaderFieldValue(HTTPConstants.HTTP_HEADER_CONTENT_TYPE, InternetMediaType.getTextHTML().getMediaType());
		responseHeader.addHeaderFieldValue(HTTPConstants.HTTP_HEADER_WWW_AUTH, "Basic " + requestHeader.getRequest());
		responseHeader.addHeaderFieldValue("JMEDS-Debug", requestHeader.getRequest());
		return responseHeader;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.ws4d.java.communication.protocol.http.HTTPResponse#serializeResponseBody
	 * (org.ws4d.java.types.URI,
	 * org.ws4d.java.communication.protocol.http.header.HTTPRequestHeader,
	 * java.io.OutputStream, org.ws4d.java.communication.ProtocolData,
	 * org.ws4d.java.communication.monitor.MonitoringContext)
	 */
	public void serializeResponseBody(URI request, HTTPRequestHeader header, OutputStream out, ProtocolData protocolData, MonitoringContext context) throws IOException {
		out.write(b);
		out.flush();
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.communication.protocol.http.HTTPResponse#waitFor()
	 */
	public void waitFor() {
		// void
	}

}
