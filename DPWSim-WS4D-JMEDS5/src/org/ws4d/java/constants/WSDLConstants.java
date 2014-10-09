/*******************************************************************************
 * Copyright (c) 2009 MATERNA Information & Communications. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html. For further
 * project-related information visit http://www.ws4d.org. The most recent
 * version of the JMEDS framework can be obtained from
 * http://sourceforge.net/projects/ws4d-javame.
 ******************************************************************************/

package org.ws4d.java.constants;

/**
 * Constants used by WSDL.
 * 
 * @author mspies
 */
public interface WSDLConstants {

	/** "http://schemas.xmlsoap.org/wsdl/". */
	public static final String	WSDL_NAMESPACE_NAME				= "http://schemas.xmlsoap.org/wsdl/";

	/** The default prefix for the wsdl namespace name. */
	public static final String	WSDL_NAMESPACE_PREFIX			= "wsdl";

	/** Namespace name of WSDL - SOAP 1.2 Binding */
	public static final String	SOAP12_BINDING_NAMESPACE_NAME	= "http://schemas.xmlsoap.org/wsdl/soap12/";

	/** Standard namespace prefix of WSDL - SOAP 1.2 Binding */
	public static final String	SOAP12_BINDING_PREFIX			= "wsoap12";

	/** "definitions". */
	public static final String	WSDL_ELEM_DEFINITIONS			= "definitions";

	/** "documentation". */
	public static final String	WSDL_ELEM_DOCUMENTATION			= "documentation";

	/** "targetNamespace". */
	public static final String	WSDL_ATTRIB_TARGETNAMESPACE		= "targetNamespace";

	/** "types". */
	public static final String	WSDL_ELEM_TYPES					= "types";

	/** "message". */
	public static final String	WSDL_ELEM_MESSAGE				= "message";

	/** "portType". */
	public static final String	WSDL_ELEM_PORTTYPE				= "portType";

	/** "binding". */
	public static final String	WSDL_ELEM_BINDING				= "binding";

	/** "operation". */
	public static final String	WSDL_ELEM_OPERATION				= "operation";

	/** "service". */
	public static final String	WSDL_ELEM_SERVICE				= "service";

	/** "port". */
	public static final String	WSDL_ELEM_PORT					= "port";

	/** "fault". */
	public static final String	WSDL_ELEM_FAULT					= "fault";

	/** "input". */
	public static final String	WSDL_ELEM_INPUT					= "input";

	/** "output". */
	public static final String	WSDL_ELEM_OUTPUT				= "output";

	/** "part". */
	public static final String	WSDL_ELEM_PART					= "part";

	/** "import". */
	public static final String	WSDL_ELEM_IMPORT				= "import";

	/** "address". */
	public static final String	WSDL_ELEM_ADDRESS				= "address";

	/** "body". */
	public static final String	SOAP12_ELEM_BODY				= "body";

	/** "fault". */
	public static final String	SOAP12_ELEM_FAULT				= "fault";

	// Added SSch 2011-01-12 Used for WS-DualChannel
	/** "header". **/
	public static final String	SOAP12_ELEM_HEADER				= "header";

	/** "namespace". */
	public static final String	WSDL_ATTRIB_NAMESPACE			= "namespace";

	/** "binding". */
	public static final String	WSDL_ATTRIB_BINDING				= "binding";

	/** "location". */
	public static final String	WSDL_ATTRIB_LOCATION			= "location";

	/** "name". */
	public static final String	WSDL_ATTRIB_NAME				= "name";

	/** "message". */
	public static final String	WSDL_ATTRIB_MESSAGE				= "message";

	/** "element". */
	public static final String	WSDL_ATTRIB_ELEMENT				= "element";

	/** "type". */
	public static final String	WSDL_ATTRIB_TYPE				= "type";

	/** "style". */
	public static final String	WSDL_ATTRIB_STYLE				= "style";

	/** "use". */
	public static final String	WSDL_ATTRIB_USE					= "use";

	/** "style". */
	public static final String	WSDL_ATTRIB_TRANSPORT			= "transport";

	/** "soapAction". */
	public static final String	WSDL_ATTRIB_SOAP_ACTION			= "soapAction";

	// Added SSch 2011-01-12 Used for WS-DualChannel
	/** "part". **/
	public static final String	WSDL_ATTRIB_PART				= "part";

}
