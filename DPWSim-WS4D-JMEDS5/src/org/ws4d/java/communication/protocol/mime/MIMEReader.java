/*******************************************************************************
 * Copyright (c) 2009 MATERNA Information & Communications. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html. For further
 * project-related information visit http://www.ws4d.org. The most recent
 * version of the JMEDS framework can be obtained from
 * http://sourceforge.net/projects/ws4d-javame.
 ******************************************************************************/
package org.ws4d.java.communication.protocol.mime;

import java.io.IOException;
import java.io.InputStream;

import org.ws4d.java.communication.ProtocolException;
import org.ws4d.java.communication.protocol.http.HTTPUtil;
import org.ws4d.java.constants.MIMEConstants;
import org.ws4d.java.constants.Specialchars;
import org.ws4d.java.structures.HashMap;
import org.ws4d.java.util.Log;
import org.ws4d.java.util.MIMEUtil;
import org.ws4d.java.util.Search;
import org.ws4d.java.util.Sync;

/**
 * The <code>MIMEReader</code> reads the MIME from a given input stream.
 */
public class MIMEReader {

	// search stuff
	private byte[]					boundaryBytes				= null;
	
	private int[]					boundaryFaultFunction		= null;

	private int						part						= 0;

	// predefined exception messages.
	protected static final String	FAULT_UNEXPECTED_END		= "Unexpected end of stream.";

	protected static final String	FAULT_MALFORMED_HEADERFIELD	= "Malformed MIME header field.";

	protected static final String	FAULT_NOT_FINISHED			= "Previous part not finished.";

	// Parent processor stuff
	protected InputStream			in							= null;

	protected MIMEInputStream		mimeIn						= null;

	// header
	private MIMEBodyHeader			header						= null;

	private Sync					incomingMIMEPartLock		= null;

	private volatile boolean		finished					= false;

	public MIMEReader(InputStream in, byte[] boundary) throws IOException {
		this(in, boundary, null);
	}

	public MIMEReader(InputStream in, byte[] boundary, Sync incomingMIMEPartLock) throws IOException {
		/*
		 * gets rid of the first boundary!!!
		 */
		MIMEUtil.readBoundary(in, boundary);
		this.in = in;
		
		boundaryBytes = new byte[boundary.length + 2];
		System.arraycopy(boundary, 0, boundaryBytes, 2, boundary.length);
		// insert hyphen characters.
		boundaryBytes[0] = MIMEConstants.BOUNDARY_HYPHEN;
		boundaryBytes[1] = MIMEConstants.BOUNDARY_HYPHEN;

		boundaryFaultFunction = Search.createFaultFunction(boundaryBytes);
		
		try {
			readMIMEPartHeader();
		} catch (ProtocolException e) {
			Log.error("Cannot read first MIME header. " + e.getMessage());
		}
		this.incomingMIMEPartLock = incomingMIMEPartLock;
		part = 1;
	}

	/**
	 * Returns <code>true</code> if there is another part (next
	 * <code>InputStream</code>) to read. This method throws a
	 * <code>IOExcpetion</code> if someone tries to invoke this method before
	 * the previous part is read completely or if another I/O error occurs.
	 * 
	 * @return <code>true</code> if there is another part. <code>false</code>
	 *         otherwise.
	 */
	public synchronized boolean nextPart() throws IOException {
		try {
			if (mimeIn == null && part > 1) {
				finished = true;
				notifyAll();
				return false;
			}
			if (mimeIn == null) {
				mimeIn = new MIMEInputStream();
				return true;
			}
			if (mimeIn.isInUse() && !mimeIn.isClosed()) throw new IOException(FAULT_NOT_FINISHED);
			int i = in.read();
			if (i == Specialchars.CR) {
				i = in.read();
				if (i == Specialchars.LF) {
					try {
						readMIMEPartHeader();
					} catch (ProtocolException e) {
						Log.error("Cannot read MIME header. " + e.getMessage());
						finished = true;
						notifyAll();
						return false;
					}
					mimeIn = new MIMEInputStream();
					part++;
					return true;
				}
			} else if (i == MIMEConstants.BOUNDARY_HYPHEN) {
				i = in.read();
				if (i == MIMEConstants.BOUNDARY_HYPHEN) {
					// the MIME ends here!
					HTTPUtil.readRequestLine(in);
					finished = true;
					notifyAll();
					return false;
				}
			}
			finished = true;
			notifyAll();
			return false;
		} catch (IOException ioe) {
			if (!FAULT_NOT_FINISHED.equals(ioe.getMessage())) {
				finished = true;
				notifyAll();
			}
			throw ioe;
		}
	}

