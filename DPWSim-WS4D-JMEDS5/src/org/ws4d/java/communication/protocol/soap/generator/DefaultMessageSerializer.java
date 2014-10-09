/*******************************************************************************
 * Copyright (c) 2009 MATERNA Information & Communications. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html. For further
 * project-related information visit http://www.ws4d.org. The most recent
 * version of the JMEDS framework can be obtained from
 * http://sourceforge.net/projects/ws4d-javame.
 ******************************************************************************/
package org.ws4d.java.communication.protocol.soap.generator;

import java.io.IOException;

import org.ws4d.java.DPWSFramework;
import org.ws4d.java.communication.CommunicationBinding;
import org.ws4d.java.communication.CommunicationManager;
import org.ws4d.java.communication.CommunicationUtil;
import org.ws4d.java.communication.ProtocolData;
import org.ws4d.java.configuration.FrameworkProperties;
import org.ws4d.java.constants.ConstantsHelper;
import org.ws4d.java.constants.MEXConstants;
import org.ws4d.java.constants.SOAPConstants;
import org.ws4d.java.constants.WSAConstants;
import org.ws4d.java.constants.WSDConstants;
import org.ws4d.java.constants.WSEConstants;
import org.ws4d.java.constants.XMLConstants;
import org.ws4d.java.eventing.EventSink;
import org.ws4d.java.io.xml.ElementHandler;
import org.ws4d.java.io.xml.ElementHandlerRegistry;
import org.ws4d.java.io.xml.XmlSerializer;
import org.ws4d.java.message.FaultMessage;
import org.ws4d.java.message.InvokeMessage;
import org.ws4d.java.message.Message;
import org.ws4d.java.message.SOAPHeader;
import org.ws4d.java.message.discovery.ByeMessage;
import org.ws4d.java.message.discovery.HelloMessage;
import org.ws4d.java.message.discovery.ProbeMatch;
import org.ws4d.java.message.discovery.ProbeMatchesMessage;
import org.ws4d.java.message.discovery.ProbeMessage;
import org.ws4d.java.message.discovery.ResolveMatch;
import org.ws4d.java.message.discovery.ResolveMatchesMessage;
import org.ws4d.java.message.discovery.ResolveMessage;
import org.ws4d.java.message.eventing.GetStatusMessage;
import org.ws4d.java.message.eventing.GetStatusResponseMessage;
import org.ws4d.java.message.eventing.RenewMessage;
import org.ws4d.java.message.eventing.RenewResponseMessage;
import org.ws4d.java.message.eventing.SubscribeMessage;
import org.ws4d.java.message.eventing.SubscribeResponseMessage;
import org.ws4d.java.message.eventing.SubscriptionEndMessage;
import org.ws4d.java.message.eventing.UnsubscribeMessage;
import org.ws4d.java.message.eventing.UnsubscribeResponseMessage;
import org.ws4d.java.message.metadata.GetMessage;
import org.ws4d.java.message.metadata.GetMetadataMessage;
import org.ws4d.java.message.metadata.GetMetadataResponseMessage;
import org.ws4d.java.message.metadata.GetResponseMessage;
import org.ws4d.java.service.parameter.ParameterValue;
import org.ws4d.java.structures.ArrayList;
import org.ws4d.java.structures.DataStructure;
import org.ws4d.java.structures.HashMap;
import org.ws4d.java.structures.HashMap.Entry;
import org.ws4d.java.structures.Iterator;
import org.ws4d.java.structures.List;
import org.ws4d.java.types.AppSequence;
import org.ws4d.java.types.AttributedURI;
import org.ws4d.java.types.CustomizeMData;
import org.ws4d.java.types.CustomizeMDataHandler;
import org.ws4d.java.types.Delivery;
import org.ws4d.java.types.DiscoveryData;
import org.ws4d.java.types.EndpointReference;
import org.ws4d.java.types.EndpointReferenceSet;
import org.ws4d.java.types.EprInfo;
import org.ws4d.java.types.Filter;
import org.ws4d.java.types.HostMData;
import org.ws4d.java.types.HostedMData;
import org.ws4d.java.types.LocalizedString;
import org.ws4d.java.types.MetadataMData;
import org.ws4d.java.types.ProbeScopeSet;
import org.ws4d.java.types.QName;
import org.ws4d.java.types.QNameSet;
import org.ws4d.java.types.ReferenceParametersMData;
import org.ws4d.java.types.ReferenceParametersMData.ReferenceParameter;
import org.ws4d.java.types.RelationshipMData;
import org.ws4d.java.types.ScopeSet;
import org.ws4d.java.types.ThisDeviceMData;
import org.ws4d.java.types.ThisModelMData;
import org.ws4d.java.types.URI;
import org.ws4d.java.types.URISet;
import org.ws4d.java.types.UnknownDataContainer;
import org.ws4d.java.types.XAddressInfoSet;
import org.ws4d.java.util.Log;
import org.ws4d.java.util.SerializeUtil;
import org.xmlpull.v1.IllegalStateException;

class DefaultMessageSerializer extends MessageSerializer {

	public static final int	MAX_QNAME_SERIALIZATION	= 10;

	public void serialize(HelloMessage message, XmlSerializer serializer, ProtocolData protocolData) throws IOException {
		ConstantsHelper helper = getConstantsHelper(message, protocolData);

		// ################## Body-StartTag ##################
		serializer.startTag(SOAPConstants.SOAP12_NAMESPACE_NAME, SOAPConstants.SOAP_ELEM_BODY);

		// Canonicalize and serialize this element
		if (DPWSFramework.hasModule(DPWSFramework.SECURITY_MODULE)) serializer.attribute("", "ID", "BID1");

		// Start-Tag
		serializer.startTag(helper.getWSDNamespace(), WSDConstants.WSD_ELEMENT_HELLO);
		// Adds UnknownAttributes
		serializeUnknownAttributes(serializer, message);
		// Discovery Data adden
		serialize(message.getDiscoveryData(), serializer, helper);
		// Adds UnknownElements
		serializeUnknownElements(serializer, message);
		// End-Tag
		serializer.endTag(helper.getWSDNamespace(), WSDConstants.WSD_ELEMENT_HELLO);
		// ################## BODY-EndTag ##################
		serializer.endTag(SOAPConstants.SOAP12_NAMESPACE_NAME, SOAPConstants.SOAP_ELEM_BODY);
	}

	public void serialize(ByeMessage message, XmlSerializer serializer, ProtocolData protocolData) throws IOException {
		ConstantsHelper helper = getConstantsHelper(message, protocolData);

		// ################## Body-StartTag ##################
		serializer.startTag(SOAPConstants.SOAP12_NAMESPACE_NAME, SOAPConstants.SOAP_ELEM_BODY);

		// Canonicalize and serialize this element
		if (DPWSFramework.hasModule(DPWSFramework.SECURITY_MODULE)) serializer.attribute("", "ID", "BID1");

		// Start-Tag
		serializer.startTag(helper.getWSDNamespace(), WSDConstants.WSD_ELEMENT_BYE);
		// Adds UnknownAttributes
		serializeUnknownAttributes(serializer, message);
		// Discovery Data adden
		serialize(message.getDiscoveryData(), serializer, helper);
		// Adds UnknownElements
		serializeUnknownElements(serializer, message);
		// END-Tag
		serializer.endTag(helper.getWSDNamespace(), WSDConstants.WSD_ELEMENT_BYE);
		// ################## BODY-EndTag ##################
		serializer.endTag(SOAPConstants.SOAP12_NAMESPACE_NAME, SOAPConstants.SOAP_ELEM_BODY);
	}

	public void serialize(ProbeMessage message, XmlSerializer serializer, ProtocolData protocolData) throws IOException {
		ConstantsHelper helper = getConstantsHelper(message, protocolData);

		// ################## Body-StartTag ##################
		serializer.startTag(SOAPConstants.SOAP12_NAMESPACE_NAME, SOAPConstants.SOAP_ELEM_BODY);
		// Start-Tag
		serializer.startTag(helper.getWSDNamespace(), WSDConstants.WSD_ELEMENT_PROBE);
		// Adds UnknownAttributes
		serializeUnknownAttributes(serializer, message);

		QNameSet types = message.getTypes();
		// QNameSet types
		if (types != null) {
			serialize(types, serializer, helper.getWSDNamespace(), WSDConstants.WSD_ELEMENT_TYPES);
		}

		ProbeScopeSet scopes = message.getScopes();
		// ScopeSet scopes
		if (scopes != null) {
			serialize(scopes, serializer, helper.getWSDNamespace());
		}
		// Adds UnknownElements
		serializeUnknownElements(serializer, message);
		// End-Tag
		serializer.endTag(helper.getWSDNamespace(), WSDConstants.WSD_ELEMENT_PROBE);
		// ################## BODY-EndTag ##################
		serializer.endTag(SOAPConstants.SOAP12_NAMESPACE_NAME, SOAPConstants.SOAP_ELEM_BODY);
	}

