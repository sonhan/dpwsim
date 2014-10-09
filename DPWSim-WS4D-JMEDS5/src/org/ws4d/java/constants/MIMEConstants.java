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
 * Collection of MIME constants.
 */
public interface MIMEConstants {

	// RFC2046 5.1.1 Common Syntax
	public static final char	BOUNDARY_HYPHEN							= 45;

	public static final String	BOUNDARY_PREFIX							= "boundary.";

	public static final String	MIME_HEADER_CONTENT_TYPE				= "Content-Type";

	public static final String	MIME_HEADER_CONTENT_TRANSFER_ENCODING	= "Content-Transfer-Encoding";

	public static final String	MIME_HEADER_CONTENT_ID					= "Content-ID";

	// RFC2392: Content-ID and Message-ID Uniform Resource Locators
	public static final String	CONTENT_ID_PREFIX						= "cid";

	public static final String	MESSAGE_ID_PREFIX						= "mid";

	public static final String	ID_SEPARATOR							= ":";

	public static final String	ID_BEGINCHAR							= "<";

	public static final String	ID_ENDCHAR								= ">";

	// separator
	public static final String	SEPARATOR								= "/";

	public static final String	PARAMETER_START							= "start";

	public static final String	PARAMETER_STARTINFO						= "start-info";

	public static final String	PARAMETER_TYPE							= "type";

	public static final String	PARAMETER_STARTVALUE					= "soap-envelope";

	public static final String	PARAMETER_BOUNDARY						= "boundary";

	// media type
	public static final String	MEDIATYPE_TEXT							= "text";

	public static final String	MEDIATYPE_IMAGE							= "image";

	public static final String	MEDIATYPE_AUDIO							= "audio";

	public static final String	MEDIATYPE_VIDEO							= "video";

	public static final String	MEDIATYPE_APPLICATION					= "application";

	public static final String	MEDIATYPE_MULTIPART						= "multipart";

	// sub type
	public static final String	SUBTYPE_PLAIN							= "plain";

	public static final String	SUBTYPE_HTML							= "html";

	public static final String	SUBTYPE_XML								= "xml";

	public static final String	SUBTYPE_JAVASCRIPT						= "javascript";

	public static final String	SUBTYPE_CSS								= "css";

	public static final String	SUBTYPE_PNG								= "png";

	public static final String	SUBTYPE_GIF								= "gif";

	public static final String	SUBTYPE_TIFF							= "tiff";

	public static final String	SUBTYPE_ICON							= "x-icon";

	public static final String	SUBTYPE_JPEG							= "jpeg";

	public static final String	SUBTYPE_MPEG							= "mpeg";

	public static final String	SUBTYPE_POSTSCRIPT						= "postscript";

	public static final String	SUBTYPE_PDF								= "pdf";

	public static final String	SUBTYPE_GZIP							= "gzip";

	public static final String	SUBTYPE_ZIP								= "zip";

	public static final String	SUBTYPE_MSWORD							= "msword";

	public static final String	SUBTYPE_MSEXCEL							= "msexcel";

	public static final String	SUBTYPE_RELATED							= "related";

	public static final String	SUBTYPE_XWWWFORMURLENCODED				= "x-www-form-urlencoded";

	public static final String	SUBTYPE_SOAPXML							= "soap+xml";

	public static final String	SUBTYPE_XOPXML							= "xop+xml";

	public static final String	SUBTYPE_XHTML							= "xhtml+xml";

	public static final String	SUBTYPE_FORMDATA						= "form-data";

	public static final String	SUBTYPE_WINDOWSMEDIA					= "x-ms-wmv";

	public static final String	SUBTYPE_RAR								= "x-rar-compressed";

	public static final String	SUBTYPE_SHOCKWAVEFLASH					= "x-shockwave-flash";

	public static final String	SUBTYPE_WINDOWSEXECUTEABLE				= "octet-stream";

	public static final String	SUBTYPE_AVI								= "x-msvideo";

	public static final String	SUBTYPE_MPEG3							= "x-mpeg";

	public static final String	SUBTYPE_RICHTEXT						= "rtf";

	public static final String	SUBTYPE_OCTETSTEAM						= "octet-stream";

	public static final String	DEFAULT_HEADERFIELD_PREFIX				= "content-";

}
