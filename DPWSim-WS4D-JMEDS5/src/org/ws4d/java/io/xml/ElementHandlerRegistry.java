/*******************************************************************************
 * Copyright (c) 2009 MATERNA Information & Communications. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html. For further
 * project-related information visit http://www.ws4d.org. The most recent
 * version of the JMEDS framework can be obtained from
 * http://sourceforge.net/projects/ws4d-javame.
 ******************************************************************************/
package org.ws4d.java.io.xml;

import org.ws4d.java.structures.HashMap;
import org.ws4d.java.types.QName;

/**
 * 
 */
public final class ElementHandlerRegistry {

	private static final ElementHandlerRegistry	INSTANCE	= new ElementHandlerRegistry();

	/**
	 * Returns the singleton registry instance for the current runtime.
	 * 
	 * @return the one and only element handler registry instance
	 */
	public static ElementHandlerRegistry getRegistry() {
		return INSTANCE;
	}

	/**
	 * This map holds all registered unknown element handlers.
	 */
	// key = QName, value = ElementHandler
	private final HashMap	elementHandlers	= new HashMap();

	public synchronized void registerElementHandler(QName elementName, ElementHandler handler) {
		if (elementName == null || handler == null) {
			return;
		}
		if (elementHandlers.containsKey(elementName)) {
			throw new IllegalArgumentException("a handler is already registered for this element name: " + elementName);
		}
		elementHandlers.put(elementName, handler);
	}

	public synchronized ElementHandler getElementHandler(QName elementName) {
		return (ElementHandler) elementHandlers.get(elementName);
	}

	/**
	 * Private constructor, disallows any instantiations from outside this
	 * class.
	 */
	private ElementHandlerRegistry() {
		super();
	}
	/**
	 * Give all registered handlers 
	 * @return HashMap that contains all registered element handler.
	 */
	public synchronized HashMap getAllElementHandler()
	{
		return elementHandlers;
	}

}
