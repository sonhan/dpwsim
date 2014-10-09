/*******************************************************************************
 * Copyright (c) 2009 MATERNA Information & Communications. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html. For further
 * project-related information visit http://www.ws4d.org. The most recent
 * version of the JMEDS framework can be obtained from
 * http://sourceforge.net/projects/ws4d-javame.
 ******************************************************************************/
package org.ws4d.java.communication;

import org.ws4d.java.DPWSFramework;
import org.ws4d.java.structures.ArrayList;
import org.ws4d.java.structures.DataStructure;
import org.ws4d.java.structures.HashMap;
import org.ws4d.java.structures.HashSet;
import org.ws4d.java.structures.Iterator;
import org.ws4d.java.util.Log;

/**
 * 
 */
public final class Discovery {

	private static final DataStructure	DEFAULT_OUTPUT_DOMAINS	= new HashSet();

	private static final HashMap		DOMAIN_CACHE			= new HashMap();

	static DataStructure				domains					= new ArrayList();

	/**
	 * @return the currently set default protocol domains
	 */
	public static DataStructure getDefaultOutputDomains() {
		synchronized (DEFAULT_OUTPUT_DOMAINS) {
			return new HashSet(DEFAULT_OUTPUT_DOMAINS);
		}
	}

	public static void addDefaultOutputDomain(ProtocolDomain domain) {
		if (domain == null) {
			return;
		}
		synchronized (DEFAULT_OUTPUT_DOMAINS) {
			DEFAULT_OUTPUT_DOMAINS.add(domain);
		}
		if (Log.isDebug()) {
			Log.debug("Output Discovery over " + domain.getCommunicationManagerId() + ", " + domain, Log.DEBUG_LAYER_FRAMEWORK);
		}
	}

	public static void removeDefaultOutputDomain(ProtocolDomain domain) {
		if (domain == null) {
			return;
		}
		synchronized (DEFAULT_OUTPUT_DOMAINS) {
			DEFAULT_OUTPUT_DOMAINS.remove(domain);
		}
	}

	public static void clearDefaultOutputDomains() {
		synchronized (DEFAULT_OUTPUT_DOMAINS) {
			DEFAULT_OUTPUT_DOMAINS.clear();
		}
	}

	public static DataStructure getAllAvailableDomains() {
		DataStructure domains = new ArrayList();
		for (Iterator it = CommunicationManagerRegistry.getLoadedManagers(); it.hasNext();) {
			CommunicationManager manager = (CommunicationManager) it.next();
			domains.addAll(manager.getAvailableDomains());
		}
		/*
		 * check for domain changes and debug. if this domains are new, show
		 * them.
		 */
		if (Log.isDebug() && !Discovery.domains.equals(domains)) {
			Log.debug("-------------------------------------------", Log.DEBUG_LAYER_FRAMEWORK);
			for (Iterator it = domains.iterator(); it.hasNext();) {
				ProtocolDomain domain = (ProtocolDomain) it.next();
				Log.debug("Found Protocol Domain: " + domain.getCommunicationManagerId() + ", " + domain.toString(), Log.DEBUG_LAYER_FRAMEWORK);
			}
			Log.debug("-------------------------------------------", Log.DEBUG_LAYER_FRAMEWORK);
		}
		if (Discovery.domains == null || Discovery.domains.size() == 0) {
			Discovery.domains = domains;
		}
		return domains;
	}

	/**
	 * @param protocolId
	 * @param domainId
	 * @return returns the protocol domain instance matching the given
	 *         <code>protocolId</code> and <code>domainId</code> or
	 *         <code>null</code> if such a protocol domain was not found/is not
	 *         present
	 */
	public static ProtocolDomain getDomain(String protocolId, String domainId) {
		if (protocolId == null) {
			return null;
		}
		synchronized (DOMAIN_CACHE) {
			ProtocolDomain domain = (ProtocolDomain) DOMAIN_CACHE.get(protocolId + (domainId == null ? "" : domainId));
			if (domain != null) {
				return domain;
			}
			CommunicationManager manager = DPWSFramework.getCommunicationManager(protocolId);
			if (manager == null) {
				Log.warn("No communication manager found for protocol ID " + protocolId);
				return null;
			}
			DataStructure domains = manager.getAvailableDomains();
			if (domains == null || domains.isEmpty()) {
				Log.warn("Communication manager for protocol ID " + protocolId + " has no available domains");
				return null;
			}
			for (Iterator it = domains.iterator(); it.hasNext();) {
				domain = (ProtocolDomain) it.next();
				/*
				 * domainId could be e.g. a network interface name or IP
				 * address, e.g. eth0, 139.2.58.102, etc.
				 */
				String[] domainIds = domain.getDomainIds();
				for (int i = 0; i < domainIds.length; i++) {
					if (domainIds[i].equals(domainId)) {
						DOMAIN_CACHE.put(protocolId + domainId, domain);
						return domain;
					}
				}
			}
		}
		Log.warn("No protocol domain found with domain ID " + domainId);
		return null;
	}

	/**
	 * 
	 */
	private Discovery() {
		super();
	}

}
