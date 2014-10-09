/*******************************************************************************
 * Copyright (c) 2009 MATERNA Information & Communications. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html. For further
 * project-related information visit http://www.ws4d.org. The most recent
 * version of the JMEDS framework can be obtained from
 * http://sourceforge.net/projects/ws4d-javame.
 ******************************************************************************/
package org.ws4d.java.message;

import org.ws4d.java.communication.ProtocolData;
import org.ws4d.java.communication.protocol.soap.generator.SOAP2MessageGenerator;

/**
 * Implementations of this interface are queried each time a message is to be
 * created within {@link SOAP2MessageGenerator}.
 */
public interface MessageDiscarder {

	public static final int	NOT_DISCARDED			= 0;

	public static final int	OWN_MESSAGE				= 1;

	public static final int	DUPLICATE_MESSAGE		= 2;

	public static final int	NOT_RELEVANT_MESSAGE	= 3;

	public static final int	VERSION_NOT_SUPPORTED	= 4;

	public static final int	OLD_APPSEQUENCE			= 5;

	public static final int	VALIDATION_FAILED		= 6;

	/**
	 * Returns <code>true</code> in cases where the message with the given SOAP
	 * <code>header</code> and with the associated transport information
	 * described by <code>protocolData</code> should not be further processed
	 * (i.e. it should be discarded immediately).
	 * 
	 * @param header the header of the message
	 * @param protocolData transport-related addressing information attached to
	 *            the message with the given <code>header</code>
	 * @return whether to discard the message or not
	 *         <p>
	 *         <ul>
	 *         <li>0 = message not discarded</li>
	 *         <li>1 = message discarded because it was an own message send and
	 *         received by the framework</li>
	 *         <li>2 = message discarded because it an earlier version of that
	 *         message was already parsed</li>
	 *         <li>3 = message is not relevant</li>
	 *         <li>4 = message version not supported</li>
	 *         <li>5 = old <code>AppSequence</code> found</li>
	 *         </ul>
	 *         </p>
	 */
	public int discardMessage(SOAPHeader header, ProtocolData protocolData);
}