package org.ws4d.java.constants;

import org.ws4d.java.types.QName;
import org.ws4d.java.types.URI;

public interface WSAConstants2006 {

	/** The namespace name for WS Addressing. */
	public static final String	WSA_NAMESPACE_NAME								= "http://schemas.xmlsoap.org/ws/2004/08/addressing";

	/** "wsa" The default prefix for the WS Addressing namespace. */
	public static final String	WSA_NAMESPACE_PREFIX							= "wsa";

	public static final String	WSA_ELEM_REFERENCE_PROPERTIES					= "ReferenceProperties";

	public static final String	WSA_ELEM_PORT_TYPE								= "PortType";

	public static final String	WSA_ELEM_SERVICE_NAME							= "ServiceName";

	public static final String	WSA_ELEM_PORT_NAME								= "PortName";

	public static final String	WSA_ELEM_POLICY									= "Policy";

	/** "http://schemas.xmlsoap.org/ws/2004/08/addressing/anonymous". */
	public static final URI		WSA_ANONYMOUS									= new URI(WSA_NAMESPACE_NAME + "/role/anonymous");

	public static final String	WSA_ACTION_ADDRESSING_FAULT						= WSA_NAMESPACE_NAME + "/fault";

	public static final String	WSA_ACTION_SOAP_FAULT							= WSA_NAMESPACE_NAME + "/soap/fault";

	/* faults */
	public static final QName	WSA_FAULT_DESTINATION_UNREACHABLE				= new QName("DestinationUnreachable", WSA_NAMESPACE_NAME, WSA_NAMESPACE_PREFIX);

	public static final QName	WSA_FAULT_INVALID_ADDRESSING_HEADER				= new QName("InvalidAddressingHeader", WSA_NAMESPACE_NAME, WSA_NAMESPACE_PREFIX);

	public static final QName	WSA_FAULT_MESSAGE_ADDRESSING_HEADER_REQUIRED	= new QName("MessageInformationHeaderRequired", WSA_NAMESPACE_NAME, WSA_NAMESPACE_PREFIX);

	public static final QName	WSA_FAULT_ACTION_NOT_SUPPORTED					= new QName("ActionNotSupported", WSA_NAMESPACE_NAME, WSA_NAMESPACE_PREFIX);

	public static final QName	WSA_FAULT_ENDPOINT_UNAVAILABLE					= new QName("EndpointUnavailable", WSA_NAMESPACE_NAME, WSA_NAMESPACE_PREFIX);

	public static final QName	WSA_PROBLEM_HEADER_QNAME						= new QName("ProblemHeaderQName", WSA_NAMESPACE_NAME, WSA_NAMESPACE_PREFIX);

	public static final QName	WSA_PROBLEM_ACTION								= new QName("ProblemAction", WSA_NAMESPACE_NAME, WSA_NAMESPACE_PREFIX);

}
