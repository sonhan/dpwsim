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

/**
 * Synchronizes objects for correct wait() and notify() implementation as
 * described in Object.wait().
 */

public class Sync {

	private volatile boolean	notify		= false;

	private Exception			exception	= null;

	/**
	 * Returns <code>true</code> if a notification was created,
	 * <code>false</code> otherwise.
	 * 
	 * @return <code>true</code> if a notification was created,
	 *         <code>false</code> otherwise.
	 */
	public synchronized boolean isNotified() {
		return notify;
	}

	/**
	 * Notifies all.
	 */
	public synchronized void notifyNow() {
		notify = true;
		this.notifyAll();
	}

	/**
	 * Notifies all if an exception occurs.
	 */
	public synchronized void notifyNow(Exception e) {
		notify = true;
		exception = e;
		this.notifyAll();
	}

	/**
	 * Returns the exception for this synchronization object.
	 * 
	 * @return
	 */
	public synchronized Exception getException() {
		return exception;
	}

	public synchronized void reset() {
		notify = false;
		exception = null;
	}

}
