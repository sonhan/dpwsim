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
 * Constants of WS Transfer.
 */
public interface WXFConstants {

	/** The namespace name for WS Transfer. */
	public static final String	WXF_NAMESPACE_NAME		= "http://schemas.xmlsoap.org/ws/2004/09/transfer";

	/** The default prefix for the WSMEX namespace. */
	public static final String	WXF_NAMESPACE_PREFIX	= "wxf";

	/* WS Transfer known actions */
	public static final String	WXF_ACTION_GET			= WXF_NAMESPACE_NAME + "/Get";

	public static final String	WXF_ACTION_GETRESPONSE	= WXF_NAMESPACE_NAME + "/GetResponse";

	public static final String	WXF_ACTION_GET_REQUEST	= WXF_NAMESPACE_NAME + "/Get/Request";

	public static final String	WXF_ACTION_GET_RESPONSE	= WXF_NAMESPACE_NAME + "/Get/Response";

	public static final String	WXF_ELEMENT_GET			= "Get";

	public static final String	WXF_ELEMENT_GETRESPONSE	= "GetResponse";

}
