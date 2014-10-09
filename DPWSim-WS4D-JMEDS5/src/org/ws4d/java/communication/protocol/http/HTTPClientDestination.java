/*******************************************************************************
 * Copyright (c) 2009 MATERNA Information & Communications. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html. For further
 * project-related information visit http://www.ws4d.org. The most recent
 * version of the JMEDS framework can be obtained from
 * http://sourceforge.net/projects/ws4d-javame.
 ******************************************************************************/
package org.ws4d.java.communication.protocol.http;

import org.ws4d.java.communication.connection.ip.IPAddress;
import org.ws4d.java.structures.HashMap;
import org.ws4d.java.types.URI;

public class HTTPClientDestination {

	/**
	 * The host address.
	 */
	private IPAddress				host	= null;

	/**
	 * The host port.
	 */
	private int						port	= 80;

	private boolean					secure	= false;

	private String					alias	= null;

	private static final HashMap	maxCon	= new HashMap();

	private int						hashCode;

	public HTTPClientDestination(String request) {
		this(new URI(request), false, null);
	}

	public HTTPClientDestination(String request, boolean secured, String alias) {
		this(new URI(request), secured, alias);
	}

	public HTTPClientDestination(URI request) {
		this(request, false, null);
	}

	public HTTPClientDestination(URI request, boolean secured, String alias) {
		this(new IPAddress(request.getHost()), request.getPort(), secured, alias);
	}

	public HTTPClientDestination(IPAddress host, int port) {
		this(host, port, false, null);
	}

	public HTTPClientDestination(IPAddress host, int port, boolean secured, String alias) {
		this.host = host;
		this.port = port;
		this.secure = secured;
		this.alias = alias;

		final int prime = 31;
		hashCode = 1;
		hashCode = prime * hashCode + ((alias == null) ? 0 : alias.hashCode());
		hashCode = prime * hashCode + ((host == null) ? 0 : host.hashCode());
		hashCode = prime * hashCode + port;
		hashCode = prime * hashCode + (secure ? 1231 : 1237);

		setMaxConnections(HTTPClient.MAX_CLIENT_CONNECTIONS);
	}

	public void setMaxConnections(int maxConnections) {
		maxCon.put(host, new Integer(maxConnections));
	}

	public int getMaxConnections() {
		return ((Integer) maxCon.get(host)).intValue();
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
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		HTTPClientDestination other = (HTTPClientDestination) obj;
		if (alias == null) {
			if (other.alias != null) return false;
		} else if (!alias.equals(other.alias)) return false;
		if (host == null) {
			if (other.host != null) return false;
		} else if (!host.equals(other.host)) return false;
		if (port != other.port) return false;
		if (secure != other.secure) return false;
		return true;
	}

	public IPAddress getHost() {
		return host;
	}

	public int getPort() {
		return port;
	}

	public boolean isSecure() {
		return secure;
	}

	public String getAlias() {
		return alias;
	}

}
