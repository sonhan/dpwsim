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

import org.ws4d.java.communication.CommunicationManagerRegistry;
import org.ws4d.java.communication.ProtocolInfo;

public class XAddressInfo {

	private URI				xAddress;

	String					comManId;

	private ProtocolInfo	protocolInfo;

	// if protocolInfo could not be determined safely protocolInfoIsNotDependable should be set to true 
	private boolean	protocolInfoNotDependable = false;
	
	public XAddressInfo() {
		this(null, CommunicationManagerRegistry.getDefault(), CommunicationManagerRegistry.getManager(CommunicationManagerRegistry.getDefault()).getProtocolInfo());
	}

	/**
	 * @param xAddressInfo
	 */
	public XAddressInfo(XAddressInfo xAddressInfo) {
		xAddress = xAddressInfo.xAddress;
		comManId = xAddressInfo.comManId;
		protocolInfo = (xAddressInfo.protocolInfo != null) ? xAddressInfo.protocolInfo.newClone() : null;
	}

	/**
	 * @param address
	 * @param comManId
	 */
	public XAddressInfo(URI address, String comManId) {
		this(address, comManId, null);
	}

	public XAddressInfo(URI address, String comManId, ProtocolInfo protocolInfo) {
		this.xAddress = address;
		this.comManId = comManId;
		this.protocolInfo = (protocolInfo != null) ? protocolInfo.newClone() : null;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append("XAddressInfo [ address=").append(xAddress).append(", comManId=").append(comManId).append(" ]");
		return buffer.toString();
	}

	/**
	 * @return the xAddress
	 */
	public URI getXAddress() {
		return xAddress;
	}

	/**
	 * @param address the xAddress to set
	 */
	public void setXAddress(URI address) {
		this.xAddress = address;
	}

	/**
	 * @return the comManId
	 */
	public String getComManId() {
		return comManId;
	}

	/**
	 * @param comManId the comManId to set
	 */
	public void setComManId(String comManId) {
		this.comManId = comManId;
	}

	public ProtocolInfo getProtocolInfo() {
		return protocolInfo;
	}

	public void setProtocolInfo(ProtocolInfo protocolInfo) {
		this.protocolInfo = (protocolInfo != null) ? protocolInfo.newClone() : null;
		protocolInfoNotDependable = false;
	}

	public void mergeProtocolInfo(ProtocolInfo protocolInfo) {
		if (this.protocolInfo != null && !protocolInfoNotDependable) {
			this.protocolInfo.merge(protocolInfo);
		} else {
			setProtocolInfo(protocolInfo);
		}
	}

	public void mergeProtocolInfo(XAddressInfo xAddressInfo) {
		if (this.protocolInfo != null && !protocolInfoNotDependable) {
			this.protocolInfo.merge(xAddressInfo.getProtocolInfo());
		} else {
			setProtocolInfo(xAddressInfo.getProtocolInfo());
		}
	}
	
	public boolean isProtocolInfoNotDependable() {
		return protocolInfoNotDependable;
	}

	
	public void setProtocolInfoNotDependable(boolean protocolInfoNotDependable) {
		this.protocolInfoNotDependable = protocolInfoNotDependable;
	}

	/**
	 * Returns a hash code value for the object.
	 * <P>
	 * ATTENTION: only {@link #getXAddress() address} and {@link #getComManId()
	 * comManId} are considered
	 * <P>
	 * 
	 * @return a hash code value for this object.
	 * @see java.lang.Object#hashCode()
	 */

	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((xAddress == null) ? 0 : xAddress.hashCode());
		result = prime * result + ((comManId == null) ? 0 : comManId.hashCode());
		return result;
	}

	/**
	 * Indicates whether some other object is "equal to" this one.
	 * <P>
	 * ATTENTION: only {@link #getXAddress() address} and {@link #getComManId()
	 * comManId} are considered
	 * <P>
	 * 
	 * @param obj the reference object with which to compare.
	 * @return <code>true</code> if this object is the same as the obj argument;
	 *         <code>false</code> otherwise.
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		XAddressInfo other = (XAddressInfo) obj;
		if (xAddress == null) {
			if (other.xAddress != null) return false;
		} else if (!xAddress.equals(other.xAddress)) return false;
		if (comManId == null) {
			if (other.comManId != null) return false;
		} else if (!comManId.equals(other.comManId)) return false;
		return true;
	}

}