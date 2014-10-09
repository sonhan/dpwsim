/*******************************************************************************
 * Copyright (c) 2009 MATERNA Information & Communications. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html. For further
 * project-related information visit http://www.ws4d.org. The most recent
 * version of the JMEDS framework can be obtained from
 * http://sourceforge.net/projects/ws4d-javame.
 ******************************************************************************/

package org.ws4d.java.dispatch;

import org.ws4d.java.communication.CommunicationManager;
import org.ws4d.java.communication.CommunicationManagerRegistry;
import org.ws4d.java.communication.Discovery;
import org.ws4d.java.communication.ProtocolDomain;
import org.ws4d.java.communication.ResponseCallback;
import org.ws4d.java.message.InvokeMessage;
import org.ws4d.java.message.Message;
import org.ws4d.java.message.discovery.ByeMessage;
import org.ws4d.java.message.discovery.HelloMessage;
import org.ws4d.java.message.discovery.ProbeMessage;
import org.ws4d.java.message.discovery.ResolveMessage;
import org.ws4d.java.message.eventing.GetStatusMessage;
import org.ws4d.java.message.eventing.RenewMessage;
import org.ws4d.java.message.eventing.SubscribeMessage;
import org.ws4d.java.message.eventing.SubscriptionEndMessage;
import org.ws4d.java.message.eventing.UnsubscribeMessage;
import org.ws4d.java.message.metadata.GetMessage;
import org.ws4d.java.message.metadata.GetMetadataMessage;
import org.ws4d.java.structures.DataStructure;
import org.ws4d.java.structures.Iterator;
import org.ws4d.java.types.XAddressInfo;
import org.ws4d.java.util.Log;

/**
 * @author mspies
 */
public class OutDispatcher {

	private static final OutDispatcher	INSTANCE	= new OutDispatcher();

	/**
	 * Returns the output dispatcher.
	 * 
	 * @return the output dispatcher.
	 */
	public static OutDispatcher getInstance() {
		return INSTANCE;
	}

	private OutDispatcher() {
		super();
	}

	public void send(HelloMessage hello, XAddressInfo targetXAdrInfo, DataStructure protocolDomains) {
		sendMulticast(hello, targetXAdrInfo, protocolDomains, null);
	}

	public void send(ByeMessage bye, XAddressInfo targetXAdrInfo, DataStructure protocolDomains) {
		sendMulticast(bye, targetXAdrInfo, protocolDomains, null);
	}

	public void send(ProbeMessage probe, XAddressInfo targetXAdrInfo, DataStructure protocolDomains, ResponseCallback callback) {
		sendMulticast(probe, targetXAdrInfo, protocolDomains, callback);
	}

	public void send(ResolveMessage resolve, XAddressInfo targetXAdrInfo, DataStructure protocolDomains, ResponseCallback callback) {
		sendMulticast(resolve, targetXAdrInfo, protocolDomains, callback);
	}

	// this is for directed probes only!
	public void send(ProbeMessage probe, XAddressInfo targetXAdrInfo, ResponseCallback callback) {
		sendUnicast(probe, targetXAdrInfo, callback);
	}

	public void send(GetMessage get, XAddressInfo targetXAdrInfo, ResponseCallback callback) {
		sendUnicast(get, targetXAdrInfo, callback);
	}

	public void send(GetMetadataMessage getMetadata, XAddressInfo targetXAdrInfo, ResponseCallback callback) {
		sendUnicast(getMetadata, targetXAdrInfo, callback);
	}

	public void send(SubscribeMessage subscribe, XAddressInfo targetXAdrInfo, ResponseCallback callback) {
		sendUnicast(subscribe, targetXAdrInfo, callback);
	}

	public void send(GetStatusMessage getStatus, XAddressInfo targetXAdrInfo, ResponseCallback callback) {
		sendUnicast(getStatus, targetXAdrInfo, callback);
	}

	public void send(RenewMessage renew, XAddressInfo targetXAdrInfo, ResponseCallback callback) {
		sendUnicast(renew, targetXAdrInfo, callback);
	}

	public void send(UnsubscribeMessage unsubscribe, XAddressInfo targetXAdrInfo, ResponseCallback callback) {
		sendUnicast(unsubscribe, targetXAdrInfo, callback);
	}

	public void send(SubscriptionEndMessage subscriptionEnd, XAddressInfo targetXAdrInfo) {
		sendUnicast(subscriptionEnd, targetXAdrInfo, null);
	}

	public void send(InvokeMessage invoke, XAddressInfo targetXAdrInfo, ResponseCallback callback) {
		sendUnicast(invoke, targetXAdrInfo, callback);
	}

	private void sendMulticast(Message message, XAddressInfo targetXAdrInfo, DataStructure protocolDomains, ResponseCallback callback) {
		if (message == null) {
			return;
		}
		if (message.getRoutingScheme() == Message.UNKNOWN_ROUTING_SCHEME) {
			message.setRoutingScheme(Message.MULTICAST_ROUTING_SCHEME);
		}
		if (protocolDomains == null) {
			protocolDomains = Discovery.getDefaultOutputDomains();
		}
		if (protocolDomains.isEmpty()) {
			protocolDomains = Discovery.getAllAvailableDomains();
		}
		preSend(message);
		for (Iterator it = protocolDomains.iterator(); it.hasNext();) {
			ProtocolDomain domain = (ProtocolDomain) it.next();
			CommunicationManager comman = CommunicationManagerRegistry.getManager(domain.getCommunicationManagerId());
			comman.send(message, targetXAdrInfo, domain, callback);
		}
	}

	private void sendUnicast(Message message, XAddressInfo targetXAdrInfo, ResponseCallback callback) {
		if (message == null) {
			return;
		}
		if (message.getRoutingScheme() == Message.UNKNOWN_ROUTING_SCHEME) {
			message.setRoutingScheme(Message.UNICAST_ROUTING_SCHEME);
		}
		String comManId = targetXAdrInfo.getComManId();
		if (comManId == null) {
			// Log.warn("No protocol ID to send unicast message over: " +
			// message);
			// return;
			// instead of rejecting this, we use a system default one
			comManId = CommunicationManagerRegistry.getDefault();
		}
		preSend(message);
		CommunicationManager comman = CommunicationManagerRegistry.getManager(comManId);
		comman.send(message, targetXAdrInfo, null, callback);
	}

	private void preSend(Message message) {
		message.setInbound(false);
		if (Log.isDebug()) {
			Log.debug("<O> " + message, Log.DEBUG_LAYER_COMMUNICATION);
		}
	}

	// Methods added by Stefan Schlichting
	public void sendGenericMessage(Message msg, XAddressInfo targetXAdrInfo, ResponseCallback callback) {
		if (msg != null && msg.getRoutingScheme() == Message.UNICAST_ROUTING_SCHEME) {
			sendUnicast(msg, targetXAdrInfo, callback);
			return;
		} else {
			CommunicationManager comman = CommunicationManagerRegistry.getManager(targetXAdrInfo.getComManId());
			if (comman != null) comman.send(msg, targetXAdrInfo, null, callback);
		}
	}

	public void sendGenericMessageToProtocolDomain(Message msg, XAddressInfo targetXAdrInfo, DataStructure protocolDomains, ResponseCallback callback) {
		if (msg != null && msg.getRoutingScheme() == Message.MULTICAST_ROUTING_SCHEME) {
			sendMulticast(msg, targetXAdrInfo, protocolDomains, callback);
		} else {
			if (Log.isInfo()) Log.info("Could not send message as multicast. " + msg);
		}
	}

}