	public void serialize(ProbeMatchesMessage message, XmlSerializer serializer, ProtocolData protocolData) throws IOException {
		ConstantsHelper helper = getConstantsHelper(message, protocolData);

		// ################## Body-StartTag ##################
		serializer.startTag(SOAPConstants.SOAP12_NAMESPACE_NAME, SOAPConstants.SOAP_ELEM_BODY);

		// Canonicalize and serialize this element
		if (DPWSFramework.hasModule(DPWSFramework.SECURITY_MODULE)) serializer.attribute("", "ID", "BID1");

		// Start-Tag
		serializer.startTag(helper.getWSDNamespace(), WSDConstants.WSD_ELEMENT_PROBEMATCHES);
		// Adds UnknownAttributes
		serializeUnknownAttributes(serializer, message);
		// Adds ProbeMatch Elements
		DataStructure matches = message.getProbeMatches();
		if (matches != null && !message.isEmpty()) {
			for (Iterator it = matches.iterator(); it.hasNext();) {
				ProbeMatch probeMatch = (ProbeMatch) it.next();
				serialize(probeMatch, serializer, helper);
			}
		}
		// Adds UnknownElements
		serializeUnknownElements(serializer, message);
		// End-Tag
		serializer.endTag(helper.getWSDNamespace(), WSDConstants.WSD_ELEMENT_PROBEMATCHES);
		// ################## BODY-EndTag ##################
		serializer.endTag(SOAPConstants.SOAP12_NAMESPACE_NAME, SOAPConstants.SOAP_ELEM_BODY);
	}

	public void serialize(InvokeMessage message, XmlSerializer serializer, ProtocolData protocolData) throws IOException {
		// ################## Body-StartTag ##################
		serializer.startTag(SOAPConstants.SOAP12_NAMESPACE_NAME, SOAPConstants.SOAP_ELEM_BODY);
		ParameterValue parameters = message.getContent();
		if (parameters != null) {
			parameters.serialize(serializer);
		}
		// ################## BODY-EndTag ##################
		serializer.endTag(SOAPConstants.SOAP12_NAMESPACE_NAME, SOAPConstants.SOAP_ELEM_BODY);
	}

	public void serialize(GetStatusMessage message, XmlSerializer serializer, ProtocolData protocolData) throws IOException {
		// ################## Body-StartTag ##################
		serializer.startTag(SOAPConstants.SOAP12_NAMESPACE_NAME, SOAPConstants.SOAP_ELEM_BODY);
		// Start-Tag
		serializer.startTag(WSEConstants.WSE_NAMESPACE_NAME, WSEConstants.WSE_ELEM_GETSTATUS);
		// Do Nothing because its in the specification defined
		// End-Tag
		serializer.endTag(WSEConstants.WSE_NAMESPACE_NAME, WSEConstants.WSE_ELEM_GETSTATUS);
		// ################## BODY-EndTag ##################
		serializer.endTag(SOAPConstants.SOAP12_NAMESPACE_NAME, SOAPConstants.SOAP_ELEM_BODY);
	}

	public void serialize(GetStatusResponseMessage message, XmlSerializer serializer, ProtocolData protocolData) throws IOException {
		// ################## Body-StartTag ##################
		serializer.startTag(SOAPConstants.SOAP12_NAMESPACE_NAME, SOAPConstants.SOAP_ELEM_BODY);
		// Start-Tag
		serializer.startTag(WSEConstants.WSE_NAMESPACE_NAME, WSEConstants.WSE_ELEM_GETSTATUSRESPONSE);
		// Adds UnknownAttributes
		serializeUnknownAttributes(serializer, message);
		// Expires
		String expires = message.getExpires();
		if (expires != null && !(expires.equals(""))) {
			SerializeUtil.serializeTag(serializer, WSEConstants.WSE_NAMESPACE_NAME, WSEConstants.WSE_ELEM_EXPIRES, expires);
		}
		// Adds UnknownElements
		serializeUnknownElements(serializer, message);
		// End-Tag
		serializer.endTag(WSEConstants.WSE_NAMESPACE_NAME, WSEConstants.WSE_ELEM_GETSTATUSRESPONSE);
		// ################## BODY-EndTag ##################
		serializer.endTag(SOAPConstants.SOAP12_NAMESPACE_NAME, SOAPConstants.SOAP_ELEM_BODY);
	}

	public void serialize(RenewMessage message, XmlSerializer serializer, ProtocolData protocolData) throws IOException {
		// ################## Body-StartTag ##################
		serializer.startTag(SOAPConstants.SOAP12_NAMESPACE_NAME, SOAPConstants.SOAP_ELEM_BODY);
		// Start-Tag
		serializer.startTag(WSEConstants.WSE_NAMESPACE_NAME, WSEConstants.WSE_ELEM_RENEW);
		// Adds UnknownAttributes
		serializeUnknownAttributes(serializer, message);
		// Expires
		String expires = message.getExpires();
		if (expires != null && !(expires.equals(""))) {
			SerializeUtil.serializeTag(serializer, WSEConstants.WSE_NAMESPACE_NAME, WSEConstants.WSE_ELEM_EXPIRES, expires);
		}
		// Adds UnknownElements
		serializeUnknownElements(serializer, message);
		// End-Tag
		serializer.endTag(WSEConstants.WSE_NAMESPACE_NAME, WSEConstants.WSE_ELEM_RENEW);
		// ################## BODY-EndTag ##################
		serializer.endTag(SOAPConstants.SOAP12_NAMESPACE_NAME, SOAPConstants.SOAP_ELEM_BODY);
	}

	public void serialize(RenewResponseMessage message, XmlSerializer serializer, ProtocolData protocolData) throws IOException {
		// ################## Body-StartTag ##################
		serializer.startTag(SOAPConstants.SOAP12_NAMESPACE_NAME, SOAPConstants.SOAP_ELEM_BODY);
		// Start-Tag
		serializer.startTag(WSEConstants.WSE_NAMESPACE_NAME, WSEConstants.WSE_ELEM_RENEWRESPONSE);
		// Adds UnknownAttributes
		serializeUnknownAttributes(serializer, message);
		String expires = message.getExpires();
		// Expires
		if (expires != null && !(expires.equals(""))) {
			SerializeUtil.serializeTag(serializer, WSEConstants.WSE_NAMESPACE_NAME, WSEConstants.WSE_ELEM_EXPIRES, expires);
		}
		// Adds UnknownElements
		serializeUnknownElements(serializer, message);
		// End-Tag
		serializer.endTag(WSEConstants.WSE_NAMESPACE_NAME, WSEConstants.WSE_ELEM_RENEWRESPONSE);
		// ################## BODY-EndTag ##################
		serializer.endTag(SOAPConstants.SOAP12_NAMESPACE_NAME, SOAPConstants.SOAP_ELEM_BODY);
	}

