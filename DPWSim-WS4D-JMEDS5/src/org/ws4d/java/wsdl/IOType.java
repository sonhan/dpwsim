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

import org.ws4d.java.structures.DataStructure;
import org.ws4d.java.structures.EmptyStructures;
import org.ws4d.java.structures.HashMap;
import org.ws4d.java.types.AttributableSupport;
import org.ws4d.java.types.CustomAttributeValue;
import org.ws4d.java.types.QName;
import org.ws4d.java.types.URI;

/**
 * WSDL 1.1 <code>paramType</code>. Possible usage = wsdl:input, wsdl:output,
 * wsdl:fault.
 */
public class IOType extends AttributableSupport {

	public static final String	SUFFIX_INPUT	= "Input";

	public static final String	SUFFIX_OUTPUT	= "Output";

	public static final String	SUFFIX_FAULT	= "Fault";

	// CHANGED 2010-08-11 SSch Changed to blank see
	// http://pzf.fremantle.org/2007/05/handlign.html
	public static final String	REQUEST_SUFFIX	= "";

	public static final String	RESPONSE_SUFFIX	= "Response";

	public static final String	SOLICIT_SUFFIX	= "Solicit";

	public static final String	URL_DELIMITER	= "/";

	public static final String	URN_DELIMITER	= ":";

	private WSDLOperation		operation;

	private String				name;

	private QName				messageName;

	private String				action;

	private boolean				nameSet			= false;

	private boolean				actionSet		= false;

	/**
	 * 
	 */
	public IOType() {
		this(null);
	}

	/**
	 * @param message
	 */
	public IOType(QName message) {
		this(null, message);
	}

