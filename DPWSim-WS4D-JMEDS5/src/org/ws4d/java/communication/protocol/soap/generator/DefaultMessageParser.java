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
import org.ws4d.java.communication.CommunicationManager;
import org.ws4d.java.communication.CommunicationUtil;
import org.ws4d.java.communication.ProtocolData;
import org.ws4d.java.communication.VersionMismatchException;
import org.ws4d.java.constants.ConstantsHelper;
import org.ws4d.java.constants.DPWSConstants;
import org.ws4d.java.constants.DPWSConstants2006;
import org.ws4d.java.constants.MEXConstants;
import org.ws4d.java.constants.SOAPConstants;
import org.ws4d.java.constants.WSAConstants;
import org.ws4d.java.constants.WSAConstants2006;
import org.ws4d.java.constants.WSDConstants;
import org.ws4d.java.constants.WSDLConstants;
import org.ws4d.java.constants.WSEConstants;
import org.ws4d.java.constants.WSSecurityConstants;
import org.ws4d.java.io.xml.ElementHandler;
import org.ws4d.java.io.xml.ElementParser;
import org.ws4d.java.message.FaultMessage;
import org.ws4d.java.message.InvokeMessage;
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
import org.ws4d.java.service.Fault;
import org.ws4d.java.service.OperationDescription;
import org.ws4d.java.service.parameter.ParameterDefinition;
import org.ws4d.java.structures.ArrayList;
import org.ws4d.java.structures.DataStructure;
import org.ws4d.java.structures.HashMap;
import org.ws4d.java.structures.Iterator;
import org.ws4d.java.structures.List;
import org.ws4d.java.types.AppSequence;
import org.ws4d.java.types.AttributedURI;
import org.ws4d.java.types.CustomizeMDataHandler;
import org.ws4d.java.types.Delivery;
import org.ws4d.java.types.DiscoveryData;
import org.ws4d.java.types.EndpointReference;
import org.ws4d.java.types.EndpointReference2004;
import org.ws4d.java.types.EprInfo;
import org.ws4d.java.types.EprInfoSet;
import org.ws4d.java.types.Filter;
import org.ws4d.java.types.HostMData;
import org.ws4d.java.types.HostedMData;
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
import org.ws4d.java.types.XAddressInfo;
import org.ws4d.java.types.XAddressInfoSet;
import org.ws4d.java.util.Log;
import org.ws4d.java.wsdl.WSDL;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

class DefaultMessageParser extends MessageParser {

	public SOAPHeader parseSOAPHeader(ElementParser parser, ProtocolData protocolData, ConstantsHelper helper) throws XmlPullParserException, IOException, VersionMismatchException {
		return parseSOAPHeader(parser, helper, protocolData);
	}

	public HelloMessage parseHelloMessage(SOAPHeader header, ElementParser parser, ProtocolData protocolData, ConstantsHelper helper) throws XmlPullParserException, IOException {
		DiscoveryData discoveryData = new DiscoveryData();
		parse(discoveryData, parser, WSDConstants.WSD_ELEMENT_HELLO, helper, protocolData.getCommunicationManagerId());
		HelloMessage hello = new HelloMessage(header, discoveryData);
		if (hello.getHeader().getSignature() != null) {
			String[] potentialAlia = new String[hello.getXAddressInfoSet().size()];
			Iterator iter = hello.getXAddressInfoSet().iterator();
			for (int i = 0; iter.hasNext(); i++) {
				potentialAlia[i] = ((XAddressInfo)iter.next()).getXAddress().toString();
			}
			hello.getHeader().setValidated(DPWSFramework.getSecurityManager().validateMessage(hello.getHeader().getSignature(), protocolData, hello.getEndpointReference(), potentialAlia));
		}
		return hello;
	}

	public ByeMessage parseByeMessage(SOAPHeader header, ElementParser parser, ProtocolData protocolData, ConstantsHelper helper) throws XmlPullParserException, IOException {
		DiscoveryData discoveryData = new DiscoveryData();
		parse(discoveryData, parser, WSDConstants.WSD_ELEMENT_BYE, helper, protocolData.getCommunicationManagerId());
		ByeMessage bye = new ByeMessage(header, discoveryData);

		if (bye.getHeader().getSignature() != null) {
			String[] potentialAlia = new String[bye.getXAddressInfoSet().size()];
			Iterator iter = bye.getXAddressInfoSet().iterator();
			for (int i = 0; iter.hasNext(); i++) {
				potentialAlia[i] = ((XAddressInfo)iter.next()).getXAddress().toString();
			}
			bye.getHeader().setValidated(DPWSFramework.getSecurityManager().validateMessage(bye.getHeader().getSignature(), protocolData, bye.getEndpointReference(), potentialAlia));
		}
		return bye;
	}

	public ProbeMessage parseProbeMessage(SOAPHeader header, ElementParser parser, ProtocolData protocolData, ConstantsHelper helper) throws XmlPullParserException, IOException {
		ProbeMessage probeMessage = new ProbeMessage(header);

		parseUnknownAttributes(probeMessage, parser);

		while (parser.nextTag() != XmlPullParser.END_TAG) {
			String namespace = parser.getNamespace();
			String name = parser.getName();
			if (helper.getWSDNamespace().equals(namespace)) {
				if (WSDConstants.WSD_ELEMENT_TYPES.equals(name)) {
					probeMessage.setTypes(parseQNameSet(parser));
					String comManId = header.getCommunicationManagerID();
					CommunicationManager comMan = DPWSFramework.getCommunicationManager(comManId);
					CommunicationUtil comUtil = comMan.getCommunicationUtil();
					comUtil.changeIncomingProbe(probeMessage);
				} else if (WSDConstants.WSD_ELEMENT_SCOPES.equals(name)) {
					probeMessage.setScopes(parseNextProbeScopeSet(parser));
				} else {
					parseUnknownElement(probeMessage, parser, namespace, name);
				}
			} else {
				parseUnknownElement(probeMessage, parser, namespace, name);
			}
		}
		return probeMessage;
	}

	public ProbeMatchesMessage parseProbeMatchesMessage(SOAPHeader header, ElementParser parser, ProtocolData protocolData, ConstantsHelper helper) throws XmlPullParserException, IOException {
		ProbeMatchesMessage probeMatchesMessage = new ProbeMatchesMessage(header);

		parseUnknownAttributes(probeMatchesMessage, parser);

		while (parser.nextTag() != XmlPullParser.END_TAG) {
			String namespace = parser.getNamespace();
			String name = parser.getName();
			if (helper.getWSDNamespace().equals(namespace)) {
				if (WSDConstants.WSD_ELEMENT_PROBEMATCH.equals(name)) {
					ProbeMatch probeMatch = new ProbeMatch();
					parse(probeMatch, parser, WSDConstants.WSD_ELEMENT_PROBEMATCH, helper, protocolData.getCommunicationManagerId());
					probeMatchesMessage.addProbeMatch(probeMatch);
				} else {
					parseUnknownElement(probeMatchesMessage, parser, namespace, name);
				}
			} else {
				parseUnknownElement(probeMatchesMessage, parser, namespace, name);
			}
		}

		// FIXME only first EPR
		if (DPWSFramework.hasModule(DPWSFramework.SECURITY_MODULE) && probeMatchesMessage.getHeader().getSignature() != null) {
			if (probeMatchesMessage.getProbeMatch(0) != null) {
				ArrayList potentialAlia = new ArrayList();
				for (int i = 0; i < probeMatchesMessage.getProbeMatchCount(); i++) {
					Iterator iter = probeMatchesMessage.getProbeMatch(i).getXAddressInfoSet().iterator();
					for (int j = 0; iter.hasNext(); j++) {
						String str = ((XAddressInfo)iter.next()).getXAddress().toString();
						if (!potentialAlia.contains(str)) potentialAlia.add(str);
					}
				}

				Iterator iter = potentialAlia.iterator();
				String[] pA = new String[potentialAlia.size()];
				for (int i = 0; i < potentialAlia.size(); i++) {
					pA[i] = iter.next().toString();
				}
				probeMatchesMessage.setSecure(true);
				probeMatchesMessage.getHeader().setValidated(DPWSFramework.getSecurityManager().validateMessage(probeMatchesMessage.getHeader().getSignature(), protocolData, probeMatchesMessage.getProbeMatch(0).getEndpointReference(), pA));
			}
		}
		return probeMatchesMessage;
	}

