/*******************************************************************************
 * Copyright (c) 2009 MATERNA Information & Communications. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html. For further
 * project-related information visit http://www.ws4d.org. The most recent
 * version of the JMEDS framework can be obtained from
 * http://sourceforge.net/projects/ws4d-javame.
 ******************************************************************************/
package org.ws4d.java.communication;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Random;

import org.ws4d.java.DPWSFramework;
import org.ws4d.java.attachment.Attachment;
import org.ws4d.java.attachment.AttachmentException;
import org.ws4d.java.communication.DPWS2006.DefaultDPWSCommunicatonUtil;
import org.ws4d.java.communication.connection.ip.IPAddress;
import org.ws4d.java.communication.connection.ip.IPNetworkDetection;
import org.ws4d.java.communication.connection.ip.NetworkInterface;
import org.ws4d.java.communication.monitor.ResourceLoader;
import org.ws4d.java.communication.protocol.http.HTTPGroup;
import org.ws4d.java.communication.protocol.http.HTTPClient;
import org.ws4d.java.communication.protocol.http.HTTPClientDestination;
import org.ws4d.java.communication.protocol.http.HTTPRequest;
import org.ws4d.java.communication.protocol.http.HTTPRequestUtil;
import org.ws4d.java.communication.protocol.http.server.DefaultHTTPResourceHandler;
import org.ws4d.java.communication.protocol.http.server.HTTPServer;
import org.ws4d.java.communication.protocol.mime.DefaultMIMEHandler;
import org.ws4d.java.communication.protocol.mime.MIMEBodyHeader;
import org.ws4d.java.communication.protocol.soap.SOAPRequest;
import org.ws4d.java.communication.protocol.soap.SOAPoverUDPClient;
import org.ws4d.java.communication.protocol.soap.SOAPoverUDPClient.SOAPoverUDPHandler;
import org.ws4d.java.communication.protocol.soap.generator.MessageReceiver;
import org.ws4d.java.communication.protocol.soap.generator.SOAPMessageGeneratorFactory;
import org.ws4d.java.communication.protocol.soap.server.SOAPServer;
import org.ws4d.java.communication.protocol.soap.server.SOAPServer.SOAPHandler;
import org.ws4d.java.communication.protocol.soap.server.SOAPoverUDPServer;
import org.ws4d.java.configuration.DPWSProperties;
import org.ws4d.java.configuration.DispatchingProperties;
import org.ws4d.java.configuration.Properties;
import org.ws4d.java.constants.ConstantsHelper;
import org.ws4d.java.constants.DPWSConstants;
import org.ws4d.java.constants.DPWSConstants2006;
import org.ws4d.java.constants.DPWSMessageConstants;
import org.ws4d.java.constants.HTTPConstants;
import org.ws4d.java.constants.MIMEConstants;
import org.ws4d.java.constants.PrefixRegistry;
import org.ws4d.java.constants.WSAConstants;
import org.ws4d.java.constants.WSAConstants2006;
import org.ws4d.java.dispatch.MessageInformer;
import org.ws4d.java.dispatch.RequestResponseCoordinator;
import org.ws4d.java.message.Message;
import org.ws4d.java.message.SOAPHeader;
import org.ws4d.java.message.metadata.GetMetadataMessage;
import org.ws4d.java.structures.ArrayList;
import org.ws4d.java.structures.DataStructure;
import org.ws4d.java.structures.HashMap;
import org.ws4d.java.structures.HashSet;
import org.ws4d.java.structures.Iterator;
import org.ws4d.java.structures.LinkedSet;
import org.ws4d.java.structures.List;
import org.ws4d.java.structures.MessageIdBuffer;
import org.ws4d.java.structures.Set;
import org.ws4d.java.types.InternetMediaType;
import org.ws4d.java.types.QNameSet;
import org.ws4d.java.types.URI;
import org.ws4d.java.types.XAddressInfo;
import org.ws4d.java.util.Log;
import org.ws4d.java.util.MIMEUtil;
import org.ws4d.java.util.WS4DIllegalStateException;

/**
 * 
 */
public class DPWSCommunicationManager implements CommunicationManager {
	
	public static final String					COMMUNICATION_MANAGER_ID		= "DPWS";

	private static final Random					RND								= new Random();

	private static final MessageReceiver		GENERIC_RECEIVER				= new GenericReceiver();

	private static final MessageInformer		MESSAGE_INFORMER				= MessageInformer.getInstance();

	private static final CommunicationUtil		DPWS_UTIL						= DefaultDPWSCommunicatonUtil.getInstance();

	protected static final MessageIdBuffer		SENT_MULTICAST_MESSAGE_IDS		= new MessageIdBuffer();

	private volatile boolean					stopped							= true;

	// key = DPWSDomain, value = SOAPoverUDPClient
	private final HashMap						udpClientsPerDomain				= new HashMap();

	// contains either the IPv4 or the IPv6 based multicast UDP server, or both
	private final HashMap						udpServers						= new HashMap();

	// key = host ':' port, value = SOAPServer
	private final HashMap						soapServers						= new HashMap();

	// key = host ':' port, value = HTTPServer
	private final HashMap						httpServers						= new HashMap();

	private final RequestResponseCoordinator	rrc								= RequestResponseCoordinator.getInstance();

	private final SOAPoverUDPHandler			udpResponseHandler				= new SOAPoverUDPHandler(new UDPResponseReceiver(rrc));

	private static Set							registerForGetMetadata			= new HashSet();

	private static Set							MessageIDsForGetMetadataMapping	= new HashSet();

	private final Object						udpTransmissionsLock			= new Object();

	private volatile int						pendingUDPTransmissions			= 0;

	private DataStructure						domains							= getAvailableDomains();

	private DataStructure						discoveryBindings				= null;

	public static Set getRegisterForGetMetadata() {
		return registerForGetMetadata;
	}

	public static Set getMessageIDsForGetMetadataMapping() {
		return MessageIDsForGetMetadataMapping;
	}

	public static boolean hasSentMessage(URI messageId) {
		return SENT_MULTICAST_MESSAGE_IDS.contains(messageId);
	}

