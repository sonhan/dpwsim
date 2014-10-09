package org.ws4d.java.communication;

import org.ws4d.java.communication.connection.ip.IPAddress;
import org.ws4d.java.constants.HTTPConstants;

/**
 * This Binding must be used for a secure service or a secure device.
 */
public class HTTPSBinding extends HTTPBinding {

	/**
	 * The alias of the certificate in the keystore. Only set if security is
	 * used
	 */
	private String	alias	= null;

	/**
	 * Constructor. <BR>
	 * Behaves like the HTTPBinding. The alias of the certificate that will be
	 * used is extrapolated from the address, the port and the path.
	 * 
	 * @param ipAddress
	 * @param port
	 * @param path
	 */
	public HTTPSBinding(IPAddress ipAddress, int port, String path) {
		super(ipAddress, port, path);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @deprecated <BR>
	 *             Use HTTPSBinding(IPAddress ipAddress, int port, String path)
	 * @param address String
	 * @param port
	 * @param path
	 */

	public HTTPSBinding(String address, int port, String path) {
		super(address, port, path);
		alias = address + ":" + port + path;
	}

	/**
	 * Sets the alias of the certificate to use for SSL Encryption. This is only
	 * necessary for HTTPS Sockets.
	 * 
	 * @param alias
	 */
	public void setAlias(String alias) {
		this.alias = alias;
	}

	public String getAlias() {
		return alias;
	}

	public int getType() {
		return HTTPS_BINDING;
	}

	public String getURISchema() {
		return HTTPConstants.HTTPS_SCHEMA;
	}
}
