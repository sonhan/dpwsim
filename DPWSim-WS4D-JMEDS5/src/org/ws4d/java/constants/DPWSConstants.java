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
 * Constants of DPWS.
 */
public interface DPWSConstants {

	/** The namespace name for DPWS. */
	public static final String	DPWS_NAMESPACE_NAME							= "http://docs.oasis-open.org/ws-dd/ns/dpws/2009/01";

	/** "dpws", the default prefix for DPWS. */
	public static final String	DPWS_NAMESPACE_PREFIX						= "dpws";

	/** Constant to dispatch new and old version */
	public static final int		DPWS_VERSION2009							= 0;

	/** Constant to display the Name of the DPWS Version */
	public static final String	DPWS_2009_NAME								= "DPWS1.1";

	/** 2048. No processing required when URIs are longer than this */
	public static final int		DPWS_MAX_URI_SIZE							= 2048;

	/** 32767. Max size for envelopes */
	public static final int		DPWS_MAX_ENVELOPE_SIZE						= 32767;

	/** 4096. The maximum size of a SOAP envelope sent or received over UDP */
	public static final int		DPWS_MAX_UDP_ENVELOPE_SIZE					= 4096;

	/**
	 * The multicast address for IPv4 datagram packets as defined in the DPWS
	 * standard.
	 */
	public static final String	DPWS_MCAST_IPv4								= "239.255.255.250";

	/**
	 * The multicast address for IPv6 datagram packets as defined in the DPWS
	 * standard.
	 */
	public static final String	DPWS_MCAST_IPv6								= "[FF02::C]";																							// ff02:0000:0000:0000:0000:0000:0000:000c

	public static final int		DPWS_MCAST_PORT								= 3702;

	/**
	 * 500. Sometimes, we have to wait a random time between 0 and this in ms
	 * before sending a message.
	 */
	public static final int		DPWS_APP_MAX_DELAY							= 2500;

	/** Maximum length for some fields. */
	public static final int		DPWS_MAX_FIELD_SIZE							= 256 - 1;																								// the

	// spec
	// says
	// "fewer"

	/**
	 * 1. The DPWS specific value of the MULTICAST_UDP_REPEAT defined in
	 * SOAP-over-UDP
	 */
	public static final int		MULTICAST_UDP_REPEAT						= 1;

	/**
	 * 1. The DPWS specific value of the UNICAST_UDP_REPEAT defined in
	 * SOAP-over-UDP
	 */
	public static final int		UNICAST_UDP_REPEAT							= 1;

	/**
	 * 50. The DPWS specific value of the UDP_MIN_DELAY defined in SOAP-over-UDP
	 */
	public static final int		UDP_MIN_DELAY								= 50;

	/**
	 * 250. The DPWS specific value of the UDP_MAX_DELAY defined in
	 * SOAP-over-UDP
	 */
	public static final int		UDP_MAX_DELAY								= 250;

	/**
	 * 450. The DPWS specific value of the UDP_UPPER_DELAY defined in
	 * SOAP-over-UDP
	 */
	public static final int		UDP_UPPER_DELAY								= 450;

	/** 10000. The timeout for resolve match answers in milliseconds */
	public static final int		MATCH_TIMEOUT								= 10000;

	/** "MetadataExchange". */
	public static final String	DPWS_TYPE_METADATAEXCHANGE					= "MetadataExchange";

	/** "Device". */
	public static final String	DPWS_TYPE_DEVICE							= "Device";

	/** "EndpointReference". */
	public static final String	DPWS_ELEM_ENDPOINTREFEFERENCE				= "EndpointReference";

	/** "ServiceId". */
	public static final String	DPWS_ELEM_SERVICEID							= "ServiceId";

	/** "Types". */
	public static final String	DPWS_ELEM_TYPES								= "Types";

	/** "ThisDevice". */
	public static final String	DPWS_ELEM_THISDEVICE						= "ThisDevice";

	/** "ThisModel". */
	public static final String	DPWS_ELEM_THISMODEL							= "ThisModel";

	/** "FriendlyName". */
	public static final String	DPWS_ELEM_FRIENDLYNAME						= "FriendlyName";

	/** "ModelName". */
	public static final String	DPWS_ELEM_MODELNAME							= "ModelName";

	/** "Manufacturer". */
	public static final String	DPWS_ELEM_MANUFACTURER						= "Manufacturer";

	/** "ManufacturerUrl". */
	public static final String	DPWS_ELEM_MANUFACTURERURL					= "ManufacturerUrl";

	/** "ModelNumber". */
	public static final String	DPWS_ELEM_MODELNUMBER						= "ModelNumber";

	/** "ModelUrl". */
	public static final String	DPWS_ELEM_MODELURL							= "ModelUrl";

	/** "PresentationUrl". */
	public static final String	DPWS_ELEM_PRESENTATIONURL					= "PresentationUrl";

	/** "FirmwareVersion". */
	public static final String	DPWS_ELEM_FIRMWAREVERSION					= "FirmwareVersion";

	/** "SerialNumber". */
	public static final String	DPWS_ELEM_SERIALNUMBER						= "SerialNumber";

	public static final String	DPWS_FILTER_EVENTING_ACTION					= DPWS_NAMESPACE_NAME + "/Action";

