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

/**
 * Internal IP-Address container. It comprised the Address as a string, the info
 * if it is a Loopback address, a IPv4 or IPv6 address and if IPv6 if it is a
 * LinkLocal address
 */
public class IPAddress {

	private String			address;

	private String			addressWithoutNicId;

	private final boolean	isLoopback;

	private final boolean	isIPv6;

	private final boolean	isIPv6LinkLocal;

	public IPAddress(String address, boolean isLoopback, boolean isIPv6, boolean isIPv6LinkLocal) {
		super();

		this.isIPv6 = isIPv6;
		this.address = address.trim();
		addBrackets();
		this.isLoopback = isLoopback;
		this.isIPv6LinkLocal = isIPv6LinkLocal;
		createAddressWithoutNicId();
	}

	public IPAddress(String address) {
		super();
		/*
		 * If the address contains any ":", address is an ipv6 address. Correct
		 * ipv6 address has brackets.
		 */
		isIPv6 = address.indexOf(':') != -1;
		this.address = address.trim();
		addBrackets();
		this.isLoopback = false;
		this.isIPv6LinkLocal = false;
		createAddressWithoutNicId();
	}

	public String getAddress() {
		return address;
	}

	public String getAddressWithoutNicId() {
		return addressWithoutNicId;
	}

	public boolean isLoopback() {
		return isLoopback;
	}

	public boolean isIPv6() {
		return isIPv6;
	}

	public boolean isIPv6LinkLocal() {
		return isIPv6LinkLocal;
	}

	public String toString() {
		return address;
	}

	public int hashCode() {
		int result = 31 + ((address == null) ? 0 : address.hashCode());
		return result;
	}

	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		IPAddress other = (IPAddress) obj;
		if (address == null) {
			if (other.address != null) return false;
		} else if (!address.equals(other.address)) return false;
		return true;
	}

	private void addBrackets() {
		/*
		 * Correct ipv6 address has brackets.
		 */
		if (isIPv6) {
			if (this.address.charAt(0) != '[') {
				this.address = "[" + this.address;
			}
			if (this.address.charAt(this.address.length() - 1) != ']') {
				this.address = this.address + "]";
			}
		}
	}

	private void createAddressWithoutNicId() {
		if (isIPv6()) {
			int idx = address.indexOf('%');
			if (idx != -1) {
				addressWithoutNicId = address.substring(0, idx) + "]";
			} else {
				addressWithoutNicId = address;
			}
		} else {
			addressWithoutNicId = address;
		}
	}

}