	/**
	 * Public default constructor, needed for reflective instance creation (
	 * <code>Class.forName(...)</code>).
	 */
	public DPWSCommunicationManager() {
		super();
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.communication.CommunicationManager#init()
	 */
	public void init() {
		Properties.getInstance().register(Properties.HEADER_SECTION_DPWS, Properties.DPWS_PROPERTIES_HANDLER_CLASS);
		Properties.getInstance().register(Properties.HEADER_SECTION_HTTP, Properties.HTTP_PROPERTIES_HANDLER_CLASS);
		Properties.getInstance().register(Properties.HEADER_SECTION_IP, Properties.IP_PROPERTIES_HANDLER_CLASS);

		PrefixRegistry.addPrefix(DPWSConstants2006.DPWS_NAMESPACE_NAME, DPWSConstants2006.DPWS_NAMESPACE_PREFIX);
		PrefixRegistry.addPrefix(DPWSConstants.DPWS_NAMESPACE_NAME, DPWSConstants.DPWS_NAMESPACE_PREFIX);
	}

	/**
	 * @return
	 */
	public String getCommunicationManagerId() {
		return COMMUNICATION_MANAGER_ID;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.ws4d.java.communication.CommunicationManager#getAvailableDomains()
	 */
	public DataStructure getAvailableDomains() {
		if (domains == null) {
			domains = new ArrayList();
			for (Iterator it = IPNetworkDetection.getInstance().getNetworkInterfaces(); it.hasNext();) {
				NetworkInterface ni = (NetworkInterface) it.next();

				if (!ni.isUp()) {
					if (Log.isDebug()) {
						Log.debug("The interface " + ni.getName() + " is not up and running...", Log.DEBUG_LAYER_COMMUNICATION);
					}
					continue;
				}

				if (!ni.supportsMulticast()) {
					if (Log.isDebug()) {
						Log.debug("The interface " + ni.getName() + " does not support multicast. No listener will be bound.", Log.DEBUG_LAYER_COMMUNICATION);
					}
					continue;
				}

				for (Iterator it2 = ni.getAddresses(); it2.hasNext();) {
					IPAddress address = (IPAddress) it2.next();
					domains.add(new DPWSDomain(ni, address, true, true));
				}
			}
		}
		return domains;
	}

	public DataStructure refreshAvailableDomains() {
		this.domains = null;
		return getAvailableDomains();
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.ws4d.java.communication.CommunicationManager#addressMatches(org.ws4d
	 * .java.data.uri.URI, boolean, org.ws4d.java.communication.ProtocolData)
	 */
	public boolean addressMatches(URI xAddr, boolean source, ProtocolData protocolData) {
		if (!(protocolData instanceof DPWSProtocolData)) {
			return false;
		}
		DPWSProtocolData data = (DPWSProtocolData) protocolData;
		String otherAddr = getCanonicalAddress(source ? data.getSourceHost() : data.getDestinationHost());
		String hostAddr = getCanonicalAddress(xAddr.getHost());
		return otherAddr != null && otherAddr.equals(hostAddr);
	}

	public long getRandomApplicationDelay(int version) {
		CommunicationUtil comUtil = getCommunicationUtil();
		ConstantsHelper helper = comUtil.getHelper(version);
		int max = helper.getRandomApplicationDelay();
		if (max <= 0) {
			return 0;
		}
		int delay = RND.nextInt();
		if (delay < 0) {
			delay = -delay;
		}
		return delay % (max + 1);
	}

	public void start() throws IOException {
		if (!stopped) {
			return;
		}
		stopped = false;
		if (Log.isInfo()) {
			Log.info(DPWSProperties.getInstance().printSupportedDPWSVersions());
		}
	}

	public DataStructure getDiscoveryBindings() throws IOException {
		if (discoveryBindings != null) {
			return discoveryBindings;
		}
		DataStructure domains = getAvailableDomains();
		discoveryBindings = new LinkedSet();
		for (Iterator it = domains.iterator(); it.hasNext();) {
			ProtocolDomain domain = (ProtocolDomain) it.next();
			discoveryBindings.add(getDiscoveryBindingForDomain(domain));
		}
		return discoveryBindings;
	}

	/**
	 * Unsupported
	 */
	public DiscoveryBinding getDiscoveryBindingForDomain(ProtocolDomain domain) throws IOException {
		if (!(domain instanceof DPWSDomain)) {
			throw new IOException("Unsupported Domain: " + domain);
		}
		DPWSDomain dpwsDomain = (DPWSDomain) domain;
		int type = (dpwsDomain.getIPAddress().isIPv6()) ? 6 : 4;
		DPWSDiscoveryBinding discovery = new DPWSDiscoveryBinding(type, dpwsDomain.getInterfaceName());
		Log.info("Add discovery binding: " + discovery);
		return discovery;
	}

	public DiscoveryBinding getDiscoveryBindingForProtocolData(ProtocolData data) {
		String address = data.getDestinationAddress();
		String iFace = data.getIFace();

		DPWSDiscoveryBinding binding = null;

		if (address.indexOf(':') == -1) {
			// ipV4:
			binding = new DPWSDiscoveryBinding(4, iFace);
		} else {
			// ipV6:
			binding = new DPWSDiscoveryBinding(6, iFace);
		}
		return binding;
	}

	/**
	 */
	public DataStructure getDiscoveryDomainForBinding(DiscoveryBinding binding) throws IOException {
		if (binding == null) {
			throw new IOException("No spezcified binding");
		}
		if (!(binding instanceof DPWSDiscoveryBinding)) {
			throw new IOException("Wrong Binding: " + binding);
		}
		ArrayList test = new ArrayList();
		DPWSDiscoveryBinding bind = (DPWSDiscoveryBinding) binding;
		for (Iterator iterator = ((ArrayList) getAvailableDomains()).iterator(); iterator.hasNext();) {
			DPWSDomain domain = (DPWSDomain) iterator.next();
			int ipVersion = domain.getIPAddress().isIPv6() ? 6 : 4;
			if (domain.getInterfaceName().equals(bind.getIface()) && ipVersion == bind.ipVersion) {
				test.add(domain);
			}
		}
		if (test.size() == 0) {
			throw new IOException("No Domain found for Binding: " + binding);
		}
		return test;
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.communication.CommunicationManager#kill()
	 */
	public void kill() {
		stopInternal(true);
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.communication.CommunicationManager#shutdown()
	 */
	public void stop() {
		stopInternal(false);
	}

	private void stopInternal(boolean kill) {
		if (stopped) {
			return;
		}
		synchronized (udpTransmissionsLock) {
			while (pendingUDPTransmissions > 0) {
				try {
					udpTransmissionsLock.wait();
				} catch (InterruptedException e) {
					// ignore
				}
			}

			// close servers first
			synchronized (udpServers) {
				for (Iterator it = udpServers.values().iterator(); it.hasNext();) {
					SOAPoverUDPServer server = (SOAPoverUDPServer) it.next();
					try {
						server.stop();
					} catch (IOException e) {
						Log.error("Unable to close SOAPoverUDPServer: " + e);
						Log.printStackTrace(e);
					}
				}
				udpServers.clear();
			}
			synchronized (soapServers) {
				for (Iterator it = soapServers.values().iterator(); it.hasNext();) {
					SOAPServer server = (SOAPServer) it.next();
					try {
						SOAPServer.unregisterAndStop(server);
					} catch (IOException e) {
						Log.error("Unable to close SOAPServer: " + e);
						Log.printStackTrace(e);
					}
				}
				soapServers.clear();
			}
			SOAPMessageGeneratorFactory.clear();
			synchronized (httpServers) {
				for (Iterator it = httpServers.values().iterator(); it.hasNext();) {
					HTTPServer server = (HTTPServer) it.next();
					try {
						HTTPServer.unregisterAndStop(server);
					} catch (IOException e) {
						Log.error("Unable to close HTTPServer: " + e);
						Log.printStackTrace(e);
					}
				}
				httpServers.clear();
			}
			// now close clients, too
			synchronized (udpClientsPerDomain) {
				for (Iterator it = udpClientsPerDomain.values().iterator(); it.hasNext();) {
					SOAPoverUDPClient client = (SOAPoverUDPClient) it.next();
					try {
						client.close();
					} catch (IOException e) {
						Log.error("Unable to close SOAPoverUDPClient: " + e);
						Log.printStackTrace(e);
					}
				}
				udpClientsPerDomain.clear();
			}
			if (kill) {
				HTTPClient.killAllClients();
			} else {
				HTTPClient.closeAllClients();
			}

			SENT_MULTICAST_MESSAGE_IDS.clear();

			stopped = true;
		}
	}

	/**
	 * Registers a device to receive (TCP) messages on a specified socket.
	 * 
	 * @param messageTypes Messages to receive. See for example
	 *            DPWSMessageConstants.
	 * @param binding HttpBinding.
	 * @param listener
	 * @throws IOException If the binding type does not match.
	 * @throws WS4DIllegalStateException
	 */
	public void registerDevice(int[] messageTypes, CommunicationBinding binding, IncomingMessageListener listener, HTTPGroup user) throws IOException, WS4DIllegalStateException {
		checkStopped();
		try {
			HTTPBinding httpBinding = (HTTPBinding) binding;
			SOAPServer server;
			if (DPWSFramework.hasModule(DPWSFramework.SECURITY_MODULE)) {
				server = getSOAPServer(httpBinding.getHostAddress(), httpBinding.getPort(), httpBinding.getType() == IPBinding.HTTPS_BINDING, DPWSFramework.getSecurityManager().getAliasFromBinding(httpBinding));
			} else {
				server = getSOAPServer(httpBinding.getHostAddress(), httpBinding.getPort(), httpBinding.getType() == IPBinding.HTTPS_BINDING, null);
			}
			if (httpBinding.getPort() == 0) {
				httpBinding.setPort(server.getHTTPServer().getPort());
			}
			String path = httpBinding.getPath();
			SOAPHandler handler = new IncomingSOAPReceiver(listener);
			server.register(path, handler, user);

			for (int i = 0; i < messageTypes.length; i++) {
				if (DPWSMessageConstants.GET_METADATA_MESSAGE == messageTypes[i]) {
					addUriToRegister(httpBinding.getTransportAddress(), registerForGetMetadata);
				}

				if (DPWSMessageConstants.INVOKE_MESSAGE == messageTypes[i]) {
					DefaultMIMEHandler requestHandler = new DefaultMIMEHandler();
					requestHandler.register(InternetMediaType.getApplicationXOPXML(), new IncomingMIMEReceiver(listener));
					requestHandler.register(2, -1, AttachmentStoreHandler.getInstance());			
					server.getHTTPServer().register(path, InternetMediaType.getMultipartRelated(), requestHandler, user);

				}
			}
		} catch (ClassCastException e) {
			throw new IOException("Unsupported binding type. Need HTTPBinding but was: " + binding);
		}
	}

	/**
	 * Registers a service to receive (TCP) messages on a specified socket.
	 * 
	 * @param messageTypes Messages to receive. See for example
	 *            DPWSMessageConstants.
	 * @param binding HttpBinding.
	 * @param listener
	 * @throws IOException If the binding type does not match.
	 * @throws WS4DIllegalStateException
	 */
	public void registerService(int[] messageTypes, CommunicationBinding binding, IncomingMessageListener listener, HTTPGroup user) throws IOException, WS4DIllegalStateException {
		checkStopped();
		try {
			HTTPBinding httpBinding = (HTTPBinding) binding;
			SOAPServer server;
			if (DPWSFramework.hasModule(DPWSFramework.SECURITY_MODULE)) {
				server = getSOAPServer(httpBinding.getHostAddress(), httpBinding.getPort(), httpBinding.getType() == IPBinding.HTTPS_BINDING, DPWSFramework.getSecurityManager().getAliasFromBinding(httpBinding));
			} else {

				server = getSOAPServer(httpBinding.getHostAddress(), httpBinding.getPort(), httpBinding.getType() == IPBinding.HTTPS_BINDING, null);
			}
			if (httpBinding.getPort() == 0) {
				httpBinding.setPort(server.getHTTPServer().getPort());
			}
			String path = httpBinding.getPath();
			SOAPHandler handler = new IncomingSOAPReceiver(listener);
			server.register(path, handler, user);

			for (int i = 0; i < messageTypes.length; i++) {
				if (DPWSMessageConstants.GET_METADATA_MESSAGE == messageTypes[i]) {
					addUriToRegister(httpBinding.getTransportAddress(), registerForGetMetadata);
				}

				if (DPWSMessageConstants.INVOKE_MESSAGE == messageTypes[i]) {
					DefaultMIMEHandler requestHandler = new DefaultMIMEHandler();
					requestHandler.register(InternetMediaType.getApplicationXOPXML(), new IncomingMIMEReceiver(listener));
					if (DPWSFramework.hasModule(DPWSFramework.ATTACHMENT_MODULE)) {
						requestHandler.register(2, -1, AttachmentStoreHandler.getInstance());
					}
					server.getHTTPServer().register(path, InternetMediaType.getMultipartRelated(), requestHandler, user);
				}
			}
		} catch (ClassCastException e) {
			throw new IOException("Unsupported binding type. Need HTTPBinding but was: " + binding);
		}
	}

	/**
	 * Registers the framework to receive (UDP) discovery messages on a
	 * specified socket.
	 * 
	 * @param messageTypes Messages to receive. See for example
	 *            DPWSMessageConstants.
	 * @param binding DiscoveryBinding.
	 * @param listener
	 * @throws IOException If the binding type does not match.
	 * @throws WS4DIllegalStateException
	 */
	public void registerDeviceReference(int[] messageTypes, DiscoveryBinding binding, IncomingMessageListener listener) throws IOException, WS4DIllegalStateException {
		if (binding == null) {
			throw new IOException("Parameter \"binding\" must not be null.");
		}
		
		checkStopped();
		
		try {
			DPWSDiscoveryBinding discoveryBinding = (DPWSDiscoveryBinding) binding;
			SOAPoverUDPServer server = getSOAPoverUDPServer(discoveryBinding.getHostAddress(), discoveryBinding.getPort(), discoveryBinding.getIface());
			IncomingUDPReceiver receiver = (IncomingUDPReceiver) server.getHandler();
			receiver.register(messageTypes, listener);
			if (Log.isDebug()) {
				Log.debug("Lifecycle discovery over: " + discoveryBinding, Log.DEBUG_LAYER_COMMUNICATION);
			}
		} catch (ClassCastException e) {
			throw new IOException("Unsupported binding type. Need DPWSDiscoveryBinding but was: " + binding);
		}
	}

	/**
	 * Registers the framework to receive (UDP) discovery messages on a
	 * specified socket.
	 * 
	 * @param messageTypes Messages to receive. See for example
	 *            DPWSMessageConstants.
	 * @param binding DiscoveryBinding.
	 * @param listener
	 * @throws IOException If the binding type does not mat
	 * @throws WS4DIllegalStateException
	 */
	public void registerDiscovery(int[] messageTypes, DiscoveryBinding binding, IncomingMessageListener listener) throws IOException, WS4DIllegalStateException {
		checkStopped();
		try {
			DPWSDiscoveryBinding discoveryBinding = (DPWSDiscoveryBinding) binding;
			SOAPoverUDPServer server = getSOAPoverUDPServer(discoveryBinding.getHostAddress(), discoveryBinding.getPort(), discoveryBinding.getIface());
			if (server != null) {
				IncomingUDPReceiver receiver = (IncomingUDPReceiver) server.getHandler();
				receiver.register(messageTypes, listener);
			}
		} catch (ClassCastException e) {
			throw new IOException("Unsupported binding type. Need DiscoveryBinding but was: " + binding.getClass().getName());
		}
	}

	public void unregisterDevice(int[] messageTypes, CommunicationBinding binding, IncomingMessageListener listener) throws IOException, WS4DIllegalStateException {
		checkStopped();
		try {
			HTTPBinding httpBinding = (HTTPBinding) binding;
			if (httpBinding != null) {
				SOAPServer server = null;
				if (DPWSFramework.hasModule(DPWSFramework.SECURITY_MODULE)) {
					server = getSOAPServer(httpBinding.getHostAddress(), httpBinding.getPort(), (httpBinding.getType() == IPBinding.HTTPS_BINDING), DPWSFramework.getSecurityManager().getAliasFromBinding(httpBinding));
				} else {
					server = getSOAPServer(httpBinding.getHostAddress(), httpBinding.getPort(), (httpBinding.getType() == IPBinding.HTTPS_BINDING), null);
				}

				String path = httpBinding.getPath();
				server.unregister(path);
				server.getHTTPServer().unregister(path, InternetMediaType.getMultipartRelated());
				httpBinding.resetAutoPort();
			}
			for (int i = 0; i < messageTypes.length; i++) {
				if (DPWSMessageConstants.GET_METADATA_MESSAGE == messageTypes[i]) {
					removeUriFromRegister(httpBinding.getTransportAddress(), registerForGetMetadata);
				}
			}
		} catch (ClassCastException e) {
			throw new IOException("Unsupported binding type. Need HTTPBinding but was: " + binding);
		}
	}

	public void unregisterService(int[] messageTypes, CommunicationBinding binding, IncomingMessageListener listener) throws IOException, WS4DIllegalStateException {
		checkStopped();
		try {
			HTTPBinding httpBinding = (HTTPBinding) binding;
			if (httpBinding != null) {
				SOAPServer server = null;
				if (DPWSFramework.hasModule(DPWSFramework.SECURITY_MODULE)) {
					server = getSOAPServer(httpBinding.getHostAddress(), httpBinding.getPort(), (httpBinding.getType() == IPBinding.HTTPS_BINDING), DPWSFramework.getSecurityManager().getAliasFromBinding(httpBinding));
				} else
					server = getSOAPServer(httpBinding.getHostAddress(), httpBinding.getPort(), (httpBinding.getType() == IPBinding.HTTPS_BINDING), null);

				String path = httpBinding.getPath();
				server.unregister(path);
				server.getHTTPServer().unregister(path, InternetMediaType.getMultipartRelated());
				httpBinding.resetAutoPort();
			}
			for (int i = 0; i < messageTypes.length; i++) {
				if (DPWSMessageConstants.GET_METADATA_MESSAGE == messageTypes[i]) {
					removeUriFromRegister(httpBinding.getTransportAddress(), registerForGetMetadata);
				}
			}
		} catch (ClassCastException e) {
			throw new IOException("Unsupported binding type. Need HTTPBinding but was: " + binding);
		}
	}

	public void unregisterDiscovery(int[] messageTypes, DiscoveryBinding binding, IncomingMessageListener listener) throws IOException, WS4DIllegalStateException {
		checkStopped();
		if (binding != null) {
			try {
				DPWSDiscoveryBinding discoveryBinding = (DPWSDiscoveryBinding) binding;
				IPAddress hostAddress = discoveryBinding.getHostAddress();
				int port = discoveryBinding.getPort();
				String ifaceName = discoveryBinding.getIface();
				SOAPoverUDPServer server = getSOAPoverUDPServer(hostAddress, port, ifaceName);
				if (server != null) {
					IncomingUDPReceiver receiver = (IncomingUDPReceiver) server.getHandler();
					receiver.unregister(messageTypes, listener);
					if (receiver.isEmpty()) {
						String key = hostAddress.getAddress() + ":" + port + "%" + ifaceName;
						synchronized (udpServers) {
							try {
								server.stop();
								udpServers.remove(key);
							} catch (IOException e) {
								Log.warn("unable to remove SOAP-over-UDP server for multicast address " + key + ". " + e.getMessage());
							}
						}
					}
				}
			} catch (ClassCastException e) {
				throw new IOException("Unsupported binding type. Need DiscoveryBinding but was: " + binding);
			}
		}
	}

	public void unregisterDeviceReference(int[] messageTypes, DiscoveryBinding binding, IncomingMessageListener listener) throws IOException, WS4DIllegalStateException {
		checkStopped();
		if (binding != null) {
			try {
				DPWSDiscoveryBinding discoveryBinding = (DPWSDiscoveryBinding) binding;
				IPAddress hostAddress = discoveryBinding.getHostAddress();
				int port = discoveryBinding.getPort();
				String ifaceName = discoveryBinding.getIface();
				SOAPoverUDPServer server = getSOAPoverUDPServer(hostAddress, port, ifaceName);
				IncomingUDPReceiver receiver = (IncomingUDPReceiver) server.getHandler();
				receiver.unregister(messageTypes, listener);
				if (receiver.isEmpty()) {
					String key = hostAddress.getAddress() + ":" + port + "%" + ifaceName;
					synchronized (udpServers) {
						try {
							server.stop();
							udpServers.remove(key);
						} catch (IOException e) {
							Log.warn("unable to remove SOAP-over-UDP server for multicast address " + key + ". " + e.getMessage());
						}
					}
				}
			} catch (ClassCastException e) {
				throw new IOException("Unsupported binding type. Need DiscoveryBinding but was: " + binding);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.ws4d.java.communication.CommunicationManager#deploy(org.ws4d.java
	 * .communication.Resource,
	 * org.ws4d.java.communication.CommunicationBinding, java.lang.String)
	 */
	public URI registerResource(Resource resource, CommunicationBinding binding, String resourcePath, HTTPGroup user) throws IOException, WS4DIllegalStateException {
		checkStopped();
		if (binding != null && !(binding instanceof HTTPBinding)) {
			throw new IOException("Unsupported binding type: " + binding);
		}
		HTTPBinding httpBinding = (HTTPBinding) binding;
		IPAddress host = httpBinding.getHostAddress();
		int port = httpBinding.getPort();
		HTTPServer server = httpBinding.getType() == IPBinding.HTTPS_BINDING ? getHTTPServer(host, port, true, DPWSFramework.getSecurityManager().getAliasFromBinding(httpBinding)) : getHTTPServer(host, port);
		if (port == 0) {
			port = server.getPort();
			httpBinding.setPort(port);
		}
		String basicPath = httpBinding.getPath();
		if (resourcePath == null) {
			resourcePath = "";
		} else if (!(resourcePath.startsWith("/") || basicPath.endsWith("/"))) {
			resourcePath = "/" + resourcePath;
		}
		String addressPath = basicPath + resourcePath;
		server.register(addressPath, new DefaultHTTPResourceHandler(resource), user);
		return new URI((httpBinding.getType() == IPBinding.HTTPS_BINDING ? "https://" : "http://") + host.getAddressWithoutNicId() + ":" + port + addressPath);
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.communication.CommunicationManager#undeploy(org.ws4d
	 * .java.data.uri.URI)
	 */
	public void unregisterResource(URI deployAddress, CommunicationBinding binding) throws IOException, WS4DIllegalStateException {
		HTTPBinding httpBinding;
		try {
			httpBinding = (HTTPBinding) binding;
		} catch (ClassCastException e) {
			if (Log.isError()) Log.error("DPWSCommunicationManager.unregisterResource: unsupported CommunicationBinding class (" + binding.getClass() + ")");
			return;
		}
		checkStopped();
		int port = deployAddress.getPort();
		HTTPServer server = getHTTPServer(httpBinding.getHostAddress(), port);
		server.unregister(deployAddress.getPath());
		httpBinding.resetAutoPort();
	}


	/* (non-Javadoc)
	 * @see org.ws4d.java.communication.CommunicationManager#send(org.ws4d.java.message.Message, org.ws4d.java.types.XAddressInfo, org.ws4d.java.communication.ProtocolDomain, org.ws4d.java.communication.ResponseCallback)
	 */
	public void send(Message message, XAddressInfo targetXAdrInfo, ProtocolDomain domain, ResponseCallback callback) throws WS4DIllegalStateException {
		checkStopped();
		/*
		 * only request messages (including one-way Hello and Bye) can be sent
		 * by this method
		 */
		if (message == null) return;
		switch (message.getRoutingScheme()) {
			case Message.UNICAST_ROUTING_SCHEME: {
				if (targetXAdrInfo == null) {
					Log.warn("No target address found within request message " + message);
					throw new IllegalArgumentException("No target address set for message " + message);
				}
				sendTCPAndCheckDPWSVersion(message, callback, targetXAdrInfo);
				break;
			}
			case Message.MULTICAST_ROUTING_SCHEME: {
				sendUDPMulticastAndCheckDPWSVersion(message, domain, callback);
				break;
			}
			case Message.UNKNOWN_ROUTING_SCHEME:
			default: {
				Log.warn("Attempt to send a message of an unexpected type: " + message);
				throw new IllegalArgumentException("Unexpected message type: " + message);
			}
		}

		// switch (message.getType()) {
		// // SOAP/HTTP/TCP messages first
		// case (DPWSMessageConstants.GET_MESSAGE):
		// case (DPWSMessageConstants.GET_METADATA_MESSAGE):
		// case (DPWSMessageConstants.SUBSCRIBE_MESSAGE):
		// case (DPWSMessageConstants.GET_STATUS_MESSAGE):
		// case (DPWSMessageConstants.RENEW_MESSAGE):
		// case (DPWSMessageConstants.UNSUBSCRIBE_MESSAGE):
		// case (DPWSMessageConstants.SUBSCRIPTION_END_MESSAGE):
		// case (DPWSMessageConstants.INVOKE_MESSAGE): {
		// URI targetAddress = message.getTargetAddress();
		// if (targetAddress == null) {
		// Log.warn("No target address found within request message " +
		// message);
		// throw new
		// IllegalArgumentException("No target address set for message " +
		// message);
		// }
		// sendTCPAndCheckDPWSVersion(message, callback, targetAddress);
		// break;
		// }
		// // SOAP-over-UDP messages
		// case (DPWSMessageConstants.HELLO_MESSAGE):
		// case (DPWSMessageConstants.BYE_MESSAGE):
		// case (DPWSMessageConstants.RESOLVE_MESSAGE): {
		// sendUDPMulticastAndCheckDPWSVersion(message, domain, callback);
		// break;
		// }
		// /*
		// * special case Probe: it can be either SOAP-over-UDP or
		// * SOAP/HTTP/TCP for directed Probes, i.e. when targetAddress !=
		// * null
		// */
		// case (DPWSMessageConstants.PROBE_MESSAGE): {
		// URI targetAddress = message.getTargetAddress();
		// if (targetAddress == null) {
		// // SOAP-over-UDP aka. multicast Probe
		// sendUDPMulticastAndCheckDPWSVersion(message, domain, callback);
		// } else {
		// sendTCPAndCheckDPWSVersion(message, callback, targetAddress);
		// }
		// break;
		// }
		// default: {
		// Log.warn("Attempt to send a message of an unexpected type: " +
		// message);
		// throw new IllegalArgumentException("Unexpected message type: " +
		// message);
		// }
		// }
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.ws4d.java.communication.CommunicationManager#getResourceAsStream(
	 * org.ws4d.java.types.URI)
	 */
	public ResourceLoader getResourceAsStream(URI location) throws IOException {
		if (location.getSchema().startsWith(HTTPConstants.HTTP_SCHEMA)) {
			try {
				return HTTPRequestUtil.getResourceAsStream(location.toString());
			} catch (ProtocolException e) {
				throw new IOException("HTTP protocol exception.");
			}
		}

		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.ws4d.java.communication.CommunicationManager#getAutobindings(java
	 * .lang.String, org.ws4d.java.structures.DataStructure)
	 */
	public void getAutobindings(String descriptor, DataStructure bindings) throws IOException {
		checkStopped();
		List ipv4Loopback = new ArrayList(4);
		List ipv6Loopback = new ArrayList(4);
		// 'real' external IPs
		List ipv4 = new ArrayList(4);
		List ipv6 = new ArrayList(4);
		for (Iterator it = getAvailableDomains().iterator(); it.hasNext();) {
			DPWSDomain domain = (DPWSDomain) it.next();
			IPAddress addr = domain.getIPAddress();
			boolean loopbackIface = domain.getIface().isLoopback();
			if (addr.isIPv6()) {
				if (loopbackIface || addr.isLoopback()) {
					ipv6Loopback.add(domain);
				} else {
					ipv6.add(domain);
				}

			} else {
				// must be v4
				if (loopbackIface || addr.isLoopback()) {
					ipv4Loopback.add(domain);
				} else {
					ipv4.add(domain);
				}
			}
		}
		// observe IPv4 case first
		List srcList = ipv4.isEmpty() ? ipv4Loopback : ipv4;

		HashMap ipv4filter = new HashMap();
		for (Iterator it = srcList.iterator(); it.hasNext();) {
			DPWSDomain domain = (DPWSDomain) it.next();
			String ifaceName = domain.getInterfaceName();
			if (ipv4filter.containsKey(ifaceName)) {
				continue;
			}
			ipv4filter.put(ifaceName, domain);
		}
		// TODO

		// observe IPv6 case
		srcList = ipv6.isEmpty() ? ipv6Loopback : ipv6;

		HashMap ipv6filter = new HashMap();
		for (Iterator it = srcList.iterator(); it.hasNext();) {
			DPWSDomain domain = (DPWSDomain) it.next();
			String ifaceName = domain.getInterfaceName();
			DPWSDomain otherDomain = (DPWSDomain) ipv6filter.get(ifaceName);
			if (otherDomain != null) {
				if (otherDomain.getIface().isLoopback()) {
					if (otherDomain.getIPAddress().isLoopback()) {
						continue;
					}
				} else if (otherDomain.getIPAddress().isIPv6LinkLocal()) {
					continue;
				}
			}
			ipv6filter.put(ifaceName, domain);
		}
		bindAddresses(descriptor, bindings, ipv4filter.values());
		bindAddresses(descriptor, bindings, ipv6filter.values());
	}

	private void bindAddresses(String descriptor, DataStructure bindings, DataStructure domains) {
		for (Iterator it = domains.iterator(); it.hasNext();) {
			DPWSDomain domain = (DPWSDomain) it.next();

			HTTPBinding binding = new HTTPBinding(domain.getIPAddress(), 0, descriptor);
			if (Log.isDebug()) {
				Log.debug("Adding HTTP auto-binding on " + domain.getInterfaceName() + ": " + binding, Log.DEBUG_LAYER_COMMUNICATION);
			}
			bindings.add(binding);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.ws4d.java.communication.CommunicationManager#getProtocolDomains(org
	 * .ws4d.java.structures.Iterator, org.ws4d.java.structures.DataStructure)
	 */
	public void getProtocolDomains(Iterator bindings, DataStructure domains) throws IOException {
		DataStructure ourDomains = getAvailableDomains();
		while (bindings.hasNext()) {
			Object o = bindings.next();
			if (!(o instanceof HTTPBinding)) {
				continue;
			}

			IPAddress bindingIPAddr = ((HTTPBinding) o).getHostAddress();
			for (Iterator it = ourDomains.iterator(); it.hasNext();) {
				DPWSDomain d = (DPWSDomain) it.next();
				if (d.getIPAddress().equals(bindingIPAddr)) {
					domains.add(d);
					break;
				}
			}
		}
	}

	private void checkStopped() throws WS4DIllegalStateException {
		if (stopped) {
			throw new WS4DIllegalStateException("this communication manager is stopped");
		}
	}

	/**
	 * Method checks the supported DPWSVersions from the DPWSProperties. If in
	 * the Properties no DPWS Version is defined the user is a nerd. If there is
	 * one DPWSVersion defined, it will be set to the message, else if more than
	 * one DPWSVersion is defined nothing will be done.
	 * 
	 * @param message , the message which checks for Version.
	 */
	private void checkSupportedDPWSVersions(Message message) {
		HashSet supportedDPWSVersions = DPWSProperties.getInstance().getSupportedDPWSVersions();

		if (message.getProtocolInfo() == null && supportedDPWSVersions.size() == 1) {
			Iterator it = supportedDPWSVersions.iterator();
			Integer i = (Integer) it.next();
			message.setProtocolInfo(new DPWSProtocolInfo(i.intValue()));
			return;
		} else if (message.getProtocolInfo() != null) {
			if (!supportedDPWSVersions.contains(new Integer(message.getProtocolInfo().getVersion()))) {
				Iterator it = supportedDPWSVersions.iterator();
				Integer i = (Integer) it.next();
				message.setProtocolInfo(new DPWSProtocolInfo(i.intValue()));
				if (Log.isDebug()) {
					Log.debug("The choosen DPWS Versio is not supported, changed to " + message.getProtocolInfo().getDisplayName() + ".", Log.DEBUG_LAYER_COMMUNICATION);
				}
			}
			return;
		}
	}

	protected SOAPoverUDPServer getSOAPoverUDPServer(IPAddress localHostAddress, int port, String ifaceName) {
		String key = localHostAddress.getAddress() + ":" + port + "%" + ifaceName;
		SOAPoverUDPServer server = null;
		synchronized (udpServers) {
			server = (SOAPoverUDPServer) udpServers.get(key);
			if (server == null || !server.isRunning()) {
				try {
					server = new SOAPoverUDPServer(localHostAddress, port, ifaceName, new IncomingUDPReceiver());
					udpServers.put(key, server);
				} catch (IOException e) {
					Log.warn("Unable to create SOAP-over-UDP server for multicast address " + key + ". " + e.getMessage());
				}
			}
		}
		return server;
	}

	private SOAPServer getSOAPServer(IPAddress localHostAddress, int port, boolean secure, String alias) throws IOException {
		String localAddress;
		SOAPServer server;
		if (port == 0) {
			server = SOAPServer.get(localHostAddress, port, secure, alias);
			localAddress = localHostAddress.getAddress() + ":" + server.getHTTPServer().getPort();
			synchronized (soapServers) {
				soapServers.put(localAddress, server);
			}
		} else {
			localAddress = localHostAddress.getAddress() + ":" + port;
			synchronized (soapServers) {
				server = (SOAPServer) soapServers.get(localAddress);
				if (server == null || !server.getHTTPServer().isRunning()) {
					server = SOAPServer.get(localHostAddress, port, secure, alias);
					soapServers.put(localAddress, server);
				} else {
					return server;
				}
			}
		}
		// cache the underlying HTTP server, too
		HTTPServer httpServer = server.getHTTPServer();
		synchronized (httpServers) {
			httpServers.put(localAddress, httpServer);
		}
		return server;
	}

	private HTTPServer getHTTPServer(IPAddress localHostAddress, int port) throws IOException {
		return getHTTPServer(localHostAddress, port, false, null);
	}

	private HTTPServer getHTTPServer(IPAddress localHostAddress, int port, boolean secure, String alias) throws IOException {
		HTTPServer server;
		if (port == 0) {
			server = HTTPServer.get(localHostAddress, port, secure, alias);
			synchronized (httpServers) {
				httpServers.put(localHostAddress.getAddress() + ":" + server.getPort(), server);
			}
			return server;
		}
		String localAddress = localHostAddress.getAddress() + ":" + port;
		synchronized (httpServers) {
			server = (HTTPServer) httpServers.get(localAddress);
			if (server == null || !server.isRunning()) {
				server = HTTPServer.get(localHostAddress, port, secure, alias);
				httpServers.put(localAddress, server);
			}
		}
		return server;
	}

	private String getCanonicalAddress(String address) {
		/*
		 * be aware of alias addresses, like localhost vs. 127.0.0.1 or DNS
		 * aliases, etc. map them all to a single canonical form, e.g. IPv4
		 * address
		 */
		return IPNetworkDetection.getInstance().getCanonicalAddress(address);
	}

	private void addUriToRegister(URI uri, Set register) {
		/*
		 * in case the URI's host is given as a DNS name, it is important to add
		 * another URI with the equivalent IP address to the register in order
		 * to find it when searching for it with the value returned from
		 * DPWSProtocolData.getTransportAddress() (which will rather contain an
		 * IP address)
		 */
		register.add(uri);
		URI canonicalUri = createCanonicalUri(uri);
		if (canonicalUri != null) {
			register.add(canonicalUri);
		}
	}

	private void removeUriFromRegister(URI uri, Set register) {
		register.remove(uri);
		URI canonicalUri = createCanonicalUri(uri);
		if (canonicalUri != null) {
			register.remove(canonicalUri);
		}
	}

	private URI createCanonicalUri(URI srcUri) {
		String host = srcUri.getHost();
		if (host == null || "".equals(host)) {
			return null;
		}
		String canonicalHost = getCanonicalAddress(host);
		if (host.equals(canonicalHost)) {
			return null;
		}
		String s = srcUri.toString();
		int hostIndex = s.indexOf(host);
		// replace original host with canonical one
		s = s.substring(0, hostIndex) + canonicalHost + s.substring(hostIndex + host.length());
		URI canonicalUri = new URI(s);
		return canonicalUri;
	}

	protected void sendTCPAndCheckDPWSVersion(final Message message, final ResponseCallback callback, final XAddressInfo targetXAdrInfo) {
		Runnable r = new Runnable() {

			public void run() {
				// Checks and set the DPWS Version of the Message
				checkSupportedDPWSVersions(message);
				if (targetXAdrInfo.getProtocolInfo() == null || targetXAdrInfo.getProtocolInfo().getVersion() == DPWSProtocolInfo.DPWS_VERSION_NOT_SET) {
					/*
					 * in that case we must potentially send multiple copies of
					 * the message - one per supported version; thereby, we must
					 * be able to intercept any faults in response resulting
					 * from version mismatch
					 */
					ResponseCallback faultAwareCallback = new FaultAwareResponseCallback(targetXAdrInfo, callback, 2);
					HashSet supportedVersions = DPWSProperties.getInstance().getSupportedDPWSVersions();
					if (supportedVersions.contains(new Integer(DPWSConstants.DPWS_VERSION2009))) {
						Message dpws1_1 = DPWS_UTIL.copyOutgoingMessage(message);
						dpws1_1.setProtocolInfo(new DPWSProtocolInfo(DPWSConstants.DPWS_VERSION2009));
						sendTCP(dpws1_1, faultAwareCallback, targetXAdrInfo.getXAddress());
					}
					if (supportedVersions.contains(new Integer(DPWSConstants2006.DPWS_VERSION2006))) {
						Message dpws2006 = DPWS_UTIL.copyOutgoingMessage(message);
						if (message instanceof GetMetadataMessage) {
							// changing the GetMetadataMessage to GetMessage
							Message msgGetFromGetMetadata = DPWS_UTIL.changeOutgoingMessage(DPWSConstants2006.DPWS_VERSION2006, (GetMetadataMessage) dpws2006);
							msgGetFromGetMetadata.setProtocolInfo(new DPWSProtocolInfo(DPWSConstants2006.DPWS_VERSION2006));
							MessageIDsForGetMetadataMapping.add(msgGetFromGetMetadata.getMessageId());
							sendTCP(msgGetFromGetMetadata, callback, targetXAdrInfo.getXAddress());
						} else {
							// here just attributs must be changed
							Message changedMessage2006 = DPWS_UTIL.changeOutgoingMessage(DPWSConstants2006.DPWS_VERSION2006, dpws2006);
							changedMessage2006.setProtocolInfo(new DPWSProtocolInfo(DPWSConstants2006.DPWS_VERSION2006));
							sendTCP(changedMessage2006, callback, targetXAdrInfo.getXAddress());
						}
					}
				} else {
					if (Log.isDebug()) {
						Log.debug("Send " + message.getProtocolInfo().getDisplayName() + " Message", Log.DEBUG_LAYER_COMMUNICATION);
					}
					if (message.getProtocolInfo().getVersion() == DPWSConstants2006.DPWS_VERSION2006) {
						Message dpws2006 = DPWS_UTIL.copyOutgoingMessage(message);
						if (message instanceof GetMetadataMessage) {
							// changing the GetMetadataMessage to GetMessage
							Message msgGetFromGetMetadata = DPWS_UTIL.changeOutgoingMessage(message.getProtocolInfo().getVersion(), (GetMetadataMessage) dpws2006);
							msgGetFromGetMetadata.setProtocolInfo(new DPWSProtocolInfo(DPWSConstants2006.DPWS_VERSION2006));
							MessageIDsForGetMetadataMapping.add(msgGetFromGetMetadata.getMessageId());
							sendTCP(msgGetFromGetMetadata, callback, targetXAdrInfo.getXAddress());
						} else {
							// here just attributs must be changed
							Message changedMessage2006 = DPWS_UTIL.changeOutgoingMessage(message.getProtocolInfo().getVersion(), dpws2006);
							changedMessage2006.setProtocolInfo(new DPWSProtocolInfo(DPWSConstants2006.DPWS_VERSION2006));
							sendTCP(changedMessage2006, callback, targetXAdrInfo.getXAddress());
						}
					} else {
						sendTCP(message, callback, targetXAdrInfo.getXAddress());
					}
				}
			}
		};

		DPWSFramework.getThreadPool().execute(r);
	}

	protected void sendUDPMulticastAndCheckDPWSVersion(final Message message, final ProtocolDomain domain, final ResponseCallback callback) {
		// Checks and set the DPWS Version of the Message
		checkSupportedDPWSVersions(message);

		synchronized (udpTransmissionsLock) {
			pendingUDPTransmissions++;
			if (message.getProtocolInfo() == null) {
				pendingUDPTransmissions++;
			}
		}
		if (message.getProtocolInfo() == null || message.getProtocolInfo().getVersion() == DPWSProtocolInfo.DPWS_VERSION_NOT_SET) {
			HashSet supportedVersions = DPWSProperties.getInstance().getSupportedDPWSVersions();
			/*
			 * Check DPWS2009 aka DPWS1.1
			 */
			if (supportedVersions.contains(new Integer(DPWSConstants.DPWS_VERSION2009))) {
				Message dpws1_1 = DPWS_UTIL.copyOutgoingMessage(message);
				DPWS_UTIL.changeOutgoingMessage(DPWSConstants.DPWS_VERSION2009, dpws1_1);
				dpws1_1.setProtocolInfo(new DPWSProtocolInfo(DPWSConstants.DPWS_VERSION2009));
				sendUDPMulticast(dpws1_1, domain, callback);
			}
			/*
			 * Check DPWS2006
			 */
			if (supportedVersions.contains(new Integer(DPWSConstants2006.DPWS_VERSION2006))) {
				Message dpws2006 = DPWS_UTIL.copyOutgoingMessage(message);
				DPWS_UTIL.changeOutgoingMessage(DPWSConstants2006.DPWS_VERSION2006, dpws2006);
				dpws2006.setProtocolInfo(new DPWSProtocolInfo(DPWSConstants2006.DPWS_VERSION2006));
				sendUDPMulticast(dpws2006, domain, callback);
			}
		} else {
			if (Log.isDebug()) {
				Log.debug("Send " + message.getProtocolInfo().getDisplayName() + " Message", Log.DEBUG_LAYER_COMMUNICATION);
			}
			if (message.getProtocolInfo().getVersion() == DPWSConstants2006.DPWS_VERSION2006) {
				DPWS_UTIL.changeOutgoingMessage(message.getProtocolInfo().getVersion(), message);
			}
			sendUDPMulticast(message, domain, callback);
		}
	}

	/**
	 * @param message
	 * @param callback
	 * @param targetAddress
	 */
	protected void sendTCP(Message message, ResponseCallback callback, URI targetAddress) {
		MessageReceiver receiver = callback == null ? GENERIC_RECEIVER : new SOAPResponseReceiver(message, callback);
		HTTPRequest request = new SOAPRequest(targetAddress.getPath(), message, receiver);

		if (DPWSFramework.hasModule(DPWSFramework.SECURITY_MODULE) && DPWSFramework.getSecurityManager().isHTTPS(targetAddress)) {
			HTTPClient.exchange(new HTTPClientDestination(targetAddress, true, null), request);
		} else {
			HTTPClient.exchange(new HTTPClientDestination(targetAddress), request);
		}
	}

	/**
	 * @param message
	 * @param domain
	 * @param callback
	 */
	protected void sendUDPMulticast(final Message message, final ProtocolDomain domain, final ResponseCallback callback) {
		if (!(domain instanceof DPWSDomain)) {
			return;
		}
		// remember outgoing message id in order to ignore it when we receive it
		SENT_MULTICAST_MESSAGE_IDS.containsOrEnqueue(message.getMessageId());

		final Waiter waiter = new Waiter();

		final DPWSCommunicationManager myself = this;

		// send without letting the caller wait
		Runnable r = new Runnable() {

			/*
			 * (non-Javadoc)
			 * @see java.lang.Runnable#run()
			 */
			public void run() {
				synchronized (waiter) {
					waiter.pending = false;
					waiter.notify();
				}

				DPWSDomain dpwsDomain = (DPWSDomain) domain;
				IPAddress localHostAddress = dpwsDomain.getIPAddress();
				// chose target address based on specified domain
				int targetMulticastPort = DPWSConstants.DPWS_MCAST_PORT;
				IPAddress targetMulticastAddress = localHostAddress.isIPv6() ? DPWSDiscoveryBinding.DPWS_MCAST_GROUP_IPv6 : DPWSDiscoveryBinding.DPWS_MCAST_GROUP_IPv4;

				URI targetAdressURI = message.getTargetAddress();
				if (targetAdressURI != null) {
					targetMulticastAddress = new IPAddress(getCanonicalAddress(targetAdressURI.getHost()));
					if (targetAdressURI.getPort() > 0) targetMulticastPort = targetAdressURI.getPort();
				}

				int messageType = message.getType();
				if (messageType == DPWSMessageConstants.HELLO_MESSAGE || messageType == DPWSMessageConstants.PROBE_MATCHES_MESSAGE) {
					try {
						Thread.sleep(myself.getRandomApplicationDelay(message.getProtocolInfo().getVersion()));
					} catch (InterruptedException e) {
						// void
					}
				}

				SOAPoverUDPClient client = null;

				DPWSProtocolData protocolData = new DPWSProtocolData(dpwsDomain.getInterfaceName(), ProtocolData.DIRECTION_OUT, localHostAddress.getAddressWithoutNicId(), 0, targetMulticastAddress.getAddressWithoutNicId(), DPWSConstants.DPWS_MCAST_PORT, false);
				try {
					// make soap-over-udp clients reusable
					synchronized (udpClientsPerDomain) {
						client = (SOAPoverUDPClient) udpClientsPerDomain.get(dpwsDomain);
						if (client == null || client.isClosed()) {
							client = SOAPoverUDPClient.get(localHostAddress, 0, dpwsDomain.getInterfaceName());
							udpClientsPerDomain.put(dpwsDomain, client);
						}
					}
					if (callback != null && (messageType == DPWSMessageConstants.PROBE_MESSAGE || messageType == DPWSMessageConstants.RESOLVE_MESSAGE)) {
						rrc.registerResponseCallback(message, callback, DispatchingProperties.getInstance().getMatchWaitTime());
					}
					client.send(targetMulticastAddress, targetMulticastPort, message, udpResponseHandler, protocolData);
					MESSAGE_INFORMER.forwardMessage(message, protocolData);
					// success!
				} catch (IOException e) {
					Log.warn("Could not multicast DPWS message to " + targetMulticastAddress + ":" + DPWSConstants.DPWS_MCAST_PORT + " over " + localHostAddress + ":" + protocolData.getSourcePort() + " due to an exception. Message: " + message + ", Exception: " + e.getMessage() + ", Callback: " + (callback == null ? "no callback" : callback.toString()));
					// cleanup unusable client
					try {
						client.close();
					} catch (IOException ex) {
						Log.warn("Unable to close unusable UDP client");
					}
					synchronized (udpClientsPerDomain) {
						udpClientsPerDomain.remove(dpwsDomain);
					}
					// Log.printStackTrace(e);
					if (callback != null) {
						callback.handleTransmissionException(message, e, protocolData);
					}
				}

				// free-up on-stop-lock...
				synchronized (udpTransmissionsLock) {
					pendingUDPTransmissions--;
					udpTransmissionsLock.notifyAll();
				}
			}

		};

		DPWSFramework.getThreadPool().execute(r);
		/*
		 * make sure we return after actually having started the send
		 * procedure...
		 */
		synchronized (waiter) {
			while (waiter.pending) {
				try {
					waiter.wait();
				} catch (InterruptedException e) {
					// ignore
				}
			}
		}
	}

	private static class Waiter {

		volatile boolean	pending	= true;
	}

	public void serializeMessageWithAttachments(Message message, String attachmentSep, List attachments, OutputStream out, ProtocolData pd) throws IOException {
		/*
		 * For DPWS the "attachmentSep" is the MIME boundary.
		 */
		if (attachmentSep == null) {
			SOAPMessageGeneratorFactory.getInstance().getMessage2SOAPGeneratorForCurrentThread().generateSOAPMessage(out, message, pd);
		} else {
			MIMEUtil.writeBoundary(out, attachmentSep.getBytes(), false, false);
			MIMEBodyHeader mimeHeader = new MIMEBodyHeader();
			mimeHeader.setHeaderField(MIMEConstants.MIME_HEADER_CONTENT_ID, MIMEConstants.PARAMETER_STARTVALUE);
			mimeHeader.setHeaderField(MIMEConstants.MIME_HEADER_CONTENT_TYPE, InternetMediaType.getApplicationXOPXML().toString());
			mimeHeader.setHeaderField(MIMEConstants.MIME_HEADER_CONTENT_TRANSFER_ENCODING, HTTPConstants.HTTP_HEADERVALUE_TRANSFERENCODING_BINARY);

			mimeHeader.toStream(out);
			SOAPMessageGeneratorFactory.getInstance().getMessage2SOAPGeneratorForCurrentThread().generateSOAPMessage(out, message, pd);
			out.flush();

			while (!attachments.isEmpty() && DPWSFramework.hasModule(DPWSFramework.ATTACHMENT_MODULE)) {
				// attachments now implement MIMEEntity
				Attachment response = (Attachment) attachments.remove(0);

				mimeHeader = new MIMEBodyHeader();
				mimeHeader.setHeaderField(MIMEConstants.MIME_HEADER_CONTENT_ID, response.getContentId());
				try {
					// if contentType is null set to "" to avoid
					// NullPointerExceptions
					String contentTypeString = "";
					InternetMediaType contentType = response.getContentType();
					if (contentType != null) {
						contentTypeString = contentType.toString();
					} else {
						contentTypeString = InternetMediaType.getApplicationOctetStream().toString();
					}

					String transferEncoding = response.getTransferEncoding();
					if (transferEncoding == null) {
						transferEncoding = HTTPConstants.HTTP_HEADERVALUE_TRANSFERENCODING_BINARY;
					}

					mimeHeader.setHeaderField(MIMEConstants.MIME_HEADER_CONTENT_TYPE, contentTypeString);
					mimeHeader.setHeaderField(MIMEConstants.MIME_HEADER_CONTENT_TRANSFER_ENCODING, transferEncoding);
				} catch (AttachmentException e) {
					/*
					 * shouldn't ever happen, as getContentType() or
					 * getTransferEncoding() shouldn't fail locally
					 */
					Log.printStackTrace(e);
				}
				MIMEUtil.writeBoundary(out, attachmentSep.getBytes(), false, false);
				mimeHeader.toStream(out);
				// flush the header. this allows the receiver to read the part
				// send before.
				out.flush();
				try {
					response.serialize(out);
				} catch (AttachmentException e) {
					throw new IOException(e.getMessage());
				}
				// this is needed for streaming support
				out.flush();
			}
			MIMEUtil.writeBoundary(out, attachmentSep.getBytes(), false, true);
		}
		out.flush();
	}

	public ProtocolInfo getProtocolInfo() {
		return new DPWSProtocolInfo();
	}

	public CommunicationUtil getCommunicationUtil() {
		return DPWS_UTIL;
	}

	public ProtocolInfo createProtocolInfo(int version) {
		return new DPWSProtocolInfo(version);
	}

	public HashSet getSupportedVersions() {
		return DPWSProperties.getInstance().getSupportedDPWSVersions();
	}

	public boolean supportsAddressingNamespace(SOAPHeader header, String namespace, ProtocolData pd) throws VersionMismatchException {
		if (WSAConstants.WSA_NAMESPACE_NAME.equals(namespace)) {
			if (getSupportedVersions().contains(new Integer(DPWSConstants.DPWS_VERSION2009))) {
				pd.setProtocolInfo(createProtocolInfo(DPWSConstants.DPWS_VERSION2009));
				header.setProtocolInfo(pd.getProtocolInfo());
				return true;
			} else {
				throw new VersionMismatchException("WS-Addressing: " + namespace + " is not supported in this Configuration", VersionMismatchException.TYPE_WRONG_ADDRESSING_VERSION);
			}
		} else if (WSAConstants2006.WSA_NAMESPACE_NAME.equals(namespace)) {
			if (getSupportedVersions().contains(new Integer(DPWSConstants2006.DPWS_VERSION2006))) {
				pd.setProtocolInfo(createProtocolInfo(DPWSConstants2006.DPWS_VERSION2006));
				header.setProtocolInfo(pd.getProtocolInfo());
				return true;
			} else {
				throw new VersionMismatchException("WS-Addressing: " + namespace + " is not supported in this Configuration", VersionMismatchException.TYPE_WRONG_ADDRESSING_VERSION);
			}
		}
		return false;
	}

	public QNameSet getDeviceTypes() {
		QNameSet dTypes = new QNameSet();
		Iterator it = getSupportedVersions().iterator();
		while (it.hasNext()) {
			Integer version = (Integer) it.next();
			int v = version.intValue();
			switch (v) {
				case DPWSConstants.DPWS_VERSION2009:
					dTypes.add(DPWSConstants.DPWS_QN_DEVICETYPE);
					break;
				case DPWSConstants2006.DPWS_VERSION2006:
					dTypes.add(DPWSConstants2006.DPWS_QN_DEVICETYPE);
					break;
			}
		}
		return dTypes;
	}
}