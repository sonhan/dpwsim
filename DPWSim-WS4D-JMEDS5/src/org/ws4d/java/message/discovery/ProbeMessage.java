/*******************************************************************************
 * Copyright (c) 2009 MATERNA Information & Communications. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html. For further
 * project-related information visit http://www.ws4d.org. The most recent
 * version of the JMEDS framework can be obtained from
 * http://sourceforge.net/projects/ws4d-javame.
 ******************************************************************************/

package org.ws4d.java.message.discovery;

import org.ws4d.java.constants.WSDConstants;
import org.ws4d.java.message.Message;
import org.ws4d.java.message.SOAPHeader;
import org.ws4d.java.types.ProbeScopeSet;
import org.ws4d.java.types.QNameSet;
import org.ws4d.java.types.URI;
import org.ws4d.java.util.StringUtil;

public class ProbeMessage extends Message {

	public static final URI	ACTION	= new URI(WSDConstants.WSD_ACTION_PROBE);

	private QNameSet		types;

	private ProbeScopeSet	scopes;

	private boolean			directed;

	public static SOAPHeader createProbeHeader(String communicationManagerId) {
		return DiscoveryMessage.createDiscoveryHeader(WSDConstants.WSD_ACTION_PROBE, communicationManagerId);
	}

	/**
	 * Creates a new Probe message containing a {@link SOAPHeader} with the
	 * appropriate {@link SOAPHeader#getAction() action property} set, the
	 * default {@link SOAPHeader#getTo() to property} for ad-hoc mode (
	 * {@link WSDConstants#WSD_TO}) and a unique
	 * {@link SOAPHeader#getMessageId() message ID property}. All other header-
	 * and discovery-related fields are empty and it is the caller's
	 * responsibility to fill them with suitable values.
	 */
	public ProbeMessage(String communicationManagerId) {
		this(createProbeHeader(communicationManagerId));
	}

	/**
	 * @param header
	 */
	public ProbeMessage(SOAPHeader header) {
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
		sb.append(", directed=").append(directed);
		sb.append(", types=").append(types);
		sb.append(", scopes=").append(scopes);
		sb.append(" ]");
		return sb.toString();
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.message.DPWSMessage#getType()
	 */
	public int getType() {
		return PROBE_MESSAGE;
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.message.discovery.ProbeMessage#getScopes()
	 */
	public ProbeScopeSet getScopes() {
		return scopes;
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.message.discovery.ProbeMessage#getTypes()
	 */
	public QNameSet getTypes() {
		return types;
	}

	/**
	 * @param types the types to set
	 */
	public void setTypes(QNameSet types) {
		this.types = types;
	}

	/**
	 * @param scopes the scopes to set
	 */
	public void setScopes(ProbeScopeSet scopes) {
		this.scopes = scopes;
	}

	/**
	 * @return the directed
	 */
	public boolean isDirected() {
		return directed;
	}

	/**
	 * @param directed the directed to set
	 */
	public void setDirected(boolean directed) {
		this.directed = directed;
	}
}