	public ResolveMessage parseResolveMessage(SOAPHeader header, ElementParser parser, ProtocolData protocolData, ConstantsHelper helper) throws XmlPullParserException, IOException {
		ResolveMessage resolveMessage = new ResolveMessage(header);

		parseUnknownAttributes(resolveMessage, parser);

		int event = parser.nextTag();
		if (event == XmlPullParser.END_TAG) {
			throw new XmlPullParserException("Resolve is empty");
		}
		do {
			String namespace = parser.getNamespace();
			String name = parser.getName();
			if (helper.getWSANamespace().equals(namespace)) {
				if (WSAConstants.WSA_ELEM_ENDPOINT_REFERENCE.equals(name)) {
					resolveMessage.setEndpointReference(parseEndpointReference(helper.getDPWSVersion(), parser));
				} else {
					parseUnknownElement(resolveMessage, parser, namespace, name);
				}
			} else {
				parseUnknownElement(resolveMessage, parser, namespace, name);
			}
			event = parser.nextTag();
		} while (event != XmlPullParser.END_TAG);
		return resolveMessage;
	}

	public ResolveMatchesMessage parseResolveMatchesMessage(SOAPHeader header, ElementParser parser, ProtocolData protocolData, ConstantsHelper helper) throws XmlPullParserException, IOException {
		ResolveMatchesMessage resolveMatchesMessage = new ResolveMatchesMessage(header);

		parseUnknownAttributes(resolveMatchesMessage, parser);

		while (parser.nextTag() != XmlPullParser.END_TAG) {
			String namespace = parser.getNamespace();
			String name = parser.getName();
			if (helper.getWSDNamespace().equals(namespace)) {
				if (WSDConstants.WSD_ELEMENT_RESOLVEMATCH.equals(name)) {
					ResolveMatch resolveMatch = new ResolveMatch();
					parse(resolveMatch, parser, WSDConstants.WSD_ELEMENT_RESOLVEMATCH, helper, protocolData.getCommunicationManagerId());
					resolveMatchesMessage.setResolveMatch(resolveMatch);
				} else {
					parseUnknownElement(resolveMatchesMessage, parser, namespace, name);
				}
			} else {
				parseUnknownElement(resolveMatchesMessage, parser, namespace, name);
			}
		}

		if (resolveMatchesMessage.getHeader().getSignature() != null) {
			if (resolveMatchesMessage.getResolveMatch() != null) {
				resolveMatchesMessage.getHeader().setValidated(DPWSFramework.getSecurityManager().validateMessage(resolveMatchesMessage.getHeader().getSignature(), protocolData, resolveMatchesMessage.getResolveMatch().getEndpointReference(), null));
			}
		}
		return resolveMatchesMessage;
	}

	public InvokeMessage parseInvokeMessage(SOAPHeader header, ElementParser parser, ProtocolData protocolData, ConstantsHelper helper) throws XmlPullParserException, IOException {
		// TODO Auto-generated method stub
		return null;
	}

	public GetStatusMessage parseGetStatusMessage(SOAPHeader header, ElementParser parser, ProtocolData protocolData, ConstantsHelper helper) throws XmlPullParserException, IOException {
		GetStatusMessage getStatusMessage = new GetStatusMessage(header);
		parser.nextGenericElement(getStatusMessage);
		return getStatusMessage;
	}

	public GetStatusResponseMessage parseGetStatusResponseMessage(SOAPHeader header, ElementParser parser, ProtocolData protocolData, ConstantsHelper helper) throws XmlPullParserException, IOException {
		GetStatusResponseMessage getStatusResponseMessage = new GetStatusResponseMessage(header);

		while (parser.nextTag() != XmlPullParser.END_TAG) {
			String namespace = parser.getNamespace();
			String name = parser.getName();
			if (WSEConstants.WSE_NAMESPACE_NAME.equals(namespace)) {
				if (WSEConstants.WSE_ELEM_EXPIRES.equals(name)) {
					getStatusResponseMessage.setExpires(parser.nextText());
				} else {
					parseUnknownElement(getStatusResponseMessage, parser, namespace, name);
				}
			} else {
				parseUnknownElement(getStatusResponseMessage, parser, namespace, name);
			}
		}
		return getStatusResponseMessage;
	}

	public RenewMessage parseRenewMessage(SOAPHeader header, ElementParser parser, ProtocolData protocolData, ConstantsHelper helper) throws XmlPullParserException, IOException {
		RenewMessage renewMessage = new RenewMessage(header);
		parseUnknownAttributes(renewMessage, parser);

		while (parser.nextTag() != XmlPullParser.END_TAG) {
			String namespace = parser.getNamespace();
			String name = parser.getName();
			if (WSEConstants.WSE_NAMESPACE_NAME.equals(namespace)) {
				if (WSEConstants.WSE_ELEM_EXPIRES.equals(name)) {
					renewMessage.setExpires(parser.nextText());
				} else {
					parseUnknownElement(renewMessage, parser, namespace, name);
				}
			} else {
				parseUnknownElement(renewMessage, parser, namespace, name);
			}
		}
		return renewMessage;
	}

	public RenewResponseMessage parseRenewResponseMessage(SOAPHeader header, ElementParser parser, ProtocolData protocolData, ConstantsHelper helper) throws XmlPullParserException, IOException {
		RenewResponseMessage renewResponseMessage = new RenewResponseMessage(header);

		while (parser.nextTag() != XmlPullParser.END_TAG) {
			String namespace = parser.getNamespace();
			String name = parser.getName();
			if (WSEConstants.WSE_NAMESPACE_NAME.equals(namespace)) {
				if (WSEConstants.WSE_ELEM_EXPIRES.equals(name)) {
					renewResponseMessage.setExpires(parser.nextText());
				} else {
					parseUnknownElement(renewResponseMessage, parser, namespace, name);
				}
			} else {
				parseUnknownElement(renewResponseMessage, parser, namespace, name);
			}
		}
		return renewResponseMessage;
	}

	public SubscribeMessage parseSubscribeMessage(SOAPHeader header, ElementParser parser, ProtocolData protocolData, ConstantsHelper helper) throws XmlPullParserException, IOException {
		SubscribeMessage subscribeMessage = new SubscribeMessage(header);

		parseUnknownAttributes(subscribeMessage, parser);

		int event = parser.nextTag();
		if (event == XmlPullParser.END_TAG) {
			throw new XmlPullParserException("Subscribe is empty");
		}
		do {
			String namespace = parser.getNamespace();
			String name = parser.getName();
			CommunicationManager comMan = DPWSFramework.getCommunicationManager(header.getCommunicationManagerID());
			CommunicationUtil comUtil = comMan.getCommunicationUtil();

			if (WSEConstants.WSE_NAMESPACE_NAME.equals(namespace)) {
				if (WSEConstants.WSE_ELEM_ENDTO.equals(name)) {
					subscribeMessage.setEndTo(parseEndpointReference((header.getProtocolInfo()).getVersion(), parser));
				} else if (WSEConstants.WSE_ELEM_DELIVERY.equals(name)) {
					subscribeMessage.setDelivery(parseDelivery(parser, comUtil.getHelper(header.getProtocolInfo().getVersion()), header.getCommunicationManagerID()));
				} else if (WSEConstants.WSE_ELEM_EXPIRES.equals(name)) {
					subscribeMessage.setExpires(parser.nextText());
				} else if (WSEConstants.WSE_ELEM_FILTER.equals(name)) {
					subscribeMessage.setFilter(parseFilter(parser, header.getCommunicationManagerID()));
				} else {
					parseUnknownElement(subscribeMessage, parser, namespace, name);
				}
			} else {
				parseUnknownElement(subscribeMessage, parser, namespace, name);
			}
			event = parser.nextTag();
		} while (event != XmlPullParser.END_TAG);
		return subscribeMessage;
	}

