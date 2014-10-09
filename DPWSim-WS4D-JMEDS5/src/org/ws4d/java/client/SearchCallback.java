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

import org.ws4d.java.service.reference.DeviceReference;
import org.ws4d.java.service.reference.ServiceReference;

/**
 * Implementations of this interface are capable of receiving notifications when
 * devices and/or services matching a specified search criteria are found.
 * <p>
 * Searches for devices/services can be initiated by means of the corresponding
 * static methods of class {@link SearchManager}, which accept an instance of
 * the <code>SearchCallback</code> interface.
 * </p>
 * 
 * @see SearchManager
 */
public interface SearchCallback {

	/**
	 * This method is called each time a device matching the initial search
	 * criteria (as contained within argument <code>search</code>) has been
	 * found.
	 * 
	 * @param devRef a reference to the matching device
	 * @param search the collection of criteria the search was initiated with
	 */
	public void deviceFound(DeviceReference devRef, SearchParameter search);

	/**
	 * This method is called each time a service matching the initial search
	 * criteria (as contained within argument <code>search</code>) has been
	 * found.
	 * 
	 * @param servRef a reference to the matching service
	 * @param search the list of criteria the search was initiated with
	 */
	public void serviceFound(ServiceReference servRef, SearchParameter search);

}
