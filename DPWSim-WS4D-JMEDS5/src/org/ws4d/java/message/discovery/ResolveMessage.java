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
import org.ws4d.java.types.EndpointReference;
import org.ws4d.java.types.URI;
import org.ws4d.java.util.StringUtil;

public class ResolveMessage extends Message {

	public static final URI		ACTION	= new URI(WSDConstants.WSD_ACTION_RESOLVE);

	private EndpointReference	endpointReference;

	public static SOAPHeader createResolveHeader(String communicationManagerId) {
		return DiscoveryMessage.createDiscoveryHeader(WSDConstants.WSD_ACTION_RESOLVE, communicationManagerId);
	}

	/**
	 * Creates a new Resolve message containing a {@link SOAPHeader} with the
	 * appropriate {@link SOAPHeader#getAction() action property} set, the
	 * default {@link SOAPHeader#getTo() to property} for ad-hoc mode (
	 * {@link WSDConstants#WSD_TO}) and a unique
	 * {@link SOAPHeader#getMessageId() message ID property}. All other header-
	 * and discovery-related fields are empty and it is the caller's
	 * responsibility to fill them with suitable values.
	 */
	public ResolveMessage(String communicationManagerId) {
		this(createResolveHeader(communicationManagerId));
	}

	public ResolveMessage(SOAPHeader header) {
		this(header, null);
	}

	public ResolveMessage(SOAPHeader header, EndpointReference endpointReference) {
		super(header);
		this.endpointReference = endpointReference;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		StringBuffer sb = new StringBuffer(StringUtil.formatClassName(getClass()));
		sb.append(" [ header=").append(header);
		sb.append(", inbound=").append(inbound);
		sb.append(", endpointReference=").append(endpointReference);
		sb.append(" ]");
		return sb.toString();
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.message.DPWSMessage#getType()
	 */
	public int getType() {
		return RESOLVE_MESSAGE;
	}

	/*
	 * (non-Javadoc)
	 * @seeorg.ws4d.java.communication.message.discovery.ResolveMessage#
	 * getEndpointReference()
	 */
	public EndpointReference getEndpointReference() {
		return endpointReference;
	}

	/**
	 * @param endpointReference the endpointReference to set
	 */
	public void setEndpointReference(EndpointReference endpointReference) {
		this.endpointReference = endpointReference;
	}
}
