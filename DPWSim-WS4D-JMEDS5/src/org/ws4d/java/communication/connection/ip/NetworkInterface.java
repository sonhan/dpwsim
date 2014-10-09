/*******************************************************************************
 * Copyright (c) 2009 MATERNA Information & Communications. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html. For further
 * project-related information visit http://www.ws4d.org. The most recent
 * version of the JMEDS framework can be obtained from
 * http://sourceforge.net/projects/ws4d-javame.
 ******************************************************************************/
package org.ws4d.java.communication.connection.ip;

import org.ws4d.java.structures.DataStructure;
import org.ws4d.java.structures.Iterator;
import org.ws4d.java.structures.LinkedList;

/**
 * This class represents the physical network interface.
 */
public class NetworkInterface {

	private DataStructure	addresses	= null;

	private String			name		= null;

	private final boolean	supportsMulticast;

	private final boolean	isUP;

	private final boolean	isLoopback;

	/**
	 * @param name the name.
	 */
	public NetworkInterface(String name, boolean supportsMulticast, boolean isUp, boolean isLoopback) {
		this.name = name;
		this.supportsMulticast = supportsMulticast;
		this.isUP = isUp;
		this.isLoopback = isLoopback;
	}

	/**
	 * Returns the name of this network interface.
	 * 
	 * @return the name.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Adds an network address to this network interface.
	 * 
	 * @param ip network address to add.
	 */
	public void addAddress(IPAddress address) {
		if (addresses == null) {
			addresses = new LinkedList();
		}
		addresses.add(address);
	}

	/**
	 * Returns an array with all network addresses for this network interface.
	 * 
	 * @return array with network addresses.
	 */
	public Iterator getAddresses() {
		if (addresses == null || addresses.size() == 0) {
			return null;
		}
		return addresses.iterator();
	}

	/**
	 * @return the supportsMulticast
	 */
	public boolean supportsMulticast() {
		return supportsMulticast;
	}

	/**
	 * @return whether the interface is up and running
	 */
	public boolean isUp() {
		return this.isUP;
	}

	/**
	 * @return the isLoopback
	 */
	public boolean isLoopback() {
		return isLoopback;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		return name.hashCode();
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		StringBuffer sb = new StringBuffer(getName());
		sb.append(" < ");
		Iterator addrs = addresses.iterator();
		while (addrs.hasNext()) {
			String adr = addrs.next().toString();
			sb.append(adr);
			if (addrs.hasNext()) {
				sb.append(", ");
			}
		}
		sb.append(" >");
		return sb.toString();
	}
}
