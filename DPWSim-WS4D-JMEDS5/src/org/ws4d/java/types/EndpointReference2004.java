package org.ws4d.java.types;

public class EndpointReference2004 extends EndpointReference {

	/** optional "wsa:EndpointReference/wsa:ReferenceProperties" element */
	private ReferenceParametersMData	referenceProperties	= null;

	/** optional "wsa:EndpointReference/wsa:PortType" element */
	private QName						portType			= null;

	/** optional "wsa:EndpointReference/wsa:ServiceName" element */
	private QName						serviceName			= null;

	/** optional "wsa:EndpointReference/wsa:ServiceName/@PortName" element */
	private String						portName			= null;

	// -------------------------------------------------------------------------

	/**
	 * Constructor.
	 * 
	 * @param address "wsa:EndpointReference/wsa:Address" element
	 */
	public EndpointReference2004(URI address) {
		this(address, null, null);
	}

	/**
	 * Constructor.
	 * 
	 * @param address "wsa:EndpointReference/wsa:Address" element
	 */
	public EndpointReference2004(AttributedURI address) {
		this(address, null, null);
	}

	/**
	 * Constructor.
	 * 
	 * @param address "wsa:EndpointReference/wsa:Address" element
	 * @param referenceParameters
	 *            "wsa:EndpointReference/wsa:ReferenceParameters"
	 */
	public EndpointReference2004(URI address, ReferenceParametersMData referenceParameters) {
		this(address, referenceParameters, null);
	}

	/**
	 * Constructor.
	 * 
	 * @param address "wsa:EndpointReference/wsa:Address" element
	 * @param referenceParameters
	 *            "wsa:EndpointReference/wsa:ReferenceParameters"
	 */
	public EndpointReference2004(AttributedURI address, ReferenceParametersMData referenceParameters) {
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
	public EndpointReference2004(URI address, ReferenceParametersMData referenceParameters, MetadataMData endpointMetadata) {
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
	public EndpointReference2004(AttributedURI address, ReferenceParametersMData referenceParameters, MetadataMData endpointMetadata) {
		super(address, referenceParameters, endpointMetadata);
	}

	/**
	 * Constructor
	 * 
	 * @param address "wsa:EndpointReference/wsa:Address" element
	 * @param portType "wsa:EndpointReference/wsa:PortType" element
	 * @param serviceName "wsa:EndpointReference/wsa:ServiceName" element
	 * @param portName "wsa:EndpointReference/wsa:ServiceName/@PortName" element
	 */
	public EndpointReference2004(URI address, QName portType, QName serviceName, String portName) {
		super(address);
		this.portType = portType;
		this.serviceName = serviceName;
		this.portName = portName;
	}

	/**
	 * Constructor
	 * 
	 * @param address "wsa:EndpointReference/wsa:Address" element
	 * @param portType "wsa:EndpointReference/wsa:PortType" element
	 * @param serviceName "wsa:EndpointReference/wsa:ServiceName" element
	 * @param portName "wsa:EndpointReference/wsa:ServiceName/@PortName" element
	 */
	public EndpointReference2004(AttributedURI address, QName portType, QName serviceName, String portName) {
		super(address);
		this.portType = portType;
		this.serviceName = serviceName;
		this.portName = portName;
	}

	/**
	 * Constructor
	 * 
	 * @param address "wsa:EndpointReference/wsa:Address" element
	 * @param portType "wsa:EndpointReference/wsa:PortType" element
	 * @param serviceName "wsa:EndpointReference/wsa:ServiceName" element
	 * @param portName "wsa:EndpointReference/wsa:ServiceName/@PortName" element
	 */
	public EndpointReference2004(URI address, ReferenceParametersMData referenceParameters, QName portType, QName serviceName, String portName) {
		super(address, referenceParameters);
		this.portType = portType;
		this.serviceName = serviceName;
		this.portName = portName;
	}

	/**
	 * Constructor
	 * 
	 * @param address "wsa:EndpointReference/wsa:Address" element
	 * @param portType "wsa:EndpointReference/wsa:PortType" element
	 * @param serviceName "wsa:EndpointReference/wsa:ServiceName" element
	 * @param portName "wsa:EndpointReference/wsa:ServiceName/@PortName" element
	 */
	public EndpointReference2004(AttributedURI address, ReferenceParametersMData referenceParameters, QName portType, QName serviceName, String portName) {
		super(address, referenceParameters);
		this.portType = portType;
		this.serviceName = serviceName;
		this.portName = portName;
	}

	/**
	 * Constructor
	 * 
	 * @param address "wsa:EndpointReference/wsa:Address" element
	 * @param referenceProperties
	 *            "wsa:EndpointReference/wsa:ReferenceProperties" element
	 * @param portType "wsa:EndpointReference/wsa:PortType" element
	 * @param serviceName "wsa:EndpointReference/wsa:ServiceName" element
	 * @param portName "wsa:EndpointReference/wsa:ServiceName/@PortName" element
	 */
	public EndpointReference2004(AttributedURI address, ReferenceParametersMData referenceParameters, ReferenceParametersMData referenceProperties, QName portType, QName serviceName, String portName) {
		super(address, referenceParameters);
		this.referenceProperties = referenceProperties;
		this.portType = portType;
		this.serviceName = serviceName;
		this.portName = portName;
	}

	// -----------------------------------------------------------------------

	public QName getPortType() {
		return portType;
	}

	public QName getServiceName() {
		return serviceName;
	}

	public String getPortName() {
		return portName;
	}

	public ReferenceParametersMData getReferenceProperties() {
		return referenceProperties;
	}

	// ------------------------------------------------------------------------

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("EndpointReference [ address=").append(getAddress());
		sb.append(", referenceProperties=").append(getReferenceProperties());
		sb.append(", referenceParameters=").append(getReferenceParameters());
		sb.append(", portType=").append(getPortType());
		sb.append(", serviceName=").append(getServiceName() + "{@portName= " + getPortName() + " }");
		sb.append(" ]");
		return sb.toString();
	}

}