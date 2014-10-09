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
import org.ws4d.java.structures.ArrayList;
import org.ws4d.java.structures.Iterator;
import org.ws4d.java.structures.LinkedList;
import org.ws4d.java.structures.List;
import org.ws4d.java.structures.ReadOnlyIterator;
import org.ws4d.java.types.QName;
import org.ws4d.java.util.StringUtil;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

/**
 * This class represents the model group part of XML Schema.
 * <p>
 * <a href="http://www.w3.org/TR/xmlschema11-1/#Model_Groups">http://www.w3.org/
 * TR/xmlschema11-1/#Model_Groups<a>
 * </p>
 * <p>
 * It allows to add elements, groups and other containers.
 * </p>
 * <h3>XML Schema</h3>
 * <p>
 * It is possible to create nested container structures like this:
 * 
 * <pre>
 * &lt;xs:schema xmlns:xs=&quot;http://www.w3.org/2001/XMLSchema&quot; targetNamespace=&quot;http://www.example.org&quot;&gt;
 *    &lt;element name=&quot;nested&quot; type=&quot;nestedType&quot; /&gt;
 *    &lt;xs:complexType name=&quot;nestedType&quot;&gt;
 *       &lt;xs:all&gt;
 *          &lt;xs:element name=&quot;a&quot; type=&quot;xs:string&quot; /&gt;
 *          &lt;xs:element name=&quot;b&quot; type=&quot;xs:string&quot; /&gt;
 *          &lt;xs:sequence&gt;
 *              &lt;xs:element name=&quot;c&quot; type=&quot;xs:string&quot; /&gt;
 *              &lt;xs:element name=&quot;d&quot; type=&quot;xs:string&quot; /&gt;
 *              &lt;xs:choice&gt;
 *                 &lt;xs:element name=&quot;e&quot; type=&quot;xs:string&quot; /&gt;
 *                 &lt;xs:element name=&quot;f&quot; type=&quot;xs:string&quot; /&gt;
 *              &lt;/xs:choice&gt;
 *          &lt;/xs:sequence&gt;
 *       &lt;/xs:all&gt;
 *    &lt;/xs:complexType&gt;
 * &lt;/xs:schema&gt;
 * </pre>
 * 
 * </p>
 * <h3>Framework</h3>
 * <p>
 * 
 * <pre>
 * // get primitive data types
 * Type xsString = SchemaUtil.getSchemaType(&quot;string&quot;);
 * 
 * // create inner elements
 * Element a = new Element(new QName(&quot;a&quot;, &quot;http://www.example.org&quot;), xsString);
 * Element b = new Element(new QName(&quot;b&quot;, &quot;http://www.example.org&quot;), xsString);
 * Element c = new Element(new QName(&quot;c&quot;, &quot;http://www.example.org&quot;), xsString);
 * Element d = new Element(new QName(&quot;d&quot;, &quot;http://www.example.org&quot;), xsString);
 * Element e = new Element(new QName(&quot;f&quot;, &quot;http://www.example.org&quot;), xsString);
 * 
 * // create the nested structure (bottom-up)
 * ChoiceContainer choice = new ChoiceContainer();
 * choice.addElement(e);
 * choice.addElement(f);
 * 
 * SequenceContainer sequence = new SequenceContainer();
 * sequence.add(c);
 * sequence.add(d);
 * sequence.addContainer(choice);
 * 
 * // create the complex type
 * ComplexType nestedType = new ComplexType(new QName(&quot;nestedType&quot;, &quot;http://www.example.org&quot;), ComplexType.CONTAINER_ALL);
 * nestedType.addElement(a);
 * nestedType.addElement(b);
 * 
 * // get the enclosed container and add the other container
 * AllContainer all = nestedType.getContainer();
 * all.addContainer(sequence);
 * 
 * </pre>
 * 
 * </p>
 */
public abstract class ElementContainer implements Any {

	static final String	TAG_CHOICE		= ELEMENT_CHOICE;

	static final String	TAG_SEQUENCE	= ELEMENT_SEQUENCE;

