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
 * Class to memorize time information. Override timedOut() method to do
 * something at time out time.
 * 
 * @author mspies
 */
public abstract class TimedEntry {

	long	timeToRemove;

	boolean	disabled	= false;

	// used for timed entries which are unregistered and reregistered
	boolean	registered	= false;

	/**
	 * Sets timer, when this timed entry should be timed out and removed.
	 */
	void setTimer(final long timeUntilTimeout) {
		this.timeToRemove = timeUntilTimeout + System.currentTimeMillis();
	}

	/**
	 * Compares this TimedEntry with the specified TimedEntry for order. Returns
	 * a negative long, zero, or a positive long as this TimedEntry is less
	 * than, equal to, or greater than the specified TimedEntry.
	 * 
	 * @param other
	 * @return
	 */
	long compareTo(TimedEntry other) {
		return timeToRemove - other.timeToRemove;
	}

	/**
	 * Runs what should be done at time out. Will be called by the Watchdog at
	 * time out.
	 */
	protected abstract void timedOut();

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return "(TimeToRemove: " + timeToRemove + ")";
	}
}
