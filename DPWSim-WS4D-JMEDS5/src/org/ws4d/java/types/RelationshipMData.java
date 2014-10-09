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

import org.ws4d.java.constants.ConstantsHelper;
import org.ws4d.java.structures.DataStructure;
import org.ws4d.java.structures.Iterator;
import org.ws4d.java.structures.LinkedList;
import org.ws4d.java.util.StringUtil;

/**
 * 
 * 
 */
public class RelationshipMData extends UnknownDataContainer {

	// public static final URI RELATIONSHIP_METADATA_DIALECT = new
	// URI(DPWSConstants.METADATA_DIALECT_RELATIONSHIP);

	// public static final URI HOST_RELATIONSHIP_TYPE = new
	// URI(DPWSConstants.METADATA_RELATIONSHIP_HOSTING_TYPE);

	private URI				type		= null;

	private HostMData		hostData	= null;

	private DataStructure	hostedData	= null;

	/**
	 * Constructor.
	 */
	public RelationshipMData() {
		super();
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		StringBuffer sb = new StringBuffer(StringUtil.formatClassName(getClass()));
		sb.append(" [ type=").append(type);
		sb.append(", hostData=").append(hostData);
		sb.append(", hostedData=").append(hostedData);
		sb.append(" ]");
		return sb.toString();
	}

	// ----------------------------- METHODS --------------------------

	/**
	 * Gets relationship type.
	 * 
	 * @return relationship type.
	 */
	public URI getType() {
		return type;
	}

	/**
	 * Sets relationship type.
	 * 
	 * @param type relationship type.
	 */
	public void setType(URI type) {
		this.type = type;
	}

	/**
	 * Gets host metadata.
	 * 
	 * @return host metadata.
	 */
	public HostMData getHost() {
		return hostData;
	}

	/**
	 * Sets host (device) metadata.
	 * 
	 * @param hostData host metadata.
	 */
	public void setHost(HostMData hostData) {
		this.hostData = hostData;
	}

	/**
	 * Gets the data structure of {@link HostedMData hosted (service) metadata}.
	 * 
	 * @return list of hosted metadata.
	 */
	public DataStructure getHosted() {
		return hostedData;
	}

	/**
	 * Sets the data structure of hosted (service) metadata.
	 * 
	 * @param hostedData data structure of hosted metadata.
	 */
	public void setHosted(DataStructure hostedData) {
		this.hostedData = hostedData;
	}

	public void addHosted(HostedMData hosted) {
		if (hostedData == null) {
			hostedData = new LinkedList();
		}
		hostedData.add(hosted);
	}

	public void mergeWith(RelationshipMData relationship, ConstantsHelper helper) {
		if (relationship == null) {
			return;
		}
		if (type == null || !helper.getMetadataRelationshipHostingType().equals(type.toString())) {
			return;
		}
		if (relationship.type == null || !helper.getMetadataRelationshipHostingType().equals(relationship.type.toString())) {
			return;
		}
		if (relationship.hostedData != null) {
			for (Iterator it = relationship.hostedData.iterator(); it.hasNext();) {
				Object o = it.next();
				if (!hostedData.contains(o)) {
					hostedData.add(o);
				}
			}
		}
	}

}
