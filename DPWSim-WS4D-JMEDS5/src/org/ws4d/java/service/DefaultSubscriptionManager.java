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

import org.ws4d.java.DPWSFramework;
import org.ws4d.java.communication.CommunicationManager;
import org.ws4d.java.communication.CommunicationManagerRegistry;
import org.ws4d.java.communication.CommunicationUtil;
import org.ws4d.java.communication.ProtocolData;
import org.ws4d.java.communication.TimeoutException;
import org.ws4d.java.configuration.FrameworkProperties;
import org.ws4d.java.constants.ConstantsHelper;
import org.ws4d.java.constants.SOAPConstants;
import org.ws4d.java.constants.WSAConstants;
import org.ws4d.java.constants.WSEConstants;
import org.ws4d.java.dispatch.OutDispatcher;
import org.ws4d.java.eventing.ClientSubscription;
import org.ws4d.java.eventing.EventSink;
import org.ws4d.java.eventing.EventingException;
import org.ws4d.java.eventing.SubscriptionManager;
import org.ws4d.java.message.FaultMessage;
import org.ws4d.java.message.Message;
import org.ws4d.java.message.SOAPException;
import org.ws4d.java.message.eventing.GetStatusMessage;
import org.ws4d.java.message.eventing.GetStatusResponseMessage;
import org.ws4d.java.message.eventing.RenewMessage;
import org.ws4d.java.message.eventing.RenewResponseMessage;
import org.ws4d.java.message.eventing.SubscribeMessage;
import org.ws4d.java.message.eventing.SubscribeResponseMessage;
import org.ws4d.java.message.eventing.SubscriptionEndMessage;
import org.ws4d.java.message.eventing.UnsubscribeMessage;
import org.ws4d.java.message.eventing.UnsubscribeResponseMessage;
import org.ws4d.java.schema.SchemaUtil;
import org.ws4d.java.structures.HashMap.Entry;
import org.ws4d.java.structures.Iterator;
import org.ws4d.java.structures.LockedMap;
import org.ws4d.java.types.Delivery;
import org.ws4d.java.types.EndpointReference;
import org.ws4d.java.types.EprInfo;
import org.ws4d.java.types.Filter;
import org.ws4d.java.types.LocalizedString;
import org.ws4d.java.types.QName;
import org.ws4d.java.types.ReferenceParametersMData;
import org.ws4d.java.types.URI;
import org.ws4d.java.types.URISet;
import org.ws4d.java.util.IDGenerator;
import org.ws4d.java.util.TimedEntry;
import org.ws4d.java.util.WatchDog;

/**
 * 
 */
public class DefaultSubscriptionManager implements SubscriptionManager {

	private static final String	FAULT_REASON_DELIVERY_MODE						= "The requested delivery mode is not supported.";

	private static final String	FAULT_REASON_FILTERING_DIALECT					= "The requested filter dialect is not supported.";

	private static final String	FAULT_REASON_FILTER_ACTION_NOT_SUPPORTED		= "No notifications match the supplied filter.";

	private static final String	FAULT_REASON_INVALID_MESSAGE					= "The message is not valid and cannot be processed.";

	private static final String	FAULT_REASON_UNABLE_TO_RENEW__NO_SUBSCRIPTION	= "No such subscription";

	private static final String	EVENT_SOURCE_SHUTTING_DOWN						= "Event source shutting down.";

	private static final long	REMOVAL_POLL_INTERVAL							= SchemaUtil.MILLIS_PER_MINUTE;

	/*
	 * key = wse:Identifier (as uuid: URN), value = service subscription entry
	 * instance
	 */
	private final LockedMap		subscriptions									= new LockedMap();

	/** this subscription manager is associated to this service */
	private final LocalService	service;

	public DefaultSubscriptionManager(LocalService service) {
		super();

		this.service = service;
		TimedEntry entry = new TimedEntry() {

			protected void timedOut() {
				cleanUpSubscriptions();
				WatchDog.getInstance().register(this, REMOVAL_POLL_INTERVAL);
			}
		};
		WatchDog.getInstance().register(entry, REMOVAL_POLL_INTERVAL);
	}

