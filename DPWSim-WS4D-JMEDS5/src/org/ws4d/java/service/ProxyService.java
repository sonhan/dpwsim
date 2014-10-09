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

import java.io.IOException;

import org.ws4d.java.DPWSFramework;
import org.ws4d.java.communication.CommunicationManager;
import org.ws4d.java.communication.CommunicationUtil;
import org.ws4d.java.communication.DefaultResponseCallback;
import org.ws4d.java.communication.ProtocolData;
import org.ws4d.java.communication.TimeoutException;
import org.ws4d.java.configuration.FrameworkProperties;
import org.ws4d.java.constants.ConstantsHelper;
import org.ws4d.java.constants.DPWSMessageConstants;
import org.ws4d.java.dispatch.MissingMetadataException;
import org.ws4d.java.dispatch.OutDispatcher;
import org.ws4d.java.dispatch.ServiceReferenceInternal;
import org.ws4d.java.eventing.ClientSubscription;
import org.ws4d.java.eventing.ClientSubscriptionInternal;
import org.ws4d.java.eventing.EventSink;
import org.ws4d.java.eventing.EventingException;
import org.ws4d.java.eventing.EventingFactory;
import org.ws4d.java.message.FaultMessage;
import org.ws4d.java.message.InvokeMessage;
import org.ws4d.java.message.Message;
import org.ws4d.java.message.SOAPHeader;
import org.ws4d.java.message.eventing.GetStatusMessage;
import org.ws4d.java.message.eventing.GetStatusResponseMessage;
import org.ws4d.java.message.eventing.RenewMessage;
import org.ws4d.java.message.eventing.RenewResponseMessage;
import org.ws4d.java.message.eventing.SubscribeMessage;
import org.ws4d.java.message.eventing.SubscribeResponseMessage;
import org.ws4d.java.message.eventing.UnsubscribeMessage;
import org.ws4d.java.message.eventing.UnsubscribeResponseMessage;
import org.ws4d.java.schema.SchemaUtil;
import org.ws4d.java.service.parameter.ParameterValue;
import org.ws4d.java.service.reference.DeviceReference;
import org.ws4d.java.service.reference.ServiceReference;
import org.ws4d.java.structures.DataStructure;
import org.ws4d.java.structures.HashSet;
import org.ws4d.java.structures.Iterator;
import org.ws4d.java.types.Delivery;
import org.ws4d.java.types.EndpointReference;
import org.ws4d.java.types.EprInfo;
import org.ws4d.java.types.Filter;
import org.ws4d.java.types.QName;
import org.ws4d.java.types.QNameSet;
import org.ws4d.java.types.ReferenceParametersMData;
import org.ws4d.java.types.URI;
import org.ws4d.java.types.URISet;
import org.ws4d.java.types.XAddressInfo;
import org.ws4d.java.util.Log;
import org.ws4d.java.wsdl.WSDL;
import org.ws4d.java.wsdl.WSDLOperation;
import org.ws4d.java.wsdl.WSDLPortType;
import org.ws4d.java.wsdl.WSDLRepository;

/**
 * Proxy class of a dpws service.
 * 
 * @author mspies
 */
public class ProxyService extends ServiceCommons {

	private ServiceReference	serviceReference;

	/**
	 * Constructor. Will create proxy service, which must be initialized by
	 * {@link #initialize(ServiceReference, DeviceReference)()} later on.
	 */
	ProxyService() {}

	/**
	 * @param serviceReference
	 * @throws MissingMetadataException in case no service description metadata
	 *             (i.e. WSDL) was found for at least one of the service's port
	 *             types
	 */
	public ProxyService(ServiceReference serviceReference) throws MissingMetadataException {
		super();

		try {
			initialize(serviceReference);
		} catch (InstantiationException e) {
			// won't happen
		}
	}