	public SubscribeResponseMessage parseSubscribeResponseMessage(SOAPHeader header, ElementParser parser, ProtocolData protocolData, ConstantsHelper helper) throws XmlPullParserException, IOException {
		SubscribeResponseMessage subscribeResponseMessage = new SubscribeResponseMessage(header);

		parseUnknownAttributes(subscribeResponseMessage, parser);

		int event = parser.nextTag();
		if (event == XmlPullParser.END_TAG) {
			throw new XmlPullParserException("SubscribeResponse is empty");
		}
		do {
			String namespace = parser.getNamespace();
			String name = parser.getName();
			if (WSEConstants.WSE_NAMESPACE_NAME.equals(namespace)) {
				if (WSEConstants.WSE_ELEM_SUBSCRIPTIONMANAGER.equals(name)) {
					subscribeResponseMessage.setSubscriptionManager(parseEndpointReference((header.getProtocolInfo()).getVersion(), parser));
				} else if (WSEConstants.WSE_ELEM_EXPIRES.equals(name)) {
					subscribeResponseMessage.setExpires(parser.nextText());
				} else {
					parseUnknownElement(subscribeResponseMessage, parser, namespace, name);
				}
			} else {
				parseUnknownElement(subscribeResponseMessage, parser, namespace, name);
			}
			event = parser.nextTag();
		} while (event != XmlPullParser.END_TAG);
		return subscribeResponseMessage;
	}

	public SubscriptionEndMessage parseSubscriptionEndMessage(SOAPHeader header, ElementParser parser, ProtocolData protocolData, ConstantsHelper helper) throws XmlPullParserException, IOException {
		SubscriptionEndMessage subscriptionEndMessage = new SubscriptionEndMessage(header);

		parseUnknownAttributes(subscriptionEndMessage, parser);

		int event = parser.nextTag();
		if (event == XmlPullParser.END_TAG) {
			throw new XmlPullParserException("SubscriptionEnd is empty");
		}
		do {
			String namespace = parser.getNamespace();
			String name = parser.getName();
			if (WSEConstants.WSE_NAMESPACE_NAME.equals(namespace)) {
				if (WSEConstants.WSE_ELEM_SUBSCRIPTIONMANAGER.equals(name)) {
					subscriptionEndMessage.setSubscriptionManager(parseEndpointReference((header.getProtocolInfo()).getVersion(), parser));
				} else if (WSEConstants.WSE_ELEM_STATUS.equals(name)) {
					subscriptionEndMessage.setStatus(new URI(parser.nextText().trim()));
				} else if (WSEConstants.WSE_ELEM_REASON.equals(name)) {
					subscriptionEndMessage.setReason(parser.nextLocalizedString());
				} else {
					parseUnknownElement(subscriptionEndMessage, parser, namespace, name);
				}
			} else {
				parseUnknownElement(subscriptionEndMessage, parser, namespace, name);
			}
			event = parser.nextTag();
		} while (event != XmlPullParser.END_TAG);
		return subscriptionEndMessage;
	}

	public UnsubscribeMessage parseUnsubscribeMessage(SOAPHeader header, ElementParser parser, ProtocolData protocolData, ConstantsHelper helper) throws XmlPullParserException, IOException {
		UnsubscribeMessage unsubscribeMessage = new UnsubscribeMessage(header);
		parser.nextGenericElement(unsubscribeMessage);
		return unsubscribeMessage;
	}

	public UnsubscribeResponseMessage parseUnsubscribeResponseMessage(SOAPHeader header, ElementParser parser, ProtocolData protocolData, ConstantsHelper helper) throws XmlPullParserException, IOException {
		return new UnsubscribeResponseMessage(header);
	}

	public GetMessage parseGetMessage(SOAPHeader header, ElementParser parser, ProtocolData protocolData, ConstantsHelper helper) throws XmlPullParserException, IOException {
		return new GetMessage(header);
	}

	public GetResponseMessage parseGetResponseMessage(SOAPHeader header, ElementParser parser, ProtocolData protocolData, ConstantsHelper helper) throws XmlPullParserException, IOException {
		
		GetResponseMessage getResponseMessage = new GetResponseMessage(header);
		System.out.println(getResponseMessage);
		parseUnknownAttributes(getResponseMessage, parser);

		int event = parser.nextTag();
		if (event == XmlPullParser.END_TAG) {
			throw new XmlPullParserException("GetResponse is empty");
		}
		do {
			String namespace = parser.getNamespace();
			String name = parser.getName();
			if (MEXConstants.WSX_NAMESPACE_NAME.equals(namespace)) {
				if (MEXConstants.WSX_ELEM_METADATASECTION.equals(name)) {
					// get attribute Dialect and decide upon it
					String dialect = parser.getAttributeValue(null, MEXConstants.WSX_ELEM_DIALECT);
					if (helper.getMetadataDialectThisModel().equals(dialect)) {
						getResponseMessage.setThisModel(parseThisModelMData(parser, helper));
					} else if (helper.getMetadataDialectThisDevice().equals(dialect)) {
						getResponseMessage.setThisDevice(parseThisDeviceMData(parser, helper));
					} else if (helper.getMetatdataDialectRelationship().equals(dialect)) {
						try {
							RelationshipMData data = parseRelationshipMData(parser, helper, header.getCommunicationManagerID());
							getResponseMessage.addRelationship(data, helper);
						} catch (VersionMismatchException e) {
							Log.printStackTrace(e);
						}
					}
					else {
						//if the parser finds an element which not fits on one of the defined dialects 
						//then it look whether the user has register his own ElementHandler
						// if not a generic ElementHandler will be start
						parser.next();
						QName custom = new QName(parser.getName(),parser.getNamespace());
						Object handler = parser.chainHandler(custom, false);
						if(handler!=null)
						{
							Object data = ((ElementHandler)handler).handleElement(custom, parser);
							getResponseMessage.setCustomMData((String) data);
						}
						else{
						Object data = CustomizeMDataHandler.getInstance().handleElement(custom, parser);
						getResponseMessage.setCustomMData( (String) data);
						}
					}
				} else {
					parseUnknownElement(getResponseMessage, parser, namespace, name);
				}
			} else {
				parseUnknownElement(getResponseMessage, parser, namespace, name);
			}
			event = parser.nextTag();
		} while (event != XmlPullParser.END_TAG);
		return getResponseMessage;
	}

	public GetMetadataMessage parseGetMetadataMessage(SOAPHeader header, ElementParser parser, ProtocolData protocolData, ConstantsHelper helper) throws XmlPullParserException, IOException {
		GetMetadataMessage getMetadataMessage = new GetMetadataMessage(header);

		parseUnknownAttributes(getMetadataMessage, parser);

		while (parser.nextTag() != XmlPullParser.END_TAG) {
			String namespace = parser.getNamespace();
			String name = parser.getName();
			if (MEXConstants.WSX_NAMESPACE_NAME.equals(namespace)) {
				if (MEXConstants.WSX_ELEM_DIALECT.equals(name)) {
					getMetadataMessage.setDialect(new URI(parser.nextText().trim()));
				} else if (MEXConstants.WSX_ELEM_IDENTIFIER.equals(name)) {
					getMetadataMessage.setIdentifier(new URI(parser.nextText().trim()));
				} else {
					parseUnknownElement(getMetadataMessage, parser, namespace, name);
				}
			} else {
				parseUnknownElement(getMetadataMessage, parser, namespace, name);
			}
		}
		return getMetadataMessage;
	}

