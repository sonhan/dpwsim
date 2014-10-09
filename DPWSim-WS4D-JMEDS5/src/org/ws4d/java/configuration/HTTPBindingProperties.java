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

import org.ws4d.java.communication.HTTPBinding;
import org.ws4d.java.communication.connection.ip.IPAddress;
import org.ws4d.java.communication.connection.ip.IPNetworkDetection;
import org.ws4d.java.structures.ArrayList;
import org.ws4d.java.structures.Iterator;
import org.ws4d.java.util.Log;
import org.ws4d.java.util.StringUtil;

public class HTTPBindingProperties implements PropertiesHandler {

	public static final String				PROP_ADDRESS						= "Address";

	public static final String				PROP_PORT							= "Port";

	public static final String				PROP_SUFFIX							= "Suffix";

	public static final String				PROP_ADDRESS_GROUP					= "AddressGroup";

	public static final String				SUBSECTION_HTTP_BINDINGS			= "HTTPBindings";

	public static final String				SUBSUBSECTION_HTTP_BINDING			= "HTTPBinding";

	public static final String				PROP_ADDRESS_GROUP_INET6			= "inet6";

	public static final String				PROP_ADDRESS_GROUP_INET4			= "inet4";

	public static final String				PROP_ADDRESS_GROUP_ALL				= "all";

	public static final String				PROP_ADDRESS_GROUP_LO				= "lo";

	public static final PropertyHeader		HEADER_SUBSECTION_HTTP_BINDINGS		= new PropertyHeader(SUBSECTION_HTTP_BINDINGS, Properties.HEADER_SECTION_BINDINGS);

	public static final PropertyHeader		HEADER_SUBSUBSECTION_HTTP_BINDING	= new PropertyHeader(SUBSUBSECTION_HTTP_BINDING, HEADER_SUBSECTION_HTTP_BINDINGS);

	// ---------------------------------------

	private static HTTPBindingProperties	handler								= null;

	// ------------ DEFAULT -------------------

	private ArrayList						defaultAddresses					= new ArrayList();

	private int								defaultPort							= 0;

	private String							defaultSuffix						= null;

	// ----------------------------------------

	private BuildUpProperties				buildUpBinding						= null;

	HTTPBindingProperties() {
		super();
		if (handler != null) {
			throw new RuntimeException("HTTPBindingProperties: class already instantiated!");
		}
		handler = this;
	}

	static synchronized HTTPBindingProperties getInstance() {
		if (handler == null) {
			handler = new HTTPBindingProperties();
		}
		return handler;
	}

