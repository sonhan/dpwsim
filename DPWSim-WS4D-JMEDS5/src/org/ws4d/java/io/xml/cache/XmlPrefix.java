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

public class XmlPrefix implements XmlStructure {

	private int	type	= XmlStructure.XML_PREFIX;

	String		name;

	String		namespace;

	public XmlPrefix(String name, String namespace) {
		this.name = name;
		this.namespace = namespace;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.ws4d.java.io.xml.XmlStructure#flush(org.ws4d.java.io.xml.XmlSerializer
	 * )
	 */
	public void flush(CanonicalSerializer serializer) throws IllegalArgumentException, IllegalStateException, IOException {
		serializer.setPrefixo(name, namespace);
	}

	public void flush(XmlSerializer serializer) throws IllegalArgumentException, IllegalStateException, IOException {
		serializer.setPrefix(name, namespace);
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.io.xml.XmlStructure#getType()
	 */
	public int getType() {
		return type;
	}

	public String getName() {
		return name;
	}

	public String getNamespace() {
		return namespace;
	}

	public String getValue() {
		return namespace;
	}

	public void setNameSpace(String ns) {
		namespace = ns;
	}

}
