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

import java.io.IOException;
import java.io.InputStream;

import org.ws4d.java.communication.ProtocolException;
import org.ws4d.java.constants.HTTPConstants;
import org.ws4d.java.io.buffered.BufferedInputStream;
import org.ws4d.java.util.Sync;

/**
 * HTTP input stream wrapper. This class wraps the input stream and controls the
 * length of data read.
 */
public class HTTPInputStream extends InputStream {

	private InputStream			in				= null;

	private int					size			= 0;

	private int					read			= 0;

	private String				encoding		= null;

	private boolean				end				= false;

	private InputStream			wrapped			= null;

	protected HTTPChunkHeader	chunkedheader	= null;

	protected boolean			chunked			= false;

	private Sync				notify			= null;

	/**
	 * Creates a HTTP input stream.
	 */
	public HTTPInputStream(InputStream in, String encoding, int size) {
		this(in, encoding, size, null);
	}

	/**
	 * Creates a HTTP input stream with synchronization.
	 */
	public HTTPInputStream(InputStream in, String encoding, int size, Sync notify) {
		this.in = in;
		this.encoding = encoding;
		if (HTTPConstants.HTTP_HEADERVALUE_TRANSFERCODING_CHUNKED.equals(encoding)) {
			chunked = true;
		}
		if (size < 0) {
			this.size = 0;
		} else {
			this.size = size;
		}
		this.notify = notify;
	}

	/*
	 * (non-Javadoc)
	 * @see java.io.InputStream#close()
	 */
	public void close() throws IOException {
		try {
			if (in == null) {
				throw new IOException("InputStream not available");
			}
			if (notify != null) {
				synchronized (notify) {
					notify.notifyNow();
				}
			}
			if (wrapped != null) {
				wrapped.close();
			}
			in.close();
		} catch (IOException e) {
			if (notify != null) {
				synchronized (notify) {
					notify.notifyNow(e);
				}
			}
			throw e;
		}
	}

	/*
	 * (non-Javadoc)
	 * @see java.io.InputStream#read()
	 */
	public synchronized int read() throws IOException {
		try {
			if (in == null) {
				throw new IOException("InputStream not available");
			}
			if (end) {
				if (notify != null) {
					synchronized (notify) {
						notify.notifyNow();
					}
				}
				return -1;
			}

			int k = -1;
			if (!chunked) {
				k = readNonChunked();
			} else {
				k = readChunked();
			}
			if (notify != null && k == -1) {
				synchronized (notify) {
					notify.notifyNow();
				}
			}
			return k;
		} catch (IOException e) {
			if (notify != null) {
				synchronized (notify) {
					notify.notifyNow(e);
				}
			}
			throw e;
		}
	}

	private int readNonChunked() throws IOException {
		/*
		 * HTTP body not chunked
		 */
		if (size > 0) {
			if (wrapped == null) {
				wrapped = wrap(size);
			}
			read++;
			return wrapped.read();
		}
		/*
		 * The size MUST NOT be < 0.
		 */
		return -1;
	}

	private int readChunked() throws IOException {
		/*
		 * HTTP body chunked
		 */
		if (chunkedheader == null) {
			readChunkHeader();
		}

		int chunksize = chunkedheader.getSize();

		if (chunksize == 0) {
			end = true;
			return -1;
		}

		if (wrapped == null) {
			wrapped = wrap(chunksize);
		}
		int k = wrapped.read();

		/*
		 * Chunk done ...
		 */
		if (k == -1) {
			chunkedheader = null;
			wrapped = null;
			read = 0;
			/*
			 * next chunk ...
			 */
			HTTPUtil.readRequestLine(in);
			return readChunked();
		}
		read++;
		return k;
	}

	private void readChunkHeader() throws IOException {
		try {
			chunkedheader = HTTPUtil.readChunkHeader(in);
		} catch (ProtocolException e) {
			chunkedheader = null;
			throw new IOException("Cannot read HTTP chunk header. " + e.getMessage());
		}
	}

	private InputStream wrap(int size) {
		// if (CommunicationConstants.BUFFERED_INPUT) {
		// return new BufferedInputStream(in, size);
		// } else {
		// return new WrappedInputStream(in, size);
		// }
		return new WrappedInputStream(in, size);
	}

	/*
	 * (non-Javadoc)
	 * @see java.io.InputStream#available()
	 */
	public int available() throws IOException {
		try {
			if (end) return 0;
			if (!chunked) {
				return size - read;
			}
			if (chunkedheader == null) {
				// read first chunk header
				readChunkHeader();
			}
			return (chunkedheader.getSize() - read);
		} catch (IOException e) {
			if (notify != null) {
				synchronized (notify) {
					notify.notifyNow(e);
				}
			}
			throw e;
		}
	}

	/**
	 * Returns the encoding for this stream.
	 * 
	 * @return the stream encoding.
	 */
	public String getEncoding() {
		return encoding;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((in == null) ? 0 : in.hashCode());
		return result;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		HTTPInputStream other = (HTTPInputStream) obj;
		if (in == null) {
			if (other.in != null) return false;
		} else if (!in.equals(other.in)) return false;
		return true;
	}

	/**
	 * This input stream handles chunks if no {@link BufferedInputStream} is
	 * used.
	 */
	private class WrappedInputStream extends InputStream {

		private int			size	= 0;

		private int			read	= 0;

		private InputStream	in		= null;

		WrappedInputStream(InputStream in, int size) {
			this.in = in;
			this.size = size;
		}

		/*
		 * (non-Javadoc)
		 * @see java.io.InputStream#available()
		 */
		public int available() throws IOException {
			return read - size;
		}

		public int read() throws IOException {
			if (read == size) return -1;
			read++;
			return in.read();
		}

		private HTTPInputStream getOuterType() {
			return HTTPInputStream.this;
		}

		/*
		 * (non-Javadoc)
		 * @see java.lang.Object#hashCode()
		 */
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result + ((in == null) ? 0 : in.hashCode());
			return result;
		}

		/*
		 * (non-Javadoc)
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		public boolean equals(Object obj) {
			if (this == obj) return true;
			if (obj == null) return false;
			if (getClass() != obj.getClass()) return false;
			WrappedInputStream other = (WrappedInputStream) obj;
			if (!getOuterType().equals(other.getOuterType())) return false;
			if (in == null) {
				if (other.in != null) return false;
			} else if (!in.equals(other.in)) return false;
			return true;
		}

	}

}
