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

import org.ws4d.java.DPWSFramework;
import org.ws4d.java.communication.monitor.ResourceLoader;
import org.ws4d.java.constants.SchemaConstants;
import org.ws4d.java.constants.WSAConstants;
import org.ws4d.java.constants.WSAConstants2006;
import org.ws4d.java.constants.WSDLConstants;
import org.ws4d.java.constants.WSEConstants;
import org.ws4d.java.constants.WSPConstants;
import org.ws4d.java.io.xml.ElementParser;
import org.ws4d.java.io.xml.XmlPullParserSupport;
import org.ws4d.java.schema.Schema;
import org.ws4d.java.schema.SchemaException;
import org.ws4d.java.types.CustomAttributeParser;
import org.ws4d.java.types.QName;
import org.ws4d.java.types.URI;
import org.ws4d.java.util.Log;
import org.ws4d.java.util.StringUtil;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

/**
 *
 */
public class DefaultWSDLParser implements WSDLParser {

	private static final int	INPUT	= 1;

	private static final int	OUTPUT	= 2;

	private static final int	FAULT	= 3;

	private static String getNameAttribute(ElementParser parser) {
		int attributeCount = parser.getAttributeCount();
		for (int i = 0; i < attributeCount; i++) {
			String attributeNamespace = parser.getAttributeNamespace(i);
			String attributeName = parser.getAttributeName(i);
			if ("".equals(attributeNamespace)) {
				if (WSDLConstants.WSDL_ATTRIB_NAME.equals(attributeName)) {
					return parser.getAttributeValue(i);
				}
			}
		}
		return null;
	}

	/**
	 * 
	 */
	public DefaultWSDLParser() {
		super();
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.wsdl.WSDLParser#parse(java.io.InputStream,
	 * org.ws4d.java.types.URI, java.lang.String, boolean)
	 */
	public WSDL parse(InputStream in, URI fromUri, String targetNamespace, boolean loadReferencedFiles) throws XmlPullParserException, IOException {
		return parse0(new WSDL(), in, fromUri, targetNamespace, loadReferencedFiles);
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.wsdl.WSDLParser#parse(org.xmlpull.v1.XmlPullParser,
	 * org.ws4d.java.types.URI, java.lang.String, boolean)
	 */
	public WSDL parse(XmlPullParser parser, URI fromUri, String targetNamespace, boolean loadReferencedFiles) throws XmlPullParserException, IOException {
		return parse0(new WSDL(), parser, fromUri, targetNamespace, loadReferencedFiles);
	}

	public WSDLMessage parseMessage(ElementParser parser, String targetNamespace) throws XmlPullParserException, IOException {
		WSDLMessage message = new WSDLMessage();
		int attributeCount = parser.getAttributeCount();
		for (int i = 0; i < attributeCount; i++) {
			String attributeNamespace = parser.getAttributeNamespace(i);
			String attributeName = parser.getAttributeName(i);
			if ("".equals(attributeNamespace)) {
				if (WSDLConstants.WSDL_ATTRIB_NAME.equals(attributeName)) {
					message.setName(new QName(parser.getAttributeValue(i), targetNamespace));
				}
			}
		}
		while (parser.nextTag() != XmlPullParser.END_TAG) {
			String namespace = parser.getNamespace();
			String name = parser.getName();
			if (WSDLConstants.WSDL_NAMESPACE_NAME.equals(namespace)) {
				if (WSDLConstants.WSDL_ELEM_PART.equals(name)) {
					message.addPart(parseMessagePart(parser));
				} else if (WSDLConstants.WSDL_ELEM_DOCUMENTATION.equals(name)) {
					// eat everything below
					new ElementParser(parser).consume();
				}
			}
		}
		return message;
	}

	public WSDLMessagePart parseMessagePart(ElementParser parser) throws XmlPullParserException, IOException {
		WSDLMessagePart part = new WSDLMessagePart();
		int attributeCount = parser.getAttributeCount();
		for (int i = 0; i < attributeCount; i++) {
			String attributeNamespace = parser.getAttributeNamespace(i);
			String attributeName = parser.getAttributeName(i);
			if ("".equals(attributeNamespace)) {
				if (WSDLConstants.WSDL_ATTRIB_NAME.equals(attributeName)) {
					part.setName(parser.getAttributeValue(i));
				} else if (WSDLConstants.WSDL_ATTRIB_ELEMENT.equals(attributeName)) {
					part.setElementName(parser.getAttributeValueAsQName(i));
				} else if (WSDLConstants.WSDL_ATTRIB_TYPE.equals(attributeName)) {
					part.setTypeName(parser.getAttributeValueAsQName(i));
				}
			}
		}
		parser.nextTag(); // go to closing tag
		return part;
	}

