/*******************************************************************************
 * Copyright (c) 2009 MATERNA Information & Communications. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html. For further
 * project-related information visit http://www.ws4d.org. The most recent
 * version of the JMEDS framework can be obtained from
 * http://sourceforge.net/projects/ws4d-javame.
 ******************************************************************************/
package org.ws4d.java.communication.protocol.http;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.ws4d.java.DPWSFramework;
import org.ws4d.java.communication.ProtocolData;
import org.ws4d.java.communication.connection.tcp.TCPClient;
import org.ws4d.java.communication.connection.tcp.TCPConnection;
import org.ws4d.java.communication.monitor.MonitorStreamFactory;
import org.ws4d.java.communication.monitor.MonitoringContext;
import org.ws4d.java.communication.protocol.http.header.HTTPRequestHeader;
import org.ws4d.java.communication.protocol.http.header.HTTPResponseHeader;
import org.ws4d.java.communication.protocol.http.requests.DefaultHTTPGetRequest;
import org.ws4d.java.configuration.DPWSProperties;
import org.ws4d.java.configuration.HTTPProperties;
import org.ws4d.java.constants.HTTPConstants;
import org.ws4d.java.message.Message;
import org.ws4d.java.structures.HashMap;
import org.ws4d.java.structures.Iterator;
import org.ws4d.java.structures.LinkedList;
import org.ws4d.java.structures.Queue;
import org.ws4d.java.types.InternetMediaType;
import org.ws4d.java.util.Log;
import org.ws4d.java.util.Sync;
import org.ws4d.java.util.TimedEntry;
import org.ws4d.java.util.WatchDog;

/**
 * Client for asynchronous HTTP communication.
 * <p>
 * This client allows the asynchronous communication over HTTP.
 * </p>
 * <h3>Example</h3>
 * <p>
 * HTTPClient client = HTTPClient.create("http://127.0.0.1:8080/hello");<br />
 * // access http://127.0.0.1:8080/hello<br />
 * client.exchange();<br />
 * // access http://127.0.0.1:8080/test<br />
 * client.exchange("/test");<br />
 * // access http://127.0.0.1:8080/test with a user defined HTTP GET request<br />
 * HTTPRequest request = new DefaultHTTPGetRequest(); client.exchange(request);<br />
 * // Close the communication<br />
 * client.close();
 * </p>
 * 
 * @see HTTPRequest
 */
public class HTTPClient extends TimedEntry {

	public static int					MAX_CLIENT_CONNECTIONS	= HTTPProperties.getInstance().getMaxConnections();

	/**
	 * Indicates whether this client is closed or not.
	 */
	private boolean						closed					= false;

	/**
	 * The underlying simple HTTP client which allows the HTTP communication.
	 */
	private SimpleHTTPClient			simpleHTTPClient		= null;

	/**
	 * Thread which handles queued requests.
	 */
	private AsyncRequesterWriter		requester				= null;

	/**
	 * Thread which handles incoming responses.
	 */
	private AsyncResponseReader			responder				= null;

	/**
	 * Map of registered handlers which handles the incoming response.
	 */
	private HashMap						handlers				= new HashMap();

	/**
	 * Keep-alive mode (get first mode from framework).
	 */
	private boolean						keepalive				= true;

	/**
	 * Flag for the asynchronous writer. Indicates that the writer is done.
	 */
	private boolean						writerReady				= false;

	/**
	 * Flag for the asynchronous reader. Indicates that the reader is done.
	 */
	private boolean						readerReady				= false;

	private Queue						pendingRequests			= null;

	/**
	 * Timeout limit for the watch dog.
	 */
	private static long					REQUEST_TIMEOUT			= 5000;

	/**
	 * 
	 */
	private static boolean				multipleCons			= true;

	/**
	 * Table of HTTP clients.
	 */
	private static HashMap				allClients				= new HashMap();

	/**
	 * Table of HTTP clients which can be reused for requests.
	 */
	private static HashMap				freeClients				= new HashMap();