	public void serialize(SubscribeMessage message, XmlSerializer serializer, ProtocolData protocolData) throws IOException {
		ConstantsHelper helper = getConstantsHelper(message, protocolData);

		// ################## Body-StartTag ##################
		serializer.startTag(SOAPConstants.SOAP12_NAMESPACE_NAME, SOAPConstants.SOAP_ELEM_BODY);
		// Start-Tag
		serializer.startTag(WSEConstants.WSE_NAMESPACE_NAME, WSEConstants.WSE_ELEM_SUBSCRIBE);
		// Adds UnknownAttributes
		serializeUnknownAttributes(serializer, message);
		// EndTo

		EndpointReference endTo = message.getEndTo();
		if (endTo != null) {
			serialize(endTo, serializer, helper, WSEConstants.WSE_NAMESPACE_NAME, WSEConstants.WSE_ELEM_ENDTO);
		}

		Delivery delivery = message.getDelivery();
		EventSink eventSink = message.getEventSink();

		// Delivery
		if (delivery != null) {
			if (eventSink != null) {
				URI notifyToAddress = null;
				for (Iterator it = eventSink.getBindings(); it.hasNext();) {
					CommunicationBinding binding = (CommunicationBinding) it.next();
					URI address = binding.getTransportAddress();
					if (protocolData.sourceMatches(address)) {
						if (FrameworkProperties.REFERENCE_PARAM_MODE) {
							notifyToAddress = address;
						} else {
							notifyToAddress = (URI) address.clone();
							notifyToAddress.setFragmentEncoded(delivery.getNotifyTo().getReferenceParameters().getWseIdentifier());
						}
						break;
					}
					if (!it.hasNext()) {
						notifyToAddress = address;
						if (Log.isWarn()) {
							Log.warn("No appropriate local address found for event notification, fallbacking to: " + notifyToAddress);
						}
					}
				}
				delivery.setNotifyTo(new EndpointReference(notifyToAddress, delivery.getNotifyTo().getReferenceParameters()));
			}
			serialize(delivery, serializer, helper);
		}

		// Expires
		String expires = message.getExpires();
		if (expires != null && !(expires.equals(""))) {
			SerializeUtil.serializeTag(serializer, WSEConstants.WSE_NAMESPACE_NAME, WSEConstants.WSE_ELEM_EXPIRES, expires);
		}
		// Filter
		Filter filter = message.getFilter();
		if (filter != null) {
			serialize(filter, serializer, helper);
		}
		// Adds UnknownElements
		serializeUnknownElements(serializer, message);
		// End-Tag
		serializer.endTag(WSEConstants.WSE_NAMESPACE_NAME, WSEConstants.WSE_ELEM_SUBSCRIBE);
		// ################## BODY-EndTag ##################
		serializer.endTag(SOAPConstants.SOAP12_NAMESPACE_NAME, SOAPConstants.SOAP_ELEM_BODY);

	}

	public void serialize(SubscribeResponseMessage message, XmlSerializer serializer, ProtocolData protocolData) throws IOException {
		ConstantsHelper helper = getConstantsHelper(message, protocolData);

		// ################## Body-StartTag ##################
		serializer.startTag(SOAPConstants.SOAP12_NAMESPACE_NAME, SOAPConstants.SOAP_ELEM_BODY);
		// Start-Tag
		serializer.startTag(WSEConstants.WSE_NAMESPACE_NAME, WSEConstants.WSE_ELEM_SUBSCRIBERESPONSE);
		// Adds UnknownAttributes
		serializeUnknownAttributes(serializer, message);
		// Subscripton Manager
		EndpointReference subscriptionManager = message.getSubscriptionManager();
		if (subscriptionManager != null) {
			if (!FrameworkProperties.REFERENCE_PARAM_MODE) {

			}
			serialize(subscriptionManager, serializer, helper, WSEConstants.WSE_NAMESPACE_NAME, WSEConstants.WSE_ELEM_SUBSCRIPTIONMANAGER);
		}
		// Expires
		String expires = message.getExpires();
		if (expires != null && !(expires.equals(""))) {
			SerializeUtil.serializeTag(serializer, WSEConstants.WSE_NAMESPACE_NAME, WSEConstants.WSE_ELEM_EXPIRES, expires);
		}
		// Adds UnknownElements
		serializeUnknownElements(serializer, message);
		// End-Tag
		serializer.endTag(WSEConstants.WSE_NAMESPACE_NAME, WSEConstants.WSE_ELEM_SUBSCRIBERESPONSE);
		// ################## BODY-EndTag ##################
		serializer.endTag(SOAPConstants.SOAP12_NAMESPACE_NAME, SOAPConstants.SOAP_ELEM_BODY);
	}

	public void serialize(SubscriptionEndMessage message, XmlSerializer serializer, ProtocolData protocolData) throws IOException {
		ConstantsHelper helper = getConstantsHelper(message, protocolData);

		// ################## Body-StartTag ##################
		serializer.startTag(SOAPConstants.SOAP12_NAMESPACE_NAME, SOAPConstants.SOAP_ELEM_BODY);
		// Start-Tag
		serializer.startTag(WSEConstants.WSE_NAMESPACE_NAME, WSEConstants.WSE_ELEM_SUBSCRIPTIONEND);
		// Adds UnknownAttributes
		serializeUnknownAttributes(serializer, message);
		// Subscripton Manager
		EndpointReference subscriptionManager = message.getSubscriptionManager();
		if (subscriptionManager != null) {
			serialize(subscriptionManager, serializer, helper, WSEConstants.WSE_NAMESPACE_NAME, WSEConstants.WSE_ELEM_SUBSCRIPTIONMANAGER);
		}

		// Status
		URI status = message.getStatus();
		if (status != null) {
			SerializeUtil.serializeTag(serializer, WSEConstants.WSE_NAMESPACE_NAME, WSEConstants.WSE_ELEM_STATUS, status.toString());
		}

		LocalizedString reason = message.getReason();
		// Reason
		if (reason != null) {
			SerializeUtil.serializeTagWithAttribute(serializer, WSEConstants.WSE_NAMESPACE_NAME, WSEConstants.WSE_ELEM_REASON, reason.getValue(), XMLConstants.XML_NAMESPACE_NAME, XMLConstants.XML_ATTRIBUTE_LANGUAGE, reason.getLanguage());
		}
		// Adds UnknownElements
		serializeUnknownElements(serializer, message);
		// End-Tag
		serializer.endTag(WSEConstants.WSE_NAMESPACE_NAME, WSEConstants.WSE_ELEM_SUBSCRIPTIONEND);
		// ################## BODY-EndTag ##################
		serializer.endTag(SOAPConstants.SOAP12_NAMESPACE_NAME, SOAPConstants.SOAP_ELEM_BODY);
	}

	public void serialize(UnsubscribeMessage message, XmlSerializer serializer, ProtocolData protocolData) throws IOException {
		// ################## Body-StartTag ##################
		serializer.startTag(SOAPConstants.SOAP12_NAMESPACE_NAME, SOAPConstants.SOAP_ELEM_BODY);
		// Start-Tag
		serializer.startTag(WSEConstants.WSE_NAMESPACE_NAME, WSEConstants.WSE_ELEM_UNSUBSCRIBE);
		// Do Nothing because its in the specification defined
		// End-Tag
		serializer.endTag(WSEConstants.WSE_NAMESPACE_NAME, WSEConstants.WSE_ELEM_UNSUBSCRIBE);
		// ################## BODY-EndTag ##################
		serializer.endTag(SOAPConstants.SOAP12_NAMESPACE_NAME, SOAPConstants.SOAP_ELEM_BODY);
	}

	public void serialize(UnsubscribeResponseMessage message, XmlSerializer serializer, ProtocolData protocolData) throws IOException {
		// ################## Body-StartTag ##################
		serializer.startTag(SOAPConstants.SOAP12_NAMESPACE_NAME, SOAPConstants.SOAP_ELEM_BODY);

		// ################## BODY-EndTag ##################
		serializer.endTag(SOAPConstants.SOAP12_NAMESPACE_NAME, SOAPConstants.SOAP_ELEM_BODY);
	}

	public void serialize(GetMessage message, XmlSerializer serializer, ProtocolData protocolData) throws IOException {
		// ################## Body-StartTag ##################
		serializer.startTag(SOAPConstants.SOAP12_NAMESPACE_NAME, SOAPConstants.SOAP_ELEM_BODY);

		// ################## BODY-EndTag ##################
		serializer.endTag(SOAPConstants.SOAP12_NAMESPACE_NAME, SOAPConstants.SOAP_ELEM_BODY);
	}

