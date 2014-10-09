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
import java.io.InputStream;
import java.io.OutputStream;

import org.ws4d.java.communication.ProtocolData;
import org.ws4d.java.communication.Resource;
import org.ws4d.java.communication.monitor.MonitoringContext;
import org.ws4d.java.communication.protocol.http.HTTPResponse;
import org.ws4d.java.communication.protocol.http.HTTPResponseUtil;
import org.ws4d.java.communication.protocol.http.header.HTTPRequestHeader;
import org.ws4d.java.communication.protocol.http.header.HTTPResponseHeader;
import org.ws4d.java.constants.HTTPConstants;
import org.ws4d.java.structures.HashMap;
import org.ws4d.java.structures.Iterator;
import org.ws4d.java.structures.Set;
import org.ws4d.java.types.URI;

public class DefaultResourceResponse extends HTTPResponse {

	private final Resource		resource;

	private final InputStream	requestBody;

	private HTTPResponseHeader	responseHeader	= null;

	public DefaultResourceResponse(Resource resource, InputStream requestBody) {
		this.resource = resource;
		this.requestBody = requestBody;

		long size = resource.size();
		responseHeader = HTTPResponseUtil.getResponseHeader(200);
		responseHeader.addHeaderFieldValue(HTTPConstants.HTTP_HEADER_CONTENT_TYPE, resource.getContentType().getMediaType());

		HashMap resourceHeaderFields = resource.getHeaderFields();

		/*
		 * Check for additional header fields.
		 */
		if (resourceHeaderFields != null) {
			Set keys = resourceHeaderFields.keySet();
			Iterator it = keys.iterator();
			while (it.hasNext()) {
				String key = (String) it.next();
				String value = (String) resourceHeaderFields.get(key);
				/*
				 * TODO Maybe it is necessary to filter the header fields which
				 * are already set by the header. At the moment the resource
				 * will overwrite the existing header fields.
				 */
				responseHeader.addHeaderFieldValue(key, value);
			}
		}

		/*
		 * Check whether we should use chunked encoding or not.
		 */
		if (size == -1) {
			responseHeader.addHeaderFieldValue(HTTPConstants.HTTP_HEADER_TRANSFER_ENCODING, HTTPConstants.HTTP_HEADERVALUE_TRANSFERCODING_CHUNKED);
		} else {
			responseHeader.addHeaderFieldValue(HTTPConstants.HTTP_HEADER_CONTENT_LENGTH, Long.toString(size));
		}
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.ws4d.java.communication.protocol.http.HTTPResponse#getResponseHeader
	 * ()
	 */
	public HTTPResponseHeader getResponseHeader() {
		return responseHeader;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.ws4d.java.communication.protocol.http.HTTPResponse#serializeResponseBody
	 * (org.ws4d.java.types.uri.URI,
	 * org.ws4d.java.communication.protocol.http.header.HTTPRequestHeader,
	 * java.io.OutputStream, org.ws4d.java.communication.DPWSProtocolData)
	 */
	public void serializeResponseBody(URI request, HTTPRequestHeader header, OutputStream out, ProtocolData protocolData, MonitoringContext context) throws IOException {
		resource.serialize(request, header, requestBody, out);

		/*
		 * flushes the stream.
		 */
		out.flush();

	}
}
