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

import org.ws4d.java.types.QName;

/**
 * Constants used by SOAP.
 */
public interface SOAPConstants {

	public static final String	SOAP_OVER_UDP_SCHEMA			= "soap.udp";

	/** The namespace name for SOAP 1.2! */
	public static final String	SOAP12_NAMESPACE_NAME			= "http://www.w3.org/2003/05/soap-envelope";

	/** The namespace name for the old SOAP 1.1! */
	public static final String	SOAP11_OLD_NAMESPACE_NAME		= "http://schemas.xmlsoap.org/soap/envelope/";

	/** The default prefix for the SOAP 1.2 namespace. */
	public static final String	SOAP12_NAMESPACE_PREFIX			= "s12";

	public static final String	SOAP_HTTP_TRANSPORT				= "http://schemas.xmlsoap.org/soap/http";

	public static final String	SOAP_FAULT_VERSIONMISMATCH_NAME	= "VersionMismatch";

	public static final String	SOAP_FAULT_MUSTUNDERSTAND		= "MustUnderstand";

	public static final String	SOAP_FAULT_DATAENCODINGUNKNOWN	= "DataEncodingUnknown";

	/** Problem with the message format or contained information. */
	public static final String	SOAP_FAULT_SENDER_NAME			= "Sender";

	/** Problems when processing the message, reason not the content itself. */
	public static final String	SOAP_FAULT_RECEIVER_NAME		= "Receiver";

	public static final QName	SOAP_FAULT_RECEIVER				= new QName(SOAP_FAULT_RECEIVER_NAME, SOAP12_NAMESPACE_NAME);

	public static final QName	SOAP_FAULT_VERSIONMISMATCH		= new QName(SOAP_FAULT_VERSIONMISMATCH_NAME, SOAP12_NAMESPACE_NAME);

	public static final QName	SOAP_FAULT_SENDER				= new QName(SOAP_FAULT_SENDER_NAME, SOAP12_NAMESPACE_NAME);

	/** "Envelope". */
	public static final String	SOAP_ELEM_ENVELOPE				= "Envelope";

	/** "Header". */
	public static final String	SOAP_ELEM_HEADER				= "Header";

	/** "Body". */
	public static final String	SOAP_ELEM_BODY					= "Body";

	/** "Fault". */
	public static final String	SOAP_ELEM_FAULT					= "Fault";

	/** "Code". */
	public static final String	SOAP_ELEM_CODE					= "Code";

	/** "Subcode". */
	public static final String	SOAP_ELEM_SUBCODE				= "Subcode";

	/** "Reason". */
	public static final String	SOAP_ELEM_REASON				= "Reason";

	/** "Detail". */
	public static final String	SOAP_ELEM_DETAIL				= "Detail";

	/** "Value". */
	public static final String	SOAP_ELEM_VALUE					= "Value";

	/** "Text". */
	public static final String	SOAP_ELEM_TEXT					= "Text";

	/** "Security" */
	public static final String	SOAP_ELEM_SECURITY				= "Security";
}
