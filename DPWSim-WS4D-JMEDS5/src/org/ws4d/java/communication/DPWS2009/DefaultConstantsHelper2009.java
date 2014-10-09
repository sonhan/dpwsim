package org.ws4d.java.communication.DPWS2009;

import org.ws4d.java.constants.ConstantsHelper;
import org.ws4d.java.constants.DPWSConstants;
import org.ws4d.java.constants.DPWSConstants2006;
import org.ws4d.java.constants.WSAConstants;
import org.ws4d.java.constants.WSDConstants;
import org.ws4d.java.types.QName;
import org.ws4d.java.types.URI;

public class DefaultConstantsHelper2009 implements ConstantsHelper {

	private static DefaultConstantsHelper2009	helper	= new DefaultConstantsHelper2009();

	public static synchronized DefaultConstantsHelper2009 getInstance() {
		if (helper != null)
			return helper;
		else
			return new DefaultConstantsHelper2009();
	}

	public int getDPWSVersion() {
		return DPWSConstants.DPWS_VERSION2009;
	}

	public int getRandomApplicationDelay() {
		return DPWSConstants.DPWS_APP_MAX_DELAY;
	}

	/**
	 * DPWS Constants
	 */
	public String getDPWSNamespace() {
		return DPWSConstants.DPWS_NAMESPACE_NAME;
	}

	public String getDPWSNamespacePrefix() {
		return DPWSConstants.DPWS_NAMESPACE_PREFIX;
	}

	public String getDPWSFilterEventingAction() {
		return DPWSConstants.DPWS_FILTER_EVENTING_ACTION;
	}

	public URI getDPWSUriFilterEeventingAction() {
		return DPWSConstants.DPWS_URI_FILTER_EVENTING_ACTION;
	}

	public QName getDPWSFaultFilterActionNotSupported() {
		return DPWSConstants.DPWS_FAULT_FILTER_ACTION_NOT_SUPPORTED;
	}

	/** METADATA. */
	public String getMetadataDialectThisModel() {
		return DPWSConstants.METADATA_DIALECT_THISMODEL;
	}

	public String getMetadataDialectThisDevice() {
		return DPWSConstants.METADATA_DIALECT_THISDEVICE;
	}

	public String getMetatdataDialectRelationship() {
		return DPWSConstants.METADATA_DIALECT_RELATIONSHIP;
	}

	public String getMetadataRelationshipHostingType() {
		return DPWSConstants.METADATA_RELATIONSHIP_HOSTING_TYPE;
	}

	/** The DPWS SOAP fault action. */
	public String getDPWSActionFault() {
		return DPWSConstants.DPWS_ACTION_DPWS_FAULT;
	}

	public String getDPWSAttributeRelationshipType() {
		return DPWSConstants.DPWS_RELATIONSHIP_ATTR_TYPE;
	}

	public String getDPWSElementRelationshipHost() {
		return DPWSConstants.DPWS_RELATIONSHIP_ELEM_HOST;
	}

	public String getDPWSElementRelationshipHosted() {
		return DPWSConstants.DPWS_RELATIONSHIP_ELEM_HOSTED;
	}

	public String getDPWSElementTypes() {
		return DPWSConstants.DPWS_ELEM_TYPES;
	}

	public String getDPWSElementRelationship() {
		return DPWSConstants.DPWS_ELEM_RELATIONSHIP;
	}

	public String getDPWSElementServiceId() {
		return DPWSConstants.DPWS_ELEM_SERVICEID;
	}

	public String getDPWSElementFriendlyName() {
		return DPWSConstants.DPWS_ELEM_FRIENDLYNAME;
	}

	public String getDPWSElementFirmwareVersion() {
		return DPWSConstants.DPWS_ELEM_FIRMWAREVERSION;
	}

	public String getDPWSElementSerialnumber() {
		return DPWSConstants.DPWS_ELEM_SERIALNUMBER;
	}

	public String getDPWSElementThisDevice() {
		return DPWSConstants.DPWS_ELEM_THISDEVICE;
	}

	public String getDPWSElementThisModel() {
		return DPWSConstants.DPWS_ELEM_THISMODEL;
	}

	public String getDPWSElementManufacturer() {
		return DPWSConstants.DPWS_ELEM_MANUFACTURER;
	}

	public String getDPWSElementManufacturerURL() {
		return DPWSConstants.DPWS_ELEM_MANUFACTURERURL;
	}

	public String getDPWSElementModelName() {
		return DPWSConstants.DPWS_ELEM_MODELNAME;
	}

	public String getDPWSElementModelNumber() {
		return DPWSConstants.DPWS_ELEM_MODELNUMBER;
	}

	public String getDPWSElementModelURL() {
		return DPWSConstants.DPWS_ELEM_MODELURL;
	}

	public String getDPWSElementPresentationURL() {
		return DPWSConstants.DPWS_ELEM_PRESENTATIONURL;
	}

	/** QualifiedName of "Manufacturer". */
	public QName getDPWSQnManufacturer() {
		return DPWSConstants.DPWS_QN_MANUFACTURER;
	}