	static final String	TAG_ALL			= ELEMENT_ALL;

	protected List		container		= null;

	protected int		min				= 1;

	protected int		max				= 1;

	protected int		containerCount	= 0;

	protected int		elementCount	= 0;

	protected int		groupCount		= 0;

	static final void handleContainerElements(ElementParser parser, ElementContainer container, String targetNamespace, Schema schema) throws XmlPullParserException, IOException, SchemaException {
		String min = parser.getAttributeValue(null, AnyElement.ATTRIBUTE_MINOCCURS);
		String max = parser.getAttributeValue(null, AnyElement.ATTRIBUTE_MAXOCCURS);
		if (min != null) {
			container.setMinOccurs(Integer.parseInt(min.trim()));
		}
		if (max != null) {
			if (MAXOCCURS_UNBOUNDED.equals(max)) {
				container.setMaxOccurs(-1);
			} else {
				container.setMaxOccurs(Integer.parseInt(max.trim()));
			}
		}
		int d = parser.getDepth();
		while (parser.nextTag() != XmlPullParser.END_TAG && parser.getDepth() == d + 1) {
			String namespace = parser.getNamespace();
			String name = parser.getName();
			if (XMLSCHEMA_NAMESPACE.equals(namespace)) {
				if (StringUtil.equalsIgnoreCase(Element.TAG_ELEMENT, name)) {
					Element e = Element.createElement(parser, targetNamespace, schema);
					container.addElement(e);
				} else if (StringUtil.equalsIgnoreCase(TAG_SEQUENCE, name)) {
					ElementContainer iContainer = new SequenceContainer();
					handleContainerElements(parser, iContainer, targetNamespace, schema);
					container.addContainer(iContainer);
				} else if (StringUtil.equalsIgnoreCase(TAG_ALL, name)) {
					ElementContainer iContainer = new AllContainer();
					handleContainerElements(parser, iContainer, targetNamespace, schema);
					container.addContainer(iContainer);
				} else if (StringUtil.equalsIgnoreCase(TAG_CHOICE, name)) {
					ElementContainer iContainer = new ChoiceContainer();
					handleContainerElements(parser, iContainer, targetNamespace, schema);
					container.addContainer(iContainer);
				} else if (StringUtil.equalsIgnoreCase(Group.TAG_GROUP, name)) {
					Group g = Group.createGroup(parser, targetNamespace, schema);
					container.addGroup(g);
				} else if (StringUtil.equalsIgnoreCase(TAG_ANY, name)) {
					AnyElement e = AnyElement.createAnyElement(parser);
					container.addAnyElement(e);
				}
			}
		}
	}

	ElementContainer(List container) {
		this.container = container;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		int all = getElementCount();
		return "Container [ own=" + elementCount + ", inherit=" + (all - elementCount) + ", all=" + all + ", min=" + min + ", max=" + max + ", container=" + container + " ]";
	}

	/**
	 * Returns the minimum occurrence for the container.
	 * <p>
	 * The "minOccurs" attribute in XML Schema describes the minimum occurrence
	 * of the container inside the created XML instance document.
	 * </p>
	 * 
	 * @return the minimum occurrence for the container.
	 */
	public int getMinOccurs() {
		return min;
	}

	/**
	 * Returns the maximum occurrence for the container.
	 * <p>
	 * The "maxOccurs" attribute in XML Schema describes the maximum occurrence
	 * of the container inside the created XML instance document.
	 * </p>
	 * 
	 * @return the maximum occurrence for the container.
	 */
	public int getMaxOccurs() {
		return max;
	}

	/**
	 * Sets the minimum occurrence for the container.
	 * <p>
	 * The "minOccurs" attribute in XML Schema describes the minimum occurrence
	 * of the container inside the created XML instance document.
	 * </p>
	 * 
	 * @param min the minimum occurrence for the container.
	 */
	public void setMinOccurs(int min) {
		this.min = min;
	}

