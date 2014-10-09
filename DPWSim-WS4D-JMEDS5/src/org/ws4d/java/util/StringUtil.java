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
import java.io.UnsupportedEncodingException;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.TimeZone;
import java.util.Vector;

import org.ws4d.java.constants.Specialchars;
import org.ws4d.java.structures.ArrayList;
import org.ws4d.java.structures.HashMap;
import org.ws4d.java.structures.List;

/**
 *
 */
public final class StringUtil {

	private static String[]			encList					= { "UTF-8", "ISO-8859-1" };

	/*
	 * Some stuff defined by the RFC 822 (5. DATE AND TIME SPECIFICATION)
	 */

	public static final String[]	day						= { "Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat" };

	public static final String[]	month					= { "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec" };

	public static final String[][]	zone					= { { "UT", "0" }, { "GMT", "0" }, { "EST", "-5" }, { "EDT", "-4" }, { "CST", "-6" }, { "CDT", "-5" }, { "MST", "-7" }, { "MDT", "-6" }, { "PST", "-8" }, { "PDT", "-7" }, { "Z", "0" }, { "A", "-1" }, { "M", "-12" }, { "N", "+1" }, { "Y", "+12" }, { "J", "0" } };

	/**
	 * When set to <code>true</code>, most of the framework classes will use
	 * fully qualified class names within their <code>toString()</code> methods.
	 * Otherwise (when <code>false</code>), class names will be shorten up by
	 * means of {@link #simpleClassName(String)}.
	 */
	private static final boolean	USE_LONG_CLASS_NAMES	= false;

	private static final String[]	EMPTY_STRING_ARRAY		= new String[0];

	public static boolean equalsIgnoreCase(String s1, String s2) {
		if (s1 == s2) {
			return true;
		}
		return s1 != null && s2 != null && s1.length() == s2.length() && s1.regionMatches(true, 0, s2, 0, s1.length());
	}

	public static int lastIndexOf(String what, String within) {
		if (what == null || within == null || what.length() == 0) {
			return -1;
		}
		int i = 0;
		int lastI = -1;
		while ((i = within.indexOf(what, i)) != -1) {
			lastI = i++;
		}
		return lastI;
	}

	public static final String getStringEncoding() {
		return encList[0];
	}

	/**
	 * Splits a string at a given separator char in substrings and returns these
	 * elements in a string array.
	 * 
	 * @param s The string to split.
	 * @param c The separator char.
	 * @return The array containing the substrings.
	 */
	public static String[] split(String s, char c) {
		if (s == null) return null;
		Vector v = new Vector();
		int idx;
		int begin = 0;

		while (true) {
			idx = s.indexOf(c, begin);

			if (idx == -1) {
				v.addElement(s.substring(begin));
				break;
			} else {
				v.addElement(s.substring(begin, idx));
			}
			begin = idx + 1;
		}

		String[] result = new String[v.size()];
		for (int i = 0; i < result.length; i++) {
			result[i] = (String) v.elementAt(i);
		}

		return result;
	}

	/**
	 * Splits a string at a given separator char in substrings and returns these
	 * elements in a string array.
	 * 
	 * @param s The string to split.
	 * @param separator The separator char.
	 * @return The array containing the substrings.
	 */
	public static String[] split(String s, String separator) {
		if (s == null) return null;

		Vector v = stringToVector(s, separator);

		String[] result = new String[v.size()];
		for (int i = 0; i < result.length; i++) {
			result[i] = (String) v.elementAt(i);
		}

		return result;
	}

	public static String[] splitAtWhitespace(String s) {
		if (s == null) {
			return null;
		}
		if ("".equals(s = s.trim())) {
			return EMPTY_STRING_ARRAY;
		}
		List l = new ArrayList();
		int len = s.length();
		int firstNonWsIdx = -1;
		int i;
		for (i = 0; i < len; i++) {
			char c = s.charAt(i);
			switch (c) {
				/*
				 * according to XML Schema Part 2, Section 4.3.6, which itself
				 * refers to XML 1.0 (Second Edition), whitespace is defined as
				 * arbitrary sequences of one of the characters #x9 (tab), #xA
				 * (line feed), #xD (carriage return) or #x20 (space)
				 */
				case ('\t'):
				case ('\n'):
				case ('\r'):
				case (' '): {
					if (firstNonWsIdx != -1) {
						l.add(s.substring(firstNonWsIdx, i));
						firstNonWsIdx = -1;
					}
					break;
				}
				default: {
					if (firstNonWsIdx == -1) {
						firstNonWsIdx = i;
					}
					break;
				}
			}
		}
		// last chunk
		if (firstNonWsIdx != -1) {
			l.add(s.substring(firstNonWsIdx));
		}
		return (String[]) l.toArray(new String[l.size()]);
	}

