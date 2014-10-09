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
import org.ws4d.java.constants.DPWSMessageConstants;
import org.ws4d.java.io.xml.XmlSerializer;
import org.ws4d.java.message.FaultMessage;
import org.ws4d.java.message.InvokeMessage;
import org.ws4d.java.message.Message;
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

abstract class MessageSerializer {

	// message serialization

	public void serialize(Message message, XmlSerializer serializer, ProtocolData protocolData) throws IOException {
		switch (message.getType()) {
			case DPWSMessageConstants.HELLO_MESSAGE:
				serialize((HelloMessage) message, serializer, protocolData);
				break;
			case DPWSMessageConstants.BYE_MESSAGE:
				serialize((ByeMessage) message, serializer, protocolData);
				break;
			case DPWSMessageConstants.PROBE_MESSAGE:
				serialize((ProbeMessage) message, serializer, protocolData);
				break;
			case DPWSMessageConstants.PROBE_MATCHES_MESSAGE:
				serialize((ProbeMatchesMessage) message, serializer, protocolData);
				break;
			case DPWSMessageConstants.RESOLVE_MESSAGE:
				serialize((ResolveMessage) message, serializer, protocolData);
				break;
			case DPWSMessageConstants.RESOLVE_MATCHES_MESSAGE:
				serialize((ResolveMatchesMessage) message, serializer, protocolData);
				break;
			case DPWSMessageConstants.GET_MESSAGE:
				serialize((GetMessage) message, serializer, protocolData);
				break;
			case DPWSMessageConstants.GET_RESPONSE_MESSAGE:
				serialize((GetResponseMessage) message, serializer, protocolData);
				break;
			case DPWSMessageConstants.GET_METADATA_MESSAGE:
				serialize((GetMetadataMessage) message, serializer, protocolData);
				break;
			case DPWSMessageConstants.GET_METADATA_RESPONSE_MESSAGE:
				serialize((GetMetadataResponseMessage) message, serializer, protocolData);
				break;
			case DPWSMessageConstants.FAULT_MESSAGE:
				serialize((FaultMessage) message, serializer, protocolData);
				break;
			case DPWSMessageConstants.INVOKE_MESSAGE:
				serialize((InvokeMessage) message, serializer, protocolData);
				break;
			case DPWSMessageConstants.SUBSCRIBE_MESSAGE:
				serialize((SubscribeMessage) message, serializer, protocolData);
				break;
			case DPWSMessageConstants.SUBSCRIBE_RESPONSE_MESSAGE:
				serialize((SubscribeResponseMessage) message, serializer, protocolData);
				break;
			case DPWSMessageConstants.SUBSCRIPTION_END_MESSAGE:
				serialize((SubscriptionEndMessage) message, serializer, protocolData);
				break;
			case DPWSMessageConstants.GET_STATUS_MESSAGE:
				serialize((GetStatusMessage) message, serializer, protocolData);
				break;
			case DPWSMessageConstants.GET_STATUS_RESPONSE_MESSAGE:
				serialize((GetStatusResponseMessage) message, serializer, protocolData);
				break;
			case DPWSMessageConstants.RENEW_MESSAGE:
				serialize((RenewMessage) message, serializer, protocolData);
				break;
			case DPWSMessageConstants.RENEW_RESPONSE_MESSAGE:
				serialize((RenewResponseMessage) message, serializer, protocolData);
				break;
			case DPWSMessageConstants.UNSUBSCRIBE_MESSAGE:
				serialize((UnsubscribeMessage) message, serializer, protocolData);
				break;
			case DPWSMessageConstants.UNSUBSCRIBE_RESPONSE_MESSAGE:
				serialize((UnsubscribeResponseMessage) message, serializer, protocolData);
				break;
			default:
				throw new IOException("Cannot determinate message type.");
		}
	}

	public abstract void serialize(HelloMessage message, XmlSerializer serializer, ProtocolData protocolData) throws IOException;

	public abstract void serialize(ByeMessage message, XmlSerializer serializer, ProtocolData protocolData) throws IOException;

	public abstract void serialize(ProbeMessage message, XmlSerializer serializer, ProtocolData protocolData) throws IOException;

	public abstract void serialize(ProbeMatchesMessage message, XmlSerializer serializer, ProtocolData protocolData) throws IOException;

	public abstract void serialize(ResolveMessage message, XmlSerializer serializer, ProtocolData protocolData) throws IOException;

	public abstract void serialize(ResolveMatchesMessage message, XmlSerializer serializer, ProtocolData protocolData) throws IOException;

	public abstract void serialize(InvokeMessage message, XmlSerializer serializer, ProtocolData protocolData) throws IOException;

	public abstract void serialize(GetStatusMessage message, XmlSerializer serializer, ProtocolData protocolData) throws IOException;

	public abstract void serialize(GetStatusResponseMessage message, XmlSerializer serializer, ProtocolData protocolData) throws IOException;

	public abstract void serialize(RenewMessage message, XmlSerializer serializer, ProtocolData protocolData) throws IOException;

	public abstract void serialize(RenewResponseMessage message, XmlSerializer serializer, ProtocolData protocolData) throws IOException;

	public abstract void serialize(SubscribeMessage message, XmlSerializer serializer, ProtocolData protocolData) throws IOException;

	public abstract void serialize(SubscribeResponseMessage message, XmlSerializer serializer, ProtocolData protocolData) throws IOException;

	public abstract void serialize(SubscriptionEndMessage message, XmlSerializer serializer, ProtocolData protocolData) throws IOException;

	public abstract void serialize(UnsubscribeMessage message, XmlSerializer serializer, ProtocolData protocolData) throws IOException;

	public abstract void serialize(UnsubscribeResponseMessage message, XmlSerializer serializer, ProtocolData protocolData) throws IOException;

	public abstract void serialize(GetMessage message, XmlSerializer serializer, ProtocolData protocolData) throws IOException;

	public abstract void serialize(GetResponseMessage message, XmlSerializer serializer, ProtocolData protocolData) throws IOException;

	public abstract void serialize(GetMetadataMessage message, XmlSerializer serializer, ProtocolData protocolData) throws IOException;

	public abstract void serialize(GetMetadataResponseMessage message, XmlSerializer serializer, ProtocolData protocolData) throws IOException;

	public abstract void serialize(FaultMessage message, XmlSerializer serializer, ProtocolData protocolData) throws IOException;

	public abstract void serialize(SOAPHeader header, XmlSerializer serializer, ProtocolData protocolData) throws IOException;

}
