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

/**
 * 
 */
public class DPWSProtocolData extends ProtocolData {

	private static final Object	INSTANCE_ID_LOCK	= new Object();

	private static long			instanceIdInc		= 0L;

	private final Long			instanceId;

	private final boolean		connectionOriented;

	private final String		iFace;

	private final String		sourceHost;

	private int					sourcePort;

	private final String		destinationHost;

	private final int			destinationPort;

	private volatile ContextID	currentMIMEContext;

	private URI					transportAddress	= null;

	public DPWSProtocolData(String iFace, boolean direction, String sourceHost, int sourcePort, String destinationHost, int destinationPort, boolean connectionOriented) {
		super(direction);
		synchronized (INSTANCE_ID_LOCK) {
			this.instanceId = new Long(instanceIdInc++);
		}
		this.iFace = iFace;
		this.sourceHost = sourceHost;
		this.sourcePort = sourcePort;
		this.destinationHost = destinationHost;
		this.destinationPort = destinationPort;
		this.connectionOriented = connectionOriented;
	}
	
	private DPWSProtocolData(String iFace, boolean direction, String sourceHost, int sourcePort, String destinationHost, int destinationPort, boolean connectionOriented, Long instanceId) {
		super(direction);
		this.instanceId = instanceId;
		this.iFace = iFace;
		this.sourceHost = sourceHost;
		this.sourcePort = sourcePort;
		this.destinationHost = destinationHost;
		this.destinationPort = destinationPort;
		this.connectionOriented = connectionOriented;
	}

	public ProtocolData createSwappedProtocolData() {
		return (ProtocolData) new DPWSProtocolData(this.iFace, !direction, this.destinationHost, this.destinationPort, this.sourceHost, this.sourcePort, this.connectionOriented, this.instanceId);
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.communication.ProtocolData#getProtocolId()
	 */
	public String getCommunicationManagerId() {
		return DPWSCommunicationManager.COMMUNICATION_MANAGER_ID;
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.communication.ProtocolData#getiFace()
	 */
	public String getIFace() {
		return iFace;
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.communication.ProtocolData#getSourceAddress()
	 */
	public String getSourceAddress() {
		return getSourceHost() + '@' + getSourcePort();
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.communication.ProtocolData#getDestinationAddress()
	 */
	public String getDestinationAddress() {
		return getDestinationHost() + '@' + getDestinationPort();
	}

	/**
	 * @return the instanceId
	 */
	public Long getInstanceId() {
		return instanceId;
	}

	/**
	 * @return
	 */
	public String getSourceHost() {
		return sourceHost;
	}

	/**
	 * @return
	 */
	public int getSourcePort() {
		return sourcePort;
	}

	/**
	 * @return
	 */
	public String getDestinationHost() {
		return destinationHost;
	}

	/**
	 * @return
	 */
	public int getDestinationPort() {
		return destinationPort;
	}

	/**
	 * @return
	 */
	public boolean isConnectionOriented() {
		return connectionOriented;
	}

	/**
	 * @return the currentMIMEContext
	 */
	public ContextID getCurrentMIMEContext() {
		return currentMIMEContext;
	}

	/**
	 * @param currentMIMEContext the currentMIMEContext to set
	 */
	public void setCurrentMIMEContext(ContextID currentMIMEContext) {
		this.currentMIMEContext = currentMIMEContext;
	}

	/**
	 * @param sourcePort the sourcePort to set
	 */
	public void setSourcePort(int sourcePort) {
		if (this.sourcePort == 0) {
			this.sourcePort = sourcePort;
		} else if (this.sourcePort != sourcePort) {
			throw new RuntimeException("Attempt to overwrite a non-zero source port.");
		}
	}

	public String toString() {
		return "DPWSProtocolData [ id=" + getInstanceId() + ", from=" + getSourceAddress() + ", to=" + getDestinationAddress() + " ]";
	}

	public URI getTransportAddress() {
		return transportAddress;
	}

	public void setTransportAddress(URI transportAddress) {
		this.transportAddress = transportAddress;
	}

}
