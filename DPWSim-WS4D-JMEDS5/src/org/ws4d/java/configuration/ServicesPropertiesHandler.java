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
 * @author mspies
 */
public class ServicesPropertiesHandler implements PropertiesHandler {

	private HashMap				servProps			= new HashMap();

	private ServiceProperties	buildUpProperties	= null;

	/** default properties for all services */
	private ServiceProperties	defaultProperties	= null;

	// private static ServicesPropertiesHandler handler = null;
	//
	// private static String className = null;

	// -------------------------------------------------------------------

	ServicesPropertiesHandler() {
		super();
		// if (handler != null) {
		// throw new
		// RuntimeException("ServicePropertiesHandler: class already instantiated!");
		// }
		// className = this.getClass().getName();
		// handler = this;
	}

	/**
	 * Returns instance of service properties handler.
	 * 
	 * @return the singleton instance of the service properties
	 */
	public static ServicesPropertiesHandler getInstance() {
		// if (handler == null) {
		// handler = new ServicesPropertiesHandler();
		// }
		// return handler;
		return (ServicesPropertiesHandler) Properties.forClassName(Properties.SERVICES_PROPERTIES_HANDLER_CLASS);
	}

	// /**
	// * Returns class name if object of this class has previously been created,
	// * else null.
	// *
	// * @return Class name if object of this class has previously been created,
	// * else null.
	// */
	// public static String getClassName() {
	// return className;
	// }

	/**
	 * Gets service properties.
	 * 
	 * @param configurationId
	 * @return service properties
	 */
	public ServiceProperties getServiceProperties(Integer configurationId) {
		return (ServiceProperties) servProps.get(configurationId);
	}

	// -------------------------------------------------------------

	public void setProperties(PropertyHeader header, Property property) {

		if (Properties.HEADER_SECTION_SERVICES.equals(header)) {
			// Properties of "Services" Section, default for subsections
			if (defaultProperties == null) {
				defaultProperties = new ServiceProperties();
			}

			defaultProperties.addProperty(property);
		}

		else if (Properties.HEADER_SUBSECTION_SERVICE.equals(header)) {
			// Properties of "Service" Section
			if (buildUpProperties == null) {
				if (defaultProperties != null) {
					buildUpProperties = new ServiceProperties(defaultProperties);
				} else {
					buildUpProperties = new ServiceProperties();
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
			if (!buildUpProperties.getConfigurationId().equals(ServiceProperties.DEFAULT_CONFIGURATION_ID)) {
				Integer id = buildUpProperties.getConfigurationId();
				servProps.put(id, buildUpProperties);
			}
			buildUpProperties = null;
		} else if (depth <= 1) {
			// XXX remove all management structure, it is not used anymore
			defaultProperties = null;
			buildUpProperties = null;
			// servProps = null;
			// Properties.getInstance().unregister(Properties.HEADER_SECTION_SERVICES);
		}

	}

	// /**
	// * Adds service to configurable services of service properties.
	// * The configuartionId must be unique within the framework.
	// * The configurationId maps to the service property entry
	// "ConfigurationId".
	// *
	// * @param service service to be configurable
	// * @param configurationId unique identifier within the framework.
	// */
	// public synchronized void addConfigurableService( LocalService service,
	// int configurationId ){
	// services.put( new Integer( configurationId ), service );
	// }
}
