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

import org.ws4d.java.util.Log;

public class GlobalPropertiesHandler implements PropertiesHandler {

	// ---------- Logging Properties -----------------
	public static final String	PROP_LOG_LEVEL			= "LogLevel";

	public static final String	PROP_LOG_TIMESTAMP		= "LogTimestamp";

	public static final String	PROP_LOG_STACK_TRACE	= "LogStackTrace";

	// public static final String PROP_LOG_XML_OUTPUT = "LogXMLOutput";
	//
	// -------------------------------------------------------------

	// private static GlobalPropertiesHandler handler = null;
	//
	// private static String className = null;

	// -------------------------------------------------------------

	/**
	 * Private constructor.
	 */
	GlobalPropertiesHandler() {
		super();
		// if (handler != null) {
		// throw new
		// RuntimeException("GlobalPropertiesHandler: class already instantiated!");
		// }
		// className = this.getClass().getName();
		// handler = this;
	}

	/**
	 * Get instance of this.
	 * 
	 * @return the singleton instance of the global properties handler
	 */
	public static GlobalPropertiesHandler getInstance() {
		// if (handler == null) {
		// handler = new GlobalPropertiesHandler();
		// }
		// return handler;
		return (GlobalPropertiesHandler) Properties.forClassName(Properties.GLOBAL_PROPERTIES_HANDLER_CLASS);
	}

	// /**
	// * Returns class name, if an object of this class was previously created,
	// * else null.
	// *
	// * @return Class name, if an object of this class was previously created,
	// * else null.
	// */
	// public static String getClassName() {
	// return className;
	// }

	// -------------------------------------------------------------

	/*
	 * (non-Javadoc)
	 * @see
	 * org.ws4d.java.configuration.PropertiesHandler#setProperties(org.ws4d.
	 * java.configuration.PropertyHeader, org.ws4d.java.configuration.Property)
	 */
	public void setProperties(PropertyHeader header, Property property) {
		if (Log.isDebug()) {
			Log.debug("GlobalPropertiesHandler.setProperties: " + property, Log.DEBUG_LAYER_FRAMEWORK);
		}

		if (Properties.HEADER_SUBSECTION_LOGGING.equals(header)) {

			if (PROP_LOG_LEVEL.equals(property.key)) {
				Log.setLogLevel(Integer.parseInt(property.value.trim()));
			} else if (PROP_LOG_TIMESTAMP.equals(property.key)) {
				if ("true".equals(property.value)) {
					Log.setShowTimestamp(true);
				} else if ("false".equals(property.value)) {
					Log.setShowTimestamp(false);
				}
			} else if (PROP_LOG_STACK_TRACE.equals(property.key)) {
				if ("true".equals(property.value)) {
					Log.setLogStackTrace(true);
				} else if ("false".equals(property.value)) {
					Log.setLogStackTrace(false);
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.configuration.PropertiesHandler#finishedSection(int)
	 */
	public void finishedSection(int depth) {
		// XXX handler is not used anymore => remove
		if (depth <= 1) {
			Properties.getInstance().unregister(Properties.HEADER_SECTION_GLOBAL);
		}
	}

}
