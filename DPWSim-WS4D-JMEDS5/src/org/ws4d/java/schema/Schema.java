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
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;

import org.ws4d.java.DPWSFramework;
import org.ws4d.java.communication.RequestHeader;
import org.ws4d.java.communication.Resource;
import org.ws4d.java.communication.monitor.ResourceLoader;
import org.ws4d.java.constants.SchemaConstants;
import org.ws4d.java.constants.XMLConstants;
import org.ws4d.java.io.xml.ElementParser;
import org.ws4d.java.io.xml.XmlPullParserSupport;
import org.ws4d.java.io.xml.XmlSerializerImplementation;
import org.ws4d.java.structures.ArrayList;
import org.ws4d.java.structures.EmptyStructures;
import org.ws4d.java.structures.HashMap;
import org.ws4d.java.structures.HashSet;
import org.ws4d.java.structures.Iterator;
import org.ws4d.java.structures.LinkedList;
import org.ws4d.java.structures.List;
import org.ws4d.java.structures.ReadOnlyIterator;
import org.ws4d.java.structures.Set;
import org.ws4d.java.types.InternetMediaType;
import org.ws4d.java.types.QName;
import org.ws4d.java.types.URI;
import org.ws4d.java.util.StringUtil;
import org.ws4d.java.wsdl.WSDLRepository;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

/**
 * General XML Schema class.
 */
public class Schema extends Annotation implements Resource {

	public static final Type		ANY_TYPE				= SchemaUtil.getAnyType();

	private static final HashMap	SCHEMA_CACHE			= new HashMap();

	private static final boolean	USE_SCHEMA_CACHE		= false;

	private String					targetNamespace			= "";

	private HashMap					elements				= EmptyStructures.EMPTY_MAP;

	private HashMap					types					= EmptyStructures.EMPTY_MAP;

	private HashMap					attributes				= EmptyStructures.EMPTY_MAP;

	private HashMap					groups					= EmptyStructures.EMPTY_MAP;

	private HashMap					attributeGroups			= EmptyStructures.EMPTY_MAP;

	private HashMap					notations				= EmptyStructures.EMPTY_MAP;

	private List					elementResolver			= EmptyStructures.EMPTY_LIST;

	private List					attributeResolver		= EmptyStructures.EMPTY_LIST;

	private List					groupResolver			= EmptyStructures.EMPTY_LIST;

	private List					attributeGroupResolver	= EmptyStructures.EMPTY_LIST;

	private List					baseResolver			= EmptyStructures.EMPTY_LIST;

	private List					referenceResolver		= EmptyStructures.EMPTY_LIST;

	private List					baseReferenceResolver	= EmptyStructures.EMPTY_LIST;

	private List					listItemTypeResolver	= EmptyStructures.EMPTY_LIST;

	private List					unionMemberResolver		= EmptyStructures.EMPTY_LIST;

	private HashMap					linkedSchemas			= EmptyStructures.EMPTY_MAP;

	private HashMap					imports					= EmptyStructures.EMPTY_MAP;

	private Set						includes				= EmptyStructures.EMPTY_SET;
	
	private long					lastMod					= 0L;

	/**
	 * Removes all entries from the schema cache.
	 */
	public static void flushSchemaCache() {
		synchronized (SCHEMA_CACHE) {
			SCHEMA_CACHE.clear();
		}
	}

	public static Schema parse(XmlPullParser parser, URI fromUri, String targetNamespace, boolean loadReferencedFiles) throws XmlPullParserException, IOException, SchemaException {
		Schema schema;
		synchronized (SCHEMA_CACHE) {
			schema = (Schema) SCHEMA_CACHE.get(targetNamespace);
		}
		if (schema == null) {
			schema = new Schema(targetNamespace);
			schema.handleSchema(new ElementParser(parser), fromUri, loadReferencedFiles);
			if (loadReferencedFiles) {
				schema.resolveSchema();
			}
		} else {
			/*
			 * read trough entire section as we might be embedded within another
			 * file ...
			 */
			new ElementParser(parser).consume();
		}
		return schema;
	}

	public static Schema parse(URI fromUri) throws XmlPullParserException, IOException, SchemaException {
		return parse(fromUri, true);
	}

	public static Schema parse(URI fromUri, boolean loadReferencedFiles) throws XmlPullParserException, IOException, SchemaException {
		ResourceLoader rl = DPWSFramework.getResourceAsStream(fromUri);
		InputStream in = rl.getInputStream();
		try {
			Schema schema = parse(in, fromUri, loadReferencedFiles);
			if (schema != null && loadReferencedFiles) {
				schema.resolveSchema();
			}
			return schema;
		} finally {
			in.close();
		}
	}

	public static Schema parse(InputStream in, URI fromUri, boolean loadReferencedFiles) throws XmlPullParserException, IOException, SchemaException {
		XmlPullParser parser = XmlPullParserSupport.getFactory().newPullParser();
		parser.setInput(in, null);
		parser.nextTag();
		String namespace = parser.getNamespace();
		String name = parser.getName();
		if (!XMLSCHEMA_NAMESPACE.equals(namespace) || !StringUtil.equalsIgnoreCase(SCHEMA_SCHEMA, name)) {
			throw new IOException("This is not an XML schema.");
		}
		String tns = parser.getAttributeValue(null, SCHEMA_TARGETNAMESPACE);

		Schema schema;
		synchronized (SCHEMA_CACHE) {
			schema = (Schema) SCHEMA_CACHE.get(tns);
		}
		if (schema == null) {
			schema = new Schema(tns);
			schema.handleSchema(new ElementParser(parser), fromUri, loadReferencedFiles);
			schema.resolveSchema();
		}
		// we don't care about in at this point - it may be left unread ...
		return schema;
	}

