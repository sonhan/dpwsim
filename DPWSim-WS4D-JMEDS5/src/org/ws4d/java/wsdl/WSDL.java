/*******************************************************************************
 * Copyright (c) 2009 MATERNA Information & Communications. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html. For further
 * project-related information visit http://www.ws4d.org. The most recent
 * version of the JMEDS framework can be obtained from
 * http://sourceforge.net/projects/ws4d-javame.
 ******************************************************************************/
package org.ws4d.java.wsdl;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;

import org.ws4d.java.DPWSFramework;
import org.ws4d.java.communication.ProtocolData;
import org.ws4d.java.communication.RequestHeader;
import org.ws4d.java.communication.Resource;
import org.ws4d.java.communication.monitor.MonitorStreamFactory;
import org.ws4d.java.communication.monitor.MonitoringContext;
import org.ws4d.java.communication.monitor.ResourceLoader;
import org.ws4d.java.constants.HTTPConstants;
import org.ws4d.java.constants.SOAPConstants;
import org.ws4d.java.constants.SchemaConstants;
import org.ws4d.java.constants.WSAConstants;
import org.ws4d.java.constants.WSDLConstants;
import org.ws4d.java.schema.Element;
import org.ws4d.java.schema.Schema;
import org.ws4d.java.schema.SchemaUtil;
import org.ws4d.java.schema.Type;
import org.ws4d.java.structures.ArrayList;
import org.ws4d.java.structures.DataStructure;
import org.ws4d.java.structures.EmptyStructures;
import org.ws4d.java.structures.HashMap;
import org.ws4d.java.structures.HashMap.Entry;
import org.ws4d.java.structures.Iterator;
import org.ws4d.java.structures.LinkedMap;
import org.ws4d.java.structures.List;
import org.ws4d.java.structures.ReadOnlyIterator;
import org.ws4d.java.types.Attributable;
import org.ws4d.java.types.CustomAttributeParser;
import org.ws4d.java.types.CustomAttributeValue;
import org.ws4d.java.types.InternetMediaType;
import org.ws4d.java.types.QName;
import org.ws4d.java.types.StringAttributeParser;
import org.ws4d.java.types.URI;
import org.ws4d.java.util.StringUtil;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

/**
 * 
 */
public class WSDL implements Resource {

	private static final HashMap	DEFAULT_NAMESPACES		= new LinkedMap();

	static {
		DEFAULT_NAMESPACES.put(WSDLConstants.WSDL_NAMESPACE_NAME, WSDLConstants.WSDL_NAMESPACE_PREFIX);
		DEFAULT_NAMESPACES.put(SOAPConstants.SOAP12_NAMESPACE_NAME, SOAPConstants.SOAP12_NAMESPACE_PREFIX);
		DEFAULT_NAMESPACES.put(SchemaConstants.XMLSCHEMA_NAMESPACE, SchemaConstants.XMLSCHEMA_PREFIX);
		DEFAULT_NAMESPACES.put(WSAConstants.WSA_NAMESPACE_NAME, WSAConstants.WSA_NAMESPACE_PREFIX);
	}

	private static HashMap			customAttributeParsers	= null;

	// key = namespace URI as string, value = prefix
	private HashMap					defaultNamespaces;

	private String					name;

	private String					targetNamespace;

	/*
	 * key = target namespace of imported WSDL as String, value = WSDL location
	 * as String
	 */
	private HashMap					imports;

	// key = target namespace of Schema as String, value = Schema instance
	private HashMap					types;

	// key = local name of message as String, value = WSDLMessage instance
	private HashMap					messages;

	// key = local name of port type as String, value = WSDLPortType instance
	private HashMap					portTypes;

	// we always expect to have (only) a document-literal SOAP 1.2 binding

	// key = local name of binding as String, value = WSDLBinding instance
	private HashMap					bindings;

	// key = local name of service as String, value = WSDLService instance
	private HashMap					services;

	// key = target namespace of imported WSDL as String, value = WSDL instance
	private HashMap					linkedWsdls;

	private long					lastMod					= 0L;

	public static void registerAttributeParserForNamespace(String namespace, CustomAttributeParser parser) {
		if (customAttributeParsers == null) {
			customAttributeParsers = new HashMap();
		}
		customAttributeParsers.put(namespace, parser);
	}

