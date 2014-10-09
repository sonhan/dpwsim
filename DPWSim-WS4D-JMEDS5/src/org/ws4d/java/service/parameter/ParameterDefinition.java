/*******************************************************************************
 * Copyright (c) 2009 MATERNA Information & Communications. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html. For further
 * project-related information visit http://www.ws4d.org. The most recent
 * version of the JMEDS framework can be obtained from
 * http://sourceforge.net/projects/ws4d-javame.
 ******************************************************************************/
package org.ws4d.java.service.parameter;

import java.io.IOException;

import org.ws4d.java.constants.SchemaConstants;
import org.ws4d.java.constants.XOPConstants;
import org.ws4d.java.schema.ComplexType;
import org.ws4d.java.schema.Element;
import org.ws4d.java.schema.Schema;
import org.ws4d.java.schema.SchemaUtil;
import org.ws4d.java.schema.Type;
import org.ws4d.java.service.OperationDescription;
import org.ws4d.java.service.Service;
import org.ws4d.java.structures.HashMap;
import org.ws4d.java.structures.Iterator;
import org.ws4d.java.types.QName;
import org.ws4d.java.util.Log;
import org.ws4d.java.wsdl.WSDL;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

public abstract class ParameterDefinition extends ParameterValue {

	public abstract String serialize();

	public abstract void parse(String content);

	public abstract void parseContent(XmlPullParser parser) throws IOException, XmlPullParserException;

	public abstract void serializeContent(XmlSerializer serializer) throws IOException;

	public int getValueType() {
		return TYPE_UNKNOWN;
	}

	/**
	 * This method parses an given XML Parser object (XML instance document)
	 * into a equivalent parameter value.
	 * 
	 * @param parser the XML Parser.
	 * @return the parsed parameter value.
	 * @throws XmlPullParserException throws this exception if the parser cannot
	 *             correctly parse the XML.
	 * @throws IOException throws this exception if the parser cannot correctly
	 *             parse the XML.
	 */
	public static ParameterValue parse(XmlPullParser parser, Element base, OperationDescription operation) throws XmlPullParserException, IOException {
		return parse0(parser, base, operation);
	}

	private final static ParameterValue[] parse1(XmlPullParser parser) throws XmlPullParserException, IOException {
		QName parsedName = new QName(parser.getName(), parser.getNamespace(), parser.getPrefix());

		ParameterValue[] pv = new ParameterValue[2];

		pv[0] = new ParameterValue();

		pv[0].setName(parsedName);

		int attributeCount = parser.getAttributeCount();
		if (attributeCount > 0) {

			for (int i = 0; i < attributeCount; i++) {
				QName attName = new QName(parser.getAttributeName(i), parser.getAttributeNamespace(i), parser.getAttributePrefix(i));
				if (SchemaConstants.XSI_NAMESPACE.equals(attName.getNamespace()) && SchemaConstants.ATTRIBUTE_XSINIL.equals(attName.getLocalPart())) {
					/*
					 * XML instance <strong>nil</code> set? This parameter can
					 * have a nil value.
					 */
					pv[0].setNil(true);
				} else {
					ParameterAttribute attribute = new ParameterAttribute(attName);
					attribute.setValue(parser.getAttributeValue(i));
					pv[0].add(attribute);
				}
			}
		}

		int tag = parser.getEventType();
		switch (tag) {
			case XmlPullParser.START_DOCUMENT:
				tag = parser.nextTag();
				break;
			case XmlPullParser.START_TAG:
				if (XOPConstants.XOP_NAMESPACE_NAME.equals(parser.getNamespace()) && XOPConstants.XOP_ELEM_INCLUDE.equals(parser.getName())) {
					pv[1] = load(SchemaUtil.getSchemaType(SchemaUtil.TYPE_BASE64_BINARY));
					ParameterDefinition pd = (ParameterDefinition) pv[1];
					pd.parseContent(parser);
					tag = parser.nextTag();
					return pv;
				} else {
					tag = parser.next();
					break;
				}
		}
		if (tag == XmlPullParser.TEXT) {
			pv[0] = load(null);
			ParameterDefinition pd = (ParameterDefinition) pv[0];
			pd.parseContent(parser);
			tag = parser.nextTag();

			pv[0].setName(parsedName);
			return pv;
		}
		int d = parser.getDepth();
		while (tag != XmlPullParser.END_TAG && parser.getDepth() >= d) {
			ParameterValue[] child = ParameterDefinition.parse1(parser);
			if (child[1] != null && child[1].getValueType() == ParameterValue.TYPE_ATTACHMENT) {
				child[1].setName(parsedName);
				pv[0] = child[1];
				child[1] = null;
			} else {
				pv[0].add(child[0]);
			}
			tag = parser.nextTag(); // check tag
			if (tag == XmlPullParser.END_TAG && parser.getDepth() == d) {
				// own end tag, go to next start tag
				tag = parser.nextTag();
			}
		}

		return pv;
	}

