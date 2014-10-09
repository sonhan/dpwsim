/*******************************************************************************
 * Copyright (c) 2009 MATERNA Information & Communications. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html. For further
 * project-related information visit http://www.ws4d.org. The most recent
 * version of the JMEDS framework can be obtained from
 * http://sourceforge.net/projects/ws4d-javame.
 ******************************************************************************/
package org.ws4d.java.communication.protocol.http;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.ws4d.java.DPWSFramework;
import org.ws4d.java.communication.DPWSProtocolData;
import org.ws4d.java.communication.ProtocolData;
import org.ws4d.java.communication.ProtocolException;
import org.ws4d.java.communication.connection.ip.IPAddress;
import org.ws4d.java.communication.connection.tcp.Socket;
import org.ws4d.java.communication.connection.tcp.SocketFactory;
import org.ws4d.java.communication.monitor.MonitorStreamFactory;
import org.ws4d.java.communication.monitor.MonitoredInputStream;
import org.ws4d.java.communication.monitor.MonitoredOutputStream;
import org.ws4d.java.communication.monitor.MonitoringContext;
import org.ws4d.java.communication.monitor.ResourceLoader;
import org.ws4d.java.communication.protocol.http.header.HTTPRequestHeader;
import org.ws4d.java.communication.protocol.http.header.HTTPResponseHeader;
import org.ws4d.java.constants.HTTPConstants;
import org.ws4d.java.constants.MIMEConstants;
import org.ws4d.java.security.DPWSSecurityManager;
import org.ws4d.java.security.SecurityManager;
import org.ws4d.java.structures.HashMap;
import org.ws4d.java.types.InternetMediaType;
import org.ws4d.java.types.URI;
import org.ws4d.java.util.Log;

/**
 * Utility class for handling HTTP requests.
 */
public class HTTPRequestUtil {

	protected static final String	FAULT_METHOD_NOT_SUPPORTED	= "HTTP Method not supported.";

	// predefined exception messages.
	protected static final String	FAULT_UNEXPECTED_END		= "Unexpected end of stream.";

	protected static final String	FAULT_MALFORMED_REQUEST		= "Malformed HTTP request line.";

	protected static final String	FAULT_MALFORMED_HEADERFIELD	= "Malformed HTTP header field.";

	protected static final String	FAULT_MALFORMED_CHUNK		= "Malformed HTTP chunk header.";

	// HTTP stuff
	protected static final String[]	supportedMethods			= { HTTPConstants.HTTP_METHOD_GET, HTTPConstants.HTTP_METHOD_HEAD, HTTPConstants.HTTP_METHOD_POST };

	protected static int			delta						= -1;

	static {
		/*
		 * Get maximal length for HTTP method string. After this length we
		 * should know whether the incoming data is HTTP or not.
		 */
		for (int k = 0; k < supportedMethods.length; k++) {
			if (supportedMethods[k].length() > delta) {
				delta = supportedMethods[k].length();
			}
		}
	}

	/**
	 * We are shy!
	 */
	private HTTPRequestUtil() {

	}

	public static HTTPRequestHeader handleRequest(InputStream in) throws IOException, ProtocolException {
		/*
		 * This method handles the incoming HTTP connection and looks for the
		 * HTTP header and payload.
		 */
		String method = null;
		String request = null;
		String version = null;
		method = HTTPUtil.readElement(in, delta);
		if (method == null || method.length() == 0) {
			return null;
		} else {
			// supported HTTP method found?
			boolean supported = false;
			for (int k = 0; k < supportedMethods.length; k++) {
				if (method.equals(supportedMethods[k])) {
					supported = true;
					break;
				}
			}
			if (!supported) {
				throw new ProtocolException(HTTPRequestUtil.FAULT_METHOD_NOT_SUPPORTED + " (" + method + ")");
			}
		}
		// Read the HTTP request
		request = HTTPUtil.readElement(in);

		// Read the HTTP version
		version = HTTPUtil.readRequestVersion(in);

		// Read the HTTP header fields
		HashMap headerfields = new HashMap();
		HTTPUtil.readHeaderFields(in, headerfields);

		// Create HTTP header object
		return new HTTPRequestHeader(method, request, version, headerfields, null);
	}

