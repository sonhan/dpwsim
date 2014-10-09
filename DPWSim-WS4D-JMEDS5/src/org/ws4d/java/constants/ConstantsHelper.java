/*******************************************************************************
 * Copyright (c) 2009 MATERNA Information & Communications. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html. For further
 * project-related information visit http://www.ws4d.org. The most recent
 * version of the JMEDS framework can be obtained from
 * http://sourceforge.net/projects/ws4d-javame.
 ******************************************************************************/
package org.ws4d.java.constants;

import org.ws4d.java.types.QName;
import org.ws4d.java.types.URI;

/**
 * This interface allows to get version depending constants.
 */
public interface ConstantsHelper {

	public int getDPWSVersion();

	/* DPWS */

	public int getRandomApplicationDelay();

	public String getDPWSNamespace();

	public String getDPWSNamespacePrefix();

	public String getDPWSFilterEventingAction();

	public URI getDPWSUriFilterEeventingAction();

	public QName getDPWSFaultFilterActionNotSupported();

	public String getMetadataDialectThisModel();

	public String getMetadataDialectThisDevice();

	public String getMetatdataDialectRelationship();

	public String getMetadataRelationshipHostingType();

	public String getDPWSActionFault();

	public String getDPWSAttributeRelationshipType();

	public String getDPWSElementRelationshipHost();

	public String getDPWSElementRelationshipHosted();

	public String getDPWSElementTypes();

	public String getDPWSElementRelationship();

	public String getDPWSElementServiceId();

	public String getDPWSElementFriendlyName();

	public String getDPWSElementFirmwareVersion();

	public String getDPWSElementSerialnumber();

	public String getDPWSElementThisDevice();

	public String getDPWSElementThisModel();

	public String getDPWSElementManufacturer();

	public String getDPWSElementManufacturerURL();

	public String getDPWSElementModelName();

	public String getDPWSElementModelNumber();

	public String getDPWSElementModelURL();

	public String getDPWSElementPresentationURL();

	/* Qualified names */

	public QName getDPWSQnManufacturer();

	public QName getDPWSQnManufactuerURL();

	public QName getDPWSQnModelname();

	public QName getDPWSQnModelnumber();

	public QName getDPWSQnModelURL();

	public QName getDPWSQnPresentationURL();

	public QName getDPWSQnFriendlyName();

	public QName getDPWSQnFirmware();

	public QName getDPWSQnSerialnumber();

	public QName getDPWSQnServiceID();

	public QName getDPWSQnEndpointReference();

	public QName getDPWSQnTypes();

	public QName getDPWSQnDeviceType();

	/* WSA Constants */

	public String getWSANamespace();

	public String getWSAElemReferenceProperties();

	public String getWSAElemPortType();

	public String getWSAElemServiceName();

	public String getWSAElemPolicy();

	public URI getWSAAnonymus();

	public String getWSAActionAddressingFault();

	public String getWSAActionSoapFault();

	/* Faults */

	public QName getWSAFaultDestinationUnreachable();

	public QName getWSAFaultInvalidAddressingHeader();

	public QName getWSAFaultMessageAddressingHeaderRequired();

	public QName getWSAFaultActionNotSupported();

	public QName getWSAfaultEndpointUnavailable();

	public QName getWSAProblemHeaderQname();

	public QName getWSAProblemAction();

	/* WSD Constants */

	public String getWSDNamespace();

	public String getWSDTo();

	public String getWSDActionHello();

	public String getWSDActionBye();

	public String getWSDActionProbe();

	public String getWSDActionProbeMatches();

	public String getWSDActionResolve();

	public String getWSDActionResolveMatches();

	public String getWSDActionFault();

	String getMetadataDialectCustomizeMetadata();
}