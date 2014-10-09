/*******************************************************************************
 * Copyright (c) 2009 MATERNA Information & Communications. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html. For further
 * project-related information visit http://www.ws4d.org. The most recent
 * version of the JMEDS framework can be obtained from
 * http://sourceforge.net/projects/ws4d-javame.
 ******************************************************************************/
package org.ws4d.java.service;

import org.ws4d.java.communication.ProtocolData;
import org.ws4d.java.dispatch.DefaultDeviceReference;
import org.ws4d.java.dispatch.MissingMetadataException;
import org.ws4d.java.message.metadata.GetResponseMessage;
import org.ws4d.java.service.reference.ServiceReference;
import org.ws4d.java.types.QNameSet;

/**
 * This factory allows to create proxies for devices and services.
 */
public interface ProxyFactory {

	public Device createProxyDevice(GetResponseMessage message, DefaultDeviceReference devRef, Device oldDevice, ProtocolData protocolData);

	public boolean checkServiceUpdate(Service service, QNameSet newPortTypes) throws MissingMetadataException;

	public Service createProxyService(ServiceReference serviceReference, ProtocolData protocolData) throws MissingMetadataException;

}
