/*******************************************************************************
 * Copyright (c) 2009 MATERNA Information & Communications. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html. For further
 * project-related information visit http://www.ws4d.org. The most recent
 * version of the JMEDS framework can be obtained from
 * http://sourceforge.net/projects/ws4d-javame.
 ******************************************************************************/
package org.ws4d.java.dispatch;

import org.ws4d.java.communication.ProtocolData;
import org.ws4d.java.message.Message;

/**
 * Implementations of this interface can register within {@link MessageInformer}
 * to receive notifications about
 * {@link #receivedInboundMessage(Message, ProtocolData) inbound} and
 * {@link #receivedOutboundMessage(Message, ProtocolData) outbound} messages
 * running through a DPWS framework instance.
 */
public interface MessageListener {

	/**
	 * Called each time an inbound message arrives, which matches the interest
	 * of this message listener instance (see {@link MessageSelector}). The
	 * implementation should return as quickly as possible.
	 * 
	 * @param msg the message of interest
	 * @param protocolData transport-specific addressing information attached to
	 *            the message
	 */
	public void receivedInboundMessage(Message msg, ProtocolData protocolData);

	/**
	 * Called each time when an outbound message arrives, which matches the
	 * interest of this message listener instance (see {@link MessageSelector}).
	 * The implementation should return as quickly as possible.
	 * 
	 * @param msg the message of interest
	 * @param protocolData transport-specific addressing information attached to
	 *            the message
	 */
	public void receivedOutboundMessage(Message msg, ProtocolData protocolData);

}
