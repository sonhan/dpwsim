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

import org.ws4d.java.communication.ProtocolException;
import org.ws4d.java.constants.Specialchars;
import org.ws4d.java.structures.HashMap;

/**
 * HTTP support class. All RFC methods are here!
 */
public class HTTPUtil {

	/**
	 * We are shy!
	 */
	private HTTPUtil() {

	}

	/**
	 * Reads a single element from the input stream. Elements are separated by
	 * space characters. (see RFC2616 5.1)
	 * 
	 * @param in input stream to read from.
	 * @return the read element.
	 */
	public static String readElement(InputStream in) throws IOException {
		return HTTPUtil.readElement(in, 0);
	}

	/**
	 * Reads a single element from the input stream. Elements are separated by
	 * space characters. (see RFC2616 5.1). Stops after given amount of bytes.
	 * 
	 * @param in in input stream to read from.
	 * @param maxlen max length to read from stream.
	 * @return the read element.
	 * @throws IOException
	 */
	public static String readElement(InputStream in, int maxlen) throws IOException {
		int i = -1;
		int j = -1;
		StringBuffer buffer = new StringBuffer();
		// read until "space"
		while ((j < maxlen) && ((i = in.read()) != -1) && ((byte) i != Specialchars.SP)) {
			if (maxlen > 0) j++;
			buffer.append((char) i);
		}
		if (i == -1) {
			return null;
		}
		return buffer.toString();
	}

	/**
	 * Reads a single protocol line from the input stream. HTTP defines the
	 * sequence CR LF as the end-of-line marker. (see RFC2616 2.2)
	 * 
	 * @param in input stream to read from.
	 * @return the protocol line.
	 */
	public static String readRequestLine(InputStream in) throws IOException {
		int i;
		StringBuffer buffer = new StringBuffer();
		int j = 0;
		// read until new line
		while (((i = in.read()) != -1)) {
			if ((byte) i == Specialchars.CR) {
				j = 1;
				continue;
			}
			if ((byte) i == Specialchars.LF && j == 1) {
				j = 0;
				return buffer.toString();
			}
			buffer.append((char) i);
		}
		throw new IOException(HTTPRequestUtil.FAULT_UNEXPECTED_END);
	}

	/**
	 * Reads the HTTP version from the input stream. (see RFC2616 3.1)
	 * 
	 * @param in input stream to read from.
	 * @return the read element.
	 */
	public static String readRequestVersion(InputStream in) throws IOException, ProtocolException {
		int i;
		StringBuffer buffer = new StringBuffer();
		/*
		 * "HTTP" "/" 1*DIGIT "." 1*DIGIT
		 */
		int j = 0;
		int k = 0;
		byte http[] = { 0x48, 0x54, 0x54, 0x50 }; // HTTP;
		while (((i = in.read()) != -1)) {
			// check for HTTP
			if (j < http.length && k == 0) {
				if ((byte) i != http[j]) {
					throw new ProtocolException(HTTPRequestUtil.FAULT_MALFORMED_REQUEST);
				}
				buffer.append((char) i);
				j++;
				continue;
			}
			// check for slash after HTTP string
			if (j == http.length) {
				if ((byte) i == 0x2F) { // slash
					buffer.append((char) i);
					k = 1;
					j = 0;
					continue;
				}
				throw new ProtocolException(HTTPRequestUtil.FAULT_MALFORMED_REQUEST);
			}
			// check for 0-9 and a dot
			if ((k == 1)) {
				if ((byte) i >= 0x30 && (byte) i <= 0x39) {
					buffer.append((char) i);
					continue;
				}
				if ((byte) i == 0x2E) { // dot
					buffer.append((char) i);
					k = 2;
					continue;
				}
				throw new ProtocolException(HTTPRequestUtil.FAULT_MALFORMED_REQUEST);
			}
			// check for 0-9 and a new line
			if ((k == 2)) {
				if ((byte) i >= 0x30 && (byte) i <= 0x39) {
					buffer.append((char) i);
					continue;
				}
				if ((byte) i == Specialchars.CR) {
					k = 3;
					continue;
				}
				throw new ProtocolException(HTTPRequestUtil.FAULT_MALFORMED_REQUEST);
			}
			// check for new line end
			if (k == 3) {
				if ((byte) i == Specialchars.LF) {
					j = 0;
					k = 0;
					// exit!
					return buffer.toString();
				}
			}
		}
		throw new IOException(HTTPRequestUtil.FAULT_UNEXPECTED_END);
	}

