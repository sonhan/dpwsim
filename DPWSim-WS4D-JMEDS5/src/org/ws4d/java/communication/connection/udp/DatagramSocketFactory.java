/*******************************************************************************
 * Copyright (c) 2009 MATERNA Information & Communications. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html. For further
 * project-related information visit http://www.ws4d.org. The most recent
 * version of the JMEDS framework can be obtained from
 * http://sourceforge.net/projects/ws4d-javame.
 ******************************************************************************/
package org.ws4d.java.communication.connection.udp;

import java.io.IOException;

import org.ws4d.java.communication.connection.ip.IPAddress;
import org.ws4d.java.util.Log;

/**
 * Creates server and client sockets.
 */
public abstract class DatagramSocketFactory {

	private static DatagramSocketFactory	instance;

	public static DatagramSocketFactory getInstance() {
		if (instance == null) {
			try {
				Class clazz = Class.forName("org.ws4d.java.communication.connection.udp.PlatformDatagramSocketFactory");
				instance = (DatagramSocketFactory) clazz.newInstance();
			} catch (Exception e) {
				Log.error("Unable to create PlatformDatagramSocketFactory: " + e.getMessage());
				throw new RuntimeException(e.getMessage());
			}
		}
		return instance;
	}

	/**
	 * Creates a <code>DatagramSocket</code>.<br />
	 * 
	 * @param host the host address.
	 * @param port port
	 * @return the ServerSocket.
	 * @throws IOException
	 */
	public abstract DatagramSocket createDatagramSocket(IPAddress host, int port, String ifaceName) throws IOException;

	/**
	 * Creates a <code>DatagramSocket</code>. *
	 * 
	 * @param host the host address.
	 * @param port port
	 * @param ifaceName
	 * @return the ServerSocket.
	 * @throws IOException
	 */
	public abstract DatagramSocket createDatagramServerSocket(IPAddress host, int port, String ifaceName) throws IOException;

	/**
	 * Creates a <code>MulticastSocket</code>. i It s for receiving multicast.
	 * 
	 * @param host
	 * @param port
	 * @param ifaceName
	 * @return
	 * @throws IOException
	 */
	public abstract DatagramSocket registerMulticastGroup(IPAddress host, int port, String ifaceName) throws IOException;

}
