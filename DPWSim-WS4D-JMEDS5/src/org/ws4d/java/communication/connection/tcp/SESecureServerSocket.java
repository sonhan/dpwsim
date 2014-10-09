package org.ws4d.java.communication.connection.tcp;

import java.io.IOException;

import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509KeyManager;

import org.ws4d.java.DPWSFramework;
import org.ws4d.java.communication.connection.ip.IPAddress;
import org.ws4d.java.security.ForcedAliasKeyManager;

public class SESecureServerSocket implements ServerSocket {

	private IPAddress			ipAddress		= null;

	private int					port			= -1;

	java.net.ServerSocket		server			= null;

	private String				alias			= null;

	/**
	 * The number of attempts to find a random port to listen to before giving
	 * up.
	 */
	protected static final int	PORT_RETRIES	= 3;

	public SESecureServerSocket(IPAddress address, int port, String alias) throws IOException {
		this.alias = alias;
		this.ipAddress = address;
		this.port = port;
		try {
			javax.net.ServerSocketFactory ssf = this.getSSLServerSocketFactory();
			server = ssf.createServerSocket(port);
		} catch (IOException e) {
			throw new IOException(e.getMessage() + "For " + address + " at port " + port);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.communication.connection.tcp.ServerSocket#accept()
	 */
	public Socket accept() throws IOException {
		return new SESecureSocket(server.accept());
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.communication.connection.tcp.ServerSocket#close()
	 */
	public void close() throws IOException {
		server.close();
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.communication.connection.tcp.ServerSocket#getAddress()
	 */
	public IPAddress getIPAddress() {
		return ipAddress;
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.communication.connection.tcp.ServerSocket#getPort()
	 */
	public int getPort() {
		return port;
	}

	protected SSLServerSocketFactory getSSLServerSocketFactory() throws IOException, Exception {
		KeyManager[] kms = (KeyManager[]) DPWSFramework.getSecurityManager().getKeyManagers();
		TrustManager[] tms = (TrustManager[]) DPWSFramework.getSecurityManager().getTrustManagers();

		if (alias != null) {
			for (int i = 0; i < kms.length; i++) {
				if (kms[i] instanceof X509KeyManager) kms[i] = new ForcedAliasKeyManager((X509KeyManager) kms[i], alias);
			}
		}

		SSLContext context = SSLContext.getInstance("SSL");
		context.init(kms, tms, null);

		SSLServerSocketFactory ssf = context.getServerSocketFactory();
		return ssf;
	}

}
