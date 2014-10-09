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

/**
 * This class allows object representation of XML Schema complex types.
 * <p>
 * Those types are part of the XML Schema definition and are used inside WSDL
 * documents to describe the content of a message. It is possible to define XML
 * Schema structures with the classes {@link Schema}, {@link Element},
 * {@link Attribute}, {@link SimpleType}, {@link ComplexType}, {@link Group} and
 * {@link AttributeGroup}. This is at least necessary to invoke SOAP operations
 * (like used in DPWS).<br />
 * A complex type consists of a qualified name and the description of the
 * content structure.
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
 * If you want to create the complex type described above, it is necessary to
 * create the derived data type too and use the primitive data type
 * <i>string</i>. If you can access predefined primitive data types with the
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
 * The following examples will show how to use the complex type to create
 * different XML Schema structures.
 * <ul>
 * <li>
 * <h4>Model groups</h4>
 * <ul>
 * <li><a
 * href="http://www.w3.org/TR/xmlschema11-1/#Model_Groups">http://www.w3.org
 * /TR/xmlschema11-1/#Model_Groups</a></li>
 * <li>XML Schema:
 * <p>
 * Different model groups allow the assignment of an order to the content
 * structure. The content order can be <strong>all</strong>,
 * <strong>sequence</strong> or <strong>choice</strong>. The
 * <strong>all</strong> model group does not restrict the order of the content.
 * The <strong>sequence</strong> model group requires the correct order of the
 * content. The <strong>choice</strong> model group allows only one element to
 * be chosen for content.
 * 
 * <pre>
 * &lt;xs:schema xmlns:xs=&quot;http://www.w3.org/2001/XMLSchema&quot; targetNamespace=&quot;http://www.example.org&quot;&gt;
 *    &lt;element name=&quot;all&quot; type=&quot;allType&quot; /&gt;
 *    &lt;element name=&quot;sequence&quot; type=&quot;sequenceType&quot; /&gt;
 *    &lt;element name=&quot;choice&quot; type=&quot;choiceType&quot; /&gt;
 *    &lt;xs:complexType name=&quot;allType&quot;&gt;
 *       &lt;xs:all&gt;
 *          &lt;xs:element name=&quot;a&quot; type=&quot;xs:string&quot; /&gt;
 *          &lt;xs:element name=&quot;b&quot; type=&quot;xs:string&quot; /&gt;
 *       &lt;/xs:all&gt;
 *    &lt;/xs:complexType&gt;
 *    &lt;xs:complexType name=&quot;sequenceType&quot;&gt;
 *       &lt;xs:sequence&gt;
 *          &lt;xs:element name=&quot;a&quot; type=&quot;xs:string&quot; /&gt;
 *          &lt;xs:element name=&quot;b&quot; type=&quot;xs:string&quot; /&gt;
 *       &lt;/xs:sequence&gt;
 *    &lt;/xs:complexType&gt;
 *    &lt;xs:complexType name=&quot;choiceType&quot;&gt;
 *       &lt;xs:choice&gt;
 *          &lt;xs:element name=&quot;a&quot; type=&quot;xs:string&quot; /&gt;
 *          &lt;xs:element name=&quot;b&quot; type=&quot;xs:string&quot; /&gt;
 *       &lt;/xs:choice&gt;
 *    &lt;/xs:complexType&gt;
 * &lt;/xs:schema&gt;
 * </pre>
 * 
 * </p>
 * </li>
 * <li>XML:
 * <p>
 * The XML Schema listed above allows three different elements to be used as
 * root element for an XML instance. In case of the element "all", the inner
 * elements do not need any order. The order can be: a, b or b, a.<br />
 * 
 * <pre>
 * &lt;?xml version=&quot;1.0&quot;?&gt;
 * &lt;all&gt;
 *    &lt;a&gt;some text&lt;/a&gt;
 *    &lt;b&gt;more text&lt;/b&gt;
 * &lt;/all&gt;
 * </pre>
 * 
 * <pre>
 * &lt;?xml version=&quot;1.0&quot;?&gt;
 * &lt;all&gt;
 *    &lt;b&gt;more text&lt;/b&gt;
 *    &lt;a&gt;some text&lt;/a&gt;
 * &lt;/all&gt;
 * </pre>
 * 
 * In case of the element "sequence", the inner elements can only be used in the
 * given order. The order can be: a, b<br />
 * 
 * <pre>
 * &lt;?xml version=&quot;1.0&quot;?&gt;
 * &lt;sequence&gt;
 *    &lt;a&gt;some text&lt;/a&gt;
 *    &lt;b&gt;more text&lt;/b&gt;
 * &lt;/sequence&gt;
 * </pre>
 * 
 * In case of the element "choice", one element must be chosen from the given
 * elements. The elements can be: a or b, not both. <br />
 * 
 * <pre>
 * &lt;?xml version=&quot;1.0&quot;?&gt;
 * &lt;choice&gt;
 *    &lt;a&gt;some text&lt;/a&gt;
 * &lt;/choice&gt;
 * </pre>
 * 
 * <pre>
 * &lt;?xml version=&quot;1.0&quot;?&gt;
 * &lt;choice&gt;
 *    &lt;b&gt;more text&lt;/b&gt;
 * &lt;/choice&gt;
 * </pre>
 * 
 * </p>
 * </li>
 * <li>Framework:
 * <p>
 * The framework supports the <strong>all</strong>, <strong>sequence</strong>
 * and <strong>choice</strong> model groups.
 * 
 * <pre>
 * // get primitive data types
 * Type xsString = SchemaUtil.getSchemaType(&quot;string&quot;);
 * 
 * // create inner elements for the complex types
 * Element a = new Element(new QName(&quot;a&quot;, &quot;http://www.example.org&quot;), xsString);
 * Element b = new Element(new QName(&quot;b&quot;, &quot;http://www.example.org&quot;), xsString);
 * 
 * // create some complex type (all) and add inner elements
 * ComplexType allType = new ComplexType(new QName(&quot;allType&quot;, &quot;http://www.example.org&quot;), ComplexType.CONTAINER_ALL);
 * allType.addElement(a);
 * allType.addElement(b);
 * 
 * // create some complex type (sequence) and add inner elements
 * ComplexType sequenceType = new ComplexType(new QName(&quot;sequenceType&quot;, &quot;http://www.example.org&quot;), ComplexType.CONTAINER_SEQUENCE);
 * sequenceType.addElement(a);
 * sequenceType.addElement(b);
 * 
 * // create some complex type (choice) and add inner elements
 * ComplexType choiceType = new ComplexType(new QName(&quot;choiceType&quot;, &quot;http://www.example.org&quot;), ComplexType.CONTAINER_CHOICE);
 * choiceType.addElement(a);
 * choiceType.addElement(b);
 * </pre>
 * 
 * </p>
 * </li>
 * </ul>
 * </li>
 * </ul>
 * </p>
 * 
 * @see ComplexType#CONTAINER_ALL
 * @see ComplexType#CONTAINER_SEQUENCE
 * @see ComplexType#CONTAINER_CHOICE
 */
