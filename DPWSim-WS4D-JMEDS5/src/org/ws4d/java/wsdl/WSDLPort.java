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

import java.io.IOException;

import org.ws4d.java.structures.DataStructure;
import org.ws4d.java.structures.EmptyStructures;
import org.ws4d.java.types.QName;
import org.xmlpull.v1.XmlSerializer;

/**
 * 
 */
public abstract class WSDLPort {

	private WSDLService	service;

	private String		name;

	private QName		bindingName;

	/**
	 * 
	 */
	public WSDLPort() {
		this(null);
	}

	/**
	 * @param name
	 */
	public WSDLPort(String name) {
		this(name, null);
	}

	/**
	 * @param name
	 * @param bindingName the fully qualified name of the binding to which this
	 *            port refers
	 */
	public WSDLPort(String name, QName bindingName) {
		super();
		this.name = name;
		this.bindingName = bindingName;
	}

	public abstract void serializePortExtension(XmlSerializer serializer) throws IOException;

	public DataStructure getOperations() {
		WSDLPortType portType = getPortType();
		return portType == null ? EmptyStructures.EMPTY_STRUCTURE : portType.getOperations();
	}

	public WSDLPortType getPortType() {
		WSDLBinding binding = getBinding();
		return binding == null ? null : binding.getPortType();
	}

	public WSDLBinding getBinding() {
		WSDL wsdl = getWsdl();
		return wsdl == null ? null : wsdl.getBinding(bindingName);
	}

	public WSDL getWsdl() {
		return service == null ? null : service.getWsdl();
	}

	/**
	 * Returns the namespace within which this port resides. This is the
	 * namespace of the surrounding service if one exists, or <code>null</code>
	 * otherwise.
	 * 
	 * @return the namespace of the service of this port, or <code>null</code>
	 *         if service not set
	 */
	public String getNamespace() {
		return service == null ? null : service.getNamespace();
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the bindingName
	 */
	public QName getBindingName() {
		return bindingName;
	}

	/**
	 * @param bindingName the bindingName to set
	 */
	public void setBindingName(QName bindingName) {
		this.bindingName = bindingName;
	}

	/**
	 * @return the service
	 */
	public WSDLService getService() {
		return service;
	}

	/**
	 * @param service the service to set
	 */
	void setService(WSDLService service) {
		this.service = service;
	}

}
