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
import org.ws4d.java.structures.HashMap;
import org.ws4d.java.types.Attributable;
import org.ws4d.java.types.AttributableSupport;
import org.ws4d.java.types.CustomAttributeValue;
import org.ws4d.java.types.QName;
import org.ws4d.java.types.StringAttributeValue;
import org.ws4d.java.util.StringUtil;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

/**
 * This class allows object representation of XML Schema elements.
 * <p>
 * Those elements are part of the XML Schema definition and are used inside WSDL
 * documents to describe the content of a message. It is possible to define XML
 * Schema structures with the classes {@link Schema}, {@link Element},
 * {@link Attribute}, {@link SimpleType}, {@link ComplexType}, {@link Group} and
 * {@link AttributeGroup}. This is at least necessary to invoke SOAP operations
 * (like used in DPWS).<br />
 * An element consists of a qualified name (local part and namespace) and a
 * type.
 * </p>
 * <h3>XML Schema</h3>
 * <p>
 * XML Schema describes the structure of the content for a XML instance
 * document. Each element is linked to a specific data type. XML Schema comes
 * with built-in primitive data types like <i>string</i>, <i>boolean</i>,
 * <i>decimal</i> and derived data types like <i>byte</i>, <i>int</i>,
 * <i>token</i> and <i>positiveInteger</i>. It is also possible to define one's
 * own derived data types. An XML Schema could looks like this:
 * </p>
 * 
 * <pre>
 * &lt;xs:schema xmlns:xs=&quot;http://www.w3.org/2001/XMLSchema&quot; targetNamespace=&quot;http://www.example.org&quot;&gt; 
 *    &lt;xs:complexType name=&quot;personType&quot;&gt;
 *       &lt;xs:sequence&gt;
 *          &lt;xs:element name=&quot;firstname&quot; type=&quot;xs:string&quot; /&gt;
 *          &lt;xs:element name=&quot;lastname&quot; type=&quot;xs:string&quot; /&gt;
 *          &lt;xs:element name=&quot;age&quot; type=&quot;xs:int&quot; /&gt;
 *       &lt;/xs:sequence&gt;
 *    &lt;/xs:complexType&gt;
 *    &lt;xs:element name=&quot;person&quot; type=&quot;personType&quot; /&gt;
 * &lt;/xs:schema&gt;
 * </pre>
 * <p>
 * The XML Schema above defines a derived data type called <i>personType</i>
 * which contains inner-elements. The derived data type is used by the element
 * <i>person</i>. This XML schema allows the creation of the following XML
 * instance document:
 * </p>
 * 
 * <pre>
 * &lt;?xml version=&quot;1.0&quot;?&gt;
 * &lt;person&gt;
 *    &lt;firstname&gt;John&lt;/firstname&gt;
 *    &lt;lastname&gt;Doe&lt;/lastname&gt;
 *    &lt;age&gt;66&lt;/age&gt;
 * &lt;/person&gt;
 * </pre>
 * <p>
 * You can learn more about XML Schema at <a
 * href="http://www.w3.org/XML/Schema">http://www.w3.org/XML/Schema</a>
 * </p>
 * <h3>Framework</h3>
 * <p>
 * If you like to create the element described above, it is necessary to create
 * the derived data type too and use the primitive data type <i>string</i>. If
 * you can access predefined primitive data types with the
 * {@link SchemaUtil#getSchemaType(String)} method.<br />
 * The created code should look like this:
 * </p>
 * 
 * <pre>
 * // get primitive data types
 * Type xsString = SchemaUtil.getSchemaType(&quot;string&quot;);
 * Type xsInt = SchemaUtil.getSchemaType(&quot;int&quot;);
 * 
 * // create inner elements for personType
 * Element firstname = new Element(new QName(&quot;firstname&quot;, &quot;http://www.example.org&quot;), xsString);
 * Element lastname = new Element(new QName(&quot;lastname&quot;, &quot;http://www.example.org&quot;), xsString);
 * Element age = new Element(new QName(&quot;age&quot;, &quot;http://www.example.org&quot;), xsInt);
 * 
 * // create personType and add inner elements
 * ComplexType personType = new ComplexType(new QName(&quot;personType&quot;, &quot;http://www.example.org&quot;), ComplexType.CONTAINER_SEQUENCE);
 * personType.addElement(firstname);
 * personType.addElement(lastname);
 * personType.addElement(age);
 * 
 * // create element
 * Element person = new Element(new QName(&quot;person&quot;, &quot;http://www.example.org&quot;), personType);
 * </pre>
 * 
 * <h3>Details</h3>
 * <p>
 * The following examples will show how to use the element to create different
 * XML Schema structures.
 * <ul>
 * <li>
 * <h4>Element reference</h4>
 * <ul>
 * <li><a
 * href="http://www.w3.org/TR/xmlschema11-1/#ref.elt.global">http://www.w3
 * .org/TR/xmlschema11-1/#ref.elt.global</a></li>
 * <li>XML Schema:
 * <p>
 * Element references allow to reference global elements.
 * 
 * <pre>
 * &lt;xs:schema xmlns:xs=&quot;http://www.w3.org/2001/XMLSchema&quot; targetNamespace=&quot;http://www.example.org&quot;&gt;
 *    &lt;xs:element name=&quot;a&quot; type=&quot;xs:string&quot; /&gt;
 *    &lt;xs:complexType name=&quot;b&quot;&gt;
 *       &lt;xs:sequence&gt;
 *          &lt;xs:element ref=&quot;a&quot; /&gt;
 *       &lt;/xs:sequence&gt;
 *    &lt;/xs:complexType&gt;
 * &lt;/xs:schema&gt;
 * </pre>
 * 
 * </p>
 * </li>
 * <li>Framework:
 * <p>
 * 
 * <pre>
 * // get primitive data types
 * Type xsString = SchemaUtil.getSchemaType(&quot;string&quot;);
 * 
 * // create element a
 * Element a = new Element(new QName(&quot;a&quot;, &quot;http://example.org&quot;), xsString);
 * 
 * // create reference for element a
 * Element aref = new Element(a);
 * 
 * // create type b
 * ComplexType b = new ComplexType(new QName(&quot;b&quot;, &quot;http://example.org&quot;), ComplexType.CONTAINER_SEQUENCE);
 * b.addElement(aref);
 * </pre>
 * 
 * </p>
 * </li>
 * </ul>
 * </li>
 * <li>
 * <h4>Substitution group</h4>
 * <ul>
 * <li><a
 * href="http://www.w3.org/TR/xmlschema11-1/#sec-cos-equiv-class">http://www
 * .w3.org/TR/xmlschema11-1/#sec-cos-equiv-class</a></li>
 * <li>XML Schema:
 * <p>
 * 
 * <pre>
 * &lt;xs:schema xmlns:xs=&quot;http://www.w3.org/2001/XMLSchema&quot; targetNamespace=&quot;http://www.example.org&quot;&gt;
 *    &lt;xs:element name=&quot;a&quot; type=&quot;xs:string&quot; /&gt;
 *    &lt;xs:element name=&quot;b&quot; substitutionGroup=&quot;a&quot; /&gt;
 * &lt;/xs:schema&gt;
 * </pre>
 * 
 * </p>
 * </li>
 * <li>Framework:
 * <p>
 * 
 * <pre>
 * // get primitive data types
 * Type xsString = SchemaUtil.getSchemaType(&quot;string&quot;);
 * 
 * // create element a
 * Element a = new Element(new QName(&quot;a&quot;, &quot;http://example.org&quot;), xsString);
 * 
 * // create a substituted element b
 * Element b = new Element(new QName(&quot;b&quot;, &quot;http://example.org&quot;));
 * b.setSubstitutionGroup(new QName(&quot;a&quot;, &quot;http://example.org&quot;));
 * </pre>
 * 
 * </p>
 * </ul>
 * </li>
 * </ul>
 * </p>
 * 
 * @see Schema
 * @see Attribute
 * @see SimpleType
 * @see ComplexType
 * @see Group
 * @see AttributeGroup
 */
