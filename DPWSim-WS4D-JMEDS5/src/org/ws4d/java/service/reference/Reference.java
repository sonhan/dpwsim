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

import org.ws4d.java.communication.CommunicationManagerRegistry;
import org.ws4d.java.communication.TimeoutException;
import org.ws4d.java.types.URI;

/**
 * Super interface of service and device reference.
 */
public interface Reference {

	public static final int	LOCATION_UNKNOWN	= 0;

	public static final int	LOCATION_REMOTE		= 1;

	public static final int	LOCATION_LOCAL		= 2;

	// /**
	// * Returns whether this reference points to a remote or a local device or
	// * service instance.
	// *
	// * @return the location of the device or service
	// */
	// public boolean isRemote();
	//
	// /**
	// * Returns if this reference points to a local device or
	// * service instance or not.
	// *
	// * @return the location of the device or service
	// */
	// public boolean isLocal();

	/**
	 * Returns the location of the device or service, which may be -
	 * LOCATION_UNKNOWN, - LOCATION_REMOTE, - LOCATION_LOCAL.
	 * 
	 * @return the location of the device or service
	 */
	public int getLocation();

	/**
	 * Returns the preferred transport address for communication with this
	 * reference.
	 * 
	 * @return the preferred transport address for this reference
	 * @throws TimeoutException if no suitable transport address can be detected
	 */
	public URI getPreferredXAddress() throws TimeoutException;

	/**
	 * Returns the ID of the communication protocol to use when communicating
	 * with this reference's target over the current
	 * {@link #getPreferredXAddress() preferred transport address}. Will return
	 * {@link CommunicationManagerRegistry#getDefault() the framework-wide
	 * default communication ID}, if no preferred transport address has been
	 * explicitly set.
	 * 
	 * @return the ID of the protocol to communicate over with the target of
	 *         this reference
	 * @throws TimeoutException if no suitable transport address can be detected
	 */
	public String getPreferredCommunicationManagerID() throws TimeoutException;

}
