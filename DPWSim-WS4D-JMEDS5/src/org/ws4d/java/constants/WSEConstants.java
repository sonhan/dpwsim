/*******************************************************************************
 * Copyright (c) 2009 MATERNA Information & Communications. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html. For further
 * project-related information visit http://www.ws4d.org. The most recent
 * version of the JMEDS framework can be obtained from
 * http://sourceforge.net/projects/ws4d-javame.
 ******************************************************************************/

package org.ws4d.java.constants;

import org.ws4d.java.types.QName;

/**
 * Constants used by WS Eventing.
 */
public interface WSEConstants {

	/** The namespace name for WS Eventing. */
	public static final String	WSE_NAMESPACE_NAME								= "http://schemas.xmlsoap.org/ws/2004/08/eventing";

	/** The default prefix for the WSE namespace. */
	public static final String	WSE_NAMESPACE_PREFIX							= "wse";

	/* WS Eventing known actions. */
	public static final String	WSE_ACTION_SUBSCRIBE							= WSE_NAMESPACE_NAME + "/Subscribe";

	public static final String	WSE_ACTION_SUBSCRIBERESPONSE					= WSE_NAMESPACE_NAME + "/SubscribeResponse";

	public static final String	WSE_ACTION_UNSUBSCRIBE							= WSE_NAMESPACE_NAME + "/Unsubscribe";

	public static final String	WSE_ACTION_UNSUBSCRIBERESPONSE					= WSE_NAMESPACE_NAME + "/UnsubscribeResponse";

	public static final String	WSE_ACTION_RENEW								= WSE_NAMESPACE_NAME + "/Renew";

	public static final String	WSE_ACTION_RENEWRESPONSE						= WSE_NAMESPACE_NAME + "/RenewResponse";

	public static final String	WSE_ACTION_SUBSCRIPTIONEND						= WSE_NAMESPACE_NAME + "/SubscriptionEnd";

	public static final String	WSE_ACTION_GETSTATUS							= WSE_NAMESPACE_NAME + "/GetStatus";

	public static final String	WSE_ACTION_GETSTATUSRESPONSE					= WSE_NAMESPACE_NAME + "/GetStatusResponse";

	/** "EventSource". */
	public static final String	WSE_ATTR_EVENTSOURCE							= "EventSource";

	/** "Mode" within "Delivery". */
	public static final String	WSE_ATTR_DELIVERY_MODE							= "Mode";

	public static final String	WSE_ATTR_FILTER_DIALECT							= "Dialect";

	/* The elements tag names for WS Eventing */
	public static final String	WSE_ELEM_CODE									= "Code";

	public static final String	WSE_ELEM_DELIVERY								= "Delivery";

	/** "EndTo". */
	public static final String	WSE_ELEM_ENDTO									= "EndTo";

	public static final String	WSE_ELEM_EXPIRES								= "Expires";

	public static final String	WSE_ELEM_FILTER									= "Filter";

	public static final String	WSE_ELEM_GETSTATUS								= "GetStatus";

	public static final String	WSE_ELEM_GETSTATUSRESPONSE						= "GetStatusResponse";

	public static final String	WSE_ELEM_IDENTIFIER								= "Identifier";

	/** "NotifyTo". */
	public static final String	WSE_ELEM_NOTIFYTO								= "NotifyTo";

	public static final String	WSE_ELEM_REASON									= "Reason";

	public static final String	WSE_ELEM_RENEW									= "Renew";

	public static final String	WSE_ELEM_RENEWRESPONSE							= "RenewResponse";

	public static final String	WSE_ELEM_STATUS									= "Status";

	public static final String	WSE_ELEM_SUBSCRIBE								= "Subscribe";

	public static final String	WSE_ELEM_UNSUBSCRIBERESPONSE					= "UnsubscribeResponse";

	public static final String	WSE_ELEM_SUBSCRIBERESPONSE						= "SubscribeResponse";

	public static final String	WSE_ELEM_SUBSCRIPTIONEND						= "SubscriptionEnd";

	/** "SubscriptionManager". */
	public static final String	WSE_ELEM_SUBSCRIPTIONMANAGER					= "SubscriptionManager";

	public static final String	WSE_ELEM_UNSUBSCRIBE							= "Unsubscribe";

	public static final QName	WSE_QN_IDENTIFIER								= new QName(WSE_ELEM_IDENTIFIER, WSE_NAMESPACE_NAME, WSE_NAMESPACE_PREFIX);

	public static final QName	WSE_SUPPORTED_DELIVERY_MODE						= new QName("SupportedDeliveryMode", WSE_NAMESPACE_NAME, WSE_NAMESPACE_PREFIX);

	public static final QName	WSE_SUPPORTED_DIALECT							= new QName("SupportedDialect", WSE_NAMESPACE_NAME, WSE_NAMESPACE_PREFIX);

	/* delivery mode */
	public static final String	WSE_DELIVERY_MODE_PUSH							= WSE_NAMESPACE_NAME + "/DeliveryModes/Push";

	/* status codes */
	public static final String	WSE_STATUS_DELIVERY_FAILURE						= WSE_NAMESPACE_NAME + "/DeliveryFailure";

	public static final String	WSE_STATUS_SOURCE_SHUTTING_DOWN					= WSE_NAMESPACE_NAME + "/SourceShuttingDown";

	public static final String	WSE_STATUS_SOURCE_CANCELING						= WSE_NAMESPACE_NAME + "/SourceCanceling";

	/* faults */
	public static final QName	WSE_FAULT_FILTERING_NOT_SUPPORTED				= new QName("FilteringNotSupported", WSE_NAMESPACE_NAME, WSE_NAMESPACE_PREFIX);

	public static final QName	WSE_FAULT_FILTERING_REQUESTED_UNAVAILABLE		= new QName("FilteringRequestedUnavailable", WSE_NAMESPACE_NAME, WSE_NAMESPACE_PREFIX);

	public static final QName	WSE_FAULT_UNSUPPORTED_EXPIRATION_TYPE			= new QName("UnsupportedExpirationType", WSE_NAMESPACE_NAME, WSE_NAMESPACE_PREFIX);

	public static final QName	WSE_FAULT_DELIVERY_MODE_REQUESTED_UNVAILABLE	= new QName("DeliveryModeRequestedUnavailable", WSE_NAMESPACE_NAME, WSE_NAMESPACE_PREFIX);

	public static final QName	WSE_FAULT_INVALID_EXPIRATION_TIME				= new QName("InvalidExpirationTime", WSE_NAMESPACE_NAME, WSE_NAMESPACE_PREFIX);

	public static final QName	WSE_FAULT_INVALID_MESSAGE						= new QName("InvalidMessage", WSE_NAMESPACE_NAME, WSE_NAMESPACE_PREFIX);

	public static final QName	WSE_FAULT_EVENT_SOURCE_UNABLE_TO_PROCESS		= new QName("EventSourceUnableToProcess", WSE_NAMESPACE_NAME, WSE_NAMESPACE_PREFIX);

	public static final QName	WSE_FAULT_UNABLE_TO_RENEW						= new QName("UnableToRenew", WSE_NAMESPACE_NAME, WSE_NAMESPACE_PREFIX);

}
