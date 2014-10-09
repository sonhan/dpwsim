/*******************************************************************************
 * Copyright (c) 2009 MATERNA Information & Communications. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html. For further
 * project-related information visit http://www.ws4d.org. The most recent
 * version of the JMEDS framework can be obtained from
 * http://sourceforge.net/projects/ws4d-javame.
 ******************************************************************************/

package org.ws4d.java.configuration;

/**
 * Class includes name of property section headline and the property depth.
 * Minimal depth of property section headline is "1";
 * 
 * @author mspies
 */
public class PropertyHeader {

	final String[]	headers;

	/**
	 * @param headers
	 */
	public PropertyHeader(String[] headers) {
		this.headers = headers;
	}

	/**
	 * Constructor. This constructor won't initialize the super headers.
	 * Therefore the method initSuperHeaders must be invoked afterwards.
	 * 
	 * @param header header
	 * @param depth Depth of header, minimal depth is "1".
	 */
	public PropertyHeader(String header, int depth) {
		this.headers = new String[depth];
		headers[depth - 1] = header;
	}

	/**
	 * Constructor.
	 * 
	 * @param header
	 * @param superHeaders
	 */
	public PropertyHeader(String header, String[] superHeaders) {
		this.headers = new String[superHeaders.length + 1];
		System.arraycopy(superHeaders, 0, headers, 0, superHeaders.length);
		headers[superHeaders.length] = header;
	}

	/**
	 * @param header
	 * @param superHeaders
	 */
	public PropertyHeader(String header, PropertyHeader superHeaders) {
		this(header, superHeaders.headers);
	}

	public void initSuperHeaders(PropertyHeader superHeaders) {
		System.arraycopy(superHeaders.headers, 0, headers, 0, headers.length - 1);
	}

	public int depth() {
		return headers.length;
	}

	public PropertyHeader superHeader() {
		if (headers.length <= 1) {
			return null;
		}
		int newDepth = headers.length - 1;
		PropertyHeader superHeader = new PropertyHeader(headers[newDepth - 1], newDepth);
		System.arraycopy(headers, 0, superHeader.headers, 0, newDepth - 1);
		return superHeader;
	}

	public String toString() {
		StringBuffer out = new StringBuffer(headers.length * 16);
		for (int i = 0; i < headers.length; i++) {
			out.append("<" + headers[i] + ">");
		}

		return out.toString();
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + PropertyHeader.hashCode(headers);
		return result;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		PropertyHeader other = (PropertyHeader) obj;

		if (headers.length == other.headers.length) {
			for (int i = 0; i < headers.length; i++) {
				if (!headers[i].equals(other.headers[i])) return false;
			}
			return true;
		}

		return false;
	}

	private static int hashCode(Object[] array) {
		int prime = 31;
		if (array == null) return 0;
		int result = 1;
		for (int index = 0; index < array.length; index++) {
			result = prime * result + (array[index] == null ? 0 : array[index].hashCode());
		}
		return result;
	}

}