	/**
	 * Reads a HTTP header fields from the input stream. To learn more about
	 * HTTP header fields, take a look at RFC2616 4.2, 4.5, 5.3, 6.2, 7.1
	 * 
	 * @param in the input stream to read from.
	 * @param headerfields <code>Hashtable</code> to store the fields in.
	 */
	public static void readHeaderFields(InputStream in, HashMap headerfields) throws IOException, ProtocolException {

		String fieldname = null;
		String fieldvalue = null;

		int i;
		StringBuffer buffer = new StringBuffer();
		int j = 0; // length of read bytes.
		int k = 0; // CRLF counter. 2xCRLF = header end.
		int l = 0; // CRLF detection. 0=nothing, 1=CR, 2=CRLF.
		// message-header = field-name ":" [ field-value ]
		// field-name = token
		// field-value = *( field-content | LWS ) field-content = *TEXT |
		// *(token, separators, quoted-string)
		while (((i = in.read()) != -1)) {
			if (fieldname == null) {
				// check for new line
				if ((byte) i == Specialchars.CR) {
					l = 1;
					continue;
				}
				// check for new line end
				if ((byte) i == Specialchars.LF && l == 1) {
					l = 0;
					return;
				}
				// check for colon and create field-name
				if ((byte) i == Specialchars.COL) {
					fieldname = buffer.toString().toLowerCase();
					buffer = new StringBuffer();
					j = 1;
					continue;
				}
				// no CTL (ascii 0-31) allowed for field-name
				if ((byte) i >= 0x00 && (char) i <= 0x1F) { //
					throw new ProtocolException(HTTPRequestUtil.FAULT_MALFORMED_HEADERFIELD + " (" + buffer.toString() + ")");
				}
				// no separators allowed for token (see RFC2616 2.2)
				if ((byte) i == 0x28 || (byte) i == 0x29 || (byte) i == 0x3C || (byte) i == 0x3D || (byte) i == 0x3E || (byte) i == 0x40 || (byte) i == 0x2C || (byte) i == 0x3F || (byte) i == 0x3B || (byte) i == 0x2F || (byte) i == 0x5C || (byte) i == 0x5B || (byte) i == 0x5D || (byte) i == 0x7B || (byte) i == 0x7D || (byte) i == 0x22 || (byte) i == Specialchars.SP || (byte) i == Specialchars.HT) {
					throw new ProtocolException(HTTPRequestUtil.FAULT_MALFORMED_HEADERFIELD + " (" + buffer.toString() + ")");
				}
			} else {
				// if field-name set, must read field-value.
				if (((byte) i == Specialchars.SP || (byte) i == Specialchars.HT)) {
					buffer.append((char) Specialchars.SP);
					j++;
					continue;
				}
				// check for new line
				if ((byte) i == Specialchars.CR) {
					l = 1;
				}
				// check for new line end
				if ((byte) i == Specialchars.LF && l == 1) {
					j = 0;
					k++;
					l = 2;
				}
				if (k > 1) {
					// add
					fieldvalue = buffer.toString();
					fieldvalue = fieldvalue.trim();
					fieldname = fieldname.toLowerCase();
					headerfields.put(fieldname, fieldvalue);
					// double CRLF, header ends here
					j = 0;
					k = 0;
					l = 0;
					fieldname = null;
					fieldvalue = null;
					return;
				}
				if (l > 0) {
					if (l == 2) {
						l = 0;
					}
					continue;
				}
				if (j == 0) {
					// add filed-name and field-value
					fieldvalue = buffer.toString();
					fieldvalue = fieldvalue.trim();
					fieldname = fieldname.toLowerCase();
					headerfields.put(fieldname, fieldvalue);

					// reset
					buffer = new StringBuffer();
					fieldname = null;
					fieldvalue = null;
				}
			}
			buffer.append((char) i);
			j++;
			k = 0;
			l = 0;
		}
		throw new IOException(HTTPRequestUtil.FAULT_UNEXPECTED_END + " (" + buffer.toString() + ")");
	}

