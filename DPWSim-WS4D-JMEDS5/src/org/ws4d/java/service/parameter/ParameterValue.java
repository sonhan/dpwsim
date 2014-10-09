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
import java.io.OutputStream;

import org.ws4d.java.constants.SchemaConstants;
import org.ws4d.java.constants.XMLConstants;
import org.ws4d.java.io.xml.XmlSerializerImplementation;
import org.ws4d.java.schema.Attribute;
import org.ws4d.java.schema.AttributeGroup;
import org.ws4d.java.schema.ComplexType;
import org.ws4d.java.schema.Element;
import org.ws4d.java.schema.ExtendedComplexContent;
import org.ws4d.java.schema.ExtendedSimpleContent;
import org.ws4d.java.schema.Group;
import org.ws4d.java.schema.Schema;
import org.ws4d.java.schema.SchemaUtil;
import org.ws4d.java.schema.SimpleType;
import org.ws4d.java.schema.Type;
import org.ws4d.java.service.Operation;
import org.ws4d.java.structures.ArrayList;
import org.ws4d.java.structures.EmptyStructures;
import org.ws4d.java.structures.HashMap;
import org.ws4d.java.structures.Iterator;
import org.ws4d.java.structures.LinkedList;
import org.ws4d.java.structures.List;
import org.ws4d.java.structures.ListIterator;
import org.ws4d.java.structures.ReadOnlyIterator;
import org.ws4d.java.types.QName;
import org.ws4d.java.util.Log;
import org.ws4d.java.util.ParameterUtil;
import org.ws4d.java.util.StringUtil;
import org.xmlpull.v1.XmlSerializer;

/**
 * This class allows object representation of XML instance documents.
 * <p>
 * XML Schema describes the structure of content for XML instance documents.
 * Those definitions are used inside WSDL documents to describe a message's
 * content. It is possible to define XML Schema structures with the classes
 * {@link Schema}, {@link Element}, {@link Attribute}, {@link SimpleType},
 * {@link ComplexType}, {@link Group} and {@link AttributeGroup}. This is at
 * least necessary to invoke SOAP operations (like used in DPWS).<br />
 * A complex type consists of a qualified name and the description of the
 * content structure.
 * </p>
 * <h3>XML Schema</h3>
 * <p>
 * XML Schema describes the structure of the content for a XML instance
 * document. Each element is dedicated to a specific data type. XML Schema comes
 * with built-in primitive data types like <i>string</i>, <i>boolean</i>,
 * <i>decimal</i> and derived data types like <i>byte</i>, <i>int</i>,
 * <i>token</i> and <i>positiveInteger</i>. It is also possible to define one's
 * own derived data types. An XML Schema could look like this:
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
 * The <i>person</i> element defined above can be used as <strong>input</strong>
 * or <strong>output</strong> parameter of an operation. This will allow to use
 * this parameter within a service. As shown in the XML Schema part, an element
 * defined inside a XML Schema will be used to create XML instance documents.
 * The Framework allows to create those XML instance documents with this class.
 * A parameter value can be created from an element with the
 * {@link ParameterValue#createElementValue(Element)} method, or will be
 * pass-through within action invocation.
 * </p>
 * <p>
 * The <code>ParameterValue</code> class allows nested structures like seen in
 * XML. An object of this class represents a single entry in a XML instance
 * document. The XML shown above, has an root element named "person" containing
 * three inner-elements, firstname, lastname and age.<br />
 * This would lead to an parameter value with three nested inner-elements. The
 * <code>ParameterValue</code> class allows to access the element directly and
 * any inner-element. <strong>To access the value of a parameter it is necessary
 * to check the type of the parameter and cast to the correct
 * implementation.</strong> The framework comes along with the implementation of
 * xs:string {@link StringValue}, xs:QNAME {@link QNameValue} and
 * xs:base64binary {@link AttachmentValue}. It is possible to register own
 * implementation of XML Schema datatypes. If no implementation matches the
 * given data type a it will be handles as xs:string (fallback). The following
 * lines of code, will show the usage for the structure defined above:
 * </p>
 * 
 * <pre>
 * // create ParameterValue from element
 * ParameterValue personInstance = ParameterValue.createElementValue(person);
 * 
 * // as person does not have any values to set, set the value of the
 * // inner-elements.
 * // direct access using the path (something like XPath).
 * ParameterValue fname = personInstance.get(&quot;firstname&quot;);
 * ParameterValue lname = personInstance.setValue(&quot;lastname&quot;);
 * ParameterValue a = personInstance.setValue(&quot;age&quot;);
 * 
 * // check for correct type, cast and set the value
 * if (fname.getValueType() == ParameterValue.TYPE_STRING) {
 * 	StringValue firstname = (StringValue) fname;
 * 	// set value for the string
 * 	firstname.set(&quot;John&quot;);
 * }
 * 
 * if (lname.getValueType() == ParameterValue.TYPE_STRING) {
 * 	StringValue lastname = (StringValue) lname;
 * 	// set value for the string
 * 	lastname.set(&quot;Doe&quot;);
 * }
 * 
 * // As there is not implementation for xs:integer we must use the xs:string
 * // fallback here
 * if (a.getValueType() == ParameterValue.TYPE_STRING) {
 * 	StringValue age = (StringValue) a;
 * 	// set value for the string
 * 	lastname.set(&quot;66&quot;);
 * }
 * 
 * // check for correct type, cast and set the value
 * if (fname.getValueType() == ParameterValue.TYPE_STRING) {
 * 	StringValue firstname = (StringValue) fname;
 * 	// set value for the string
 * 	String fn = firstname.get();
 * }
 * </pre>
 * <p>
 * The <strong>path</strong> value used in different methods, allows direct
 * access the inner-elements. Let us assume the XML content below:
 * </p>
 * 
 * <pre>
 * &lt;?xml version=&quot;1.0&quot;?&gt;
 * &lt;person&gt;
 *    &lt;firstname&gt;John&lt;/firstname&gt;
 *    &lt;lastname&gt;Doe&lt;/lastname&gt;
 *    &lt;age&gt;66&lt;/age&gt;
 *    &lt;address&gt;
 *       &lt;street&gt;Mainstreet 20&lt;/firstname&gt;
 *       &lt;city&gt;Los Wochos&lt;/lastname&gt;
 *       &lt;phone&gt;555-123-780-JOHNDOE&lt;/phone&gt;
 *       &lt;phone&gt;555-123-780-XML&lt;/phone&gt;
 *    &lt;/address&gt;
 * &lt;/person&gt;
 * </pre>
 * <p>
 * To access the elements like street, or even the both phone elements, it
 * necessary to extend the path. The path is always relative to the current
 * element. Every next entry in the path is divided by a slash (\). No set path
 * points the current element. You can use the {@link #setValue(String)} and
 * {@link #getValue()} methods for direct access without path. If an entry
 * exists more then once, like the phone element in the example above, an
 * specific element can be accessed by using an index. The index starts with 0.
 * Omitting the index is like using 0.
 * </p>
 * <p>
 * <strong>path syntax:</strong>
 * child[index]/child-from-child[index]/child-from-child-from-chil[index]/ ...
 * and so on.
 * </p>
 * 
 * <pre>
 * // create ParameterValue from element
 * ParameterValue personInstance = ParameterValue.createElementValue(person);
 * 
 * // as person does not have any values to set, set the value of the
 * // inner-elements.
 * // direct access using the path (something like XPath).
 * personInstance.get(&quot;firstname&quot;);
 * </pre>
 * 
 * <h3>Notice</h3>
 * <p>
 * The {@link ParameterUtil} class offers shortcut methods for the most common
 * cast, get and set operations for the build-in implementation of datatypes.
 * </p>
 * 
 * @see Element
 * @see Operation
 * @see StringValue
 * @see QNameValue
 * @see AttachmentValue
 * @see ParameterUtil
 */
