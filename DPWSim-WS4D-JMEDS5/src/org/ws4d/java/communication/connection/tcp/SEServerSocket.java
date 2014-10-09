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
import java.net.InetAddress;

import org.ws4d.java.communication.connection.ip.IPAddress;

/**
 * This class encapsulates an SE listening socket.
 */
public class SEServerSocket implements ServerSocket {

	private IPAddress			ipAddress		= null;

	private int					port			= -1;

	java.net.ServerSocket		server			= null;

	/**
	 * The number of attempts to find a random port before giving up
	 */
	protected static final int	PORT_RETRIES	= 3;

	public SEServerSocket(IPAddress ipAddress, int port) throws IOException {
		InetAddress adr = InetAddress.getByName(ipAddress.getAddress());
		try {
			server = new java.net.ServerSocket(port, 0, adr);
			if (port == 0) {
				port = server.getLocalPort();
			}
		} catch (Exception e) {
			throw new IOException(e.getMessage() + " for " + ipAddress + " at port " + port);
		}
		this.ipAddress = ipAddress;
		this.port = port;
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.communication.connection.tcp.ServerSocket#accept()
	 */
	public Socket accept() throws IOException {
		return new SESocket(server.accept(), getIPAddress());
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.communication.connection.tcp.ServerSocket#close()
	 */
	public void close() throws IOException {
		server.close();
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.communication.connection.tcp.ServerSocket#getAddress()
	 */
	public IPAddress getIPAddress() {
		return ipAddress;
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.communication.connection.tcp.ServerSocket#getPort()
	 */
	public int getPort() {
		return port;
	}

}