	/**
	 * Converts text with spaces into <code>Vector</code>.
	 * 
	 * @param s The text to convert.
	 * @return <code>Vector</code> containing the text parts.
	 */
	public static Vector stringToVector(String s, String separator) {
		Vector v = new Vector();
		int idx;
		int begin = 0;

		while (true) {
			idx = s.indexOf(separator, begin);

			if (idx == -1) {
				v.addElement(s.substring(begin));
				break;
			} else {
				v.addElement(s.substring(begin, idx));
			}
			begin = idx + separator.length();
		}

		return v;
	}

	/**
	 * Concats the elements of a vector to a string (calls toString of each
	 * element) separated by given string. Returns null if no vector is given,
	 * returns "" if no elements are in the vector.
	 * 
	 * @param v Vector to work on.
	 * @param separator The string which should be used as separator.
	 * @return The built string.
	 */
	public static String vectorToString(Vector v, String separator) {
		if (v == null) {
			return null;
		}
		if (separator == null) {
			separator = "";
		}

		String s = "";
		for (Enumeration enu = v.elements(); enu.hasMoreElements();) {
			s += enu.nextElement().toString() + separator;
		}
		s.trim();

		return s;
	}

	/**
	 * Encodes an given URL.
	 * 
	 * @param url the URL to encode.
	 * @return the encoded URL.
	 */
	public static String encodeURL(String url) {
		/*
		 * This encoding was done according to
		 * http://www.blooberry.com/indexdot/html/topics/urlencoding.htm
		 */
		if (url != null) {
			StringBuffer buffer = new StringBuffer();
			int i = 0;
			while (i < url.length()) {
				int b = url.charAt(i++);

				boolean encoded = false;

				// get control characters encoded
				if (b >= 0x00 && b <= 0x1F || b == 0x7F) {
					buffer.append("%");
					if (b <= 0xf) buffer.append("0");
					buffer.append(Integer.toHexString(b));
					encoded = true;
				}

				// get non-ascii characters encoded
				if (b >= 0x80 && b <= 0xFF) {
					buffer.append("%");
					if (b <= 0xf) buffer.append("0");
					buffer.append(Integer.toHexString(b));
					encoded = true;
				}

				// TODO: get reserved URL characters encoded
				// switch (b) {
				// case 0x24:
				// case 0x26:
				// case 0x2B:
				// case 0x2F:
				// case 0x3A:
				// case 0x3B:
				// case 0x3D:
				// case 0x3F:
				// case 0x40:
				// case 0x21:
				// buffer.append("%");
				// if (b <= 0xf) buffer.append("0");
				// buffer.append(Integer.toHexString(b));
				// encoded = true;
				// break;
				// }

				// get unsafe characters encoded
				switch (b) {
					case 0x20: // Space
					case 0x22: // Quotation marks
					case 0x3C: // Less Than
					case 0x3E: // Greater Than
					case 0x23: // Pound
					case 0x25: // Percent
					case 0x7B: // Left Curly Brace
					case 0x7D: // Right Curly Brace
					case 0x7C: // Vertical Bar/Pipe
					case 0x5C: // Backslash
					case 0x5E: // Caret
					case 0x7E: // Tilde
					case 0x5B: // Left Square Bracket
					case 0x5D: // Right Square Bracket
					case 0x60: // Grave Accent
						buffer.append("%");
						if (b <= 0xf) buffer.append("0");
						buffer.append(Integer.toHexString(b));
						encoded = true;
						break;
				}

				if (!encoded) {
					buffer.append((char) b);
				}
			}
			return buffer.toString();
		}
		return null;
	}

	/**
	 * Decodes a String in <code>application/x-www-form-urlencoded</code> format
	 * as specified in <a
	 * href="http://www.w3.org/TR/html401/interact/forms.html#h-17.13.4.1">
	 * Chapter Forms</a> of the HTML 4.01 Specification. Uses default character
	 * encoding.
	 * 
	 * @param url String to decode.
	 * @return Decoded String.
	 * @throws UnsupportedEncodingException
	 */
	public static String decodeURL(String url) {
		try {
			return decodeURL(url, getStringEncoding());
		} catch (UnsupportedEncodingException e) {
			Log.printStackTrace(e);
		}
		return url;
	}

