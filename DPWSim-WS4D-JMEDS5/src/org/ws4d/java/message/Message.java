/*******************************************************************************
 * Copyright (c) 2009 MATERNA Information & Communications. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html. For further
 * project-related information visit http://www.ws4d.org. The most recent
 * version of the JMEDS framework can be obtained from
 * http://sourceforge.net/projects/ws4d-javame.
 ******************************************************************************/
package org.ws4d.java.message;

import org.ws4d.java.communication.ProtocolInfo;
import org.ws4d.java.constants.DPWSMessageConstants;
import org.ws4d.java.types.AppSequence;
import org.ws4d.java.types.AttributedURI;
import org.ws4d.java.types.EndpointReference;
import org.ws4d.java.types.URI;
import org.ws4d.java.types.UnknownDataContainer;
import org.ws4d.java.types.XAddressInfo;
import org.ws4d.java.util.StringUtil;

/**
 * This class implements an abstract MessageObject.
 */
public abstract class Message extends UnknownDataContainer implements DPWSMessageConstants {

	// Routing schemes added by Stefan Schlichting
	public final static int	UNKNOWN_ROUTING_SCHEME		= 0x0;

	public final static int	UNICAST_ROUTING_SCHEME		= 0x1;

	public final static int	MULTICAST_ROUTING_SCHEME	= 0x2;

	protected SOAPHeader	header;

	/**
	 * If <code>true</code>, then this is a message received over a remote
	 * communication channel; if <code>false</code>, the message is being sent
	 * from this stack instance.
	 */
	protected boolean		inbound						= false;

	/*
	 * TODO 13.05.2011:
	 * Remove address from message object
	 */
	// only meaningful for outgoing request messages
	private XAddressInfo	targetXAddressInfo;

	private int				routingScheme				= UNKNOWN_ROUTING_SCHEME;

	private boolean			secureMessage				= false;

	private Object			certificate;

	private Object			privateKey;

	/**
	 * Constructor.
	 * 
	 * @param header
	 */
	public Message(SOAPHeader header) {
		super();
		this.header = header;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		StringBuffer sb = new StringBuffer(StringUtil.formatClassName(getClass()));
		sb.append(" [ header=").append(header);
		sb.append(", inbound=").append(inbound);
		sb.append(" ]");
		return sb.toString();
	}

	/**
	 * Sets the {@link #getRelatesTo() [relationship]}, {@link #getTo() [to]}
	 * and [parameters] properties of this message to the values of the
	 * {@link #getMessageId() [message ID]} and {@link #getReplyTo() [reply to]}
	 * properties of the passed in message.
	 * 
	 * @param request the message from which to extract the source properties
	 */
	public void setResponseTo(Message request) {
		header.setResponseTo(request.header);
		header.setProtocolInfo(request.getProtocolInfo());
	}

	/**
	 * Sets the {@link #getRelatesTo() [relationship]}, {@link #getTo() [to]}
	 * and [parameters] properties of this message to the values of the
	 * {@link SOAPHeader#getMessageId() [message ID]} and
	 * {@link SOAPHeader#getReplyTo() [reply to]} properties of the passed in
	 * SOAP header.
	 * 
	 * @param requestHeader the SOAP header from which to extract the source
	 *            properties
	 */
	public void setResponseTo(SOAPHeader requestHeader) {
		header.setResponseTo(requestHeader);
	}

	// ----------------------- MESSAGE -----------------------------

	/**
	 * Type of message.
	 * 
	 * @return type.
	 */
	public abstract int getType();

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.message.DPWSMessage#getHeader()
	 */
	public SOAPHeader getHeader() {
		return header;
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.message.DPWSMessage#getAction()
	 */
	public AttributedURI getAction() {
		return header.getAction();
	}

	/**
	 * Send using WS-Security techniques.
	 */
	public boolean isSecure() {
		return secureMessage;
	}

	/**
	 * This is the certificate against which the messages signature will be
	 * validated. Only used within the security module.
	 * 
	 * @param certificate must be the java.security.cert.Certificate of the
	 *            sender device/service
	 */
	public void setCertificate(Object certificate) {
		this.certificate = certificate;
	}

	/**
	 * This is the private key with which this message will be signed. Only used
	 * within the security module.
	 * 
	 * @param privKey must be the java.security.PrivateKey of the sender device/
	 *            service
	 */
	public void setPrivateKey(Object privKey) {
		this.privateKey = privKey;
	}

	/**
	 * This is the certificate against which the messages signature will be
	 * validated. * Only used within the security module.
	 * 
	 * @return java.security.cert.Certificate
	 */
	public Object getCertificate() {
		return certificate;
	}

	/**
	 * This is the private key with which this message will be signed. Only used
	 * within the security module.
	 * 
	 * @return java.security.PrivateKey
	 */
	public Object getPrivateKey() {
		return privateKey;
	}

	/**
	 * Sets wether or not the message should be sent secure. Caution: don't set
	 * this flag when received the message.
	 */
	public void setSecure(boolean b) {
		this.secureMessage = b;
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.message.DPWSMessage#getMessageId()
	 */
	public AttributedURI getMessageId() {
		return header.getMessageId();
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.message.DPWSMessage#getRelatesTo()
	 */
	public AttributedURI getRelatesTo() {
		return header.getRelatesTo();
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.message.DPWSMessage#getTo()
	 */
	public AttributedURI getTo() {
		return header.getTo();
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.message.DPWSMessage#getReplyTo()
	 */
	public EndpointReference getReplyTo() {
		return header.getReplyTo();
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.message.DPWSMessage#getAppSequence()
	 */
	public AppSequence getAppSequence() {
		return header.getAppSequence();
	}

	/**
	 * @return the targetAddress
	 */
	public URI getTargetAddress() {
		return (targetXAddressInfo != null) ? targetXAddressInfo.getXAddress() : null;
	}

	/**
	 * @return the targetAddress
	 */
	public XAddressInfo getTargetXAddressInfo() {
		return targetXAddressInfo;
	}
	
	/**
	 * @param targetXAddressInfo the targetAddress to set
	 */
	public void setTargetXAddressInfo(XAddressInfo targetXAddressInfo) {
		this.targetXAddressInfo = targetXAddressInfo;
	}

	/**
	 * Returns <code>true</code> if this message was received over a remote
	 * communication channel. Returns <code>false</code> if the message is being
	 * sent from this stack instance.
	 * 
	 * @return whether this is an inbound or an outbound message
	 */
	public boolean isInbound() {
		return inbound;
	}

	/**
	 * @param inbound the inbound to set
	 */
	public void setInbound(boolean inbound) {
		this.inbound = inbound;
	}

	public void setProtocolInfo(ProtocolInfo Version) {
		header.setProtocolInfo(Version);
	}

	public ProtocolInfo getProtocolInfo() {
		return header.getProtocolInfo();
	}

	/**
	 * Gets the outgoing routing scheme for this message. It can be unknown
	 * (0x0), unicast (0x1), multicast (0x2).
	 * http://en.wikipedia.org/wiki/Routing
	 * 
	 * @return
	 */
	public int getRoutingScheme() {
		return routingScheme;
	}

	public void setRoutingScheme(int routingScheme) {
		this.routingScheme = routingScheme;
	}

	protected void setSOAPHeader(SOAPHeader header) {
		this.header = header;
	}
}
