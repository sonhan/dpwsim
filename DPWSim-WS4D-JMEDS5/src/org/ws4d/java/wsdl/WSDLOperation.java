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

import org.ws4d.java.structures.ArrayList;
import org.ws4d.java.structures.DataStructure;
import org.ws4d.java.structures.EmptyStructures;
import org.ws4d.java.structures.HashMap;
import org.ws4d.java.structures.Iterator;
import org.ws4d.java.structures.LinkedMap;
import org.ws4d.java.structures.List;
import org.ws4d.java.types.AttributableSupport;
import org.ws4d.java.types.CustomAttributeValue;
import org.ws4d.java.types.QName;

/**
 * Implementation of the WSDL 1.1 Operation.<br />
 * WSDL 1.1, 2.4ff
 */
public class WSDLOperation extends AttributableSupport {

	public static final int	TYPE_UNKNOWN			= -1;

	public static final int	TYPE_ONE_WAY			= 1;

	public static final int	TYPE_REQUEST_RESPONSE	= 2;

	public static final int	TYPE_SOLICIT_RESPONSE	= 3;

	public static final int	TYPE_NOTIFICATION		= 4;

	private WSDLPortType	portType;

	private String			name;

	private int				type					= TYPE_UNKNOWN;

	private int				derivedType				= type;

	private IOType			input					= null;

	private IOType			output					= null;

	// key = name of fault as String, value = IOType instance
	private HashMap			faults;

	public static String typeToString(int operationType) {
		switch (operationType) {
			case (TYPE_ONE_WAY): {
				return "One-Way";
			}
			case (TYPE_REQUEST_RESPONSE): {
				return "Request-Response";
			}
			case (TYPE_SOLICIT_RESPONSE): {
				return "Solicit-Response";
			}
			case (TYPE_NOTIFICATION): {
				return "Notification";
			}
		}
		return "Unknown";
	}

	/**
	 * Create an empty WSDL operation with a name set to <code>null</code>.
	 */
	public WSDLOperation() {
		this(null);
	}

	/**
	 * @param name
	 */
	public WSDLOperation(String name) {
		super();
		this.name = name;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("[ name=").append(name);
		sb.append(", type=").append(getTypeAsString());
		sb.append(", input=").append(input);
		sb.append(", output=").append(output);
		sb.append(", faults=").append(faults);
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
		return portType == null ? null : portType.getWsdl();
	}

	/**
	 * Returns a data structure of {@link WSDLMessagePart} instances describing
	 * the parts of this operation's input message.
	 * 
	 * @return the parts of this operation's input message
	 */
	public DataStructure getInputParts() {
		return input == null ? EmptyStructures.EMPTY_STRUCTURE : input.getParts();
	}

	/**
	 * Returns a data structure of {@link WSDLMessagePart} instances describing
	 * the parts of this oepration's output message.
	 * 
	 * @return the parts of this operation's output message
	 */
	public DataStructure getOutputParts() {
		return output == null ? EmptyStructures.EMPTY_STRUCTURE : output.getParts();
	}

	/**
	 * Returns a data structure of {@link WSDLMessagePart} instances describing
	 * the parts of this operation's named fault message.
	 * 
	 * @param faultName the local name of the requested fault; a namespace equal
	 *            to this operation's namespace is assumed
	 * @return the parts of this operation's named fault message.
	 */
	public DataStructure getFaultParts(String faultName) {
		IOType fault = getFault(faultName);
		return fault == null ? EmptyStructures.EMPTY_STRUCTURE : fault.getParts();
	}

	public WSDLMessage getInputMessage() {
		return input == null ? null : input.getMessage();
	}

	public WSDLMessage getOutputMessage() {
		return output == null ? null : output.getMessage();
	}

	/**
	 * @return the messages of all faults
	 */
	public DataStructure getFaultMessages() {
		if (faults == null || !isBidirectional()) {
			return EmptyStructures.EMPTY_STRUCTURE;
		}
		List l = new ArrayList(faults.size());
		for (Iterator it = faults.values().iterator(); it.hasNext();) {
			IOType fault = (IOType) it.next();
			WSDLMessage message = fault.getMessage();
			if (message != null) {
				l.add(message);
			}
		}
		return l;
	}

	/**
	 * @param name the local name of the fault of which to return the message; a
	 *            namespace equal to this operation's namespace is assumed
	 * @return the message of the named fault or <code>null</code>
	 */
	public WSDLMessage getFaultMessage(String name) {
		IOType fault = getFault(name);
		return fault == null ? null : fault.getMessage();
	}

	/**
	 * @return the name of this WSDL operation's input, if any, or
	 *         <code>null</code>
	 */
	public String getInputName() {
		return input == null ? null : input.getName();
	}

	/**
	 * @return the name of this WSDL operation's output, if any, or
	 *         <code>null</code>
	 */
	public String getOutputName() {
		return output == null ? null : output.getName();
	}

