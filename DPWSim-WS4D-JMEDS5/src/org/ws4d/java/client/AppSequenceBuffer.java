/*******************************************************************************
 * Copyright (c) 2009 MATERNA Information & Communications. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html. For further
 * project-related information visit http://www.ws4d.org. The most recent
 * version of the JMEDS framework can be obtained from
 * http://sourceforge.net/projects/ws4d-javame.
 ******************************************************************************/

package org.ws4d.java.client;

import org.ws4d.java.structures.AppSequenceTracker;
import org.ws4d.java.structures.LinkedMap;
import org.ws4d.java.types.AppSequence;
import org.ws4d.java.types.EndpointReference;

public class AppSequenceBuffer {

	protected static int	MAX_BUFFER_SIZE	= 50;

	// endpointReference address String -> AppSequenceTracker
	private LinkedMap		buffer;

	/**
	 * Constructor. Creates a new AppSequenceBuffer with the default size.
	 */
	public AppSequenceBuffer() {
		buffer = new LinkedMap(true);
	}

	/**
	 * Constructor. Creates a new AppSequenceBuffer with the given size.
	 * 
	 * @param size number of entries the buffer can hold.
	 */
	public AppSequenceBuffer(int size) {
		buffer = new LinkedMap(size, true);
	}

	/**
	 * Return true if the buffer contains an entry with the given epr, else
	 * false.
	 * 
	 * @param epr the EndpointReference to search for.
	 * @return true entry is found, else false.
	 */
	public synchronized boolean contains(EndpointReference epr) {
		return buffer.containsKey(epr);
	}

	/**
	 * @param epr
	 * @param other
	 * @return
	 */
	public synchronized boolean checkAndUpdate(EndpointReference epr, AppSequence appSeq) {
		String eprAddr = epr.getAddress().toString();

		AppSequenceTracker appSeqTracker = (AppSequenceTracker) buffer.get(eprAddr);

		if (appSeqTracker != null) return appSeqTracker.checkAndUpdate(appSeq);

		// tracker for epr not found
		if (buffer.size() >= MAX_BUFFER_SIZE) buffer.removeFirst();

		buffer.put(eprAddr, new AppSequenceTracker(appSeq));
		return true;
	}

	/**
	 * removes all entries.
	 */
	// public synchronized void clear() {
	//
	// }

	// private synchronized void removeEldestEntry() {
	//
	// }

}
