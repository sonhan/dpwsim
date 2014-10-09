/*******************************************************************************
 * Copyright (c) 2009 MATERNA Information & Communications. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html. For further
 * project-related information visit http://www.ws4d.org. The most recent
 * version of the JMEDS framework can be obtained from
 * http://sourceforge.net/projects/ws4d-javame.
 ******************************************************************************/
package org.ws4d.java.communication.protocol.http.header;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.ws4d.java.communication.ResponseHeader;
import org.ws4d.java.communication.protocol.http.HTTPStatus;
import org.ws4d.java.communication.protocol.http.HTTPUtil;
import org.ws4d.java.constants.HTTPConstants;
import org.ws4d.java.constants.Specialchars;
import org.ws4d.java.structures.HashMap;
import org.ws4d.java.structures.Iterator;

/**
 * This class represents the HTTP response header.
 */
public class HTTPResponseHeader extends HTTPHeader implements HTTPStatus, ResponseHeader {

	private String	version	= null;

	private int		status	= 0;

	private String	reason	= null;

	/**
	 * HTTP response Header
	 * 
	 * @param version HTTP Version
	 * @param status HTTP status code.
	 * @param reason reason phrase.
	 */
	public HTTPResponseHeader(String version, int status, String reason) {
		super();
		this.version = version;
		this.status = status;
		this.reason = reason;
	}

	/**
	 * HTTP response Header
	 * 
	 * @param version HTTP Version
	 * @param status HTTP status code.
	 * @param reason reason phrase.
	 * @param headerfields the HTTP header fields.
	 */
	public HTTPResponseHeader(String version, int status, String reason, HashMap headerfields) {
		super();
		this.version = version;
		this.status = status;
		this.reason = reason;
		this.headerfields = headerfields;
	}

	/**
	 * Returns the HTTP version for this response.
	 * 
	 * @return the version
	 */
	public String getVersion() {
		return version;
	}

	/**
	 * Returns the HTTP status code for this response.
	 * 
	 * @return the status
	 */
	public int getStatus() {
		return status;
	}

	/**
	 * Returns the HTTP reason phrase for this response.
	 * 
	 * @return the reason
	 */
	public String getReason() {
		return reason;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		StringBuffer sBuf = new StringBuffer();
		sBuf.append("HTTP response [ ");
		sBuf.append("status=");
		sBuf.append(status);
		sBuf.append(", ");
		sBuf.append("reason=");
		sBuf.append(reason);
		sBuf.append(" ]");
		return sBuf.toString();

	}

	/**
	 * Writes the HTTP response header to the given stream.
	 * 
	 * @param stream the stream to write the HTTP header to.
	 * @throws IOException
	 */
	public void toStream(OutputStream stream) throws IOException {
		// write header to stream
		stream.write(HTTPConstants.HTTP_VERSION11.getBytes());
		stream.write((char) Specialchars.SP);
		stream.write(String.valueOf(status).getBytes());
		stream.write((char) Specialchars.SP);
		stream.write(reason.getBytes());
		stream.write((char) Specialchars.CR);
		stream.write((char) Specialchars.LF);
		if (headerfields != null && headerfields.size() > 0) {
			Iterator fields = headerfields.keySet().iterator();
			while (fields.hasNext()) {
				String fieldname = (String) fields.next();
				String fieldvalue = (String) headerfields.get(fieldname);
				stream.write(HTTPUtil.camelCase(fieldname));
				stream.write((char) Specialchars.COL);
				stream.write((char) Specialchars.SP);
				stream.write(fieldvalue.getBytes());
				stream.write((char) Specialchars.CR);
				stream.write((char) Specialchars.LF);
			}
		}
		stream.write((char) Specialchars.CR);
		stream.write((char) Specialchars.LF);
	}

	/**
	 * Returns the byte array representation of this response header.
	 * 
	 * @return the byte array containing the header data.
	 */
	public byte[] getBytes() {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try {
			toStream(out);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return out.toByteArray();
	}

}
