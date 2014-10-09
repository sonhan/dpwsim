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
import org.ws4d.java.types.EndpointReference;
import org.ws4d.java.types.URI;
import org.ws4d.java.util.StringUtil;

public class SubscribeResponseMessage extends EventingResponseMessage {

	public static final URI		ACTION	= new URI(WSEConstants.WSE_ACTION_SUBSCRIBERESPONSE);

	private EndpointReference	subscriptionManager;

	/**
	 * Creates a new SubscribeResponse message containing a {@link SOAPHeader}
	 * with the appropriate {@link SOAPHeader#getAction() action property} set.
	 * All other header- and eventing-related fields are empty and it is the
	 * caller's responsibility to fill them with suitable values.
	 */
	public SubscribeResponseMessage(String communicationManagerId) {
		this(SOAPHeader.createHeader(WSEConstants.WSE_ACTION_SUBSCRIBERESPONSE, communicationManagerId));
	}

	/**
	 * @param header
	 */
	public SubscribeResponseMessage(SOAPHeader header) {
		this(header, null, null);
	}

	/**
	 * @param header
	 * @param subscriptionManager
	 * @param expires
	 */
	public SubscribeResponseMessage(SOAPHeader header, EndpointReference subscriptionManager, String expires) {
		super(header);
		this.subscriptionManager = subscriptionManager;
		this.expires = expires;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		StringBuffer sb = new StringBuffer(StringUtil.formatClassName(getClass()));
		sb.append(" [ header=").append(header);
		sb.append(", inbound=").append(inbound);
		sb.append(", subscriptionManager=").append(subscriptionManager);
		sb.append(", expires=").append(expires);
		sb.append(" ]");
		return sb.toString();
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.message.DPWSMessage#getType()
	 */
	public int getType() {
		return SUBSCRIBE_RESPONSE_MESSAGE;
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.message.eventing.SubscribeResponseMessage
	 * #getSubscriptionManager()
	 */
	public EndpointReference getSubscriptionManager() {
		return subscriptionManager;
	}

	/**
	 * @param subscriptionManager the subscriptionManager to set
	 */
	public void setSubscriptionManager(EndpointReference subscriptionManager) {
		this.subscriptionManager = subscriptionManager;
	}

}
