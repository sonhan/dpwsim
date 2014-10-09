/*******************************************************************************
 * Copyright (c) 2009 MATERNA Information & Communications. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html. For further
 * project-related information visit http://www.ws4d.org. The most recent
 * version of the JMEDS framework can be obtained from
 * http://sourceforge.net/projects/ws4d-javame.
 ******************************************************************************/
package org.ws4d.java.communication.protocol.http;

import java.io.IOException;
import java.io.OutputStream;

import org.ws4d.java.communication.ProtocolData;
import org.ws4d.java.communication.monitor.MonitoringContext;
import org.ws4d.java.communication.protocol.http.header.HTTPRequestHeader;
import org.ws4d.java.communication.protocol.http.header.HTTPResponseHeader;
import org.ws4d.java.communication.protocol.http.server.HTTPRequestHandler;
import org.ws4d.java.communication.protocol.mime.MIMEReader;
import org.ws4d.java.types.URI;

public abstract class HTTPResponse {
	
	MIMEReader readerToWaitFor = null;

	public abstract HTTPResponseHeader getResponseHeader();

	public abstract void serializeResponseBody(URI request, HTTPRequestHeader header, OutputStream out, ProtocolData protocolData, MonitoringContext context) throws IOException;

	/**
	 * The purpose of this method is to make the HTTP server wait before
	 * accepting the next request over the same connection, consuming unread
	 * bytes from the connection's input stream or closing the connection. This
	 * method must block until the corresponding {@link HTTPRequestHandler}
	 * which generated this {@link HTTPResponse} instance is done with request
	 * processing.
	 */
	public void waitFor() {
		if (readerToWaitFor == null) {
			return;
		}
		readerToWaitFor.waitFor();
	}

	public void setMIMEReaderToWaitFor(MIMEReader readerToWaitFor) {
		this.readerToWaitFor = readerToWaitFor;
	}
}
