/*******************************************************************************
 * Copyright (c) 2009 MATERNA Information & Communications. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html. For further
 * project-related information visit http://www.ws4d.org. The most recent
 * version of the JMEDS framework can be obtained from
 * http://sourceforge.net/projects/ws4d-javame.
 ******************************************************************************/
package org.ws4d.java.message.eventing;

import org.ws4d.java.constants.WSEConstants;
import org.ws4d.java.message.SOAPHeader;
import org.ws4d.java.types.URI;

public class RenewResponseMessage extends EventingResponseMessage {

	public static final URI	ACTION	= new URI(WSEConstants.WSE_ACTION_RENEWRESPONSE);

	/**
	 * Creates a new RenewResponse message containing a {@link SOAPHeader} with
	 * the appropriate {@link SOAPHeader#getAction() action property} set. All
	 * other header- and eventing-related fields are empty and it is the
	 * caller's responsibility to fill them with suitable values.
	 */
	public RenewResponseMessage(String communicationManagerId) {
		this(SOAPHeader.createHeader(WSEConstants.WSE_ACTION_RENEWRESPONSE, communicationManagerId));
	}

	/**
	 * @param header
	 */
	public RenewResponseMessage(SOAPHeader header) {
		super(header);
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.message.DPWSMessage#getType()
	 */
	public int getType() {
		return RENEW_RESPONSE_MESSAGE;
	}

}
