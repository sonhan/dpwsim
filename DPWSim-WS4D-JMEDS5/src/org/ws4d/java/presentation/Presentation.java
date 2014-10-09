/*******************************************************************************
 * Copyright (c) 2009 MATERNA Information & Communications. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html. For further
 * project-related information visit http://www.ws4d.org. The most recent
 * version of the JMEDS framework can be obtained from
 * http://sourceforge.net/projects/ws4d-javame.
 ******************************************************************************/
package org.ws4d.java.presentation;

import org.ws4d.java.service.LocalDevice;
import org.ws4d.java.types.URI;

/**
 * Device and service presentation.
 */
public interface Presentation {

	/**
	 * Register a device for device presentation.
	 * 
	 * @param device the device.
	 * @return the URI which was used to register the device.
	 */
	public URI register(LocalDevice device);
}