public class Element extends AnyElement implements Attributable {

	static final String		TAG_ELEMENT					= SCHEMA_ELEMENT;

	static final String		ATTRIBUTE_NILLABLE			= ELEMENT_NILLABLE;

	static final String		ATTRIBUTE_TYPE				= SCHEMA_TYPE;

	static final String		ATTRIBUTE_SUBSTITUTIONGROUP	= SCHEMA_SUBSTITUTIONGROUP;

	protected static int	count						= 0;

	protected boolean		globalScope					= false;

	protected QName			typeLink					= null;

	protected Type			type						= null;

	protected QName			subtitutionGroup			= null;

	protected String		fixed						= null;

	protected boolean		nillable					= false;

	private Attributable	attributableDelegate;

	protected Type			localType					= null;

	/**
	 * Returns the number of elements created by the framework. This can be used
	 * for debug purposes.
	 * 
	 * @return the number of elements created by the framework.
	 */
	public static int getElementCount() {
		return count;
	}

	static final Element createElement(ElementParser parser, String targetNamespace, Schema schema) throws XmlPullParserException, IOException, SchemaException {
		return createElement(parser, targetNamespace, schema, false);
	}

	static final Element createElement(ElementParser parser, String targetNamespace, Schema schema, boolean globalScope) throws XmlPullParserException, IOException, SchemaException {
		HashMap attributes = null;
		String eName = null;
		String eType = null;
		String eRef = null;
		String sGroup = null;
		String eAbstract = null;
		String minOccurs = null;
		String maxOccurs = null;
		String nil = null;

		int c = parser.getAttributeCount();
		for (int i = 0; i < c; i++) {
			String attributeName = parser.getAttributeName(i);
			String attributeNamespace = parser.getAttributeNamespace(i);
			String attributeValue = parser.getAttributeValue(i);
			if (attributeNamespace == null || "".equals(attributeNamespace)) {
				if (ATTRIBUTE_NAME.equals(attributeName)) {
					eName = attributeValue;
				} else if (ATTRIBUTE_TYPE.equals(attributeName)) {
					eType = attributeValue;
				} else if (ATTRIBUTE_REF.equals(attributeName)) {
					eRef = attributeValue;
				} else if (ATTRIBUTE_SUBSTITUTIONGROUP.equals(attributeName)) {
					sGroup = attributeValue;
				} else if (ATTRIBUTE_ABSTRACT.equals(attributeName)) {
					eAbstract = attributeValue;
				} else if (AnyElement.ATTRIBUTE_MINOCCURS.equals(attributeName)) {
					minOccurs = attributeValue;
				} else if (AnyElement.ATTRIBUTE_MAXOCCURS.equals(attributeName)) {
					maxOccurs = attributeValue;
				} else if (ATTRIBUTE_NILLABLE.equals(attributeName)) {
					nil = attributeValue;
				} else {
					if (attributes == null) {
						attributes = new HashMap();
					}
					attributes.put(new QName(attributeName, attributeNamespace), new StringAttributeValue(attributeValue));
				}
			} else {
				if (attributes == null) {
					attributes = new HashMap();
				}
				attributes.put(new QName(attributeName, attributeNamespace), new StringAttributeValue(attributeValue));
			}
		}

		if (eType != null && eRef != null) {
			throw new SchemaException("Cannot create element. Element definition SHOULD NOT have both, type and ref attribute.");
		}

		Element e = new Element();
		e.setParentSchema(schema);
		e.globalScope = globalScope;
		if (attributes != null) {
			e.setAttributes(attributes);
		}

		/*
		 * Set element name.
		 */
		if (eName != null) {
			e.setName(new QName(eName, targetNamespace));
		}

		/*
		 * Set element minimum occurs.
		 */
		if (!globalScope && minOccurs != null) {
			e.setMinOccurs(Integer.valueOf(minOccurs).intValue());
		}

		/*
		 * Set element maximum occurs.
		 */
		if (!globalScope && maxOccurs != null) {
			if (maxOccurs.equals(MAXOCCURS_UNBOUNDED)) {
				e.setMaxOccurs(-1);
			} else {
				e.setMaxOccurs(Integer.valueOf(maxOccurs).intValue());
			}
		}

		if (nil != null) {
			if (StringUtil.equalsIgnoreCase(ATTRIBUTE_VALUE_TRUE, nil)) {
				e.setNillable(true);
			}
		}

		/*
		 * Set the abstract attribute. 0 = not abstract 1 = abstract (false) 2 =
		 * abstract (true)
		 */
		if (eAbstract != null) {
			e.setAbstract(StringUtil.equalsIgnoreCase(ATTRIBUTE_VALUE_TRUE, eAbstract));
		}

		/*
		 * Set substitution group.
		 */
		if (sGroup != null) {
			String p = SchemaUtil.getPrefix(sGroup);
			String n = SchemaUtil.getName(sGroup);
			String ns = parser.getNamespace(p);
			e.setSubstitutionGroup(new QName(n, ns));
		}

		if (eType != null && eRef == null) {
			String p = SchemaUtil.getPrefix(eType);
			String n = SchemaUtil.getName(eType);
			String ns = parser.getNamespace(p);
			QName typeName = new QName(n, ns);
			if (XMLSCHEMA_NAMESPACE.equals(ns)) {
				Type t = SchemaUtil.getType(typeName);
				if (t != null) {
					e.setType(t);
				}
			} else {
				e.setTypeLink(typeName);
				schema.addElementForResolve(e);
			}
		} else if (eRef != null && eType == null) {
			String p = SchemaUtil.getPrefix(eRef);
			String n = SchemaUtil.getName(eRef);
			String ns = parser.getNamespace(p);
			e.setReferenceLink(new QName(n, ns));
			schema.addElementForResolve(e);
		}

		int d = parser.getDepth();
		while (parser.nextTag() != XmlPullParser.END_TAG && parser.getDepth() == d + 1) {
			/*
			 * check for inner definitions
			 */
			String namespace = parser.getNamespace();
			String name = parser.getName();
			if (XMLSCHEMA_NAMESPACE.equals(namespace)) {
				if (StringUtil.equalsIgnoreCase(ComplexType.TAG_COMPLEXTYPE, name)) {
					Type t = ComplexType.createComplexType(parser, targetNamespace, schema);
					e.setType(t);
				} else if (StringUtil.equalsIgnoreCase(SimpleType.TAG_SIMPLETYPE, name)) {
					Type t = SimpleType.createSimpleType(parser, targetNamespace, schema);
					e.setType(t);
				} else if (StringUtil.equalsIgnoreCase(Annotation.TAG_ANNOTATION, name)) {
					Annotation.handleAnnotation(parser, e);
				}
			}
		}
		return e;
	}

