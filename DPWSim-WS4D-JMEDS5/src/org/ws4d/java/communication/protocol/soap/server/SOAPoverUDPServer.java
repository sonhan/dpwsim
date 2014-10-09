/*******************************************************************************
 * Copyright (c) 2009 MATERNA Information & Communications. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html. For further
 * project-related information visit http://www.ws4d.org. The most recent
 * version of the JMEDS framework can be obtained from
 * http://sourceforge.net/projects/ws4d-javame.
 ******************************************************************************/
package org.ws4d.java.communication.protocol.soap.server;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.ws4d.java.DPWSFramework;
import org.ws4d.java.communication.ProtocolData;
import org.ws4d.java.communication.connection.ip.IPAddress;
import org.ws4d.java.communication.connection.udp.Datagram;
import org.ws4d.java.communication.connection.udp.DatagramInputStream;
import org.ws4d.java.communication.connection.udp.UDPDatagramHandler;
import org.ws4d.java.communication.connection.udp.UDPServer;
import org.ws4d.java.communication.monitor.MonitorStreamFactory;
import org.ws4d.java.communication.monitor.MonitoredInputStream;
import org.ws4d.java.communication.monitor.MonitoredMessageReceiver;
import org.ws4d.java.communication.monitor.MonitoredOutputStream;
import org.ws4d.java.communication.monitor.MonitoringContext;
import org.ws4d.java.communication.protocol.soap.SOAPoverUDPClient;
import org.ws4d.java.communication.protocol.soap.generator.DefaultMessageDiscarder;
import org.ws4d.java.communication.protocol.soap.generator.MessageReceiver;
import org.ws4d.java.communication.protocol.soap.generator.SOAPMessageGeneratorFactory;
import org.ws4d.java.message.Message;
import org.ws4d.java.types.ByteArrayBuffer;
import org.ws4d.java.util.Log;
import org.ws4d.java.util.Math;

/**
 * SOAP-over-UDP server.
 * <p>
 * This server uses the {@link UDPServer} to listen for incoming UDP datagram
 * packets which contains SOAP messages.
 * </p>
 * <p>
 * The incoming datagram will be handled by the internal
 * {@link UDPDatagramHandler} if a {@link MessageReceiver} is set. Uses the
 * {@link #setReceiver(MessageReceiver)} method to set the correct receiver.
 * </p>
 */
public class SOAPoverUDPServer {

	/**
	 * The local UDP host address that this server should listen to.
	 */
	private IPAddress							ipAddress	= null;

	/**
	 * The local UDP port that this server should listen to.
	 */
	private int									port		= -1;

	private String								ifaceName;

	private final SOAPoverUDPDatagramHandler	handler;

	/**
	 * Indicates whether this server is running or not.
	 */
	private boolean								running		= false;

	/**
	 * Create a SOAP-over-UDP Server with given address and port for a specified
	 * interface.
	 * 
	 * @param address the address
	 * @param port the port
	 * @param ifaceName the name of the interface
	 * @param handler the handler which will receive incoming UDP datagrams.
	 * @throws IOException
	 */
	public SOAPoverUDPServer(IPAddress ipAddress, int port, String ifaceName, SOAPoverUDPDatagramHandler handler) throws IOException {
		this.ipAddress = ipAddress;
		this.port = port;
		this.ifaceName = ifaceName;
		this.handler = handler;
		start();
	}

	/**
	 * Starts the SOAP-over-UDP server.
	 * 
	 * @throws IOException
	 */
	public synchronized void start() throws IOException {
		if (running) return;
		UDPServer.open(ipAddress, port, ifaceName, handler);
		running = true;
	}

	/**
	 * Stops the SOAP-over-UDP server.
	 * 
	 * @throws IOException
	 */
	public synchronized void stop() throws IOException {
		if (!running) return;
		UDPServer.close(ipAddress, port, ifaceName);
		running = false;
	}