	Schema() {
		this("");
	}

	Schema(String targetNamespace) {
		super();
		updateLastModified();
		setTargetNamespace(targetNamespace);
	}
	
	protected synchronized void updateLastModified() {
		Date d = new Date();
		lastMod = d.getTime();
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return "Schema [ targetNamespace=" + getTargetNamespace() + " ]";
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.types.schema.Annotation#getSchemaIdentifier()
	 */
	public int getSchemaIdentifier() {
		return SchemaConstants.XSD_SCHEMA;
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.communication.Resource#getContentType()
	 */
	public InternetMediaType getContentType() {
		return InternetMediaType.getXML();
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.ws4d.java.communication.Resource#serialize(org.ws4d.java.types.URI,
	 * org.ws4d.java.communication.RequestHeader, java.io.InputStream,
	 * java.io.OutputStream)
	 */
	public void serialize(URI request, RequestHeader requestHeader, InputStream requestBody, OutputStream out) throws IOException {
		serialize0(out);

	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.communication.Resource#size()
	 */
	public long size() {
		return -1;
	}

	public String getTargetNamespace() {
		return targetNamespace;
	}

	public Iterator getLinkedSchemas() {
		return new ReadOnlyIterator(linkedSchemas.values());
	}

	public Iterator getElements() {
		return new ReadOnlyIterator(elements.values());
	}

	public Element getElement(QName name) {
		String namespace = name == null ? null : name.getNamespace();
		if (targetNamespace.equals(namespace)) {
			return (Element) elements.get(name);
		}
		// try linked schemas
		Schema schema = (Schema) linkedSchemas.get(namespace);
		if (schema != null) {
			return schema.getElement(name);
		}
		// try to find it within an imported/linked schema at a deeper level...
		for (Iterator it = linkedSchemas.values().iterator(); it.hasNext();) {
			schema = (Schema) it.next();
			Element element = schema.getElement(name);
			if (element != null) {
				return element;
			}
		}
		return null;
	}

	public Element getElement(String name, String namespace) {
		QName qn = new QName(name, namespace);
		return getElement(qn);
	}

	public int getElementCount() {
		return elements.size();
	}

	public Iterator getTypes() {
		return new ReadOnlyIterator(types.values());
	}

	public Type getType(String name, String namespace) {
		QName qn = new QName(name, namespace);
		return getType(qn);
	}

	public Type getType(QName name) {
		String namespace = name == null ? null : name.getNamespace();
		if (XMLSCHEMA_NAMESPACE.equals(namespace)) {
			return SchemaUtil.getType(name);
		}
		if (targetNamespace.equals(namespace)) {
			return (Type) types.get(name);
		}
		// try linked schemas
		Schema schema = (Schema) linkedSchemas.get(namespace);
		if (schema != null) {
			return schema.getType(name);
		}
		// try to find it within an imported/linked schema at a deeper level...
		for (Iterator it = linkedSchemas.values().iterator(); it.hasNext();) {
			schema = (Schema) it.next();
			Type type = schema.getType(name);
			if (type != null) {
				return type;
			}
		}
		return null;
	}

	public int getTypeCount() {
		return types.size();
	}

	public Iterator getAttributes() {
		return new ReadOnlyIterator(attributes.values());
	}

	public Attribute getAttribute(String name, String namespace) {
		QName qn = new QName(name, namespace);
		return getAttribute(qn);
	}

	public Attribute getAttribute(QName name) {
		String namespace = name == null ? null : name.getNamespace();
		if (targetNamespace.equals(namespace)) {
			return (Attribute) attributes.get(name);
		}
		// try linked schemas
		Schema schema = (Schema) linkedSchemas.get(namespace);
		if (schema != null) {
			return schema.getAttribute(name);
		}
		// try to find it within an imported/linked schema at a deeper level...
		for (Iterator it = linkedSchemas.values().iterator(); it.hasNext();) {
			schema = (Schema) it.next();
			Attribute attribute = schema.getAttribute(name);
			if (attribute != null) {
				return attribute;
			}
		}
		return null;
	}

	public int getAttributCount() {
		return attributes.size();
	}

	public Group getGroup(String name, String namespace) {
		QName qn = new QName(name, namespace);
		return getGroup(qn);
	}

	public Group getGroup(QName name) {
		String namespace = name == null ? null : name.getNamespace();
		if (targetNamespace.equals(namespace)) {
			return (Group) groups.get(name);
		}
		// try linked schemas
		Schema schema = (Schema) linkedSchemas.get(namespace);
		if (schema != null) {
			return schema.getGroup(name);
		}
		// try to find it within an imported/linked schema at a deeper level...
		for (Iterator it = linkedSchemas.values().iterator(); it.hasNext();) {
			schema = (Schema) it.next();
			Group group = schema.getGroup(name);
			if (group != null) {
				return group;
			}
		}
		return null;
	}

	public int getGroupCount() {
		return groups.size();
	}

	public AttributeGroup getAttributeGroup(String name, String namespace) {
		QName qn = new QName(name, namespace);
		return getAttributeGroup(qn);
	}

	public AttributeGroup getAttributeGroup(QName name) {
		String namespace = name == null ? null : name.getNamespace();
		if (targetNamespace.equals(namespace)) {
			return (AttributeGroup) attributeGroups.get(name);
		}
		// try linked schemas
		Schema schema = (Schema) linkedSchemas.get(namespace);
		if (schema != null) {
			return schema.getAttributeGroup(name);
		}
		// try to find it within an imported/linked schema at a deeper level...
		for (Iterator it = linkedSchemas.values().iterator(); it.hasNext();) {
			schema = (Schema) it.next();
			AttributeGroup attributeGroup = schema.getAttributeGroup(name);
			if (attributeGroup != null) {
				return attributeGroup;
			}
		}
		return null;
	}

	public int getAttributeGroupCount() {
		return attributeGroups.size();
	}

	public Notation getNotation(String name) {
		return (Notation) notations.get(name);
	}

	public int getNotationCount() {
		return notations.size();
	}

	/**
	 * Adds an import information to this schema.
	 * 
	 * @param targetNamespace the target namespace of the imported schema.
	 * @param location the location.
	 */
	public void addImport(String targetNamespace, String location) {
		updateLastModified();
		if (imports == EmptyStructures.EMPTY_MAP) {
			imports = new HashMap();
		}
		imports.put(targetNamespace, location);
	}

	/**
	 * Removes an import from this schema.
	 * 
	 * @param targetNamespace the target namespace of the import.
	 */
	public void removeImport(String targetNamespace) {
		updateLastModified();
		imports.remove(targetNamespace);
	}

	/**
	 * @return
	 */
	public HashMap getImports() {
		if (imports.isEmpty()) {
			return EmptyStructures.EMPTY_MAP;
		}
		return new HashMap(imports);
	}

	public void addInclude(String schemaLocation) {
		updateLastModified();
		if (includes == EmptyStructures.EMPTY_SET) {
			includes = new HashSet();
		}
		includes.add(schemaLocation);
	}

	public Set getIncludes() {
		if (includes.isEmpty()) {
			return EmptyStructures.EMPTY_SET;
		}
		return new HashSet(includes);
	}

	public void serialize(XmlSerializer serializer) throws IOException {
		serialize0(serializer);
	}

	private void setTargetNamespace(String targetNamespace) {
		if (targetNamespace == null) {
			targetNamespace = "";
		}
		if (USE_SCHEMA_CACHE) {
			synchronized (SCHEMA_CACHE) {
				SCHEMA_CACHE.remove(this.targetNamespace);
				this.targetNamespace = targetNamespace;
				SCHEMA_CACHE.put(targetNamespace, this);
			}
		} else {
			this.targetNamespace = targetNamespace;
		}
	}

	void addElement(Element e) {
		if (e == null) return;
		if (elements == EmptyStructures.EMPTY_MAP) {
			elements = new HashMap();
		}
		String namespace = e.getName().getNamespace();
		if (!targetNamespace.equals(namespace)) {
			Schema schema = (Schema) linkedSchemas.get(namespace);
			if (schema != null) {
				schema.addElement(e);
			} else {
				// try to find it within an imported/linked schema at a deeper
				// level...
				boolean linkedSchemaFound = false;
				for (Iterator it = linkedSchemas.values().iterator(); it.hasNext();) {
					schema = (Schema) it.next();
					if (namespace.equals(schema.getTargetNamespace())) {
						linkedSchemaFound = true;
						schema.addElement(e);
						break;
					}
				}
				if (!linkedSchemaFound) {
					// create a new linked schema for the new namespace
					Schema childSchema = new Schema(namespace);
					childSchema.addElement(e);
					addLinkedSchema(childSchema);
				}
			}
			return;
		}
		elements.put(e.getName(), e);
		Type t = e.getType();
		if (t != null) {
			if (t.getName() != null) {
				addType(t);
			} else {
				addType(t, namespace);
			}
		}
	}

	public void addType(Type t) {
		if (t == null) return;
		updateLastModified();
		addType(t, t.getName().getNamespace());
	}

	private void addType(Type t, String namespace) {
		if (SchemaConstants.XMLSCHEMA_NAMESPACE.equals(namespace)) {
			return;
		}
		if (types == EmptyStructures.EMPTY_MAP) {
			types = new HashMap();
		}
		// String namespace = t.getName().getNamespace();
		if (!targetNamespace.equals(namespace)) {
			if (t.getName() != null) {
				Schema schema = (Schema) linkedSchemas.get(namespace);
				if (schema != null) {
					schema.addType(t);
				} else {
					// try to find it within an imported/linked schema at a
					// deeper level...
					boolean linkedSchemaFound = false;
					for (Iterator it = linkedSchemas.values().iterator(); it.hasNext();) {
						schema = (Schema) it.next();
						if (namespace.equals(schema.getTargetNamespace())) {
							linkedSchemaFound = true;
							schema.addType(t);
							break;
						}
					}
					if (!linkedSchemaFound) {
						Schema childSchema = new Schema(namespace);
						childSchema.addType(t, namespace);
						addLinkedSchema(childSchema);
					}
				}
			}
			return;
		}

		if (t.getName() != null) types.put(t.getName(), t);

		// handle base types
		int schemaId = t.getSchemaIdentifier();
		if (schemaId == SchemaConstants.XSD_COMPLEXTYPE || schemaId == SchemaConstants.XSD_EXTENDEDCOMPLEXCONTENT || schemaId == SchemaConstants.XSD_RESTRICTEDCOMPLEXCONTENT) {
			ComplexType ct = (ComplexType) t;

			// Fixed: base type added to schema
			if (schemaId == SchemaConstants.XSD_EXTENDEDCOMPLEXCONTENT) {
				ExtendedComplexContent ecc = (ExtendedComplexContent) ct;
				addType(ecc.getBase());
			} else if (schemaId == SchemaConstants.XSD_RESTRICTEDCOMPLEXCONTENT) {
				RestrictedComplexContent rcc = (RestrictedComplexContent) ct;
				addType(rcc.getBase());
			}

			Iterator elements = ct.elements();
			while (elements.hasNext()) {
				Any anyInnerElement = (Any) elements.next();
				if (anyInnerElement.getSchemaIdentifier() == SchemaConstants.XSD_ELEMENT) {
					Element innerElement = (Element) anyInnerElement;
					Type innerType = innerElement.getType();
					if (innerType != null) {
						if (innerType.getName() != null) {
							if (!types.containsKey(innerType.getName())) {
								addType(innerType);
							}
						} else {
							addType(innerType, namespace);
						}
					}
				}
			}
		}
	}

	private void addAttribute(Attribute a) {
		if (a == null) return;
		if (attributes == EmptyStructures.EMPTY_MAP) {
			attributes = new HashMap();
		}
		attributes.put(a.getName(), a);
		Type t = a.getType();
		if (t != null) {
			if (t.getName() != null) {
				addType(t);
			} else {
				addType(t, a.getName().getNamespace());
			}
		}
	}

	private void addGroup(Group g) {
		if (g == null) return;
		if (groups == EmptyStructures.EMPTY_MAP) {
			groups = new HashMap();
		}
		groups.put(g.getName(), g);
	}

	private void addAttributeGroup(AttributeGroup g) {
		if (g == null) return;
		if (attributeGroups == EmptyStructures.EMPTY_MAP) {
			attributeGroups = new HashMap();
		}
		attributeGroups.put(g.getName(), g);
	}

	private void addNotation(Notation n) {
		if (n == null) return;
		if (notations == EmptyStructures.EMPTY_MAP) {
			notations = new HashMap();
		}
		notations.put(n.getName(), n);
	}

	void addElementForResolve(Element e) {
		if (e == null) return;
		if (elementResolver == EmptyStructures.EMPTY_LIST) {
			elementResolver = new LinkedList();
		}
		elementResolver.add(e);
	}

	void addAttributeForResolve(Attribute e) {
		if (e == null) return;
		if (attributeResolver == EmptyStructures.EMPTY_LIST) {
			attributeResolver = new LinkedList();
		}
		attributeResolver.add(e);
	}

	void addGroupForResolve(Group g) {
		if (g == null) return;
		if (groupResolver == EmptyStructures.EMPTY_LIST) {
			groupResolver = new LinkedList();
		}
		groupResolver.add(g);
	}

	void addAttributeGroupForResolve(AttributeGroup g) {
		if (g == null) return;
		if (attributeGroupResolver == EmptyStructures.EMPTY_LIST) {
			attributeGroupResolver = new LinkedList();
		}
		attributeGroupResolver.add(g);
	}

	void addBaseForResolve(InheritType t) {
		if (t == null) return;
		if (baseResolver == EmptyStructures.EMPTY_LIST) {
			baseResolver = new LinkedList();
		}
		baseResolver.add(t);
	}

	void addListForResolve(SimpleType list) {
		if (list == null) return;
		if (listItemTypeResolver == EmptyStructures.EMPTY_LIST) {
			listItemTypeResolver = new LinkedList();
		}
		listItemTypeResolver.add(list);
	}

	void addUnionForResolve(SimpleType union) {
		if (union == null) return;
		if (unionMemberResolver == EmptyStructures.EMPTY_LIST) {
			unionMemberResolver = new LinkedList();
		}
		unionMemberResolver.add(union);
	}

	void addReferenceElement(Element e) {
		if (elements.containsKey(e.getName())) {
			return;
		}
		if (referenceResolver == EmptyStructures.EMPTY_LIST) {
			referenceResolver = new LinkedList();
		}
		if (!referenceResolver.contains(e) && !elements.containsValue(e)) {
			referenceResolver.add(e);
		}
	}

	void addBaseReference(Type t) {
		if (types.containsKey(t.getName())) {
			return;
		}
		if (baseReferenceResolver == EmptyStructures.EMPTY_LIST) {
			baseReferenceResolver = new LinkedList();
		}
		if (!baseReferenceResolver.contains(t) && !types.containsValue(t)) {
			baseReferenceResolver.add(t);
		}
	}

	void resolveSchema() throws SchemaException {
		resolveElements();
		resolveAttributes();
		resolveGroups();
		resolveAttributeGroups();
		resolveListItemTypes();
		resolveUnionMemberTypes();
		resolveBaseTypes();
	}

	private void serialize0(OutputStream out) throws IOException {
		XmlSerializer serializer = new XmlSerializerImplementation();
		// serializer.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output",
		// true);
		serializer.setOutput(out, XMLConstants.ENCODING);
		serializer.startDocument(XMLConstants.ENCODING, null);
		serialize0(serializer);
		serializer.endDocument();
	}

	private void serialize0(XmlSerializer serializer) throws IOException {
		String prefix = serializer.getPrefix(XMLSCHEMA_NAMESPACE, false);
		if (prefix == null) {
			serializer.setPrefix(XMLSCHEMA_PREFIX, XMLSCHEMA_NAMESPACE);
		}
		if (!"".equals(targetNamespace)) {
			prefix = serializer.getPrefix(targetNamespace, false);
			if (prefix == null) {
				serializer.setPrefix(XMLConstants.XMLNS_TARGETNAMESPACE_PREFIX, targetNamespace);
			}
		}
		serializer.startTag(XMLSCHEMA_NAMESPACE, SCHEMA_SCHEMA);
		// set prefixes for all used namespaces!!!
		if (linkedSchemas != null) {
			for (Iterator it = linkedSchemas.values().iterator(); it.hasNext();) {
				Schema s = (Schema) it.next();
				serializer.getPrefix(s.getTargetNamespace(), true);
			}
		}
		if (!"".equals(targetNamespace)) {
			serializer.attribute(null, SCHEMA_TARGETNAMESPACE, targetNamespace);
		}

		serializer.attribute(null, SCHEMA_ELEMENTFORMDEFAULT, SCHEMA_QUALIFIED);
		serializer.attribute(null, SCHEMA_ATTRIBUTEFORMDEFAULT, SCHEMA_UNQUALIFIED);

		if (imports.size() > 0) {
			Set entries = imports.entrySet();
			Iterator it = entries.iterator();
			while (it.hasNext()) {
				HashMap.Entry entry = (HashMap.Entry) it.next();
				String iTNS = (String) entry.getKey();
				String iLocation = (String) entry.getValue();
				serializer.startTag(XMLSCHEMA_NAMESPACE, SCHEMA_IMPORT);
				serializer.attribute(null, SCHEMA_NAMESPACE, iTNS);
				serializer.attribute(null, SCHEMA_LOCATION, iLocation);
				serializer.endTag(XMLSCHEMA_NAMESPACE, SCHEMA_IMPORT);
			}
		}

		for (Iterator it = elements.values().iterator(); it.hasNext();) {
			Element element = (Element) it.next();
			element.serialize(serializer, this);
		}
		for (Iterator it = types.values().iterator(); it.hasNext();) {
			Type type = (Type) it.next();
			type.serialize(serializer, this);
		}
		for (Iterator it = attributes.values().iterator(); it.hasNext();) {
			Attribute attribute = (Attribute) it.next();
			attribute.serialize(serializer, this);
		}
		for (Iterator it = groups.values().iterator(); it.hasNext();) {
			Group group = (Group) it.next();
			group.serialize(serializer, this);
		}
		for (Iterator it = attributeGroups.values().iterator(); it.hasNext();) {
			AttributeGroup attributeGroup = (AttributeGroup) it.next();
			attributeGroup.serialize(serializer, this);
		}

		resolveReferences(serializer);

		/*
		 * Resolve subtypes
		 */
		ArrayList subTypes = new ArrayList();

		for (Iterator it = types.values().iterator(); it.hasNext();) {
			Type t = (Type) it.next();

			Iterator iterSub = t.getKownSubtypes();
			while (iterSub.hasNext()) {
				Type subType = (Type) iterSub.next();
				subTypes.add(subType);
			}
		}

		while (!subTypes.isEmpty()) {
			Type type = (Type) subTypes.remove(0);

			for (Iterator iter = type.getKownSubtypes(); iter.hasNext();) {
				Type subType = (Type) iter.next();
				subTypes.add(subType);
			}

			// serialize type
			QName typeName = type.getName();
			if (typeName != null && !types.containsKey(typeName)) {
				addType(type);
				/*
				 * TODO Check this! Why we should serialize types which are part
				 * of an other schema?
				 */
				if (targetNamespace != null && targetNamespace.equals(typeName.getNamespace())) {
					type.serialize(serializer, this);
				}
			}
		}

		serializer.endTag(XMLSCHEMA_NAMESPACE, SCHEMA_SCHEMA);
	}

	// modified resolving algorithm in accordance with patch submitted by Stefan
	// Schlichting
	private void resolveReferences(XmlSerializer serializer) throws IOException {
		LinkedList tempRefResolver = new LinkedList();
		LinkedList removeList = new LinkedList();
		LinkedList alreadySerializedTypes = new LinkedList();
		if (!types.isEmpty()) {
			alreadySerializedTypes.addAll(types.values());
		}
		while (!referenceResolver.isEmpty() || !baseReferenceResolver.isEmpty()) {
			List ntypes = new LinkedList();
			tempRefResolver.addAll(referenceResolver);
			tempRefResolver.addAll(baseReferenceResolver);
			for (Iterator it = tempRefResolver.iterator(); it.hasNext();) {
				Object o = it.next();
				if (o instanceof Element) {
					Element element = (Element) o;

					if (elements.containsValue(element)) {
						removeList.add(element);
						continue;
					}
					element.serialize(serializer, this);
					if (elements == EmptyStructures.EMPTY_MAP) elements = new HashMap();

					elements.put(element.getName(), element);
					removeList.add(element);
					Type t = element.getType();
					if (t.getName() != null) {
						String namespace = t.getName().getNamespace();
						if (types != null && !types.containsValue(t) && !XMLSCHEMA_NAMESPACE.equals(namespace)) {
							/*
							 * Collect type which where not serialized in the
							 * first step.
							 */
							ntypes.add(t);
						}
					}
				} else {
					if (o instanceof SimpleType) {
						SimpleType st = (SimpleType) o;
						if (!XMLSCHEMA_NAMESPACE.equals(st.getName().getNamespace())) {
							st.serialize(serializer, this);
						}
					}
					removeList.add(o);
				}
			}
			tempRefResolver.clear();

			for (Iterator it = removeList.iterator(); it.hasNext();) {
				Object o = it.next();
				if (referenceResolver != EmptyStructures.EMPTY_LIST) referenceResolver.remove(o);
				if (baseReferenceResolver != EmptyStructures.EMPTY_LIST) baseReferenceResolver.remove(o);
			}
			removeList.clear();

			// Bugfix 2010-07-15 SSch
			if (!ntypes.isEmpty() && types == EmptyStructures.EMPTY_MAP) {
				types = new HashMap();
			}

			/*
			 * Serialize the new types. This type where found while serializing
			 * the references.
			 */
			for (Iterator it = ntypes.iterator(); it.hasNext();) {
				Type type = (Type) it.next();
				addType(type);
				// type.serialize(serializer, this);
				// types.put(type.getName(), type);
			}

			if (!types.isEmpty()) {
				Iterator ut = types.values().iterator();
				while (ut.hasNext()) {
					Type t = (Type) ut.next();
					if (!alreadySerializedTypes.contains(t)) {
						t.serialize(serializer, this);
						alreadySerializedTypes.add(t);
					}
				}
			}

		}
	}

	// private void resolveReferences(XmlSerializer serializer) throws
	// IOException {
	// /*
	// * Resolve references
	// */
	// Set refTypes = new HashSet();
	// /*
	// * this is for recursively added references or base references during
	// * resolving other ones :-P
	// */
	// while (!refTypes.isEmpty() || !referenceResolver.isEmpty() ||
	// !baseReferenceResolver.isEmpty()) {
	// while (!referenceResolver.isEmpty()) {
	// Element element = (Element) referenceResolver.remove(0);
	// QName elementName = element.getName();
	// if (elements.containsKey(elementName)) {
	// continue;
	// }
	// if (elementName.getNamespace().equals(targetNamespace)) {
	// elements.put(elementName, element);
	// element.serialize(serializer, this);
	// Type t = element.getType();
	// if (t.getName() != null) {
	// String namespace = t.getName().getNamespace();
	// if (!types.containsValue(t) && !XMLSCHEMA_NAMESPACE.equals(namespace)) {
	// /*
	// * Collect type which where not serialized in the
	// * first step.
	// */
	// refTypes.add(t);
	// }
	// }
	// }
	// }
	//
	// while (!baseReferenceResolver.isEmpty()) {
	// Type type = (Type) baseReferenceResolver.remove(0);
	// QName typeName = type.getName();
	// if (types.containsKey(typeName) || refTypes.contains(type)) {
	// continue;
	// }
	// if (typeName != null) {
	// String namespace = typeName.getNamespace();
	// if (!XMLSCHEMA_NAMESPACE.equals(namespace)) {
	// /*
	// * Collect type which where not serialized in the first
	// * step.
	// */
	// refTypes.add(type);
	// }
	// }
	// }
	//
	// /*
	// * Serialize the new types. This type where found while serializing
	// * the references.
	// */
	// for (Iterator it = refTypes.iterator(); it.hasNext();) {
	// Type type = (Type) it.next();
	// addType(type);
	// type.serialize(serializer, this);
	// it.remove();
	// }
	// }
	// }

	private void resolveElements() throws SchemaException {
		if (elementResolver == null) return;
		if (elementResolver.size() > 0) {
			for (Iterator it = elementResolver.iterator(); it.hasNext();) {
				Element e = (Element) it.next();
				QName ref = e.getReferenceLink();
				if (ref != null) {
					Element reference = getElement(ref);
					if (reference != null) {
						e.setReference(reference);
						continue;
					}
				}
				QName typeLink = e.getTypeLink();
				if (typeLink != null) {
					Type type = getType(typeLink);
					if (type != null) {
						e.setType(type);
						continue;
					}
				}
				throw new SchemaException("Cannot resolve element: " + e + " within " + targetNamespace);
			}
		}
		elementResolver = null;
	}

	private void resolveAttributes() throws SchemaException {
		if (attributeResolver == null) return;
		if (attributeResolver.size() > 0) {
			for (Iterator it = attributeResolver.iterator(); it.hasNext();) {
				Attribute a = (Attribute) it.next();
				QName ref = a.getReferenceLink();
				if (ref != null) {
					Attribute reference = getAttribute(ref);
					if (reference != null) {
						a.setReference(reference);
						continue;
					}
				}
				QName typeLink = a.getTypeLink();
				if (typeLink != null) {
					Type type = getType(typeLink);
					if (type != null) {
						a.setType(type);
						continue;
					}
				}
				throw new SchemaException("Cannot resolve attribute: " + a);
			}
		}
		attributeResolver = null;
	}

	private void resolveGroups() throws SchemaException {
		if (groupResolver == null) return;
		if (groupResolver.size() > 0) {
			for (Iterator it = groupResolver.iterator(); it.hasNext();) {
				Group g = (Group) it.next();
				QName ref = g.getReferenceLink();
				if (ref != null) {
					Group reference = getGroup(ref);
					if (reference != null) {
						g.setReference(reference);
						continue;
					}
				}
				throw new SchemaException("Cannot resolve group: " + g);
			}
		}
		groupResolver = null;
	}

	private void resolveAttributeGroups() throws SchemaException {
		if (attributeGroupResolver == null) return;
		if (attributeGroupResolver.size() > 0) {
			for (Iterator it = attributeGroupResolver.iterator(); it.hasNext();) {
				AttributeGroup g = (AttributeGroup) it.next();
				QName ref = g.getReferenceLink();
				if (ref != null) {
					AttributeGroup reference = getAttributeGroup(ref);
					if (reference != null) {
						g.setReference(reference);
						continue;
					}
				}
				throw new SchemaException("Cannot resolve attribute group: " + g);
			}
		}
		attributeGroupResolver = null;
	}

	private void resolveListItemTypes() throws SchemaException {
		if (listItemTypeResolver == null) return;
		if (listItemTypeResolver.size() > 0) {
			for (Iterator it = listItemTypeResolver.iterator(); it.hasNext();) {
				SimpleType list = (SimpleType) it.next();
				QName ref = list.getListItemLink();
				if (ref != null) {
					Type reference = getType(ref);
					if (reference != null) {
						list.setListItemType(reference);
						continue;
					}
				}
				throw new SchemaException("Cannot resolve list item type: " + list);
			}
		}
		listItemTypeResolver = null;
	}

	private void resolveUnionMemberTypes() throws SchemaException {
		if (unionMemberResolver == null) return;
		if (unionMemberResolver.size() > 0) {
			for (Iterator it = unionMemberResolver.iterator(); it.hasNext();) {
				SimpleType union = (SimpleType) it.next();
				List members = new ArrayList();
				for (Iterator it2 = union.getMemberLinks(); it2.hasNext();) {
					QName ref = (QName) it2.next();
					if (ref != null) {
						Type reference = getType(ref);
						if (reference != null) {
							members.add(reference);
							continue;
						}
					}
					throw new SchemaException("Cannot resolve union member types: " + union);
				}
				union.insertMembers(members);
			}
		}
		unionMemberResolver = null;
	}

	private void resolveBaseTypes() throws SchemaException {
		if (baseResolver == null) return;
		if (baseResolver.size() > 0) {
			for (Iterator it = baseResolver.iterator(); it.hasNext();) {
				InheritType t = (InheritType) it.next();
				QName baseLink = null;
				if (t instanceof RestrictedSimpleContent) {
					baseLink = ((RestrictedSimpleContent) t).getBaseLink();
				} else if (t instanceof RestrictedComplexContent) {
					baseLink = ((RestrictedComplexContent) t).getBaseLink();
				} else if (t instanceof ExtendedSimpleContent) {
					baseLink = ((ExtendedSimpleContent) t).getBaseLink();
				} else if (t instanceof ExtendedComplexContent) {
					baseLink = ((ExtendedComplexContent) t).getBaseLink();
				} else if (t instanceof RestrictedSimpleType) {
					baseLink = ((RestrictedSimpleType) t).getBaseLink();
				}
				if (baseLink != null) {
					Type base = getType(baseLink);
					if (base != null) {
						t.setBase(base);
						continue;
					}
				}
				throw new SchemaException("Cannot resolve the base for: " + t);
			}
		}
		baseResolver = null;
	}

	private void handleSchema(ElementParser parser, URI fromUri, boolean loadReferencedFiles) throws XmlPullParserException, IOException, SchemaException {
		int d = parser.getDepth();
		/*
		 * check depth and allow this method only to handle it. no diving
		 * inside.
		 */
		while (parser.nextTag() != XmlPullParser.END_TAG && parser.getDepth() == d + 1) {
			String namespace = parser.getNamespace();
			String name = parser.getName();
			if (XMLSCHEMA_NAMESPACE.equals(namespace)) {
				if (StringUtil.equalsIgnoreCase(Element.TAG_ELEMENT, name)) {
					Element e = Element.createElement(parser, targetNamespace, this, true);
					addElement(e);
				} else if (StringUtil.equalsIgnoreCase(ComplexType.TAG_COMPLEXTYPE, name)) {
					Type t = ComplexType.createComplexType(parser, targetNamespace, this);
					addType(t);
				} else if (StringUtil.equalsIgnoreCase(SimpleType.TAG_SIMPLETYPE, name)) {
					Type t = SimpleType.createSimpleType(parser, targetNamespace, this);
					addType(t);
				} else if (StringUtil.equalsIgnoreCase(Attribute.TAG_ATTRIBUTE, name)) {
					Attribute a = Attribute.createAttribute(parser, targetNamespace, this);
					addAttribute(a);
				} else if (StringUtil.equalsIgnoreCase(Group.TAG_GROUP, name)) {
					Group g = Group.createGroup(parser, targetNamespace, this);
					addGroup(g);
				} else if (StringUtil.equalsIgnoreCase(AttributeGroup.TAG_ATTRIBUTEGROUP, name)) {
					AttributeGroup g = AttributeGroup.createAttributeGroup(parser, targetNamespace, this);
					addAttributeGroup(g);
				} else if (StringUtil.equalsIgnoreCase(Annotation.TAG_ANNOTATION, name)) {
					Annotation.handleAnnotation(parser, this);
				} else if (StringUtil.equalsIgnoreCase(Notation.TAG_NOTATION, name)) {
					Notation n = Notation.createNotation(parser);
					addNotation(n);
				} else if (StringUtil.equalsIgnoreCase(SCHEMA_INCLUDE, name)) {
					String schemaLocation = parser.getAttributeValue(null, SCHEMA_LOCATION);
					Schema s;
					if (fromUri == null) {
						new ElementParser(parser).consume();
						s = WSDLRepository.getInstance().getSchema(schemaLocation, getTargetNamespace());
					} else {
						URI newUri = URI.absolutize(fromUri, schemaLocation);
						s = SchemaUtil.includeOrImportSchema(parser, newUri, loadReferencedFiles);
					}
					if (s != null) {
						if (!getTargetNamespace().equals(s.targetNamespace)) {
							throw new SchemaException("Cannot include " + schemaLocation + " with different target namespace. Try to import.");
						}
						if (loadReferencedFiles) {
							s.resolveSchema();
						}
						mergeSchema(s, this);
						// addLinkedSchema(s);
					}
					addInclude(schemaLocation);
				} else if (StringUtil.equalsIgnoreCase(SCHEMA_IMPORT, name)) {
					String nNamespace = parser.getAttributeValue(null, SCHEMA_NAMESPACE);
					String schemaLocation = parser.getAttributeValue(null, SCHEMA_LOCATION);
					if (loadReferencedFiles) {
						Schema s;
						if (fromUri == null) {
							new ElementParser(parser).consume();
							s = WSDLRepository.getInstance().getSchema(schemaLocation, nNamespace);
						} else {
							URI newUri = URI.absolutize(fromUri, schemaLocation);
							s = SchemaUtil.includeOrImportSchema(parser, newUri, true);
							if (nNamespace != null && !nNamespace.equals(s.targetNamespace)) {
								throw new SchemaException("Import schema from " + schemaLocation + " doesn't match expected target namespace " + nNamespace + ".");
							}
						}
						if (s != null) {
							s.resolveSchema();
							// mergeSchema(s, schema);
							addLinkedSchema(s);
						}
					} else {
						new ElementParser(parser).consume();
					}
					addImport(nNamespace, schemaLocation);
				}
			}
		}
	}

	private void mergeSchema(Schema src, Schema dest) {
		if (src.imports.size() > 0) {
			if (dest.imports == EmptyStructures.EMPTY_MAP) {
				dest.imports = new HashMap();
			}
			dest.imports.putAll(src.imports);
		}
		if (src.types.size() > 0) {
			if (dest.types == EmptyStructures.EMPTY_MAP) {
				dest.types = new HashMap();
			}
			dest.types.putAll(src.types);
		}
		if (src.elements.size() > 0) {
			if (dest.elements == EmptyStructures.EMPTY_MAP) {
				dest.elements = new HashMap();
			}
			dest.elements.putAll(src.elements);
		}
		if (src.attributes.size() > 0) {
			if (dest.attributes == EmptyStructures.EMPTY_MAP) {
				dest.attributes = new HashMap();
			}
			dest.attributes.putAll(src.attributes);
		}
		if (src.attributeGroups.size() > 0) {
			if (dest.attributeGroups == EmptyStructures.EMPTY_MAP) {
				dest.attributeGroups = new HashMap();
			}
			dest.attributeGroups.putAll(src.attributeGroups);
		}
		if (src.groups.size() > 0) {
			if (dest.groups == EmptyStructures.EMPTY_MAP) {
				dest.groups = new HashMap();
			}
			dest.groups.putAll(src.groups);
		}
	}

	private void addLinkedSchema(Schema s) {
		if (linkedSchemas == EmptyStructures.EMPTY_MAP) {
			linkedSchemas = new HashMap();
		}
		linkedSchemas.put(s.getTargetNamespace(), s);
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.communication.Resource#getHeaderFields()
	 */
	public HashMap getHeaderFields() {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.communication.Resource#shortDescription()
	 */
	public String shortDescription() {
		return "XSD [ " + getTargetNamespace() + " ]";
	}

	/* (non-Javadoc)
	 * @see org.ws4d.java.communication.Resource#getLastModifiedDate()
	 */
	public long getLastModifiedDate() {
		return lastMod;
	}

}
