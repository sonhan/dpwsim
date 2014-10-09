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
import org.ws4d.java.structures.HashMap.Entry;
import org.ws4d.java.structures.Iterator;
import org.xmlpull.v1.XmlSerializer;

/**
 * A standard implementation of the {@link Attributable} interface.
 */
public class AttributableSupport implements Attributable {

	protected HashMap	attributes;

	/**
	 * Default constructor.
	 */
	public AttributableSupport() {
		super();
	}

	/**
	 * Creates a new instance with the given <code>attributes</code>.
	 */
	public AttributableSupport(HashMap attributes) {
		super();
		this.attributes = attributes;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return attributes == null ? "{}" : attributes.toString();
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		AttributableSupport other = (AttributableSupport) obj;
		if (attributes == null) {
			if (other.attributes != null) {
				return false;
			}
		} else if (!attributes.equals(other.attributes)) {
			return false;
		}
		return true;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((attributes == null) ? 0 : attributes.hashCode());
		return result;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.ws4d.java.wsdl.Attributable#getAttribute(org.ws4d.java.types.QName)
	 */
	public CustomAttributeValue getAttribute(QName name) {
		return attributes == null ? null : (CustomAttributeValue) attributes.get(name);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.ws4d.java.wsdl.Attributable#setAttribute(org.ws4d.java.types.QName,
	 * java.lang.String)
	 */
	public void setAttribute(QName name, CustomAttributeValue value) {
		if (name == null) {
			throw new IllegalArgumentException("name is null");
		}
		if (attributes == null) {
			attributes = new HashMap();
		}
		attributes.put(name, value);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.ws4d.java.types.Attributable#setAttribute(org.ws4d.java.types.QName,
	 * java.lang.String)
	 */
	public void setAttribute(QName name, String value) {
		setAttribute(name, new StringAttributeValue(value));
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.wsdl.Attributable#getAttributes()
	 */
	public HashMap getAttributes() {
		if (attributes == null) {
			/*
			 * we do this in order to enable a caller to put attributes directly
			 * into the returned map
			 */
			attributes = new HashMap();
		}
		return attributes;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.ws4d.java.wsdl.Attributable#setAttributes(org.ws4d.java.structures
	 * .HashMap)
	 */
	public void setAttributes(HashMap attributes) {
		this.attributes = attributes;
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.types.Attributable#hasAttributes()
	 */
	public boolean hasAttributes() {
		return attributes != null && !attributes.isEmpty();
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.types.Attributable#serializeAttributes(org.xmlpull.v1.
	 * XmlSerializer)
	 */
	public void serializeAttributes(XmlSerializer serializer) throws IOException {
		if (attributes == null) {
			return;
		}
		for (Iterator it = attributes.entrySet().iterator(); it.hasNext();) {
			Entry ent = (Entry) it.next();
			QName name = (QName) ent.getKey();
			CustomAttributeValue value = (CustomAttributeValue) ent.getValue();
			if (value != null) {
				value.serialize(serializer, name);
			}
		}
	}

}
