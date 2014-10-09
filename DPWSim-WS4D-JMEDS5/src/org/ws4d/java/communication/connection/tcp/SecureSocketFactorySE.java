package org.ws4d.java.communication.connection.tcp;

import java.io.IOException;

import org.ws4d.java.communication.connection.ip.IPAddress;

public class SecureSocketFactorySE {

	/**
	 * Creates an SSL secured SE ServerSocket.
	 * 
	 * @param adr IP address.
	 * @param port port
	 * @return the ServerSocket.
	 * @throws IOException
	 */
	public static ServerSocket createServerSocket(IPAddress adr, int port, String alias) throws IOException {
		return new SESecureServerSocket(adr, port, alias);
	}

	/**
	 * Creates an SSL secured SE Socket.
	 * 
	 * @param adr IP address.
	 * @param port port
	 * @return the ServerSocket.
	 * @throws IOException
	 */
	public static Socket createSocket(IPAddress adr, int port, String alias) throws IOException {
		return new SESecureSocket(adr, port, alias);
	}
}
