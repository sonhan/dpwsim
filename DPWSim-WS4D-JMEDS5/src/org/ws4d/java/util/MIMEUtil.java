/*******************************************************************************
 * Copyright (c) 2009 MATERNA Information & Communications. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html. For further
 * project-related information visit http://www.ws4d.org. The most recent
 * version of the JMEDS framework can be obtained from
 * http://sourceforge.net/projects/ws4d-javame.
 ******************************************************************************/
package org.ws4d.java.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.ws4d.java.attachment.AttachmentException;
import org.ws4d.java.attachment.IncomingAttachment;
import org.ws4d.java.communication.ProtocolException;
import org.ws4d.java.constants.MIMEConstants;
import org.ws4d.java.constants.Specialchars;
import org.ws4d.java.structures.HashMap;
import org.ws4d.java.structures.Iterator;

/**
 * Utility class for MIME handling.
 */
public class MIMEUtil {

	public static int				DEFAULT_MIME_BUFFER			= 1024;

	// predefined exception messages.
	protected static final String	FAULT_UNEXPECTED_END		= "Unexpected end of stream.";

	protected static final String	FAULT_MALFORMED_HEADERFIELD	= "Malformed MIME header field.";

	protected static final String	FAULT_NOT_FINISHED			= "Previous part not finished.";

	/**
	 * Reads the boundary string.
	 * 
	 * @param in input stream to read from.
	 * @param boundary the given boundary information.
	 * @return the read element.
	 * @throws IOException
	 */
	public static boolean readBoundary(InputStream in, byte[] boundary) throws IOException {
		int i = -1;
		int j = 0;
		int maxlen = boundary.length;
		/*
		 * Check for boundary two hyphen characters. See RFC2046 5.1.1
		 */
		i = in.read();
		if ((byte) i != MIMEConstants.BOUNDARY_HYPHEN) {
			return false;
		}
		i = in.read();
		if ((byte) i != MIMEConstants.BOUNDARY_HYPHEN) {
			return false;
		}

		// Check for boundary
		while ((j < maxlen) && ((i = in.read()) != -1) && ((byte) i == boundary[j])) {
			j++;
			if (j == maxlen) {
				i = in.read();
				if ((byte) i == Specialchars.CR) {
					i = in.read();
					if ((byte) i == Specialchars.LF) {
						return true;
					}
				}
			}
		}
		return false;
	}

	/**
	 * Writes a MIME boundary.
	 * 
	 * @param out
	 * @param boundary
	 * @param crlf
	 * @param last
	 * @throws IOException
	 */
	public static void writeBoundary(OutputStream out, byte[] boundary, boolean crlf, boolean last) throws IOException {
		if (crlf) {
			out.write(Specialchars.CR);
			out.write(Specialchars.LF);
		}
		out.write(MIMEConstants.BOUNDARY_HYPHEN);
		out.write(MIMEConstants.BOUNDARY_HYPHEN);
		out.write(boundary);
		if (last) {
			out.write(MIMEConstants.BOUNDARY_HYPHEN);
			out.write(MIMEConstants.BOUNDARY_HYPHEN);
		}
		out.write(Specialchars.CR);
		out.write(Specialchars.LF);
	}

	public static void serializeAttachment(OutputStream out, IncomingAttachment attachment) throws IOException, AttachmentException {
		byte[] buffer = new byte[DEFAULT_MIME_BUFFER];
		InputStream in = attachment.getInputStream();
		int i;
		int size = 0;
		long time = System.currentTimeMillis();
		while ((i = in.read(buffer)) > 0) {
			out.write(buffer, 0, i);
			size += i;
		}
		out.flush();
		if (Log.isDebug()) {
			Log.debug("Attachment serialized: " + (System.currentTimeMillis() - time) + "ms. " + size + " bytes.", Log.DEBUG_LAYER_COMMUNICATION);
		}
	}

	/**
	 * Writes MIME header fields to the output stream.
	 * 
	 * @param out
	 * @param headerfields
	 * @throws IOException
	 */
	public static void writeHeaderFields(OutputStream out, HashMap headerfields) throws IOException {
		if (headerfields == null) {
			out.write(Specialchars.CR);
			out.write(Specialchars.LF);
			return;
		}
		Iterator keys = headerfields.keySet().iterator();
		if (keys == null) {
			out.write(Specialchars.CR);
			out.write(Specialchars.LF);
			return;
		}
		while (keys.hasNext()) {
			String fieldname = (String) keys.next();
			String fieldvalue = (String) headerfields.get(fieldname);
			out.write(fieldname.getBytes());
			out.write(Specialchars.COL);
			out.write(Specialchars.SP);
			out.write(fieldvalue.getBytes());
			out.write(Specialchars.CR);
			out.write(Specialchars.LF);
		}
		out.write(Specialchars.CR);
		out.write(Specialchars.LF);
	}