	/**
	 * Returns the custom attribute parser instance previously registered for
	 * the given namespace. If there no parser was registered, a default one is
	 * returned, namely {@link StringAttributeParser#INSTANCE}).
	 * 
	 * @param namespace the namespace to return a custom attribute parser for
	 * @return the custom attribute parser for the specified namespace
	 */
	public static CustomAttributeParser getAttributeParserForNamespace(String namespace) {
		if (customAttributeParsers == null) {
			return StringAttributeParser.INSTANCE;
		}
		CustomAttributeParser parser = (CustomAttributeParser) customAttributeParsers.get(namespace);
		return parser == null ? StringAttributeParser.INSTANCE : parser;
	}

	public static WSDL parse(URI fromUri) throws XmlPullParserException, IOException {
		return parse(fromUri, true);
	}

	public static WSDL parse(URI fromUri, boolean loadReferencedFiles) throws XmlPullParserException, IOException {
		ResourceLoader rl = DPWSFramework.getResourceAsStream(fromUri);
		InputStream in = rl.getInputStream();
		try {
			WSDL w = parse(in, fromUri, loadReferencedFiles);

			MonitorStreamFactory monFac = DPWSFramework.getMonitorStreamFactory();

			if (monFac != null) {
				ProtocolData pd = rl.getProtocolData();
				MonitoringContext context = monFac.getMonitoringContextIn(pd);
				monFac.receivedResource(pd, context, w);
			}

			return w;
		} finally {
			if (in != null) {
				in.close();
			}
		}
	}

	public static WSDL parse(InputStream in) throws XmlPullParserException, IOException {
		return parse(in, true);
	}

	public static WSDL parse(InputStream in, boolean loadReferencedFiles) throws XmlPullParserException, IOException {
		return parse(in, null, loadReferencedFiles);
	}

	public static WSDL parse(InputStream in, URI fromUri, boolean loadReferencedFiles) throws XmlPullParserException, IOException {
		return WSDLSupportFactory.getInstance().newParser().parse(in, fromUri, null, loadReferencedFiles);
	}

	public static WSDL parse(XmlPullParser parser) throws XmlPullParserException, IOException {
		return parse(parser, true);
	}

	public static WSDL parse(XmlPullParser parser, boolean loadReferencedFiles) throws XmlPullParserException, IOException {
		return WSDLSupportFactory.getInstance().newParser().parse(parser, null, null, loadReferencedFiles);
	}

	/**
	 * Creates an empty WSDL.
	 */
	public WSDL() {
		this("", "");
	}

	/**
	 * Creates an empty WSDL with the given target namespace.
	 * 
	 * @param targetNamespace the target namespace of the WSDL
	 */
	public WSDL(String targetNamespace) {
		this(targetNamespace, "");
	}

