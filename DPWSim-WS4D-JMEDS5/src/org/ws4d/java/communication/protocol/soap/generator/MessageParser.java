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

import org.ws4d.java.communication.ProtocolData;
import org.ws4d.java.communication.VersionMismatchException;
import org.ws4d.java.constants.ConstantsHelper;
import org.ws4d.java.io.xml.ElementParser;
import org.ws4d.java.message.FaultMessage;
import org.ws4d.java.message.InvokeMessage;
import org.ws4d.java.message.SOAPHeader;
import org.ws4d.java.message.discovery.ByeMessage;
import org.ws4d.java.message.discovery.HelloMessage;
import org.ws4d.java.message.discovery.ProbeMatchesMessage;
import org.ws4d.java.message.discovery.ProbeMessage;
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
import org.ws4d.java.service.OperationDescription;
import org.xmlpull.v1.XmlPullParserException;

abstract class MessageParser {

	public abstract SOAPHeader parseSOAPHeader(ElementParser parser, ProtocolData protocolData, ConstantsHelper helper) throws XmlPullParserException, IOException, VersionMismatchException;

	public abstract HelloMessage parseHelloMessage(SOAPHeader header, ElementParser parser, ProtocolData protocolData, ConstantsHelper helper) throws XmlPullParserException, IOException;

	public abstract RenewMessage parseRenewMessage(SOAPHeader header, ElementParser parser, ProtocolData protocolData, ConstantsHelper helper) throws XmlPullParserException, IOException;

	public abstract ByeMessage parseByeMessage(SOAPHeader header, ElementParser parser, ProtocolData protocolData, ConstantsHelper helper) throws XmlPullParserException, IOException;

	public abstract ProbeMessage parseProbeMessage(SOAPHeader header, ElementParser parser, ProtocolData protocolData, ConstantsHelper helper) throws XmlPullParserException, IOException;

	public abstract ProbeMatchesMessage parseProbeMatchesMessage(SOAPHeader header, ElementParser parser, ProtocolData protocolData, ConstantsHelper helper) throws XmlPullParserException, IOException;

	public abstract ResolveMessage parseResolveMessage(SOAPHeader header, ElementParser parser, ProtocolData protocolData, ConstantsHelper helper) throws XmlPullParserException, IOException;

	public abstract ResolveMatchesMessage parseResolveMatchesMessage(SOAPHeader header, ElementParser parser, ProtocolData protocolData, ConstantsHelper helper) throws XmlPullParserException, IOException;

	public abstract InvokeMessage parseInvokeMessage(SOAPHeader header, ElementParser parser, ProtocolData protocolData, ConstantsHelper helper) throws XmlPullParserException, IOException;

	public abstract GetStatusMessage parseGetStatusMessage(SOAPHeader header, ElementParser parser, ProtocolData protocolData, ConstantsHelper helper) throws XmlPullParserException, IOException;

	public abstract GetStatusResponseMessage parseGetStatusResponseMessage(SOAPHeader header, ElementParser parser, ProtocolData protocolData, ConstantsHelper helper) throws XmlPullParserException, IOException;

	public abstract RenewResponseMessage parseRenewResponseMessage(SOAPHeader header, ElementParser parser, ProtocolData protocolData, ConstantsHelper helper) throws XmlPullParserException, IOException;

	public abstract SubscribeMessage parseSubscribeMessage(SOAPHeader header, ElementParser parser, ProtocolData protocolData, ConstantsHelper helper) throws XmlPullParserException, IOException;

	public abstract SubscribeResponseMessage parseSubscribeResponseMessage(SOAPHeader header, ElementParser parser, ProtocolData protocolData, ConstantsHelper helper) throws XmlPullParserException, IOException;

	public abstract SubscriptionEndMessage parseSubscriptionEndMessage(SOAPHeader header, ElementParser parser, ProtocolData protocolData, ConstantsHelper helper) throws XmlPullParserException, IOException;

	public abstract UnsubscribeMessage parseUnsubscribeMessage(SOAPHeader header, ElementParser parser, ProtocolData protocolData, ConstantsHelper helper) throws XmlPullParserException, IOException;

	public abstract UnsubscribeResponseMessage parseUnsubscribeResponseMessage(SOAPHeader header, ElementParser parser, ProtocolData protocolData, ConstantsHelper helper) throws XmlPullParserException, IOException;

	public abstract GetMessage parseGetMessage(SOAPHeader header, ElementParser parser, ProtocolData protocolData, ConstantsHelper helper) throws XmlPullParserException, IOException;

	public abstract GetResponseMessage parseGetResponseMessage(SOAPHeader header, ElementParser parser, ProtocolData protocolData, ConstantsHelper helper) throws XmlPullParserException, IOException;

	public abstract GetMetadataMessage parseGetMetadataMessage(SOAPHeader header, ElementParser parser, ProtocolData protocolData, ConstantsHelper helper) throws XmlPullParserException, IOException;

	public abstract GetMetadataResponseMessage parseGetMetadataResponseMessage(SOAPHeader header, ElementParser parser, ProtocolData protocolData, ConstantsHelper helper) throws XmlPullParserException, IOException;

	public abstract FaultMessage parseFaultMessage(SOAPHeader header, ElementParser parser, ProtocolData protocolData, ConstantsHelper helper, String actionName, OperationDescription op) throws XmlPullParserException, IOException;

}
