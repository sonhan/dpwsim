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

import java.io.IOException;
import java.io.OutputStream;

import org.ws4d.java.communication.Resource;
import org.ws4d.java.message.Message;
import org.ws4d.java.types.URI;

/**
 * Simple extension of the <code>MonitorStreamFactory</code> which allows to
 * write any incoming or outgoing <code>Message</code> to the default error
 * output stream.
 */
public class DefaultMonitoredStreamFactory extends MonitorStreamFactory {

	private static final OutputStream	FORWARDER	= new OutputStream() {

														public void write(int b) throws IOException {
															System.err.write(b);
														}

													};

	public StreamMonitor createInputMonitor() {
		return new DefaultStreamMonitor();
	}

	public StreamMonitor createOutputMonitor() {
		return new DefaultStreamMonitor();
	}

	private class DefaultStreamMonitor implements StreamMonitor {

		public OutputStream getOutputStream() {
			/*
			 * !!! don't return System.out or System.err directly, as they would
			 * get closed by the monitors!!!
			 */
			return FORWARDER;
		}

		public void assign(MonitoringContext context, Message message) {
			// TODO Auto-generated method stub

		}

		public void fault(MonitoringContext context, Exception e) {
			// TODO Auto-generated method stub
		}

		public void setMonitoringContext(MonitoringContext context) {
			// TODO Auto-generated method stub

		}

		public MonitoringContext getMonitoringContext() {
			// TODO Auto-generated method stub
			return null;
		}

		public void discard(MonitoringContext context, int reason) {
			// TODO Auto-generated method stub

		}

		public void resource(MonitoringContext context, Resource resource) {
			// TODO Auto-generated method stub

		}

		public void request(MonitoringContext context, URI location) {
			// TODO Auto-generated method stub

		}

	}

}
