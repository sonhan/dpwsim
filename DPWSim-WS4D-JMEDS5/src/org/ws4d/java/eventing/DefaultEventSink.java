/*******************************************************************************
 * Copyright (c) 2009 MATERNA Information & Communications. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html. For further
 * project-related information visit http://www.ws4d.org. The most recent
 * version of the JMEDS framework can be obtained from
 * http://sourceforge.net/projects/ws4d-javame.
 ******************************************************************************/

package org.ws4d.java.eventing;

import java.io.IOException;

import org.ws4d.java.communication.CommunicationBinding;
import org.ws4d.java.communication.CommunicationManager;
import org.ws4d.java.communication.CommunicationManagerRegistry;
import org.ws4d.java.communication.DefaultIncomingMessageListener;
import org.ws4d.java.communication.ProtocolData;
import org.ws4d.java.communication.TimeoutException;
import org.ws4d.java.communication.protocol.soap.generator.handlers.ClientSubscriptionElementHandler;
import org.ws4d.java.configuration.EventingProperties;
import org.ws4d.java.constants.DPWSMessageConstants;
import org.ws4d.java.constants.SOAPConstants;
import org.ws4d.java.constants.WSAConstants;
import org.ws4d.java.constants.WSEConstants;
import org.ws4d.java.io.xml.ElementHandlerRegistry;
import org.ws4d.java.message.FaultMessage;
import org.ws4d.java.message.InvokeMessage;
import org.ws4d.java.message.SOAPException;
import org.ws4d.java.message.eventing.SubscriptionEndMessage;
import org.ws4d.java.schema.PredefinedSchemaTypes;
import org.ws4d.java.service.DefaultClientSubscription;
import org.ws4d.java.service.OperationDescription;
import org.ws4d.java.service.Service;
import org.ws4d.java.service.parameter.ParameterValue;
import org.ws4d.java.service.parameter.QNameValue;
import org.ws4d.java.service.reference.ServiceReference;
import org.ws4d.java.structures.ArrayList;
import org.ws4d.java.structures.DataStructure;
import org.ws4d.java.structures.HashMap;
import org.ws4d.java.structures.HashSet;
import org.ws4d.java.structures.Iterator;
import org.ws4d.java.structures.LockedMap;
import org.ws4d.java.structures.ReadOnlyIterator;
import org.ws4d.java.types.LocalizedString;
import org.ws4d.java.types.QName;
import org.ws4d.java.types.URI;
import org.ws4d.java.util.Log;
import org.ws4d.java.util.StringUtil;
import org.ws4d.java.util.WS4DIllegalStateException;

/**
 * Class represents an endpoint to receive notifications.
 */
public class DefaultEventSink implements EventSink {

	private static final int[]				EVENT_SINK_MESSAGE_TYPES	= { DPWSMessageConstants.INVOKE_MESSAGE, DPWSMessageConstants.SUBSCRIPTION_END_MESSAGE };

	private final EventSinkMessageListener	incomingListener			= new EventSinkMessageListener();

	private final DataStructure				bindings					= new ArrayList();

	private final EventListener				eventListener;

	private boolean							registered					= false;

	private HashMap							map_CSubId_2_CSub			= new LockedMap(new HashMap(5));

	static {
		/*
		 * Register element handler for eventListener side eventing.
		 */
		ElementHandlerRegistry.getRegistry().registerElementHandler(WSEConstants.WSE_QN_IDENTIFIER, new ClientSubscriptionElementHandler());
	}

	/**
	 * Constructor.
	 * 
	 * @param eventListener Client with which this event sink should be
	 *            associated. Received events will be transmitted to the
	 *            eventListener.
	 */
	private DefaultEventSink(EventListener eventListener) {
		this.eventListener = eventListener;
	}

	/**
	 * Constructor.
	 * 
	 * @param eventListener Client with which this event sink should be
	 *            associated. Received events will be transmitted to the
	 *            eventListener.
	 * @param bindings a data structure of {@link CommunicationBinding}
	 *            instances over which to expose this event sink
	 */
	public DefaultEventSink(EventListener eventListener, DataStructure bindings) {
		this(eventListener);
		if (bindings != null) {
			// needed only for a remote event sink
			this.bindings.addAll(bindings);
		}
	}

