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

import org.ws4d.java.types.URI;

public abstract class ProtocolData {

	public static final boolean	DIRECTION_IN	= true;

	public static final boolean	DIRECTION_OUT	= false;

	protected boolean			direction		= DIRECTION_IN;

	private ProtocolInfo		protocolInfo	= null;

	protected ProtocolData(boolean direction) {
		this.direction = direction;
	}

	public boolean isIncoming() {
		return direction;
	}

	public abstract ProtocolData createSwappedProtocolData();

	public abstract String getCommunicationManagerId();

	public boolean sourceMatches(URI uri) {
		return CommunicationManagerRegistry.getManager(getCommunicationManagerId()).addressMatches(uri, true, this);
	}

	public boolean destinationMatches(URI uri) {
		return CommunicationManagerRegistry.getManager(getCommunicationManagerId()).addressMatches(uri, false, this);
	}

	public abstract String getIFace();

	public abstract String getSourceAddress();

	public abstract String getDestinationAddress();

	public ProtocolInfo getProtocolInfo() {
		return protocolInfo;
	}

	public void setProtocolInfo(ProtocolInfo protocolInfo) {
		if (this.protocolInfo != null && protocolInfo != null) {
			this.protocolInfo.merge(protocolInfo);
		}
		this.protocolInfo = protocolInfo;
	}

	/**
	 * @return the currentMIMEContext
	 */
	public abstract ContextID getCurrentMIMEContext();

	/**
	 * @param currentMIMEContext the currentMIMEContext to set
	 */
	public abstract void setCurrentMIMEContext(ContextID currentMIMEContext);

	public abstract URI getTransportAddress();

	public abstract void setTransportAddress(URI transportAddress);

	public abstract String getDestinationHost();

	public abstract int getDestinationPort();

	public abstract Long getInstanceId();
}
