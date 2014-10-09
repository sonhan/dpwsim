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
 * Class representation for simple type.
 */
public class SimpleType extends Type {

	static final String	TAG_SIMPLETYPE			= SCHEMA_SIMPLETYPE;

	static final String	TAG_UNION				= SCHEMA_UNION;

	static final String	TAG_LIST				= SCHEMA_LIST;

	static final String	ATTRIBUTE_MEMBERTYPES	= SCHEMA_MEMBERTYPES;

	static final String	ATTRIBUTE_ITEMTYPE		= SCHEMA_ITEMTYPE;

	protected List		member					= EmptyStructures.EMPTY_LIST;

	protected List		memberLinks				= EmptyStructures.EMPTY_LIST;

	protected QName		listItemLink			= null;

	protected Type		listItemType			= null;

	static final Type createSimpleType(ElementParser parser, String targetNamespace, Schema schema) throws XmlPullParserException, IOException, SchemaException {
		String tName = parser.getAttributeValue(null, ATTRIBUTE_NAME);
		Type t = null;
		if (tName == null) {
			t = new SimpleType();
		} else {
			t = new SimpleType(new QName(tName, targetNamespace));
		}
		t.setParentSchema(schema);
		int d = parser.getDepth();
		while (parser.nextTag() != XmlPullParser.END_TAG && parser.getDepth() == d + 1) {
			String namespace = parser.getNamespace();
			String name = parser.getName();
			if (XMLSCHEMA_NAMESPACE.equals(namespace)) {
				if (StringUtil.equalsIgnoreCase(InheritType.TAG_RESTRICTION, name)) {
					if (tName == null) {
						t = new RestrictedSimpleType();
					} else {
						t = new RestrictedSimpleType(new QName(tName, targetNamespace));
					}
					RestrictedSimpleType restricted = (RestrictedSimpleType) t;
					RestrictedSimpleType.handleRestriction(parser, restricted, targetNamespace, schema);
					if (restricted.getBase() == null) {
						schema.addBaseForResolve(restricted);
					}
				} else if (TAG_UNION.equals(name)) {
					SimpleType simple = (SimpleType) t;
					handleUnion(parser, simple, targetNamespace, schema);
				} else if (TAG_LIST.equals(name)) {
					SimpleType simple = (SimpleType) t;
					handleList(parser, simple, targetNamespace, schema);
				} else if (Annotation.TAG_ANNOTATION.equals(name)) {
					Annotation.handleAnnotation(parser, t);
				}
			}
		}
		return t;
	}

	private static final void handleUnion(ElementParser parser, SimpleType t, String targetNamespace, Schema schema) throws XmlPullParserException, IOException, SchemaException {
		String members = parser.getAttributeValue(null, ATTRIBUTE_MEMBERTYPES);
		if (members != null) {
			String[] member = StringUtil.split(members, (char) 32);
			for (int i = 0; i < member.length; i++) {
				String p = SchemaUtil.getPrefix(member[i]);
				String n = SchemaUtil.getName(member[i]);
				String ns = parser.getNamespace(p);
				QName link = new QName(n, ns);
				t.addMemberLink(link);
			}
			schema.addUnionForResolve(t);
		}
		int d = parser.getDepth();
		while (parser.nextTag() != XmlPullParser.END_TAG && parser.getDepth() == d + 1) {
			String namespace = parser.getNamespace();
			String name = parser.getName();
			if (XMLSCHEMA_NAMESPACE.equals(namespace)) {
				if (StringUtil.equalsIgnoreCase(TAG_SIMPLETYPE, name)) {
					Type simple = SimpleType.createSimpleType(parser, targetNamespace, schema);
					t.addMember(simple);
				}
			}
		}
	}

	private static final void handleList(ElementParser parser, SimpleType t, String targetNamespace, Schema schema) throws XmlPullParserException, IOException, SchemaException {
		String itemtype = parser.getAttributeValue(null, ATTRIBUTE_ITEMTYPE);
		if (itemtype != null) {
			String p = SchemaUtil.getPrefix(itemtype);
			String n = SchemaUtil.getName(itemtype);
			String ns = parser.getNamespace(p);
			QName link = new QName(n, ns);
			t.setListItemLink(link);
			schema.addListForResolve(t);
		}
		/*
		 * either attribute 'itemType' must be specified, or there must be an
		 * inline simple type definition - both together is not allowed by XML
		 * Schema!
		 */
		int d = parser.getDepth();
		while (parser.nextTag() != XmlPullParser.END_TAG && parser.getDepth() == d + 1) {
			String namespace = parser.getNamespace();
			String name = parser.getName();
			if (XMLSCHEMA_NAMESPACE.equals(namespace)) {
				if (StringUtil.equalsIgnoreCase(TAG_SIMPLETYPE, name)) {
					Type simple = SimpleType.createSimpleType(parser, targetNamespace, schema);
					t.setListItemType(simple);
				}
			}
		}
	}