	/**
	 * The thread pool for the threads started by this client.
	 */
	private static final DPWSProperties	properties				= DPWSProperties.getInstance();

	/**
	 * Kill all existing and provided HTTP clients with the {@link #kill()}
	 * method.
	 */
	public synchronized static void killAllClients() {
		Iterator it = allClients.values().iterator();
		while (it.hasNext()) {
			LinkedList l = (LinkedList) it.next();
			if (l != null) {
				Iterator jt = l.iterator();
				while (jt.hasNext()) {
					HTTPClient hc = (HTTPClient) jt.next();
					hc.kill(false);
					jt.remove();
				}
			}
			it.remove();
		}
	}

	/**
	 * Close all existing and provided HTTP clients with the {@link #close()}
	 * method.
	 */
	public synchronized static void closeAllClients() {
		Iterator it = allClients.values().iterator();
		while (it.hasNext()) {
			LinkedList l = (LinkedList) it.next();
			if (l != null) {
				Iterator jt = l.iterator();
				while (jt.hasNext()) {
					HTTPClient hc = (HTTPClient) jt.next();
					hc.close(false);
					jt.remove();
				}
			}
			it.remove();
		}
	}

	/**
	 * Adds a HTTP client to the list of existing and provided clients.
	 * 
	 * @param client the client to add.
	 * @return the client which was added.
	 */
	private static HTTPClient addClient(HTTPClient client) {
		LinkedList c = (LinkedList) allClients.get(client.simpleHTTPClient.getDestination());
		if (c == null) {
			c = new LinkedList();
			allClients.put(client.simpleHTTPClient.getDestination(), c);
		}
		c.add(client);
		return client;
	}

	/**
	 * Removes a HTTP client from the list of existing and provided clients.
	 * 
	 * @param client to remove.
	 */
	private static void removeClient(HTTPClient client) {
		LinkedList c = (LinkedList) allClients.get(client.simpleHTTPClient.getDestination());
		c.remove(client);
		if (c.size() == 0) {
			allClients.remove(client.simpleHTTPClient.getDestination());
		}
	}

	/**
	 * Adds a HTTP client to the list of free clients.
	 * 
	 * @param client the client to add.
	 */
	private synchronized static void addFreeClient(HTTPClient client) {
		HTTPRequest request = client.getPendingRequest();
		if (request != null) {
			client.requester.setRequest(request);
			return;
		}
		synchronized (freeClients) {
			LinkedList frees = (LinkedList) freeClients.get(client.simpleHTTPClient.getDestination());
			if (frees == null) {
				frees = new LinkedList();
				frees.addFirst(client);
				freeClients.put(client.simpleHTTPClient.getDestination(), frees);
			} else {
				frees.addFirst(client);
			}
			WatchDog.getInstance().register(client, REQUEST_TIMEOUT);
		}
	}

	/**
	 * Removes a HTTP client from the list of free clients.
	 * 
	 * @param client the client to remove.
	 */
	private synchronized static boolean removeFreeClient(HTTPClient client) {
		boolean result = false;
		synchronized (freeClients) {
			WatchDog.getInstance().unregister(client);
			LinkedList frees = (LinkedList) freeClients.get(client.simpleHTTPClient.getDestination());
			if (frees != null) {
				result = frees.remove(client);
				if (frees.size() == 0) {
					freeClients.remove(client.simpleHTTPClient.getDestination());
				}
			}
		}
		return result;
	}

	/**
	 * Creates HTTP client based on host and port.
	 * 
	 * @param host the host address to connect to.
	 * @param port the port on the host.
	 */
	private HTTPClient(HTTPClientDestination dest) {
		simpleHTTPClient = new SimpleHTTPClient(dest);
		keepalive = properties.getHTTPClientKeepAlive();
		responder = new AsyncResponseReader(this);
		requester = new AsyncRequesterWriter(this);
	}

