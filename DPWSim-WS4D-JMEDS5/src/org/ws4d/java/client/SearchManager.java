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

import org.ws4d.java.DPWSFramework;
import org.ws4d.java.communication.CommunicationManager;
import org.ws4d.java.communication.CommunicationManagerRegistry;
import org.ws4d.java.communication.DefaultResponseCallback;
import org.ws4d.java.communication.Discovery;
import org.ws4d.java.communication.DiscoveryBinding;
import org.ws4d.java.communication.ProtocolData;
import org.ws4d.java.communication.ProtocolDomain;
import org.ws4d.java.communication.TimeoutException;
import org.ws4d.java.dispatch.DeviceServiceRegistry;
import org.ws4d.java.dispatch.DuplicateServiceReferenceException;
import org.ws4d.java.dispatch.HelloData;
import org.ws4d.java.dispatch.OutDispatcher;
import org.ws4d.java.message.Message;
import org.ws4d.java.message.discovery.ProbeMatch;
import org.ws4d.java.message.discovery.ProbeMatchesMessage;
import org.ws4d.java.message.discovery.ProbeMessage;
import org.ws4d.java.service.Device;
import org.ws4d.java.service.reference.DeviceListener;
import org.ws4d.java.service.reference.DeviceReference;
import org.ws4d.java.service.reference.ServiceReference;
import org.ws4d.java.structures.DataStructure;
import org.ws4d.java.structures.HashSet;
import org.ws4d.java.structures.Iterator;
import org.ws4d.java.types.EndpointReference;
import org.ws4d.java.types.ProbeScopeSet;
import org.ws4d.java.types.QNameSet;
import org.ws4d.java.types.URI;
import org.ws4d.java.types.XAddressInfo;
import org.ws4d.java.types.XAddressInfoSet;
import org.ws4d.java.util.Log;

/**
 * This class provides tools for searching local and remote devices and services
 * given a set of search criteria (see {@link SearchParameter}) and obtaining
 * references to devices/services with known endpoint addresses.
 * <p>
 * A typical usage of the search functionality states that the caller provides
 * an implementation of the {@link SearchCallback} interface which will receive
 * asynchronous notifications about matching services/devices found during the
 * search. Given this <code>SearchCallback</code> implementation and a
 * <code>SearchParameter</code> instance describing what kind of
 * devices/services to look for, the actual search can be started as depicted in
 * the following sample code:
 * </p>
 * 
 * <pre>
 * SearchCallback callback = ...; // provide a receiver for search matches
 * SearchParameter search = ...; // specify what to search for
 * SearchManager.searchDevice(parameter, callback, null);
 * </pre>
 * <p>
 * This example starts a search for a device, as the name of the called method
 * {@link #searchDevice(SearchParameter, SearchCallback, null)} suggests. If a device
 * fulfilling the given search parameter criteria is found, this will be
 * indicated asynchronously by a call to
 * {@link SearchCallback#deviceFound(DeviceReference, SearchParameter)}.
 * Similarly, if a search for services was issued (by means of
 * {@link #searchService(SearchParameter, SearchCallback)}), then matches would
 * result in a call to
 * {@link SearchCallback#serviceFound(ServiceReference, SearchParameter)}.
 * </p>
 * <p>
 * The second purpose of this class is to enable the obtaining of a reference to
 * a (local or remote) device/service when knowing its endpoint address (i.e.
 * one of its endpoint references). This process differs somehow from the
 * aforementioned search as it doesn't involve probing the network to assert the
 * existence of the specified device or service (as its endpoint reference is
 * already known). Thus, it is possible that calling
 * {@link DeviceReference#getDevice()} or {@link ServiceReference#getService()}
 * on the returned reference object results in a {@link TimeoutException} being
 * thrown because the specified device/service is for some reason currently not
 * reachable (e.g. it is not running at the moment or there is no network path
 * connecting it with the local machine). In contrast, using the search
 * abilities will provide notifications only about devices/services which are
 * currently running and reachable.
 * </p>
 */
public final class SearchManager {

	/**
	 * Gets device reference of device specified by an endpoint reference and an
	 * address. If <code>listener</code> is not <code>null</code>, it will be used as
	 * callback for device changes of the corresponding device.
	 * <p>
	 * This method will NOT try to discover (resolve/probe) the device. If the
	 * address is unreachable or wrong this method will return <code>null</code>.
	 * </p>
	 * 
	 * <p>
	 * A DeviceReference that was created by this method has not DiscoveryBinding and
	 * will therefore not receive hello or bye messages form its referenced device.
	 * </p>
	 * 
	 * @param epr endpoint reference of device for which to get device reference
	 * @param address the address of the device
	 * @param listener optional; will be informed on changes of device' state
	 * @param comManId ID of the communication manager to use when interacting
	 *        with supplied endpoint reference
	 * @return device reference for the specified device
	 */
	public static DeviceReference getDeviceReference(EndpointReference epr, URI address, DeviceListener listener, String comManId) {
		XAddressInfo xAdrInfo = new XAddressInfo(address, comManId);
		DeviceReference dRef = DeviceServiceRegistry.getDeviceReference(epr, new XAddressInfoSet(xAdrInfo), true);
		if (listener != null) {
			dRef.addListener(listener);
		}
		if (Log.isDebug()) {
			Log.debug("Device reference created from " + address + " over " + comManId);
		}
		return dRef;
	}

