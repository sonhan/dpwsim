/*******************************************************************************
 * Copyright (c) 2009 MATERNA Information & Communications. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html. For further
 * project-related information visit http://www.ws4d.org. The most recent
 * version of the JMEDS framework can be obtained from
 * http://sourceforge.net/projects/ws4d-javame.
 ******************************************************************************/
package org.ws4d.java.wsdl;

import java.io.IOException;
import java.io.InputStream;

import org.ws4d.java.types.URI;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

/**
 *
 */
public interface WSDLParser {

	/**
	 * @param in
	 * @param fromUri the URI pointing to the WSDL document being parsed
	 * @param targetNamespace if <code>null</code>, then this is the top-level
	 *            WSDL file (i.e. not an import)
	 * @param loadReferencedFiles if <code>true</code>, other WSDL and XML
	 *            Schema files referenced by the parsed WSDL will be parsed
	 *            recursively, too
	 * @return
	 * @throws XmlPullParserException
	 * @throws IOException
	 */
	public WSDL parse(InputStream in, URI fromUri, String targetNamespace, boolean loadReferencedFiles) throws XmlPullParserException, IOException;

	/**
	 * @param parser
	 * @param fromUri the URI pointing to the WSDL document being parsed
	 * @param targetNamespace if <code>null</code>, then this is the top-level
	 *            WSDL file (i.e. not an import)
	 * @param loadReferencedFiles if <code>true</code>, other WSDL and XML
	 *            Schema files referenced by the parsed WSDL will be parsed
	 *            recursively, too
	 * @return
	 * @throws XmlPullParserException
	 * @throws IOException
	 */
	public WSDL parse(XmlPullParser parser, URI fromUri, String targetNamespace, boolean loadReferencedFiles) throws XmlPullParserException, IOException;

}
