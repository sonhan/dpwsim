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

/**
 * HTTP stream which writes bytes for a given length.
 * <p>
 * This stream should be used to write the HTTP body.
 * </p>
 */
public class HTTPOutputStream extends OutputStream {

	private long			length	= 0;

	private OutputStream	out		= null;

	private boolean			write	= false;

	public HTTPOutputStream(OutputStream out, long length) {
		this.length = length;
		this.out = out;
	}

	/**
	 * Sets the length of the HTTP body.
	 * <p>
	 * Can only be set BEFORE someone writes bytes to this stream.
	 * </p>
	 * 
	 * @param length the length to set.
	 */
	public void setLength(long length) {
		if (write) throw new IndexOutOfBoundsException("Cannot set length for the HTTP output stream. Bytes are already written to this stream.");
		this.length = length;
	}

	/*
	 * (non-Javadoc)
	 * @see java.io.OutputStream#write(int)
	 */
	public void write(int arg0) throws IOException {
		if (length > 0) {
			length--;
			out.write(arg0);
			write = true;
		}
	}

	/*
	 * (non-Javadoc)
	 * @see java.io.OutputStream#write(byte[], int, int)
	 */
	public void write(byte[] b, int off, int len) throws IOException {
		if ((length - len) >= 0) {
			out.write(b, off, len);
			length -= len;
			write = true;
		}
	}

	/*
	 * (non-Javadoc)
	 * @see java.io.OutputStream#flush()
	 */
	public void flush() throws IOException {
		out.flush();
	}

	/*
	 * (non-Javadoc)
	 * @see java.io.OutputStream#close()
	 */
	public void close() throws IOException {
		out.flush();
		out.close();
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((out == null) ? 0 : out.hashCode());
		return result;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		HTTPOutputStream other = (HTTPOutputStream) obj;
		if (out == null) {
			if (other.out != null) return false;
		} else if (!out.equals(other.out)) return false;
		return true;
	}

}
