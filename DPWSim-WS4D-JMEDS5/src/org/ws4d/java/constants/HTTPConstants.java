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

public interface HTTPConstants {

	/*
	 * HTTP request header
	 */

	public static final String	HTTP_SCHEMA									= "http";

	public static final String	HTTPS_SCHEMA								= "https";

	public static final String	HTTP_METHOD_GET								= "GET";

	public static final String	HTTP_METHOD_POST							= "POST";

	public static final String	HTTP_METHOD_HEAD							= "HEAD";

	public static final String	HTTP_METHOD_CONNECT							= "CONNECT";

	public static final String	HTTP_VERSION11								= "HTTP/1.1";

	public static final String	HTTP_HEADER_CONTENT_TYPE					= "Content-Type";

	public static final String	HTTP_HEADER_CONTENT_TRANSFER_ENCODING		= "Content-Transfer-Encoding";

	public static final String	HTTP_HEADER_EXPECT							= "Expect";

	public static final String	HTTP_HEADER_TRANSFER_ENCODING				= "Transfer-Encoding";

	public static final String	HTTP_HEADER_CONTENT_ENCODING				= "Content-Encoding";

	public static final String	HTTP_HEADER_CONTENT_LENGTH					= "Content-Length";

	public static final String	HTTP_HEADER_CONTENT_DISPOSITION				= "Content-Disposition";

	public static final String	HTTP_HEADER_AUTHORIZATION					= "Authorization";

	public static final String	HTTP_HEADER_WWW_AUTH						= "WWW-Authenticate";

	public static final String	HTTP_HEADER_CONNECTION						= "Connection";

	public static final String	HTTP_HEADER_DATE							= "Date";

	public static final String	HTTP_HEADER_LAST_MODIFIED					= "Last-Modified";

	public static final String	HTTP_HEADER_IF_MODIFIED_SINCE				= "If-Modified-Since";

	public static final String	HTTP_HEADER_USER_AGENT						= "User-Agent";

	public static final String	HTTP_HEADER_HOST							= "Host";

	public static final String	HTTP_HEADER_LOCATION						= "Location";

	public static final String	HTTP_HEADER_CACHECONTROL					= "Cache-Control";

	public static final String	HTTP_HEADER_TE								= "TE";

	public static final String	HTTP_HEADERVALUE_TRANSFERCODING_IDENTITY	= "identity";

	public static final String	HTTP_HEADERVALUE_TRANSFERCODING_CHUNKED		= "chunked";

	public static final String	HTTP_HEADERVALUE_TRANSFERENCODING_BINARY	= "binary";

	public static final String	HTTP_HEADERVALUE_TRANSFERENCODING_8BIT		= "8bit";

	public static final String	HTTP_HEADERVALUE_CONNECTION_CLOSE			= "close";

	public static final String	HTTP_HEADERVALUE_EXPECT_CONTINUE			= "100-continue";

	public static final String	HTTP_HEADERVALUE_TE_TRAILERS				= "trailers";

}
