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

import java.io.IOException;

import org.ws4d.java.communication.connection.ip.IPAddress;

/**
 * Datagram socket for UDP connections.
 */
public interface DatagramSocket {

	/*
	 * Whether to forward messages we send to our own loopback interface or not.
	 */
	// void setMulticastLoopback(boolean loop);

	/**
	 * Sends a datagram. The <code>Datagram</code> object includes the
	 * information indicating what data to send, its length, and the address of
	 * the receiver. The method sends length bytes starting at the current
	 * <code>offset</code> of the Datagram object, where length and
	 * <code>offset</code> are internal state variables of the Datagram object.
	 * 
	 * @param datagram A datagram.
	 * @throws IOException
	 */
	void send(Datagram datagram) throws IOException;

	/**
	 * Receives a datagram. When this method returns, the internal buffer in the
	 * Datagram object is filled with the data received, starting at the
	 * location determined by the offset state variable. The data is ready to be
	 * read using the methods of the DataInput interface. This method remains
	 * idle until a datagram is received. The internal length state variable in
	 * the Datagram object contains the length of the received datagram. If the
	 * received data is longer than the length of the internal buffer minus
	 * offset, the data is truncated. This method does not change the internal
	 * read/write state variable of the Datagram object. Use method
	 * Datagram.reset to change the pointer before reading if necessary.
	 * 
	 * @return A datagram.
	 * @throws IOException
	 */
	Datagram receive() throws IOException;

	/**
	 * Closes the connection.
	 * 
	 * @throws IOException
	 */
	void close() throws IOException;

	/**
	 * Returns <code>datagram</code> to this socket for reusability.
	 * 
	 * @param datagram the datagram, which can be reused by this socket
	 */
	void release(Datagram datagram);

	/**
	 * Returns the address this socket is bound to.
	 * 
	 * @return the address.
	 */
	IPAddress getSocketAddress();

	/**
	 * Returns the port this socket is bound to.
	 * 
	 * @return the port.
	 */
	int getSocketPort();

}