	public GetMetadataResponseMessage parseGetMetadataResponseMessage(SOAPHeader header, ElementParser parser, ProtocolData protocolData, ConstantsHelper helper) throws XmlPullParserException, IOException {
		GetMetadataResponseMessage getMetadataResponseMessage = new GetMetadataResponseMessage(header);

		parseUnknownAttributes(getMetadataResponseMessage, parser);

		// go to first wsx:MetadataSection element
		int event = parser.nextTag();
		if (event == XmlPullParser.END_TAG) {
			throw new XmlPullParserException("GetMetadataResponse is empty");
		}
		do {
			String namespace = parser.getNamespace();
			String name = parser.getName();
			if (MEXConstants.WSX_NAMESPACE_NAME.equals(namespace)) {
				if (MEXConstants.WSX_ELEM_METADATASECTION.equals(name)) {
					// get attribute Dialect and decide upon it
					String dialect = parser.getAttributeValue(null, MEXConstants.WSX_ELEM_DIALECT);
					if (MEXConstants.WSX_DIALECT_WSDL.equals(dialect)) {
						parser.nextTag(); // go to child element
						namespace = parser.getNamespace();
						name = parser.getName();
						if (MEXConstants.WSX_NAMESPACE_NAME.equals(namespace)) {
							if (MEXConstants.WSX_ELEM_METADATAREFERENCE.equals(name)) {
								getMetadataResponseMessage.addMetadataReference(parseEndpointReference(helper.getDPWSVersion(), parser));
							} else if (MEXConstants.WSX_ELEM_LOCATION.equals(name)) {
								getMetadataResponseMessage.addMetadataLocation(new URI(parser.nextText().trim()));
							}
						} else if (WSDLConstants.WSDL_NAMESPACE_NAME.equals(namespace)) {
							if (WSDLConstants.WSDL_ELEM_DEFINITIONS.equals(name)) {
								getMetadataResponseMessage.addWSDL(WSDL.parse(new ElementParser(parser)));
							}
						}
						// go to closing child
						parser.nextTag();
					} else if (helper.getMetatdataDialectRelationship().equals(dialect)) {
						try {
							RelationshipMData data = parseRelationshipMData(parser, helper, header.getCommunicationManagerID());
							getMetadataResponseMessage.addRelationship(data, helper);
						} catch (VersionMismatchException e) {
							Log.printStackTrace(e);
						}

					} else {
						// unknown metadata dialect
						/*
						 * what about XML Schema and/or WS-Policy dialects? and
						 * what about embedded metadata elements, like
						 * wsdl:definitions or xs:schema? these all could be
						 * handled here, if we want it someday...
						 */
						parseUnknownElement(getMetadataResponseMessage, parser, namespace, name);
					}
				} else {
					parseUnknownElement(getMetadataResponseMessage, parser, namespace, name);
				}
			} else {
				parseUnknownElement(getMetadataResponseMessage, parser, namespace, name);
			}
			event = parser.nextTag();
		} while (event != XmlPullParser.END_TAG);
		return getMetadataResponseMessage;
	}

	public FaultMessage parseFaultMessage(SOAPHeader header, ElementParser parser, ProtocolData protocolData, ConstantsHelper helper, String actionName, OperationDescription op) throws XmlPullParserException, IOException {
		FaultMessage faultMessage = new FaultMessage(header);

		Fault fault = null;
		if (op != null) {
			Iterator it = op.getFaults();
			while (it.hasNext()) {
				fault = (Fault) it.next();
				if (actionName != null && actionName.equals(fault.getAction())) {
					break;
				}
			}
		}

		parser.handleUnknownAttributes(faultMessage);

		int event = parser.nextTag();
		if (event == XmlPullParser.END_TAG) {
			throw new XmlPullParserException("Fault is empty");
		}
		do {
			String namespace = parser.getNamespace();
			String name = parser.getName();
			if (SOAPConstants.SOAP12_NAMESPACE_NAME.equals(namespace)) {
				if (SOAPConstants.SOAP_ELEM_CODE.equals(name)) {
					parseCode(faultMessage, parser);
				} else if (SOAPConstants.SOAP_ELEM_REASON.equals(name)) {
					faultMessage.setReason(nextReason(parser));
				} else if (SOAPConstants.SOAP_ELEM_DETAIL.equals(name)) {
					// go to content of soap:Detail
					if (parser.getEventType() == XmlPullParser.START_TAG) {
						if (fault != null) {
							parser.nextTag();
							faultMessage.setDetail(ParameterDefinition.parse(parser, fault.getElement(), op));
						} else {
							parser.addUnknownElement(faultMessage, namespace, name);
						}

					}
					// parser.nextTag();
				} else {
					parser.addUnknownElement(faultMessage, namespace, name);
				}
			} else {
				parser.addUnknownElement(faultMessage, namespace, name);
			}
			event = parser.nextTag();
		} while (event != XmlPullParser.END_TAG);
		return faultMessage;
	}

	private void parseCode(FaultMessage faultMessage, ElementParser parser) throws XmlPullParserException, IOException {
		int event = parser.nextTag();
		if (event == XmlPullParser.END_TAG) {
			throw new XmlPullParserException("Code is empty");
		}
		do {
			String namespace = parser.getNamespace();
			String name = parser.getName();
			if (SOAPConstants.SOAP12_NAMESPACE_NAME.equals(namespace)) {
				if (SOAPConstants.SOAP_ELEM_VALUE.equals(name)) {
					faultMessage.setCode(parser.nextQName());
				} else if (SOAPConstants.SOAP_ELEM_SUBCODE.equals(name)) {
					int event2 = parser.nextTag();
					if (event2 == XmlPullParser.END_TAG) {
						throw new XmlPullParserException("Subcode is empty");
					}
					do {
						String namespace2 = parser.getNamespace();
						String name2 = parser.getName();
						if (SOAPConstants.SOAP12_NAMESPACE_NAME.equals(namespace2)) {
							if (SOAPConstants.SOAP_ELEM_VALUE.equals(name2)) {
								faultMessage.setSubcode(parser.nextQName());
							} else if (SOAPConstants.SOAP_ELEM_SUBCODE.equals(name)) {
								int event3 = parser.nextTag();
								if (event3 == XmlPullParser.END_TAG) {
									throw new XmlPullParserException("Subcode is empty");
								}
								do {
									String namespace3 = parser.getNamespace();
									String name3 = parser.getName();
									if (SOAPConstants.SOAP12_NAMESPACE_NAME.equals(namespace3)) {
										if (SOAPConstants.SOAP_ELEM_VALUE.equals(name3)) {
											faultMessage.setSubsubcode(parser.nextQName());
										} else if (SOAPConstants.SOAP_ELEM_SUBCODE.equals(name3)) {
											// void, enough recursion
										}
									}
									event3 = parser.nextTag();
								} while (event3 != XmlPullParser.END_TAG);
							}
						}
						event2 = parser.nextTag();
					} while (event2 != XmlPullParser.END_TAG);
				}
			}
			event = parser.nextTag();
		} while (event != XmlPullParser.END_TAG);
	}

