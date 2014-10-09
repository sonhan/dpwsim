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

import org.ws4d.java.communication.connection.ip.IPAddress;

/**
 * Creates server and client sockets.
 */
public class PlatformSocketFactory extends SocketFactory {

	/**
	 * Creates an SE ServerSocket.
	 * 
	 * @param adr IP address.
	 * @param port port
	 * @return the ServerSocket.
	 * @throws IOException
	 */
	public ServerSocket createServerSocket(IPAddress adr, int port) throws IOException {
		return new SEServerSocket(adr, port);
	}

	/**
	 * Creates an SE Socket.
	 * 
	 * @param adr IP address.
	 * @param port port
	 * @return the ServerSocket.
	 * @throws IOException
	 */
	public Socket createSocket(IPAddress adr, int port) throws IOException {
		return new SESocket(adr, port);
	}
}
