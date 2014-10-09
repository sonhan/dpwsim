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

import org.ws4d.java.message.discovery.DiscoveryMessage;
import org.ws4d.java.types.ProbeScopeSet;
import org.ws4d.java.types.QNameSet;
import org.ws4d.java.types.ScopeSet;
import org.ws4d.java.util.Log;
import org.ws4d.java.util.StringUtil;

/**
 * A collection of search criteria used when searching for devices or services.
 * 
 * @see SearchManager
 */
public class SearchParameter {

	public static final byte	MODE_LOCAL				= 0x1;

	public static final byte	MODE_REMOTE				= 0x2;

	public static final byte	MODE_LOCAL_AND_REMOTE	= MODE_LOCAL | MODE_REMOTE;

	private byte				searchMode				= MODE_LOCAL_AND_REMOTE;

	private SearchMap			searchMap;

	/** */
	private QNameSet			deviceTypes				= null;

	/** */
	private ProbeScopeSet		scopes					= null;

	/** */
	private QNameSet			serviceTypes			= null;

	/** */
	private Object				referenceObject			= null;

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		StringBuffer sb = new StringBuffer(StringUtil.formatClassName(getClass()));
		sb.append(" [ deviceTypes=").append(deviceTypes);
		sb.append(", scopes=").append(scopes);
		sb.append(", serviceTypes=").append(serviceTypes);
		sb.append(", searchMap=").append(searchMap);
		sb.append(" ]");
		return sb.toString();
	}

	/**
	 * Returns the configured search mode. If not explicitly set vie
	 * {@link #setSearchMode(byte)}, the default search mode
	 * {@link #MODE_LOCAL_AND_REMOTE} is returned.
	 * <p>
	 * The search mode defines whether to search only local devices and
	 * services, only remote devices and services or both based on this
	 * <code>SearchParameter</code> instance.
	 * </p>
	 * 
	 * @return the search mode to use
	 * @see #setSearchMode(byte)
	 * @see #MODE_LOCAL
	 * @see #MODE_REMOTE
	 * @see #MODE_LOCAL_AND_REMOTE
	 */
	public byte getSearchMode() {
		return searchMode;
	}

	/**
	 * Sets the desired search mode.
	 * 
	 * @param searchMode the search mode to set
	 * @see #getSearchMode()
	 * @see #MODE_LOCAL
	 * @see #MODE_REMOTE
	 * @see #MODE_LOCAL_AND_REMOTE
	 */
	public void setSearchMode(byte searchMode) {
		if ((searchMode & MODE_LOCAL_AND_REMOTE) == 0) {
			Log.warn("invalid search mode: " + searchMode + ", setting to default");
			searchMode = MODE_LOCAL_AND_REMOTE;
		}
		this.searchMode = searchMode;
	}

	/**
	 * Returns the search map to use when discovering devices and services.
	 * 
	 * @return the search map for the search process
	 */
	public SearchMap getSearchMap() {
		return searchMap;
	}

	/**
	 * Sets the search map for the discovery process.
	 * 
	 * @param searchMap the search map to use, if <code>null</code>, a default
	 *            search map will be used
	 */
	public void setSearchMap(SearchMap searchMap) {
		this.searchMap = searchMap;
	}

	/**
	 * Gets device port types of device to discover.
	 * 
	 * @return device port type.
	 */
	public QNameSet getDeviceTypes() {
		return deviceTypes;
	}

	/**
	 * Sets device port types of device to discover.
	 * 
	 * @param deviceTypes device port types.
	 */
	public void setDeviceTypes(QNameSet deviceTypes) {
		this.deviceTypes = deviceTypes;
	}

	/**
	 * Gets list of scopes of device to discover.
	 * 
	 * @return list of scopes.
	 */
	public ProbeScopeSet getScopes() {
		return scopes;
	}

	/**
	 * Sets list of scopes of device to discover.
	 * 
	 * @param scopes list of scopes.
	 */
	public void setScopes(ProbeScopeSet scopes) {
		this.scopes = scopes;
	}

	/**
	 * Gets service port types of service to discover.
	 * 
	 * @return service port types.
	 */
	public QNameSet getServiceTypes() {
		return serviceTypes;
	}

	/**
	 * Sets service port types of service to discover. If no device filters are
	 * set, all devices are discovered. Later on the discovered services all
	 * filtered.
	 * 
	 * @param serviceTypes service port types.
	 */
	public void setServiceTypes(QNameSet serviceTypes) {
		this.serviceTypes = serviceTypes;
	}

	/**
	 * Gets reference object.
	 * 
	 * @return reference object.
	 */
	public Object getReferenceObject() {
		return referenceObject;
	}

	/**
	 * Sets reference object. The reference object can include data, which is
	 * important for the further handling of the discovered devices or services.
	 * 
	 * @param referenceObject
	 */
	public void setReferenceObject(Object referenceObject) {
		this.referenceObject = referenceObject;
	}

	/**
	 * Checks if the device sending the discovery message matches the searched
	 * device port types and scopes, which are part of the searchParameter. To
	 * match the device both the port types and the scopes must be part of the
	 * device.
	 * 
	 * @param searchParameter SearchParameter containing port types and scopes.
	 * @param message Discovery message of device.
	 * @return <code>true</code> - if both the given device port types and
	 *         scopes are part of the device.
	 */
	protected boolean matchesSearch(DiscoveryMessage message) {
		QNameSet msgDeviceTypes = message.getTypes();
		if (deviceTypes == null || deviceTypes.isEmpty() || (msgDeviceTypes != null && msgDeviceTypes.containsAll(deviceTypes))) {
			// check scopes
			if (scopes != null && !scopes.isEmpty()) {
				ScopeSet msgScopes = message.getScopes();
				if (msgScopes == null || msgScopes.isEmpty() || !msgScopes.containsAll(scopes)) {
					return false;
				}
			}
			return true;
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((deviceTypes == null) ? 0 : deviceTypes.hashCode());
		result = prime * result + ((referenceObject == null) ? 0 : referenceObject.hashCode());
		result = prime * result + ((scopes == null) ? 0 : scopes.hashCode());
		result = prime * result + ((searchMap == null) ? 0 : searchMap.hashCode());
		result = prime * result + searchMode;
		result = prime * result + ((serviceTypes == null) ? 0 : serviceTypes.hashCode());
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		SearchParameter other = (SearchParameter) obj;
		if (deviceTypes == null) {
			if (other.deviceTypes != null) return false;
		} else if (!deviceTypes.equals(other.deviceTypes)) return false;
		if (referenceObject == null) {
			if (other.referenceObject != null) return false;
		} else if (!referenceObject.equals(other.referenceObject)) return false;
		if (scopes == null) {
			if (other.scopes != null) return false;
		} else if (!scopes.equals(other.scopes)) return false;
		if (searchMap == null) {
			if (other.searchMap != null) return false;
		} else if (!searchMap.equals(other.searchMap)) return false;
		if (searchMode != other.searchMode) return false;
		if (serviceTypes == null) {
			if (other.serviceTypes != null) return false;
		} else if (!serviceTypes.equals(other.serviceTypes)) return false;
		return true;
	}
	
	

}
