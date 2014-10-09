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

import org.ws4d.java.communication.ProtocolData;
import org.ws4d.java.message.FaultMessage;
import org.ws4d.java.message.InvokeMessage;
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

/**
 * Part of the new {@link SOAP2MessageGenerator} API. Instances implementing
 * this interface are capable of receiving messages created by the generator. A
 * DPWS message of a certain type is delivered to the receiving instance by a
 * call to the appropriate <code>receive()</code> method (e.g.
 * {@link #receive(Hello)} for hello messages).
 */
public interface MessageReceiver {

	public void receive(HelloMessage hello, ProtocolData protocolData);

	public void receive(ByeMessage bye, ProtocolData protocolData);

	public void receive(ProbeMessage probe, ProtocolData protocolData);

	public void receive(ProbeMatchesMessage probeMatches, ProtocolData protocolData);

	public void receive(ResolveMessage resolve, ProtocolData protocolData);

	public void receive(ResolveMatchesMessage resolveMatches, ProtocolData protocolData);

	public void receive(GetMessage get, ProtocolData protocolData);

	public void receive(GetResponseMessage getResponse, ProtocolData protocolData);

	public void receive(GetMetadataMessage getMetadata, ProtocolData protocolData);

	public void receive(GetMetadataResponseMessage getMetadataResponse, ProtocolData protocolData);

	public void receive(SubscribeMessage subscribe, ProtocolData protocolData);

	public void receive(SubscribeResponseMessage subscribeResponse, ProtocolData protocolData);

	public void receive(GetStatusMessage getStatus, ProtocolData protocolData);

	public void receive(GetStatusResponseMessage getStatusResponse, ProtocolData protocolData);

	public void receive(RenewMessage renew, ProtocolData protocolData);

	public void receive(RenewResponseMessage renewResponse, ProtocolData protocolData);

	public void receive(UnsubscribeMessage unsubscribe, ProtocolData protocolData);

	public void receive(UnsubscribeResponseMessage unsubscribeResponse, ProtocolData protocolData);

	public void receive(SubscriptionEndMessage subscriptionEnd, ProtocolData protocolData);

	public void receive(InvokeMessage invoke, ProtocolData protocolData);

	public void receive(FaultMessage fault, ProtocolData protocolData);

	public void receiveFailed(Exception e, ProtocolData protocolData);

	public void sendFailed(Exception e, ProtocolData protocolData);

	public OperationDescription getOperation(String action);

}
