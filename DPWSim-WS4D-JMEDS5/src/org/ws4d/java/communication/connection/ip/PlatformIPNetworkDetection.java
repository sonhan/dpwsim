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

import java.io.IOException;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.util.Enumeration;

import org.ws4d.java.structures.HashMap;
import org.ws4d.java.structures.Iterator;
import org.ws4d.java.structures.LinkedList;
import org.ws4d.java.util.Log;

/**
 * IP address detection for SE.
 */
public class PlatformIPNetworkDetection extends IPNetworkDetection {

	PlatformIPNetworkDetection() {
		super();
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.communication.connection.ip.IPNetworkDetection#
	 * getCanonicalAddress()
	 */
	public String getCanonicalAddress(String address) {
		try {
			return InetAddress.getByName(address).getHostAddress();
		} catch (UnknownHostException e) {
			return null;
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.communication.connection.ip.IPNetworkDetection#
	 * detectInterfaces()
	 */
	void detectInterfaces() throws IOException {
		if (Log.isDebug()) {
			Log.debug("Start interface detection...");
		}
		if (networkinterfaces != null) {
			networkinterfaces.clear();
		} else {
			networkinterfaces = new LinkedList();
		}
		Enumeration nis = NetworkInterface.getNetworkInterfaces();
		while (nis.hasMoreElements()) {
			NetworkInterface niSE = (NetworkInterface) nis.nextElement();
			org.ws4d.java.communication.connection.ip.NetworkInterface ni = new org.ws4d.java.communication.connection.ip.NetworkInterface(niSE.getName(), niSE.supportsMulticast(), niSE.isUp(), niSE.isLoopback());
			Enumeration addrs = niSE.getInetAddresses();
			while (addrs.hasMoreElements()) {
				InetAddress addr = (InetAddress) addrs.nextElement();
				ni.addAddress(new IPAddress(addr.getHostAddress(), addr.isLoopbackAddress(), (addr instanceof Inet6Address), addr.isLinkLocalAddress()));
			}
			if (ni.getAddresses() != null) {
				networkinterfaces.add(ni);
				if (Log.isDebug()) {
					Log.debug("Interface found: " + ni);
				}
			}
		}
		if (Log.isDebug()) {
			Log.debug("Interface detection done.");
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.communication.connection.ip.IPNetworkDetection#
	 * detectAddresses()
	 */
	void detectAddresses() throws IOException {
		if (networkinterfaces == null) {
			detectInterfaces();
		}
		Iterator niIter = networkinterfaces.iterator();
		if (addresses != null) {
			addresses.clear();
		} else {
			addresses = new HashMap();
		}
		iPv4LoopbackAddress = null;

		while (niIter.hasNext()) {
			org.ws4d.java.communication.connection.ip.NetworkInterface ni = (org.ws4d.java.communication.connection.ip.NetworkInterface) niIter.next();
			Iterator addrIter = ni.getAddresses();
			while (addrIter.hasNext()) {
				IPAddress addr = (IPAddress) addrIter.next();
				String hostAddress = addr.getAddress();

				if (addr.isLoopback() && !addr.isIPv6() && addr.getAddress().startsWith("127.")) {
					iPv4LoopbackAddress = addr;
				}

				addresses.put(hostAddress, addr);
				if (addr.isIPv6()) {
					addresses.put(addr.getAddressWithoutNicId(), addr);
				}
			}
		}
	}

}