	/**
	 * Creates a request URI for the given request (relative request) and
	 * endpoint.
	 * 
	 * @param request the HTTP request.
	 * @param address the host address.
	 * @return the <code>URI</code> representing this request.
	 */
	public static URI createRequestURI(String request, String address) {
		// Create URI object for this request
		// create base host address from endpoint information.
		String hostAdr = HTTPConstants.HTTP_SCHEMA + "://" + address;

		URI host = new URI(hostAdr);
		URI requestURI = new URI(request, host);
		return requestURI;
	}

	/**
	 * Creates an HTTP request header for HTTP POST with application/soap+xml
	 * content type.
	 * 
	 * @param request the URI to which the header is sent.
	 * @return the HTTP request header.
	 */
	public static HTTPRequestHeader getDPWSPOSTHeader(String request) {
		HashMap headerfields = new HashMap();
		InternetMediaType type = new InternetMediaType(MIMEConstants.MEDIATYPE_APPLICATION, MIMEConstants.SUBTYPE_SOAPXML);
		headerfields.put(HTTPConstants.HTTP_HEADER_CONTENT_TYPE, type.getMediaType());
		HTTPRequestHeader header = new HTTPRequestHeader(HTTPConstants.HTTP_METHOD_POST, request, HTTPConstants.HTTP_VERSION11, headerfields);
		return header;
	}

	/**
	 * Creates an HTTP request header for HTTP GET with application/soap+xml
	 * content type.
	 * 
	 * @param request the URI to which the header is sent.
	 * @return the HTTP request header.
	 */
	public static HTTPRequestHeader getDPWSGETHeader(String request) {
		HashMap headerfields = new HashMap();
		InternetMediaType type = new InternetMediaType(MIMEConstants.MEDIATYPE_APPLICATION, MIMEConstants.SUBTYPE_SOAPXML);
		headerfields.put(HTTPConstants.HTTP_HEADER_CONTENT_TYPE, type.getMediaType());
		HTTPRequestHeader header = new HTTPRequestHeader(HTTPConstants.HTTP_METHOD_GET, request, HTTPConstants.HTTP_VERSION11, headerfields);
		return header;
	}

	/**
	 * Writes an HTTP GET request header to the stream for the given request URI
	 * and with given media type (e.g. application/soap+xml). Can be set to
	 * chunked mode if the length of followed communication cannot be
	 * determined. The returned <code>OutputStream</code> MUST be used, it
	 * should be ensure that the chnuks are written correctly.
	 * 
	 * @param out the output stream to which to write the HTTP request.
	 * @param method the HTTP request method.
	 * @param request the request URI.
	 * @param type the internet media type.
	 * @param chunked <code>true</code> if a special chunked output stream
	 *            should be returned, <code>false</code> otherwise.
	 * @param trailer <code>true</code> if the chunk trailer should be appended
	 *            at the end, <code>false</code> otherwise.
	 * @return <code>ChunkedOutputStream</code> if <code>chunked</code> is true,
	 *         the normal output stream otherwise.
	 * @throws IOException
	 */
	public static OutputStream writeRequest(OutputStream out, String method, String request, InternetMediaType type, boolean chunked, boolean trailer) throws IOException {
		return writeRequest(out, method, request, null, type, chunked, trailer);
	}

