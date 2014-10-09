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

import org.ws4d.java.schema.Element;
import org.ws4d.java.service.parameter.ParameterValue;
import org.ws4d.java.structures.Iterator;
import org.ws4d.java.types.QName;
import org.ws4d.java.wsdl.WSDLOperation;

/**
 * Common Interface of operations and event sources.
 */
public interface OperationDescription {

	/**
	 * Returns the local name of this operation. See {@link OperationCommons
	 * note on overloading operations}.
	 * 
	 * @return the local name of this operation
	 */
	public String getName();

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
	public int getType();

	/**
	 * Returns the input element of this operation. The input element defines
	 * the data structure for the input parameters of this operation. It can be
	 * used to {@link #createInputValue() create} a suitable
	 * {@link ParameterValue} container for the actual values when sending
	 * messages to the operation.
	 * 
	 * @return the input element
	 */
	public Element getInput();

	/**
	 * Returns the WS-Addressing [action] property for this operation's input
	 * message. Will return <code>null</code> only if this operation's
	 * {@link #getType() transmission type} is
	 * {@link WSDLOperation#TYPE_NOTIFICATION}, i.e. the operation doesn't have
	 * any input. Otherwise, if no input action was explicitly
	 * {@link OperationCommons#setInputAction(String) set}, this method will
	 * generate a default one according to the rules of <a
	 * href="http://www.w3.org/TR/ws-addr-wsdl/">WS-Addressing 1.0 - WSDL
	 * Binding</a>.
	 * 
	 * @return the WS-Addressing [action] property of this operation's input
	 */
	public String getInputAction();

	/**
	 * /** Returns the name of the input of this operation. If this operation
	 * doesn't have any input (i.e. its {@link #getType() transmission type} is
	 * {@link WSDLOperation#TYPE_NOTIFICATION}, <code>null</code> is returned.
	 * Otherwise, if no input name was
	 * {@link OperationCommons#setInputName(String) set} previously, a default
	 * input name is generated according to WSDL 1.1 Specification.
	 * 
	 * @return this operation's input name
	 */
	public String getInputName();

	/**
	 * Returns the output element of this operation. The output element defines
	 * the data structure for the output parameters of this operation. It can be
	 * used to {@link #createOutputValue() create} a suitable
	 * {@link ParameterValue} container for the actual values an operation
	 * returns.
	 * 
	 * @return the output element
	 */
	public Element getOutput();

	/**
	 * Returns the WS-Addressing [action] property for this oepration's output
	 * message. Will return <code>null</code> only if this operation's
	 * {@link #getType() transmission type} is
	 * {@link WSDLOperation#TYPE_ONE_WAY}, i.e. the operation doesn't have any
	 * output. Otherwise, if no output action is explicitly
	 * {@link OperationCommons#setOutputAction(String) set}, this method will
	 * generate a default one according to the rules of <a
	 * href="http://www.w3.org/TR/ws-addr-wsdl/">WS-Addressing 1.0 - WSDL
	 * Binding</a>.
	 * 
	 * @return the WS-Addressing [action] property of this operation's output
	 */
	public String getOutputAction();

	/**
	 * Returns the name of the output of this operation. If this operation
	 * doesn't have any output (i.e. its {@link #getType() transmission type} is
	 * {@link WSDLOperation#TYPE_ONE_WAY}, <code>null</code> is returned.
	 * Otherwise, if no output name has been previously
	 * {@link OperationCommons#setOutputName(String) set}, a default output name
	 * is generated according to WSDL 1.1 Specification.
	 * 
	 * @return this operation's output name
	 */
	public String getOutputName();

	/**
	 * Returns this operation's fault with the given <code>faultName</code>,
	 * which is considered to be unique within the operation's scope.
	 * 
	 * @param faultName the requested fault's name
	 * @return the fault with the given name or <code>null</code> if there is no
	 *         fault with that name within this operation
	 */
	public Fault getFault(String faultName);

	/**
	 * Returns the number of declared faults for this operation.
	 * 
	 * @return the number of declared faults
	 */
	public int getFaultCount();

	/**
	 * Returns an {@link Iterator} over all faults declared for this operation.
	 * 
	 * @return an iterator over this operation's faults
	 * @see OperationCommons#addFault(Fault)
	 */
	public Iterator getFaults();

	/**
	 * Returns the port type to which this operation belongs.
	 * 
	 * @return this operation's port type
	 */
	public QName getPortType();

	/**
	 * Returns the service to which this operation is associated. May return
	 * <code>null</code>, if this operation has not yet been added to a
	 * particular service.
	 * 
	 * @return the service this operation belongs to
	 */
	public Service getService();

	/**
	 * This is a shorthand method for creating a {@link ParameterValue parameter
	 * value} container for this operation's input. It is semantically
	 * equivalent to <code>ParameterValue.createElementValue(getInput());</code>
	 * .
	 * 
	 * @return a parameter container suitable for this operation's input
	 * @see ParameterValue#createElementValue(Element)
	 */
	public ParameterValue createInputValue();

	/**
	 * This is a shorthand method for creating a {@link ParameterValue parameter
	 * value} container for this operation's output. It is semantically
	 * equivalent to
	 * <code>ParameterValue.createElementValue(getOutput());</code>.
	 * 
	 * @return a parameter container suitable for this operation's output
	 * @see #getOutput()
	 * @see ParameterValue#createElementValue(Element)
	 */
	public ParameterValue createOutputValue();

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
	public ParameterValue createFaultValue(String faultName);

}