	public SimpleType() {
		super();
	}

	public SimpleType(QName name) {
		super(name);
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.types.schema.Annotation#getSchemaIdentifier()
	 */
	public int getSchemaIdentifier() {
		return XSD_SIMPLETYPE;
	}

	public boolean isList() {
		return listItemLink != null || listItemType != null;
	}

	public boolean isUnion() {
		if (member != null && member.size() > 0) return true;
		if (memberLinks != null && memberLinks.size() > 0) return true;
		return false;
	}

	public Iterator getMembers() {
		return member.iterator();
	}

	public void addMember(Type t) {
		if (member == EmptyStructures.EMPTY_LIST) {
			member = new LinkedList();
		}
		member.add(t);
	}

	public void setListItemType(Type t) {
		int id = t.getSchemaIdentifier();
		if (id != XSD_SIMPLETYPE && id != XSD_RESTRICTEDSIMPLETYPE) {
			throw new IllegalArgumentException("Illegal list item type (expected a simple type): " + t);
		}

		listItemLink = null;
		listItemType = t;
	}

	public Type getListItemType() {
		return listItemType;
	}

	void serialize(XmlSerializer serializer, Schema schema) throws IOException {
		serializer.startTag(XMLSCHEMA_NAMESPACE, TAG_SIMPLETYPE);
		if (name != null) {
			serializer.attribute(null, ATTRIBUTE_NAME, name.getLocalPart());
		}
		if (isUnion()) {
			serializer.startTag(XMLSCHEMA_NAMESPACE, TAG_UNION);
			if (memberLinks.size() > 0) {
				StringBuffer sBuf = new StringBuffer();
				for (Iterator it = getMemberLinks(); it.hasNext();) {
					QName link = (QName) it.next();
					String prefix = serializer.getPrefix(link.getNamespace(), false);
					if (!(prefix == null || "".equals(prefix))) {
						link.setPrefix(prefix);
						sBuf.append(link.getLocalPartPrefixed());
					} else {
						sBuf.append(link.getLocalPart());
					}
					if (it.hasNext()) {
						sBuf.append(" ");
					}
				}
				serializer.attribute(null, ATTRIBUTE_MEMBERTYPES, sBuf.toString());
			}
			if (member.size() > 0) {
				for (Iterator it = getMembers(); it.hasNext();) {
					Type t = (Type) it.next();
					if (t.getName() == null) {
						/*
						 * only serialize anonymous inline simple type
						 * declarations!
						 */
						t.serialize(serializer, schema);
					}
				}
			}
			serializer.endTag(XMLSCHEMA_NAMESPACE, TAG_UNION);
		} else if (isList()) {
			serializer.startTag(XMLSCHEMA_NAMESPACE, TAG_LIST);
			if (listItemType == null) {
				serializer.attribute(null, ATTRIBUTE_ITEMTYPE, SchemaUtil.getPrefixedName(serializer, listItemLink));
			} else if (listItemType.getName() != null) {
				serializer.attribute(null, ATTRIBUTE_ITEMTYPE, SchemaUtil.getPrefixedName(serializer, listItemType.getName()));
			} else {
				// serialize item type as inline simple type!
				listItemType.serialize(serializer, schema);
			}
			serializer.endTag(XMLSCHEMA_NAMESPACE, TAG_LIST);
		}
		serializer.endTag(XMLSCHEMA_NAMESPACE, TAG_SIMPLETYPE);
	}

	Iterator getMemberLinks() {
		return memberLinks.iterator();
	}

	void addMemberLink(QName link) {
		if (memberLinks == EmptyStructures.EMPTY_LIST) {
			memberLinks = new LinkedList();
		}
		memberLinks.add(link);
	}

	void insertMembers(List members) {
		/*
		 * the purpose of this method is to _insert_ the members before the
		 * already existing ones, as 'members' contains types declared within
		 * the value of attribute 'memberTypes', which must precede in order any
		 * members defined by means of inline simple type declarations
		 */
		if (member == EmptyStructures.EMPTY_LIST) {
			member = new LinkedList();
		}
		member.addAll(0, members);
	}

	void setListItemLink(QName link) {
		listItemLink = link;
	}

	QName getListItemLink() {
		return listItemLink;
	}

}
