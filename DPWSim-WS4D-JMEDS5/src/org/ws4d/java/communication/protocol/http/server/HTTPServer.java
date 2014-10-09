/*******************************************************************************
 * Copyright (c) 2009 MATERNA Information & Communications. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html. For further
 * project-related information visit http://www.ws4d.org. The most recent
 * version of the JMEDS framework can be obtained from
 * http://sourceforge.net/projects/ws4d-javame.
 ******************************************************************************/
package org.ws4d.java.communication.protocol.http.server;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;

import org.ws4d.java.DPWSFramework;
import org.ws4d.java.communication.ProtocolData;
import org.ws4d.java.communication.ProtocolException;
import org.ws4d.java.communication.connection.ip.IPAddress;
import org.ws4d.java.communication.connection.tcp.TCPConnection;
import org.ws4d.java.communication.connection.tcp.TCPConnectionHandler;
import org.ws4d.java.communication.connection.tcp.TCPListener;
import org.ws4d.java.communication.connection.tcp.TCPServer;
import org.ws4d.java.communication.monitor.MonitorStreamFactory;
import org.ws4d.java.communication.monitor.MonitoringContext;
import org.ws4d.java.communication.protocol.http.ChunkedOutputStream;
import org.ws4d.java.communication.protocol.http.HTTPGroup;
import org.ws4d.java.communication.protocol.http.HTTPInputStream;
import org.ws4d.java.communication.protocol.http.HTTPOutputStream;
import org.ws4d.java.communication.protocol.http.HTTPRequestUtil;
import org.ws4d.java.communication.protocol.http.HTTPResponse;
import org.ws4d.java.communication.protocol.http.HTTPResponseUtil;
import org.ws4d.java.communication.protocol.http.HTTPUser;
import org.ws4d.java.communication.protocol.http.header.HTTPRequestHeader;
import org.ws4d.java.communication.protocol.http.header.HTTPResponseHeader;
import org.ws4d.java.communication.protocol.http.server.responses.DefaultNotFoundResponse;
import org.ws4d.java.communication.protocol.http.server.responses.DefaultUnauthorizedResponse;
import org.ws4d.java.configuration.DPWSProperties;
import org.ws4d.java.constants.HTTPConstants;
import org.ws4d.java.message.Message;
import org.ws4d.java.structures.HashMap;
import org.ws4d.java.structures.Iterator;
import org.ws4d.java.structures.LinkedList;
import org.ws4d.java.structures.List;
import org.ws4d.java.types.InternetMediaType;
import org.ws4d.java.types.URI;
import org.ws4d.java.util.Log;
import org.ws4d.java.util.StringUtil;
import org.ws4d.java.util.TimedEntry;
import org.ws4d.java.util.WatchDog;

/**
 * This class allows the creation of an HTTP server to handle incoming HTTP
 * requests.
 */
public class HTTPServer {

	/**
	 * This is a fall back for HTTP path search.
	 * <p>
	 * If <code>true</code> the used handler search will be changed. Usually we
	 * try to match the request directly to a registered handler. If no handler
	 * were found, the {@link DefaultHTTPNotFoundHandler} will be used to handle
	 * the request. Setting {@link #BACKTRACK} <code>true</code> implies that
	 * the handlers above the given request will also be searched.
	 * </p>
	 * <h4>Example</h4>
	 * <p>
	 * If no handler is set for <strong>/home/johndoe</strong>. The request for
	 * this path will fail. With {@link #BACKTRACK} <code>true</code>, the look
	 * up will be done at <strong>/home</strong> and <strong>/</strong> too.
	 * </p>
	 */
	private static final boolean	BACKTRACK			= false;

	/**
	 * This allows to <i>eat</i> the bytes inside the HTTP request body if the
	 * handler does not read them.
	 */
	private static final boolean	EAT					= true;

	/**
	 * This is the root path of the HTTP server.
	 */
	private URI						base				= null;

	/**
	 * The host address of this HTTP server.
	 */
	private IPAddress				ipAddress			= null;

	/**
	 * The host port of this HTTP server.
	 */
	private int						port				= -1;

	/**
	 * A TCP connection handler which will handle the incoming HTTP requests.
	 */
	private HTTPConnectionHandler	handler				= new HTTPConnectionHandler();

