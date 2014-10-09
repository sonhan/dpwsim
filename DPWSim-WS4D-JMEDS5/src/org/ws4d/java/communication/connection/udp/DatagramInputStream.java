/*******************************************************************************
 * Copyright (c) 2009 MATERNA Information & Communications. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html. For further
 * project-related information visit http://www.ws4d.org. The most recent
 * version of the JMEDS framework can be obtained from
 * http://sourceforge.net/projects/ws4d-javame.
 ******************************************************************************/
package org.ws4d.java.communication.connection.udp;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Internal datagram stream. This stream throws away the internal
 * <code>BufferedInputStream</code> if the whole datagram packet has been read.
 * This should provide better memory usage.
 */
public class DatagramInputStream extends InputStream {

	private Datagram	datagram;

	private InputStream	in		= null;

	private int			r		= 0;

	private int			size	= 0;

	public DatagramInputStream(Datagram datagram) {
		this.datagram = datagram;
		in = new ByteArrayInputStream(datagram.getData());
		// in = new BufferedInputStream(new ByteArrayInputStream(data), size);
		// CHANGED SSch It seems to better to use the content length and not the
		// length of the buffer
		// this.size = datagram.getLength();
		this.size = datagram.getContentLength();
	}

	public int read() throws IOException {
		if (in == null) return -1;
		if (r > size) {
			if (in != null) {
				// return to pool
				datagram.release();
				in = null;
			}
			return -1;
		}
		int i = in.read();
		r++;
		return i;
	}

	public int read(byte[] buffer) throws IOException {
		if (in == null) return -1;
		if (r > size) {
			if (in != null) {
				// return to pool
				datagram.release();
				in = null;
			}
			return -1;
		}
		int i = in.read(buffer);
		if (i < 0) {
			// return to pool
			datagram.release();
			return -1;
		}
		r += i;
		return i;
	}

	public int read(byte[] buffer, int off, int len) throws IOException {
		if (in == null) return -1;
		if (r > size) {
			if (in != null) {
				// return to pool
				datagram.release();
				in = null;
			}
			return -1;
		}
		int i = in.read(buffer, off, len);
		if (i < 0) {
			// return to pool
			datagram.release();
			return -1;
		}
		r += i;
		return i;
	}

	public int available() throws IOException {
		// Bugfix SSch 2011-01-13 in may be null
		return in != null ? in.available() : 0;
	}

	public void close() throws IOException {
		// Bugfix SSch 2011-01-13 in may be null
		if (in != null) in.close();
		// return to pool
		if (datagram != null) datagram.release();
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((in == null) ? 0 : in.hashCode());
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
		DatagramInputStream other = (DatagramInputStream) obj;
		if (in == null) {
			if (other.in != null) return false;
		} else if (!in.equals(other.in)) return false;
		return true;
	}

}
