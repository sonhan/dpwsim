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

import org.ws4d.java.structures.HashMap;

public class PrefixRegistry {

	/*
	 * Namespace -> Prefix
	 */
	private static HashMap	prefixes	= new HashMap();

	static {
		prefixes.put(MEXConstants.WSX_NAMESPACE_NAME, MEXConstants.WSX_NAMESPACE_PREFIX);
		prefixes.put(SOAPConstants.SOAP12_NAMESPACE_NAME, SOAPConstants.SOAP12_NAMESPACE_PREFIX);
		prefixes.put(WS4DConstants.WS4D_NAMESPACE_NAME, WS4DConstants.WS4D_NAMESPACE_PREFIX);
		prefixes.put(WSAConstants2006.WSA_NAMESPACE_NAME, WSAConstants.WSA_NAMESPACE_PREFIX);
		prefixes.put(WSAConstants.WSA_NAMESPACE_NAME, WSAConstants.WSA_NAMESPACE_PREFIX);
		prefixes.put(WSDConstants2006.WSD_NAMESPACE_NAME, WSDConstants.WSD_NAMESPACE_PREFIX);
		prefixes.put(WSDConstants.WSD_NAMESPACE_NAME, WSDConstants.WSD_NAMESPACE_PREFIX);
		prefixes.put(WSDLConstants.WSDL_NAMESPACE_NAME, WSDLConstants.WSDL_NAMESPACE_PREFIX);
		prefixes.put(WSEConstants.WSE_NAMESPACE_NAME, WSEConstants.WSE_NAMESPACE_PREFIX);
		prefixes.put(WSPConstants.WSP_NAMESPACE_NAME_DPWS11, WSPConstants.WSP_NAMESPACE_PREFIX);
		prefixes.put(WSPConstants.WSP_NAMESPACE_NAME, WSPConstants.WSP_NAMESPACE_PREFIX);
		prefixes.put(WXFConstants.WXF_NAMESPACE_NAME, WXFConstants.WXF_NAMESPACE_PREFIX);
		prefixes.put(XMLConstants.XML_NAMESPACE_NAME, XMLConstants.XML_NAMESPACE_PREFIX);
		prefixes.put(XOPConstants.XOP_NAMESPACE_NAME, XOPConstants.XOP_NAMESPACE_PREFIX);
	}

	public static String getPrefix(String namespace) {
		return (String) prefixes.get(namespace);
	}

	public static void addPrefix(String namespace, String prefix) {
		prefixes.put(namespace, prefix);
	}
}
