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

import org.ws4d.java.communication.Bindable;
import org.ws4d.java.eventing.EventSource;
import org.ws4d.java.structures.Iterator;
import org.ws4d.java.types.QName;

/**
 * DPWS service.
 * <p>
 * Contains the methods to use a DPWS service. Does not care about the location
 * of the service. It possible to use local services created inside the own
 * virtual machine or remote services from other machines.
 * </p>
 */
public interface LocalService extends Service, Bindable {

	/**
	 * Add a port type (name) to this service. The port type will initially be
	 * empty, i.e. it will contain neither event sources, nor any operations.
	 * 
	 * @param portTypeName the name of the port type to add.
	 */
	public void addPortType(QName portTypeName);

	/**
	 * Add an operation to this service.
	 * 
	 * @param operation operation to add.
	 */
	public void addOperation(Operation operation);

	/**
	 * Add an event to this service.
	 * 
	 * @param event event to add.
	 */
	public void addEventSource(EventSource event);

	/**
	 * Starts this service. Registers service within dispatcher.
	 */
	public void start() throws IOException;

	/**
	 * Pauses service. After calling this, subsequent incoming requests to this
	 * service will result in a &quot;ServiceNotAvailable&quot; fault.
	 */
	public void pause();

	/**
	 * Stops this service. Unregisters service in device service registry.
	 */
	public void stop() throws IOException;

	/**
	 * Returns <code>true</code>, if this local service is currently in the
	 * running state. Returns <code>false</code> otherwise.
	 * 
	 * @return whether this local service is currently running or not
	 */
	public boolean isRunning();

	/**
	 * Sets the parent device for this service.
	 * <p>
	 * Every service is assigned to one device.
	 * </p>
	 * 
	 * @param devicet the device which the service should be assigned to.
	 */
	public void setParentDevice(LocalDevice device);

	/**
	 * Returns an iterator containing all WSDL documents describing this
	 * service.
	 * 
	 * @return an iterator containing all WSDL documents describing this
	 *         service, i.e. those containing port types of the service.
	 */
	public Iterator getDescriptionsForPortTypes();

}
