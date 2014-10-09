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

import org.ws4d.java.service.Device;

/**
 * Interface to implement device reference listeners.
 */
public interface DeviceListener {

	/**
	 * Callback method, if device is usable.
	 * 
	 * @param deviceRef a reference to the device that caused the notification
	 */
	public void deviceRunning(DeviceReference deviceRef);

	/**
	 * Callback method, if discovery metadata was completely received from
	 * device (via directed probe).
	 * 
	 * @param deviceRef a reference to the device that caused the notification
	 */
	public void deviceCompletelyDiscovered(DeviceReference deviceRef);

	/**
	 * Callback method, if device bye was received or the local device was
	 * stopped.
	 * 
	 * @param deviceRef a reference to the device that caused the notification
	 */
	public void deviceBye(DeviceReference deviceRef);

	/**
	 * Callback method, if device was changed and the device data is no longer
	 * accurate.
	 * 
	 * @param deviceRef a reference to the device that caused the notification
	 */
	public void deviceChanged(DeviceReference deviceRef);

	/**
	 * Callback method, if device within the {@link DeviceReference} was
	 * created.
	 * 
	 * @param deviceRef a reference to the device that caused the notification
	 * @param device the proxy device just built up
	 */
	public void deviceBuiltUp(DeviceReference deviceRef, Device device);

	/**
	 * Callback method, if communication with the remote device of the
	 * {@link DeviceReference} has failed or the reference was reset.
	 * 
	 * @param deviceRef a reference to the device that caused the notification
	 */
	public void deviceCommunicationErrorOrReset(DeviceReference deviceRef);

}
