/*******************************************************************************
 * Copyright (c) 2009 MATERNA Information & Communications. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html. For further
 * project-related information visit http://www.ws4d.org. The most recent
 * version of the JMEDS framework can be obtained from
 * http://sourceforge.net/projects/ws4d-javame.
 ******************************************************************************/
package org.ws4d.java.communication.protocol.http;

/**
 * Collection of HTTP status codes.
 */
public interface HTTPStatus {

	static final String	HTTP_100	= "Continue";

	static final String	HTTP_200	= "OK";

	static final String	HTTP_202	= "Accepted";

	static final String	HTTP_204	= "No Content";

	static final String	HTTP_300	= "Multiple Choices";

	static final String	HTTP_301	= "Moved Permanently";

	static final String	HTTP_302	= "Found";

	static final String	HTTP_303	= "See Other";
	
	static final String	HTTP_304	= "Not Modified";

	static final String	HTTP_307	= "Temporary Redirect";

	static final String	HTTP_400	= "Bad Request";

	static final String	HTTP_401	= "Unauthorized";

	static final String	HTTP_403	= "Forbidden";

	static final String	HTTP_404	= "Not Found";

	static final String	HTTP_415	= "Unsupported Media Type";

	static final String	HTTP_500	= "Internal Server Error";

	static final String	HTTP_501	= "Not Implemented";

	static final String	HTTP_505	= "HTTP Version not supported";

}
