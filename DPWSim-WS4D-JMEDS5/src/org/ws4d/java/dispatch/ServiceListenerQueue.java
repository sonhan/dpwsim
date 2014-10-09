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
import org.ws4d.java.service.Service;
import org.ws4d.java.service.reference.ServiceListener;
import org.ws4d.java.service.reference.ServiceReference;
import org.ws4d.java.structures.LinkedList;
import org.ws4d.java.structures.List;
import org.ws4d.java.util.Log;

/**
 *
 */
class ServiceListenerQueue {

	static final byte				SERVICE_CREATED_EVENT	= 1;

	static final byte				SERVICE_CHANGED_EVENT	= 2;

	static final byte				SERVICE_DISPOSED_EVENT	= 3;

	private final ServiceListener	listener;

	private List					queue					= new LinkedList();

	private ServiceEvent			currentEvent			= null;

	/**
	 * 
	 */
	ServiceListenerQueue(ServiceListener listener) {
		this.listener = listener;
	}

	synchronized void announce(ServiceEvent event) {
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
						synchronized (ServiceListenerQueue.this) {
							boolean endOfLoop = true;
							while (queue.size() > 0) {
								ServiceEvent first = (ServiceEvent) queue.remove(0);
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
				case (SERVICE_CREATED_EVENT): {
					listener.serviceCreated(currentEvent.servRef, currentEvent.service);
					break;
				}
				case (SERVICE_CHANGED_EVENT): {
					listener.serviceChanged(currentEvent.servRef, currentEvent.service);
					break;
				}
				case (SERVICE_DISPOSED_EVENT): {
					listener.serviceDisposed(currentEvent.servRef);
					break;
				}
			}
		} catch (Throwable t) {
			Log.error("Exception during listener notification: " + t.getMessage());
			Log.printStackTrace(t);
		}
	}

	static class ServiceEvent {

		byte				eventType;

		ServiceReference	servRef;

		Service				service;

		ServiceEvent(byte eventType, ServiceReference servRef, Service service) {
			this.eventType = eventType;
			this.servRef = servRef;
			this.service = service;
		}

	}

}
