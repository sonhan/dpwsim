/*******************************************************************************
 * Copyright (c) 2009 MATERNA Information & Communications. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html. For further
 * project-related information visit http://www.ws4d.org. The most recent
 * version of the JMEDS framework can be obtained from
 * http://sourceforge.net/projects/ws4d-javame.
 ******************************************************************************/

package org.ws4d.java.constants;

import org.ws4d.java.types.QName;
import org.ws4d.java.types.URI;

/**
 * Constants used by WS Addressing.
 */
public interface WSAConstants {

	/** The namespace name for WS Addressing. */
	public static final String	WSA_NAMESPACE_NAME								= "http://www.w3.org/2005/08/addressing";

	/** "wsa" The default prefix for the WS Addressing namespace. */
	public static final String	WSA_NAMESPACE_PREFIX							= "wsa";

	public static final String	WSA_ATTR_RELATIONSHIP_TYPE						= "RelationshipType";

	public static final String	WSA_ATTR_IS_REFERENCE_PARAMETER					= "IsReferenceParameter";

	/** "Action". */
	public static final String	WSA_ELEM_ACTION									= "Action";

	/** "Address". */
	public static final String	WSA_ELEM_ADDRESS								= "Address";

	/** "To". */
	public static final String	WSA_ELEM_TO										= "To";

	/** "EndpointReference". */
	public static final String	WSA_ELEM_ENDPOINT_REFERENCE						= "EndpointReference";

	public static final String	WSA_ELEM_FAULT_ENDPOINT							= "FaultTo";

	/** "MessageID". */
	public static final String	WSA_ELEM_MESSAGE_ID								= "MessageID";

	/** "Metadata". */
	public static final String	WSA_ELEM_METADATA								= "Metadata";

	public static final String	WSA_ELEM_REFERENCE_PARAMETERS					= "ReferenceParameters";

	/** "RelatesTo". */
	public static final String	WSA_ELEM_RELATESTO								= "RelatesTo";

	public static final String	WSA_ELEM_REPLY_TO								= "ReplyTo";

	public static final String	WSA_ELEM_SOURCE_ENDPOINT						= "From";

	/** "http://www.w3.org/2005/08/addressing/anonymous". */
	public static final URI		WSA_ANONYMOUS									= new URI(WSA_NAMESPACE_NAME + "/anonymous");

	public static final String	WSA_TYPE_RELATIONSHIP_REPLY						= "Reply";

	public static final String	WSA_ACTION_ADDRESSING_FAULT						= WSA_NAMESPACE_NAME + "/fault";

	public static final String	WSA_ACTION_SOAP_FAULT							= WSA_NAMESPACE_NAME + "/soap/fault";

	/* faults */
	public static final QName	WSA_FAULT_DESTINATION_UNREACHABLE				= new QName("DestinationUnreachable", WSA_NAMESPACE_NAME, WSA_NAMESPACE_PREFIX);

	public static final QName	WSA_FAULT_INVALID_ADDRESSING_HEADER				= new QName("InvalidAddressingHeader", WSA_NAMESPACE_NAME, WSA_NAMESPACE_PREFIX);

	public static final QName	WSA_FAULT_MESSAGE_ADDRESSING_HEADER_REQUIRED	= new QName("MessageAddressingHeaderRequired", WSA_NAMESPACE_NAME, WSA_NAMESPACE_PREFIX);

	public static final QName	WSA_FAULT_ACTION_NOT_SUPPORTED					= new QName("ActionNotSupported", WSA_NAMESPACE_NAME, WSA_NAMESPACE_PREFIX);

	public static final QName	WSA_FAULT_ENDPOINT_UNAVAILABLE					= new QName("EndpointUnavailable", WSA_NAMESPACE_NAME, WSA_NAMESPACE_PREFIX);

	public static final QName	WSA_PROBLEM_HEADER_QNAME						= new QName("ProblemHeaderQName", WSA_NAMESPACE_NAME, WSA_NAMESPACE_PREFIX);

	public static final QName	WSA_PROBLEM_ACTION								= new QName("ProblemAction", WSA_NAMESPACE_NAME, WSA_NAMESPACE_PREFIX);

}
