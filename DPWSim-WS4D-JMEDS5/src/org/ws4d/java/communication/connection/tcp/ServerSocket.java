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
 * Interface to hide the underlying ServerSocket implementation in order to
 * decouple the stack from CLDC, SE, etc.
 */
public interface ServerSocket {

	/**
	 * Closes the ServerSocket.
	 * 
	 * @throws IOException an I/O exception.
	 */
	void close() throws IOException;

	/**
	 * Waits until a client connects and returns an IConnection implementation.
	 * 
	 * @return An IConnection instance or null if the the Socket has been
	 *         closed.
	 * @throws IOException an I/O exception.
	 */
	Socket accept() throws IOException;

	/**
	 * Returns the local address.
	 * 
	 * @return the local address.
	 */
	IPAddress getIPAddress();

	/**
	 * Returns the local port.
	 * 
	 * @return the local port.
	 */
	int getPort();
}
