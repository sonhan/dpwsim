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
import org.ws4d.java.types.InternetMediaType;

/**
 * Interface for HTTP request implementations.
 * <p>
 * This interface allows to queue a HTTP request into the {@link HTTPClient}.
 * </p>
 */
public interface HTTPRequest {

	/**
	 * Returns the HTTP header for this HTTP request.
	 * 
	 * @return the HTTP request header.
	 */
	public HTTPRequestHeader getRequestHeader();

	/**
	 * Serializes the HTTP body to the given stream.
	 * 
	 * @param out the stream to serialize the body.
	 * @param protocolData addressing information associated to the request
	 * @throws IOException
	 */
	public void serializeRequestBody(OutputStream out, ProtocolData protocolData, MonitoringContext context) throws IOException;

	/**
	 * This method allows the handling of this request's incoming response.
	 * <p>
	 * The {@link HTTPClient} will call this method for a incoming response. If
	 * no {@link HTTPResponseHandler} belongs to this request, the HTTP client
	 * will check the internal handler list.
	 * </p>
	 * 
	 * @param mediaType The Internet media type of the response.
	 * @return The {@link HTTPResponseHandler} which should handle the response.
	 * @throws IOException
	 */
	public HTTPResponseHandler getResponseHandler(InternetMediaType mediaType) throws IOException;

	/**
	 * This method will be invoked if an exception occurs whilst sending the
	 * HTTP request.
	 * <p>
	 * This method allows to check for exceptions during the HTTP request.
	 * </p>
	 * 
	 * @param e the exception.
	 */
	public void requestSendFailed(Exception e, ProtocolData protocolData);

	/**
	 * This method will be invoked if an exception occurs whilst receiving the
	 * HTTP response.
	 * <p>
	 * This method allows to check for exceptions during the HTTP response.
	 * </p>
	 * 
	 * @param e the exception.
	 */
	public void responseReceiveFailed(Exception e, ProtocolData protocolData);

}
