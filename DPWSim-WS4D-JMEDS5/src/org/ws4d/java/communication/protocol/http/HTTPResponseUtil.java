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

import org.ws4d.java.communication.ProtocolException;
import org.ws4d.java.communication.protocol.http.header.HTTPResponseHeader;
import org.ws4d.java.constants.HTTPConstants;
import org.ws4d.java.constants.MIMEConstants;
import org.ws4d.java.html.HTMLDocument;
import org.ws4d.java.html.SimpleHTML;
import org.ws4d.java.structures.ByteArray;
import org.ws4d.java.structures.HashMap;
import org.ws4d.java.types.InternetMediaType;
import org.ws4d.java.types.URI;
import org.ws4d.java.util.Log;

/**
 * Utility class for the easier creation of HTTP response messages.
 */
public class HTTPResponseUtil {

	/*
	 * We are shy!
	 */
	private HTTPResponseUtil() {

	}

	/**
	 * Sends default HTTP 100 Continue response.
	 * 
	 * @param out stream to work with.
	 * @param message message body.
	 */
	public static void sendCountinueResponse(OutputStream out, String message) {
		sendResponse(out, 100, message);
	}

	/**
	 * Sends default HTTP 200 OK response.
	 * 
	 * @param out stream to work with.
	 * @param message message body.
	 */
	public static void sendOKResponse(OutputStream out, String message) {
		sendResponse(out, 200, message);
	}

	/**
	 * Sends default HTTP 200 OK response.
	 * 
	 * @param out stream to work with.
	 * @param document message document.
	 */
	public static void sendOKResponse(OutputStream out, HTMLDocument document) {
		sendResponse(out, 200, document);
	}

	/**
	 * Sends default HTTP 204 No Content response.
	 * 
	 * @param out stream to work with.
	 * @param message message body.
	 * @return the HTTP response.
	 */
	public static void sendNoContentResponse(OutputStream out, String message) {
		sendResponse(out, 204, message);
	}

	/**
	 * Sends default HTTP 404 Not Found response.
	 * 
	 * @param out stream to work with.
	 * @param document message document.
	 */
	public static void sendNotFoundResponse(OutputStream out, HTMLDocument document) {
		sendResponse(out, 404, document);
	}

	/**
	 * Sends default HTTP 404 Not Found response.
	 * 
	 * @param out stream to work with.
	 * @param message message body.
	 */
	public static void sendNotFoundResponse(OutputStream out, String message) {
		sendResponse(out, 404, message);
	}

	/**
	 * Sends a byte array with correct HTTP response.
	 * 
	 * @param out stream to work with.
	 * @param status HTTP status code for the response.
	 * @param message message body.
	 */
	public static void sendResponse(OutputStream out, int status, String message) {
		// create response header
		HTTPResponseHeader header = getResponseHeader(status);

		String defaultContentType = MIMEConstants.MEDIATYPE_TEXT + MIMEConstants.SEPARATOR + MIMEConstants.SUBTYPE_PLAIN;

		// compute message
		int ml = 0;
		byte[] messageData = null;
		if (message != null) {
			messageData = message.getBytes();
			ml = messageData.length;
			header.addHeaderFieldValue(HTTPConstants.HTTP_HEADER_CONTENT_LENGTH, String.valueOf(ml));
			header.addHeaderFieldValue(HTTPConstants.HTTP_HEADER_CONTENT_TYPE, defaultContentType);
		}
		sendResponse(out, messageData, header);
	}

	/**
	 * Sends a byte array with correct HTTP response.
	 * 
	 * @param out stream to work with.
	 * @param status HTTP status code for the response.
	 * @param message message body.
	 */
	public static void sendResponse(OutputStream out, int status, HTMLDocument message) {
		// create response header
		HTTPResponseHeader header = getResponseHeader(status);

		String defaultContentType = MIMEConstants.MEDIATYPE_TEXT + MIMEConstants.SEPARATOR + MIMEConstants.SUBTYPE_HTML;

		// compute message
		int ml = 0;
		byte[] messageData = null;
		if (message != null) {
			messageData = message.getData();
			ml = messageData.length;
			header.addHeaderFieldValue(HTTPConstants.HTTP_HEADER_CONTENT_LENGTH, String.valueOf(ml));
			header.addHeaderFieldValue(HTTPConstants.HTTP_HEADER_CONTENT_TYPE, defaultContentType);
		}
		sendResponse(out, messageData, header);
	}

