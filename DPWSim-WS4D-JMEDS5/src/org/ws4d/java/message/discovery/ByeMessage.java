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
import org.ws4d.java.message.SOAPHeader;
import org.ws4d.java.types.DiscoveryData;
import org.ws4d.java.types.EndpointReference;
import org.ws4d.java.types.URI;

public class ByeMessage extends DiscoveryMessage {

	public static final URI	ACTION	= new URI(WSDConstants.WSD_ACTION_BYE);

	public static SOAPHeader createByeHeader(String communicationManagerId) {
		return createDiscoveryHeader(WSDConstants.WSD_ACTION_BYE, communicationManagerId);
	}

	/**
	 * Creates a new Bye message containing a {@link SOAPHeader} with the
	 * appropriate {@link SOAPHeader#getAction() action property} set, the
	 * default {@link SOAPHeader#getTo() to property} for ad-hoc mode (
	 * {@link WSDConstants#WSD_TO}) and a unique
	 * {@link SOAPHeader#getMessageId() message ID property}. All other header-
	 * and discovery-related fields are empty and it is the caller's
	 * responsibility to fill them with suitable values.
	 */
	public ByeMessage(String communicationManagerId) {
		this(createByeHeader(communicationManagerId), new DiscoveryData());
	}

	/**
	 * @param header
	 */
	public ByeMessage(SOAPHeader header) {
		this(header, null);
	}

	/**
	 * @param header
	 * @param endpointReference
	 * @param metadataVersion
	 */
	public ByeMessage(SOAPHeader header, EndpointReference endpointReference, long metadataVersion) {
		this(header, new DiscoveryData(endpointReference, metadataVersion));
	}

	/**
	 * @param discoveryData
	 */
	public ByeMessage(DiscoveryData discoveryData, String communicationManagerId) {
		super(createByeHeader(communicationManagerId), discoveryData);
	}

	/**
	 * @param header
	 * @param discoveryData
	 */
	public ByeMessage(SOAPHeader header, DiscoveryData discoveryData) {
		super(header, discoveryData);
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.message.DPWSMessage#getType()
	 */
	public int getType() {
		return BYE_MESSAGE;
	}

}
