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
import org.ws4d.java.util.StringUtil;

public class RenewMessage extends Message {

	public static final URI	ACTION	= new URI(WSEConstants.WSE_ACTION_RENEW);

	private String			expires;

	/**
	 * Creates a new Renew message containing a {@link SOAPHeader} with the
	 * appropriate {@link SOAPHeader#getAction() action property} set and a
	 * unique {@link SOAPHeader#getMessageId() message ID property}. All other
	 * header- and eventing-related fields are empty and it is the caller's
	 * responsibility to fill them with suitable values.
	 */
	public RenewMessage(String communicationManagerId) {
		this(SOAPHeader.createRequestHeader(WSEConstants.WSE_ACTION_RENEW, communicationManagerId));
	}

	/**
	 * @param header
	 */
	public RenewMessage(SOAPHeader header) {
		super(header);
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		StringBuffer sb = new StringBuffer(StringUtil.formatClassName(getClass()));
		sb.append(" [ header=").append(header);
		sb.append(", inbound=").append(inbound);
		sb.append(", expires=").append(expires);
		sb.append(" ]");
		return sb.toString();
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.message.DPWSMessage#getType()
	 */
	public int getType() {
		return RENEW_MESSAGE;
	}

	/**
	 * @return the expires
	 */
	public String getExpires() {
		return expires;
	}

	/**
	 * @param expires the expires to set
	 */
	public void setExpires(String expires) {
		this.expires = expires;
	}
}