	private final static ParameterValue parse0(XmlPullParser parser, Element base, OperationDescription operation) throws XmlPullParserException, IOException {
		Type t = base.getType();
		Type instanceType = t;
		QName parsedName = new QName(parser.getName(), parser.getNamespace(), parser.getPrefix());

		/*
		 * check given element and parsed element
		 */
		if (!parsedName.equals(base.getName())) {
			throw new IOException("Cannot create parameter. Element mismatch. Should be " + base.getName() + ", but " + parsedName + " was found.");
		}

		boolean nil = false;

		HashMap attrs = null;

		int attributeCount = parser.getAttributeCount();
		if (attributeCount > 0) {

			attrs = new HashMap();

			for (int i = 0; i < attributeCount; i++) {
				String localPart = parser.getAttributeName(i);
				String ns = parser.getAttributeNamespace(i);
				QName attName = new QName(localPart, ns, parser.getAttributePrefix(i));
				if (SchemaConstants.XSI_NAMESPACE.equals(ns)) {
					if (SchemaConstants.ATTRIBUTE_XSINIL.equals(localPart)) {
						/*
						 * XML instance <strong>nil</code> set? This parameter
						 * can have a nil value.
						 */
						nil = true;
					} else if (SchemaConstants.ATTRIBUTE_XSITYPE.equals(localPart)) {
						String xsiType = parser.getAttributeValue(i);
						if (xsiType != null && xsiType.trim().length() > 0) {
							// xsi:type support, thx to Stefan Schlichting
							String nsp = null;
							int index = xsiType.indexOf(":");
							if (index >= 0) {
								if (index > 0) {
									nsp = xsiType.substring(0, index);
								}
								xsiType = xsiType.substring(index + 1);
							}
							QName qn = new QName(xsiType, parser.getNamespace(nsp));
							// lookup type from operation
							if (operation != null) {
								Service service = operation.getService();
								for (Iterator it = service.getDescriptions(); it.hasNext();) {
									WSDL wsdl = (WSDL) it.next();
									Type iType = wsdl.getSchemaType(qn);
									if (iType != null) {
										instanceType = iType;
										break;
									}
								}
							}
						}
					} else {
						ParameterAttribute attribute = new ParameterAttribute(attName);
						attribute.setValue(parser.getAttributeValue(i));
						attrs.put(localPart, attribute);
					}
				} else {
					ParameterAttribute attribute = new ParameterAttribute(attName);
					attribute.setValue(parser.getAttributeValue(i));
					attrs.put(localPart, attribute);
				}
			}
		}

		ParameterValue pv = null;

		/*
		 * Eat text or check for children.
		 */
		if (!instanceType.isComplexType()) {
			pv = load(instanceType);
			ParameterDefinition pd = (ParameterDefinition) pv;
			pd.parseContent(parser);
		} else {
			int tag = parser.nextTag();
			int d = parser.getDepth();
			pv = new ParameterValue();
			ComplexType complex = (ComplexType) instanceType;
			while (tag != XmlPullParser.END_TAG && parser.getDepth() >= d) {
				QName nextStartName = new QName(parser.getName(), parser.getNamespace(), parser.getPrefix());

				/*
				 * TODO: This is a very simple parser implementation. It should
				 * be better if we check for occurrence and container type like
				 * ALL, SEQUENCE and CHOICE. At the moment we just check whether
				 * the element name is possible or not.
				 */
				Element nextElement = searchElement(complex, nextStartName);

				ParameterValue child = null;
				if (nextElement == null) {
					if (complex.getName() != null && complex.getName().equals(new QName(SchemaUtil.TYPE_ANYTYPE, SchemaConstants.XMLSCHEMA_NAMESPACE))) {
						/*
						 * is ANY type
						 */

						/*
						 * TODO 13.05.2011: We should create a schema repository
						 * ... the definition for the searched type can be part
						 * of any schema we ever used within a service.
						 */
						Schema s = base.getParentSchema();
						if (s != null) {
							/*
							 * search inside linked schema
							 */
							nextElement = s.getElement(nextStartName);
							child = parse0(parser, nextElement, operation);
							if (nextElement == null) {
								parseAnyChild(parser, parsedName, nextStartName);
							}
						} else {
							child = parseAnyChild(parser, parsedName, nextStartName);
						}
					} else {
						/*
						 * maybe ANY type?!
						 */
						child = parseAnyChild(parser, parsedName, nextStartName);
						// throw new IOException("Element " + nextStartName +
						// " is not allowed as child of " + parsedName + ".");
					}
				} else {
					child = parse0(parser, nextElement, operation);
				}
				pv.add(child);
				tag = parser.nextTag(); // check tag
				if (tag == XmlPullParser.END_TAG && parser.getDepth() == d) {
					// own end tag, go to next start tag
					tag = parser.nextTag();
				}
			}
		}

		if (attrs != null) {
			pv.attributes = attrs;
			pv.setNil(nil);
		}

		pv.setMaxOccurs(base.getMaxOccurs());
		pv.setMinOccurs(base.getMinOccurs());

		pv.setName(parsedName);
		pv.setType(t);
		pv.setInstanceType(instanceType == t ? null : instanceType);
		return pv;
	}

