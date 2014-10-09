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
import org.ws4d.java.types.EndpointReference;
import org.ws4d.java.types.LocalizedString;
import org.ws4d.java.types.URI;
import org.ws4d.java.util.StringUtil;

/*
 * <?xml version='1.0' encoding='UTF-8' ?> <s12:Envelope
 * xmlns:s12="http://www.w3.org/2003/05/soap-envelope"
 * xmlns:wsa="http://schemas.xmlsoap.org/ws/2004/08/addressing"> <s12:Header>
 * <wsa:MessageID>urn:uuid:c2bbca30-c79e-11dd-bf38-c51032e39693</wsa:MessageID>
 * <
 * wsa:Action>http://schemas.xmlsoap.org/ws/2004/08/eventing/SubscriptionEnd</wsa
 * :Action>
 * <wsa:To>http://schemas.xmlsoap.org/ws/2004/08/addressing/role/anonymous
 * </wsa:To> </s12:Header> <s12:Body> <wse:SubscriptionEnd
 * xmlns:wse="http://schemas.xmlsoap.org/ws/2004/08/eventing">
 * <wse:SubscriptionManager>
 * <wsa:Address>http://139.2.58.102:54805/ac3de2c0-c79e
 * -11dd-bf2a-c51032e39600/DeviceAdmin</wsa:Address> <wsa:ReferenceParameters>
 * <wse
 * :Identifier>urn:uuid:c04fd340-c79e-11dd-bf35-c51032e39693</wse:Identifier>
 * </wsa:ReferenceParameters> </wse:SubscriptionManager>
 * <wse:Status>SourceShuttingDown</wse:Status> <wse:Reason lang="de-DE">Geraet
 * wird abgeschaltet.</wse:Reason> <wse:Reason lang="en-US">Device is shutting
 * down.</wse:Reason> </wse:SubscriptionEnd> </s12:Body> </s12:Envelope>
 */

public class SubscriptionEndMessage extends Message {

	public static final URI		ACTION						= new URI(WSEConstants.WSE_ACTION_SUBSCRIPTIONEND);

	public static final URI		DELIVERY_FAILURE_STATUS		= new URI(WSEConstants.WSE_STATUS_DELIVERY_FAILURE);

	public static final URI		SOURCE_SHUTTING_DOWN_STATUS	= new URI(WSEConstants.WSE_STATUS_SOURCE_SHUTTING_DOWN);

	public static final URI		SOURCE_CANCELLING_STATUS	= new URI(WSEConstants.WSE_STATUS_SOURCE_CANCELING);

	private EndpointReference	subscriptionManager;

	private URI					status;

	// is this rather a list of LocalizedStrings???
	private LocalizedString		reason;

	/**
	 * Creates a new SubscriptionEnd message containing a {@link SOAPHeader}
	 * with the appropriate {@link SOAPHeader#getAction() action property} set.
	 * All other header- and eventing-related fields are empty and it is the
	 * caller's responsibility to fill them with suitable values.
	 */
	public SubscriptionEndMessage(String communicationManagerId) {
		this(SOAPHeader.createHeader(WSEConstants.WSE_ACTION_SUBSCRIPTIONEND, communicationManagerId));
	}

	/**
	 * @param header
	 */
	public SubscriptionEndMessage(SOAPHeader header) {
		this(header, null, null);
	}

	/**
	 * @param header
	 * @param subscriptionManager
	 * @param status
	 */
	public SubscriptionEndMessage(SOAPHeader header, EndpointReference subscriptionManager, URI status) {
		super(header);
		this.subscriptionManager = subscriptionManager;
		this.status = status;
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
		sb.append(", status=").append(status);
		sb.append(", reason=").append(reason);
		sb.append(" ]");
		return sb.toString();
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.message.DPWSMessage#getType()
	 */
	public int getType() {
		return SUBSCRIPTION_END_MESSAGE;
	}

	/*
	 * (non-Javadoc)
	 * @seeorg.ws4d.java.communication.message.eventing.SubscriptionEndMessage#
	 * getReasons()
	 */
	public LocalizedString getReason() {
		return reason;
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.message.eventing.SubscriptionEndMessage#getStatus ()
	 */
	public URI getStatus() {
		return status;
	}

	/*
	 * (non-Javadoc)
	 * @seeorg.ws4d.java.communication.message.eventing.SubscriptionEndMessage#
	 * getSubscriptionManager()
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

	/**
	 * @param status the status to set
	 */
	public void setStatus(URI status) {
		this.status = status;
	}

	/**
	 * @param reason the reason to set
	 */
	public void setReason(LocalizedString reason) {
		this.reason = reason;
	}
}