	/**
	 * Reads MIME header fields from the input stream. To learn more about MIME
	 * header fields, take a look at RFC2045 3.
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
					j = 0;
					continue;
				}
				// no CTL (ascii 0-31) allowed for field-name
				if ((byte) i >= 0x00 && (char) i <= 0x1F) { //
					throw new ProtocolException(FAULT_MALFORMED_HEADERFIELD);
				}
				// no separators allowed for token (see RFC2616 2.2)
				if ((byte) i == 0x28 || (byte) i == 0x29 || (byte) i == 0x3C || (byte) i == 0x3D || (byte) i == 0x3E || (byte) i == 0x40 || (byte) i == 0x2C || (byte) i == 0x3F || (byte) i == 0x3B || (byte) i == 0x2F || (byte) i == 0x5C || (byte) i == 0x5B || (byte) i == 0x5D || (byte) i == 0x7B || (byte) i == 0x7D || (byte) i == 0x22 || (byte) i == Specialchars.SP || (byte) i == Specialchars.HT) {
					throw new ProtocolException(FAULT_MALFORMED_HEADERFIELD);
				}
			} else {
				// if field-name set, must read field-value.
				if (((byte) i == Specialchars.SP || (byte) i == Specialchars.HT) && j == 0) {
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
					/*
					 * Every MIME body header field must begin with "content-"
					 * like described in RFC2045 3.
					 */
					if (fieldname.startsWith(MIMEConstants.DEFAULT_HEADERFIELD_PREFIX.toLowerCase())) {
						headerfields.put(fieldname, fieldvalue);
					}
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
					/*
					 * Every MIME body header field must begin with "content-"
					 * like described in RFC2045 3.
					 */
					if (fieldname.startsWith(MIMEConstants.DEFAULT_HEADERFIELD_PREFIX.toLowerCase())) {
						headerfields.put(fieldname, fieldvalue);
					}
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
		throw new IOException(FAULT_MALFORMED_HEADERFIELD);
	}

	/**
	 * Gets estimated Content-Type via filename.
	 * 
	 * @param filename fileName with extension.
	 * @return Content-Type estimated Content-Type based on the file extension.
	 */
	public static String estimateContentType(String filename) {
		int last = 0;
		last = filename.lastIndexOf('.');

		String fileExt = filename.substring(last + 1);
		return extensionContentType(fileExt);
	}

	/**
	 * Returns a file extension that is most likely with given Content-Type
	 * 
	 * @param mime MIME Type
	 * @return Extension probable file extension, "" if Content- Type is unknown
	 */
	public static String contentToExtension(String mime) {
		if (MIMEUtil.isValidConstructedMIMEType(mime)) {
			int sep = mime.indexOf("/");
			String mediatype = mime.substring(0, sep);
			String subtype = mime.substring(sep + 1);

			if (StringUtil.equalsIgnoreCase(mediatype, MIMEConstants.MEDIATYPE_IMAGE)) {
				if (StringUtil.equalsIgnoreCase(subtype, MIMEConstants.SUBTYPE_GIF)) {
					return "*.gif";
				} else if (StringUtil.equalsIgnoreCase(subtype, MIMEConstants.SUBTYPE_JPEG)) {
					return "*.jpg";
				} else if (StringUtil.equalsIgnoreCase(subtype, MIMEConstants.SUBTYPE_PNG)) {
					return "*.png";
				} else if (StringUtil.equalsIgnoreCase(subtype, MIMEConstants.SUBTYPE_TIFF)) {
					return "*.tiff";
				} else if (StringUtil.equalsIgnoreCase(subtype, MIMEConstants.SUBTYPE_ICON)) {
					return "*.ico";
				}
			} else if (StringUtil.equalsIgnoreCase(mediatype, MIMEConstants.MEDIATYPE_TEXT)) {
				if (StringUtil.equalsIgnoreCase(subtype, MIMEConstants.SUBTYPE_CSS)) {
					return "*.css";
				} else if (StringUtil.equalsIgnoreCase(subtype, MIMEConstants.SUBTYPE_HTML)) {
					return "*.htm";
				} else if (StringUtil.equalsIgnoreCase(subtype, MIMEConstants.SUBTYPE_JAVASCRIPT)) {
					return "*.js";
				} else if (StringUtil.equalsIgnoreCase(subtype, MIMEConstants.SUBTYPE_PLAIN)) {
					return "*.txt";
				} else if (StringUtil.equalsIgnoreCase(subtype, MIMEConstants.SUBTYPE_RICHTEXT)) {
					return "*.rtf";
				} else if (StringUtil.equalsIgnoreCase(subtype, MIMEConstants.SUBTYPE_SOAPXML)) {
					return "*.xml";
				}
			} else if (StringUtil.equalsIgnoreCase(mediatype, MIMEConstants.MEDIATYPE_APPLICATION)) {
				if (StringUtil.equalsIgnoreCase(subtype, MIMEConstants.SUBTYPE_MSEXCEL)) {
					return "*.xls";
				} else if (StringUtil.equalsIgnoreCase(subtype, MIMEConstants.SUBTYPE_MSWORD)) {
					return "*.doc";
				} else if (StringUtil.equalsIgnoreCase(subtype, MIMEConstants.SUBTYPE_RAR)) {
					return "*.rar";
				} else if (StringUtil.equalsIgnoreCase(subtype, MIMEConstants.SUBTYPE_PDF)) {
					return "*.pdf";
				} else if (StringUtil.equalsIgnoreCase(subtype, MIMEConstants.SUBTYPE_SHOCKWAVEFLASH)) {
					return "*.swf";
				} else if (StringUtil.equalsIgnoreCase(subtype, MIMEConstants.SUBTYPE_WINDOWSEXECUTEABLE)) {
					return "*.exe";
				} else if (StringUtil.equalsIgnoreCase(subtype, MIMEConstants.SUBTYPE_ZIP)) {
					return "*.zip";
				}
			} else if (StringUtil.equalsIgnoreCase(mediatype, MIMEConstants.MEDIATYPE_VIDEO)) {
				if (StringUtil.equalsIgnoreCase(subtype, MIMEConstants.SUBTYPE_WINDOWSMEDIA)) {
					return "*.wmv";
				} else if (StringUtil.equalsIgnoreCase(subtype, MIMEConstants.SUBTYPE_AVI)) {
					return "*.avi";
				}
			} else if (StringUtil.equalsIgnoreCase(mediatype, MIMEConstants.MEDIATYPE_AUDIO)) {
				if (StringUtil.equalsIgnoreCase(subtype, MIMEConstants.SUBTYPE_MPEG3)) {
					return "*.mp3";
				}
			}
		}
		return "*.*";
	}

	/**
	 * Gets Content-Type via file extension.
	 * 
	 * @param fileExt file extension.
	 * @return Content-Type (type and subtype).
	 */
	public static String extensionContentType(String fileExt) {
		if (StringUtil.equalsIgnoreCase(fileExt, "jpg") || StringUtil.equalsIgnoreCase(fileExt, "jpeg")) {
			return MIMEConstants.MEDIATYPE_IMAGE + MIMEConstants.SEPARATOR + MIMEConstants.SUBTYPE_JPEG;
		} else if (StringUtil.equalsIgnoreCase(fileExt, "txt")) {
			return MIMEConstants.MEDIATYPE_TEXT + MIMEConstants.SEPARATOR + MIMEConstants.SUBTYPE_PLAIN;
		} else if (StringUtil.equalsIgnoreCase(fileExt, "gif")) {
			return MIMEConstants.MEDIATYPE_IMAGE + MIMEConstants.SEPARATOR + MIMEConstants.SUBTYPE_GIF;
		} else if (StringUtil.equalsIgnoreCase(fileExt, "png")) {
			return MIMEConstants.MEDIATYPE_IMAGE + MIMEConstants.SEPARATOR + MIMEConstants.SUBTYPE_PNG;
		} else if (StringUtil.equalsIgnoreCase(fileExt, "tiff")) {
			return MIMEConstants.MEDIATYPE_IMAGE + MIMEConstants.SEPARATOR + MIMEConstants.SUBTYPE_TIFF;
		} else if (StringUtil.equalsIgnoreCase(fileExt, "tif")) {
			return MIMEConstants.MEDIATYPE_IMAGE + MIMEConstants.SEPARATOR + MIMEConstants.SUBTYPE_TIFF;
		} else if (StringUtil.equalsIgnoreCase(fileExt, "htm") || StringUtil.equalsIgnoreCase(fileExt, "html")) {
			return MIMEConstants.MEDIATYPE_TEXT + MIMEConstants.SEPARATOR + MIMEConstants.SUBTYPE_HTML;
		} else if (StringUtil.equalsIgnoreCase(fileExt, "xml")) {
			return MIMEConstants.MEDIATYPE_TEXT + MIMEConstants.SEPARATOR + MIMEConstants.SUBTYPE_XML;
		} else if (StringUtil.equalsIgnoreCase(fileExt, "js")) {
			return MIMEConstants.MEDIATYPE_TEXT + MIMEConstants.SEPARATOR + MIMEConstants.SUBTYPE_JAVASCRIPT;
		} else if (StringUtil.equalsIgnoreCase(fileExt, "css")) {
			return MIMEConstants.MEDIATYPE_TEXT + MIMEConstants.SEPARATOR + MIMEConstants.SUBTYPE_CSS;
		} else if (StringUtil.equalsIgnoreCase(fileExt, "zip")) {
			return MIMEConstants.MEDIATYPE_APPLICATION + MIMEConstants.SEPARATOR + MIMEConstants.SUBTYPE_ZIP;
		} else if (StringUtil.equalsIgnoreCase(fileExt, "pdf")) {
			return MIMEConstants.MEDIATYPE_APPLICATION + MIMEConstants.SEPARATOR + MIMEConstants.SUBTYPE_PDF;
		} else if (StringUtil.equalsIgnoreCase(fileExt, "wmv")) {
			return MIMEConstants.MEDIATYPE_VIDEO + MIMEConstants.SEPARATOR + MIMEConstants.SUBTYPE_WINDOWSMEDIA;
		} else if (StringUtil.equalsIgnoreCase(fileExt, "rar")) {
			return MIMEConstants.MEDIATYPE_APPLICATION + MIMEConstants.SEPARATOR + MIMEConstants.SUBTYPE_RAR;
		} else if (StringUtil.equalsIgnoreCase(fileExt, "swf")) {
			return MIMEConstants.MEDIATYPE_APPLICATION + MIMEConstants.SEPARATOR + MIMEConstants.SUBTYPE_SHOCKWAVEFLASH;
		} else if (StringUtil.equalsIgnoreCase(fileExt, "exe")) {
			return MIMEConstants.MEDIATYPE_APPLICATION + MIMEConstants.SEPARATOR + MIMEConstants.SUBTYPE_WINDOWSEXECUTEABLE;
		} else if (StringUtil.equalsIgnoreCase(fileExt, "avi")) {
			return MIMEConstants.MEDIATYPE_VIDEO + MIMEConstants.SEPARATOR + MIMEConstants.SUBTYPE_AVI;
		} else if (StringUtil.equalsIgnoreCase(fileExt, "doc") || StringUtil.equalsIgnoreCase(fileExt, "dot")) {
			return MIMEConstants.MEDIATYPE_APPLICATION + MIMEConstants.SEPARATOR + MIMEConstants.SUBTYPE_MSWORD;
		} else if (StringUtil.equalsIgnoreCase(fileExt, "ico")) {
			return MIMEConstants.MEDIATYPE_IMAGE + MIMEConstants.SEPARATOR + MIMEConstants.SUBTYPE_ICON;
		} else if (StringUtil.equalsIgnoreCase(fileExt, "mp2") || StringUtil.equalsIgnoreCase(fileExt, "mp3")) {
			return MIMEConstants.MEDIATYPE_AUDIO + MIMEConstants.SEPARATOR + MIMEConstants.SUBTYPE_MPEG3;
		} else if (StringUtil.equalsIgnoreCase(fileExt, "rtf")) {
			return MIMEConstants.MEDIATYPE_TEXT + MIMEConstants.SEPARATOR + MIMEConstants.SUBTYPE_RICHTEXT;
		} else if (StringUtil.equalsIgnoreCase(fileExt, "xls") || StringUtil.equalsIgnoreCase(fileExt, "xla")) {
			return MIMEConstants.MEDIATYPE_APPLICATION + MIMEConstants.SEPARATOR + MIMEConstants.SUBTYPE_MSEXCEL;
		}
		return MIMEConstants.MEDIATYPE_TEXT + MIMEConstants.SEPARATOR + MIMEConstants.SUBTYPE_PLAIN;
	}

	/**
	 * Checks if a given string can be a valid MIME- Type
	 * 
	 * @param mime String which should be checked
	 * @return boolean returns true if given string can be a correct MIME- Type,
	 *         false otherwise
	 */
	private static boolean isValidConstructedMIMEType(String mime) {
		// mime is null
		if (mime == null) return false;
		// mime is empty
		if (mime.length() == 0) return false;
		int sep = mime.indexOf("/");
		// mime does not contain "/"
		if (sep == -1) return false;
		// no mediatype
		if (sep == 0) return false;
		// no subtype
		if (mime.length() <= sep + 1) return false;

		return true;
	}
}
