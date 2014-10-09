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
 * Constants from the XML world. You might have guessed that already, you old
 * fox ;)
 */
public interface XMLConstants {

	/** The namespace bound to the xml prefix by definition. */
	public static final String	XML_NAMESPACE_NAME				= "http://www.w3.org/XML/1998/namespace";

	public static final String	XML_NAMESPACE_PREFIX			= "xml";

	/** The namespace bound to the xmlns prefix by definition. */
	public static final String	XMLNS_NAMESPACE_NAME			= "http://www.w3.org/2000/xmlns/";

	public static final String	XMLNS_NAMESPACE_PREFIX			= "xmlns";

	public static final String	XMLNS_TARGETNAMESPACE_PREFIX	= "tns";

	/** Default namespace prefix for generated prefixes. */
	public static final String	XMLNS_DEFAULT_PREFIX			= "i";

	/** This is used to separate the namespace prefix from the namespace name. */
	public static final String	XML_NAMESPACE_SEPARATOR			= ":";

	/** xml attribute "xml:lang" */
	public static final String	XML_ATTRIBUTE_LANGUAGE			= "lang";

	/** Encoding for XML for SE */
	public static final String	ENCODING						= "UTF-8";

	/** Encoding for XML for CLDC */
	// public static final String ENCODING = "ISO-8859-1";

	/** Encoding for Foxboard */
	// public static final String ENCODING = "ISO-8859-1";

	/** QualifiedName of attribute "xml:lang". */
	public static final QName	XML_QN_LANG						= new QName(XML_ATTRIBUTE_LANGUAGE, XML_NAMESPACE_NAME, XML_NAMESPACE_PREFIX);

}
