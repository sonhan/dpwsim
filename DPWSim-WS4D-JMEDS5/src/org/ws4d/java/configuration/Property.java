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
 * Class holds property name and value
 */
public class Property {

	// ================ Device Properties ==================

	public static final String	PROP_DEVICE_UUID								= "DeviceUuid";

	public static final String	PROP_METADATA_VERSION							= "MetadataVersion";

	public static final String	PROP_SEND_WSDL									= "SendWSDL";

	// ================ Service Properties ==================

	// Should the service be secured or not
	public static final String	PROP_SERVICE_SECURED							= "ServiceSecured";

	// ================ Global Properties ==================

	// ---------- System behavior Properties ---------------
	public static final String	PROP_ASYNC_ONEWAY_OPS							= "AsyncOnewayOperations";

	// Workaround for CLDC/MIDP environments without multi-casting
	// works only with platform_cldc toolkit
	public static final String	PROP_BROADCAST_ADDRESS							= "BroadcastAddress";

	public static final String	PROP_DEVICE_IP_ADDRESS							= "DeviceIPAddress";

	// experimental IP change detection
	public static final String	PROP_DEVICE_IP_CHECK							= "DeviceIPCheck";

	public static final String	PROP_DEVICE_START_TIME							= "DeviceStartTime";

	public static final String	PROP_DPWS_MCAST_MODE							= "MulticastMode";

	public static final String	PROP_FORCE_SHUTDOWN								= "ForceShutdown";

	public static final String	PROP_PROXY_USE_DISABLED							= "ProxyUseDisabled";

	public static final String	PROP_UDP_USE_TIMED_MESSAGEID_BUFFER				= "UDPUseTimedMessageIDBuffer";

	// Memorize WSDL. Costs memory! Won't memorize, if PROP_DEVICE_IP_CHECK ==
	// true
	public static final String	PROP_WSDL_MEMORIZE								= "WSDLMemorize";

	// ---------- Buffering Properties ------------------
	public static final String	PROP_BUFFERED_IN_TIMEOUT_REPEAT					= "BufferedInputTimeoutRepeat";

	public static final String	PROP_BUFFERED_IN_TIMEOUT_WAIT					= "BufferedInputTimeoutWait";

	public static final String	PROP_BUFFERED_IN_USE_TIMEOUT					= "BufferedInputUseTimeout";

	public static final String	PROP_BUFFERED_IN_BUF_SIZE						= "BufferedInputBufSize";

	public static final String	PROP_BUFFERED_WRITER_BUF_SIZE					= "BufferedWriterBufSize";

	public static final String	PROP_BYTE_BUFFER_BUF_SIZE						= "ByteBufferBufSize";

	// ---------- HTTP Server Properties -----------------
	public static final String	PROP_HTTP_CLIENT_READ_TIMEOUT_REPEAT			= "HttpClientReadTimeoutRepeat";

	public static final String	PROP_HTTP_CLIENT_READ_TIMEOUT_WAIT				= "HttpClientReadTimeoutWait";

	public static final String	PROP_HTTP_SERVER_MAX_THREADS					= "HttpServerMaxThreads";

	public static final String	PROP_HTTP_SERVER_USE_THREAD_POOL				= "HttpServerUseThreadPool";

	public static final String	PROP_HTTP_SERVER_PORT							= "HttpServerPort";

	public static final String	PROP_HTTPS_SERVER_PORT							= "HttpsServerPort";

	public static final String	PROP_HTTP_CONNECTION_TIMEOUT					= "HttpConnectionTimeout";

	// ---------- SearchCache properties --------------------
	public static final String	PROP_SEARCH_CACHE_ENABLED						= "SearchCacheEnabled";

	public static final String	PROP_SEARCH_CACHE_ENTRY_EXPIRATION_TIME			= "SearchCacheEntryExpirationTime";

	public static final String	PROP_MAX_SEARCH_CACHE_THREAD_NUMBER				= "MaxSearchCacheThreadNumber";

	public static final String	PROP_MAX_SEARCH_CACHE_ENTRIES					= "MaxSearchCacheEntries";

	public static final String	PROP_SEARCH_CACHE_ENTRIES_REDUCTION				= "SearchCacheEntriesReduction";

	public static final String	PROP_SEARCH_CACHE_COMPARE_XADDRS				= "CacheCompareXAddrs";

	// ---------- PresentationURL properties -----------------
	public static final String	PROP_PRESENTATION_URL_USE_STYLESHEET			= "PresentationURLUseStylesheet";

	public static final String	PROP_PRESENTATION_URL_ATTACHMENT_DURATION		= "PresentationURLAttachmentDuration";

	public static final String	PROP_PRESENTATION_URL_USE_FILECACHING			= "PresentationURLUseFileCaching";

	public static final String	PROP_PRESENTATION_URL_SHOW_RESOURCES_DATABOX	= "PresentationURLShowRessourcesDatabox";

	public static final String	PROP_PRESENTATION_URL_SHOW_SESSION_DATABOX		= "PresentationURLShowSessionDatabox";

	public static final String	PROP_PRESENTATION_URL_HIDE_DEFAULT_RESOURCES	= "PresentationURLHideDefaultRessources";

	// ---------- WSDLRepository Properties -----------------
	public static final String	PROP_WSDLREPOSITORY_NAME						= "WSDLRepositoryName";

	public static final String	PROP_WSDLREPOSITORY_WSDL_AUTOADD				= "WSDLRepositoryWSDLAutoAdd";

	// ---------- XML Properties -------------------
	public static final String	PROP_SCHEMA_DOWNLOAD							= "SchemaDownload";

	public static final String	PROP_XML_INDENT									= "XMLIndent";

	public static final String	PROP_XML_VERSION								= "XMLVersion";

	// ---------- Resolver Properties ------------------
	public static final String	PROP_RESOLVER_CACHE_TIMEOUT						= "ResolverCacheTimeout";

	public static final String	PROP_RESOLVER_NO_CACHE							= "ResolverNoCache";

	// ---------- Eventing Properties -------------------
	public static final String	PROP_EVENTING_MAXTRIES							= "EventingMaxTries";

	// ---------- Logging Properties -----------------
	public static final String	PROP_LOG_LEVEL									= "LogLevel";

	public static final String	PROP_LOG_TIMESTAMP								= "LogTimestamp";

	public static final String	PROP_LOG_XML_OUTPUT								= "LogXMLOutput";

	// ---------- Network Interface Properties --------------
	// Used Network Interface (SE only)
	public static final String	PROP_DEVICE_SE_NETWORKINTERFACE					= "DeviceSENetworkInterface";

	// Used Network Interface (SE only)
	public static final String	PROP_ENCODING									= "Encoding";

	// ---------- SSL Properties --------------------------
	// Set the location of the KeyStore file
	public static final String	PROP_KEYSTORE_FILE								= "KeyStoreFile";

	// Set the password of the KeyStore file
	public static final String	PROP_KEYSTORE_PASSWORD							= "KeyStorePassword";

	// ---------- Management Properties --------------------------
	public static final String	PROP_DEVICE_ADMIN_SERVICE						= "DeviceAdminService";

	public String				key;

	public String				value;

	public Property(String key, String value) {
		this.key = key;
		this.value = value;
	}

	public String toString() {
		return "<" + key + ">=<" + value + ">";
	}

}
