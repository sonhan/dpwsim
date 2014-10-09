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
import org.ws4d.java.structures.ArrayList;
import org.ws4d.java.structures.EmptyStructures;
import org.ws4d.java.structures.HashMap;
import org.ws4d.java.structures.Iterator;
import org.ws4d.java.structures.LinkedMap;
import org.ws4d.java.structures.List;
import org.ws4d.java.types.QName;
import org.ws4d.java.util.StringUtil;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

/**
 * Class for attributes groups.
 */
public class AttributeGroup extends Reference {

	static final String		TAG_ATTRIBUTEGROUP	= SCHEMA_ATTRIBUTEGROUP;

	protected static int	count				= 0;

	protected HashMap		attributes			= EmptyStructures.EMPTY_MAP;

	protected HashMap		attributeGroups		= EmptyStructures.EMPTY_MAP;

	protected AnyAttribute	anyAttribute		= null;

	public static int getTotalAttributeGroupCount() {
		return count;
	}

	static final AttributeGroup createAttributeGroup(ElementParser parser, String targetNamespace, Schema schema) throws XmlPullParserException, IOException, SchemaException {
		String gName = parser.getAttributeValue(null, ATTRIBUTE_NAME);
		String gRef = parser.getAttributeValue(null, ATTRIBUTE_REF);
		AttributeGroup g = null;
		if (gName != null) {
			g = new AttributeGroup(new QName(gName, targetNamespace));
		} else {
			g = new AttributeGroup();
		}

		if (gRef != null) {
			String p = SchemaUtil.getPrefix(gRef);
			String n = SchemaUtil.getName(gRef);
			String ns = parser.getNamespace(p);
			g.setReferenceLink(new QName(n, ns));
			schema.addAttributeGroupForResolve(g);
		}

		int d = parser.getDepth();
		while (parser.nextTag() != XmlPullParser.END_TAG && parser.getDepth() == d + 1) {
			String namespace = parser.getNamespace();
			String name = parser.getName();
			if (XMLSCHEMA_NAMESPACE.equals(namespace)) {
				if (StringUtil.equalsIgnoreCase(Attribute.TAG_ATTRIBUTE, name)) {
					Attribute a = Attribute.createAttribute(parser, targetNamespace, schema);
					g.addAttribute(a);
				} else if (StringUtil.equalsIgnoreCase(TAG_ATTRIBUTEGROUP, name)) {
					AttributeGroup ag = AttributeGroup.createAttributeGroup(parser, targetNamespace, schema);
					g.addAttributeGroup(ag);
				} else if (StringUtil.equalsIgnoreCase(TAG_ANYATTRIBUTE, name)) {
					AnyAttribute a = AnyAttribute.createAnyAttribute(parser);
					g.setAnyAttribute(a);
				} else if (StringUtil.equalsIgnoreCase(Annotation.TAG_ANNOTATION, name)) {
					Annotation.handleAnnotation(parser, g);
				}
			}
		}
		return g;
	}

	AttributeGroup() {
		this(null);
	}

	AttributeGroup(QName name) {
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
		return XSD_ATTRIBUTEGROUP;
	}

	/**
	 * Returns whether this attribute group can contain attributes not defined
	 * by the schema.
	 * 
	 * @return <code>true</code> if any attribute can be added to this attribute
	 *         group, <code>false</code> otherwise.
	 */
	public boolean hasAnyAttribute() {
		return reference == null ? anyAttribute != null : ((AttributeGroup) reference).hasAnyAttribute();
	}

	/**
	 * This will add the possibility to add any attribute from other schema to
	 * this attribute group.
	 */
	public void allowAnyAttribute() {
		if (anyAttribute == null) {
			anyAttribute = new AnyAttribute();
		}
	}

	/**
	 * This will remove the possibility to add any attribute from other schema
	 * to this attribute group.
	 */
	public void denyAnyAttribute() {
		anyAttribute = null;
	}

	public void addAttribute(Attribute a) {
		QName name = a.getName();
		if (name == null) {
			name = a.getReferenceLink();
		}
		if (attributes == EmptyStructures.EMPTY_MAP) {
			attributes = new LinkedMap();
		}
		attributes.put(name, a);
	}

	public Attribute getAttribute(QName name) {
		return (Attribute) attributes.get(name);
	}

	public int getAttributeCount() {
		return attributes.size();
	}

	public Iterator attributes() {
		return attributes.values().iterator();
	}