	private DataStructure nextReason(ElementParser parser) throws XmlPullParserException, IOException {
		List reason = new ArrayList();
		int event = parser.nextTag();
		if (event == XmlPullParser.END_TAG) {
			throw new XmlPullParserException("Reason is empty");
		}
		do {
			String namespace = parser.getNamespace();
			String name = parser.getName();
			if (SOAPConstants.SOAP12_NAMESPACE_NAME.equals(namespace)) {
				if (SOAPConstants.SOAP_ELEM_TEXT.equals(name)) {
					reason.add(parser.nextLocalizedString());
				}
			}
			event = parser.nextTag();
		} while (event != XmlPullParser.END_TAG);
		return reason;
	}

	private SOAPHeader parseSOAPHeader(ElementParser parser, ConstantsHelper helper, ProtocolData pd) throws XmlPullParserException, IOException, VersionMismatchException {

		String communicatonManagerID = pd.getCommunicationManagerId();
		SOAPHeader header = new SOAPHeader(communicatonManagerID);
		parser.handleUnknownAttributes(header);

		int event = parser.nextTag();
		if (event == XmlPullParser.END_TAG) {
			throw new XmlPullParserException("SOAP Header is empty");
		}
		do {
			CommunicationManager comMan = DPWSFramework.getCommunicationManager(communicatonManagerID);
			String namespace = parser.getNamespace();
			String name = parser.getName();
			if (comMan.supportsAddressingNamespace(header, namespace, pd)) {
				parseAddressingHeader(parser, header, name, namespace);
			} else if (WSSecurityConstants.XML_SOAP_DISCOVERY.equals(namespace)) {
				if (WSSecurityConstants.COMPACT_SECURITY.equals(name) && DPWSFramework.hasModule(DPWSFramework.SECURITY_MODULE)) {
					parser.nextTag();
					for (int i = parser.getAttributeCount() - 1; i >= 0; i--) {
						if (parser.getAttributeName(i).equals(WSSecurityConstants.COMPACT_SIG)) {
							header.setSignature(DPWSFramework.getSecurityManager().decode(parser.getAttributeValue(i)));
						}
					}
					// closing security tag and header tag
					parser.nextTag();
					parser.nextTag();
				} else if (WSDConstants.WSD_ELEMENT_APPSEQUENCE.equals(name)) {
					header.setAppSequence(AppSequence.parse(parser, helper));
				} else {
					parseUnknownElement(header, parser, namespace, name);
				}
			} else if (WSDConstants.WSD_NAMESPACE_NAME.equals(namespace)) {
				if (WSDConstants.WSD_ELEMENT_APPSEQUENCE.equals(name)) {
					header.setAppSequence(AppSequence.parse(parser, helper));
				} else {
					parseUnknownElement(header, parser, namespace, name);
				}
			} else if (WSEConstants.WSE_NAMESPACE_NAME.equals(namespace)) {
				if (WSEConstants.WSE_ELEM_IDENTIFIER.equals(name)) {
					header.setWseIdentifier(parser.nextText().trim());
				} else {
					parseUnknownElement(header, parser, namespace, name);
				}
			} else {
				parseUnknownElement(header, parser, namespace, name);
			}
			event = parser.nextTag();
		} while (event != XmlPullParser.END_TAG);

		if (Log.isDebug()) Log.debug("<I> Incoming " + header.getProtocolInfo().getDisplayName() + " Message, Action: " + header.getAction() + ", Id: " + header.getMessageId(), Log.DEBUG_LAYER_FRAMEWORK);

		return header;
	}

	private void parseAddressingHeader(ElementParser parser, SOAPHeader header, String name, String namespace) throws XmlPullParserException, IOException {
		if (WSAConstants.WSA_ELEM_ACTION.equals(name)) {
			header.setAction(AttributedURI.parse(parser));
		} else if (WSAConstants.WSA_ELEM_MESSAGE_ID.equals(name)) {
			header.setMessageId(AttributedURI.parse(parser));
		} else if (WSAConstants.WSA_ELEM_RELATESTO.equals(name)) {
			header.setRelatesTo(AttributedURI.parse(parser));
		} else if (WSAConstants.WSA_ELEM_REPLY_TO.equals(name)) {
			header.setReplyTo(parseEndpointReference((header.getProtocolInfo()).getVersion(), parser));
		} else if (WSAConstants.WSA_ELEM_TO.equals(name)) {
			header.setTo(AttributedURI.parse(parser));
		} else {
			parseUnknownElement(header, parser, namespace, name);
		}
	}

	private RelationshipMData parseRelationshipMData(ElementParser parser, ConstantsHelper helper, String communicationManagerID) throws XmlPullParserException, IOException, VersionMismatchException {
		parser.nextTag(); // go to Relationship

		// get attribute Type and decide upon it
		String type = parser.getAttributeValue(null, helper.getDPWSAttributeRelationshipType());
		if (helper.getMetadataRelationshipHostingType().equals(type)) {
			RelationshipMData relationship = new RelationshipMData();
			relationship.setType(new URI(helper.getMetadataRelationshipHostingType()));

			int event = parser.nextTag();
			if (event == XmlPullParser.END_TAG) {
				throw new XmlPullParserException("Relationship is empty");
			}
			DataStructure hosted = null;
			do {
				String namespace = parser.getNamespace();
				String name = parser.getName();
				if (helper.getDPWSNamespace().equals(namespace)) {
					if (helper.getDPWSElementRelationshipHost().equals(name)) {
						relationship.setHost(parseHostMData(parser, helper, communicationManagerID));
					} else if (helper.getDPWSElementRelationshipHosted().equals(name)) {
						if (hosted == null) {
							hosted = new ArrayList();
						}
						hosted.add(parseHostedMData(parser, helper, communicationManagerID));
					} else {
						parseUnknownElement(relationship, parser, namespace, name);
					}
				} else {
					parseUnknownElement(relationship, parser, namespace, name);
				}
				event = parser.nextTag();
			} while (event != XmlPullParser.END_TAG);
			if (hosted != null) {
				relationship.setHosted(hosted);
			}

			parser.nextTag(); // go to closing MetadataSection
			return relationship;
		} else {
			// wrong type
			throw new VersionMismatchException("Wrong Type Attribute", VersionMismatchException.TYPE_WRONG_DPWS_VERSION);
		}
	}

	private void parse(DiscoveryData data, ElementParser parser, String displayName, ConstantsHelper helper, String communicationManagerId) throws XmlPullParserException, IOException {
		parseUnknownAttributes(data, parser);

		int event = parser.nextTag();
		if (event == XmlPullParser.END_TAG) {
			throw new XmlPullParserException(displayName + " is empty");
		}
		do {
			String namespace = parser.getNamespace();
			String name = parser.getName();
			if (helper.getWSANamespace().equals(namespace)) {
				if (WSAConstants.WSA_ELEM_ENDPOINT_REFERENCE.equals(name)) {
					data.setEndpointReference(parseEndpointReference(helper.getDPWSVersion(), parser));
				} else {
					parseUnknownElement(data, parser, namespace, name);
				}
			} else if (helper.getWSDNamespace().equals(namespace)) {
				if (WSDConstants.WSD_ELEMENT_TYPES.equals(name)) {
					data.setTypes(parseQNameSet(parser));
				} else if (WSDConstants.WSD_ELEMENT_SCOPES.equals(name)) {
					data.setScopes(parseScopeSet(parser));
				} else if (WSDConstants.WSD_ELEMENT_XADDRS.equals(name)) {
					data.setXAddresInfoSet(parseXAddressInfoSet(parser, communicationManagerId));
				} else if (WSDConstants.WSD_ELEMENT_METADATAVERSION.equals(name)) {
					String value = parser.nextText().trim();
					long metadataVersion = 0L;
					try {
						metadataVersion = Long.parseLong(value);
					} catch (NumberFormatException e) {
						throw new XmlPullParserException(displayName + "/MetadataVersion is not a number: " + value);
					}
					data.setMetadataVersion(metadataVersion);
				} else {
					parseUnknownElement(data, parser, namespace, name);
				}
			} else {
				parseUnknownElement(data, parser, namespace, name);
			}
			event = parser.nextTag();
		} while (event != XmlPullParser.END_TAG);
	}

