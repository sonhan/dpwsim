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
import org.ws4d.java.types.URI;
import org.ws4d.java.util.WS4DIllegalStateException;

/**
 * Abstract class to represent the communication binding for an IP based
 * transport protocol such as TCP and UDP.
 * <p>
 * The IP binding contains a host hostAddress (as a String, so that it could be
 * either an IPv4 or IPv6 hostAddress literal or a DNS host name) and a port.
 * </p>
 */
public abstract class IPBinding implements CommunicationBinding {

	public static final int	HTTP_BINDING		= 0;

	public static final int	HTTPS_BINDING		= 1;

	public static final int	DISCOVERY_BINDING	= 2;

	protected IPAddress		ipAddress			= null;

	protected int			port				= -1;

	protected URI			transportAddress	= null;

	protected IPBinding(IPAddress ipAddress, int port) {
		if (ipAddress == null) {
			throw new WS4DIllegalStateException("Cannot create IP binding without IP host address");
		}
		if (port < 0 || port > 65535) {
			throw new WS4DIllegalStateException("Cannot create IP binding with illegal port number");
		}

		this.ipAddress = ipAddress;
		this.port = port;
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

	/**
	 * Returns the type of this binding.
	 * 
	 * @return HTTPBinding: 0, HTTPSBinding: 1, DiscoveryBinding: 2.
	 */
	public abstract int getType();

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ipAddress.hashCode();
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
		final IPBinding other = (IPBinding) obj;
		if (!ipAddress.equals(other.ipAddress)) return false;
		if (port != other.port) return false;
		return true;
	}

	public String toString() {
		return this.ipAddress + ":" + this.port;
	}

}
