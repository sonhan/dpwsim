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

import org.ws4d.java.DPWSFramework;
import org.ws4d.java.communication.DPWSDiscoveryBinding;
import org.ws4d.java.communication.DPWSProtocolData;
import org.ws4d.java.communication.ProtocolData;
import org.ws4d.java.communication.connection.ip.IPAddress;
import org.ws4d.java.configuration.IPProperties;
import org.ws4d.java.constants.DPWSConstants;
import org.ws4d.java.util.Log;

/**
 * UDP listener which allows to listen for incoming UDP packets.
 * <p>
 * Each incoming packet will be handled in a separate thread.
 * </p>
 * <h2>DPWS Multicast</h2>
 * <p>
 * This listener checks for the DPWS multicast address (239.255.255.250) and
 * port (3702). If the address and port which should be used match the DPWS
 * multicast values, this listener registers itself for multicast datagram
 * packets.
 * </p>
 */
public class UDPListener implements Runnable {

	/** Number of attempts to open a server connection before giving up. */
	private static final int	ACCEPT_RETRIES		= 3;

	/** Time in ms before we try accepting a connection with errors again. */
	private static final int	ACCEPT_RETRY_DELAY	= 1000;

	private IPAddress			ipAddress			= null;

	private int					port				= -1;

	private Object				lockObj				= new Object();

	private volatile boolean	running				= false;

	private DatagramSocket		datagramSocket		= null;

	private UDPDatagramHandler	handler				= null;

	private String				iface				= null;

	/**
	 * Creates a UDP listener for the given address and port.
	 * <p>
	 * This will open a server socket for the given address and port and will
	 * pass a {@link Datagram} to the given {@link UDPDatagramHandler}
	 * </p>
	 * <h2>DPWS Multicast</h2>
	 * <p>
	 * This listener checks for the DPWS multicast address (239.255.255.250) and
	 * port (3702). If the address and port which should be used match the DPWS
	 * multicast values, this listener registers itself for multicast datagram
	 * packets.
	 * </p>
	 * 
	 * @param address the address to listen.
	 * @param port the port.
	 * @param ifaceName
	 * @param handler the handler which will handle the TCP connection.
	 * @throws IOException
	 */
	// Added SSch
	UDPListener(IPAddress ipAddress, int port, String ifaceName, UDPDatagramHandler handler) throws IOException {
		this(ipAddress, port, ifaceName, handler, false);
	}

	UDPListener(IPAddress ipAddress, int port, String ifaceName, UDPDatagramHandler handler, boolean isMulticast) throws IOException {
		if (handler == null) {
			throw new IOException("Cannot listen for incoming data. No handler set for connection handling.");
		}
		if (ipAddress == null) {
			throw new IOException("Cannot listen for incoming data. No IP address given.");
		}
		if (port < 0 || port > 65535) {
			throw new IOException("Cannot listen for incoming data. Port number invalid.");
		}
		this.handler = handler;
		this.ipAddress = ipAddress;
		this.iface = ifaceName;

		// Changed SSch Now the UDPListener could be used not only for WS-D but
		// also for generic Multicast
		isMulticast = isMulticast || ((ipAddress == DPWSDiscoveryBinding.DPWS_MCAST_GROUP_IPv4 || ipAddress == DPWSDiscoveryBinding.DPWS_MCAST_GROUP_IPv6) && port == DPWSConstants.DPWS_MCAST_PORT);
		if (isMulticast) {
			this.datagramSocket = DatagramSocketFactory.getInstance().registerMulticastGroup(ipAddress, port, ifaceName);
		} else {
			this.datagramSocket = DatagramSocketFactory.getInstance().createDatagramServerSocket(ipAddress, port, ifaceName);
		}
		this.port = datagramSocket.getSocketPort();

	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		if (Log.isDebug()) {
			Log.debug("UDP listener up for " + ipAddress + "(" + iface + ") and port " + port + ".", Log.DEBUG_LAYER_COMMUNICATION);
		}
		int retryCount = 0;
		
		synchronized (lockObj) {
			running = true;
			lockObj.notifyAll();
		}
		
		while (isRunning()) {
			try {
				/*
				 * Wait for incoming connection.
				 */
				Datagram datagram = datagramSocket.receive();
				if (!isRunning()) {
					break;
				}
				if (datagram == null) {
					Log.warn("Incoming UDP datagram was empty. Re-listening for new connections.");
					continue;
				}

				if (!IPProperties.getInstance().isAllowedByIPFilter(datagram.getIPAddress())) {
					continue;
				}

				/*
				 * Create and handle the incoming UDP connection.
				 */
				DPWSFramework.getThreadPool().execute(new UDPDatagramThread(datagram, handler));
			} catch (Exception e) {
				if (isRunning()) {
					if (retryCount++ < ACCEPT_RETRIES) {
						try {
							Thread.sleep(ACCEPT_RETRY_DELAY);
						} catch (InterruptedException ie) {
							Log.warn("UDP listener interrupted. UDP listener shutdown for " + ipAddress + " and port " + port + ".");
							break;
						}
						Log.warn("Can not open port " + port + " for " + ipAddress + ". Try " + retryCount + ".");
						continue;
					}
					Log.error("Can not open port " + port + " for " + ipAddress + ". UDP listener shutdown for " + ipAddress + " and port " + port + ".");
					break;
				} else {
					break;
				}
			}
		}

	}