	private QNameSet parseQNameSet(ElementParser parser) throws XmlPullParserException, IOException {
		QNameSet qNameSet = new QNameSet();
		String value = parser.nextText();
		int pos1 = -1;
		int pos2 = pos1;
		do {
			pos1 = ElementParser.nextNonWhiteSpace(value, pos1);
			if (pos1 == -1) {
				break;
			}
			pos2 = ElementParser.nextWhiteSpace(value, pos1);
			if (pos2 == -1) {
				pos2 = value.length();
			}
			String rawQName = value.substring(pos1, pos2);
			qNameSet.add(parser.createQName(rawQName));
			pos1 = pos2;
		} while (pos1 != -1);
		return qNameSet;
	}

	private ScopeSet parseScopeSet(ElementParser parser) throws XmlPullParserException, IOException {
		ScopeSet scopes = new ScopeSet();
		String value = parser.nextText();
		int pos1 = -1;
		int pos2 = pos1;
		do {
			pos1 = ElementParser.nextNonWhiteSpace(value, pos1);
			if (pos1 == -1) {
				break;
			}
			pos2 = ElementParser.nextWhiteSpace(value, pos1);
			if (pos2 == -1) {
				pos2 = value.length();
			}
			String uri = value.substring(pos1, pos2);
			scopes.addScope(uri);
			pos1 = pos2;
		} while (pos1 != -1);

		return scopes;
	}

	private ProbeScopeSet parseNextProbeScopeSet(ElementParser parser) throws XmlPullParserException, IOException {
		ProbeScopeSet scopeSet = new ProbeScopeSet();
		int attributeCount = parser.getAttributeCount();
		String matchBy = WSDConstants.WSD_MATCHING_RULE_DEFAULT;
		for (int i = 0; i < attributeCount; i++) {
			String namespace = parser.getAttributeNamespace(i);
			String name = parser.getAttributeName(i);
			String value = parser.getAttributeValue(i);
			if ("".equals(namespace) && WSDConstants.WSD_ATTR_MATCH_BY.equals(name)) {
				matchBy = value;
			} else {
				scopeSet.addUnknownAttribute(new QName(name, namespace), value);
			}
		}
		scopeSet.setMatchBy(matchBy);
		scopeSet.addAll(parseScopeSet(parser));
		return scopeSet;
	}

	private URISet parseURISet(ElementParser parser) throws XmlPullParserException, IOException {
		URISet uriSet = new URISet();
		String value = parser.nextText();
		int pos1 = -1;
		int pos2 = pos1;
		do {
			pos1 = ElementParser.nextNonWhiteSpace(value, pos1);
			if (pos1 == -1) {
				break;
			}
			pos2 = ElementParser.nextWhiteSpace(value, pos1);
			if (pos2 == -1) {
				pos2 = value.length();
			}
			String uri = value.substring(pos1, pos2);
			uriSet.add(new URI(uri));
			pos1 = pos2;
		} while (pos1 != -1);
		return uriSet;
	}

	private XAddressInfoSet parseXAddressInfoSet(ElementParser parser, String comManId) throws XmlPullParserException, IOException {
		XAddressInfoSet xAdrInfoSet = new XAddressInfoSet();
		String value = parser.nextText();
		int pos1 = -1;
		int pos2 = pos1;
		do {
			pos1 = ElementParser.nextNonWhiteSpace(value, pos1);
			if (pos1 == -1) {
				break;
			}
			pos2 = ElementParser.nextWhiteSpace(value, pos1);
			if (pos2 == -1) {
				pos2 = value.length();
			}
			String uri = value.substring(pos1, pos2);
			xAdrInfoSet.add(new XAddressInfo(new URI(uri), comManId));
			pos1 = pos2;
		} while (pos1 != -1);
		return xAdrInfoSet;
	}

	private Delivery parseDelivery(ElementParser parser, ConstantsHelper helper, String communicationManagerId) throws XmlPullParserException, IOException {
		Delivery delivery = new Delivery();

		int attributeCount = parser.getAttributeCount();
		for (int i = 0; i < attributeCount; i++) {
			String namespace = parser.getAttributeNamespace(i);
			String name = parser.getAttributeName(i);
			String value = parser.getAttributeValue(i);
			if ("".equals(namespace) && WSEConstants.WSE_ATTR_DELIVERY_MODE.equals(name)) {
				delivery.setMode(new URI(value));
			} else {
				delivery.addUnknownAttribute(new QName(name, namespace), value);
			}
		}

		while (parser.nextTag() != XmlPullParser.END_TAG) {
			String namespace = parser.getNamespace();
			String name = parser.getName();
			if (WSEConstants.WSE_NAMESPACE_NAME.equals(namespace)) {
				if (WSEConstants.WSE_ELEM_NOTIFYTO.equals(name)) {
					delivery.setNotifyTo(parseEndpointReference(helper.getDPWSVersion(), parser));
				} else {
					parseUnknownElement(delivery, parser, namespace, name);
				}
			} else {
				parseUnknownElement(delivery, parser, namespace, name);
			}
		}
		return delivery;
	}

	private Filter parseFilter(ElementParser parser, String comManId) throws XmlPullParserException, IOException {
		Filter filter = new Filter();

		int attributeCount = parser.getAttributeCount();
		for (int i = 0; i < attributeCount; i++) {
			String namespace = parser.getAttributeNamespace(i);
			String name = parser.getAttributeName(i);
			String value = parser.getAttributeValue(i);
			if ("".equals(namespace) && WSEConstants.WSE_ATTR_FILTER_DIALECT.equals(name)) {
				filter.setDialect(new URI(value));
			} else {
				filter.addUnknownAttribute(new QName(name, namespace), value);
			}
		}
		filter.setActions(parseURISet(parser));

		return filter;
	}

	private ThisModelMData parseThisModelMData(ElementParser parser, ConstantsHelper helper) throws XmlPullParserException, IOException {
		ThisModelMData thisModel = new ThisModelMData();

		parser.nextTag(); // go to ThisModel

		parseUnknownAttributes(thisModel, parser);

		int event = parser.nextTag();
		if (event == XmlPullParser.END_TAG) {
			throw new XmlPullParserException("ThisModel is empty");
		}
		do {
			String namespace = parser.getNamespace();
			String name = parser.getName();
			if (helper.getDPWSNamespace().equals(namespace)) {
				if (helper.getDPWSElementManufacturer().equals(name)) {
					thisModel.addManufacturerName(parser.nextLocalizedString());
				} else if (helper.getDPWSElementManufacturerURL().equals(name)) {
					thisModel.setManufacturerUrl(new URI(parser.nextText().trim()));
				} else if (helper.getDPWSElementModelName().equals(name)) {
					thisModel.addModelName(parser.nextLocalizedString());
				} else if (helper.getDPWSElementModelNumber().equals(name)) {
					thisModel.setModelNumber(parser.nextText().trim());
				} else if (helper.getDPWSElementModelURL().equals(name)) {
					thisModel.setModelUrl(new URI(parser.nextText().trim()));
				} else if (helper.getDPWSElementPresentationURL().equals(name)) {
					thisModel.setPresentationUrl(new URI(parser.nextText().trim()));
				} else {
					parseUnknownElement(thisModel, parser, namespace, name);
				}
			} else {
				parseUnknownElement(thisModel, parser, namespace, name);
			}
			event = parser.nextTag();
		} while (event != XmlPullParser.END_TAG);

		parser.nextTag(); // go to closing MetadataSection
		return thisModel;
	}