	/**
	 * Returns the number of the current part.
	 * 
	 * @return the number of the current part.
	 */
	public int getPartNumber() {
		return part;
	}

	/**
	 * Returns the MIME body header.
	 * 
	 * @return the MIME body header.
	 */
	public MIMEBodyHeader getMIMEBodyHeader() {
		return header;
	}

	/**
	 * Returns the input stream for this processor. Because this is a multipart
	 * processor, this method will return different streams after the use of
	 * <code>nextPart()</code>.
	 * 
	 * @return the <code>InputStream</code>.
	 */
	public InputStream getInputStream() {
		if (mimeIn == null) {
			mimeIn = new MIMEInputStream();
		}
		return mimeIn;
	}

	public synchronized void waitFor() {
		while (!finished) {
			try {
				wait();
			} catch (InterruptedException e) {
				// void
			}
		}

	}

	/**
	 * Reads the header field of the MIME part.
	 */
	private synchronized void readMIMEPartHeader() throws IOException, ProtocolException {
		HashMap headerfields = new HashMap();
		MIMEUtil.readHeaderFields(in, headerfields);
		header = new MIMEBodyHeader(headerfields);
	}

	/**
	 * MIME input stream wrapper. This class wraps the input stream and controls
	 * the length of the data read.
	 */
	private class MIMEInputStream extends InputStream {

		private InputStream	inKMP	= null;

		private boolean		closed	= false;

		private boolean		read	= false;

		public MIMEInputStream() {
			inKMP = Search.getSearchPatternWrapper(in, boundaryBytes, boundaryFaultFunction);
		}

		/*
		 * (non-Javadoc)
		 * @see java.io.InputStream#available()
		 */
		public int available() throws IOException {
			try {
				return inKMP.available();
			} catch (IOException e) {
				if (incomingMIMEPartLock != null) {
					synchronized (incomingMIMEPartLock) {
						incomingMIMEPartLock.notifyNow(e);
					}
				}
				throw e;
			}
		}

		/*
		 * (non-Javadoc)
		 * @see java.io.InputStream#read()
		 */
		public int read() throws IOException {
			try {
				if (closed) return -1;
				read = true;
				int i = inKMP.read();
				if (i == -1) {
					if (incomingMIMEPartLock != null) {
						synchronized (incomingMIMEPartLock) {
							closed = true;
							incomingMIMEPartLock.notifyNow();
						}
					} else {
						closed = true;
					}
				}
				return i;
			} catch (IOException e) {
				if (incomingMIMEPartLock != null) {
					synchronized (incomingMIMEPartLock) {
						incomingMIMEPartLock.notifyNow(e);
					}
				}
				throw e;
			}
		}

		/*
		 * (non-Javadoc)
		 * @see java.io.InputStream#close()
		 */
		public void close() throws IOException {
			try {
				if (incomingMIMEPartLock != null) {
					synchronized (incomingMIMEPartLock) {
						incomingMIMEPartLock.notifyNow();
					}
				}
				inKMP.close();
			} catch (IOException e) {
				if (incomingMIMEPartLock != null) {
					synchronized (incomingMIMEPartLock) {
						incomingMIMEPartLock.notifyNow(e);
					}
				}
				throw e;
			}

		}

		/**
		 * Returns <code>true</code> if someone is already reading from this
		 * stream, <code>false</code> otherwise.
		 * 
		 * @return <code>true</code> if someone is already reading from this
		 *         stream, <code>false</code> otherwise.
		 */
		public boolean isInUse() {
			return read;
		}

		/**
		 * Returns <code>true</code> if the stream is closed, <code>false</code>
		 * otherwise.
		 * 
		 * @return <code>true</code> if the stream is closed, <code>false</code>
		 *         otherwise.
		 */
		public boolean isClosed() {
			return closed;
		}

		/*
		 * (non-Javadoc)
		 * @see java.lang.Object#hashCode()
		 */
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result + ((inKMP == null) ? 0 : inKMP.hashCode());
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
			MIMEInputStream other = (MIMEInputStream) obj;
			if (!getOuterType().equals(other.getOuterType())) return false;
			if (inKMP == null) {
				if (other.inKMP != null) return false;
			} else if (!inKMP.equals(other.inKMP)) return false;
			return true;
		}

		private MIMEReader getOuterType() {
			return MIMEReader.this;
		}
	}

}
