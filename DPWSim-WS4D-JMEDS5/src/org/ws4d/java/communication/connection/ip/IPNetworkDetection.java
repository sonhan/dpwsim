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

import org.ws4d.java.structures.HashMap;
import org.ws4d.java.structures.Iterator;
import org.ws4d.java.structures.LinkedList;
import org.ws4d.java.structures.List;
import org.ws4d.java.util.Log;
import org.ws4d.java.util.StringUtil;

/**
 * This abstract class defines some methods for the platform specific network
 * detection.
 */
public abstract class IPNetworkDetection {

	protected static List				networkinterfaces	= null;

	protected static HashMap			addresses			= null;

	protected static IPAddress			iPv4LoopbackAddress	= null;

	private static IPNetworkDetection	instance			= null;

	public static IPNetworkDetection getInstance() {
		if (instance == null) {
			try {
				Class clazz = Class.forName("org.ws4d.java.communication.connection.ip.PlatformIPNetworkDetection");
				instance = (IPNetworkDetection) clazz.newInstance();
			} catch (Exception e) {
				Log.error("Unable to instantiate PlatformIPNetworkDetection: " + e);
				throw new RuntimeException(e.getMessage());
			}
		}
		return instance;
	}

	/**
	 * Returns all network interfaces found on this platform. Starts an
	 * interface detection phase if necessary.
	 * 
	 * @return all network interfaces.
	 */
	public final synchronized Iterator getNetworkInterfaces() {
		if (networkinterfaces == null) {
			try {
				detectInterfaces();
			} catch (IOException e) {
				// TODO refactor
				Log.printStackTrace(e);
			}
		}
		return networkinterfaces.iterator();
	}

	/**
	 * Returns an iterator with all available addresses.
	 * 
	 * @return
	 */
	public final synchronized Iterator getAddresses() {
		if (addresses == null) {
			try {
				detectAddresses();
			} catch (IOException e) {
				// TODO refactor
				Log.printStackTrace(e);
			}
		}
		return addresses.entrySet().iterator();
	}

	/**
	 * Returns an iterator with all available addresses. Filtered by the type of
	 * protocol or by the interface name.
	 * 
	 * @param protocol inet4, inet6 or null for wildcard.
	 * @param ifaceName for example eth0 or null for wildcard.
	 * @return
	 */
	public Iterator getAddresses(String protocol, String ifaceName) {
		Iterator niIter = getNetworkInterfaces();
		LinkedList result = new LinkedList();

		while (niIter.hasNext()) {
			org.ws4d.java.communication.connection.ip.NetworkInterface ni = (org.ws4d.java.communication.connection.ip.NetworkInterface) niIter.next();
			if (ifaceName == null || StringUtil.equalsIgnoreCase(ni.getName(), ifaceName)) {
				Iterator addrIter = ni.getAddresses();
				while (addrIter.hasNext()) {
					IPAddress addr = (IPAddress) addrIter.next();
					if (protocol == null || (!addr.isIPv6() && StringUtil.equalsIgnoreCase(protocol, "inet4")) || (addr.isIPv6() && StringUtil.equalsIgnoreCase(protocol, "inet6"))) {
						result.add(addr);
					}
				}
			}
		}
		return result.iterator();
	}

	public synchronized IPAddress getIPAddress(String address) {
		if (addresses == null) {
			try {
				detectAddresses();
			} catch (IOException e) {
				// TODO refactor
				Log.printStackTrace(e);
			}
		}

		IPAddress result = (IPAddress) addresses.get(address);

		if (result == null && address.indexOf(':') != -1 && address.charAt(0) != '[') {
			address = "[" + address + "]";
			result = (IPAddress) addresses.get(address);
		}

		if (result == null) {
			result = (IPAddress) addresses.get(getCanonicalAddress(address));
		}

		if (result == null && iPv4LoopbackAddress != null && address.startsWith("127.")) {
			return iPv4LoopbackAddress;
		}

		if (result == null && Log.isError()) {

			Iterator it = addresses.keySet().iterator();
			String s = "";
			while (it.hasNext()) {
				s += it.next();
				if (it.hasNext()) {
					s += ", ";
				}
			}
			Log.error("IPAddress object not found for " + address + ". Addresses found: " + s);
			if (Log.isDebug()) {
				Log.printStackTrace(new Exception());
			}
		}

		return result;
	}

	/**
	 * This method returns the canonical form of the supplied
	 * <code>address</code>.
	 * 
	 * @param address either an IPv4, IPv6 address or a DNS name
	 * @return the canonical address corresponding to <code>address</code>,
	 *         usually an IP address
	 */
	abstract public String getCanonicalAddress(String address);

	/**
	 * Starts the interface detection.
	 * 
	 * @throws IOException
	 */
	abstract void detectInterfaces() throws IOException;

	/**
	 * Starts the address detection.
	 * 
	 * @throws IOException
	 */
	abstract void detectAddresses() throws IOException;

}
