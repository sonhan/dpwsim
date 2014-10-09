/*******************************************************************************
 * Copyright (c) 2009 MATERNA Information & Communications. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html. For further
 * project-related information visit http://www.ws4d.org. The most recent
 * version of the JMEDS framework can be obtained from
 * http://sourceforge.net/projects/ws4d-javame.
 ******************************************************************************/
package org.ws4d.java.communication.protocol.soap;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.ws4d.java.DPWSFramework;
import org.ws4d.java.communication.ProtocolData;
import org.ws4d.java.communication.connection.ip.IPAddress;
import org.ws4d.java.communication.connection.udp.Datagram;
import org.ws4d.java.communication.connection.udp.DatagramInputStream;
import org.ws4d.java.communication.connection.udp.UDPClient;
import org.ws4d.java.communication.connection.udp.UDPDatagramHandler;
import org.ws4d.java.communication.monitor.MonitorStreamFactory;
import org.ws4d.java.communication.monitor.MonitoredInputStream;
import org.ws4d.java.communication.monitor.MonitoredMessageReceiver;
import org.ws4d.java.communication.monitor.MonitoredOutputStream;
import org.ws4d.java.communication.monitor.MonitoringContext;
import org.ws4d.java.communication.protocol.soap.generator.DefaultMessageDiscarder;
import org.ws4d.java.communication.protocol.soap.generator.MessageReceiver;
import org.ws4d.java.communication.protocol.soap.generator.SOAPMessageGeneratorFactory;
import org.ws4d.java.message.Message;
import org.ws4d.java.message.SOAPHeader;
import org.ws4d.java.structures.HashMap;
import org.ws4d.java.structures.MessageIdBuffer;
import org.ws4d.java.types.ByteArrayBuffer;
import org.ws4d.java.util.Math;

/**
 * A SOAP-over-UDP client, which allows the sending of a SOAP message as a UDP
 * datagram packet.
 */
public class SOAPoverUDPClient {

	/*
	 * This <code>Announcer</code> implements the "Retransmission" described in
	 * the SOAP-over-UDP (3.4) document with the restriction made by DPWS
	 * Committee Draft 03 (Appendix B).
	 */

	public static final int	MULTICAST_UNICAST_UDP_REPEAT	= 1;				// DPWS

	public static final int	UDP_MIN_DELAY					= 50;

	public static final int	UDP_MAX_DELAY					= 250;

	public static final int	UDP_UPPER_DELAY					= 450;				// DPWS

	/**
	 * The internal UDP client.
	 */
	private UDPClient		client							= null;

	/**
	 * Table of SOAP-over-UDP clients.
	 */
	private static HashMap	clients							= new HashMap();

	/**
	 * Returns a SOAP-over-UDP client with address and port for incoming UDP
	 * messages. If no client exists, a new client will be created.
	 * 
	 * @param address the local address.
	 * @param port the port.
	 * @return the SOAP-over-UDP client.
	 */
	public synchronized static SOAPoverUDPClient get(IPAddress ipAddress, int port, String ifaceName) {
		if (port == 0) {
			return new SOAPoverUDPClient(UDPClient.get(ipAddress, port, ifaceName));
		}
		String key = ipAddress.getAddress() + "@" + port + "%" + ifaceName;
		SOAPoverUDPClient soapc = (SOAPoverUDPClient) clients.get(key);
		if (soapc != null) return soapc;
		soapc = new SOAPoverUDPClient(UDPClient.get(ipAddress, port, ifaceName));
		clients.put(key, soapc);
		return soapc;
	}

	private SOAPoverUDPClient(UDPClient client) {
		this.client = client;
	}

	/**
	 * Creates a UDP datagram socket and uses this socket to send the given SOAP
	 * message.
	 * <p>
	 * The SOAP message will be sent twice as described in the DPWS 1.1
	 * specification.
	 * </p>
	 * 
	 * @param dstAddress destination address of the SOAP message.
	 * @param dstPort destination port of the SOAP message.
	 * @param ifaceName
	 * @param message SOAP message to send.
	 * @param handler this handler will handle the incoming UDP datagram
	 *            packets.
	 * @throws IOException
	 */
	public void send(IPAddress dstAddress, int dstPort, Message message, SOAPoverUDPHandler handler, ProtocolData protocolData) throws IOException {
		if (client.isClosed()) return;

		ByteArrayBuffer b = SOAPMessageGeneratorFactory.getInstance().getMessage2SOAPGeneratorForCurrentThread().generateSOAPMessage(message, protocolData);

		boolean putToCache = client.getPort() == 0;
		sendInternal(dstAddress, dstPort, message, handler, protocolData, b);
		if (putToCache) {
			String key = client.getIPAddress().getAddress() + "@" + client.getPort() + "%" + client.getIfaceName();
			synchronized (this.getClass()) {
				clients.put(key, this);
			}
		}

		int repeatCount = SOAPoverUDPClient.MULTICAST_UNICAST_UDP_REPEAT;
		if (repeatCount <= 0) {
			return;
		}

		int delay = Math.nextInt(SOAPoverUDPClient.UDP_MIN_DELAY, SOAPoverUDPClient.UDP_MAX_DELAY);
		while (true) {
			try {
				Thread.sleep(delay);
			} catch (InterruptedException e) {
				// ignore
			}

			sendInternal(dstAddress, dstPort, message, handler, protocolData, b);

			if (--repeatCount == 0) {
				break;
			}

			delay *= 2;
			if (delay > SOAPoverUDPClient.UDP_UPPER_DELAY) {
				delay = SOAPoverUDPClient.UDP_UPPER_DELAY;
			}
		}
	}