	/**
	 * @param eventListener
	 * @param configurationId
	 */
	public DefaultEventSink(EventListener eventListener, int configurationId) {
		this(eventListener);
		this.bindings.addAll(EventingProperties.getInstance().getBindings(new Integer(configurationId)));
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.communication.Bindable#hasBindings()
	 */
	public boolean hasBindings() {
		return (bindings.size() > 0);
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.communication.Bindable#getBindings()
	 */
	public Iterator getBindings() {
		return new ReadOnlyIterator(bindings);
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.communication.Bindable#supportsBindingChanges()
	 */
	public boolean supportsBindingChanges() {
		return !registered;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.ws4d.java.communication.Bindable#addBinding(org.ws4d.java.communication
	 * .CommunicationBinding)
	 */
	public void addBinding(CommunicationBinding binding) throws WS4DIllegalStateException {
		if (registered) {
			throw new WS4DIllegalStateException("Event Sink is already running, unable to add binding");
		}
		bindings.add(binding);
	}

	/*
	 * (non-Javadoc)
	 * @seeorg.ws4d.java.communication.Bindable#removeBinding(org.ws4d.java.
	 * communication.CommunicationBinding)
	 */
	public boolean removeBinding(CommunicationBinding binding) throws WS4DIllegalStateException {
		if (registered) {
			throw new WS4DIllegalStateException("Event Sink is already running, unable to remove binding");
		}
		return bindings.remove(binding);
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.communication.Bindable#clearBindings()
	 */
	public void clearBindings() throws WS4DIllegalStateException {
		if (registered) {
			throw new WS4DIllegalStateException("Event Sink is already running, unable to clear bindings");
		}
		bindings.clear();
	}

	/**
	 * Opens event receiving for this event endpoint.
	 */
	public void open() throws EventingException {
		if (registered == true) {
			if (Log.isDebug()) {
				Log.debug("EventSink already opened", Log.DEBUG_LAYER_FRAMEWORK);
			}
			return;
		}
		if (!hasBindings()) {
			String descriptor = StringUtil.simpleClassName(getClass());
			if (Log.isDebug()) {
				Log.debug("No bindings found, autobinding event sink " + descriptor, Log.DEBUG_LAYER_FRAMEWORK);
			}
			DataStructure autoBindings = new HashSet();
			for (Iterator it = CommunicationManagerRegistry.getLoadedManagers(); it.hasNext();) {
				CommunicationManager manager = (CommunicationManager) it.next();
				try {
					manager.getAutobindings(descriptor, autoBindings);
				} catch (IOException e) {
					Log.error("Unable to obtain autobindings from communication manager " + manager.getCommunicationManagerId());
					Log.printStackTrace(e);
				}
			}
			for (Iterator it = autoBindings.iterator(); it.hasNext();) {
				CommunicationBinding binding = (CommunicationBinding) it.next();
				addBinding(binding);
			}
		}
		for (Iterator it = bindings.iterator(); it.hasNext();) {
			CommunicationBinding binding = (CommunicationBinding) it.next();
			try {
				CommunicationManager manager = CommunicationManagerRegistry.getManager(binding.getCommunicationManagerId());
				//TODO: Group user
				manager.registerService(EVENT_SINK_MESSAGE_TYPES, binding, incomingListener, null);
			} catch (IOException e) {
				// FIXME no need to signal per invocation exception
				EventingException ex = new EventingException("Unable to bind Event Sink to " + binding.getTransportAddress() + ": " + e);
				Log.printStackTrace(ex);
				throw ex;
			}
		}
		registered = true;
	}

	/**
	 * Closes event receiving for this event endpoint.
	 */
	public void close() {
		// unbind all communication bindings
		for (Iterator it = bindings.iterator(); it.hasNext();) {
			CommunicationBinding binding = (CommunicationBinding) it.next();
			try {
				CommunicationManager manager = CommunicationManagerRegistry.getManager(binding.getCommunicationManagerId());
				manager.unregisterService(EVENT_SINK_MESSAGE_TYPES, binding, incomingListener);
			} catch (IOException e) {
				Log.error("unable to unbind from " + binding.getTransportAddress());
				e.printStackTrace();
			}
		}
		map_CSubId_2_CSub.clear();
		registered = false;
	}

	public EventListener getEventListener() {
		return eventListener;
	}

	public boolean isOpen() {
		return registered;
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.eventing.EventSink#getSubscription(java.lang.String)
	 */
	public ClientSubscription getSubscription(String clientSubId) {
		return (ClientSubscription) map_CSubId_2_CSub.get(clientSubId);
	}

	/**
	 * @param clientSubId
	 * @return the removed client subscription
	 */
	public ClientSubscription removeSubscription(String clientSubId) {
		return (ClientSubscription) map_CSubId_2_CSub.remove(clientSubId);
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.eventing.EventSink#addSubscription(java.lang.String,
	 * org.ws4d.java.eventing.ClientSubscription)
	 */
	public void addSubscription(String clientSubId, ClientSubscription subscription) {
		map_CSubId_2_CSub.put(clientSubId, subscription);
	}

	/**
	 * @param clientSubscriptionId
	 * @param actionUri
	 * @param outputParameter
	 * @return a possible result to a solicit-response event
	 */
	public ParameterValue receiveLocalEvent(String clientSubscriptionId, URI actionUri, ParameterValue outputParameter) {
		ClientSubscription subscription;
		subscription = (ClientSubscription) map_CSubId_2_CSub.get(clientSubscriptionId);

		return eventListener.eventReceived(subscription, actionUri, outputParameter);
	}

	private final class EventSinkMessageListener extends DefaultIncomingMessageListener {

		private EventSinkMessageListener() {
			super();
		}

		/*
		 * (non-Javadoc)
		 * @see
		 * org.ws4d.java.communication.DefaultIncomingMessageListener#handle
		 * (org.ws4d.java.message.invocation.InvokeMessage,
		 * org.ws4d.java.communication.ProtocolData)
		 */
		public InvokeMessage handle(final InvokeMessage msg, ProtocolData protocolData) throws SOAPException {
			if (!isOpen()) {
				// send Fault wsa:EndpointUnavailable
				throw new SOAPException(FaultMessage.createEndpointUnavailableFault(msg));
			}
			String clientSubscriptionId = null;
			if (msg.getHeader().getWseIdentifier() == null) {
				Log.error("A header representing the eventListener supbscription ID (as part of the [reference parameters]) is missing.");
			} else {
				clientSubscriptionId = msg.getHeader().getWseIdentifier();
			}
			if (clientSubscriptionId == null) {
				// throw wsa:InvalidAddresingHeader exception
				FaultMessage fault = new FaultMessage(WSAConstants.WSA_ACTION_ADDRESSING_FAULT, protocolData.getCommunicationManagerId());
				fault.setResponseTo(msg);
				fault.setCode(SOAPConstants.SOAP_FAULT_SENDER);
				// fill in subcode, reason and detail
				fault.setSubcode(WSAConstants.WSA_FAULT_INVALID_ADDRESSING_HEADER);
				LocalizedString reason = new LocalizedString("A header representing the eventListener supbscription ID (as part of the [reference parameters]) is missing", null);
				fault.addReason(reason);

				ParameterValue detail = ParameterValue.createElementValue(PredefinedSchemaTypes.WSA_PROBLEM_HEADER_QNAME);
				if (detail.getValueType() == ParameterValue.TYPE_QNAME) {
					QNameValue value = (QNameValue) detail;
					value.set(new QName(WSEConstants.WSE_ELEM_IDENTIFIER, WSEConstants.WSE_NAMESPACE_NAME));
				}
				fault.setDetail(detail);
				throw new SOAPException(fault);
			}
			final ClientSubscription subscription;
			subscription = (ClientSubscription) map_CSubId_2_CSub.get(clientSubscriptionId);
			if (subscription == null) {
				// throw wsa:InvalidAddresingHeader exception
				FaultMessage fault = new FaultMessage(WSAConstants.WSA_ACTION_ADDRESSING_FAULT, protocolData.getCommunicationManagerId());
				fault.setResponseTo(msg);
				fault.setCode(SOAPConstants.SOAP_FAULT_SENDER);
				// fill in subcode, reason and detail
				fault.setSubcode(WSAConstants.WSA_FAULT_INVALID_ADDRESSING_HEADER);
				LocalizedString reason = new LocalizedString("Unknown eventListener supbscription ID found: " + clientSubscriptionId, null);
				fault.addReason(reason);

				ParameterValue detail = ParameterValue.createElementValue(PredefinedSchemaTypes.WSA_PROBLEM_HEADER_QNAME);
				if (detail.getValueType() == ParameterValue.TYPE_QNAME) {
					QNameValue value = (QNameValue) detail;
					value.set(new QName(WSEConstants.WSE_ELEM_IDENTIFIER, WSEConstants.WSE_NAMESPACE_NAME));
				}
				fault.setDetail(detail);
				throw new SOAPException(fault);
			}

			ParameterValue paramValue = eventListener.eventReceived(subscription, msg.getAction(), msg.getContent());

			if (paramValue != null) {
				/*
				 * Send solicit response message type response.
				 */
				String outputActionName = msg.getAction().toString();
				Service service;
				try {
					service = subscription.getServiceReference().getService();
					EventSource event = service.getEventSource(outputActionName);
					String inputActionName = event.getInputAction();
					InvokeMessage rspMsg = new InvokeMessage(inputActionName, false, protocolData.getCommunicationManagerId());
					rspMsg.setResponseTo(msg);

					// set DPWSVersion from the Request to the Response
					rspMsg.setProtocolInfo(msg.getProtocolInfo());

					rspMsg.setContent(paramValue);

					return rspMsg;
				} catch (TimeoutException e) {
					Log.error("EventSink.handleMessage(Invoke): can't get service (timeout).");
					e.printStackTrace();

					throw new SOAPException(FaultMessage.createEndpointUnavailableFault(msg));
				}
			} else {
				// send HTTP response (202)
				return null;
			}
		}

		/*
		 * (non-Javadoc)
		 * @see
		 * org.ws4d.java.communication.DefaultIncomingMessageListener#handle
		 * (org.ws4d.java.message.eventing.SubscriptionEndMessage,
		 * org.ws4d.java.communication.ProtocolData)
		 */
		public void handle(SubscriptionEndMessage msg, ProtocolData protocolData) {
			if (!isOpen()) {
				return;
			}
			String msgSubId = msg.getSubscriptionManager().getReferenceParameters().getWseIdentifier();
			if (msgSubId == null) {
				Log.error("DefaultEventSink.handleMessage(SubscriptionEnd): received subscription end message without service subscription id");
				return;
			}

			DefaultClientSubscription clientSub = null;
			DataStructure clientSubs = map_CSubId_2_CSub.values();
			for (Iterator it = clientSubs.iterator(); it.hasNext();) {
				/*
				 * We have to check each eventListener subscription in map
				 */
				clientSub = (DefaultClientSubscription) it.next();
				if (clientSub != null) {
					String serviceSubId = clientSub.getServiceSubscriptionId();
					if (msgSubId.equals(serviceSubId)) {
						URI reason = msg.getStatus();
						eventListener.subscriptionEndReceived(clientSub, reason);
					}
				}
			}
		}

		/*
		 * (non-Javadoc)
		 * @see
		 * org.ws4d.java.communication.DefaultIncomingMessageListener#getOperation
		 * (java.lang.String)
		 */
		public OperationDescription getOperation(String action) {
			OperationDescription operation = null;
			DefaultClientSubscription clientSub = null;
			DataStructure clientSubs = map_CSubId_2_CSub.values();
			for (Iterator it = clientSubs.iterator(); it.hasNext();) {
				/*
				 * We have to check each eventListener subscription in map
				 */
				clientSub = (DefaultClientSubscription) it.next();
				if (clientSub != null) {
					// String serviceSubId =
					// clientSub.getServiceSubscriptionId();
					ServiceReference servRef = clientSub.getServiceReference();
					Service service;
					try {
						service = servRef.getService();
						operation = service.getEventSource(action);
						if (operation != null) {
							break;
						}
					} catch (TimeoutException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
			return operation;
		}

	}

}
