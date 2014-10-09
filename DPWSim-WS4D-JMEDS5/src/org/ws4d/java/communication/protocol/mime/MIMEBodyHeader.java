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

import org.ws4d.java.structures.HashMap;
import org.ws4d.java.structures.Iterator;
import org.ws4d.java.util.MIMEUtil;

/**
 * This class represents the MIME body header.
 */
public class MIMEBodyHeader {

	private HashMap	headerfields	= null;

	/**
	 * MIME body header.
	 * 
	 * @param headerfields <code>Map</code> containing the HTTP header fields.
	 */
	public MIMEBodyHeader(HashMap headerfields) {
		this.headerfields = headerfields;
	}

	/**
	 * MIME body header.
	 * 
	 * @param headerfields <code>Map</code> containing the HTTP header fields.
	 */
	public MIMEBodyHeader() {
		this.headerfields = new HashMap();
	}

	/**
	 * Returns the header value for the requested header field.
	 * 
	 * @param fieldname the field to get the value from.
	 * @return the value.
	 */
	public String getHeaderFieldValue(String fieldname) {
		return (String) headerfields.get(fieldname.toLowerCase());
	}

	public void setHeaderField(String fieldname, String fieldvalue) {
		headerfields.put(fieldname.toLowerCase(), fieldvalue);
	}

	/**
	 * Returns a <code>String</code> representation of the MIME header
	 * containing all header fields.
	 * 
	 * @return a string representation of the MIME header.
	 */
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append("MIME header [ ");
		Iterator it = headerfields.keySet().iterator();
		if (it == null) return new String();
		while (it.hasNext()) {
			String fieldname = (String) it.next();
			String fieldvalue = (String) headerfields.get(fieldname);
			buffer.append(fieldname);
			buffer.append("=");
			buffer.append(fieldvalue);
			if (it.hasNext()) {
				buffer.append(", ");
			}
		}
		buffer.append(" ]");
		return buffer.toString();
	}

	public void toStream(OutputStream out) throws IOException {
		MIMEUtil.writeHeaderFields(out, headerfields);
	}

}
