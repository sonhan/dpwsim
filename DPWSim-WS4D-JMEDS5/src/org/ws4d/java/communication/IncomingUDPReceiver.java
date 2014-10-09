/*******************************************************************************
 * Copyright (c) 2009 MATERNA Information & Communications. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html. For further
 * project-related information visit http://www.ws4d.org. The most recent
 * version of the JMEDS framework can be obtained from
 * http://sourceforge.net/projects/ws4d-javame.
 ******************************************************************************/
package org.ws4d.java.communication;

import org.ws4d.java.DPWSFramework;
import org.ws4d.java.communication.DPWS2006.DefaultDPWSCommunicatonUtil;
import org.ws4d.java.communication.connection.ip.IPAddress;
import org.ws4d.java.communication.monitor.MonitorStreamFactory;
import org.ws4d.java.communication.monitor.MonitoringContext;
import org.ws4d.java.communication.protocol.soap.generator.DefaultMessageDiscarder;
import org.ws4d.java.communication.protocol.soap.server.SOAPoverUDPServer.SOAPoverUDPDatagramHandler;
import org.ws4d.java.constants.DPWSMessageConstants;
import org.ws4d.java.dispatch.DeviceServiceRegistry;
import org.ws4d.java.dispatch.MessageInformer;
import org.ws4d.java.message.FaultMessage;
import org.ws4d.java.message.InvokeMessage;
import org.ws4d.java.message.Message;
import org.ws4d.java.message.MessageDiscarder;
import org.ws4d.java.message.SOAPException;
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
import org.ws4d.java.structures.HashSet;
import org.ws4d.java.structures.Iterator;
import org.ws4d.java.structures.MessageIdBuffer;
import org.ws4d.java.util.Log;

/**
 *
 */
final class IncomingUDPReceiver extends SOAPoverUDPDatagramHandler {

	private static final MessageInformer		MESSAGE_INFORMER		= MessageInformer.getInstance();

	private final DefaultMessageDiscarder		discarder				= new RelevanceMessageDiscarder();

	private final DefaultDPWSCommunicatonUtil	util					= DefaultDPWSCommunicatonUtil.getInstance();

	private final HashSet						helloListeners			= new HashSet();

	private final HashSet						byeListeners			= new HashSet();

	private final HashSet						probeResolveListeners	= new HashSet();