	/**
	 * Gets device reference of device specified by an endpoint reference and an
	 * address. If <code>listener</code> is not <code>null</code>, it will be used as
	 * callback for device changes of the corresponding device.
	 * <p>
	 * This method will NOT try to discover (resolve/probe) the device. If the
	 * address is unreachable or wrong this method will return <code>null</code>
	 * .
	 * </p>
	 * 
	 * @param epr endpoint reference of device for which to get device reference
	 * @param address the address of the device
	 * @param listener optional; will be informed on changes of device' state
	 * @param binding a DiscoveryBinding that specifies how to receive hello and bye messages for the DeviceReference
	 * @return device reference for the specified device
	 */
	public static DeviceReference getDeviceReference(EndpointReference epr, URI address, DeviceListener listener, DiscoveryBinding binding) {
		String comManId = binding.getCommunicationManagerId();
		XAddressInfo xAdrInfo = new XAddressInfo(address, comManId);
		DeviceReference dRef = DeviceServiceRegistry.getDeviceReference(epr, new XAddressInfoSet(xAdrInfo), true);
		if (listener != null) {
			dRef.addListener(listener);
		}
		DeviceServiceRegistry.register(binding);
		if (Log.isDebug()) {
			Log.debug("Device reference created from " + address + " over " + comManId);
		}
		return dRef;
	}

	/**
	 * Gets device reference of device with specified endpoint reference. If
	 * <code>listener</code> is not <code>null</code>, it will be used as
	 * callback for device changes of the corresponding device.
	 * 
	 * @param epr endpoint reference of device for which to get device reference
	 * @param listener optional; will be informed on changes of device' state
	 * @param binding a DiscoveryBinding that specifies how to receive hello and bye messages for the DeviceReference
	 * @return device reference
	 */
	public static DeviceReference getDeviceReference(EndpointReference epr, DeviceListener listener, DiscoveryBinding binding) {
		DeviceReference devRef = DeviceServiceRegistry.getDeviceReference(epr);
		if (listener != null) {
			devRef.addListener(listener);
		}
		DeviceServiceRegistry.register(binding);
		return devRef;
	}

	/**
	 * Gets device reference of device with specified endpoint reference. If
	 * <code>listener</code> is not <code>null</code>, it will be used as
	 * callback for device changes of the corresponding device.
	 * 
	 * @param helloData hello data of device for which to get device reference
	 * @param listener optional; will be informed about changes of the device's
	 *            state
	 * @return device reference
	 */
	public static DeviceReference getDeviceReference(HelloData helloData, DeviceListener listener) {
		DeviceReference devRef = DeviceServiceRegistry.getDeviceReference(helloData);
		if (listener != null) {
			devRef.addListener(listener);
		}

		for (Iterator it = CommunicationManagerRegistry.getLoadedManagers(); it.hasNext();) {
			CommunicationManager manager = (CommunicationManager) it.next();
			DeviceServiceRegistry.register(manager.getDiscoveryBindingForProtocolData(helloData.getProtocolData()));
		}

		return devRef;
	}

	/**
	 * Gets service reference of service with specified endpoint reference.
	 * <p>
	 * The returned @link {@link ServiceReference} instance can be used to
	 * obtain the actual service by calling
	 * {@link ServiceReference#getService()}.
	 * </p>
	 * 
	 * @param epr endpoint reference of service to get service reference for
	 * @param comManId ID of the communication manager to use when interacting
	 *            with supplied endpoint reference
	 * @return service reference
	 */
	public static ServiceReference getServiceReference(EndpointReference epr, String comManId) {
		return DeviceServiceRegistry.getServiceReference(epr, comManId, true);
	}

	/**
	 * Gets service reference of service with specified endpoint reference.
	 * <p>
	 * The returned @link {@link ServiceReference} instance can be used to
	 * obtain the actual service by calling
	 * {@link ServiceReference#getService()}.
	 * </p>
	 * 
	 * @param epr endpoint reference of service to get service reference for
	 * @param comManId ID of the communication manager to use when interacting
	 *            with supplied endpoint reference
	 * @return service reference
	 * @throws DuplicateServiceReferenceException in case a service reference
	 *             with the same endpoint reference is already present
	 */
	public static ServiceReference createServiceReference(EndpointReference epr, QNameSet portTypes, String comManId) throws DuplicateServiceReferenceException {
		return DeviceServiceRegistry.createServiceReference(epr, portTypes, comManId, null);
	}

