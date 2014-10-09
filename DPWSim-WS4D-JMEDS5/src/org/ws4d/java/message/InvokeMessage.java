/*******************************************************************************
 * Copyright (c) 2009 MATERNA Information & Communications. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html. For further
 * project-related information visit http://www.ws4d.org. The most recent
 * version of the JMEDS framework can be obtained from
 * http://sourceforge.net/projects/ws4d-javame.
 ******************************************************************************/
package org.ws4d.java.message;

import org.ws4d.java.service.parameter.ParameterValue;
import org.ws4d.java.util.StringUtil;

public class InvokeMessage extends Message {

	/** object representation of the content of the message's body */
	private ParameterValue	content	= null;

	/**
	 * Creates a new InvokeInput message containing a {@link SOAPHeader} with a
	 * {@link SOAPHeader#getAction() action property} set to the value of
	 * argument <code>inputAction</code> and a unique
	 * {@link SOAPHeader#getMessageId() message ID property}. All other header-
	 * fields are empty and it is the caller's responsibility to fill them with
	 * suitable values.
	 */
	public InvokeMessage(String action, String communicationManagerId) {
		this(action, true, communicationManagerId);
	}

	/**
	 * Creates a new InvokeInput message containing a {@link SOAPHeader} with a
	 * {@link SOAPHeader#getAction() action property} set to the value of the
	 * argument <code>inputAction</code>. If argument <code>request</code> is
	 * <code>true</code> a unique {@link SOAPHeader#getMessageId() message ID
	 * property} is set too. All other header- fields are empty and it is the
	 * caller's responsibility to fill them with suitable values.
	 */
	public InvokeMessage(String action, boolean request, String communicationManagerId) {
		this(request ? SOAPHeader.createRequestHeader(action, communicationManagerId) : SOAPHeader.createHeader(action, communicationManagerId));
	}

	public InvokeMessage(SOAPHeader header) {
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
		sb.append(", content=").append(content);
		sb.append(" ]");
		return sb.toString();
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.message.Message#getType()
	 */
	public int getType() {
		return INVOKE_MESSAGE;
	}

	/**
	 * Gets the content of the body of this message. The {@link ParameterValue}
	 * is the object representation of the content of the body of the message.
	 * 
	 * @return the content of this message
	 */
	public ParameterValue getContent() {
		return content;
	}

	/**
	 * Sets content of the message body. The {@link ParameterValue} is the
	 * object representation of the content of the message body.
	 * 
	 * @param content the content of this message
	 */
	public void setContent(ParameterValue content) {
		this.content = content;
	}
}
