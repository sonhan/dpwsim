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
import org.ws4d.java.communication.CommunicationBinding;
import org.ws4d.java.communication.CommunicationManager;
import org.ws4d.java.communication.CommunicationManagerRegistry;
import org.ws4d.java.communication.CommunicationUtil;
import org.ws4d.java.communication.DefaultIncomingMessageListener;
import org.ws4d.java.communication.ProtocolData;
import org.ws4d.java.communication.TimeoutException;
import org.ws4d.java.communication.protocol.http.HTTPGroup;
import org.ws4d.java.communication.protocol.http.HTTPUser;
import org.ws4d.java.configuration.ServiceProperties;
import org.ws4d.java.configuration.ServicesPropertiesHandler;
import org.ws4d.java.constants.ConstantsHelper;
import org.ws4d.java.constants.DPWSMessageConstants;
import org.ws4d.java.dispatch.DeviceServiceRegistry;
import org.ws4d.java.dispatch.ServiceReferenceInternal;
import org.ws4d.java.eventing.ClientSubscription;
import org.ws4d.java.eventing.ClientSubscriptionInternal;
import org.ws4d.java.eventing.EventSink;
import org.ws4d.java.eventing.EventSource;
import org.ws4d.java.eventing.EventingException;
import org.ws4d.java.eventing.EventingFactory;
import org.ws4d.java.eventing.SubscriptionManager;
import org.ws4d.java.message.FaultMessage;
import org.ws4d.java.message.InvokeMessage;
import org.ws4d.java.message.SOAPException;
import org.ws4d.java.message.eventing.GetStatusMessage;
import org.ws4d.java.message.eventing.GetStatusResponseMessage;
import org.ws4d.java.message.eventing.RenewMessage;
import org.ws4d.java.message.eventing.RenewResponseMessage;
import org.ws4d.java.message.eventing.SubscribeMessage;
import org.ws4d.java.message.eventing.SubscribeResponseMessage;
import org.ws4d.java.message.eventing.UnsubscribeMessage;
import org.ws4d.java.message.eventing.UnsubscribeResponseMessage;
import org.ws4d.java.message.metadata.GetMetadataMessage;
import org.ws4d.java.message.metadata.GetMetadataResponseMessage;
import org.ws4d.java.schema.Element;
import org.ws4d.java.schema.Schema;
import org.ws4d.java.schema.SchemaUtil;
import org.ws4d.java.service.parameter.ParameterValue;
import org.ws4d.java.service.reference.DeviceReference;
import org.ws4d.java.service.reference.ServiceReference;
import org.ws4d.java.structures.ArrayList;
import org.ws4d.java.structures.DataStructure;
import org.ws4d.java.structures.EmptyStructures;
import org.ws4d.java.structures.HashMap;
import org.ws4d.java.structures.HashMap.Entry;
import org.ws4d.java.structures.HashSet;
import org.ws4d.java.structures.Iterator;
import org.ws4d.java.structures.ReadOnlyIterator;
import org.ws4d.java.structures.Set;
import org.ws4d.java.types.EndpointReference;
import org.ws4d.java.types.EprInfo;
import org.ws4d.java.types.EprInfoSet;
import org.ws4d.java.types.HostMData;
import org.ws4d.java.types.HostedMData;
import org.ws4d.java.types.QName;
import org.ws4d.java.types.QNameSet;
import org.ws4d.java.types.RelationshipMData;
import org.ws4d.java.types.URI;
import org.ws4d.java.types.URISet;
import org.ws4d.java.util.Log;
import org.ws4d.java.util.StringUtil;
import org.ws4d.java.util.WS4DIllegalStateException;
import org.ws4d.java.wsdl.IOType;
import org.ws4d.java.wsdl.UnsupportedBindingException;
import org.ws4d.java.wsdl.WSDL;
import org.ws4d.java.wsdl.WSDLBinding;
import org.ws4d.java.wsdl.WSDLMessage;
import org.ws4d.java.wsdl.WSDLMessagePart;
import org.ws4d.java.wsdl.WSDLOperation;
import org.ws4d.java.wsdl.WSDLPortType;
import org.ws4d.java.wsdl.WSDLRepository;
import org.ws4d.java.wsdl.WSDLService;
import org.ws4d.java.wsdl.soap12.SOAP12DocumentLiteralHTTPBinding;
import org.ws4d.java.wsdl.soap12.SOAP12DocumentLiteralHTTPPort;

/**
 * Default implementation of a DPWS service.
 * <p>
 * This class should be used to create a DPWS service. A new service should
 * extend the <code>DefaultService</code> class and add operations to the newly
 * created service. It is also possible to use the default implementation.
 * </p>
 * 
 * <pre>
 * 
 * 
 * 
 * 
 * 
 * 
 * 
 * 
 * public class SampleService extends DefaultService {
 * 
 * 	public SampleService() {
 * 		// create operations here, add them to the service
 * 	}
 * }
 * </pre>
 */
public class DefaultService extends ServiceCommons implements LocalService {

	protected static final int[]		SERVICE_MESSAGE_TYPES			= { DPWSMessageConstants.GET_METADATA_MESSAGE, DPWSMessageConstants.INVOKE_MESSAGE };

	protected static final int[]		EVENTED_SERVICE_MESSAGE_TYPES	= { DPWSMessageConstants.GET_METADATA_MESSAGE, DPWSMessageConstants.SUBSCRIBE_MESSAGE, DPWSMessageConstants.GET_STATUS_MESSAGE, DPWSMessageConstants.RENEW_MESSAGE, DPWSMessageConstants.UNSUBSCRIBE_MESSAGE, DPWSMessageConstants.INVOKE_MESSAGE };

	protected static final byte			SERVICE_STATE_UNREGISTERED		= 1;

	protected static final byte			SERVICE_STATE_REGISTERED		= 2;

	protected static final byte			SERVICE_STATE_RUNNING			= 3;

	// ADDED 2010-08-11 SSch Added string constants and set most of them to
	// empty strings
	// "Message"
	protected static final String		IN_MSG_POSTFIX					= "Message";

	// "Message" Response
	protected static final String		OUT_MSG_POSTFIX					= "Message";

	// "Message" Response
	protected static final String		FAULT_MSG_POSTFIX				= "Message";

	protected static final String		BINDING_POSTFIX					= "Binding";

	/** Configuration identifier */
	protected int						configurationId;

	protected ServiceMessageListener	incomingListener				= new ServiceMessageListener();

	protected final HostedMData			hosted							= new HostedMData();

	protected ServiceReference			serviceReference				= null;

	protected LocalDevice				parentDevice					= null;

	protected final ServiceProperties	serviceProp;

	// key = CommunicationBinding, value = HashSet of URIs
	protected final HashMap				wsdlURIs						= new HashMap();

	// key = CommunicationBinding, value = HashSet of URIs
	protected final HashMap				resourceURIs					= new HashMap();

	protected byte						state							= SERVICE_STATE_UNREGISTERED;

	protected SubscriptionManager		subscriptionManager				= null;

	protected final DataStructure		bindings;

	protected String					sid								= null;

	/** Authentication */

	private HTTPGroup						userGroup						= null;

	private static ResourcePath createResourcePath(String namespace, String resourceSuffix) {
		URI nsUri = new URI(namespace);
		String host = nsUri.getHost();
		String path = nsUri.getPath();
		if (nsUri.isURN()) {
			path = path.replace(':', '_');
		}
		String nsPath = (host == null ? "" : host) + path + ((path.charAt(path.length() - 1) == '/' ? "" : "/") + resourceSuffix);

		int depth = 0;
		int idx = nsPath.indexOf('/');
		while (idx != -1) {
			if (idx != 0) {
				depth++;
			}
			idx = nsPath.indexOf('/', idx + 1);
		}

		return new ResourcePath(nsPath, depth);
	}

	/**
	 * Default DPWS service.
	 * <p>
	 * No configuration identifier used. No configuration will be loaded for
	 * this service.
	 * </p>
	 */
	public DefaultService() {
		this(-1);
	}