public class ParameterValue {
	
	public static final boolean		ALLOW_NOINDEX			= true;

	public static final int			TYPE_UNKNOWN			= -1;

	public static final int			TYPE_COMPLEX			= 0;

	public static final int			TYPE_STRING				= 1;

	protected static final String	TYPE_STRING_CLASS		= "org.ws4d.java.service.parameter.StringValue";

	public static final int			TYPE_ATTACHMENT			= 2;

	protected static final String	TYPE_ATTACHMENT_CLASS	= "org.ws4d.java.service.parameter.AttachmentValue";

	public static final int			TYPE_QNAME				= 3;

	protected static final String	TYPE_QNAME_CLASS		= "org.ws4d.java.service.parameter.QNameValue";

	protected String				override				= null;

	protected Type					type					= null;

	protected Type					instanceType			= null;

	protected int					min						= 1;

	protected int					max						= 1;

	protected boolean				nil						= false;

	protected QName					name					= null;

	protected List					children				= EmptyStructures.EMPTY_LIST;

	protected HashMap				attributes				= EmptyStructures.EMPTY_MAP;

	protected HashMap				namespaceCache			= null;

	/**
	 * This map contains mappings from XML Schema datatypes to the classes which
	 * will be loaded at runtime. <Type, String>
	 */
	protected static final HashMap	registeredValues		= new HashMap();

	static {
		registeredValues.put(SchemaUtil.getSchemaType(SchemaUtil.TYPE_STRING), TYPE_STRING_CLASS);
		registeredValues.put(SchemaUtil.getSchemaType(SchemaUtil.TYPE_BASE64_BINARY), TYPE_ATTACHMENT_CLASS);
		registeredValues.put(SchemaUtil.getSchemaType(SchemaUtil.TYPE_QNAME), TYPE_QNAME_CLASS);
	}

	/**
	 * Returns the namespaces used by this parameter value.
	 * <p>
	 * This method allows to collect all namespaces and use it if necessary.
	 * </p>
	 * 
	 * @return a {@link List} of {@link QName}.
	 */
	public List getNamespaces() {
		List ns = new LinkedList();
		ns.add(name);
		if (attributes != EmptyStructures.EMPTY_MAP) {
			Iterator it = attributes.values().iterator();
			while (it.hasNext()) {
				ParameterAttribute pa = (ParameterAttribute) it.next();
				ns.add(pa.getName());
			}
		}
		return ns;
	}

	/**
	 * Register a class for a XML Schema datatype {@link Type}.
	 * 
	 * @param type the type which should be used.
	 * @param clazz the class which should be used to handle that type.
	 * @return the classname if already set.
	 */
	public static String register(Type type, String clazz) {
		return (String) registeredValues.put(type, clazz);
	}

	/**
	 * Unregister a class for a XML Schema datatype {@link Type}.
	 * 
	 * @param type the type which should be used.
	 * @return the classname.
	 */
	public static String unregister(Type type) {
		return (String) registeredValues.remove(type);
	}

