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

import org.ws4d.java.structures.ArrayList;
import org.ws4d.java.structures.DataStructure;
import org.ws4d.java.structures.HashMap;
import org.ws4d.java.structures.Iterator;
import org.ws4d.java.structures.List;
import org.ws4d.java.util.WS4DIllegalStateException;

/**
 * 
 */
public class UnknownDataContainer {

	protected HashMap	unknownAttributes			= null;

	protected HashMap	unknownElements_QN_2_List	= null;

	/**
	 * Constructor
	 */
	public UnknownDataContainer() {
		super();
	}

	/**
	 * Copy Constructor.
	 */
	public UnknownDataContainer(UnknownDataContainer container) {
		if (container == null) {
			return;
		}

		if (container.unknownAttributes != null) {
			for (Iterator it = container.unknownAttributes.entrySet().iterator(); it.hasNext();) {
				HashMap.Entry entry = (HashMap.Entry) it.next();
				this.addUnknownAttribute((QName) entry.getKey(), (String) entry.getValue());
			}
		}
		if (container.unknownElements_QN_2_List != null) {
			for (Iterator it = container.unknownElements_QN_2_List.entrySet().iterator(); it.hasNext();) {
				HashMap.Entry entry = (HashMap.Entry) it.next();

				List elements = (List) entry.getValue();
				for (Iterator it_elem = elements.iterator(); it_elem.hasNext();) {
					this.addUnknownElement((QName) entry.getKey(), it_elem.next());
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("UnknownDataContainer attributes=").append(unknownAttributes);
		sb.append(", elements=").append(unknownElements_QN_2_List);
		return sb.toString();
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.ws4d.java.types.xml.XMLContainer#getUnknownAttribute(org.ws4d.java
	 * .data.QName)
	 */
	public String getUnknownAttribute(QName elementName) {
		return unknownAttributes == null ? null : (String) unknownAttributes.get(elementName);
	}

	/**
	 * Returns the <strong>first</strong> object with the given element name.
	 * 
	 * @param elementName element name
	 * @return the first object of given element name
	 */
	public Object getUnknownElement(QName elementName) {
		if (unknownElements_QN_2_List == null) {
			return null;
		}

		List elements = (List) unknownElements_QN_2_List.get(elementName);
		if (elements == null) {
			return null;
		}

		return elements.get(0);
	}

	/**
	 * Gets list of elements with given name.
	 * 
	 * @param elementName element name
	 * @return List includes each java object linked to the given element name.
	 */
	public List getUnknownElements(QName elementName) {
		if (unknownElements_QN_2_List == null) {
			return null;
		}

		return (List) unknownElements_QN_2_List.get(elementName);
	}

	/**
	 * Returns a map containing all unknown attributes of this XML container.
	 * The keys within the map are of type {@link QName} and reflect the
	 * qualified names of the attributes, whereas the values represent the
	 * corresponding attribute values as string.
	 * 
	 * @return a map of all unknown attributes
	 */
	public HashMap getUnknownAttributes() {
		// TODO return a clone or an unmodifiable view??
		return unknownAttributes;
	}

	/**
	 * Returns a map containing all unknown elements of this XML container. The
	 * keys within the map are of type {@link QName} and reflect the qualified
	 * names of the elements whereas the values are of type {@link List} and
	 * contain an entry for each child element with the same qualified name.
	 * 
	 * @return a map of all unknown elements
	 */
	public HashMap getUnknownElements() {
		// TODO return a clone or an unmodifiable view??
		return unknownElements_QN_2_List;
	}

	/**
	 * @param attributeName the name of the unknown attribute
	 * @param value the attribute's value represented as a Java string
	 */
	public void addUnknownAttribute(QName attributeName, String value) {
		if (unknownAttributes == null) {
			unknownAttributes = new HashMap();
		}
		unknownAttributes.put(attributeName, value);
	}

	/**
	 * @param elementName the name of the unknown element
	 * @param element the element represented as a Java object
	 */
	public void addUnknownElement(QName elementName, Object element) {
		if (unknownElements_QN_2_List == null) {
			unknownElements_QN_2_List = new HashMap();
		}

		DataStructure elements = (DataStructure) unknownElements_QN_2_List.get(elementName);
		if (elements == null) {
			elements = new ArrayList();
			unknownElements_QN_2_List.put(elementName, elements);
		}
		elements.add(element);
	}

	/**
	 * @param attributes
	 */
	public void setUnknownAttributes(HashMap attributes) {
		if (unknownAttributes != null) {
			throw new WS4DIllegalStateException("unknownAttributes already exist");
		}
		unknownAttributes = new HashMap(attributes);
	}

	/**
	 * @param elements
	 */
	public void setUnknownElements(HashMap elements) {
		if (unknownElements_QN_2_List != null) {
			throw new WS4DIllegalStateException("unknownElements already exist");
		}
		unknownElements_QN_2_List = new HashMap(elements);
	}

}