	/**
	 * Reads the HTTP chunk header from stream.
	 * 
	 * @param in Stream from which to read the header.
	 * @return a <code>HTTPChunkHeader</code>.
	 * @throws IOException
	 * @throws ProtocolException
	 */
	public static HTTPChunkHeader readChunkHeader(InputStream in) throws IOException, ProtocolException {
		int chunksize = 0;
		HashMap chunkextensions = null;
		HashMap chunktrailer = null;

		int chunkext = 0;

		/*
		 * Reads the HTTP chunk size if in chunk mode. (RFC 2616, 3.6.1)
		 */
		int i;
		StringBuffer buffer = new StringBuffer();
		while (((i = in.read()) != -1)) {
			if (((byte) i >= 0x30 && (byte) i <= 0x39) || ((byte) i >= 0x41 && (byte) i <= 0x46) || ((byte) i >= 0x61 && (byte) i <= 0x66)) {
				buffer.append((char) i);
				continue;
			}
			if ((byte) i == Specialchars.SCOL) {
				try {
					int n = Integer.parseInt(buffer.toString(), 16);
					chunkext = 3;
					chunksize = n;
					break;
				} catch (NumberFormatException e) {
					throw new IOException(HTTPRequestUtil.FAULT_MALFORMED_CHUNK + " (" + buffer.toString() + ")");
				}
			}
			if ((byte) i == Specialchars.CR) {
				chunkext = 1;
				continue;
			}
			if ((byte) i == Specialchars.LF && chunkext == 1) {
				try {
					int n = Integer.parseInt(buffer.toString(), 16);
					chunkext = 2;
					chunksize = n;
					break;
				} catch (NumberFormatException e) {
					throw new IOException(HTTPRequestUtil.FAULT_MALFORMED_CHUNK + " (" + buffer.toString() + ")");
				}
			}
		}
		if (i == -1) {
			throw new IOException(HTTPRequestUtil.FAULT_UNEXPECTED_END + " (" + buffer.toString() + ")");
		}

		chunkextensions = new HashMap();
		if (chunkext == 3) {
			HTTPUtil.readChunkExtensions(in, chunkextensions);
		}
		if (chunksize == 0) {
			chunktrailer = new HashMap();
			// check for trailer
			readHeaderFields(in, chunktrailer);
		}
		if (chunkextensions.size() == 0) {
			chunkextensions = null;
		}
		if (chunktrailer != null && chunktrailer.size() == 0) {
			chunktrailer = null;
		}
		return new HTTPChunkHeader(chunksize, chunkextensions, chunktrailer);
	}

	/**
	 * Reads the chunk extension from stream. (RFC 2616, 3.6.1)
	 * 
	 * @param in the stream to read from.
	 * @param chunkextensions <code>Map</code> to store the fields in.
	 */
	public static void readChunkExtensions(InputStream in, HashMap chunkextensions) throws IOException, ProtocolException {
		int i;
		String chunkextname = null;
		String chunkextvalue = null;
		int j = 0;
		StringBuffer buffer = new StringBuffer();
		while (((i = in.read()) != -1)) {
			if (chunkextname == null) {
				if ((byte) i == Specialchars.EQ) {
					chunkextname = buffer.toString().toLowerCase();
					buffer = new StringBuffer();
					continue;
				}
				// no CTL (ascii 0-31) allowed for chunk-ext-name
				if ((byte) i >= 0x00 && (byte) i <= 0x1F) { //
					throw new ProtocolException(HTTPRequestUtil.FAULT_MALFORMED_CHUNK + " (" + buffer.toString() + ")");
				}
				// no separators allowed for token (see RFC2616 2.2)
				if ((byte) i == 0x28 || (byte) i == 0x29 || (byte) i == 0x3C || (byte) i == 0x3D || (byte) i == 0x3E || (byte) i == 0x40 || (byte) i == 0x2C || (byte) i == 0x3F || (byte) i == 0x3B || (byte) i == 0x2F || (byte) i == 0x5C || (byte) i == 0x5B || (byte) i == 0x5D || (byte) i == 0x7B || (byte) i == 0x7D || (byte) i == 0x22 || (byte) i == Specialchars.SP || (byte) i == Specialchars.HT) {
					throw new ProtocolException(HTTPRequestUtil.FAULT_MALFORMED_CHUNK + " (" + buffer.toString() + ")");
				}
				// check for equal and create chunk-ext-name
			} else {
				if ((byte) i == Specialchars.CR) {
					j = 1;
					continue;
				}
				// check for new line end
				if ((byte) i == Specialchars.LF && j == 1) {
					j = 0;
					chunkextvalue = buffer.toString();
					chunkextname = chunkextname.trim();
					chunkextname = chunkextname.toLowerCase();
					chunkextensions.put(chunkextname, chunkextvalue);
					return;
				}
				if ((byte) i == Specialchars.SCOL) {
					// add filed-name and field-value
					chunkextvalue = buffer.toString();
					chunkextname = chunkextname.trim();
					chunkextname = chunkextname.toLowerCase();
					chunkextensions.put(chunkextname, chunkextvalue);

					// reset
					buffer = new StringBuffer();
					chunkextname = null;
					continue;
				}
			}
			buffer.append((char) i);
		}
		throw new IOException(HTTPRequestUtil.FAULT_UNEXPECTED_END + " (" + buffer.toString() + ")");
	}

	public static byte[] camelCase(String s) {
		byte[] b = s.getBytes();
		boolean camel = true;
		for (int i = 0; i < b.length; i++) {
			if (b[i] >= 97 && b[i] <= 122 && camel) {
				b[i] = (byte) (b[i] - 32);
				camel = false;
			}
			if (b[i] == 32 && !camel) {
				camel = true;
			}
			if (b[i] == 45 && !camel) {
				camel = true;
			}
		}
		return b;
	}

}
