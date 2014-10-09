/*******************************************************************************
 * Copyright (c) 2009 MATERNA Information & Communications. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html. For further
 * project-related information visit http://www.ws4d.org. The most recent
 * version of the JMEDS framework can be obtained from
 * http://sourceforge.net/projects/ws4d-javame.
 ******************************************************************************/
package org.ws4d.java.wsdl.soap12;

import java.io.IOException;

import org.ws4d.java.constants.WSDLConstants;
import org.ws4d.java.types.QName;
import org.ws4d.java.types.URI;
import org.ws4d.java.wsdl.WSDLPort;
import org.xmlpull.v1.XmlSerializer;

/**
 * 
 */
public class SOAP12DocumentLiteralHTTPPort extends WSDLPort {

	private URI	location;

	/**
	 * 
	 */
	public SOAP12DocumentLiteralHTTPPort() {
		this(null);
	}

	/**
	 * @param name
	 */
	public SOAP12DocumentLiteralHTTPPort(String name) {
		this(name, null);
	}

	/**
	 * @param name
	 * @param bindingName
	 */
	public SOAP12DocumentLiteralHTTPPort(String name, QName bindingName) {
		super(name, bindingName);
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.wsdl.WSDLPort#serializePortExtension(org.xmlpull.v1
	 * .XmlSerializer)
	 */
	public void serializePortExtension(XmlSerializer serializer) throws IOException {
		serializer.startTag(WSDLConstants.SOAP12_BINDING_NAMESPACE_NAME, WSDLConstants.WSDL_ELEM_ADDRESS);
		serializer.attribute(null, WSDLConstants.WSDL_ATTRIB_LOCATION, location.toString());
		serializer.endTag(WSDLConstants.SOAP12_BINDING_NAMESPACE_NAME, WSDLConstants.WSDL_ELEM_ADDRESS);
	}

	/**
	 * @return the location
	 */
	public URI getLocation() {
		return location;
	}

	/**
	 * @param location the location to set
	 */
	public void setLocation(URI location) {
		this.location = location;
	}

}