	/**
	 * Registers a handler with given Internet media type which will handle
	 * incoming HTTP responses.
	 * <p>
	 * This {@link HTTPResponseHandler} will ONLY BE USED if NO handler is
	 * returned by the {@link HTTPRequest#getResponseHandler(InternetMediaType)}
	 * method.
	 * </p>
	 * 
	 * @param type the Internet media type.
	 * @param handler the handler which will handle the HTTP response.
	 * @see HTTPRequest
	 */
	public void register(InternetMediaType type, HTTPResponseHandler handler) {
		handlers.put(type, handler);
	}

	/**
	 * Sends a simple HTTP GET request to the host, defined by the
	 * <code>create</code> method.
	 * <p>
	 * The request is not actually sent instantaneously to the host. It is put
	 * into a request queue and will be started as soon as possible. The speed
	 * depends on the thread scheduler and the Object.notifiy() method.
	 * </p>
	 */
	public synchronized static void exchange(HTTPClientDestination dest) {
		exchange(dest, "/");
	}

	/**
	 * Sends a simple HTTP GET request with given request path to the host,
	 * defined by the <code>create</code> method.
	 * <p>
	 * The request is not actually sent instantaneously to the host. It is put
	 * into a request queue and will be started as soon as possible. The speed
	 * depends on the thread scheduler and the Object.notifiy() method.
	 * </p>
	 * 
	 * @param request the HTTP request path.
	 */
	public synchronized static void exchange(HTTPClientDestination dest, String request) {
		exchange(dest, new DefaultHTTPGetRequest(request));
	}

	/**
	 * Sends a HTTP request (with the path defined in the HTTP header inside the
	 * request) to the host, defined by the <code>create</code> method.
	 * <p>
	 * The request is not actually sent instantaneously to the host. It is put
	 * into a request queue and will be started as soon as possible. The speed
	 * depends on the thread scheduler and the Object.notifiy() method..
	 * </p>
	 * 
	 * @param request the HTTP request.
	 */
	public synchronized static void exchange(HTTPClientDestination dest, HTTPRequest request) {
		addRequest(dest, request);
	}

	/**
	 * Returns the preset HTTP path for the request.
	 * <p>
	 * This request is used as default for the {@link #exchange()} method.
	 * </p>
	 * 
	 * @return the HTTP path.
	 */
	public String getPresetRequest() {
		return simpleHTTPClient.getPresetRequest();
	}

	/**
	 * Returns the TCP connection for this HTTP client.
	 * 
	 * @return the TCP connection
	 * @see TCPConnection
	 */
	public TCPConnection getTCPConnection() {
		if (simpleHTTPClient != null) {
			TCPClient tcpClient = simpleHTTPClient.getTCPClient();
			if (tcpClient != null) {
				return tcpClient.getConnection();
			}
		}
		return null;
	}

	/**
	 * Closes the connection with the server.
	 * <p>
	 * This will stop the response and request threads before the connection is
	 * closed. The client will try to send the queued requests and handle the
	 * incoming responses before the client is closed.
	 * </p>
	 * <p>
	 * If the client should be closed immediately use the {@link #kill()}
	 * method.
	 * </p>
	 */
	public synchronized void close() {
		close(true);
	}

	private void close(boolean remove) {
		if (closed) return;
		removeFreeClient(this);
		closed = true;
		requester.stop();
		responder.stop();
		try {
			simpleHTTPClient.close();
		} catch (IOException e) {
			Log.error("Cannot close client connection. " + e.getMessage());
		}
		if (remove) {
			removeClient(this);
		}
	}

	/**
	 * Closes the connection with the server immediately!!!
	 * <p>
	 * Existing connections will be closed regardless of which thread wants to
	 * read the streams. This will stop the response and request threads before
	 * the connection is closed.
	 * </p>
	 * <p>
	 * If the connection should be closed without killing the connections the
	 * {@link #close()} method should be used.
	 * </p>
	 */
	public synchronized void kill() {
		kill(true);
	}

	private void kill(boolean remove) {
		if (closed) return;
		removeFreeClient(this);
		closed = true;
		/*
		 * Close the internal client before killing threads. This will close all
		 * connections.
		 */
		try {
			simpleHTTPClient.close();
		} catch (IOException e) {
			Log.error("Cannot close client connection. " + e.getMessage());
		}
		requester.stop();
		responder.kill();
		if (remove) {
			removeClient(this);
		}
	}

