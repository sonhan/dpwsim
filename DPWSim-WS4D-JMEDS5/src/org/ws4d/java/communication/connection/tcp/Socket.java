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

import org.ws4d.java.communication.connection.ip.IPAddress;

/**
 * Represents a connection. Used to decouple the stack from CLDC and Java SE.
 * Socket implementations.
 */
public interface Socket {

	/**
	 * Returns an input stream for this socket.
	 * 
	 * @return the InputStream.
	 * @throws IOException an I/O exception.
	 */
	InputStream getInputStream() throws IOException;

	/**
	 * Returns an output stream for this socket.
	 * 
	 * @return the OutputStream.
	 * @throws IOException an I/O exception.
	 */
	OutputStream getOutputStream() throws IOException;

	/**
	 * Closes the connection.
	 * 
	 * @throws IOException an I/O exception.
	 */
	void close() throws IOException;

	/**
	 * Returns the remote address of the endpoint that this socket is connected
	 * to, or <code>null</code> if it is disconnected. WARNING: CLDC does not
	 * support the remote address, so <code>null</code> will always be returned
	 * for CLDC.
	 * 
	 * @return the remote address.
	 */
	IPAddress getRemoteAddress();

	/**
	 * Returns the remote port of the endpoint to which this socket is
	 * connected, or <code>-1</code> if it is unconnected. WARNING: CLDC does
	 * not support the remote port, so you always get <code>-1</code> for CLDC.
	 * 
	 * @return
	 */
	int getRemotePort();

	/**
	 * Returns the local address for this socket.
	 * 
	 * @return the local address.
	 */
	IPAddress getLocalAddress();

	/**
	 * Returns the local port of this socket.
	 * 
	 * @return the local port.
	 */
	int getLocalPort();
}