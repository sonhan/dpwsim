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
 * An ID for a context (Primary for MIME context). Stores a protocol data
 * instance ID and a message number exchanged within the scope of that protocol
 * data ID.
 */
public class ContextID {

	private final long	instanceId;

	private final long	messageNumber;

	/**
	 * @param instanceId
	 * @param messageNumber
	 */
	public ContextID(long instanceId, long messageNumber) {
		super();
		this.instanceId = instanceId;
		this.messageNumber = messageNumber;
	}

	/**
	 * @return the instanceId
	 */
	public long getInstanceId() {
		return instanceId;
	}

	/**
	 * @return the messageNumber
	 */
	public long getMessageNumber() {
		return messageNumber;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		StringBuffer sb = new StringBuffer("ContextID [ instanceId=");
		sb.append(getInstanceId());
		sb.append(", messageNumber=").append(getMessageNumber()).append(" ]");
		return sb.toString();
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		ContextID other = (ContextID) obj;
		if (instanceId != other.instanceId) {
			return false;
		}
		if (messageNumber != other.messageNumber) {
			return false;
		}
		return true;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (instanceId ^ (instanceId >>> 32));
		result = prime * result + (int) (messageNumber ^ (messageNumber >>> 32));
		return result;
	}

}
