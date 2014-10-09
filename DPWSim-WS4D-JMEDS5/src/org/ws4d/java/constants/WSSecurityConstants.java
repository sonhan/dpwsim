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

public interface WSSecurityConstants {

	public static final String	XML_DIGITAL_SIGNATURE			= "http://www.w3.org/2000/09/xmldsig#";

	public static final String	WS_SECURITY						= "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd";

	public static final String	XML_SOAP_DISCOVERY				= "http://schemas.xmlsoap.org/ws/2005/04/discovery";

	public static final String	WS_SECURITY_WSU					= "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wsswssecurity-utility-1.0.xsd";

	public static final String	XML_DIGITAL_SIGNATURE_PREFIX	= "ds";

	public static final String	WS_SECURITY_PREFIX				= "wsse";

	public static final String	SIGNATURE_NAME					= "Signature";

	public static final String	SIGNED_INFO_NAME				= "SignedInfo";

	public static final String	CANONICALIZATION_METHOD_NAME	= "CanonicalizationMethod";

	public static final String	SIGNATURE_METHOD_NAME			= "SignatureMethod";

	public static final String	REFERENCE_NAME					= "Reference";

	public static final String	TRANSFORMS_NAME					= "Transforms";

	public static final String	TRANSFORM_NAME					= "Transform";

	public static final String	DIGENST_METHOD_NAME				= "DigestMethod";

	public static final String	DIGEST_VALUE_NAME				= "DigestValue";

	public static final String	ALGORITHM_NAME					= "Algorithm";

	public static final String	SIGNATURE_VALUE_NAME			= "SignatureValue";

	public static final String	KEY_INFO_NAME					= "KeyInfo";

	public static final String	SECURITY_TOKEN_REF_NAME			= "SecurityTokenReference";

	public static final String	KEY_IDENTIFIER_NAME				= "KeyIdentifier";

	public static final String	EXC_C14N						= "http://www.w3.org/2001/10/xml-exc-c14n#";

	public static final String	DSIG_RSA_SHA1					= XML_DIGITAL_SIGNATURE + "rsa-sha1";

	public static final String	DIGEST_METHOD					= XML_DIGITAL_SIGNATURE + "sha1";

	public static final String	COMPACT_SECURITY				= "Security";

	public static final String	COMPACT_SIG						= "Sig";

	public static final String	COMPACT_SCHEME					= "Scheme";

	public static final String	COMPACT_KEYID					= "KeyId";

	public static final String	COMPACT_REFS					= "Refs";

	public static final QName	WSSE_FAULT_AUTHENTICATION		= new QName("FailedAuthentication", WS_SECURITY, WS_SECURITY_PREFIX);

}