	private ThisDeviceMData parseThisDeviceMData(ElementParser parser, ConstantsHelper helper) throws XmlPullParserException, IOException {
		ThisDeviceMData thisDevice = new ThisDeviceMData();

		parser.nextTag(); // go to ThisDevice

		parseUnknownAttributes(thisDevice, parser);

		int event = parser.nextTag();
		if (event == XmlPullParser.END_TAG) {
			throw new XmlPullParserException("ThisDevice is empty");
		}
		do {
			String namespace = parser.getNamespace();
			String name = parser.getName();
			if (helper.getDPWSNamespace().equals(namespace)) {
				if (helper.getDPWSElementFriendlyName().equals(name)) {
					thisDevice.addFriendlyName(parser.nextLocalizedString());
				} else if (helper.getDPWSElementFirmwareVersion().equals(name)) {
					thisDevice.setFirmwareVersion(parser.nextText().trim());
				} else if (helper.getDPWSElementSerialnumber().equals(name)) {
					thisDevice.setSerialNumber(parser.nextText().trim());
				} else {
					parseUnknownElement(thisDevice, parser, namespace, name);
				}
			} else {
				parseUnknownElement(thisDevice, parser, namespace, name);
			}
			event = parser.nextTag();
		} while (event != XmlPullParser.END_TAG);

		parser.nextTag(); // go to closing MetadataSection
		return thisDevice;
	}

	private HostMData parseHostMData(ElementParser parser, ConstantsHelper helper, String communicationManagerId) throws XmlPullParserException, IOException {
		HostMData host = new HostMData();

		parseUnknownAttributes(host, parser);

		int event = parser.nextTag();
		if (event == XmlPullParser.END_TAG) {
			throw new XmlPullParserException("Host is empty");
		}
		do {
			String namespace = parser.getNamespace();
			String name = parser.getName();
			if (helper.getWSANamespace().equals(namespace)) {
				if (WSAConstants.WSA_ELEM_ENDPOINT_REFERENCE.equals(name)) {
					host.setEndpointReference(parseEndpointReference(helper.getDPWSVersion(), parser));
				} else {
					parseUnknownElement(host, parser, namespace, name);
				}
			} else if (helper.getDPWSNamespace().equals(namespace)) {
				if (helper.getDPWSElementTypes().equals(name)) {
					host.setTypes(parseQNameSet(parser));
				} else {
					parseUnknownElement(host, parser, namespace, name);
				}
			} else {
				parseUnknownElement(host, parser, namespace, name);
			}
			event = parser.nextTag();
		} while (event != XmlPullParser.END_TAG);
		return host;
	}

	private HostedMData parseHostedMData(ElementParser parser, ConstantsHelper helper, String communicationManagerId) throws XmlPullParserException, IOException {
		HostedMData hosted = new HostedMData();

		parseUnknownAttributes(hosted, parser);

		int event = parser.nextTag();
		if (event == XmlPullParser.END_TAG) {
			throw new XmlPullParserException("Hosted is empty");
		}
		EprInfoSet references = null;
		do {
			String namespace = parser.getNamespace();
			String name = parser.getName();
			if (helper.getWSANamespace().equals(namespace)) {
				if (WSAConstants.WSA_ELEM_ENDPOINT_REFERENCE.equals(name)) {
					if (references == null) {
						references = new EprInfoSet();
					}
					references.add(parseEprInfo(helper.getDPWSVersion(), communicationManagerId, parser));
				} else {
					parseUnknownElement(hosted, parser, namespace, name);
				}
			} else if (helper.getDPWSNamespace().equals(namespace)) {
				if (helper.getDPWSElementTypes().equals(name)) {
					hosted.setTypes(parseQNameSet(parser));
				} else if (helper.getDPWSElementServiceId().equals(name)) {
					hosted.setServiceId(new URI(parser.nextText().trim()));
				} else {
					parseUnknownElement(hosted, parser, namespace, name);
				}
			} else {
				parseUnknownElement(hosted, parser, namespace, name);
			}
			event = parser.nextTag();
		} while (event != XmlPullParser.END_TAG);

		if (references != null) {
			hosted.setEprInfoSet(references);
		}
		return hosted;
	}

	private ReferenceParametersMData parseReferenceParametersMData(ElementParser parentParser) throws XmlPullParserException, IOException {
		ReferenceParametersMData parameters = new ReferenceParametersMData();
		parseUnknownAttributes(parameters, parentParser);
		int event = parentParser.nextTag(); // go to first child
		int depth = parentParser.getDepth();
		String namespace = parentParser.getNamespace();
		String name = parentParser.getName();
		if (event == XmlPullParser.END_TAG && (WSAConstants.WSA_NAMESPACE_NAME.equals(namespace) || WSAConstants2006.WSA_NAMESPACE_NAME.equals(namespace)) && WSAConstants.WSA_ELEM_REFERENCE_PARAMETERS.equals(name)) {
			// empty but existing reference parameters block
			return parameters;
		}
		ElementParser parser = new ElementParser(parentParser);
		ReferenceParameter currentParameter = null;
		boolean onTopLevel = true;
		StringBuffer result = new StringBuffer();
		while (true) {
			do {
				switch (event) {
					case (XmlPullParser.START_TAG): {
						namespace = parser.getNamespace();
						name = parser.getName();
						if (onTopLevel) {
							if (WSEConstants.WSE_NAMESPACE_NAME.equals(namespace) && WSEConstants.WSE_ELEM_IDENTIFIER.equals(name)) {
								parameters.setWseIdentifier(parser.nextText().trim());
								continue;
							}
							QName elementName = new QName(name, namespace);
							Object obj = parentParser.chainHandler(elementName, false);
							if (obj != null) {
								parameters.addUnknownElement(elementName, obj);
								continue;
							}
							// 1st chunk = '<' literal (statically known)
							// 2nd chunk = element namespace
							// 3rd chunk = ':' literal + element name
							// 4th chunk = bulk char data
							// 5th chunk = next attribute/element's namespace
							// 6th chunk = see 4th chunk
							// 7th chunk = see 5th chunk
							currentParameter = new ReferenceParameter(namespace, name);
							parameters.add(currentParameter);
						} else {
							result.append('<');
							currentParameter.appendChunk(result.toString());
							result = new StringBuffer();
							currentParameter.appendChunk(namespace);
							result.append(':').append(name);
						}

						int attrCount = parser.getAttributeCount();
						for (int i = 0; i < attrCount; i++) {
							result.append(' ');
							String prefix = parser.getAttributePrefix(i);
							String attribute = parser.getAttributeName(i);
							if (prefix == null) {
								// assume same attribute namespace as element
								if ((WSAConstants.WSA_NAMESPACE_NAME.equals(namespace) || WSAConstants2006.WSA_NAMESPACE_NAME.equals(namespace)) && WSAConstants.WSA_ATTR_IS_REFERENCE_PARAMETER.equals(attribute)) {
									// skip wsa:IsReferenceParameter
									continue;
								}
							} else {
								String attributeNamespace = parser.getAttributeNamespace(i);
								if ((WSAConstants.WSA_NAMESPACE_NAME.equals(attributeNamespace) || WSAConstants2006.WSA_NAMESPACE_NAME.equals(attributeNamespace)) && WSAConstants.WSA_ATTR_IS_REFERENCE_PARAMETER.equals(attribute)) {
									// skip wsa:IsReferenceParameter
									continue;
								}
								currentParameter.appendChunk(result.toString());
								currentParameter.appendChunk(attributeNamespace);
								result = new StringBuffer();
								result.append(':');
							}
							String value = parser.getAttributeValue(i);
							result.append(attribute).append("=\"").append(value).append('\"');
						}
						result.append('>');
						onTopLevel = false;
						break;
					}
					case (XmlPullParser.TEXT): {
						result.append(parser.getText().trim());
						break;
					}
					case (XmlPullParser.END_TAG): {
						result.append("</");
						currentParameter.appendChunk(result.toString());
						currentParameter.appendChunk(parser.getNamespace());
						result = new StringBuffer();
						result.append(':').append(parser.getName()).append('>');
						break;
					}
				}
			} while ((event = parser.next()) != XmlPullParser.END_DOCUMENT);
			event = parentParser.nextTag();
			if (parentParser.getDepth() == depth) {
				// next reference parameter starts
				parser = new ElementParser(parentParser);
				currentParameter.appendChunk(result.toString());
				result = new StringBuffer();
				onTopLevel = true;
			} else {
				// reference parameters end tag
				break;
			}
		}
		if (currentParameter != null) {
			currentParameter.appendChunk(result.toString());
		}
		return parameters;
	}

