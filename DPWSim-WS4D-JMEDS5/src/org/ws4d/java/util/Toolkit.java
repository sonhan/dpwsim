/*******************************************************************************
 * Copyright (c) 2009 MATERNA Information & Communications. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html. For further
 * project-related information visit http://www.ws4d.org. The most recent
 * version of the JMEDS framework can be obtained from
 * http://sourceforge.net/projects/ws4d-javame.
 ******************************************************************************/
package org.ws4d.java.util;

import java.io.PrintStream;

/**
 * This utility class includes a collection of methods, which are specific to
 * different Java Editions.
 */
public interface Toolkit {

	/**
	 * Implementation of the stack trace logging.
	 * 
	 * @param err Stream to, if possible, print the stack trace on.
	 * @param t Throwable to print.
	 */
	public void printStackTrace(PrintStream err, Throwable t);

	/**
	 * Returns the Java VM stack trace if possible.
	 * <p>
	 * Can return <code>null</code> if the platform does not support access to
	 * the stack trace!
	 * </p>
	 * 
	 * @param t stack trace
	 * @return stack trace as array of <code>String</code>.
	 */
	public String[] getStackTrace(Throwable t);

}
