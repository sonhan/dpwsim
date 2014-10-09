/*******************************************************************************
 * Copyright (c) 2009 MATERNA Information & Communications. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html. For further
 * project-related information visit http://www.ws4d.org. The most recent
 * version of the JMEDS framework can be obtained from
 * http://sourceforge.net/projects/ws4d-javame.
 ******************************************************************************/
package org.ws4d.java.wsdl.soap12;

import org.ws4d.java.constants.WSDLConstants;
import org.ws4d.java.io.xml.ElementParser;
import org.ws4d.java.types.QName;
import org.ws4d.java.types.URI;
import org.ws4d.java.wsdl.UnsupportedBindingException;
import org.ws4d.java.wsdl.WSDLBinding;
import org.ws4d.java.wsdl.WSDLBindingBuilder;
import org.ws4d.java.wsdl.WSDLPort;

/**
 * 
 */
public class SOAP12DocumentLiteralHTTPBindingBuilder implements WSDLBindingBuilder {

	private SOAP12DocumentLiteralHTTPBinding	binding;

	/**
	 * 
	 */
	public SOAP12DocumentLiteralHTTPBindingBuilder() {
		super();
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.wsdl.WSDLBindingBuilder#getNamespace()
	 */
	public String getNamespace() {
		return WSDLConstants.SOAP12_BINDING_NAMESPACE_NAME;
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.wsdl.WSDLBindingBuilder#getBinding()
	 */
	public WSDLBinding getBinding() {
		return binding;
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.wsdl.WSDLBindingBuilder#parseBindingExtension(org.ws4d
	 * .java.data.QName, org.ws4d.java.data.QName,
	 * org.ws4d.java.communication.protocol.soap.generator.ElementParser)
	 */
	public void parseBindingExtension(QName bindingName, QName portType, ElementParser parser) throws UnsupportedBindingException {
		String style = SOAP12DocumentLiteralHTTPBinding.DOCUMENT_STYLE;
		String transport = null;
		int attributeCount = parser.getAttributeCount();
		for (int i = 0; i < attributeCount; i++) {
			String attributeNamespace = parser.getAttributeNamespace(i);
			String attributeName = parser.getAttributeName(i);
			if ("".equals(attributeNamespace)) {
				if (WSDLConstants.WSDL_ATTRIB_STYLE.equals(attributeName)) {
					style = parser.getAttributeValue(i);
				} else if (WSDLConstants.WSDL_ATTRIB_TRANSPORT.equals(attributeName)) {
					transport = parser.getAttributeValue(i);
				}
			}
		}
		if (!SOAP12DocumentLiteralHTTPBinding.DOCUMENT_STYLE.equals(style)) {
			throw new UnsupportedBindingException("unsupported style: " + style);
		}
		if (!SOAP12DocumentLiteralHTTPBinding.HTTP_TRANSPORT.equals(transport)) {
			throw new UnsupportedBindingException("unsupported transport: " + transport);
		}
		binding = new SOAP12DocumentLiteralHTTPBinding(bindingName, portType);
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.wsdl.WSDLBindingBuilder#parseOperationExtension(java
	 * .lang.String, org.ws4d.java.io.xml.ElementParser)
	 */
	public void parseOperationExtension(String operationName, ElementParser parser) throws UnsupportedBindingException {
		if (binding == null) {
			throw new UnsupportedBindingException("no acceptable binding extension processed");
		}
		String style = SOAP12DocumentLiteralHTTPBinding.DOCUMENT_STYLE;
		String soapAction = null;
		int attributeCount = parser.getAttributeCount();
		for (int i = 0; i < attributeCount; i++) {
			String attributeNamespace = parser.getAttributeNamespace(i);
			String attributeName = parser.getAttributeName(i);
			if ("".equals(attributeNamespace)) {
				if (WSDLConstants.WSDL_ATTRIB_STYLE.equals(attributeName)) {
					style = parser.getAttributeValue(i);
				} else if (WSDLConstants.WSDL_ATTRIB_SOAP_ACTION.equals(attributeName)) {
					soapAction = parser.getAttributeValue(i);
				}
			}
		}
		if (!SOAP12DocumentLiteralHTTPBinding.DOCUMENT_STYLE.equals(style)) {
			binding = null;
			throw new UnsupportedBindingException("unsupported style: " + style);
		}
		if (soapAction != null) {
			binding.setSoapAction(operationName, soapAction);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.wsdl.WSDLBindingBuilder#parseInputExtension(java.lang
	 * .String, org.ws4d.java.io.xml.ElementParser)
	 */
	public void parseInputExtension(String inputName, ElementParser parser) throws UnsupportedBindingException {
		parseIoTypeExtension(parser);
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.wsdl.WSDLBindingBuilder#parseOutputExtension(java.
	 * lang.String, org.ws4d.java.io.xml.ElementParser)
	 */
	public void parseOutputExtension(String outputName, ElementParser parser) throws UnsupportedBindingException {
		parseIoTypeExtension(parser);

	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.wsdl.WSDLBindingBuilder#parseFaultExtension(java.lang
	 * .String, org.ws4d.java.io.xml.ElementParser)
	 */
	public void parseFaultExtension(String faultName, ElementParser parser) throws UnsupportedBindingException {
		parseIoTypeExtension(parser);

	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.wsdl.WSDLBindingBuilder#parsePortExtension(org.ws4d
	 * .java.data.QName, org.ws4d.java.data.QName,
	 * org.ws4d.java.io.xml.ElementParser)
	 */
	public WSDLPort parsePortExtension(String portName, QName bindingName, ElementParser parser) {
		SOAP12DocumentLiteralHTTPPort port = new SOAP12DocumentLiteralHTTPPort(portName, bindingName);
		int attributeCount = parser.getAttributeCount();
		for (int i = 0; i < attributeCount; i++) {
			String attributeNamespace = parser.getAttributeNamespace(i);
			String attributeName = parser.getAttributeName(i);
			if ("".equals(attributeNamespace)) {
				if (WSDLConstants.WSDL_ATTRIB_LOCATION.equals(attributeName)) {
					port.setLocation(new URI(parser.getAttributeValue(i)));
				}
			}
		}
		return port;
	}

	private void parseIoTypeExtension(ElementParser parser) throws UnsupportedBindingException {
		if (binding == null) {
			throw new UnsupportedBindingException("no acceptable binding extension or operation extension processed");
		}
		String use = null;
		int attributeCount = parser.getAttributeCount();
		for (int i = 0; i < attributeCount; i++) {
			String attributeNamespace = parser.getAttributeNamespace(i);
			String attributeName = parser.getAttributeName(i);
			if ("".equals(attributeNamespace)) {
				if (WSDLConstants.WSDL_ATTRIB_USE.equals(attributeName)) {
					use = parser.getAttributeValue(i);
				}
			}
		}
		if (!SOAP12DocumentLiteralHTTPBinding.LITERAL_USE.equals(use)) {
			binding = null;
			throw new UnsupportedBindingException("unsupported use: " + use);
		}
	}

}
