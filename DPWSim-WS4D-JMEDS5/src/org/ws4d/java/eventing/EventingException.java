/*******************************************************************************
 * Copyright (c) 2009 MATERNA Information & Communications. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html. For further
 * project-related information visit http://www.ws4d.org. The most recent
 * version of the JMEDS framework can be obtained from
 * http://sourceforge.net/projects/ws4d-javame.
 ******************************************************************************/

package org.ws4d.java.eventing;

import org.ws4d.java.constants.SOAPConstants;
import org.ws4d.java.constants.WSAConstants;
import org.ws4d.java.message.FaultMessage;
import org.ws4d.java.service.InvocationException;
import org.ws4d.java.types.QName;

/**
 * WS-Eventing exception.
 * 
 * @author mspies
 */
public class EventingException extends InvocationException {

	/**
	 * 
	 */
	private static final long	serialVersionUID	= 7796438706511739206L;

	/**
	 * @param reason
	 */
	public EventingException(String reason) {
		this(null, reason);
	}

	/**
	 * @param subcode
	 * @param reason
	 */
	public EventingException(QName subcode, String reason) {
		super(WSAConstants.WSA_ACTION_ADDRESSING_FAULT, SOAPConstants.SOAP_FAULT_SENDER, subcode, createReasonFromString(reason), null);
	}

	public EventingException(FaultMessage fault) {
		super(fault);
	}

}
