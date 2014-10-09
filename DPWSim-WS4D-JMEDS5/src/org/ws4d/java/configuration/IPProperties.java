/*******************************************************************************
 * Copyright (c) 2009 MATERNA Information & Communications. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html. For further
 * project-related information visit http://www.ws4d.org. The most recent
 * version of the JMEDS framework can be obtained from
 * http://sourceforge.net/projects/ws4d-javame.
 ******************************************************************************/
package org.ws4d.java.configuration;

import org.ws4d.java.communication.connection.ip.IPAddress;
import org.ws4d.java.structures.HashSet;
import org.ws4d.java.structures.Set;
import org.ws4d.java.util.Log;

public class IPProperties implements PropertiesHandler {

	public static final int				ALLOW_ALL					= 0;

	public static final int				DENY_ALL					= 1;

	public static final String			SUBSUBSECTION_ALLOW			= "Allow";

	public static final String			SUBSUBSECTION_DENY			= "Deny";

	public static final String			PROP_ADDRESS				= "Address";

	public static final String			PROP_FILTER_MODE			= "FilterMode";

	public static final PropertyHeader	HEADER_SUBSUBSECTION_ALLOW	= new PropertyHeader(SUBSUBSECTION_ALLOW, Properties.HEADER_SECTION_IP);

	public static final PropertyHeader	HEADER_SUBSUBSECTION_DENY	= new PropertyHeader(SUBSUBSECTION_DENY, Properties.HEADER_SECTION_IP);

	private Set							whiteList					= new HashSet();

	private Set							blackList					= new HashSet();

	private Set							notified					= new HashSet();

	private int							ipFilterMode				= 0;

	IPProperties() {
		super();
	}

	public static synchronized IPProperties getInstance() {
		return (IPProperties) Properties.forClassName(Properties.IP_PROPERTIES_HANDLER_CLASS);
	}

	/**
	 * Returns the IPFilterMode
	 * 
	 * @return
	 */
	public int getIPFilterMode() {
		return ipFilterMode;
	}

	public boolean isAllowedByIPFilter(IPAddress adr) {
		boolean ret = true;
		if (ipFilterMode == ALLOW_ALL) {
			ret = !blackList.contains(adr);
		} else if (ipFilterMode == DENY_ALL) {
			ret = whiteList.contains(adr);
		}
		if (!ret && !notified.contains(adr)) {
			Log.warn("Incoming communication from address " + adr + " discarded by IP filter.");
			notified.add(adr);
		}
		return ret;
	}

	public void allowAddress(IPAddress adr) {
		whiteList.add(adr);
	}

	public void denyAddress(IPAddress adr) {
		blackList.add(adr);
	}

	public void setProperties(PropertyHeader header, Property property) {
		if (HEADER_SUBSUBSECTION_DENY.equals(header)) {
			if (PROP_ADDRESS.equals(property.key)) {
				blackList.add(new IPAddress(property.value));
			}
		} else if (HEADER_SUBSUBSECTION_ALLOW.equals(header)) {
			if (PROP_ADDRESS.equals(property.key)) {
				whiteList.add(new IPAddress(property.value));
			}
		} else if (PROP_FILTER_MODE.equals(property.key)) {
			ipFilterMode = Integer.parseInt(property.value.trim());
		}
	}

	public void finishedSection(int depth) {

	}

}