	public WSDLPortType parsePortType(ElementParser parser, String targetNamespace) throws XmlPullParserException, IOException {
		WSDLPortType portType = new WSDLPortType();
		int attributeCount = parser.getAttributeCount();
		for (int i = 0; i < attributeCount; i++) {
			String attributeNamespace = parser.getAttributeNamespace(i);
			String attributeName = parser.getAttributeName(i);
			String attributeValue = parser.getAttributeValue(i);
			if ("".equals(attributeNamespace)) {
				if (WSDLConstants.WSDL_ATTRIB_NAME.equals(attributeName)) {
					portType.setName(new QName(attributeValue, targetNamespace));
				} else {
					CustomAttributeParser cap = WSDL.getAttributeParserForNamespace(attributeNamespace);
					portType.setAttribute(new QName(attributeName, attributeNamespace), cap.parse(parser, i));
				}
			} else if (WSEConstants.WSE_NAMESPACE_NAME.equals(attributeNamespace)) {
				if (WSEConstants.WSE_ATTR_EVENTSOURCE.equals(attributeName)) {
					portType.setEventSource(StringUtil.equalsIgnoreCase("true", attributeValue));
				} else {
					CustomAttributeParser cap = WSDL.getAttributeParserForNamespace(attributeNamespace);
					portType.setAttribute(new QName(attributeName, attributeNamespace), cap.parse(parser, i));
				}
			} else {
				CustomAttributeParser cap = WSDL.getAttributeParserForNamespace(attributeNamespace);
				portType.setAttribute(new QName(attributeName, attributeNamespace), cap.parse(parser, i));
			}
		}
		while (parser.nextTag() != XmlPullParser.END_TAG) {
			String namespace = parser.getNamespace();
			String name = parser.getName();
			if (WSDLConstants.WSDL_NAMESPACE_NAME.equals(namespace)) {
				if (WSDLConstants.WSDL_ELEM_OPERATION.equals(name)) {
					portType.addOperation(parseOperation(parser));
				} else if (WSDLConstants.WSDL_ELEM_DOCUMENTATION.equals(name)) {
					// eat everything below
					new ElementParser(parser).consume();
				}
			}
		}
		return portType;
	}

	public WSDLOperation parseOperation(ElementParser parser) throws XmlPullParserException, IOException {
		WSDLOperation operation = new WSDLOperation();
		int attributeCount = parser.getAttributeCount();
		for (int i = 0; i < attributeCount; i++) {
			String attributeNamespace = parser.getAttributeNamespace(i);
			String attributeName = parser.getAttributeName(i);
			String attributeValue = parser.getAttributeValue(i);
			if ("".equals(attributeNamespace)) {
				if (WSDLConstants.WSDL_ATTRIB_NAME.equals(attributeName)) {
					operation.setName(attributeValue);
				} else {
					CustomAttributeParser cap = WSDL.getAttributeParserForNamespace(attributeNamespace);
					operation.setAttribute(new QName(attributeName, attributeNamespace), cap.parse(parser, i));
				}
			} else {
				CustomAttributeParser cap = WSDL.getAttributeParserForNamespace(attributeNamespace);
				operation.setAttribute(new QName(attributeName, attributeNamespace), cap.parse(parser, i));
			}
		}

		while (parser.nextTag() != XmlPullParser.END_TAG) {
			String namespace = parser.getNamespace();
			String name = parser.getName();
			if (WSDLConstants.WSDL_NAMESPACE_NAME.equals(namespace)) {
				if (WSDLConstants.WSDL_ELEM_INPUT.equals(name)) {
					operation.setInput(parseIOType(parser));
				} else if (WSDLConstants.WSDL_ELEM_OUTPUT.equals(name)) {
					operation.setOutput(parseIOType(parser));
				} else if (WSDLConstants.WSDL_ELEM_FAULT.equals(name)) {
					operation.addFault(parseIOType(parser));
				} else if (WSDLConstants.WSDL_ELEM_DOCUMENTATION.equals(name)) {
					// eat everything below
					new ElementParser(parser).consume();
				}
			}
		}
		return operation;
	}