	/**
	 * Decodes a String in <code>application/x-www-form-urlencoded</code> format
	 * as specified in <a
	 * href="http://www.w3.org/TR/html401/interact/forms.html#h-17.13.4.1">
	 * Chapter Forms</a> of the HTML 4.01 Specification.
	 * 
	 * @param url String to decode.
	 * @param encoding Encoding.
	 * @return Decoded String.
	 * @throws UnsupportedEncodingException
	 */
	public static String decodeURL(String url, String encoding) throws UnsupportedEncodingException {
		if (encoding.length() == 0) {
			throw new UnsupportedEncodingException("StringUtil.decodeURL: No encoding specified");
		}

		int length = url.length();
		int i = 0;
		char c;
		for (; i < length; i++) {
			c = url.charAt(i);
			if (c == '+' || c == '%') {
				break;
			}
		}

		if (i == length) {
			return url;
		}

		StringBuffer mainBuffer = new StringBuffer(length);
		char[] charArray = new char[i];
		url.getChars(0, i, charArray, 0);
		mainBuffer.append(charArray);

		byte[] byteBuffer = null;

		while (i < length) {
			c = url.charAt(i);

			if (c == '%') {
				try {
					if (byteBuffer == null) {
						byteBuffer = new byte[(length - i) / 3];
					}
					int pos = 0;

					while (((i + 2) < length) && (c == '%')) {
						byteBuffer[pos++] = (byte) Integer.parseInt(url.substring(i + 1, i + 3), 16);
						i += 3;
						if (i < length) {
							c = url.charAt(i);
						}
					}

					if ((i < length) && (c == '%')) {
						throw new IllegalArgumentException("StringUtil.decodeURL: Incomplete percent pattern");
					}

					mainBuffer.append(new String(byteBuffer, 0, pos, encoding));
				} catch (NumberFormatException e) {
					throw new IllegalArgumentException("StringUtil.decodeURL: Unsupported character in percent pattern (" + e.getMessage() + ")");
				}
			} else if (c == '+') {
				mainBuffer.append(' ');
				i++;
			} else {
				mainBuffer.append(c);
				i++;
			}
		}

		return mainBuffer.toString();
	}

	/**
	 * Depending on the value of {@link #USE_LONG_CLASS_NAMES} returns either
	 * the fully qualified class name of <code>clazz</code> or the result of
	 * calling {@link #simpleClassName(Class)} on it.
	 * 
	 * @param clazz the class to return a formatted name of
	 * @return either the fully qualified or the simple class name of argument
	 *         <code>clazz</code>
	 * @see #USE_LONG_CLASS_NAMES
	 * @see #simpleClassName(Class)
	 */
	public static String formatClassName(Class clazz) {
		return formatClassName(clazz.getName());
	}

	/**
	 * Depending on the value of {@link #USE_LONG_CLASS_NAMES} returns either
	 * the same String given in argument <code>qualifiedClassName</code> or the
	 * result of calling {@link #simpleClassName(String)} on it.
	 * 
	 * @param qualifiedClassName a fully qualified Java class name
	 * @return either the same String as <code>qualifiedClassName</code> or the
	 *         simple class name
	 * @see #USE_LONG_CLASS_NAMES
	 * @see #simpleClassName(String)
	 */
	public static String formatClassName(String qualifiedClassName) {
		return USE_LONG_CLASS_NAMES ? qualifiedClassName : simpleClassName(qualifiedClassName);
	}

	/**
	 * Returns only the substring after the last dot character
	 * (&quote;.&quote;), within the name of <code>clazz</code>, i.e.
	 * <code>Object</code> instead of <code>java.lang.Object</code>.
	 * 
	 * @param clazz the class to return the simple name of
	 * @return the simple class name
	 */
	public static String simpleClassName(Class clazz) {
		return simpleClassName(clazz.getName());
	}

	/**
	 * Given a fully qualified class name (like <code>java.lang.Object</code>),
	 * returns only the substring after the last dot character
	 * (&quote;.&quote;), i.e. <code>Object</code> instead of
	 * <code>java.lang.Object</code>.
	 * 
	 * @param qualifiedClassName a fully qualified Java class name
	 * @return the simple class name
	 */
	public static String simpleClassName(String qualifiedClassName) {
		if (qualifiedClassName == null || "".equals(qualifiedClassName)) {
			return qualifiedClassName;
		}
		int idx = qualifiedClassName.lastIndexOf('.');
		return idx == -1 ? qualifiedClassName : qualifiedClassName.substring(idx + 1);
	}

