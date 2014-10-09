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
import java.io.OutputStream;

import org.ws4d.java.communication.ProtocolData;
import org.ws4d.java.message.Message;
import org.ws4d.java.types.ByteArrayBuffer;
import org.ws4d.java.util.WS4DIllegalStateException;

public interface Message2SOAPGenerator {

	/**
	 * Serialize an DPWS Message Object to the given output stream.
	 * 
	 * @param out stream, to which the element should be serialized.
	 * @param pd TODO
	 * @param msg, which is use to get the Information
	 */
	public void generateSOAPMessage(OutputStream out, Message msg, ProtocolData pd) throws IllegalArgumentException, WS4DIllegalStateException, IOException;

	/**
	 * Serialize an DPWS Message Object to an array of bytes.
	 * 
	 * @param msg msg, which is use to get the Information
	 */
	public ByteArrayBuffer generateSOAPMessage(Message msg, ProtocolData pd) throws IOException;

}