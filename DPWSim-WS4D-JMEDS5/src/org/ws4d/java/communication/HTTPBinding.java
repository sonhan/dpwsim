/*******************************************************************************
 * Copyright (c) 2009 MATERNA Information & Communications. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html. For further
 * project-related information visit http://www.ws4d.org. The most recent
 * version of the JMEDS framework can be obtained from
 * http://sourceforge.net/projects/ws4d-javame.
 ******************************************************************************/
package org.ws4d.java.communication;

import org.ws4d.java.communication.connection.ip.IPAddress;
import org.ws4d.java.communication.connection.ip.IPNetworkDetection;
import org.ws4d.java.constants.HTTPConstants;
import org.ws4d.java.types.URI;
import org.ws4d.java.util.IDGenerator;
import org.ws4d.java.util.WS4DIllegalStateException;

/**
 * HTTP binding to allows access to DPWS devices and services. <br>
 * This HTTP binding allows the creation of an HTTP address for a device or a
 * service.
 * <p>
 * <code>
 * HTTPBinding addr = new HTTPBinding(&quot;192.168.0.1&quot;, 8080, &quot;/device&quot;);
 * </code>
 * </p>
 * The HTTP binding above will create the address http://192.168.0.1:8080/device
 * and can be used for devices.
 */
public class HTTPBinding implements CommunicationBinding {

	private final String	path;

	private final int		hashCode;

	private final boolean	autoPort;

	public static final int	HTTP_BINDING		= 0;

	public static final int	HTTPS_BINDING		= 1;

	public static final int	DISCOVERY_BINDING	= 2;

	protected IPAddress		ipAddress			= null;

	protected int			port				= -1;

	protected URI			transportAddress	= null;

	/**
	 * Constructor.
	 * 
	 * @param ipAddress
	 * @param port
	 * @param path
	 */
	public HTTPBinding(IPAddress ipAddress, int port, String path) {
		if (ipAddress == null) {
			throw new WS4DIllegalStateException("Cannot create IP binding without IP host address");
		}
		if (port < 0 || port > 65535) {
			throw new WS4DIllegalStateException("Cannot create IP binding with illegal port number");
		}

		this.ipAddress = ipAddress;
		this.port = port;
		autoPort = port == 0;
		if (path == null) {
			path = "/" + IDGenerator.getUUID();
		} else if (!path.startsWith("/")) {
			path = "/" + path;
		}
		int prime = 31;
		int result = 1;
		result = prime * result + ipAddress.hashCode();
		result = prime * result + path.hashCode();
		this.path = path;
		hashCode = result;
	}

	/**
	 * @deprecated <BR>
	 *             Use HTTPBinding(IPAddress ipAddress, int port, String path)
	 * @param address
	 * @param port
	 * @param path
	 */
	public HTTPBinding(String address, int port, String path) {
		this(IPNetworkDetection.getInstance().getIPAddress(address), port, path);
	}

	/**
	 * Returns the path of the HTTP address.
	 * 
	 * @return the path of the HTTP address.
	 */
	public String getPath() {
		return path;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		return hashCode;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!super.equals(obj)) return false;
		if (getClass() != obj.getClass()) return false;
		final HTTPBinding other = (HTTPBinding) obj;
		if (!getTransportAddress().equals(other.getTransportAddress()) || getType() != other.getType()) return false;
		return true;
	}

	/**
	 * @param port the port to set
	 */
	public void setPort(int port) {
		if (this.port == 0) {
			this.port = port;
			transportAddress = null;
		} else if (this.port != port) {
			throw new RuntimeException("Attempt to overwrite non-zero port.");
		}
	}

	public void resetAutoPort() {
		if (autoPort) {
			port = 0;
			transportAddress = null;
		}
	}

	public int getType() {
		return HTTP_BINDING;
	}

	public URI getTransportAddress() {
		if (transportAddress == null) {
			transportAddress = new URI(getURISchema() + "://" + ipAddress.getAddressWithoutNicId() + ":" + port + path);
		}
		return transportAddress;
	}

	public String getURISchema() {
		return HTTPConstants.HTTP_SCHEMA;
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.communication.CommunicationBinding#getProtocolId()
	 */
	public String getCommunicationManagerId() {
		return DPWSCommunicationManager.COMMUNICATION_MANAGER_ID;
	}

	/**
	 * Returns the host address of this binding. The host address can be either
	 * an IPv4 literal, an IPv6 literal or a DNS host name.
	 * 
	 * @return the host address of this binding.
	 */
	public IPAddress getHostAddress() {
		return ipAddress;
	}

	/**
	 * Returns the TCP/UDP port for this IP-based binding.
	 * 
	 * @return the TCP/UDP port for this IP-based binding.
	 */
	public int getPort() {
		return port;
	}

	public String toString() {
		return this.ipAddress + ":" + this.port;
	}
}
