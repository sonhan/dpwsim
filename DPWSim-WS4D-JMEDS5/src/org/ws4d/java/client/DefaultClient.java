/*******************************************************************************
 * Copyright (c) 2009 MATERNA Information & Communications. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html. For further
 * project-related information visit http://www.ws4d.org. The most recent
 * version of the JMEDS framework can be obtained from
 * http://sourceforge.net/projects/ws4d-javame.
 ******************************************************************************/

package org.ws4d.java.client;

import java.io.IOException;

import org.ws4d.java.DPWSFramework;
import org.ws4d.java.communication.CommunicationBinding;
import org.ws4d.java.communication.CommunicationManager;
import org.ws4d.java.communication.CommunicationManagerRegistry;
import org.ws4d.java.communication.DefaultIncomingMessageListener;
import org.ws4d.java.communication.DiscoveryBinding;
import org.ws4d.java.communication.ProtocolData;
import org.ws4d.java.communication.monitor.MonitorStreamFactory;
import org.ws4d.java.communication.monitor.MonitoringContext;
import org.ws4d.java.constants.DPWSMessageConstants;
import org.ws4d.java.dispatch.DeviceServiceRegistry;
import org.ws4d.java.dispatch.HelloData;
import org.ws4d.java.dispatch.ServiceReferenceEventRegistry;
import org.ws4d.java.eventing.ClientSubscription;
import org.ws4d.java.eventing.EventListener;
import org.ws4d.java.eventing.EventSink;
import org.ws4d.java.eventing.EventingFactory;
import org.ws4d.java.message.MessageDiscarder;
import org.ws4d.java.message.discovery.HelloMessage;
import org.ws4d.java.service.Device;
import org.ws4d.java.service.Service;
import org.ws4d.java.service.parameter.ParameterValue;
import org.ws4d.java.service.reference.DeviceListener;
import org.ws4d.java.service.reference.DeviceReference;
import org.ws4d.java.service.reference.ServiceListener;
import org.ws4d.java.service.reference.ServiceReference;
import org.ws4d.java.structures.ArrayList;
import org.ws4d.java.structures.DataStructure;
import org.ws4d.java.structures.HashMap;
import org.ws4d.java.structures.Iterator;
import org.ws4d.java.structures.MessageIdBuffer;
import org.ws4d.java.types.EndpointReference;
import org.ws4d.java.types.QNameSet;
import org.ws4d.java.types.ScopeSet;
import org.ws4d.java.types.URI;
import org.ws4d.java.util.Log;
import org.ws4d.java.util.WS4DIllegalStateException;

