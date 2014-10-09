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
import org.ws4d.java.security.DPWSSecurityManager;
import org.ws4d.java.security.SecurityManager;
import org.ws4d.java.util.Log;

public class TCPClient {

	private Socket			socket		= null;

	private TCPConnection	connection	= null;

	public static TCPClient connect(IPAddress address, int port) throws IOException {
		return connect(address, port, false, null);
	}

	public static TCPClient connect(IPAddress address, int port, boolean secure, String alias) throws IOException {
		if (address == null) {
			throw new IOException("Cannot connect. No IP address given.");
		}
		if (port < 1 || port > 65535) {
			throw new IOException("Cannot connect. Port number invalid.");
		}
		TCPClient client = new TCPClient(address, port, secure, alias);
		return client;
	}

	private TCPClient(IPAddress address, int port, boolean secure, String alias) throws IOException {
		if (DPWSFramework.hasModule(DPWSFramework.SECURITY_MODULE)) {
			SecurityManager secMan = DPWSFramework.getSecurityManager();
			socket = (secure && secMan != null && secMan instanceof DPWSSecurityManager) ? ((DPWSSecurityManager) secMan).getSecureSocket(address, port, alias) : SocketFactory.getInstance().createSocket(address, port);
		} else {
			socket = SocketFactory.getInstance().createSocket(address, port);
		}

		InputStream in;
		OutputStream out;

		DPWSProtocolData data = new DPWSProtocolData(null, ProtocolData.DIRECTION_OUT, socket.getLocalAddress().getAddressWithoutNicId(), socket.getLocalPort(), socket.getRemoteAddress().getAddressWithoutNicId(), socket.getRemotePort(), true);

		if (DPWSFramework.getMonitorStreamFactory() != null) {
			in = new MonitoredInputStream(socket.getInputStream(), data.createSwappedProtocolData());
			out = new MonitoredOutputStream(socket.getOutputStream(), data);
		} else {
			in = socket.getInputStream();
			out = socket.getOutputStream();
		}

		connection = new TCPConnection(in, out, socket, data);

		if (Log.isDebug()) {
			Log.debug("<O-TCP> To " + socket.getLocalAddress() + "@" + socket.getLocalPort() + " from " + socket.getRemoteAddress() + "@" + socket.getRemotePort() + ", " + connection, Log.DEBUG_LAYER_COMMUNICATION);
		}
	}

	public TCPConnection getConnection() {
		return connection;
	}
}
