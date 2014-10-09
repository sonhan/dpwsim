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

import org.ws4d.java.communication.TimeoutException;
import org.ws4d.java.schema.ComplexType;
import org.ws4d.java.schema.Element;
import org.ws4d.java.schema.SimpleType;
import org.ws4d.java.service.parameter.ParameterValue;
import org.ws4d.java.types.QName;
import org.ws4d.java.wsdl.WSDLOperation;

/**
 * An operation is an abstraction of executable code. Operations are (together
 * with {@link DefaultEventSource events}) the main parts of a DPWS service
 * implementation.
 * <p>
 * The actual business logic behind an operation is contained within the
 * {@link #invoke(ParameterValue)} method. <code>Operation</code> subclasses are
 * required to overwrite it providing the code to be executed when this
 * operation is called.
 * </p>
 * <p>
 * Before adding an operation to a {@link DefaultService service}, the types of
 * its {@link #getInput() input} and {@link #getOutput() output} parameters must
 * be defined in terms of XML Schema constructs like {@link Element element}s,
 * {@link SimpleType simple type}s and {@link ComplexType complex types}s. A
 * simple operation with no input and a single string message as its only output
 * parameter could look like:
 * 
 * <pre>
 * Operation myOperation = new Operation() {
 * 
 *     public ParameterValue invoke(ParameterValue params)
 *         throws InvocationException, TimeotException {
 *         // business logic goes here
 *         ...
 *     }
 * 
 * };
 * Element message = new Element(&quot;message&quot;,
 *     &quot;http://www.example.org/messageService&quot;, SchemaUtil.TYPE_STRING);
 * myOperation.setOutput(message);
 * </pre>
 * 
 * Additionally, if an operation's invocation can cause expected (checked)
 * exceptional conditions (errors), they must be declared as
 * {@link #addFault(Fault) faults}.
 * </p>
 * <strong>Note:</strong> According to <a href="http://www.w3.org/TR/wsdl">WSDL
 * 1.1 Specification</a>, an operation's {@link #getName() name} is not required
 * to be unique within the scope of its containing port type in order to support
 * overloading. However, when overloading operations, the combination of each
 * one's {@link #getName() name}, {@link #getInputName() input name} and
 * {@link #getOutputName() output name} must be unique in order to avoid name
 * clashes. </p>
 */
public abstract class Operation extends OperationCommons {

	/**
	 * Creates a new operation instance with the given local <code>name</code>
	 * and <code>portType</code>.
	 * 
	 * @param name the name of the operation; see {@link OperationCommons here}
	 *            for a short description of uniqueness requirements regarding
	 *            operation names
	 * @param portType the qualified port type of the operation
	 */
	public Operation(String name, QName portType) {
		super(name, portType);
	}

	/**
	 * Creates a new operation instance without specified name.
	 */
	public Operation() {
		super(null, null);
	}

	/**
	 * Creates a new operation instance with the given <code>name</code>.
	 * 
	 * @param name
	 */
	public Operation(String name) {
		super(name, null);
	}

	/**
	 * Creates a new operation instance with the given <code>name</code> and the
	 * name of the specified service. Namespace default is "http://www.ws4d.or"
	 * can be set once in the DefaultDevice (setDefaultNamespace). There is also
	 * the possibility to set the namespace for every operation.
	 * 
	 * @param name
	 */
	public Operation(String name, String serviceName) {
		super(name, new QName(serviceName, null));
	}

	protected Operation(WSDLOperation operation) {
		super(operation);
	}

	/**
	 * Returns the <code>transmission type</code> of this operation according to
	 * <a href="http://www.w3.org/TR/wsdl">WSDL 1.1 specification</a>. The value
	 * returned is one of {@link WSDLOperation#TYPE_ONE_WAY} or
	 * {@link WSDLOperation#TYPE_REQUEST_RESPONSE}.
	 * 
	 * @return type the transmission type of this operation
	 */
	public final int getType() {
		if (type == WSDLOperation.TYPE_UNKNOWN) {
			/*
			 * this code handles only one-way and request-response operations,
			 * i.e. operations initiated from the client-side
			 */
			if (getOutput() == null && getFaultCount() == 0) {
				type = WSDLOperation.TYPE_ONE_WAY;
			} else {
				type = WSDLOperation.TYPE_REQUEST_RESPONSE;
			}
		}
		return type;
	}

