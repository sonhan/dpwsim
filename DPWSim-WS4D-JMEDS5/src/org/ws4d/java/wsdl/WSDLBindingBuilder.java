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

import org.ws4d.java.io.xml.ElementParser;
import org.ws4d.java.types.QName;

/**
 * 
 */
public interface WSDLBindingBuilder {

	/**
	 * Returns the namespace URI, which uniquely characterizes the specific
	 * binding for which this binding builder is responsible.
	 * 
	 * @return the binding-specific URI for this binding builder
	 */
	public String getNamespace();

	/**
	 * Reads an extension element to a binding WSDL definition from the supplied
	 * element parser <code>parser</code>.
	 * 
	 * @param bindingName the fully qualified name of the surrounding binding
	 *            element
	 * @param portType the fully qualified name of the port type to which the
	 *            surrounding binding refers
	 * @param parser the element parser from which to obtain the binding
	 *            extension element
	 * @throws UnsupportedBindingException in case a binding extension is found
	 *             during parsing which doesn't conform to the expectations of
	 *             this binding builder instance
	 */
	public void parseBindingExtension(QName bindingName, QName portType, ElementParser parser) throws UnsupportedBindingException;

	/**
	 * Reads an extension element to an operation element within a WSDL binding
	 * definition from the supplied element parser <code>parser</code>.
	 * 
	 * @param operationName the local name of the surrounding operation element
	 * @param parser the element parser from which to obtain the
	 *            operation-related binding extension element
	 * @throws UnsupportedBindingException in case a operation-related binding
	 *             extension is found during parsing which doesn't conform to
	 *             the expectations of this binding builder instance
	 */
	public void parseOperationExtension(String operationName, ElementParser parser) throws UnsupportedBindingException;

	/**
	 * Reads an extension element to an input element within a WSDL binding
	 * definition from the supplied element parser <code>parser</code>.
	 * 
	 * @param inputName the local name of the surrounding input element
	 * @param parser the element parser from which to obtain the input-related
	 *            binding extension element
	 * @throws UnsupportedBindingException in case an input-related binding
	 *             extension is found during parsing which doesn't conform to
	 *             the expectations of this binding builder instance
	 */
	public void parseInputExtension(String inputName, ElementParser parser) throws UnsupportedBindingException;

	/**
	 * Reads an extension element to an output element within a WSDL binding
	 * definition from the supplied element parser <code>parser</code>.
	 * 
	 * @param outputName the local name of the surrounding output element
	 * @param parser the element parser from which to obtain the output-related
	 *            binding extension element
	 * @throws UnsupportedBindingException in case an output-related binding
	 *             extension is found during parsing which doesn't conform to
	 *             the expectations of this binding builder instance
	 */
	public void parseOutputExtension(String outputName, ElementParser parser) throws UnsupportedBindingException;

	/**
	 * Reads an extension element to a fault element within a WSDL binding
	 * definition from the supplied element parser <code>parser</code>.
	 * 
	 * @param faultName the local name of the surrounding fault element
	 * @param parser the element parser from which to obtain the fault-related
	 *            binding extension element
	 * @throws UnsupportedBindingException in case a fault-related binding
	 *             extension is found during parsing which doesn't conform to
	 *             the expectations of this binding builder instance
	 */
	public void parseFaultExtension(String faultName, ElementParser parser) throws UnsupportedBindingException;

	/**
	 * Returns the resulting binding after processing all binding-specific
	 * information within a WSDL binding definition. This method should be
	 * called <em>after</em> processing all extensibility elements of the WSDL
	 * binding definition by means of the following methods
	 * {@link WSDLBindingBuilder#parseBindingExtension(QName, QName, ElementParser)}
	 * ,
	 * {@link WSDLBindingBuilder#parseOperationExtension(String, ElementParser)}
	 * , {@link WSDLBindingBuilder#parseInputExtension(String, ElementParser)},
	 * {@link WSDLBindingBuilder#parseOutputExtension(String, ElementParser)}
	 * and {@link WSDLBindingBuilder#parseFaultExtension(String, ElementParser)}
	 * . Otherwise, it should return <code>null</code>.
	 * 
	 * @return the resulting binding or <code>null</code>, if still no binding
	 *         was processed
	 */
	public WSDLBinding getBinding();

	/**
	 * Parses an extension section within a WSDL port definition and returns a
	 * {@link WSDLPort} instance representing the port together with its
	 * extension.
	 * 
	 * @param portName the name of the port
	 * @param bindingName the name of the binding to which the port refers
	 * @param childParser the element parser from which to obtain the extension
	 *            information
	 * @return a concrete WSDL port instance containing any extensibility
	 *         information found during parsing
	 */
	public WSDLPort parsePortExtension(String portName, QName bindingName, ElementParser childParser);

}
