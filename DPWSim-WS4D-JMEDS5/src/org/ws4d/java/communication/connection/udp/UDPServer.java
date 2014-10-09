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
import org.ws4d.java.structures.HashMap;
import org.ws4d.java.structures.Iterator;
import org.ws4d.java.util.Log;

/**
 * UPD server .
 */
public class UDPServer {

	private static final HashMap	listeners	= new HashMap();

	/**
	 * Opens a datagram socket for a given address and port.
	 * <p>
	 * This will start a new UDP listener for the given address and port. This
	 * listener will pass-through the incoming UDP datagram to the given UDP
	 * handler.
	 * </p>
	 * 
	 * @param address the address.
	 * @param port the port.
	 * @param ifaceName
	 * @param handler the UDP datagram handler which will handle the incoming
	 *            UDP datagram.
	 * @throws IOException will throw an IO exception if the datagram socket
	 *             could not be opened.
	 */
	public synchronized static void open(IPAddress ipAddress, int port, String ifaceName, UDPDatagramHandler handler) throws IOException {
		// THX @Stefan Schlichting: Added as legacy wrapper
		open(ipAddress, port, ifaceName, handler, false);
	}

	public synchronized static void open(IPAddress ipAddress, int port, String ifaceName, UDPDatagramHandler handler, boolean isMulticast) throws IOException {
		// THX @Stefan Schlichting: Changed method signature to allow the flag
		if (ipAddress == null) {
			throw new IOException("Cannot create UDP listener. No IP address given.");
		}
		if (port < 1 || port > 65535) {
			throw new IOException("Cannot create UDP listener Port number invalid.");
		}
		UDPListener listener = null;
		String key = ipAddress.getAddress() + "@" + port + "%" + ifaceName;
		listener = (UDPListener) listeners.get(key);
		if (listener != null) {
			throw new IOException("Cannot create UDP listener for " + ipAddress + " and port " + port + ". This address is already in use.");
		}
		listener = new UDPListener(ipAddress, port, ifaceName, handler, isMulticast);
		listeners.put(key, listener);
		listener.start();
	}

	/**
	 * Closes the created UDP connection listener by address and port.
	 * 
	 * @param address the address.
	 * @param port the port.
	 * @throws IOException
	 */
	public synchronized static void close(IPAddress ipAddress, int port, String ifaceName) throws IOException {
		if (ipAddress == null) {
			return;
		}
		if (port < 1 || port > 65535) {
			return;
		}
		UDPListener listener = null;
		listener = (UDPListener) listeners.get(ipAddress.getAddress() + "@" + port + "%" + ifaceName);
		if (listener == null) {
			return;
		}
		close(listener);
	}

	/**
	 * Closes the given UDP listener.
	 * 
	 * @param listener the listener which should be closed.
	 * @throws IOException
	 */
	private static void close(UDPListener listener) throws IOException {
		/*
		 * Remove both, the handler and address+port registration.
		 */
		Iterator it = listeners.values().iterator();
		while (it.hasNext()) {
			UDPListener l = (UDPListener) it.next();
			if (l == listener) {
				it.remove();
			}
		}
		listener.stop();
	}

	/**
	 * Sends a datagram packet with the given address and port.
	 * 
	 * @param address the source address.
	 * @param port the source port.
	 * @param dstAddress the destination address of the datagram packet.
	 * @param dstPort the destination port of the datagram packet.
	 * @param data the content of the datagram packet.
	 * @param len the length of the datagram packet.
	 * @throws IOException
	 */
	public synchronized static void send(IPAddress localAddress, int port, String ifaceName, IPAddress dstAddress, int dstPort, byte[] data, int len) throws IOException {
		if (localAddress == null) {
			return;
		}
		if (port < 1 || port > 65535) {
			return;
		}
		UDPListener listener = null;
		listener = (UDPListener) listeners.get(localAddress.getAddress() + "@" + port + "%" + ifaceName);
		if (listener == null) {
			return;
		}
		send(listener.getDatagramSocket(), dstAddress, dstPort, data, len);
	}

	/**
	 * Sends a datagram packet with the given handler.
	 * 
	 * @param transportAddress the source address.
	 * @param port the source port.
	 * @param dstAddress the destination address of the datagram packet.
	 * @param dstPort the destination port of the datagram packet.
	 * @param data the content of the datagram packet.
	 * @param len the length of the datagram packet.
	 * @throws IOException
	 */
	public synchronized static void send(UDPDatagramHandler handler, IPAddress dstAddress, int dstPort, byte[] data, int len) throws IOException {
		if (listeners == null || listeners.isEmpty()) return;
		Iterator it = listeners.values().iterator();
		while (it.hasNext()) {
			UDPListener listener = (UDPListener) it.next();
			UDPDatagramHandler udpHandler = listener.getUDPDatagramHandler();
			if (udpHandler == handler) {
				send(listener.getDatagramSocket(), dstAddress, dstPort, data, len);
			}
		}
	}

	/**
	 * Sends a datagram packet with the given socket.
	 * 
	 * @param socket the UDP datagram socket.
	 * @param dstAddress the destination address of the datagram packet.
	 * @param dstPort the destination port of the datagram packet.
	 * @param data the content of the datagram packet.
	 * @param len the length of the datagram packet.
	 * @throws IOException
	 */
	static void send(DatagramSocket socket, IPAddress dstAddress, int dstPort, byte[] data, int len) throws IOException {
		Datagram d = new Datagram(socket, data, len);
		d.setAddress(dstAddress);
		d.setPort(dstPort);
		if (Log.isDebug()) {
			Log.debug("<O-UDP> To " + dstAddress + "@" + dstPort + " from " + socket.getSocketAddress() + "@" + socket.getSocketPort() + ", " + d, Log.DEBUG_LAYER_COMMUNICATION);
		}
		socket.send(d);
	}

}
