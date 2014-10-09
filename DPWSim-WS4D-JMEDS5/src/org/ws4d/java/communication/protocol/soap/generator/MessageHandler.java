/*******************************************************************************
 * Copyright (c) 2009 MATERNA Information & Communications. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html. For further
 * project-related information visit http://www.ws4d.org. The most recent
 * version of the JMEDS framework can be obtained from
 * http://sourceforge.net/projects/ws4d-javame.
 ******************************************************************************/
package org.ws4d.java.communication.protocol.soap.generator;

import java.io.IOException;

import org.ws4d.java.communication.ProtocolData;
import org.ws4d.java.communication.VersionMismatchException;
import org.ws4d.java.io.xml.ElementParser;
import org.ws4d.java.message.SOAPHeader;
import org.xmlpull.v1.XmlPullParserException;

/**
 * 
 * 
 */
public interface MessageHandler {

	public void deliverMessage(String actionName, SOAPHeader header, ElementParser parser, MessageReceiver to, ProtocolData protocolData) throws XmlPullParserException, IOException, UnexpectedMessageException, VersionMismatchException;

}