public class ComplexType extends Type {

	/**
	 * This is the constant field value for the <strong>all</strong> model
	 * group.
	 * <p>
	 * Should be used to define complex types without relevant element order.
	 * </p>
	 */
	public static final int		CONTAINER_ALL		= 0;

	/**
	 * This is the constant field value for the <strong>sequence</strong> model
	 * group.
	 * <p>
	 * Should be used to define complex types with relevant element order.
	 * </p>
	 */
	public static final int		CONTAINER_SEQUENCE	= 1;

	/**
	 * This is the constant field value for the <strong>choice</strong> model
	 * group.
	 * <p>
	 * Should be used to define complex types where one element must be chosen.
	 * </p>
	 */
	public static final int		CONTAINER_CHOICE	= 2;

	static final String			TAG_COMPLEXTYPE		= SCHEMA_COMPLEXTYPE;

	protected ElementContainer	container			= null;

	protected Group				group				= null;

	static final Type createComplexType(ElementParser parser, String targetNamespace, Schema schema) throws XmlPullParserException, IOException, SchemaException {
		String tName = parser.getAttributeValue(null, ATTRIBUTE_NAME);
		String tAbstract = parser.getAttributeValue(null, ATTRIBUTE_ABSTRACT);

		Type t = null;
		if (tName == null) {
			t = new ComplexType();
		} else {
			t = new ComplexType(new QName(tName, targetNamespace));
		}
		
		t.setParentSchema(schema);

		/*
		 * Set the abstract attribute. 0 = not abstract 1 = abstract (false) 2 =
		 * abstract (true)
		 */
		if (tAbstract != null) {
			t.setAbstract(StringUtil.equalsIgnoreCase(ATTRIBUTE_VALUE_TRUE, tAbstract));
		}

		int d = parser.getDepth();
		while (parser.nextTag() != XmlPullParser.END_TAG && parser.getDepth() == d + 1) {
			String namespace = parser.getNamespace();
			String name = parser.getName();
			if (XMLSCHEMA_NAMESPACE.equals(namespace)) {
				if (StringUtil.equalsIgnoreCase(ElementContainer.TAG_ALL, name)) {
					ElementContainer container = new AllContainer();
					ElementContainer.handleContainerElements(parser, container, targetNamespace, schema);
					ComplexType complex = (ComplexType) t;
					complex.setContainer(container);
				} else if (StringUtil.equalsIgnoreCase(ElementContainer.TAG_SEQUENCE, name)) {
					ElementContainer container = new SequenceContainer();
					ElementContainer.handleContainerElements(parser, container, targetNamespace, schema);
					ComplexType complex = (ComplexType) t;
					complex.setContainer(container);
				} else if (StringUtil.equalsIgnoreCase(ElementContainer.TAG_CHOICE, name)) {
					ElementContainer container = new ChoiceContainer();
					ElementContainer.handleContainerElements(parser, container, targetNamespace, schema);
					ComplexType complex = (ComplexType) t;
					complex.setContainer(container);
				} else if (StringUtil.equalsIgnoreCase(Group.TAG_GROUP, name)) {
					Group g = Group.createGroup(parser, targetNamespace, schema);
					ComplexType complex = (ComplexType) t;
					complex.setGroup(g);
				} else if (StringUtil.equalsIgnoreCase(SimpleContent.TAG_SIMPLECONTENT, name)) {
					if (tName == null) {
						t = SimpleContent.handleSimpleContent(parser, null, targetNamespace, schema);
					} else {
						t = SimpleContent.handleSimpleContent(parser, new QName(tName, targetNamespace), targetNamespace, schema);
					}
					SimpleContent simpleContent = (SimpleContent) t;
					if (simpleContent.getBase() == null) {
						schema.addBaseForResolve(simpleContent);
					}
				} else if (StringUtil.equalsIgnoreCase(ComplexContent.TAG_COMPLEXCONTENT, name)) {
					if (tName == null) {
						t = ComplexContent.handleComplexContent(parser, null, targetNamespace, schema);
					} else {
						t = ComplexContent.handleComplexContent(parser, new QName(tName, targetNamespace), targetNamespace, schema);
					}
					ComplexContent complexContent = (ComplexContent) t;
					schema.addBaseForResolve(complexContent);
				} else if (StringUtil.equalsIgnoreCase(Annotation.TAG_ANNOTATION, name)) {
					Annotation.handleAnnotation(parser, t);
				} else if (StringUtil.equalsIgnoreCase(Attribute.TAG_ATTRIBUTE, name)) {
					ComplexType complex = (ComplexType) t;
					Attribute a = Attribute.createAttribute(parser, targetNamespace, schema);
					complex.addAttribute(a);
				} else if (StringUtil.equalsIgnoreCase(AttributeGroup.TAG_ATTRIBUTEGROUP, name)) {
					ComplexType complex = (ComplexType) t;
					AttributeGroup g = AttributeGroup.createAttributeGroup(parser, targetNamespace, schema);
					complex.addAttributeGroup(g);
				} else if (StringUtil.equalsIgnoreCase(TAG_ANYATTRIBUTE, name)) {
					AnyAttribute a = AnyAttribute.createAnyAttribute(parser);
					ComplexType complex = (ComplexType) t;
					complex.setAnyAttribute(a);
				}
			}
		}
		return t;
	}

