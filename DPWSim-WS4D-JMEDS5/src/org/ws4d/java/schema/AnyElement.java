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
import org.ws4d.java.io.xml.ElementParser;
import org.ws4d.java.util.StringUtil;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

public class AnyElement extends Reference {

	static final String	TAG_ELEMENT			= SCHEMA_ANY;

	protected int		min					= 1;

	protected int		max					= 1;

	static final String	ATTRIBUTE_MAXOCCURS	= ELEMENT_MAXOCCURS;

	static final String	ATTRIBUTE_MINOCCURS	= ELEMENT_MINOCCURS;

	static final AnyElement createAnyElement(ElementParser parser) throws XmlPullParserException, IOException {
		String minOccurs = null;
		String maxOccurs = null;

		int c = parser.getAttributeCount();
		for (int i = 0; i < c; i++) {
			String attributeName = parser.getAttributeName(i);
			String attributeNamespace = parser.getAttributeNamespace(i);
			String attributeValue = parser.getAttributeValue(i);
			if (attributeNamespace == null || "".equals(attributeNamespace)) {
				if (AnyElement.ATTRIBUTE_MINOCCURS.equals(attributeName)) {
					minOccurs = attributeValue;
				} else if (AnyElement.ATTRIBUTE_MAXOCCURS.equals(attributeName)) {
					maxOccurs = attributeValue;
				}
			}
		}

		AnyElement e = new AnyElement();

		/*
		 * Set element minimum occurs.
		 */
		if (minOccurs != null) {
			e.setMinOccurs(Integer.valueOf(minOccurs).intValue());
		}

		/*
		 * Set element maximum occurs.
		 */
		if (maxOccurs != null) {
			if (maxOccurs.equals(MAXOCCURS_UNBOUNDED)) {
				e.setMaxOccurs(-1);
			} else {
				e.setMaxOccurs(Integer.valueOf(maxOccurs).intValue());
			}
		}

		int d = parser.getDepth();
		while (parser.nextTag() != XmlPullParser.END_TAG && parser.getDepth() == d + 1) {
			/*
			 * check for inner definitions
			 */
			String namespace = parser.getNamespace();
			String name = parser.getName();
			if (XMLSCHEMA_NAMESPACE.equals(namespace)) {
				if (StringUtil.equalsIgnoreCase(Annotation.TAG_ANNOTATION, name)) {
					Annotation.handleAnnotation(parser, e);
				}
			}
		}
		return e;
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.schema.Any#getSchemaIdentifier()
	 */
	public int getSchemaIdentifier() {
		return SchemaConstants.XSD_ANYELEMENT;
	}

	/**
	 * Returns the minimum occurrence for this element.
	 * <p>
	 * The "minOccurs" attribute in XML Schema describes the minimum occurrence
	 * of this element inside the created XML instance document.
	 * </p>
	 * 
	 * @return the minimum occurrence of this element.
	 */
	public int getMinOccurs() {
		return min;
	}

	/**
	 * Returns the maximum occurrence for this element.
	 * <p>
	 * The "maxOccurs" attribute in XML Schema describes the maximum occurrence
	 * of this element inside the created XML instance document.
	 * </p>
	 * 
	 * @return the maximum occurrence of this element.
	 */
	public int getMaxOccurs() {
		return max;
	}

	/**
	 * Sets the minimum occurrence for this element.
	 * <p>
	 * The "minOccurs" attribute in XML Schema describes the minimum occurrence
	 * of this element inside the created XML instance document.
	 * </p>
	 * 
	 * @param min the minimum occurrence for this element.
	 */
	public void setMinOccurs(int min) {
		this.min = min;
	}

	/**
	 * Sets the maximum occurrence for this element.
	 * <p>
	 * The "maxOccurs" attribute in XML Schema describes the maximum occurrence
	 * of this element inside the created XML instance document.
	 * </p>
	 * 
	 * @param max the maximum occurrence for this element.
	 */
	public void setMaxOccurs(int max) {
		this.max = max;
	}

	/**
	 * Returns <code>null</code>. This element has no type! Inside the XML
	 * instance document an element of any type can be used.
	 * 
	 * @return <code>null</code>.
	 */
	public Type getType() {
		return SchemaUtil.getAnyType();
	}

	/**
	 * Serializes the schema element.
	 * 
	 * @param serializer the serializer which should be used.
	 * @param schema the schema.
	 * @throws IOException
	 */
	void serialize(XmlSerializer serializer, Schema schema) throws IOException {
		serializer.startTag(XMLSCHEMA_NAMESPACE, TAG_ANY);
		if (min != 1) {
			serializer.attribute(null, AnyElement.ATTRIBUTE_MINOCCURS, String.valueOf(min));
		}
		if (max != 1) {
			if (max == -1) {
				serializer.attribute(null, AnyElement.ATTRIBUTE_MAXOCCURS, MAXOCCURS_UNBOUNDED);
			} else {
				serializer.attribute(null, AnyElement.ATTRIBUTE_MAXOCCURS, String.valueOf(max));
			}
		}
		serializer.endTag(XMLSCHEMA_NAMESPACE, TAG_ANY);

	}

}
