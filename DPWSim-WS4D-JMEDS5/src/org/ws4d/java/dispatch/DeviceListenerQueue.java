/*******************************************************************************
 * Copyright (c) 2009 MATERNA Information & Communications. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html. For further
 * project-related information visit http://www.ws4d.org. The most recent
 * version of the JMEDS framework can be obtained from
 * http://sourceforge.net/projects/ws4d-javame.
 ******************************************************************************/
package org.ws4d.java.dispatch;

import org.ws4d.java.DPWSFramework;
import org.ws4d.java.service.Device;
import org.ws4d.java.service.reference.DeviceListener;
import org.ws4d.java.service.reference.DeviceReference;
import org.ws4d.java.structures.LinkedList;
import org.ws4d.java.structures.List;
import org.ws4d.java.util.Log;

/**
 *
 */
class DeviceListenerQueue {

	static final byte				DEVICE_RUNNING_EVENT						= 1;

	static final byte				DEVICE_COMPLETELY_DISCOVERED_EVENT			= 2;

	static final byte				DEVICE_BUILT_UP_EVENT						= 3;

	static final byte				DEVICE_BYE_EVENT							= 4;

	static final byte				DEVICE_CHANGED_EVENT						= 5;

	static final byte				DEVICE_CHANGED_AND_BUILT_UP_EVENT			= 6;

	static final byte				DEVICE_RUNNING_AND_BUILT_UP_EVENT			= 7;

	static final byte				DEVICE_COMMUNICATION_ERROR_OR_RESET_EVENT	= 8;

	private final DeviceListener	listener;

	private final DeviceReference	devRef;

	private List					queue										= new LinkedList();

	private DeviceEvent				currentEvent								= null;

	/**
	 * 
	 */
	DeviceListenerQueue(DeviceListener listener, DeviceReference devRef) {
		this.listener = listener;
		this.devRef = devRef;
	}

	synchronized void announce(DeviceEvent event) {
		if (currentEvent == null) {
			currentEvent = event;
			DPWSFramework.getThreadPool().execute(new Runnable() {

				/*
				 * (non-Javadoc)
				 * @see java.lang.Runnable#run()
				 */
				public void run() {
					while (true) {
						deliverCurrentEvent();
						synchronized (DeviceListenerQueue.this) {
							boolean endOfLoop = true;
							while (queue.size() > 0) {
								DeviceEvent first = (DeviceEvent) queue.remove(0);
								if (first.eventType != currentEvent.eventType) {
									currentEvent = first;
									endOfLoop = false;
									break;
								}
							}
							if (endOfLoop) {
								currentEvent = null;
								return;
							}
						}
					}
				}

			});
		} else {
			queue.add(event);
		}
	}

	private void deliverCurrentEvent() {
		try {
			switch (currentEvent.eventType) {
				case (DEVICE_RUNNING_EVENT): {
					listener.deviceRunning(devRef);
					break;
				}
				case (DEVICE_COMPLETELY_DISCOVERED_EVENT): {
					listener.deviceCompletelyDiscovered(devRef);
					break;
				}
				case (DEVICE_BUILT_UP_EVENT): {
					listener.deviceBuiltUp(devRef, currentEvent.device);
					break;
				}
				case (DEVICE_BYE_EVENT): {
					listener.deviceBye(devRef);
					break;
				}
				case (DEVICE_CHANGED_EVENT): {
					listener.deviceChanged(devRef);
					break;
				}
				case (DEVICE_CHANGED_AND_BUILT_UP_EVENT): {
					listener.deviceChanged(devRef);
					listener.deviceBuiltUp(devRef, currentEvent.device);
					break;
				}
				case (DEVICE_RUNNING_AND_BUILT_UP_EVENT): {
					listener.deviceRunning(devRef);
					listener.deviceBuiltUp(devRef, currentEvent.device);
					break;
				}
				case (DEVICE_COMMUNICATION_ERROR_OR_RESET_EVENT): {
					listener.deviceCommunicationErrorOrReset(devRef);
					break;
				}
			}
		} catch (Throwable t) {
			Log.error("Exception during listener notification: " + t.getMessage());
			Log.printStackTrace(t);
		}
	}

	static class DeviceEvent {

		byte	eventType;

		Device	device;

		DeviceEvent(byte eventType, Device device) {
			this.eventType = eventType;
			this.device = device;
		}

	}

}
