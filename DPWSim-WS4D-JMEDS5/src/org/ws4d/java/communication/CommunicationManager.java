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

import org.ws4d.java.communication.monitor.ResourceLoader;
import org.ws4d.java.communication.protocol.http.HTTPGroup;
import org.ws4d.java.constants.DPWSMessageConstants;
import org.ws4d.java.message.Message;
import org.ws4d.java.message.SOAPHeader;
import org.ws4d.java.message.discovery.ByeMessage;
import org.ws4d.java.message.discovery.HelloMessage;
import org.ws4d.java.message.discovery.ProbeMessage;
import org.ws4d.java.message.discovery.ResolveMessage;
import org.ws4d.java.message.eventing.SubscriptionEndMessage;
import org.ws4d.java.structures.DataStructure;
import org.ws4d.java.structures.HashSet;
import org.ws4d.java.structures.Iterator;
import org.ws4d.java.structures.List;
import org.ws4d.java.types.QNameSet;
import org.ws4d.java.types.URI;
import org.ws4d.java.types.XAddressInfo;
import org.ws4d.java.util.WS4DIllegalStateException;

/**
 * The <em>* NEW *</em> communication manager interface.
 */
public interface CommunicationManager {
	
	public static final String ID_NULL = null;

	/**
	 * This method will be invoked be the framework during the start up phase.
	 */
	public void init();

	/**
	 * Returns the <em>unique</em> identifier of this communication manager's
	 * technology (e.g. <strong>DPWS</strong>, <strong>ZigBee</strong>,
	 * <strong>BT</strong>, etc.).
	 * 
	 * @return the unique ID of this communication manager
	 */
	public String getCommunicationManagerId();

	/**
	 * Starts this communication manager instance. This method executes any
	 * needed initialization steps so that further calls to other methods such
	 * as
	 * {@link #register(int[], CommunicationBinding, IncomingMessageListener)},
	 * {@link #send(Message, ProtocolDomain, ResponseCallback)}, etc. can
	 * operate correctly.
	 * <p>
	 * If this communication manager has already been started, this method must
	 * not do anything else other than quickly return .
	 * </p>
	 * 
	 * @throws IOException in case initializing communication failed for some
	 *             reason; the caller should assume that this communication
	 *             manager instance is not usable
	 */
	public void start() throws IOException;

	/**
	 * Stops this communication manager as soon as possible, closes all
	 * connections and frees any used resources. Any further interactions with
	 * this instance like
	 * {@link #register(int[], CommunicationBinding, IncomingMessageListener)
	 * registering listeners} or
	 * {@link #send(Message, ProtocolDomain, ResponseCallback) sending messages}
	 * will result in {@link WS4DIllegalStateException illegal state exceptions}
	 * .
	 * <p>
	 * If it is necessary to stop the communication manager immediately the
	 * {@link #kill()} method should be used.
	 * </p>
	 * <p>
	 * <strong>WARNING!</strong> This method causes the communication manager to
	 * loose all of its current state! That is, reactivating the communication
	 * manager again after this method has been called will result in having no
	 * {@link #register(int[], CommunicationBinding, IncomingMessageListener)
	 * registrations} for incoming messages.
	 * </p>
	 * <p>
	 * If this communication manager has already been stopped, this method must
	 * not do anything else other than quickly return
	 * </p>
	 */
	public void stop();

	/**
	 * Stops this communication manager <strong>immediately</strong>, closes all
	 * connections and frees any used resources without waiting for. Any further
	 * interactions with this instance like
	 * {@link #register(int[], CommunicationBinding, IncomingMessageListener)
	 * registering listeners} or
	 * {@link #send(Message, ProtocolDomain, ResponseCallback) sending messages}
	 * will result in {@link WS4DIllegalStateException illegal state exceptions}
	 * .
	 * <p>
	 * <strong>WARNING!</strong> This method causes the communication manager to
	 * loose all of its current state! That is, reactivating the communication
	 * manager again after this method has been called will result in having no
	 * {@link #register(int[], CommunicationBinding, IncomingMessageListener)
	 * registrations} for incoming messages.
	 * </p>
	 * <p>
	 * If this communication manager has already been stopped, this method must
	 * not do anything else other than quickly return
	 * </p>
	 */
	public void kill();

	/**
	 * Returns a data structure containing all {@link ProtocolDomain domains}
	 * within the corresponding technology that this communication manager
	 * instance provides. Domains are technology-specific endpoint designators,
	 * e.g. for IP over Ethernet a domain would consist of a network interface
	 * name (such as <quote>eth0</quote>) and an IP address.
	 * 
	 * @return a data structure with all available domains that this
	 *         communication manager provides within that technology
	 */
	public abstract DataStructure getAvailableDomains();

