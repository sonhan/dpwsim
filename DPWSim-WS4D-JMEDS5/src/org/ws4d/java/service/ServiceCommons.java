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
import org.ws4d.java.eventing.EventSource;
import org.ws4d.java.eventing.EventingFactory;
import org.ws4d.java.structures.DataStructure;
import org.ws4d.java.structures.EmptyStructures;
import org.ws4d.java.structures.HashMap;
import org.ws4d.java.structures.HashSet;
import org.ws4d.java.structures.Iterator;
import org.ws4d.java.structures.ReadOnlyIterator;
import org.ws4d.java.structures.Set;
import org.ws4d.java.types.AttributableSupport;
import org.ws4d.java.types.CustomAttributeValue;
import org.ws4d.java.types.EprInfo;
import org.ws4d.java.types.QName;
import org.ws4d.java.util.Log;
import org.ws4d.java.wsdl.WSDL;
import org.ws4d.java.wsdl.WSDLOperation;
import org.ws4d.java.wsdl.WSDLPortType;

/**
 * Class represents the common part of a proxy/local DPWS device.
 */
public abstract class ServiceCommons implements Service {

	// key = portType as QName, value = PortType instance
	final HashMap			portTypes	= new HashMap();

	// key = wsa:Action as String, value = Operation instance
	final HashMap			operations	= new HashMap();

	// key = wsa:Action as String, value = Event instance
	final HashMap			events		= new HashMap();

	/*
	 * we store different WSDL documents, one for each target namespace of our
	 * service types
	 */
	protected final HashMap	wsdls		= new HashMap();

	/** Security */
	protected boolean		secure		= false;

	protected Object		certificate;

	protected Object		privateKey;

	ServiceCommons() {
		super();
	}

	/*
	 * ADDED 2011-01-17 by Stefan Schlichting: added to allow extension
	 * synchronize necessary
	 */
	protected HashMap getPortTypesInternal() {
		return portTypes;
	}

	protected HashMap getOperationsInternal() {
		return operations;
	}

