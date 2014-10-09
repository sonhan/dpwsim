/*******************************************************************************
 * Copyright (c) 2009 MATERNA Information & Communications. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html. For further
 * project-related information visit http://www.ws4d.org. The most recent
 * version of the JMEDS framework can be obtained from
 * http://sourceforge.net/projects/ws4d-javame.
 ******************************************************************************/
package org.ws4d.java.io.buffered;

import java.io.IOException;
import java.io.InputStream;

import org.ws4d.java.constants.Specialchars;

/**
 * This class allows the enabling of mark support on an input stream. It is
 * possible to read some bytes from the stream without exhausting it.
 */
public class BufferedInputStream extends InputStream {

	private static final int		DEFAULT_BUFFER_SIZE	= 8192;

	private static final boolean	MARK_SUPPORT		= true;

	private static final boolean	SENDZEROBYTE		= false;

	/* stream */
	private InputStream				in					= null;

	/* mark */
	private int						mark				= -1;

	private int						marked				= -1;

	/* pointer */
	private int						read				= -1;

	private int						last				= -1;

	/* buffer */
	private byte[]					buffer;

	private boolean					markwatch			= false;

	/* timeout stuff */
	private boolean					timeout				= false;

	private boolean					reducedtimeout		= false;

	private int						reducedtries		= 2;

	private int						sleep				= 10;

	private int						tries				= 500;

	/* end */
	private boolean					closed				= false;

	private int						maxsize				= -1;

	private int						rawread				= 0;

	/**
	 * This class allows the enabling of mark support on an input stream. It is
	 * possible to read some bytes from the stream without exhausting it.
	 * 
	 * @param in input stream without mark support.
	 */
	public BufferedInputStream(InputStream in) {
		this(in, DEFAULT_BUFFER_SIZE, -1);
	}

	/**
	 * This class allows the enabling of mark support on an input stream. It is
	 * possible to read some bytes from the stream without exhausting it.
	 * 
	 * @param in input stream without mark support.
	 * @param maxsize do not read more bytes.
	 */
	public BufferedInputStream(InputStream in, int maxsize) {
		this(in, DEFAULT_BUFFER_SIZE, maxsize);
	}

	/**
	 * This class allows the enabling of mark support on an input stream. It is
	 * possible to read some bytes from the stream without exhausting it.
	 * 
	 * @param in input stream without mark support.
	 * @param size buffer size.
	 * @param maxsize do not read more bytes.
	 */
	public BufferedInputStream(InputStream in, int size, int maxsize) {
		if (in instanceof BufferedInputStream) {
			throw new RuntimeException("Cannot buffer a BufferedInputStream");
		}
		if (in == null) {
			throw new RuntimeException("Cannot buffer nonexistent stream.");
		}
		this.in = in;
		this.buffer = new byte[size];
		this.maxsize = maxsize;
	}

	// positions

	/**
	 * Sets the read and last byte to the default initialization values.
	 */
	private void initLegal() {
		read = -1;
		last = -1;
	}

	/**
	 * Returns the last legal position inside the buffer.
	 * 
	 * @return the last legal position.
	 */
	private int getLastLegal() {
		return last;
	}

	/**
	 * Returns the read position inside the buffer.
	 * 
	 * @return the read position.
	 */
	private int getReadLegal() {
		return read;
	}

	/**
	 * Returns the mark position inside the buffer.
	 * 
	 * @return the mark position.
	 */
	private int getMarkLegal() {
		return mark;
	}

	/**
	 * The read position relation the the buffer length.
	 * 
	 * @return the read position.
	 */
	private int getReadPosition() {
		return getReadLegal() % buffer.length;
	}

	/**
	 * The read position relation the the buffer length.
	 * 
	 * @return the read position.
	 */
	private int getLastPosition() {
		return getLastLegal() % buffer.length;
	}

