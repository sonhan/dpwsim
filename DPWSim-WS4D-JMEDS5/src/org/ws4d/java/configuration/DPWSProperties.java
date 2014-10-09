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

import org.ws4d.java.constants.DPWSConstants;
import org.ws4d.java.constants.DPWSConstants2006;
import org.ws4d.java.structures.DataStructure;
import org.ws4d.java.structures.HashSet;
import org.ws4d.java.structures.Iterator;
import org.ws4d.java.util.Log;
import org.ws4d.java.util.StringUtil;

public class DPWSProperties implements PropertiesHandler {

	// private static DPWSProperties instance;

	/* ################# Native Router Properties ################### */

	/**
	 * Using the native udp router default: false
	 */
	public static final String	PROP_DPWS_ROUTER							= "UseCLDCUDPRouter";

	/**
	 * The routers ip default: 127.0.0.1
	 */
	public static final String	PROP_DPWS_ROUTER_ADDR						= "CLDCUDPRouterAddr";

	/**
	 * The routers port default: 1111
	 */
	public static final String	PROP_DPWS_ROUTER_PORT						= "CLDCUDPRouterPort";

	/* ###################### Connection Properties ################# */

	public static final String	PROP_DPWS_HTTP_SERVER_KEEPALIVE				= "HTTPServerKeepAlive";

	public static final String	PROP_DPWS_HTTP_CLIENT_KEEPALIVE				= "HTTPClientKeepAlive";

	public static final String	PROP_DPWS_HTTP_RESPONSE_CHUNKED_MODE		= "HTTPResponseChunkedMode";

	public static final String	PROP_DPWS_HTTP_REQUEST_CHUNKED_MODE			= "HTTPRequestChunkedMode";

	/**
	 * Time to wait for (next) request until server closes http connection.
	 */
	public static final String	PROP_DPWS_HTTP_SERVER_REQUEST_TIMEOUT		= "HTTPServerRequestTimeout";

	/**
	 * Time to wait for (next) request until client closes http connection.
	 */
	public static final String	PROP_DPWS_HTTP_CLIENT_REQUEST_TIMEOUT		= "HTTPClientRquesttimeout";

	/* ################## DPWS Version Properties ################### */

	/**
	 * Property key for supported DPWS Versions
	 */
	public static final String	PROP_DPWS_SUPPORTED_DPWS_VERSIONS			= "SupportedDPWSVersions";

	/**
	 * Property key for class name of the factory for Message2SOAPGenerator and
	 * Message2SOAPGenerator implementing classes.
	 */
	public static final String	PROP_DPWS_SOAPMSG_GENERATOR_FACTORY_CLASS	= "SOAPMessageGeneratorFactoryClass";

	// -------------------------------------------------------------------------------------------------

	public static final int		DEFAULT_DPWS_VERSION						= DPWSConstants.DPWS_VERSION2009;

	private DataStructure		supportedDPWSVersions						= new HashSet();

	/**
	 * Native Router for Communication over the CLDC Platform
	 */

	private boolean				useNativeRouter								= false;

	private String				routerIp									= "127.0.0.1";

	private int					routerPort									= 1111;

	/**
	 * Indicates whether this server should keep the connection. XXX if
	 * timeout.keepAlive == true, does not work with CLDC (no multitasking) -
	 * Problem: MetaData timeout Problem occurs in HTTPServer on line 360
	 * (while(timeout.keepAlive() || firstRequest) {)
	 */
	private boolean				httpServerKeepAlive							= true;

	/**
	 * Indicates whether this client should keep the connection.
	 */
	private boolean				httpClientKeepAlive							= true;

	/*
	 * HTTP CHUNK MODES for our DPWS communication
	 */

	/**
	 * Don't use HTTP chunked coding.
	 * <p>
	 * BE AWARE! If chunked coding is off for all messages, streams will lock a
	 * TCP communication until the stream ends. This can cause deadlocks!
	 * </p>
	 */
	public static final int		HTTP_CHUNKED_OFF							= 0;

	/**
	 * Use HTTP chunked coding.
	 */
	public static final int		HTTP_CHUNKED_ON								= 1;

	/**
	 * Don't use HTTP chunked coding for metadata exchange (wxf:Get), but use
	 * chunked coding for invoke messages.
	 */
	public static final int		HTTP_CHUNKED_ON_FOR_INVOKE					= 2;

