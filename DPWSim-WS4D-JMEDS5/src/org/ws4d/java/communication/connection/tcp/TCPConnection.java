/*******************************************************************************
 * Copyright (c) 2009 MATERNA Information & Communications. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html. For further
 * project-related information visit http://www.ws4d.org. The most recent
 * version of the JMEDS framework can be obtained from
 * http://sourceforge.net/projects/ws4d-javame.
 ******************************************************************************/
package org.ws4d.java.communication.connection.tcp;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.ws4d.java.communication.DPWSProtocolData;
import org.ws4d.java.communication.ProtocolData;
import org.ws4d.java.util.Log;

/**
 * TCP connection.
 * <p>
 * This class represents an existing TCP connection based on a socket
 * implementation. The socket implementation is stored inside this class to
 * allow to correctly close the connection.
 * </p>
 */
public class TCPConnection {

	private final InputStream		in;

	private final OutputStream		out;

	private final Socket			socket;

	private final DPWSProtocolData	protocolData;

	private boolean					closed		= false;

	private boolean					fstRead		= true;

	private boolean					fstWrite	= true;

	public TCPConnection(InputStream in, OutputStream out, Socket socket) {
		this(in, out, socket, new DPWSProtocolData(null, ProtocolData.DIRECTION_OUT, socket.getLocalAddress().getAddressWithoutNicId(), socket.getLocalPort(), socket.getRemoteAddress().getAddressWithoutNicId(), socket.getRemotePort(), true));
	}

	public TCPConnection(InputStream in, OutputStream out, Socket socket, DPWSProtocolData pd) {
		this.in = in;
		this.out = out;
		this.socket = socket;
		this.protocolData = pd;
	}

	/**
	 * Returns the input stream for this connection.
	 * 
	 * @return input stream for this connection.
	 */
	public InputStream getInputStream() {
		if (Log.isDebug()) {
			return new InputStreamWrapper(in, this);
		} else {
			return in;
		}
	}

	/**
	 * Returns the output stream for this connection.
	 * 
	 * @return output stream for this connection.
	 */
	public OutputStream getOutputStream() {
		if (Log.isDebug()) {
			return new OutputStreamWrapper(out, this);
		} else {
			return out;
		}
	}

	/**
	 * Returns the transport/addressing information belonging to this TCP
	 * connection. This includes the unique connection ID and the source and
	 * destination addresses and ports.
	 * 
	 * @return the addressing information belonging to this connection
	 */
	public DPWSProtocolData getProtocolData() {
		return protocolData;
	}

	/**
	 * Closes this connection.
	 * <p>
	 * This will close the input and output stream and the socket.
	 * </p>
	 * 
	 * @throws IOException
	 */
	public void close() throws IOException {
		if (closed) return;
		out.flush();
		in.close();
		out.close();
		socket.close();
		closed = true;
	}

	/**
	 * Returns the identifier for this connection.
	 * 
	 * @return identifier for this connection.
	 */
	public Long getIdentifier() {
		return protocolData.getInstanceId();
	}

	public String toString() {
		if (protocolData != null) {
			return "TCP Connection [ id = " + protocolData.getInstanceId() + " ]";
		} else {
			return "TCP Connection";
		}
	}

	synchronized boolean isFirstRead() {
		return fstRead;
	}

	synchronized boolean isFirstWrite() {
		return fstWrite;
	}

	synchronized void firstRead() {
		fstRead = false;
	}

	synchronized void firstWrite() {
		fstWrite = false;
	}

	/**
	 * Returns <code>true</code> if the connection is closed, <code>false</code>
	 * otherwise.
	 * 
	 * @return <code>true</code> if the connection is closed, <code>false</code>
	 *         otherwise.
	 */
	public boolean isClosed() {
		return closed;
	}

	private class InputStreamWrapper extends InputStream {

		private InputStream		in			= null;

		private TCPConnection	connection	= null;

		InputStreamWrapper(InputStream in, TCPConnection connection) {
			this.in = in;
			this.connection = connection;
		}

		public int read() throws IOException {
			if (connection.isFirstRead() && Log.isDebug()) {
				connection.firstRead();
				Log.debug("<I-TCP> Reading data, " + connection, Log.DEBUG_LAYER_COMMUNICATION);
			}
			return in.read();
		}

		public int read(byte[] b, int off, int len) throws IOException {
			if (connection.isFirstRead() && Log.isDebug()) {
				connection.firstRead();
				Log.debug("<I-TCP> Reading data, " + connection, Log.DEBUG_LAYER_COMMUNICATION);
			}
			return in.read(b, off, len);
		}

		public int read(byte[] b) throws IOException {
			if (connection.isFirstRead() && Log.isDebug()) {
				connection.firstRead();
				Log.debug("<I-TCP> Reading data, " + connection, Log.DEBUG_LAYER_COMMUNICATION);
			}
			return in.read(b);
		}

		public void close() throws IOException {
			in.close();
		}

		public int available() throws IOException {
			return in.available();
		}

		public synchronized void mark(int readlimit) {
			in.mark(readlimit);
		}

		public boolean markSupported() {
			return in.markSupported();
		}

		public synchronized void reset() throws IOException {
			in.reset();
		}

		public long skip(long n) throws IOException {
			return in.skip(n);
		}

	}

	private class OutputStreamWrapper extends OutputStream {

		private OutputStream	out			= null;

		private TCPConnection	connection	= null;

		OutputStreamWrapper(OutputStream out, TCPConnection connection) {
			this.out = out;
			this.connection = connection;
		}

		public void write(int arg0) throws IOException {
			if (connection.isFirstWrite() && Log.isDebug()) {
				connection.firstWrite();
				Log.debug("<O-TCP> Sending data, " + connection, Log.DEBUG_LAYER_COMMUNICATION);
			}
			out.write(arg0);
		}

		public void write(byte[] b) throws IOException {
			if (connection.isFirstWrite() && Log.isDebug()) {
				connection.firstWrite();
				Log.debug("<O-TCP> Sending data, " + connection, Log.DEBUG_LAYER_COMMUNICATION);
			}
			out.write(b);
		}

		public void write(byte[] b, int off, int len) throws IOException {
			if (connection.isFirstWrite() && Log.isDebug()) {
				connection.firstWrite();
				Log.debug("<O-TCP> Sending data, " + connection, Log.DEBUG_LAYER_COMMUNICATION);
			}
			out.write(b, off, len);
		}

		public void close() throws IOException {
			out.close();
		}

		public void flush() throws IOException {
			out.flush();
		}

	}

}