	public IOType parseIOType(ElementParser parser) throws XmlPullParserException, IOException {
		IOType ioType = new IOType();
		int attributeCount = parser.getAttributeCount();
		for (int i = 0; i < attributeCount; i++) {
			String attributeNamespace = parser.getAttributeNamespace(i);
			String attributeName = parser.getAttributeName(i);
			String attributeValue = parser.getAttributeValue(i);
			if ("".equals(attributeNamespace)) {
				if (WSDLConstants.WSDL_ATTRIB_NAME.equals(attributeName)) {
					ioType.setName(attributeValue);
				} else if (WSDLConstants.WSDL_ATTRIB_MESSAGE.equals(attributeName)) {
					ioType.setMessage(parser.getAttributeValueAsQName(i));
				} else {
					CustomAttributeParser cap = WSDL.getAttributeParserForNamespace(attributeNamespace);
					ioType.setAttribute(new QName(attributeName, attributeNamespace), cap.parse(parser, i));
				}
			} else if (WSAConstants.WSA_NAMESPACE_NAME.equals(attributeNamespace) || WSAConstants2006.WSA_NAMESPACE_NAME.equals(attributeNamespace)) {
				if (WSAConstants.WSA_ELEM_ACTION.equals(attributeName)) {
					ioType.setAction(attributeValue);
				} else {
					CustomAttributeParser cap = WSDL.getAttributeParserForNamespace(attributeNamespace);
					ioType.setAttribute(new QName(attributeName, attributeNamespace), cap.parse(parser, i));
				}
			} else {
				CustomAttributeParser cap = WSDL.getAttributeParserForNamespace(attributeNamespace);
				ioType.setAttribute(new QName(attributeName, attributeNamespace), cap.parse(parser, i));
			}
		}

		while (parser.nextTag() != XmlPullParser.END_TAG) {
			String namespace = parser.getNamespace();
			String name = parser.getName();
			if (WSDLConstants.WSDL_NAMESPACE_NAME.equals(namespace)) {
				if (WSDLConstants.WSDL_ELEM_DOCUMENTATION.equals(name)) {
					// eat everything below
					new ElementParser(parser).consume();
				}
			}
		}
		return ioType;
	}

	public WSDLBinding parseBinding(ElementParser parser, String targetNamespace) throws XmlPullParserException, IOException, UnsupportedBindingException {
		QName bindingName = null;
		QName bindingType = null;
		int attributeCount = parser.getAttributeCount();
		for (int i = 0; i < attributeCount; i++) {
			String attributeNamespace = parser.getAttributeNamespace(i);
			String attributeName = parser.getAttributeName(i);
			if ("".equals(attributeNamespace)) {
				if (WSDLConstants.WSDL_ATTRIB_NAME.equals(attributeName)) {
					bindingName = new QName(parser.getAttributeValue(i), targetNamespace);
				} else if (WSDLConstants.WSDL_ATTRIB_TYPE.equals(attributeName)) {
					bindingType = parser.getAttributeValueAsQName(i);
				}
			}
		}
		parser.nextTag(); // go to first child of wsdl:binding element
		String namespace = parser.getNamespace();
		String name = parser.getName();

		// extract wsdl:documentation!
		if (WSDLConstants.WSDL_NAMESPACE_NAME.equals(namespace)) {
			if (WSDLConstants.WSDL_ELEM_DOCUMENTATION.equals(name)) {
				// eat everything below
				new ElementParser(parser).consume();
				parser.nextTag(); // go to next child of wsdl:binding element
				namespace = parser.getNamespace();
			}
		}

		/*
		 * get concrete binding subclass according to namespace and forward
		 * extension element processing to it
		 */
		WSDLBindingBuilder builder = WSDLBinding.getBuilder(namespace);
		if (builder == null) {
			throw new UnsupportedBindingException(namespace);
		}
		// narrow scope of potentially untrusted code
		ElementParser childParser = new ElementParser(parser);
		builder.parseBindingExtension(bindingName, bindingType, childParser);
		// XXX there could be more than one extensibility elements here...
		childParser.consume();
		// run through operations
		while (parser.nextTag() != XmlPullParser.END_TAG) {
			namespace = parser.getNamespace();
			name = parser.getName();
			if (WSDLConstants.WSDL_NAMESPACE_NAME.equals(namespace)) {
				if (WSDLConstants.WSDL_ELEM_OPERATION.equals(name)) {
					String operationName = getNameAttribute(parser);
					parseBindingOperation(operationName, builder, parser);
				}
			}
		}
		return builder.getBinding();
	}

