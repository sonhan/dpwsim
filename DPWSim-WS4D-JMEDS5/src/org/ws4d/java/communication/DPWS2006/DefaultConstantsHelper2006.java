/*******************************************************************************
 * Copyright (c) 2009 MATERNA Information & Communications. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html. For further
 * project-related information visit http://www.ws4d.org. The most recent
 * version of the JMEDS framework can be obtained from
 * http://sourceforge.net/projects/ws4d-javame.
 ******************************************************************************/
package org.ws4d.java.communication.DPWS2006;

import org.ws4d.java.constants.ConstantsHelper;
import org.ws4d.java.constants.DPWSConstants2006;
import org.ws4d.java.constants.WSAConstants2006;
import org.ws4d.java.constants.WSDConstants2006;
import org.ws4d.java.types.QName;
import org.ws4d.java.types.URI;

public class DefaultConstantsHelper2006 implements ConstantsHelper {

	private static DefaultConstantsHelper2006	helper	= new DefaultConstantsHelper2006();

	public static synchronized DefaultConstantsHelper2006 getInstance() {
		if (helper != null)
			return helper;
		else
			return new DefaultConstantsHelper2006();
	}

	public int getDPWSVersion() {
		return DPWSConstants2006.DPWS_VERSION2006;
	}

	public int getRandomApplicationDelay() {
		return DPWSConstants2006.DPWS_APP_MAX_DELAY;
	}

	public String getDPWSNamespace() {
		return DPWSConstants2006.DPWS_NAMESPACE_NAME;
	}

	public String getDPWSNamespacePrefix() {
		return DPWSConstants2006.DPWS_NAMESPACE_PREFIX;
	}

	public String getDPWSFilterEventingAction() {
		return DPWSConstants2006.DPWS_FILTER_EVENTING_ACTION;
	}

	public URI getDPWSUriFilterEeventingAction() {
		return DPWSConstants2006.DPWS_URI_FILTER_EVENTING_ACTION;
	}

	public QName getDPWSFaultFilterActionNotSupported() {
		return DPWSConstants2006.DPWS_FAULT_FILTER_ACTION_NOT_SUPPORTED;
	}

	/** METADATA. */
	public String getMetadataDialectThisModel() {
		return DPWSConstants2006.METADATA_DIALECT_THISMODEL;
	}

	public String getMetadataDialectThisDevice() {
		return DPWSConstants2006.METADATA_DIALECT_THISDEVICE;
	}

	public String getMetatdataDialectRelationship() {
		return DPWSConstants2006.METADATA_DIALECT_RELATIONSHIP;
	}

	public String getMetadataRelationshipHostingType() {
		return DPWSConstants2006.METADATA_RELATIONSHIP_HOSTING_TYPE;
	}

	/** The DPWS SOAP fault action. */
	public String getDPWSActionFault() {
		return DPWSConstants2006.DPWS_ACTION_DPWS_FAULT;
	}

	public String getDPWSAttributeRelationshipType() {
		return DPWSConstants2006.DPWS_RELATIONSHIP_ATTR_TYPE;
	}

	public String getDPWSElementRelationshipHost() {
		return DPWSConstants2006.DPWS_RELATIONSHIP_ELEM_HOST;
	}

	public String getDPWSElementRelationshipHosted() {
		return DPWSConstants2006.DPWS_RELATIONSHIP_ELEM_HOSTED;
	}

	public String getDPWSElementTypes() {
		return DPWSConstants2006.DPWS_ELEM_TYPES;
	}

	public String getDPWSElementRelationship() {
		return DPWSConstants2006.DPWS_ELEM_RELATIONSHIP;
	}

	public String getDPWSElementServiceId() {
		return DPWSConstants2006.DPWS_ELEM_SERVICEID;
	}

	public String getDPWSElementFriendlyName() {
		return DPWSConstants2006.DPWS_ELEM_FRIENDLYNAME;
	}

	public String getDPWSElementFirmwareVersion() {
		return DPWSConstants2006.DPWS_ELEM_FIRMWAREVERSION;
	}

	public String getDPWSElementSerialnumber() {
		return DPWSConstants2006.DPWS_ELEM_SERIALNUMBER;
	}

	public String getDPWSElementThisDevice() {
		return DPWSConstants2006.DPWS_ELEM_THISDEVICE;
	}

	public String getDPWSElementThisModel() {
		return DPWSConstants2006.DPWS_ELEM_THISMODEL;
	}

	public String getDPWSElementManufacturer() {
		return DPWSConstants2006.DPWS_ELEM_MANUFACTURER;
	}

	public String getDPWSElementModelNumber() {
		return DPWSConstants2006.DPWS_ELEM_MODELNUMBER;
	}

	public String getDPWSElementManufacturerURL() {
		return DPWSConstants2006.DPWS_ELEM_MANUFACTURERURL;
	}

	public String getDPWSElementModelName() {
		return DPWSConstants2006.DPWS_ELEM_MODELNAME;
	}

	public String getDPWSElementModelURL() {
		return DPWSConstants2006.DPWS_ELEM_MODELURL;
	}

	public String getDPWSElementPresentationURL() {
		return DPWSConstants2006.DPWS_ELEM_PRESENTATIONURL;
	}

	/** QualifiedName of "Manufacturer". */
	public QName getDPWSQnManufacturer() {
		return DPWSConstants2006.DPWS_QN_MANUFACTURER;
	}

	/** QualifiedName of "ManufacturerUrl". */
	public QName getDPWSQnManufactuerURL() {
		return DPWSConstants2006.DPWS_QN_MANUFACTURERURL;
	}

