/*******************************************************************************
 * Copyright (c) 2009 MATERNA Information & Communications. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html. For further
 * project-related information visit http://www.ws4d.org. The most recent
 * version of the JMEDS framework can be obtained from
 * http://sourceforge.net/projects/ws4d-javame.
 ******************************************************************************/

package org.ws4d.java.types;

/**
 * Exception to keep an eye on malformed URIs.
 */
public class MalformedURIException extends Exception {

	/**
	 * 
	 */
	private static final long	serialVersionUID	= -5978275442418448552L;

	public MalformedURIException() {
		super("URI can not be parsed");
	}

	public MalformedURIException(String s) {
		super(s);
	}

}
