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

import org.ws4d.java.util.StringUtil;

/**
 * 
 */
public class SOAPException extends Exception {

	/**
	 * 
	 */
	private static final long	serialVersionUID	= -2359211597196944496L;

	private final FaultMessage	fault;

	/**
	 * 
	 */
	public SOAPException() {
		this(null, null);
	}

	/**
	 * @param s the detail message
	 */
	public SOAPException(String s) {
		this(s, null);
	}

	/**
	 * @param fault the SOAP Fault to encapsulate
	 */
	public SOAPException(FaultMessage fault) {
		this(null, fault);
	}

	/**
	 * @param s the detail message
	 * @param fault the SOAP Fault to encapsulate
	 */
	public SOAPException(String s, FaultMessage fault) {
		super(s);
		this.fault = fault;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		StringBuffer sb = new StringBuffer(StringUtil.formatClassName(getClass()));
		sb.append(": [ fault=").append(fault);
		sb.append(" ]");
		return sb.toString();
	}

	/**
	 * @return the fault
	 */
	public FaultMessage getFault() {
		return fault;
	}

}