	public void serialize(GetResponseMessage message, XmlSerializer serializer, ProtocolData protocolData) throws IOException {
		ConstantsHelper helper = getConstantsHelper(message, protocolData);

		// ################## Body-StartTag ##################
		serializer.startTag(SOAPConstants.SOAP12_NAMESPACE_NAME, SOAPConstants.SOAP_ELEM_BODY);
		// Start-Tag
		serializer.startTag(MEXConstants.WSX_NAMESPACE_NAME, MEXConstants.WSX_ELEM_METADATA);
		// Adds UnknownAttributes
		serializeUnknownAttributes(serializer, message);
		// ThisModelMData adden
		ThisModelMData thisModel = message.getThisModel();
		if (thisModel != null) {
			serializer.startTag(MEXConstants.WSX_NAMESPACE_NAME, MEXConstants.WSX_ELEM_METADATASECTION);
			serializer.attribute(null, MEXConstants.WSX_ELEM_DIALECT, helper.getMetadataDialectThisModel());
			serialize(thisModel, serializer, helper);
			serializer.endTag(MEXConstants.WSX_NAMESPACE_NAME, MEXConstants.WSX_ELEM_METADATASECTION);
		}
		// ThisDeviceMData adden
		ThisDeviceMData thisDevice = message.getThisDevice();
		if (thisDevice != null) {
			serializer.startTag(MEXConstants.WSX_NAMESPACE_NAME, MEXConstants.WSX_ELEM_METADATASECTION);
			serializer.attribute(null, MEXConstants.WSX_ELEM_DIALECT, helper.getMetadataDialectThisDevice());
			serialize(thisDevice, serializer, helper);
			serializer.endTag(MEXConstants.WSX_NAMESPACE_NAME, MEXConstants.WSX_ELEM_METADATASECTION);
		}
		// RelationshipMData adden
		RelationshipMData relationship = message.getRelationship();
		if (relationship != null) {
			serializer.startTag(MEXConstants.WSX_NAMESPACE_NAME, MEXConstants.WSX_ELEM_METADATASECTION);
			serializer.attribute(null, MEXConstants.WSX_ELEM_DIALECT, helper.getMetatdataDialectRelationship());
			serialize(relationship, serializer, helper);
			serializer.endTag(MEXConstants.WSX_NAMESPACE_NAME, MEXConstants.WSX_ELEM_METADATASECTION);
		}
		// if a user has add customize metadata they will be serialize.
		CustomizeMData mdata = message.getCustomMdataInstance();
		if (mdata != null) {
			serializer.startTag(MEXConstants.WSX_NAMESPACE_NAME, MEXConstants.WSX_ELEM_METADATASECTION);
			// System.out.println(mdata);
			HashMap handler = ElementHandlerRegistry.getRegistry().getAllElementHandler();
			if (!handler.isEmpty()) {
				Iterator it = handler.keySet().iterator();
				while (it.hasNext()) {
					QName key = (QName) it.next();
					Object value = handler.get(key);
					// check if the user has an own ElementHandler
					// if not a generic handler will serialize the data
					ElementHandler customHandler = ElementHandlerRegistry.getRegistry().getElementHandler(key);
					if (customHandler.equals(null)) {
						customHandler.serializeElement(serializer, key, value);
					} else
						CustomizeMDataHandler.getInstance().serializeElement(serializer, CustomizeMData.CUSTOM, mdata.getUnknownElements());
				}
			}
			serializer.endTag(MEXConstants.WSX_NAMESPACE_NAME, MEXConstants.WSX_ELEM_METADATASECTION);
		}
		// Adds UnknownElements
		serializeUnknownElements(serializer, message);
		// End-Tag
		serializer.endTag(MEXConstants.WSX_NAMESPACE_NAME, MEXConstants.WSX_ELEM_METADATA);
		// ################## BODY-EndTag ##################
		serializer.endTag(SOAPConstants.SOAP12_NAMESPACE_NAME, SOAPConstants.SOAP_ELEM_BODY);
	}

	public void serialize(GetMetadataMessage message, XmlSerializer serializer, ProtocolData protocolData) throws IOException {
		// ################## Body-StartTag ##################
		serializer.startTag(SOAPConstants.SOAP12_NAMESPACE_NAME, SOAPConstants.SOAP_ELEM_BODY);
		// Start-Tag
		serializer.startTag(MEXConstants.WSX_NAMESPACE_NAME, MEXConstants.WSX_ELEM_GETMETADATA);
		// Adds UnknownAttributes
		serializeUnknownAttributes(serializer, message);
		// Dialect adden
		URI dialect = message.getDialect();
		if (dialect != null) {
			SerializeUtil.serializeTag(serializer, MEXConstants.WSX_NAMESPACE_NAME, MEXConstants.WSX_ELEM_DIALECT, (dialect.toString() == null ? "" : dialect.toString()));
		}
		// Identifier adden
		URI identifier = message.getIdentifier();
		if (identifier != null) {
			SerializeUtil.serializeTag(serializer, MEXConstants.WSX_NAMESPACE_NAME, MEXConstants.WSX_ELEM_IDENTIFIER, (identifier.toString() == null ? "" : identifier.toString()));
		}
		// Adds UnknownElements
		serializeUnknownElements(serializer, message);
		// End-Tag
		serializer.endTag(MEXConstants.WSX_NAMESPACE_NAME, MEXConstants.WSX_ELEM_GETMETADATA);
		// ################## BODY-EndTag ##################
		serializer.endTag(SOAPConstants.SOAP12_NAMESPACE_NAME, SOAPConstants.SOAP_ELEM_BODY);
	}

	public void serialize(GetMetadataResponseMessage message, XmlSerializer serializer, ProtocolData protocolData) throws IOException {
		ConstantsHelper helper = getConstantsHelper(message, protocolData);

		// ################## Body-StartTag ##################
		serializer.startTag(SOAPConstants.SOAP12_NAMESPACE_NAME, SOAPConstants.SOAP_ELEM_BODY);
		// Start Tag
		serializer.startTag(MEXConstants.WSX_NAMESPACE_NAME, MEXConstants.WSX_ELEM_METADATA);
		// Adds UnknownAttributes
		serializeUnknownAttributes(serializer, message);
		EndpointReferenceSet metadataReferences = message.getMetadataReferences();
		if (metadataReferences != null) {
			for (Iterator it = metadataReferences.iterator(); it.hasNext();) {
				EndpointReference ref = (EndpointReference) it.next();
				// Start MetadataSection for WSDL
				serializer.startTag(MEXConstants.WSX_NAMESPACE_NAME, MEXConstants.WSX_ELEM_METADATASECTION);
				// Dialect adden
				serializer.attribute(null, MEXConstants.WSX_ELEM_DIALECT, MEXConstants.WSX_DIALECT_WSDL);
				// EndpointReference(s) adden
				serialize(ref, serializer, helper, MEXConstants.WSX_NAMESPACE_NAME, MEXConstants.WSX_ELEM_METADATAREFERENCE);
				// End Tag
				serializer.endTag(MEXConstants.WSX_NAMESPACE_NAME, MEXConstants.WSX_ELEM_METADATASECTION);
			}
		}
		URISet metadataLocations = message.getMetadataLocations();
		if (metadataLocations != null) {
			for (Iterator it = metadataLocations.iterator(); it.hasNext();) {
				URI location = (URI) it.next();
				// Start MetadataSection for WSDL
				serializer.startTag(MEXConstants.WSX_NAMESPACE_NAME, MEXConstants.WSX_ELEM_METADATASECTION);
				// Dialect adden
				serializer.attribute(null, MEXConstants.WSX_ELEM_DIALECT, MEXConstants.WSX_DIALECT_WSDL);
				// URI(s) adden
				SerializeUtil.serializeTag(serializer, MEXConstants.WSX_NAMESPACE_NAME, MEXConstants.WSX_ELEM_LOCATION, (location.toString() == null ? "" : location.toString()));
				// End Tag
				serializer.endTag(MEXConstants.WSX_NAMESPACE_NAME, MEXConstants.WSX_ELEM_METADATASECTION);
			}
		}
		// Start MetadataSection for Relationship
		serializer.startTag(MEXConstants.WSX_NAMESPACE_NAME, MEXConstants.WSX_ELEM_METADATASECTION);
		// RelationshipMData adden
		serializer.attribute(null, MEXConstants.WSX_ELEM_DIALECT, helper.getMetatdataDialectRelationship());
		RelationshipMData r = message.getRelationship();
		if (r != null) {
			serialize(r, serializer, helper);
		}
		// End Tags
		serializer.endTag(MEXConstants.WSX_NAMESPACE_NAME, MEXConstants.WSX_ELEM_METADATASECTION);
		// Adds UnknownElements
		serializeUnknownElements(serializer, message);
		serializer.endTag(MEXConstants.WSX_NAMESPACE_NAME, MEXConstants.WSX_ELEM_METADATA);
		// ################## BODY-EndTag ##################
		serializer.endTag(SOAPConstants.SOAP12_NAMESPACE_NAME, SOAPConstants.SOAP_ELEM_BODY);
	}

