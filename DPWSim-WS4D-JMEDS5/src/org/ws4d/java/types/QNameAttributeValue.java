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
 * Representation of an attribute value as a {@link QName qualified name}.
 */
public class QNameAttributeValue implements CustomAttributeValue {

	private QName	value;

	private HashMap	namespaces	= null;

	/**
	 * 
	 */
	public QNameAttributeValue() {}

	/**
	 * Creates a new instance with the specified value.
	 * 
	 * @param value the attribute's value
	 */
	public QNameAttributeValue(QName value) {
		setValue(value);
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return value == null ? null : value.toString();
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.types.CustomAttributeValue#getNamespaces()
	 */
	public HashMap getNamespaces() {
		return namespaces;
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.types.CustomAttributeValue#getValue()
	 */
	public Object getValue() {
		return value;
	}

	/**
	 * Sets the value for this attribute. If the attribute had a previous value,
	 * its namespace will be removed from the namespaces to be declared for this
	 * attribute instance. Consequently, if the new value is not null, its
	 * namespace will be added to the namespaces to declare in the surrounding
	 * scope of this attribute.
	 * 
	 * @param value the new value for the attribute
	 */
	public void setValue(QName value) {
		if (this.value != null) {
			if (namespaces != null) {
				namespaces.remove(this.value.getNamespace());
			}
		}
		this.value = value;
		if (value != null) {
			if (namespaces == null) {
				namespaces = new HashMap();
			}
			String ns = value.getNamespace();
			if (!"".equals(ns)) { // ignore default namespace
				namespaces.put(ns, value.getPrefix());
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.types.CustomAttributeValue#serialize(org.xmlpull.v1.
	 * XmlSerializer, org.ws4d.java.types.QName)
	 */
	public void serialize(XmlSerializer serializer, QName attributeName) throws IOException {
		String attributeNs = attributeName.getNamespace();
		if ("".equals(attributeNs)) {
			attributeNs = null;
		}
		if (value != null) {
			String prefix;
			String ns = value.getNamespace();
			if ("".equals(ns)) {
				prefix = null;
			} else {
				prefix = serializer.getPrefix(ns, true);
			}
			serializer.attribute(attributeNs, attributeName.getLocalPart(), prefix == null ? value.getLocalPart() : (prefix + ':' + value.getLocalPart()));
		} else {
			serializer.attribute(attributeNs, attributeName.getLocalPart(), "");
		}
	}

}
