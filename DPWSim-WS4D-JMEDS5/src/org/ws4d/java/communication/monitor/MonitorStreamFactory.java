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

import org.ws4d.java.communication.ProtocolData;
import org.ws4d.java.communication.Resource;
import org.ws4d.java.message.Message;
import org.ws4d.java.structures.HashMap;
import org.ws4d.java.types.URI;

/**
 * Factory which allows to create an <code>OutputStream</code> to catch incoming
 * and outgoing data which allows the creation of <code>Message</code> objects.
 */
public abstract class MonitorStreamFactory {

	public static HashMap		inMon			= new HashMap();

	public static HashMap		outMon			= new HashMap();

	public abstract StreamMonitor createInputMonitor();

	public abstract StreamMonitor createOutputMonitor();

	/**
	 * Creates a <code>StreamMonitor</code> for incoming messages.
	 * 
	 * @return the <code>StreamMonitor</code> for incoming messages.
	 */
	public final synchronized StreamMonitor getInputMonitor(ProtocolData pd) {
		StreamMonitor mon = (StreamMonitor) inMon.get(pd.getInstanceId());
		if (mon == null) {
			mon = createInputMonitor();
			inMon.put(pd.getInstanceId(), mon);
		}
		return mon;
	}

	/**
	 * Creates a <code>StreamMonitor</code> for outgoing messages.
	 * 
	 * @return the <code>StreamMonitor</code> for outgoing messages.
	 */
	public final synchronized StreamMonitor getOutputMonitor(ProtocolData pd) {
		StreamMonitor mon = (StreamMonitor) outMon.get(pd.getInstanceId());
		if (mon == null) {
			mon = createOutputMonitor();
			outMon.put(pd.getInstanceId(), mon);
		}
		return mon;
	}
	
	public final synchronized void resetMonitoringContextIn(ProtocolData pd) {
		StreamMonitor mon = (StreamMonitor) inMon.get(pd.getInstanceId());
		if (mon != null) {
			mon.setMonitoringContext(null);
		}
	}

	public final synchronized void resetMonitoringContextOut(ProtocolData pd) {
		StreamMonitor mon = (StreamMonitor) outMon.get(pd.getInstanceId());
		if (mon != null) {
			mon.setMonitoringContext(null);
		}
	}

	public final synchronized MonitoringContext getNewMonitoringContextIn(ProtocolData pd) {
		StreamMonitor mon = (StreamMonitor) inMon.get(pd.getInstanceId());
		MonitoringContext context = new MonitoringContext(pd);
		if (mon != null) {
			mon.setMonitoringContext(context);
		}
		return context;
	}

	public final synchronized MonitoringContext getNewMonitoringContextOut(ProtocolData pd) {
		StreamMonitor mon = (StreamMonitor) outMon.get(pd.getInstanceId());
		MonitoringContext context = new MonitoringContext(pd);
		if (mon != null) {
			mon.setMonitoringContext(context);
		}
		return context;
	}

	public final synchronized MonitoringContext getMonitoringContextIn(ProtocolData pd) {
		StreamMonitor mon = (StreamMonitor) inMon.get(pd.getInstanceId());
		if (mon != null) {
			return mon.getMonitoringContext();
		}
		return null;
	}

	public final synchronized MonitoringContext getMonitoringContextOut(ProtocolData pd) {
		StreamMonitor mon = (StreamMonitor) outMon.get(pd.getInstanceId());
		if (mon != null) {
			return mon.getMonitoringContext();
		}
		return null;
	}

	/**
	 * Method which allows the link between the current <code>Thread</code> and
	 * a <code>StreamMonitor</code> for an <code>OutputStream</code>.
	 * 
	 * @param pd the protocol data which will be used to identify the monitor.
	 * @param mon the stream monitor
	 */
	public final synchronized void linkIn(ProtocolData pd, StreamMonitor mon) {
		inMon.put(pd.getInstanceId(), mon);
	}

	/**
	 * Method which allows the link between the current <code>Thread</code> and
	 * a <code>StreamMonitor</code> for an <code>InputStream</code>.
	 * 
	 * @param pd the protocol data which will be used to identify the monitor.
	 * @param mon the stream monitor
	 */
	public final synchronized void linkOut(ProtocolData pd, StreamMonitor mon) {
		outMon.put(pd.getInstanceId(), mon);
	}

	/**
	 * Allows the assignment of a incoming <code>Message</code> to a previously
	 * given <code>OutputStream</code>.
	 * 
	 * @param pd the protocol data which will be used to identify the monitor.
	 * @param message the message.
	 */
	public final synchronized void received(ProtocolData pd, MonitoringContext context, Message message) {
		StreamMonitor mon = (StreamMonitor) inMon.get(pd.getInstanceId());
		if (mon != null) {
			mon.assign(context, message);
		}
	}

	/**
	 * Allows the assignment of a incoming discarded <code>Message</code> to a
	 * previously given <code>OutputStream</code>.
	 * 
	 * @param pd the protocol data which will be used to identify the monitor.
	 * @param header the message header.
	 */
	public final synchronized void discard(ProtocolData pd, MonitoringContext context, int discardReason) {
		StreamMonitor mon = (StreamMonitor) inMon.get(pd.getInstanceId());
		if (mon != null) {
			mon.discard(context, discardReason);
		}
	}

	/**
	 * Allows the assignment of a outgoing <code>Message</code> to a previously
	 * given <code>OutputStream</code>.
	 * 
	 * @param pd the protocol data which will be used to identify the monitor.
	 * @param message the message.
	 */
	public final synchronized void send(ProtocolData pd, MonitoringContext context, Message message) {
		StreamMonitor mon = (StreamMonitor) outMon.get(pd.getInstanceId());
		if (mon != null) {
			mon.assign(context, message);
		}
	}

	/**
	 * Allows to inform the incoming monitor about a fault.
	 * 
	 * @param pd the protocol data which will be used to identify the monitor.
	 */
	public final synchronized void receivedFault(ProtocolData pd, MonitoringContext context, Exception e) {
		StreamMonitor mon = (StreamMonitor) inMon.get(pd.getInstanceId());
		if (mon != null) {
			mon.fault(context, e);
		}
	}

	/**
	 * Allows to inform the outgoing monitor about a fault.
	 * 
	 * @param pd the protocol data which will be used to identify the monitor.
	 */
	public final synchronized void sendFault(ProtocolData pd, MonitoringContext context, Exception e) {
		StreamMonitor mon = (StreamMonitor) outMon.get(pd.getInstanceId());
		if (mon != null) {
			mon.fault(context, e);
		}
	}

	public final synchronized void sendResource(ProtocolData pd, MonitoringContext context, Resource r) {
		StreamMonitor mon = (StreamMonitor) outMon.get(pd.getInstanceId());
		if (mon != null) {
			mon.resource(context, r);
		}
	}

	public final synchronized void requestResource(ProtocolData pd, MonitoringContext context, URI location) {
		StreamMonitor mon = (StreamMonitor) outMon.get(pd.getInstanceId());
		if (mon != null) {
			mon.request(context, location);
		}
	}

	public final synchronized void receivedResource(ProtocolData pd, MonitoringContext context, Resource r) {
		StreamMonitor mon = (StreamMonitor) inMon.get(pd.getInstanceId());
		if (mon != null) {
			mon.resource(context, r);
		}
	}

}