	/**
	 * @param msg
	 * @param subcode
	 * @param reason
	 * @return
	 */
	static SOAPException createFault(Message msg, QName subcode, LocalizedString reason) {
		return createFault(msg, SOAPConstants.SOAP_FAULT_SENDER, subcode, reason);
	}

	/**
	 * @param msg
	 * @param subcode
	 * @param reason
	 * @return SOAPException
	 */
	static SOAPException createFault(Message msg, QName code, QName subcode, LocalizedString reason) {
		FaultMessage fault = new FaultMessage(WSAConstants.WSA_ACTION_ADDRESSING_FAULT, CommunicationManager.ID_NULL);
		fault.setResponseTo(msg);
		fault.setCode(code);
		fault.setSubcode(subcode);
		fault.addReason(reason);
		fault.setResponseTo(msg);
		return new SOAPException(fault);
	}

	static SOAPException createDeliveryModeUnavailableFault(Message msg) {
		return createFault(msg, WSEConstants.WSE_FAULT_DELIVERY_MODE_REQUESTED_UNVAILABLE, new LocalizedString(FAULT_REASON_DELIVERY_MODE, LocalizedString.LANGUAGE_EN));
	}

	static SOAPException createInvalidMessageFault(Message msg) {
		return createFault(msg, WSEConstants.WSE_FAULT_INVALID_MESSAGE, new LocalizedString(FAULT_REASON_INVALID_MESSAGE, LocalizedString.LANGUAGE_EN));
	}

	static SOAPException createUnableToRenew(Message msg) {
		return createFault(msg, SOAPConstants.SOAP_FAULT_RECEIVER, WSEConstants.WSE_FAULT_UNABLE_TO_RENEW, new LocalizedString(FAULT_REASON_UNABLE_TO_RENEW__NO_SUBSCRIPTION, LocalizedString.LANGUAGE_EN));
	}

	static EndpointReference createSubscriptionManager(URI address, String wseIdentifier) {
		ReferenceParametersMData parameters = new ReferenceParametersMData();
		parameters.setWseIdentifier(wseIdentifier);
		return new EndpointReference(address, parameters);
	}

	private void cleanUpSubscriptions() {
		subscriptions.exclusiveLock();
		try {
			long now = System.currentTimeMillis();
			for (Iterator it = subscriptions.entrySet().iterator(); it.hasNext();) {
				Entry ent = (Entry) it.next();
				ServiceSubscription subscription = (ServiceSubscription) ent.getValue();
				if (subscription.expirationTime <= now) {
					// OutDispatcher.getInstance().send(subscriptionEnd(subscription));
					it.remove();
					removeSubscriptionFromEventSources(subscription);
				}
			}
		} finally {
			subscriptions.releaseExclusiveLock();
		}
	}

	/**
	 * Removes the subscription from each subscribed evented operation.
	 * 
	 * @param subscription subscription to from operations.
	 */
	private void removeSubscriptionFromEventSources(ServiceSubscription subscription) {
		for (Iterator it = subscription.filterActions.iterator(); it.hasNext();) {
			String action = ((URI) it.next()).toString();
			DefaultEventSource ev = (DefaultEventSource) service.getEventSource(action);

			if (ev != null) {
				ev.removeSubscription(subscription);
			}
		}
	}

	/**
	 * Adds service subscription to each matching operation with matching action
	 * uri.
	 * 
	 * @param subscription service subscription to add.
	 * @return true if at least one action matches an evented operation.
	 */
	private boolean addSubscriptionToEventSource(ServiceSubscription subscription) {
		boolean hasMatchingAction = false;
		URISet actions = subscription.filterActions;

		for (Iterator it = actions.iterator(); it.hasNext();) {
			/*
			 * Add the subscription to each evented operation
			 */
			String action = ((URI) it.next()).toString();
			DefaultEventSource ev = (DefaultEventSource) service.getEventSource(action);

			if (ev != null) {
				ev.addSubscription(subscription);
				hasMatchingAction = true;
			}
		}
		return hasMatchingAction;
	}