	/**
	 * This table contains path and handler.
	 */
	private HashMap					handlers			= new HashMap();

	/**
	 * Indicates whether this server is running or not.
	 */
	private boolean					running				= false;

	/**
	 * List of active timeouts. Necessary for correct {@link #stop()}.
	 */
	private List					timeouts			= new LinkedList();

	/**
	 * Indicates whether this server should keep the connection or not.
	 */
	private boolean					keepalive			= true;

	/**
	 * Simple counter representing the number of handlers handling incoming
	 * requests at the moment.
	 */
	private static int				hand				= 0;

	/**
	 * Simple request timeout value.
	 */
	private static long				REQUEST_TIMEOUT		= 20000;

	/**
	 * This table contains the created HTTP servers.
	 */
	private static HashMap			servers				= new HashMap();

	/**
	 * Allows the shutdown of the underlying TCP client if all registrations are
	 * removed.
	 */
	private static boolean			UNREGISTER_SHUTDOWN	= false;

	/**
	 * ws-security
	 */
	private boolean					isSecure			= false;

	private String					alias				= null;

	/**
	 * HTTP Authentication
	 */
	private HashMap					authentication		= new HashMap();

	public void setAuthentication(URI resource, HTTPGroup group) {
		authentication.put(resource, group);
	}

	public HTTPGroup getAuthenticatedUser(URI resource) {
		return (HTTPGroup) authentication.get(resource);
	}

	public synchronized static HTTPServer get(IPAddress ipAddress, int port) throws IOException {
		return get(ipAddress, port, false, null);
	}

	/**
	 * Returns a HTTP server for the given address and port. If no such server
	 * exists, a new server will be created.
	 * <p>
	 * The HTTP server is started at creation time.
	 * </p>
	 * 
	 * @param address the address of the HTTP server.
	 * @param port the port for the server.
	 * @return a new HTTP server.
	 * @throws IOException Throws exception if the port could not be opened at
	 *             the given address.
	 */
	public synchronized static HTTPServer get(IPAddress ipAddress, int port, boolean secure, String alias) throws IOException {
		String key;
		HTTPServer server;
		if (port == 0) {
			server = new HTTPServer(ipAddress, port, secure, alias);
			key = ipAddress.getAddress() + "@" + server.port;
		} else {
			key = ipAddress.getAddress() + "@" + port;
			server = (HTTPServer) servers.get(key);
			if (server != null) return server;
			server = new HTTPServer(ipAddress, port, secure, alias);
		}
		servers.put(key, server);
		return server;
	}

	public synchronized static void unregisterAndStop(HTTPServer server) throws IOException {

		String key = server.ipAddress.getAddress() + "@" + server.port;
		servers.remove(key);

		server.stop();
	}

	private HTTPServer(IPAddress ipAddress, int port, boolean secure, String alias) throws IOException {
		DPWSProperties properies = DPWSProperties.getInstance();
		keepalive = properies.getHTTPServerKeepAlive();

		this.ipAddress = ipAddress;
		this.port = port;
		this.isSecure = secure;
		this.alias = alias;

		String httpSchema = (secure ? HTTPConstants.HTTPS_SCHEMA : HTTPConstants.HTTP_SCHEMA);

		start();

		// generate base URI after actual port has been assigned
		base = new URI(httpSchema + "://" + ipAddress.getAddressWithoutNicId() + ":" + this.port);
	}

	/**
	 * Registers a relative HTTP path with a given {@link HTTPRequestHandler}.
	 * 
	 * @param path the HTTP path.
	 * @param handler the HTTP handler which should handle the request.
	 */
	public void register(String path, HTTPRequestHandler handler, HTTPGroup user) {
		URI registerURI = new URI(path, base);
		handlers.put(registerURI, handler);
		// TODO: unregister
		if (user != null) {
			setAuthentication(registerURI, user);
		}
	}

	/**
	 * Registers a relative HTTP path and a content type with a given
	 * {@link HTTPRequestHandler}.
	 * 
	 * @param path the HTTP path.
	 * @param type the HTTP content type.
	 * @param handler the HTTP handler which should handle the request.
	 */
	public void register(String path, InternetMediaType type, HTTPRequestHandler handler, HTTPGroup user) {
		URI registerURI = new URI(path, base);
		MappingEntry entry = new MappingEntry(registerURI, type);
		handlers.put(entry, handler);
		// TODO: unregister
		if (user != null) {
			setAuthentication(registerURI, user);
		}
	}

