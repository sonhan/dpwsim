/*******************************************************************************
 * Copyright (c) 2009 MATERNA Information & Communications. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html. For further
 * project-related information visit http://www.ws4d.org. The most recent
 * version of the JMEDS framework can be obtained from
 * http://sourceforge.net/projects/ws4d-javame.
 ******************************************************************************/
package org.ws4d.java.types;

import java.io.IOException;

import org.ws4d.java.structures.HashMap;
import org.xmlpull.v1.XmlSerializer;

/**
 * Custom attribute value representation as a plain string.
 */
public class StringAttributeValue implements CustomAttributeValue {

	private String	value;

	/**
	 * @param value
	 */
	public StringAttributeValue(String value) {
		this.value = value;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return value;
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.types.CustomAttributeValue#getNamespaces()
	 */
	public HashMap getNamespaces() {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.types.CustomAttributeValue#getValue()
	 */
	public Object getValue() {
		return value;
	}

	/**
	 * Sets the value.
	 * 
	 * @param value the new value
	 */
	public void setValue(String value) {
		this.value = value;
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.types.CustomAttributeValue#serialize(org.xmlpull.v1.
	 * XmlSerializer)
	 */
	public void serialize(XmlSerializer serializer, QName attributeName) throws IOException {
		String ns = attributeName.getNamespace();
		if ("".equals(ns)) {
			ns = null;
		}
		serializer.attribute(ns, attributeName.getLocalPart(), value == null ? "" : value);
	}

}