	/**
	 * Writes an HTTP GET request header to the stream for the given request URI
	 * and with given media type (e.g. application/soap+xml). Can be set to
	 * chunked mode, if the length of followed communication cannot be
	 * determined. The returned <code>OutputStream</code> MUST be used, it
	 * should ensure that the chnuks are written correctly.
	 * 
	 * @param out the output stream to write the HTTP request to.
	 * @param method the HTTP request method.
	 * @param request the request URI.
	 * @param headerfields HTTP headerfields.
	 * @param type the internet media type.
	 * @param chunked <code>true</code> if a special chunked output stream
	 *            should be returned, <code>false</code> otherwise.
	 * @param trailer <code>true</code> if the chunk trailer should be appended
	 *            at the end, <code>false</code> otherwise.
	 * @return <code>ChunkedOutputStream</code> if <code>chunked</code> is true,
	 *         the normal output stream otherwise.
	 * @throws IOException
	 */
	public static OutputStream writeRequest(OutputStream out, String method, String request, HashMap headerfields, InternetMediaType type, boolean chunked, boolean trailer) throws IOException {
		if (method == null || (!method.equals(HTTPConstants.HTTP_METHOD_GET) && !method.equals(HTTPConstants.HTTP_METHOD_POST))) {
			throw new IOException("No HTTP method set.");
		}

		if (headerfields == null) {
			headerfields = new HashMap();
		}

		HTTPRequestHeader header = null;

		if (method.equals(HTTPConstants.HTTP_METHOD_POST)) {
			header = new HTTPRequestHeader(HTTPConstants.HTTP_METHOD_POST, request, HTTPConstants.HTTP_VERSION11, headerfields);
		} else {
			header = new HTTPRequestHeader(HTTPConstants.HTTP_METHOD_GET, request, HTTPConstants.HTTP_VERSION11, headerfields);
		}

		if (Log.isDebug()) {
			Log.debug("<O> " + header.toString(), Log.DEBUG_LAYER_COMMUNICATION);
		}

		header.addHeaderFieldValue(HTTPConstants.HTTP_HEADER_CONTENT_TYPE, type.toString());
		if (chunked) {
			header.addHeaderFieldValue(HTTPConstants.HTTP_HEADER_TRANSFER_ENCODING, HTTPConstants.HTTP_HEADERVALUE_TRANSFERCODING_CHUNKED);
			header.toStream(out);
			// out.flush();
			return new ChunkedOutputStream(out, trailer);
		}
		header.toStream(out);
		// out.flush();
		return out;
	}

