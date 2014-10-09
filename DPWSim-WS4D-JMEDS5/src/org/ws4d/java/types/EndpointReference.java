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

import org.ws4d.java.constants.HTTPConstants;
import org.ws4d.java.constants.SOAPConstants;

/**
 * Implementation of "wsa:EndpointReference" element.
 * 
 * @author mspies
 */
public class EndpointReference extends UnknownDataContainer {

	/** "wsa:EndpointReference/wsa:Address" element */
	private AttributedURI				address;

	/** optional "wsa:EndpointReference/wsa:ReferenceParameters" element */
	private ReferenceParametersMData	referenceParameters	= null;

	/** optional "wsa:EndpointReference/wsa:Metadata" element */
	private MetadataMData				endpointMetadata	= null;

	// /** List<XAddress> */
	// private List xaddresses = null;

	// ------------------------ CONSTRUCTOR ---------------------------

	/**
	 * Constructor.
	 * 
	 * @param address "wsa:EndpointReference/wsa:Address" element
	 */
	public EndpointReference(URI address) {
		this(address, null, null);
	}

	/**
	 * Constructor.
	 * 
	 * @param address "wsa:EndpointReference/wsa:Address" element
	 */
	public EndpointReference(AttributedURI address) {
		this(address, null, null);
	}

	/**
	 * Constructor.
	 * 
	 * @param address "wsa:EndpointReference/wsa:Address" element
	 * @param referenceParameters
	 *            "wsa:EndpointReference/wsa:ReferenceParameters"
	 */
	public EndpointReference(URI address, ReferenceParametersMData referenceParameters) {
		this(address, referenceParameters, null);
	}

	/**
	 * Constructor.
	 * 
	 * @param address "wsa:EndpointReference/wsa:Address" element
	 * @param referenceParameters
	 *            "wsa:EndpointReference/wsa:ReferenceParameters"
	 */
	public EndpointReference(AttributedURI address, ReferenceParametersMData referenceParameters) {
		this(address, referenceParameters, null);
	}

	/**
	 * Constructor.
	 * 
	 * @param address "wsa:EndpointReference/wsa:Address" element
	 * @param referenceParameters
	 *            "wsa:EndpointReference/wsa:ReferenceParameters"
	 * @param endpointMetadata "wsa:EndpointReference/wsa:Metadata"
	 */
	public EndpointReference(URI address, ReferenceParametersMData referenceParameters, MetadataMData endpointMetadata) {
		this(new AttributedURI(address), referenceParameters, endpointMetadata);
	}

	/**
	 * Constructor.
	 * 
	 * @param address "wsa:EndpointReference/wsa:Address" element
	 * @param referenceParameters
	 *            "wsa:EndpointReference/wsa:ReferenceParameters"
	 * @param endpointMetadata "wsa:EndpointReference/wsa:Metadata"
	 */
	public EndpointReference(AttributedURI address, ReferenceParametersMData referenceParameters, MetadataMData endpointMetadata) {
		super();
		this.address = address;
		this.referenceParameters = referenceParameters;
		this.endpointMetadata = endpointMetadata;
	}

	// -----------------------------------------------------------

	/**
	 * Gets "wsa:EndpointReference/wsa:Address" element from endpoint reference
	 * 
	 * @return "wsa:EndpointReference/wsa:Address" element
	 */
	public AttributedURI getAddress() {
		return address;
	}

	// /**
	// * Get list of xaddresses.
	// *
	// * @return List of type XAddress
	// */
	// public List getXAddresses() {
	// return xaddresses;
	// }
	//
	// /**
	// * Set list of xaddresses.
	// *
	// * @param xaddresses
	// * List of type XAddress;
	// */
	// public void setXAddresses(List xaddresses) {
	// this.xaddresses = xaddresses;
	// }

	/**
	 * Gets "wsa:EndpointReference/wsa:ReferenceParameters" element from
	 * endpoint reference
	 * 
	 * @return "wsa:EndpointReference/wsa:ReferenceParameters" element
	 */
	public ReferenceParametersMData getReferenceParameters() {
		return referenceParameters;
	}

	/**
	 * Gets "wsa:EndpointReference/wsa:Metadata" element from endpoint reference
	 * 
	 * @return "wsa:EndpointReference/wsa:Metadata" element
	 */
	public MetadataMData getEndpointMetadata() {
		return endpointMetadata;
	}

	/**
	 * Returns <code>true</code> if this endpoint reference is a HTTP or
	 * SOAP-over-UDP transport address.
	 * 
	 * @return <code>true</code> if this endpoint reference is a HTTP or
	 *         SOAP-over-UDP transport address.
	 */
	public boolean isXAddress() {
		if (address.isURN()) return false;

		String schema = address.getSchema();
		if (HTTPConstants.HTTP_SCHEMA.startsWith(schema)) {
			return true;
		} else if (HTTPConstants.HTTPS_SCHEMA.startsWith(schema)) {
			return true;
		} else if (SOAPConstants.SOAP_OVER_UDP_SCHEMA.startsWith(schema)) return true;
		return false;
	}

	// ---------------------------- Object -----------------------------------

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("EndpointReference [ address=").append(getAddress());
		sb.append(", referenceParameters=").append(getReferenceParameters());
		sb.append(" ]");
		return sb.toString();
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		return 31 + ((address == null) ? 0 : address.hashCode());
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object obj) {
		/*
		 * Only the address field is significant for endpoint reference.
		 */
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof EndpointReference)) {
			return false;
		}

		EndpointReference other = (EndpointReference) obj;
		if (address == null) {
			if (other.address != null) {
				return false;
			}
		} else if (!address.equals(other.address)) {
			return false;
		}

		return true;
	}

	public void setReferenceParameters(ReferenceParametersMData ref) {
		this.referenceParameters = ref;
	}
}
