/*******************************************************************************
 * Copyright (c) 2009 MATERNA Information & Communications. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html. For further
 * project-related information visit http://www.ws4d.org. The most recent
 * version of the JMEDS framework can be obtained from
 * http://sourceforge.net/projects/ws4d-javame.
 ******************************************************************************/
package org.ws4d.java.message.metadata;

import org.ws4d.java.constants.WXFConstants;
import org.ws4d.java.message.Message;
import org.ws4d.java.message.SOAPHeader;
import org.ws4d.java.types.URI;

public class GetMessage extends Message {

	public static final URI	ACTION	= new URI(WXFConstants.WXF_ACTION_GET);

	/**
	 * Creates a new Get message containing a {@link SOAPHeader} with the
	 * appropriate {@link SOAPHeader#getAction() action property} set and a
	 * unique {@link SOAPHeader#getMessageId() message ID property}. All other
	 * header- and transfer-related fields are empty and it is the caller's
	 * responsibility to fill them with suitable values.
	 */
	public GetMessage(String communicationManagerId) {
		this(SOAPHeader.createRequestHeader(WXFConstants.WXF_ACTION_GET, communicationManagerId));
	}

	/**
	 * @param header
	 */
	public GetMessage(SOAPHeader header) {
		super(header);
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.message.DPWSMessage#getType()
	 */
	public int getType() {
		return GET_MESSAGE;
	}

}
