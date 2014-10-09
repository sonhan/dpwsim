/*******************************************************************************
 * Copyright (c) 2009 MATERNA Information & Communications. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html. For further
 * project-related information visit http://www.ws4d.org. The most recent
 * version of the JMEDS framework can be obtained from
 * http://sourceforge.net/projects/ws4d-javame.
 ******************************************************************************/

package org.ws4d.java.service.reference;

import org.ws4d.java.dispatch.ServiceReferenceEventRegistry;
import org.ws4d.java.service.Service;

/**
 * Callback interface for listeners interested in state changes of services.
 * Global service listening is initiated by registration via
 * {@link ServiceReferenceEventRegistry#registerServiceListening(ServiceListener)}
 * , the method
 * {@link ServiceReferenceEventRegistry#unregisterServiceListening(ServiceListener)}
 * removes service listening.
 */
public interface ServiceListener {

	/**
	 * Callback method, if service within the {@link ServiceReference} has been
	 * changed.
	 * 
	 * @param serviceRef a reference to the service that caused the notification
	 * @param the service that has just been created
	 */
	public void serviceChanged(ServiceReference serviceRef, Service service);

	/**
	 * Callback method, if the service within the {@link ServiceReference} was
	 * created.
	 * 
	 * @param serviceRef a reference to the service that caused the notification
	 *            qpram service the service that just have changed
	 */
	public void serviceCreated(ServiceReference serviceRef, Service service);

	/**
	 * Callback method, if the service within the {@link ServiceReference} was
	 * disposed of.
	 * 
	 * @param serviceRef a reference to the service that caused the notification
	 */
	public void serviceDisposed(ServiceReference serviceRef);

}
