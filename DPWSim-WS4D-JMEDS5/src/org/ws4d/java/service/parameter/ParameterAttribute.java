/*******************************************************************************
 * Copyright (c) 2009 MATERNA Information & Communications. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html. For further
 * project-related information visit http://www.ws4d.org. The most recent
 * version of the JMEDS framework can be obtained from
 * http://sourceforge.net/projects/ws4d-javame.
 ******************************************************************************/
package org.ws4d.java.service.parameter;

import org.ws4d.java.schema.Type;
import org.ws4d.java.types.QName;

/**
 * This class represents an XML attribute in an XML instance document.
 * <p>
 * It will be used for the internal representation in the {@link ParameterValue}
 * .
 * </p>
 */
public class ParameterAttribute {

	protected QName		name	= null;

	protected String	value	= null;

	protected Type		type	= null;

	ParameterAttribute(QName name) {
		this.name = name;
	}

	/**
	 * Returns the name of the parameter attribute. The name of the parameter
	 * attribute is the name of the entry inside the XML document.
	 * 
	 * @return the parameter attribute name
	 */
	public QName getName() {
		return name;
	}

	/**
	 * Sets the value of this parameter attribute.
	 * 
	 * @param value the value to set.
	 */
	public void setValue(String value) {
		this.value = value;
	}

	/**
	 * Returns the value of this parameter attribute.
	 * 
	 * @return the value.
	 */
	public String getValue() {
		return this.value;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return "PA [ name=" + name + ", value=" + value + " ]";
	}

	/**
	 * Returns the type of this parameter attribute.
	 * 
	 * @return the parameter attribute.
	 */
	public Type getType() {
		return type;
	}

	void setType(Type type) {
		this.type = type;
	}

}
