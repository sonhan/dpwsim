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

import org.ws4d.java.constants.WSDLConstants;
import org.ws4d.java.structures.DataStructure;
import org.ws4d.java.structures.EmptyStructures;
import org.ws4d.java.structures.HashMap;
import org.ws4d.java.types.QName;
import org.ws4d.java.wsdl.soap12.SOAP12DocumentLiteralHTTPBindingBuilder;
import org.xmlpull.v1.XmlSerializer;

/**
 * Implementation of the WSDL 1.1 Bindings.<br />
 * WSDL 1.1, 2.5 Bindings
 */
public abstract class WSDLBinding extends NamedItem {

	// key = QName of custom binding-level extension element, value =
	// WSDLBindingBuilder instance
	private static final HashMap	SUPPORTED_BINDING_BUILDERS	= new HashMap();

	static {
		SUPPORTED_BINDING_BUILDERS.put(WSDLConstants.SOAP12_BINDING_NAMESPACE_NAME, new SOAP12DocumentLiteralHTTPBindingBuilder());
	}

	private WSDL					wsdl;

	// name of port type this binding refers to
	private QName					typeName;

	public static WSDLBindingBuilder getBuilder(String namespace) {
		return (WSDLBindingBuilder) SUPPORTED_BINDING_BUILDERS.get(namespace);
	}

	/**
	 * 
	 */
	public WSDLBinding() {
		this(null);
	}

	/**
	 * @param name
	 */
	public WSDLBinding(QName name) {
		this(name, null);
	}

	/**
	 * @param name
	 * @param typeName the fully qualified name of the port type to which this
	 *            binding refers
	 */
	public WSDLBinding(QName name, QName typeName) {
		super(name);
		this.typeName = typeName;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("[ ");
		sb.append(super.toString());
		sb.append(", typeName=").append(typeName);
		sb.append(" ]");
		return sb.toString();
	}

	/**
	 * Returns the namespace URI which uniquely characterizes this specific
	 * binding.
	 * 
	 * @return the binding-specific URI for this binding
	 * @see WSDLBindingBuilder#getNamespace()
	 */
	public abstract String getBindingNamespace();

	public abstract void serializeBindingExtension(XmlSerializer serializer) throws IOException;

	public abstract void serializeOperationExtension(WSDLOperation operation, XmlSerializer serializer) throws IOException;

	public abstract void serializeInputExtension(IOType input, XmlSerializer serializer) throws IOException;

	public abstract void serializeOutputExtension(IOType output, XmlSerializer serializer) throws IOException;

	public abstract void serializeFaultExtension(IOType fault, XmlSerializer serializer) throws IOException;

	/**
	 * @param name the local name of the operation to return
	 * @param inputName the name of the operation's input element, if any;
	 *            needed in case more than one operation with the same name
	 *            defined within the same port type
	 * @param outputName the name of the operation's output element, if any;
	 *            needed in case more than one operation with the same name
	 *            defined within the same port type
	 * @return the named operation or <code>null</code>
	 */
	public WSDLOperation getOperation(String name, String inputName, String outputName) {
		WSDLPortType portType = getPortType();
		if (portType == null) {
			return null;
		}
		return portType.getOperation(name, inputName, outputName);
	}

	public DataStructure getOperations() {
		WSDLPortType portType = getPortType();
		if (portType == null) {
			return EmptyStructures.EMPTY_STRUCTURE;
		}
		return portType.getOperations();
	}

	public WSDLPortType getPortType() {
		return wsdl == null ? null : wsdl.getPortType(typeName);
	}

	/**
	 * @return the typeName
	 */
	public QName getTypeName() {
		return typeName;
	}

	/**
	 * @param typeName the typeName to set
	 */
	public void setTypeName(QName typeName) {
		this.typeName = typeName;
	}

	/**
	 * @return the wsdl
	 */
	public WSDL getWsdl() {
		return wsdl;
	}

	/**
	 * @param wsdl the wsdl to set
	 */
	void setWsdl(WSDL wsdl) {
		this.wsdl = wsdl;
	}

}
