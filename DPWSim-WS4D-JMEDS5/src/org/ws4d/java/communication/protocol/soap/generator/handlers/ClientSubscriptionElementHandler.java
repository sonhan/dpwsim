/*******************************************************************************
 * Copyright (c) 2009 MATERNA Information & Communications. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html. For further
 * project-related information visit http://www.ws4d.org. The most recent
 * version of the JMEDS framework can be obtained from
 * http://sourceforge.net/projects/ws4d-javame.
 ******************************************************************************/
package org.ws4d.java.communication.protocol.soap.generator.handlers;

import java.io.IOException;

import org.ws4d.java.io.xml.ElementHandler;
import org.ws4d.java.io.xml.ElementParser;
import org.ws4d.java.io.xml.XmlSerializer;
import org.ws4d.java.types.QName;
import org.ws4d.java.util.WS4DIllegalStateException;
import org.xmlpull.v1.XmlPullParserException;

public class ClientSubscriptionElementHandler implements ElementHandler {

	public void serializeElement(XmlSerializer serializer, QName qname, Object value) throws IllegalArgumentException, WS4DIllegalStateException, IOException {
		serializer.startTag(qname.getNamespace(), qname.getLocalPart());
		serializer.text((String) value);
		serializer.endTag(qname.getNamespace(), qname.getLocalPart());
	}

	public Object handleElement(QName elementName, ElementParser parser) throws XmlPullParserException, IOException {
		String s = parser.nextText();
		return s;
	}
}