	/**
	 * Searches for services. Uses search parameter to specify the search
	 * criteria. When matching services are found, notifications are sent to the
	 * given <code>callback</code> by means of the method
	 * {@link SearchCallback#serviceFound(ServiceReference, SearchParameter)}.
	 * 
	 * @param search search parameter to specify the criteria that matching
	 *            services must fulfill
	 * @param callback recipient of notifications about found matching services
	 */
	public static void searchService(SearchParameter search, SearchCallback callback) {
		searchDevice(search, callback, null);
	}

	/**
	 * Initiates a search for devices. A device is considered to match this
	 * search if its properties correspond to the values provided within
	 * argument <code>search</code>.
	 * <p>
	 * When a matching device is found, it is passed to the method
	 * {@link SearchCallback#deviceFound(DeviceReference, SearchParameter)} of
	 * the specified <code>callback</code> argument. Should
	 * <code>listener</code> not be <code>null</code>, it will be registered for
	 * tracking device changes on each matching device.
	 * </p>
	 * 
	 * @param search the search criteria for matching devices
	 * @param callback where search results are to be delivered to; must not be
	 *            <code>null</code>
	 * @param listener if not <code>null</code>, the listener is used for
	 *            asynchronous callbacks each time the state of a device
	 *            matching the search criteria changes (i.e. when it goes
	 *            online, etc.)
	 */
	public static void searchDevice(SearchParameter search, SearchCallback callback, DeviceListener listener) {
		if (callback == null) {
			throw new NullPointerException("callback is null");
		}
		if (search == null) {
			search = new SearchParameter();
		}
		if ((search.getSearchMode() & SearchParameter.MODE_LOCAL) != 0) {
			// look for local devices which would match the search criteria
			searchLocalReferences(search, callback, listener);
		}

		if ((search.getSearchMode() & SearchParameter.MODE_REMOTE) != 0) {
			/*
			 * FIXME handle searches over DeviceServiceRegistry, as potentially
			 * there could already exist some matching (cached) devices!
			 */
			ProbeMessage probe = new ProbeMessage(CommunicationManager.ID_NULL);

			QNameSet deviceTypes = search.getDeviceTypes();
			if (deviceTypes != null) {
				probe.setTypes(deviceTypes);
			}
			ProbeScopeSet scopes = search.getScopes();
			if (scopes != null) {
				probe.setScopes(scopes);
			}
			SearchMap map = search.getSearchMap();
			DataStructure domains;
			if (map != null) {
				domains = new HashSet();
				for (Iterator it = map.getPaths().iterator(); it.hasNext();) {
					SearchPath path = (SearchPath) it.next();
					ProtocolDomain domain = Discovery.getDomain(path.getTechnologyIdentifier(), path.getDomainIdentifier());
					if (domain != null) {
						domains.add(domain);
					} else {
						Log.warn("No protocol domain found for search path " + path);
					}
				}
			} else {
				// fall back to default output domains
				domains = Discovery.getDefaultOutputDomains();
			}

			OutDispatcher.getInstance().send(probe, null, domains, new SearchManagerCallback(null, search, callback, listener));
		}
	}

	/**
	 * Returns a data structure containing all the local devices within the
	 * current DPWS framework.
	 * 
	 * @return all local devices
	 */
	public static DataStructure getLocalDevices() {
		return DeviceServiceRegistry.getLocalDeviceReferences(null, null);
	}

	private static void searchLocalReferences(final SearchParameter search, final SearchCallback callback, final DeviceListener listener) {
		QNameSet serviceTypes = search.getServiceTypes();
		if (serviceTypes != null && serviceTypes.size() > 0) {
			DataStructure matchingLocalServices = DeviceServiceRegistry.getLocalServiceReferences(search.getServiceTypes(), search.getDeviceTypes(), search.getScopes());

			for (Iterator it = matchingLocalServices.iterator(); it.hasNext();) {
				final ServiceReference servRef = (ServiceReference) it.next();
				try {
					final DeviceReference devRef = servRef.getService().getParentDeviceReference();

					/*
					 * Call client code in a new thread, as it might call device
					 * remotely
					 */
					DPWSFramework.getThreadPool().execute(new Runnable() {

						/*
						 * (non-Javadoc)
						 * @see java.lang.Runnable#run()
						 */
						public void run() {
							if (devRef != null) {
								devRef.addListener(listener);
							}
							callback.serviceFound(servRef, search);
						}

					});

				} catch (TimeoutException e) {
					// this should not happen
					Log.printStackTrace(e);
				}
			}
		} else {
			DataStructure matchingLocalDevices = DeviceServiceRegistry.getLocalDeviceReferences(search.getDeviceTypes(), search.getScopes());

			for (Iterator it = matchingLocalDevices.iterator(); it.hasNext();) {
				final DeviceReference devRef = (DeviceReference) it.next();
				devRef.addListener(listener);
				/*
				 * Call client code in a new thread, as it might call device
				 * remotely
				 */
				DPWSFramework.getThreadPool().execute(new Runnable() {

					/*
					 * (non-Javadoc)
					 * @see java.lang.Runnable#run()
					 */
					public void run() {
						callback.deviceFound(devRef, search);
					}

				});
			}
		}
	}

