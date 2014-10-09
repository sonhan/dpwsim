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

import org.ws4d.java.structures.Iterator;
import org.ws4d.java.util.StringUtil;

/**
 * Container for data collected during the discovery phase.
 */
public class DiscoveryData extends UnknownDataContainer {

	public static final long	UNKNOWN_METADATA_VERSION	= -1;

	private EndpointReference	endpointReference;					// always

	// mandatory

	private QNameSet			types;

	private ScopeSet			scopes;

	private XAddressInfoSet		xAdrInfos;							// mandatory

	// only in

	private long				metadataVersion;					// not

	// always
	// mandatory

	public DiscoveryData() {
		this((EndpointReference) null);
	}

	/**
	 * @param endpointReference
	 */
	public DiscoveryData(EndpointReference endpointReference) {
		this(endpointReference, UNKNOWN_METADATA_VERSION);
	}

	/**
	 * @param endpointReference
	 * @param metadataVersion
	 */
	public DiscoveryData(EndpointReference endpointReference, long metadataVersion) {
		this(endpointReference, metadataVersion, null);

	}

	/**
	 * @param endpointReference
	 * @param metadataVersion
	 */
	public DiscoveryData(EndpointReference endpointReference, long metadataVersion, XAddressInfoSet xaddresses) {
		super();
		this.endpointReference = endpointReference;
		this.metadataVersion = metadataVersion;
		this.xAdrInfos = xaddresses;
	}

	/**
	 * Copy Constructor. Deep Copy: Data structure within will be also be
	 * copied.
	 */
	public DiscoveryData(DiscoveryData data) {
		metadataVersion = data.metadataVersion;
		endpointReference = data.endpointReference;
		if (data.getTypes() != null && !data.getTypes().isEmpty()) {
			setTypes(new QNameSet(data.types));
		}
		if (data.getScopes() != null && !data.getScopes().isEmpty()) {
			setScopes(new ScopeSet(data.scopes));
		}
		setXAddresInfoSet(new XAddressInfoSet(data.xAdrInfos));
	}

	/**
	 * Update discovery data with given new discovery data. If metadata version
	 * is newer, return true. If metadata version is older, nothing will be
	 * changed.
	 * 
	 * @param newData metadata to update this metadata.
	 * @return true - if metadata version is newer and previous metadata version
	 *         is not "-1" (== unknown metadata version), else false.
	 */
	public boolean update(DiscoveryData newData) {
		if (newData == this || newData == null) {
			return false;
		}

		if (metadataVersion < newData.metadataVersion) {
			boolean ret;
			if (metadataVersion == UNKNOWN_METADATA_VERSION) {
				ret = false;
			} else {
				ret = true;
			}

			metadataVersion = newData.metadataVersion;
			// XXX don't change the epr, it must be persistent
			types = newData.types;
			scopes = newData.scopes;
			xAdrInfos = newData.xAdrInfos;

			return ret;
		} else if (metadataVersion == newData.metadataVersion) {
			/*
			 * update current discovery data
			 */
			if (types != null) {
				QNameSet mergedTypes = new QNameSet(types);
				mergedTypes.addAll(newData.types);
				types = mergedTypes;
			} else {
				types = newData.types;
			}

			if (xAdrInfos != null) {
				XAddressInfoSet mergedXAddresses = new XAddressInfoSet(xAdrInfos);
				for (Iterator iter = newData.xAdrInfos.iterator(); iter.hasNext();) {
					XAddressInfo newXAddressInfo = (XAddressInfo) iter.next();
					XAddressInfo oldXAddressnfo = mergedXAddresses.get(newXAddressInfo);
					if (oldXAddressnfo != null) {
						oldXAddressnfo.mergeProtocolInfo(newXAddressInfo);
					} else {
						mergedXAddresses.add(newXAddressInfo);
					}
				}
				xAdrInfos = mergedXAddresses;
			} else {
				xAdrInfos = newData.xAdrInfos;
			}

			if (scopes != null) {
				ScopeSet mergedScopes = new ScopeSet(scopes);
				mergedScopes.addAll(newData.scopes);
				scopes = mergedScopes;
			} else {
				scopes = newData.scopes;
			}
		}

		return false;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		StringBuffer sb = new StringBuffer(StringUtil.formatClassName(getClass()));
		sb.append(" [ endpointReference=").append(endpointReference);
		sb.append(", types=").append(types);
		sb.append(", scopes=").append(scopes);
		sb.append(", xAddrs=").append(xAdrInfos);
		sb.append(", metadataVersion=").append(metadataVersion);
		sb.append(" ]");
		return sb.toString();
	}

	// ----------------------- GETTER / SETTER
	// ----------------------------------

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.types.DiscoveryData#getEndpointReference()
	 */
	public EndpointReference getEndpointReference() {
		return endpointReference;
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.types.DiscoveryData#getMetadataVersion()
	 */
	public long getMetadataVersion() {
		return metadataVersion;
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.types.DiscoveryData#getScopes()
	 */
	public ScopeSet getScopes() {
		return scopes;
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.types.DiscoveryData#getTypes()
	 */
	public QNameSet getTypes() {
		return types;
	}

	/**
	 * Returns a {@link XAddressInfoSet}.
	 * 
	 * @return a {@link XAddressInfoSet}.
	 */
	public XAddressInfoSet getXAddressInfoSet() {
		return xAdrInfos;
	}

	/**
	 * @param endpointReference the endpointReference to set
	 */
	public void setEndpointReference(EndpointReference endpointReference) {
		this.endpointReference = endpointReference;
	}

	/**
	 * @param metadataVersion the metadataVersion to set
	 */
	public void setMetadataVersion(long metadataVersion) {
		this.metadataVersion = metadataVersion;
	}

	/**
	 * @param types the types to set
	 */
	public void setTypes(QNameSet types) {
		this.types = types;
	}

	/**
	 * @param types the types to set
	 */
	public void addTypes(QNameSet types) {
		if (this.types != null) {
			this.types.addAll(types);
		} else {
			this.types = types;
		}
	}

	/**
	 * @param scopes the scopes to set
	 */
	public void setScopes(ScopeSet scopes) {
		this.scopes = scopes;
	}

	/**
	 * @param addrs the {@link XAddressInfo} to set.
	 */
	public void setXAddresInfoSet(XAddressInfoSet addrs) {
		xAdrInfos = addrs;
	}
}