	/**
	 * Method to parse a EndpointReference (Addressing 2005 EPR)
	 * 
	 * @return
	 * @throws XmlPullParserException
	 * @throws IOException
	 */

	private EndpointReference parseEndpointReference(ElementParser parser) throws XmlPullParserException, IOException {
		// handle attributes
		int attributeCount = parser.getAttributeCount();
		HashMap unknownAttributes = null;
		if (attributeCount > 0) {
			unknownAttributes = new HashMap();
			for (int i = 0; i < attributeCount; i++) {
				unknownAttributes.put(new QName(parser.getAttributeName(i), parser.getAttributeNamespace(i)), parser.getAttributeValue(i));
			}
		}
		AttributedURI address = null;
		ReferenceParametersMData parameters = null;
		MetadataMData metadata = null;
		HashMap unknownElements = null;
		while (parser.nextTag() != XmlPullParser.END_TAG) {
			String namespace = parser.getNamespace();
			String name = parser.getName();
			if (WSAConstants.WSA_NAMESPACE_NAME.equals(namespace)) {
				if (WSAConstants.WSA_ELEM_ADDRESS.equals(name)) {
					address = AttributedURI.parse(parser);
				} else if (WSAConstants.WSA_ELEM_REFERENCE_PARAMETERS.equals(name)) {
					parameters = parseReferenceParametersMData(new ElementParser(parser));
				} else if (WSAConstants.WSA_ELEM_METADATA.equals(name)) {
					metadata = new MetadataMData();
					parser.nextGenericElement(metadata);
				} else {
					QName elementName = new QName(name, namespace);
					Object result = parser.chainHandler(elementName);
					if (result != null) {
						if (unknownElements == null) {
							unknownElements = new HashMap();
						}
						DataStructure elements = (DataStructure) unknownElements.get(elementName);
						if (elements == null) {
							elements = new ArrayList();
							unknownElements.put(elementName, elements);
						}
						elements.add(result);
					}
				}
			}
		}
		EndpointReference epr = new EndpointReference(address, parameters, metadata);
		if (unknownAttributes != null) {
			epr.setUnknownAttributes(unknownAttributes);
		}
		if (unknownElements != null) {
			epr.setUnknownElements(unknownElements);
		}

		return epr;
	}

	private EndpointReference parseEndpointReference2004(ElementParser parser) throws XmlPullParserException, IOException {
		// handle attributes
		int attributeCount = parser.getAttributeCount();
		HashMap unknownAttributes = null;
		if (attributeCount > 0) {
			unknownAttributes = new HashMap();
			for (int i = 0; i < attributeCount; i++) {
				unknownAttributes.put(new QName(parser.getAttributeName(i), parser.getAttributeNamespace(i)), parser.getAttributeValue(i));
			}
		}
		AttributedURI address = null;
		ReferenceParametersMData properties = null;
		ReferenceParametersMData parameters = null;
		// MetadataMData metadata = null;
		HashMap unknownElements = null;
		QName portType = null;
		QName serviceName = null;
		String portName = null;
		while (parser.nextTag() != XmlPullParser.END_TAG) {
			String namespace = parser.getNamespace();
			String name = parser.getName();
			if (WSAConstants2006.WSA_NAMESPACE_NAME.equals(namespace)) {
				if (WSAConstants.WSA_ELEM_ADDRESS.equals(name)) {
					address = AttributedURI.parse(parser);
				} else if (WSAConstants2006.WSA_ELEM_REFERENCE_PROPERTIES.equals(name)) {
					properties = parseReferenceParametersMData(parser);
				} else if (WSAConstants.WSA_ELEM_REFERENCE_PARAMETERS.equals(name)) {
					parameters = parseReferenceParametersMData(parser);
				} else if (WSAConstants2006.WSA_ELEM_PORT_TYPE.equals(name)) {
					portType = parser.nextQName();
				} else if (WSAConstants2006.WSA_ELEM_SERVICE_NAME.equals(name)) {
					ArrayList list = parseServiceName(parser);
					portName = (String) list.get(0);
					serviceName = (QName) list.get(1);
				} else if (WSAConstants2006.WSA_ELEM_POLICY.equals(name)) {
					// ergaenzen
				} else {
					QName elementName = new QName(name, namespace);
					Object result = parser.chainHandler(elementName);
					if (result != null) {
						if (unknownElements == null) {
							unknownElements = new HashMap();
						}
						DataStructure elements = (DataStructure) unknownElements.get(elementName);
						if (elements == null) {
							elements = new ArrayList();
							unknownElements.put(elementName, elements);
						}
						elements.add(result);
					}
				}
			}
		}
		EndpointReference2004 er = new EndpointReference2004(address, parameters, properties, portType, serviceName, portName);
		if (unknownAttributes != null) {
			er.setUnknownAttributes(unknownAttributes);
		}
		if (unknownElements != null) {
			er.setUnknownElements(unknownElements);
		}
		return er;
	}

	private ArrayList parseServiceName(ElementParser parentParser) throws XmlPullParserException, IOException {
		ArrayList list = new ArrayList();
		ElementParser parser = new ElementParser(parentParser);
		int attributeCount = parser.getAttributeCount();
		if (attributeCount > 0) {
			String value = parser.getAttributeValue(0);
			list.add(value);
		}
		QName serviceName = parser.nextQName();
		list.add(serviceName);
		return list;
	}

	private EprInfo parseEprInfo(int dpwsVersion, String comManId, ElementParser parser) throws XmlPullParserException, IOException {

		EndpointReference ref = parseEndpointReference(dpwsVersion, parser);
		return new EprInfo(ref, comManId);

	}

	/**
	 * The method returns an EndpointReference for DPWS2009 if newAddressing is
	 * "true", else if newAddressing ist "false" it returns an EnpointReference
	 * for DPWS2006.
	 * 
	 * @param addressingVersion , int that gives info about the Addressing
	 *            Version
	 * @return EndpointReference
	 * @throws XmlPullParserException
	 * @throws IOException
	 */
	private EndpointReference parseEndpointReference(int dpwsVersion, ElementParser parser) throws XmlPullParserException, IOException {

		switch (dpwsVersion) {
			case DPWSConstants.DPWS_VERSION2009:
				return parseEndpointReference(parser);
			case DPWSConstants2006.DPWS_VERSION2006:
				return parseEndpointReference2004(parser);
			default:
				throw new IllegalArgumentException("Unsupported DPWS Version");
		}
	}
	private void parseUnknownElement(UnknownDataContainer conti, ElementParser parser, String namespace, String name) throws XmlPullParserException, IOException {
		QName childName = new QName(name, namespace);
		Object value = parser.chainHandler(childName);
		if (value != null) {
			conti.addUnknownElement(childName, value);
		}
	}

	private void parseUnknownAttributes(UnknownDataContainer conti, ElementParser parser) {
		int count = parser.getAttributeCount();
		for (int i = 0; i < count; i++) {
			conti.addUnknownAttribute(new QName(parser.getAttributeName(i), parser.getAttributeNamespace(i)), parser.getAttributeValue(i));
		}
	}

}