	/**
	 * Removes registration of a relative HTTP path for a
	 * {@link HTTPRequestHandler}.
	 * 
	 * @param path the HTTP path.
	 * @return the removed {@link HTTPRequestHandler}.
	 */
	public HTTPRequestHandler unregister(String path) {
		URI registerURI = new URI(path, base);
		HTTPRequestHandler handler = (HTTPRequestHandler) handlers.remove(registerURI);
		if (UNREGISTER_SHUTDOWN && handlers.isEmpty()) {
			try {
				TCPServer.close(ipAddress, port);
			} catch (IOException e) {
				Log.error("Cannot shutdown TCP server after all registrations removed. " + e.getMessage());
			}
		}
		return handler;
	}

	/**
	 * Removes registration of a relative HTTP path and content type for a HTTP
	 * handler.
	 * 
	 * @param path the HTTP path.
	 * @param type the HTTP content type.
	 * @return the removed {@link HTTPRequestHandler}.
	 */
	public HTTPRequestHandler unregister(String path, InternetMediaType type) {
		URI registerURI = new URI(path, base);
		MappingEntry entry = new MappingEntry(registerURI, type);
		HTTPRequestHandler handler = (HTTPRequestHandler) handlers.remove(entry);
		if (UNREGISTER_SHUTDOWN && handlers.isEmpty()) {
			try {
				TCPServer.close(ipAddress, port);
			} catch (IOException e) {
				Log.error("Cannot shutdown TCP server after all registrations removed. " + e.getMessage());
			}
		}
		return handler;
	}

	/**
	 * Starts the HTTP server.
	 * 
	 * @throws IOException
	 */
	public synchronized void start() throws IOException {
		if (running) return;

		TCPListener listener;
		if (!isSecure)
			listener = TCPServer.open(ipAddress, port, handler);
		else
			listener = TCPServer.open(ipAddress, port, handler, true, this.alias);

		if (port == 0) {
			port = listener.getPort();
		}
		running = true;
	}

	/**
	 * Stops the HTTP server.
	 * 
	 * @throws IOException
	 */
	public synchronized void stop() throws IOException {
		if (!running) return;
		TCPServer.close(ipAddress, port);
		/*
		 * Unregister all timeouts.
		 */
		Iterator it = timeouts.iterator();
		while (it.hasNext()) {
			HandlerTimeOut timeout = (HandlerTimeOut) it.next();
			WatchDog.getInstance().unregister(timeout);
			it.remove();
		}
		running = false;
	}

	/**
	 * Returns <code>true</code> if the HTTP server is running,
	 * <code>false</code> otherwise.
	 * 
	 * @return <code>true</code> if the HTTP server is running,
	 *         <code>false</code> otherwise.
	 */
	public synchronized boolean isRunning() {
		return running;
	}

	/**
	 * @return the port
	 */
	public int getPort() {
		return port;
	}

	/**
	 * TCP handler which handles the incoming HTTP requests.
	 */
	private class HTTPConnectionHandler implements TCPConnectionHandler {

