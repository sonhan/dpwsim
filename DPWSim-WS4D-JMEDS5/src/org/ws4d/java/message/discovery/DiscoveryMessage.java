/*******************************************************************************
 * Copyright (c) 2009 MATERNA Information & Communications. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html. For further
 * project-related information visit http://www.ws4d.org. The most recent
 * version of the JMEDS framework can be obtained from
 * http://sourceforge.net/projects/ws4d-javame.
 ******************************************************************************/
package org.ws4d.java.message.discovery;

import org.ws4d.java.constants.WSDConstants;
import org.ws4d.java.message.Message;
import org.ws4d.java.message.SOAPHeader;
import org.ws4d.java.types.AttributedURI;
import org.ws4d.java.types.DiscoveryData;
import org.ws4d.java.types.EndpointReference;
import org.ws4d.java.types.QNameSet;
import org.ws4d.java.types.ScopeSet;
import org.ws4d.java.types.XAddressInfoSet;
import org.ws4d.java.util.IDGenerator;
import org.ws4d.java.util.StringUtil;

/**
 * 
 */
public abstract class DiscoveryMessage extends Message {

	private DiscoveryData	discoveryData	= null;

	public static SOAPHeader createDiscoveryHeader(String action, String communicationManagerId) {
		SOAPHeader header = SOAPHeader.createHeader(action, communicationManagerId);
		header.setMessageId(new AttributedURI(IDGenerator.getUUIDasURI()));
		header.setTo(new AttributedURI(WSDConstants.WSD_TO));
		return header;
	}

	/**
	 * @param header
	 */
	DiscoveryMessage(SOAPHeader header) {
		this(header, null);
	}

	/**
	 * @param header
	 * @param discoveryData
	 */
	DiscoveryMessage(SOAPHeader header, DiscoveryData discoveryData) {
		super(header);
		this.discoveryData = discoveryData;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		StringBuffer sb = new StringBuffer(StringUtil.formatClassName(getClass()));
		sb.append(" [ header=").append(header);
		sb.append(", inbound=").append(inbound);
		sb.append(", discoveryData=").append(discoveryData);
		sb.append(" ]");
		return sb.toString();
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.types.DiscoveryData#getEndpointReference()
	 */
	public EndpointReference getEndpointReference() {
		return discoveryData.getEndpointReference();
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.types.DiscoveryData#getMetadataVersion()
	 */
	public long getMetadataVersion() {
		return discoveryData.getMetadataVersion();
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.types.DiscoveryData#getTypes()
	 */
	public QNameSet getTypes() {
		return discoveryData.getTypes();
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.types.DiscoveryData#getScopes()
	 */
	public ScopeSet getScopes() {
		return discoveryData.getScopes();
	}


	/**
	 * Returns a {@link XAddressInfoSet}.
	 * 
	 * @return a {@link XAddressInfoSet}
	 */
	public XAddressInfoSet getXAddressInfoSet() {
		return discoveryData.getXAddressInfoSet();
	}

	/**
	 * Get discovery data.
	 * 
	 * @return Discovery data.
	 */
	public DiscoveryData getDiscoveryData() {
		return discoveryData;
	}

	/**
	 * @param discoveryData the discoveryData to set
	 */
	public void setDiscoveryData(DiscoveryData discoveryData) {
		this.discoveryData = discoveryData;
	}

}