	/**
	 * Adds an attribute group to this type.
	 * 
	 * @param g the attribute group to add.
	 */
	public void addAttributeGroup(AttributeGroup g) {
		QName name = g.getName();
		if (name == null) {
			name = g.getReferenceLink();
		}
		if (attributeGroups == EmptyStructures.EMPTY_MAP) {
			attributeGroups = new LinkedMap();
		}
		attributeGroups.put(name, g);
	}

	/**
	 * Returns an attribute group by qualified name.
	 * 
	 * @param name the qualified name for the attribute group.
	 * @return the attribute group.
	 */
	public AttributeGroup getAttributeGroup(QName name) {
		return (AttributeGroup) attributeGroups.get(name);
	}

	/**
	 * Returns the number of attribute groups for this type.
	 * 
	 * @return the number of attribute groups for this type.
	 */
	public int getAttributeGroupCount() {
		return attributeGroups.size();
	}

	/**
	 * Returns the attribute groups for this type.
	 * 
	 * @return the attribute groups.
	 */
	public Iterator attributeGroups() {
		return attributeGroups.values().iterator();
	}

	/**
	 * Returns the attributes for this type, even from attribute groups.
	 * 
	 * @return the attributes.
	 */
	public Iterator allAttributes() {
		List l = null;
		if (reference == null) {
			if (attributes != null && attributes.size() > 0) {
				l = new ArrayList();
				l.addAll(attributes.values());
			}
			if (attributeGroups != null && attributeGroups.size() > 0) {
				if (l == null) {
					l = new ArrayList();
				}
				for (Iterator it = attributeGroups.values().iterator(); it.hasNext();) {
					AttributeGroup g = (AttributeGroup) it.next();
					for (Iterator it2 = g.allAttributes(); it2.hasNext();) {
						l.add(it2.next());
					}
				}
			}
		} else {
			l = new ArrayList();
			AttributeGroup resolved = (AttributeGroup) reference;
			l.addAll(resolved.attributes.values());
			if (resolved.attributeGroups != null && resolved.attributeGroups.size() > 0) {
				for (Iterator it = resolved.attributeGroups.values().iterator(); it.hasNext();) {
					AttributeGroup g = (AttributeGroup) it.next();
					for (Iterator it2 = g.allAttributes(); it2.hasNext();) {
						l.add(it2.next());
					}
				}
			}
		}

		return l == null ? EmptyStructures.EMPTY_ITERATOR : l.iterator();
	}

	void setAnyAttribute(AnyAttribute a) {
		anyAttribute = a;
	}

	void serialize(XmlSerializer serializer, Schema schema) throws IOException {
		serializer.startTag(XMLSCHEMA_NAMESPACE, TAG_ATTRIBUTEGROUP);
		if (reference != null) {
			QName refName = reference.getName();
			String prefix = serializer.getPrefix(refName.getNamespace(), false);
			if (!(prefix == null || "".equals(prefix))) {
				refName.setPrefix(prefix);
				serializer.attribute(null, ATTRIBUTE_REF, refName.getLocalPartPrefixed());
			} else {
				serializer.attribute(null, ATTRIBUTE_REF, refName.getLocalPart());
			}
		} else {
			if (name != null) {
				serializer.attribute(null, ATTRIBUTE_NAME, name.getLocalPart());
			}
			if (attributes.size() > 0) {
				for (Iterator it = attributes(); it.hasNext();) {
					Attribute a = (Attribute) it.next();
					a.serialize(serializer, schema);
				}
			}
			// serialize nested AttributeGroups!
			if (attributeGroups.size() > 0) {
				for (Iterator it = attributeGroups(); it.hasNext();) {
					AttributeGroup ag = (AttributeGroup) it.next();
					serializer.startTag(XMLSCHEMA_NAMESPACE, TAG_ATTRIBUTEGROUP);
					QName refName = ag.getName();
					String prefix = serializer.getPrefix(refName.getNamespace(), false);
					if (!(prefix == null || "".equals(prefix))) {
						refName.setPrefix(prefix);
						serializer.attribute(null, ATTRIBUTE_REF, refName.getLocalPartPrefixed());
					} else {
						serializer.attribute(null, ATTRIBUTE_REF, refName.getLocalPart());
					}
					serializer.endTag(XMLSCHEMA_NAMESPACE, TAG_ATTRIBUTEGROUP);
				}
			}
			if (anyAttribute != null) {
				anyAttribute.serialize(serializer, schema);
			}
		}
		serializer.endTag(XMLSCHEMA_NAMESPACE, TAG_ATTRIBUTEGROUP);
	}

}
