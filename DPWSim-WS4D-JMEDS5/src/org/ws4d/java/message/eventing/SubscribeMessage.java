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
import org.ws4d.java.eventing.EventSink;
import org.ws4d.java.message.Message;
import org.ws4d.java.message.SOAPHeader;
import org.ws4d.java.types.Delivery;
import org.ws4d.java.types.EndpointReference;
import org.ws4d.java.types.Filter;
import org.ws4d.java.types.URI;
import org.ws4d.java.util.StringUtil;

public class SubscribeMessage extends Message {

	public static final URI		ACTION	= new URI(WSEConstants.WSE_ACTION_SUBSCRIBE);

	private EndpointReference	endTo;

	private Delivery			delivery;

	private String				expires;

	private Filter				filter;

	private EventSink			eventSink;

	/**
	 * Creates a new Subscribe message containing a {@link SOAPHeader} with the
	 * appropriate {@link SOAPHeader#getAction() action property} set and a
	 * unique {@link SOAPHeader#getMessageId() message ID property}. All other
	 * header- and eventing-related fields are empty and it is the caller's
	 * responsibility to fill them with suitable values.
	 */
	public SubscribeMessage(String communicationManagerId) {
		this(SOAPHeader.createRequestHeader(WSEConstants.WSE_ACTION_SUBSCRIBE, communicationManagerId));
	}

	/**
	 * @param header
	 */
	public SubscribeMessage(SOAPHeader header) {
		this(header, null);
	}

	/**
	 * @param header
	 * @param delivery
	 */
	public SubscribeMessage(SOAPHeader header, Delivery delivery) {
		super(header);
		this.delivery = delivery;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		StringBuffer sb = new StringBuffer(StringUtil.formatClassName(getClass()));
		sb.append(" [ header=").append(header);
		sb.append(", inbound=").append(inbound);
		sb.append(", endTo=").append(endTo);
		sb.append(", delivery=").append(delivery);
		sb.append(", expires=").append(expires);
		sb.append(", filter=").append(filter);
		sb.append(" ]");
		return sb.toString();
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.message.DPWSMessage#getType()
	 */
	public int getType() {
		return SUBSCRIBE_MESSAGE;
	}

	public Delivery getDelivery() {
		return delivery;
	}

	public EndpointReference getEndTo() {
		return endTo;
	}

	public Filter getFilter() {
		return filter;
	}

	/**
	 * @param endTo the endTo to set
	 */
	public void setEndTo(EndpointReference endTo) {
		this.endTo = endTo;
	}

	/**
	 * @param delivery the delivery to set
	 */
	public void setDelivery(Delivery delivery) {
		this.delivery = delivery;
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

	/**
	 * @param filter the filter to set
	 */
	public void setFilter(Filter filter) {
		this.filter = filter;
	}

	/**
	 * @param eventSink
	 * @return
	 */
	public void setEventSink(EventSink eventSink) {
		this.eventSink = eventSink;
	}

	/**
	 * @return
	 */
	public EventSink getEventSink() {
		return eventSink;
	}
}