	public static final int		DEFAULT_HTTP_CHUNKED_MODE					= HTTP_CHUNKED_ON_FOR_INVOKE;

	/**
	 * This field allows to configure HTTP chunked mode for responses (HTTP
	 * server).
	 * <p>
	 * <ul>
	 * <li>0 - chunked coding off</li>
	 * <li>1 - chunked coding on</li>
	 * <li>2(default) - chunked coding off for metadata exchange (wxf:Get etc.)
	 * but on for invoke messages.</li>
	 * </ul>
	 * </p>
	 */
	private int					httpResponseChunkedMode						= DEFAULT_HTTP_CHUNKED_MODE;

	/**
	 * This field allows to configure HTTP chunked mode for requests (HTTP
	 * client).
	 * <p>
	 * <ul>
	 * <li>0 - chunked coding off</li>
	 * <li>1 - chunked coding on</li>
	 * <li>2(default) - chunked coding off for metadata exchange (wxf:Get etc.)
	 * but on for invoke messages.</li>
	 * </ul>
	 * </p>
	 * </p>
	 */
	private int					httpRequestChunkedMode						= DEFAULT_HTTP_CHUNKED_MODE;

	/**
	 * This field specifies the time until the HTTP Server closes a connection
	 * while not receiving a request.
	 */
	private long				httpServerRequestTimeout					= 20000;

	/**
	 * This field specifies the time until the HTTP Client closes a connection
	 * while not sending a request.
	 */
	private long				httpClientRequestTimeout					= 20000;

	/**
	 * Class name of the factory for soap from/to message generating classes.
	 */
	private String				soapMessageGeneratorFactoryClass			= null;

	public static DPWSProperties getInstance() {
		// return (instance == null ? (instance = new DPWSProperties()) :
		// instance);
		return (DPWSProperties) Properties.forClassName(Properties.DPWS_PROPERTIES_HANDLER_CLASS);
	}

	DPWSProperties() {
		supportedDPWSVersions.add(new Integer(DPWSConstants.DPWS_VERSION2009)); // should
																				// be
																				// DPWS1.1
		supportedDPWSVersions.add(new Integer(DPWSConstants2006.DPWS_VERSION2006));
	}

	public boolean getNativeRouter() {
		return useNativeRouter;
	}

	public String getNativeRouterIp() {
		return routerIp;
	}

	public int getNativeRouterPort() {
		return routerPort;
	}

	public boolean getHTTPServerKeepAlive() {
		return httpServerKeepAlive;
	}

	public boolean getHTTPClientKeepAlive() {
		return httpClientKeepAlive;
	}

	public int getHTTPResponseChunkedMode() {
		return httpResponseChunkedMode;
	}

	public int getHTTPRequestChunkedMode() {
		return httpRequestChunkedMode;
	}

	public long getHTTPServerRequestTimeout() {
		return httpServerRequestTimeout;
	}

	public long getHTTPClientRequestTimeout() {
		return httpClientRequestTimeout;
	}

	public String getSOAPMessageGeneratorFactoryClass() {
		return soapMessageGeneratorFactoryClass;
	}

	public void setNativeRouterPort(int port) {
		routerPort = port;
	}

	public void setNativeRouterIp(String ip) {
		routerIp = ip;
	}

	public void setNativeRouter(boolean b) {
		useNativeRouter = b;
	}

	public void setHTTPServerKeepAlive(boolean b) {
		httpServerKeepAlive = b;
	}

	public void setHTTPClientKeepAlive(boolean b) {
		httpClientKeepAlive = b;
	}

	public void setHTTPResponseChunkedMode(int i) {
		httpResponseChunkedMode = i;
	}

	public void setHTTPRequestChunkedMode(int i) {
		httpRequestChunkedMode = i;
	}

	public void setHTTPServerRequestTimeout(long timeout) {
		httpServerRequestTimeout = timeout;
	}

	public void setHTTPClientRequestTimeout(long timeout) {
		httpClientRequestTimeout = timeout;
	}

	public void setSOAPMessageGeneratorFactoryClass(String className) {
		soapMessageGeneratorFactoryClass = className;
	}

	public void addSupportedDPWSVersion(int versionInfo) {
		supportedDPWSVersions.add(new Integer(versionInfo));
	}

