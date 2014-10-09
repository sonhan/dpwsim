/*******************************************************************************
 * Copyright (c) 2009 MATERNA Information & Communications. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html. For further
 * project-related information visit http://www.ws4d.org. The most recent
 * version of the JMEDS framework can be obtained from
 * http://sourceforge.net/projects/ws4d-javame.
 ******************************************************************************/
package org.ws4d.java.security;

import org.ws4d.java.communication.connection.ip.IPAddress;
import org.ws4d.java.communication.connection.tcp.ServerSocket;
import org.ws4d.java.communication.connection.tcp.Socket;
import org.ws4d.java.types.URI;

/**
 * The DPWSSecurityManager manages many aspects of the security implementation.
 */
public interface DPWSSecurityManager extends SecurityManager {

	/**
	 * @param adr the address to bind to.
	 * @param port the port to bind to.
	 * @param alias the alias of the certificate to use for connection
	 *            encryption.
	 * @returns a new SSL/TLS secured server socket using the supplied
	 *          credentials.
	 */
	public ServerSocket getSecureServerSocket(IPAddress adr, int port, String alias);

	/**
	 * opens a connection to the remote location specified in the supplied URI
	 * 
	 * @param location
	 * @return
	 */
	public Socket getSecureSocket(URI location);

	/**
	 * @param adr the address to bind to.
	 * @param port the port to bind to.
	 * @param alias the alias of the certificate to use for connection
	 *            encryption.
	 * @returns a new SSL/TLS secured socket using the supplied credentials.
	 */
	public Socket getSecureSocket(IPAddress host, int port, String alias);

}