/**
 * Default DPWS Client implementation. This class provides easy access to
 * several points of interaction within the DPWS framework, such as searching
 * for devices/services, tracking a device's or service's state changes and
 * receiving events from subscribed services.
 * <p>
 * The basic idea behind this class is: it extends several callback interfaces
 * and provides empty implementations for all of their methods, so that an
 * implementing client can easily overwrite those in which it is really
 * interested in.
 * </p>
 * <p>
 * A simple use case of this class could be a client searching for a particular
 * device. This can be accomplished by a call to
 * {@link #searchDevice(SearchParameter)} providing the desired search criteria
 * within the expected <code>SearchParameter</code> argument. The framework will
 * then start looking asynchronously for devices matching those criteria and
 * will invoke {@link #deviceFound(DeviceReference, SearchParameter)} each time
 * a corresponding device is discovered.<br />
 * Searching for services can be done in a similar manner, this time using the
 * method {@link #searchService(SearchParameter)} to initiate the search and
 * receiving results by means of
 * {@link #serviceFound(ServiceReference, SearchParameter)}.
 * </p>
 * <p>
 * When a client starts a {@link #searchDevice(SearchParameter) device search},
 * it is automatically registered as {@link DeviceListener} to any device
 * matching the search criteria. This is especially useful for getting
 * notifications about state changes of the device, such as a
 * {@link #deviceBye(DeviceReference) device shut-down}, an
 * {@link #deviceChanged(DeviceReference) update of a device's metadata}, etc.
 * </p>
 * <p>
 * Listening to service state changes differs from the aforementioned approach.
 * In order to start receiving service update notifications, a client must
 * {@link #registerServiceListening() register} itself for that purpose. It will
 * then be notified about any state change regarding <em>every</em> service the
 * DPWS framework knows about. This also includes any services not explicitly
 * {@link #searchService(SearchParameter) searched for} by this client.
 * </p>
 * <p>
 * A simple client implementation interested in devices providing the
 * <code>ex:Printer</code> port type (where <code>ex</code> is a XML namespace
 * prefix referring to the <code>http://www.example.org/printing</code>
 * namespace) could look like:
 * 
 * <pre>
 * // create a new client
 * Client client = new DefaultClient() {
 * 
 *     // overwrite deviceFound method in order to receive callbacks
 *     public void deviceFound(DeviceReference devRef, SearchParameter search) {
 *         // start interacting with matching device
 *         ...
 *     }
 * 
 * };
 * // describe device port type to look for
 * QName printerType = new QName(&quot;Printer&quot;, &quot;http://www.example.org/printlng&quot;);
 * QNameSet types = new QNameSet(printerType);
 * 
 * // create a search parameter object and store desired type(s) into it
 * SearchParameter criteria = new SearchParameter();
 * criteria.setDeviceTypes(types);
 * 
 * // start the asynchronous search
 * client.searchDevice(criteria);
 * </pre>
 * 
 * </p>
 */
public class DefaultClient implements DeviceListener, ServiceListener, SearchCallback, EventListener, HelloListener {

	private final static int[]	INCOMING_MESSAGE_TYPES	= { DPWSMessageConstants.HELLO_MESSAGE, DPWSMessageConstants.BYE_MESSAGE };

	HashMap						helloReceivers			= null;

	/**
	 * Default constructor. Ensures the DPWS framework is running (see
	 * {@link DPWSFramework#isRunning()}. Throws a
	 * <code>java.lang.RuntimeException</code> if this is not the case.
	 * 
	 * @throws RuntimeException if the DPWS framework is not running; i.e. it
	 *             was either not started by means of
	 *             {@link DPWSFramework#start(String[])} or has already been
	 *             stopped via {@link DPWSFramework#stop()} before calling this
	 *             constructor
	 */
	public DefaultClient() {
		super();
		if (!DPWSFramework.isRunning()) {
			throw new RuntimeException("Client Constructor: DPWSFramework isn't running!");
		}
	}

	public DataStructure getAllDiscoveryBindings() {
		DataStructure bindings = new ArrayList();
		for (Iterator it = CommunicationManagerRegistry.getLoadedManagers(); it.hasNext();) {
			CommunicationManager manager = (CommunicationManager) it.next();
			try {
				bindings.addAll(manager.getDiscoveryBindings());
			} catch (IOException e) {
				Log.printStackTrace(e);
			}
		}
		return bindings;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.ws4d.java.eventing.EventListener#getEventSink(org.ws4d.java.structures
	 * .DataStructure)
	 */
	public EventSink getEventSink(DataStructure bindings) {
		EventingFactory eFac = null;
		try {
			eFac = DPWSFramework.getEventingFactory();
		} catch (IOException e) {
			throw new RuntimeException("Cannot return event sink. Eventing support not found.");
		}
		return eFac.createEventSink(this, bindings);
	}

	/**
	 * Generates an event sink which can be used when registering for event
	 * notifications from a service. The supplied configuration id is supposed
	 * to refer to an EventSink property which contains at least one binding
	 * property to create a {@link CommunicationBinding} instance. This
	 * {@link CommunicationBinding} instance defines a local transport address,
	 * to which incoming notifications will be delivered.
	 * 
	 * @param configurationId Configuration id of the properties of the event
	 *            sink to generate
	 * @return a new event sink
	 */
	public EventSink generateEventSink(int configurationId) {
		EventingFactory eFac = null;
		try {
			eFac = DPWSFramework.getEventingFactory();
		} catch (IOException e) {
			throw new RuntimeException("Cannot return event sink. Eventing support not found.");
		}
		return eFac.createEventSink(this, configurationId);
	}

