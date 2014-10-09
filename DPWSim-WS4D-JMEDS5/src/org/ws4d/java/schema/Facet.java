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

import java.io.IOException;

import org.ws4d.java.constants.SchemaConstants;
import org.ws4d.java.types.QName;
import org.xmlpull.v1.XmlSerializer;

/**
 * Class for facet handling.
 */
public class Facet implements SchemaConstants {

	static final String	ATTRIBUTE_VALUE	= SCHEMA_VALUE;

	private QName		name			= null;

	private String		value			= null;

	public Facet(QName name) {
		this.name = name;
	}

	public Facet(QName name, String value) {
		this.name = name;
		this.value = value;
	}

	public String toString() {
		return "Facet [ name=" + name.getLocalPart() + ", value=" + value + " ]";
	}

	public QName getName() {
		return name;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	void serialize(XmlSerializer serializer) throws IOException {
		serializer.startTag(this.getName().getNamespace(), this.getName().getLocalPart());
		serializer.attribute(null, ATTRIBUTE_VALUE, this.getValue());
		serializer.endTag(this.getName().getNamespace(), this.getName().getLocalPart());
	}

}