	/**
	 * The read position relation the the buffer length.
	 * 
	 * @return the read position.
	 */
	private int getMarkPosition() {
		return getMarkLegal() % buffer.length;
	}

	/**
	 * Returns the free space available behind the read pointer.
	 * 
	 * @return the upper free space
	 */
	private int getUpperFreespace() {
		return buffer.length - (getLastPosition() + 1);
	}

	/**
	 * Returns the free space available before the read pointer.
	 * 
	 * @return the lower free space
	 */
	private int getLowerFreespace() {
		return getMarkPosition();
	}

	/**
	 * Increase the read pointer.
	 * 
	 * @param value increase value.
	 */
	private void incReadLegal(int value) {
		read += value;
		if (marked == -1) {
			mark = read;
		}
	}

	/**
	 * Returns the number of bytes we can read until the buffer must be filled
	 * with new bytes.
	 * 
	 * @return the distance between the read pointer and the last valid byte.
	 */
	private int getDistance() {
		if (getReadLegal() == -1 && getLastLegal() >= 0) {
			return getLastLegal();
		} else if (getReadLegal() >= 0 && getLastLegal() >= 0) {
			return getLastLegal() - getReadLegal();
		}
		return -1;
	}

	/**
	 * Returns <code>true</code> if the stream is closed, <code>false</code>
	 * otherwise.
	 * 
	 * @return
	 */
	private boolean isClosed() {
		return closed;
	}

	/*
	 * (non-Javadoc)
	 * @see java.io.InputStream#read()
	 */
	public synchronized int read() throws IOException {
		if (isClosed()) return -1;

		/*
		 * Check for mark exceedance.
		 */
		if (isMarked() && read >= mark + marked) {
			cleanMark();
		}
		/*
		 * How many bytes left in buffer? Fill buffer if necessary!
		 */
		if (getDistance() < 0) {
			int count = fill();
			if (count < 1) {
				return -1;
			}
		}

		/*
		 * Get the value and move the pointer.
		 */
		int v = buffer[getReadPosition()];
		incReadLegal(1);
		int i = v & 255;
		return i;
	}

	/*
	 * (non-Javadoc)
	 * @see java.io.InputStream#read(byte[], int, int)
	 */
	public synchronized int read(byte b[], int off, int length) throws IOException {
		if (isClosed()) return -1;
		if ((off | length | (off + length) | (b.length - (off + length))) < 0) {
			throw new IndexOutOfBoundsException();
		}
		int result = -1;

		int l = getDistance() + 1;

		if (l >= length) {
			// We can fill the array directly from the buffer.
			System.arraycopy(buffer, getReadPosition(), b, off, length);
			incReadLegal(length);
			return length;
		} else {
			// We cannot fill the whole array from the buffer.
			if (l > 0) {
				// But we can will some...
				System.arraycopy(buffer, getReadPosition(), b, off, l);
				incReadLegal(l);
			}
			// calculate the rest (missing bytes).
			int rest = length - l;
			result = l;
			while (rest > 0) {
				int count = fill();
				if (count < 1) {
					if (!SENDZEROBYTE && result == 0) {
						return -1;
					}
					return result;
				}
				int fill = Math.min(rest, getDistance() + 1);
				System.arraycopy(buffer, getReadPosition(), b, off + result, fill);
				incReadLegal(fill);
				result += fill;
				rest -= fill;
			}
			return result;
		}
	}

	/*
	 * (non-Javadoc)
	 * @see java.io.InputStream#read(byte[])
	 */
	public int read(byte b[]) throws IOException {
		return read(b, 0, b.length);
	}

	/*
	 * (non-Javadoc)
	 * @see java.io.InputStream#reset()
	 */
	public synchronized void reset() throws IOException {
		if (isClosed()) return;
		if (!MARK_SUPPORT) {
			throw new IOException("Mark/reset not supported");
		}
		/*
		 * If mark was set, reset the read pointer
		 */
		if (isMarked()) {
			read = mark;
		}
	}

