package org.ws4d.java.communication.connection.tcp;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;

import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509KeyManager;

import org.ws4d.java.DPWSFramework;
import org.ws4d.java.communication.connection.ip.IPAddress;
import org.ws4d.java.communication.connection.ip.IPNetworkDetection;
import org.ws4d.java.security.ForcedAliasKeyManager;

public class SESecureSocket implements Socket {

	private static final int	DEFAULT_OUT_BUFFER_SIZE	= 8192;

	java.net.Socket				socket;

	private IPAddress			ipAddress				= null;

	private int					port					= -1;

	private InputStream			in						= null;

	private OutputStream		out						= null;

	private String				alias					= null;

	/**
	 * Default constructor. Initializes the object.
	 * 
	 * @param host host name.
	 * @param port port number.
	 * @param alias of the certificate to use.
	 * @throws IOException
	 */
	public SESecureSocket(IPAddress host, int port, String alias) throws IOException {
		try {
			this.alias = alias;
			javax.net.SocketFactory socketFactory = this.getSSLSocketFactory();
			socket = socketFactory.createSocket(host.getAddressWithoutNicId(), port);
		} catch (KeyStoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public SESecureSocket(java.net.Socket socket) {
		this.socket = socket;
	}

	/**
	 * Closes the connection.
	 */
	public void close() throws IOException {
		if (socket == null) {
			throw new IOException("No open connection. Can not close connection");
		}
		socket.close();
	}

	/**
	 * Opens an <code>InputStream</code> on the socket.
	 * 
	 * @return an InputStream.
	 */
	public InputStream getInputStream() throws IOException {
		if (socket == null) {
			throw new IOException("No open connection. Can not open input stream");
		}
		if (in == null) {
			in = socket.getInputStream();
		}
		return in;
	}

	/**
	 * Opens an <code>OutputStream</code> on the socket.
	 * 
	 * @return an OutputStream.
	 */
	public OutputStream getOutputStream() throws IOException {
		if (socket == null) {
			throw new IOException("No open connection. Can not open output stream");
		}
		if (out == null) {
			out = new BufferedOutputStream(socket.getOutputStream(), DEFAULT_OUT_BUFFER_SIZE);
		}
		return out;
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.communication.connection.tcp.Socket#getRemoteAddress()
	 */
	public IPAddress getRemoteAddress() {
		if (socket == null) return null;
		InetAddress i = socket.getInetAddress();
		if (i != null) {
			return new IPAddress(i.getHostAddress());
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.communication.connection.tcp.Socket#getRemotePort()
	 */
	public int getRemotePort() {
		if (socket == null) return -1;
		return socket.getPort();
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.communication.connection.tcp.Socket#getLocalAddress()
	 */
	public IPAddress getLocalAddress() {
		if (ipAddress == null) ipAddress = IPNetworkDetection.getInstance().getIPAddress(socket.getLocalAddress().getHostAddress());

		return ipAddress;
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.communication.connection.tcp.Socket#getLocalPort()
	 */
	public int getLocalPort() {
		if (port == -1) port = socket.getLocalPort();

		return port;
	}

	protected SSLSocketFactory getSSLSocketFactory() throws IOException, Exception {
		// Call the super classes to get suitable trust and key managers
		KeyManager[] kms = (KeyManager[]) DPWSFramework.getSecurityManager().getKeyManagers();
		TrustManager[] tms = (TrustManager[]) DPWSFramework.getSecurityManager().getTrustManagers();

		if (alias != null && kms != null) {
			for (int i = 0; i < kms.length; i++) {
				if (kms[i] instanceof X509KeyManager) kms[i] = new ForcedAliasKeyManager((X509KeyManager) kms[i], alias);
			}
		}

		SSLContext context = SSLContext.getInstance("SSL");
		context.init(kms, tms, null);

		SSLSocketFactory ssf = context.getSocketFactory();
		return ssf;
	}

}