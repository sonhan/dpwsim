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
import org.ws4d.java.message.Message;
import org.ws4d.java.message.SOAPHeader;
import org.ws4d.java.types.URI;

/*
 * <?xml version='1.0' encoding='UTF-8' ?> <s12:Envelope
 * xmlns:s12="http://www.w3.org/2003/05/soap-envelope"
 * xmlns:wsa="http://schemas.xmlsoap.org/ws/2004/08/addressing"> <s12:Header>
 * <wsa:MessageID>urn:uuid:7b6d7a10-c83f-11dd-bf85-1274d808dc31</wsa:MessageID>
 * <
 * wsa:Action>http://schemas.xmlsoap.org/ws/2004/08/eventing/UnsubscribeResponse
 * </wsa:Action>
 * <wsa:RelatesTo>urn:uuid:7b6d7a10-c83f-11dd-bf78-0d3a112efe2a</wsa:RelatesTo>
 * <
 * wsa:To>http://schemas.xmlsoap.org/ws/2004/08/addressing/role/anonymous</wsa:
 * To > </s12:Header> <s12:Body /> </s12:Envelope>
 */

public class UnsubscribeResponseMessage extends Message {

	public static final URI	ACTION	= new URI(WSEConstants.WSE_ACTION_UNSUBSCRIBERESPONSE);

	/**
	 * Creates a new UnsubscribeResponse message containing a {@link SOAPHeader}
	 * with the appropriate {@link SOAPHeader#getAction() action property} set.
	 * All other header- and eventing-related fields are empty and it is the
	 * caller's responsibility to fill them with suitable values.
	 */
	public UnsubscribeResponseMessage(String communicationManagerId) {
		this(SOAPHeader.createHeader(WSEConstants.WSE_ACTION_UNSUBSCRIBERESPONSE, communicationManagerId));
	}

	/**
	 * @param header
	 */
	public UnsubscribeResponseMessage(SOAPHeader header) {
		super(header);
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.message.DPWSMessage#getType()
	 */
	public int getType() {
		return UNSUBSCRIBE_RESPONSE_MESSAGE;
	}

}
