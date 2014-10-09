/*******************************************************************************
 * Copyright (c) 2009 MATERNA Information & Communications. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html. For further
 * project-related information visit http://www.ws4d.org. The most recent
 * version of the JMEDS framework can be obtained from
 * http://sourceforge.net/projects/ws4d-javame.
 ******************************************************************************/
package org.ws4d.java.wsdl;

import org.ws4d.java.configuration.FrameworkProperties;
import org.ws4d.java.util.Log;

/**
 *
 */
public class WSDLSupportFactory {

	private static final WSDLSupportFactory	INSTANCE;

	static {
		WSDLSupportFactory factory = null;
		String factoryClassName = FrameworkProperties.getInstance().getWsdlSupportFactoryClass();
		if (factoryClassName == null) {
			// use default one silently
			factory = new WSDLSupportFactory();
		} else {
			try {
				Class factoryClass = Class.forName(factoryClassName);
				factory = (WSDLSupportFactory) factoryClass.newInstance();
				if (Log.isDebug()) {
					Log.debug("Using WSDLSupportFactory [" + factoryClassName + "]", Log.DEBUG_LAYER_FRAMEWORK);
				}
			} catch (ClassNotFoundException e) {
				Log.error("Configured WSDLSupportFactory class [" + factoryClassName + "] not found, falling back to default implementation");
				factory = new WSDLSupportFactory();
			} catch (Exception ex) {
				Log.error("Unable to create instance of configured WSDLSupportFactory class [" + factoryClassName + "], falling back to default implementation");
				Log.printStackTrace(ex);
				factory = new WSDLSupportFactory();
			}
		}
		INSTANCE = factory;
	}

	/**
	 * @return
	 */
	public static WSDLSupportFactory getInstance() {
		return INSTANCE;
	}

	/**
	 * @return
	 */
	public WSDLParser newParser() {
		return new DefaultWSDLParser();
	}

	/**
	 * @return
	 */
	public WSDLSerializer newSerializer() {
		return new DefaultWSDLSerializer();
	}

}