	/**
	 * Sets the maximum occurrence for the container.
	 * <p>
	 * The "maxOccurs" attribute in XML Schema describes the maximum occurrence
	 * of the container inside the created XML instance document.
	 * </p>
	 * 
	 * @param max the maximum occurrence for the container.
	 */
	public void setMaxOccurs(int max) {
		this.max = max;
	}

	/**
	 * Merge one container into this container.
	 * 
	 * @param container the container which should be merged with this one.
	 */
	public void mergeContainer(ElementContainer container) {
		if (container == null) return;
		if (container.getContainerType() != getContainerType()) {
			throw new RuntimeException("XML schema container mismatch! Cannot merge different container types.");
		}
		Iterator it = container.allElements();
		while (it.hasNext()) {
			Element e = (Element) it.next();
			this.addElement(e);
		}
	}

	/**
	 * Returns the number of containers inside this container.
	 * 
	 * @return the number of containers inside this container.
	 */
	public int getInnerContainerCount() {
		int i = 0;
		for (Iterator it = container.iterator(); it.hasNext();) {
			Any anyElement = (Any) it.next();
			int j = anyElement.getSchemaIdentifier();
			if (j == XSD_ALLMODEL || j == XSD_CHOICEMODEL || j == XSD_SEQUENCEMODEL) {
				i++;
			}
		}
		return i;
	}

	/**
	 * Adds another container to this container.
	 * 
	 * @param container the container to add.
	 */
	public void addContainer(ElementContainer container) {
		containerCount++;
		this.container.add(container);
	}

	/**
	 * Adds an element to the container.
	 * 
	 * @param e the element to add.
	 */
	public void addElement(Element e) {
		elementCount++;
		container.add(e);
	}

	/**
	 * Adds an <strong>any</strong> element to the container. This allows to add
	 * elements from other schemas to the XML instance document.
	 * 
	 * @param e the element to add.
	 */
	public void addAnyElement(AnyElement e) {
		elementCount++;
		container.add(e);
	}

	/**
	 * Adds a group of elements to the container.
	 * 
	 * @param g the group to add.
	 */
	public void addGroup(Group g) {
		QName name = g.getName();
		if (name == null) {
			name = g.getReferenceLink();
		}
		elementCount++;
		container.add(g);
	}

	/**
	 * Returns the element count of the enclosed containers.
	 * 
	 * @return the element count.
	 */
	public int getElementCount() {
		int i = elementCount;
		for (Iterator it = container.iterator(); it.hasNext();) {
			Any anyElement = (Any) it.next();
			int j = anyElement.getSchemaIdentifier();
			if (j == XSD_ALLMODEL || j == XSD_CHOICEMODEL || j == XSD_SEQUENCEMODEL) {
				i += ((ElementContainer) anyElement).getElementCount();
			} else if (j == XSD_GROUP) {
				i += ((Group) anyElement).getElementCount();
			}
		}
		return i;
	}

	/**
	 * Returns an element from the container with matching name.
	 * 
	 * @param name the qualified name of the element which should be returned.
	 * @return the element.
	 */
	public Element getElementByName(QName name) {
		if (name == null) return null;
		for (Iterator it = container.iterator(); it.hasNext();) {
			Any anyElement = (Any) it.next();
			int j = anyElement.getSchemaIdentifier();
			Element e = null;
			if (j == XSD_ELEMENT) {
				e = (Element) anyElement;
			} else if (j == XSD_CHOICEMODEL || j == XSD_ALLMODEL || j == XSD_SEQUENCEMODEL) {
				ElementContainer subCon = (ElementContainer) anyElement;
				e = subCon.getElementByName(name);
			}
			if (e != null && name.equals(e.getName())) {
				return e;
			}
		}
		return null;
	}

