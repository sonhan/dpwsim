/*******************************************************************************
 * Copyright (c) 2009 MATERNA Information & Communications. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html. For further
 * project-related information visit http://www.ws4d.org. The most recent
 * version of the JMEDS framework can be obtained from
 * http://sourceforge.net/projects/ws4d-javame.
 ******************************************************************************/
package org.ws4d.java.dispatch;

import org.ws4d.java.communication.ProtocolData;
import org.ws4d.java.communication.ProtocolInfo;
import org.ws4d.java.message.discovery.HelloMessage;
import org.ws4d.java.structures.EmptyStructures;
import org.ws4d.java.structures.Iterator;
import org.ws4d.java.structures.ReadOnlyIterator;
import org.ws4d.java.types.AppSequence;
import org.ws4d.java.types.DiscoveryData;
import org.ws4d.java.types.EndpointReference;
import org.ws4d.java.types.QNameSet;
import org.ws4d.java.types.ScopeSet;
import org.ws4d.java.types.XAddressInfoSet;

/**
 * Data associated with a received hello message. A device reference created by
 * this data is more specific than another created only by an endpoint
 * reference.
 */
public class HelloData {

	private final HelloMessage	hello;

	private final ProtocolData	protocolData;

	public HelloData(HelloMessage hello, ProtocolData protocolData) {
		this.hello = hello;
		this.protocolData = protocolData;
	}

	/**
	 * Get endpoint reference of the device transmitted by the hello message.
	 * 
	 * @return the endpoint reference of the device
	 */
	public EndpointReference getEndpointReference() {
		return hello.getDiscoveryData().getEndpointReference();
	}

	/**
	 * Get metadata version of the device transmitted by the hello message.
	 * 
	 * @return the metadata version of the device
	 */
	public long getMetadataVersion() {
		return hello.getDiscoveryData().getMetadataVersion();
	}

	/**
	 * Get scopes of the device transmitted by the hello message.
	 * 
	 * @return the scopes of the device
	 */
	public Iterator getScopes() {
		ScopeSet scopes = hello.getDiscoveryData().getScopes();
		if (scopes != null && scopes.size() > 0) {
			return new ReadOnlyIterator(scopes.getScopesAsUris().iterator());
		}

		return EmptyStructures.EMPTY_ITERATOR;
	}

	/**
	 * Get port types of the device transmitted by the hello message.
	 * 
	 * @return the device port types
	 */
	public Iterator getDevicePortTypes() {
		QNameSet types = hello.getDiscoveryData().getTypes();
		if (types != null && types.size() > 0) {
			return new ReadOnlyIterator(types.iterator());
		}

		return EmptyStructures.EMPTY_ITERATOR;
	}

	/**
	 * Get transport addresses of the device transmitted by the hello message.
	 * 
	 * @return the XAddressInfos of the device
	 */
	public Iterator getXAddressInfos() {
		XAddressInfoSet xaddresses = hello.getDiscoveryData().getXAddressInfoSet();
		if (xaddresses != null) {
			new ReadOnlyIterator(xaddresses.iterator());
		}

		return EmptyStructures.EMPTY_ITERATOR;
	}

	/**
	 * Get the ProtocolVersion of the Hello message.
	 * 
	 * @return
	 */
	public ProtocolInfo getVersion() {
		return hello.getProtocolInfo();
	}

	public String toString() {
		if (hello != null && protocolData != null) {
			return hello.toString() + " at " + protocolData.toString();
		} else if (hello != null) {
			return hello.toString();
		}
		return "Hello data is empty!";
	}

	// -----------------------------------------------------------

	DiscoveryData getDiscoveryData() {
		return hello.getDiscoveryData();
	}

	AppSequence getAppSequence() {
		return hello.getAppSequence();
	}

	public ProtocolData getProtocolData() {
		return protocolData;
	}

	boolean isInbound() {
		return hello.isInbound();
	}
}
