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

public class GetStatusResponseMessage extends EventingResponseMessage {

	public static final URI	ACTION	= new URI(WSEConstants.WSE_ACTION_GETSTATUSRESPONSE);

	/**
	 * Creates a new GetStatusResponse message containing a {@link SOAPHeader}
	 * with the appropriate {@link SOAPHeader#getAction() action property} set.
	 * All other header- and eventing-related fields are empty and it is the
	 * caller's responsibility to fill them with suitable values.
	 */
	public GetStatusResponseMessage(String communicationManagerId) {
		this(SOAPHeader.createHeader(WSEConstants.WSE_ACTION_GETSTATUSRESPONSE, communicationManagerId));
	}

	/**
	 * @param header
	 */
	public GetStatusResponseMessage(SOAPHeader header) {
		super(header);
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.message.DPWSMessage#getType()
	 */
	public int getType() {
		return GET_STATUS_RESPONSE_MESSAGE;
	}
}
