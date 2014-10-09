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
import org.ws4d.java.communication.connection.ip.NetworkInterface;

/**
 * 
 */
public class DPWSDomain implements ProtocolDomain {

	/**
	 * This array always contains exactly two items: a network interface name at
	 * its index <code>0</code> and an IP address at its index <code>1</code>.
	 */
	private final String[]			ids	= new String[2];

	private final NetworkInterface	iface;

	private final IPAddress			ipAddress;

	private final boolean			supportsMulticast;

	private final boolean			isUp;

	private static boolean arraysEqual(Object[] ar1, Object[] ar2) {
		if (ar1 == ar2) {
			return true;
		}
		if (ar1 == null) {
			return ar2 == null;
		}
		if (ar2 == null) {
			return false;
		}
		int len1 = ar1.length;
		int len2 = ar2.length;
		if (len1 != len2) {
			return false;
		}
		for (int i = 0; i < len1; i++) {
			if ((ar1[i] != null && !ar1[i].equals(ar2[i])) || (ar1[i] == null && ar2[i] != null)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Returns a hash code value for the array
	 * 
	 * @param array the array for which to create a hash code value
	 * @return a hash code value for the array
	 */
	private static int hashCode(Object[] array) {
		int prime = 31;
		if (array == null) return 0;
		int result = 1;
		for (int index = 0; index < array.length; index++) {
			result = prime * result + (array[index] == null ? 0 : array[index].hashCode());
		}
		return result;
	}

	/**
	 * @param iface
	 * @param address
	 * @param supportsMulticast
	 */
	public DPWSDomain(NetworkInterface iface, IPAddress ipAddress, boolean supportsMulticast, boolean isUp) {
		super();
		this.ids[0] = iface.getName();
		this.ids[1] = ipAddress.getAddressWithoutNicId();
		this.iface = iface;
		this.ipAddress = ipAddress;
		this.supportsMulticast = supportsMulticast;
		this.isUp = isUp;
	}

	/**
	 * @param iface
	 * @param address
	 * @param supportsMulticast
	 */
	public DPWSDomain(NetworkInterface iface, IPAddress ipAddress) {
		this(iface, ipAddress, true, true);
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return getHostAddress() + " - " + getInterfaceName();
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + DPWSDomain.hashCode(ids);
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
		DPWSDomain other = (DPWSDomain) obj;
		if (!arraysEqual(ids, other.ids)) return false;
		return true;
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.communication.ProtocolDomain#getDomainIds()
	 */
	public String[] getDomainIds() {
		return ids;
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.communication.ProtocolDomain#getProtocolId()
	 */
	public String getCommunicationManagerId() {
		return DPWSCommunicationManager.COMMUNICATION_MANAGER_ID;
	}

	/**
	 * @return the interfaceName
	 */
	public String getInterfaceName() {
		return ids[0];
	}

	/**
	 * @return the host address
	 */
	public String getHostAddress() {
		return ids[1];
	}

	/**
	 * @return the network interface
	 */
	public NetworkInterface getIface() {
		return iface;
	}

	/**
	 * @return the IP address
	 */
	public IPAddress getIPAddress() {
		return ipAddress;
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
		return isUp;
	}

}
