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

import org.ws4d.java.communication.DPWSCommunicationManager;
import org.ws4d.java.communication.ProtocolData;
import org.ws4d.java.message.MessageDiscarder;
import org.ws4d.java.message.SOAPHeader;

/**
 * 
 *
 */
public class DefaultMessageDiscarder implements MessageDiscarder {

	/*
	 * (non-Javadoc)
	 * @see
	 * org.ws4d.java.message.MessageDiscarder#discardMessage(org.ws4d.java.message
	 * .SOAPHeader, org.ws4d.java.communication.ProtocolData)
	 */
	public int discardMessage(SOAPHeader header, ProtocolData protocolData) {
		return DPWSCommunicationManager.hasSentMessage(header.getMessageId()) ? OWN_MESSAGE : NOT_DISCARDED;
	}

}
