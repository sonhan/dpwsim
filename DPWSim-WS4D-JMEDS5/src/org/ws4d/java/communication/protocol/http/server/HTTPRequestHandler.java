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
import org.ws4d.java.communication.monitor.MonitoringContext;
import org.ws4d.java.communication.protocol.http.HTTPResponse;
import org.ws4d.java.communication.protocol.http.header.HTTPRequestHeader;
import org.ws4d.java.types.URI;

/**
 * HTTP handler interface.
 * <p>
 * A HTTP handler can be registered to a HTTP server and will handle incoming
 * HTTP requests.
 * </p>
 * <p>
 * A HTTP request is represented by {@link DefaultHTTPExchange}.
 * </p>
 */
public interface HTTPRequestHandler {

	/**
	 * Handles a HTTP request.
	 * 
	 * @param exchange the HTTP request represented by the
	 *            {@link DefaultHTTPExchange}.
	 * @param protocolData transport/addressing related information attached to
	 *            the request
	 * @throws IOException
	 */
	public HTTPResponse handle(URI request, HTTPRequestHeader header, InputStream body, ProtocolData protocolData, MonitoringContext context) throws IOException;

}
