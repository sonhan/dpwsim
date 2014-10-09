/*******************************************************************************
 * Copyright (c) 2009 MATERNA Information & Communications. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html. For further
 * project-related information visit http://www.ws4d.org. The most recent
 * version of the JMEDS framework can be obtained from
 * http://sourceforge.net/projects/ws4d-javame.
 ******************************************************************************/
package org.ws4d.java.communication;

/**
 * Thrown to indicate that an invalid version of a Web Services standard (such
 * as SOAP, WS-Addresing, DPWS, etc.) was found within a DPWS message.
 */
public class VersionMismatchException extends Exception {

	public static final int		TYPE_WRONG_SOAP_VERSION			= 0;

	public static final int		TYPE_WRONG_ADDRESSING_VERSION	= 1;

	public static final int		TYPE_WRONG_DPWS_VERSION			= 2;

	/**
	 * 
	 */
	private static final long	serialVersionUID				= 847221637960125784L;

	private int					type;

	/**
	 * @param message
	 */
	public VersionMismatchException(String message, int type) {
		super(message);
		this.type = type;
	}

	public int getType() {
		return type;
	}
}