	/**
	 * Detect a group of inet addresse strings by name. For example: value =
	 * "inet6@eth0" returns all ipv6 addresses from the interface "eth0".
	 * 
	 * @param value
	 * @return A String ArrayList with the group addresses
	 */
	private ArrayList getGroupByName(String value) {
		ArrayList ret = new ArrayList();
		Iterator iti = null;
		if (value.indexOf('@') == -1) {
			if (StringUtil.equalsIgnoreCase(PROP_ADDRESS_GROUP_ALL, value)) {
				// all
				iti = IPNetworkDetection.getInstance().getAddresses();
			} else if (StringUtil.equalsIgnoreCase(PROP_ADDRESS_GROUP_LO, value)) {
				// lo
				iti = IPNetworkDetection.getInstance().getAddresses(null, value);
			} else if (StringUtil.equalsIgnoreCase(PROP_ADDRESS_GROUP_INET4, value) || StringUtil.equalsIgnoreCase(PROP_ADDRESS_GROUP_INET6, value)) {
				// inet4, inet6
				iti = IPNetworkDetection.getInstance().getAddresses(value, null);
			} else {
				// f.e. eth0, lo
				iti = IPNetworkDetection.getInstance().getAddresses(null, value);
			}
		} else {
			String protocol = value.substring(0, value.indexOf('@'));
			String ifaceName = value.substring(value.indexOf('@') + 1);
			if (StringUtil.equalsIgnoreCase(PROP_ADDRESS_GROUP_INET4, protocol) || StringUtil.equalsIgnoreCase(PROP_ADDRESS_GROUP_ALL, protocol) || StringUtil.equalsIgnoreCase(PROP_ADDRESS_GROUP_LO, protocol) || StringUtil.equalsIgnoreCase(PROP_ADDRESS_GROUP_INET6, protocol)) {
				iti = IPNetworkDetection.getInstance().getAddresses(protocol, ifaceName);
			} else {
				if (Log.isError()) Log.error("Cann not parse address-group for protocol: " + protocol + ". Set default: all addresses.");
			}
		}
		if (iti != null && iti.hasNext()) {
			while (iti.hasNext()) {
				IPAddress ipAddr = (IPAddress) iti.next();
				ret.add(ipAddr);
			}
		} else {
			Log.warn("Cann not parse address-group. Set default: all addresses.");
		}
		return ret;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.ws4d.java.configuration.PropertiesHandler#setProperties(org.ws4d.
	 * java.configuration.PropertyHeader, org.ws4d.java.configuration.Property)
	 */
	public void setProperties(PropertyHeader header, Property property) {
		if (HEADER_SUBSECTION_HTTP_BINDINGS.equals(header)) {
			/*
			 * Properties of "HTTPBindings" Section, default for HTTPBindings
			 */
			if (PROP_ADDRESS.equals(property.key)) {
				defaultAddresses.add(property.value);
			} else if (PROP_ADDRESS_GROUP.equals(property.key)) {
				defaultAddresses.addAll(getGroupByName(property.value));

			} else if (PROP_PORT.equals(property.key)) {
				defaultPort = Integer.parseInt(property.value.trim());
			} else if (PROP_SUFFIX.equals(property.key)) {
				defaultSuffix = property.value;
			}
		} else if (HEADER_SUBSUBSECTION_HTTP_BINDING.equals(header)) {
			/*
			 * Properties of "HTTPBinding" Section
			 */
			if (buildUpBinding == null) {
				buildUpBinding = new BuildUpProperties();
			}

			if (BindingProperties.PROP_BINDING_ID.equals(property.key)) {
				buildUpBinding.bindingId = Integer.valueOf(property.value);
			} else if (PROP_ADDRESS.equals(property.key)) {
				buildUpBinding.buildUpAddresses.add(property.value);
			} else if (PROP_ADDRESS_GROUP.equals(property.key)) {
				buildUpBinding.buildUpAddresses.addAll(getGroupByName(property.value));
			} else if (PROP_PORT.equals(property.key)) {
				buildUpBinding.buildUpPort = Integer.parseInt(property.value.trim());
			} else if (PROP_SUFFIX.equals(property.key)) {
				buildUpBinding.buildUpSuffix = property.value;
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.configuration.PropertiesHandler#finishedSection(int)
	 */
	public void finishedSection(int depth) {

		if (depth == 3 && buildUpBinding != null) {
			// initialize DeviceProperties
			if (!buildUpBinding.bindingId.equals(BindingProperties.DEFAULT_BINDING_ID)) {
				ArrayList t = buildUpBinding.createBindings();
				BindingProperties.getInstance().addCommunicationBinding(buildUpBinding.bindingId, t);
			} else {
				Log.error("HTTPBindingProperties: binding id not set: " + buildUpBinding);
			}
		}
		buildUpBinding = null;
	}

	private class BuildUpProperties {

		Integer		bindingId			= BindingProperties.DEFAULT_BINDING_ID;

		ArrayList	buildUpAddresses	= null;

		int			buildUpPort;

		String		buildUpSuffix;

		public BuildUpProperties() {
			buildUpAddresses = new ArrayList();
			buildUpPort = defaultPort;
			buildUpSuffix = defaultSuffix;
		}

		public ArrayList createBindings() {
			ArrayList r = new ArrayList();
			if (buildUpAddresses.size() == 0) {
				buildUpAddresses = defaultAddresses;
			}
			for (int i = 0; i < buildUpAddresses.size(); i++) {
				IPAddress ipAddress = IPNetworkDetection.getInstance().getIPAddress((String) buildUpAddresses.get(i));
				if (ipAddress != null) {
					r.add(new HTTPBinding(ipAddress, buildUpPort, buildUpSuffix));
				}
			}
			return r;
		}

		public String toString() {
			if (buildUpAddresses.size() == 0) {
				return "BuildUpProperties not specified";
			}

			String s = "";
			int i;
			for (i = 0; i < (buildUpAddresses.size() - 1); i++)
				s = s + (String) buildUpAddresses.get(i) + " | ";
			return ("BindingId = " + bindingId + " | Addresses = [" + s + (String) buildUpAddresses.get(i) + "] " + "| Port = " + buildUpPort + " | Suffix = " + buildUpSuffix);
		}
	}
}