	private SearchManager() {
		super();
	}

	private static class SearchManagerCallback extends DefaultResponseCallback {

		private final SearchParameter	search;

		private final SearchCallback	callback;

		private final DeviceListener	listener;

		private volatile boolean		noneFound	= true;

		/**
		 * @param search
		 * @param callback
		 * @param listener
		 */
		SearchManagerCallback(XAddressInfo targetXAddressInfo, SearchParameter search, SearchCallback callback, DeviceListener listener) {
			super(targetXAddressInfo);
			this.search = search;
			this.callback = callback;
			this.listener = listener;
		}

		/*
		 * (non-Javadoc)
		 * @see
		 * org.ws4d.java.communication.ResponseCallback#handle(org.ws4d.java
		 * .communication.message.Message,
		 * org.ws4d.java.message.discovery.ProbeMatchesMessage,
		 * org.ws4d.java.communication.ProtocolData)
		 */
		public void handle(Message request, ProbeMatchesMessage response, ProtocolData protocolData) {
			noneFound = false;
			for (Iterator it = CommunicationManagerRegistry.getLoadedManagers(); it.hasNext();) {
				CommunicationManager manager = (CommunicationManager) it.next();
				DeviceServiceRegistry.register(manager.getDiscoveryBindingForProtocolData(protocolData));
			}
			// DeviceServiceRegistry.register(DiscoveryBinding.getDiscoveryBinding(protocolData));

			// Only searches can reach this callback
			final QNameSet serviceTypes = search.getServiceTypes();
			if (serviceTypes != null && serviceTypes.size() > 0) {
				// CASE: search service reference before return to client
				for (Iterator it = response.getProbeMatches().iterator(); it.hasNext();) {
					ProbeMatch match = (ProbeMatch) it.next();

					final DeviceReference devRef = DeviceServiceRegistry.getUpdatedDeviceReference(match, response, protocolData);

					/*
					 * Calls client code in a new thread, as it might call
					 * device remotely
					 */
					DPWSFramework.getThreadPool().execute(new Runnable() {

						/*
						 * (non-Javadoc)
						 * @see java.lang.Runnable#run()
						 */
						public void run() {
							informOnServiceFound(devRef);
						}

					});
				}
			} else {
				// CASE: device discovered, return
				DataStructure matches = response.getProbeMatches();
				if (matches != null) {
					for (Iterator it = matches.iterator(); it.hasNext();) {
						ProbeMatch match = (ProbeMatch) it.next();

						final DeviceReference devRef = DeviceServiceRegistry.getUpdatedDeviceReference(match, response, protocolData);
						if (listener != null) {
							devRef.addListener(listener);
						}
						DPWSFramework.getThreadPool().execute(new Runnable() {

							/*
							 * (non-Javadoc)
							 * @see java.lang.Runnable#run()
							 */
							public void run() {
								callback.deviceFound(devRef, search);
							}

						});
					}
				}
			}
		}

		public void handleTimeout(Message request) {
			if (noneFound) {
				if (Log.isDebug()) {
					Log.debug("Search timeout for query: " + search, Log.DEBUG_LAYER_FRAMEWORK);
				} else {
					Log.info("Search timeout.");
				}
			}
		}

		/**
		 * Inform the client about the service founds.
		 * 
		 * @param devRef device reference to get device from
		 */
		private void informOnServiceFound(DeviceReference devRef) {
			// Builds up device, do it in a different thread. We won't block.
			try {
				Device device = devRef.getDevice();
				for (Iterator it_servRef = device.getServiceReferences(); it_servRef.hasNext();) {
					ServiceReference servRef = (ServiceReference) it_servRef.next();
					if (search.getServiceTypes().isContainedBy(servRef.getPortTypes())) {
						// we register a listener only if the service type
						// matches
						if (listener != null) {
							devRef.addListener(listener);
						}
						callback.serviceFound(servRef, search);
					}
				}
			} catch (TimeoutException e) {
				Log.printStackTrace(e);
			}
		}

	}

}