	/**
	 * Returns the VALUE TYPE for this parameter.
	 * <p>
	 * A VALUE TYPE should be a unique representation of a
	 * {@link ParameterValue} implementation which allows to identify the
	 * implementation and cast correctly.
	 * 
	 * @return the VALUE TYPE.
	 */
	public int getValueType() {
		return TYPE_COMPLEX;
	}

	/**
	 * Allows to override the serialization of this parameter.
	 * <p>
	 * <h3>NOTICE:</h3> The given <code>String</code> can contain anything but
	 * SHOULD contain correct XML data. <strong>This method should be used for
	 * debug purposes.</strong> A nested parameter can be overriden too.
	 * </p>
	 * <p>
	 * Set to <code>null</code> to disable the override.
	 * </p>
	 * 
	 * @param value the value which should override the parameter serialization,
	 *            or <code>null</code> if the parameter should not be
	 *            overridden.
	 */
	public void overrideSerialization(String value) {
		override = value;
	}

	/**
	 * Returns whether this parameter value is overridden or not.
	 * 
	 * @return <code>true</code> the parameter serialization is overridden,
	 *         <code>false</code> otherwise.
	 */
	public boolean isOverriden() {
		return (override != null);
	}

	/**
	 * Sets the value of an attribute of this parameter value with given value.
	 * 
	 * @param attribute the name of the attribute.
	 * @param value the value of the attribute.
	 */
	public void setAttributeValue(String attribute, String value) {
		ParameterAttribute a = (ParameterAttribute) attributes.get(attribute);
		if (a == null) {
			/*
			 * Use no namespace [null], or use the namespace
			 * [name.getNamepsace()] from this parameter?
			 */
			a = new ParameterAttribute(new QName(attribute, null));
			add(a);
		}
		a.setValue(value);
	}

	/**
	 * Returns the value of an attribute for this parameter value.
	 * 
	 * @param attribute the attribute to get the value of.
	 * @return the value of the attribute.
	 */
	public String getAttributeValue(String attribute) {
		if (!hasAttributes()) return null;
		ParameterAttribute a = (ParameterAttribute) attributes.get(attribute);
		if (a == null) return null;
		return a.getValue();
	}

	public void add(ParameterAttribute attribute) {
		if (attributes == EmptyStructures.EMPTY_MAP) {
			attributes = new HashMap();
		}
		attributes.put(attribute.getName().getLocalPart(), attribute);
	}

	public void addAnyAttribute(QName name, String value) {
		ParameterAttribute attribute = new ParameterAttribute(name);
		attribute.setValue(value);
		add(attribute);
	}

	/**
	 * Returns <code>true</code> if this parameter value has attributes,
	 * <code>false</code> otherwise.
	 * 
	 * @return <code>true</code> if this parameter value has attributes,
	 *         <code>false</code> otherwise.
	 */
	public boolean hasAttributes() {
		if (attributes == null || attributes.size() == 0) return false;
		return true;
	}

	/**
	 * Returns an iterator of attributes for this parameter value.
	 * 
	 * @return an iterator of attributes for this parameter value.
	 */
	public Iterator attributes() {
		return attributes.values().iterator();
	}

	/**
	 * Returns an iterator over the qualified names of all attributes within
	 * this parameter value.
	 * 
	 * @return an iterator over {@link QName} instances, which represent the
	 *         names of this parameter value's attributes
	 */
	public Iterator attributeNames() {
		List l = new ArrayList(attributes.size());
		for (Iterator it = attributes.values().iterator(); it.hasNext();) {
			ParameterAttribute attribute = (ParameterAttribute) it.next();
			l.add(attribute.getName());
		}
		return new ReadOnlyIterator(l);
	}

	/**
	 * Set the name of this parameter value.
	 * 
	 * @param name the name.
	 */
	void setName(QName name) {
		this.name = name;
	}

	/**
	 * Set the type of this parameter value.
	 * 
	 * @param type the type.
	 */
	void setType(Type type) {
		this.type = type;
	}

	public void setInstanceType(Type instanceType) {
		this.instanceType = instanceType;
	}

	/**
	 * Set whether this parameter should carry values or not.
	 * 
	 * @param nil <code>true</code> this parameter will not have any values and
	 *            the XML instance nil will be set.
	 *            <strong>xsi:nil="true"</strong>
	 */
	public void setNil(boolean nil) {
		this.nil = nil;
	}

	/**
	 * Returns whether the XML instance <strong>nil</strong> value is set or
	 * not.
	 * 
	 * @return <code>true</code> if the XML instance <strong>nil</strong> value
	 *         is set, <code>false</code> otherwise.
	 */
	public boolean isNil() {
		return nil;
	}

	/**
	 * Returns the type of this parameter value.
	 * 
	 * @return the parameter value.
	 */
	public Type getType() {
		return type;
	}

	/**
	 * Returns the instance type of this parameter value (in accordance to
	 * xsi:Type attribute). If no instance type is set, the declared type is
	 * returned.
	 * 
	 * @return the instance type of the parameter value.
	 */
	public Type getInstanceType() {
		return instanceType == null ? getType() : instanceType;
	}

	void setMinOccurs(int min) {
		this.min = min;
	}

