/*******************************************************************************
 * Copyright (c) 2009 MATERNA Information & Communications. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html. For further
 * project-related information visit http://www.ws4d.org. The most recent
 * version of the JMEDS framework can be obtained from
 * http://sourceforge.net/projects/ws4d-javame.
 ******************************************************************************/
package org.ws4d.java.io.xml.cache;

import java.io.IOException;

import org.ws4d.java.io.xml.XmlSerializer;
import org.ws4d.java.io.xml.canonicalization.CanonicalSerializer;
import org.xmlpull.v1.IllegalStateException;

public class XmlAttribute implements XmlStructure {

	String	namespace;

	String	name;

	String	value;

	public XmlAttribute(String a, String b, String c) {
		namespace = a;
		name = b;
		value = c;
	}

	public void flush(CanonicalSerializer serializer) throws IllegalArgumentException, IllegalStateException, IOException {
		serializer.attributeo(namespace, name, value);
	}

	public void flush(XmlSerializer serializer) throws IllegalArgumentException, IllegalStateException, IOException {
		serializer.attribute(namespace, name, value);
	}

	public int getType() {
		return XmlStructure.XML_ATTRIBUTE;
	}

	public String getName() {
		return name;
	}

	public String getNamespace() {
		return namespace;
	}

	public String getValue() {
		return value;
	}

	public void setNameSpace(String ns) {
		namespace = ns;
	}
}