	Element() {
		this((QName) null);
	}

	/**
	 * Creates an element with the given name and namespace.
	 * <p>
	 * This constructor will generate an appropriate qualified name.
	 * </p>
	 * 
	 * @param name the name of the element.
	 * @param namespace the namespace.
	 */
	public Element(String name, String namespace) {
		this(new QName(name, namespace));
	}

	/**
	 * Creates an element with given name, namespace and type.
	 * <p>
	 * This constructor will generate an appropriate qualified name.
	 * </p>
	 * 
	 * @param name the name of the element.
	 * @param namespace the namespace.
	 * @param type the type of the element.
	 */
	public Element(String name, String namespace, Type type) {
		this(new QName(name, namespace), type);
	}

	/**
	 * Creates an element with given qualified name.
	 * 
	 * @param name the qualified name of the element.
	 */
	public Element(QName name) {
		this(name, null);
	}

	/**
	 * Creates an element with given element name and type.
	 * 
	 * @param name the qualified name of the element.
	 * @param type the type of the element.
	 */
	public Element(String elementName, Type type) {
		this(new QName(elementName, null), type);
	}

	/**
	 * Creates an element with given type.
	 * 
	 * @param name the qualified name of the element.
	 * @param type the type of the element.
	 */
	public Element(Type type) {
		this((QName) null, type);
	}