	/**
	 * Returns the the minimum occurrence for this parameter value.
	 * <p>
	 * The "minOccurs" attribute in XML Schema describes the minimum occurrence
	 * of this element inside the created XML instance document.
	 * </p>
	 * 
	 * @return the minimum occurrence of this parameter value.
	 */
	public int getMinOccurs() {
		return min;
	}

	void setMaxOccurs(int max) {
		this.max = max;
	}

	/**
	 * Returns the the maximum occurrence for this parameter value.
	 * <p>
	 * The "maxOccurs" attribute in XML Schema describes the maximum occurrence
	 * of this element inside the created XML instance document.
	 * </p>
	 * 
	 * @return the maximum occurrence of this parameter value.
	 */
	public int getMaxOccurs() {
		return max;
	}

	/**
	 * Returns the name of the parameter value. The name of the parameter value
	 * is the name of the entry inside the XML document.
	 * 
	 * @return the parameter value name
	 */
	public QName getName() {
		return name;
	}

	ParameterValue getChild(QName name, int index, boolean reset) {
		if (name == null || index < 0) return null;
		Iterator it = children.iterator();
		int i = -1;
		while (it.hasNext()) {
			ParameterValue child = (ParameterValue) it.next();
			if (child.getName().equals(name)) {
				i++;
			} else if (reset) {
				i = -1;
			}
			if (i == index) {
				return child;
			}
		}
		return null;
	}

	int countChildren(QName name, boolean reset) {
		if (name == null) return 0;
		Iterator it = children.iterator();
		int i = 0;
		while (it.hasNext()) {
			ParameterValue child = (ParameterValue) it.next();
			if (child.getName().equals(name)) {
				i++;
			} else if (reset) {
				i = -1;
			}
		}
		return i;
	}

	/**
	 * Adds an inner-element to this parameter value. This method is necessary
	 * to create nested structures.
	 * 
	 * @param value the parameter value to add.
	 */
	public synchronized void add(ParameterValue value) {
		if (children == EmptyStructures.EMPTY_LIST) {
			children = new LinkedList();
		}
		namespaceCache = null;
		children.add(value);
	}

	public synchronized void remove(ParameterValue value) {
		if (children != EmptyStructures.EMPTY_LIST) {
			children.remove(value);
		}
		namespaceCache = null;
	}

	/**
	 * Returns <code>true</code> if this parameter value has inner-elements,
	 * <code>false</code> otherwise.
	 * 
	 * @return <code>true</code> if this parameter value has inner-elements,
	 *         <code>false</code> otherwise.
	 */
	public boolean hasChildren() {
		if (children == null || children.size() == 0) return false;
		return true;
	}

	/**
	 * Returns the number of inner-elements for the parameter value given by the
	 * path.
	 * 
	 * @param path the path to access the inner-element.
	 * @return the amount of inner-elements.
	 */
	public int getChildrenCount(String path) {
		Iterator it = getChildren(this, path);
		if (it == null) return 0;
		int i = 0;
		while (it.hasNext()) {
			it.next();
			i++;
		}
		return i;
	}

	/**
	 * Returns the number of inner-elements for the parameter value.
	 * 
	 * @param path the path to access the inner-element.
	 * @return the amount of inner-elements.
	 */
	public int getChildrenCount() {
		return children.size();
	}

	/**
	 * Returns an iterator of inner-elements for the parameter value given by
	 * the path.
	 * 
	 * @param path the path to access the inner-element.
	 * @return iterator of inner-elements.
	 */
	public Iterator getChildren(String path) {
		return getChildren(this, path);
	}

	Iterator getChildren(ParameterValue wVal, String path) {

		ParameterPath pp = new ParameterPath(path);
		if (pp.getDepth() == 0) {
			return null;
		}

		int depth = 0;

		Type t = wVal.getInstanceType();
		if (!(t.isComplexType())) {
			return EmptyStructures.EMPTY_ITERATOR;
		}

		String node = pp.getNode(depth);
		int index = pp.getIndex(depth);
		String npath = pp.getPath(depth + 1);

		String namespace = wVal.name.getNamespace();
		QName search = new QName(node, namespace);

		if (pp.getDepth() > 1) {
			ParameterValue child = wVal.getChild(search, index, false);
			if (child == null) {
				return null;
			}
			return getChildren(child, npath);
		} else {
			ArrayList list = new ArrayList(1);
			int i = wVal.countChildren(search, false);
			for (int j = 0; j < i; j++) {
				ParameterValue child = wVal.getChild(search, j, false);
				list.add(child);
			}
			return list.iterator();
		}
	}

	/**
	 * Returns <code>true</code> if this parameter value is based on a complex
	 * type, <code>false</code> otherwise.
	 * 
	 * @return <code>true</code> if this parameter value is based on a complex
	 *         type, <code>false</code> otherwise.
	 */
	public boolean hasChildrenFromType() {
		if (type == null) return false;
		if (type instanceof ComplexType) {
			ComplexType complex = (ComplexType) type;
			return complex.hasElements();
		}
		return false;
	}

	/**
	 * Returns an iterator of types for all inner-elements.
	 * 
	 * @return an iterator of types for all inner-elements.
	 */
	public Iterator childrenFromType() {
		if (type == null) return EmptyStructures.EMPTY_ITERATOR;
		if (type.isComplexType()) {
			List list = new LinkedList();
			ComplexType complex = (ComplexType) type;
			for (Iterator it = complex.elements(); it.hasNext();) {
				Element e = (Element) it.next();
				ParameterValue pv = ParameterValue.createElementValue(e);
				pv.setMaxOccurs(e.getMaxOccurs());
				pv.setMinOccurs(e.getMinOccurs());
				list.add(pv);
			}
			return list.iterator();

		}
		return EmptyStructures.EMPTY_ITERATOR;
	}