	public void serialize(ResolveMessage message, XmlSerializer serializer, ProtocolData protocolData) throws IOException {
		ConstantsHelper helper = getConstantsHelper(message, protocolData);

		// ################## Body-StartTag ##################
		serializer.startTag(SOAPConstants.SOAP12_NAMESPACE_NAME, SOAPConstants.SOAP_ELEM_BODY);
		// Start-Tag
		serializer.startTag(helper.getWSDNamespace(), WSDConstants.WSD_ELEMENT_RESOLVE);
		// Adds UnknownAttributes
		serializeUnknownAttributes(serializer, message);
		// Add the EPR
		EndpointReference endpointReference = message.getEndpointReference();
		if (endpointReference != null) {
			serialize(endpointReference, serializer, helper, helper.getWSANamespace(), WSAConstants.WSA_ELEM_ENDPOINT_REFERENCE);
		}
		// Adds UnknownElements
		serializeUnknownElements(serializer, message);
		// End-Tag
		serializer.endTag(helper.getWSDNamespace(), WSDConstants.WSD_ELEMENT_RESOLVE);
		// ################## BODY-EndTag ##################
		serializer.endTag(SOAPConstants.SOAP12_NAMESPACE_NAME, SOAPConstants.SOAP_ELEM_BODY);
	}

	public void serialize(ResolveMatchesMessage message, XmlSerializer serializer, ProtocolData protocolData) throws IOException {
		ConstantsHelper helper = getConstantsHelper(message, protocolData);

		// ################## Body-StartTag ##################
		serializer.startTag(SOAPConstants.SOAP12_NAMESPACE_NAME, SOAPConstants.SOAP_ELEM_BODY);

		// Canonicalize and serialize this element
		if (DPWSFramework.hasModule(DPWSFramework.SECURITY_MODULE)) serializer.attribute("", "ID", "BID1");

		// Start-Tag
		serializer.startTag(helper.getWSDNamespace(), WSDConstants.WSD_ELEMENT_RESOLVEMATCHES);
		// Adds UnknownAttributes
		serializeUnknownAttributes(serializer, message);
		// Adds ResolveMatch Elements
		ResolveMatch match = message.getResolveMatch();
		if (match != null) {
			serialize(match, serializer, helper);
		}
		// Adds UnknownElements
		serializeUnknownElements(serializer, message);
		// End-Tag
		serializer.endTag(helper.getWSDNamespace(), WSDConstants.WSD_ELEMENT_RESOLVEMATCHES);
		// ################## BODY-EndTag ##################
		serializer.endTag(SOAPConstants.SOAP12_NAMESPACE_NAME, SOAPConstants.SOAP_ELEM_BODY);
	}

	public void serialize(ResolveMatch match, XmlSerializer serializer, ConstantsHelper helper) throws IllegalArgumentException, IllegalStateException, IOException {
		serializer.startTag(helper.getWSDNamespace(), WSDConstants.WSD_ELEMENT_RESOLVEMATCH);
		// Adds UnknownAttributes
		serializeUnknownAttributes(serializer, match);
		// Discovery Data adden
		serialize((DiscoveryData) match, serializer, helper);
		// Adds UnknownElements
		serializeUnknownElements(serializer, match);
		serializer.endTag(helper.getWSDNamespace(), WSDConstants.WSD_ELEMENT_RESOLVEMATCH);
	}

	public void serialize(FaultMessage message, XmlSerializer serializer, ProtocolData protocolData) throws IOException {
		// ################## Body-StartTag ##################
		serializer.startTag(SOAPConstants.SOAP12_NAMESPACE_NAME, SOAPConstants.SOAP_ELEM_BODY);
		// Fault-StartTag
		serializer.startTag(SOAPConstants.SOAP12_NAMESPACE_NAME, SOAPConstants.SOAP_ELEM_FAULT);
		// Adds UnknownAttributes
		serializeUnknownAttributes(serializer, message);
		// Code
		QName code = message.getCode();
		if (code != null) {
			serializer.startTag(SOAPConstants.SOAP12_NAMESPACE_NAME, SOAPConstants.SOAP_ELEM_CODE);
			// Valueelement
			serializer.startTag(SOAPConstants.SOAP12_NAMESPACE_NAME, SOAPConstants.SOAP_ELEM_VALUE);
			String prefix = serializer.getPrefix(code.getNamespace(), true);
			serializer.text(prefix + ":" + code.getLocalPart());
			serializer.endTag(SOAPConstants.SOAP12_NAMESPACE_NAME, SOAPConstants.SOAP_ELEM_VALUE);
			// Subcode
			QName subcode = message.getSubcode();
			if (subcode != null) {
				serializer.startTag(SOAPConstants.SOAP12_NAMESPACE_NAME, SOAPConstants.SOAP_ELEM_SUBCODE);
				// Valueelement
				serializer.startTag(SOAPConstants.SOAP12_NAMESPACE_NAME, SOAPConstants.SOAP_ELEM_VALUE);
				String prefix1 = serializer.getPrefix(subcode.getNamespace(), true);
				serializer.text(prefix1 + ":" + subcode.getLocalPart());
				serializer.endTag(SOAPConstants.SOAP12_NAMESPACE_NAME, SOAPConstants.SOAP_ELEM_VALUE);
				// Subsubcode
				QName subsubcode = message.getSubsubcode();
				if (subsubcode != null) {
					serializer.startTag(SOAPConstants.SOAP12_NAMESPACE_NAME, SOAPConstants.SOAP_ELEM_SUBCODE);
					// Valueelement
					serializer.startTag(SOAPConstants.SOAP12_NAMESPACE_NAME, SOAPConstants.SOAP_ELEM_VALUE);
					String prefix2 = serializer.getPrefix(subsubcode.getNamespace(), true);
					serializer.text(prefix2 + ":" + subsubcode.getLocalPart());
					serializer.endTag(SOAPConstants.SOAP12_NAMESPACE_NAME, SOAPConstants.SOAP_ELEM_VALUE);
					serializer.endTag(SOAPConstants.SOAP12_NAMESPACE_NAME, SOAPConstants.SOAP_ELEM_SUBCODE);
				}
				serializer.endTag(SOAPConstants.SOAP12_NAMESPACE_NAME, SOAPConstants.SOAP_ELEM_SUBCODE);
			}
			serializer.endTag(SOAPConstants.SOAP12_NAMESPACE_NAME, SOAPConstants.SOAP_ELEM_CODE);
		}
		// Reason
		DataStructure reason = message.getReason();
		if (reason != null) {
			ArrayList list = (ArrayList) reason;
			serializer.startTag(SOAPConstants.SOAP12_NAMESPACE_NAME, SOAPConstants.SOAP_ELEM_REASON);
			for (Iterator it = list.iterator(); it.hasNext();) {
				LocalizedString string = (LocalizedString) it.next();
				SerializeUtil.serializeTagWithAttribute(serializer, SOAPConstants.SOAP12_NAMESPACE_NAME, SOAPConstants.SOAP_ELEM_TEXT, string.getValue(), XMLConstants.XML_NAMESPACE_NAME, XMLConstants.XML_ATTRIBUTE_LANGUAGE, string.getLanguage());
			}
			serializer.endTag(SOAPConstants.SOAP12_NAMESPACE_NAME, SOAPConstants.SOAP_ELEM_REASON);
		}
		ParameterValue detail = message.getDetail();
		if (detail != null) {
			serializer.startTag(SOAPConstants.SOAP12_NAMESPACE_NAME, SOAPConstants.SOAP_ELEM_DETAIL);
			detail.serialize(serializer);
			serializer.endTag(SOAPConstants.SOAP12_NAMESPACE_NAME, SOAPConstants.SOAP_ELEM_DETAIL);
		}
		// Adds UnknownElements
		serializeUnknownElements(serializer, message);
		// End-Tag
		serializer.endTag(SOAPConstants.SOAP12_NAMESPACE_NAME, SOAPConstants.SOAP_ELEM_FAULT);
		// ################## BODY-EndTag ##################
		serializer.endTag(SOAPConstants.SOAP12_NAMESPACE_NAME, SOAPConstants.SOAP_ELEM_BODY);
	}

