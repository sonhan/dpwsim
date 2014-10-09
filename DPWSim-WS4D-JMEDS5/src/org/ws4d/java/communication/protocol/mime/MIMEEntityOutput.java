/*******************************************************************************
 * Copyright (c) 2009 MATERNA Information & Communications. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html. For further
 * project-related information visit http://www.ws4d.org. The most recent
 * version of the JMEDS framework can be obtained from
 * http://sourceforge.net/projects/ws4d-javame.
 ******************************************************************************/
package org.ws4d.java.communication.protocol.mime;

import java.io.IOException;
import java.io.OutputStream;

import org.ws4d.java.communication.protocol.http.HTTPResponse;

/**
 * This interfaces allows the creation of MIME entities which will be sent as
 * part of a multipart/releated HTTP response.
 */
public interface MIMEEntityOutput extends MIMEBase {

	/**
	 * Serializes the MIME part body.
	 * 
	 * @param out the output stream to serialize the MIME entity.
	 * @throws IOException
	 */
	public void serialize(OutputStream out) throws IOException;

	/**
	 * This method allows special usage of a HTTP response.
	 * <p>
	 * The {@link DefaultMIMEHandler} will check the first MIME entity for a
	 * HTTP response. If a response is set, this HTTP response will be used. The
	 * {@link DefaultMIMEHandler} will NOT proceed with the queued MIME entity.
	 * </p>
	 * 
	 * @return the HTTP response.
	 */
	public HTTPResponse getHTTPResponse();

}