	private void sendInternal(IPAddress dstAddress, int dstPort, Message message, SOAPoverUDPHandler handler, ProtocolData protocolData, ByteArrayBuffer b) throws IOException {
		MonitorStreamFactory monFac = DPWSFramework.getMonitorStreamFactory();
		MonitoringContext context = null;
		try {
			if (monFac != null) {
				OutputStream o = new ByteArrayOutputStream(b.getContentLength());
				o = new MonitoredOutputStream(o, protocolData);
				context = monFac.getNewMonitoringContextOut(protocolData);
				o.write(b.getBuffer(), 0, b.getContentLength());
				o.flush();
				o.close();
			}
			client.send(dstAddress, dstPort, b.getBuffer(), b.getContentLength(), handler, protocolData);
		} catch (IOException e) {
			if (monFac != null) {
				monFac.sendFault(protocolData, context, e);
			}
			throw e;
		}

		if (monFac != null) {
			monFac.send(protocolData, context, message);
		}
	}

	/**
	 * Closes the SOAP-over-UDP client.
	 * <p>
	 * No UDP datagram packets can be sent.
	 * </p>
	 * 
	 * @throws IOException
	 */
	public synchronized void close() throws IOException {
		client.close();
	}

	/**
	 * Returns <code>true</code> if the underlying UDP client is closed and
	 * cannot be used for a request, or <code>false</code> if the client can
	 * still be used.
	 * 
	 * @return <code>true</code> if the underlying UDP client is closed and
	 *         cannot be used for a request, or <code>false</code> if the client
	 *         can still be used.
	 */
	public synchronized boolean isClosed() {
		return client.isClosed();
	}

	/**
	 * UDP datagram handler implementation for SOAP messages.
	 */
	public static class SOAPoverUDPHandler implements UDPDatagramHandler {

		private final MessageReceiver			receiver;

		private final DefaultMessageDiscarder	discarder;

		public SOAPoverUDPHandler(MessageReceiver receiver) {
			super();
			this.receiver = receiver;
			this.discarder = new DuplicateMessageDiscarder();
		}

		/*
		 * (non-Javadoc)
		 * @see
		 * org.ws4d.java.communication.connection.udp.UDPDatagramHandler#handle
		 * (org.ws4d.java.communication.connection.udp.Datagram,
		 * org.ws4d.java.communication.DPWSProtocolData)
		 */
		public void handle(Datagram datagram, ProtocolData protocolData) throws IOException {
			InputStream in = null;

			MonitorStreamFactory monFac = DPWSFramework.getMonitorStreamFactory();

			if (monFac != null) {
				in = new MonitoredInputStream(new DatagramInputStream(datagram), protocolData);
			} else {
				in = new DatagramInputStream(datagram);
			}

			final MessageReceiver r;

			if (monFac != null) {
				MonitoringContext context = monFac.getNewMonitoringContextIn(protocolData);
				r = new MonitoredMessageReceiver(receiver, context);
			} else {
				r = receiver;
			}

			SOAPMessageGeneratorFactory.getInstance().getSOAP2MessageGeneratorForCurrentThread().deliverMessage(in, r, protocolData, discarder);
			in.close();
		}

	}

	public static class DuplicateMessageDiscarder extends DefaultMessageDiscarder {

		private final MessageIdBuffer	relMessages	= new MessageIdBuffer();

		public int discardMessage(SOAPHeader header, ProtocolData protocolData) {
			int superResult = super.discardMessage(header, protocolData);
			if (superResult != NOT_DISCARDED) return superResult;

			if (relMessages.containsOrEnqueue(header.getMessageId())) {
				return DUPLICATE_MESSAGE;
			}

			return NOT_DISCARDED;
		}

	}

}