	/**
	 * Returns a local element (i.e. one that is directly contained by this
	 * container) from the container with matching name.
	 * 
	 * @param name the qualified name of the element which should be returned.
	 * @return the element.
	 */
	public Element getLocalElementByName(QName name) {
		if (name == null) return null;
		for (Iterator it = container.iterator(); it.hasNext();) {
			Any anyElement = (Any) it.next();
			int j = anyElement.getSchemaIdentifier();
			if (j == XSD_ELEMENT) {
				Element e = (Element) anyElement;
				if (name.equals(e.getName())) {
					return e;
				}
			}
		}
		return null;
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
		if (name == null) return null;
		for (Iterator it = container.iterator(); it.hasNext();) {
			Any anyElement = (Any) it.next();
			int j = anyElement.getSchemaIdentifier();
			Element e = null;
			if (j == XSD_ELEMENT) {
				e = (Element) anyElement;
			} else if (j == XSD_CHOICEMODEL || j == XSD_ALLMODEL || j == XSD_SEQUENCEMODEL) {
				ElementContainer subCon = (ElementContainer) anyElement;
				e = subCon.getElementByName(name);
			}
			if (name.equals(e.getName().getLocalPart())) {
				return e;
			}
		}
		return null;
	}

	/**
	 * Returns a local element (i.e. one that is directly contained by this
	 * container) with matching name from the container.
	 * <p>
	 * This method will <strong>NOT</strong> verify the namespace.
	 * </p>
	 * 
	 * @param name the name of the element which should be returned.
	 * @return the element.
	 */
	public Element getLocalElementByName(String name) {
		if (name == null) return null;
		for (Iterator it = container.iterator(); it.hasNext();) {
			Any anyElement = (Any) it.next();
			int j = anyElement.getSchemaIdentifier();
			if (j == XSD_ELEMENT) {
				Element e = (Element) anyElement;
				if (name.equals(e.getName().getLocalPart())) {
					return e;
				}
			}
		}
		return null;
	}

	public Iterator listAll() {
		return new ReadOnlyIterator(new ArrayList(container));
	}

	/**
	 * Returns an iterator of containers inside this container.
	 * 
	 * @return the containers.
	 */
	public Iterator containers() {
		int size = getInnerContainerCount();
		List l = new ArrayList(size);
		for (Iterator it = container.iterator(); it.hasNext();) {
			Any anyElement = (Any) it.next();
			int j = anyElement.getSchemaIdentifier();
			if (j == XSD_ALLMODEL || j == XSD_CHOICEMODEL || j == XSD_SEQUENCEMODEL) {
				l.add(anyElement);
			}
		}
		return l.iterator();
	}

	public Iterator all() {
		int size = container.size();
		List l = new ArrayList(size);
		for (Iterator it = container.iterator(); it.hasNext();) {
			Any anyElement = (Any) it.next();
			int j = anyElement.getSchemaIdentifier();
			if (j == XSD_ALLMODEL || j == XSD_CHOICEMODEL || j == XSD_SEQUENCEMODEL) {
				l.add(anyElement);
			} else if (j == XSD_GROUP) {
				Group g = (Group) anyElement;
				Iterator elements = g.elements();
				while (elements.hasNext()) {
					Any e = (Any) elements.next();
					l.add(e);
				}
			} else if (j == XSD_ELEMENT || j == XSD_ANYELEMENT) {
				l.add(anyElement);
			}

		}
		return l.iterator();
	}

	public Element getFirstElement() {
		Element ret = null;
		for (Iterator it = container.iterator(); it.hasNext();) {
			Any anyElement = (Any) it.next();
			int j = anyElement.getSchemaIdentifier();
			if (j == XSD_GROUP) {
				for (Iterator group = ((Group) anyElement).elements(); group.hasNext();) {
					Any e = (Any) group.next();
					if (ret == null && e.getSchemaIdentifier() == SchemaConstants.XSD_ELEMENT) ret = (Element) e;
				}
			} else if (j == XSD_ELEMENT) {
				if (ret == null) ret = (Element) anyElement;
			}
		}
		return ret;
	}

	/**
	 * Returns an iterator of elements from this container.
	 * 
	 * @return the elements.
	 */
	public Iterator ownElements() {
		int size = container.size();
		List l = new ArrayList(size);
		for (Iterator it = container.iterator(); it.hasNext();) {
			Any anyElement = (Any) it.next();
			int j = anyElement.getSchemaIdentifier();
			if (j == XSD_GROUP) {
				for (Iterator group = ((Group) anyElement).elements(); group.hasNext();) {
					Any e = (Any) group.next();
					l.add(e);
				}
			} else if (j == XSD_ELEMENT || j == XSD_ANYELEMENT) {
				l.add(anyElement);
			}
		}
		return l.iterator();
	}