	public void parseBindingOperation(String operationName, WSDLBindingBuilder builder, ElementParser parser) throws XmlPullParserException, IOException, UnsupportedBindingException {
		// go to either operation-specific binding extension or first IO type
		int event = parser.nextTag();

		String namespace = parser.getNamespace();
		// extract wsdl:documentation!
		if (WSDLConstants.WSDL_NAMESPACE_NAME.equals(namespace)) {
			if (WSDLConstants.WSDL_ELEM_DOCUMENTATION.equals(parser.getName())) {
				// eat everything below
				new ElementParser(parser).consume();
				parser.nextTag(); // go to next child of IOType element
				namespace = parser.getNamespace();
			}
		}

		if (namespace.equals(builder.getNamespace())) {
			// this is an extension
			ElementParser childParser = new ElementParser(parser);
			builder.parseOperationExtension(operationName, childParser);
			// XXX there could be more than one extensibility elements here...
			childParser.consume();
			event = parser.nextTag(); // go to first IO type
		}
		// run through IO types
		while (event != XmlPullParser.END_TAG) {
			namespace = parser.getNamespace();
			String name = parser.getName();
			if (WSDLConstants.WSDL_NAMESPACE_NAME.equals(namespace)) {
				if (WSDLConstants.WSDL_ELEM_INPUT.equals(name)) {
					String ioTypeName = getNameAttribute(parser);
					parseBindingIOType(ioTypeName, builder, parser, INPUT);
				} else if (WSDLConstants.WSDL_ELEM_OUTPUT.equals(name)) {
					String ioName = getNameAttribute(parser);
					parseBindingIOType(ioName, builder, parser, OUTPUT);
				} else if (WSDLConstants.WSDL_ELEM_FAULT.equals(name)) {
					String ioName = getNameAttribute(parser);
					parseBindingIOType(ioName, builder, parser, FAULT);
				}
			}
			event = parser.nextTag();
		}
	}

	public void parseBindingIOType(String ioTypeName, WSDLBindingBuilder builder, ElementParser parser, int ioType) throws XmlPullParserException, IOException, UnsupportedBindingException {
		// go to first child of IOType (maybe the specific binding extension)
		parser.nextTag();

		// extract wsdl:documentation!
		if (WSDLConstants.WSDL_NAMESPACE_NAME.equals(parser.getNamespace())) {
			if (WSDLConstants.WSDL_ELEM_DOCUMENTATION.equals(parser.getName())) {
				// eat everything below
				new ElementParser(parser).consume();
				parser.nextTag(); // go to next child of IOType element
			}
		}

		ElementParser childParser = new ElementParser(parser);
		switch (ioType) {
			case (INPUT): {
				builder.parseInputExtension(ioTypeName, childParser);
				// XXX there could be more than one extensibility elements
				// here...
				break;
			}
			case (OUTPUT): {
				builder.parseOutputExtension(ioTypeName, childParser);
				// XXX there could be more than one extensibility elements
				// here...
				break;
			}
			case (FAULT): {
				builder.parseFaultExtension(ioTypeName, childParser);
				// XXX there could be more than one extensibility elements
				// here...
				break;
			}
		}
		childParser.consume();
		parser.nextTag(); // go to closing IO type tag
	}