	/*
	 * (non-Javadoc)
	 * @see java.io.InputStream#markSupported()
	 */
	public boolean markSupported() {
		return MARK_SUPPORT;
	}

	/*
	 * (non-Javadoc)
	 * @see java.io.InputStream#available()
	 */
	public int available() throws IOException {
		if (isClosed()) return 0;

		int raw = ((mark == -1) ? read : mark);
		int d = (last == -1) ? 0 : last - raw;
		int i = in.available();

		/*
		 * calc byte amount, if maxsize is set
		 */
		if (maxsize > -1) {
			if (i > maxsize || d > maxsize) {
				if (raw < 0) return maxsize;
				return (maxsize - raw);
			}
		}
		return (d < i) ? i : d;
	}

	/*
	 * (non-Javadoc)
	 * @see java.io.InputStream#mark(int)
	 */
	public synchronized void mark(int readAheadLimit) {
		if (isClosed()) return;
		if (!MARK_SUPPORT) return;
		if (readAheadLimit > buffer.length) {
			throw new IndexOutOfBoundsException("Cannot mark beyond the buffer size of " + buffer.length);
		}
		mark = read;
		marked = readAheadLimit;
	}

	/*
	 * (non-Javadoc)
	 * @see java.io.InputStream#close()
	 */
	public void close() throws IOException {
		if (in == null) {
			throw new IOException("No stream found");
		}
		cleanMark();
		in.close();
		closed = true;
		buffer = null;
	}

	/**
	 * Returns <code>true</code> if the stream has a mark set,
	 * <code>false</code> otherwise.
	 * 
	 * @return <code>true</code> if the stream has a mark set,
	 *         <code>false</code> otherwise.
	 */
	public boolean isMarked() {
		if (!MARK_SUPPORT) return false;
		return (marked > -1 && mark > -1);
	}

	/**
	 * Allows the stream to throw a MarkReachedException. This Exception will be
	 * thrown if someone reads until the set mark.
	 * 
	 * @param mode <code>true</code> to allow exception.
	 */
	public synchronized void setMarkWatchMode(boolean mode) {
		markwatch = mode;
	}

	/**
	 * Is mark watched?
	 * 
	 * @return <code>true</code> if mark is watched, <code>false</code>
	 *         otherwise.
	 */
	public synchronized boolean isMarkWatched() {
		return markwatch;
	}

	/**
	 * Is timeout mode enabled?
	 * 
	 * @return <code>true</code> if this stream is in timeout mode,
	 *         <code>false</code> otherwise.
	 */
	public boolean isTimeoutMode() {
		return timeout;
	}

	/**
	 * Set the timeout mode.
	 * 
	 * @param mode <code>true</code> enables timeout mode, <code>false</code>
	 *            disables it.
	 */
	public void setTimeoutMode(boolean mode) {
		timeout = mode;
	}

	/**
	 * Returns the inner input stream.
	 * 
	 * @return the input stream.
	 */
	protected InputStream getInputStream() {
		return in;
	}

	/**
	 * Resets the marked position.
	 */
	private void cleanMark() throws MarkReachedException {
		if (markwatch && isMarked()) {
			// SCREAM! if necessary!
			throw new MarkReachedException("mark=" + mark + "/" + marked + ", read=" + read);
		}
		mark = -1;
		marked = -1;
	}

	/**
	 * Fill the buffer with data from stream.
	 * 
	 * @return count of filled bytes.
	 */
	private int fill() throws IOException {
		int c = 0;
		// calculate correct positions
		// int raw = ((mark == -1) ? read : mark);

		if (getDistance() < 0 && !isMarked()) {
			/*
			 * if all bytes are read, we can overwrite the internal buffer.
			 */

			buffer = new byte[buffer.length];
			initLegal();

			c = readInternal(buffer, 0, buffer.length);
			if (c >= 1) {
				last = c - 1;
				read = 0;
			}
		} else if (getDistance() < 0 && isMarked()) {
			int l = getLowerFreespace();
			int u = getUpperFreespace();
			if (l > 0) {
				c += readInternal(buffer, 0, l);
			}
			if (u > 0) {
				c += readInternal(buffer, getLastPosition() + 1, u);
			}
			if (l == 0 && u == 0) {
				// no free space? but distance below 0?
				c += readInternal(buffer, 0, buffer.length);
			}
			last += c;
		}
		return c;
	}