	protected HashMap getEventsInternal() {
		return events;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		StringBuffer sb = new StringBuffer(getClass().getName());
		sb.append(" [ serviceId=").append(getServiceId());
		Iterator it = getEprInfos();
		if (it.hasNext()) {
			sb.append(", endpointReferences={ ");
			while (it.hasNext()) {
				sb.append(((EprInfo)it.next()).getEndpointReference()).append(' ');
			}
			sb.append('}');
		}
		it = getPortTypes();
		if (it.hasNext()) {
			sb.append(", portTypes={ ");
			while (it.hasNext()) {
				sb.append(it.next()).append(' ');
			}
			sb.append('}');
		}
		sb.append(" ]");
		return sb.toString();
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.service.Service#getOperations()
	 */
	public Iterator getOperations() {
		Set operations = new HashSet();
		for (Iterator it = portTypes.values().iterator(); it.hasNext();) {
			PortType type = (PortType) it.next();
			for (Iterator it2 = type.getOperations(); it2.hasNext();) {
				operations.add(it2.next());
			}
		}
		return new ReadOnlyIterator(operations);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.ws4d.java.service.Service#getOperations(org.ws4d.java.types.QName)
	 */
	public Iterator getOperations(QName portType) {
		PortType type = (PortType) portTypes.get(portType);
		return type == null ? EmptyStructures.EMPTY_ITERATOR : new ReadOnlyIterator(type.getOperations());
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.ws4d.java.service.Service#getOperation(org.ws4d.java.types.QName,
	 * java.lang.String, java.lang.String, java.lang.String)
	 */
	public Operation getOperation(QName portType, String opName, String inputName, String outputName) {
		if (opName == null) {
			return null;
		}
		PortType type = (PortType) portTypes.get(portType);
		return type == null ? null : type.getOperation(opName, inputName, outputName);
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.service.Service#getOperation(java.lang.String)
	 */
	public Operation getOperation(String inputAction) {
		return (Operation) operations.get(inputAction);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.ws4d.java.service.Service#getAnyOperation(org.ws4d.java.types.QName,
	 * java.lang.String)
	 */
	public Operation getAnyOperation(QName portType, String operationName) {
		if (operationName == null) {
			return null;
		}
		for (Iterator it = getOperations(portType); it.hasNext();) {
			Operation operation = (Operation) it.next();
			if (operationName.equals(operation.getName())) {
				return operation;
			}
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.service.Service#getEventSources()
	 */
	public Iterator getEventSources() {
		Set events = new HashSet();
		for (Iterator it = portTypes.values().iterator(); it.hasNext();) {
			PortType type = (PortType) it.next();
			for (Iterator it2 = type.getEventSources(); it2.hasNext();) {
				events.add(it2.next());
			}
		}
		return new ReadOnlyIterator(events);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.ws4d.java.service.Service#getEventSources(org.ws4d.java.types.QName)
	 */
	public Iterator getEventSources(QName portType) {
		PortType type = (PortType) portTypes.get(portType);
		return type == null ? EmptyStructures.EMPTY_ITERATOR : new ReadOnlyIterator(type.getEventSources());
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.ws4d.java.service.Service#getEventSource(org.ws4d.java.types.QName,
	 * java.lang.String, java.lang.String, java.lang.String)
	 */
	public EventSource getEventSource(QName portType, String eventName, String inputName, String outputName) {
		if (eventName == null) {
			return null;
		}
		PortType type = (PortType) portTypes.get(portType);
		return type == null ? null : type.getEventSource(eventName, inputName, outputName);
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.service.Service#getEventSource(java.lang.String)
	 */
	public EventSource getEventSource(String outputAction) {
		return (EventSource) events.get(outputAction);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.ws4d.java.service.Service#getAnyEventSource(org.ws4d.java.types.QName
	 * , java.lang.String)
	 */
	public EventSource getAnyEventSource(QName portType, String eventName) {
		if (eventName == null) {
			return null;
		}
		for (Iterator it = getEventSources(portType); it.hasNext();) {
			EventSource event = (EventSource) it.next();
			if (eventName.equals(event.getName())) {
				return event;
			}
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.ws4d.java.service.Service#getPortTypeAttribute(org.ws4d.java.types
	 * .QName, org.ws4d.java.types.QName)
	 */
	public CustomAttributeValue getPortTypeAttribute(QName portTypeName, QName attributeName) {
		PortType portType = (PortType) portTypes.get(portTypeName);
		if (portType == null) {
			throw new IllegalArgumentException("no such port type: " + portTypeName);
		}
		return portType.getAttribute(attributeName);
	}

	/**
	 * Sets the <code>value</code> of the port type attribute with the specified
	 * <code>name</code> of the port type with the given unique
	 * <code>portTypeName</code>. Throws a
	 * <code>java.lang.IllegalArgumentException</code> in case there is no port
	 * type with the given <code>portTypeName</code> within this service
	 * instance or if <code>name</code> is <code>null</code>.
	 * 
	 * @param portTypeName the unique name of the port type within the scope of
	 *            this service instance, see {@link #getPortTypes()}
	 * @param attributeName the name of the port type attribute to set, must not
	 *            be <code>null</code>
	 * @param value the value to set the named port type attribute to (may be
	 *            <code>null</code>
	 * @throws IllegalArgumentException if there is no port type with the given
	 *             <code>portTypeName</code> within this service instance or if
	 *             <code>name</code> is <code>null</code>
	 */
	public void setPortTypeAttribute(QName portTypeName, QName attributeName, CustomAttributeValue value) {
		PortType portType = (PortType) portTypes.get(portTypeName);
		if (portType == null) {
			throw new IllegalArgumentException("no such port type: " + portTypeName);
		}
		portType.setAttribute(attributeName, value);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.ws4d.java.service.Service#getPortTypeAttributes(org.ws4d.java.types
	 * .QName)
	 */
	public HashMap getPortTypeAttributes(QName portTypeName) {
		PortType portType = (PortType) portTypes.get(portTypeName);
		if (portType == null) {
			throw new IllegalArgumentException("no such port type: " + portTypeName);
		}
		return portType.getAttributes();
	}

	/**
	 * Sets all port type attributes of the port type with unique
	 * <code>portTypeName</code> at once to those contained within argument
	 * <code>attributes</code>. Note that depending on the actual
	 * implementation, it is possible that the map <code>attributes</code>
	 * points at may be used for the actual internal storage of the port type
	 * attributes (i.e. without copying it). That is why, after passing it to
	 * this method, modifications to this map should be made with care. This
	 * method throws a <code>java.lang.IllegalArgumentException</code> in case
	 * <code>attributes</code> is <code>null</code>.
	 * 
	 * @param portTypeName the unique name of the port type within the scope of
	 *            this service instance, see {@link #getPortTypes()}
	 * @param attributes the new port type attributes to set
	 * @throws IllegalArgumentException if no port type with the given
	 *             <code>portTypeName</code> is found or if
	 *             <code>attributes</code> is <code>null</code>
	 */
	public void setPortTypeAttributes(QName portTypeName, HashMap attributes) {
		PortType portType = (PortType) portTypes.get(portTypeName);
		if (portType == null) {
			throw new IllegalArgumentException("no such port type: " + portTypeName);
		}
		portType.setAttributes(attributes);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.ws4d.java.service.Service#hasPortTypeAttributes(org.ws4d.java.types
	 * .QName)
	 */
	public boolean hasPortTypeAttributes(QName portTypeName) {
		PortType portType = (PortType) portTypes.get(portTypeName);
		return portType != null && portType.hasAttributes();
	}

	/**
	 * Sets the service to use security techniques. Use this method after
	 * setting the services HTTPSBinding
	 * 
	 * @throws Exception
	 */
	public void setSecureService() throws Exception {
		if (!DPWSFramework.hasModule(DPWSFramework.SECURITY_MODULE)) {
			throw new RuntimeException("You are running the DPWS Framework without the required Security-Module");
		}

		this.secure = true;
	}

	/**
	 * Sends using WS-Security techniques.
	 */
	public boolean isSecure() {
		return secure;
	}

	public void setSecure(boolean sec) {
		secure = sec;
	}

	/**
	 * @param certificate must be the java.security.cert.Certificate of the
	 *            sender device/service
	 */
	public void setCertificate(Object certificate) {
		if (certificate == null) return;
		this.certificate = certificate;
		this.setSecure(true);
	}

	public Object getCertificate() {
		return certificate;
	}

	public Object getPrivateKey() {
		return privateKey;
	}

	/**
	 * @param privKey must be the java.security.PrivateKey of the sender device/
	 *            service
	 */
	public void setPrivateKey(Object privKey) {
		this.privateKey = privKey;
	}

	protected void processWSDLPortType(WSDLPortType portType) {
		QName portTypeName = portType.getName();
		if (portTypes.containsKey(portTypeName)) {
			/*
			 * we have already imported this port type probably through a
			 * different WSDL file
			 */
			return;
		}
		PortType port = new PortType();

		if (portType.hasAttributes()) {
			port.setAttributes(portType.getAttributes());
		}

		DataStructure operations = portType.getOperations();
		for (Iterator it = operations.iterator(); it.hasNext();) {
			WSDLOperation operation = (WSDLOperation) it.next();
			OperationCommons op;
			if (operation.isRequest()) {
				Operation realOp = createOperation(operation);
				op = realOp;
				port.addOperation(new OperationSignature(op), realOp);
				this.operations.put(realOp.getInputAction(), realOp);
				if (Log.isDebug()) {
					Log.debug("[NEW OPERATION]: " + realOp.toString(), Log.DEBUG_LAYER_APPLICATION);
				}
			} else if (operation.isEvented()) {
				EventSource realEvent = createEventSource(operation);
				if (realEvent == null || !(realEvent instanceof OperationCommons)) {
					Log.error("Cannot create event source from " + operation + ". Event does not exist, or is not a extension of operation.");
					continue;
				}
				op = (OperationCommons) realEvent;
				port.addEventSource(new OperationSignature(op), realEvent);
				this.events.put(realEvent.getOutputAction(), realEvent);
				if (Log.isDebug()) {
					Log.debug("[NEW EVENT SOURCE]: " + realEvent.toString(), Log.DEBUG_LAYER_APPLICATION);
				}
			} else {
				throw new IllegalArgumentException("Unknown type of WSDL operation: " + operation);
			}
			/*
			 * no need to check names, as they should be correctly sent by
			 * existing service or within given WSDL; if not, than it is not our
			 * fault :D
			 */
			op.setService(this);
		}
		portTypes.put(portTypeName, port);
	}

	/**
	 * Creates an {@link Operation} instance suitable for usage within this
	 * service instance. This method is only called from within
	 * {@link #processWSDLPortType(WSDLPortType)} and should not be used in
	 * other contexts.
	 * 
	 * @param wsdlOperation the WSDL operation describing the operation to
	 *            create
	 * @return the operation to add
	 */
	protected abstract Operation createOperation(WSDLOperation wsdlOperation);

	/**
	 * Creates a {@link DefaultEventSource} instance suitable for usage within
	 * this service instance. This method is only called from within
	 * {@link #processWSDLPortType(WSDLPortType)} and should not be used in
	 * other contexts.
	 * 
	 * @param wsdlOperation the WSDL operation describing the event source to
	 *            create
	 * @return the event source to add
	 */
	protected EventSource createEventSource(WSDLOperation wsdlOperation) {
		if (DPWSFramework.hasModule(DPWSFramework.EVENTING_MODULE)) {
			try {
				EventingFactory eFac = DPWSFramework.getEventingFactory();
				return eFac.createDefaultEventSource(wsdlOperation);
			} catch (IOException e) {
				Log.error("Cannot create event source from " + wsdlOperation + ". " + e.getMessage());
			}
		} else {
			Log.error("Cannot create event source, event support missing.");
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.service.Service#getDescriptions()
	 */
	public Iterator getDescriptions() {
		return new ReadOnlyIterator(wsdls.values().iterator());
	}

	protected WSDL getExistingDescription(String targetNamespace) {
		if (wsdls.size() > 0) {
			WSDL wsdl = (WSDL) wsdls.get(targetNamespace);
			if (wsdl != null) {
				return wsdl;
			}
			// try linked WSDLs
			for (Iterator it = wsdls.values().iterator(); it.hasNext();) {
				wsdl = (WSDL) it.next();
				WSDL child = wsdl.getLinkedWsdlRecursive(targetNamespace);
				if (child != null) {
					return child;
				}
			}
		}
		return null;
	}

	// Changed SSch 2011-01-17 Allow extension from other packages
	public static class PortType extends AttributableSupport {

		// key = OperationSignature instance, value = Operation instance
		protected final HashMap	operations	= new HashMap();

		// key = OperationSignature instance, value = Event instance
		protected final HashMap	events		= new HashMap();

		protected boolean		plombed;

		public boolean contains(OperationSignature signature) {
			return operations.containsKey(signature) || events.containsKey(signature);
		}

		public boolean hasOperations() {
			return operations.size() != 0;
		}

		public Iterator getOperations() {
			return operations.values().iterator();
		}

		public Operation getOperation(String name, String inputName, String outputName) {
			// compatible with overloaded operations (use input/output names)
			return (Operation) operations.get(new OperationSignature(name, inputName, outputName));
		}

		public void addOperation(OperationSignature signature, Operation operation) {
			operations.put(signature, operation);
		}

		public boolean hasEventSources() {
			return events.size() != 0;
		}

		public Iterator getEventSources() {
			return events.values().iterator();
		}

		public EventSource getEventSource(String name, String inputName, String outputName) {
			// compatible with overloaded operations (use input/output names)
			return (EventSource) events.get(new OperationSignature(name, inputName, outputName));
		}

		public void addEventSource(OperationSignature signature, EventSource event) {
			events.put(signature, event);
		}

		public boolean isPlombed() {
			return plombed;
		}

		protected void plomb() {
			plombed = true;
		}
	}

	// Changed SSch 2011-01-17 Allow extension from other packages
	public static class OperationSignature {

		private final String	name;

		private final String	inputName;

		private final String	outputName;

		public OperationSignature(OperationDescription opDescription) {
			this(opDescription.getName(), opDescription.getInputName(), opDescription.getOutputName());
		}

		/**
		 * @param name
		 * @param inputName
		 * @param outputName
		 */
		public OperationSignature(String name, String inputName, String outputName) {
			super();
			this.name = name;
			this.inputName = inputName; // == null ? "" : inputName;
			this.outputName = outputName; // == null ? "" : outputName;
		}

		/*
		 * (non-Javadoc)
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (getClass() != obj.getClass()) {
				return false;
			}
			OperationSignature other = (OperationSignature) obj;
			if (!name.equals(other.name)) {
				return false;
			}
			if (inputName == null) {
				if (other.inputName != null) {
					return false;
				}
			} else if (!inputName.equals(other.inputName)) {
				return false;
			}
			if (outputName == null) {
				if (other.outputName != null) {
					return false;
				}
			} else if (!outputName.equals(other.outputName)) {
				return false;
			}
			return true;
		}

		/*
		 * (non-Javadoc)
		 * @see java.lang.Object#hashCode()
		 */
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + name.hashCode();
			result = prime * result + ((inputName == null) ? 0 : inputName.hashCode());
			result = prime * result + ((outputName == null) ? 0 : outputName.hashCode());
			return result;
		}
	}

}