	public void serialize(SOAPHeader header, XmlSerializer serializer, ProtocolData protocolData) throws IOException {
		ConstantsHelper helper = getConstantsHelper(header, protocolData);

		// prerequisite namespaces for copied reference parameters => optional
		ReferenceParametersMData params = header.getReferenceParameters();
		if (params != null) {
			serializeNamespacePrefixes(params, serializer);
		}

		// ################## Header-StartTag ##################
		serializer.startTag(SOAPConstants.SOAP12_NAMESPACE_NAME, SOAPConstants.SOAP_ELEM_HEADER);
		// Adds UnknownAttributes to Header Tag if exists
		serializeUnknownAttributes(serializer, header);
		// Action-Tag => mandatory
		header.getAction().serialize(serializer, helper.getWSANamespace(), WSAConstants.WSA_ELEM_ACTION);

		// MessageID-Tag => optional
		if (header.getMessageId() != null) {
			header.getMessageId().serialize(serializer, helper.getWSANamespace(), WSAConstants.WSA_ELEM_MESSAGE_ID);
		}
		// relatesTo-Tag => optional
		if (header.getRelatesTo() != null) {
			header.getRelatesTo().serialize(serializer, helper.getWSANamespace(), WSAConstants.WSA_ELEM_RELATESTO);
		}
		// replyTo-Tag => optional
		if (header.getReplyTo() != null) {
			serialize(header.getReplyTo(), serializer, helper, helper.getWSANamespace(), WSAConstants.WSA_ELEM_REPLY_TO);
		}
		// To-Tag => optional
		if (header.getTo() != null) {
			header.getTo().serialize(serializer, helper.getWSANamespace(), WSAConstants.WSA_ELEM_TO);
		}
		// copied reference parameters => optional
		if (params != null && !params.isEmpty()) {
			serialize(params, serializer, helper, true);
		}
		// AppSequence-Tag => optional
		if (header.getAppSequence() != null) {
			serialize(header.getAppSequence(), serializer, helper);
		}
		// Adds UnknownElements to Header if exists
		serializeUnknownElements(serializer, header);
		// ################## Header-EndTag ##################
		serializer.endTag(SOAPConstants.SOAP12_NAMESPACE_NAME, SOAPConstants.SOAP_ELEM_HEADER);

	}

	/* Serialize unkown data container */

	private void serializeUnknownElements(XmlSerializer serializer, UnknownDataContainer container) throws IOException {
		HashMap unknownElements_QN_2_List = container.getUnknownElements();
		if (unknownElements_QN_2_List != null) {
			for (Iterator it = unknownElements_QN_2_List.entrySet().iterator(); it.hasNext();) {
				HashMap.Entry ent = (Entry) it.next();
				QName qname = (QName) ent.getKey();
				serializer.unknownElements(qname, (List) ent.getValue());
			}
		}

	}

	private void serializeUnknownAttributes(XmlSerializer serializer, UnknownDataContainer container) throws IOException {
		HashMap unknownAttributes = container.getUnknownAttributes();
		if (unknownAttributes != null) {
			for (Iterator it = unknownAttributes.entrySet().iterator(); it.hasNext();) {
				HashMap.Entry ent = (Entry) it.next();
				QName qname = (QName) ent.getKey();
				String value = (String) ent.getValue();
				serializer.attribute(qname.getNamespace(), qname.getLocalPart(), value);
			}
		}
	}

	private void serialize(ProbeMatch match, XmlSerializer serializer, ConstantsHelper helper) throws IllegalArgumentException, IllegalStateException, IOException {
		serializer.startTag(helper.getWSDNamespace(), WSDConstants.WSD_ELEMENT_PROBEMATCH);
		// Adds UnknownAttributes
		serializeUnknownAttributes(serializer, match);
		// Discovery Data adden
		serialize((DiscoveryData) match, serializer, helper);
		// Adds UnknownElements
		serializeUnknownElements(serializer, match);
		serializer.endTag(helper.getWSDNamespace(), WSDConstants.WSD_ELEMENT_PROBEMATCH);
	}

	private void serialize(DiscoveryData data, XmlSerializer serializer, ConstantsHelper helper) throws IOException {
		// Endpointreference
		EndpointReference endpointReference = data.getEndpointReference();
		serialize(endpointReference, serializer, helper, helper.getWSANamespace(), WSAConstants.WSA_ELEM_ENDPOINT_REFERENCE);

		// QNameSet Types
		QNameSet types = data.getTypes();
		if (types != null) {
			serialize(types, serializer, helper.getWSDNamespace(), WSDConstants.WSD_ELEMENT_TYPES);
		}
		// ScopeSet scopes
		ScopeSet scopes = data.getScopes();
		if (scopes != null) {
			serialize(scopes, serializer, helper.getWSDNamespace());
		}
		// URISet xAddress
		XAddressInfoSet xAddrs = data.getXAddressInfoSet();
		if (xAddrs != null) {
			serialize(xAddrs, serializer, helper.getWSDNamespace());
		}
		// MetadataVersion
		long metadataVersion = data.getMetadataVersion();
		if (metadataVersion >= 1) {
			SerializeUtil.serializeTag(serializer, helper.getWSDNamespace(), WSDConstants.WSD_ELEMENT_METADATAVERSION, ("" + metadataVersion));
		}
		// Adds UnknownElements to Header if exists
		serializeUnknownElements(serializer, data);
	}

	private void serialize(AppSequence sequence, XmlSerializer serializer, ConstantsHelper helper) throws IllegalArgumentException, IllegalStateException, IOException {
		serializer.startTag(helper.getWSDNamespace(), WSDConstants.WSD_ELEMENT_APPSEQUENCE);
		// Adds UnknownAttributes
		serializeUnknownAttributes(serializer, sequence);
		long instanceId = sequence.getInstanceId();
		if (instanceId >= 1) {
			serializer.attribute(null, WSDConstants.WSD_ATTR_INSTANCEID, "" + instanceId);
		}
		long messageNumber = sequence.getMessageNumber();
		if (messageNumber >= 1) {
			serializer.attribute(null, WSDConstants.WSD_ATTR_MESSAGENUMBER, "" + messageNumber);
		}
		String sequenceId = sequence.getSequenceId();
		if (sequenceId != null) {
			serializer.attribute(null, WSDConstants.WSD_ATTR_SEQUENCEID, "" + sequenceId);
		}
		// Adds UnknownElements
		serializeUnknownElements(serializer, sequence);
		serializer.endTag(helper.getWSDNamespace(), WSDConstants.WSD_ELEMENT_APPSEQUENCE);
	}

	private void serialize(EndpointReference ref, XmlSerializer serializer, ConstantsHelper helper, String namespace, String elementName) throws IOException {
		// Start-Tag
		serializer.startTag(namespace, elementName);
		// Adds UnknownAttributes to EPR Tag if exists
		serializeUnknownAttributes(serializer, ref);
		// Address Element
		AttributedURI address = ref.getAddress();
		address.serialize(serializer, helper.getWSANamespace(), WSAConstants.WSA_ELEM_ADDRESS);
		// ReferenceParameters Element
		ReferenceParametersMData referenceParameters = ref.getReferenceParameters();
		if (referenceParameters != null && !referenceParameters.isEmpty()) {
			serializer.startTag(helper.getWSANamespace(), WSAConstants.WSA_ELEM_REFERENCE_PARAMETERS);
			serializeNamespacePrefixes(referenceParameters, serializer);
			serializeUnknownAttributes(serializer, referenceParameters);
			// fake in order to dump reference parameter prefixes
			serializer.text("");
			serialize(referenceParameters, serializer, helper, false);
			serializer.endTag(helper.getWSANamespace(), WSAConstants.WSA_ELEM_REFERENCE_PARAMETERS);
		}
		// Metadata Element
		MetadataMData endpointMetadata = ref.getEndpointMetadata();
		if (endpointMetadata != null) {
			serializer.startTag(helper.getWSANamespace(), WSAConstants.WSA_ELEM_METADATA);
			serializeUnknownAttributes(serializer, endpointMetadata);
			serializeUnknownElements(serializer, endpointMetadata);
			serializer.endTag(helper.getWSANamespace(), WSAConstants.WSA_ELEM_METADATA);
		}
		// Adds UnknownElements to EPR if exists
		serializeUnknownElements(serializer, ref);
		serializer.endTag(namespace, elementName);
	}

	private void serialize(Delivery delivery, XmlSerializer serializer, ConstantsHelper helper) throws IllegalArgumentException, IllegalStateException, IOException {
		serializer.startTag(WSEConstants.WSE_NAMESPACE_NAME, WSEConstants.WSE_ELEM_DELIVERY);
		// Adds UnknownAttributes
		serializeUnknownAttributes(serializer, delivery);
		// Add the DeliveryMode
		URI mode = delivery.getMode();
		if (mode != null) {
			serializer.attribute(null, WSEConstants.WSE_ATTR_DELIVERY_MODE, mode.toString());
		}
		EndpointReference notifyTo = delivery.getNotifyTo();
		// Add the EPR
		serialize(notifyTo, serializer, helper, WSEConstants.WSE_NAMESPACE_NAME, WSEConstants.WSE_ELEM_NOTIFYTO);
		// Adds UnknownElements
		serializeUnknownElements(serializer, delivery);
		serializer.endTag(WSEConstants.WSE_NAMESPACE_NAME, WSEConstants.WSE_ELEM_DELIVERY);
	}

