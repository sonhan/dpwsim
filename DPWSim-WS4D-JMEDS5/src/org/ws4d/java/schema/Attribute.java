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
 * Class for attributes.
 */
public class Attribute extends Reference {

	static final String		TAG_ATTRIBUTE	= SCHEMA_ATTRIBUTE;

	protected static int	count			= 0;

	protected Type			type			= null;

	protected QName			typeLink		= null;

	protected byte			use				= 1;

	protected boolean		fixed			= false;

	protected String		defaultValue	= null;

	public static int getAttributeCount() {
		return count;
	}

	static final Attribute createAttribute(ElementParser parser, String targetNamespace, Schema schema) throws XmlPullParserException, IOException, SchemaException {
		String aName = parser.getAttributeValue(null, ATTRIBUTE_NAME);
		String aType = parser.getAttributeValue(null, ATTRIBUTE_TYPE);
		String aRef = parser.getAttributeValue(null, ATTRIBUTE_REF);
		String aUse = parser.getAttributeValue(null, ATTRIBUTE_USE);
		String aFixed = parser.getAttributeValue(null, ATTRIBUTE_FIXED);
		String aDefault = parser.getAttributeValue(null, ATTRIBUTE_DEFAULT);
		if (aName == null && aRef == null) {
			throw new SchemaException("Wrong attribute definiton. No name or reference set.");
		}

		Attribute a = null;
		if (aName == null) {
			a = new Attribute();
		} else {
			a = new Attribute(new QName(aName, targetNamespace));
		}
		a.setParentSchema(schema);
		if (aUse != null) {
			a.setUse(aUse);
		}
		if (aFixed != null && aDefault != null) {
			throw new SchemaException("Wrong attribute definiton. Attribute can only have fixed or default. Not both.");
		} else if (aFixed != null) {
			a.setFixed(aFixed, true);
		} else if (aDefault != null) {
			a.setDefault(aDefault);
		}

		if (aType != null && aRef == null) {
			String p = SchemaUtil.getPrefix(aType);
			String n = SchemaUtil.getName(aType);
			String ns = parser.getNamespace(p);
			QName typeName = new QName(n, ns);
			if (XMLSCHEMA_NAMESPACE.equals(ns)) {
				Type t = SchemaUtil.getType(typeName);
				if (t != null) {
					a.setType(t);
				}
			} else {
				a.setTypeLink(typeName);
				schema.addAttributeForResolve(a);
			}
		} else if (aRef != null && aType == null) {
			String p = SchemaUtil.getPrefix(aRef);
			String n = SchemaUtil.getName(aRef);
			String ns = parser.getNamespace(p);
			a.setReferenceLink(new QName(n, ns));
			schema.addAttributeForResolve(a);
		}

		int d = parser.getDepth();
		while (parser.nextTag() != XmlPullParser.END_TAG && parser.getDepth() == d + 1) {
			/*
			 * check for inline definitions
			 */
			String namespace = parser.getNamespace();
			String name = parser.getName();
			if (XMLSCHEMA_NAMESPACE.equals(namespace)) {
				if (StringUtil.equalsIgnoreCase(SimpleType.TAG_SIMPLETYPE, name)) {
					Type t = SimpleType.createSimpleType(parser, targetNamespace, schema);
					a.setType(t);
				} else if (StringUtil.equalsIgnoreCase(Annotation.TAG_ANNOTATION, name)) {
					Annotation.handleAnnotation(parser, a);
				}
			}
		}
		return a;
	}

	Attribute() {
		this((QName) null);
	}

	public Attribute(String name, String namespace) {
		this(new QName(name, namespace));
	}

	public Attribute(String name, String namespace, Type type) {
		this(new QName(name, namespace), type);
	}

	public Attribute(QName name) {
		this(name, null);
	}

	public Attribute(QName name, Type type) {
		this.name = name;
		if ((name != null && !XMLSCHEMA_NAMESPACE.equals(name.getNamespace())) || name == null) {
			count++;
		}
		setType(type);
	}