	/**
	 * Returns an input stream which allows the reading of a resource from the
	 * given location.
	 * 
	 * @param resLocation the resource's location (e.g.
	 *            http://example.org/test.wsdl).
	 * @return an input stream for the given resource.
	 */
	public static ResourceLoader getResourceAsStream(String resLocation) throws IOException, ProtocolException {
		if (resLocation.toLowerCase().startsWith(HTTPConstants.HTTP_SCHEMA)) {
			URI location = new URI(resLocation);

			if (Log.isDebug()) {
				Log.debug("<O> Accessing resource over HTTP from " + resLocation, Log.DEBUG_LAYER_COMMUNICATION);
			}

			MonitorStreamFactory monFac = DPWSFramework.getMonitorStreamFactory();

			Socket tcpSocket = null;
			if (DPWSFramework.hasModule(DPWSFramework.SECURITY_MODULE) && resLocation.toLowerCase().startsWith(HTTPConstants.HTTPS_SCHEMA)) {
				SecurityManager secMan = DPWSFramework.getSecurityManager();
				if (!(secMan instanceof DPWSSecurityManager)) {
					throw new IOException("Security manager is not valid for DPWS.");
				}
				tcpSocket = ((DPWSSecurityManager) secMan).getSecureSocket(location);
			} else {
				tcpSocket = SocketFactory.getInstance().createSocket(new IPAddress(location.getHost()), location.getPort());
			}
			DPWSProtocolData pd_out = null;
			if (tcpSocket.getRemoteAddress() == null) {
				/*
				 * TODO: CLDC quick fix! It's not possible to retrieve the
				 * remote address from the CLDC socket. :-(
				 */
				pd_out = new DPWSProtocolData(null, ProtocolData.DIRECTION_OUT, tcpSocket.getLocalAddress().getAddressWithoutNicId(), tcpSocket.getLocalPort(), null, tcpSocket.getRemotePort(), true);
			} else {
				pd_out = new DPWSProtocolData(null, ProtocolData.DIRECTION_OUT, tcpSocket.getLocalAddress().getAddressWithoutNicId(), tcpSocket.getLocalPort(), tcpSocket.getRemoteAddress().getAddressWithoutNicId(), tcpSocket.getRemotePort(), true);
			}

			final OutputStream out;

			if (DPWSFramework.getMonitorStreamFactory() != null) {
				out = new MonitoredOutputStream(tcpSocket.getOutputStream(), pd_out);
			} else {
				out = tcpSocket.getOutputStream();
			}

			if (monFac != null) {
				monFac.getNewMonitoringContextOut(pd_out);
			}

			HTTPRequestHeader requestHeader = new HTTPRequestHeader(HTTPConstants.HTTP_METHOD_GET, location.getPath(), HTTPConstants.HTTP_VERSION11);
			requestHeader.addHeaderFieldValue(HTTPConstants.HTTP_HEADER_HOST, location.getHost());
			requestHeader.addHeaderFieldValue(HTTPConstants.HTTP_HEADER_CONNECTION, HTTPConstants.HTTP_HEADERVALUE_CONNECTION_CLOSE);

			/*
			 * Write the request.
			 */
			requestHeader.toStream(out);
			out.flush();

			if (monFac != null) {
				MonitoringContext context = monFac.getMonitoringContextOut(pd_out);
				monFac.requestResource(pd_out, context, location);
			}

			String targetAddress = null;
			if (Log.isDebug()) {
				targetAddress = tcpSocket.getRemoteAddress() + "@" + tcpSocket.getRemotePort();
				Log.debug("<O> " + requestHeader + " to " + targetAddress, Log.DEBUG_LAYER_COMMUNICATION);
			}

			/*
			 * Handle the response.
			 */
			DPWSProtocolData pd_in = (DPWSProtocolData) pd_out.createSwappedProtocolData();

			InputStream in = tcpSocket.getInputStream();
			
			if (DPWSFramework.getMonitorStreamFactory() != null) {
				in = new MonitoredInputStream(in, pd_in);
			}
			

			if (monFac != null) {
				monFac.getNewMonitoringContextIn(pd_in);
			}

			HTTPResponseHeader responseHeader = null;
			try {
				responseHeader = HTTPResponseUtil.handleResponse(in);
			} catch (ProtocolException e) {
				// TODO Auto-generated catch block
				Log.printStackTrace(e);
			}

			if (responseHeader == null) {
				throw new IOException("No HTTP response found.");
			}

			if (Log.isDebug()) {
				if (targetAddress == null) {
					targetAddress = tcpSocket.getRemoteAddress() + "@" + tcpSocket.getRemotePort();
				}
				Log.debug("<I> " + responseHeader + " from " + targetAddress, Log.DEBUG_LAYER_COMMUNICATION);
			}

			String encoding = responseHeader.getHeaderFieldValue(HTTPConstants.HTTP_HEADER_TRANSFER_ENCODING);
			String sSize = responseHeader.getHeaderFieldValue(HTTPConstants.HTTP_HEADER_CONTENT_LENGTH);
			int size = 0;
			if (sSize != null) {
				size = Integer.parseInt(sSize.trim());
			}

			// wrap the HTTP stream
			InputStream httpIn = new HTTPInputStream(in, encoding, size) {
				
				public void close() throws IOException {
					out.close();
					super.close();
				}
				
			};

			if (responseHeader.getStatus() == 200) {
				ResourceLoader rl = new ResourceLoader(httpIn, pd_out);
				return rl;
			} else if (responseHeader.getStatus() == 301 || responseHeader.getStatus() == 303 || responseHeader.getStatus() == 307) {
				String newLocation = responseHeader.getHeaderFieldValue(HTTPConstants.HTTP_HEADER_LOCATION);
				if (newLocation == null) {
					throw new IOException("HTTP Response malformed.");
				}
				ResourceLoader rl = getResourceAsStream(newLocation);
				return rl;
			}

		}
		return null;
	}

	public static void writeLastChunk(OutputStream out) throws IOException {
		if (out instanceof ChunkedOutputStream) {
			ChunkedOutputStream.writeLastChunk((ChunkedOutputStream) out);
		}
	}
}
