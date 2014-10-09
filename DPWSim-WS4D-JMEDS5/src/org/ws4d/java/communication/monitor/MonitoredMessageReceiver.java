/*******************************************************************************
 * Copyright (c) 2009 MATERNA Information & Communications. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html. For further
 * project-related information visit http://www.ws4d.org. The most recent
 * version of the JMEDS framework can be obtained from
 * http://sourceforge.net/projects/ws4d-javame.
 ******************************************************************************/
package org.ws4d.java.communication.monitor;

import org.ws4d.java.DPWSFramework;
import org.ws4d.java.communication.ProtocolData;
import org.ws4d.java.communication.protocol.soap.generator.MessageReceiver;
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

public class MonitoredMessageReceiver implements MessageReceiver {

	private MessageReceiver			receiver	= null;

	private MonitoringContext		context		= null;

	private MonitorStreamFactory	monFac		= null;

	public MonitoredMessageReceiver(MessageReceiver receiver, MonitoringContext context) {
		this.receiver = receiver;
		this.context = context;
		this.monFac = DPWSFramework.getMonitorStreamFactory();
	}

	public void receive(HelloMessage hello, ProtocolData protocolData) {
		receiver.receive(hello, protocolData);
		if (monFac != null) {
			monFac.received(protocolData, context, hello);
		}
	}

	public void receive(ByeMessage bye, ProtocolData protocolData) {
		receiver.receive(bye, protocolData);
		if (monFac != null) {
			monFac.received(protocolData, context, bye);
		}
	}

	public void receive(ProbeMessage probe, ProtocolData protocolData) {
		if (monFac != null) {
			monFac.received(protocolData, context, probe);
		}
		receiver.receive(probe, protocolData);
	}

	public void receive(ProbeMatchesMessage probeMatches, ProtocolData protocolData) {
		if (monFac != null) {
			monFac.received(protocolData, context, probeMatches);
		}
		receiver.receive(probeMatches, protocolData);
	}

	public void receive(ResolveMessage resolve, ProtocolData protocolData) {
		if (monFac != null) {
			monFac.received(protocolData, context, resolve);
		}
		receiver.receive(resolve, protocolData);
	}

	public void receive(ResolveMatchesMessage resolveMatches, ProtocolData protocolData) {
		if (monFac != null) {
			monFac.received(protocolData, context, resolveMatches);
		}
		receiver.receive(resolveMatches, protocolData);
	}

	public void receive(GetMessage get, ProtocolData protocolData) {
		if (monFac != null) {
			monFac.received(protocolData, context, get);
		}
		receiver.receive(get, protocolData);
	}

	public void receive(GetResponseMessage getResponse, ProtocolData protocolData) {
		if (monFac != null) {
			monFac.received(protocolData, context, getResponse);
		}
		receiver.receive(getResponse, protocolData);
	}

	public void receive(GetMetadataMessage getMetadata, ProtocolData protocolData) {
		if (monFac != null) {
			monFac.received(protocolData, context, getMetadata);
		}
		receiver.receive(getMetadata, protocolData);
	}

	public void receive(GetMetadataResponseMessage getMetadataResponse, ProtocolData protocolData) {
		if (monFac != null) {
			monFac.received(protocolData, context, getMetadataResponse);
		}
		receiver.receive(getMetadataResponse, protocolData);
	}

	public void receive(SubscribeMessage subscribe, ProtocolData protocolData) {
		if (monFac != null) {
			monFac.received(protocolData, context, subscribe);
		}
		receiver.receive(subscribe, protocolData);
	}

	public void receive(SubscribeResponseMessage subscribeResponse, ProtocolData protocolData) {
		if (monFac != null) {
			monFac.received(protocolData, context, subscribeResponse);
		}
		receiver.receive(subscribeResponse, protocolData);
	}

	public void receive(GetStatusMessage getStatus, ProtocolData protocolData) {
		if (monFac != null) {
			monFac.received(protocolData, context, getStatus);
		}
		receiver.receive(getStatus, protocolData);
	}

	public void receive(GetStatusResponseMessage getStatusResponse, ProtocolData protocolData) {
		if (monFac != null) {
			monFac.received(protocolData, context, getStatusResponse);
		}
		receiver.receive(getStatusResponse, protocolData);
	}

	public void receive(RenewMessage renew, ProtocolData protocolData) {
		if (monFac != null) {
			monFac.received(protocolData, context, renew);
		}
		receiver.receive(renew, protocolData);
	}

	public void receive(RenewResponseMessage renewResponse, ProtocolData protocolData) {
		if (monFac != null) {
			monFac.received(protocolData, context, renewResponse);
		}
		receiver.receive(renewResponse, protocolData);
	}

	public void receive(UnsubscribeMessage unsubscribe, ProtocolData protocolData) {
		if (monFac != null) {
			monFac.received(protocolData, context, unsubscribe);
		}
		receiver.receive(unsubscribe, protocolData);
	}

	public void receive(UnsubscribeResponseMessage unsubscribeResponse, ProtocolData protocolData) {
		if (monFac != null) {
			monFac.received(protocolData, context, unsubscribeResponse);
		}
		receiver.receive(unsubscribeResponse, protocolData);
	}

	public void receive(SubscriptionEndMessage subscriptionEnd, ProtocolData protocolData) {
		if (monFac != null) {
			monFac.received(protocolData, context, subscriptionEnd);
		}
		receiver.receive(subscriptionEnd, protocolData);
	}

	public void receive(InvokeMessage invoke, ProtocolData protocolData) {
		if (monFac != null) {
			monFac.received(protocolData, context, invoke);
		}
		receiver.receive(invoke, protocolData);
	}

	public void receive(FaultMessage fault, ProtocolData protocolData) {
		if (monFac != null) {
			monFac.received(protocolData, context, fault);
		}
		receiver.receive(fault, protocolData);
	}

	public void receiveFailed(Exception e, ProtocolData protocolData) {
		if (monFac != null) {
			monFac.receivedFault(protocolData, context, e);
		}
		receiver.receiveFailed(e, protocolData);

	}

	public void sendFailed(Exception e, ProtocolData protocolData) {
		if (monFac != null) {
			monFac.receivedFault(protocolData, context, e);
		}
		receiver.sendFailed(e, protocolData);
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.communication.protocol.soap.generator.MessageReceiver#
	 * getOperation(java.lang.String)
	 */
	public OperationDescription getOperation(String action) {
		return receiver.getOperation(action);
	}

}
