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

import org.ws4d.java.structures.ArrayList;
import org.ws4d.java.structures.DataStructure;
import org.ws4d.java.structures.EmptyStructures;
import org.ws4d.java.structures.HashMap;
import org.ws4d.java.structures.Iterator;
import org.ws4d.java.structures.LinkedMap;
import org.ws4d.java.structures.List;
import org.ws4d.java.types.QName;

/**
 * 
 */
public class WSDLService extends NamedItem {

	private WSDL	wsdl;

	// key = local name of port as String, value = WSDLPort instance
	private HashMap	ports;

	/**
	 * 
	 */
	public WSDLService() {
		this(null);
	}

	/**
	 * @param name
	 */
	public WSDLService(QName name) {
		super(name);
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("[ ");
		sb.append(super.toString());
		sb.append(", ports=").append(ports);
		sb.append(" ]");
		return sb.toString();
	}

	/**
	 * Returns a data structure containing the operations of all port this
	 * service comprises.
	 * 
	 * @return a data structure containing all operations within the ports of
	 *         this service
	 */
	public DataStructure getOperations() {
		if (ports == null) {
			return EmptyStructures.EMPTY_STRUCTURE;
		}
		List l = new ArrayList();
		for (Iterator it = ports.values().iterator(); it.hasNext();) {
			WSDLPort port = (WSDLPort) it.next();
			l.addAll(port.getOperations());
		}
		return l;
	}

	public DataStructure getPortTypes() {
		if (ports == null) {
			return EmptyStructures.EMPTY_STRUCTURE;
		}
		List l = new ArrayList(ports.size());
		for (Iterator it = ports.values().iterator(); it.hasNext();) {
			WSDLPort port = (WSDLPort) it.next();
			WSDLPortType portType = port.getPortType();
			if (portType != null) {
				l.add(portType);
			}
		}
		return l;
	}

	/**
	 * Adds a port to this service.
	 * 
	 * @param port the port to add
	 */
	public void addPort(WSDLPort port) {
		if (port == null) {
			return;
		}
		if (ports == null) {
			ports = new LinkedMap();
		}
		ports.put(port.getName(), port);
		port.setService(this);
	}

	/**
	 * @param name the local name of the port to return
	 * @return the named port or <code>null</code>
	 */
	public WSDLPort getPort(String name) {
		return ports == null ? null : (WSDLPort) ports.get(name);
	}

	/**
	 * Returns a <code>DataStructure</code> containing all ports from within
	 * this service.
	 * 
	 * @return a <code>DataStructure</code> containing all ports from this
	 *         service
	 */
	public DataStructure getPorts() {
		return ports == null ? EmptyStructures.EMPTY_STRUCTURE : new ArrayList(ports.values());
	}

	public boolean containsPortsForBinding(QName bindingName) {
		if (ports == null) {
			return false;
		}
		for (Iterator it = ports.values().iterator(); it.hasNext();) {
			WSDLPort port = (WSDLPort) it.next();
			if (bindingName.equals(port.getBindingName())) {
				return true;
			}
		}
		return false;
	}

	/**
	 * @return the wsdl
	 */
	public WSDL getWsdl() {
		return wsdl;
	}

	/**
	 * @param wsdl the wsdl to set
	 */
	void setWsdl(WSDL wsdl) {
		this.wsdl = wsdl;
	}

}
