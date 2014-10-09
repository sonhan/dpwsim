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

import org.ws4d.java.io.xml.ElementParser;
import org.ws4d.java.util.StringUtil;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

/**
 * Class representation for schema:notation.
 */
public class Notation extends Annotation {

	static final String		TAG_NOTATION		= SCHEMA_NOTATION;

	static final String		ATTRIBUTE_PUBLIC	= SCHEMA_PUBLIC;

	static final String		ATTRIBUTE_SYSTEM	= SCHEMA_SYSTEM;

	protected static int	count				= 0;

	protected String		name				= null;

	protected String		pub					= null;

	protected String		sys					= null;

	static final Notation createNotation(ElementParser parser) throws XmlPullParserException, IOException, SchemaException {
		String nName = parser.getAttributeValue(null, ATTRIBUTE_NAME);
		if (nName == null) {
			throw new SchemaException("Cannot create notation. No name set.");
		}
		Notation n = new Notation(nName);

		String pub = parser.getAttributeValue(null, ATTRIBUTE_PUBLIC);
		if (pub != null) {
			n.setPublic(pub);
		}

		String sys = parser.getAttributeValue(null, ATTRIBUTE_SYSTEM);
		if (sys != null) {
			n.setSystem(sys);
		}

		int d = parser.getDepth();
		while (parser.nextTag() != XmlPullParser.END_TAG && parser.getDepth() == d + 1) {
			String namespace = parser.getNamespace();
			String name = parser.getName();
			if (XMLSCHEMA_NAMESPACE.equals(namespace)) {
				if (StringUtil.equalsIgnoreCase(TAG_ANNOTATION, name)) {
					Annotation.handleAnnotation(parser, n);
				}
			}
		}
		return n;
	}

	Notation(String name) {
		this.name = name;
		if (name != null) {
			count++;
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.types.schema.Annotation#getSchemaIdentifier()
	 */
	public int getSchemaIdentifier() {
		return XSD_NOTATION;
	}

	public String getName() {
		return name;
	}

	public String getPublic() {
		return pub;
	}

	public String getSystem() {
		return sys;
	}

	public String getSchemaElementName() {
		return TAG_NOTATION;
	}

	boolean isResolved() {
		return true;
	}

	void setPublic(String pub) {
		this.pub = pub;
	}

	void setSystem(String sys) {
		this.sys = sys;
	}

}
