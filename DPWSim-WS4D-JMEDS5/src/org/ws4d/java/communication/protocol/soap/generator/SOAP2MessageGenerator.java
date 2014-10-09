package org.ws4d.java.communication.protocol.soap.generator;

import java.io.InputStream;

import org.ws4d.java.communication.ProtocolData;
import org.ws4d.java.message.Message;

public interface SOAP2MessageGenerator {

	/**
	 * This method generates message objects from the given input stream.
	 * 
	 * @param in the stream to read input from
	 * @return complete message object - needs to be casted: use getType()
	 *         method
	 */
	public Message generateMessage(InputStream in) throws Exception;

	/**
	 * Delivers a single incoming message obtained from reading <code>in</code>
	 * to <code>to</code>. Uses default {@link DefaultMessageDiscarder}.
	 * 
	 * @param in the stream from which to parse the message
	 * @param to the receiver to deliver the message to
	 * @param protocolData transport-related information attached to the message
	 *            being received; it is passed directly to one of the
	 *            <code>receive()</code> methods of the specified
	 *            {@link MessageReceiver} <code>to</code>
	 */
	public void deliverMessage(InputStream in, MessageReceiver to, ProtocolData protocolData);

	/**
	 * Delivers a single incoming message obtained from reading <code>in</code>
	 * to <code>to</code>.
	 * 
	 * @param in the stream from which to parse the message
	 * @param to the receiver to deliver the message to
	 * @param protocolData transport-related information attached to the message
	 *            being received; it is passed directly to one of the
	 *            <code>receive()</code> methods of the specified
	 *            {@link MessageReceiver} <code>to</code>
	 * @param discarder decides whether to deliver or drop the message
	 */
	public void deliverMessage(InputStream in, MessageReceiver to, ProtocolData protocolData, DefaultMessageDiscarder discarder);

}