	/**
	 * Converts the given Vector to String array
	 * 
	 * @param strings The Vector to convert.
	 * @return The array containing the Vector parts.
	 */
	public static String[] toStringArray(Vector strings) {
		String[] result = new String[strings.size()];
		for (int i = 0; i < strings.size(); i++) {
			result[i] = strings.elementAt(i).toString();
		}
		return result;
	}

	/**
	 * Deletes the Line Feed (10) and Carriage Return (13) fields of the given
	 * String.
	 * 
	 * @param inStr The String to work on.
	 * @return The string without Line Feed and Carriage Return.
	 */
	public static String chomp(String inStr) {
		if (inStr == null || "".equals(inStr)) {
			return inStr;
		}

		char lastChar = inStr.charAt(inStr.length() - 1);

		if (lastChar == 13) {
			// if the string ends with Carriage Return
			return inStr.substring(0, inStr.length() - 1);
		} else if (lastChar == 10) {
			// if the string ends with Line Feed
			String tmp = inStr.substring(0, inStr.length() - 1);
			if ("".equals(tmp)) {
				return tmp;
			}
			lastChar = tmp.charAt(tmp.length() - 1);
			// if the string also contains Carriage Return
			if (lastChar == 13) {
				return tmp.substring(0, tmp.length() - 1);
			} else {
				return tmp;
			}
		} else {
			return inStr;
		}
	}

	/**
	 * Parses options from a <code>String</code>. The options are defined as
	 * "-name <properties>" divided by a blank.
	 * 
	 * @param args the <code>String</code> to parse the options from.
	 * @return <code>Map</code> containing the options properties.
	 */
	public static HashMap parseStringOptions(String args) {
		HashMap options = new HashMap();
		if (args == null || args.length() == 0) return options;
		while (args != null && args.length() > 0) {
			int min = args.indexOf(Specialchars.MINUS);
			int sp = args.indexOf(Specialchars.SP, min);
			String key = args.substring(min + 1, sp);
			min = args.indexOf(Specialchars.MINUS, sp + 1);
			if (min == -1) {
				String properties = args.substring(sp + 1).trim();
				if (key.length() > 0 && properties.length() > 0) options.put(key, properties);
				return options;
			}
			String properties = args.substring(sp, min).trim();
			if (key.length() > 0 && properties.length() > 0) options.put(key, properties);
			int remove = key.length() + properties.length() + 2;
			if (remove > args.length()) {
				args = null;
			} else {
				args = args.substring(remove, args.length()).trim();
			}
		}
		return options;
	}

	/**
	 * Parses properties from a <code>String</code>. The properties are defined
	 * as "name=value" pairs divided by a blank.
	 * 
	 * @param args the <code>String</code> to parse the properties from.
	 * @return <code>Map</code> containing the properties.
	 */
	public static HashMap parseStringProperties(String args) {
		HashMap options = new HashMap();
		if (args == null || args.length() == 0) return options;
		while (args != null && args.length() > 0) {
			int eq = args.indexOf(Specialchars.EQ);
			String key = args.substring(0, eq);
			int eqN = args.indexOf(Specialchars.EQ, eq + 1);
			if (eqN == -1) {
				String value = args.substring(eq + 1, args.length());
				if (key.length() > 0 && value.length() > 0) options.put(key, value);
				return options;
			}
			int last = args.lastIndexOf(Specialchars.SP, eqN - 1);
			if (last == -1) last = args.lastIndexOf(Specialchars.SP, args.length());
			if (last == -1) last = args.length();
			String value = args.substring(eq + 1, last);
			if (key.length() > 0 && value.length() > 0) options.put(key, value);
			int remove = key.length() + value.length() + 2;
			if (remove > args.length()) {
				args = null;
			} else {
				args = args.substring(remove, args.length());
			}
		}
		return options;
	}

