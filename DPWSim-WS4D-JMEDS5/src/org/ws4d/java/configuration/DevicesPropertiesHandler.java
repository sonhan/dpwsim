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

import org.ws4d.java.structures.HashMap;

/**
 * Class handles device properties.
 * 
 * @author mspies
 */
public class DevicesPropertiesHandler implements PropertiesHandler {

	// private Map devices = new HashMap();

	private HashMap				devProps			= new HashMap();

	private DeviceProperties	buildUpProperties	= null;

	/** default properties for all devices */
	private DeviceProperties	defaultProperties	= null;

	private static String		className			= null;

	// -------------------------------------------------------

	// private static DevicesPropertiesHandler handler = new
	// DevicesPropertiesHandler();

	DevicesPropertiesHandler() {
		super();
		// if (handler != null) {
		// throw new
		// RuntimeException("DevicesPropertiesHandler: class already instantiated!");
		// }
		className = this.getClass().getName();
		// handler = this;
	}

	/**
	 * Returns instance of the devices properties handler.
	 * 
	 * @return the singleton instance of the devices properties
	 */
	public static DevicesPropertiesHandler getInstance() {
		// if (handler == null) {
		// handler = new DevicesPropertiesHandler();
		// }
		// return handler;
		return (DevicesPropertiesHandler) Properties.forClassName(Properties.DEVICES_PROPERTIES_HANDLER_CLASS);
	}

	/**
	 * Returns class name if object of this class has already been created, else
	 * null.
	 * 
	 * @return Class name if object of this class has already been created, else
	 *         null.
	 */
	public static String getClassName() {
		return className;
	}

	// -------------------------------------------------------------

	/*
	 * (non-Javadoc)
	 * @see
	 * org.ws4d.java.configuration.PropertiesHandler#setProperties(org.ws4d.
	 * java.configuration.PropertyHeader, org.ws4d.java.configuration.Property)
	 */
	public void setProperties(PropertyHeader header, Property property) {
		if (Properties.HEADER_SECTION_DEVICES.equals(header)) {
			// Properties of "Devices" Section, default for devices
			if (defaultProperties == null) {
				defaultProperties = new DeviceProperties();
			}

			defaultProperties.addProperty(property);
		}

		else if (Properties.HEADER_SUBSECTION_DEVICE.equals(header)) {
			// Properties of "Device" Section
			if (buildUpProperties == null) {
				if (defaultProperties != null) {
					buildUpProperties = new DeviceProperties(defaultProperties);
				} else {
					buildUpProperties = new DeviceProperties();
				}
			}

			buildUpProperties.addProperty(property);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.configuration.PropertiesHandler#finishedSection(int)
	 */
	public void finishedSection(int depth) {
		if (depth == 2 && buildUpProperties != null) {
			// initialize DeviceProperties
			if (!buildUpProperties.getConfigurationId().equals(DeviceProperties.DEFAULT_CONFIGURATION_ID)) {
				Integer id = buildUpProperties.getConfigurationId();

				devProps.put(id, buildUpProperties);
			}
			buildUpProperties = null;
		} else if (depth <= 1) {
			// XXX remove all management structure, it is not used anymore
			defaultProperties = null;
			buildUpProperties = null;
			// devices = null;
			// Properties.getInstance().unregister(Properties.HEADER_SECTION_DEVICES);
		}

	}

	// /**
	// * Adds device to configurable devices of device properties.
	// * The configuartionId must be unique within the framework.
	// * The configurationId maps to the device property entry
	// "ConfigurationId".
	// *
	// * @param device device to be configurable
	// * @param configurationId unique identifier within the framework.
	// */
	// public synchronized void addConfigurableDevice( DefaultDevice device, int
	// configurationId ){
	// devices.put( new Integer( configurationId ), device );
	// }
	//
	// /**
	// * @return
	// */
	// public synchronized DeviceProperties getDefaultProperties() {
	// return defaultProperties;
	// }
	//
	// /**
	// * Gets map with all configurable devices.
	// *
	// * @return
	// */
	// public synchronized Map getDevices() {
	// return devices;
	// }

	/**
	 * Gets device properties by configuration id.
	 * 
	 * @param configurationId
	 * @return device properties
	 */
	public DeviceProperties getDeviceProperties(Integer configurationId) {
		return (DeviceProperties) devProps.get(configurationId);
	}
}