	/**
	 * Read bytes from stream into the buffer.
	 * 
	 * @param b buffer to read the bytes in.
	 * @param off buffer offset.
	 * @param len read length.
	 * @return read bytes count.
	 */
	private int readInternal(byte[] b, int off, int len) throws IOException {
		if ((off | len | (off + len) | (b.length - (off + len))) < 0) {
			throw new IndexOutOfBoundsException();
		}
		if (isClosed()) {
			return -1;
		}

		/*
		 * Correct the length. Never read more bytes then preset.
		 */
		if (maxsize != -1 && (rawread + len) > maxsize) {
			len = maxsize - rawread;
		}
		int available = in.available();

		if (available > 0) {
			reducedtimeout = true;
		}
		/*
		 * Timeout mode: Check in.available() until some data is received.
		 */
		int tryCount = 0;
		int tryBackup = tries;
		if (isTimeoutMode()) {
			if (reducedtimeout) {
				tryBackup = tries;
				tries = reducedtries;
			}
			while ((available == 0) && (tryCount < tries)) {
				try {
					Thread.sleep(sleep);
				} catch (InterruptedException e) {
					throw new IOException("Stream read interrupted");
				}
				available = in.available();
				tryCount++;
			}

			if (available > 0) {
				reducedtimeout = true;
				reducedtries = (tryCount > 0) ? tryCount : 1;
				reducedtries = (reducedtries < 50) ? 50 : reducedtries;
			}
			if (reducedtimeout) {
				tries = tryBackup;
			}

			// time out?
			if (tryCount >= tries) {
				return -1;
			}
		}

		/*
		 * Cannot determinate amount of bytes from underlying input stream. Read
		 * at least one byte. This is a blocking read.
		 */
		if (available == 0) {
			int v = in.read();
			if (v == -1) {
				return -1;
			}
			rawread++;
			/*
			 * Put the read byte into the buffer and return the read length (1).
			 */
			b[off] = (byte) (v & 255);
			return 1;
		}

		/*
		 * Calculate amount to read.
		 */
		int count = (available < (b.length - off)) ? available : (b.length - off);
		count = (count > len) ? len : count;
		if (count == 0) {
			return 0;
		}

		/*
		 * Read from the REAL stream.
		 */
		int l = in.read(b, off, count);
		rawread += l;

		// String s = new String(b, 0, l);
		// System.out.write(b, 0, l);

		return l;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public synchronized String toString() {
		return printBuffer(buffer);
	}

	/**
	 * Creates a <code>String</code> representation of the given byte buffer.
	 * 
	 * @param b byte buffer to make <code>String</code> from.
	 * @return the byte buffer as <code>String</code>.
	 */
	private String printBuffer(byte[] b) {
		StringBuffer sb = new StringBuffer();
		sb.append(read + "/" + last);
		sb.append((char) Specialchars.CR);
		sb.append((char) Specialchars.LF);
		// sb.append("[");
		// for (int i = 0; i < b.length; i++) {
		// sb.append(i + "=>" + b[i]);
		// if (i < (b.length - 1)) {
		// sb.append(", ");
		// }
		// }
		// sb.append("]");
		for (int i = 0; i < b.length; i++) {
			sb.append((char) b[i]);
		}
		return sb.toString();
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
		BufferedInputStream other = (BufferedInputStream) obj;
		if (in == null) {
			if (other.in != null) return false;
		} else if (!in.equals(other.in)) return false;
		return true;
	}

}
