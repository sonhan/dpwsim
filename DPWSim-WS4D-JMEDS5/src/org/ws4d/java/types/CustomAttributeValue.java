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
 * A generic container for the value of a custom attribute. Allows custom
 * serialization of the attribute value. Also, provides namespaces which should
 * be declared in the scope surrounding this attribute value.
 */
public interface CustomAttributeValue {

	/**
	 * Returns the namespaces used within this attribute value. These namespaces
	 * should be declared appropriately on an enclosing element. Each namespace
	 * is stored as String key within the hash map. The corresponding value is a
	 * recommended prefix (as String) for this namespace. It will be declared
	 * for use with that namespace, if it is available within the scope of this
	 * attribute (i.e. not associated with a different namespace).
	 * 
	 * @return the namespaces used within this attribute
	 */
	public HashMap getNamespaces();

	/**
	 * Returns the value of the attribute.
	 * 
	 * @return this attribute's value
	 */
	public Object getValue();

	/**
	 * Serializes this attribute value using the provided {@link XmlSerializer}.
	 * The value is assigned to an attribute with the specified qualified name
	 * <code>attributeName</code>.
	 * 
	 * @param serializer the serializer to write the attribute value to
	 * @param attributeName the name of the attribute to assign this attribute
	 *            value to
	 * @throws IOException if an error occurs during writing to the serializer
	 */
	public void serialize(XmlSerializer serializer, QName attributeName) throws IOException;

}