	/**
	 * Must be called after construction of ProxyService without
	 * {@link ServiceReference} as parameter.
	 * 
	 * @param serviceReference
	 */
	protected void initialize(ServiceReference serviceReference) throws InstantiationException, MissingMetadataException {
		if (this.serviceReference != null) {
			throw new InstantiationException("ProxyService already initialized!");
		}

		this.serviceReference = serviceReference;

		this.setSecure(serviceReference.isSecureService());
		if (!WSDLRepository.DEMO_MODE) {
			if (loadFromEmbeddedWSDLs(getPortTypes())) {
				return;
			}
		}
		/*
		 * not all found within embedded WSDLs, try building up from metadata
		 * locations and local repo
		 */
		if (loadFromMetadataLocations(getPortTypes())) {
			return;
		}
		/*
		 * finally, try to resolve port types within local repo
		 */
		if (loadFromRepository(getPortTypes())) {
			return;
		}
		throw new MissingMetadataException("Unable to resolve all port types of service");
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.service.Service#getServiceReference()
	 */
	public ServiceReference getServiceReference() {
		return serviceReference;
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.service.Service#getParentDeviceReference()
	 */
	public DeviceReference getParentDeviceReference() {
		return serviceReference.getParentDeviceRef();
	}

	// -------------------------------------------------------

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.service.Service#isRemote()
	 */
	public boolean isRemote() {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.service.Service#getServiceId()
	 */
	public URI getServiceId() {
		return serviceReference.getServiceId();
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.service.Service#getEndpointReferences()
	 */
	public Iterator getEprInfos() {
		return serviceReference.getEprInfos();
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.service.Service#getPortTypes()
	 */
	public Iterator getPortTypes() {
		return serviceReference.getPortTypes();
	}

	public int getPortTypeCount() {
		return portTypes.size();
	}

	/**
	 * @param portTypes new port types to add to this proxy service
	 * @throws MissingMetadataException if no metadata (WSDL) is found for at
	 *             least one of the specified <code>portTypes</code>
	 */
	public void appendPortTypes(QNameSet portTypes) throws MissingMetadataException {
		if (loadFromMetadataLocations(portTypes.iterator())) {
			return;
		}
		if (!loadFromRepository(portTypes.iterator())) {
			throw new MissingMetadataException("Unable to resolve some port types of service: " + portTypes);
		}
	}

	/**
	 * Initializes event receiving from specified event sender.
	 * 
	 * @param sink event sink which will receive the notifications.
	 * @param clientSubscriptionId
	 * @param eventActionURIs set of action URIs to subscribe to.
	 * @param duration duration in milliseconds of subscription. If 0 no
	 *            expiration of subscription.
	 * @return subscription id (wse:identifier)
	 * @throws EventingException
	 * @throws TimeoutException
	 */
	public ClientSubscription subscribe(EventSink sink, String clientSubscriptionId, URISet eventActionURIs, long duration) throws EventingException, TimeoutException {
		if (!sink.isOpen()) {
			Log.error("Cannot subscribe, event sink is not open");
			throw new EventingException("EventSink not open");
		}

		/*
		 * Create subscibe message
		 */
		
		ServiceReferenceInternal serviceRef = (ServiceReferenceInternal) getServiceReference();
		XAddressInfo preferredXAddressInfo = serviceRef.getPreferredXAddressInfo();
		SubscribeMessage request = new SubscribeMessage(preferredXAddressInfo.getComManId());
		request.getHeader().setEndpointReference(((EprInfo) getEprInfos().next()).getEndpointReference());
		request.setTargetXAddressInfo(preferredXAddressInfo);
		// request.setProtocolVersionInfo(ProtocolVersionInfoRegistry.get(getParentDeviceReference().getEndpointReference()));
		request.setProtocolInfo(preferredXAddressInfo.getProtocolInfo());

		ReferenceParametersMData refParams = new ReferenceParametersMData();
		refParams.setWseIdentifier(clientSubscriptionId);
		/*
		 * TODO find out which of the sink's bindings corresponds to the
		 * communication interface, which from the address of one of the EPRs of
		 * the target service is reachable from
		 */

		EndpointReference notifyTarget = new EndpointReference(URI.EMPTY_URI, refParams);
		Delivery delivery = new Delivery(null, notifyTarget);
		request.setDelivery(delivery);
		request.setEventSink(sink);

		if (duration != 0) {
			request.setExpires(SchemaUtil.createDuration(duration));
		}

		CommunicationManager comMan = DPWSFramework.getCommunicationManager(preferredXAddressInfo.getComManId());
		CommunicationUtil comUtil = comMan.getCommunicationUtil();
		ConstantsHelper helper = null;
		if (request.getProtocolInfo() == null) {
			helper = comUtil.getHelper(comMan.getProtocolInfo().getVersion());
		} else {
			helper = comUtil.getHelper(request.getProtocolInfo().getVersion());
		}

		Filter filter = new Filter(helper.getDPWSUriFilterEeventingAction(), eventActionURIs);
		request.setFilter(filter);

		ProxyServiceCallback handler = new ProxyServiceCallback(preferredXAddressInfo);
		OutDispatcher.getInstance().send(request, preferredXAddressInfo, handler);

		synchronized (handler) {
			while (handler.pending) {
				try {
					handler.wait();
				} catch (InterruptedException e) {
					// void
				}
			}
		}

		if (handler.exception != null) {
			throw handler.exception;
		}
		ClientSubscription subscription = null;
		if (handler.msg != null) {
			/*
			 * CASE: Subscription Response received
			 */
			SubscribeResponseMessage subscribeRsp = (SubscribeResponseMessage) handler.msg;
			EventingFactory eFac = null;
			try {
				eFac = DPWSFramework.getEventingFactory();
			} catch (IOException e) {
				throw new EventingException("Cannot subscribe for events Eventing support not found.");
			}
			subscription = eFac.createClientSubscription(sink, clientSubscriptionId, subscribeRsp.getSubscriptionManager(), handler.protocolData.getCommunicationManagerId(), SchemaUtil.parseDuration(subscribeRsp.getExpires()), serviceReference);

			// SubscriptionManager manager = new SubscriptionManagerProxy(
			// serviceSubscriptionId, sink, duration );
			//
			// subscription = new
			// DefaultClientSubscription(clientSubscriptionId,
			// serviceSubscriptionId, serviceReference, manager);
			
			
			subscription.getSubscriptionManagerAddressInfo().setProtocolInfo(handler.protocolData.getProtocolInfo());
			

			sink.addSubscription(clientSubscriptionId, subscription);
		} else if (handler.fault != null) {
			/*
			 * CASE: Fault received
			 */
			throw new EventingException(handler.fault);
		} else {
			// shouldn't ever occur
			throw new TimeoutException("Subscribe timeout");
		}

		return subscription;
	}

	/**
	 * Unsubscribes from specified subscription.
	 * 
	 * @param subscription subscription to terminate.
	 * @throws EventingException
	 * @throws TimeoutException
	 */
	public void unsubscribe(ClientSubscription subscription) throws EventingException, TimeoutException {
		if (subscription == null) {
			Log.error("Cannot unsubscribe, subscription is null");
			throw new EventingException("Subscription is null");
		}
		((ClientSubscriptionInternal) subscription).dispose();

		/*
		 * Create unsubscribe message
		 */
		EprInfo subscriptionManagerXAddressInfo = subscription.getSubscriptionManagerAddressInfo();
		UnsubscribeMessage request = new UnsubscribeMessage(subscriptionManagerXAddressInfo.getComManId());
		request.setTargetXAddressInfo(subscriptionManagerXAddressInfo);
		SOAPHeader header = request.getHeader();
		header.setEndpointReference(subscriptionManagerXAddressInfo.getEndpointReference());
		// request.setProtocolVersionInfo(ProtocolVersionInfoRegistry.get(getParentDeviceReference().getEndpointReference()));
		request.setProtocolInfo(subscriptionManagerXAddressInfo.getProtocolInfo());

		ProxyServiceCallback handler = new ProxyServiceCallback(subscriptionManagerXAddressInfo);
		/*
		 * XXX this is based on the assumption that both the subscribed service
		 * as well as its possibly stand-alone subscription manager use the same
		 * communication protocol
		 */
		OutDispatcher.getInstance().send(request, subscriptionManagerXAddressInfo, handler);

		synchronized (handler) {
			while (handler.pending) {
				try {
					handler.wait();
				} catch (InterruptedException e) {
					// void
				}
			}
		}

		if (handler.exception != null) {
			throw handler.exception;
		}
		if (handler.msg != null) {
			// CASE: Unsubscribe Response received, return
			return;
		} else if (handler.fault != null) {
			// CASE: Fault received
			throw new EventingException(handler.fault);
		} else {
			// CASE: Timeout of watchdog
			// shouldn't ever occur
			throw new TimeoutException("Unsubscribe timeout");
		}
	}

	/**
	 * Renews an existing subscription with new duration. If duration is "0"
	 * subscription never terminates.
	 * 
	 * @param subscription
	 * @param duration
	 * @return either the actual subscription duration as reported by the
	 *         service or<code>0</code> if the subscription doesn't expire at
	 *         all
	 * @throws EventingException
	 * @throws TimeoutException
	 */
	public long renew(ClientSubscription subscription, long duration) throws EventingException, TimeoutException {
		if (subscription == null) {
			Log.error("Cannot renew, subscription is null");
			throw new EventingException("Subscription is null");
		}

		if (!subscription.getEventSink().isOpen()) {
			Log.error("Cannot renew, event sink is not open");
			throw new EventingException("EventSink not open");
		}

		/*
		 * Create renew message
		 */
		EprInfo subscriptionManagerXAddressInfo = subscription.getSubscriptionManagerAddressInfo();
		RenewMessage request = new RenewMessage(subscriptionManagerXAddressInfo.getComManId());
		request.setTargetXAddressInfo(subscriptionManagerXAddressInfo);
		request.getHeader().setEndpointReference(subscriptionManagerXAddressInfo.getEndpointReference());
		if (duration != 0) {
			request.setExpires(SchemaUtil.createDuration(duration));
		}
		// request.setProtocolVersionInfo(ProtocolVersionInfoRegistry.get(getParentDeviceReference().getEndpointReference()));
		request.setProtocolInfo(((ServiceReferenceInternal) getServiceReference()).getPreferredXAddressInfo().getProtocolInfo());

		ProxyServiceCallback handler = new ProxyServiceCallback(subscriptionManagerXAddressInfo);
		/*
		 * XXX this is based on the assumption that both the subscribed service
		 * as well as its possibly stand-alone subscription manager use the same
		 * communication protocol
		 */
		OutDispatcher.getInstance().send(request, subscriptionManagerXAddressInfo, handler);

		synchronized (handler) {
			while (handler.pending)
				try {
					handler.wait();
				} catch (InterruptedException e) {
					// void
				}
		}

		if (handler.exception != null) {
			throw handler.exception;
		}
		// URI subscriptionId = null;
		if (handler.msg != null) {
			// CASE: Subscription Response received
			RenewResponseMessage renewRsp = (RenewResponseMessage) handler.msg;
			long newDuration = SchemaUtil.parseDuration(renewRsp.getExpires());
			((ClientSubscriptionInternal) subscription).renewInternal(newDuration);
			return newDuration;
		} else if (handler.fault != null) {
			// CASE: Fault received
			throw new EventingException(handler.fault);
		} else {
			// shouldn't ever occur
			throw new TimeoutException("Renew timeout");
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.service.Service#getStatus(org.ws4d.java.eventing.
	 * ClientSubscription)
	 */
	public long getStatus(ClientSubscription subscription) throws EventingException, TimeoutException {
		if (subscription == null) {
			Log.error("Cannot get status, subscription is null");
			throw new EventingException("Subscription is null");
		}

		if (!subscription.getEventSink().isOpen()) {
			Log.error("Cannot get status, event sink is not open");
			throw new EventingException("EventSink not open");
		}

		/*
		 * Create getStatus message
		 */
		EprInfo subscriptionManagerXAddressInfo = subscription.getSubscriptionManagerAddressInfo();
		GetStatusMessage request = new GetStatusMessage(subscriptionManagerXAddressInfo.getComManId());
		request.setTargetXAddressInfo(subscriptionManagerXAddressInfo);
		request.getHeader().setEndpointReference(subscriptionManagerXAddressInfo.getEndpointReference());
		// request.setProtocolVersionInfo(ProtocolVersionInfoRegistry.get(getParentDeviceReference().getEndpointReference()));
		request.setProtocolInfo(((ServiceReferenceInternal) getServiceReference()).getPreferredXAddressInfo().getProtocolInfo());
		ProxyServiceCallback handler = new ProxyServiceCallback(subscriptionManagerXAddressInfo);
		/*
		 * XXX this is based on the assumption that both the subscribed service
		 * as well as its possibly stand-alone subscription manager use the same
		 * communication protocol
		 */
		OutDispatcher.getInstance().send(request, subscriptionManagerXAddressInfo, handler);

		synchronized (handler) {
			while (handler.pending) {
				try {
					handler.wait();
				} catch (InterruptedException e) {
					// void
				}
			}
		}

		if (handler.exception != null) {
			throw handler.exception;
		}
		// URI subscriptionId = null;
		if (handler.msg != null) {
			// CASE: GetStatus response received
			GetStatusResponseMessage getStatusRsp = (GetStatusResponseMessage) handler.msg;
			return SchemaUtil.parseDuration(getStatusRsp.getExpires());

		} else if (handler.fault != null) {
			// CASE: Fault received
			throw new EventingException(handler.fault);
		} else {
			// shouldn't ever occur
			throw new TimeoutException("GetStatus timeout");
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.service.Service#getDescription(java.lang.String)
	 */
	public WSDL getDescription(String targetNamespace) {
		return getExistingDescription(targetNamespace);
	}

	private boolean loadFromEmbeddedWSDLs(Iterator portTypes) {
		Iterator wsdls = serviceReference.getWSDLs();
		if (!wsdls.hasNext()) {
			return false;
		}
		// make a copy of required port types
		DataStructure portTypesToResolve = new HashSet();
		for (Iterator it = portTypes; it.hasNext();) {
			QName portTypeName = (QName) it.next();
			portTypesToResolve.add(portTypeName);
		}
		while (wsdls.hasNext()) {
			WSDL wsdl = (WSDL) wsdls.next();
			this.wsdls.put(wsdl.getTargetNamespace(), wsdl);
			for (Iterator it = portTypesToResolve.iterator(); it.hasNext();) {
				QName portTypeName = (QName) it.next();
				WSDLPortType portType = wsdl.getPortType(portTypeName);
				/*
				 * we don't check whether this port type has an actual binding
				 * or service definition within the WSDL, as it is declared
				 * within the service reference (aka. within the service's
				 * hosted block)
				 */
				if (portType != null) {
					processWSDLPortType(portType);
					it.remove();
				}
			}
		}
		return portTypesToResolve.isEmpty();
	}

	private boolean loadFromRepository(Iterator portTypes) {
		if (FrameworkProperties.getInstance().isBypassWsdlRepository()) {
			// do not load anything from repository
			if (Log.isDebug()) {
				Log.debug("Bypassing WSDL repository due to configuration property.");
			}
			return false;
		}
		boolean allFound = true;
		WSDLRepository repo = WSDLRepository.getInstance();
		for (Iterator it = portTypes; it.hasNext();) {
			QName portTypeName = (QName) it.next();
			if (this.portTypes.containsKey(portTypeName)) {
				// port type already loaded
				continue;
			}

			WSDLPortType wsdlPortType = null;
			for (Iterator it2 = this.wsdls.values().iterator(); it2.hasNext();) {
				WSDL wsdl = (WSDL) it2.next();
				wsdlPortType = wsdl.getPortType(portTypeName);
				if (wsdlPortType != null) {
					break;
				}
			}
			if (wsdlPortType == null) {
				WSDL wsdl = repo.getWsdl(portTypeName);
				if (wsdl == null) {
					allFound = false;
					if (Log.isDebug()) {
						Log.debug("Unable to find a WSDL within local repository for port type " + portTypeName, Log.DEBUG_LAYER_FRAMEWORK);
					}
					continue;
				}
				this.wsdls.put(wsdl.getTargetNamespace(), wsdl);
				wsdlPortType = wsdl.getPortType(portTypeName);
			}
			processWSDLPortType(wsdlPortType);
		}
		return allFound;
	}

	private boolean loadFromMetadataLocations(Iterator portTypes) throws MissingMetadataException {
		Iterator locations = serviceReference.getMetadataLocations();
		// if (!locations.hasNext()) {
		/*
		 * don't throw this here but rather let us log the missing (unresolved)
		 * port types within the thrown exception below
		 */
		// throw new MissingMetadataException("No metadata locations found");
		// }
		// make a copy of required port types
		DataStructure portTypesToResolve = new HashSet();
		for (Iterator it = portTypes; it.hasNext();) {
			QName portTypeName = (QName) it.next();
			// avoid already loaded
			if (!this.portTypes.containsKey(portTypeName)) {
				portTypesToResolve.add(portTypeName);
			}
		}
		while (locations.hasNext()) {
			if (portTypesToResolve.isEmpty()) {
				return true;
			}
			URI address = (URI) locations.next();
			// Get WSDL from remote location
			try {

				WSDL wsdl = WSDLRepository.getInstance().getWSDL(address.toString());
				if (wsdl == null) {
					wsdl = WSDLRepository.loadWsdl(address);
				} else if (Log.isDebug()) {
					Log.debug("WSDL from metadata location found within local repository: " + address);
				}
				this.wsdls.put(wsdl.getTargetNamespace(), wsdl);
				for (Iterator it = portTypesToResolve.iterator(); it.hasNext();) {
					QName portTypeName = (QName) it.next();
					WSDLPortType portType = wsdl.getPortType(portTypeName);
					/*
					 * we don't check whether this port type has an actual
					 * binding or service definition within the WSDL, as it is
					 * declared within the service reference (aka. within the
					 * service's hosted block)
					 */
					if (portType != null) {
						processWSDLPortType(portType);
						it.remove();
					}
				}
			} catch (IOException e) {
				Log.printStackTrace(e);
				throw new RuntimeException(e.getMessage());
			}
		}
		if (!portTypesToResolve.isEmpty()) {
			Log.warn("Unable to resolve some port types of service from available metadata locations: " + portTypesToResolve);
			return false;
		}
		return true;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.ws4d.java.service.ServiceCommons#createOperation(org.ws4d.java.wsdl
	 * .WSDLOperation)
	 */
	protected Operation createOperation(WSDLOperation wsdlOperation) {
		return new Operation(wsdlOperation) {

			public ParameterValue invoke(ParameterValue parameterValue) throws InvocationException, TimeoutException {
				/*
				 * client side invocation dispatcher
				 */
				return dispatchInvoke(this, parameterValue);
			}

		};
	}

	protected ParameterValue dispatchInvoke(Operation op, ParameterValue parameterValue) throws InvocationException, TimeoutException {
		// create InvokeInputMessage from op; set correct action URI
		InvokeMessage msg = new InvokeMessage(op.getInputAction(), CommunicationManager.ID_NULL);
		return dispatchInvoke(msg, op, parameterValue);
	}

	protected ParameterValue dispatchInvoke(InvokeMessage msg, Operation op, ParameterValue parameterValue) throws InvocationException, TimeoutException {
		msg.getHeader().setEndpointReference(((EprInfo) getEprInfos().next()).getEndpointReference());
		ServiceReferenceInternal serviceRef = (ServiceReferenceInternal) getServiceReference();
		XAddressInfo preferredXAddressInfo = serviceRef.getPreferredXAddressInfo();
		msg.setTargetXAddressInfo(preferredXAddressInfo);
		msg.setContent(parameterValue);
		// msg.setProtocolVersionInfo(ProtocolVersionInfoRegistry.get(getParentDeviceReference().getEndpointReference()));
		msg.setProtocolInfo(preferredXAddressInfo.getProtocolInfo());
		ProxyServiceCallback handler = new ProxyServiceCallback(preferredXAddressInfo, op);
		OutDispatcher.getInstance().send(msg, preferredXAddressInfo, handler);

		if (op.isOneWay()) {
			// don't block forever
			return null;
		}

		synchronized (handler) {
			while (handler.pending) {
				try {
					handler.wait();
				} catch (InterruptedException e) {
					// void
				}
			}
		}

		if (handler.exception != null) {
			throw handler.exception;
		}
		if (handler.msg != null) {
			InvokeMessage rspMsg = (InvokeMessage) handler.msg;
			return rspMsg.getContent();
		} else if (handler.fault != null) {
			/*
			 * CASE: Fault received
			 */
			FaultMessage fault = handler.fault;
			throw new InvocationException(fault);
		} else {
			// shouldn't ever occur
			throw new TimeoutException("Invocation time out");
		}
	}

	// ========================= INNER CLASS =========================

	private class ProxyServiceCallback extends DefaultResponseCallback {

		Message				msg			= null;

		FaultMessage		fault		= null;

		TimeoutException	exception	= null;

		volatile boolean	pending		= true;

		ProtocolData		protocolData;

		Operation			op			= null;

		ProxyServiceCallback(XAddressInfo targetXAddressInfo) {
			super(targetXAddressInfo);
		}

		ProxyServiceCallback(XAddressInfo targetXAddressInfo, Operation op) {
			super(targetXAddressInfo);
			this.op = op;
		}

		/*
		 * (non-Javadoc)
		 * @see
		 * org.ws4d.java.communication.DefaultResponseCallback#handle(org.ws4d
		 * .java.communication.message.Message, org.ws4d.java.message
		 * .eventing.SubscribeResponseMessage,
		 * org.ws4d.java.communication.ProtocolData)
		 */
		public void handle(Message request, SubscribeResponseMessage msg, ProtocolData protocolData) {
			releaseMessageSynchronization(msg, protocolData);
		}

		/*
		 * (non-Javadoc)
		 * @see
		 * org.ws4d.java.communication.DefaultResponseCallback#handle(org.ws4d
		 * .java.communication.message.Message,
		 * org.ws4d.java.message.invocation.InvokeMessage,
		 * org.ws4d.java.communication.ProtocolData)
		 */
		public void handle(Message request, InvokeMessage msg, ProtocolData protocolData) {
			releaseMessageSynchronization(msg, protocolData);
		}

		/*
		 * (non-Javadoc)
		 * @see org.ws4d.java.communication.DefaultResponseCallback#
		 * handleTransmissionException(org.ws4d.java.message.Message,
		 * java.lang.Exception, org.ws4d.java.communication.ProtocolData)
		 */
		public void handleTransmissionException(Message request, Exception exception, ProtocolData protocolData) {
			if (Log.isDebug()) {
				Log.printStackTrace(exception);
			}
			this.exception = new TimeoutException("Malformed response: " + exception);
			// same as for timeouts
			handleTimeout(request);
		}

		/*
		 * (non-Javadoc)
		 * @see
		 * org.ws4d.java.communication.DefaultResponseCallback#handle(org.ws4d
		 * .java.communication.message.Message,
		 * org.ws4d.java.message.FaultMessage,
		 * org.ws4d.java.communication.ProtocolData)
		 */
		public void handle(Message request, FaultMessage msg, ProtocolData protocolData) {
			synchronized (this) {
				pending = false;
				fault = msg;
				this.protocolData = protocolData;
				notifyAll();
			}
		}

		/*
		 * (non-Javadoc)
		 * @see org.ws4d.java.communication.DefaultResponseCallback#
		 * handleMalformedResponseException(org.ws4d.java.message.Message,
		 * java.lang.Exception, org.ws4d.java.communication.ProtocolData)
		 */
		public void handleMalformedResponseException(Message request, Exception exception, ProtocolData protocolData) {
			// same as for timeouts
			if (Log.isDebug()) {
				Log.printStackTrace(exception);
			}

			synchronized (this) {
				this.exception = new TimeoutException("Malformed response: " + exception);
				pending = false;
				notifyAll();
			}
			// handleTimeout(request);
		}

		/*
		 * (non-Javadoc)
		 * @see
		 * org.ws4d.java.communication.DefaultResponseCallback#handleTimeout
		 * (org.ws4d.java.message.Message)
		 */
		public void handleTimeout(Message request) {
			try {
				ServiceReferenceInternal servRef = (ServiceReferenceInternal) getServiceReference();
				XAddressInfo xAddressInfo = servRef.getNextXAddressInfoAfterFailure(request.getTargetAddress());
				request.setTargetXAddressInfo(xAddressInfo);
				switch (request.getType()) {
					case (DPWSMessageConstants.INVOKE_MESSAGE): {
						OutDispatcher.getInstance().send((InvokeMessage) request, xAddressInfo, this);
						break;
					}
					case (DPWSMessageConstants.SUBSCRIBE_MESSAGE): {
						OutDispatcher.getInstance().send((SubscribeMessage) request, xAddressInfo, this);
						break;
					}
					case (DPWSMessageConstants.GET_STATUS_MESSAGE): {
						OutDispatcher.getInstance().send((GetStatusMessage) request, xAddressInfo, this);
						break;
					}
					case (DPWSMessageConstants.RENEW_MESSAGE): {
						OutDispatcher.getInstance().send((RenewMessage) request, xAddressInfo, this);
						break;
					}
					case (DPWSMessageConstants.UNSUBSCRIBE_MESSAGE): {
						OutDispatcher.getInstance().send((UnsubscribeMessage) request, xAddressInfo, this);
						break;
					}
				}
			} catch (Throwable e) {
				synchronized (this) {
					exception = new TimeoutException("Exception occured during transmission exception processing: " + e);
					pending = false;
					notifyAll();
				}
			}
		}

		// ---------------------- MESSAGE HANDLING --------------------

		private void releaseMessageSynchronization(Message msg, ProtocolData protocolData) {
			synchronized (this) {
				pending = false;
				this.msg = msg;
				this.protocolData = protocolData;
				notifyAll();

			}
		}

		/*
		 * (non-Javadoc)
		 * @see
		 * org.ws4d.java.communication.DefaultResponseCallback#handle(org.ws4d
		 * .java.communication.message.Message,
		 * org.ws4d.java.message.eventing.RenewResponseMessage,
		 * org.ws4d.java.communication.ProtocolData)
		 */
		public void handle(Message request, RenewResponseMessage msg, ProtocolData protocolData) {
			releaseMessageSynchronization(msg, protocolData);
		}

		/*
		 * (non-Javadoc)
		 * @see
		 * org.ws4d.java.communication.DefaultResponseCallback#handle(org.ws4d
		 * .java.communication.message.Message, org.ws4d.java.message
		 * .eventing.UnsubscribeResponseMessage,
		 * org.ws4d.java.communication.ProtocolData)
		 */
		public void handle(Message request, UnsubscribeResponseMessage msg, ProtocolData protocolData) {
			releaseMessageSynchronization(msg, protocolData);
		}

		/*
		 * (non-Javadoc)
		 * @see
		 * org.ws4d.java.communication.DefaultResponseCallback#handle(org.ws4d
		 * .java.communication.message.Message, org.ws4d.java.message
		 * .eventing.GetStatusResponseMessage,
		 * org.ws4d.java.communication.ProtocolData)
		 */
		public void handle(Message request, GetStatusResponseMessage msg, ProtocolData protocolData) {
			releaseMessageSynchronization(msg, protocolData);
		}

		/*
		 * (non-Javadoc)
		 * @see
		 * org.ws4d.java.communication.DefaultResponseCallback#getOperation()
		 */
		public OperationDescription getOperation() {
			return op;
		}
	}

}
