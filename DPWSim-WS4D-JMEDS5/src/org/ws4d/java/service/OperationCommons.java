/*******************************************************************************
 * Copyright (c) 2009 MATERNA Information & Communications. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html. For further
 * project-related information visit http://www.ws4d.org. The most recent
 * version of the JMEDS framework can be obtained from
 * http://sourceforge.net/projects/ws4d-javame.
 ******************************************************************************/
package org.ws4d.java.service;

import java.util.NoSuchElementException;

import org.ws4d.java.constants.SOAPConstants;
import org.ws4d.java.schema.ComplexType;
import org.ws4d.java.schema.Element;
import org.ws4d.java.schema.ElementContainer;
import org.ws4d.java.schema.Type;
import org.ws4d.java.service.parameter.ParameterValue;
import org.ws4d.java.structures.DataStructure;
import org.ws4d.java.structures.EmptyStructures;
import org.ws4d.java.structures.HashMap;
import org.ws4d.java.structures.HashSet;
import org.ws4d.java.structures.Iterator;
import org.ws4d.java.structures.ReadOnlyIterator;
import org.ws4d.java.structures.Set;
import org.ws4d.java.types.Attributable;
import org.ws4d.java.types.AttributableSupport;
import org.ws4d.java.types.CustomAttributeValue;
import org.ws4d.java.types.QName;
import org.ws4d.java.types.URI;
import org.ws4d.java.util.StringUtil;
import org.ws4d.java.util.WS4DIllegalStateException;
import org.ws4d.java.wsdl.IOType;
import org.ws4d.java.wsdl.WSDLMessagePart;
import org.ws4d.java.wsdl.WSDLOperation;

/**
 * This class is the common base for both {@link Operation}s and
 * {@link DefaultEventSource}s within the DPWS framework. It contains all common
 * properties for WSDL 1.1 compliant operations. It is further used to declare
 * an operation's or event's {@link #getInput() input} and {@link #getOuput()
 * output} parameters as well as any {@link #getFaults() faults} that may occur
 * on invocation. Overall in this class' documentation, the term
 * <em>&quot;operation&quot;</em> is used to mean either an actual
 * {@link Operation operation} or an {@link DefaultEventSource event}.
 * <p>
 * <strong>Note:</strong> According to <a href="http://www.w3.org/TR/wsdl">WSDL
 * 1.1 Specification</a>, an operation's {@ink #getName() name} is not required
 * to be unique within the scope of its containing port type in order to support
 * overloading. However, when overloading operations, the combination of each
 * one's {@link #getName() name}, {@link #getInputName() input name} and
 * {@link #getOutputName() output name} must be unique in order to avoid name
 * clashes.
 * </p>
 */
public abstract class OperationCommons extends AttributableSupport implements OperationDescription {

	/* The Transmission Type of the Operation */
	protected int			type					= WSDLOperation.TYPE_UNKNOWN;

	protected Attributable	inputAttributable;

	protected Attributable	outputAttributable;

	/* The Name of this operation. */
	private final String	name;

	/* The WSDL PortType of the Operation */
	private final QName		portType;												// Changed
																					// 2010-08-11
																					// SSch
																					// Changed
																					// to
																					// final,
																					// Thx
																					// to
																					// Stefan
																					// Schlichting

	private Service			service;

	/* The name of the WSDL input element */
	private String			inputName;

	/* The name of the WSDL output element */
	private String			outputName;

	/* The wsa:Action input name. */
	private String			inputAction;

	/* The wsa:Action output name. */
	private String			outputAction;

	// key = local name of fault as String, value = Fault instance
	private final HashMap	faults					= new HashMap();

	private Set				customComplexTypes		= null;

	private boolean			managedInput			= true;

	private boolean			managedOutput			= true;

	private Element			input					= null;

	private Element			output					= null;

	private boolean			inputNameSet			= false;

	private boolean			outputNameSet			= false;

	private boolean			inputActionSet			= false;

	private boolean			outputActionSet			= false;

	private boolean			inputActionExtended		= false;

	private boolean			outputActionExtended	= false;

	/**
	 * default constructor
	 * 
	 * Son Han added to solve the serialization problem
	 * @date 2013/12/12
	 * 
	 */
	
	protected OperationCommons() {
		super();
		this.name = StringUtil.simpleClassName(this.getClass());
		this.portType = new QName(StringUtil.simpleClassName(this.getClass()), null);
	}
	
	/**
	 * Creates a new instance with the given name and port type.
	 * 
	 * @param name the name of the operation, must be unique within the scope of
	 *            its port type
	 * @param portType the qualified port type
	 */
	
	protected OperationCommons(String name, QName portType) {
		super();
		if (name == null) {
			// this.name = this.getClass().getSimpleName();
			this.name = StringUtil.simpleClassName(this.getClass());
		} else {
			this.name = name;
		}
		if (portType == null) {
			// this.portType = new QName(this.getClass().getSimpleName(), null);
			this.portType = new QName(StringUtil.simpleClassName(this.getClass()), null);
		} else {
			this.portType = portType;
		}
	}