	// /**
	// * Registers the <code>messageListener</code> for receiving incoming
	// * messages of the specified <code>messageTypes</code> at the given
	// * <code>binding</code> address. See {@link DPWSMessageConstants} for a
	// list
	// * of supported DPWS message types.
	// *
	// * @param messageTypes determines which message types to register to
	// * @param binding the binding to register to
	// * @param listener the callback to deliver incoming desired messages to
	// * @throws IOException in case registration failed for some reason, e.g.
	// an
	// * address being already in use, etc.
	// * @throws WS4DIllegalStateException if this communication manager has
	// * already been {@link #stop() stopped}
	// */
	// public void register(int[] messageTypes, CommunicationBinding binding,
	// IncomingMessageListener listener) throws IOException,
	// WS4DIllegalStateException;

	/**
	 * Registers the <code>messageListener</code> for receiving incoming
	 * messages of the specified <code>messageTypes</code> at the given
	 * <code>binding</code> address. See {@link DPWSMessageConstants} for a list
	 * of supported DPWS message types.
	 * 
	 * @param messageTypes determines which message types to register to
	 * @param binding the binding to register to
	 * @param listener the callback to deliver incoming desired messages to
	 * @throws IOException in case registration failed for some reason, e.g. an
	 *             address being already in use, etc.
	 * @throws WS4DIllegalStateException if this communication manager has
	 *             already been {@link #stop() stopped}
	 */
	public void registerDevice(int[] messageTypes, CommunicationBinding binding, IncomingMessageListener listener, HTTPGroup user) throws IOException, WS4DIllegalStateException;

	/**
	 * Registers the <code>messageListener</code> for receiving incoming
	 * messages of the specified <code>messageTypes</code> at the given
	 * <code>binding</code> address. See {@link DPWSMessageConstants} for a list
	 * of supported DPWS message types.
	 * 
	 * @param messageTypes determines which message types to register to
	 * @param binding the binding to register to
	 * @param listener the callback to deliver incoming desired messages to
	 * @throws IOException in case registration failed for some reason, e.g. an
	 *             address being already in use, etc.
	 * @throws WS4DIllegalStateException if this communication manager has
	 *             already been {@link #stop() stopped}
	 */
	public void registerService(int[] messageTypes, CommunicationBinding binding, IncomingMessageListener listener, HTTPGroup user) throws IOException, WS4DIllegalStateException;

	/**
	 * Registers the <code>messageListener</code> for receiving incoming
	 * messages of the specified <code>messageTypes</code> at the given
	 * <code>binding</code> address. See {@link DPWSMessageConstants} for a list
	 * of supported DPWS message types.
	 * 
	 * @param messageTypes determines which message types to register to
	 * @param binding the binding to register to
	 * @param listener the callback to deliver incoming desired messages to
	 * @throws IOException in case registration failed for some reason, e.g. an
	 *             address being already in use, etc.
	 * @throws WS4DIllegalStateException if this communication manager has
	 *             already been {@link #stop() stopped}
	 */
	public void registerDiscovery(int[] messageTypes, DiscoveryBinding binding, IncomingMessageListener listener) throws IOException, WS4DIllegalStateException;

	/**
	 * Registers the <code>messageListener</code> for receiving incoming
	 * messages of the specified <code>messageTypes</code> at the given
	 * <code>binding</code> address. See {@link DPWSMessageConstants} for a list
	 * of supported DPWS message types.
	 * 
	 * @param messageTypes determines which message types to register to
	 * @param binding the binding to register to
	 * @param listener the callback to deliver incoming desired messages to
	 * @throws IOException in case registration failed for some reason, e.g. an
	 *             address being already in use, etc.
	 * @throws WS4DIllegalStateException if this communication manager has
	 *             already been {@link #stop() stopped}
	 */
	public void registerDeviceReference(int[] messageTypes, DiscoveryBinding binding, IncomingMessageListener listener) throws IOException, WS4DIllegalStateException;

	// /**
	// * Destroys a previously made
	// * {@link #register(int[], CommunicationBinding, IncomingMessageListener)
	// * registration} for the given <code>messageTypes</code>,
	// * <code>binding</code> and <code>listener</code>.
	// *
	// * @param messageTypes determines which message types to remove
	// registration
	// * from
	// * @param binding the binding to remove
	// * @param listener the callback which was previously registered
	// * @throws IOException in case removing the registration failed for some
	// * reason, e.g. this binding was not already registered, etc.
	// * @throws WS4DIllegalStateException if this communication manager has
	// * already been {@link #stop() stopped}
	// */
	// public void unregister(int[] messageTypes, CommunicationBinding binding,
	// IncomingMessageListener listener) throws IOException,
	// WS4DIllegalStateException;