	/**
	 * Creates an element with given qualified name and type.
	 * 
	 * @param name the qualified name of the element.
	 * @param type the type of the element.
	 */
	public Element(QName name, Type type) {
		this.name = name;
		if ((name != null && !XMLSCHEMA_NAMESPACE.equals(name.getNamespace())) || name == null) {
			count++;
		}
		if (this.name == null) {
			this.name = new QName(StringUtil.simpleClassName(getClass()), null);
		}
		setType(type);
	}

	/**
	 * Creates an element with given name, namespace and type.
	 * <p>
	 * This constructor will generated an appropriate qualified name.
	 * </p>
	 * 
	 * @param name the name of the element.
	 * @param namespace the namespace.
	 * @param type the type of the element.
	 * @param min the minimum occurrence of this element.
	 * @param max the maximum occurrence of this element.
	 */
	public Element(String name, String namespace, Type type, int min, int max) {
		this(new QName(name, namespace), type, min, max);
	}

	/**
	 * Creates an element with given qualified name, type and occurrences.
	 * 
	 * @param name the qualified name of the element.
	 * @param type the type of the element.
	 * @param min the minimum occurrence of this element.
	 * @param max the maximum occurrence of this element.
	 */
	public Element(QName name, Type type, int min, int max) {
		this.name = name;
		if ((name != null && !XMLSCHEMA_NAMESPACE.equals(name.getNamespace())) || name == null) {
			count++;
		}
		setType(type);
		setMinOccurs(min);
		setMaxOccurs(max);
	}

