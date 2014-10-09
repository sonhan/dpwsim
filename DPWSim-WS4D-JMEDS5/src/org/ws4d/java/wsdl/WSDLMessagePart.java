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

import org.ws4d.java.schema.Element;
import org.ws4d.java.schema.Type;
import org.ws4d.java.types.QName;
import org.ws4d.java.util.StringUtil;

/**
 * Implementation of the WSDL 1.1 Message Part.<br />
 * WSDL 1.1, 2.3.1 Message Parts
 */
public class WSDLMessagePart {

	public static final String	DEFAULT_PART_NAME			= "parameters";

	public static final String	DEFAULT_INPUT_PART_NAME		= DEFAULT_PART_NAME;

	public static final String	DEFAULT_OUTPUT_PART_NAME	= DEFAULT_PART_NAME;

	private WSDLMessage			message;

	private String				name;

	private QName				ref							= null;

	private boolean				refIsElement;

	/**
	 * Creates a message part with a default local name.
	 * 
	 * @see #DEFAULT_PART_NAME
	 */
	public WSDLMessagePart() {
		this(DEFAULT_PART_NAME);
	}

	/**
	 * Creates a message part with a given name.
	 * 
	 * @param name the name of the part.
	 */
	public WSDLMessagePart(String name) {
		super();
		this.name = name;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		StringBuffer sb = new StringBuffer(StringUtil.formatClassName(getClass()));
		sb.append("[ name=" + name);
		if (refIsElement) {
			sb.append(", element=");
		} else {
			sb.append(", type=");
		}
		sb.append(ref);
		sb.append(" ]");
		return sb.toString();
	}

	public WSDL getWsdl() {
		return message == null ? null : message.getWsdl();
	}

	public Element getElement() {
		if (!refIsElement) {
			return null;
		}
		WSDL wsdl = getWsdl();
		if (wsdl == null) {
			return null;
		}
		// return schema element with given name
		return wsdl.getSchemaElement(ref);
	}

	public Type getType() {
		if (refIsElement) {
			return null;
		}
		WSDL wsdl = getWsdl();
		if (wsdl == null) {
			return null;
		}
		// return schema type with given name
		return wsdl.getSchemaType(ref);
	}

	/**
	 * Returns the namespace within which this message part resides. This is the
	 * namespace of the surrounding message if one exists, or <code>null</code>
	 * otherwise.
	 * 
	 * @return the namespace of the surrounding message of this part, or
	 *         <code>null</code> if message not set
	 */
	public String getNamespace() {
		return message == null ? null : message.getNamespace();
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

	public void setElementName(QName elementName) {
		this.ref = elementName;
		this.refIsElement = true;
	}

	public void setTypeName(QName typeName) {
		this.ref = typeName;
		this.refIsElement = false;
	}

	public QName getElementName() {
		return refIsElement ? ref : null;
	}

	public QName getTypeName() {
		return refIsElement ? null : ref;
	}

	/**
	 * Returns <code>true</code> if this message part refers to a schema
	 * element, <code>false</code> if it refers to a schema type.
	 * 
	 * @return <code>true</code> if this message part refers to a schema
	 *         element, <code>false</code> if it refers to a schema type
	 */
	public boolean isElement() {
		return refIsElement;
	}

	/**
	 * @return the message
	 */
	public WSDLMessage getMessage() {
		return message;
	}

	/**
	 * @param message the message to set
	 */
	void setMessage(WSDLMessage message) {
		this.message = message;
	}

}
