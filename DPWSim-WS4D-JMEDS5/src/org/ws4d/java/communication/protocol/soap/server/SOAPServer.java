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

import java.io.IOException;
import java.io.InputStream;

import org.ws4d.java.DPWSFramework;
import org.ws4d.java.communication.ProtocolData;
import org.ws4d.java.communication.connection.ip.IPAddress;
import org.ws4d.java.communication.monitor.MonitorStreamFactory;
import org.ws4d.java.communication.monitor.MonitoredMessageReceiver;
import org.ws4d.java.communication.monitor.MonitoringContext;
import org.ws4d.java.communication.protocol.http.HTTPGroup;
import org.ws4d.java.communication.protocol.http.HTTPResponse;
import org.ws4d.java.communication.protocol.http.header.HTTPRequestHeader;
import org.ws4d.java.communication.protocol.http.server.HTTPRequestHandler;
import org.ws4d.java.communication.protocol.http.server.HTTPServer;
import org.ws4d.java.communication.protocol.soap.SOAPResponse;
import org.ws4d.java.communication.protocol.soap.generator.MessageReceiver;
import org.ws4d.java.communication.protocol.soap.generator.SOAPMessageGeneratorFactory;
import org.ws4d.java.message.Message;
import org.ws4d.java.structures.HashMap;
import org.ws4d.java.types.InternetMediaType;
import org.ws4d.java.types.URI;

/**
 * This class allows the creation of a SOAP Server to handle incoming SOAP
 * message requests.
 */
public class SOAPServer {

	/**
	 * The underlying HTTP server.
	 */
	private HTTPServer		server	= null;

	/**
	 * This table contains the created SOAP servers.
	 */
	private static HashMap	servers	= new HashMap();

	/**
	 * Returns a SOAP server based on the given HTTP server implementation. If
	 * no server exists, a new server will be created.
	 * 
	 * @param server the HTTP server to use.
	 * @return the new SOAP server.
	 */
	public synchronized static SOAPServer get(HTTPServer server) {
		SOAPServer soapsrv = (SOAPServer) servers.get(server);
		if (soapsrv != null) return soapsrv;
		soapsrv = new SOAPServer(server);
		servers.put(server, soapsrv);
		return soapsrv;
	}

	public synchronized static void unregisterAndStop(SOAPServer server) throws IOException {
		HTTPServer httpServer = server.getHTTPServer();

		servers.remove(httpServer);

		server.stop();

		HTTPServer.unregisterAndStop(httpServer);
	}

	/**
	 * Returns a SOAP server and the underlying HTTP server for the given
	 * address and port. If no server exists, a new server will be created.
	 * 
	 * @param address the host address for the underlying HTTP server.
	 * @param port the port for the underlying HTTP server.
	 * @return the new SOAP server.
	 * @throws IOException Throws exception if the HTTP server could not listen
	 *             to the given address and port.
	 */
	public synchronized static SOAPServer get(IPAddress ipAddress, int port, boolean secure, String alias) throws IOException {
		HTTPServer server = HTTPServer.get(ipAddress, port, secure, alias);
		return get(server);
	}

	/**
	 * Creates the SOAP server with the HTTP server.
	 * 
	 * @param server the underlying HTTP server.
	 */
	private SOAPServer(HTTPServer server) {
		this.server = server;
	}

	/**
	 * Registers a HTTP path for a given {@link MessageReceiver}.
	 * <p>
	 * The receiver will receive incoming SOAP messages which match the HTTP
	 * path.
	 * </p>
	 * 
	 * @param path HTTP path.
	 * @param receiver the SOAP message receiver
	 */
	public void register(String path, SOAPHandler handler, HTTPGroup user) {
		server.register(path, InternetMediaType.getSOAPXML(), handler, user);
	}

	/**
	 * Removes the registration of a {@link MessageReceiver} for a given HTTP
	 * path.
	 * 
	 * @param path the HTTP path.
	 * @return the removed {@link MessageReceiver}.
	 */
	public MessageReceiver unregister(String path) {
		return (SOAPHandler) server.unregister(path, InternetMediaType.getSOAPXML());
	}

	/**
	 * Returns the underlying HTTP server.
	 * 
	 * @return the HTTP server.
	 */
	public HTTPServer getHTTPServer() {
		return server;
	}

	/**
	 * Starts the SOAP server.
	 * 
	 * @throws IOException
	 */
	public synchronized void start() throws IOException {
		server.start();
	}

	/**
	 * Stops the SOAP Server.
	 * 
	 * @throws IOException
	 */
	public synchronized void stop() throws IOException {
		server.stop();
	}

	/**
	 * The default HTTP handler which handles SOAP-over-HTTP requests.
	 */
	public static abstract class SOAPHandler implements HTTPRequestHandler, MessageReceiver {

		// key = thread, value = HTTPResponse
		private final HashMap	responses	= new HashMap();

		protected SOAPHandler() {
			super();
		}

		/*
		 * (non-Javadoc)
		 * @see
		 * org.ws4d.java.communication.protocol.httpx.server.HTTPRequestHandler
		 * #handle(org.ws4d.java.types.uri.URI,
		 * org.ws4d.java.communication.protocol.http.HTTPRequestHeader,
		 * java.io.InputStream)
		 */
		public final HTTPResponse handle(URI request, HTTPRequestHeader header, InputStream body, ProtocolData protocolData, MonitoringContext context) throws IOException {
			/*
			 * Gets the HTTP request body if possible
			 */

			final MessageReceiver r;

			MonitorStreamFactory monFac = DPWSFramework.getMonitorStreamFactory();
			if (monFac != null) {
				r = new MonitoredMessageReceiver(this, context);
			} else {
				r = this;
			}

			SOAPMessageGeneratorFactory.getInstance().getSOAP2MessageGeneratorForCurrentThread().deliverMessage(body, r, protocolData);

			/*
			 * after delivering the request message, the corresponding response
			 * will be immediately available within field 'response'
			 */
			synchronized (responses) {
				return (HTTPResponse) responses.remove(Thread.currentThread());
			}
		}

		protected final void respond(int httpStatus, Message responseMessage) {
			/*
			 * this takes care of attachments sufficiently (concerns Invoke and
			 * Fault messages)
			 */

			synchronized (responses) {
				responses.put(Thread.currentThread(), new SOAPResponse(httpStatus, responseMessage));
			}
		}

	}

}
