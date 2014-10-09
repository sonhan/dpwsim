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
 * <wsa:MessageID>urn:uuid:7b6d7a10-c83f-11dd-bf78-0d3a112efe2a</wsa:MessageID>
 * <
 * wsa:Action>http://schemas.xmlsoap.org/ws/2004/08/eventing/Unsubscribe</wsa:Action
 * ><wsa:To>http://139.2.58.102:21682/730e6140-c83f-11dd-bf7b-1274d808dc00/
 * DeviceAdmin</wsa:To> <wsa:ReplyTo>
 * <wsa:Address>http://schemas.xmlsoap.org/ws/
 * 2004/08/addressing/role/anonymous</wsa:Address> </wsa:ReplyTo>
 * <wse:Identifier
 * xmlns:wse="http://schemas.xmlsoap.org/ws/2004/08/eventing">urn
 * :uuid:7ad9c590-c83f-11dd-bf83-1274d808dc31</wse:Identifier> </s12:Header>
 * <s12:Body> <n0:Unsubscribe
 * xmlns:n0="http://schemas.xmlsoap.org/ws/2004/08/eventing" /> </s12:Body>
 * </s12:Envelope>
 */

public class UnsubscribeMessage extends Message {

	public static final URI	ACTION	= new URI(WSEConstants.WSE_ACTION_UNSUBSCRIBE);

	/**
	 * Creates a new Unsubscribe message containing a {@link SOAPHeader} with
	 * the appropriate {@link SOAPHeader#getAction() action property} set and a
	 * unique {@link SOAPHeader#getMessageId() message ID property}. All other
	 * header- and eventing-related fields are empty and it is the caller's
	 * responsibility to fill them with suitable values.
	 */
	public UnsubscribeMessage(String communicationManagerId) {
		this(SOAPHeader.createRequestHeader(WSEConstants.WSE_ACTION_UNSUBSCRIBE, communicationManagerId));
	}

	/**
	 * @param header
	 */
	public UnsubscribeMessage(SOAPHeader header) {
		super(header);
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.message.DPWSMessage#getType()
	 */
	public int getType() {
		return UNSUBSCRIBE_MESSAGE;
	}

}
