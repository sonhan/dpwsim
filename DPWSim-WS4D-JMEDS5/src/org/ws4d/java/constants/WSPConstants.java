/**********************************************************************************
 * Copyright (c) 2007 MATERNA Information & Communications and TU Dortmund, Dpt.
 * of Computer Science, Chair 4, Distributed Systems All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 **********************************************************************************/

package org.ws4d.java.constants;

/**
 * Constants used by WS Policy.
 */
public class WSPConstants {

	/** The namespace name for WS Policy. */
	private static final String	WSP12_NAMESPACE_NAME				= "http://schemas.xmlsoap.org/ws/2004/09/policy";

	private static final String	WSP15_NAMESPACE_NAME				= "http://www.w3.org/ns/ws-policy";

	public static final String	WSP_NAMESPACE_NAME					= WSP15_NAMESPACE_NAME;

	public static final String	WSP_NAMESPACE_NAME_DPWS11			= WSP12_NAMESPACE_NAME;

	/** The default prefix for the WSP namespace. */
	public static final String	WSP_NAMESPACE_PREFIX				= "wsp";

	/** Elements */
	public static final String	WSP_ELEM_POLICY						= "Policy";

	public static final String	WSP_ELEM_POLICY_ATTR_NAME			= "Name";

	public static final String	WSP_ELEM_ALL						= "All";

	public static final String	WSP_ELEM_EXACTLYONCE				= "ExactlyOnce";

	public static final String	WSP_ELEM_POLICYREFERENCE			= "PolicyReference";

	public static final String	WSP_ELEM_POLICYREFERENCE_ATTR_URI	= "URI";

	/** Attribute used in compact from to mark elements as optional */
	public static final String	WSP_ATTR_OPTIONAL					= "Optional";

}
