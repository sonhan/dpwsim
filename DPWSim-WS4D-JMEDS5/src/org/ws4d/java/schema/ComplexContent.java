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
import org.ws4d.java.types.QName;
import org.ws4d.java.util.StringUtil;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

/**
 * Complex content representation.
 */
public abstract class ComplexContent extends ComplexType implements InheritType {

	static final String	TAG_COMPLEXCONTENT	= SCHEMA_COMPLEXCONTENT;

	protected Type		base				= null;

	protected QName		baseLink			= null;

	static final ComplexContent handleComplexContent(ElementParser parser, QName typeName, String targetNamespace, Schema schema) throws XmlPullParserException, IOException, SchemaException {
		int d = parser.getDepth();
		ComplexContent complexContent = null;
		while (parser.nextTag() != XmlPullParser.END_TAG && parser.getDepth() == d + 1) {
			String namespace = parser.getNamespace();
			String name = parser.getName();
			if (XMLSCHEMA_NAMESPACE.equals(namespace)) {
				if (StringUtil.equalsIgnoreCase(TAG_EXTENSION, name)) {
					ExtendedComplexContent extended = new ExtendedComplexContent(typeName);
					ExtendedComplexContent.handleExtension(parser, extended, targetNamespace, schema);
					complexContent = extended;
				} else if (StringUtil.equalsIgnoreCase(TAG_RESTRICTION, name)) {
					RestrictedComplexContent restricted = new RestrictedComplexContent(typeName);
					RestrictedComplexContent.handleRestriction(parser, restricted, targetNamespace, schema);
					complexContent = restricted;
				}
			}
		}
		return complexContent;
	}

	ComplexContent() {
		super();
	}

	ComplexContent(QName name, int containerType) {
		super(name, containerType);
	}

	ComplexContent(QName name) {
		super(name);
	}

	public Type getBase() {
		return base;
	}

	public void setBase(Type base) {
		baseLink = null;
		this.base = base;
		base.addSubtype(this);
	}

	QName getBaseLink() {
		return baseLink;
	}

	void setBaseLink(QName link) {
		this.baseLink = link;
	}

}
