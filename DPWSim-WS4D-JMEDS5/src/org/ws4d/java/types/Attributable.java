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
 * A simple annotation-like interface which adds arbitrary XML attribute support
 * to a given structure.
 */
public interface Attributable {

	/**
	 * Returns the value of the attribute with the given <code>name</code> or
	 * <code>null</code>, if this attribute is not available (or if its value is
	 * actually explicitly set to <code>null</code>).
	 * 
	 * @param name the name of the attribute of which to query the value
	 * @return the value of the named attribute or <code>null</code>
	 */
	public CustomAttributeValue getAttribute(QName name);

	/**
	 * Sets the <code>value</code> for the attribute with the specified
	 * <code>name</code>. Throws a
	 * <code>java.lang.IllegalArgumentException</code> in case <code>name</code>
	 * is <code>null</code>.
	 * 
	 * @param name the name of the attribute to set, must not be
	 *            <code>null</code>
	 * @param value the value to set the named attribute to (may be
	 *            <code>null</code>
	 * @throws IllegalArgumentException if <code>name</code> is
	 *             <code>null</code>
	 */
	public void setAttribute(QName name, CustomAttributeValue value);

	/**
	 * Sets the <code>value</code> for the attribute with the specified
	 * <code>name</code>. The value will be represented as plain String. It will
	 * be wrapped within a new instance of {@link StringAttributeValue}. This
	 * method throws a <code>java.lang.IllegalArgumentException</code> in case
	 * <code>name</code> is <code>null</code>.
	 * <p>
	 * This is a shorthand for
	 * <code>setAttribute(name, new StringAttributeValue(value))</code>.
	 * </p>
	 * 
	 * @param name the name of the attribute to set, must not be
	 *            <code>null</code>
	 * @param value the value to set the named attribute to (may be
	 *            <code>null</code>
	 * @throws IllegalArgumentException if <code>name</code> is
	 *             <code>null</code>
	 */
	public void setAttribute(QName name, String value);

	/**
	 * Returns all attributes explicitly set for this {@link Attributable}
	 * instance. Note that depending on the actual implementation the returned
	 * reference may point at the 'life map', i .e. the actual storage for the
	 * attributes. Thus, modifications to that map should be performed with care
	 * and keeping this in mind.
	 * 
	 * @return all already set attributes
	 */
	public HashMap getAttributes();

	/**
	 * Sets all attributes at once to those contained within argument
	 * <code>attributes</code>. Note that depending on the actual implementation
	 * it is possible that the map <code>attributes</code> points at may be used
	 * for the actual internal storage of the attributes (i.e. without copying
	 * it). That is why, after passing it to this method, modifications to this
	 * map should be made with care. This method throws a
	 * <code>java.lang.IllegalArgumentException</code> in cases where
	 * <code>attributes</code> is <code>null</code>.
	 * 
	 * @param attributes the new attributes to set
	 * @throws IllegalArgumentException if <code>attributes</code> is
	 *             <code>null</code>
	 */
	public void setAttributes(HashMap attributes);

	/**
	 * Returns <code>true</code> only if this instance has at least one
	 * attribute set. Returns <code>false</code> in any other case.
	 * 
	 * @return <code>true</code> only if there is at least one attribute set
	 *         within this instance
	 */
	public boolean hasAttributes();

	/**
	 * Serializes the attributes stored within this instance, if any.
	 * 
	 * @param serializer the serializer to which to send output
	 * @throws IOException in case writing to <code>serializer</code> fails for
	 *             any reason
	 */
	public void serializeAttributes(XmlSerializer serializer) throws IOException;

}