	/**
	 * 
	 */
	IncomingUDPReceiver() {
		super();
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.ws4d.java.communication.protocol.soap.generator.MessageReceiver#receive
	 * (org.ws4d.java.message.discovery.HelloMessage,
	 * org.ws4d.java.communication.ProtocolData)
	 */
	public synchronized void receive(final HelloMessage hello, final ProtocolData protocolData) {
		IncomingSOAPReceiver.markIncoming(hello);

		if (!hello.getHeader().isValidated()) return;

		if (!DeviceServiceRegistry.checkAndUpdateAppSequence(hello.getEndpointReference(), hello.getAppSequence())) {
			if (Log.isDebug()) Log.debug("Discarding Hello message! Old AppSequence!", Log.DEBUG_LAYER_APPLICATION);
			MonitorStreamFactory msf = DPWSFramework.getMonitorStreamFactory();
			if (msf != null) {
				MonitoringContext context = msf.getMonitoringContextIn(protocolData);
				if (context != null) {
					msf.discard(protocolData, context, MessageDiscarder.OLD_APPSEQUENCE);
				} else {
					Log.warn("Cannot get correct monitoring context for message generation.");
				}
			}
			return;
		}

		boolean first = true;
		for (Iterator it = helloListeners.iterator(); it.hasNext();) {
			final IncomingMessageListener listener = (IncomingMessageListener) it.next();
			final boolean final_first = first;
			if (first) {
				first = false;
			}
			Runnable r = new Runnable() {

				/*
				 * (non-Javadoc)
				 * @see java.lang.Runnable#run()
				 */
				public void run() {
					listener.handle(hello, protocolData);
					if (final_first) {
						MESSAGE_INFORMER.forwardMessage(hello, protocolData);
					}
				}

			};
			DPWSFramework.getThreadPool().execute(r);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.ws4d.java.communication.protocol.soap.generator.MessageReceiver#receive
	 * (org.ws4d.java.message.discovery.ByeMessage,
	 * org.ws4d.java.communication.ProtocolData)
	 */
	public synchronized void receive(final ByeMessage bye, final ProtocolData protocolData) {
		IncomingSOAPReceiver.markIncoming(bye);

		if (!bye.getHeader().isValidated()) return;

		if (!DeviceServiceRegistry.checkAndUpdateAppSequence(bye.getEndpointReference(), bye.getAppSequence())) {
			if (Log.isDebug()) Log.debug("Discarding Hello message! Old AppSequence!", Log.DEBUG_LAYER_APPLICATION);
			MonitorStreamFactory msf = DPWSFramework.getMonitorStreamFactory();
			if (msf != null) {
				MonitoringContext context = msf.getMonitoringContextIn(protocolData);
				if (context != null) {
					msf.discard(protocolData, context, MessageDiscarder.OLD_APPSEQUENCE);
				} else {
					Log.warn("Cannot get correct monitoring context for message generation.");
				}
			}
			return;
		}

		boolean first = true;
		for (Iterator it = byeListeners.iterator(); it.hasNext();) {
			final IncomingMessageListener listener = (IncomingMessageListener) it.next();
			final boolean final_first = first;
			if (first) {
				first = false;
			}
			Runnable r = new Runnable() {

				/*
				 * (non-Javadoc)
				 * @see java.lang.Runnable#run()
				 */
				public void run() {
					listener.handle(bye, protocolData);
					if (final_first) {
						MESSAGE_INFORMER.forwardMessage(bye, protocolData);
					}
				}

			};
			DPWSFramework.getThreadPool().execute(r);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.ws4d.java.communication.protocol.soap.generator.MessageReceiver#receive
	 * (org.ws4d.java.message.discovery.ProbeMessage,
	 * org.ws4d.java.communication.ProtocolData)
	 */
	public synchronized void receive(final ProbeMessage probe, final ProtocolData protocolData) {
		final long receiveTime = System.currentTimeMillis();
		IncomingSOAPReceiver.markIncoming(probe);

		boolean first = true;
		for (Iterator it = probeResolveListeners.iterator(); it.hasNext();) {
			final IncomingMessageListener receiver = (IncomingMessageListener) it.next();
			final boolean final_first = first;
			if (first) {
				first = false;
			}
			Runnable r = new Runnable() {

				/*
				 * (non-Javadoc)
				 * @see java.lang.Runnable#run()
				 */
				public void run() {
					ProtocolData pdOut = protocolData.createSwappedProtocolData();
					try {
						ProbeMatchesMessage probeMatches = receiver.handle(probe, protocolData);
						if (final_first) {
							MESSAGE_INFORMER.forwardMessage(probe, protocolData);
						}
						if (probeMatches != null) {
							
							IncomingSOAPReceiver.markOutgoing(probeMatches);
							// Check for Messageversion, if Version = 2006 the
							// Namespaces and some attributs must be changed
							util.changeOutgoingMessage(probeMatches.getProtocolInfo().getVersion(), probeMatches);

							CommunicationManager comMan = DPWSFramework.getCommunicationManager(pdOut.getCommunicationManagerId());

							// wait APP_MAX_DELAY before responding
							long sendTime = receiveTime + comMan.getRandomApplicationDelay(probe.getProtocolInfo().getVersion());
							long waitTime = sendTime - System.currentTimeMillis();
							if (waitTime > 0) {
								try {
									Thread.sleep(waitTime);
								} catch (InterruptedException e) {
									// void
								}
							}
							// respond with datagram to the given destination
							respond(probeMatches, new IPAddress(pdOut.getDestinationHost()), pdOut.getDestinationPort(), pdOut);
							MESSAGE_INFORMER.forwardMessage(probeMatches, pdOut);
						}
					} catch (SOAPException e) {
						if (final_first) {
							MESSAGE_INFORMER.forwardMessage(e.getFault(), pdOut);
						}
					}
				}
			};
			DPWSFramework.getThreadPool().execute(r);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.ws4d.java.communication.protocol.soap.generator.MessageReceiver#receive
	 * (org.ws4d.java.message.discovery.ProbeMatchesMessage,
	 * org.ws4d.java.communication.ProtocolData)
	 */
	public void receive(ProbeMatchesMessage probeMatches, ProtocolData protocolData) {
		receiveUnexpectedMessage(probeMatches, protocolData);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.ws4d.java.communication.protocol.soap.generator.MessageReceiver#receive
	 * (org.ws4d.java.message.discovery.ResolveMessage,
	 * org.ws4d.java.communication.ProtocolData)
	 */
	public synchronized void receive(final ResolveMessage resolve, final ProtocolData protocolData) {
		IncomingSOAPReceiver.markIncoming(resolve);

		boolean first = true;
		for (Iterator it = probeResolveListeners.iterator(); it.hasNext();) {
			final IncomingMessageListener receiver = (IncomingMessageListener) it.next();
			final boolean final_first = first;
			if (first) {
				first = false;
			}
			Runnable r = new Runnable() {

				/*
				 * (non-Javadoc)
				 * @see java.lang.Runnable#run()
				 */
				public void run() {
					ResolveMatchesMessage resolveMatches = receiver.handle(resolve, protocolData);

					if (final_first) {
						MESSAGE_INFORMER.forwardMessage(resolve, protocolData);
					}
					if (resolveMatches != null) {
						IncomingSOAPReceiver.markOutgoing(resolveMatches);
						// Check for Messageversion, if Version = 2006 the
						// Namespaces and some attributs must be changed
						util.changeOutgoingMessage(resolveMatches.getProtocolInfo().getVersion(), resolveMatches);

						// respond with datagram to the given destination
						respond(resolveMatches, new IPAddress(protocolData.getDestinationHost()), protocolData.getDestinationPort(), protocolData);
						MESSAGE_INFORMER.forwardMessage(resolveMatches, protocolData);
					}
				}
			};
			DPWSFramework.getThreadPool().execute(r);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.ws4d.java.communication.protocol.soap.generator.MessageReceiver#receive
	 * (org.ws4d.java.message.discovery.ResolveMatchesMessage,
	 * org.ws4d.java.communication.ProtocolData)
	 */
	public void receive(ResolveMatchesMessage resolveMatches, ProtocolData protocolData) {
		receiveUnexpectedMessage(resolveMatches, protocolData);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.ws4d.java.communication.protocol.soap.generator.MessageReceiver#receive
	 * (org.ws4d.java.message.metadata.GetMessage,
	 * org.ws4d.java.communication.ProtocolData)
	 */
	public void receive(GetMessage get, ProtocolData protocolData) {
		receiveUnexpectedMessage(get, protocolData);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.ws4d.java.communication.protocol.soap.generator.MessageReceiver#receive
	 * (org.ws4d.java.message.metadata.GetResponseMessage,
	 * org.ws4d.java.communication.ProtocolData)
	 */
	public void receive(GetResponseMessage getResponse, ProtocolData protocolData) {
		receiveUnexpectedMessage(getResponse, protocolData);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.ws4d.java.communication.protocol.soap.generator.MessageReceiver#receive
	 * (org.ws4d.java.message.metadata.GetMetadataMessage,
	 * org.ws4d.java.communication.ProtocolData)
	 */
	public void receive(GetMetadataMessage getMetadata, ProtocolData protocolData) {
		receiveUnexpectedMessage(getMetadata, protocolData);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.ws4d.java.communication.protocol.soap.generator.MessageReceiver#receive
	 * (org.ws4d.java.message.metadata. GetMetadataResponseMessage,
	 * org.ws4d.java.communication.ProtocolData)
	 */
	public void receive(GetMetadataResponseMessage getMetadataResponse, ProtocolData protocolData) {
		receiveUnexpectedMessage(getMetadataResponse, protocolData);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.ws4d.java.communication.protocol.soap.generator.MessageReceiver#receive
	 * (org.ws4d.java.message.eventing.SubscribeMessage,
	 * org.ws4d.java.communication.ProtocolData)
	 */
	public void receive(SubscribeMessage subscribe, ProtocolData protocolData) {
		receiveUnexpectedMessage(subscribe, protocolData);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.ws4d.java.communication.protocol.soap.generator.MessageReceiver#receive
	 * (org.ws4d.java.message.eventing.SubscribeResponseMessage,
	 * org.ws4d.java.communication.ProtocolData)
	 */
	public void receive(SubscribeResponseMessage subscribeResponse, ProtocolData protocolData) {
		receiveUnexpectedMessage(subscribeResponse, protocolData);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.ws4d.java.communication.protocol.soap.generator.MessageReceiver#receive
	 * (org.ws4d.java.message.eventing.GetStatusMessage,
	 * org.ws4d.java.communication.ProtocolData)
	 */
	public void receive(GetStatusMessage getStatus, ProtocolData protocolData) {
		receiveUnexpectedMessage(getStatus, protocolData);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.ws4d.java.communication.protocol.soap.generator.MessageReceiver#receive
	 * (org.ws4d.java.message.eventing.GetStatusResponseMessage,
	 * org.ws4d.java.communication.ProtocolData)
	 */
	public void receive(GetStatusResponseMessage getStatusResponse, ProtocolData protocolData) {
		receiveUnexpectedMessage(getStatusResponse, protocolData);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.ws4d.java.communication.protocol.soap.generator.MessageReceiver#receive
	 * (org.ws4d.java.message.eventing.RenewMessage,
	 * org.ws4d.java.communication.ProtocolData)
	 */
	public void receive(RenewMessage renew, ProtocolData protocolData) {
		receiveUnexpectedMessage(renew, protocolData);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.ws4d.java.communication.protocol.soap.generator.MessageReceiver#receive
	 * (org.ws4d.java.message.eventing.RenewResponseMessage,
	 * org.ws4d.java.communication.ProtocolData)
	 */
	public void receive(RenewResponseMessage renewResponse, ProtocolData protocolData) {
		receiveUnexpectedMessage(renewResponse, protocolData);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.ws4d.java.communication.protocol.soap.generator.MessageReceiver#receive
	 * (org.ws4d.java.message.eventing.UnsubscribeMessage,
	 * org.ws4d.java.communication.ProtocolData)
	 */
	public void receive(UnsubscribeMessage unsubscribe, ProtocolData protocolData) {
		receiveUnexpectedMessage(unsubscribe, protocolData);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.ws4d.java.communication.protocol.soap.generator.MessageReceiver#receive
	 * (org.ws4d.java.message.eventing.UnsubscribeResponseMessage,
	 * org.ws4d.java.communication.ProtocolData)
	 */
	public void receive(UnsubscribeResponseMessage unsubscribeResponse, ProtocolData protocolData) {
		receiveUnexpectedMessage(unsubscribeResponse, protocolData);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.ws4d.java.communication.protocol.soap.generator.MessageReceiver#receive
	 * (org.ws4d.java.message.eventing.SubscriptionEndMessage,
	 * org.ws4d.java.communication.ProtocolData)
	 */
	public void receive(SubscriptionEndMessage subscriptionEnd, ProtocolData protocolData) {
		receiveUnexpectedMessage(subscriptionEnd, protocolData);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.ws4d.java.communication.protocol.soap.generator.MessageReceiver#receive
	 * (org.ws4d.java.message.invocation.InvokeMessage,
	 * org.ws4d.java.communication.ProtocolData)
	 */
	public void receive(InvokeMessage invoke, ProtocolData protocolData) {
		receiveUnexpectedMessage(invoke, protocolData);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.ws4d.java.communication.protocol.soap.generator.MessageReceiver#receive
	 * (org.ws4d.java.message.FaultMessage,
	 * org.ws4d.java.communication.ProtocolData)
	 */
	public void receive(FaultMessage fault, ProtocolData protocolData) {
		receiveUnexpectedMessage(fault, protocolData);
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.communication.protocol.soap.generator.MessageReceiver#
	 * receiveFailed(java.lang.Exception,
	 * org.ws4d.java.communication.ProtocolData)
	 */
	public void receiveFailed(Exception e, ProtocolData protocolData) {
		/*
		 * who cares?? :-P this exception gets logged from within the SOAP 2
		 * message generator
		 */
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.communication.protocol.soap.generator.MessageReceiver#
	 * sendFailed(java.lang.Exception, org.ws4d.java.communication.ProtocolData)
	 */
	public void sendFailed(Exception e, ProtocolData protocolData) {
		/*
		 * we are on the server side, thus we don't send anything that could go
		 * wrong
		 */
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.communication.protocol.soap.server.SOAPoverUDPServer.
	 * SOAPoverUDPDatagramHandler#getDiscarder()
	 */
	protected DefaultMessageDiscarder getDiscarder() {
		return discarder;
	}

	synchronized void registerHelloListener(IncomingMessageListener listener) {
		helloListeners.add(listener);
	}

	synchronized void registerByeListener(IncomingMessageListener listener) {
		byeListeners.add(listener);
	}

	synchronized void registerProbeResolveListener(IncomingMessageListener listener) {
		probeResolveListeners.add(listener);
	}

	synchronized void unregisterHelloListener(IncomingMessageListener listener) {
		helloListeners.remove(listener);
	}

	synchronized void unregisterByeListener(IncomingMessageListener listener) {
		byeListeners.remove(listener);
	}

	synchronized void unregisterProbeResolveListener(IncomingMessageListener listener) {
		probeResolveListeners.remove(listener);
	}

	synchronized void register(int[] messageTypes, IncomingMessageListener listener) {
		for (int i = 0; i < messageTypes.length; i++) {
			switch (messageTypes[i]) {
				case (DPWSMessageConstants.HELLO_MESSAGE): {
					helloListeners.add(listener);
					break;
				}
				case (DPWSMessageConstants.BYE_MESSAGE): {
					byeListeners.add(listener);
					break;
				}
				case (DPWSMessageConstants.PROBE_MESSAGE): {
					probeResolveListeners.add(listener);
					break;
				}
				case (DPWSMessageConstants.RESOLVE_MESSAGE): {
					probeResolveListeners.add(listener);
					break;
				}
			}
		}
	}

	synchronized void unregister(int[] messageTypes, IncomingMessageListener listener) {
		for (int i = 0; i < messageTypes.length; i++) {
			switch (messageTypes[i]) {
				case (DPWSMessageConstants.HELLO_MESSAGE): {
					helloListeners.remove(listener);
					break;
				}
				case (DPWSMessageConstants.BYE_MESSAGE): {
					byeListeners.remove(listener);
					break;
				}
				case (DPWSMessageConstants.PROBE_MESSAGE): {
					probeResolveListeners.remove(listener);
					break;
				}
				case (DPWSMessageConstants.RESOLVE_MESSAGE): {
					probeResolveListeners.remove(listener);
					break;
				}
			}
		}
	}

	synchronized boolean isEmpty() {
		return helloListeners.isEmpty() && byeListeners.isEmpty() && probeResolveListeners.isEmpty();
	}

	synchronized void clear() {
		helloListeners.clear();
		byeListeners.clear();
		probeResolveListeners.clear();
	}

	private void receiveUnexpectedMessage(Message message, ProtocolData protocolData) {
		IncomingSOAPReceiver.markIncoming(message);
		String actionName = message.getAction().toString();
		Log.error("<I> Unexpected multicast SOAP-over-UDP message: " + actionName);
		if (Log.isDebug()) {
			Log.error(message.toString());
		}
		MESSAGE_INFORMER.forwardMessage(message, protocolData);
	}

	public OperationDescription getOperation(String action) {
		return null;
	}

	private class RelevanceMessageDiscarder extends DefaultMessageDiscarder {

		private final MessageIdBuffer	duplicateMessageIds	= new MessageIdBuffer();

		/*
		 * (non-Javadoc)
		 * @see
		 * org.ws4d.java.communication.protocol.soap.generator.MessageDiscarder
		 * #discardMessage(org.ws4d.java.message.SOAPHeader,
		 * org.ws4d.java.communication.ProtocolData)
		 */
		public int discardMessage(SOAPHeader header, ProtocolData protocolData) {
			int superResult = super.discardMessage(header, protocolData);
			if (superResult != NOT_DISCARDED) return superResult;

			if (duplicateMessageIds.containsOrEnqueue(header.getMessageId())) {
				return DUPLICATE_MESSAGE;
			}

			synchronized (IncomingUDPReceiver.this) {
				int msgType = header.getDPWSMessageType();
				switch (msgType) {
					case DPWSMessageConstants.HELLO_MESSAGE:
						if (helloListeners.isEmpty()) return NOT_RELEVANT_MESSAGE;
						break;
					case DPWSMessageConstants.BYE_MESSAGE:
						if (byeListeners.isEmpty()) return NOT_RELEVANT_MESSAGE;
						break;
					case DPWSMessageConstants.PROBE_MESSAGE:
						if (probeResolveListeners.isEmpty()) return NOT_RELEVANT_MESSAGE;
						break;
					case DPWSMessageConstants.RESOLVE_MESSAGE:
						if (probeResolveListeners.isEmpty()) return NOT_RELEVANT_MESSAGE;
						break;
					default:
						return NOT_RELEVANT_MESSAGE;
				}
			}

			return NOT_DISCARDED;
		}

	}

}