	/**
	 * @param name
	 * @param message
	 */
	public IOType(String name, QName message) {
		super();
		this.name = name;
		this.messageName = message;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("[ name=").append(name);
		sb.append(", messageName=").append(messageName);
		sb.append(", action=").append(getAction());
		sb.append(" ]");
		return sb.toString();
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.ws4d.java.types.AttributableSupport#setAttribute(org.ws4d.java.types
	 * .QName, org.ws4d.java.types.CustomAttributeValue)
	 */
	public void setAttribute(QName name, CustomAttributeValue value) {
		super.setAttribute(name, value);
		WSDL wsdl = getWsdl();
		if (wsdl != null) {
			wsdl.declareCustomAttributeNamespaces(name, value);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.types.AttributableSupport#setAttributes(org.ws4d.java.
	 * structures.HashMap)
	 */
	public void setAttributes(HashMap attributes) {
		super.setAttributes(attributes);
		WSDL wsdl = getWsdl();
		if (wsdl != null) {
			wsdl.declareCustomAttributeNamespaces(this);
		}
	}

	public WSDL getWsdl() {
		return operation == null ? null : operation.getWsdl();
	}

	public WSDLPortType getPortType() {
		return operation == null ? null : operation.getPortType();
	}

	/**
	 * Returns the namespace within which this input/output/fault element
	 * resides. This is the namespace of the surrounding operation if one
	 * exists, or <code>null</code> otherwise.
	 * 
	 * @return the namespace of the surrounding operation or <code>null</code>
	 *         if operation not set
	 */
	public String getNamespace() {
		return operation == null ? null : operation.getNamespace();
	}

	public String getName() {
		if (name == null) {
			name = generateDefaultName();
			nameSet = false;
		}
		return name;
	}

	public void setName(String name) {
		setNameInternal(name);
		if (name != null) {
			nameSet = true;
		}
	}

	public WSDLMessage getMessage() {
		WSDL wsdl = getWsdl();
		return wsdl == null ? null : wsdl.getMessage(messageName);
	}

	public DataStructure getParts() {
		WSDLMessage message = getMessage();
		return message == null ? EmptyStructures.EMPTY_STRUCTURE : message.getParts();
	}

	/**
	 * Returns the WSDL messageName for this
	 * 
	 * @return the messageName
	 */
	public QName getMessageName() {
		return messageName;
	}

	/**
	 * Sets the WSDL messageName.
	 * 
	 * @param messageName the messageName to set.
	 */
	public void setMessage(QName messageName) {
		this.messageName = messageName;
	}

	/**
	 * Returns the wsa:Action URI as String.
	 * 
	 * @return the action URI as String
	 */
	public String getAction() {
		if (action == null) {
			action = generateDefaultAction();
			actionSet = false;
		}
		return action;
	}

	/**
	 * Sets the wsa:Action.
	 * 
	 * @param action the action to set
	 */
	public void setAction(String action) {
		this.action = action;
		if (action != null) {
			actionSet = true;
		}
	}

	/**
	 * @return the operation
	 */
	public WSDLOperation getOperation() {
		return operation;
	}

	/**
	 * @param operation the operation to set
	 */
	void setOperation(WSDLOperation operation) {
		this.operation = operation;
	}

	/**
	 * This method doesn't toggle the nameSet flag
	 * 
	 * @param name
	 */
	void setNameInternal(String name) {
		this.name = name;
		if (!actionSet) {
			// reset action name, so we can generate default again
			action = null;
		}
	}

	protected boolean isNameSet() {
		return nameSet;
	}

	boolean isActionSet() {
		return actionSet;
	}

	private String generateDefaultName() {
		WSDLOperation operation = getOperation();
		if (operation == null) {
			return null;
		}
		switch (operation.getType()) {
			case (WSDLOperation.TYPE_ONE_WAY): {
				// input equal to this operation's name
				if (operation.getInput() == this) {
					return operation.getName();
				}
				break;
			}
			case (WSDLOperation.TYPE_NOTIFICATION): {
				// output equal to this operation's name
				if (operation.getOutput() == this) {
					return operation.getName();
				}
				break;
			}
			case (WSDLOperation.TYPE_REQUEST_RESPONSE): {
				// input equal to this operation's name + "Request" suffix
				if (operation.getInput() == this) {
					return operation.getName() + REQUEST_SUFFIX;
				}
				// output equal to this operation's name + "Response" suffix
				else if (operation.getOutput() == this) {
					return operation.getName() + RESPONSE_SUFFIX;
				}
				break;
			}
			case (WSDLOperation.TYPE_SOLICIT_RESPONSE): {
				// output equal to this operation's name + "Solicit" suffix
				if (operation.getOutput() == this) {
					return operation.getName() + SOLICIT_SUFFIX;
				}
				// input equal to this operation's name + "Response" suffix
				else if (operation.getInput() == this) {
					return operation.getName() + RESPONSE_SUFFIX;
				}
				break;
			}
		}
		return null;
	}

	private String generateDefaultAction() {
		/*
		 * if this is NOT a fault IO, than we use this default pattern: [target
		 * namespace][delimiter][port type name][delimiter][input|output name]
		 * otherwise, the default pattern must be like this: [target
		 * namespace][delimiter][port type name][delimiter][operation
		 * name][delimiter]Fault[delimiter][fault name]
		 */
		WSDL wsdl = getWsdl();
		if (wsdl == null) {
			return "";
		}
		String namespace = wsdl.getTargetNamespace();
		if (namespace == null || "".equals(namespace)) {
			return "";
		}
		WSDLPortType portType = getPortType();
		if (portType == null) {
			return "";
		}
		String localName = getName();
		if (localName == null || "".equals(localName)) {
			return "";
		}
		String delim = URL_DELIMITER;
		if (namespace.startsWith(URI.URN_SCHEMA_PREFIX)) {
			delim = URN_DELIMITER;
		}
		StringBuffer result = new StringBuffer();
		result.append(namespace);
		if (!namespace.endsWith(delim)) {
			result.append(delim);
		}
		result.append(portType.getLocalName());
		result.append(delim);

		WSDLOperation operation = getOperation();
		if (operation.getFault(name) != null) {
			// we are a fault
			result.append(operation.getName());
			result.append(delim);
			result.append("Fault");
			result.append(delim);
		}
		result.append(localName);
		return result.toString();
	}

}