	/**
	 * Returns an iterator of inner-elements for this parameter value.
	 * 
	 * @return an iterator of inner-elements for this parameter value.
	 */
	public Iterator children() {
		return children.iterator();
	}

	/**
	 * Returns an listiterator of inner-elements for this parameter value.
	 * 
	 * @return an listiterator of inner-elements for this parameter value.
	 */
	public ListIterator getChildrenList() {
		return children.listIterator();
	}

	/**
	 * Resolve the types based on the given XML schema.
	 * 
	 * @param s the XML schema which contains the types for this parameter
	 *            value.
	 */
	public void resolveTypes(Schema s) {
		Element e = s.getElement(name);
		if (e != null) {
			Type t = e.getType();
			type = t;
		} else {
			return;
		}
		Iterator it = children();
		while (it.hasNext()) {
			ParameterValue child = (ParameterValue) it.next();
			if (child.hasChildren()) {
				child.resolveType((ComplexType) type, s);
			}
		}
	}

	public ParameterValue removeChild(String path) {
		ParameterValue pv = get(this, null, path, false);
		if (pv != null && !this.equals(pv)) {
			remove(pv);
		}
		return pv;
	}

	public ParameterValue createChild(String path) {
		ParameterValue pv = get(this, null, path, true);
		return pv;
	}

	public ParameterValue createChild(String path, Type instanceType) {
		return get(this, null, path, instanceType, true);
	}

	/**
	 * Serializes the parameter value with a XML serializer.
	 * 
	 * @param serializer the XML serializer.
	 * @throws IOException throws an exception if the parameter value could not
	 *             be serialized correctly.
	 */
	protected void serialize(XmlSerializer serializer, HashMap nsCache) throws IOException {
		if (override != null) {
			/*
			 * Override the given parameter. This serializes the given string
			 * and not the content of the parameter.
			 */
			serializer.ignorableWhitespace(override);
			return;
		} else {
			serialize0(serializer, nsCache);
		}

	}

	/**
	 * Serializes the parameter value with a XML serializer.
	 * 
	 * @param serializer the XML serializer.
	 * @throws IOException throws an exception if the parameter value could not
	 *             be serialized correctly.
	 */
	public void serialize(XmlSerializer serializer) throws IOException {
		if (override != null) {
			/*
			 * Override the given parameter. This serializes the given string
			 * and not the content of the parameter.
			 */
			serializer.ignorableWhitespace(override);
			return;
		} else {
			serialize0(serializer, namespaceCache);
		}

	}

	/**
	 * Serializes the parameter value into an XML instance document on a given
	 * stream.
	 * 
	 * @param out the stream to serialize to.
	 * @throws IOException throws an exception if the parameter value could not
	 *             be serialized correctly.
	 */
	public void serialize(OutputStream out) throws IOException {
		XmlSerializer serializer = new XmlSerializerImplementation();
		serializer.setOutput(out, XMLConstants.ENCODING);
		// serializer.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output",
		// true);
		serializer.startDocument(XMLConstants.ENCODING, null);
		serialize0(serializer, namespaceCache);
		serializer.endDocument();
	}

	/**
	 * Resolve the children based on the root complex type.
	 * 
	 * @param ct the complex type.
	 */
	private void resolveType(ComplexType ct, Schema s) {
		Element e = searchElement(ct, name);
		if (e == null) {
			if (ct.getName() != null && ct.getName().equals(new QName(SchemaUtil.TYPE_ANYTYPE, SchemaConstants.XMLSCHEMA_NAMESPACE))) {
				if (s != null) {
					/*
					 * search inside linked schema
					 */
					e = s.getElement(name);
					if (e == null) {
						Log.error("Cannot resolve type in schema. Element not found. (type= " + ct + ", element name=" + name + ", schema=" + s + ")");
					}
				} else {
					Log.error("Cannot resolve type in any type. Element not found. (type= " + ct + ", element name=" + name + ", schema=" + s + ")");
				}
			} else {
				Log.error("Cannot resolve type. Element not found. (type= " + ct + ", element name=" + name + ", schema=" + s + ")");
				return;
			}
			
		}
		Type t = e.getType();
		type = t;
		Iterator it = children();
		while (it.hasNext()) {
			ParameterValue child = (ParameterValue) it.next();
			if (child.hasChildren()) {
				child.resolveType((ComplexType) type, s);
			}
		}
	}

	/**
	 * Resolve the element for the given type. Check for extended content, maybe
	 * the element was defined somewhere else.
	 * 
	 * @param t the type to check.
	 * @param name the name of the element to find.
	 * @return the element, or <code>null</code> if no element found.
	 */
	protected static Element searchElement(ComplexType t, QName name) {
		if (t == null) return null;
		Element e = t.getElementByName(name);
		if (e != null) return e;
		if (t.getSchemaIdentifier() == SchemaConstants.XSD_EXTENDEDCOMPLEXCONTENT) {
			ExtendedComplexContent ect = (ExtendedComplexContent) t;
			Type base = ect.getBase();
			int i = base.getSchemaIdentifier();
			if (i == SchemaConstants.XSD_EXTENDEDCOMPLEXCONTENT || i == SchemaConstants.XSD_RESTRICTEDCOMPLEXCONTENT || i == SchemaConstants.XSD_COMPLEXTYPE) {
				e = searchElement((ComplexType) base, name);
			}
		}
		return e;
	}