		public void handle(TCPConnection connection) throws IOException {
			/*
			 * Default HTTP 1.1 behavior.
			 */
			hand++;

			boolean firstRequest = true;

			HandlerTimeOut timeout = new HandlerTimeOut(connection, keepalive);

			ProtocolData protocolData = connection.getProtocolData();
			MonitorStreamFactory monFac = DPWSFramework.getMonitorStreamFactory();

			/*
			 * Keep persistent HTTP connection.
			 */
			while (timeout.keepAlive() || firstRequest) {
				firstRequest = false;

				MonitoringContext context = null;

				if (monFac != null) {
					context = monFac.getNewMonitoringContextIn(protocolData);
				}

				InputStream in = connection.getInputStream();
				OutputStream out = connection.getOutputStream();

				HTTPRequestHeader requestHeader = null;
				try {
					WatchDog.getInstance().register(timeout, REQUEST_TIMEOUT);
					timeouts.add(timeout);
					requestHeader = HTTPRequestUtil.handleRequest(in);
					WatchDog.getInstance().unregister(timeout);
				} catch (ProtocolException e) {
					/*
					 * Something wrong in the shiny HTTP wonderland?! Send
					 * internal server error response and close the connection.
					 */
					WatchDog.getInstance().unregister(timeout);
					HTTPResponseHeader responseHeader = HTTPResponseUtil.getResponseHeader(400);
					responseHeader.addHeaderFieldValue(HTTPConstants.HTTP_HEADER_CONNECTION, HTTPConstants.HTTP_HEADERVALUE_CONNECTION_CLOSE);
					responseHeader.addHeaderFieldValue(HTTPConstants.HTTP_HEADER_CONTENT_LENGTH, "0");
					String note = "Invalid HTTP request: " + e.getMessage();
					responseHeader.toStream(out);
					out.write(note.getBytes());
					Log.warn("Closing HTTP connection. " + note + ".");
					break;
				}

				/*
				 * No header? This happens if the input stream reaches the end.
				 */
				if (requestHeader == null) {
					break;
				}

				if (Log.isDebug()) {
					Log.debug("<I> " + requestHeader + " from " + protocolData.getSourceAddress() + ", " + connection, Log.DEBUG_LAYER_COMMUNICATION);
				}

				/*
				 * Get some parameters from the HTTP request.
				 */
				String path = requestHeader.getRequest();
				/*
				 * Check for absolute path.
				 */
				if (path.startsWith(HTTPConstants.HTTP_SCHEMA)) {
					URI absoluteURI = new URI(path);
					path = absoluteURI.getPath();
				}
				String method = requestHeader.getMethod();
				String encodingRequest = requestHeader.getHeaderFieldValue(HTTPConstants.HTTP_HEADER_TRANSFER_ENCODING);

				String bodyLength = requestHeader.getHeaderFieldValue(HTTPConstants.HTTP_HEADER_CONTENT_LENGTH);
				int size = -1;
				if (bodyLength != null) {
					size = Integer.parseInt(bodyLength.trim());
				}

				// Add the Path to ProtocolData
				connection.getProtocolData().setTransportAddress(new URI(base.toString(), path));

				/*
				 * Check for necessary length.
				 */
				if (!HTTPConstants.HTTP_HEADERVALUE_TRANSFERCODING_CHUNKED.equals(encodingRequest) && size < 0 && HTTPConstants.HTTP_METHOD_POST.equals(method)) {
					HTTPResponseHeader response = HTTPResponseUtil.getResponseHeader(400);
					response.addHeaderFieldValue(HTTPConstants.HTTP_HEADER_CONNECTION, HTTPConstants.HTTP_HEADERVALUE_CONNECTION_CLOSE);
					String note = "Neither content length nor chunked encoding found. Cannot determinate content length.";
					response.toStream(out);
					out.write(note.getBytes());
					break;
				}

				String mediaType = requestHeader.getHeaderFieldValue(HTTPConstants.HTTP_HEADER_CONTENT_TYPE);
				InternetMediaType type = new InternetMediaType(mediaType);

				/*
				 * Does the client wish to close the connection? Disable
				 * keep-alive if necessary.
				 */
				String con = requestHeader.getHeaderFieldValue(HTTPConstants.HTTP_HEADER_CONNECTION);
				if (HTTPConstants.HTTP_HEADERVALUE_CONNECTION_CLOSE.equals(con)) {
					timeout.setKeepAlive(false);
				}

				/*
				 * Get TE (RFC2616 14.39)
				 */
				boolean responseChunkedTrailer = false;
				String te = requestHeader.getHeaderFieldValue(HTTPConstants.HTTP_HEADER_TE);
				if (te != null) {
					String[] tes = StringUtil.split(te, ',');
					for (int t = 0; t < tes.length; t++) {
						if (tes[t].indexOf(HTTPConstants.HTTP_HEADERVALUE_TE_TRAILERS) >= 0) {
							responseChunkedTrailer = true;
						}
					}
				}

				/*
				 * Wrap the HTTP body inside a new stream.
				 */
				in = new HTTPInputStream(in, encodingRequest, size);

				/*
				 * The requested URI
				 */
				URI requestedURI = new URI(base, requestHeader.getRequest());

				/*
				 * Try to find the HTTP handler for this request.
				 */
				HTTPRequestHandler handler = getHTTPHandler(path, type);

				/*
				 * This object will contain the HTTP response from the handler.
				 */
				HTTPResponse response = null;

				/*
				 * Handle request (HTTP exchange) if possible. Send 404
				 * "Not found" if no handler found.
				 */

				if (handler != null) {
					
					if (authentication.get(requestedURI) != null) {
						// TODO: Authentication

						HTTPUser requestedUser = null;
						HTTPGroup authGroup = null;
						String basicCredentials;

						authGroup = (HTTPGroup) authentication.get(requestedURI);
						basicCredentials = requestHeader.getHeaderFieldValue(HTTPConstants.HTTP_HEADER_AUTHORIZATION);

						if (basicCredentials != null) {
							requestedUser = HTTPUser.createUserFromBase64String(basicCredentials.substring("Basic".length()));
						}

						if (requestedUser == null || !authGroup.inList(requestedUser)) {
							/*
							 * Default 401 Unauthorized.
							 */
							response = new DefaultUnauthorizedResponse(requestHeader);
						}
					} 

					if (response == null) {
						try {
							response = handler.handle(requestedURI, requestHeader, in, connection.getProtocolData(), context);
						} catch (IOException e) {
							/*
							 * The handler got an exception. Shit happens... We
							 * should send a HTTP 500 internal server error.
							 * This can only happen while reading the input
							 * stream.
							 */
							String note = "The registered HTTP handler (" + handler.getClass().getName() + ") got an exception. " + e.getMessage();
							Log.error(note);
							HTTPResponseHeader responseHeader = HTTPResponseUtil.getResponseHeader(500);
							responseHeader.addHeaderFieldValue(HTTPConstants.HTTP_HEADER_CONTENT_LENGTH, "0");
							responseHeader.addHeaderFieldValue("JMEDS-Debug", requestHeader.getRequest());
							responseHeader.toStream(out);
							out.write(note.getBytes());
							Log.warn("Closing HTTP connection. " + note + ".");
							break;
						}
					}
				}

				if (response == null || response.getResponseHeader() == null) {

					/*
					 * Default 404 Not found.
					 */
					response = new DefaultNotFoundResponse(requestHeader);
				}

				// TODO: Authorization????

				/*
				 * Analyze and serialize the HTTP response header and create a
				 * output stream to write the HTTP response body.
				 */

				HTTPResponseHeader responseHeader = response.getResponseHeader();

				/*
				 * Does the server (the generated response) contain a "Date"
				 * field?
				 */
				String date = responseHeader.getHeaderFieldValue(HTTPConstants.HTTP_HEADER_DATE);
				if (date == null) {
					Date d = new Date();
					responseHeader.addHeaderFieldValue(HTTPConstants.HTTP_HEADER_DATE, StringUtil.getHTTPDate(d.getTime()));
				}

				/*
				 * Does the server (the generated response) contain a
				 * "Last-Modified" field?
				 */
				String ifModSince = requestHeader.getHeaderFieldValue(HTTPConstants.HTTP_HEADER_IF_MODIFIED_SINCE);
				long ifModSinceL = -1;
				if (ifModSince != null) {
					ifModSinceL = StringUtil.getHTTPDateAsLong(ifModSince);
				}
				String lastMod = responseHeader.getHeaderFieldValue(HTTPConstants.HTTP_HEADER_LAST_MODIFIED);
				long lastModL = -1;
				if (lastMod != null) {
					lastModL = StringUtil.getHTTPDateAsLong(lastMod);
				}

				if (ifModSinceL != -1 && lastModL != -1 && lastModL <= ifModSinceL) {
					/*
					 * Resource was not modified...
					 */
					responseHeader = HTTPResponseUtil.getResponseHeader(304);
					Date d = new Date();
					responseHeader.addHeaderFieldValue(HTTPConstants.HTTP_HEADER_DATE, StringUtil.getHTTPDate(d.getTime()));
					responseHeader.toStream(out);
					if (Log.isDebug()) {
						Log.debug("Resource at " + requestedURI + " not modified since " + ifModSince + ".");
					}
					break;
				}
				

				/*
				 * Does the server (the generated response) wish to close the
				 * connection? Disable keep-alive if necessary.
				 */
				con = responseHeader.getHeaderFieldValue(HTTPConstants.HTTP_HEADER_CONNECTION);
				if (HTTPConstants.HTTP_HEADERVALUE_CONNECTION_CLOSE.equals(con)) {
					timeout.setKeepAlive(false);
				}

				/*
				 * Does the global property prohibit the keep alive function?
				 */
				if (!keepalive) {
					responseHeader.addHeaderFieldValue(HTTPConstants.HTTP_HEADER_CONNECTION, HTTPConstants.HTTP_HEADERVALUE_CONNECTION_CLOSE);
					timeout.setKeepAlive(false);
				}

				String encodingResponse = responseHeader.getHeaderFieldValue(HTTPConstants.HTTP_HEADER_TRANSFER_ENCODING);
				int contentLengthResponse = (responseHeader.getHeaderFieldValue(HTTPConstants.HTTP_HEADER_CONTENT_LENGTH) != null) ? Integer.parseInt(responseHeader.getHeaderFieldValue(HTTPConstants.HTTP_HEADER_CONTENT_LENGTH).trim()) : -1;

				
				/*
				 * Change context from incoming to outgoing.
				 */
				ProtocolData pOut = null;
				if (monFac != null) {
					pOut = protocolData.createSwappedProtocolData();
					context = monFac.getNewMonitoringContextOut(pOut);
				}

				/*
				 * Header has chunked encoding set, but we should avoid chunks
				 * ...
				 */
				ByteArrayOutputStream buffer = null;

				boolean buffered = false;
				if (!HTTPConstants.HTTP_HEADERVALUE_TRANSFERCODING_CHUNKED.equals(encodingResponse) && contentLengthResponse == -1) {
					buffer = new ByteArrayOutputStream();
					response.serializeResponseBody(requestedURI, requestHeader, buffer, connection.getProtocolData() == null ? null : connection.getProtocolData().createSwappedProtocolData(), context);
					// response.serializeResponseBody(requestedURI,
					// requestHeader, buffer,
					// ProtocolData.swap(connection.getProtocolData()),
					// context);

					contentLengthResponse = buffer.size();
					responseHeader.addHeaderFieldValue(HTTPConstants.HTTP_HEADER_CONTENT_LENGTH, Integer.toString(contentLengthResponse));
					buffered = true;
				}
				
				if (Log.isDebug()) {
					Log.debug("<O> " + responseHeader + " to " + protocolData.getSourceAddress() + ", " + connection, Log.DEBUG_LAYER_COMMUNICATION);
				}
								
				responseHeader.toStream(out);

				/*
				 * Serialize the HTTP response body.
				 */
				if (HTTPConstants.HTTP_METHOD_HEAD.equals(requestHeader.getMethod())) {
					out = new HTTPOutputStream(out, 0);

				} else {
					if (HTTPConstants.HTTP_HEADERVALUE_TRANSFERCODING_CHUNKED.equals(encodingResponse)) {
						out = new ChunkedOutputStream(out, responseChunkedTrailer);
					} else {
						out = new HTTPOutputStream(out, contentLengthResponse);
					}

				}

				if (!buffered) {
					// response.serializeResponseBody(requestedURI,
					// requestHeader, out,
					// ProtocolData.swap(connection.getProtocolData()),
					// context);
					response.serializeResponseBody(requestedURI, requestHeader, out, connection.getProtocolData() == null ? null : connection.getProtocolData().createSwappedProtocolData(), context);
				} else {
					out.write(buffer.toByteArray());
				}

				/*
				 * Was this a chunked response? Write lust chunk!
				 */

				if (HTTPConstants.HTTP_HEADERVALUE_TRANSFERCODING_CHUNKED.equals(encodingResponse)) {
					ChunkedOutputStream.writeLastChunk((ChunkedOutputStream) out);
				}

				out.flush();

				response.waitFor();

				if (monFac != null) {
					Message m = context.getMessage();
					if (m != null) {
						monFac.send(pOut, context, m);
					}
				}

				/*
				 * Should we eat the omitted bytes or not?
				 */
				consumeStream(in);
			}

			hand--;
		}