	/**
	 * This constructor <strong>must</strong> be used when declaring
	 * <em>top-level</em> types, which have a non-empty name.
	 * 
	 * @param name the name of the type.
	 * @param namespace the namespace.
	 * @param containerType the type of the enclosed container.
	 * @see #CONTAINER_ALL
	 * @see #CONTAINER_SEQUENCE
	 * @see #CONTAINER_CHOICE
	 */
	public ComplexType(String name, String namespace, int containerType) {
		this(new QName(name, namespace), containerType);
	}

	/**
	 * This constructor <strong>must</strong> be used when declaring
	 * <em>inline</em> types, which <strong>must not</strong> have a name.
	 * 
	 * @param containerType the type of the enclosed container.
	 * @see #CONTAINER_ALL
	 * @see #CONTAINER_SEQUENCE
	 * @see #CONTAINER_CHOICE
	 */
	public ComplexType(int containerType) {
		this(null, containerType);
	}

	/**
	 * This constructor <strong>must</strong> be used when declaring
	 * <em>top-level</em> types, which have a non-empty name.
	 * 
	 * @param name the qualified name of the type
	 * @param containerType the type of the enclosed container.
	 * @see #CONTAINER_ALL
	 * @see #CONTAINER_SEQUENCE
	 * @see #CONTAINER_CHOICE
	 */
	public ComplexType(QName name, int containerType) {
		super(name);
		switch (containerType) {
			case (CONTAINER_ALL): {
				container = new AllContainer();
				break;
			}
			case (CONTAINER_SEQUENCE): {
				container = new SequenceContainer();
				break;
			}
			case (CONTAINER_CHOICE): {
				container = new ChoiceContainer();
				break;
			}
			default: {
				throw new IllegalArgumentException("Unknown container type: " + containerType);
			}
		}
	}