	/**
	 * Destroys a previously made
	 * {@link #register(int[], CommunicationBinding, IncomingMessageListener)
	 * registration} for the given <code>messageTypes</code>,
	 * <code>binding</code> and <code>listener</code>.
	 * 
	 * @param messageTypes determines which message types to remove registration
	 *            from
	 * @param binding the binding to remove
	 * @param listener the callback which was previously registered
	 * @throws IOException in case removing the registration failed for some
	 *             reason, e.g. this binding was not already registered, etc.
	 * @throws WS4DIllegalStateException if this communication manager has
	 *             already been {@link #stop() stopped}
	 */
	public void unregisterDevice(int[] messageTypes, CommunicationBinding binding, IncomingMessageListener listener) throws IOException, WS4DIllegalStateException;

	/**
	 * Destroys a previously made
	 * {@link #register(int[], CommunicationBinding, IncomingMessageListener)
	 * registration} for the given <code>messageTypes</code>,
	 * <code>binding</code> and <code>listener</code>.
	 * 
	 * @param messageTypes determines which message types to remove registration
	 *            from
	 * @param binding the binding to remove
	 * @param listener the callback which was previously registered
	 * @throws IOException in case removing the registration failed for some
	 *             reason, e.g. this binding was not already registered, etc.
	 * @throws WS4DIllegalStateException if this communication manager has
	 *             already been {@link #stop() stopped}
	 */
	public void unregisterService(int[] messageTypes, CommunicationBinding binding, IncomingMessageListener listener) throws IOException, WS4DIllegalStateException;

	/**
	 * Destroys a previously made
	 * {@link #register(int[], CommunicationBinding, IncomingMessageListener)
	 * registration} for the given <code>messageTypes</code>,
	 * <code>binding</code> and <code>listener</code>.
	 * 
	 * @param messageTypes determines which message types to remove registration
	 *            from
	 * @param binding the binding to remove
	 * @param listener the callback which was previously registered
	 * @throws IOException in case removing the registration failed for some
	 *             reason, e.g. this binding was not already registered, etc.
	 * @throws WS4DIllegalStateException if this communication manager has
	 *             already been {@link #stop() stopped}
	 */
	public void unregisterDiscovery(int[] messageTypes, DiscoveryBinding binding, IncomingMessageListener listener) throws IOException, WS4DIllegalStateException;

	/**
	 * Destroys a previously made
	 * {@link #register(int[], CommunicationBinding, IncomingMessageListener)
	 * registration} for the given <code>messageTypes</code>,
	 * <code>binding</code> and <code>listener</code>.
	 * 
	 * @param messageTypes determines which message types to remove registration
	 *            from
	 * @param binding the binding to remove
	 * @param listener the callback which was previously registered
	 * @throws IOException in case removing the registration failed for some
	 *             reason, e.g. this binding was not already registered, etc.
	 * @throws WS4DIllegalStateException if this communication manager has
	 *             already been {@link #stop() stopped}
	 */
	public void unregisterDeviceReference(int[] messageTypes, DiscoveryBinding binding, IncomingMessageListener listener) throws IOException, WS4DIllegalStateException;

	/**
	 * Sends the <code>message</code> as a <em>asynchronous request</em> through
	 * the specified <code>domain</code>; any responses (if present) including
	 * faults and timeouts will be delivered to the given <code>callback</code>.
	 * <p>
	 * The argument <code>domain</code> has a certain meaning only when sending
	 * multicast/broadcast messages; this currently applies to
	 * {@link HelloMessage}s, {@link ByeMessage}s, {@link ProbeMessage}s and
	 * {@link ResolveMessage}s. In this case, it specifies the concrete
	 * transport technology and optionally, some technology- or
	 * protocol-specific interfaces (aka. &quot;domains&quot;) over which to
	 * send the multicast message. In the case of DPWS, where multicast messages
	 * are sent by means of SOAP-over-UDP, the value of this argument could
	 * depict a certain network interface (e.g. <em>eth0</em>, <em>pcn0</em>,
	 * etc.) or a specific local IP address. This should then be used to send
	 * the multicast message. For further information regarding the outgoing
	 * interface of multicast messages when using IP multicast, see <a
	 * href="http://tools.ietf.org/rfc/rfc1112.txt">RFC 1112</a>.
	 * </p>
	 * <p>
	 * In the concrete case when <code>message</code> is one of
	 * {@link HelloMessage}, {@link ByeMessage} or
	 * {@link SubscriptionEndMessage}, the value of <code>callback</code> is
	 * ignored. In any other case it is expected to have a non-<code>null</code>
	 * value.
	 * </p>
	 * 
	 * @param message the request message to send
	 * @param domain protocol domain over which to send the message
	 * @param callback where to deliver responses to the message
	 * @throws WS4DIllegalStateException if this communication manager has
	 *             already been {@link #stop() stopped}
	 */
	public void send(Message message, XAddressInfo targetXAdrInfo, ProtocolDomain domain, ResponseCallback callback) throws WS4DIllegalStateException;

