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

import org.ws4d.java.structures.ArrayList;
import org.ws4d.java.structures.DataStructure;
import org.ws4d.java.structures.EmptyStructures;
import org.ws4d.java.structures.HashMap;
import org.ws4d.java.structures.Iterator;
import org.ws4d.java.structures.LinkedMap;
import org.ws4d.java.types.Attributable;
import org.ws4d.java.types.AttributableSupport;
import org.ws4d.java.types.CustomAttributeValue;
import org.ws4d.java.types.QName;
import org.ws4d.java.types.StringAttributeValue;
import org.xmlpull.v1.XmlSerializer;

/**
 * Implementation of the WSDL 1.1 Port Types.<br />
 * WSDL 1.1, 2.4 Port Types
 */
public class WSDLPortType extends NamedItem implements Attributable {

	public static final String	SUFFIX		= "PortType";

	private WSDL				wsdl;

	// key = OperationSignature instance, value = Operation instance
	private HashMap				operations;

	private boolean				eventSource	= false;

	private Attributable		attributableDelegate;

	/**
	 * 
	 */
	public WSDLPortType() {
		this(null);
	}

	public WSDLPortType(QName name) {
		super(name);
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("[ ");
		sb.append(super.toString());
		sb.append(", eventSource=").append(eventSource);
		sb.append(", operations=").append(operations);
		sb.append(", attributes=").append(attributableDelegate);
		sb.append(" ]");
		return sb.toString();
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
		if (wsdl != null) {
			wsdl.declareCustomAttributeNamespaces(name, value);
		}
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
		if (wsdl != null) {
			wsdl.declareCustomAttributeNamespaces(this);
		}
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
	 * @see
	 * org.ws4d.java.wsdl.NamedItem#serializeAttributes(org.xmlpull.v1.XmlSerializer
	 * )
	 */
	public void serializeAttributes(XmlSerializer serializer) throws IOException {
		if (attributableDelegate != null) {
			attributableDelegate.serializeAttributes(serializer);
		}
	}

	/**
	 * @return the eventSource
	 */
	public boolean isEventSource() {
		return eventSource;
	}

	/**
	 * @param eventSource the eventSource to set
	 */
	public void setEventSource(boolean eventSource) {
		this.eventSource = eventSource;
	}

	/**
	 * @param name the local name of the operation to return
	 * @param inputName the name of the operation's input element if any;
	 *            needed, in case more than one operation with the same name is
	 *            defined within the same port type
	 * @param outputName the name of the operation's output element, if any;
	 *            needed in case more than one operation with the same name is
	 *            defined within the same port type
	 * @return the named operation or <code>null</code>
	 */
	public WSDLOperation getOperation(String name, String inputName, String outputName) {
		// compatible with overloaded operations (use input/output names)
		return operations == null ? null : (WSDLOperation) operations.get(new OperationSignature(name, inputName, outputName));
	}

	public DataStructure getOperations() {
		return operations == null ? EmptyStructures.EMPTY_STRUCTURE : new ArrayList(operations.values());
	}

	/**
	 * @param operation throws IllegalArgumentException in case an operation
	 *            with exactly the same name and NO input and output already
	 *            exists
	 */
	public void addOperation(WSDLOperation operation) {
		if (operation == null) {
			return;
		}
		IOType input = operation.getInput();
		IOType output = operation.getOutput();
		if (input == null && output == null) {
			throw new IllegalArgumentException("operation without input and output: " + operation);
		}
		if (operations == null) {
			operations = new LinkedMap();
		}
		// compatible with overloaded operations (use input/output names)
		OperationSignature sig = new OperationSignature(operation);
		String inputName = input == null ? null : input.getName();
		String outputName = output == null ? null : output.getName();
		int inputCounter = 1;
		int outputCounter = 1;
		while (operations.containsKey(sig)) {
			if (input != null) {
				if (input.isNameSet()) {
					if (output == null || output.isNameSet()) {
						throw new IllegalArgumentException("duplicate operation: " + operation);
					} else {
						output.setNameInternal(outputName + outputCounter++);
					}
				} else {
					input.setNameInternal(inputName + inputCounter++);
				}
			} else {
				// output can not be null here
				if (output.isNameSet()) {
					throw new IllegalArgumentException("duplicate operation: " + operation);
				} else {
					output.setNameInternal(outputName + outputCounter++);
				}
			}
			sig = new OperationSignature(operation);
		}
		operations.put(sig, operation);
		operation.setPortType(this);
		if (wsdl != null) {
			wsdl.declareCustomAttributeNamespaces(operation);
			wsdl.declareCustomAttributeNamespaces(operation.getInput());
			wsdl.declareCustomAttributeNamespaces(operation.getOutput());
			for (Iterator it = operation.getFaults().iterator(); it.hasNext();) {
				wsdl.declareCustomAttributeNamespaces((IOType) it.next());
			}
		}
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

	private static class OperationSignature {

		private final String	name;

		private final String	inputName;

		private final String	outputName;

		OperationSignature(WSDLOperation operation) {
			this(operation.getName(), operation.getInputName(), operation.getOutputName());
		}

		/**
		 * @param name
		 * @param inputName
		 * @param outputName
		 */
		OperationSignature(String name, String inputName, String outputName) {
			super();
			this.name = name;
			this.inputName = inputName; // == null ? "" : inputName;
			this.outputName = outputName; // == null ? "" : outputName;
		}

		/*
		 * (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		public String toString() {
			StringBuffer sb = new StringBuffer();
			sb.append("OperationSignature [ name=").append(name);
			sb.append(", inputName=").append(inputName);
			sb.append(", outputName=").append(outputName).append(" ]");
			return sb.toString();
		}

		/*
		 * (non-Javadoc)
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (getClass() != obj.getClass()) {
				return false;
			}
			OperationSignature other = (OperationSignature) obj;
			if (!name.equals(other.name)) {
				return false;
			}
			if (inputName == null) {
				if (other.inputName != null) {
					return false;
				}
			} else if (!inputName.equals(other.inputName)) {
				return false;
			}
			if (outputName == null) {
				if (other.outputName != null) {
					return false;
				}
			} else if (!outputName.equals(other.outputName)) {
				return false;
			}
			return true;
		}

		/*
		 * (non-Javadoc)
		 * @see java.lang.Object#hashCode()
		 */
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + name.hashCode();
			result = prime * result + ((inputName == null) ? 0 : inputName.hashCode());
			result = prime * result + ((outputName == null) ? 0 : outputName.hashCode());
			return result;
		}

	}

}
