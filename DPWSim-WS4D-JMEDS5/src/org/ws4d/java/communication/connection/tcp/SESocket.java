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

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;

import org.ws4d.java.communication.connection.ip.IPAddress;
import org.ws4d.java.communication.connection.ip.IPNetworkDetection;
import org.ws4d.java.util.Log;

/**
 * This class implements a connection for the SE Platform.
 */
public class SESocket implements Socket {

	private static final String	ANY_ADDRESS	= "0.0.0.0";

	java.net.Socket				socket;

	private IPAddress			ipAddress	= null;

	private int					port		= -1;

	private InputStream			in			= null;

	private OutputStream		out			= null;

	/**
	 * Default constructor. Initializes the object.
	 * 
	 * @param host host name.
	 * @param port port number.
	 * @throws IOException
	 */
	public SESocket(IPAddress host, int port) throws IOException {
		String adr = host.getAddressWithoutNicId();
		socket = new java.net.Socket(adr, port);
		this.port = socket.getLocalPort();
	}

	public SESocket(java.net.Socket socket, IPAddress address) {
		this.socket = socket;
		this.ipAddress = address;
		this.port = socket.getLocalPort();
	}

	/**
	 * Closes the connection.
	 */
	public void close() throws IOException {
		if (socket == null) {
			throw new IOException("No open connection. Can not close connection");
		}
		socket.close();
	}

	/**
	 * Opens an <code>InputStream</code> on the socket.
	 * 
	 * @return an InputStream.
	 */
	public InputStream getInputStream() throws IOException {
		if (socket == null) {
			throw new IOException("No open connection. Can not open input stream");
		}
		if (in == null) {
			in = socket.getInputStream();
		}
		return in;
	}

	/**
	 * Opens an <code>OutputStream</code> on the socket.
	 * 
	 * @return an OutputStream.
	 */
	public OutputStream getOutputStream() throws IOException {
		if (socket == null) {
			throw new IOException("No open connection. Can not open output stream");
		}
		if (out == null) {
			out = new BufferedOutputStream(socket.getOutputStream());
		}
		return out;
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.communication.connection.tcp.Socket#getRemoteAddress()
	 */
	public IPAddress getRemoteAddress() {
		if (socket == null) return null;
		InetAddress i = socket.getInetAddress();
		if (i != null) {
			return new IPAddress(i.getHostAddress());
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.communication.connection.tcp.Socket#getRemotePort()
	 */
	public int getRemotePort() {
		if (socket == null) return -1;
		return socket.getPort();
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.communication.connection.tcp.Socket#getLocalAddress()
	 */
	public IPAddress getLocalAddress() {
		InetAddress localInetAdr = socket.getLocalAddress();
		String localAdr = localInetAdr.getHostAddress();

		/*
		 * local (any address wildcard) workaround. will use the remote address
		 * as local address in case of local address was 0.0.0.0. We assume the
		 * remote address to be the local one if the local address was a
		 * wildcard.
		 */

		if (localAdr.equals(ANY_ADDRESS)) {
			localAdr = socket.getInetAddress().getHostAddress();
			if (Log.isDebug()) {
				Log.debug("Local IP address workaround used. Local address was a (0.0.0.0) wildcard.");
			}
		}

		if (ipAddress == null) ipAddress = IPNetworkDetection.getInstance().getIPAddress(localAdr);

		return ipAddress;
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.communication.connection.tcp.Socket#getLocalPort()
	 */
	public int getLocalPort() {
		return port;
	}

}