	private void serialize(Filter filter, XmlSerializer serializer, ConstantsHelper helper) throws IllegalArgumentException, IllegalStateException, IOException {
		serializer.startTag(WSEConstants.WSE_NAMESPACE_NAME, WSEConstants.WSE_ELEM_FILTER);
		// Adds UnknownAttributes
		serializeUnknownAttributes(serializer, filter);
		URI dialect = filter.getDialect();
		if (dialect != null) {
			serializer.attribute(null, WSEConstants.WSE_ATTR_FILTER_DIALECT, dialect.toString());
		}
		URISet actions = filter.getActions();
		if (actions != null) {
			for (Iterator it = actions.iterator(); it.hasNext();) {
				URI uri = (URI) it.next();
				serializer.text(uri.toString());
				if (it.hasNext()) {
					serializer.text(" ");
				}
			}
		}
		// Adds UnknownElements
		serializeUnknownElements(serializer, filter);
		serializer.endTag(WSEConstants.WSE_NAMESPACE_NAME, WSEConstants.WSE_ELEM_FILTER);
	}

	private void serialize(HostedMData data, XmlSerializer serializer, ConstantsHelper helper) throws IllegalArgumentException, IllegalStateException, IOException {
		serializer.startTag(helper.getDPWSNamespace(), helper.getDPWSElementRelationshipHosted());
		// Adds UnknownAttributes
		serializeUnknownAttributes(serializer, data);
		// Endpoint References
		for (Iterator it = data.getEprInfoSet().iterator(); it.hasNext();) {
			EprInfo epr = (EprInfo) it.next();
			serialize(epr.getEndpointReference(), serializer, helper, helper.getWSANamespace(), WSAConstants.WSA_ELEM_ENDPOINT_REFERENCE);
		}
		// ServiceTypes
		QNameSet types = data.getTypes();
		if (types != null) {
			serialize(types, serializer, helper.getDPWSNamespace(), helper.getDPWSElementTypes());
		}
		// Add ServiceID
		URI serviceId = data.getServiceId();

		if (serviceId != null) {
			SerializeUtil.serializeTag(serializer, helper.getDPWSNamespace(), helper.getDPWSElementServiceId(), (serviceId == null ? null : serviceId.toString()));
		}
		// Adds UnknownElements
		serializeUnknownElements(serializer, data);
		serializer.endTag(helper.getDPWSNamespace(), helper.getDPWSElementRelationshipHosted());
	}

	private void serialize(HostMData data, XmlSerializer serializer, ConstantsHelper helper) throws IllegalArgumentException, IllegalStateException, IOException {
		serializer.startTag(helper.getDPWSNamespace(), helper.getDPWSElementRelationshipHost());
		// Adds UnknownAttributes
		serializeUnknownAttributes(serializer, data);
		// Add the EPR of Host
		EndpointReference endpoint = data.getEndpointReference();
		if (endpoint != null) {
			serialize(endpoint, serializer, helper, helper.getWSANamespace(), WSAConstants.WSA_ELEM_ENDPOINT_REFERENCE);
		}
		// Add the Service Types of Host
		QNameSet types = data.getTypes();
		if (types != null) {
			serialize(types, serializer, helper.getDPWSNamespace(), helper.getDPWSElementTypes());
		}
		// Adds UnknownElements
		serializeUnknownElements(serializer, data);
		serializer.endTag(helper.getDPWSNamespace(), helper.getDPWSElementRelationshipHost());
	}

	private void serialize(ReferenceParametersMData data, XmlSerializer serializer, ConstantsHelper helper, boolean withinHeader) throws IOException {
		// any XML special chars should remain unescaped
		String wseIdentifier = data.getWseIdentifier();
		if (wseIdentifier != null && FrameworkProperties.REFERENCE_PARAM_MODE) {
			serializer.startTag(WSEConstants.WSE_NAMESPACE_NAME, WSEConstants.WSE_ELEM_IDENTIFIER);
			if (withinHeader) {
				serializer.attribute(helper.getWSANamespace(), WSAConstants.WSA_ATTR_IS_REFERENCE_PARAMETER, "true");
			}
			serializer.text(wseIdentifier == null ? "" : wseIdentifier);
			serializer.endTag(WSEConstants.WSE_NAMESPACE_NAME, WSEConstants.WSE_ELEM_IDENTIFIER);
		} else {
			// we need this to close the preceding element tag
			serializer.text("");
		}
		serializeUnknownElements(serializer, data);
		ReferenceParameter[] allParameters = data.getParameters();
		for (int i = 0; i < allParameters.length; i++) {
			ReferenceParameter parameter = allParameters[i];
			serializer.plainText("<");
			String prefix = serializer.getPrefix(parameter.getNamespace(), true);
			serializer.plainText(prefix);
			serializer.plainText(":");
			serializer.plainText(parameter.getName());
			// add wsa:IsReferenceParameter if withinHeader == true
			if (withinHeader) {
				serializer.plainText(" ");
				prefix = serializer.getPrefix(helper.getWSANamespace(), true);
				serializer.plainText(prefix);
				serializer.plainText(":");
				serializer.plainText(WSAConstants.WSA_ATTR_IS_REFERENCE_PARAMETER);
				serializer.plainText("=\"true\"");
			}
			String[] chunks = parameter.getChunks();
			for (int j = 0; j < chunks.length; j++) {
				if (j % 2 == 0) {
					serializer.plainText(chunks[j]);
				} else {
					prefix = serializer.getPrefix(chunks[j], true);
					serializer.plainText(prefix);
				}
			}
		}
	}

	private void serializeNamespacePrefixes(ReferenceParametersMData data, XmlSerializer serializer) {
		ReferenceParameter[] allParameters = data.getParameters();
		for (int i = 0; i < allParameters.length; i++) {
			ReferenceParameter parameter = allParameters[i];
			String[] chunks = parameter.getChunks();
			for (int j = 1; j < chunks.length; j += 2) {
				serializer.getPrefix(chunks[j], true);
			}
		}
	}

	private void serialize(RelationshipMData data, XmlSerializer serializer, ConstantsHelper helper) throws IllegalArgumentException, IllegalStateException, IOException {
		// StartTag => dpws:Relationship
		serializer.startTag(helper.getDPWSNamespace(), helper.getDPWSElementRelationship());
		serializer.attribute(null, helper.getDPWSAttributeRelationshipType(), helper.getMetadataRelationshipHostingType());
		// Adds UnknownAttributes
		serializeUnknownAttributes(serializer, data);
		// Host
		HostMData host = data.getHost();
		if (host != null) {
			serialize(host, serializer, helper);
		}
		// Hosted
		DataStructure hosted = data.getHosted();
		if (hosted != null) {
			for (Iterator it = hosted.iterator(); it.hasNext();) {
				HostedMData hostedData = (HostedMData) it.next();
				serialize(hostedData, serializer, helper);
			}
		}
		// Adds UnknownElements
		serializeUnknownElements(serializer, data);
		// EndTag => dpws:Relationship
		serializer.endTag(helper.getDPWSNamespace(), helper.getDPWSElementRelationship());
	}

	private void serialize(ThisDeviceMData data, XmlSerializer serializer, ConstantsHelper helper) throws IllegalArgumentException, IllegalStateException, IOException {
		LocalizedString value = null;
		// StartTag => dpws:thisModel
		serializer.startTag(helper.getDPWSNamespace(), helper.getDPWSElementThisDevice());
		// Adds UnknownAttributes
		serializeUnknownAttributes(serializer, data);
		// FriendlyName => 1 -> *
		DataStructure friendlyNames = data.getFriendlyNames();
		if (friendlyNames == null || friendlyNames.size() == 0) {
			Log.warn("Message2SOAPGenerator.addThisDevice: No friendly name defined within device");
		} else {
			for (Iterator it = friendlyNames.iterator(); it.hasNext();) {
				value = (LocalizedString) it.next();
				SerializeUtil.serializeTagWithAttribute(serializer, helper.getDPWSNamespace(), helper.getDPWSElementFriendlyName(), value.getValue(), XMLConstants.XML_NAMESPACE_NAME, XMLConstants.XML_ATTRIBUTE_LANGUAGE, value.getLanguage());
			}
		}
		// FirmwareVersion => 0 -> 1
		String firmwareVersion = data.getFirmwareVersion();
		if (firmwareVersion != null && !(firmwareVersion.equals(""))) {
			SerializeUtil.serializeTag(serializer, helper.getDPWSNamespace(), helper.getDPWSElementFirmwareVersion(), firmwareVersion);
		}
		// SerialNumber => 0 -> 1
		String serialNumber = data.getSerialNumber();
		if (serialNumber != null && !(serialNumber.equals(""))) {
			SerializeUtil.serializeTag(serializer, helper.getDPWSNamespace(), helper.getDPWSElementSerialnumber(), serialNumber);
		}
		// Adds UnknownElements
		serializeUnknownElements(serializer, data);
		// EndTag => dpws:thisModel
		serializer.endTag(helper.getDPWSNamespace(), helper.getDPWSElementThisDevice());
	}