	/**
	 * Resolve the element for the given type. Check for extended content, maybe
	 * the element was defined somewhere else.
	 * <p>
	 * This method will <strong>NOT</strong> check the namespace of the element.
	 * This allows to search an element in other namespaces.
	 * </p>
	 * 
	 * @param t the type to check.
	 * @param name the name of the element to find.
	 * @return the element, or <code>null</code> if no element found.
	 */
	protected static Element searchElementNamespaceless(ComplexType t, String name) {
		Element e = t.getElementByName(name);
		if (e != null) return e;
		if (t.getSchemaIdentifier() == SchemaConstants.XSD_EXTENDEDCOMPLEXCONTENT) {
			ExtendedComplexContent ect = (ExtendedComplexContent) t;
			Type base = ect.getBase();
			int i = base.getSchemaIdentifier();
			if (i == SchemaConstants.XSD_EXTENDEDCOMPLEXCONTENT || i == SchemaConstants.XSD_RESTRICTEDCOMPLEXCONTENT || i == SchemaConstants.XSD_COMPLEXTYPE) {
				e = searchElementNamespaceless((ComplexType) base, name);
			}
		}
		return e;
	}

	/**
	 * The main serialize method. This method serializes the parameter.
	 * 
	 * @param serializer
	 * @param nsCache
	 * @throws IOException
	 */
	protected synchronized void serialize0(XmlSerializer serializer, HashMap nsCache) throws IOException {
		if (nsCache == null) {
			namespaceCache = collectNamespaces(serializer);
			nsCache = namespaceCache;
		}
		serializeStartTag(serializer, nsCache);
		serializeAttributes(serializer);
		serializeChildren(serializer, nsCache);
		serializeEndTag(serializer);
	}

	protected final HashMap collectNamespaces(XmlSerializer serializer) {
		HashMap ns = new HashMap();
		ParameterValue[] nodes = { this };
		collectNamespaces(ns, nodes, serializer.getDepth());

		HashMap nsSerialization = new HashMap();
		Iterator it = ns.entrySet().iterator();
		while (it.hasNext()) {
			HashMap.Entry entry = (HashMap.Entry) it.next();
			QName namespace = (QName) entry.getKey();
			ParameterValue[] p = (ParameterValue[]) ns.get(namespace);
			List l = (List) nsSerialization.get(p[p.length - 1]);
			if (l == null) {
				l = new LinkedList();
				l.add(namespace);
				nsSerialization.put(p[p.length - 1], l);
			} else if (!l.contains(namespace)) {
				l.add(namespace);
			}
		}
		return nsSerialization;
	}

	protected final void collectNamespaces(HashMap namespaces, ParameterValue[] nodes, int depth) {
		List ns = getNamespaces();
		Iterator it = ns.iterator();
		while (it.hasNext()) {
			QName n = (QName) it.next();
			ParameterValue[] on = (ParameterValue[]) namespaces.get(n);
			if (on == null) {
				namespaces.put(n, nodes);
			} else {
				int min = Math.min(on.length, nodes.length);
				for (int i = 0; i < min; i++) {
					if (!on[i].equals(nodes[i])) {
						ParameterValue[] nn = new ParameterValue[i];
						System.arraycopy(on, 0, nn, 0, i);
						namespaces.put(n, nn);
						break;
					}
				}
			}
		}
		if (hasChildren()) {
			for (Iterator children = children(); children.hasNext();) {
				ParameterValue[] nn = new ParameterValue[nodes.length + 1];
				System.arraycopy(nodes, 0, nn, 0, nodes.length);
				ParameterValue child = (ParameterValue) children.next();
				nn[nodes.length] = child;
				child.collectNamespaces(namespaces, nn, depth + 1);
			}
		}
	}

	protected final void serializeStartTag(XmlSerializer serializer, HashMap nsCache) throws IOException {
		List l = (List) nsCache.get(this);
		if (l != null) {
			Iterator it = l.iterator();
			while (it.hasNext()) {
				QName namespace = (QName) it.next();
				String ns = namespace.getNamespace();
				if (ns == null || "".equals(ns)) {
					continue;
				}
				String prefix = serializer.getPrefix(ns, false);
				if (prefix == null) {
					serializer.setPrefix(namespace.getPrefix(), namespace.getNamespace());
				}
			}
		}
		serializer.startTag(getName().getNamespace(), getName().getLocalPart());
		if (isNil()) {
			serializer.attribute(SchemaConstants.XSI_NAMESPACE, SchemaConstants.ATTRIBUTE_XSINIL, "true");
		}
		if (instanceType != null && instanceType != type) {
			QName qn = instanceType.getName();
			String prefix = serializer.getPrefix(qn.getNamespace(), true);
			if (prefix == null) {
				serializer.attribute(SchemaConstants.XSI_NAMESPACE, SchemaConstants.ATTRIBUTE_XSITYPE, qn.getLocalPart());
			} else {
				qn.setPrefix(prefix);
				serializer.attribute(SchemaConstants.XSI_NAMESPACE, SchemaConstants.ATTRIBUTE_XSITYPE, qn.getLocalPartPrefixed());
			}
		}
	}

	protected final void serializeEndTag(XmlSerializer serializer) throws IOException {
		serializer.endTag(getName().getNamespace(), getName().getLocalPart());
	}

