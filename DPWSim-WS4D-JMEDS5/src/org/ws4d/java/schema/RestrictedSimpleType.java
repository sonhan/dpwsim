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

/**
 * Class representation for simpleType:restriction.
 */
public class RestrictedSimpleType extends Type implements Facets, InheritType {

	protected QName	baseLink	= null;

	protected Type	base		= null;

	protected List	facets		= EmptyStructures.EMPTY_LIST;

	static final void handleRestriction(ElementParser parser, RestrictedSimpleType t, String targetNamespace, Schema schema) throws XmlPullParserException, IOException, SchemaException {
		String base = parser.getAttributeValue(null, ATTRIBUTE_BASE);
		if (base != null) {
			String p = SchemaUtil.getPrefix(base);
			String n = SchemaUtil.getName(base);
			String ns = parser.getNamespace(p);
			t.setBaseLink(new QName(n, ns));
		}

		int d = parser.getDepth();
		while (parser.nextTag() != XmlPullParser.END_TAG && parser.getDepth() == d + 1) {
			String namespace = parser.getNamespace();
			String name = parser.getName();
			if (XMLSCHEMA_NAMESPACE.equals(namespace)) {
				if (StringUtil.equalsIgnoreCase(FACET_ENUMERATION, name)) {
					handleFacet(parser, t, t);
				} else if (StringUtil.equalsIgnoreCase(FACET_FRACTIONDIGITS, name)) {
					handleFacet(parser, t, t);
				} else if (StringUtil.equalsIgnoreCase(FACET_LENGTH, name)) {
					handleFacet(parser, t, t);
				} else if (StringUtil.equalsIgnoreCase(FACET_MAXEXCLUSIVE, name)) {
					handleFacet(parser, t, t);
				} else if (StringUtil.equalsIgnoreCase(FACET_MAXINCLUSIVE, name)) {
					handleFacet(parser, t, t);
				} else if (StringUtil.equalsIgnoreCase(FACET_MAXLENGTH, name)) {
					handleFacet(parser, t, t);
				} else if (StringUtil.equalsIgnoreCase(FACET_MINEXCLUSIVE, name)) {
					handleFacet(parser, t, t);
				} else if (StringUtil.equalsIgnoreCase(FACET_MININCLUSIVE, name)) {
					handleFacet(parser, t, t);
				} else if (StringUtil.equalsIgnoreCase(FACET_MINLENGTH, name)) {
					handleFacet(parser, t, t);
				} else if (StringUtil.equalsIgnoreCase(FACET_PATTERN, name)) {
					handleFacet(parser, t, t);
				} else if (StringUtil.equalsIgnoreCase(FACET_TOTALDIGITS, name)) {
					handleFacet(parser, t, t);
				} else if (StringUtil.equalsIgnoreCase(FACET_WHITESPACE, name)) {
					handleFacet(parser, t, t);
				} else if (StringUtil.equalsIgnoreCase(SimpleType.TAG_SIMPLETYPE, name)) {
					Type baseType = SimpleType.createSimpleType(parser, targetNamespace, schema);
					t.setBase(baseType);
				} else if (StringUtil.equalsIgnoreCase(Annotation.TAG_ANNOTATION, name)) {
					Annotation.handleAnnotation(parser, t);
				}
			}
		}
		if (t.getBaseLink() == null && t.getBase() == null) {
			throw new IOException("Cannot restrict given type. No base type set.");
		}
	}

	static final void handleFacet(ElementParser parser, Facets facets, Annotation annotation) throws XmlPullParserException, IOException {
		String fName = parser.getName();
		String fNamespace = parser.getNamespace();
		String fValue = parser.getAttributeValue(null, Facet.ATTRIBUTE_VALUE);
		Facet f = new Facet(new QName(fName, fNamespace), fValue);
		facets.addFacet(f);
		int d = parser.getDepth();
		while (parser.nextTag() != XmlPullParser.END_TAG && parser.getDepth() == d + 1) {
			String namespace = parser.getNamespace();
			String name = parser.getName();
			if (XMLSCHEMA_NAMESPACE.equals(namespace)) {
				if (StringUtil.equalsIgnoreCase(Annotation.TAG_ANNOTATION, name)) {
					Annotation.handleAnnotation(parser, annotation);
				}
			}
			// XXX ???
			SchemaUtil.handleUnkownTags(parser);
		}
	}

	RestrictedSimpleType() {
		super();
	}

	public RestrictedSimpleType(String name, String namespace) {
		this(new QName(name, namespace));
	}

	public RestrictedSimpleType(QName name) {
		super(name);
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.types.schema.Annotation#getSchemaIdentifier()
	 */
	public int getSchemaIdentifier() {
		return XSD_RESTRICTEDSIMPLETYPE;
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

	public Type getBase() {
		return base;
	}

	public void setBase(Type base) {
		baseLink = null;
		this.base = base;
		base.addSubtype(this);
	}

	void serialize(XmlSerializer serializer, Schema schema) throws IOException {
		serializer.startTag(XMLSCHEMA_NAMESPACE, SimpleType.TAG_SIMPLETYPE);
		if (getName() != null) {
			serializer.attribute(null, ATTRIBUTE_NAME, getName().getLocalPart());
		}
		serializer.startTag(XMLSCHEMA_NAMESPACE, TAG_RESTRICTION);
		QName baseName = base.getName();
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
		serializer.endTag(XMLSCHEMA_NAMESPACE, TAG_RESTRICTION);
		serializer.endTag(XMLSCHEMA_NAMESPACE, SimpleType.TAG_SIMPLETYPE);

	}

	QName getBaseLink() {
		return baseLink;
	}

	void setBaseLink(QName link) {
		baseLink = link;
	}

}
