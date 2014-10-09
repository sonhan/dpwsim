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
 * Constants used by WS MetadataExchange.
 */
public interface MEXConstants {

	/** The namespace name for WS MetadataExchange. */
	public static final String	WSX_NAMESPACE_NAME				= "http://schemas.xmlsoap.org/ws/2004/09/mex";

	/** The default prefix for the WSMEX namespace. */
	public static final String	WSX_NAMESPACE_PREFIX			= "wsx";

	/* WS MetadataExchange known actions */
	public static final String	WSX_ACTION_GETMETADATA_REQUEST	= WSX_NAMESPACE_NAME + "/GetMetadata/Request";

	public static final String	WSX_ACTION_GETMETADATA_RESPONSE	= WSX_NAMESPACE_NAME + "/GetMetadata/Response";

	public static final String	WSX_ELEM_GETMETADATA			= "GetMetadata";

	/** "Dialect". */
	public static final String	WSX_ELEM_DIALECT				= "Dialect";

	/** "Metadata". */
	public static final String	WSX_ELEM_METADATA				= "Metadata";

	/** "MetadataSection". */
	public static final String	WSX_ELEM_METADATASECTION		= "MetadataSection";

	/** "MetadataReference". */
	public static final String	WSX_ELEM_METADATAREFERENCE		= "MetadataReference";

	/** "Identifier". */
	public static final String	WSX_ELEM_IDENTIFIER				= "Identifier";

	/** "Location". */
	public static final String	WSX_ELEM_LOCATION				= "Location";

	public static final String	WSX_DIALECT_WSDL				= "http://schemas.xmlsoap.org/wsdl";

}
