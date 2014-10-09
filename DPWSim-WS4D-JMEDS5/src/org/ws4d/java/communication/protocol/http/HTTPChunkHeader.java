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

import org.ws4d.java.communication.protocol.http.header.HTTPHeader;
import org.ws4d.java.constants.Specialchars;
import org.ws4d.java.structures.HashMap;

/**
 * This class represents the HTTP chunk header.
 */
class HTTPChunkHeader extends HTTPHeader {

	private int		size;

	private HashMap	extensions	= null;

	HTTPChunkHeader(int size, HashMap extensions, HashMap headerfields) {
		this.size = size;
		this.extensions = extensions;
		this.headerfields = headerfields;
	}

	/**
	 * Returns the extension value for the requested extension field.
	 * 
	 * @param fieldname the field to get the value from.
	 * @return the value.
	 */
	public String getExtensionFieldValue(String fieldname) {
		return (String) extensions.get(fieldname.toLowerCase());
	}

	/**
	 * Returns the chunk size.
	 * 
	 * @return the size
	 */
	public int getSize() {
		return size;
	}

	/**
	 * Writes the HTTP chunk header to the stream.
	 * 
	 * @param out outputstream to write to.
	 * @throws IOException
	 */
	public void toStream(OutputStream out) throws IOException {
		out.write(Integer.toHexString(size).getBytes());
		out.write(Specialchars.CR);
		out.write(Specialchars.LF);
	}

	/**
	 * Returns the byte array representation of this HTTP chunk header.
	 * 
	 * @return byte array.
	 */
	public byte[] getBytes() {
		StringBuffer buffer = new StringBuffer();
		buffer.append(Integer.toHexString(size));
		buffer.append((char) Specialchars.CR);
		buffer.append((char) Specialchars.LF);
		return buffer.toString().getBytes();
	}
}
