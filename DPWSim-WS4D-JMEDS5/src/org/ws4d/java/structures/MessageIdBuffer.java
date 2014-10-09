/*******************************************************************************
 * Copyright (c) 2009 MATERNA Information & Communications. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html. For further
 * project-related information visit http://www.ws4d.org. The most recent
 * version of the JMEDS framework can be obtained from
 * http://sourceforge.net/projects/ws4d-javame.
 ******************************************************************************/
package org.ws4d.java.structures;

import org.ws4d.java.configuration.DispatchingProperties;
import org.ws4d.java.types.URI;

/**
 * @author mspies
 */
public class MessageIdBuffer {

	URI[]	messageIds;

	Set		uriSet;

	int		pos		= 0;

	int		size	= 0;

	/**
	 * 
	 */
	public MessageIdBuffer() {
		this(DispatchingProperties.getInstance().getMessageIdBufferSize());
	}

	/**
	 * @param size size of the message buffer to allocate
	 */
	public MessageIdBuffer(int size) {
		messageIds = new URI[size];
		uriSet = new HashSet(size);
	}

	public synchronized boolean contains(URI msgId) {
		return uriSet.contains(msgId);
	}

	/**
	 * Returns true, if buffer contains msg id, else false. If not contained,
	 * msg id is enqueued in buffer.
	 * 
	 * @param msgId the message to check presence of or to enqueue
	 * @return <code>true</code> if <code>msg</code> was already present,
	 *         <code>false</code> if it was just enqueued
	 */
	public synchronized boolean containsOrEnqueue(URI msgId) {
		if (msgId == null) {
			return false;
		}
		if (uriSet.contains(msgId)) {
			return true;
		}

		if (size == messageIds.length) {
			uriSet.remove(messageIds[pos]);
		} else {
			size++;
		}

		uriSet.add(msgId);
		messageIds[pos] = msgId;

		pos = (pos + 1) % messageIds.length;

		return false;
	}

	public synchronized void clear() {
		uriSet.clear();
		for (int i = 0; i < messageIds.length; i++) {
			messageIds[i] = null;
		}
		size = 0;
		pos = 0;
	}

}
