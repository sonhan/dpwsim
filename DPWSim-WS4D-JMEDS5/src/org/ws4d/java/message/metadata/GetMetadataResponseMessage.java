/*******************************************************************************
 * Copyright (c) 2009 MATERNA Information & Communications. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html. For further
 * project-related information visit http://www.ws4d.org. The most recent
 * version of the JMEDS framework can be obtained from
 * http://sourceforge.net/projects/ws4d-javame.
 ******************************************************************************/
package org.ws4d.java.message.metadata;

import org.ws4d.java.constants.ConstantsHelper;
import org.ws4d.java.constants.MEXConstants;
import org.ws4d.java.message.Message;
import org.ws4d.java.message.SOAPHeader;
import org.ws4d.java.structures.ArrayList;
import org.ws4d.java.structures.DataStructure;
import org.ws4d.java.structures.Iterator;
import org.ws4d.java.types.AttributedURI;
import org.ws4d.java.types.EndpointReference;
import org.ws4d.java.types.EndpointReferenceSet;
import org.ws4d.java.types.HostMData;
import org.ws4d.java.types.HostedMData;
import org.ws4d.java.types.RelationshipMData;
import org.ws4d.java.types.URI;
import org.ws4d.java.types.URISet;
import org.ws4d.java.util.StringUtil;
import org.ws4d.java.wsdl.WSDL;

/**
 * 
 */
public class GetMetadataResponseMessage extends Message {

	public static final URI			ACTION	= new URI(MEXConstants.WSX_ACTION_GETMETADATA_RESPONSE);

	private EndpointReferenceSet	metadataReferences;

	private URISet					metadataLocations;

	private DataStructure			wsdls;

	private RelationshipMData		relationship;

	/**
	 * Creates a new GetMetadataResponse message containing a {@link SOAPHeader}
	 * with the appropriate {@link SOAPHeader#getAction() action property} set.
	 * All other header- and metadataexchange-related fields are empty and it is
	 * the caller's responsibility to fill them with suitable values.
	 */
	public GetMetadataResponseMessage(String communicationManagerId) {
		this(SOAPHeader.createHeader(MEXConstants.WSX_ACTION_GETMETADATA_RESPONSE, communicationManagerId));
	}

	/**
	 * @param header
	 */
	public GetMetadataResponseMessage(SOAPHeader header) {
		super(header);
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		StringBuffer sb = new StringBuffer(StringUtil.formatClassName(getClass()));
		sb.append(" [ header=").append(header);
		sb.append(", inbound=").append(inbound);
		sb.append(", metadataReferences=").append(metadataReferences);
		sb.append(", metadataLocations=").append(metadataLocations);
		sb.append(", relationship=").append(relationship);
		sb.append(" ]");
		return sb.toString();
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.message.Message#getType()
	 */
	public int getType() {
		return GET_METADATA_RESPONSE_MESSAGE;
	}

	/**
	 * @return the relationship
	 */
	public RelationshipMData getRelationship() {
		return relationship;
	}

	public HostMData getHost() {
		return relationship == null ? null : relationship.getHost();
	}

	public HostedMData getHosted(AttributedURI address) {
		if (relationship == null) {
			return null;
		} else {
			Iterator hostedIterator = relationship.getHosted().iterator();
			while (hostedIterator.hasNext()) {
				HostedMData tmp = (HostedMData) hostedIterator.next();
				if (tmp.getEprInfoSet().containsEprAddress(address)) {
					return tmp;
				}
			}
		}
		return null;
	}

	/**
	 * @param relationship the relationship to add
	 */
	public void addRelationship(RelationshipMData relationship, ConstantsHelper helper) {
		if (this.relationship == null) {
			this.relationship = relationship;
		} else {
			this.relationship.mergeWith(relationship, helper);
		}
	}

	/**
	 * @return the metadataReferences
	 */
	public EndpointReferenceSet getMetadataReferences() {
		return metadataReferences;
	}

	/**
	 * @param metadataReference the metadataReference to add
	 */
	public void addMetadataReference(EndpointReference metadataReference) {
		if (metadataReferences == null) {
			metadataReferences = new EndpointReferenceSet();
		}
		metadataReferences.add(metadataReference);
	}

	/**
	 * @return the metadataLocations
	 */
	public URISet getMetadataLocations() {
		return metadataLocations;
	}

	public void setMetadataLocations(URISet locations) {
		this.metadataLocations = locations;
	}

	/**
	 * @param metadataLocation the metadataLocation to add
	 */
	public void addMetadataLocation(URI metadataLocation) {
		if (metadataLocations == null) {
			metadataLocations = new URISet();
		}
		metadataLocations.add(metadataLocation);
	}

	/**
	 * @return the wsdls
	 */
	public DataStructure getWSDLs() {
		return wsdls;
	}

	/**
	 * @param wsdl the wsdl to add
	 */
	public void addWSDL(WSDL wsdl) {
		if (wsdls == null) {
			wsdls = new ArrayList();
		}
		wsdls.add(wsdl);
	}

}
