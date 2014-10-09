/*******************************************************************************
 * Copyright (c) 2009 MATERNA Information & Communications. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html. For further
 * project-related information visit http://www.ws4d.org. The most recent
 * version of the JMEDS framework can be obtained from
 * http://sourceforge.net/projects/ws4d-javame.
 ******************************************************************************/
package org.ws4d.java.types;

import org.xmlpull.v1.XmlPullParser;

/**
 * Parser for custom attributes of type String. Simply returns the same
 * attribute value as obtained from the underlying XML parser wrapped within a
 * {@link StringAttributeValue} instance.
 */
public class StringAttributeParser implements CustomAttributeParser {

	public static final CustomAttributeParser	INSTANCE	= new StringAttributeParser();

	/*
	 * (non-Javadoc)
	 * @see
	 * org.ws4d.java.types.CustomAttributeParser#parse(org.xmlpull.v1.XmlPullParser
	 * , int)
	 */
	public CustomAttributeValue parse(XmlPullParser parser, int attributeIndex) {
		return new StringAttributeValue(parser.getAttributeValue(attributeIndex));
	}

}