	protected final void serializeAttributes(XmlSerializer serializer) throws IOException {
		if (hasAttributes()) {
			for (Iterator it = attributes(); it.hasNext();) {
				ParameterAttribute attribute = (ParameterAttribute) it.next();
				String value = attribute.getValue();
				if (value != null) {
					serializer.attribute(attribute.getName().getNamespace(), attribute.getName().getLocalPart(), attribute.getValue());
				}
			}
		}
	}

	protected final void serializeChildren(XmlSerializer serializer, HashMap nsCache) throws IOException {
		if (hasChildren()) {
			for (Iterator it = children(); it.hasNext();) {
				ParameterValue child = (ParameterValue) it.next();
				child.serialize(serializer, nsCache);
			}
		}
	}

	public ParameterValue get(String path) throws IndexOutOfBoundsException, IllegalArgumentException {
		return ParameterValue.get(this, null, path, false);
	}

	/**
	 * Creates an XML instance document representation from a given XML Schema
	 * element.
	 * 
	 * @param element the element to create the representation from.
	 * @return the XML instance document representation.
	 */
	public static ParameterValue createElementValue(Element element) {
		return createElementValue(element, null);
	}

	public static ParameterValue createElementValue(Element element, Type instanceType) {
		if (element == null) {
			return null;
		}
		Type tmpType = instanceType == null ? element.getType() : instanceType;
		ParameterValue pVal = null;
		if (tmpType.isComplexType()) {
			pVal = new ParameterValue();
		} else {
			pVal = load(tmpType);
		}
		pVal.setMaxOccurs(element.getMaxOccurs());
		pVal.setMinOccurs(element.getMinOccurs());
		pVal.setName(element.getName());
		pVal.setType(element.getType());
		pVal.setInstanceType(instanceType);
		addAttributesFromType(pVal, tmpType);
		return pVal;
	}

	protected static Type addAttributesFromType(ParameterValue pv, Type type) {
		while (type != null) {
			for (Iterator atts = type.allAttributes(); atts.hasNext();) {
				Attribute att = (Attribute) atts.next();
				Type attType = att.getType();
				ParameterAttribute pAtt = new ParameterAttribute(att.getName());
				if (att.getDefault() != null) {
					pAtt.setValue(att.getDefault());
				}
				pAtt.setType(attType);
				pv.add(pAtt);
			}
			int schemaId = type.getSchemaIdentifier();
			if (schemaId == SchemaConstants.XSD_EXTENDEDCOMPLEXCONTENT) {
				type = ((ExtendedComplexContent) type).getBase();
			} else if (schemaId == SchemaConstants.XSD_EXTENDEDSIMPLECONTENT) {
				type = ((ExtendedSimpleContent) type).getBase();
			} else {
				type = null;
			}
		}
		return type;
	}

	protected static ParameterValue load(Type t) {

		String className = (String) registeredValues.get(t);
		if (className == null) {
			// Log.warn("Cannot load value interpreter. Type " + t.getName() +
			// " does not match. Using " +
			// SchemaUtil.getSchemaType(SchemaUtil.TYPE_STRING) + " instead.");
			className = TYPE_STRING_CLASS;
		}

		ParameterValue v = null;
		Class clazz;
		try {
			clazz = Class.forName(className);
			v = (ParameterValue) clazz.newInstance();
		} catch (ClassNotFoundException e) {
			throw new RuntimeException("Cannot load value interpreter. Class " + className + " not found.");
		} catch (InstantiationException e) {
			throw new RuntimeException("Cannot load value interpreter. Cannot create object for " + className + ".");
		} catch (IllegalAccessException e) {
			throw new RuntimeException("Cannot load value interpreter. Access not allowed for " + className + ".");
		}

		return v;
	}

	private static ParameterValue get(ParameterValue wVal, ParameterValue parent, String path, boolean cr) throws IndexOutOfBoundsException, IllegalArgumentException {
		return get(wVal, parent, path, null, cr);
	}