	protected OperationCommons(WSDLOperation operation) {
		this(operation.getName(), operation.getPortType().getName());
		setType(operation.getType());
		setInputName(operation.getInputName());
		setOutputName(operation.getOutputName());

		// copy action URIs from WSDLOperation to our operation
		setInputAction(operation.getInputAction());
		setOutputAction(operation.getOutputAction());

		if (operation.hasAttributes()) {
			setAttributes(operation.getAttributes());
		}

		IOType input = operation.getInput();
		if (input != null) {
			if (input.hasAttributes()) {
				setInputAttributes(input.getAttributes());
			}
		}

		IOType output = operation.getOutput();
		if (output != null) {
			if (output.hasAttributes()) {
				setOutputAttributes(output.getAttributes());
			}
		}

		DataStructure inputParts = operation.getInputParts();
		int size = inputParts.size();
		if (size > 1) {
			throw new IllegalArgumentException("Unsupported WSDL feature (input message with more than one parts ): " + operation.getInputMessage());
		} else if (size == 1) {
			WSDLMessagePart part = (WSDLMessagePart) inputParts.iterator().next();
			if (part.isElement()) {
				setInput(part.getElement());
			} else {
				/*
				 * don't throw unsupported WSDL feature exception, if we can
				 * create a unique element from the referenced type
				 */
				// throw new
				// IllegalArgumentException("Unsupported WSDL feature (message part referring to a type): "
				// + part);
				Element element = new Element(getInputName(), getPortType().getNamespace(), part.getType());
				setInput(element);
			}
		}
		DataStructure outputParts = operation.getOutputParts();
		size = outputParts.size();
		if (size > 1) {
			throw new IllegalArgumentException("Unsupported WSDL feature (output message with more than one parts): " + operation.getInputMessage());
		} else if (size == 1) {
			WSDLMessagePart part = (WSDLMessagePart) outputParts.iterator().next();
			if (part.isElement()) {
				setOutput(part.getElement());
			} else {
				/*
				 * don't throw unsupported WSDL feature exception, if we can
				 * create a unique element from the referenced type
				 */
				// throw new
				// IllegalArgumentException("Unsupported WSDL feature (message part referring to a type): "
				// + part);
				Element element = new Element(getOutputName(), getPortType().getNamespace(), part.getType());
				setInput(element);
			}
		}
		// add faults, too
		DataStructure faults = operation.getFaults();
		for (Iterator it = faults.iterator(); it.hasNext();) {
			IOType faultIO = (IOType) it.next();
			Fault fault = new Fault(faultIO);

			if (faultIO.hasAttributes()) {
				fault.setAttributes(faultIO.getAttributes());
			}

			addFault(fault);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		StringBuffer sb = new StringBuffer(getClass().getName());
		sb.append(" [ name=").append(getName());
		sb.append(", portType=").append(getPortType());
		sb.append(", type=").append(WSDLOperation.typeToString(getType()));
		sb.append(", inputName=").append(getInputName());
		sb.append(", outputName=").append(getOutputName());
		sb.append(", inputAction=").append(getInputAction());
		sb.append(", outputAction=").append(getOutputAction());
		sb.append(", input=").append(getInput());
		sb.append(", output=").append(getOutput());
		sb.append(" ]");
		return sb.toString();
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public final boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		OperationCommons other = (OperationCommons) obj;
		if (!name.equals(other.name)) {
			return false;
		}
		if (portType == null) {
			if (other.portType != null) {
				return false;
			}
		} else if (!portType.equals(other.portType)) {
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
	public final int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + name.hashCode();
		result = prime * result + ((portType == null) ? 0 : portType.hashCode());
		result = prime * result + ((inputName == null) ? 0 : inputName.hashCode());
		result = prime * result + ((outputName == null) ? 0 : outputName.hashCode());
		return result;
	}

	/**
	 * Returns the local name of this operation. See {@link OperationCommons
	 * note on overloading operations}.
	 * 
	 * @return the local name of this operation
	 */
	public String getName() {
		return name;
	}

	/**
	 * Returns the port type that this operation belongs to.
	 * 
	 * @return this operation's port type
	 */
	public QName getPortType() {
		return portType;
	}

	/**
	 * Returns the <code>transmission type</code> of this operation according to
	 * <a href="http://www.w3.org/TR/wsdl">WSDL 1.1 specification</a>. The value
	 * returned is one of {@link WSDLOperation#TYPE_ONE_WAY},
	 * {@link WSDLOperation#TYPE_REQUEST_RESPONSE},
	 * {@link WSDLOperation#TYPE_NOTIFICATION} and
	 * {@link WSDLOperation#TYPE_SOLICIT_RESPONSE}.
	 * 
	 * @return the transmission type of this operation
	 */
	public abstract int getType();

	/**
	 * Returns the name of this operation's input. If this operation doesn't
	 * have any input (i.e. its {@link #getType() transmission type} is
	 * {@link WSDLOperation#TYPE_NOTIFICATION}, <code>null</code> is returned.
	 * Otherwise, if no input name was previously {@link #setInputName(String)
	 * set}, a default input name is generated according to WSDL 1.1
	 * Specification.
	 * 
	 * @return this operation's input name
	 * @see #setInputName(String)
	 */
	public String getInputName() {
		if (getType() == WSDLOperation.TYPE_NOTIFICATION) {
			return null;
		}
		if (inputName == null) {
			inputName = generateDefaultInputName();
			inputNameSet = false;
		}
		return inputName;
	}

	/**
	 * Sets the name of this operation's input. See {@link OperationCommons note
	 * on overloading operations}.
	 * 
	 * @param inputName the new input name to set
	 * @throws WS4DIllegalStateException if this instance is already added to a
	 *             service
	 * @see #getInputName()
	 */
	public void setInputName(String inputName) {
		checkModifiable();
		setInputNameInternal(inputName);
		if (inputName != null) {
			inputNameSet = true;
		}
	}

	/**
	 * Returns the WS-Addressing [action] property for the input message of this
	 * operation. Will return <code>null</code> only if this operation's
	 * {@link #getType() transmission type} is
	 * {@link WSDLOperation#TYPE_NOTIFICATION}, i.e. the operation doesn't have
	 * any input. Otherwise, if no input action was explicitly
	 * {@link #setInputAction(String) set}, this method will generate a default
	 * one according to the rules of <a
	 * href="http://www.w3.org/TR/ws-addr-wsdl/">WS-Addressing 1.0 - WSDL
	 * Binding</a>.
	 * 
	 * @return the WS-Addressing [action] property of this operation's input
	 * @see #setInputAction(String)
	 */
	public String getInputAction() {
		if (getType() == WSDLOperation.TYPE_NOTIFICATION) {
			return null;
		}
		if (inputAction == null) {
			inputAction = generateDefaultAction(true);
			inputActionSet = false;
			inputActionExtended = false;
		}
		return inputAction;
	}

	/**
	 * Sets the WS-Addressing [action] property for this operation's input
	 * message
	 * 
	 * @param inputAction the new [action] to set; must be an absolute URI
	 * @throws WS4DIllegalStateException if this instance is already added to a
	 *             service
	 * @see #getInputAction()
	 */
	public void setInputAction(String inputAction) {
		checkModifiable();
		this.inputAction = inputAction;
		if (inputAction != null) {
			inputActionSet = true;
			inputActionExtended = false;
		}
	}

	/**
	 * Returns the name of this operation's output. If this operation doesn't
	 * have any output (i.e. its {@link #getType() transmission type} is
	 * {@link WSDLOperation#TYPE_ONE_WAY}, <code>null</code> is returned.
	 * Otherwise, if no output name was previously
	 * {@link #setOutputName(String) set} a default output name is generated
	 * according to WSDL 1.1 Specification.
	 * 
	 * @return this operation's output name
	 * @see #setOutputName(String)
	 */
	public String getOutputName() {
		if (getType() == WSDLOperation.TYPE_ONE_WAY) {
			return null;
		}
		if (outputName == null) {
			outputName = generateDefaultOutputName();
			outputNameSet = false;
		}
		return outputName;
	}

	/**
	 * Sets the name of this operation's output. See {@link OperationCommons
	 * note on overloading operations}.
	 * 
	 * @param outputName the new output name to set
	 * @throws WS4DIllegalStateException if this instance is already added to a
	 *             service
	 * @see #getOutputName()
	 */
	public void setOutputName(String outputName) {
		checkModifiable();
		setOutputNameInternal(outputName);
		if (outputName != null) {
			outputNameSet = true;
		}
	}

	/**
	 * Returns the WS-Addressing [action] property for the output message of
	 * this operation. Will return <code>null</code> only if this operation's
	 * {@link #getType() transmission type} is
	 * {@link WSDLOperation#TYPE_ONE_WAY}, i.e. the operation doesn't have any
	 * output. Otherwise, if no output action is explicitly
	 * {@link #setInputAction(String) set}, this method will generate a default
	 * one according to the rules of <a
	 * href="http://www.w3.org/TR/ws-addr-wsdl/">WS-Addressing 1.0 - WSDL
	 * Binding</a>.
	 * 
	 * @return the WS-Addressing [action] property of this operation's output
	 * @see #setOutputAction(String)
	 */
	public String getOutputAction() {
		if (getType() == WSDLOperation.TYPE_ONE_WAY) {
			return null;
		}
		if (outputAction == null) {
			outputAction = generateDefaultAction(false);
			outputActionSet = false;
			outputActionExtended = false;
		}
		return outputAction;
	}

	/**
	 * Sets the WS-Addressing [action] property for this operation's output
	 * message.
	 * 
	 * @param outputAction the new [action] to set; must be an absolute URI
	 * @throws WS4DIllegalStateException if this instance has already been added
	 *             to a service
	 * @see #getOutputAction()
	 */
	public void setOutputAction(String outputAction) {
		checkModifiable();
		this.outputAction = outputAction;
		if (outputAction != null) {
			outputActionSet = true;
			outputActionExtended = false;
		}
	}

	/**
	 * Returns the input element of this operation. The input element defines
	 * the data structure for the input parameters of this operation. It can be
	 * used to {@link #createInputValue() create} a suitable
	 * {@link ParameterValue} container for the actual values when sending
	 * messages to the operation.
	 * 
	 * @return the input element
	 * @see #setInput(Element)
	 */
	public Element getInput() {
		return input;
	}

	/**
	 * TODO
	 * 
	 * @param parameterName
	 * @return
	 */
	public Element getInputParameter(String parameterName) {
		if (!managedInput) {
			throw new WS4DIllegalStateException("unable to retrieve input parameter when input is set explicitly via setInput(Element)");
		}
		if (input == null) {
			return null;
		}
		ComplexType wrapperType = (ComplexType) input.getType();
		ElementContainer wrapperContainer = wrapperType.getContainer();
		return wrapperContainer.getLocalElementByName(new QName(parameterName, getPortType().getNamespace()));
	}

	/**
	 * Sets the input of this operation. This element is assumed to contain all
	 * input parameters of this operation.
	 * 
	 * @param element the input element
	 * @see #getInput()
	 */
	public void setInput(Element element) {
		checkModifiable();
		checkNamespace(element);
		input = element;
		managedInput = false;
		resetType();
	}

	/**
	 * Adds an input parameter with the given <code>parameterName</code> and of
	 * the specified <code>type</code> to this operation. This method creates a
	 * wrapper element of a {@link ComplexType complex type} with a single
	 * top-most container of type {@link ComplexType#CONTAINER_SEQUENCE} and
	 * uses this as {@link #getInput() input} of the operation. The wrapper
	 * element's name is equal to this operation's {@link #getName() name}. This
	 * conforms to the <a href=
	 * "http://atmanes.blogspot.com/2005/03/wrapped-documentliteral-convention.html"
	 * >document-literal wrapped style binding</a> for SOAP 1.2. The new
	 * parameter is then appended to the end of the sequence container of the
	 * wrapper element.
	 * <p>
	 * <strong>NOTE:</strong> Using this method is only possible when the input
	 * element for this operation was <em>NOT</em> previously set by a call to
	 * {@link #setInput(Element)}. Otherwise, this method will throw a
	 * {@link WS4DIllegalStateException}.
	 * </p>
	 * 
	 * @param parameterName the name of the input parameter to add
	 * @param type the type for the new input parameter
	 * @throws WS4DIllegalStateException if an input element was previously
	 *             {@link #setInput(Element) set} on this operation
	 */
	public void addInputParameter(String parameterName, Type type) {
		checkModifiable();
		if (!managedInput) {
			throw new WS4DIllegalStateException("unable to add input parameter when input is set explicitly via setInput(Element)");
		}
		ComplexType wrapperType;
		if (input == null) {
			wrapperType = new ComplexType(ComplexType.CONTAINER_SEQUENCE);
			input = new Element(new QName(getName(), getPortType().getNamespace()), wrapperType);
			resetType();
		} else {
			wrapperType = (ComplexType) input.getType();
		}
		ElementContainer wrapperContainer = wrapperType.getContainer();
		wrapperContainer.addElement(new Element(new QName(parameterName, getPortType().getNamespace()), type));
		managedInput = true;
	}

	/**
	 * Returns the output element of this operation. The output element defines
	 * the data structure for the output parameters of this operation. It can be
	 * used to {@link #createOutputValue() create} a suitable
	 * {@link ParameterValue} container for the actual values an operation
	 * returns.
	 * 
	 * @return the output element
	 * @see #setOutput(Element)
	 */
	public Element getOutput() {
		return output;
	}

	/**
	 * TODO
	 * 
	 * @param parameterName
	 * @return
	 */
	public Element getOutputParameter(String parameterName) {
		if (!managedOutput) {
			throw new WS4DIllegalStateException("unable to retrieve output parameter when output is set explicitly via setOutput(Element)");
		}
		if (output == null) {
			return null;
		}
		ComplexType wrapperType = (ComplexType) output.getType();
		ElementContainer wrapperContainer = wrapperType.getContainer();
		return wrapperContainer.getLocalElementByName(new QName(parameterName, getPortType().getNamespace()));
	}

	/**
	 * Sets the output of this operation. This element is assumed to contain all
	 * output parameters of this operation.
	 * 
	 * @param element the output element
	 * @see #getOutput()
	 */
	public void setOutput(Element element) {
		checkModifiable();
		checkNamespace(element);
		output = element;
		managedOutput = false;
		resetType();
	}

	/**
	 * Adds an output parameter with the given <code>parameterName</code> and of
	 * the specified <code>type</code> to this operation. This method creates a
	 * wrapper element of a {@link ComplexType complex type} with a single
	 * top-most container of type {@link ComplexType#CONTAINER_SEQUENCE} and
	 * uses this as {@link #getOutput() output} of the operation. The wrapper
	 * element's name is equal to this operation's {@link #getName() name} with
	 * a 'Response' suffix appended to it. This conforms to the <a href=
	 * "http://atmanes.blogspot.com/2005/03/wrapped-documentliteral-convention.html"
	 * >document-literal wrapped style binding</a> for SOAP 1.2. The new
	 * parameter is then appended to the end of the sequence container of the
	 * wrapper element.
	 * <p>
	 * <strong>NOTE:</strong> Using this method is only possible when the output
	 * element for this operation was <em>NOT</em> previously set by a call to
	 * {@link #setOutput(Element)}. Otherwise, this method will throw a
	 * {@link WS4DIllegalStateException}.
	 * </p>
	 * 
	 * @param parameterName the name of the output parameter to add
	 * @param type the type for the new output parameter
	 * @throws WS4DIllegalStateException if an output element was previously
	 *             {@link #setOutput(Element) set} on this operation
	 */
	public void addOutputParameter(String parameterName, Type type) {
		checkModifiable();
		if (!managedOutput) {
			throw new WS4DIllegalStateException("unable to add output parameter when output is set explicitly via setOutput(Element)");
		}
		ComplexType wrapperType;
		if (output == null) {
			wrapperType = new ComplexType(ComplexType.CONTAINER_SEQUENCE);
			output = new Element(new QName(getName() + IOType.RESPONSE_SUFFIX, getPortType().getNamespace()), wrapperType);
			resetType();
		} else {
			wrapperType = (ComplexType) output.getType();
		}
		ElementContainer wrapperContainer = wrapperType.getContainer();
		wrapperContainer.addElement(new Element(new QName(parameterName, getPortType().getNamespace()), type));
		managedOutput = true;
	}

	/**
	 * Returns an {@link Iterator} over all faults declared for this operation.
	 * 
	 * @return an iterator over this operation's faults
	 * @see #addFault(Fault)
	 */
	public Iterator getFaults() {
		return new ReadOnlyIterator(faults.values());
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.service.OperationDescription#getFaultCount()
	 */
	public int getFaultCount() {
		return faults.size();
	}

	/**
	 * Returns this operation's fault with the given <code>faultName</code>,
	 * which is considered to be unique within the operation's scope.
	 * 
	 * @param faultName the requested fault's name
	 * @return the fault with the given name or <code>null</code> if there is no
	 *         fault with that name within this operation
	 * @see #addFault(Fault)
	 * @see #getFaults()
	 */
	public Fault getFault(String faultName) {
		return (Fault) faults.get(faultName);
	}

	/**
	 * Adds the given <code>fault</code> to this operation.
	 * <p>
	 * A fault on a web service operation corresponds to a checked declared
	 * exception on a Java method. If an operation's
	 * {@link Operation#invoke(ParameterValue) invocation} causes a fault, the
	 * invoking client will receive an {@link InvocationException} providing
	 * additional information about this fault.
	 * </p>
	 * 
	 * @param fault the fault to add
	 * @throws NullPointerException if <code>fault</code> is null
	 * @throws IllegalArgumentException if a fault with the given name already
	 *             exists, as fault names must be unique within the scope of an
	 *             operation
	 * @see #getFaults()
	 * @see #getFault(String)
	 */
	public void addFault(Fault fault) {
		checkModifiable();
		if (fault == null) {
			throw new NullPointerException("fault is null");
		}
		String faultName = fault.getName();
		if (faults.containsKey(faultName)) {
			throw new IllegalArgumentException("duplicate fault name: " + faultName);
		}
		if (fault.getAction() == null) {
			// action not set explicitly, generate one
			fault.setAction(generateDefaultFaultAction(faultName));
		}
		faults.put(fault.getName(), fault);
		fault.attached = true;
		checkNamespace(fault.getElement());
		resetType();
	}

	/**
	 * Removes the fault with the given <code>faultName</code> from this
	 * operation.
	 * 
	 * @param faultName the name of the fault to remove
	 * @see #addFault(Fault)
	 * @see #getFault(String)
	 */
	public void removeFault(String faultName) {
		checkModifiable();
		Fault fault = (Fault) faults.remove(faultName);
		if (fault != null) {
			fault.attached = false;
		}
		if (getFaultCount() == 0) {
			resetType();
		}
	}

	/**
	 * This is a shorthand method for creating a {@link ParameterValue parameter
	 * value} container for the output of this operation. It is semantically
	 * equivalent to
	 * <code>ParameterValue.createElementValue(getOutput());</code>.
	 * 
	 * @return a parameter container suitable for this operation's output
	 * @see #getOutput()
	 * @see ParameterValue#createElementValue(Element)
	 */
	public ParameterValue createOutputValue() {
		return ParameterValue.createElementValue(getOutput());
	}

	/**
	 * This is a shorthand method for creating a {@link ParameterValue parameter
	 * value} container for the input of this operation. It is semantically
	 * equivalent to <code>ParameterValue.createElementValue(getInput());</code>
	 * .
	 * 
	 * @return a parameter container suitable for this operation's input
	 * @see #getInput()
	 * @see ParameterValue#createElementValue(Element)
	 */
	public ParameterValue createInputValue() {
		return ParameterValue.createElementValue(getInput());
	}

	/**
	 * This method creates a {@link ParameterValue parameter value} container
	 * for the fault with the given unique <code>faultName</code> (within the
	 * scope of this operation).
	 * 
	 * @param faultName the name of the fault to create a parameter container
	 *            for
	 * @return the parameter container suitable for the specified fault
	 * @throws NoSuchElementException if a fault with the given name is not
	 *             declared within this operation
	 * @see #getFault(String)
	 */
	public ParameterValue createFaultValue(String faultName) {
		Fault fault = getFault(faultName);
		if (fault == null) {
			throw new NoSuchElementException("unknown fault: " + faultName);
		}
		return fault.createValue();
	}

	/**
	 * Returns the service to which this operation is associated. May return
	 * <code>null</code>, if this operation has yet not been added to a
	 * particular service.
	 * 
	 * @return the service to which this operation belongs
	 */
	public Service getService() {
		return service;
	}

	/**
	 * Returns the value of the input attribute with the given <code>name</code>
	 * or <code>null</code> if this attribute is not available (or if its value
	 * is explicitly set to <code>null</code>).
	 * 
	 * @param attributeName the name of the input attribute of which to query
	 *            the value
	 * @return the value for the named input attribute or <code>null</code>
	 */
	public CustomAttributeValue getInputAttribute(QName attributeName) {
		return inputAttributable == null ? null : inputAttributable.getAttribute(attributeName);
	}

	/**
	 * Sets the <code>value</code> for the input attribute with the specified
	 * <code>name</code>. Throws a
	 * <code>java.lang.IllegalArgumentException</code> in case <code>name</code>
	 * is <code>null</code>.
	 * 
	 * @param attributeName the name of the input attribute to set, must not be
	 *            <code>null</code>
	 * @param value the value to which to set the named input attribute (may be
	 *            <code>null</code>
	 * @throws IllegalArgumentException if <code>name</code> is
	 *             <code>null</code>
	 */
	public void setInputAttribute(QName attributeName, CustomAttributeValue value) {
		if (inputAttributable == null) {
			inputAttributable = new AttributableSupport();
		}
		inputAttributable.setAttribute(attributeName, value);
	}

	/**
	 * Returns all input attributes explicitly set on this instance. Note that
	 * depending on the actual implementation the returned reference may point
	 * at the 'life map', i .e. the actual storage for the input attributes.
	 * Modifications to that map should therefore be performed with care and
	 * keeping this in mind.
	 * 
	 * @return all already set input attributes
	 */
	public HashMap getInputAttributes() {
		if (inputAttributable == null) {
			inputAttributable = new AttributableSupport();
		}
		return inputAttributable.getAttributes();
	}

	/**
	 * Sets all input attributes at once to those contained within argument
	 * <code>attributes</code>. Note that depending on the actual implementation
	 * it is possible that the map <code>attributes</code> points at may be used
	 * for the actual internal storage of the input attributes (i.e. without
	 * copying it). This is why, after passing it to this method, modifications
	 * to this map should be made with care . This method throws a
	 * <code>java.lang.IllegalArgumentException</code> in case
	 * <code>attributes</code> is <code>null</code>.
	 * 
	 * @param attributes the new input attributes to set
	 * @throws IllegalArgumentException if <code>attributes</code> is
	 *             <code>null</code>
	 */
	public void setInputAttributes(HashMap attributes) {
		if (inputAttributable == null) {
			inputAttributable = new AttributableSupport();
		}
		inputAttributable.setAttributes(attributes);
	}

	/**
	 * Returns <code>true</code> only if this instance has at least one input
	 * attribute set. Returns <code>false</code> in any other case.
	 * 
	 * @return <code>true</code> only if there is at least one input attribute
	 *         set within this instance
	 */
	public boolean hasInputAttributes() {
		return inputAttributable != null && inputAttributable.hasAttributes();
	}

	/**
	 * Returns the value of the output attribute with the given
	 * <code>name</code> or <code>null</code>, if this attribute is not
	 * available (or if its value is actually explicitly set to
	 * <code>null</code>).
	 * 
	 * @param attributeName the name of the output attribute to query the value
	 *            of
	 * @return the value for the named output attribute or <code>null</code>
	 */
	public CustomAttributeValue getOutputAttribute(QName attributeName) {
		return outputAttributable == null ? null : outputAttributable.getAttribute(attributeName);
	}

	/**
	 * Sets the <code>value</code> of the output attribute with the specified
	 * <code>name</code>. Throws a
	 * <code>java.lang.IllegalArgumentException</code> in case <code>name</code>
	 * is <code>null</code>.
	 * 
	 * @param attributeName the name of the output attribute to set, must not be
	 *            <code>null</code>
	 * @param value the value to set the named output attribute to (may be
	 *            <code>null</code>
	 * @throws IllegalArgumentException if <code>name</code> is
	 *             <code>null</code>
	 */
	public void setOutputAttribute(QName attributeName, CustomAttributeValue value) {
		if (outputAttributable == null) {
			outputAttributable = new AttributableSupport();
		}
		outputAttributable.setAttribute(attributeName, value);
	}

	/**
	 * Returns all output attributes explicitly set on this instance. Note that
	 * depending on the actual implementation the returned reference may point
	 * at the 'life map', i .e. the actual storage for the output attributes.
	 * Thus, modifications to that map should be performed with care and with
	 * this in mind.
	 * 
	 * @return all already set output attributes
	 */
	public HashMap getOutputAttributes() {
		if (outputAttributable == null) {
			outputAttributable = new AttributableSupport();
		}
		return outputAttributable.getAttributes();
	}

	/**
	 * Sets all output attributes at once to those contained within argument
	 * <code>attributes</code>. Note that depending on the actual implementation
	 * it is possible that the map <code>attributes</code> points at may be used
	 * for the actual internal storage of the output attributes (i.e. without
	 * copying it). That is why modifications to this map should be made with
	 * care after passing it to this method. This method throws a
	 * <code>java.lang.IllegalArgumentException</code> in case
	 * <code>attributes</code> is <code>null</code>.
	 * 
	 * @param attributes the new output attributes to set
	 * @throws IllegalArgumentException if <code>attributes</code> is
	 *             <code>null</code>
	 */
	public void setOutputAttributes(HashMap attributes) {
		if (outputAttributable == null) {
			outputAttributable = new AttributableSupport();
		}
		outputAttributable.setAttributes(attributes);
	}

	/**
	 * Returns <code>true</code> only if this instance has at least one output
	 * attribute set. Returns <code>false</code> in any other case.
	 * 
	 * @return <code>true</code> only if there is at least one output attribute
	 *         set within this instance
	 */
	public boolean hasOutputAttributes() {
		return outputAttributable != null && outputAttributable.hasAttributes();
	}

	/**
	 * Returns the value of the fault attribute with the given <code>name</code>
	 * for the fault with the specified unique <code>faultName</code> or
	 * <code>null</code>, either if this attribute is not available (or if its
	 * value is actually explicitly set to <code>null</code>).
	 * <p>
	 * This method throws a <code>java.lang.IllegalArgumentException</code> if a
	 * fault with the given <code>faultName</code> is not found within this
	 * instance.
	 * </p>
	 * 
	 * @param faultName the unique name of the fault within the scope of this
	 *            instance, see {@link #getFault(String)}
	 * @param attributeName the name of the fault attribute to query the value
	 *            of
	 * @return the value for the named fault attribute or <code>null</code>
	 * @throws IllegalArgumentException if no fault with the given
	 *             <code>faultName</code> is found
	 */
	public CustomAttributeValue getFaultAttribute(String faultName, QName attributeName) {
		Fault fault = getFault(faultName);
		if (fault == null) {
			throw new IllegalArgumentException("no such fault: " + faultName);
		}
		return fault.getAttribute(attributeName);
	}

	/**
	 * Sets the <code>value</code> for the fault attribute with the specified
	 * <code>name</code> of the fault with the given unique
	 * <code>faultName</code>. Throws a
	 * <code>java.lang.IllegalArgumentException</code> in case there is no fault
	 * with the given <code>faultName</code> within this instance or if
	 * <code>name</code> is <code>null</code>.
	 * 
	 * @param faultName the unique name of the fault within the scope of this
	 *            instance, see {@link #getFault(String)}
	 * @param attributeName the name of the fault attribute to set, must not be
	 *            <code>null</code>
	 * @param value the value to set the named fault attribute to (may be
	 *            <code>null</code>
	 * @throws IllegalArgumentException if there is no fault with the given
	 *             <code>faultName</code> within this instance or if
	 *             <code>name</code> is <code>null</code>
	 */
	public void setFaultAttribute(String faultName, QName attributeName, CustomAttributeValue value) {
		Fault fault = getFault(faultName);
		if (fault == null) {
			throw new IllegalArgumentException("no such fault: " + faultName);
		}
		fault.setAttribute(attributeName, value);
	}

	/**
	 * Returns all fault attributes explicitly set on this instance for the
	 * fault with the given unique <code>faultName</code>. Note that depending
	 * on the actual implementation the returned reference may point at the
	 * 'life map', i .e. the actual storage for the fault attributes. Thus,
	 * modifications to that map should be performed with care and keeping this
	 * in mind.
	 * <p>
	 * This method throws a <code>java.lang.IllegalArgumentException</code> if a
	 * fault with the given <code>faultName</code> is not found within this
	 * instance.
	 * </p>
	 * 
	 * @param faultName the unique name of the fault within the scope of this
	 *            instance, see {@link #getFault(String)}
	 * @return all already set fault attributes
	 * @throws IllegalArgumentException if no fault with the given
	 *             <code>faultName</code> is found
	 */
	public HashMap getFaultAttributes(String faultName) {
		Fault fault = getFault(faultName);
		if (fault == null) {
			throw new IllegalArgumentException("no such fault: " + faultName);
		}
		return fault.getAttributes();
	}

	/**
	 * Sets at once all fault attributes of the fault with unique
	 * <code>faultName</code> to those contained within argument
	 * <code>attributes</code>. Note that depending on the actual
	 * implementation, it is possible that the map <code>attributes</code>
	 * points at may be used for the actual internal storage of the fault
	 * attributes (i.e. without copying it). That is why, after passing it to
	 * this method, modifications to this map should be made with care. This
	 * method throws a <code>java.lang.IllegalArgumentException</code> in case
	 * <code>attributes</code> is <code>null</code>.
	 * 
	 * @param faultName the unique name of the fault within the scope of this
	 *            instance, see {@link #getFault(String)}
	 * @param attributes the new fault attributes to set
	 * @throws IllegalArgumentException if no fault with the given
	 *             <code>faultName</code> is found or if <code>attributes</code>
	 *             is <code>null</code>
	 */
	public void setFaultAttributes(String faultName, HashMap attributes) {
		Fault fault = getFault(faultName);
		if (fault == null) {
			throw new IllegalArgumentException("no such fault: " + faultName);
		}
		fault.setAttributes(attributes);
	}

	/**
	 * Returns <code>true</code> only if this instance has at least one fault
	 * attribute set for the fault with the specified unique
	 * <code>faultName</code>. Returns <code>false</code> in any other case,
	 * including when there is no fault with the given <code>faultName</code>.
	 * 
	 * @param faultName the unique name of the fault within the scope of this
	 *            instance, see {@link #getFault(String)}
	 * @return <code>true</code> only if there is at least one fault attribute
	 *         set for the named fault within this instance
	 */
	public boolean hasFaultAttributes(String faultName) {
		Fault fault = getFault(faultName);
		return fault != null && fault.hasAttributes();
	}

	/**
	 * @param service the service to set
	 */
	public void setService(Service service) {
		this.service = service;
	}

	/**
	 * This method doesn't toggle the inputNameSet flag
	 * 
	 * @param inputName
	 */
	public void setInputNameInternal(String inputName) {
		this.inputName = inputName;
		if (!inputActionSet) {
			// reset action name, so we can generate default again
			inputAction = null;
		}
	}

	/**
	 * This method doesn't toggle the outputNameSet flag
	 * 
	 * @param outputName
	 */
	public void setOutputNameInternal(String outputName) {
		this.outputName = outputName;
		if (!outputActionSet) {
			// reset action name, so we can generate default again
			outputAction = null;
		}
	}

	public String setExtendedDefaultInputAction() {
		int type = getType();
		if (type == WSDLOperation.TYPE_UNKNOWN) {
			return null;
		}
		String delim = IOType.URL_DELIMITER;
		if (portType.getNamespace().startsWith(URI.URN_SCHEMA_PREFIX)) {
			delim = IOType.URN_DELIMITER;
		}
		inputActionExtended = true;
		return inputAction = buildActionName(getInputName() + delim + getOutputName(), delim);
	}

	public String setExtendedDefaultOutputAction() {
		int type = getType();
		if (type == WSDLOperation.TYPE_UNKNOWN) {
			return null;
		}
		String delim = IOType.URL_DELIMITER;
		if (portType.getNamespace().startsWith(URI.URN_SCHEMA_PREFIX)) {
			delim = IOType.URN_DELIMITER;
		}
		outputActionExtended = true;
		return outputAction = buildActionName(getOutputName() + delim + getInputName(), delim);
	}

	public boolean isInputNameSet() {
		return inputNameSet;
	}

	public boolean isInputActionSet() {
		return inputActionSet;
	}

	public boolean isOutputNameSet() {
		return outputNameSet;
	}

	public boolean isOutputActionSet() {
		return outputActionSet;
	}

	public boolean isInputActionExtended() {
		return inputActionExtended;
	}

	public boolean isOutputActionExtended() {
		return outputActionExtended;
	}

	/**
	 * This method adds a custom <code>xsd:ComplexType</code> to the operation.
	 * This custom type will be serialized in the WSDL document.
	 * <p>
	 * <h3>Notice</h3> Do <strong>not</strong> add custom types with the same
	 * name as the types used for input or output definitions. This may
	 * overwrite the normal definitions.
	 * </p>
	 * <p>
	 * The added custom type <strong>MUST</strong> have a valid name. If no name
	 * is set, this method will throw an {@link RuntimeException}.
	 * </p>
	 * 
	 * @param type the custom type which should be add to the WSDL document.
	 */
	public synchronized void addCustomComplexType(ComplexType type) {
		if (type.getName() == null) throw new RuntimeException("Cannot add custom complex type with out name to the operation.");
		if (customComplexTypes == null) {
			customComplexTypes = new HashSet();
		}
		customComplexTypes.add(type);
	}

	/**
	 * Remove a given custom <code>xsd:ComplexType</code> from the operation.
	 * 
	 * @param type the custom type which should be removed.
	 * @see #addCustomComplexType(ComplexType)
	 */
	public synchronized void removeCustomComplexType(ComplexType type) {
		if (customComplexTypes == null) {
			return;
		}
		customComplexTypes.remove(type);
	}

	/**
	 * Clears the list of custom types.
	 * 
	 * @see #addCustomComplexType(ComplexType)
	 */
	public synchronized void clearCustomComplexTypes() {
		if (customComplexTypes == null) {
			return;
		}
		customComplexTypes = null;
	}

	/**
	 * Returns an iterator containing {@link ComplexType}. This list contains
	 * the custom types set for this operation.
	 * 
	 * @return an iterator containing {@link ComplexType}.
	 * @see #addCustomComplexType(ComplexType)
	 */
	public synchronized Iterator getCustomComplexTypes() {
		if (customComplexTypes == null) {
			return EmptyStructures.EMPTY_ITERATOR;
		}
		return customComplexTypes.iterator();
	}

	/**
	 * Set the <code>type</code> of this operation, which is one of the
	 * following: TYPE_UNKNOWN = -1; TYPE_ONE_WAY = 1; TYPE_REQUEST_RESPONSE =
	 * 2; TYPE_SOLICIT_RESPONSE = 3; TYPE_NOTIFICATION = 4;
	 * 
	 * @param type
	 * @throws WS4DIllegalStateException if this instance has already been added
	 *             to a service
	 */
	private void setType(int type) {
		checkModifiable();
		this.type = type;
	}

	private void checkModifiable() {
		if (service != null) {
			throw new WS4DIllegalStateException("unable to modify after being added to service");
		}
	}

	private void checkNamespace(Element element) {
		if (element != null) {
			QName name = element.getName();
			if (name != null) {
				String namespace = name.getNamespace();
				String localPart = name.getLocalPart();
				String prefix = name.getPrefix();
				int priority = name.getPriority();

				if ("".equals(namespace)) {
					if (portType == null) {
						namespace = generateDefaultNamespace();
					} else {
						namespace = getPortType().getNamespace();
					}
					name = new QName(localPart, namespace, prefix, priority);
					element.setName(name);
					element.checkNamespace(element.getType());
				}
			}
		}
	}

	private String generateDefaultNamespace() {
		return "http://www.ws4d.org";
	}

	private String generateDefaultInputName() {
		return generateDefaultName(true);
	}

	private String generateDefaultOutputName() {
		return generateDefaultName(false);
	}

	private String generateDefaultName(boolean input) {
		String opName = getName();
		switch (getType()) {
			case (WSDLOperation.TYPE_ONE_WAY): {
				// input equal to this operation's name
				if (input) {
					return opName;
				}
				break;
			}
			case (WSDLOperation.TYPE_NOTIFICATION): {
				// output equal to this operation's name
				if (!input) {
					return opName;
				}
				break;
			}
			case (WSDLOperation.TYPE_REQUEST_RESPONSE): {
				// input equal to this operation's name + "Request" suffix
				if (input) {
					return opName + IOType.REQUEST_SUFFIX;
				}
				// output equal to this operation's name + "Response" suffix
				else if (!input) {
					return opName + IOType.RESPONSE_SUFFIX;
				}
				break;
			}
			case (WSDLOperation.TYPE_SOLICIT_RESPONSE): {
				// output equal to this operation's name + "Solicit" suffix
				if (!input) {
					return opName + IOType.SOLICIT_SUFFIX;
				}
				// input equal to this operation's name + "Response" suffix
				else if (input) {
					return opName + IOType.RESPONSE_SUFFIX;
				}
				break;
			}
		}
		return null;
	}

	/**
	 * Decides and generates a DefaultActionName
	 * 
	 * @param input
	 * @return DefaultActionName
	 */
	private String generateDefaultAction(boolean input) {
		int type = getType();
		if (type == WSDLOperation.TYPE_UNKNOWN) {
			return null;
		}
		String delim = IOType.URL_DELIMITER;
		if (portType.getNamespace().startsWith(URI.URN_SCHEMA_PREFIX)) {
			delim = IOType.URN_DELIMITER;
		}
		switch (type) {
			case (WSDLOperation.TYPE_ONE_WAY): {
				return buildActionName(getInputName(), delim);
			}
			case (WSDLOperation.TYPE_REQUEST_RESPONSE): {
				if (input)
					return buildActionName(getInputName(), delim);
				else
					return buildActionName(getOutputName(), delim);
			}
			case (WSDLOperation.TYPE_SOLICIT_RESPONSE): {
				if (input)
					return buildActionName(getInputName(), delim);
				else
					return buildActionName(getOutputName(), delim);
			}
			case (WSDLOperation.TYPE_NOTIFICATION): {
				return buildActionName(getOutputName(), delim);
			}
		}
		return null;
	}

	/**
	 * Builds the DefaultActionName String
	 * 
	 * @param delim
	 * @param ioTypeName
	 * @return DefaultActionName String
	 */
	private String buildActionName(String ioTypeName, String delim) {
		StringBuffer text = new StringBuffer();
		String namespace = portType.getNamespace();
		text.append(namespace);
		if (!namespace.endsWith(delim)) {
			text.append(delim);
		}
		text.append(portType.getLocalPart());
		text.append(delim);
		text.append(ioTypeName);
		return text.toString();
	}

	/**
	 * Decides and generates a DefaultFaultAction String
	 * 
	 * @param faultName
	 * @return defaultFaultAction String
	 */
	private String generateDefaultFaultAction(String faultName) {
		String delim = IOType.URL_DELIMITER;
		String namespace = portType.getNamespace();
		if (namespace.startsWith(URI.URN_SCHEMA_PREFIX)) {
			delim = IOType.URN_DELIMITER;
		}
		StringBuffer text = new StringBuffer();
		text.append(namespace);
		if (!namespace.endsWith(delim)) {
			text.append(delim);
		}
		text.append(portType.getLocalPart());
		text.append(delim);
		text.append(getName());
		text.append(delim);
		text.append(SOAPConstants.SOAP_ELEM_FAULT);
		text.append(delim);
		text.append(faultName);
		return text.toString();
	}

	private void resetType() {
		type = WSDLOperation.TYPE_UNKNOWN;
	}

}