	public Attribute(Element reference) {
		this((QName) null);
		setReference(reference);
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.schema.NamedObject#toString()
	 */
	public String toString() {
		StringBuffer sb = new StringBuffer(StringUtil.formatClassName(getClass()));
		QName name = getName();
		sb.append(" [ name=").append(name.getLocalPart());
		sb.append(", namespace=").append(name.getNamespace());
		sb.append(", type=").append(getType().getName());
		if (fixed) {
			sb.append(", fixed=").append(defaultValue);
		} else if (defaultValue != null) {
			sb.append(", default=").append(defaultValue);
		}
		sb.append(" ]");
		return sb.toString();
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.types.schema.Annotation#getSchemaIdentifier()
	 */
	public int getSchemaIdentifier() {
		return XSD_ATTRIBUTE;
	}

	public Type getType() {
		if (reference != null) return ((Attribute) reference).getType();
		if (type == null) return SchemaUtil.getAnySimpleType();
		return type;
	}

	public boolean isOptional() {
		return (use == 1);
	}

	public boolean isProhibited() {
		return (use == 2);
	}

	public boolean isRequired() {
		return (use == 3);
	}

	public boolean isFixed() {
		return fixed;
	}

	public String getDefault() {
		return defaultValue;
	}

	public String getFixed() {
		return fixed ? defaultValue : null;
	}

	public void setType(Type type) {
		typeLink = null;
		this.type = type;
	}

	public void setUse(String usage) {
		if (USE_OPTIONAL.equals(usage)) {
			use = 1;
		} else if (USE_PROHIBITED.equals(usage)) {
			use = 2;
		} else if (USE_REQUIRED.equals(usage)) {
			use = 3;
		}
	}

	public String getUse() {
		if (use == 2) {
			return USE_PROHIBITED;
		} else if (use == 3) {
			return USE_REQUIRED;
		}
		return USE_OPTIONAL;
	}

	public void setDefault(String value) {
		if (fixed) return;
		defaultValue = value;
	}

	public void setFixed(String value, boolean isFixed) {
		fixed = isFixed;
		defaultValue = value;
	}

	void setUse(byte usage) {
		use = usage;
	}

	void setTypeLink(QName typeLink) {
		this.typeLink = typeLink;
	}

	QName getTypeLink() {
		return typeLink;
	}

	void serialize(XmlSerializer serializer, Schema schema) throws IOException {
		serializer.startTag(XMLSCHEMA_NAMESPACE, TAG_ATTRIBUTE);
		if (name != null) {
			serializer.attribute(null, ATTRIBUTE_NAME, name.getLocalPart());
		}
		if (fixed) {
			serializer.attribute(null, ATTRIBUTE_FIXED, defaultValue);
		} else if (defaultValue != null) {
			serializer.attribute(null, ATTRIBUTE_DEFAULT, defaultValue);
		}

		if (use > 1) {
			serializer.attribute(null, ATTRIBUTE_USE, getUse());
		}
		if (type != null) {
			QName typeName = type.getName();
			if (typeName == null) {
				type.serialize(serializer, schema);
			} else {
				String prefix = serializer.getPrefix(typeName.getNamespace(), false);
				if (!(prefix == null || "".equals(prefix))) {
					typeName.setPrefix(prefix);
					serializer.attribute(null, ATTRIBUTE_TYPE, typeName.getLocalPartPrefixed());
				} else {
					serializer.attribute(null, ATTRIBUTE_TYPE, typeName.getLocalPart());
				}
			}
		} else if (reference != null) {
			QName refName = reference.getName();
			String prefix = serializer.getPrefix(refName.getNamespace(), false);
			if (!(prefix == null || "".equals(prefix))) {
				refName.setPrefix(prefix);
				serializer.attribute(null, ATTRIBUTE_REF, refName.getLocalPartPrefixed());
			} else {
				serializer.attribute(null, ATTRIBUTE_REF, refName.getLocalPart());
			}
		}
		serializer.endTag(XMLSCHEMA_NAMESPACE, TAG_ATTRIBUTE);
	}

}
