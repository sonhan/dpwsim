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
 * Class representation for complexType:complexContent:restriction.
 */
public class RestrictedComplexContent extends ComplexContent {

	static final void handleRestriction(ElementParser parser, RestrictedComplexContent t, String targetNamespace, Schema schema) throws XmlPullParserException, IOException, SchemaException {
		String base = parser.getAttributeValue(null, ATTRIBUTE_BASE);
		if (base == null) {
			throw new SchemaException("Cannot restrict given type. No base type set.");
		}
		String p = SchemaUtil.getPrefix(base);
		String n = SchemaUtil.getName(base);
		String ns = parser.getNamespace(p);
		t.setBaseLink(new QName(n, ns));

		while (parser.nextTag() != XmlPullParser.END_TAG) {
			String namespace = parser.getNamespace();
			String name = parser.getName();
			if (XMLSCHEMA_NAMESPACE.equals(namespace)) {
				if (StringUtil.equalsIgnoreCase(ElementContainer.TAG_SEQUENCE, name)) {
					ElementContainer container = new SequenceContainer();
					ElementContainer.handleContainerElements(parser, container, targetNamespace, schema);
					t.container = container;
				} else if (StringUtil.equalsIgnoreCase(ElementContainer.TAG_CHOICE, name)) {
					ElementContainer container = new ChoiceContainer();
					ElementContainer.handleContainerElements(parser, container, targetNamespace, schema);
					t.container = container;
				} else if (StringUtil.equalsIgnoreCase(ElementContainer.TAG_ALL, name)) {
					ElementContainer container = new AllContainer();
					ElementContainer.handleContainerElements(parser, container, targetNamespace, schema);
					t.container = container;
				} else if (StringUtil.equalsIgnoreCase(Attribute.TAG_ATTRIBUTE, name)) {
					Attribute a = Attribute.createAttribute(parser, targetNamespace, schema);
					t.addAttribute(a);
				} else if (StringUtil.equalsIgnoreCase(AttributeGroup.TAG_ATTRIBUTEGROUP, name)) {
					AttributeGroup g = AttributeGroup.createAttributeGroup(parser, targetNamespace, schema);
					t.addAttributeGroup(g);
				} else if (StringUtil.equalsIgnoreCase(TAG_ANYATTRIBUTE, name)) {
					AnyAttribute a = AnyAttribute.createAnyAttribute(parser);
					t.setAnyAttribute(a);
				} else if (StringUtil.equalsIgnoreCase(Annotation.TAG_ANNOTATION, name)) {
					Annotation.handleAnnotation(parser, t);
				}
			}
		}
		if (t.getBaseLink() == null && t.getBase() == null) {
			throw new IOException("Cannot restrict given type. No base type set.");
		}
	}

	RestrictedComplexContent() {
		super();
	}

	RestrictedComplexContent(QName name) {
		super(name);
	}

	public RestrictedComplexContent(String name, String namespace, int containerType) {
		this(new QName(name, namespace), containerType);
	}

	public RestrictedComplexContent(QName name, int containerType) {
		super(name, containerType);
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.types.schema.ComplexType#getSchemaIdentifier()
	 */
	public int getSchemaIdentifier() {
		return XSD_RESTRICTEDCOMPLEXCONTENT;
	}

	void serialize(XmlSerializer serializer, Schema schema) throws IOException {
		serializer.startTag(XMLSCHEMA_NAMESPACE, TAG_COMPLEXTYPE);
		if (getName() != null) {
			serializer.attribute(null, ATTRIBUTE_NAME, getName().getLocalPart());
		}
		serializer.startTag(XMLSCHEMA_NAMESPACE, TAG_COMPLEXCONTENT);
		serializer.startTag(XMLSCHEMA_NAMESPACE, TAG_RESTRICTION);
		QName baseName = base.getName();
		schema.addBaseReference(base);
		String prefix = serializer.getPrefix(baseName.getNamespace(), false);
		if (!(prefix == null || "".equals(prefix))) {
			baseName.setPrefix(prefix);
			serializer.attribute(null, ATTRIBUTE_BASE, baseName.getLocalPartPrefixed());
		} else {
			serializer.attribute(null, ATTRIBUTE_BASE, baseName.getLocalPart());
		}
		serializeElements(serializer, schema);
		serializeAttributes(serializer, schema);
		serializeAttributeGroups(serializer, schema);
		serializeAnyAttribute(serializer, schema);

		serializer.endTag(XMLSCHEMA_NAMESPACE, TAG_RESTRICTION);
		serializer.endTag(XMLSCHEMA_NAMESPACE, TAG_COMPLEXCONTENT);
		serializer.endTag(XMLSCHEMA_NAMESPACE, TAG_COMPLEXTYPE);
	}

}