	/**
	 * @param targetNamespace the namespace this WSDL defines
	 * @param name the name of the WSDL
	 */
	public WSDL(String targetNamespace, String name) {
		super();
		this.targetNamespace = targetNamespace == null ? "" : targetNamespace;
		this.name = name;
		updateLastModified();
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
		StringBuffer sb = new StringBuffer();
		sb.append("[ name=").append(name);
		sb.append(", targetNamespace=").append(targetNamespace);
		sb.append(", types=").append(types);
		sb.append(", messages=").append(messages);
		sb.append(", portTypes=").append(portTypes);
		sb.append(", bindins=").append(bindings);
		sb.append(", services=").append(services);
		sb.append(" ]");
		return sb.toString();
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.ws4d.java.communication.Resource#serialize(org.ws4d.java.types.URI,
	 * org.ws4d.java.communication.RequestHeader, java.io.InputStream,
	 * java.io.OutputStream)
	 */
	public void serialize(URI request, RequestHeader requestHeader, InputStream requestBody, OutputStream out) throws IOException {
		serialize(out);
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
	 * @see org.ws4d.java.communication.Resource#getHeaderFields()
	 */
	public HashMap getHeaderFields() {
		HashMap map = new HashMap();
		map.put(HTTPConstants.HTTP_HEADER_LAST_MODIFIED, StringUtil.getHTTPDate(lastMod));
		return map;
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.communication.Resource#size()
	 */
	public long size() {
		return -1;
	}

	/**
	 * Returns a data structure of {@link WSDLOperation} instances, which belong
	 * to port types within this WSDL with an appropriate (i.e. supported)
	 * binding specified.
	 * 
	 * @return a data structure of all supported operations
	 */
	public DataStructure getSupportedOperations() {
		if (services != null && services.size() > 0) {
			/*
			 * if there are any services defined, we try using them as starting
			 * point for obtaining the legal port types and operations;
			 * otherwise, we fall back to supported bindings
			 */
			List l = new ArrayList();
			for (Iterator it = services.values().iterator(); it.hasNext();) {
				WSDLService service = (WSDLService) it.next();
				l.addAll(service.getOperations());
			}
			return l;
		}
		if (bindings == null) {
			return EmptyStructures.EMPTY_STRUCTURE;
		}
		List l = new ArrayList();
		for (Iterator it = bindings.values().iterator(); it.hasNext();) {
			WSDLBinding binding = (WSDLBinding) it.next();
			l.addAll(binding.getOperations());
		}
		return l;
	}

	/**
	 * Returns a data structure {@link WSDLPortType} instances within this WSDL
	 * with an appropriate (i.e. supported) binding specified.
	 * 
	 * @return a data structure of all supported port types
	 */
	public DataStructure getSupportedPortTypes() {
		if (services != null && services.size() > 0) {
			/*
			 * if there are any services defined, we try using them as starting
			 * point for obtaining the legal port types; otherwise, we fall back
			 * to supported bindings
			 */
			List l = new ArrayList();
			for (Iterator it = services.values().iterator(); it.hasNext();) {
				WSDLService service = (WSDLService) it.next();
				l.addAll(service.getPortTypes());
			}
			return l;
		}
		if (bindings == null) {
			return EmptyStructures.EMPTY_STRUCTURE;
		}
		List l = new ArrayList(bindings.size());
		for (Iterator it = bindings.values().iterator(); it.hasNext();) {
			WSDLBinding binding = (WSDLBinding) it.next();
			WSDLPortType portType = binding.getPortType();
			if (portType != null) {
				l.add(portType);
			}
		}
		return l;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		updateLastModified();
		this.name = name;
	}

	/**
	 * @return the targetNamespace
	 */
	public String getTargetNamespace() {
		return targetNamespace;
	}

	/**
	 * @param targetNamespace the targetNamespace to set
	 */
	public void setTargetNamespace(String targetNamespace) {
		updateLastModified();
		this.targetNamespace = targetNamespace;
	}

	/**
	 * @return a <code>copy</code> of the imports declared on this WSDL instance
	 */
	public HashMap getImports() {
		if (imports == null || imports.isEmpty()) {
			return EmptyStructures.EMPTY_MAP;
		}
		return new LinkedMap(imports);
	}

	/**
	 * @param name the local name of the port type to return; a namespace equal
	 *            to this WSDL's target namespace is assumed
	 * @return the named port type or <code>null</code>
	 */
	public WSDLPortType getPortType(String name) {
		return portTypes == null ? null : (WSDLPortType) portTypes.get(name);
	}

	/**
	 * @param name the qualified name of the port type to return
	 * @return the named port type or <code>null</code>
	 */
	public WSDLPortType getPortType(QName name) {
		String namespace = name == null ? null : name.getNamespace();
		if (targetNamespace.equals(namespace)) {
			if (portTypes == null) {
				return null;
			}
			return (WSDLPortType) portTypes.get(name.getLocalPart());
		}
		if (namespace == null || "".equals(namespace)) {
			// try within this instance
			WSDLPortType portType = (WSDLPortType) portTypes.get(name.getLocalPart());
			if (portType != null) {
				return portType;
			}
		}
		if (linkedWsdls == null) {
			return null;
		}
		// try linked WSDLs
		WSDL wsdl = (WSDL) linkedWsdls.get(namespace);
		if (wsdl != null) {
			return wsdl.getPortType(name.getLocalPart());
		}
		// try to find it within an imported/linked WSDL at a deeper level...
		for (Iterator it = linkedWsdls.values().iterator(); it.hasNext();) {
			wsdl = (WSDL) it.next();
			WSDLPortType portType = wsdl.getPortType(name);
			if (portType != null) {
				return portType;
			}
		}
		return null;
	}

	/**
	 * Returns an iterator over all port types defined within this WSDL
	 * instance.
	 * 
	 * @return an iterator over all defined port types
	 */
	public Iterator getPortTypes() {
		return portTypes == null ? EmptyStructures.EMPTY_ITERATOR : new ReadOnlyIterator(portTypes.values());
	}

	/**
	 * @param name the local name of the message to return; a namespace equal to
	 *            this WSDL's target namespace is assumed
	 * @return the named message or <code>null</code>
	 */
	public WSDLMessage getMessage(String name) {
		return messages == null ? null : (WSDLMessage) messages.get(name);
	}

	/**
	 * @param name the qualified name of the message to return
	 * @return the named message or <code>null</code>
	 */
	public WSDLMessage getMessage(QName name) {
		String namespace = name == null ? null : name.getNamespace();
		if (targetNamespace.equals(namespace)) {
			if (messages == null) {
				return null;
			}
			return (WSDLMessage) messages.get(name.getLocalPart());
		}
		if (linkedWsdls == null) {
			return null;
		}
		// try linked WSDLs
		WSDL wsdl = (WSDL) linkedWsdls.get(namespace);
		if (wsdl != null) {
			return wsdl.getMessage(name.getLocalPart());
		}
		// try to find it within an imported/linked WSDL at a deeper level...
		for (Iterator it = linkedWsdls.values().iterator(); it.hasNext();) {
			wsdl = (WSDL) it.next();
			WSDLMessage message = wsdl.getMessage(name);
			if (message != null) {
				return message;
			}
		}
		return null;
	}

	/**
	 * Returns an iterator over all messages defined within this WSDL instance.
	 * 
	 * @return an iterator over all defined messages
	 */
	public Iterator getMessages() {
		return messages == null ? EmptyStructures.EMPTY_ITERATOR : new ReadOnlyIterator(messages.values());
	}

	/**
	 * @param name the local name of the binding to return; a namespace equal to
	 *            this WSDL's target namespace is assumed
	 * @return the named binding or <code>null</code>
	 */
	public WSDLBinding getBinding(String name) {
		return bindings == null ? null : (WSDLBinding) bindings.get(name);
	}

	/**
	 * @param name the qualified name of the binding to return
	 * @return the named binding or <code>null</code>
	 */
	public WSDLBinding getBinding(QName name) {
		String namespace = name == null ? null : name.getNamespace();
		if (targetNamespace.equals(namespace)) {
			if (bindings == null) {
				return null;
			}
			return (WSDLBinding) bindings.get(name.getLocalPart());
		}
		if (linkedWsdls == null) {
			return null;
		}
		// try linked WSDLs
		WSDL wsdl = (WSDL) linkedWsdls.get(namespace);
		if (wsdl != null) {
			return wsdl.getBinding(name.getLocalPart());
		}
		// try to find it within an imported/linked WSDL at a deeper level...
		for (Iterator it = linkedWsdls.values().iterator(); it.hasNext();) {
			wsdl = (WSDL) it.next();
			WSDLBinding binding = wsdl.getBinding(name);
			if (binding != null) {
				return binding;
			}
		}
		return null;
	}

	/**
	 * Returns an iterator over all bindings defined within this WSDL instance.
	 * 
	 * @return an iterator over all defined bindings
	 */
	public Iterator getBindings() {
		return bindings == null ? EmptyStructures.EMPTY_ITERATOR : new ReadOnlyIterator(bindings.values());
	}

	/**
	 * Returns an iterator over all bindings for the port type with the given
	 * fully qualified name.
	 * 
	 * @param portType the fully qualified name of the port type for which to
	 *            find corresponding bindings
	 * @return an iterator over all bindings for the given port type
	 */
	public Iterator getBindings(QName portType) {
		if (portType == null || bindings == null) {
			return EmptyStructures.EMPTY_ITERATOR;
		}
		List l = new ArrayList();
		for (Iterator it = bindings.values().iterator(); it.hasNext();) {
			WSDLBinding binding = (WSDLBinding) it.next();
			if (portType.equals(binding.getTypeName())) {
				l.add(binding);
			}
		}
		return new ReadOnlyIterator(l);
	}

	/**
	 * @param name the local name of the service to return; a namespace equal to
	 *            this WSDL's target namespace is assumed
	 * @return the named service or <code>null</code>
	 */
	public WSDLService getService(String name) {
		return services == null ? null : (WSDLService) services.get(name);
	}

	/**
	 * @param name the qualified name of the service to return
	 * @return the named service or <code>null</code>
	 */
	public WSDLService getService(QName name) {
		String namespace = name == null ? null : name.getNamespace();
		if (targetNamespace.equals(namespace)) {
			if (services == null) {
				return null;
			}
			return (WSDLService) services.get(name.getLocalPart());
		}
		if (linkedWsdls == null) {
			return null;
		}
		// try linked WSDLs
		WSDL wsdl = (WSDL) linkedWsdls.get(namespace);
		if (wsdl != null) {
			return wsdl.getService(name.getLocalPart());
		}
		// try to find it within an imported/linked WSDL at a deeper level...
		for (Iterator it = linkedWsdls.values().iterator(); it.hasNext();) {
			wsdl = (WSDL) it.next();
			WSDLService service = wsdl.getService(name);
			if (service != null) {
				return service;
			}
		}
		return null;
	}

	/**
	 * Returns an iterator over all services defined within this WSDL instance.
	 * 
	 * @return an iterator over all defined services
	 */
	public Iterator getServices() {
		return services == null ? EmptyStructures.EMPTY_ITERATOR : new ReadOnlyIterator(services.values());
	}

	/**
	 * @param namespace the target namespace of the Schema instance to return
	 * @return the Schema instance with the given target namespace or
	 *         <code>null</code>
	 */
	public Schema getTypes(String namespace) {
		return types == null ? null : (Schema) types.get(namespace);
	}

	/**
	 * Returns an iterator over all <code>Schema</code> instances (i.e. XML
	 * schema definitions) referenced by this WSDL instance.
	 * 
	 * @return an iterator over all known Schema instances
	 */
	public Iterator getTypes() {
		return types == null ? EmptyStructures.EMPTY_ITERATOR : new ReadOnlyIterator(types.values());
	}

	/**
	 * @param name the local name of the schema type to return; a namespace
	 *            equal to this WSDL's target namespace is assumed
	 * @return the named schema type or <code>null</code>
	 */
	public Type getSchemaType(String name) {
		return getSchemaType(new QName(name, getTargetNamespace()));
	}

	/**
	 * @param name the qualified name of the schema type to return
	 * @return the named schema type or <code>null</code>
	 */
	public Type getSchemaType(QName name) {
		if (name == null || types == null) {
			return null;
		}
		Schema schema = (Schema) types.get(name.getNamespace());
		if (schema != null) {
			// a top-level schema
			return schema.getType(name);
		}
		// try to find an imported/linked schema inside the top-level ones ...
		for (Iterator it = types.values().iterator(); it.hasNext();) {
			schema = (Schema) it.next();
			Type type = schema.getType(name);
			if (type != null) {
				return type;
			}
		}
		// check linkedWSDLs
		if (linkedWsdls != null) {
			for (Iterator it = linkedWsdls.values().iterator(); it.hasNext();) {
				WSDL wsdl = (WSDL) it.next();
				Type type = wsdl.getSchemaType(name);
				if (type != null) {
					return type;
				}
			}
		}
		// last fallback, if it is a built-in schema type
		return SchemaUtil.getType(name);
	}

	/**
	 * @param name the local name of the schema element to return; a namespace
	 *            equal to this WSDL's target namespace is assumed
	 * @return the named schema element or <code>null</code>
	 */
	public Element getSchemaElement(String name) {
		return getSchemaElement(new QName(name, getTargetNamespace()));
	}

	/**
	 * @param name the qualified name of the schema element to return
	 * @return the named schema element or <code>null</code>
	 */
	public Element getSchemaElement(QName name) {
		if (name == null || types == null) {
			return null;
		}
		Schema schema = (Schema) types.get(name.getNamespace());
		if (schema != null) {
			// a top-level schema
			return schema.getElement(name);
		}
		// try to find an imported/linked schema inside the top-level ones ...
		for (Iterator it = types.values().iterator(); it.hasNext();) {
			schema = (Schema) it.next();
			Element element = schema.getElement(name);
			if (element != null) {
				return element;
			}
		}
		// check linkedWSDLs
		if (linkedWsdls != null) {
			for (Iterator it = linkedWsdls.values().iterator(); it.hasNext();) {
				WSDL wsdl = (WSDL) it.next();
				Element element = wsdl.getSchemaElement(name);
				if (element != null) {
					return element;
				}
			}
		}
		return null;
	}

	/**
	 * Adds an import information to this WSDL.
	 * 
	 * @param targetNamespace the target namespace of the imported WSDL
	 * @param location the location
	 */
	public void addImport(String targetNamespace, String location) {
		updateLastModified();
		if (imports == null) {
			imports = new LinkedMap();
		}
		imports.put(targetNamespace, location);
	}

	/**
	 * @param namespace the target namespace of the imported WSDL instance to
	 *            return
	 * @return the WSDL instance with the given target namespace as imported by
	 *         this WSDL or any of its recursively imported WSDLs or
	 *         <code>null</code>
	 */
	public WSDL getLinkedWsdlRecursive(String namespace) {
		if (linkedWsdls == null) {
			return null;
		}
		WSDL wsdl = (WSDL) linkedWsdls.get(namespace);
		if (wsdl != null) {
			return wsdl;
		}
		for (Iterator it = linkedWsdls.values().iterator(); it.hasNext();) {
			wsdl = (WSDL) it.next();
			wsdl = wsdl.getLinkedWsdlRecursive(namespace);
			if (wsdl != null) {
				return wsdl;
			}
		}
		return null;
	}

	/**
	 * @param namespace the target namespace of the imported WSDL instance to
	 *            return
	 * @return the WSDL instance with the given target namespace as imported by
	 *         this WSDL or <code>null</code>
	 */
	public WSDL getLinkedWsdl(String namespace) {
		return linkedWsdls == null ? null : (WSDL) linkedWsdls.get(namespace);
	}

	/**
	 * Returns an iterator over all <code>WSDL</code> instances imported by this
	 * WSDL instance.
	 * 
	 * @return an iterator over all imported WSDL instances
	 */
	public Iterator getLinkedWsdls() {
		return linkedWsdls == null ? EmptyStructures.EMPTY_ITERATOR : new ReadOnlyIterator(linkedWsdls.values());
	}

	public void addLinkedWsdl(WSDL wsdl) {
		if (wsdl == null) {
			return;
		}
		updateLastModified();
		if (linkedWsdls == null) {
			linkedWsdls = new LinkedMap();
		}
		linkedWsdls.put(wsdl.getTargetNamespace(), wsdl);
	}

	public void addTypes(Schema schema) {
		if (schema == null) {
			return;
		}
		updateLastModified();
		if (types == null) {
			types = new LinkedMap();
		}
		types.put(schema.getTargetNamespace(), schema);
	}

	public void addMessage(WSDLMessage message) {
		if (message == null) {
			return;
		}
		updateLastModified();
		if (messages == null) {
			messages = new LinkedMap();
		}
		messages.put(message.getLocalName(), message);
		message.setWsdl(this);
	}

	public void addPortType(WSDLPortType portType) {
		if (portType == null) {
			return;
		}
		updateLastModified();
		if (portTypes == null) {
			portTypes = new LinkedMap();
		}
		portTypes.put(portType.getLocalName(), portType);
		portType.setWsdl(this);
		declareCustomAttributeNamespaces(portType);
		for (Iterator it = portType.getOperations().iterator(); it.hasNext();) {
			WSDLOperation operation = (WSDLOperation) it.next();
			declareCustomAttributeNamespaces(operation);
			declareCustomAttributeNamespaces(operation.getInput());
			declareCustomAttributeNamespaces(operation.getOutput());
			for (Iterator it2 = operation.getFaults().iterator(); it2.hasNext();) {
				declareCustomAttributeNamespaces((IOType) it2.next());
			}
		}
	}

	public void addBinding(WSDLBinding binding) {
		if (binding == null) {
			return;
		}
		updateLastModified();
		if (bindings == null) {
			bindings = new LinkedMap();
		}
		bindings.put(binding.getLocalName(), binding);
		binding.setWsdl(this);
	}

	public void addService(WSDLService service) throws UnsupportedBindingException {
		if (service == null) {
			return;
		}
		updateLastModified();
		DataStructure ports = service.getPorts();
		for (Iterator it = ports.iterator(); it.hasNext();) {
			WSDLPort port = (WSDLPort) it.next();
			WSDLBinding binding = getBinding(port.getBindingName());
			if (binding == null) {
				throw new UnsupportedBindingException("no binding specified for port " + port.getName() + " of service " + service.getName());
			}
		}
		if (services == null) {
			services = new LinkedMap();
		}
		services.put(service.getLocalName(), service);
		service.setWsdl(this);
	}

	public void serialize(OutputStream out) throws IOException {
		WSDLSupportFactory.getInstance().newSerializer().serialize(this, out);
	}

	/**
	 * @return
	 */
	public HashMap getDefaultNamespaces() {
		if (defaultNamespaces == null) {
			// we were created from scratch, not loaded from a WSDL file
			defaultNamespaces = new LinkedMap(DEFAULT_NAMESPACES);
		} else if (defaultNamespaces.isEmpty()) {
			defaultNamespaces.putAll(DEFAULT_NAMESPACES);
		}
		return new LinkedMap(defaultNamespaces);
	}

	public void storeDefaultNamespaces(XmlPullParser parser) throws XmlPullParserException {
		int nsCount = parser.getNamespaceCount(parser.getDepth());
		if (nsCount == 0) {
			return;
		}
		updateLastModified();
		if (defaultNamespaces == null) {
			defaultNamespaces = new LinkedMap();
		}
		for (int i = 0; i < nsCount; i++) {
			String prefix = parser.getNamespacePrefix(i);
			String nsUri = parser.getNamespaceUri(i);
			if (!defaultNamespaces.containsKey(nsUri)) {
				defaultNamespaces.put(nsUri, prefix);
			}
		}
	}

	// added 2011-01-12 SSch allow inserting of default namespace, e.g. for
	// policy, streaming ...
	public void storeDefaultNamespace(String prefix, String nsUri) {
		if (nsUri == null || "".equals(nsUri)) {
			return;
		}
		updateLastModified();
		if (defaultNamespaces == null) {
			defaultNamespaces = new LinkedMap();
		}
		if (!defaultNamespaces.containsKey(nsUri)) {
			defaultNamespaces.put(nsUri, prefix);
		}
	}

	/**
	 * @param attributable
	 */
	void declareCustomAttributeNamespaces(Attributable attributable) {
		if (attributable == null) {
			return;
		}
		HashMap customAttributes = attributable.getAttributes();
		if (customAttributes == null) {
			return;
		}
		updateLastModified();
		for (Iterator it = customAttributes.entrySet().iterator(); it.hasNext();) {
			Entry ent = (Entry) it.next();
			QName attributeName = (QName) ent.getKey();
			CustomAttributeValue attributeValue = (CustomAttributeValue) ent.getValue();
			declareCustomAttributeNamespaces(attributeName, attributeValue);
		}
	}

	/**
	 * @param attributeName
	 * @param attributeValue
	 */
	void declareCustomAttributeNamespaces(QName attributeName, CustomAttributeValue attributeValue) {
		storeDefaultNamespace(attributeName.getPrefix(), attributeName.getNamespace());
		if (attributeValue == null) {
			return;
		}
		HashMap namespaces = attributeValue.getNamespaces();
		if (namespaces == null) {
			return;
		}
		updateLastModified();
		for (Iterator nsIt = namespaces.entrySet().iterator(); nsIt.hasNext();) {
			Entry nsEnt = (Entry) nsIt.next();
			storeDefaultNamespace((String) nsEnt.getValue(), (String) nsEnt.getKey());
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.communication.Resource#shortDescription()
	 */
	public String shortDescription() {
		StringBuffer sb = new StringBuffer();
		sb.append("WSDL [ ").append(targetNamespace).append(" ]");
		return sb.toString();
	}

	/* (non-Javadoc)
	 * @see org.ws4d.java.communication.Resource#getLastModifiedDate()
	 */
	public long getLastModifiedDate() {
		return lastMod;
	}

}
