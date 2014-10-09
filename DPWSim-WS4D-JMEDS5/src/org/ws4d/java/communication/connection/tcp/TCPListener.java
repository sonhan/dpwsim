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

import org.ws4d.java.DPWSFramework;
import org.ws4d.java.communication.DPWSProtocolData;
import org.ws4d.java.communication.ProtocolData;
import org.ws4d.java.communication.connection.ip.IPAddress;
import org.ws4d.java.communication.monitor.MonitoredInputStream;
import org.ws4d.java.communication.monitor.MonitoredOutputStream;
import org.ws4d.java.configuration.IPProperties;
import org.ws4d.java.io.buffered.BufferedInputStream;
import org.ws4d.java.security.DPWSSecurityManager;
import org.ws4d.java.security.SecurityManager;
import org.ws4d.java.structures.Iterator;
import org.ws4d.java.structures.LinkedList;
import org.ws4d.java.structures.List;
import org.ws4d.java.util.Log;

/**
 * TCP listener which allows listening for incoming TCP connections.
 * <p>
 * Each incoming connection will be handled in a separate thread.
 * </p>
 */
public class TCPListener implements Runnable {

	/** Number of attempts to open a server connection before giving up. */
	private static final int		ACCEPT_RETRIES		= 3;

	/** Time in ms before we retry to accept a connection with errors. */
	private static final int		ACCEPT_RETRY_DELAY	= 1000;

	private static final boolean	BUFFERED_INPUT		= true;
	
	private IPAddress				ipAddress			= null;

	private int						port				= -1;

	private Object					lockObj				= new Object();

	private volatile boolean		running				= false;

	private ServerSocket			serverSocket		= null;

	private TCPConnectionHandler	handler				= null;

	private List					connections			= new LinkedList();

	private boolean					secure				= false;

	/**
	 * Creates a TCP listener for the given address and port.
	 * <p>
	 * This will open a server socket for the given address and port and will
	 * pass a {@link TCPConnection} to the given {@link TCPConnectionHandler}
	 * </p>
	 * 
	 * @param address the address to which to listen.
	 * @param port the port.
	 * @param handler the handler which will handle the TCP connection.
	 * @throws IOException
	 */
	TCPListener(IPAddress ipAddress, int port, TCPConnectionHandler handler) throws IOException {
		this(ipAddress, port, handler, false, null);
	}

