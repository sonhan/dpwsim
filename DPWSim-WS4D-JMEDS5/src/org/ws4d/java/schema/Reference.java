/*******************************************************************************
 * Copyright (c) 2009 MATERNA Information & Communications. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html. For further
 * project-related information visit http://www.ws4d.org. The most recent
 * version of the JMEDS framework can be obtained from
 * http://sourceforge.net/projects/ws4d-javame.
 ******************************************************************************/
package org.ws4d.java.schema;

import org.ws4d.java.types.QName;

/**
 * Abstract class for reference handling.
 */
abstract class Reference extends NamedObject {

	static final String	ATTRIBUTE_REF	= SCHEMA_REF;

	protected QName		referenceLink	= null;

	protected Reference	reference		= null;

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.types.schema.NamedObject#getName()
	 */
	public QName getName() {
		if (referenceLink != null) return referenceLink;
		if (reference != null) return reference.getName();
		return name;
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.schema.NamedObject#setName(org.ws4d.java.types.QName)
	 */
	public void setName(QName name) {
		if (isReference()) {
			reference.setName(name);
		} else {
			super.setName(name);
		}
	}

	/**
	 * Returns <code>true</code> if the element is a reference for another
	 * schema object, <code>false</code> otherwise.
	 * 
	 * @return <code>true</code> if the element is a reference for another
	 *         schema object, <code>false</code> otherwise.
	 */
	public boolean isReference() {
		return (reference != null);
	}

	public Reference getReference() {
		return reference;
	}

	void setReferenceLink(QName reference) {
		this.referenceLink = reference;
	}

	QName getReferenceLink() {
		return referenceLink;
	}

	void setReference(Reference reference) {
		referenceLink = null;
		this.reference = reference;
	}

}