	ComplexType() {
		this(null);
	}

	ComplexType(QName name) {
		super(name);
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.types.schema.Annotation#getSchemaIdentifier()
	 */
	public int getSchemaIdentifier() {
		return XSD_COMPLEXTYPE;
	}

	/**
	 * Returns the minimum occurrence for the enclosed container.
	 * <p>
	 * The "minOccurs" attribute in XML Schema describes the minimum occurrence
	 * of the enclosed container inside the created XML instance document.
	 * </p>
	 * 
	 * @return the minimum occurrence for the enclosed container.
	 */
	public int getContainerMinOccurs() {
		if (container == null) {
			if (group == null) {
				return 1;
			}
			return group.getContainerMinOccurs();
		}
		return container.getMinOccurs();
	}

	/**
	 * Returns the maximum occurrence for the enclosed container.
	 * <p>
	 * The "maxOccurs" attribute in XML Schema describes the maximum occurrence
	 * of the enclosed container inside the created XML instance document.
	 * </p>
	 * 
	 * @return the maximum occurrence for the enclosed container.
	 */
	public int getContainerMaxOccurs() {
		if (container == null) {
			if (group == null) {
				return 1;
			}
			return group.getContainerMaxOccurs();
		}
		return container.getMaxOccurs();
	}

	/**
	 * Sets the minimum occurrence for the enclosed container.
	 * <p>
	 * The "minOccurs" attribute in XML Schema describes the minimum occurrence
	 * of the enclosed container inside the created XML instance document.
	 * </p>
	 * 
	 * @param min the minimum occurrence for the enclosed container.
	 */
	public void setContainerMinOccurs(int min) {
		if (container == null) {
			return;
		}
		container.setMinOccurs(min);
	}

	/**
	 * Sets the maximum occurrence for the enclosed container.
	 * <p>
	 * The "maxOccurs" attribute in XML Schema describes the maximum occurrence
	 * of the enclosed container inside the created XML instance document.
	 * </p>
	 * 
	 * @param max the maximum occurrence for the enclosed container.
	 */
	public void setContainerMaxOccurs(int max) {
		if (container == null) {
			return;
		}
		container.setMaxOccurs(max);
	}

	/**
	 * Returns <code>true</code> if the enclosed container has element
	 * definitions, <code>false</code> otherwise.
	 * 
	 * @return <code>true</code> if the enclosed container has element
	 *         definitions, <code>false</code> otherwise.
	 */
	public boolean hasElements() {
		if (container == null) {
			if (group == null) {
				return false;
			}
			return group.hasElements();
		}
		return container.hasElements();
	}

	/**
	 * Adds an element to the enclosed container.
	 * 
	 * @param e the element to add.
	 */
	public void addElement(Element e) {
		if (container == null) return;
		container.addElement(e);
	}

	/**
	 * Returns an element with matching name from the container.
	 * 
	 * @param name the qualified name of the element which should be returned.
	 * @return the element.
	 */
	public Element getElementByName(QName name) {
		if (container == null) {
			if (group == null) {
				return null;
			}
			return group.getElementByName(name);
		}
		return container.getElementByName(name);
	}

	/**
	 * Returns an element with matching name from the container.
	 * <p>
	 * This method will <strong>NOT</strong> verify the namespace.
	 * </p>
	 * 
	 * @param name the name of the element which should be returned.
	 * @return the element.
	 */
	public Element getElementByName(String name) {
		if (container == null) {
			if (group == null) {
				return null;
			}
			return group.getElementByName(name);
		}
		return container.getElementByName(name);
	}

	/**
	 * Returns the element count in the enclosed container.
	 * 
	 * @return the element count.
	 */
	public int getElementCount() {
		if (container == null) {
			if (group == null) {
				return 0;
			}
			return group.getElementCount();
		}
		return container.getElementCount();
	}

	/**
	 * Returns an iterator of elements from the enclosed container.
	 * 
	 * @return the elements.
	 */
	public Iterator elements() {
		if (container == null) {
			if (group == null) {
				return EmptyStructures.EMPTY_ITERATOR;
			}
			return group.elements();
		}
		return container.allElements();
	}

	/**
	 * Returns the enclosed container for this complex type.
	 * 
	 * @return the enclosed container.
	 * @see SequenceContainer
	 * @see AllContainer
	 * @see ChoiceContainer
	 */
	public ElementContainer getContainer() {
		return container == null ? group == null ? null : group.getContainer() : container;
	}

	protected void serializeElements(XmlSerializer serializer, Schema schema) throws IOException {
		if (container == null) {
			if (group == null) {
				return;
			}
			group.serialize(serializer, schema);
		}
		container.serialize(serializer, schema);
	}

	void serialize(XmlSerializer serializer, Schema schema) throws IOException {
		serializer.startTag(XMLSCHEMA_NAMESPACE, TAG_COMPLEXTYPE);
		if (name != null) {
			serializer.attribute(null, ATTRIBUTE_NAME, name.getLocalPart());
		}
		if (isAbstract()) {
			serializer.attribute(null, ATTRIBUTE_ABSTRACT, ATTRIBUTE_VALUE_TRUE);
		}
		serializeElements(serializer, schema);
		serializeAttributes(serializer, schema);
		serializeAttributeGroups(serializer, schema);
		serializeAnyAttribute(serializer, schema);
		serializer.endTag(XMLSCHEMA_NAMESPACE, TAG_COMPLEXTYPE);
	}

	void setContainer(ElementContainer container) {
		this.container = container;
	}

	void setGroup(Group group) {
		container = null;
		this.group = group;
	}

}
