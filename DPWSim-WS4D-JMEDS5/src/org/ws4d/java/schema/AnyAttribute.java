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

public class AnyAttribute extends Reference {

	static final AnyAttribute createAnyAttribute(ElementParser parser) throws XmlPullParserException, IOException {
		AnyAttribute a = new AnyAttribute();

		int d = parser.getDepth();
		while (parser.nextTag() != XmlPullParser.END_TAG && parser.getDepth() == d + 1) {
			/*
			 * check for inline definitions
			 */
			String namespace = parser.getNamespace();
			String name = parser.getName();
			if (XMLSCHEMA_NAMESPACE.equals(namespace)) {
				if (StringUtil.equalsIgnoreCase(Annotation.TAG_ANNOTATION, name)) {
					Annotation.handleAnnotation(parser, a);
				}
			}
		}
		return a;
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.schema.Any#getSchemaIdentifier()
	 */
	public int getSchemaIdentifier() {
		return SchemaConstants.XSD_ANYATTRIBUTE;
	}

	/**
	 * Serializes the schema attribute.
	 * 
	 * @param serializer the serializer which should be used.
	 * @param schema the schema.
	 * @throws IOException
	 */
	void serialize(XmlSerializer serializer, Schema schema) throws IOException {
		serializer.startTag(XMLSCHEMA_NAMESPACE, TAG_ANYATTRIBUTE);
		serializer.endTag(XMLSCHEMA_NAMESPACE, TAG_ANYATTRIBUTE);

	}

}
