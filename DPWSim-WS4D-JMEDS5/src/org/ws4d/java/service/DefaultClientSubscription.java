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

import org.ws4d.java.DPWSFramework;
import org.ws4d.java.communication.TimeoutException;
import org.ws4d.java.eventing.ClientSubscriptionInternal;
import org.ws4d.java.eventing.EventSink;
import org.ws4d.java.eventing.EventingException;
import org.ws4d.java.service.reference.ServiceReference;
import org.ws4d.java.types.EndpointReference;
import org.ws4d.java.types.EprInfo;
import org.ws4d.java.util.TimedEntry;
import org.ws4d.java.util.WatchDog;

/**
 * Subscription class, manages a client-side subscription.
 */
public final class DefaultClientSubscription extends TimedEntry implements ClientSubscriptionInternal {

	final EventSink					sink;

	final String					clientSubscriptionId;

	private long					timeoutTime;

	final EprInfo		subscriptionManagerAddressInfo;

	private ServiceReference		servRef;

	private String	comManId;

	/**
	 * Constructor.
	 * 
	 * @param sink
	 * @param clientSubscriptionId
	 * @param serviceSubscriptionId
	 * @param duration
	 * @param servRef
	 */
	public DefaultClientSubscription(EventSink sink, String clientSubscriptionId, EndpointReference subscriptionManagerEpr, String comManId, long duration, ServiceReference servRef) {
		this.sink = sink;
		this.clientSubscriptionId = clientSubscriptionId;
		subscriptionManagerAddressInfo = new EprInfo(subscriptionManagerEpr, comManId);
		this.comManId = comManId;
		if (duration != 0) {
			timeoutTime = System.currentTimeMillis() + duration;
			WatchDog.getInstance().register(this, duration);
		} else {
			timeoutTime = 0;
		}
		this.servRef = servRef;
		DPWSFramework.addClientSubscrption(this);
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.eventing.ClientSubscription#getTimeoutTime()
	 */
	public synchronized long getTimeoutTime() {
		return timeoutTime;
	}

	// public URI getSubscriptionId() {
	// return serviceSubscriptionId.getReferenceParameters().getWseIdentifier();
	// }


	/* (non-Javadoc)
	 * @see org.ws4d.java.eventing.ClientSubscription#getSubscriptionManagerXAddressInfo()
	 */
	public EprInfo getSubscriptionManagerAddressInfo() {
		return subscriptionManagerAddressInfo;
	}


	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.eventing.ClientSubscription#getServiceSubscriptionId()
	 */
	public String getServiceSubscriptionId() {
		return subscriptionManagerAddressInfo.getEndpointReference().getReferenceParameters().getWseIdentifier();
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.ws4d.java.eventing.ClientSubscription#getCommunicationManagerId()
	 */
	public String getCommunicationManagerId() {
		return comManId;
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.eventing.ClientSubscription#getEventSink()
	 */
	public EventSink getEventSink() {
		return sink;
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.eventing.ClientSubscription#getServiceReference()
	 */
	public ServiceReference getServiceReference() {
		return servRef;
	}

	// ----------------------- SUBCRIPTION HANDLING -----------------

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.eventing.ClientSubscription#renew(long)
	 */
	public long renew(long duration) throws EventingException, TimeoutException {
		return servRef.getService().renew(this, duration);
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.eventing.ClientSubscription#unsubscribe()
	 */
	public void unsubscribe() throws EventingException, TimeoutException {
		servRef.getService().unsubscribe(this);
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.eventing.ClientSubscription#getStatus()
	 */
	public long getStatus() throws EventingException, TimeoutException {
		long duration = servRef.getService().getStatus(this);
		updateTimeoutTime(duration);
		return duration;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.ws4d.java.eventing.ClientSubscriptionInternal#renewInternal(long)
	 */
	public void renewInternal(long newDuration) {
		if (newDuration != 0) {
			WatchDog.getInstance().update(this, newDuration);
		} else {
			WatchDog.getInstance().unregister(this);
		}
		updateTimeoutTime(newDuration);
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.eventing.ClientSubscriptionInternal#dispose()
	 */
	public void dispose() {
		WatchDog.getInstance().unregister(this);
		DPWSFramework.removeClientSubscrption(this);
	}

	// -------------------- TimedEntry --------------------

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.management.TimedEntry#timedOut()
	 */
	protected void timedOut() {
		DPWSFramework.removeClientSubscrption(this);
		sink.getEventListener().subscriptionTimeoutReceived(this);
	}

	private synchronized void updateTimeoutTime(long duration) {
		if (duration == 0L) {
			timeoutTime = 0L;
		} else {
			timeoutTime = System.currentTimeMillis() + duration;
		}
	}

}