	/**
	 * Returns the namespace within which this operation resides. This is the
	 * namespace of the surrounding port type if one exists, or
	 * <code>null</code> otherwise.
	 * 
	 * @return the namespace of the port type of this operation, or
	 *         <code>null</code> if port type not set
	 */
	public String getNamespace() {
		return portType == null ? null : portType.getNamespace();
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
		this.name = name;
	}

	/**
	 * @return the type
	 */
	public int getType() {
		return type == TYPE_UNKNOWN ? derivedType == TYPE_UNKNOWN ? deriveType() : derivedType : type;
	}

	/**
	 * @param type the type to set
	 */
	public void setType(int type) {
		this.type = type;
	}

	public String getTypeAsString() {
		return typeToString(getType());
	}

	public boolean isRequest() {
		int type = getType();
		return (type == TYPE_ONE_WAY || type == TYPE_REQUEST_RESPONSE);
	}

	public boolean isEvented() {
		if (portType != null && !portType.isEventSource()) {
			return false;
		}
		int type = getType();
		return (type == TYPE_NOTIFICATION || type == TYPE_SOLICIT_RESPONSE);
	}

	/**
	 * Returns <code>true</code> only if this operation's {@link #getType()
	 * transmission type} is either {@link #TYPE_REQUEST_RESPONSE
	 * request-response} or {@link #TYPE_SOLICIT_RESPONSE solicit-response}.
	 * 
	 * @return whether this is a bidirectional operation or not
	 */
	public boolean isBidirectional() {
		int type = getType();
		return (type == TYPE_REQUEST_RESPONSE || type == TYPE_SOLICIT_RESPONSE);
	}

	/**
	 * Returns the WS-Addressing action URI for the input of this operation if
	 * any.
	 * 
	 * @return the WS-Addressing action URI for the input of this operation
	 */
	public String getInputAction() {
		return input == null ? null : input.getAction();
	}

	/**
	 * Returns the WS-Addressing action URI for the output of this operation if
	 * any.
	 * 
	 * @return the WS-Addressing action URI for the output of this operation
	 */
	public String getOutputAction() {
		return output == null ? null : output.getAction();
	}

	public HashMap getFaultActions() {
		if (faults == null || !isBidirectional()) {
			return EmptyStructures.EMPTY_MAP;
		}
		HashMap m = new LinkedMap(faults.size());
		for (Iterator it = faults.values().iterator(); it.hasNext();) {
			IOType fault = (IOType) it.next();
			m.put(fault.getName(), fault.getAction());
		}
		return m;
	}

	/**
	 * @return the input
	 */
	public IOType getInput() {
		return input;
	}

	/**
	 * @param input the input to set
	 */
	public void setInput(IOType input) {
		if (output == null) {
			derivedType = TYPE_ONE_WAY;
		} else {
			derivedType = TYPE_SOLICIT_RESPONSE;
		}
		this.input = input;
		input.setOperation(this);
		WSDL wsdl = getWsdl();
		if (wsdl != null) {
			wsdl.declareCustomAttributeNamespaces(input);
		}
	}

	/**
	 * @return the output
	 */
	public IOType getOutput() {
		return output;
	}

	/**
	 * @param output the output to set
	 */
	public void setOutput(IOType output) {
		if (input == null) {
			derivedType = TYPE_NOTIFICATION;
		} else {
			derivedType = TYPE_REQUEST_RESPONSE;
		}
		this.output = output;
		output.setOperation(this);
		WSDL wsdl = getWsdl();
		if (wsdl != null) {
			wsdl.declareCustomAttributeNamespaces(output);
		}
	}

	/**
	 * @return the faults
	 */
	public DataStructure getFaults() {
		return faults == null || !isBidirectional() ? EmptyStructures.EMPTY_STRUCTURE : new ArrayList(faults.values());
	}

	/**
	 * @param name the local name of the fault to return; a namespace equal to
	 *            this operation's namespace is assumed
	 * @return the named fault or <code>null</code>
	 */
	public IOType getFault(String name) {
		return faults == null || !isBidirectional() ? null : (IOType) faults.get(name);
	}

	/**
	 * @param fault the fault to add
	 */
	public void addFault(IOType fault) {
		if (fault == null) {
			return;
		}
		if (faults == null) {
			faults = new LinkedMap();
		}
		faults.put(fault.getName(), fault);
		fault.setOperation(this);
		WSDL wsdl = getWsdl();
		if (wsdl != null) {
			wsdl.declareCustomAttributeNamespaces(fault);
		}
	}

	/**
	 * @return the portType
	 */
	public WSDLPortType getPortType() {
		return portType;
	}

	/**
	 * @param portType the portType to set
	 */
	void setPortType(WSDLPortType portType) {
		this.portType = portType;
	}

	private int deriveType() {
		if (input == null) {
			if (output == null) {
				derivedType = TYPE_UNKNOWN;
			} else {
				derivedType = TYPE_NOTIFICATION;
			}
		} else {
			if (output == null) {
				derivedType = TYPE_ONE_WAY;
			} else {
				derivedType = TYPE_UNKNOWN;
			}
		}
		return derivedType;
	}

}
