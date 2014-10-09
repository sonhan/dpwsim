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

import org.ws4d.java.constants.FrameworkConstants;
import org.ws4d.java.util.Log;

/**
 * Class of framework properties.
 */
public class FrameworkProperties implements PropertiesHandler {

	/**
	 * Qualified name of the WSDL support factory class to use when creating
	 * WSDL parsers and serializers. <BR>
	 * Type: String <BR>
	 * Default: none (using internal default factory)
	 */
	public static final String	PROP_WSDL_SUPPORT_FACTORY_CLASS		= "WsdlSupportFactoryClass";

	public static final String	PROP_PROXY_SERVICE_FACTORY_CLASS	= "ProxyServiceFactoryClass";

	/**
	 * Property id to specify the service reference factory class name.
	 */
	public static final String	PROP_SERVREF_FACTRORY_CLASS			= "ServiceReferenceFactoryClass";

	/**
	 * Property id to specify the size of the ThreadPool.
	 */
	public static final String	PROP_KILL_ON_SHUTDOWN_HOOK			= "KillOnShutdownHook";

	/**
	 * Property id to specify the size of the ThreadPool.
	 */
	public static final String	PROP_THREADPOOL_SIZE				= "ThreadPoolSize";

	/**
	 * Property id to specify the size of the ThreadPool.
	 */
	public static final String	PROP_MAX_DGRAM_SIZE					= "MaxDatagramSize";

	public static final String	PROP_BYPASS_WSDL_REPOSITORY			= "BypassWSDLRepository";

	// -----------------------------------------------------

	// private static FrameworkProperties handler = null;
	//
	// private static String className = null;

	private String				wsdlSupportFactoryClass				= null;

	private String				serviceReferenceFactoryClass		= null;

	private String				proxyServiceFactoryClass			= null;

	private int					threadPoolSize						= 10000;

	private int					maxDatagramSize						= FrameworkConstants.DGRAM_MAX_SIZE;

	private boolean				killOnShutdownHook					= true;

	public static boolean		REFERENCE_PARAM_MODE				= true;

	private boolean				bypassWsdlRepository				= false;

	FrameworkProperties() {
		super();
		// if (handler != null) {
		// throw new
		// RuntimeException("FrameworkProperties: class already instantiated!");
		// }
		// className = this.getClass().getName();
		// handler = this;
	}

	/**
	 * Return instance of device properties.
	 * 
	 * @return the singleton instance of the framework properties
	 */
	public static FrameworkProperties getInstance() {
		// if (handler == null) {
		// handler = new FrameworkProperties();
		// }
		// return handler;
		return (FrameworkProperties) Properties.forClassName(Properties.FRAMEWORK_PROPERTIES_HANDLER_CLASS);
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

	public void setProperties(PropertyHeader header, Property property) {
		if (Properties.HEADER_SUBSECTION_FRAMEWORK.equals(header)) {
			try {
				if (PROP_WSDL_SUPPORT_FACTORY_CLASS.equals(property.key)) {
					setWsdlSupportFactoryClass(property.value);
				} else if (PROP_PROXY_SERVICE_FACTORY_CLASS.equals(property.key)) {
					setProxyServiceFactoryClass(property.value);
				} else if (PROP_SERVREF_FACTRORY_CLASS.equals(property.key)) {
					setServiceReferenceFactoryClass(property.value);
				} else if (PROP_KILL_ON_SHUTDOWN_HOOK.equals(property.key)) {
					setKillOnShutdownHook("true".equals(property.value));
				} else if (PROP_THREADPOOL_SIZE.equals(property.key)) {
					setThreadPoolSize(Integer.valueOf(property.value).intValue());
				} else if (PROP_MAX_DGRAM_SIZE.equals(property.key)) {
					setMaxDatagramSize(Integer.valueOf(property.value).intValue());
				} else if (PROP_BYPASS_WSDL_REPOSITORY.equals(property.key)) {
					setBypassWsdlRepository("true".equals(property.value));
				}
			} catch (NumberFormatException e) {
				Log.printStackTrace(e);
			}
		}
	}

	public void finishedSection(int depth) {

	}

	/**
	 * @return the wsdlSupportFactoryClass
	 */
	public String getWsdlSupportFactoryClass() {
		return wsdlSupportFactoryClass;
	}

	/**
	 * @return class name of proxy service
	 */
	public String getProxyServiceFactroryClass() {
		return proxyServiceFactoryClass;
	}

	/**
	 * Get the class name of the service reference factory class.
	 * 
	 * @return class name of the service reference factory class.
	 */
	public String getServiceReferenceFactoryClass() {
		return serviceReferenceFactoryClass;
	}

	public boolean getKillOnShutdownHook() {
		return this.killOnShutdownHook;
	}

	/**
	 * Get the size of the common thread pool.
	 * 
	 * @return Size of the common thread pool.
	 */
	public int getThreadPoolSize() {
		return threadPoolSize;
	}

	/**
	 * get the maximum UDP datagram size.
	 * 
	 * @return the maximum UDP datagram size the framework expects
	 */
	public int getMaxDatagramSize() {
		return maxDatagramSize;
	}

	/**
	 * @return whether the WSDL repository should be bypassed during proxy
	 *         service creation
	 */
	public boolean isBypassWsdlRepository() {
		return bypassWsdlRepository;
	}

	/**
	 * @param bypassWsdlRepository whether to bypass the WSDL repository during
	 *            proxy service creation or not
	 */
	public void setBypassWsdlRepository(boolean bypassWsdlRepository) {
		this.bypassWsdlRepository = bypassWsdlRepository;
	}

	public void setProxyServiceFactoryClass(String className) {
		this.proxyServiceFactoryClass = className;
	}

	public void setServiceReferenceFactoryClass(String className) {
		this.serviceReferenceFactoryClass = className;
	}

	public void setKillOnShutdownHook(boolean b) {
		this.killOnShutdownHook = b;
	}

	public void setThreadPoolSize(int size) {
		this.threadPoolSize = size;
	}

	public void setMaxDatagramSize(int maxDatagramSize) {
		this.maxDatagramSize = maxDatagramSize;
	}

	/**
	 * @param wsdlSupportFactoryClass the wsdlSupportFactoryClass to set
	 */
	public void setWsdlSupportFactoryClass(String wsdlSupportFactoryClass) {
		if (wsdlSupportFactoryClass != null && "".equals(wsdlSupportFactoryClass)) {
			wsdlSupportFactoryClass = null;
		}
		this.wsdlSupportFactoryClass = wsdlSupportFactoryClass;
	}

}
