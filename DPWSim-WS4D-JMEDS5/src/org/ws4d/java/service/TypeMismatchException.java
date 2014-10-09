/*******************************************************************************
 * Copyright (c) 2009 MATERNA Information & Communications. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html. For further
 * project-related information visit http://www.ws4d.org. The most recent
 * version of the JMEDS framework can be obtained from
 * http://sourceforge.net/projects/ws4d-javame.
 ******************************************************************************/
package org.ws4d.java.service;

import org.ws4d.java.constants.SOAPConstants;
import org.ws4d.java.constants.WS4DConstants;
import org.ws4d.java.constants.WSAConstants;

/**
 *
 */
public class TypeMismatchException extends InvocationException {

	/**
	 * 
	 */
	private static final long	serialVersionUID		= 5445156909017299338L;

	private static final String	TYPE_MISMATCH_REASON	= "Unexpected Message content.";

	/**
	 * 
	 */
	public TypeMismatchException() {
		super(WSAConstants.WSA_ACTION_ADDRESSING_FAULT, SOAPConstants.SOAP_FAULT_SENDER, WS4DConstants.WS4D_FAULT_TYPE_MISMATCH, createReasonFromString(TYPE_MISMATCH_REASON), null);
	}

}
