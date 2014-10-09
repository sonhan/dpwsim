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

import org.ws4d.java.communication.DPWSProtocolData;
import org.ws4d.java.communication.ProtocolData;
import org.ws4d.java.communication.connection.ip.IPAddress;
import org.ws4d.java.structures.HashMap;
import org.ws4d.java.util.Log;
import org.ws4d.java.util.TimedEntry;
import org.ws4d.java.util.WatchDog;

/**
 * UDP client which allows the sending of UDP datagram packets.
 */
public class UDPClient {

	/**
	 * Indicates whether this client is closed or not.
	 */
	private boolean				closed					= false;

	/**
	 * UDP listener timeout.
	 */
	private static final int	UDP_RECEIVER_TIMEOUT	= 20000;

	/**
	 * Local host address for the UDP datagram socket.
	 */
	private IPAddress			ipAddress				= null;

	/**
	 * Local host port for the UDP datagram socket.
	 */
	private int					port					= -1;

	private String				ifaceName;

	/**
	 * The listener if necessary. Used with
	 * {@link #send(String, int, byte[], int, UDPDatagramHandler)} method.
	 */
	private UDPListener			listener				= null;

	/**
	 * Local timeout object.
	 */
	private ListenerTimeout		timeout					= null;

	/**
	 * Table of UDP clients.
	 */
	private static HashMap		clients					= new HashMap();

	/**
	 * Returns a UDP client with address and port for incoming UDP messages. If
	 * no client exists, a new client will be created.
	 * <p>
	 * An UDP datagram socket has the possibility to send and receive UDP
	 * messages, that is why it is necessary to set the local address and port
	 * for the client.<br />
	 * The given address and port will be used to create the UDP datagram socket
	 * which will receive and send UDP datagram packets.
	 * </p>
	 * 
	 * @param address the local address.
	 * @param port the port.
	 * @param ifaceName
	 * @return the UDP client.
	 */
	public synchronized static UDPClient get(IPAddress ipAddress, int port, String ifaceName) {
		if (port == 0) {
			return new UDPClient(ipAddress, port, ifaceName);
		}
		String key = ipAddress.getAddress() + "@" + port + "%" + ifaceName;
		UDPClient udpc = (UDPClient) clients.get(key);
		if (udpc != null) return udpc;
		udpc = new UDPClient(ipAddress, port, ifaceName);
		clients.put(key, udpc);
		return udpc;
	}

	private UDPClient(IPAddress ipAddress, int port, String ifaceName) {
		this.ipAddress = ipAddress;
		this.port = port;
		this.ifaceName = ifaceName;
	}

	/**
	 * Creates a UDP datagram socket and uses this socket to send the given data
	 * as UDP datagram packet.
	 * <p>
	 * The UDP datagram socket is closed immediately after sending the UDP
	 * datagram packet.
	 * </p>
	 * 
	 * @param dstAddress destination address of the UDP datagram packet.
	 * @param dstPort destination port of the UDP datagram packet.
	 * @param data the byte array which contains the data.
	 * @param len the length of bytes inside the byte array which should be
	 *            sent.
	 * @throws IOException
	 */
	public synchronized void send(IPAddress dstAddress, int dstPort, byte[] data, int len) throws IOException {
		if (closed) return;
		DatagramSocket socket = DatagramSocketFactory.getInstance().createDatagramSocket(ipAddress, port, ifaceName);
		send(socket, dstAddress, dstPort, data, len);
		socket.close();
	}

	/**
	 * Creates a UDP datagram socket and uses this socket to send the given data
	 * as UDP datagram packet.
	 * <p>
	 * A listener is started for the created UDP datagram socket. This listener
	 * will exist for this given time and handle incoming UDP messages for the
	 * created UDP datagram socket.
	 * </p>
	 * <p>
	 * An incoming UDP message will be forwarded to the given
	 * {@link UDPDatagramHandler} which can handle the UDP datagram packet.
	 * </p>
	 * <p>
	 * The listener should be closed with the {@link #close()} method.
	 * </p>
	 * 
	 * @param dstAddress destination address of the UDP datagram packet.
	 * @param dstPort destination port of the UDP datagram packet.
	 * @param data the byte array which contains the data.
	 * @param len the length of bytes inside the byte array which should be
	 *            sent.
	 * @param handler this handler will handle the incoming UDP datagram
	 *            packets.
	 * @throws IOException
	 */
	public synchronized void send(IPAddress dstAddress, int dstPort, byte[] data, int len, UDPDatagramHandler handler, ProtocolData protocolData) throws IOException {
		if (closed) return;
		// 2011-11-12 SSch changed to allow the sending with UDPClient that does
		// not care about incoming data
		if (handler == null) {
			send(dstAddress, dstPort, data, len);
			return;
		}

		if (listener == null) {
			listener = new UDPListener(ipAddress, port, ifaceName, handler);
			if (port == 0) {
				port = listener.getPort();
				String key = ipAddress.getAddress() + "@" + port + "%" + ifaceName;
				synchronized (this.getClass()) {
					clients.put(key, this);
				}
			}
			timeout = new ListenerTimeout(listener);
			WatchDog.getInstance().register(timeout, UDP_RECEIVER_TIMEOUT);
			listener.start();
		}
		DatagramSocket socket = listener.getDatagramSocket();
		// TODO replace with more generic way to set actual source port
		if (protocolData instanceof DPWSProtocolData) {
			((DPWSProtocolData) protocolData).setSourcePort(socket.getSocketPort());
		}

		send(socket, dstAddress, dstPort, data, len);
	}

	/**
	 * Closes an existing UDP listener.
	 * <p>
	 * A listener is started if the method
	 * {@link #send(String, int, byte[], int, UDPDatagramHandler)} is used.
	 * </p>
	 * 
	 * @throws IOException
	 */
	public synchronized void close() throws IOException {
		if (closed) return;
		if (listener != null) {
			WatchDog.getInstance().unregister(timeout);
			listener.stop();
			listener = null;
		}
		closed = true;
		clients.remove(this);
	}

	/**
	 * Returns <code>true</code> if the client is closed and cannot be used for
	 * a request, or <code>false</code> if the client can still be used.
	 * 
	 * @return <code>true</code> if the client is closed and cannot be used for
	 *         a request, or <code>false</code> if the client can still be used.
	 */
	public synchronized boolean isClosed() {
		return closed;
	}

	/**
	 * @return the ifaceName
	 */
	public String getIfaceName() {
		return ifaceName;
	}

	public IPAddress getIPAddress() {
		return ipAddress;
	}

	public int getPort() {
		return port;
	}

	private void send(DatagramSocket socket, IPAddress dstAddress, int dstPort, byte[] data, int len) throws IOException {
		UDPServer.send(socket, dstAddress, dstPort, data, len);
	}

	/**
	 * This timeout object is used for registration with the {@link WatchDog}.
	 * This allows to shutdown the {@link UDPListener} after a given time.
	 */
	private class ListenerTimeout extends TimedEntry {

		private UDPListener	listener	= null;

		ListenerTimeout(UDPListener listener) {
			this.listener = listener;
		}

		protected void timedOut() {
			synchronized (UDPClient.this) {
				if (listener != null) {
					try {
						listener.stop();
					} catch (IOException e) {
						Log.warn("Could not stop UDP listener from UDP client.");
					}
				}
				closed = true;
			}
		}

	}

}
