/*******************************************************************************
 * Copyright (c) 2009 MATERNA Information & Communications. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html. For further
 * project-related information visit http://www.ws4d.org. The most recent
 * version of the JMEDS framework can be obtained from
 * http://sourceforge.net/projects/ws4d-javame.
 ******************************************************************************/
package org.ws4d.java.communication.monitor;

import java.io.OutputStream;

import org.ws4d.java.communication.Resource;
import org.ws4d.java.message.Message;
import org.ws4d.java.types.URI;

/**
 * This interfaces allows to assign the message and the given stream.
 */
public interface StreamMonitor {

	/**
	 * A new and shiny <code>OutputStream</code> to write new bytes from
	 * incoming or outgoing connections etc.
	 * <p>
	 * Remember: This method should always return the same
	 * <code>OutputStream</code> for an individual <code>StreamMonitor</code>
	 * because it is used in different parts of the framework.
	 * </p>
	 * 
	 * @return the <code>OutputStream</code>.
	 */
	public OutputStream getOutputStream();

	public void setMonitoringContext(MonitoringContext context);

	public MonitoringContext getMonitoringContext();

	/**
	 * This method will be invoked by the framework if a message is finally
	 * built up and should be assigned to the <code>OutputStream</code> created
	 * with the <code>getOutputStream</code> method.
	 * 
	 * @param message the message which should be assigned to the
	 *            <code>OutputStream</code>
	 */
	public void assign(MonitoringContext context, Message message);

	/**
	 * This is a fault method which will be invoked if the framework could not
	 * build a correct message object. It is like the <code>assign</code>
	 * method, but points to a fault while the message is created.
	 */
	public void fault(MonitoringContext context, Exception e);

	public void discard(MonitoringContext context, int discardReason);

	public void resource(MonitoringContext context, Resource resource);

	public void request(MonitoringContext context, URI location);

}
