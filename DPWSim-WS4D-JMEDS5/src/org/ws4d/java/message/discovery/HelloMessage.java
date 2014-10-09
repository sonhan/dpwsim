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
import org.ws4d.java.types.URI;

/**
 * 
 *
 */
public class HelloMessage extends DiscoveryMessage {

	public URI	ACTION	= new URI(WSDConstants.WSD_ACTION_HELLO);

	private static SOAPHeader createHelloHeader(String communicationManagerId) {
		return createDiscoveryHeader(WSDConstants.WSD_ACTION_HELLO, communicationManagerId);
	}

	/**
	 * Creates a new Hello message containing a {@link SOAPHeader} with the
	 * appropriate {@link SOAPHeader#getAction() action property} set, the
	 * default {@link SOAPHeader#getTo() to property} for ad-hoc mode (
	 * {@link WSDConstants#WSD_TO}) and a unique
	 * {@link SOAPHeader#getMessageId() message ID property}. All other header-
	 * and discovery-related fields are empty and it is the caller's
	 * responsibility to fill them with suitable values.
	 */
	public HelloMessage(String communicationManagerId) {
		this(new DiscoveryData(), communicationManagerId);
	}

	/**
	 * @param discoveryData
	 */
	public HelloMessage(DiscoveryData discoveryData, String communicationManagerId) {
		this(createHelloHeader(communicationManagerId), discoveryData);
	}

	/**
	 * @param header
	 * @param discoveryData
	 */
	public HelloMessage(SOAPHeader header, DiscoveryData discoveryData) {
		super(header, discoveryData);
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.message.DPWSMessage#getType()
	 */
	public int getType() {
		return HELLO_MESSAGE;
	}
}
