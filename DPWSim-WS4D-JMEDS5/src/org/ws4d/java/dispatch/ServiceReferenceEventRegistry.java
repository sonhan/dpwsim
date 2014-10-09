package org.ws4d.java.dispatch;

import org.ws4d.java.dispatch.ServiceListenerQueue.ServiceEvent;
import org.ws4d.java.service.Service;
import org.ws4d.java.service.reference.ServiceListener;
import org.ws4d.java.service.reference.ServiceReference;
import org.ws4d.java.structures.Iterator;
import org.ws4d.java.structures.LockedMap;

/**
 * Registry to manage all listeners to service changes, who implement the
 * {@link ServiceListener} interface.
 */
public class ServiceReferenceEventRegistry {

	private static ServiceReferenceEventRegistry	instance	= new ServiceReferenceEventRegistry();

	public static ServiceReferenceEventRegistry getInstance() {
		return instance;
	}

	// ServiceListener --> ListenerQueue
	private LockedMap	listeners	= new LockedMap();

	private ServiceReferenceEventRegistry() {

	}

	/**
	 * Register listener (callback) for service reference changes. Listeners get
	 * get information about service changes.
	 * 
	 * @param listener listener (callback) to register.
	 */
	public void registerServiceListening(ServiceListener listener) {
		if (listeners == null) {
			listeners = new LockedMap();
		}
		listeners.exclusiveLock();
		try {
			if (listeners.containsKey(listener)) {
				// no need to create new listener queue
				return;
			}
			listeners.put(listener, new ServiceListenerQueue(listener));
		} finally {
			listeners.releaseExclusiveLock();
		}
	}

	/**
	 * Unregisters listener for service reference listening. This method should
	 * be called, if holder of reference is no more interested in this
	 * reference.
	 * 
	 * @param listener listener to remove.
	 */
	public void unregisterServiceListening(ServiceListener listener) {
		listeners.exclusiveLock();
		try {
			listeners.remove(listener);
		} finally {
			listeners.releaseExclusiveLock();
		}
	}

	private void announceServiceListenerEvent(byte eventType, ServiceReference servRef, Service service) {
		listeners.sharedLock();
		try {
			ServiceEvent event = new ServiceEvent(eventType, servRef, service);
			for (Iterator it = listeners.values().iterator(); it.hasNext();) {
				ServiceListenerQueue queue = (ServiceListenerQueue) it.next();
				queue.announce(event);
			}
		} finally {
			listeners.releaseSharedLock();
		}
	}

	protected void announceServiceChanged(ServiceReference servRef, Service service) {
		announceServiceListenerEvent(ServiceListenerQueue.SERVICE_CHANGED_EVENT, servRef, service);
	}

	protected void announceServiceCreated(ServiceReference servRef, Service service) {
		announceServiceListenerEvent(ServiceListenerQueue.SERVICE_CREATED_EVENT, servRef, service);
	}

	protected void announceServiceDisposed(ServiceReference servRef) {
		announceServiceListenerEvent(ServiceListenerQueue.SERVICE_DISPOSED_EVENT, servRef, null);
	}

}
