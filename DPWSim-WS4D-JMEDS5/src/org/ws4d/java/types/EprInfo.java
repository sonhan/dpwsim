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

import org.ws4d.java.communication.ProtocolInfo;

public class EprInfo extends XAddressInfo {

	EndpointReference	endpointReference;

	public EprInfo(EndpointReference endpointReference, URI xAddress, String comManId) {
		this(endpointReference, xAddress, comManId, null);
	}

	public EprInfo(EndpointReference endpointReference, URI xAddress, String comManId, ProtocolInfo pvi) {
		super(xAddress, comManId, pvi);
		this.endpointReference = endpointReference;
	}

	public EprInfo(EndpointReference endpointReference, String comManId) {
		this(endpointReference, comManId, null);
	}

	public EprInfo(EndpointReference endpointReference, String comManId, ProtocolInfo pvi) {
		super(null, comManId, pvi);
		if (endpointReference.isXAddress()) setXAddress(endpointReference.getAddress());
		this.endpointReference = endpointReference;
	}

	public EndpointReference getEndpointReference() {
		return endpointReference;
	}

	public void setEndpointReference(EndpointReference epr) {
		this.endpointReference = epr;
	}

	/**
	 * Returns a hash code value for the object.
	 * <P>
	 * ATTENTION: only {@link #getEndpointReference() endpointReference} and
	 * {@link XAddressInfo#getComManId() comManId} are considered
	 * <P>
	 * 
	 * @return a hash code value for this object.
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((comManId == null) ? 0 : comManId.hashCode());
		result = prime * result + ((endpointReference == null) ? 0 : endpointReference.hashCode());
		return result;
	}

	/**
	 * Indicates whether some other object is "equal to" this one.
	 * <P>
	 * ATTENTION: only {@link #getEndpointReference() endpointReference} and
	 * {@link XAddressInfo#getComManId() comManId} are considered
	 * <P>
	 * 
	 * @param obj the reference object with which to compare.
	 * @return <code>true</code> if this object is the same as the obj argument;
	 *         <code>false</code> otherwise.
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!super.equals(obj)) return false;
		if (getClass() != obj.getClass()) return false;
		EprInfo other = (EprInfo) obj;
		if (comManId == null) {
			if (other.comManId != null) return false;
		} else if (!comManId.equals(other.comManId)) return false;
		if (endpointReference == null) {
			if (other.endpointReference != null) return false;
		} else if (!endpointReference.equals(other.endpointReference)) return false;
		return true;
	}

}