	public WSDLService parseService(ElementParser parser, String targetNamespace) throws XmlPullParserException, IOException, UnsupportedBindingException {
		WSDLService service = new WSDLService();
		int attributeCount = parser.getAttributeCount();
		for (int i = 0; i < attributeCount; i++) {
			String attributeNamespace = parser.getAttributeNamespace(i);
			String attributeName = parser.getAttributeName(i);
			if ("".equals(attributeNamespace)) {
				if (WSDLConstants.WSDL_ATTRIB_NAME.equals(attributeName)) {
					service.setName(new QName(parser.getAttributeValue(i), targetNamespace));
				}
			}
		}
		while (parser.nextTag() != XmlPullParser.END_TAG) {
			String namespace = parser.getNamespace();
			String name = parser.getName();
			if (WSDLConstants.WSDL_NAMESPACE_NAME.equals(namespace)) {
				if (WSDLConstants.WSDL_ELEM_PORT.equals(name)) {
					service.addPort(parsePort(parser));
				} else if (WSDLConstants.WSDL_ELEM_DOCUMENTATION.equals(name)) {
					// eat everything below
					new ElementParser(parser).consume();
				}
			}
		}
		return service;
	}

	public WSDLPort parsePort(ElementParser parser) throws XmlPullParserException, IOException, UnsupportedBindingException {
		String portName = null;
		QName bindingName = null;
		int attributeCount = parser.getAttributeCount();
		for (int i = 0; i < attributeCount; i++) {
			String attributeNamespace = parser.getAttributeNamespace(i);
			String attributeName = parser.getAttributeName(i);
			if ("".equals(attributeNamespace)) {
				if (WSDLConstants.WSDL_ATTRIB_NAME.equals(attributeName)) {
					portName = parser.getAttributeValue(i);
				} else if (WSDLConstants.WSDL_ATTRIB_BINDING.equals(attributeName)) {
					bindingName = parser.getAttributeValueAsQName(i);
				}
			}
		}
		parser.nextTag(); // go to first child of wsdl:port element

		String namespace = parser.getNamespace();
		String name = parser.getName();

		// extract wsdl:documentation!
		if (WSDLConstants.WSDL_NAMESPACE_NAME.equals(namespace)) {
			if (WSDLConstants.WSDL_ELEM_DOCUMENTATION.equals(name)) {
				// eat everything below
				new ElementParser(parser).consume();
				parser.nextTag(); // go to next child of wsdl:port element
				namespace = parser.getNamespace();
			}
		}

		/*
		 * get concrete binding builder according to namespace and forward
		 * extension element processing to it
		 */
		WSDLBindingBuilder builder = WSDLBinding.getBuilder(namespace);
		if (builder == null) {
			throw new UnsupportedBindingException(namespace);
		}
		// narrow scope of potentially untrusted code
		ElementParser childParser = new ElementParser(parser);
		WSDLPort port = builder.parsePortExtension(portName, bindingName, childParser);
		childParser.consume();
		parser.nextTag(); // go to closing wsdl:port tag
		return port;
	}

	private WSDL parse0(WSDL wsdl, InputStream in, URI fromUri, String targetNamespace, boolean loadReferencedFiles) throws XmlPullParserException, IOException {
		XmlPullParser parser = XmlPullParserSupport.getFactory().newPullParser();
		parser.setInput(in, null);

		parser.nextTag(); // go to WSDL definitions

		return parse0(wsdl, parser, fromUri, targetNamespace, loadReferencedFiles);
	}

	private WSDL parse0(WSDL wsdl, XmlPullParser parser, URI fromUri, String targetNamespace, boolean loadReferencedFiles) throws XmlPullParserException, IOException {
		wsdl.storeDefaultNamespaces(parser);
		String namespace = parser.getNamespace();
		String name = parser.getName();
		if (WSDLConstants.WSDL_NAMESPACE_NAME.equals(namespace)) {
			if (WSDLConstants.WSDL_ELEM_DEFINITIONS.equals(name)) {
				int attributeCount = parser.getAttributeCount();
				for (int i = 0; i < attributeCount; i++) {
					String attributeNamespace = parser.getAttributeNamespace(i);
					String attributeName = parser.getAttributeName(i);
					if ("".equals(attributeNamespace)) {
						attributeNamespace = parser.getNamespace();
					}
					if (WSDLConstants.WSDL_NAMESPACE_NAME.equals(attributeNamespace)) {
						if (WSDLConstants.WSDL_ATTRIB_NAME.equals(attributeName)) {
							if (targetNamespace == null) {
								wsdl.setName(parser.getAttributeValue(i));
							}
						} else if (WSDLConstants.WSDL_ATTRIB_TARGETNAMESPACE.equals(attributeName)) {
							// propagate included/imported target namespace
							String containedTargetNamespace = parser.getAttributeValue(i);
							if (targetNamespace == null) {
								targetNamespace = containedTargetNamespace;
								wsdl.setTargetNamespace(targetNamespace);
							} else if (!targetNamespace.equals(containedTargetNamespace)) {
								throw new XmlPullParserException("declared namespace " + containedTargetNamespace + " doesn't match expected namespace " + targetNamespace);
							}
						}
					}
				}
				handleDefinitions(wsdl, new ElementParser(parser), fromUri, targetNamespace, loadReferencedFiles);
			}
		} else if (SchemaConstants.XMLSCHEMA_NAMESPACE.equals(namespace)) {
			if (SchemaConstants.SCHEMA_SCHEMA.equals(name)) {
				handleSchema(wsdl, new ElementParser(parser), fromUri, loadReferencedFiles);
			}
		}
		/*
		 * as of Basic Profile 1.1, Section 4, R2001, XML schema definitions may
		 * only be imported by means of a corresponding XML schema import
		 * element; WSDL import element may only be used for importing other
		 * WSDL definitions
		 */
		// else if (SchemaConstants.XMLSCHEMA_NAMESPACE.equals(namespace)) {
		// if (SchemaConstants.SCHEMA_SCHEMA.equals(name)) {
		// handleSchema(wsdl, new ElementParser(parser));
		// }
		// }
		return wsdl;
	}