	/**
	 * Prints a stream to System.out.
	 * 
	 * @param in the stream to print.
	 */
	public static void printStream(InputStream in) {
		if (in == null) {
			if (Log.isDebug()) {
				Log.debug("No input stream set.", Log.DEBUG_LAYER_FRAMEWORK);
				return;
			}
		}
		byte[] buffer = new byte[8192];
		try {
			while (in.available() > 0) {
				int len = in.read(buffer);
				for (int i = 0; i < len; i++) {
					System.out.print((char) buffer[i]);
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Creates a String from an array of int.
	 * 
	 * @param array the array to show.
	 * @return the String.
	 */
	public static String arrayToStringInt(int[] array) {
		StringBuffer sBuf = new StringBuffer();
		for (int i = 0; i < array.length; i++) {
			sBuf.append(array[i]);
			if (i < array.length - 1) {
				sBuf.append(",");
			}
		}
		return sBuf.toString();
	}

	/**
	 * Creates a String from an array of byte.
	 * 
	 * @param array the array to show.
	 * @return the String.
	 */
	public static String arrayToStringByte(byte[] array) {
		StringBuffer sBuf = new StringBuffer();
		for (int i = 0; i < array.length; i++) {
			sBuf.append(array[i]);
			if (i < array.length - 1) {
				sBuf.append(",");
			}
		}
		return sBuf.toString();
	}

	/**
	 * Creates a String form a array of long.
	 * 
	 * @param array the array to show.
	 * @return the String.
	 */
	public static String arrayToStringLong(long[] array) {
		StringBuffer sBuf = new StringBuffer();
		for (int i = 0; i < array.length; i++) {
			sBuf.append(array[i]);
			if (i < array.length - 1) {
				sBuf.append(",");
			}
		}
		return sBuf.toString();
	}

	public static long getHTTPDateAsLong(String httpDate) {
		/*
		 * RFC 822 (5. DATE AND TIME SPECIFICATION)
		 */
		boolean hasDay = (httpDate.indexOf(',') > 0);
		String day = null;
		if (hasDay) {
			day = httpDate.substring(0, 3);
			httpDate = httpDate.substring(5, httpDate.length());
		}

		// dd mm yy hh:mm:ss zzz
		String[] slice = StringUtil.split(httpDate, ' ');
		String[] time = StringUtil.split(slice[3], ':');
		int ddD = Integer.valueOf(slice[0]).intValue();
		String mmDS = slice[1];
		int mmD = 0;
		for (int i = 0; i < month.length; i++) {
			if (month[i].equals(mmDS)) {
				mmD = i;
				break;
			}
		}
		int yyD = Integer.valueOf(slice[2]).intValue();
		int hhT = Integer.valueOf(time[0]).intValue();
		int mmT = Integer.valueOf(time[1]).intValue();
		int ssT = 0;
		if (time.length > 2) {
			ssT = Integer.valueOf(time[2]).intValue();
		}

		String zzzS = slice[4];

		TimeZone tz = TimeZone.getTimeZone(zzzS);
		Calendar c = Calendar.getInstance(tz);
		c.set(Calendar.DAY_OF_MONTH, ddD);
		c.set(Calendar.MONTH, mmD);
		c.set(Calendar.YEAR, yyD);
		c.set(Calendar.MINUTE, mmT);
		c.set(Calendar.HOUR_OF_DAY, hhT);
		c.set(Calendar.SECOND, ssT);
		c.set(Calendar.MILLISECOND, 0);
		return c.getTime().getTime();
	}

	public static String getHTTPDate(long date) {
		/*
		 * RFC 822 (5. DATE AND TIME SPECIFICATION)
		 */
		Date d = new Date(date);
		Calendar c = Calendar.getInstance();
		c.setTime(d);
		TimeZone tz = c.getTimeZone();
		int addHG = tz.getRawOffset() / 1000 / 3600;
		int addMG = tz.getRawOffset() / 1000 / 60 % 60;
		String addHGS = getInt(addHG);
		String addMGS = getInt(addMG);
		String add = addHGS + addMGS;
		int dI = c.get(Calendar.DAY_OF_WEEK);
		int ddD = c.get(Calendar.DAY_OF_MONTH);
		String mmD = month[c.get(Calendar.MONTH)];
		int yyD = c.get(Calendar.YEAR);
		int hhT = c.get(Calendar.HOUR_OF_DAY);
		int mmT = c.get(Calendar.MINUTE);
		int ssT = c.get(Calendar.SECOND);
		String result = day[dI - 1] + ", " + ddD + " " + mmD + " " + yyD + " " + getInt(hhT) + ":" + getInt(mmT) + ":" + getInt(ssT) + " GMT+" + add;
		return result;
	}

	private static String getInt(int i) {
		String r = "";
		if (i < 10) {
			r = "0" + String.valueOf(i);
		} else {
			r = String.valueOf(i);
		}
		return r;
	}

}
