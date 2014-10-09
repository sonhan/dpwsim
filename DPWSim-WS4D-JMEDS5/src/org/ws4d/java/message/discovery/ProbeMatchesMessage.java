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
import org.ws4d.java.structures.ArrayList;
import org.ws4d.java.structures.DataStructure;
import org.ws4d.java.structures.List;
import org.ws4d.java.types.URI;
import org.ws4d.java.util.StringUtil;

/**
 * 
 */
public class ProbeMatchesMessage extends Message {

	public static final URI	ACTION	= new URI(WSDConstants.WSD_ACTION_PROBEMATCHES);

	private List			probeMatches;

	public static SOAPHeader createProbeMatchesHeader(String communicationManagerId) {
		return DiscoveryMessage.createDiscoveryHeader(WSDConstants.WSD_ACTION_PROBEMATCHES, communicationManagerId);
	}

	/**
	 * Creates a new ProbeMatches message containing a {@link SOAPHeader} with
	 * the appropriate {@link SOAPHeader#getAction() action property} set, the
	 * default {@link SOAPHeader#getTo() to property} for ad-hoc mode (
	 * {@link WSDConstants#WSD_TO}) and a unique
	 * {@link SOAPHeader#getMessageId() message ID property}. All other header-
	 * and discovery-related fields are empty and it is the caller's
	 * responsibility to fill them with suitable values.
	 */
	public ProbeMatchesMessage(String communicationManagerId) {
		this(createProbeMatchesHeader(communicationManagerId));
	}

	/**
	 * @param header
	 */
	public ProbeMatchesMessage(SOAPHeader header) {
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
		sb.append(", probeMatches=").append(probeMatches);
		sb.append(" ]");
		return sb.toString();
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.message.DPWSMessage#getType()
	 */
	public int getType() {
		return PROBE_MATCHES_MESSAGE;
	}

	/*
	 * (non-Javadoc)
	 * @seeorg.ws4d.java.communication.message.discovery.ProbeMatchesMessage#
	 * getProbeMatch(int)
	 */
	public ProbeMatch getProbeMatch(int index) {
		return probeMatches == null ? null : (ProbeMatch) probeMatches.get(index);
	}

	/*
	 * (non-Javadoc)
	 * @seeorg.ws4d.java.communication.message.discovery.ProbeMatchesMessage#
	 * getProbeMatchCount()
	 */
	public int getProbeMatchCount() {
		return probeMatches == null ? 0 : probeMatches.size();
	}

	/*
	 * (non-Javadoc)
	 * @seeorg.ws4d.java.communication.message.discovery.ProbeMatchesMessage#
	 * getProbeMatches()
	 */
	public DataStructure getProbeMatches() {
		// XXX return a clone or an unmodifiable view?
		return probeMatches;
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.message.discovery.ProbeMatchesMessage#isEmpty ()
	 */
	public boolean isEmpty() {
		return probeMatches == null || probeMatches.isEmpty();
	}

	public void addProbeMatch(ProbeMatch probeMatch) {
		if (probeMatch == null) {
			return;
		}
		if (probeMatches == null) {
			probeMatches = new ArrayList();
		}
		probeMatches.add(probeMatch);
	}
}
