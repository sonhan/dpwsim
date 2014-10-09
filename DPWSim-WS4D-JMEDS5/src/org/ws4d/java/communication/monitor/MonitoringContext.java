/*******************************************************************************
 * Copyright (c) 2009 MATERNA Information & Communications. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html. For further
 * project-related information visit http://www.ws4d.org. The most recent
 * version of the JMEDS framework can be obtained from
 * http://sourceforge.net/projects/ws4d-javame.
 ******************************************************************************/
package org.ws4d.java.communication.monitor;

import org.ws4d.java.communication.ProtocolData;
import org.ws4d.java.message.Message;

public class MonitoringContext {

	private static volatile long	gid		= 0L;

	private volatile long			id		= 0L;

	private Message					message	= null;

	private ProtocolData			pd		= null;

	MonitoringContext(ProtocolData pd) {
		id = gid++;
		this.pd = pd;
	}

	public long getIdentifier() {
		return id;
	}

	public String toString() {
		return "MonitoringContext [ id = " + id + ", pd = " + pd + " ]";
	}

	public void setMessage(Message message) {
		this.message = message;
	}

	public Message getMessage() {
		return message;
	}

	public ProtocolData getProtocolData() {
		return pd;
	}

	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (id ^ (id >>> 32));
		return result;
	}

	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		MonitoringContext other = (MonitoringContext) obj;
		if (id != other.id) return false;
		return true;
	}

}