	/**
	 * Creates a reference based on a specified element.
	 * 
	 * @param reference the element which should be referenced.
	 */
	public Element(Element reference) {
		this((QName) null);
		setReference(reference);
	}

	/**
	 * Creates a reference based on a specified element.
	 * 
	 * @param reference the element which should be referenced.
	 * @param min the minimum occurrence of this element.
	 * @param max the maximum occurrence of this element.
	 */
	public Element(Element reference, int min, int max) {
		this((QName) null);
		setReference(reference);
		setMinOccurs(min);
		setMaxOccurs(max);
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		StringBuffer sb = new StringBuffer(StringUtil.formatClassName(getClass()));
		QName name = getName();
		if (name != null) {
			sb.append(" [ name=").append(name.getLocalPart());
			sb.append(", namespace=").append(name.getNamespace());
			sb.append(", type=").append(getType().getName());
			sb.append(" ]");
		}
		return sb.toString();
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.types.schema.Annotation#getSchemaIdentifier()
	 */
	public int getSchemaIdentifier() {
		return SchemaConstants.XSD_ELEMENT;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.ws4d.java.wsdl.Attributable#getAttribute(org.ws4d.java.types.QName)
	 */
	public CustomAttributeValue getAttribute(QName name) {
		return attributableDelegate == null ? null : attributableDelegate.getAttribute(name);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.ws4d.java.types.Attributable#setAttribute(org.ws4d.java.types.QName,
	 * org.ws4d.java.types.CustomAttributeValue)
	 */
	public void setAttribute(QName name, CustomAttributeValue value) {
		if (attributableDelegate == null) {
			attributableDelegate = new AttributableSupport();
		}
		attributableDelegate.setAttribute(name, value);

	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.ws4d.java.types.Attributable#setAttribute(org.ws4d.java.types.QName,
	 * java.lang.String)
	 */
	public void setAttribute(QName name, String value) {
		setAttribute(name, new StringAttributeValue(value));
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.wsdl.Attributable#getAttributes()
	 */
	public HashMap getAttributes() {
		if (attributableDelegate == null) {
			attributableDelegate = new AttributableSupport();
		}
		return attributableDelegate.getAttributes();
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.ws4d.java.wsdl.Attributable#setAttributes(org.ws4d.java.structures
	 * .HashMap)
	 */
	public void setAttributes(HashMap attributes) {
		if (attributableDelegate == null) {
			if (attributes == null) {
				return;
			}
			attributableDelegate = new AttributableSupport();
		}
		attributableDelegate.setAttributes(attributes);
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.types.Attributable#hasAttributes()
	 */
	public boolean hasAttributes() {
		return attributableDelegate != null && attributableDelegate.hasAttributes();
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.types.Attributable#serializeAttributes(org.xmlpull.v1.
	 * XmlSerializer)
	 */
	public void serializeAttributes(XmlSerializer serializer) throws IOException {
		if (attributableDelegate != null) {
			attributableDelegate.serializeAttributes(serializer);
		}
	}

	/**
	 * Returns the type of this element.
	 * 
	 * @return the type of this element.
	 */
	public Type getType() {
		if (reference != null) return ((Element) reference).getType();
		if (type == null) return (localType != null ? localType : Schema.ANY_TYPE);
		return type;
	}

	/**
	 * Returns whether this element value is fixed or not.
	 * 
	 * @return <code>true</code> if the value of this element cannot be changed,
	 *         <code>false</code> otherwise.
	 */
	public boolean isFixed() {
		return (fixed == null);
	}

	/**
	 * Returns the fixed value for this element.
	 * <p>
	 * The fixed value cannot be changed inside a XML instance document.
	 * </p>
	 * 
	 * @return the fixed value for this element.
	 */
	public String getFixedValue() {
		return fixed;
	}

	/**
	 * Sets the name of the substitution group for this element.
	 * <p>
	 * This affects only elements which are root elements of a XML Schema.
	 * </p>
	 * 
	 * @param group the qualified name for the group.
	 */
	public void setSubstitutionGroup(QName group) {
		subtitutionGroup = group;
	}

	/**
	 * Returns the name of the substitution group for this element.
	 * 
	 * @return the qualified name of the group.
	 */
	public QName getSubstitutionGroup() {
		return subtitutionGroup;
	}

	/**
	 * Sets the type of the element.
	 * 
	 * @param type the type of the element.
	 */
	public void setType(Type type) {
		typeLink = null;
		abstractValue = false;
		localType = null;
		this.type = type;
	}

	protected void setLocalType(Type type) {
		// this.type = null;
		// localType = type;
	}

	/**
	 * Returns whether the instance of this element can handle
	 * <strong>nil</strong> values or not.
	 * 
	 * @return <code>true</code> if the instance can handle <strong>nil</strong>
	 *         values, <code>false</code> otherwise.
	 */
	public boolean isNillable() {
		return nillable;
	}

	/**
	 * Set whether the instance of this element can handle <strong>nil</strong>
	 * values or not.
	 * 
	 * @param nillable <code>true</code> if the instance can handle
	 *            <strong>nil</strong> values, <code>false</code> otherwise.
	 */
	public void setNillable(boolean nillable) {
		this.nillable = nillable;
	}

	void setTypeLink(QName typeLink) {
		this.typeLink = typeLink;
	}

	QName getTypeLink() {
		return typeLink;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.ws4d.java.schema.AnyElement#serialize(org.xmlpull.v1.XmlSerializer,
	 * org.ws4d.java.schema.Schema)
	 */
	void serialize(XmlSerializer serializer, Schema schema) throws IOException {
		serializer.startTag(XMLSCHEMA_NAMESPACE, TAG_ELEMENT);
		/*
		 * THX @Stefan Schlichting: Do not generate name if ref is present see
		 * http://www.w3.org/TR/xmlschema-1/#d0e4233 2.1
		 */
		if (name != null && reference == null) {
			serializer.attribute(null, ATTRIBUTE_NAME, name.getLocalPart());
		}
		if (subtitutionGroup != null) {
			serializer.attribute(null, ATTRIBUTE_SUBSTITUTIONGROUP, subtitutionGroup.getLocalPart());
		}
		if (abstractValue) {
			serializer.attribute(null, ATTRIBUTE_ABSTRACT, ATTRIBUTE_VALUE_TRUE);
		}
		if (nillable == true) {
			serializer.attribute(null, ATTRIBUTE_NILLABLE, ATTRIBUTE_VALUE_TRUE);
		}
		if (!globalScope) {
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
		}
		if (type != null) {
			QName typeName = type.getName();
			if (typeName == null) {
				serializeAttributes(serializer);
				type.serialize(serializer, schema);
			} else {
				String prefix = serializer.getPrefix(typeName.getNamespace(), false);
				if (!(prefix == null || "".equals(prefix))) {
					typeName.setPrefix(prefix);
					serializer.attribute(null, ATTRIBUTE_TYPE, typeName.getLocalPartPrefixed());
				} else {
					serializer.attribute(null, ATTRIBUTE_TYPE, typeName.getLocalPart());
				}
				serializeAttributes(serializer);
			}
		} else if (reference != null) {
			QName refName = reference.getName();
			schema.addReferenceElement((Element) reference);
			String prefix = serializer.getPrefix(refName.getNamespace(), false);
			if (!(prefix == null || "".equals(prefix))) {
				refName.setPrefix(prefix);
				serializer.attribute(null, ATTRIBUTE_REF, refName.getLocalPartPrefixed());
			} else {
				serializer.attribute(null, ATTRIBUTE_REF, refName.getLocalPart());
			}
			serializeAttributes(serializer);
		} else {
			serializeAttributes(serializer);
		}

		serializer.endTag(XMLSCHEMA_NAMESPACE, TAG_ELEMENT);
	}

}
