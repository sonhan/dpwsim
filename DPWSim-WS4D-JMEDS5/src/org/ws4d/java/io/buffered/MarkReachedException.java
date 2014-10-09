/*******************************************************************************
 * Copyright (c) 2009 MATERNA Information & Communications. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html. For further
 * project-related information visit http://www.ws4d.org. The most recent
 * version of the JMEDS framework can be obtained from
 * http://sourceforge.net/projects/ws4d-javame.
 ******************************************************************************/
package org.ws4d.java.io.buffered;

import java.io.IOException;

/**
 * This exception should be thrown if someone reads more bytes from a
 * <code>BufferedInputStream</code> as marked.
 */
public class MarkReachedException extends IOException {

	/**
	 * 
	 */
	private static final long	serialVersionUID	= 7003878986340548509L;

	public MarkReachedException() {
		super("Stream mark reached");
	}

	public MarkReachedException(String message) {
		super("Stream mark reached: " + message);
	}
}
