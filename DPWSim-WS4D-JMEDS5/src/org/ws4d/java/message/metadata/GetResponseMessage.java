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
import org.ws4d.java.constants.WXFConstants;
import org.ws4d.java.message.Message;
import org.ws4d.java.message.SOAPHeader;
import org.ws4d.java.structures.DataStructure;
import org.ws4d.java.types.CustomizeMData;
import org.ws4d.java.types.HostMData;
import org.ws4d.java.types.RelationshipMData;
import org.ws4d.java.types.ThisDeviceMData;
import org.ws4d.java.types.ThisModelMData;
import org.ws4d.java.types.URI;
import org.ws4d.java.util.StringUtil;

/*
 * <?xml version='1.0' encoding='UTF-8' ?> <soap:Envelope
 * xmlns:un0="http://schemas.microsoft.com/windows/pnpx/2005/10"
 * xmlns:pub="http://schemas.microsoft.com/windows/pub/2005/07"
 * xmlns:wsdp="http://schemas.xmlsoap.org/ws/2006/02/devprof"
 * xmlns:soap="http://www.w3.org/2003/05/soap-envelope"
 * xmlns:wsx="http://schemas.xmlsoap.org/ws/2004/09/mex"
 * xmlns:wsa="http://schemas.xmlsoap.org/ws/2004/08/addressing"> <soap:Header>
 * <wsa
 * :To>http://schemas.xmlsoap.org/ws/2004/08/addressing/role/anonymous</wsa:To>
 * <
 * wsa:Action>http://schemas.xmlsoap.org/ws/2004/09/transfer/GetResponse</wsa:Action
 * >
 * <wsa:MessageID>urn:uuid:95fdc5f6-b856-4397-9b0c-5bd77669fded</wsa:MessageID>
 * <wsa:RelatesTo>urn:uuid:37836700-c845-11dd-bfa6-0d3a112efe2a</wsa:RelatesTo>
 * </soap:Header> <soap:Body> <wsx:Metadata> <wsx:MetadataSection
 * Dialect="http://schemas.xmlsoap.org/ws/2006/02/devprof/ThisDevice">
 * <wsdp:ThisDevice> <wsdp:FriendlyName>Microsoft Publication Service Device
 * Host</wsdp:FriendlyName> <wsdp:FirmwareVersion>1.0</wsdp:FirmwareVersion>
 * <wsdp:SerialNumber>20050718</wsdp:SerialNumber> </wsdp:ThisDevice>
 * </wsx:MetadataSection> <wsx:MetadataSection
 * Dialect="http://schemas.xmlsoap.org/ws/2006/02/devprof/ThisModel">
 * <wsdp:ThisModel> <wsdp:Manufacturer>Microsoft Corporation</wsdp:Manufacturer>
 * <wsdp:ManufacturerUrl>http://www.microsoft.com</wsdp:ManufacturerUrl>
 * <wsdp:ModelName>Microsoft Publication Service</wsdp:ModelName>
 * <wsdp:ModelNumber>1</wsdp:ModelNumber>
 * <wsdp:ModelUrl>http://www.microsoft.com</wsdp:ModelUrl>
 * <wsdp:PresentationUrl>http://www.microsoft.com</wsdp:PresentationUrl>
 * <un0:DeviceCategory
 * >{2BC7C4DF-D940-46f2-BCF2-CB7DADEE2D93}</un0:DeviceCategory>
 * </wsdp:ThisModel> </wsx:MetadataSection> <wsx:MetadataSection
 * Dialect="http://schemas.xmlsoap.org/ws/2006/02/devprof/Relationship">
 * <wsdp:Relationship Type="http://schemas.xmlsoap.org/ws/2006/02/devprof/host">
 * <wsdp:Host> <wsa:EndpointReference>
 * <wsa:Address>urn:uuid:45672533-4576-4911-8cec-92ed48faa748</wsa:Address>
 * </wsa:EndpointReference> <wsdp:Types>pub:Computer</wsdp:Types>
 * <wsdp:ServiceId
 * >urn:uuid:45672533-4576-4911-8cec-92ed48faa748</wsdp:ServiceId>
 * <pub:Computer>BUI-NB232/Domain:BUI</pub:Computer> </wsdp:Host>
 * </wsdp:Relationship> </wsx:MetadataSection> </wsx:Metadata> </soap:Body>
 * </soap:Envelope>
 */

public class GetResponseMessage extends Message {

	public static final URI		ACTION	= new URI(WXFConstants.WXF_ACTION_GETRESPONSE);

	private ThisModelMData		thisModel;

	private ThisDeviceMData		thisDevice;

	private RelationshipMData	relationship;

	private String				customMData;

	private CustomizeMData		customMDataInstance;

	/**
	 * Creates a new GetMetadataResponse message containing a {@link SOAPHeader}
	 * with the appropriate {@link SOAPHeader#getAction() action property} set.
	 * All other header- and transfer-related fields are empty and it is the
	 * caller's responsibility to fill them with suitable values.
	 */
	public GetResponseMessage(String communicationManagerId) {
		this(SOAPHeader.createHeader(WXFConstants.WXF_ACTION_GETRESPONSE, communicationManagerId));
	}

	/**
	 * @param header
	 */
	public GetResponseMessage(SOAPHeader header) {
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
		sb.append(", thisModel=").append(thisModel);
		sb.append(", thisDevice=").append(thisDevice);
		sb.append(", relationship=").append(relationship);
		if (customMData != null) {
			sb.append(", customizeMData=").append(customMData);
		}
		sb.append(" ]");
		return sb.toString();
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.message.DPWSMessage#getType()
	 */
	public int getType() {
		return GET_RESPONSE_MESSAGE;
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.message.metadata.GetResponseMessage #getThisDevice()
	 */
	public ThisDeviceMData getThisDevice() {
		return thisDevice;
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.message.metadata.GetResponseMessage #getThisModel()
	 */
	public ThisModelMData getThisModel() {
		return thisModel;
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.message.metadata.GetResponseMessage #getRelationship()
	 */
	public RelationshipMData getRelationship() {
		return relationship;
	}

	/**
	 * @return String contains the customize metadata
	 */
	public String getCustomMdata() {
		return customMData;

	}

	public HostMData getHost() {
		return relationship == null ? null : relationship.getHost();
	}

	public DataStructure getHosted() {
		return relationship == null ? null : relationship.getHosted();
	}

	/**
	 * @param thisModel the thisModel to set
	 */
	public void setThisModel(ThisModelMData thisModel) {
		this.thisModel = thisModel;
	}

	/**
	 * @param thisDevice the thisDevice to set
	 */
	public void setThisDevice(ThisDeviceMData thisDevice) {
		this.thisDevice = thisDevice;
	}

	/**
	 * @param mdataCustom String which contains the new user added metadata
	 */
	public void setCustomMData(String mdataCustom) {
		this.customMData = mdataCustom;
	}

	/**
	 * @param relationship the relationship to set
	 */
	public void addRelationship(RelationshipMData relationship, ConstantsHelper helper) {
		if (this.relationship == null) {
			this.relationship = relationship;
		} else {
			this.relationship.mergeWith(relationship, helper);
		}
	}

	/**
	 * @param mdata instance of the type CustomizeMData
	 */
	public void addCustomizeMetaData(CustomizeMData mdata) {
		if (this.customMDataInstance == null) {
			this.customMDataInstance = mdata;
		}

	}

	/**
	 * @return the instance of the typ CustomizeMData
	 */
	public CustomizeMData getCustomMdataInstance() {
		return this.customMDataInstance;
	}

}