	/**
	 * TODO
	 * 
	 * @param subscription
	 * @return
	 */
	private SubscriptionEndMessage subscriptionEnd(ServiceSubscription subscription, URI status, String reason) {
		SubscriptionEndMessage subscriptionEndMessage = new SubscriptionEndMessage(subscription.getCommunicationManagerID());
		// set to preferred xAddress of client / event sink
		subscriptionEndMessage.setTargetXAddressInfo(subscription.endTo);
		subscriptionEndMessage.getHeader().setEndpointReference(subscription.endTo.getEndpointReference());
		subscriptionEndMessage.setStatus(status);
		subscriptionEndMessage.setReason(new LocalizedString(reason, LocalizedString.DEFAULT_LANG));
		subscriptionEndMessage.setSubscriptionManager(subscription.getSubscriptionManager());
		return subscriptionEndMessage;
	}

	// ------------------PUBLIC SUBSCRIPTION MANAGEMENT -------------------

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.message.eventing.SubscriptionManager#subscribe
	 * (org.ws4d.java.message.eventing.SubscribeMessage,
	 * org.ws4d.java.communication.ProtocolData)
	 */
	public SubscribeResponseMessage subscribe(SubscribeMessage msg, ProtocolData protocolData) throws SOAPException {
		Delivery delivery = msg.getDelivery();
		if (delivery == null) {
			// Fault wse:DeliveryModeRequestedUnavailable
			throw createDeliveryModeUnavailableFault(msg);
		}
		URI mode = delivery.getMode();
		if (mode == null || !WSEConstants.WSE_DELIVERY_MODE_PUSH.equals(mode.toString())) {
			// Fault wse:DeliveryModeRequestedUnavailable
			throw createDeliveryModeUnavailableFault(msg);
		}
		ServiceSubscription subscription = new ServiceSubscription(protocolData.getProtocolInfo());
		subscription.notifyTo = new EprInfo(delivery.getNotifyTo(), protocolData.getCommunicationManagerId());
		subscription.notifyTo.mergeProtocolInfo(protocolData.getProtocolInfo());
		if (msg.getEndTo() != null) {
			subscription.endTo = new EprInfo(msg.getEndTo(), protocolData.getCommunicationManagerId());
			subscription.endTo.mergeProtocolInfo(protocolData.getProtocolInfo());
		} else {
			subscription.endTo = null;
		}
		subscription.communicationManagerId = protocolData.getCommunicationManagerId();

		CommunicationManager comMan = DPWSFramework.getCommunicationManager(protocolData.getCommunicationManagerId());
		CommunicationUtil comUtil = comMan.getCommunicationUtil();

		ConstantsHelper helper = comUtil.getHelper(msg.getProtocolInfo().getVersion());

		Filter filter = msg.getFilter();

		if (filter != null) {
			URI dialect = filter.getDialect();
			if (dialect == null) {
				// Fault wse:FilteringRequestedUnavailable
				throw createFault(msg, SOAPConstants.SOAP_FAULT_SENDER, WSEConstants.WSE_FAULT_FILTERING_REQUESTED_UNAVAILABLE, new LocalizedString(FAULT_REASON_FILTERING_DIALECT, LocalizedString.DEFAULT_LANG));
			}

			if (helper.getDPWSFilterEventingAction().equals(dialect.toString())) {

				subscription.filterActions = filter.getActions();
				boolean hasMatchingAction = addSubscriptionToEventSource(subscription);

				if (!hasMatchingAction) {

					/*
					 * Fault dpws:FilterActionNotSupported
					 */
					FaultMessage fault = new FaultMessage(helper.getDPWSActionFault(), protocolData.getCommunicationManagerId());
					fault.setResponseTo(msg);
					fault.setCode(SOAPConstants.SOAP_FAULT_SENDER);
					fault.setSubcode(helper.getDPWSFaultFilterActionNotSupported());
					fault.addReason(new LocalizedString(FAULT_REASON_FILTER_ACTION_NOT_SUPPORTED, LocalizedString.DEFAULT_LANG));
					throw new SOAPException(fault);

				}
			}
			// XXX ok to add subscription without filter?
			subscription.setExpiration(msg.getExpires(), msg);

			/*
			 * create subscribe response message
			 */
			SubscribeResponseMessage response = new SubscribeResponseMessage(protocolData.getCommunicationManagerId());
			response.setResponseTo(msg);

			// set DPWSVersion from the Request to the Response
			response.setProtocolInfo(msg.getProtocolInfo());

			URI to = msg.getTo();
			String wseIdentifier = IDGenerator.URI_UUID_PREFIX + IDGenerator.getUUID();
			EndpointReference subscriptionManager;
			if (FrameworkProperties.REFERENCE_PARAM_MODE) {
				subscriptionManager = createSubscriptionManager(to, wseIdentifier);
			} else {
				to.setFragmentEncoded(wseIdentifier);
				subscriptionManager = new EndpointReference(to);
			}
			subscription.setSubscriptionManager(subscriptionManager);
			response.setSubscriptionManager(subscriptionManager);
			response.setExpires(SchemaUtil.createDuration(subscription.expirationTime - System.currentTimeMillis()));
			subscriptions.exclusiveLock();
			try {
				subscriptions.put(wseIdentifier, subscription);
			} finally {
				subscriptions.releaseExclusiveLock();
			}
			return response;
		} else { // Fault wse:FilteringRequestedUnavailable
			throw createFault(msg, SOAPConstants.SOAP_FAULT_SENDER, WSEConstants.WSE_FAULT_FILTERING_REQUESTED_UNAVAILABLE, new LocalizedString(FAULT_REASON_FILTERING_DIALECT, LocalizedString.DEFAULT_LANG));
		}

	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.ws4d.java.service.SubscriptionManagerInterface#subscribe(org.ws4d
	 * .java.eventing.EventSink, java.lang.String,
	 * org.ws4d.java.types.uri.URISet, long)
	 */
	public ClientSubscription subscribe(EventSink sink, String clientSubscriptionId, URISet eventActionURIs, long duration) throws EventingException {

		ServiceSubscription entry = new ServiceSubscription(null);
		entry.filterActions = eventActionURIs;
		entry.sink = sink;
		entry.clientSubscriptionId = clientSubscriptionId;
		entry.setExpiration(duration);
		entry.communicationManagerId = CommunicationManagerRegistry.getDefault();

		boolean hasMatchingAction = addSubscriptionToEventSource(entry);

		if (!hasMatchingAction) {
			/*
			 * Fault dpws:FilterActionNotSupported
			 */
			CommunicationManager comMan = DPWSFramework.getCommunicationManager(entry.getCommunicationManagerID());
			CommunicationUtil comUtil = comMan.getCommunicationUtil();

			Iterator it = comMan.getSupportedVersions().iterator();
			QName subcode = (it.hasNext()) ? comUtil.getHelper(((Integer)it.next()).intValue()).getDPWSFaultFilterActionNotSupported() : new QName(null);
			throw new EventingException(subcode, FAULT_REASON_FILTER_ACTION_NOT_SUPPORTED);
		}

		String wseIdentifier = IDGenerator.URI_UUID_PREFIX + IDGenerator.getUUID();
		subscriptions.exclusiveLock();
		try {
			subscriptions.put(wseIdentifier, entry);
		} finally {
			subscriptions.releaseExclusiveLock();
		}

		/*
		 * Create client subscription
		 */
		Iterator serviceEprs = service.getEprInfos();
		EprInfo eprInfo = (EprInfo) serviceEprs.next();
		URI serviceUri = eprInfo.getEndpointReference().getAddress();
		return new DefaultClientSubscription(sink, clientSubscriptionId, createSubscriptionManager(serviceUri, wseIdentifier), entry.getCommunicationManagerID(), duration, service.getServiceReference());
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.message.eventing.SubscriptionManager#unsubscribe
	 * (org.ws4d.java.message.eventing.UnsubscribeMessage,
	 * org.ws4d.java.communication.ProtocolData)
	 */
	public UnsubscribeResponseMessage unsubscribe(UnsubscribeMessage msg, ProtocolData protocolData) throws SOAPException {
		String wseIdentifier = msg.getHeader().getWseIdentifier();
		if (wseIdentifier == null) {
			// Fault wse:InvalidMessage
			throw createInvalidMessageFault(msg);
		}
		ServiceSubscription subscription = null;
		subscriptions.exclusiveLock();
		try {
			subscription = (ServiceSubscription) subscriptions.remove(wseIdentifier);
		} finally {
			subscriptions.releaseExclusiveLock();
		}

		if (subscription == null) {
			// Fault wse:InvalidMessage
			throw createInvalidMessageFault(msg);
		}
		removeSubscriptionFromEventSources(subscription);
		UnsubscribeResponseMessage response = new UnsubscribeResponseMessage(protocolData.getCommunicationManagerId());
		response.setResponseTo(msg);

		// set DPWSVersion from the Request to the Response
		response.setProtocolInfo(msg.getProtocolInfo());

		return response;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.ws4d.java.service.SubscriptionManagerInterface#unsubscribe(org.ws4d
	 * .java.eventing.ClientSubscription)
	 */
	public void unsubscribe(ClientSubscription subscription) throws EventingException {
		String wseIdentifier = subscription.getServiceSubscriptionId();

		ServiceSubscription serviceSubscription = null;

		subscriptions.exclusiveLock();
		try {
			serviceSubscription = (ServiceSubscription) subscriptions.remove(wseIdentifier);
		} finally {
			subscriptions.releaseExclusiveLock();
		}
		if (serviceSubscription == null) {
			/*
			 * Fault wse:InvalidMessage
			 */
			throw new EventingException(WSEConstants.WSE_FAULT_INVALID_MESSAGE, FAULT_REASON_INVALID_MESSAGE);
		}
		removeSubscriptionFromEventSources(serviceSubscription);
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.message.eventing.SubscriptionManager#renew
	 * (org.ws4d.java.message.eventing.RenewMessage,
	 * org.ws4d.java.communication.ProtocolData)
	 */
	public RenewResponseMessage renew(RenewMessage msg, ProtocolData protocolData) throws SOAPException {
		String wseIdentifier = msg.getHeader().getWseIdentifier();
		if (wseIdentifier == null) {
			// Fault wse:InvalidMessage
			throw createInvalidMessageFault(msg);
		}
		RenewResponseMessage response = new RenewResponseMessage(protocolData.getCommunicationManagerId());
		response.setResponseTo(msg);

		// set DPWSVersion from the Request to the Response
		response.setProtocolInfo(msg.getProtocolInfo());

		subscriptions.exclusiveLock();
		try {
			ServiceSubscription serviceSubscription = (ServiceSubscription) subscriptions.get(wseIdentifier);
			if (serviceSubscription == null) {
				// Fault wse:InvalidMessage
				throw createUnableToRenew(msg);
			}

			long currentTime = System.currentTimeMillis();
			if (serviceSubscription.expirationTime <= currentTime) {
				// Fault wse:InvalidMessage
				throw createUnableToRenew(msg);
			}

			serviceSubscription.setExpiration(msg.getExpires(), msg);
			// this MUST be done while we still hold the lock!
			response.setExpires(SchemaUtil.createDuration(serviceSubscription.expirationTime - currentTime));
		} finally {
			subscriptions.releaseExclusiveLock();
		}
		return response;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.ws4d.java.service.SubscriptionManagerInterface#renew(org.ws4d.java
	 * .eventing.ClientSubscription, long)
	 */
	public long renew(ClientSubscription subscription, long duration) throws EventingException {
		String wseIdentifier = subscription.getServiceSubscriptionId();

		ServiceSubscription serviceSubscription = null;
		subscriptions.exclusiveLock();
		try {
			serviceSubscription = (ServiceSubscription) subscriptions.get(wseIdentifier);
			if (serviceSubscription == null) {
				// Fault wse:InvalidMessage
				throw new EventingException(WSEConstants.WSE_FAULT_INVALID_MESSAGE, FAULT_REASON_INVALID_MESSAGE);
			}

			long currentTime = System.currentTimeMillis();
			if (serviceSubscription.expirationTime <= currentTime) {
				// Fault wse:InvalidMessage
				throw new EventingException(WSEConstants.WSE_FAULT_INVALID_MESSAGE, FAULT_REASON_INVALID_MESSAGE);
			}

			serviceSubscription.setExpiration(duration);
			return duration;
		} finally {
			subscriptions.releaseExclusiveLock();
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.message.eventing.SubscriptionManager#getStatus
	 * (org.ws4d.java.message.eventing.GetStatusMessage,
	 * org.ws4d.java.communication.ProtocolData)
	 */
	public GetStatusResponseMessage getStatus(GetStatusMessage msg, ProtocolData protocolData) throws SOAPException {
		String wseIdentifier = msg.getHeader().getWseIdentifier();
		if (wseIdentifier == null) {
			// Fault wse:InvalidMessage
			throw createInvalidMessageFault(msg);
		}
		GetStatusResponseMessage response = new GetStatusResponseMessage(protocolData.getCommunicationManagerId());
		response.setResponseTo(msg);

		// set DPWSVersion from the Request to the Response
		response.setProtocolInfo(msg.getProtocolInfo());

		subscriptions.sharedLock();
		try {
			ServiceSubscription subscription = (ServiceSubscription) subscriptions.get(wseIdentifier);
			if (subscription == null) {
				// Fault wse:InvalidMessage
				throw createInvalidMessageFault(msg);
			}
			long currentTime = System.currentTimeMillis();
			if (subscription.expirationTime <= currentTime) {
				// Fault wse:InvalidMessage
				throw createInvalidMessageFault(msg);
			}

			// this MUST be done while we hold the lock!
			response.setExpires(SchemaUtil.createDuration(subscription.expirationTime - currentTime));
		} finally {
			subscriptions.releaseSharedLock();
		}
		return response;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.ws4d.java.eventing.SubscriptionManager#getStatus(org.ws4d.java.eventing
	 * .ClientSubscription)
	 */
	public long getStatus(ClientSubscription subscription) throws EventingException, TimeoutException {
		String wseIdentifier = subscription.getServiceSubscriptionId();

		ServiceSubscription serviceSubscription = null;
		subscriptions.exclusiveLock();
		try {
			serviceSubscription = (ServiceSubscription) subscriptions.get(wseIdentifier);
			if (serviceSubscription == null) {
				// Fault wse:InvalidMessage
				throw new EventingException(WSEConstants.WSE_FAULT_INVALID_MESSAGE, FAULT_REASON_INVALID_MESSAGE);
			}

			long currentTime = System.currentTimeMillis();
			if (serviceSubscription.expirationTime <= currentTime) {
				// Fault wse:InvalidMessage
				throw new EventingException(WSEConstants.WSE_FAULT_INVALID_MESSAGE, FAULT_REASON_INVALID_MESSAGE);
			}

			return serviceSubscription.expirationTime - currentTime;
		} finally {
			subscriptions.releaseExclusiveLock();
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.eventing.SubscriptionManager#sendSubscriptionEnd()
	 */
	public void sendSubscriptionEnd() {
		subscriptions.exclusiveLock();
		try {
			for (Iterator it = subscriptions.values().iterator(); it.hasNext();) {
				ServiceSubscription subscription = (ServiceSubscription) it.next();
				it.remove();
				removeSubscriptionFromEventSources(subscription);
				if (subscription.sink == null) {
					// remote subscription
					SubscriptionEndMessage subscriptionEnd = subscriptionEnd(subscription, SubscriptionEndMessage.SOURCE_SHUTTING_DOWN_STATUS, EVENT_SOURCE_SHUTTING_DOWN);
					OutDispatcher.getInstance().send(subscriptionEnd, subscription.endTo);
				} else {
					ClientSubscription clientSubscription = subscription.sink.getSubscription(subscription.clientSubscriptionId);
					if (clientSubscription != null) {
						subscription.sink.getEventListener().subscriptionEndReceived(clientSubscription, SubscriptionEndMessage.SOURCE_SHUTTING_DOWN_STATUS);
					}
				}
			}
		} finally {
			subscriptions.releaseExclusiveLock();
		}
	}

	public boolean isRemote() {
		return false;
	}

}