	/**
	 * Returns <code>true</code> if the SOAP-over-UDP server is running,
	 * <code>false</code> otherwise.
	 * 
	 * @return <code>true</code> if the SOAP-over-UDP server is running,
	 *         <code>false</code> otherwise.
	 */
	public synchronized boolean isRunning() {
		return running;
	}

	/**
	 * Sends a UDP datagram packet with the UDP datagram socket used for this
	 * SOAP-over-UDP server.
	 * 
	 * @param dstAddress the destination address of the datagram packet.
	 * @param dstPort the destination port of the datagram packet.
	 * @param data the content of the datagram packet.
	 * @param len the length of the datagram packet.
	 * @throws IOException
	 */
	public void send(IPAddress dstAddress, int dstPort, byte[] data, int len) throws IOException {
		UDPServer.send(ipAddress, port, ifaceName, dstAddress, dstPort, data, len);
	}

	/**
	 * Returns the handler configured on this UDP server instance.
	 * 
	 * @return this UDP server's handler
	 */
	public SOAPoverUDPDatagramHandler getHandler() {
		return handler;
	}

	/**
	 * Internal SOAP-over-UDP datagram handler.
	 */
	public static abstract class SOAPoverUDPDatagramHandler implements UDPDatagramHandler, MessageReceiver {

		/**
		 * 
		 */
		public SOAPoverUDPDatagramHandler() {
			super();
		}

		/*
		 * (non-Javadoc)
		 * @see
		 * org.ws4d.java.communication.connection.udp.UDPDatagramHandler#handle
		 * (org.ws4d.java.communication.connection.udp.Datagram,
		 * org.ws4d.java.communication.DPWSProtocolData)
		 */
		public void handle(Datagram datagram, ProtocolData protocolData) throws IOException {
			InputStream in;

			MonitorStreamFactory monFac = DPWSFramework.getMonitorStreamFactory();

			if (monFac != null) {
				in = new MonitoredInputStream(new DatagramInputStream(datagram), protocolData);
			} else {
				in = new DatagramInputStream(datagram);
			}

			final MessageReceiver r;

			if (monFac != null) {
				MonitoringContext context = monFac.getNewMonitoringContextIn(protocolData);
				r = new MonitoredMessageReceiver(this, context);
			} else {
				r = this;
			}

			SOAPMessageGeneratorFactory.getInstance().getSOAP2MessageGeneratorForCurrentThread().deliverMessage(in, r, protocolData, getDiscarder());
			in.close();
		}

		protected abstract DefaultMessageDiscarder getDiscarder();

		protected final void respond(Message message, IPAddress destAddr, int destPort, ProtocolData pd) {
			MonitorStreamFactory monFac = DPWSFramework.getMonitorStreamFactory();
			MonitoringContext context = null;
			try {
				ByteArrayBuffer buffer = SOAPMessageGeneratorFactory.getInstance().getMessage2SOAPGeneratorForCurrentThread().generateSOAPMessage(message, pd);
				UDPServer.send(this, destAddr, destPort, buffer.getBuffer(), buffer.getContentLength());

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
					if (monFac != null) {
						OutputStream o = new ByteArrayOutputStream(buffer.getContentLength());
						o = new MonitoredOutputStream(o, pd);
						context = monFac.getNewMonitoringContextOut(pd);
						o.write(buffer.getBuffer(), 0, buffer.getContentLength());
						o.flush();
						o.close();
					}
					UDPServer.send(this, destAddr, destPort, buffer.getBuffer(), buffer.getContentLength());

					if (monFac != null) {
						monFac.send(pd, context, message);
					}

					if (--repeatCount == 0) break;

					delay *= 2;
					if (delay > SOAPoverUDPClient.UDP_UPPER_DELAY) {
						delay = SOAPoverUDPClient.UDP_UPPER_DELAY;
					}

				}
			} catch (IOException e) {
				Log.error("Unable to send SOAP-over-UDP response: " + e);
				Log.printStackTrace(e);
				if (monFac != null) {
					monFac.sendFault(pd, context, e);
				}
			}
		}
	}
}
