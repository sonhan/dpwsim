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

import org.ws4d.java.communication.CommunicationBinding;
import org.ws4d.java.communication.DiscoveryBinding;
import org.ws4d.java.structures.ArrayList;
import org.ws4d.java.structures.HashMap;
import org.ws4d.java.structures.Iterator;
import org.ws4d.java.structures.List;

/**
 * 
 */
public class BindingProperties implements PropertiesHandler {

	public static final String	PROP_BINDING_ID		= "BindingId";

	public static final Integer	DEFAULT_BINDING_ID	= new Integer(-1);

	// ------------------------------------------------

	private HashMap				bindings			= new HashMap();

	private HashMap				discoveryBindings	= new HashMap();

	// private static BindingProperties handler = null;
	//
	private static String		className			= null;

	// -------------------------------------------------

	BindingProperties() {
		super();
		// if (handler != null) {
		// throw new
		// RuntimeException("BindingProperties: class already instantiated!");
		// }
		className = this.getClass().getName();
		// handler = this;
	}

	public static BindingProperties getInstance() {
		// if (handler == null) {
		// handler = new BindingProperties();
		// }
		// return handler;
		return (BindingProperties) Properties.forClassName(Properties.BINDING_PROPERTIES_HANDLER_CLASS);
	}

	/**
	 * Returns class name, if object of this class was already created, else
	 * null.
	 * 
	 * @return Class name, if object of this class was already created, else
	 *         null.
	 */
	public static String getClassName() {
		return className;
	}

	// ----------------------------------------------------------------------

	public void setProperties(PropertyHeader header, Property property) {
		// no common binding properties
	}

	public void finishedSection(int depth) {
		// no common binding properties
	}

	void addCommunicationBinding(Integer bindingId, ArrayList binding) {
		bindings.put(bindingId, binding);
	}

	void addDiscoveryBinding(Integer bindingId, DiscoveryBinding binding) {
		discoveryBindings.put(bindingId, binding);
	}

	/**
	 * Returns a List with bindings.
	 * 
	 * @param bindingId List with Bindings
	 * @return
	 */
	public List getCommunicationBinding(Integer bindingId) {
		return (ArrayList) bindings.get(bindingId);
	}

	public CommunicationBinding getDiscoveryBinding(Integer bindingId) {
		return (CommunicationBinding) discoveryBindings.get(bindingId);
	}

	public String toString() {
		StringBuffer out = new StringBuffer(50 * bindings.size());

		for (Iterator it = bindings.entrySet().iterator(); it.hasNext();) {
			HashMap.Entry entry = (HashMap.Entry) it.next();
			out.append(entry.getKey() + "=" + entry.getValue() + " | ");
		}

		return out.toString();
	}

}