	/** QualifiedName of "ModelName". */
	public QName getDPWSQnModelname() {
		return DPWSConstants2006.DPWS_QN_MODELNAME;
	}

	/** QualifiedName of "ModelNumber". */
	public QName getDPWSQnModelnumber() {
		return DPWSConstants2006.DPWS_QN_MODELNUMBER;
	}

	/** QualifiedName of "ModelUrl". */
	public QName getDPWSQnModelURL() {
		return DPWSConstants2006.DPWS_QN_MODELURL;
	}

	/** QualifiedName of "PresentationUrl". */
	public QName getDPWSQnPresentationURL() {
		return DPWSConstants2006.DPWS_QN_PRESENTATIONURL;
	}

	// QualifiedNames of ThisDevice

	/** QualifiedName of "FriendlyName". */
	public QName getDPWSQnFriendlyName() {
		return DPWSConstants2006.DPWS_QN_FRIENDLYNAME;
	}

	/** QualifiedName of "FirmwareVersion". */
	public QName getDPWSQnFirmware() {
		return DPWSConstants2006.DPWS_QN_FIRMWARE;
	}

	/** QualifiedName of "SerialNumber". */
	public QName getDPWSQnSerialnumber() {
		return DPWSConstants2006.DPWS_QN_SERIALNUMBER;
	}

	// QualifiedNames of Host

	/** QualifiedName of "ServiceId". */
	public QName getDPWSQnServiceID() {
		return DPWSConstants2006.DPWS_QN_SERVICEID;
	}

	/** QualifiedName of "EndpointReference". */
	public QName getDPWSQnEndpointReference() {
		return DPWSConstants2006.DPWS_QN_ENDPOINTREFERENCE;
	}

	/** QualifiedName of "Types". */
	public QName getDPWSQnTypes() {
		return DPWSConstants2006.DPWS_QN_TYPES;
	}

	/** DPWS dpws:Device type like described in R1020 */
	public QName getDPWSQnDeviceType() {
		return DPWSConstants2006.DPWS_QN_DEVICETYPE;
	}

	/**
	 * WSA Constants
	 */
	public String getWSANamespace() {
		return WSAConstants2006.WSA_NAMESPACE_NAME;
	}

	public String getWSAElemReferenceProperties() {
		return WSAConstants2006.WSA_ELEM_REFERENCE_PROPERTIES;
	}

	public String getWSAElemPortType() {
		return WSAConstants2006.WSA_ELEM_PORT_TYPE;
	}

	public String getWSAElemServiceName() {
		return WSAConstants2006.WSA_ELEM_SERVICE_NAME;
	}

	public String getWSAElemPolicy() {
		return WSAConstants2006.WSA_ELEM_POLICY;
	}

	public URI getWSAAnonymus() {
		return WSAConstants2006.WSA_ANONYMOUS;
	}

	public String getWSAActionAddressingFault() {
		return WSAConstants2006.WSA_ACTION_ADDRESSING_FAULT;
	}

	public String getWSAActionSoapFault() {
		return WSAConstants2006.WSA_ACTION_SOAP_FAULT;
	}

	/* faults */
	public QName getWSAFaultDestinationUnreachable() {
		return WSAConstants2006.WSA_FAULT_DESTINATION_UNREACHABLE;
	}

	public QName getWSAFaultInvalidAddressingHeader() {
		return WSAConstants2006.WSA_FAULT_INVALID_ADDRESSING_HEADER;
	}

	public QName getWSAFaultMessageAddressingHeaderRequired() {
		return WSAConstants2006.WSA_FAULT_MESSAGE_ADDRESSING_HEADER_REQUIRED;
	}

	public QName getWSAFaultActionNotSupported() {
		return WSAConstants2006.WSA_FAULT_ACTION_NOT_SUPPORTED;
	}

	public QName getWSAfaultEndpointUnavailable() {
		return WSAConstants2006.WSA_FAULT_ENDPOINT_UNAVAILABLE;
	}

	public QName getWSAProblemHeaderQname() {
		return WSAConstants2006.WSA_PROBLEM_HEADER_QNAME;
	}

	public QName getWSAProblemAction() {
		return WSAConstants2006.WSA_PROBLEM_ACTION;
	}

	/**
	 * WSD Constants
	 */
	public String getWSDNamespace() {
		return WSDConstants2006.WSD_NAMESPACE_NAME;
	}

	public String getWSDTo() {
		return WSDConstants2006.WSD_TO;
	}

	public String getWSDActionHello() {
		return WSDConstants2006.WSD_ACTION_HELLO;
	}

	public String getWSDActionBye() {
		return WSDConstants2006.WSD_ACTION_BYE;
	}

	public String getWSDActionProbe() {
		return WSDConstants2006.WSD_ACTION_PROBE;
	}

	public String getWSDActionProbeMatches() {
		return WSDConstants2006.WSD_ACTION_PROBEMATCHES;
	}

	public String getWSDActionResolve() {
		return WSDConstants2006.WSD_ACTION_RESOLVE;
	}

	public String getWSDActionResolveMatches() {
		return WSDConstants2006.WSD_ACTION_RESOLVEMATCHES;
	}

	public String getWSDActionFault() {
		return WSDConstants2006.WSD_ACTION_WSD_FAULT;
	}

	
	public String getMetadataDialectCustomizeMetadata() {
		return DPWSConstants2006.METADATA_DIALECT_CUSTOM;
	}
}
