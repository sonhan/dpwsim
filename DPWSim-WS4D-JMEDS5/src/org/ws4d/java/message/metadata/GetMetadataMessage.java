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

import org.ws4d.java.constants.MEXConstants;
import org.ws4d.java.message.Message;
import org.ws4d.java.message.SOAPHeader;
import org.ws4d.java.types.URI;

/**
 * 
 */
public class GetMetadataMessage extends Message {

	public static final URI	ACTION	= new URI(MEXConstants.WSX_ACTION_GETMETADATA_REQUEST);

	private URI				dialect;

	private URI				identifier;

	/**
	 * Creates a new Get message containing a {@link SOAPHeader} with the
	 * appropriate {@link SOAPHeader#getAction() action property} set and a
	 * unique {@link SOAPHeader#getMessageId() message ID property}. All other
	 * header- and metadataexchange-related fields are empty and it is the
	 * caller's responsibility to fill them with suitable values.
	 */
	public GetMetadataMessage(String communicationManagerId) {
		this(SOAPHeader.createRequestHeader(MEXConstants.WSX_ACTION_GETMETADATA_REQUEST, communicationManagerId));
	}

	/**
	 * @param header
	 */
	public GetMetadataMessage(SOAPHeader header) {
		super(header);
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.message.Message#getType()
	 */
	public int getType() {
		return GET_METADATA_MESSAGE;
	}

	/**
	 * @return the dialect
	 */
	public URI getDialect() {
		return dialect;
	}

	/**
	 * @param dialect the dialect to set
	 */
	public void setDialect(URI dialect) {
		this.dialect = dialect;
	}

	/**
	 * @return the identifier
	 */
	public URI getIdentifier() {
		return identifier;
	}

	/**
	 * @param identifier the identifier to set
	 */
	public void setIdentifier(URI identifier) {
		this.identifier = identifier;
	}
}