	/**
	 * Returns an iterator of elements from this container and all elements from
	 * nested containers.
	 * 
	 * @return the elements.
	 */
	public Iterator allElements() {
		int size = getElementCount();
		List l = new ArrayList(size);
		for (Iterator it = container.iterator(); it.hasNext();) {
			Any anyElement = (Any) it.next();
			int j = anyElement.getSchemaIdentifier();
			if (j == XSD_ALLMODEL || j == XSD_CHOICEMODEL || j == XSD_SEQUENCEMODEL) {
				for (Iterator conti = ((ElementContainer) anyElement).allElements(); conti.hasNext();) {
					Any e = (Any) conti.next();
					l.add(e);
				}
			} else if (j == XSD_GROUP) {
				for (Iterator group = ((Group) anyElement).elements(); group.hasNext();) {
					Any e = (Any) group.next();
					l.add(e);
				}
			} else if (j == XSD_ELEMENT || j == XSD_ANYELEMENT) {
				l.add(anyElement);
			}
		}
		return l.iterator();
	}

	public Iterator getContainerContent() {
		// int size = container.size();
		List l = new LinkedList();
		for (Iterator it = container.iterator(); it.hasNext();) {
			Any anyElement = (Any) it.next();
			int j = anyElement.getSchemaIdentifier();
			if (j == XSD_ALLMODEL || j == XSD_CHOICEMODEL || j == XSD_SEQUENCEMODEL) {
				l.add(anyElement);
			} else if (j == XSD_GROUP) {
				for (Iterator group = ((Group) anyElement).elements(); group.hasNext();) {
					Any e = (Any) group.next();
					l.add(e);
				}
			} else if (j == XSD_ELEMENT || j == XSD_ANYELEMENT) {
				l.add(anyElement);
			}
		}
		return l.iterator();
	}

	/**
	 * Returns <code>true</code> if the container has element definitions,
	 * <code>false</code> otherwise.
	 * 
	 * @return <code>true</code> if the container has element definitions,
	 *         <code>false</code> otherwise.
	 */
	public boolean hasElements() {
		if (elementCount > 0) return true;
		return (getElementCount() > 0);
	}

	protected void serialize0(XmlSerializer serializer, Schema schema) throws IOException {
		if (getMinOccurs() != 1) {
			serializer.attribute(null, AnyElement.ATTRIBUTE_MINOCCURS, Integer.toString(min));
		}
		if (max != 1) {
			if (max == -1) {
				serializer.attribute(null, AnyElement.ATTRIBUTE_MAXOCCURS, MAXOCCURS_UNBOUNDED);
			} else {
				serializer.attribute(null, AnyElement.ATTRIBUTE_MAXOCCURS, Integer.toString(max));
			}
		}
		for (Iterator it = container.iterator(); it.hasNext();) {
			Any anyElement = (Any) it.next();
			int j = anyElement.getSchemaIdentifier();
			if (j == XSD_ALLMODEL || j == XSD_CHOICEMODEL || j == XSD_SEQUENCEMODEL) {
				((ElementContainer) anyElement).serialize(serializer, schema);
			} else if (j == XSD_GROUP) {
				((Group) anyElement).serialize(serializer, schema);
			} else if (j == XSD_ELEMENT) {
				((Element) anyElement).serialize(serializer, schema);
			} else if (j == XSD_ANYELEMENT) {
				((AnyElement) anyElement).serialize(serializer, schema);
			}
		}
	}

	/**
	 * Returns the information about the type of this container. (xs:all,
	 * xs:sequence or xs:choice).
	 * 
	 * @return the type of this container.
	 */
	public abstract int getContainerType();

	abstract void serialize(XmlSerializer serializer, Schema schema) throws IOException;

}
