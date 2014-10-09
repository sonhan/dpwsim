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

import org.ws4d.java.schema.Element;
import org.ws4d.java.service.parameter.ParameterValue;
import org.ws4d.java.structures.DataStructure;
import org.ws4d.java.types.AttributableSupport;
import org.ws4d.java.util.WS4DIllegalStateException;
import org.ws4d.java.wsdl.IOType;
import org.ws4d.java.wsdl.WSDLMessagePart;

/**
 * Faults are the web-services correspondents to exceptions from the Java world.
 * They can be declared on {@link Operation}s or {@link DefaultEventSource}s
 * just like exceptions may be declared on Java methods. A fault has a
 * {@link #getName() name}, which must not be <code>null</code> and unique
 * within the scope of the surrounding operation/event, and an
 * {@link #getAction() action URI} corresponding to the <em>wsa:Action</em>
 * property of the fault message within which the fault is sent . The action URI
 * must also not be <code>null</code> and must be unique within the operation.
 * However, when creating a user-defined <code>Fault</code>, it is possible to
 * omit specifying the action URI explicitly as the DPWS framework will provide
 * an auto-generated one in case it was not set prior to adding the fault to an
 * operation/event.
 * <p>
 * A <code>Fault</code> can also carry user-defined data, just like exceptions
 * can be designed to encapsulate additional state within their instance
 * variables. Within the <code>Fault</code>, this state is described by means of
 * its associated {@link #getElement() element}.
 * </p>
 * <p>
 * It is important to notice that modifications to a fault are not possible
 * after it has been added to an operation. Calling any of the setter methods at
 * this time will result in <code>IllegalStateException</code>s being thrown.
 * </p>
 */
public class Fault extends AttributableSupport {

	boolean					attached	= false;

	private final String	name;

	private String			action;

	private Element			element;

	/**
	 * Creates a fault with the given <code>name</code>, which must be unique
	 * within the scope of the surrounding operation. The fault will be assigned
	 * an auto-generated {@link #getAction() action} when it is
	 * {@link OperationCommons#addFault(Fault) added} to an operation or event.
	 * 
	 * @param name the name of this fault; unique within the scope of the
	 *            surrounding operation
	 */
	public Fault(String name) {
		this(name, null);
	}

	/**
	 * Creates a fault with the given <code>name</code> (must be unique within
	 * the scope of the surrounding operation). The <code>action</code> argument
	 * should be a URI representing the <em>wsa:Action</em> associated to this
	 * fault.
	 * 
	 * @param name the name of the fault; unique within the scope of the
	 *            surrounding operation
	 * @param action the wsa:Action URI of this fault
	 */
	public Fault(String name, String action) {
		super();
		this.name = name;
		this.action = action;
	}

	Fault(IOType fault) {
		this(fault.getName(), fault.getAction());
		DataStructure parts = fault.getParts();
		int size = parts.size();
		if (size > 1) {
			// TODO throw exception: unsupported WSDL feature
			throw new IllegalArgumentException("Unsupported WSDL feature (fault message with more than one parts ): " + fault.getMessage());
		} else if (size == 1) {
			WSDLMessagePart part = (WSDLMessagePart) parts.iterator().next();
			if (part.isElement()) {
				setElement(part.getElement());
			} else {
				// TODO throw exception: unsupported WSDL feature
				throw new IllegalArgumentException("Unsupported WSDL feature (message part referring to a type): " + part);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("Fault [ name=").append(getName());
		sb.append(", action=").append(getAction());
		sb.append(", element=").append(getElement());
		sb.append(" ]");
		return sb.toString();
	}

	/**
	 * Returns the <em>wsa:Action</em> associated to this fault.
	 * <p>
	 * As <code>Faults</code> are always required to have an action URI
	 * associated to them, one will be automatically generated when adding this
	 * <code>Fault</code> to an {@link Operation operation} or
	 * {@link DefaultEventSource event}, if it was not specified explicitly.
	 * 
	 * @return the action of this fault
	 */
	public String getAction() {
		return action;
	}

	/**
	 * Sets the <em>wsa:Action</em> of this fault. This method may be called
	 * only <strong>before</strong> this <code>Fault</code> is added to an
	 * {@link Operation operation} or {@link DefaultEventSource event}.
	 * 
	 * @param action the action to set
	 * @throws WS4DIllegalStateException if the fault is currently attached to
	 *             an operation or an event
	 */
	public void setAction(String action) {
		if (attached) {
			throw new WS4DIllegalStateException("unable to modify after being added to operation or event");
		}
		this.action = action;
	}

	/**
	 * Returns the element of this fault, which defines the type and structure
	 * of any user-defined data the fault may contain. It can be used to
	 * {@link #createValue() create} a suitable {@link ParameterValue} container
	 * for the actual values when sending messages containing this fault.
	 * 
	 * @return this fault's element
	 * @see #setElement(Element)
	 */
	public Element getElement() {
		return element;
	}

	/**
	 * Sets the element of this <code>Fault</code>. The element describes the
	 * type and contents of a user-defined data structure providing additional
	 * information about the fault. Calling this method after having already
	 * added this <code>Fault</code> to an {@link Operation operation} or
	 * {@link DefaultEventSource event} will result in an
	 * <code>IllegalStateException</code> being thrown.
	 * 
	 * @param element the element to set
	 * @throws WS4DIllegalStateException if the fault is currently attached to
	 *             an operation or an event
	 */
	public void setElement(Element element) {
		if (attached) {
			throw new WS4DIllegalStateException("unable to modify after being added to operation or event");
		}
		this.element = element;
	}

	/**
	 * Returns the name of this <code>Fault</code>. This name must be unique
	 * within the scope of its surrounding operation or event.
	 * 
	 * @return this fault's name
	 */
	public String getName() {
		return name;
	}

	/**
	 * A shorthand method for creating a suitable data container for this
	 * fault's element. Technically equivalent to
	 * <code>ParameterValue.createElementValue(getElement())</code>.
	 * 
	 * @return a data container suitable for this fault's element
	 * @see #getElement()
	 */
	public ParameterValue createValue() {
		return ParameterValue.createElementValue(getElement());
	}

}
