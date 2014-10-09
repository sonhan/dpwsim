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
import org.xmlpull.v1.XmlSerializer;

/**
 * Simple content representation.
 */
public abstract class SimpleContent extends SimpleType implements InheritType {

	static final String	TAG_SIMPLECONTENT	= SCHEMA_SIMPLECONTENT;

	protected Type		base				= null;

	protected QName		baseLink			= null;

	static final SimpleContent handleSimpleContent(ElementParser parser, QName typeName, String targetNamespace, Schema schema) throws XmlPullParserException, IOException, SchemaException {
		int d = parser.getDepth();
		SimpleContent simpleContent = null;
		while (parser.nextTag() != XmlPullParser.END_TAG && parser.getDepth() == d + 1) {
			String namespace = parser.getNamespace();
			String name = parser.getName();
			if (XMLSCHEMA_NAMESPACE.equals(namespace)) {
				if (StringUtil.equalsIgnoreCase(TAG_EXTENSION, name)) {
					ExtendedSimpleContent extended = new ExtendedSimpleContent(typeName);
					ExtendedSimpleContent.handleExtension(parser, extended, targetNamespace, schema);
					simpleContent = extended;
				} else if (StringUtil.equalsIgnoreCase(TAG_RESTRICTION, name)) {
					RestrictedSimpleContent restricted = new RestrictedSimpleContent(typeName);
					RestrictedSimpleContent.handleRestriction(parser, restricted, targetNamespace, schema);
					simpleContent = restricted;
				}
			}
		}
		return simpleContent;
	}

	SimpleContent() {
		super();
	}

	SimpleContent(QName name) {
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

	protected void serializeAnyAttribute(XmlSerializer serializer, Schema schema) throws IOException {
		if (anyAttribute != null) {
			anyAttribute.serialize(serializer, schema);
		}
	}

	QName getBaseLink() {
		return baseLink;
	}

	void setBaseLink(QName link) {
		this.baseLink = link;
	}

}
