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
import org.ws4d.java.types.QName;
import org.ws4d.java.util.StringUtil;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

public class Group extends Reference {

	static final String			TAG_GROUP	= SCHEMA_GROUP;

	protected static int		count		= 0;

	protected ElementContainer	container	= null;

	public static int getGroupCount() {
		return count;
	}

	static final Group createGroup(ElementParser parser, String targetNamespace, Schema schema) throws XmlPullParserException, IOException, SchemaException {
		String gName = parser.getAttributeValue(null, ATTRIBUTE_NAME);
		String gRef = parser.getAttributeValue(null, ATTRIBUTE_REF);
		if (gName == null && gRef == null) {
			throw new SchemaException("Wrong group definiton. No name or reference set.");
		}
		Group g = null;
		if (gName != null) {
			g = new Group(new QName(gName, targetNamespace));
		} else {
			g = new Group();
		}

		if (gRef != null) {
			String p = SchemaUtil.getPrefix(gRef);
			String n = SchemaUtil.getName(gRef);
			String ns = parser.getNamespace(p);
			g.setReferenceLink(new QName(n, ns));
			schema.addGroupForResolve(g);
		}

		int d = parser.getDepth();
		while (parser.nextTag() != XmlPullParser.END_TAG && parser.getDepth() == d + 1) {
			String namespace = parser.getNamespace();
			String name = parser.getName();
			if (XMLSCHEMA_NAMESPACE.equals(namespace)) {
				if (StringUtil.equalsIgnoreCase(ElementContainer.TAG_ALL, name)) {
					ElementContainer container = new AllContainer();
					ElementContainer.handleContainerElements(parser, container, targetNamespace, schema);
					g.container = container;
				} else if (StringUtil.equalsIgnoreCase(ElementContainer.TAG_SEQUENCE, name)) {
					ElementContainer container = new SequenceContainer();
					ElementContainer.handleContainerElements(parser, container, targetNamespace, schema);
					g.container = container;
				} else if (StringUtil.equalsIgnoreCase(ElementContainer.TAG_CHOICE, name)) {
					ElementContainer container = new ChoiceContainer();
					ElementContainer.handleContainerElements(parser, container, targetNamespace, schema);
					g.container = container;
				} else if (StringUtil.equalsIgnoreCase(Annotation.TAG_ANNOTATION, name)) {
					Annotation.handleAnnotation(parser, g);
				}
			}
		}

		return g;
	}

	Group() {
		this(null);
	}

	Group(QName name) {
		this.name = name;
		if ((name != null && !XMLSCHEMA_NAMESPACE.equals(name.getNamespace())) || name == null) {
			count++;
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.types.schema.Annotation#getSchemaIdentifier()
	 */
	public int getSchemaIdentifier() {
		return XSD_GROUP;
	}

	public Element getElementByName(QName name) {
		return reference != null ? ((Group) reference).getElementByName(name) : container == null ? null : container.getElementByName(name);
	}

	public Element getElementByName(String name) {
		return reference != null ? ((Group) reference).getElementByName(name) : container == null ? null : container.getElementByName(name);
	}

	public int getElementCount() {
		return reference != null ? ((Group) reference).getElementCount() : container == null ? 0 : container.getElementCount();
	}

	public boolean hasElements() {
		return reference != null ? ((Group) reference).hasElements() : container == null ? false : container.hasElements();
	}

	public Iterator elements() {
		return reference != null ? ((Group) reference).elements() : container == null ? EmptyStructures.EMPTY_ITERATOR : container.allElements();
	}

	public int getContainerType() {
		return reference != null ? ((Group) reference).getContainerType() : container != null ? container.getContainerType() : ComplexType.CONTAINER_ALL;
	}

	public int getContainerMinOccurs() {
		return reference != null ? ((Group) reference).getContainerMinOccurs() : container != null ? container.getMinOccurs() : 0;
	}

	public int getContainerMaxOccurs() {
		return reference != null ? ((Group) reference).getContainerMaxOccurs() : container != null ? container.getMaxOccurs() : 0;
	}

	/**
	 * Returns the enclosed container for this group.
	 * 
	 * @return the enclosed container.
	 * @see SequenceContainer
	 * @see AllContainer
	 * @see ChoiceContainer
	 */
	public ElementContainer getContainer() {
		return reference != null ? ((Group) reference).getContainer() : container != null ? container : null;
	}

	public Iterator listAll() {
		if (reference != null) {
			return ((Group) reference).listAll();
		}
		if (container == null) {
			return EmptyStructures.EMPTY_ITERATOR;
		}
		return container.listAll();
	}

	void serialize(XmlSerializer serializer, Schema schema) throws IOException {
		serializer.startTag(XMLSCHEMA_NAMESPACE, TAG_GROUP);
		if (name != null) {
			serializer.attribute(null, ATTRIBUTE_NAME, name.getLocalPart());
		}
		if (reference != null) {
			QName refName = reference.getName();
			String prefix = serializer.getPrefix(refName.getNamespace(), false);
			if (!(prefix == null || "".equals(prefix))) {
				serializer.attribute(null, ATTRIBUTE_REF, prefix + ":" + refName.getLocalPart());
			} else {
				serializer.attribute(null, ATTRIBUTE_REF, refName.getLocalPart());
			}
		}
		if (container != null) {
			container.serialize(serializer, schema);
		}
		serializer.endTag(XMLSCHEMA_NAMESPACE, TAG_GROUP);
	}

}