	private void serialize(ThisModelMData data, XmlSerializer serializer, ConstantsHelper helper) throws IllegalArgumentException, IllegalStateException, IOException {
		LocalizedString value = null;
		// StartTag => dpws:thisModel
		serializer.startTag(helper.getDPWSNamespace(), helper.getDPWSElementThisModel());
		// Adds UnknownAttributes
		serializeUnknownAttributes(serializer, data);
		// Manufacturer => 1 -> *
		DataStructure manufacturer = data.getManufacturerNames();
		for (Iterator it = manufacturer.iterator(); it.hasNext();) {
			value = (LocalizedString) it.next();
			SerializeUtil.serializeTagWithAttribute(serializer, helper.getDPWSNamespace(), helper.getDPWSElementManufacturer(), value.getValue(), XMLConstants.XML_NAMESPACE_NAME, XMLConstants.XML_ATTRIBUTE_LANGUAGE, value.getLanguage());
		}
		// ManufaturerURL => 0 -> 1
		URI manufacturerURL = data.getManufacturerUrl();
		if (manufacturerURL != null && !(manufacturerURL.toString().equals(""))) {
			SerializeUtil.serializeTag(serializer, helper.getDPWSNamespace(), helper.getDPWSElementManufacturerURL(), (manufacturerURL.toString() == null ? "" : manufacturerURL.toString()));
		}
		// ModelName => 1 -> *
		value = null;
		DataStructure modelNames = data.getModelNames();
		if (modelNames == null || modelNames.size() == 0) {
			Log.warn("Message2SOAPGenerator.addThisModel: No model name defined within device");
		} else {
			for (Iterator it = modelNames.iterator(); it.hasNext();) {
				value = (LocalizedString) it.next();
				SerializeUtil.serializeTagWithAttribute(serializer, helper.getDPWSNamespace(), helper.getDPWSElementModelName(), value.getValue(), XMLConstants.XML_NAMESPACE_NAME, XMLConstants.XML_ATTRIBUTE_LANGUAGE, value.getLanguage());
			}
		}
		// ModelNumber => 0 -> 1
		String modelNumber = data.getModelNumber();
		if (modelNumber != null && !(modelNumber.equals(""))) {
			SerializeUtil.serializeTag(serializer, helper.getDPWSNamespace(), helper.getDPWSElementModelNumber(), (modelNumber == null ? "" : modelNumber));
		}
		// ModelUrl => 0 -> 1
		URI modelUrl = data.getModelUrl();
		if (modelUrl != null && !(modelUrl.toString().equals(""))) {
			SerializeUtil.serializeTag(serializer, helper.getDPWSNamespace(), helper.getDPWSElementModelURL(), (modelUrl.toString() == null ? "" : modelUrl.toString()));
		}
		// PresentationUrl => 0 -> 1
		URI presURL = data.getPresentationUrl();
		if (presURL != null && !(presURL.toString().equals(""))) {
			SerializeUtil.serializeTag(serializer, helper.getDPWSNamespace(), helper.getDPWSElementPresentationURL(), (presURL.toString() == null ? "" : presURL.toString()));
		}
		// Adds UnknownElements
		serializeUnknownElements(serializer, data);
		// EndTag => dpws:thisModel
		serializer.endTag(helper.getDPWSNamespace(), helper.getDPWSElementThisModel());

	}

	private void serialize(QNameSet qnames, XmlSerializer serializer, String namespace, String element) throws IllegalArgumentException, IllegalStateException, IOException {
		QName[] qns = qnames.toArray();
		if (qns == null) return;

		int max = qns.length;
		if (qns.length > 0) {
			for (int j = 0; j < max; j++) {
				String prefix = serializer.getPrefix(qns[j].getNamespace(), false);
				if (prefix == null || prefix.equals("")) {
					serializer.setPrefix(qns[j].getPrefix(), qns[j].getNamespace());
				}
			}
			serializer.startTag(namespace, element);
			for (int j = 0; j < max; j++) {
				String prefix = serializer.getPrefix(qns[j].getNamespace(), false);
				if (prefix != null && !prefix.equals("")) {
					serializer.text(prefix + ":" + qns[j].getLocalPart());
				} else {
					serializer.text(qns[j].getLocalPart());
				}
				if (j < max) {
					serializer.text(" ");
				}
			}
			serializer.endTag(namespace, element);
		}
	}

	private void serialize(ProbeScopeSet scopes, XmlSerializer serializer, String namespace) throws IOException {
		if (!scopes.isEmpty()) {
			serializer.startTag(namespace, WSDConstants.WSD_ELEMENT_SCOPES);

			String matchBy = scopes.getMatchBy();
			if (matchBy != null) {
				serializer.attribute(namespace, WSDConstants.WSD_ATTR_MATCH_BY, matchBy);
			}

			HashMap unknownAttributes = scopes.getUnknownAttributes();
			if (unknownAttributes != null && !(unknownAttributes.isEmpty())) {
				for (Iterator it = unknownAttributes.entrySet().iterator(); it.hasNext();) {
					HashMap.Entry ent = (Entry) it.next();
					QName qname = (QName) ent.getKey();
					String value = (String) ent.getValue();
					serializer.attribute(qname.getNamespace(), qname.getLocalPart(), value);
				}
			}
			serializer.text(scopes.getScopesAsString());
			serializer.endTag(namespace, WSDConstants.WSD_ELEMENT_SCOPES);
		}
	}

	private void serialize(ScopeSet scopes, XmlSerializer serializer, String namespace) throws IOException {
		if (!scopes.isEmpty()) {
			serializer.startTag(namespace, WSDConstants.WSD_ELEMENT_SCOPES);

			HashMap unknownAttributes = scopes.getUnknownAttributes();
			if (unknownAttributes != null && !(unknownAttributes.isEmpty())) {
				for (Iterator it = unknownAttributes.entrySet().iterator(); it.hasNext();) {
					HashMap.Entry ent = (Entry) it.next();
					QName qname = (QName) ent.getKey();
					String value = (String) ent.getValue();
					serializer.attribute(qname.getNamespace(), qname.getLocalPart(), value);
				}
			}
			serializer.text(scopes.getScopesAsString());
			serializer.endTag(namespace, WSDConstants.WSD_ELEMENT_SCOPES);
		}
	}

	private void serialize(URISet uris, XmlSerializer serializer, String namespace) throws IllegalArgumentException, IllegalStateException, IOException {
		serializer.startTag(namespace, WSDConstants.WSD_ELEMENT_XADDRS);
		serializer.text(uris.toString() == null ? "" : uris.toString());
		serializer.endTag(namespace, WSDConstants.WSD_ELEMENT_XADDRS);
	}

	private void serialize(XAddressInfoSet xAdrInfoSet, XmlSerializer serializer, String namespace) throws IllegalArgumentException, IllegalStateException, IOException {
		serializer.startTag(namespace, WSDConstants.WSD_ELEMENT_XADDRS);
		serializer.text(xAdrInfoSet.toString() == null ? "" : xAdrInfoSet.toString());
		serializer.endTag(namespace, WSDConstants.WSD_ELEMENT_XADDRS);
	}

	private ConstantsHelper getConstantsHelper(Message message, ProtocolData protocolData) {
		return getConstantsHelper(message.getHeader(), protocolData);
	}

	private ConstantsHelper getConstantsHelper(SOAPHeader header, ProtocolData protocolData) {
		if (protocolData == null) {
			return null;
		}
		CommunicationManager comMan = DPWSFramework.getCommunicationManager(protocolData.getCommunicationManagerId());
		CommunicationUtil comUtil = comMan.getCommunicationUtil();
		ConstantsHelper helper = comUtil.getHelper(header.getProtocolInfo().getVersion());
		return helper;
	}

}