	/**
	 * Sends a byte array with correct HTTP response.
	 * 
	 * @param out stream to work with.
	 * @param message message body.
	 * @param header the HTTP response header. Please set the correct content
	 *            length etc.
	 */
	public static void sendResponse(OutputStream out, byte[] message, HTTPResponseHeader header) {
		try {
			header.toStream(out);
			if (message != null) {
				out.write(message);
			}
			out.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/**
	 * Sends an HTTP bad request.
	 * 
	 * @param out stream to work with.
	 * @param note the error note.
	 */
	public static void sendBadRequest(OutputStream out, String note) {
		// create response header
		HTTPResponseHeader header = getResponseHeader(400);
		try {
			header.toStream(out);
			if (note != null) {
				out.write(note.getBytes());
			}
			out.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Sends an HTTP bad request.
	 * 
	 * @param out stream to work with.
	 * @param note the error note.
	 */
	public static void sendInternalServerError(OutputStream out, String note) {
		// create response header
		HTTPResponseHeader header = getResponseHeader(500);
		header.addHeaderFieldValue(HTTPConstants.HTTP_HEADER_CONNECTION, HTTPConstants.HTTP_HEADERVALUE_CONNECTION_CLOSE);
		try {
			header.toStream(out);
			if (note != null) {
				out.write(note.getBytes());
			}
			out.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Sends an HTTP version not supported.
	 * 
	 * @param out stream to work with.
	 * @param note the error note.
	 */
	public static void sendHTTPVersionNotSupported(OutputStream out, String note) {
		// create response header
		HTTPResponseHeader header = getResponseHeader(505);
		header.addHeaderFieldValue(HTTPConstants.HTTP_HEADER_CONNECTION, HTTPConstants.HTTP_HEADERVALUE_CONNECTION_CLOSE);
		try {
			header.toStream(out);
			if (note != null) {
				out.write(note.getBytes());
			}
			out.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Sends an HTTP unsupported media type
	 * 
	 * @param out stream to work with.
	 * @param note the error note.
	 */
	public static void sendUnsupportedMediaType(OutputStream out, String note) {
		// create response header
		HTTPResponseHeader header = getResponseHeader(415);
		try {
			header.toStream(out);
			if (note != null) {
				out.write(note.getBytes());
			}
			out.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Sends a HTTP redirect.
	 * 
	 * @param out stream to work with.
	 * @param request the request which was done.
	 * @param note the error note.
	 */
	public static void sendRedirect(OutputStream out, URI request, String note) {
		// create response header
		HTTPResponseHeader header = getResponseHeader(307);
		header.addHeaderFieldValue(HTTPConstants.HTTP_HEADER_LOCATION, request.getPath());
		try {
			header.toStream(out);
			if (note != null) {
				out.write(note.getBytes());
			}
			out.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Sends the default Error document.
	 * 
	 * @param out stream to work with.
	 * @param request the request which was done.
	 */
	public static void sendDefaultErrorDocument(OutputStream out, String request) {
		SimpleHTML html = new SimpleHTML("Not Found");
		html.addParagraph("The requested URI " + request + " was not found on this server.");
		html.addHorizontalRule();
		html.addParagraph("<i>Java Multi Edition DPWS Framework</i>");
		sendNotFoundResponse(out, html);
	}

	/**
	 * Sends the default document.
	 * 
	 * @param out stream to work with.
	 */
	public static void sendDefaultDocument(OutputStream out) {
		SimpleHTML html = new SimpleHTML("It works!");
		sendOKResponse(out, html);
	}

	/**
	 * Creates HTTP 204 "No Content" Header.
	 * 
	 * @return the HTTP response header.
	 */
	public static HTTPResponseHeader getResponseHeader() {
		return getResponseHeader(204);
	}

	/**
	 * Returns the default HTTP response header for the given status code.
	 * 
	 * @param status the status code.
	 * @return the HTTP response header.
	 */
	public static HTTPResponseHeader getResponseHeader(int status) {
		String version = HTTPConstants.HTTP_VERSION11;
		String phrase = null;
		switch (status) {
			case 100:
				phrase = HTTPStatus.HTTP_100;
				break;
			case 200:
				phrase = HTTPStatus.HTTP_200;
				break;
			case 202:
				phrase = HTTPStatus.HTTP_202;
				break;
			case 204:
				phrase = HTTPStatus.HTTP_204;
				break;
			case 300:
				phrase = HTTPStatus.HTTP_300;
				break;
			case 301:
				phrase = HTTPStatus.HTTP_301;
				break;
			case 302:
				phrase = HTTPStatus.HTTP_302;
				break;
			case 303:
				phrase = HTTPStatus.HTTP_303;
				break;
			case 304:
				phrase = HTTPStatus.HTTP_304;
				break;
			case 307:
				phrase = HTTPStatus.HTTP_307;
				break;
			case 400:
				phrase = HTTPStatus.HTTP_400;
				break;
			case 401:
				phrase = HTTPStatus.HTTP_401;
				break;
			case 403:
				phrase = HTTPStatus.HTTP_403;
				break;
			case 404:
				phrase = HTTPStatus.HTTP_404;
				break;
			case 415:
				phrase = HTTPStatus.HTTP_415;
				break;
			case 500:
				phrase = HTTPStatus.HTTP_500;
				break;
			case 501:
				phrase = HTTPStatus.HTTP_501;
				break;
			case 505:
				phrase = HTTPStatus.HTTP_505;
				break;
		}

		return new HTTPResponseHeader(version, status, phrase);
	}

	public static HTTPResponseHeader handleResponse(InputStream in) throws IOException, ProtocolException {
		String version = null;
		String status = null;
		String reason = null;

		version = HTTPUtil.readElement(in);

		// if (!version.equals(HTTPConstants.HTTP_VERSION11)) {
		// throw new ProtocolException("Unsupported HTTP version.");
		// }

		status = HTTPUtil.readElement(in);

		reason = HTTPUtil.readRequestLine(in);

		// Read the HTTP header fields
		HashMap headerfields = new HashMap();
		HTTPUtil.readHeaderFields(in, headerfields);

		int s = 0;
		try {
			s = Integer.valueOf(status).intValue();
		} catch (NumberFormatException e) {
			throw new IOException("Cannot determinate HTTP version.");
		}

		return new HTTPResponseHeader(version, s, reason, headerfields);
	}

	/**
	 * Writes an HTTP response header to the stream with given media type (e.g.
	 * application/soap+xml). Can be set to chunked mode if the length of
	 * followed communication cannot be determined. The returned
	 * <code>OutputStream</code> MUST be used it should be ensured that the
	 * chunks are written correctly.
	 * 
	 * @param out the output stream to write the HTTP request to.
	 * @param code the HTTP response code.
	 * @param type the internet media type.
	 * @param chunked <code>true</code> if a special chunked output stream
	 *            should be returned, <code>false</code> otherwise.
	 * @param trailer <code>true</code> if the chunk trailer should be appended
	 *            at the end, <code>false</code> otherwise.
	 * @return <code>ChunkedOutputStream</code> if <code>chunked</code> is true,
	 *         the normal output stream otherwise.
	 * @throws IOException
	 */
	public static OutputStream writeResponse(OutputStream out, int code, InternetMediaType type, boolean chunked, boolean trailer) throws IOException {
		HTTPResponseHeader header = HTTPResponseUtil.getResponseHeader(code);

		if (Log.isDebug()) {
			Log.debug("<O> " + header.toString(), Log.DEBUG_LAYER_COMMUNICATION);
		}

		header.addHeaderFieldValue(HTTPConstants.HTTP_HEADER_CONTENT_TYPE, type.toString());
		if (chunked) {
			header.addHeaderFieldValue(HTTPConstants.HTTP_HEADER_TRANSFER_ENCODING, HTTPConstants.HTTP_HEADERVALUE_TRANSFERCODING_CHUNKED);
			header.toStream(out);
			return new ChunkedOutputStream(out, trailer);
		}
		header.toStream(out);
		return out;
	}

	/**
	 * Sends the resource.
	 * 
	 * @param out stream to work with.
	 * @param res resource to send.
	 * @param type <code>MIME</code> type for this resource.
	 * @param chunked if <code>false</code> the whole resource is loaded into
	 *            memory before it is sent. if <code>true</code> the resource is
	 *            sent as chunked response, with out much memory usage.
	 * @param trailer <code>true</code> if you want to send the OPTIONAL chunk
	 *            trailer, <code>false</code> otherwise.
	 * @return <code>true</code> if the resource could be loaded and could be
	 *         sent, <code>false</code> otherwise.
	 */
	public static boolean sendResource(OutputStream out, String res, InternetMediaType type, boolean chunked, boolean trailer) {

		InputStream resIn = out.getClass().getResourceAsStream(res);

		if (resIn == null) {
			return false;
		}
		try {
			out = writeResponse(out, 200, type, chunked, trailer);
			if (chunked) {
				int i = -1;

				if (Log.isDebug()) {
					Log.debug("Sending chunked resource [ " + res + " ] over HTTP.", Log.DEBUG_LAYER_COMMUNICATION);
				}

				while (resIn.available() > 0 && (i = resIn.read()) != -1) {
					out.write(i);
				}
				// out.flush();
			} else {
				if (Log.isDebug()) {
					Log.debug("Sending resource [ " + res + " ] over HTTP.", Log.DEBUG_LAYER_COMMUNICATION);
				}

				// create response header
				HTTPResponseHeader header = getResponseHeader(200);

				String defaultContentType = type.getMediaType();

				// compute message
				header.addHeaderFieldValue(HTTPConstants.HTTP_HEADER_CONTENT_TYPE, defaultContentType);
				// load resource into memory
				int i = -1;
				ByteArray buffer = new ByteArray();
				while (resIn.available() > 0 && (i = resIn.read()) != -1) {
					buffer.append((byte) i);
				}

				// now we know the length
				header.addHeaderFieldValue(HTTPConstants.HTTP_HEADER_CONTENT_LENGTH, String.valueOf(buffer.size()));

				// write HTTP header
				header.toStream(out);
				// out.flush();

				// write HTTP body
				out.write(buffer.getBytes());
			}
			out.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			Log.printStackTrace(e);
		} finally {
			try {
				resIn.close();
			} catch (IOException e) {
				Log.printStackTrace(e);
			}
		}
		return true;
	}

}
