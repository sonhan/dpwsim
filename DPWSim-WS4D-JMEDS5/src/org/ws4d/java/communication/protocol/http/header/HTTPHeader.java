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

import org.ws4d.java.structures.HashMap;

public class HTTPHeader {

	protected HashMap	headerfields	= null;

	public HTTPHeader() {
		headerfields = new HashMap();
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

	/**
	 * Adds a header value to this header.
	 * 
	 * @param fieldname the field to add.
	 * @param value the value for this field.
	 */
	public void addHeaderFieldValue(String name, String value) {
		headerfields.put(name.toLowerCase(), value);
	}

	/**
	 * Removes a header value from this header.
	 * 
	 * @param fieldname the field to remove.
	 */
	public void removeHeaderFieldValue(String name) {
		headerfields.remove(name.toLowerCase());
	}

}