	/**
	 * Returns <code>true</code> if the transmission type of this operation is
	 * {@link WSDLOperation#TYPE_ONE_WAY}. Returns <code>false</code> in any
	 * other case.
	 * 
	 * @return checks whether this is a {@link WSDLOperation#TYPE_ONE_WAY
	 *         one-way} operation
	 */
	public final boolean isOneWay() {
		return getType() == WSDLOperation.TYPE_ONE_WAY;
	}

	/**
	 * Returns <code>true</code> if the transmission type of this operation is
	 * {@link WSDLOperation#TYPE_REQUEST_RESPONSE}. Returns <code>false</code>
	 * in any other case.
	 * 
	 * @return checks whether this is a
	 *         {@link WSDLOperation#TYPE_REQUEST_RESPONSE request-response}
	 *         operation
	 */
	public final boolean isRequestResponse() {
		return getType() == WSDLOperation.TYPE_REQUEST_RESPONSE;
	}

	/**
	 * Invokes the operation.
	 * <p>
	 * The "business" logic of this operation. This method MUST be implemented
	 * by the children classes. This method is the center of the universe!
	 * </p>
	 * <p>
	 * When implementing this method, an easy way to create a suitable container
	 * for the return value for operations with output parameters is provided by
	 * method {@link #createOutputValue()}. Similarly, clients invoking this
	 * operation can create the input parameters to pass to it by means of
	 * {@link #createInputValue()}. If this operation declares any
	 * {@link #getFaults() faults} which may occur during invocation, these are
	 * indicated by throwing an appropriate {@link InvocationException} and
	 * including information about the fault within it. In case a particular
	 * fault needs user-defined parameters to be provided, creating a value
	 * container can be accomplished by {@link #createFaultValue(String)} (given
	 * the fault's name).
	 * </p>
	 * <p>
	 * An example implementation of a simple
	 * {@link WSDLOperation#TYPE_REQUEST_RESPONSE request-response} operation
	 * could look like:
	 * 
	 * <pre>
	 * public ParameterValue invoke(ParameterValue params)
	 *     throws InvocationException, TimeoutException {
	 *     ... // extract argument values from params and call business logic
	 *     ParameterValue result = createOutputValue(); // create result container
	 *     
	 *     ... // fill-in return value(s) within result
	 *     return result;
	 * }
	 * </pre>
	 * 
	 * </p>
	 * <p>
	 * And here is an example of how to indicate that a faulty condition was
	 * discovered during execution by means of a declared fault:
	 * 
	 * <pre>
	 * public ParameterValue invoke(ParameterValue params)
	 *     throws InvocationException, TimeoutException {
	 *     try {
	 *         ... // extract argument values from params and call business logic
	 *     } catch(Exception e) {
	 *         String faultName = ...; // determine type of fault that occurred
	 *         Fault fault = getFault(faultName); // obtain corresponding fault instance
	 *         
	 *         // create container for additional fault information
	 *         ParameterValue additionalFaultDetails = fault.createValue();
	 *         ... // fill-in value(s) within additionalFaultDetails
	 *         
	 *         // create exception and wrap fault and detail data within it
	 *         throw new InvocationException(fault, additionalFaultDetails);
	 *     }
	 * }
	 * </pre>
	 * 
	 * </p>
	 * 
	 * @param parameterValue a container providing the input parameters of the
	 *            operation
	 * @return the result of this operation in terms of output parameter value
	 *         which should be delivered to the caller; in case this is just a
	 *         one-way operation, this method must return <code>null</code>;
	 *         returning an empty {@link ParameterValue} still means, that an
	 *         empty response to the caller should be created
	 * @throws InvocationException thrown to indicate that a declared
	 *             {@link #getFaults() fault} occurred during execution of this
	 *             operation's business logic; clients can extract further
	 *             fault-related information from this exception, such as
	 *             user-defined data attached to it
	 * @throws TimeoutException in case invoking an operation of a remote
	 *             service times out
	 */
	public abstract ParameterValue invoke(ParameterValue parameterValue) throws InvocationException, TimeoutException;

}
