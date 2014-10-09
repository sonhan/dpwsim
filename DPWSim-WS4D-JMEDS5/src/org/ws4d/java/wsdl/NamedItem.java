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

import org.ws4d.java.types.QName;

/**
 * 
 */
public abstract class NamedItem {

	protected QName	name;

	/**
	 * 
	 */
	public NamedItem() {
		this(null);
	}

	public NamedItem(QName name) {
		super();
		this.name = name;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("name=").append(getName());
		return sb.toString();
	}

	public String getNamespace() {
		QName name = getName();
		return name == null ? null : name.getNamespace();
	}

	public String getLocalName() {
		QName name = getName();
		return name == null ? null : name.getLocalPart();
	}

	/**
	 * @return the name
	 */
	public QName getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(QName name) {
		this.name = name;
	}

}