	/**
	 * Returns <code>true</code> if the client is closed and cannot be used for
	 * a request or <code>false</code> if the client can still be used.
	 * 
	 * @return <code>true</code> if the client is closed and cannot be used for
	 *         a request or <code>false</code> if the client can still be used.
	 */
	public synchronized boolean isClosed() {
		return closed;
	}

	/**
	 * Eats the omitted bytes.
	 * 
	 * @return the amount of bytes eaten.
	 * @throws IOException
	 */
	private int eat(InputStream body) throws IOException {
		if (body == null) return 0;
		int n = 0;
		while (body.read() != -1) {
			n++;
		}
		return n;
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.management.TimedEntry#timedOut()
	 */
	protected void timedOut() {
		if (Log.isDebug()) {
			TCPConnection connection = getTCPConnection();
			if (connection != null) {
				Log.debug("HTTP client timeout: " + connection.getProtocolData(), Log.DEBUG_LAYER_COMMUNICATION);
			} else {
				Log.debug("HTTP client timeout, no connection data available.", Log.DEBUG_LAYER_COMMUNICATION);
			}
		}

		if (!removeFreeClient(this)) return;

		close();
	}

	private synchronized void writerReady() {
		if (readerReady) {
			if (keepalive && requester.running && responder.running) {
				readerReady = false;
				addFreeClient(this);
			} else {
				closeAndProcessPendingRequest();
			}
		} else {
			writerReady = true;
		}
	}

	private synchronized void readerReady() {
		if (writerReady) {
			if (keepalive && requester.running && responder.running) {
				writerReady = false;
				addFreeClient(this);
			} else {
				closeAndProcessPendingRequest();
			}
		} else {
			readerReady = true;
		}
	}

	private static void addRequest(HTTPClientDestination dest, HTTPRequest request) {
		/*
		 * Check for HTTP client.
		 */
		HTTPClient hc = null;
		synchronized (freeClients) {
			LinkedList frees = (LinkedList) freeClients.get(dest);
			if (frees == null) {
				LinkedList c = (LinkedList) allClients.get(dest);
				int cons = (c == null ? 0 : c.size());
				if (!multipleCons) {
					if (c != null) {
						hc = (HTTPClient) c.getFirst();
						hc.queueRequest(request);
						return;
					}
				} else if (cons >= dest.getMaxConnections()) {
					if (c != null) {
						hc = (HTTPClient) c.getFirst();
						hc.queueRequest(request);
						return;
					}
				}
				hc = new HTTPClient(dest);
				addClient(hc);
			} else {
				hc = (HTTPClient) frees.removeFirst();
				WatchDog.getInstance().unregister(hc);
				if (frees.size() == 0) {
					freeClients.remove(dest);
				}
			}
		}

		/*
		 * Do not queue any requests if the client is already closed.
		 */
		if (hc.isClosed()) {
			throw new RuntimeException("Cannot send request. HTTP client closed.");
		}
		hc.requester.setRequest(request);
	}

	private void queueRequest(HTTPRequest request) {
		if (pendingRequests == null) {
			pendingRequests = new Queue();
		}
		pendingRequests.enqueue(request);
	}

	private HTTPRequest getPendingRequest() {
		if (pendingRequests == null) {
			return null;
		}
		return (HTTPRequest) pendingRequests.get();
	}

	private void closeAndProcessPendingRequest() {
		close();
		HTTPRequest request = getPendingRequest();
		if (request != null) {
			HTTPClientDestination dest = simpleHTTPClient.getDestination();
			HTTPClient hc = new HTTPClient(dest);
			addClient(hc);
			hc.pendingRequests = pendingRequests;
			hc.requester.setRequest(request);
		}
	}

	/**
	 * This threads waits until it is allowed to read blocks from the input
	 * stream. It is synchronized with the {@link AsyncRequesterWriter}.
	 */
	private class AsyncResponseReader implements Runnable {

		/**
		 * Indicates whether this thread should work or not.
		 */
		private volatile boolean		running			= true;

		/**
		 * This entry will be set by the {@link AsyncRequesterWriter} which
		 * allows this reader to know every thing about the request.
		 * <p>
		 */
		private volatile HTTPRequest	request			= null;

		/**
		 * This object is used to wait until a request is made.
		 */
		private Object		waitForRequest	= new Object();

		/**
		 * This object is used to wait until the whole response was read before
		 * stopping the thread.
		 */
		private Object		lockResponse	= new Object();

		/**
		 * Reference of the outer class.
		 */
		private HTTPClient	client			= null;

		/**
		 * Creates a reader and starts it as thread.
		 */
		AsyncResponseReader(HTTPClient client) {
			this.client = client;
			DPWSFramework.getThreadPool().execute(this);
		}

		/**
		 * This method is invoked by the {@link AsyncRequesterWriter} to notify
		 * it of a request.
		 * <p>
		 * This will put this thread into a blocking read on the input stream.
		 * </p>
		 * 
		 * @param entry This entry contains every interesting information about
		 *            the request made.
		 */
		public void notifyAboutRequest(HTTPRequest request) {
			synchronized (waitForRequest) {
				this.request = request;
				waitForRequest.notifyAll();
			}

		}

		public void justNotify() {
			synchronized (waitForRequest) {
				waitForRequest.notifyAll();
			}
		}

		/*
		 * (non-Javadoc)
		 * @see java.lang.Runnable#run()
		 */
		public void run() {
			/*
			 * response
			 */
			try {
				/*
				 * Should we work? ;-)
				 */
				RUNNING: while (running) {
					ProtocolData pd = null;

					/*
					 * Wait until the request sender notifies us.
					 */
					synchronized (waitForRequest) {
						while (request == null) {
							waitForRequest.wait(500);
							/*
							 * Check for "stop". Maybe we should not continue
							 * sending. Check for keep alive too...
							 */
							if (!running) {
								if (request != null) {
									throw new RuntimeException("HTTP response was not handled. HTTP reader not running.");
								}

								break RUNNING;
							}
						}

					}

					MonitorStreamFactory monFac = DPWSFramework.getMonitorStreamFactory();

					/*
					 * Try to read the response. This will block on the input
					 * stream.
					 */
					synchronized (lockResponse) {
						try {
							Sync streamLock = new Sync();

							TCPClient c = simpleHTTPClient.getTCPClient();
							TCPConnection connection = c.getConnection();
							pd = connection.getProtocolData().createSwappedProtocolData();

							MonitoringContext context = null;

							if (monFac != null) {
								context = monFac.getNewMonitoringContextIn(pd);
							}

							HTTPResponseHeader response = simpleHTTPClient.getResponseHeader();

							InputStream in = simpleHTTPClient.getResponseBody(streamLock);

							if (Log.isDebug()) {
								Log.debug("<I> " + response + " from " + pd.getDestinationAddress() + ", " + connection, Log.DEBUG_LAYER_COMMUNICATION);
							}

							String encoding = response.getHeaderFieldValue(HTTPConstants.HTTP_HEADER_TRANSFER_ENCODING);
							int contentLength = (response.getHeaderFieldValue(HTTPConstants.HTTP_HEADER_CONTENT_LENGTH) != null) ? Integer.parseInt(response.getHeaderFieldValue(HTTPConstants.HTTP_HEADER_CONTENT_LENGTH).trim()) : -1;
							String contenttype = response.getHeaderFieldValue(HTTPConstants.HTTP_HEADER_CONTENT_TYPE);
							InternetMediaType mediaType = new InternetMediaType(contenttype);

							String con = response.getHeaderFieldValue(HTTPConstants.HTTP_HEADER_CONNECTION);
							if (HTTPConstants.HTTP_HEADERVALUE_CONNECTION_CLOSE.equals(con)) {
								/*
								 * The server wishes to close the connection
								 * after the response is done.
								 */
								keepalive = false;
								requester.notifyKeepAliveDisabled();
							}

							/*
							 * Check for response handler belonging to the
							 * request.
							 */
							HTTPResponseHandler handler = request.getResponseHandler(mediaType);

							/*
							 * No handler found inside the request? Check the
							 * internal table.
							 */
							if (handler == null) {
								handler = (HTTPResponseHandler) handlers.get(mediaType);
							}

							/*
							 * If no handler was found, the consumer thread will
							 * be started and will finish after eating all
							 * omitted bytes. This should NOT happen. A client
							 * should not start a request without a handler
							 * which can handle the incoming response.
							 */

							if ((HTTPConstants.HTTP_HEADERVALUE_TRANSFERCODING_CHUNKED.equals(encoding) || contentLength > 0)) {
								/*
								 * HTTP response contains content, read it.
								 */
								StreamConsumerThread consumer = new StreamConsumerThread(handler, response, in, request, context);

								/*
								 * Wait until the current response is fully
								 * read.
								 */
								streamLock.reset();
								synchronized (streamLock) {
									while (!streamLock.isNotified()) {
										try {
											DPWSFramework.getThreadPool().execute(consumer);
											streamLock.wait();
										} catch (InterruptedException e) {
											streamLock.notifyNow();
										}
									}
								}
								Exception e = streamLock.getException();
								if (e != null) {
									if (e instanceof IOException) {
										throw (IOException) e;
									} else {
										Log.error("A problem occured during stream read. " + e.getMessage());
									}
								}
							} else {
								/*
								 * This response has no HTTP body, we will pass
								 * "null" to the handler.
								 */
								StreamConsumerThread consumer = new StreamConsumerThread(handler, response, null, request, context);
								DPWSFramework.getThreadPool().execute(consumer);

							}

						} catch (IOException e) {
							if (!closed) {
								/*
								 * We cannot handle response?
								 */
								Log.error("Cannot handle HTTP response. " + e.getMessage());
								ExceptionNotification eNotification = new ExceptionNotification(pd, request, e, true);
								eNotification.start();
							}
						}

						request = null;

					}

					if (!keepalive) {
						break;
					}

					client.readerReady();
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			running = false;
			client.readerReady();
		}

		/**
		 * Stops the response reader as soon as possible.
		 * <p>
		 * This method will wait to handle the response. If it is necessary to
		 * stop the reader immediately the {@link #kill()} method should be
		 * used.
		 * </p>
		 */
		public void stop() {
			if (running == false) return;
			synchronized (lockResponse) {
				running = false;
			}
			synchronized (waitForRequest) {
				waitForRequest.notifyAll();
			}
		}

		/**
		 * Kill the response reader immediately.
		 */
		public void kill() {
			if (running == false) return;
			running = false;
			synchronized (waitForRequest) {
				waitForRequest.notifyAll();
			}
		}

	}

	/**
	 * This thread is used to queue the HTTP client requests.
	 */
	private class AsyncRequesterWriter implements Runnable {

		/**
		 * The request which should be send.
		 */
		private volatile HTTPRequest	request		= null;

		/**
		 * Indicates whether this thread should work or not.
		 */
		private volatile boolean		running		= true;

		/**
		 * This object is used to wait until the whole request was sent before
		 * stopping the thread.
		 */
		private Object		lockRequest	= new Object();

		/**
		 * The parent asynchronous HTTP client.
		 */
		private HTTPClient	client		= null;

		/**
		 * Creates a writer and starts it as thread.
		 */
		AsyncRequesterWriter(HTTPClient client) {
			this.client = client;
			DPWSFramework.getThreadPool().execute(this);
		}

		/**
		 * Sets an HTTP request.
		 * 
		 * @param request the HTTP request.
		 */
		public synchronized void setRequest(HTTPRequest request) {
			synchronized (lockRequest) {
				this.request = request;
				lockRequest.notifyAll();
			}
		}

		public void notifyKeepAliveDisabled() {
			running = false;
		}

		/*
		 * (non-Javadoc)
		 * @see java.lang.Runnable#run()
		 */
		public void run() {
			try {
				RUNNING: while (running) {
					/*
					 * Wait until a element is queued.
					 */
					ProtocolData pd = null;

					boolean gotException = false;

					/*
					 * Try to send the request
					 */
					synchronized (lockRequest) {

						while (request == null) {
							lockRequest.wait(500);
							/*
							 * Check for "stop". Maybe we should not continue
							 * sending. Check for keep alive too...
							 */
							if (!running) {
								/*
								 * Unregister watch dog if leaving here.
								 */
								if (request != null) {
									throw new RuntimeException("HTTP request was not send. HTTP writer was not running.");
								}

								break RUNNING;
							}
						}

						MonitorStreamFactory monFac = DPWSFramework.getMonitorStreamFactory();

						try {

							HTTPRequestHeader header = request.getRequestHeader();
							/*
							 * Open the connection if necessary. Notify the
							 * reader about the request. Send the request.
							 */
							simpleHTTPClient.explicitConnect();

							responder.notifyAboutRequest(request);

							TCPClient c = simpleHTTPClient.getTCPClient();
							TCPConnection con = c.getConnection();
							pd = con.getProtocolData();

							if (Log.isDebug()) {
								Log.debug("<O> " + header + " to " + pd.getDestinationAddress() + ", " + con, Log.DEBUG_LAYER_COMMUNICATION);
							}

							MonitoringContext context = null;

							if (monFac != null) {
								context = monFac.getNewMonitoringContextOut(pd);
							}

							String enc = header.getHeaderFieldValue(HTTPConstants.HTTP_HEADER_TRANSFER_ENCODING);

							int contentLength = (header.getHeaderFieldValue(HTTPConstants.HTTP_HEADER_CONTENT_LENGTH) != null) ? Integer.parseInt(header.getHeaderFieldValue(HTTPConstants.HTTP_HEADER_CONTENT_LENGTH).trim()) : -1;

							OutputStream requestBody = null;

							/*
							 * No HTTP Content-Length set and it is not
							 * chunked?! We MUST calculate the content length to
							 * send a correct HTTP message. WILL NOT WORK FOR
							 * STREAMS!
							 */
							if (!HTTPConstants.HTTP_HEADERVALUE_TRANSFERCODING_CHUNKED.equals(enc) && contentLength == -1) {
								header.removeHeaderFieldValue(HTTPConstants.HTTP_HEADER_TRANSFER_ENCODING);
								enc = null;
								requestBody = simpleHTTPClient.exchange(header, false);
								ByteArrayOutputStream buffer = new ByteArrayOutputStream();
								request.serializeRequestBody(buffer, pd, context); // TODO
								int size = buffer.size();
								((HTTPOutputStream) requestBody).setLength(size);
								header.addHeaderFieldValue(HTTPConstants.HTTP_HEADER_CONTENT_LENGTH, Integer.toString(size));

								simpleHTTPClient.sendHeader();

								byte[] b = buffer.toByteArray();
								requestBody.write(b);
								requestBody.flush();
							} else {
								requestBody = simpleHTTPClient.exchange(header, true);
								request.serializeRequestBody(requestBody, pd, context); // TODO
							}

							/*
							 * Was chunked? Write last chunk.
							 */
							if (HTTPConstants.HTTP_HEADERVALUE_TRANSFERCODING_CHUNKED.equals(enc)) {
								ChunkedOutputStream.writeLastChunk((ChunkedOutputStream) requestBody);
								requestBody.flush();
							}

							if (monFac != null) {
								Message m = context.getMessage();
								if (m != null) {
									monFac.send(pd, context, m);
								}
								
								monFac.resetMonitoringContextOut(pd);
							}
						} catch (IOException e) {
							gotException = true;
							if (!closed) {
								Log.error("Cannot send HTTP request. " + e.getMessage() + ". Resetting TCP connection (" + ((pd == null) ? "no address known" : pd.toString()) + ").");
								simpleHTTPClient.resetConnection();
								ExceptionNotification eNotification = new ExceptionNotification(pd, request, e, false);
								eNotification.start();
							}
						}

						request = null;
					}

					if (!keepalive) {
						break;
					}

					client.writerReady();
					if (gotException) {
						/*
						 * the reader may not receive any response for the
						 * current request; in consequence, it will not call
						 * readerReady() itself and this HTTPClient will not be
						 * cleaned-up by watch dog. that's why we do this from
						 * here
						 */
						readerReady();
					}
				}

			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			running = false;
			client.writerReady();
		}

		/**
		 * Stops the request writer as soon as possible.
		 * <p>
		 * This method allows the writer to complete queued requests. If it is
		 * necessary to kill the writer immediately the {@link #kill()} method
		 * should be used.
		 * </p>
		 */
		public void stop() {
			if (running == false) return;
			running = false;
			synchronized (lockRequest) {
				/*
				 * wait until request is done
				 */
				request = null;
				lockRequest.notifyAll();
			}

			// Notify the reader
			responder.justNotify();
		}

	}

	/**
	 * This thread allows the handling of an incoming response independently
	 * from the thread handling the persistent HTTP connection.
	 */
	private class StreamConsumerThread implements Runnable {

		private HTTPResponseHandler		handler	= null;

		private HTTPResponseHeader		header	= null;

		private InputStream				body	= null;

		private HTTPRequest				request	= null;

		private final MonitoringContext	context;

		StreamConsumerThread(HTTPResponseHandler handler, HTTPResponseHeader header, InputStream body, HTTPRequest request, MonitoringContext context) {
			this.handler = handler;
			this.header = header;
			this.body = body;
			this.request = request;
			this.context = context;
		}

		public void run() {
			
			if (handler != null) {
				try {
					handler.handle(header, body, request, simpleHTTPClient.getTCPClient().getConnection().getProtocolData(), context);
				} catch (IOException e) {
					try {
						int n = eat(body);
						if (n > 0) {
							Log.warn("The registered handler has not consumed the HTTP body from the response because of an exception. Eating " + n + " bytes. Exception was: " + e.getMessage());
						}
					} catch (IOException e1) {
						Log.error("Could not consume omitted bytes from HTTP response. " + e1.getMessage());
					}
				}
				try {
					int n = eat(body);
					if (n > 0) {
						Log.warn("The registered handler has not consumed the HTTP body from the response. Eating " + n + " bytes.");
					}
				} catch (IOException e) {
					Log.error("Could not consume omitted bytes from HTTP response. " + e.getMessage());
				}
			} else {
				/*
				 * We MUST eat the omitted bytes. Why? Because we started a
				 * request and nobody wants the response!? Thats should not
				 * happen!
				 */
				try {
					int n = eat(body);
					if (n > 0) {
						Log.warn("No registered handler was found to consume the HTTP body from the response. Eating " + n + " bytes.");
						header.toStream(System.err);
					}
				} catch (IOException e1) {
					Log.error("Could not consume omitted bytes from HTTP response. " + e1.getMessage());
				}
			}
			MonitorStreamFactory monFac = DPWSFramework.getMonitorStreamFactory();
			if (monFac != null) {
				monFac.resetMonitoringContextIn(context.getProtocolData());
			}
		}
	}

	/**
	 * This thread allows exception handling without blocking the HTTP request
	 * queue.
	 */
	private class ExceptionNotification implements Runnable {

		private HTTPRequest		request		= null;

		private Exception		e			= null;

		private boolean			response	= false;

		private ProtocolData	pd			= null;

		ExceptionNotification(ProtocolData pd, HTTPRequest request, Exception e, boolean response) {
			this.request = request;
			this.e = e;
			this.response = response;
			this.pd = pd;
		}

		public void run() {
			if (request != null && e != null) {
				if (response) {
					request.responseReceiveFailed(e, pd);
				} else {
					request.requestSendFailed(e, pd);
				}
			}

		}

		public void start() {
			DPWSFramework.getThreadPool().execute(this);
		}
	}

}