	/**
	 * Default DPWS service with given configuration identifier.
	 * <p>
	 * Creates an default DPWS service and tries to load the configuration
	 * properties for the service.
	 * </p>
	 * 
	 * @param configurationId configuration identifier.
	 */
	public DefaultService(int configurationId) {
		super();
		hosted.setTypes(QNameSet.newInstanceReadOnly(portTypes.keySet()));
		this.configurationId = configurationId;
		if (this.configurationId != -1) {
			Integer cid = new Integer(configurationId);
			serviceProp = ServicesPropertiesHandler.getInstance().getServiceProperties(cid);

			if (serviceProp == null) {
				Log.error("DefaultService(configurationId): No service properties for configuration id " + configurationId);
				bindings = new ArrayList(2);
			} else {
				URI sid = serviceProp.getServiceId();
				if (sid != null) {
					setServiceId(sid);
				}

				bindings = serviceProp.getBindings();
				// if (bindings.size() > 0) {
				// for (Iterator it = bindings.iterator(); it.hasNext();) {
				// CommunicationBinding binding = (CommunicationBinding)
				// it.next();
				// EndpointReference epr = new
				// EndpointReference(binding.getTransportAddress());
				// hosted.addEndpointReference(epr);
				// }
				// }
			}
		} else {
			serviceProp = null;
			bindings = new ArrayList(2);

		}

		if (serviceProp != null && serviceProp.isServiceSecured() && DPWSFramework.hasModule(DPWSFramework.SECURITY_MODULE)) {
			try {
				this.setSecureService();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		sid = StringUtil.simpleClassName(getClass());
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.service.LocalService#start()
	 */
	public synchronized void start() throws IOException {
		if (!DPWSFramework.isRunning()) {
			throw new RuntimeException("DPWSFramework not running, please start it in advance!");
		}
		if (Log.isDebug()) {
			Log.info("### Start Service: " + sid);
		}
		if (isRunning()) {
			Log.warn("Service already running, nothing to start");
			return;
		}
		if (state == SERVICE_STATE_UNREGISTERED) {
			if (hosted.getServiceId() == null) {
				hosted.setServiceId(new URI(sid));
			}

			int[] messageTypes = SERVICE_MESSAGE_TYPES;
			for (Iterator it = portTypes.values().iterator(); it.hasNext();) {
				PortType portType = (PortType) it.next();
				portType.plomb();
				if (portType.hasEventSources()) {
					messageTypes = EVENTED_SERVICE_MESSAGE_TYPES;
				}
			}
			if (!hasBindings()) {
				String descriptor = StringUtil.simpleClassName(getClass());
				if (Log.isDebug()) {
					Log.info("No bindings found for Service. Autobinding service " + descriptor);
				}
				DataStructure autoBindings = new HashSet();
				for (Iterator it = CommunicationManagerRegistry.getLoadedManagers(); it.hasNext();) {
					CommunicationManager manager = (CommunicationManager) it.next();
					manager.getAutobindings(descriptor, autoBindings);
				}
				for (Iterator it = autoBindings.iterator(); it.hasNext();) {
					CommunicationBinding binding = (CommunicationBinding) it.next();
					addBinding(binding);
				}
			}
			for (Iterator it = getBindings(); it.hasNext();) {
				CommunicationBinding binding = (CommunicationBinding) it.next();
				CommunicationManager manager = CommunicationManagerRegistry.getManager(binding.getCommunicationManagerId());
				manager.registerService(messageTypes, binding, incomingListener, userGroup);
				EndpointReference eRef = new EndpointReference(binding.getTransportAddress());
				hosted.addEprInfo(new EprInfo(eRef, binding.getCommunicationManagerId()));
			}

			if (this.secure) {
				URI addr = ((EndpointReference) this.getEprInfos().next()).getAddress();
				String alias = addr.getHost() + ":" + addr.getPort() + addr.getPath();
				if ((this.certificate = DPWSFramework.getSecurityManager().getCertificate(alias)) == null) {
					throw new IOException("Security credentials not found");
				}
				this.privateKey = DPWSFramework.getSecurityManager().getPrivateKey(alias, null);

				this.secure = true;
			}

			DeviceServiceRegistry.register(this);

			deployMetadataResources();
			state = SERVICE_STATE_REGISTERED;
		}

		if (Log.isInfo()) {
			Iterator it = hosted.getEprInfoSet().iterator();
			StringBuffer sBuf = new StringBuffer();
			while (it.hasNext()) {
				EprInfo epr = (EprInfo) it.next();
				sBuf.append(epr.getEndpointReference().getAddress());
				if (it.hasNext()) {
					sBuf.append(", ");
				}
			}
			Log.info("Service [ " + sBuf + " ] online.");
		}

		state = SERVICE_STATE_RUNNING;
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.service.LocalService#stop()
	 */
	public synchronized void stop() throws IOException {
		if (state == SERVICE_STATE_UNREGISTERED) {
			return;
		}
		if (subscriptionManager != null) {
			subscriptionManager.sendSubscriptionEnd();
		}
		undeployMetadataResources();
		int[] messageTypes = SERVICE_MESSAGE_TYPES;
		for (Iterator it = portTypes.values().iterator(); it.hasNext();) {
			PortType portType = (PortType) it.next();
			if (portType.hasEventSources()) {
				messageTypes = EVENTED_SERVICE_MESSAGE_TYPES;
				break;
			}
		}
		DeviceServiceRegistry.unregister(this);
		for (Iterator it = bindings.iterator(); it.hasNext();) {
			CommunicationBinding binding = (CommunicationBinding) it.next();
			EndpointReference eRef = new EndpointReference(binding.getTransportAddress());
			hosted.getEprInfoSet().remove(new EprInfo(eRef, null, binding.getCommunicationManagerId()));
			CommunicationManager manager = CommunicationManagerRegistry.getManager(binding.getCommunicationManagerId());
			manager.unregisterService(messageTypes, binding, incomingListener);
		}
		state = SERVICE_STATE_UNREGISTERED;
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.service.LocalService#pause()
	 */
	public synchronized void pause() {
		state = SERVICE_STATE_REGISTERED;
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.service.LocalService#isRunning()
	 */
	public synchronized boolean isRunning() {
		return state == SERVICE_STATE_RUNNING;
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.service.Service#getServiceReference()
	 */
	public ServiceReference getServiceReference() {
		if (serviceReference == null) {
			serviceReference = DeviceServiceRegistry.getUpdatedServiceReference(hosted, getParentDeviceReference(), CommunicationManagerRegistry.getDefault(), null);
			((ServiceReferenceInternal) serviceReference).setService(this, hosted);
		}

		return serviceReference;
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
	public synchronized boolean supportsBindingChanges() {
		return state == SERVICE_STATE_UNREGISTERED;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.ws4d.java.communication.Bindable#addBinding(org.ws4d.java.communication
	 * .CommunicationBinding)
	 */
	public void addBinding(CommunicationBinding binding) throws WS4DIllegalStateException {
		exclusiveLock();
		try {
			bindings.add(binding);
			// if (bindings.add(binding)) {
			// EndpointReference epr = new
			// EndpointReference(binding.getTransportAddress());
			// hosted.addEndpointReference(epr);
			// }
		} finally {
			releaseExclusiveLock();
		}
	}

	/*
	 * (non-Javadoc)
	 * @seeorg.ws4d.java.communication.Bindable#removeBinding(org.ws4d.java.
	 * communication.CommunicationBinding)
	 */
	public boolean removeBinding(CommunicationBinding binding) throws WS4DIllegalStateException {
		exclusiveLock();
		try {
			return bindings.remove(binding);
			// if (result) {
			// EndpointReference epr = new
			// EndpointReference(binding.getTransportAddress());
			// hosted.getEndpointReferences().remove(epr);
			// wsdlURIs.remove(binding);
			// resourceURIs.remove(binding);
			// }
			// return result;
		} finally {
			releaseExclusiveLock();
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.communication.Bindable#clearBindings()
	 */
	public void clearBindings() throws WS4DIllegalStateException {
		exclusiveLock();
		try {
			// for (Iterator it = bindings.iterator(); it.hasNext();) {
			// CommunicationBinding binding = (CommunicationBinding) it.next();
			// EndpointReference epr = new
			// EndpointReference(binding.getTransportAddress());
			// hosted.getEndpointReferences().remove(epr);
			// it.remove();
			// wsdlURIs.remove(binding);
			// resourceURIs.remove(binding);
			// }
			bindings.clear();
		} finally {
			releaseExclusiveLock();
		}
	}

	/**
	 * Creates a shared lock for this service. If the service has a parent
	 * device, the lock is acquired from the device.
	 */
	protected void sharedLock() {
		if (parentDevice == null) {
			return;
		}

		parentDevice.sharedLock();
	}

	/**
	 * Creates a exclusive lock for this service. If the service has a parent
	 * device, the lock is acquired from the device.
	 */
	protected void exclusiveLock() {
		if (state != SERVICE_STATE_UNREGISTERED) {
			throw new RuntimeException("Service must not be changed while running!");
		}
		if (parentDevice == null) {
			return;
		}

		parentDevice.exclusiveLock();
	}

	/**
	 * Releases a shared lock for this service. If the service has a parent
	 * device, the lock is released from the device.
	 */
	protected void releaseSharedLock() {
		if (parentDevice == null) {
			return;
		}

		parentDevice.releaseSharedLock();
	}

	/**
	 * Releases a exclusive lock for this service. If the service has a parent
	 * device, the lock is released from the device.
	 */
	protected void releaseExclusiveLock() {
		if (state != SERVICE_STATE_UNREGISTERED) {
			throw new RuntimeException("Service must not be changed while running!");
		}
		if (parentDevice == null) {
			return;
		}
		parentDevice.releaseExclusiveLock();
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.service.Service#isRemote()
	 */
	public boolean isRemote() {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.service.ServiceCommons#getServiceId()
	 */
	public URI getServiceId() {
		sharedLock();
		try {
			URI serviceId = hosted.getServiceId();
			if (serviceId == null) {
				serviceId = new URI(sid);
				exclusiveLock();
				try {
					hosted.setServiceId(serviceId);
				} finally {
					releaseExclusiveLock();
				}
			}
			return serviceId;
		} finally {
			releaseSharedLock();
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.service.Service#getOperations()
	 */
	public Iterator getOperations() {
		sharedLock();
		try {
			return super.getOperations();
		} finally {
			releaseSharedLock();
		}
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.ws4d.java.service.Service#getOperations(org.ws4d.java.types.QName)
	 */
	public Iterator getOperations(QName portType) {
		sharedLock();
		try {
			return super.getOperations(portType);
		} finally {
			releaseSharedLock();
		}
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.ws4d.java.service.ServiceCommons#getOperation(org.ws4d.java.types.
	 * QName, java.lang.String, java.lang.String, java.lang.String)
	 */
	public Operation getOperation(QName portType, String opName, String inputName, String outputName) {
		if (opName == null) {
			return null;
		}
		sharedLock();
		try {
			return super.getOperation(portType, opName, inputName, outputName);
		} finally {
			releaseSharedLock();
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.service.Service#getOperation(java.lang.String)
	 */
	public Operation getOperation(String inputAction) {
		if (inputAction == null) {
			return null;
		}
		sharedLock();
		try {
			return super.getOperation(inputAction);
		} finally {
			releaseSharedLock();
		}
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.ws4d.java.service.ServiceCommons#getAnyOperation(org.ws4d.java.types
	 * .QName, java.lang.String)
	 */
	public Operation getAnyOperation(QName portType, String operationName) {
		if (operationName == null) {
			return null;
		}
		sharedLock();
		try {
			return super.getAnyOperation(portType, operationName);
		} finally {
			releaseSharedLock();
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.service.ServiceCommons#getEventSources()
	 */
	public Iterator getEventSources() {
		sharedLock();
		try {
			return super.getEventSources();
		} finally {
			releaseSharedLock();
		}
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.ws4d.java.service.ServiceCommons#getEventSources(org.ws4d.java.types
	 * .QName)
	 */
	public Iterator getEventSources(QName portType) {
		sharedLock();
		try {
			return super.getEventSources(portType);
		} finally {
			releaseSharedLock();
		}
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.ws4d.java.service.ServiceCommons#getEventSource(org.ws4d.java.types
	 * .QName, java.lang.String, java.lang.String, java.lang.String)
	 */
	public EventSource getEventSource(QName portType, String eventName, String inputName, String outputName) {
		if (eventName == null) {
			return null;
		}
		sharedLock();
		try {
			return super.getEventSource(portType, eventName, inputName, outputName);
		} finally {
			releaseSharedLock();
		}
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.ws4d.java.service.ServiceCommons#getEventSource(java.lang.String)
	 */
	public EventSource getEventSource(String outputAction) {
		if (outputAction == null) {
			return null;
		}
		sharedLock();
		try {
			return super.getEventSource(outputAction);
		} finally {
			releaseSharedLock();
		}
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.ws4d.java.service.ServiceCommons#getAnyEventSource(org.ws4d.java.
	 * data.QName, java.lang.String)
	 */
	public EventSource getAnyEventSource(QName portType, String eventName) {
		if (eventName == null) {
			return null;
		}
		sharedLock();
		try {
			return super.getAnyEventSource(portType, eventName);
		} finally {
			releaseSharedLock();
		}
	}

	/**
	 * Sets the service identifier for this service.
	 * <p>
	 * The service identifier identifies the service uniquely for the parent
	 * device.
	 * 
	 * @param serviceId the service identifier to set.
	 */
	public void setServiceId(URI serviceId) {
		exclusiveLock();
		try {
			hosted.setServiceId(serviceId);
		} finally {
			releaseExclusiveLock();
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.service.Service#getPortTypes()
	 */
	public Iterator getPortTypes() {
		QNameSet s = hosted.getTypes();
		return s == null ? EmptyStructures.EMPTY_ITERATOR : new ReadOnlyIterator(s.iterator());
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.ws4d.java.service.LocalService#addPortType(org.ws4d.java.types.QName)
	 */
	public void addPortType(QName portTypeName) {
		if (portTypes.containsKey(portTypeName)) {
			return;
		}
		// null values not aloud within portTyps map!
		portTypes.put(portTypeName, new PortType());
	}


	/* (non-Javadoc)
	 * @see org.ws4d.java.service.Service#getEprInfos()
	 */
	public Iterator getEprInfos() {
		EprInfoSet s = hosted.getEprInfoSet();
		return s == null ? EmptyStructures.EMPTY_ITERATOR : new ReadOnlyIterator(s.iterator());
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.ws4d.java.service.LocalService#addOperation(org.ws4d.java.service
	 * .Operation)
	 */
	public void addOperation(Operation operation) {
		// Check for necessary stuff.
		if (operation == null) {
			throw new NullPointerException("operation is null");
		}
		exclusiveLock();
		try {
			QName portType = operation.getPortType();
			OperationSignature signature = new OperationSignature(operation);
			// Add operation to port type table.
			PortType type = (PortType) portTypes.get(portType);
			if (type == null) {
				type = new PortType();
				portTypes.put(portType, type);
			} else {
				if (type.isPlombed()) {
					throw new WS4DIllegalStateException("Operations can not be added to an existing port type after a service has been started once");
				}
				String inputName = operation.getInputName();
				String outputName = operation.getOutputName();
				int inputCounter = 1;
				int outputCounter = 1;
				while (type.contains(signature)) {
					if (operation.isInputNameSet()) {
						if (operation.isOneWay() || operation.isOutputNameSet()) {
							throw new IllegalArgumentException("duplicate operation or event: " + operation);
						} else {
							operation.setOutputNameInternal(outputName + outputCounter++);
						}
					} else {
						operation.setInputNameInternal(inputName + inputCounter++);
					}

					signature = new OperationSignature(operation);
				}
			}
			// check for duplicate input action
			String inputAction = operation.getInputAction();
			if (operations.containsKey(inputAction)) {
				if (operation.isInputActionSet() || operation.isOneWay()) {
					throw new IllegalArgumentException("duplicate input action: " + inputAction);
				}
				inputAction = operation.setExtendedDefaultInputAction();
				if (operations.containsKey(inputAction)) {
					throw new IllegalArgumentException("duplicate input action: " + inputAction);
				}
			}
			type.addOperation(signature, operation);
			// add operation with wsa:Action of input for faster access
			operations.put(operation.getInputAction(), operation);
			operation.setService(this);

			if (Log.isDebug()) {
				Log.debug("[NEW OPERATION]: " + operation.toString(), Log.DEBUG_LAYER_APPLICATION);
			}
		} finally {
			releaseExclusiveLock();
		}
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.ws4d.java.service.LocalService#addEventSource(org.ws4d.java.service
	 * .DefaultEventSource)
	 */
	public void addEventSource(EventSource event) {
		if (event == null) {
			throw new RuntimeException("Cannot add event to service. No event given.");
		}
		if (!(event instanceof OperationCommons)) {
			throw new RuntimeException("Cannot add event to service. Given event MUST extend the operation class.");
		}
		EventingFactory eFac = null;
		try {
			eFac = DPWSFramework.getEventingFactory();
		} catch (IOException e) {
			throw new RuntimeException(e.getMessage());
		}
		exclusiveLock();
		try {
			QName portType = event.getPortType();
			OperationSignature signature = new OperationSignature(event);
			// add event to port type table
			PortType type = (PortType) portTypes.get(portType);
			if (type == null) {
				type = new PortType();
				portTypes.put(portType, type);
			} else {
				if (type.isPlombed()) {
					throw new WS4DIllegalStateException("Events can not be added to an existing port type after a service has been started once");
				}
				String outputName = event.getOutputName();
				String inputName = event.getInputName();
				int outputCounter = 1;
				int inputCounter = 1;
				while (type.contains(signature)) {
					if (((OperationCommons) event).isOutputNameSet()) {
						if (event.isNotification() || ((OperationCommons) event).isInputNameSet()) {
							throw new IllegalArgumentException("duplicate operation or event: " + event);
						} else {
							((OperationCommons) event).setInputNameInternal(inputName + inputCounter++);
						}
					} else {
						((OperationCommons) event).setOutputNameInternal(outputName + outputCounter++);
					}

					signature = new OperationSignature(event);
				}
			}
			// check for duplicate output action
			String outputAction = event.getOutputAction();
			if (events.containsKey(outputAction)) {
				if (((OperationCommons) event).isOutputActionSet() || event.isNotification()) {
					throw new IllegalArgumentException("duplicate output action: " + outputAction);
				}
				outputAction = ((OperationCommons) event).setExtendedDefaultOutputAction();
				if (events.containsKey(outputAction)) {
					throw new IllegalArgumentException("duplicate output action: " + outputAction);
				}
			}
			type.addEventSource(signature, event);
			// add event with wsa:Action of output for faster access
			events.put(event.getOutputAction(), event);
			if (subscriptionManager == null) {

				subscriptionManager = eFac.getSubscriptionManager(this);
			}
			((OperationCommons) event).setService(this);

			if (Log.isDebug()) {
				Log.debug("[NEW EVENT SOURCE]: " + event.toString(), Log.DEBUG_LAYER_APPLICATION);
			}

		} finally {
			releaseExclusiveLock();
		}
	}

	/**
	 * Gets configuration identifier.
	 * <p>
	 * The configuration identifier is necessary to resolve properties based
	 * configuration.
	 * </p>
	 * 
	 * @return the configuration identifier.
	 */
	public int getConfigurationID() {
		return configurationId;
	}

	/**
	 * Sets the parent device for this service.
	 * <p>
	 * Every service is assigned to one device.
	 * </p>
	 * 
	 * @param devicet the device which the service should be assigned to.
	 */
	public void setParentDevice(LocalDevice device) {
		parentDevice = device;
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.service.Service#getParentDeviceReference()
	 */
	public DeviceReference getParentDeviceReference() {
		if (parentDevice == null) {
			return null;
		}
		return parentDevice.getDeviceReference();
	}

	/**
	 * Registers all WSDL and XML Schema files to the internal resource server
	 * (e.g. HTTP).
	 */
	protected void deployMetadataResources() {
		try {
			String resourcesBasePath = "ws4d/resources/";
			/*
			 * get target namespaces for this service.
			 */
			Set targetNamespaces = new HashSet(portTypes.size() * 2);
			HashMap copy = new HashMap();
			for (Iterator it = portTypes.keySet().iterator(); it.hasNext();) {
				QName key = (QName) it.next();
				String targetNamespace = key.getNamespace();
				if (targetNamespace.equals("")) {
					QName renew = new QName(key.getLocalPart(), this.parentDevice.getDefaultNamespace(), key.getPrefix(), key.getPriority());
					PortType p = (PortType) portTypes.get(key);
					copy.put(renew, p);
					// portTypes.remove(key);
					for (Iterator i = p.getOperations(); i.hasNext();) {
						Operation o = (Operation) i.next();
						// for input
						Element inputElement = o.getInput();
						if (inputElement != null && inputElement.getName() != null) {
							QName pre = inputElement.getName();
							if (pre.getNamespace().equals("")) {
								QName post = new QName(pre.getLocalPart(), this.parentDevice.getDefaultNamespace(), pre.getPrefix(), pre.getPriority());
								o.getInput().setName(post);
							}
						}
						// for output
						Element outputElement = o.getOutput();
						if (outputElement != null && outputElement.getName() != null) {
							QName pre = outputElement.getName();
							if (pre.getNamespace().equals("")) {
								QName post = new QName(pre.getLocalPart(), this.parentDevice.getDefaultNamespace(), pre.getPrefix(), pre.getPriority());
								outputElement.setName(post);
							}
						}
					}
				} else {
					PortType p = (PortType) portTypes.get(key);
					copy.put(key, p);
				}
				targetNamespaces.add(targetNamespace);

			}
			portTypes.clear();
			portTypes.putAll(copy);

			/*
			 * register at HTTP server.
			 */
			Iterator targets = targetNamespaces.iterator();
			while (targets.hasNext()) {
				String targetNamespace = (String) targets.next();
				if (targetNamespace.equals("")) {
					targetNamespace = this.parentDevice.getDefaultNamespace();
				}
				WSDL wsdl = getDescription(targetNamespace);
				if (!wsdls.containsKey(targetNamespace)) {
					/*
					 * this is an embedded, i.e. linked-in WSDL, we shouldn't
					 * export it as top-level
					 */
					continue;
				}

				ResourcePath wsdlPath = createResourcePath(targetNamespace, "description.wsdl");

				Iterator bit = bindings.iterator();
				while (bit.hasNext()) {
					CommunicationBinding binding = (CommunicationBinding) bit.next();

					CommunicationManager manager = CommunicationManagerRegistry.getManager(binding.getCommunicationManagerId());
					// TODO: Group user
					URI uri = manager.registerResource(wsdl, binding, resourcesBasePath + wsdlPath.path, null);

					Set uris = (Set) wsdlURIs.get(binding);
					if (uris == null) {
						uris = new HashSet();
						wsdlURIs.put(binding, uris);
					}
					uris.add(uri);

					uris = (Set) resourceURIs.get(binding);
					if (uris == null) {
						uris = new HashSet();
						resourceURIs.put(binding, uris);
					}
					uris.add(uri);

					if (Log.isDebug()) {
						Log.debug("Service [ WSDL = " + uri + " ]", Log.DEBUG_LAYER_APPLICATION);
					}

					recurseLinkedWsdls(wsdl, binding, resourcesBasePath, wsdlPath.depth);

					for (Iterator it = wsdl.getTypes(); it.hasNext();) {
						Schema schema = (Schema) it.next();
						recurseLinkedSchemas(schema, binding, resourcesBasePath, wsdlPath.depth);
					}
				}
			}
		} catch (IOException e) {
			Log.warn("No HTTP Server found. Cannot register WSDL for download.");
		}
	}

	private void recurseLinkedWsdls(WSDL wsdl, CommunicationBinding binding, String resourcesBasePath, int depth) throws IOException {
		for (Iterator it = wsdl.getLinkedWsdls(); it.hasNext();) {
			WSDL linkedWsdl = (WSDL) it.next();
			String targetNamespace = linkedWsdl.getTargetNamespace();
			ResourcePath wsdlPath = createResourcePath(targetNamespace, "description.wsdl");
			String location = wsdlPath.path;
			for (int i = 0; i < depth; i++) {
				location = "../" + location;
			}
			wsdl.addImport(targetNamespace, location);
			CommunicationManager manager = CommunicationManagerRegistry.getManager(binding.getCommunicationManagerId());
			// TODO: Group user
			URI uri = manager.registerResource(linkedWsdl, binding, resourcesBasePath + wsdlPath.path, null);

			Set uris = (Set) resourceURIs.get(binding);
			if (uris == null) {
				uris = new HashSet();
				resourceURIs.put(binding, uris);
			}
			uris.add(uri);
			if (Log.isDebug()) {
				Log.debug("Service [ WSDL = " + uri + " ]", Log.DEBUG_LAYER_APPLICATION);
			}
			recurseLinkedWsdls(linkedWsdl, binding, resourcesBasePath, wsdlPath.depth);
		}
	}

	protected void recurseLinkedSchemas(Schema schema, CommunicationBinding binding, String resourcesBasePath, int depth) throws IOException {
		DataStructure deployedNamespaces = new HashSet();
		recurseLinkedSchemas(schema, binding, resourcesBasePath, depth, deployedNamespaces);
	}

	protected void recurseLinkedSchemas(Schema schema, CommunicationBinding binding, String resourcesBasePath, int depth, DataStructure deployedNamespaces) throws IOException {
		for (Iterator it = schema.getLinkedSchemas(); it.hasNext();) {
			Schema linkedSchema = (Schema) it.next();
			String targetNamespace = linkedSchema.getTargetNamespace();
			ResourcePath schemaPath = createResourcePath(targetNamespace, "schema.xsd");
			String location = schemaPath.path;
			for (int i = 0; i < depth; i++) {
				location = "../" + location;
			}
			schema.addImport(targetNamespace, location);
			if (deployedNamespaces.contains(targetNamespace)) {
				continue;
			}
			CommunicationManager manager = CommunicationManagerRegistry.getManager(binding.getCommunicationManagerId());
			// TODO: Group user
			URI uri = manager.registerResource(linkedSchema, binding, resourcesBasePath + schemaPath.path, null);

			deployedNamespaces.add(targetNamespace);
			Set uris = (Set) resourceURIs.get(binding);
			if (uris == null) {
				uris = new HashSet();
				resourceURIs.put(binding, uris);
			}
			uris.add(uri);
			if (Log.isDebug()) {
				Log.debug("Service [ Schema = " + uri + " ]", Log.DEBUG_LAYER_APPLICATION);
			}
			recurseLinkedSchemas(linkedSchema, binding, resourcesBasePath, schemaPath.depth, deployedNamespaces);
		}
	}

	protected void undeployMetadataResources() {
		for (Iterator it = bindings.iterator(); it.hasNext();) {
			CommunicationBinding binding = (CommunicationBinding) it.next();
			wsdlURIs.remove(binding);
			Set uris = (HashSet) resourceURIs.remove(binding);
			if (uris != null) {
				for (Iterator it2 = uris.iterator(); it2.hasNext();) {
					URI uri = (URI) it2.next();
					try {
						CommunicationManager manager = CommunicationManagerRegistry.getManager(binding.getCommunicationManagerId());
						manager.unregisterResource(uri, binding);
					} catch (IOException e) {
						Log.printStackTrace(e);
					}
				}
			}
		}
	}

	/**
	 * Returns the namespaces based on the port types for this service.
	 * 
	 * @return the namespaces based on the port types for this service.
	 */
	public Iterator getTargetNamespaces() {
		Set ts = new HashSet();
		for (Iterator it = portTypes.keySet().iterator(); it.hasNext();) {
			QName key = (QName) it.next();
			ts.add(key.getNamespace());
		}
		return new ReadOnlyIterator(ts);
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.service.LocalService#getDescriptionsForPortTypes()
	 */
	public Iterator getDescriptionsForPortTypes() {
		Iterator targetNamespaces = getTargetNamespaces();
		Set wsdls = new HashSet();
		while (targetNamespaces.hasNext()) {
			String namespace = (String) targetNamespaces.next();
			wsdls.add(getDescription(namespace));
		}
		return new ReadOnlyIterator(wsdls);
	}

	/**
	 * Returns a WSDL document describing this service by the given namespace.
	 * 
	 * @param targetNamespace the namespace.
	 * @return the WSDL document describing this service by the given namespace.
	 */
	public WSDL getDescription(String targetNamespace) {
		WSDL wsdl = getExistingDescription(targetNamespace);
		if (wsdl != null) {
			addServiceAndPortsIfMissing(wsdl);
			return wsdl;
		}

		/*
		 * we have a WSDL instance for each distinct namespace within our
		 * service types
		 */
		wsdl = new WSDL(targetNamespace);
		// wsdl.addTypes(SchemaUtil.createSchema(this));
		// CHANGED 2010-08-11 SSch There may be a set of schemas not only one
		HashMap schemaList = SchemaUtil.createSchema(this, targetNamespace);
		Iterator schemasIt = schemaList.entrySet().iterator();
		while (schemasIt.hasNext()) {
			Entry entry = (Entry) schemasIt.next();
			wsdl.addTypes((Schema) entry.getValue());
		}

		/*
		 * Time to create the WSDL document for this service. No change allowed
		 * if the service is running.
		 */
		Set ptypes = portTypes.entrySet();
		Iterator ptit = ptypes.iterator();
		while (ptit.hasNext()) {
			Entry entry = (Entry) ptit.next();
			QName portTypeName = (QName) entry.getKey();
			String namespace = portTypeName.getNamespace();
			if (!targetNamespace.equals(namespace)) {
				// skip port types from other target namespaces
				continue;
			}

			PortType type = (PortType) entry.getValue();

			WSDLPortType portType = new WSDLPortType(portTypeName);

			if (type.hasAttributes()) {
				portType.setAttributes(type.getAttributes());
			}

			Iterator opit = type.getOperations();
			while (opit.hasNext()) {
				/*
				 * Get the next operation.
				 */
				Operation operation = (Operation) opit.next();
				/*
				 * Create a WSDL operation and add it to the actual port type.
				 */
				String operationName = operation.getName();
				WSDLOperation wsdlOperation = new WSDLOperation(operationName);

				if (operation.hasAttributes()) {
					wsdlOperation.setAttributes(operation.getAttributes());
				}

				/*
				 * Create the input/output message names.
				 */
				String inputName = operation.getInputName();
				QName inMsgName = new QName(inputName + IN_MSG_POSTFIX, namespace);
				IOType inputIO = new IOType(inMsgName);

				if (operation.hasInputAttributes()) {
					inputIO.setAttributes(operation.getInputAttributes());
				}

				// check whether auto-generated or set
				if (operation.isInputNameSet()) {
					inputIO.setName(inputName);
				}
				if (operation.isInputActionSet() || operation.isInputActionExtended()) {
					inputIO.setAction(operation.getInputAction());
				}
				WSDLMessage wsdlMessageInput = new WSDLMessage(inMsgName);
				Element input = operation.getInput();
				if (input != null) {
					WSDLMessagePart part = new WSDLMessagePart();
					part.setElementName(input.getName());
					wsdlMessageInput.addPart(part);
				}
				/*
				 * in case there are no input parameters, we add an empty
				 * message (with no parts) to WSDL operation
				 */
				wsdl.addMessage(wsdlMessageInput);
				wsdlOperation.setInput(inputIO);

				if (operation.isRequestResponse()) {
					String outputName = operation.getOutputName();
					QName outMsgName = new QName(outputName + OUT_MSG_POSTFIX, namespace);
					IOType outputIO = new IOType(outMsgName);

					if (operation.hasOutputAttributes()) {
						outputIO.setAttributes(operation.getOutputAttributes());
					}

					// check whether auto-generated or set
					if (operation.isOutputNameSet()) {
						outputIO.setName(outputName);
					}
					if (operation.isOutputActionSet()) {
						outputIO.setAction(operation.getOutputAction());
					}
					/*
					 * we always include an output message for real operations,
					 * although their output element may be null
					 */
					WSDLMessage wsdlMessageOutput = new WSDLMessage(outMsgName);
					Element output = operation.getOutput();
					if (output != null) {
						WSDLMessagePart part = new WSDLMessagePart();
						part.setElementName(output.getName());
						wsdlMessageOutput.addPart(part);
					}
					wsdl.addMessage(wsdlMessageOutput);
					wsdlOperation.setOutput(outputIO);
				}
				// add fault IOTypes and action URIs
				for (Iterator it = operation.getFaults(); it.hasNext();) {
					Fault fault = (Fault) it.next();

					String faultName = fault.getName();
					QName faultMsgName = new QName(operationName + faultName + FAULT_MSG_POSTFIX, namespace);
					IOType faultIO = new IOType(faultMsgName);

					if (fault.hasAttributes()) {
						faultIO.setAttributes(fault.getAttributes());
					}

					// check whether auto-generated or set
					faultIO.setName(faultName);
					String action = fault.getAction();
					if (action != null) {
						faultIO.setAction(action);
					}
					WSDLMessage wsdlMessageFault = new WSDLMessage(faultMsgName);
					Element faultElement = fault.getElement();
					if (faultElement != null) {
						WSDLMessagePart part = new WSDLMessagePart();
						part.setElementName(faultElement.getName());
						wsdlMessageFault.addPart(part);
					}
					wsdl.addMessage(wsdlMessageFault);
					wsdlOperation.addFault(faultIO);
				}

				portType.addOperation(wsdlOperation);
			}

			Iterator evit = type.getEventSources();
			while (evit.hasNext()) {
				/*
				 * Get the next event.
				 */
				OperationCommons event = (OperationCommons) evit.next();
				/*
				 * Create a WSDL operation and add it to the actual port type.
				 */
				String eventName = event.getName();
				portType.setEventSource(true);
				WSDLOperation wsdlOperation = new WSDLOperation(eventName);

				if (event.hasAttributes()) {
					wsdlOperation.setAttributes(event.getAttributes());
				}

				/*
				 * Create the input/output message names.
				 */
				String outputName = event.getOutputName();
				QName outMsgName = new QName(outputName + OUT_MSG_POSTFIX, namespace);
				IOType outputIO = new IOType(outMsgName);

				if (event.hasOutputAttributes()) {
					outputIO.setAttributes(event.getOutputAttributes());
				}

				// check whether auto-generated or set
				if (event.isOutputNameSet()) {
					outputIO.setName(outputName);
				}
				if (event.isOutputActionSet() || event.isOutputActionExtended()) {
					outputIO.setAction(event.getOutputAction());
				}
				WSDLMessage wsdlMessageOutput = new WSDLMessage(outMsgName);
				Element output = event.getOutput();
				if (output != null) {
					WSDLMessagePart part = new WSDLMessagePart();
					part.setElementName(output.getName());
					wsdlMessageOutput.addPart(part);
				}
				/*
				 * in case there are no output parameters, we add an empty
				 * message (with no parts) to WSDL operation
				 */
				wsdl.addMessage(wsdlMessageOutput);
				wsdlOperation.setOutput(outputIO);
				if (((EventSource) event).isSolicitResponse()) {
					String inputName = event.getInputName();
					QName inMsgName = new QName(inputName + IN_MSG_POSTFIX, namespace);
					IOType inputIO = new IOType(inMsgName);

					if (event.hasInputAttributes()) {
						inputIO.setAttributes(event.getInputAttributes());
					}

					// check whether auto-generated or set
					if (event.isInputNameSet()) {
						inputIO.setName(inputName);
					}
					if (event.isInputActionSet()) {
						inputIO.setAction(event.getInputAction());
					}
					/*
					 * we always include an input message for real operations,
					 * although their input element may be null
					 */
					WSDLMessage wsdlMessageInput = new WSDLMessage(inMsgName);
					Element input = event.getInput();
					if (input != null) {
						WSDLMessagePart part = new WSDLMessagePart();
						part.setElementName(input.getName());
						wsdlMessageInput.addPart(part);
					}
					wsdl.addMessage(wsdlMessageInput);
					wsdlOperation.setInput(inputIO);
				}
				// add fault IOTypes and action URIs
				for (Iterator it = event.getFaults(); it.hasNext();) {
					Fault fault = (Fault) it.next();

					String faultName = fault.getName();
					QName faultMsgName = new QName(eventName + faultName + FAULT_MSG_POSTFIX, namespace);
					IOType faultIO = new IOType(faultMsgName);

					if (fault.hasAttributes()) {
						faultIO.setAttributes(fault.getAttributes());
					}

					// check whether auto-generated or set
					faultIO.setName(fault.getName());
					String action = fault.getAction();
					if (action != null) {
						faultIO.setAction(action);
					}
					WSDLMessage wsdlMessageFault = new WSDLMessage(faultMsgName);
					Element faultElement = fault.getElement();
					if (faultElement != null) {
						WSDLMessagePart part = new WSDLMessagePart();
						part.setElementName(faultElement.getName());
						wsdlMessageFault.addPart(part);
					}
					wsdl.addMessage(wsdlMessageFault);
					wsdlOperation.addFault(faultIO);
				}

				portType.addOperation(wsdlOperation);
			}

			wsdl.addPortType(portType);
			wsdl.addBinding(new SOAP12DocumentLiteralHTTPBinding(new QName(portTypeName.getLocalPart() + BINDING_POSTFIX, namespace), portTypeName));
		}
		wsdls.put(targetNamespace, wsdl);
		addServiceAndPortsIfMissing(wsdl);
		return wsdl;
	}

	private void addServiceAndPortsIfMissing(WSDL wsdl) {
		if (wsdl == null) {
			return;
		}
		WSDLService service = wsdl.getService(sid);
		if (service == null) {
			service = new WSDLService(new QName(sid, wsdl.getTargetNamespace()));
			try {
				wsdl.addService(service);
			} catch (UnsupportedBindingException e) {
				// shouldn't ever occur
			}
		}
		for (Iterator bindings = wsdl.getBindings(); bindings.hasNext();) {
			WSDLBinding binding = (WSDLBinding) bindings.next();
			WSDLPortType bindingPortType = binding.getPortType();
			if (service.containsPortsForBinding(binding.getName())) {
				continue;
			}
			int suffix = 0;
			String basePortName = bindingPortType.getLocalName() + "Port";
			for (Iterator eprs = getEprInfos(); eprs.hasNext();) {
				EprInfo epr = (EprInfo) eprs.next();
				SOAP12DocumentLiteralHTTPPort port = new SOAP12DocumentLiteralHTTPPort(basePortName + suffix++, binding.getName());
				port.setLocation(epr.getXAddress());
				service.addPort(port);
			}
		}
	}

	/**
	 * Enables dynamic service creation from an existing WSDL description.
	 * <p>
	 * This method analyzes the WSDL loaded from <code>wsdlUri</code> and adds
	 * all supported port types found to this service. For each supported
	 * operation (i.e. either one-way or request-response transmission types),
	 * an instance of class {@link OperationStub} is created and added, whereas
	 * for each event source (aka. notification or solicit-response transmission
	 * types) an instance of class {@link DefaultEventSource} is added.
	 * </p>
	 * <p>
	 * The actual business logic of imported one-way or request-response
	 * operations can be specified on the corresponding {@link OperationStub}
	 * instance after having obtained it from this service via one of the
	 * <code>getOperation(...)</code> methods like this:
	 * 
	 * <pre>
	 * DefaultService myService = ...;
	 * myService.define(&quot;http://www.example.org/myService/description.wsdl&quot;);
	 * 
	 * InvokeDelegate myDelegate = ...;
	 * 
	 * Operation myOp = (OperationStub) myService.getOperation(&quot;http://www.example.org/MyServicePortType/MyOperation&quot;);
	 * myOp.setDelegate(myDelegate);
	 * </pre>
	 * 
	 * The {@link InvokeDelegate} instance above defines the actual code to be
	 * executed when the <code>myOperation</code> gets called. Its
	 * {@link InvokeDelegate#invoke(Operation, ParameterValue)} method receives
	 * the parameters sent to the operation, as well as the operation instance
	 * itself. The latter is useful for implementors who want to share a single
	 * {@link InvokeDelegate} instance between different operations.
	 * </p>
	 * <p>
	 * Note that the cast to {@link OperationStub} above is only safe if the
	 * operation being obtained was actually created via a call to this
	 * {@link #define(URI)} method - in any other case, e.g. when it was added
	 * manually by means of {@link #addOperation(Operation)}, this cast will
	 * most likely result in a <code>java.lang.ClassCastException</code>.
	 * </p>
	 * 
	 * @param wsdlUri URI pointing to the location of the WSDL document to
	 *            define this service from; the URI may have an arbitrary schema
	 *            (e.g. file, http, https, etc.) as long as there is runtime
	 *            support available for accessing it within the DPWS framework,
	 *            see {@link DPWSFramework#getResourceAsStream(URI)}
	 * @throws IOException if a failure occurs while attempting to obtain the
	 *             WSDL from the given {@link URI}
	 */
	public void define(URI wsdlUri) throws IOException {
		WSDL wsdl = WSDLRepository.loadWsdl(wsdlUri);
		define(wsdl);
	}

	/**
	 * Enables dynamic service creation from an existing WSDL description.
	 * <p>
	 * This method analyzes the WSDL loaded from <code>wsdlUri</code> and adds
	 * all supported port types found to this service. For each supported
	 * operation (i.e. either one-way or request-response transmission types),
	 * an instance of class {@link OperationStub} is created and added, whereas
	 * for each event source (aka. notification or solicit-response transmission
	 * types) an instance of class {@link DefaultEventSource} is added.
	 * </p>
	 * <p>
	 * The actual business logic of imported one-way or request-response
	 * operations can be specified on the corresponding {@link OperationStub}
	 * instance after having obtained it from this service via one of the
	 * <code>getOperation(...)</code> methods like this:
	 * 
	 * <pre>
	 * DefaultService myService = ...;
	 * myService.define(&quot;http://www.example.org/myService/description.wsdl&quot;);
	 * 
	 * InvokeDelegate myDelegate = ...;
	 * 
	 * Operation myOp = (OperationStub) myService.getOperation(&quot;http://www.example.org/MyServicePortType/MyOperation&quot;);
	 * myOp.setDelegate(myDelegate);
	 * </pre>
	 * 
	 * The {@link InvokeDelegate} instance above defines the actual code to be
	 * executed when the <code>myOperation</code> gets called. Its
	 * {@link InvokeDelegate#invoke(Operation, ParameterValue)} method receives
	 * the parameters sent to the operation, as well as the operation instance
	 * itself. The latter is useful for implementors who want to share a single
	 * {@link InvokeDelegate} instance between different operations.
	 * </p>
	 * <p>
	 * Note that the cast to {@link OperationStub} above is only safe if the
	 * operation being obtained was actually created via a call to this
	 * {@link #define(URI)} method - in any other case, e.g. when it was added
	 * manually by means of {@link #addOperation(Operation)}, this cast will
	 * most likely result in a <code>java.lang.ClassCastException</code>.
	 * </p>
	 * 
	 * @param wsdl the WSDL object which should be used to define the serivce.
	 * @throws IOException if a failure occurs while attempting to obtain the
	 *             WSDL from the given {@link URI}
	 */
	public void define(WSDL wsdl) throws IOException {
		Iterator it = wsdl.getSupportedPortTypes().iterator();
		if (!it.hasNext()) {
			Log.warn("WSDL doesn't contain any supported port types.");
		} else {
			while (it.hasNext()) {
				WSDLPortType portType = (WSDLPortType) it.next();
				processWSDLPortType(portType);
			}
			/*
			 * BUGFIX for SF 3043032: no subscription manager for event sources
			 * defined via WSDL
			 */
			EventingFactory eFac = null;
			try {
				eFac = DPWSFramework.getEventingFactory();
			} catch (IOException e) {
				throw new RuntimeException(e.getMessage());
			}
			if (!events.isEmpty() && subscriptionManager == null) {
				subscriptionManager = eFac.getSubscriptionManager(this);
			}
		}
		// wsdl.serialize(System.err);
		// System.err.println();
		wsdls.put(wsdl.getTargetNamespace(), wsdl);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.ws4d.java.service.ServiceCommons#createOperation(org.ws4d.java.wsdl
	 * .WSDLOperation)
	 */
	protected Operation createOperation(WSDLOperation wsdlOperation) {
		return new OperationStub(wsdlOperation);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.ws4d.java.service.Service#subscribe(org.ws4d.java.eventing.EventSink,
	 * java.lang.String, org.ws4d.java.types.uri.URISet, long)
	 */
	public ClientSubscription subscribe(EventSink sink, String clientSubscriptionId, URISet eventActionURIs, long duration) throws EventingException, TimeoutException {
		ClientSubscription subscription = null;
		if (subscriptionManager != null) {
			subscription = subscriptionManager.subscribe(sink, clientSubscriptionId, eventActionURIs, duration);
			sink.addSubscription(clientSubscriptionId, subscription);

		}

		return subscription;
	}

	/*
	 * (non-Javadoc)
	 * @seeorg.ws4d.java.service.Service#unsubscribe(org.ws4d.java.eventing.
	 * ClientSubscription)
	 */
	public void unsubscribe(ClientSubscription subscription) throws EventingException, TimeoutException {
		((ClientSubscriptionInternal) subscription).dispose();
		if (subscriptionManager != null) {
			subscriptionManager.unsubscribe(subscription);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.ws4d.java.service.Service#renew(org.ws4d.java.eventing.ClientSubscription
	 * , long)
	 */
	public long renew(ClientSubscription subscription, long duration) throws EventingException, TimeoutException {
		if (subscriptionManager != null) {
			long newDuration = subscriptionManager.renew(subscription, duration);
			((ClientSubscriptionInternal) subscription).renewInternal(newDuration);
		}
		return 0;
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.service.Service#getStatus(org.ws4d.java.eventing.
	 * ClientSubscription)
	 */
	public long getStatus(ClientSubscription subscription) throws EventingException, TimeoutException {
		if (subscriptionManager != null) {
			return subscriptionManager.getStatus(subscription);
		}
		return 0L;
	}

	public void addUser(HTTPUser user) {
		if (userGroup == null) {
			userGroup = new HTTPGroup();
		}
		userGroup.addUser(user);
	}
	
	public void addGroup(HTTPGroup group){
		if(userGroup == null){
			userGroup = group;
		} else {
			//TODO mehrere Gruppen hinzufügen können
		}
	}

	protected class ServiceMessageListener extends DefaultIncomingMessageListener {

		protected ServiceMessageListener() {
			super();
		}

		/*
		 * (non-Javadoc)
		 * @see
		 * org.ws4d.java.communication.DefaultIncomingMessageListener#handle
		 * (org.
		 * ws4d.java.communication.message.metadataexchange.GetMetadataMessage,
		 * org.ws4d.java.communication.ProtocolData)
		 */
		public GetMetadataResponseMessage handle(GetMetadataMessage getMetadata, ProtocolData protocolData) throws SOAPException {
			if (!isRunning()) {
				// send Fault wsa:ServiceUnavailable
				throw new SOAPException(FaultMessage.createEndpointUnavailableFault(getMetadata));
			}
			GetMetadataResponseMessage response = new GetMetadataResponseMessage(protocolData.getCommunicationManagerId());
			response.setResponseTo(getMetadata);

			// set DPWSVersion from the Request to the Response
			response.setProtocolInfo(getMetadata.getProtocolInfo());

			sharedLock();
			try {
				if (parentDevice != null) {
					RelationshipMData relationship = new RelationshipMData();

					// the host part
					HostMData host = new HostMData();
					host.setEndpointReference(parentDevice.getEndpointReference());
					QNameSet types = new QNameSet();
					for (Iterator it = parentDevice.getPortTypes(); it.hasNext();) {
						QName type = (QName) it.next();
						types.add(type);
					}
					host.setTypes(types);
					relationship.setHost(host);

					// HostedMData hosted = new HostedMData();
					/*
					 * Filter endpoint references which are not transport
					 * addresses. DPWS specification 2.5 R0042
					 */
					Iterator eprsCurrent = getEprInfos();
					EprInfoSet eprsFiltered = new EprInfoSet();
					while (eprsCurrent.hasNext()) {
						EprInfo epr = (EprInfo) eprsCurrent.next();
						if (epr.getXAddress() != null) {
							eprsFiltered.add(epr);
						}
					}
					hosted.setEprInfoSet(eprsFiltered);
					Iterator typesCurrent = getPortTypes();
					QNameSet typesFilled = new QNameSet();
					while (typesCurrent.hasNext()) {
						QName name = (QName) typesCurrent.next();
						typesFilled.add(name);
					}
					hosted.setTypes(typesFilled);

					if (hosted.getServiceId() == null) {
						hosted.setServiceId(new URI(sid));
					}

					relationship.addHosted(hosted);

					CommunicationManager comMan = DPWSFramework.getCommunicationManager(protocolData.getCommunicationManagerId());
					CommunicationUtil comUtil = comMan.getCommunicationUtil();
					ConstantsHelper helper = comUtil.getHelper(protocolData.getProtocolInfo().getVersion());

					response.addRelationship(relationship, helper);
				}

				for (Iterator it = wsdlURIs.values().iterator(); it.hasNext();) {
					Set uris = (Set) it.next();
					if (uris == null) {
						continue;
					}
					for (Iterator it2 = uris.iterator(); it2.hasNext();) {
						URI uri = (URI) it2.next();
						if (protocolData.destinationMatches(uri)) {
							response.addMetadataLocation(uri);
						}
					}
				}
			} finally {
				releaseSharedLock();
			}
			return response;
		}

		/*
		 * (non-Javadoc)
		 * @see
		 * org.ws4d.java.communication.DefaultIncomingMessageListener#handle
		 * (org.ws4d.java.message.eventing.SubscribeMessage,
		 * org.ws4d.java.communication.ProtocolData)
		 */
		public SubscribeResponseMessage handle(SubscribeMessage subscribe, ProtocolData protocolData) throws SOAPException {
			if (!isRunning()) {
				// send Fault wsa:ServiceUnavailable
				throw new SOAPException(FaultMessage.createEndpointUnavailableFault(subscribe));
			}
			if (subscriptionManager == null) {
				// eventing not supported
				throw new SOAPException(FaultMessage.createActionNotSupportedFault(subscribe));
			}
			sharedLock();
			try {
				return subscriptionManager.subscribe(subscribe, protocolData);
			} catch (SOAPException e) {
				Log.printStackTrace(e);
				throw e;
			} finally {
				releaseSharedLock();
			}
		}

		/*
		 * (non-Javadoc)
		 * @see
		 * org.ws4d.java.communication.DefaultIncomingMessageListener#handle
		 * (org.ws4d.java.message.eventing.GetStatusMessage,
		 * org.ws4d.java.communication.ProtocolData)
		 */
		public GetStatusResponseMessage handle(GetStatusMessage getStatus, ProtocolData protocolData) throws SOAPException {
			if (!isRunning()) {
				// send Fault wsa:ServiceUnavailable
				throw new SOAPException(FaultMessage.createEndpointUnavailableFault(getStatus));
			}
			if (subscriptionManager == null) {
				// eventing not supported
				throw new SOAPException(FaultMessage.createActionNotSupportedFault(getStatus));
			}
			sharedLock();
			try {
				return subscriptionManager.getStatus(getStatus, protocolData);
			} catch (SOAPException e) {
				Log.printStackTrace(e);
				throw e;
			} finally {
				releaseSharedLock();
			}
		}

		/*
		 * (non-Javadoc)
		 * @see
		 * org.ws4d.java.communication.DefaultIncomingMessageListener#handle
		 * (org.ws4d.java.message.eventing.RenewMessage,
		 * org.ws4d.java.communication.ProtocolData)
		 */
		public RenewResponseMessage handle(RenewMessage renew, ProtocolData protocolData) throws SOAPException {
			if (!isRunning()) {
				// send Fault wsa:ServiceUnavailable
				throw new SOAPException(FaultMessage.createEndpointUnavailableFault(renew));
			}
			if (subscriptionManager == null) {
				// eventing not supported
				throw new SOAPException(FaultMessage.createActionNotSupportedFault(renew));
			}
			sharedLock();
			try {
				return subscriptionManager.renew(renew, protocolData);
			} catch (SOAPException e) {
				Log.printStackTrace(e);
				throw e;
			} finally {
				releaseSharedLock();
			}
		}

		/*
		 * (non-Javadoc)
		 * @see
		 * org.ws4d.java.communication.DefaultIncomingMessageListener#handle
		 * (org.ws4d.java.message.eventing.UnsubscribeMessage,
		 * org.ws4d.java.communication.ProtocolData)
		 */
		public UnsubscribeResponseMessage handle(UnsubscribeMessage unsubscribe, ProtocolData protocolData) throws SOAPException {
			if (!isRunning()) {
				// send Fault wsa:ServiceUnavailable
				throw new SOAPException(FaultMessage.createEndpointUnavailableFault(unsubscribe));
			}
			if (subscriptionManager == null) {
				// eventing not supported
				throw new SOAPException(FaultMessage.createActionNotSupportedFault(unsubscribe));
			}
			sharedLock();
			try {
				return subscriptionManager.unsubscribe(unsubscribe, protocolData);
			} catch (SOAPException e) {
				Log.printStackTrace(e);
				throw e;
			} finally {
				releaseSharedLock();
			}
		}

		/*
		 * (non-Javadoc)
		 * @see
		 * org.ws4d.java.communication.DefaultIncomingMessageListener#handle
		 * (org.ws4d.java.message.invocation.InvokeMessage,
		 * org.ws4d.java.communication.ProtocolData)
		 */
		public InvokeMessage handle(final InvokeMessage invokeRequest, ProtocolData protocolData) throws SOAPException {
			if (!isRunning()) {
				// send Fault wsa:ServiceUnavailable
				throw new SOAPException(FaultMessage.createEndpointUnavailableFault(invokeRequest));
			}

			Operation operation = null;

			sharedLock();
			try {
				// Remote invocation
				String inputAction = invokeRequest.getAction().toString();

				if (Log.isDebug()) {
					Log.debug("<I> Receiving invocation input for " + inputAction, Log.DEBUG_LAYER_APPLICATION);
				}

				operation = (Operation) operations.get(inputAction);
				if (operation == null) {
					throw new SOAPException(FaultMessage.createActionNotSupportedFault(invokeRequest));
				}
			} finally {
				releaseSharedLock();
			}

			try {
				/*
				 * User Thread
				 */

				/*
				 * Resolve the types based on the input!
				 */
				ParameterValue reqVal = invokeRequest.getContent();
				if (reqVal != null) {
					DataStructure wsdlCol = wsdls.values();
					Iterator wsdlIt = wsdlCol.iterator();
					while (wsdlIt.hasNext()) {
						WSDL wsdl = (WSDL) wsdlIt.next();
						Iterator schemaIt = wsdl.getTypes();
						while (schemaIt.hasNext()) {
							Schema schema = (Schema) schemaIt.next();
							reqVal.resolveTypes(schema);
						}

					}
				}
				ParameterValue retVal = operation.invoke(reqVal);
				if (operation.isRequestResponse()) {
					/*
					 * Send response
					 */
					InvokeMessage invokeResponse = new InvokeMessage(operation.getOutputAction(), false, protocolData.getCommunicationManagerId());
					invokeResponse.setResponseTo(invokeRequest);

					// set DPWSVersion from the Request to the Response
					invokeResponse.setProtocolInfo(invokeRequest.getProtocolInfo());

					invokeResponse.setContent(retVal);
					return invokeResponse;
				} else {
					// send HTTP response (202)
					return null;
				}
			} catch (InvocationException e) {
				// Log.printStackTrace(e);
				Log.warn("Exception during invocation: " + e.getMessage());
				// respond with fault to sender
				FaultMessage fault = new FaultMessage(e.getAction(), protocolData.getCommunicationManagerId());
				fault.setResponseTo(invokeRequest);
				fault.setCode(e.getCode());
				fault.setSubcode(e.getSubcode());
				fault.setReason(e.getReason());
				fault.setDetail(e.getDetail());
				throw new SOAPException(fault);
			} catch (TimeoutException e) {
				// this shouldn't ever occur locally
				Log.printStackTrace(e);
				return null;
			}
		}

		public OperationDescription getOperation(String action) {
			Operation operation = null;

			sharedLock();
			try {
				operation = (Operation) operations.get(action);
			} finally {
				releaseSharedLock();
			}

			return operation;
		}

	}

	private static class ResourcePath {

		final String	path;

		final int		depth;

		ResourcePath(String path, int depth) {
			super();
			this.path = path;
			this.depth = depth;
		}

	}

}