	private void handleDefinitions(WSDL wsdl, ElementParser parser, URI fromUri, String targetNamespace, boolean loadReferencedFiles) throws XmlPullParserException, IOException {
		// we should be currently at the wsdl:definitions element
		while (parser.nextTag() != XmlPullParser.END_TAG) {
			String namespace = parser.getNamespace();
			String name = parser.getName();
			if (WSDLConstants.WSDL_NAMESPACE_NAME.equals(namespace)) {
				if (WSDLConstants.WSDL_ELEM_IMPORT.equals(name)) {
					handleImport(wsdl, parser, fromUri, loadReferencedFiles);
				} else if (WSDLConstants.WSDL_ELEM_TYPES.equals(name)) {
					handleTypes(wsdl, parser, fromUri, loadReferencedFiles);
				} else if (WSDLConstants.WSDL_ELEM_MESSAGE.equals(name)) {
					wsdl.addMessage(parseMessage(parser, targetNamespace));
				} else if (WSDLConstants.WSDL_ELEM_PORTTYPE.equals(name)) {
					wsdl.addPortType(parsePortType(parser, targetNamespace));
				} else if (WSDLConstants.WSDL_ELEM_BINDING.equals(name)) {
					ElementParser childParser = new ElementParser(parser);
					try {
						wsdl.addBinding(parseBinding(childParser, targetNamespace));
					} catch (UnsupportedBindingException e) {
						Log.warn("Found unsupported binding within WSDL " + fromUri + ": " + e.getMessage());
						if (Log.isDebug()) {
							Log.printStackTrace(e);
						}
						childParser.consume();
					}
				} else if (WSDLConstants.WSDL_ELEM_SERVICE.equals(name)) {
					ElementParser childParser = new ElementParser(parser);
					try {
						wsdl.addService(parseService(childParser, targetNamespace));
					} catch (UnsupportedBindingException e) {
						Log.warn("Found unsupported service within WSDL " + fromUri + ": " + e.getMessage());
						if (Log.isDebug()) {
							Log.printStackTrace(e);
						}
						childParser.consume();
					}
				} else if (WSDLConstants.WSDL_ELEM_DOCUMENTATION.equals(name)) {
					// eat everything below
					new ElementParser(parser).consume();
				}
			} else if (WSPConstants.WSP_NAMESPACE_NAME.equals(namespace) || WSPConstants.WSP_NAMESPACE_NAME_DPWS11.equals(namespace)) {
				if (WSPConstants.WSP_ELEM_POLICY.equals(name)) {
					handlePolicyTags(parser, wsdl);
				}
			} else if (SchemaConstants.XMLSCHEMA_NAMESPACE.equals(namespace)) {
				/*
				 * folks at Microsoft include xs:annotation (with an embedded
				 * xs:documentation element) within their
				 * WSDPrinterService.wsdl, which we should skip
				 */
				new ElementParser(parser).consume();
			} else {
				// Added 2011-01-12 SSch
				handleUnkownTags(parser);
			}
		}
	}

