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
 * A parser for a custom attribute value. It can be used to create an
 * appropriate Java data type for representation of the attribute value.
 */
public interface CustomAttributeParser {

	/**
	 * Returns a custom attribute value instance created from the attribute at
	 * the specified <code>attributeIndex</code> from the given
	 * <code>parser</code>.
	 * 
	 * @param parser the parser the attribute is read from
	 * @param attributeIndex the index of the attribute, at which it can be
	 *            obtained from the parser
	 * @return an instance representing the attribute value as a Java object of
	 *         a specific type
	 */
	public CustomAttributeValue parse(XmlPullParser parser, int attributeIndex);

}