	private static ParameterValue get(ParameterValue wVal, ParameterValue parent, String path, Type instanceType, boolean cr) throws IndexOutOfBoundsException, IllegalArgumentException {
		if (path == null) return wVal;
		ParameterPath pp = wVal.new ParameterPath(path);
		if (pp.getDepth() == 0) {
			return wVal;
		}

		int depth = 0;

		Type t = wVal.getInstanceType();
		if (!(t.isComplexType())) {
			return wVal;
		}

		String node = pp.getNode(depth);
		int index = pp.getIndex(depth);
		String npath = pp.getPath(depth + 1);

		ComplexType complex = (ComplexType) t;
		String namespace = wVal.name.getNamespace();
		QName search = new QName(node, namespace);

		/*
		 * Possibility check. An element should exists inside the underlying
		 * container, or we cannot add it here.
		 */
		Element e = searchElement(complex, search);
		
		if (e == null) {
			if (complex.getName() != null && complex.getName().equals(new QName(SchemaUtil.TYPE_ANYTYPE, SchemaConstants.XMLSCHEMA_NAMESPACE))) {
				/*
				 * is ANY type
				 */
				if (parent != null) {
					Type pT = parent.getType();
					if (pT == null) {
						throw new IllegalArgumentException("Parent parameter has no type set! parent=" + parent);
					}
					/*
					 * TODO 13.05.2011: We should create a schema repository
					 * ... the definition for the searched type can be part
					 * of any schema we ever used within a service.
					 */
					Schema s = pT.getParentSchema();
					if (s != null) {
						/*
						 * search inside linked schema
						 */
						e = s.getElement(search);
					}
				}
			} else {
				String n = search.getLocalPart();
				e = searchElementNamespaceless(complex, n);
			}
		}

		if (e == null) {
			throw new IndexOutOfBoundsException("No child found. Missing: " + node);
		}

		int eMin = e.getMinOccurs();
		int eMax = e.getMaxOccurs();

		Type eType = e.getType();

		int cMax = complex.getContainerMaxOccurs();

		int dMax = -1;

		/*
		 * It is necessary to check the occurrence from both, the element itself
		 * and the model containing this element. Maybe an element is listed
		 * twice (occurrence=2) with an maximum occurrence 1, but the model has
		 * maximum occurrence 5. In this case the element can have occurrence 2,
		 * because the model can exits 5 times.
		 */
		if (eMax == -1 && cMax == -1) dMax = -1;
		if ((eMax == 0 && cMax == -1) || (eMax == -1 && cMax == 0)) dMax = 0;
		if (eMax >= 1 && cMax >= 1) dMax = eMax * cMax;

		if ((index + 1) > dMax && dMax != -1) {
			throw new IndexOutOfBoundsException("Cannot create child. index=" + index + ", max=" + dMax + ", model and element occurrence.");
		}

		ParameterValue child = null;
		
		int c = wVal.countChildren(search, false);
		
		boolean hIndex = pp.hasIndex(depth);
		
		if (ALLOW_NOINDEX && !hIndex && pp.getDepth() == 1 && cr) {
			index += c;
		}
		
		if (index < wVal.getChildrenCount()) {
			child = wVal.getChild(search, index, false);
		} else {
			int diff = index - wVal.getChildrenCount();
			for (int i = 0; i < diff; i++) {
				ParameterValue bastard = ParameterValue.createElementValue(e, instanceType);
				wVal.add(bastard);
			}
			child = ParameterValue.createElementValue(e, instanceType);
			wVal.add(child);
		}

		if (child == null) {
			/*
			 * okay, no child with this name found. we need to create the whole
			 * structure...
			 */
			child = ParameterValue.createElementValue(e, instanceType);
			wVal.add(child);
		}

		if (child.getType() != null) {
			if (child.getType() != e.getType()) throw new IllegalArgumentException("Type mismatch for " + node);
		} else {
			Type inType = e.getType();
			child.setType(inType);
			child.setMaxOccurs(eMax);
			child.setMinOccurs(eMin);
			inType = addAttributesFromType(child, inType);
		}

		if (pp.getDepth() > 1) {
			return get(child, wVal, npath, cr);
		}

		return child;
	}

	/**
	 * Returns the number of <em>direct</em> children of <code>pv</code> with a
	 * local name of <code>childLocalName</code>. Returns <code>0</code>, if
	 * either <code>pv</code> or <code>childLocalName</code> are
	 * <code>null</code>.
	 * 
	 * @param pv the parameter value instance, which of to count the direct
	 *            children with the given local name
	 * @param childLocalName the local name of children to look for
	 * @return the number of direct children of <code>pv</code> with the
	 *         specified local name
	 */
	public static int childCount(ParameterValue pv, String childLocalName) {
		if (pv == null || childLocalName == null) {
			return 0;
		}
		int count = 0;
		for (Iterator it = pv.children(); it.hasNext();) {
			ParameterValue child = (ParameterValue) it.next();
			if (childLocalName.equals(child.getName().getLocalPart())) {
				count++;
			}
		}
		return count;
	}

	public String toString() {
		StringBuffer sBuf = new StringBuffer();
		sBuf.append("PV [ name=");
		sBuf.append(name);
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

	/**
	 * This class allows to separate the path.
	 */
	protected class ParameterPath {

		private static final char	PATH_SEPERATOR	= '/';

		private static final char	INDEX_BEGIN		= '[';

		private static final char	INDEX_EMD		= ']';

		private String[]			nodes			= null;

		ParameterPath(String path) {
			nodes = StringUtil.split(path, PATH_SEPERATOR);
			if (nodes == null) {
				return;
			}
		}

		public int getDepth() {
			return (nodes == null) ? 0 : nodes.length;
		}

		public String getNode(int depth) {
			String node = nodes[depth];

			// check for index
			int sPos = node.indexOf(INDEX_BEGIN);
			if (sPos > -1) {
				node = node.substring(0, sPos);
			}
			return node;
		}

		public int getIndex(int depth) {
			String node = nodes[depth];
			int index = 0;

			// check for index
			int sPos = node.indexOf(INDEX_BEGIN);
			if (sPos > -1) {
				int ePos = node.indexOf(INDEX_EMD, sPos);
				index = Integer.valueOf(node.substring(sPos + 1, ePos)).intValue();
			}
			return index;
		}
		
		public boolean hasIndex(int depth) {
			String node = nodes[depth];

			// check for index
			int sPos = node.indexOf(INDEX_BEGIN);
			if (sPos == -1) {
				return false;
			}
			return true;
		}

		public String getPath(int depth) {
			String path = "";
			for (int i = depth; i < nodes.length; i++) {
				if (i == depth) {
					path += nodes[i];
				} else {
					path += PATH_SEPERATOR + nodes[i];
				}
			}
			return path;
		}
	}

}