	protected void handlePolicyTags(ElementParser parser, Object parent) throws XmlPullParserException, IOException {
		handleUnkownTags(parser);
	}

	private void handleImport(WSDL wsdl, ElementParser parser, URI fromUri, boolean loadReferencedFile) throws XmlPullParserException, IOException {
		String location = null;
		String namespace = null;
		int attributeCount = parser.getAttributeCount();
		for (int i = 0; i < attributeCount; i++) {
			String attributeNamespace = parser.getAttributeNamespace(i);
			String attributeName = parser.getAttributeName(i);
			if ("".equals(attributeNamespace)) {
				// we don't care about namespace name at this point
				if (WSDLConstants.WSDL_ATTRIB_NAMESPACE.equals(attributeName)) {
					namespace = parser.getAttributeValue(i);
				} else if (WSDLConstants.WSDL_ATTRIB_LOCATION.equals(attributeName)) {
					location = parser.getAttributeValue(i);
				}
			}
		}
		wsdl.addImport(namespace, location);
		if (loadReferencedFile) {
			if (fromUri == null) {
				WSDL newWsdl = WSDLRepository.getInstance().getWSDL(location);
				if (newWsdl != null) {
					wsdl.addLinkedWsdl(newWsdl);
				}
			} else {
				URI newUri = URI.absolutize(fromUri, location);
				ResourceLoader rl = DPWSFramework.getResourceAsStream(newUri);
				InputStream in = rl.getInputStream();
				try {
					// depending on namespace, either embed or link in!
					if (wsdl.getTargetNamespace().equals(namespace)) {
						parse0(wsdl, in, newUri, namespace, true);
					} else {
						WSDL newWsdl = parse(in, newUri, null, true);
						if (newWsdl != null) {
							wsdl.addLinkedWsdl(newWsdl);
						}
					}
				} finally {
					in.close();
				}
			}
		}
		int depth = parser.getDepth();
		while (parser.next() != XmlPullParser.END_TAG || parser.getDepth() > depth) {
			// void
		}
	}

	private void handleTypes(WSDL wsdl, ElementParser parser, URI fromUri, boolean loadReferencedFiles) throws XmlPullParserException, IOException {
		// we should be currently at the wsdl:types element
		int d = parser.getDepth();
		while (parser.nextTag() != XmlPullParser.END_TAG && parser.getDepth() == d + 1) {
			String namespace = parser.getNamespace();
			String name = parser.getName();
			if (SchemaConstants.XMLSCHEMA_NAMESPACE.equals(namespace)) {
				if (SchemaConstants.SCHEMA_SCHEMA.equals(name)) {
					handleSchema(wsdl, new ElementParser(parser), fromUri, loadReferencedFiles);
				}
			} else if (WSDLConstants.WSDL_NAMESPACE_NAME.equals(namespace)) {
				if (WSDLConstants.WSDL_ELEM_DOCUMENTATION.equals(name)) {
					// eat everything below
					new ElementParser(parser).consume();
				}
			}
		}
	}

	private void handleSchema(WSDL wsdl, ElementParser parser, URI fromUri, boolean loadReferencedFiles) throws XmlPullParserException, IOException {
		try {
			String schemaTNS = parser.getAttributeValue(null, SchemaConstants.SCHEMA_TARGETNAMESPACE);
			if (schemaTNS == null) {
				/*
				 * no explicit namespace set? use the targetNamespace from the
				 * WSDL?
				 */
				schemaTNS = wsdl.getTargetNamespace();
			}
			Schema schema = Schema.parse(parser, fromUri, schemaTNS, loadReferencedFiles);
			wsdl.addTypes(schema);
		} catch (SchemaException e) {
			Log.error(e.getMessage());
			Log.printStackTrace(e);
			throw new XmlPullParserException("Unable to parse schema import", parser, e);
		}
	}

	private void handleUnkownTags(ElementParser parser) throws XmlPullParserException, IOException {
		/*
		 * eat every unknown tag, to move the parser to next nice one. ;)
		 */
		int i = parser.getDepth();
		int e = parser.getEventType();
		while (e != XmlPullParser.END_TAG && e != XmlPullParser.END_DOCUMENT && parser.getDepth() >= i) {
			e = parser.nextTag();
			handleUnkownTags(parser);
		}
	}
}