	private static final ParameterValue parseAnyChild(XmlPullParser parser, QName parsedName, QName nextStartName) throws XmlPullParserException, IOException {
		if (Log.isDebug()) {
			Log.debug("Cannot determinate element with name " + nextStartName + ". Assuming ANY type.", Log.DEBUG_LAYER_FRAMEWORK);
		}
		ParameterValue child = null;
		ParameterValue[] pvv = parse1(parser);
		if (pvv[1] != null && pvv[1].getValueType() == ParameterValue.TYPE_ATTACHMENT) {
			pvv[1].setName(parsedName);
			child = pvv[1];
			pvv[1] = null;
		} else {
			child = pvv[0];
		}
		child = pvv[0];
		return child;
	}

	protected synchronized void serialize0(XmlSerializer serializer, HashMap nsCache) throws IOException {
		if (nsCache == null) {
			namespaceCache = collectNamespaces(serializer);
			nsCache = namespaceCache;
		}
		serializeStartTag(serializer, nsCache);
		serializeAttributes(serializer);
		if (hasChildren()) {
			serializeChildren(serializer, nsCache);
		} else {
			serializeContent(serializer);
		}
		serializeEndTag(serializer);
	}

	public String toString() {
		StringBuffer sBuf = new StringBuffer();
		sBuf.append("PV [ name=");
		sBuf.append(name);
		String value = serialize();
		if (value != null) {
			sBuf.append(", value=");
			sBuf.append(value);
		}
		if (attributes.size() > 0) {
			sBuf.append(", attributes=");
			sBuf.append("(");
			for (Iterator it = attributes(); it.hasNext();) {
				ParameterAttribute pa = (ParameterAttribute) it.next();
				sBuf.append(pa.toString());
				if (it.hasNext()) {
					sBuf.append(", ");
				}
			}
			sBuf.append(")");

		}
		if (children.size() > 0) {
			sBuf.append(", children=");
			sBuf.append("(");
			for (Iterator it = children(); it.hasNext();) {
				ParameterValue pv = (ParameterValue) it.next();
				sBuf.append(pv.toString());
				if (it.hasNext()) {
					sBuf.append(", ");
				}
			}
			sBuf.append(")");
		}
		sBuf.append(", min=");
		sBuf.append(min);
		sBuf.append(", max=");
		sBuf.append(max);
		sBuf.append(" ]");
		return sBuf.toString();
	}

}
