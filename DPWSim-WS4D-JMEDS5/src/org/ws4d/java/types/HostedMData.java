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

public class HostedMData extends UnknownDataContainer {

	private URI			serviceId;

	private EprInfoSet	endpointRefs	= null;

	private QNameSet	types;

	/**
	 * Constructor.
	 */
	public HostedMData() {
		super();
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		StringBuffer sb = new StringBuffer(StringUtil.formatClassName(getClass()));
		sb.append(" [ endpointRefs=").append(endpointRefs);
		sb.append(", types=").append(types);
		sb.append(", serviceId=").append(serviceId);
		sb.append(" ]");
		return sb.toString();
	}

	public boolean isEqualTo(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		HostedMData other = (HostedMData) obj;
		if (endpointRefs == null) {
			if (other.endpointRefs != null) {
				return false;
			}
		} else if (!endpointRefs.equals(other.endpointRefs)) {
			return false;
		}
		if (serviceId == null) {
			if (other.serviceId != null) {
				return false;
			}
		} else if (!serviceId.equals(other.serviceId)) {
			return false;
		}
		if (types == null) {
			if (other.types != null) {
				return false;
			}
		} else if (!types.equals(other.types)) {
			return false;
		}
		return true;
	}

	public URI getServiceId() {
		return serviceId;
	}

	/**
	 * Sets service id.
	 * 
	 * @param serviceId
	 */
	public void setServiceId(URI serviceId) {
		this.serviceId = serviceId;
	}

	/**
	 * Returns the {@link EprInfoSet}.
	 * 
	 * @return the {@link EprInfoSet}.
	 */
	public EprInfoSet getEprInfoSet() {
		if (endpointRefs == null) {
			endpointRefs = new EprInfoSet();
		}
		return endpointRefs;
	}

	/**
	 * Sets the {@link EprInfoSet}.
	 * 
	 * @param eprInfoSet
	 */
	public void setEprInfoSet(EprInfoSet eprInfoSet) {
		this.endpointRefs = eprInfoSet;
	}

	/**
	 * Method to add {@link EprInfo}. Not thread safe.
	 * 
	 * @param eprInfo
	 */
	public void addEprInfo(EprInfo eprInfo) {
		if (endpointRefs == null) {
			endpointRefs = new EprInfoSet();
		}
		endpointRefs.add(eprInfo);
	}

	public QNameSet getTypes() {
		return types;
	}

	/**
	 * Sets service types.
	 * 
	 * @param types
	 */
	public void setTypes(QNameSet types) {
		this.types = types;
	}
}
