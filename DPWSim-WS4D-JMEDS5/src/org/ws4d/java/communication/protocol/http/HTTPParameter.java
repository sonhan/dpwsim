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

import java.io.UnsupportedEncodingException;
import java.util.Enumeration;
import java.util.Hashtable;

import org.ws4d.java.types.URI;
import org.ws4d.java.util.Log;
import org.ws4d.java.util.StringUtil;

/**
 * The HTTPParameter class decodes the URI and creates a table with the
 * parameters found in the HTTP request.
 */
public class HTTPParameter {

	protected static final String	DEFAULT_SEPARATOR	= "&";

	protected static final String	DEFAULT_PARACHAR	= "=";

	protected static final String	DEFAULT_ENCODING	= StringUtil.getStringEncoding();

	/** Hashtable mapping the parameter name to the parameter. */
	private Hashtable				stringParameters;

	/**
	 * Default constructor.
	 * 
	 * @param uri the URI which should be decoded at the beginning.
	 */
	public HTTPParameter(URI uri) {
		stringParameters = new Hashtable();
		decodeURIString(uri.getQuery());
	}

	/**
	 * Decodes a string.<br />
	 * e.g.<br />
	 * string syntax: "parameter1=value1&parameter2=value2" or
	 * "parameter1=value1&parameter2=value2";
	 * 
	 * @param parameterString the string with parameters.
	 */
	private void decodeURIString(String parameterString) {
		if (parameterString == null) return;
		int intFirstParamIndex = 0;
		String reqURIParam = parameterString;

		// Build up hashtable with parameter for action to be invoked
		if (reqURIParam.length() > 0) {

			// strip off optional parameterList from given uri
			intFirstParamIndex = reqURIParam.indexOf(DEFAULT_SEPARATOR);

			while (intFirstParamIndex != -1) {
				String param = reqURIParam.substring(0, intFirstParamIndex);

				int div = reqURIParam.indexOf(DEFAULT_PARACHAR);
				if (div != -1) {
					try {
						stringParameters.put(StringUtil.decodeURL(param.substring(0, div), DEFAULT_ENCODING), StringUtil.decodeURL(param.substring(div + 1, intFirstParamIndex), DEFAULT_ENCODING));
					} catch (UnsupportedEncodingException e) {
						Log.printStackTrace(e);
					}
				}

				reqURIParam = reqURIParam.substring(intFirstParamIndex + 1);
				intFirstParamIndex = reqURIParam.indexOf(DEFAULT_SEPARATOR);
			}

			String param = reqURIParam;

			int div = reqURIParam.indexOf(DEFAULT_PARACHAR);
			if (div != -1) {
				try {
					stringParameters.put(StringUtil.decodeURL(param.substring(0, div), DEFAULT_ENCODING), StringUtil.decodeURL(param.substring(div + 1, param.length()), DEFAULT_ENCODING));
				} catch (UnsupportedEncodingException e) {
					Log.printStackTrace(e);
				}
			} else {
				try {
					stringParameters.put(StringUtil.decodeURL(param, DEFAULT_ENCODING), StringUtil.decodeURL("", DEFAULT_ENCODING));
				} catch (UnsupportedEncodingException e) {
					Log.printStackTrace(e);
				}
			}
		}
	}

	/**
	 * Creates a string with parameters from the table.
	 * 
	 * @return the coded string.
	 */
	public synchronized String encodeURIParameters() {
		String param = new String();
		for (Enumeration enu = stringParameters.keys(); enu.hasMoreElements();) {
			String key = (String) enu.nextElement();
			String element = (String) stringParameters.get(key);

			param = param.concat(key + DEFAULT_PARACHAR + element);

			if (enu.hasMoreElements()) {
				param = param.concat(DEFAULT_SEPARATOR);
			}

		}
		return param;
	}

	/**
	 * Sets parameter inside the table.
	 * 
	 * @param parameterName the parameter name.
	 * @param parameterValue the parameter value.
	 */
	public synchronized void setURIParameter(String parameterName, String parameterValue) {
		stringParameters.put(parameterName, parameterValue);
	}

	/**
	 * Returns parameter value from table.
	 * 
	 * @param paramName the parameter name.
	 * @return string value found in the table.
	 */
	public String getURIParameter(String paramName) {
		return (String) stringParameters.get(paramName);
	}
}
