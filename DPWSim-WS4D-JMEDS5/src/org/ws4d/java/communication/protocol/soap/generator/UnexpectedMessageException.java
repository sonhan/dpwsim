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

import org.ws4d.java.message.Message;
import org.ws4d.java.message.discovery.ProbeMessage;
import org.ws4d.java.message.discovery.ResolveMatchesMessage;

/**
 * Thrown when a {@link Message} is received in response to a previously sent
 * request message, but the response message type doesn't correspond to the
 * request message type (e.g. when receiving a {@link ResolveMatchesMessage} in
 * response to a {@link ProbeMessage}).
 */
public class UnexpectedMessageException extends Exception {

	/**
	 * 
	 */
	private static final long	serialVersionUID	= -6075038208769929699L;

	/**
	 * 
	 */
	public UnexpectedMessageException() {}

	/**
	 * @param message
	 */
	public UnexpectedMessageException(String message) {
		super(message);
	}

}
