/*******************************************************************************
 * Copyright (c) 2009 MATERNA Information & Communications. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html. For further
 * project-related information visit http://www.ws4d.org. The most recent
 * version of the JMEDS framework can be obtained from
 * http://sourceforge.net/projects/ws4d-javame.
 ******************************************************************************/
package org.ws4d.java.communication.protocol.mime;

import java.io.IOException;

import org.ws4d.java.communication.ProtocolData;
import org.ws4d.java.communication.monitor.MonitoringContext;
import org.ws4d.java.structures.Queue;

public interface MIMEHandler {

	public void handleRequest(MIMEEntityInput part, Queue responses, ProtocolData protocolData, MonitoringContext context) throws IOException;

	public void handleResponse(MIMEEntityInput part, ProtocolData protocolData, MonitoringContext context) throws IOException;

}
