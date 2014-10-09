/*******************************************************************************
 * Copyright (c) 2009 MATERNA Information & Communications. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html. For further
 * project-related information visit http://www.ws4d.org. The most recent
 * version of the JMEDS framework can be obtained from
 * http://sourceforge.net/projects/ws4d-javame.
 ******************************************************************************/
package org.ws4d.java.communication.protocol.soap.generator;

import org.ws4d.java.configuration.DPWSProperties;
import org.ws4d.java.configuration.FrameworkProperties;
import org.ws4d.java.structures.HashMap;
import org.ws4d.java.util.Log;

/**
 * Implementation of the factory class to get the default
 * {@link SOAP2MessageGenerator} and {@link Message2SOAPGenerator} objects.
 */
public class SOAPMessageGeneratorFactory {

	/**
	 * Size this map according to the size of the framework's thread pool size.
	 * ATTENTION! To get the correct property values, the framework must be
	 * started before. key = Thread, value = Message Generator
	 */
	private static final HashMap						SOAP2MSG_GENERATOR_CACHE	= new HashMap(FrameworkProperties.getInstance().getThreadPoolSize());

	/**
	 * Size this map according to the size of the framework's thread pool size.
	 * ATTENTION! To get the correct property values, the framework must be
	 * started before. key = Thread, value = SOAP Generator
	 */
	private static final HashMap						MSG2SOAP_GENERATOR_CACHE	= new HashMap(FrameworkProperties.getInstance().getThreadPoolSize());

	private static final SOAPMessageGeneratorFactory	INSTANCE;

	static {
		String factoryClassName = DPWSProperties.getInstance().getSOAPMessageGeneratorFactoryClass();

		SOAPMessageGeneratorFactory factory = null;
		if (factoryClassName == null) {
			factory = new SOAPMessageGeneratorFactory();
		} else {
			try {
				Class factoryClass = Class.forName(factoryClassName);
				factory = (SOAPMessageGeneratorFactory) factoryClass.newInstance();
				if (Log.isDebug()) {
					Log.debug("Using SOAPMessageGeneratorFactory [" + factoryClassName + "]", Log.DEBUG_LAYER_FRAMEWORK);
				}
			} catch (ClassNotFoundException e) {
				Log.error("SOAPMessageGeneratorFactory: Configured SOAPMessageGeneratorFactory class [" + factoryClassName + "] not found, falling back to default implementation");
				factory = new SOAPMessageGeneratorFactory();
			} catch (Exception e) {
				Log.error("SOAPMessageGeneratorFactory: Unable to create instance of configured SOAPMessageGeneratorFactory class [" + factoryClassName + "], falling back to default implementation");
				Log.printStackTrace(e);
				factory = new SOAPMessageGeneratorFactory();
			}
		}
		INSTANCE = factory;
	}

	public static SOAPMessageGeneratorFactory getInstance() {
		return INSTANCE;
	}

	public static void clear() {
		SOAP2MSG_GENERATOR_CACHE.clear();
		MSG2SOAP_GENERATOR_CACHE.clear();
	}

	public synchronized SOAP2MessageGenerator getSOAP2MessageGeneratorForCurrentThread() {
		Thread t = Thread.currentThread();
		SOAP2MessageGenerator generator = (SOAP2MessageGenerator) SOAP2MSG_GENERATOR_CACHE.get(t);
		if (generator == null) {
			// Changed 2011-01-11 SSch Ease the extension of this class
			// Cache could be reused
			generator = newSOAP2MessageGenerator();
			SOAP2MSG_GENERATOR_CACHE.put(t, generator);
		}
		return generator;
	}

	public synchronized Message2SOAPGenerator getMessage2SOAPGeneratorForCurrentThread() {
		Thread t = Thread.currentThread();
		Message2SOAPGenerator generator = (Message2SOAPGenerator) MSG2SOAP_GENERATOR_CACHE.get(t);
		if (generator == null) {
			// Changed 2011-01-11 SSch Ease the extension of this class
			// Cache could be reused
			generator = newMessage2SOAPGenerator();
			MSG2SOAP_GENERATOR_CACHE.put(t, generator);
		}
		return generator;
	}

	protected Message2SOAPGenerator newMessage2SOAPGenerator() {
		return new DefaultMessage2SOAPGenerator();
	}

	protected SOAP2MessageGenerator newSOAP2MessageGenerator() {
		return new DefaultSOAP2MessageGenerator();
	}

}