	public static final URI		DPWS_URI_FILTER_EVENTING_ACTION				= new URI(DPWS_FILTER_EVENTING_ACTION);

	/* faults */
	public static final String	DPWS_FAULT_FILTER_ACTION_NOT_SUPPORTED_NAME	= "FilterActionNotSupported";

	public static final QName	DPWS_FAULT_FILTER_ACTION_NOT_SUPPORTED		= new QName(DPWS_FAULT_FILTER_ACTION_NOT_SUPPORTED_NAME, DPWS_NAMESPACE_NAME);

	/** "Host". */
	public static final String	DPWS_RELATIONSHIP_ELEM_HOST					= "Host";

	/** "Hosted". */
	public static final String	DPWS_RELATIONSHIP_ELEM_HOSTED				= "Hosted";

	/** "Relationship". */
	public static final String	DPWS_ELEM_RELATIONSHIP						= "Relationship";

	/** "Type". */
	public static final String	DPWS_RELATIONSHIP_ATTR_TYPE					= "Type";

	/** METADATA. */
	public static final String	METADATA_DIALECT_THISMODEL					= DPWS_NAMESPACE_NAME + "/ThisModel";

	public static final String	METADATA_DIALECT_THISDEVICE					= DPWS_NAMESPACE_NAME + "/ThisDevice";

	public static final String	METADATA_DIALECT_RELATIONSHIP				= DPWS_NAMESPACE_NAME + "/Relationship";

	public static final String	METADATA_RELATIONSHIP_HOSTING_TYPE			= DPWS_NAMESPACE_NAME + "/host";

	/** The DPWS SOAP fault action. */
	public static final String	DPWS_ACTION_DPWS_FAULT						= DPWS_NAMESPACE_NAME + "/fault";

	// QualifiedNames of ThisModel

	/** QualifiedName of "Manufacturer". */
	public static final QName	DPWS_QN_MANUFACTURER						= new QName(DPWS_ELEM_MANUFACTURER, DPWS_NAMESPACE_NAME, DPWS_NAMESPACE_PREFIX);

	/** QualifiedName of "ManufacturerUrl". */
	public static final QName	DPWS_QN_MANUFACTURERURL						= new QName(DPWS_ELEM_MANUFACTURERURL, DPWS_NAMESPACE_NAME, DPWS_NAMESPACE_PREFIX);

	/** QualifiedName of "ModelName". */
	public static final QName	DPWS_QN_MODELNAME							= new QName(DPWS_ELEM_MODELNAME, DPWS_NAMESPACE_NAME, DPWS_NAMESPACE_PREFIX);

	/** QualifiedName of "ModelNumber". */
	public static final QName	DPWS_QN_MODELNUMBER							= new QName(DPWS_ELEM_MODELNUMBER, DPWS_NAMESPACE_NAME, DPWS_NAMESPACE_PREFIX);

	/** QualifiedName of "ModelUrl". */
	public static final QName	DPWS_QN_MODELURL							= new QName(DPWS_ELEM_MODELURL, DPWS_NAMESPACE_NAME, DPWS_NAMESPACE_PREFIX);

	/** QualifiedName of "PresentationUrl". */
	public static final QName	DPWS_QN_PRESENTATIONURL						= new QName(DPWS_ELEM_PRESENTATIONURL, DPWS_NAMESPACE_NAME, DPWS_NAMESPACE_PREFIX);

	// QualifiedNames of ThisDevice

	/** QualifiedName of "FriendlyName". */
	public static final QName	DPWS_QN_FRIENDLYNAME						= new QName(DPWS_ELEM_FRIENDLYNAME, DPWS_NAMESPACE_NAME, DPWS_NAMESPACE_PREFIX);

	/** QualifiedName of "FirmwareVersion". */
	public static final QName	DPWS_QN_FIRMWARE							= new QName(DPWS_ELEM_FIRMWAREVERSION, DPWS_NAMESPACE_NAME, DPWS_NAMESPACE_PREFIX);

	/** QualifiedName of "SerialNumber". */
	public static final QName	DPWS_QN_SERIALNUMBER						= new QName(DPWS_ELEM_SERIALNUMBER, DPWS_NAMESPACE_NAME, DPWS_NAMESPACE_PREFIX);

	// QualifiedNames of Host

	/** QualifiedName of "ServiceId". */
	public static final QName	DPWS_QN_SERVICEID							= new QName(DPWS_ELEM_SERVICEID, DPWS_NAMESPACE_NAME, DPWS_NAMESPACE_PREFIX);

	/** QualifiedName of "EndpointReference". */
	public static final QName	DPWS_QN_ENDPOINTREFERENCE					= new QName(DPWS_ELEM_ENDPOINTREFEFERENCE, DPWS_NAMESPACE_NAME, DPWS_NAMESPACE_PREFIX);

	/** QualifiedName of "Types". */
	public static final QName	DPWS_QN_TYPES								= new QName(DPWS_ELEM_TYPES, DPWS_NAMESPACE_NAME, DPWS_NAMESPACE_PREFIX);

	/** DPWS dpws:Device type like described in R1020 */
	public static final QName	DPWS_QN_DEVICETYPE							= new QName(DPWS_TYPE_DEVICE, DPWS_NAMESPACE_NAME, DPWS_NAMESPACE_PREFIX, QName.QNAME_WITH_PRIORITY);
}
