/*******************************************************************************
 * Copyright (c) 2009 MATERNA Information & Communications. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html. For further
 * project-related information visit http://www.ws4d.org. The most recent
 * version of the JMEDS framework can be obtained from
 * http://sourceforge.net/projects/ws4d-javame.
 ******************************************************************************/

package org.ws4d.java.types;

import org.ws4d.java.util.StringUtil;

public class HostMData extends UnknownDataContainer {

	protected EndpointReference	endpoint;

	protected QNameSet			types;

	// protected Map mapHostMetadata_QN_2_MElement;
	//
	// // lazy initialization
	// protected Map mapAttributeMap_QN_2_MEAttribute;

	public HostMData() {
		super();
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		StringBuffer sb = new StringBuffer(StringUtil.formatClassName(getClass()));
		sb.append(" [ endpoint=").append(endpoint);
		sb.append(", types=").append(types);
		sb.append(" ]");
		return sb.toString();
	}

	// ---------------------- GETTER ------------------------

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.types.Host#getEndpointReferences()
	 */
	public EndpointReference getEndpointReference() {
		return endpoint;
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.types.Host#getTypes()
	 */
	public QNameSet getTypes() {
		return types;
	}

	// --------------------------- SETTER ----------------------

	/**
	 * Sets endpoint references.
	 * 
	 * @param endpoint
	 */
	public void setEndpointReference(EndpointReference endpoint) {
		this.endpoint = endpoint;
	}

	/**
	 * Sets port types.
	 * 
	 * @param qnTypes
	 */
	public void setTypes(QNameSet qnTypes) {
		this.types = qnTypes;
	}
}