	/**
	 * Deploys the given resource so that it can be accessed over the technology
	 * that this communication manager instance represents. The resource is made
	 * available over the addressing information provided by the specified
	 * <code>binding</code> and additional resource-specific addressing
	 * information found in <code>resourcePath</code>. Returns an {@link URI}
	 * depicting the actual address the resource is bound to.
	 * 
	 * @param resource the resource to deploy
	 * @param binding a binding over which to make the resource available
	 * @param resourcePath additional addressing-related information for use
	 *            when binding the resource
	 * @return actual address the resource is bound to
	 * @throws IOException in case binding the resource failed for some reason,
	 *             e.g. an address being already in use, etc.
	 * @throws WS4DIllegalStateException if this communication manager has
	 *             already been {@link #stop() stopped}
	 */
	public URI registerResource(Resource resource, CommunicationBinding binding, String resourcePath, HTTPGroup user) throws IOException, WS4DIllegalStateException;

	/**
	 * Removes the previously
	 * {@link #registerResource(Resource, CommunicationBinding, String)
	 * deployed} resource at the given <code>deployAddress</code> from this
	 * communication manager.
	 * 
	 * @param deployAddress the addressing at which the resource previously was
	 *            {@link #registerResource(Resource, CommunicationBinding, String)}
	 * @throws IOException in case removing the resource failed for some reason,
	 *             e.g. the resource was not previously deployed, etc.
	 * @throws WS4DIllegalStateException if this communication manager has
	 *             already been {@link #stop() stopped}
	 */
	public void unregisterResource(URI deployAddress, CommunicationBinding binding) throws IOException, WS4DIllegalStateException;

	/**
	 * Returns an input stream which allows to read a resource from the given
	 * location.
	 * 
	 * @param location the location of the resource (e.g.
	 *            http://example.org/test.wsdl).
	 * @return an input stream for the given resource.
	 */
	public ResourceLoader getResourceAsStream(URI location) throws IOException;

	/**
	 * @param xAddr a protocol dependent transport address
	 * @param source if <code>true</code>, the given URI <code>xAddr</code> is
	 *            compared against the source address of the specified
	 *            <code>protocolData</code>, otherwise against the destination
	 *            address
	 * @param protocolData protocol data instance associated with a certain
	 *            incoming message
	 * @return <code>true</code> only if the given <code>protocolData</code> has
	 *         a source or destination address matching the specified
	 *         <code>xAddr</code> URI
	 */
	public boolean addressMatches(URI xAddr, boolean source, ProtocolData protocolData);

	/**
	 * TODO
	 * 
	 * @param descriptor
	 * @param bindings
	 * @throws IOException
	 */
	public void getAutobindings(String descriptor, DataStructure bindings) throws IOException;

	/**
	 * TODO
	 * 
	 * @param bindings
	 * @param domains
	 * @throws IOException
	 */
	public void getProtocolDomains(Iterator bindings, DataStructure domains) throws IOException;

	/**
	 * To get a MulticastReceiver to all available Domains.
	 */
	public DataStructure getDiscoveryBindings() throws IOException;

	/**
	 * Returns a discovery binding for a specified domain.
	 * 
	 * @param domain specified domain.
	 * @return Binding.
	 * @throws IOException If the Domain does not fit.
	 */
	public DiscoveryBinding getDiscoveryBindingForDomain(ProtocolDomain domain) throws IOException;

	public DataStructure getDiscoveryDomainForBinding(DiscoveryBinding binding) throws IOException;

	public DiscoveryBinding getDiscoveryBindingForProtocolData(ProtocolData data);

	public void serializeMessageWithAttachments(Message message, String attachmentSep, List attachments, OutputStream out, ProtocolData pd) throws IOException;

	/**
	 * Returns the an instance of {@link ProtocolInfo} for this communication
	 * manager.
	 * 
	 * @return the protocol info.
	 */
	public ProtocolInfo getProtocolInfo();

	/**
	 * Creates an instance of {@link ProtocolInfo} according to the given
	 * version information.
	 * 
	 * @param version the version the protocol info should be created for.
	 * @return the protocol info for the given version.
	 */
	public ProtocolInfo createProtocolInfo(int version);

	/**
	 * Returns a set of supported versions by this communication manager.
	 * <p>
	 * This should be a set containing {@link Integer} objects with the version
	 * number.
	 * 
	 * @return a set of supported versions.
	 */
	public HashSet getSupportedVersions();

	/**
	 * Returns an utility class for this communication manager.
	 * 
	 * @return the utility class for this communication manager.
	 */
	public CommunicationUtil getCommunicationUtil();

	public long getRandomApplicationDelay(int version);

	public boolean supportsAddressingNamespace(SOAPHeader header, String namespace, ProtocolData pd) throws VersionMismatchException;

	public QNameSet getDeviceTypes();
}
