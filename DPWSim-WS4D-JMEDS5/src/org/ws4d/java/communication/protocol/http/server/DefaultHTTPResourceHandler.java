/*******************************************************************************
 * Copyright (c) 2009 MATERNA Information & Communications. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html. For further
 * project-related information visit http://www.ws4d.org. The most recent
 * version of the JMEDS framework can be obtained from
 * http://sourceforge.net/projects/ws4d-javame.
 ******************************************************************************/
package org.ws4d.java.communication.protocol.http.server;

import java.io.IOException;
import java.io.InputStream;

import org.ws4d.java.communication.ProtocolData;
import org.ws4d.java.communication.Resource;
import org.ws4d.java.communication.monitor.MonitoringContext;
import org.ws4d.java.communication.protocol.http.HTTPResponse;
import org.ws4d.java.communication.protocol.http.header.HTTPRequestHeader;
import org.ws4d.java.communication.protocol.http.server.responses.DefaultResourceResponse;
import org.ws4d.java.types.URI;
import org.ws4d.java.util.Log;

/**
 * Default implementation of an HTTP handler which allows access to a resource.
 */
public class DefaultHTTPResourceHandler implements HTTPRequestHandler {

	private final Resource	resource;

	/**
	 * Creates a default HTTP resource handler with a given buffer size.
	 * 
	 * @param resource the resource to send.
	 */
	public DefaultHTTPResourceHandler(Resource resource) {
		this.resource = resource;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.ws4d.java.communication.protocol.http.server.HTTPRequestHandler#handle
	 * (org.ws4d.java.types.uri.URI,
	 * org.ws4d.java.communication.protocol.http.header.HTTPRequestHeader,
	 * java.io.InputStream, org.ws4d.java.communication.DPWSProtocolData,
	 * org.ws4d.java.io.monitor.MonitoringContext)
	 */
	public HTTPResponse handle(URI request, HTTPRequestHeader header, InputStream body, ProtocolData protocolData, MonitoringContext context) throws IOException {
		if (Log.isDebug()) {
			Log.debug("<I> Accessing HTTP resource at " + request, Log.DEBUG_LAYER_COMMUNICATION);
		}

		return new DefaultResourceResponse(resource, body);
	}
}