	/** QualifiedName of "ManufacturerUrl". */
	public QName getDPWSQnManufactuerURL() {
		return DPWSConstants.DPWS_QN_MANUFACTURERURL;
	}

	/** QualifiedName of "ModelName". */
	public QName getDPWSQnModelname() {
		return DPWSConstants.DPWS_QN_MODELNAME;
	}

	/** QualifiedName of "ModelNumber". */
	public QName getDPWSQnModelnumber() {
		return DPWSConstants.DPWS_QN_MODELNUMBER;
	}

	/** QualifiedName of "ModelUrl". */
	public QName getDPWSQnModelURL() {
		return DPWSConstants.DPWS_QN_MODELURL;
	}

	/** QualifiedName of "PresentationUrl". */
	public QName getDPWSQnPresentationURL() {
		return DPWSConstants.DPWS_QN_PRESENTATIONURL;
	}

	// QualifiedNames of ThisDevice

	/** QualifiedName of "FriendlyName". */
	public QName getDPWSQnFriendlyName() {
		return DPWSConstants.DPWS_QN_FRIENDLYNAME;
	}

	/** QualifiedName of "FirmwareVersion". */
	public QName getDPWSQnFirmware() {
		return DPWSConstants.DPWS_QN_FIRMWARE;
	}

	/** QualifiedName of "SerialNumber". */
	public QName getDPWSQnSerialnumber() {
		return DPWSConstants.DPWS_QN_SERIALNUMBER;
	}

	// QualifiedNames of Host

	/** QualifiedName of "ServiceId". */
	public QName getDPWSQnServiceID() {
		return DPWSConstants.DPWS_QN_SERVICEID;
	}

	/** QualifiedName of "EndpointReference". */
	public QName getDPWSQnEndpointReference() {
		return DPWSConstants.DPWS_QN_ENDPOINTREFERENCE;
	}

	/** QualifiedName of "Types". */
	public QName getDPWSQnTypes() {
		return DPWSConstants.DPWS_QN_TYPES;
	}

	/** DPWS dpws:Device type like described in R1020 */
	public QName getDPWSQnDeviceType() {
		return DPWSConstants.DPWS_QN_DEVICETYPE;
	}

	/**
	 * WSA Constants
	 */
	public String getWSANamespace() {
		return WSAConstants.WSA_NAMESPACE_NAME;
	}

	public String getWSAElemReferenceProperties() {
		return null;
	}

	public String getWSAElemPortType() {
		return null;
	}

	public String getWSAElemServiceName() {
		return null;
	}

	public String getWSAElemPolicy() {
		return null;
	}

	public URI getWSAAnonymus() {
		return WSAConstants.WSA_ANONYMOUS;
	}

	public String getWSAActionAddressingFault() {
		return WSAConstants.WSA_ACTION_ADDRESSING_FAULT;
	}

	public String getWSAActionSoapFault() {
		return WSAConstants.WSA_ACTION_SOAP_FAULT;
	}

	/* faults */
	public QName getWSAFaultDestinationUnreachable() {
		return WSAConstants.WSA_FAULT_DESTINATION_UNREACHABLE;
	}

	public QName getWSAFaultInvalidAddressingHeader() {
		return WSAConstants.WSA_FAULT_INVALID_ADDRESSING_HEADER;
	}

	public QName getWSAFaultMessageAddressingHeaderRequired() {
		return WSAConstants.WSA_FAULT_MESSAGE_ADDRESSING_HEADER_REQUIRED;
	}

	public QName getWSAFaultActionNotSupported() {
		return WSAConstants.WSA_FAULT_ACTION_NOT_SUPPORTED;
	}

	public QName getWSAfaultEndpointUnavailable() {
		return WSAConstants.WSA_FAULT_ENDPOINT_UNAVAILABLE;
	}

	public QName getWSAProblemHeaderQname() {
		return WSAConstants.WSA_PROBLEM_HEADER_QNAME;
	}

	public QName getWSAProblemAction() {
		return WSAConstants.WSA_PROBLEM_ACTION;
	}

	/**
	 * WSD Constants
	 */
	public String getWSDNamespace() {

		return WSDConstants.WSD_NAMESPACE_NAME;
	}

	public String getWSDTo() {
		return WSDConstants.WSD_TO;
	}

	public String getWSDActionHello() {
		return WSDConstants.WSD_ACTION_HELLO;
	}

	public String getWSDActionBye() {
		return WSDConstants.WSD_ACTION_BYE;
	}

	public String getWSDActionProbe() {
		return WSDConstants.WSD_ACTION_PROBE;
	}

	public String getWSDActionProbeMatches() {
		return WSDConstants.WSD_ACTION_PROBEMATCHES;
	}

	public String getWSDActionResolve() {
		return WSDConstants.WSD_ACTION_RESOLVE;
	}

	public String getWSDActionResolveMatches() {
		return WSDConstants.WSD_ACTION_RESOLVEMATCHES;
	}

	public String getWSDActionFault() {
		return WSDConstants.WSD_ACTION_WSD_FAULT;
	}

	public String getMetadataDialectCustomizeMetadata() {
		return DPWSConstants2006.METADATA_DIALECT_CUSTOM;
	}
}