	public void removeSupportedDPWSVersion(int versionInfo) {
		supportedDPWSVersions.remove(new Integer(versionInfo));
	}

	public HashSet getSupportedDPWSVersions() {
		if (supportedDPWSVersions.size() < 1) {
			supportedDPWSVersions.add(new Integer(DEFAULT_DPWS_VERSION));
		}
		return (HashSet) supportedDPWSVersions;
	}

	private void setSupportedDPWSVersions(String value) {
		if (value != null && !value.equals("")) {
			String[] tmp = StringUtil.split(value, ',');
			// Bugfix SSc 2011-01-13 Must be less and not less than
			for (int i = 0; i < tmp.length; i++) {
				String val = tmp[i].trim();
				if (StringUtil.equalsIgnoreCase(DPWSConstants.DPWS_2009_NAME, val)) {
					supportedDPWSVersions.add(new Integer(DPWSConstants.DPWS_VERSION2009));
				} else if (StringUtil.equalsIgnoreCase(DPWSConstants2006.DPWS_2006_NAME, val)) {
					supportedDPWSVersions.add(new Integer(DPWSConstants2006.DPWS_VERSION2006));
				} else {
					throw new RuntimeException("Unrecognized DPWS Version in Properties defined, known values are: 'DPWS1.1', 'DPWS2006' or both (comma separated).");
				}
			}
		} else {
			throw new RuntimeException("No Supported Version in Properties defined, for example use DPWS1.1, DPWS2006 or both (comma separated).");
		}
	}

	public void finishedSection(int depth) {
		// TODO implement me!
	}

	public String printSupportedDPWSVersions() {
		HashSet set = getSupportedDPWSVersions();
		StringBuffer string = new StringBuffer();
		string.append("Supported DPWS Version(s): ");
		Iterator it = set.iterator();
		while (it.hasNext()) {
			Integer version = (Integer) it.next();
			if (version.intValue() == DPWSConstants.DPWS_VERSION2009) {
				string.append("DPWS1.1");
			} else if (version.intValue() == DPWSConstants2006.DPWS_VERSION2006) {
				string.append("DPWS2006");
			}
			if (it.hasNext()) {
				string.append(", ");
			}
		}
		return string.toString();
	}

	public void setProperties(PropertyHeader header, Property property) {
		if (Properties.HEADER_SECTION_DPWS.equals(header)) {
			try {
				if (PROP_DPWS_ROUTER.equals(property.key)) {
					useNativeRouter = property.value.equals("true");
				} else if (PROP_DPWS_ROUTER_ADDR.equals(property.key)) {
					routerIp = property.value;
				} else if (PROP_DPWS_ROUTER_PORT.equals(property.key)) {
					routerPort = Integer.parseInt(property.value.trim());
				} else if (PROP_DPWS_HTTP_SERVER_KEEPALIVE.equals(property.key)) {
					setHTTPServerKeepAlive(property.value.equals("true"));
				} else if (PROP_DPWS_HTTP_CLIENT_KEEPALIVE.equals(property.key)) {
					setHTTPClientKeepAlive(property.value.equals("true"));
				} else if (PROP_DPWS_HTTP_RESPONSE_CHUNKED_MODE.equals(property.key)) {
					setHTTPResponseChunkedMode(Integer.parseInt(property.value.trim()));
				} else if (PROP_DPWS_HTTP_REQUEST_CHUNKED_MODE.equals(property.key)) {
					setHTTPRequestChunkedMode(Integer.parseInt(property.value.trim()));
				} else if (PROP_DPWS_SUPPORTED_DPWS_VERSIONS.equals(property.key)) {
					setSupportedDPWSVersions(property.value);
				} else if (PROP_DPWS_HTTP_CLIENT_REQUEST_TIMEOUT.equals(property.key)) {
					setHTTPClientRequestTimeout(Long.parseLong(property.value.trim()));
				} else if (PROP_DPWS_HTTP_SERVER_REQUEST_TIMEOUT.equals(property.key)) {
					setHTTPServerRequestTimeout(Long.parseLong(property.value.trim()));
				} else if (PROP_DPWS_SOAPMSG_GENERATOR_FACTORY_CLASS.equals(property.key)) {
					setSOAPMessageGeneratorFactoryClass(property.value);
				}
			} catch (NumberFormatException e) {
				Log.printStackTrace(e);
			}
		}
	}

}