		/**
		 * @param in
		 * @throws IOException
		 */
		private void consumeStream(InputStream in) throws IOException {
			if (EAT) {
				int n = -1;
				while (in.read() != -1) {
					/*
					 * Eat the omitted bytes from stream...
					 */
					n++;
				}
				if (n > -1) {
					Log.warn("The registered handler has not consumed the HTTP body from the request. Eating " + n + " bytes.");
				}
			}
		}
	}

	/**
	 * Returns the HTTP handler for the given path and content type.
	 * <p>
	 * This method will search for the HTTP handler depending on the value of
	 * the {@link HTTPServer#BACKTRACK} field.
	 * </p>
	 * 
	 * @param path the path.
	 * @param type the content type.
	 * @return the HTTP handler which match path and content type.
	 */
	private HTTPRequestHandler getHTTPHandler(String path, InternetMediaType type) {
		URI requestURI = new URI(path, base);
		MappingEntry entry = new MappingEntry(requestURI, type);

		HTTPRequestHandler handler = null;

		/*
		 * Tries to get specific handler for the given type.
		 */
		handler = (HTTPRequestHandler) handlers.get(entry);

		/*
		 * No specific handler found? Tries to find an handler which accepts
		 * every type for this address.
		 */
		if (handler == null) {
			handler = (HTTPRequestHandler) handlers.get(requestURI);
		}

		if (BACKTRACK) {
			/*
			 * No handler found? Does some backtracking... Looks up along the
			 * path, maybe some handler is there.
			 */
			if (handler == null && requestURI.getPathDeepness() > 0) {
				URI backtrackURI = requestURI;
				while (backtrackURI.getPathDeepness() > 0) {
					int deepness = backtrackURI.getPathDeepness();
					deepness--;
					String backtrackPath = backtrackURI.getPath(deepness);
					backtrackURI = new URI(backtrackURI, backtrackPath);
					entry = new MappingEntry(requestURI, type);
					handler = (HTTPRequestHandler) handlers.get(entry);
					if (handler == null) {
						handler = (HTTPRequestHandler) handlers.get(backtrackURI);
					}
					if (handler != null) {
						break;
					}
				}
			}
		}
		return handler;
	}

