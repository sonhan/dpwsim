/*******************************************************************************
 * Copyright (c) 2009 MATERNA Information & Communications. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html. For further
 * project-related information visit http://www.ws4d.org. The most recent
 * version of the JMEDS framework can be obtained from
 * http://sourceforge.net/projects/ws4d-javame.
 ******************************************************************************/
package org.ws4d.java.service;

import org.ws4d.java.communication.ProtocolInfo;
import org.ws4d.java.constants.WSEConstants;
import org.ws4d.java.eventing.EventSink;
import org.ws4d.java.eventing.EventingException;
import org.ws4d.java.message.Message;
import org.ws4d.java.message.SOAPException;
import org.ws4d.java.schema.SchemaUtil;
import org.ws4d.java.structures.EmptyStructures;
import org.ws4d.java.structures.Iterator;
import org.ws4d.java.structures.ReadOnlyIterator;
import org.ws4d.java.types.EndpointReference;
import org.ws4d.java.types.EprInfo;
import org.ws4d.java.types.LocalizedString;
import org.ws4d.java.types.URI;
import org.ws4d.java.types.URISet;

/**
 * Instances of this class hold essential information about an event
 * subscription as seen from the server-side. These include the
 * {@link #getNotifyTo() endpoint reference} to which events are to be
 * delivered, the {@link #getFilterActions() actions} the subscription
 * addresses, its {@link #getExpirationTime() expiration time} and the optional
 * endpoint to which the server-initiated end of the subscription should be
 * announced.
 */
public class ServiceSubscription {

	private static final long	MAX_EXPIRATION_DURATION					= SchemaUtil.MILLIS_PER_YEAR;

	private static final String	FAULT_REASON_INVALID_EXIRATION_TIME		= "The expiration time requested is invalid.";

	private static final String	FAULT_REASON_UNSUPPORTED_EXIRATION_TYPE	= "Only expiration durations are supported.";

	// -------------- VAR ------------------

	// milliseconds from the epoch
	long						expirationTime;																		// in

	/** remote client subscription id */
	EprInfo		notifyTo;

	EprInfo		endTo;

	String		communicationManagerId;

	URISet						filterActions;

	/** Local event sink needed for local client */
	EventSink					sink									= null;

	/** Local client subscription */
	String						clientSubscriptionId					= null;

	EndpointReference			subscriptionManager;

	final ProtocolInfo			pInfo;

	public ServiceSubscription(ProtocolInfo pInfo) {
		this.pInfo = pInfo;
	}

	void setExpiration(String expires, Message msg) throws SOAPException {
		if (expires == null || (expires = expires.trim()).length() == 0) {
			expirationTime = System.currentTimeMillis() + MAX_EXPIRATION_DURATION;
			return;
		}
		if (expires.charAt(0) == 'P') {
			// _positive_ duration
			long duration = SchemaUtil.parseDuration(expires);
			if (duration <= 0L) {
				// Fault wse:InvalidExpirationTime
				throw DefaultSubscriptionManager.createFault(msg, WSEConstants.WSE_FAULT_INVALID_EXPIRATION_TIME, new LocalizedString(FAULT_REASON_INVALID_EXIRATION_TIME, LocalizedString.DEFAULT_LANG));
			}
			expirationTime = System.currentTimeMillis() + duration;
		} else {
			// we currently don't support dateTime
			// long expTime = parseDateTime(expires);
			// if (expTime <= System.currentTimeMillis()) {
			// return false;
			// }
			// expirationTime = expTime;
			// Fault wse:UnsupportedExpirationType
			// TODO add supported expiration types within fault detail
			throw DefaultSubscriptionManager.createFault(msg, WSEConstants.WSE_FAULT_UNSUPPORTED_EXPIRATION_TYPE, new LocalizedString(FAULT_REASON_UNSUPPORTED_EXIRATION_TYPE, LocalizedString.DEFAULT_LANG));
		}
	}

	/**
	 * Sets expiration for local client subscription.
	 * 
	 * @param duration
	 */
	void setExpiration(long duration) throws EventingException {
		if (duration == 0) {
			expirationTime = System.currentTimeMillis() + MAX_EXPIRATION_DURATION;
			return;
		} else if (duration > 0) {
			expirationTime = System.currentTimeMillis() + duration;
		} else {
			/*
			 * negative duration, throw exception
			 */
			throw new EventingException(WSEConstants.WSE_FAULT_INVALID_EXPIRATION_TIME, FAULT_REASON_INVALID_EXIRATION_TIME);
		}
	}

	/**
	 * Sets EPR of subscription manager for this service subscription instance.
	 * 
	 * @param subscriptionManager
	 */
	void setSubscriptionManager(EndpointReference subscriptionManager) {
		this.subscriptionManager = subscriptionManager;
	}

	/**
	 * Returns the expiration time of this subscription in milliseconds from the
	 * epoch.
	 * 
	 * @return the expiration time
	 */
	public long getExpirationTime() {
		return expirationTime;
	}

	/**
	 * Returns the EprInfo to which notifications matching this
	 * subscription shall be sent.
	 * <P>
	 * Be aware that the xAddress of the returned <code>EprInfo</code> may be
	 * <code>null</code> if the endpoint reference is not a transport address
	 * </P>
	 * @return the <code>EprInfo</code> to which to send notifications
	 */
	public EprInfo getNotifyTo() {
		return notifyTo;
	}

	/**
	 * Returns the (optional) EprInfo to which a server-side
	 * cancellation of the subscription should be announced.
	 * <P>
	 * Be aware that the xAddress of the returned <code>EprInfo</code> may be
	 * <code>null</code> if the endpoint reference is not a transport address
	 * </P>
	 * @return the <code>EprInfo</code> to which to send a subscription-end
	 *         announcement
	 */
	public EprInfo getEndTo() {
		return endTo;
	}

	/**
	 * Returns the ID of the protocol to communicate over with the client (aka.
	 * event sink) for this subscription. In other words, this is the same
	 * protocol to use when sending messages to either one of the
	 * {@link #getNotifyTo() notify-to} or {@link #getEndTo() end-to} addresses.
	 * 
	 * @return the ID of the protocol to use for communication with the
	 *         subscriber of this subscription
	 */
	public String getCommunicationManagerID() {
		return communicationManagerId;
	}

	/**
	 * Returns a read-only iterator over the set of {@link URI action URIs} to
	 * which this subscription refers. This method never returns
	 * <code>null</code>, it will instead return an empty iterator in the case
	 * where no filter actions are available.
	 * 
	 * @return an iterator over {@link URI} instances representing the actions
	 *         to which this subscription refers
	 */
	public Iterator getFilterActions() {
		return filterActions == null ? EmptyStructures.EMPTY_ITERATOR : new ReadOnlyIterator(filterActions.iterator());
	}

	/**
	 * Returns the EPR of the subscription manager governing the state of this
	 * service subscription. Usually, this EPR includes - in addition to the
	 * manager's address - a server-side identifier (e.g. wse:Identifier from
	 * WS-Eventing) for this subscription instance.
	 * 
	 * @return the endpoint reference of the subscription manager for this
	 *         subscription
	 */
	public EndpointReference getSubscriptionManager() {
		return subscriptionManager;
	}

	public ProtocolInfo getProtocolInfo() {
		return pInfo;
	}

}