	TCPListener(IPAddress ipAddress, int port, TCPConnectionHandler handler, boolean secure, String alias) throws IOException {
		if (secure && !DPWSFramework.hasModule(DPWSFramework.SECURITY_MODULE)) throw new IOException("Cannot create SSL Socket. DPWS security module missing.");

		if (handler == null) throw new IOException("Cannot listen for incoming data. No handler set for connection handling.");

		if (ipAddress == null) throw new IOException("Cannot listen for incoming data. No IP address given.");

		if (port < 0 || port > 65535) throw new IOException("Cannot listen for incoming data. Port number invalid.");

		this.handler = handler;
		this.ipAddress = ipAddress;
		SecurityManager secMan = DPWSFramework.getSecurityManager();
		this.serverSocket = (secure && secMan != null && secMan instanceof DPWSSecurityManager) ? ((DPWSSecurityManager) secMan).getSecureServerSocket(ipAddress, port, alias) : SocketFactory.getInstance().createServerSocket(ipAddress, port);
		this.port = serverSocket.getPort();
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		if (Log.isDebug()) {
			Log.debug("TCP listener up for " + ipAddress + " and port " + port + ".", Log.DEBUG_LAYER_COMMUNICATION);
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
				Socket socket = serverSocket.accept();

				if (!isRunning()) {
					break;
				}
				if (socket == null) {
					Log.warn("Incoming TCP connection has returned no socket. Re-listening for new connections.");
					continue;
				}

				if (!IPProperties.getInstance().isAllowedByIPFilter(socket.getRemoteAddress())) {
					continue;
				}

				/*
				 * Get the streams.
				 */
				InputStream in = socket.getInputStream();
				OutputStream out = socket.getOutputStream();

				if (in == null) {
					Log.warn("Incoming TCP connection has no input stream. Cannot handle connection. Re-listening for new connections.");
					continue;
				}

				if (out == null) {
					Log.warn("Incoming TCP connection has no output stream. Cannot handle connection. Re-listening for new connections.");
					continue;
				}

				DPWSProtocolData data = null;

				if (socket.getRemoteAddress() == null) {
					/*
					 * TODO: CLDC quick fix! It's not possible to retrieve the
					 * remote address from the CLDC socket. :-(
					 */
					data = new DPWSProtocolData(null, ProtocolData.DIRECTION_IN, null, socket.getRemotePort(), socket.getLocalAddress().getAddressWithoutNicId(), socket.getLocalPort(), true);
				} else {
					data = new DPWSProtocolData(null, ProtocolData.DIRECTION_IN, socket.getRemoteAddress().getAddressWithoutNicId(), socket.getRemotePort(), socket.getLocalAddress().getAddressWithoutNicId(), socket.getLocalPort(), true);
				}

				if (BUFFERED_INPUT) {
					in = new BufferedInputStream(in);
				}

				if (DPWSFramework.getMonitorStreamFactory() != null) {
					in = new MonitoredInputStream(in, data);
					out = new MonitoredOutputStream(out, data.createSwappedProtocolData());
				}

				/*
				 * Create incoming TCP connection.
				 */
				TCPConnection connection = new TCPConnection(in, out, socket, data);

				/*
				 * Store connection for the KILL method! ;-)
				 */
				connections.add(connection);

				if (Log.isDebug()) {
					if (socket.getRemoteAddress() != null) {
						Log.debug("<I-TCP> From " + socket.getRemoteAddress() + "@" + socket.getRemotePort() + " to " + socket.getLocalAddress() + "@" + socket.getLocalPort() + ", " + connection, Log.DEBUG_LAYER_COMMUNICATION);
					} else {
						Log.debug("<I-TCP> From unkown host to " + ipAddress + " and port " + port + ", " + connection, Log.DEBUG_LAYER_COMMUNICATION);
					}
				}

				/*
				 * Handle incoming TCP connection in an own thread.
				 */
				DPWSFramework.getThreadPool().execute(new TCPConnectionThread(connection, handler));
			} catch (IOException e) {
				if (isRunning()) {
					if (retryCount++ < ACCEPT_RETRIES) {
						try {
							Thread.sleep(ACCEPT_RETRY_DELAY);
						} catch (InterruptedException ie) {
							Log.warn("TCP listener interrupted. TCP listener shutdown for " + ipAddress + " and port " + port + ".");
							break;
						}
						Log.warn("Cannot open port " + port + " for " + ipAddress + ". Try " + retryCount + ".");
						continue;
					}
					Log.error("Cannot open port " + port + " for " + ipAddress + ". TCP listener shutdown for " + ipAddress + " and port " + port + ".");
					break;
				} else {
					break;
				}
			}
		}
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
	 * Starts the TCP listener.
	 * 
	 * @return <code>true</code> if the listener is started or already running,
	 *         <code>false</code> otherwise.
	 */
	public synchronized boolean start() {
		if (running) return true;

		/*
		 * Get lock, and wait until the TCP listener is ready. This is necessary
		 * because we do not know, whether the thread pool starts this thread
		 * instant or not.
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
	 * Stops the TCP listener.
	 * <p>
	 * Existing TCP connection will remain active! To stop the TCP server and
	 * close all established connections.
	 * </p>
	 */
	public synchronized void stop() throws IOException {
		if (!running) return;
		running = false;
		serverSocket.close();
		if (Log.isDebug()) {
			Log.debug("TCP listener shutdown for " + ipAddress + " and port " + port + ".", Log.DEBUG_LAYER_COMMUNICATION);
		}
	}

	/**
	 * Stops the TCP listener and kills all established connection.
	 * <p>
	 * This will also close all established connections.
	 * </p>
	 */
	public synchronized void kill() throws IOException {
		stop();
		TCPConnection connection = null;
		try {
			Iterator it = connections.iterator();
			while (it.hasNext()) {
				connection = (TCPConnection) it.next();
				connection.close();
				it.remove();
			}
		} catch (IOException e) {
			if (connection != null) {
				Log.error("Cannot close TCP connection (" + connection.getIdentifier() + ").");
			}
		}
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
		TCPListener other = (TCPListener) obj;
		if (ipAddress == null) {
			if (other.ipAddress != null) return false;
		} else if (!ipAddress.equals(other.ipAddress)) return false;
		if (port != other.port) return false;
		return true;
	}

	public void setSecure(boolean secure) {
		this.secure = secure;
	}

	public boolean isSecure() {
		return secure;
	}

	/**
	 * This thread allows the handling of each incoming connection.
	 */
	private class TCPConnectionThread implements Runnable {

		private TCPConnection			connection		= null;

		private TCPConnectionHandler	handler			= null;

		TCPConnectionThread(TCPConnection connection, TCPConnectionHandler handler) {
			this.connection = connection;
			this.handler = handler;
		}

		public void run() {
			try {
				handler.handle(connection);
				if (Log.isDebug()) {
					Log.debug("<I> Incoming TCP connection (" + connection.getIdentifier() + ") handling done.", Log.DEBUG_LAYER_COMMUNICATION);
				}
				connection.close();
			} catch (IOException e) {
				if (!connection.isClosed()) {
					Log.warn("<I> Incoming TCP connection (" + connection.getIdentifier() + "). " + e.getMessage() + ".");
				}
			}
		}
	}
}