	/**
	 * HTTP timeout.
	 */
	private class HandlerTimeOut extends TimedEntry {

		private TCPConnection	connection	= null;

		private boolean			keepalive	= true;

		private HandlerTimeOut(TCPConnection connection, boolean keepalive) {
			this.connection = connection;
			this.keepalive = keepalive;
		}

		protected void timedOut() {
			keepalive = false;
			if (Log.isDebug()) {
				Log.debug("<I> Incoming TCP connection (" + connection.getIdentifier() + ") timeout after " + REQUEST_TIMEOUT + "ms.", Log.DEBUG_LAYER_COMMUNICATION);
			}
			try {
				connection.close();
			} catch (IOException e) {
				Log.error("Cannot close server connection. " + e.getMessage());
			}
		}

		public boolean keepAlive() {
			return keepalive;
		}

		public void setKeepAlive(boolean keepalive) {
			this.keepalive = keepalive;
		}

	}

	/**
	 * This entry contains a URI and content type.
	 */
	private class MappingEntry {

		private URI					uri		= null;

		private InternetMediaType	type	= null;

		MappingEntry(URI uri, InternetMediaType type) {
			this.uri = uri;
			this.type = type;
		}

		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result + ((type == null) ? 0 : type.hashCode());
			result = prime * result + ((uri == null) ? 0 : uri.hashCode());
			return result;
		}

		public boolean equals(Object obj) {
			if (this == obj) return true;
			if (obj == null) return false;
			if (getClass() != obj.getClass()) return false;
			MappingEntry other = (MappingEntry) obj;
			if (!getOuterType().equals(other.getOuterType())) return false;
			if (type == null) {
				if (other.type != null) return false;
			} else if (!type.equals(other.type)) return false;
			if (uri == null) {
				if (other.uri != null) return false;
			} else if (!uri.equals(other.uri)) return false;
			return true;
		}

		private HTTPServer getOuterType() {
			return HTTPServer.this;
		}

	}

}
