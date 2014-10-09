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
import java.net.DatagramPacket;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.net.UnknownHostException;

import org.ws4d.java.communication.connection.ip.IPAddress;
import org.ws4d.java.configuration.FrameworkProperties;
import org.ws4d.java.util.Log;
import org.ws4d.java.util.ObjectPool;
import org.ws4d.java.util.ObjectPool.InstanceCreator;

/**
 * DatagramSocket wrapper for SE.
 */
public class SEDatagramSocket implements DatagramSocket {

	private static final ObjectPool		BUFFERS			= new ObjectPool(new InstanceCreator() {

															public Object createInstance() {
																return new byte[FrameworkProperties.getInstance().getMaxDatagramSize()];
															}

														}, 4);

	private java.net.MulticastSocket	socket			= null;

	private String						ifaceName		= "";

	private int							conCount		= 1;

	private IPAddress					socketAddress	= null;

	private int							port			= -1;

	private boolean						isMulticast		= false;

	public SEDatagramSocket(IPAddress socketAddress, int port, String ifaceName) throws IOException {

		InetAddress inetAddress = InetAddress.getByName(socketAddress.getAddress());
		NetworkInterface iface = NetworkInterface.getByName(ifaceName);
		MulticastSocket msocket = null;

		if (inetAddress.isMulticastAddress()) {
			isMulticast = true;
			try {
				msocket = new MulticastSocket(port);
				msocket.setNetworkInterface(iface);
				msocket.joinGroup(inetAddress);
			} catch (IOException e) {
				Log.warn("Can not join multicast group (" + socketAddress + "@" + port + ") at interface " + ifaceName + ". No receiving of UDP packets on this interface.");
				throw e;
			}
		} else {
			try {
				// Added by Stefan Schlichting
				if (port >= 0 && port <= 0xFFFF) {
					msocket = new MulticastSocket(port);
				} else {
					msocket = new MulticastSocket();
				}
				msocket.setNetworkInterface(iface);
			} catch (IOException e) {
				Log.warn("Can not set NetworkInterface (" + socketAddress + "@" + port + ") at interface " + ifaceName);
				throw e;
			}
		}
		socket = msocket;
		this.socketAddress = socketAddress;
		this.port = msocket.getLocalPort();
		this.ifaceName = ifaceName;
	}

	public void test(String host, int port) throws UnknownHostException {
		socket.connect(InetAddress.getByName(host), port);
	}

	/**
	 * Increments the count of connections on the socket.
	 */
	public void incrementConCount() {
		this.conCount++;
	}

	public void close() throws IOException {
		if (isMulticast) {
			socket.close();
			if (Log.isDebug()) {
				Log.debug("UDP multicast socket closed for interface: " + ifaceName + ".");
			}
		} else {
			socket.close();
			if (Log.isDebug()) {
				Log.debug("UDP socket closed for interface: " + ifaceName + " - " + socketAddress);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.communication.connection.udp.DatagramSocket#receive()
	 */
	public Datagram receive() throws IOException {
		// get pooled buffer
		byte[] buffer = new byte[FrameworkProperties.getInstance().getMaxDatagramSize()]; // (byte[])
																							// BUFFERS.acquire();
		DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
		socket.receive(packet);
		Datagram dgram = new Datagram(this, buffer, packet.getLength());
		dgram.setSocketAddress(this.socketAddress);
		dgram.setSocketPort(this.port);
		InetAddress ina = packet.getAddress();
		dgram.setAddress(new IPAddress(ina.getHostAddress(), ina.isLoopbackAddress(), (ina instanceof Inet6Address), ina.isLinkLocalAddress()));
		dgram.setPort(packet.getPort());

		return dgram;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.ws4d.java.communication.connection.udp.DatagramSocket#send(org.ws4d
	 * .java.communication.connection.udp.Datagram)
	 */
	public void send(Datagram datagram) throws IOException {
		byte[] data = datagram.getData();

		InetAddress address = InetAddress.getByName(datagram.getIPAddress().getAddressWithoutNicId());
		DatagramPacket packet = new DatagramPacket(data, datagram.getContentLength(), address, datagram.getPort());
		socket.send(packet);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.ws4d.java.communication.connection.udp.DatagramSocket#release(org
	 * .ws4d.java.communication.connection.udp.Datagram)
	 */
	public void release(Datagram datagram) {
		// return pooled buffer
		// BUFFERS.release(datagram.getData());
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return "interface: " + ifaceName + ", port: " + port;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.ws4d.java.communication.connection.udp.DatagramSocket#getSocketAddress
	 * ()
	 */
	public IPAddress getSocketAddress() {
		return socketAddress;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.ws4d.java.communication.connection.udp.DatagramSocket#getSocketPort()
	 */
	public int getSocketPort() {
		return port;
	}
}