	// --------------------- DEVICE STATE ----------------------

	/*
	 * (non-Javadoc)
	 * @see
	 * org.ws4d.java.service.reference.DeviceListener#deviceBye(org.ws4d.java
	 * .service.reference.DeviceReference)
	 */
	public void deviceBye(DeviceReference deviceRef) {
		Log.info("Client: Overwrite deviceBye() to receive device status changes");
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.ws4d.java.service.reference.DeviceListener#deviceCompletelyDiscovered
	 * (org.ws4d.java.service.reference.DeviceReference)
	 */
	public void deviceCompletelyDiscovered(DeviceReference deviceRef) {
		Log.info("Client: Overwrite deviceCompletelyDiscovered() to receive device status changes");
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.ws4d.java.service.reference.DeviceListener#deviceChanged(org.ws4d
	 * .java.service.reference.DeviceReference)
	 */
	public void deviceChanged(DeviceReference deviceRef) {
		Log.info("Client: Overwrite deviceChanged() to receive device status changes");
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.ws4d.java.service.reference.DeviceListener#deviceRunning(org.ws4d
	 * .java.service.reference.DeviceReference)
	 */
	public void deviceRunning(DeviceReference deviceRef) {
		Log.info("Client: Overwrite deviceRunning() to receive device status changes");
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.ws4d.java.service.reference.DeviceListener#deviceBuiltUp(org.ws4d
	 * .java.service.reference.DeviceReference, org.ws4d.java.service.Device)
	 */
	public void deviceBuiltUp(DeviceReference deviceRef, Device device) {
		Log.info("Client: Overwrite deviceBuiltUp() to receive device status changes");
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.ws4d.java.service.reference.DeviceListener#deviceNotResponding(org
	 * .ws4d.java.service.reference.DeviceReference)
	 */
	public void deviceCommunicationErrorOrReset(DeviceReference deviceRef) {
		Log.info("Client: Overwrite deviceCommunicationErrorOrReset() to receive device status changes");
	}

	// --------------------- SERVICE CHANGE LISTENING ------------------------

	/*
	 * (non-Javadoc)
	 * @see
	 * org.ws4d.java.service.reference.ServiceListener#serviceChanged(org.ws4d
	 * .java.service.reference.ServiceReference, org.ws4d.java.service.Service)
	 */
	public void serviceChanged(ServiceReference serviceRef, Service service) {
		Log.info("Client: Overwrite serviceChanged() to receive service status changes");
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.ws4d.java.service.reference.ServiceListener#serviceCreated(org.ws4d
	 * .java.service.reference.ServiceReference, org.ws4d.java.service.Service)
	 */
	public void serviceCreated(ServiceReference serviceRef, Service service) {
		Log.info("Client: Overwrite serviceCreated() to receive service status changes");
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.ws4d.java.service.reference.ServiceListener#onServiceDisposed(org
	 * .ws4d.java.service.reference.ServiceReference)
	 */
	public void serviceDisposed(ServiceReference serviceRef) {
		Log.info("Client: Overwrite serviceDisposed() to receive service status changes");
	}

	/**
	 * Registers client for service reference changes. Client gets information
	 * about service changes.
	 * 
	 * @see ServiceListener
	 */
	public void registerServiceListening() {
		ServiceReferenceEventRegistry.getInstance().registerServiceListening(this);
	}

	/**
	 * Unregisters service listening for this service client. This method should
	 * be called, if holder of reference is no longer interested in this
	 * reference.
	 * 
	 * @see ServiceListener
	 */
	public void unregisterServiceListening() {
		ServiceReferenceEventRegistry.getInstance().unregisterServiceListening(this);
	}

	// ---------------------- DISCOVERY ------------------------

	/**
	 * Gets device reference of device with specified endpoint reference. This
	 * <code>Client</code> instance will be used as callback for device changes
	 * of the corresponding device.
	 * 
	 * @param deviceEpr endpoint reference of device to get device reference for
	 * @return device reference
	 * @see SearchManager#getDeviceReference(EndpointReference, DeviceListener)
	 */
	public DeviceReference getDeviceReference(EndpointReference deviceEpr, DiscoveryBinding binding) {
		/*
		 * we won't send resolve messages, let it be done by user with
		 * devRef.getDevice()
		 */
		return SearchManager.getDeviceReference(deviceEpr, this, binding);
	}

	public DeviceReference getDeviceReference(EndpointReference deviceEpr) {
		/*
		 * we won't send resolve messages, let it be done by user with
		 * devRef.getDevice()
		 */
		return SearchManager.getDeviceReference(deviceEpr, this, null);
	}

	/**
	 * Gets device reference of device which sent the specified hello data. This
	 * <code>Client</code> instance will be used as callback for device changes
	 * of the corresponding device.
	 * 
	 * @param helloData Hello data received from
	 *            {@link #helloReceived(HelloData)} callback method.
	 * @return device reference
	 * @see SearchManager#getDeviceReference(HelloData, DeviceListener)
	 */
	public DeviceReference getDeviceReference(HelloData helloData) {
		/*
		 * we won't send resolve messages, let it be done by user with
		 * devRef.getDevice()
		 */
		return SearchManager.getDeviceReference(helloData, this);
	}

	/**
	 * Gets service reference of service with specified endpoint reference.
	 * 
	 * @param serviceEpr endpoint reference of service to get service reference
	 *            for
	 * @param comManId ID of the communication manager to use when interacting
	 *            with supplied endpoint reference
	 * @return service reference
	 * @see SearchManager#getServiceReference(EndpointReference)
	 */
	public ServiceReference getServiceReference(EndpointReference serviceEpr, String comManId) {
		/*
		 * we won't send GetMetadata messages, let it be done by user with
		 * servRef.getService()
		 */
		return SearchManager.getServiceReference(serviceEpr, comManId);
	}

	/**
	 * Shorthand method for searching devices. Expect search results
	 * asynchronously within this client instance's
	 * {@link #deviceFound(DeviceReference, SearchParameter)} method.
	 * 
	 * @param search search criteria
	 * @see SearchManager#searchDevice(SearchParameter, SearchCallback,
	 *      DeviceListener)
	 */
	public void searchDevice(SearchParameter search) {
		SearchManager.searchDevice(search, this, this);
	}

	/**
	 * Searches for services. Uses search parameter to specify the search.
	 * Obtained results will be delivered asynchronously to this client
	 * instance's {@link #serviceFound(ServiceReference, SearchParameter)}
	 * method.
	 * 
	 * @param search search parameter to specify the search for device and
	 *            service
	 * @see SearchManager#searchService(SearchParameter, SearchCallback)
	 */
	public void searchService(SearchParameter search) {
		SearchManager.searchDevice(search, this, this);
	}

	/**
	 * Registers for incoming HelloMessages for all possible domains.
	 * <p>
	 * This method will check every {@link CommunicationManager} registered
	 * inside the framework and registers all discovery domains found with
	 * {@link #registerHelloListening(CommunicationBinding)}.
	 * </p>
	 * <p>
	 * The client will be used as receiver for the incoming Hello messages.
	 * </p>
	 */
	public synchronized void registerHelloListening() {
		try {
			Iterator mans = CommunicationManagerRegistry.getLoadedManagers();
			while (mans.hasNext()) {
				CommunicationManager manager = (CommunicationManager) mans.next();
				DataStructure bindings = manager.getDiscoveryBindings();
				if (bindings != null) {
					Iterator binds = bindings.iterator();
					while (binds.hasNext()) {
						DiscoveryBinding binding = (DiscoveryBinding) binds.next();
						registerHelloListening(binding);
					}
				}
			}
		} catch (IOException e) {
			Log.error("Cannot register for incoming wsd:Hello messages. " + e.getMessage());
		}

	}

	/**
	 * Registers for incoming HelloMessages for all possible domains, with
	 * specified types and scopes ({@link SearchParameter}).
	 * <p>
	 * {@link #helloReceived(HelloData)} is called to deliver the hello data.
	 * </p>
	 * <p>
	 * This method will check every {@link CommunicationManager} registered
	 * inside the framework and registers all discovery domains found with
	 * {@link #registerHelloListening(CommunicationBinding)}.
	 * </p>
	 * <p>
	 * The client will be used as receiver for the incoming Hello messages.
	 * </p>
	 * 
	 * @param search containing the types and scopes.
	 */
	public synchronized void registerHelloListening(SearchParameter search) {
		try {
			Iterator mans = CommunicationManagerRegistry.getLoadedManagers();
			while (mans.hasNext()) {
				CommunicationManager manager = (CommunicationManager) mans.next();
				DataStructure bindings = manager.getDiscoveryBindings();
				if (bindings != null) {
					Iterator binds = bindings.iterator();
					while (binds.hasNext()) {
						DiscoveryBinding binding = (DiscoveryBinding) binds.next();
						registerHelloListening(search, binding);
					}
				}
			}
		} catch (IOException e) {
			Log.error("Cannot register for incoming wsd:Hello messages. " + e.getMessage());
		}

	}

	/**
	 * Registers for incoming Hello messages.
	 * <p>
	 * {@link #helloReceived(HelloData)} is called to deliver the hello data.
	 * </p>
	 * *
	 * <p>
	 * The client will be used as receiver for the incoming Hello messages.
	 * </p>
	 * 
	 * @param binding the binding for the listener.
	 */
	public synchronized void registerHelloListening(DiscoveryBinding binding) {
		SearchParameter any = new SearchParameter();
		registerHelloListening(any, binding);
	}

	/**
	 * Registers for incoming Hello messages, which matches to the specified
	 * types and scopes ({@link SearchParameter}).
	 * <p>
	 * {@link #helloReceived(HelloData)} is called to deliver the hello data.
	 * </p>
	 * <p>
	 * The client will be used as receiver for the incoming Hello messages.
	 * </p>
	 * 
	 * @param search containing the types and scopes.
	 * @param binding the binding for the listener.
	 */
	public void registerHelloListening(SearchParameter search, DiscoveryBinding binding) {
		registerHelloListening(search, binding, this);
	}

	/**
	 * Registers for incoming HelloMessages, which matches to the specified
	 * types and scopes ({@link SearchParameter}).
	 * <p>
	 * {@link #helloReceived(HelloData)} is called to deliver the hello data.
	 * </p>
	 * 
	 * @param search containing the types and scopes.
	 * @param binding the binding for the listener.
	 * @param helloListener the listener to receive the hello data from matching
	 *            hello messages.
	 */
	public synchronized void registerHelloListening(final SearchParameter search, DiscoveryBinding binding, HelloListener helloListener) {
		if (helloReceivers == null) {
			helloReceivers = new HashMap(3);
		}

		HelloRegisterKey key = new HelloRegisterKey(search, binding);

		HelloReceiver helloReceiver = new HelloReceiver(helloListener == null ? this : helloListener, search);

		if (helloReceivers.containsKey(key)) {
			return;
		}

		helloReceivers.put(key, helloReceiver);

		try {
			CommunicationManager manager = CommunicationManagerRegistry.getManager(binding.getCommunicationManagerId());
			manager.registerDiscovery(INCOMING_MESSAGE_TYPES, binding, helloReceiver);
		} catch (WS4DIllegalStateException e) {
			throw new RuntimeException(e.getMessage());
		} catch (IOException e) {
			throw new RuntimeException(e.getMessage());
		}
		
		DeviceServiceRegistry.incAppSequenceUser();
	}

	/**
	 * Unregisters the listeners for incoming hello messages according to the
	 * given {@link DiscoveryBinding}.
	 * 
	 * @param binding the binding for the listener.
	 */
	public synchronized void unregisterHelloListening(DiscoveryBinding binding) {
		SearchParameter any = new SearchParameter();
		unregisterHelloListening(any, binding);
	}

	/**
	 * Unregisters the listeners for incoming hello messages according to the
	 * given {@link SearchParameter} and ALL {@link DiscoveryBinding} found.
	 * 
	 * @param search containing the types and scopes.
	 */
	public synchronized void unregisterHelloListening(SearchParameter search) {
		try {
			Iterator mans = CommunicationManagerRegistry.getLoadedManagers();
			while (mans.hasNext()) {
				CommunicationManager manager = (CommunicationManager) mans.next();
				DataStructure bindings = manager.getDiscoveryBindings();
				if (bindings != null) {
					Iterator binds = bindings.iterator();
					while (binds.hasNext()) {
						DiscoveryBinding binding = (DiscoveryBinding) binds.next();
						unregisterHelloListening(search, binding);
					}
				}
			}
		} catch (IOException e) {
			Log.error("Cannot unregister for incoming wsd:Hello messages. " + e.getMessage());
		}

	}

	/**
	 * Unregisters the listeners for incoming hello messages according to the
	 * given {@link SearchParameter} and {@link DiscoveryBinding}.
	 * 
	 * @param search containing the types and scopes.
	 * @param binding the binding for the listener.
	 */
	public synchronized void unregisterHelloListening(SearchParameter search, DiscoveryBinding binding) {
		if (helloReceivers != null && helloReceivers.size() > 0) {

			HelloRegisterKey key = new HelloRegisterKey(search, binding);

			HelloReceiver helloReceiver = (HelloReceiver) helloReceivers.remove(key);

			if (helloReceiver == null) return;

			try {
				CommunicationManager manager = CommunicationManagerRegistry.getManager(binding.getCommunicationManagerId());

				manager.unregisterDiscovery(INCOMING_MESSAGE_TYPES, binding, helloReceiver);

			} catch (WS4DIllegalStateException e) {
				throw new RuntimeException(e.getMessage());
			} catch (IOException e) {
				throw new RuntimeException(e.getMessage());
			}
			
			DeviceServiceRegistry.decAppSequenceUser();
		}
	}

	/**
	 * @param helloData
	 */
	public void helloReceived(HelloData helloData) {
		Log.info("Client: Overwrite helloReceived() to receive and handle the UUIDs of new HelloMessages");
	}

	// --------------------- SEARCH CALLBACKS ------------------

	/*
	 * (non-Javadoc)
	 * @see
	 * org.ws4d.java.client.SearchCallback#onDeviceFound(org.ws4d.java.service
	 * .reference.DeviceReference, org.ws4d.java.client.SearchParameter)
	 */
	public void deviceFound(DeviceReference devRef, SearchParameter search) {
		Log.info("Client: Overwrite deviceFound() to receive device discovery responses");
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.ws4d.java.client.SearchCallback#onServiceFound(org.ws4d.java.service
	 * .reference.ServiceReference, org.ws4d.java.client.SearchParameter)
	 */
	public void serviceFound(ServiceReference servRef, SearchParameter search) {
		Log.info("Client: Overwrite serviceFound() to receive service discovery responses");
	}

	// --------------------- EVENT CALLBACKS ---------------------

	/*
	 * (non-Javadoc)
	 * @see
	 * org.ws4d.java.client.EventListener#receiveEvent(org.ws4d.java.eventing
	 * .ClientSubscription, org.ws4d.java.types.uri.URI,
	 * org.ws4d.java.service.ParameterValue)
	 */
	public ParameterValue eventReceived(ClientSubscription subscription, URI actionURI, ParameterValue parameterValue) {
		Log.info("Client: Overwrite eventReceived() to receive and handle events");
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.ws4d.java.client.EventListener#receiveSubscriptionEnd(org.ws4d.java
	 * .eventing.ClientSubscription, org.ws4d.java.types.uri.URI)
	 */
	public void subscriptionEndReceived(ClientSubscription subscription, URI reason) {
		Log.info("Client: Overwrite subscriptionEndReceived() to receive and handle end of subscriptions");
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.ws4d.java.client.EventListener#receiveSubscriptionTimeout(org.ws4d
	 * .java.eventing.ClientSubscription)
	 */
	public void subscriptionTimeoutReceived(ClientSubscription subscription) {
		Log.info("Client: Overwrite subscriptionTimeoutReceived() to receive and handle subscription timeouts");
	}

	/**
	 * This class helps to generate hash codes for a pair of
	 * {@link SearchParameter} and {@link DiscoveryBinding}.
	 */
	private final class HelloRegisterKey {

		private final SearchParameter	search;

		private final DiscoveryBinding	binding;

		public HelloRegisterKey(SearchParameter search, DiscoveryBinding binding) {
			this.search = search;
			this.binding = binding;
		}

		/*
		 * (non-Javadoc)
		 * @see java.lang.Object#hashCode()
		 */
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result + ((binding == null) ? 0 : binding.hashCode());
			result = prime * result + ((search == null) ? 0 : search.hashCode());
			return result;
		}

		/*
		 * (non-Javadoc)
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		public boolean equals(Object obj) {
			if (this == obj) return true;
			if (obj == null) return false;
			if (getClass() != obj.getClass()) return false;
			HelloRegisterKey other = (HelloRegisterKey) obj;
			if (!getOuterType().equals(other.getOuterType())) return false;
			if (binding == null) {
				if (other.binding != null) return false;
			} else if (!binding.equals(other.binding)) return false;
			if (search == null) {
				if (other.search != null) return false;
			} else if (!search.equals(other.search)) return false;
			return true;
		}

		private DefaultClient getOuterType() {
			return DefaultClient.this;
		}

	}

	private final class HelloReceiver extends DefaultIncomingMessageListener {

		private final MessageIdBuffer	messageIdBuffer		= new MessageIdBuffer();

		private final SearchParameter	search;

		private final HelloListener		helloListener;

		public HelloReceiver(HelloListener helloListener, SearchParameter search) {
			this.helloListener = helloListener;
			this.search = search;
		}

		public void handle(HelloMessage hello, ProtocolData protocolData) {
			QNameSet inTypes = hello.getDiscoveryData().getTypes();
			QNameSet searchTypes = search.getDeviceTypes();
			ScopeSet inScopes = hello.getScopes();
			ScopeSet searchScopes = search.getScopes();
			boolean typesMatch = false;
			boolean scopesMatch = false;

			/*
			 * contains types? or no types searched?
			 */
			if ((searchTypes != null && inTypes != null && inTypes.containsAll(searchTypes)) || searchTypes == null) {
				typesMatch = true;
			}

			int reason = MessageDiscarder.NOT_DISCARDED;

			/*
			 * contains scopes? or no scopes searched?
			 */
			if ((searchScopes != null && inScopes != null && inScopes.containsAll(searchScopes)) || searchScopes == null) {
				scopesMatch = true;
			}

			if (!typesMatch && !scopesMatch) {
				if (Log.isDebug()) Log.debug("Discarding Hello message! Message does not match the search criteria!", Log.DEBUG_LAYER_APPLICATION);
				reason = MessageDiscarder.NOT_RELEVANT_MESSAGE;
			}

			if (messageIdBuffer.containsOrEnqueue(hello.getMessageId())) {
				if (Log.isDebug()) Log.debug("Discarding Hello message! Already saw this message ID!", Log.DEBUG_LAYER_APPLICATION);
				reason = MessageDiscarder.DUPLICATE_MESSAGE;
			}

			if (reason > MessageDiscarder.NOT_DISCARDED) {
				MonitorStreamFactory msf = DPWSFramework.getMonitorStreamFactory();
				if (msf != null) {
					MonitoringContext context = msf.getMonitoringContextIn(protocolData);
					if (context != null) {
						msf.discard(protocolData, context, reason);
					} else {
						Log.warn("Cannot get correct monitoring context for message generation.");
					}
				}
				/*
				 * Message discarded
				 */
				return;
			}
			
			helloListener.helloReceived(new HelloData(hello, protocolData));
		}
	}

}