	/**
	 * Returns the datagram socket which is used for incoming datagram packets.
	 * 
	 * @return the datagram socket.
	 */
	public synchronized DatagramSocket getDatagramSocket() {
		return datagramSocket;
	}

	/**
	 * Returns the datagram handler for this listener.
	 * 
	 * @return the datagram handler.
	 */
	public synchronized UDPDatagramHandler getUDPDatagramHandler() {
		return handler;
	}

	/**
	 * Indicates whether this listener is running or not.
	 * 
	 * @return <code>true</code> if the listener is running and will handle
	 *         incoming TCP connections, <code>false</code> otherwise.
	 */
	public synchronized boolean isRunning() {
		return running;
	}

	/**
	 * Starts the UDP listener.
	 * 
	 * @return <code>true</code> if the listener is started or already running,
	 *         <code>false</code> otherwise.
	 */
	public synchronized boolean start() {
		if (running) return true;

		/*
		 * Gets lock, and waits until the UDP listener is ready. This is
		 * necessary because we do not know, whether the thread pool starts this
		 * thread straight away or not.
		 */
		synchronized (lockObj) {
			try {
				if (DPWSFramework.getThreadPool().executeOrAbort(this)) {
					while (!running) {
						lockObj.wait();
					}
					return true;
				}
				else {
					return false;
				}
			} catch (InterruptedException e) {
				return false;
			}
		}
	}

	/**
	 * Stops the UDP listener.
	 */
	public synchronized void stop() throws IOException {
		if (!running) return;
		datagramSocket.close();
		if (Log.isDebug()) {
			Log.debug("UDP listener shutdown for " + ipAddress + "(" + iface + ") and port " + port + ".", Log.DEBUG_LAYER_COMMUNICATION);
		}
		running = false;
	}

	/**
	 * @return the port
	 */
	public int getPort() {
		return port;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((ipAddress == null) ? 0 : ipAddress.hashCode());
		result = prime * result + port;
		return result;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		UDPListener other = (UDPListener) obj;
		if (ipAddress == null) {
			if (other.ipAddress != null) return false;
		} else if (!ipAddress.equals(other.ipAddress)) return false;
		if (port != other.port) return false;
		return true;
	}

	/**
	 * This thread allows the handling of each incoming datagram.
	 */
	private class UDPDatagramThread implements Runnable {

		private Datagram			datagram		= null;
		
		private UDPDatagramHandler	handler			= null;

		UDPDatagramThread(Datagram datagram, UDPDatagramHandler handler) {
			this.datagram = datagram;
			this.handler = handler;
		}

		public void run() {
			try {
				if (Log.isDebug()) {
					Log.debug("<I-UDP> From " + datagram.getIPAddress() + "@" + datagram.getPort() + " to " + datagram.getSocketAddress() + "@" + datagram.getSocketPort() + ", " + datagram, Log.DEBUG_LAYER_COMMUNICATION);
				}
				handler.handle(datagram, new DPWSProtocolData(iface, ProtocolData.DIRECTION_IN, datagram.getIPAddress().getAddressWithoutNicId(), datagram.getPort(), ipAddress.getAddressWithoutNicId(), port, false));
			} catch (IOException e) {
				Log.warn("Incoming UDP datagram (" + datagram.getIdentifier() + ") could not be handled. " + e.getMessage() + ".");
			}
		}
	}

}
