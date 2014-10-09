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
import org.ws4d.java.structures.EmptyStructures;
import org.ws4d.java.structures.Iterator;
import org.ws4d.java.structures.LinkedList;
import org.ws4d.java.structures.List;
import org.ws4d.java.types.QName;
import org.ws4d.java.util.StringUtil;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

public class RestrictedSimpleContent extends SimpleContent implements Facets {

	protected List	facets	= EmptyStructures.EMPTY_LIST;

	static final void handleRestriction(ElementParser parser, RestrictedSimpleContent t, String targetNamespace, Schema schema) throws XmlPullParserException, IOException, SchemaException {
		String base = parser.getAttributeValue(null, InheritType.ATTRIBUTE_BASE);
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
				if (StringUtil.equalsIgnoreCase(FACET_ENUMERATION, name)) {
					RestrictedSimpleType.handleFacet(parser, t, t);
				} else if (StringUtil.equalsIgnoreCase(FACET_FRACTIONDIGITS, name)) {
					RestrictedSimpleType.handleFacet(parser, t, t);
				} else if (StringUtil.equalsIgnoreCase(FACET_LENGTH, name)) {
					RestrictedSimpleType.handleFacet(parser, t, t);
				} else if (StringUtil.equalsIgnoreCase(FACET_MAXEXCLUSIVE, name)) {
					RestrictedSimpleType.handleFacet(parser, t, t);
				} else if (StringUtil.equalsIgnoreCase(FACET_MAXINCLUSIVE, name)) {
					RestrictedSimpleType.handleFacet(parser, t, t);
				} else if (StringUtil.equalsIgnoreCase(FACET_MAXLENGTH, name)) {
					RestrictedSimpleType.handleFacet(parser, t, t);
				} else if (StringUtil.equalsIgnoreCase(FACET_MINEXCLUSIVE, name)) {
					RestrictedSimpleType.handleFacet(parser, t, t);
				} else if (StringUtil.equalsIgnoreCase(FACET_MININCLUSIVE, name)) {
					RestrictedSimpleType.handleFacet(parser, t, t);
				} else if (StringUtil.equalsIgnoreCase(FACET_MINLENGTH, name)) {
					RestrictedSimpleType.handleFacet(parser, t, t);
				} else if (StringUtil.equalsIgnoreCase(FACET_PATTERN, name)) {
					RestrictedSimpleType.handleFacet(parser, t, t);
				} else if (StringUtil.equalsIgnoreCase(FACET_TOTALDIGITS, name)) {
					RestrictedSimpleType.handleFacet(parser, t, t);
				} else if (StringUtil.equalsIgnoreCase(FACET_WHITESPACE, name)) {
					RestrictedSimpleType.handleFacet(parser, t, t);
				} else if (StringUtil.equalsIgnoreCase(TAG_SIMPLETYPE, name)) {
					Type baseType = createSimpleType(parser, targetNamespace, schema);
					/*
					 * don't set it as base type as there is already one given
					 * by the 'base' attribute; rather, take the additional
					 * restrictions defined within this 'baseType' and apply
					 * them on 't'
					 */
					// t.setBase(baseType);
					if (baseType instanceof RestrictedSimpleType) {
						RestrictedSimpleType tt = (RestrictedSimpleType) baseType;
						for (Iterator it = tt.facets(); it.hasNext();) {
							t.addFacet((Facet) it.next());
						}
					}
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

	RestrictedSimpleContent() {
		super();
	}

	public RestrictedSimpleContent(String name, String namespace) {
		this(new QName(name, namespace));
	}

	public RestrictedSimpleContent(QName name) {
		super(name);
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.types.schema.SimpleType#getSchemaIdentifier()
	 */
	public int getSchemaIdentifier() {
		return XSD_RESTRICTEDSIMPLECONTENT;
	}

	public Iterator facets() {
		return facets.iterator();
	}

	public void addFacet(Facet f) {
		if (facets == EmptyStructures.EMPTY_LIST) {
			facets = new LinkedList();
		}
		facets.add(f);
	}

	void serialize(XmlSerializer serializer, Schema schema) throws IOException {
		serializer.startTag(XMLSCHEMA_NAMESPACE, ComplexType.TAG_COMPLEXTYPE);
		if (getName() != null) {
			serializer.attribute(null, ATTRIBUTE_NAME, getName().getLocalPart());
		}
		serializer.startTag(XMLSCHEMA_NAMESPACE, TAG_SIMPLECONTENT);
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
		for (Iterator it = facets(); it.hasNext();) {
			Facet f = (Facet) it.next();
			f.serialize(serializer);
		}
		serializeAttributes(serializer, schema);
		serializeAttributeGroups(serializer, schema);
		serializeAnyAttribute(serializer, schema);

		serializer.endTag(XMLSCHEMA_NAMESPACE, TAG_RESTRICTION);
		serializer.endTag(XMLSCHEMA_NAMESPACE, TAG_SIMPLECONTENT);
		serializer.endTag(XMLSCHEMA_NAMESPACE, ComplexType.TAG_COMPLEXTYPE);
	}

}
