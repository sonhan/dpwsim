/*******************************************************************************
 * Copyright (c) 2009 MATERNA Information & Communications. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html. For further
 * project-related information visit http://www.ws4d.org. The most recent
 * version of the JMEDS framework can be obtained from
 * http://sourceforge.net/projects/ws4d-javame.
 ******************************************************************************/
package org.ws4d.java.attachment;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.ws4d.java.DPWSFramework;
import org.ws4d.java.communication.ContextID;
import org.ws4d.java.configuration.AttachmentProperties;
import org.ws4d.java.io.fs.FileSystem;
import org.ws4d.java.structures.HashMap;
import org.ws4d.java.structures.Iterator;
import org.ws4d.java.types.InternetMediaType;
import org.ws4d.java.util.Log;
import org.ws4d.java.util.ObjectPool;
import org.ws4d.java.util.ObjectPool.InstanceCreator;

/**
 * 
 */
public class DefaultAttachmentStore extends AttachmentStore {

	private static final AttachmentProperties	PROPS		= AttachmentProperties.getInstance();

	private static final ObjectPool				BUFFERS		= new ObjectPool(new InstanceCreator() {

																/*
																 * (non-Javadoc
																 * )
																 * @see org
																 * .ws4d . java.
																 * util.
																 * ObjectPool .
																 * InstanceCreator
																 * #
																 * createInstance
																 * ()
																 */
																public Object createInstance() {
																	return new byte[PROPS.getReadBufferSize()];
																}

															}, 1);

	// key = StoreKey, value = DefaultAttachment instance
	private final HashMap						attachments	= new HashMap();

	// key = StoreKey, value = the same StoreKey
	private final HashMap						lockKeys	= new HashMap();

	private final FileSystem					fs;

	/**
	 * Returns the number of bytes read in. The stream <code>from</code> is
	 * always completely read out unless a <code>java.io.IOException</code>
	 * occurs.
	 * 
	 * @param from the stream to read from
	 * @param out the stream in which to write everything to
	 * @return the number of bytes read
	 * @throws IOException if reading from <code>from</code> or writing to
	 *             <code>out</code> failed for any reason
	 */
	static int readOut(InputStream from, OutputStream out) throws IOException {
		try {
			return readOut(from, -1, out);
		} catch (AttachmentException e) {
			/*
			 * shouldn't ever occur, as we don't impose a limit on the amount of
			 * bytes to read
			 */
			throw new IOException(e.getMessage());
		}
	}

	/**
	 * Returns the number of bytes read in. The stream <code>from</code> is
	 * always completely read out unless a <code>java.io.IOException</code>
	 * occurs.
	 * 
	 * @param from the stream to read from
	 * @param out the stream in which to write everything to
	 * @param buffy the buffer to use when copying bytes from <code>from</code>
	 *            to <code>out</code>
	 * @return the number of bytes read
	 * @throws IOException if reading from <code>from</code> or writing to
	 *             <code>out</code> failed for any reason
	 */
	static int readOut(InputStream from, OutputStream out, byte[] buffy) throws IOException {
		try {
			return readOut(from, -1, out, buffy);
		} catch (AttachmentException e) {
			/*
			 * shouldn't ever occur, as we don't impose a limit on the amount of
			 * bytes to read
			 */
			throw new IOException(e.getMessage());
		}
	}

	/**
	 * Returns the number of bytes read in. The stream <code>from</code> is
	 * always completely read out unless a <code>java.io.IOException</code>
	 * occurs. That is, even if this method throws an
	 * {@link AttachmentException} because of a violation to the maximum
	 * acceptable byte count, it still reads out everything from
	 * <code>from</code>.
	 * 
	 * @param from the stream to read from
	 * @param maxSizeToAccept the maximum size in bytes to accept
	 * @param out the stream in which to write everything to
	 * @return the number of bytes read
	 * @throws AttachmentException if <code>from</code> contained more bytes
	 *             than specified by <code>maxSizeToAccept</code>
	 * @throws IOException if reading from <code>from</code> or writing to
	 *             <code>out</code> failed for any reason
	 */
	static int readOut(InputStream from, int maxSizeToAccept, OutputStream out) throws AttachmentException, IOException {
		return readOut(from, maxSizeToAccept, out, (byte[]) BUFFERS.acquire());
	}

	/**
	 * Returns the number of bytes read in. The stream <code>from</code> is
	 * always completely read out unless a <code>java.io.IOException</code>
	 * occurs. That is, even if this method throws an
	 * {@link AttachmentException} because of violation to the maximum
	 * acceptable bytes count, it will still have read out everything from
	 * <code>from</code>.
	 * 
	 * @param from the stream to read from
	 * @param maxSizeToAccept the maximum size in bytes to accept
	 * @param out the stream in which to write everything to
	 * @param buffy the buffer to use when copying bytes from <code>from</code>
	 *            to <code>out</code>
	 * @return the number of bytes read
	 * @throws AttachmentException if <code>from</code> contained more bytes
	 *             than specified by <code>maxSizeToAccept</code>
	 * @throws IOException if reading from <code>from</code> or writing to
	 *             <code>out</code> failed for any reason
	 */
	private static int readOut(InputStream from, int maxSizeToAccept, OutputStream out, byte[] buffy) throws AttachmentException, IOException {
		try {
			int size = 0;
			int j = from.read(buffy);
			AttachmentException toThrow = null;
			while (j > 0) {
				size += j;
				if (maxSizeToAccept > 0 && size > maxSizeToAccept && toThrow == null) {
						toThrow = new AttachmentException("Attachment size exceeds maximum allowed limit (" + maxSizeToAccept + ")");
				}
				if (toThrow == null) {
					out.write(buffy, 0, j);
					// especially for attachment support
//					out.flush();
				}
				j = from.read(buffy);
			}
			out.flush();
			if (toThrow == null) {
				return size;
			}
			throw toThrow;
		} finally {
			BUFFERS.release(buffy);
		}
	}

	/**
	 * 
	 */
	public DefaultAttachmentStore() {
		super();
		FileSystem fs = null;
		try {
			fs = DPWSFramework.getLocalFileSystem();
			fs.deleteFile(PROPS.getStorePath());
		} catch (IOException e) {
			/*
			 * no file system available within current runtime or framework not
			 * started
			 */
			Log.error("No local file system available, attachment store policy POLICY_EXT_STORAGE will not work.");
		}
		this.fs = fs;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.ws4d.java.attachment.AttachmentStore#store(org.ws4d.java.communication
	 * .MIMEContextID, java.lang.String, java.lang.String, java.lang.String,
	 * java.io.InputStream)
	 */
	public void store(ContextID context, String cid, String contentType, String transferEncoding, InputStream from) {
		/*
		 * there are FOUR feasible possibilities for obtaining an attachment's
		 * raw data:
		 */
		/*
		 * 1) store attachment within memory (byte buffer) and allow access to
		 * it via Attachment.getBytes() only
		 */
		/*
		 * 2) store attachment within memory (byte buffer) and allow access to
		 * it via Attachment.getBytes() AND Attachment.getInputStream(), which
		 * essentially wraps the byte array within a ByteArrayInputStream
		 */
		/*
		 * 3) store attachment within external storage (e.g. file system) and
		 * allow access to it via Attachment.getInputStream() only
		 */
		/*
		 * 4) store attachment within external storage (e.g. file system) and
		 * allow access to it via Attachment.getInputStream() AND
		 * Attachment.getBytes(), which reads out the entire stream and stores
		 * it within a byte array
		 */
		AbstractAttachment attachment;

		int storePolicy = getStorePolicy();
		if (storePolicy == POLICY_EXT_STORAGE && fs == null) {
			storePolicy = POLICY_MEM_BUFFER;
			Log.warn("No platform support available for requested store policy POLICY_EXT_STORAGE, reverting to POLICY_MEM_BUFFER");
		}
		
		InternetMediaType mimeType = new InternetMediaType(contentType);
		if (isStreamingMediaType(mimeType)) {
			attachment = new InputStreamAttachment(from);
		} else if (storePolicy == POLICY_EXT_STORAGE) {
			// store content of 'from' within file system repository
			// we make cid unique within FS store by means of a timestamp
			String filePath = PROPS.getStorePath() + fs.fileSeparator() + System.currentTimeMillis() + "_" + fs.escapeFileName(context.getInstanceId() + ":" + context.getMessageNumber() + ":" + cid);
			try {
				OutputStream out = fs.writeFile(filePath);
				readOut(from, PROPS.getMaxAttachmentSize(), out);
				out.flush();
				out.close();
				attachment = new FileAttachment(filePath, false);
			} catch (AttachmentException e) {
				fs.deleteFile(filePath);
				attachment = new MemoryAttachment(e);
			} catch (IOException e) {
				AttachmentException ae = new AttachmentException("Reading from stream or writing into attachment store failed: " + e);
				Log.error(ae.toString());
				attachment = new MemoryAttachment(ae);
			}
		} else {
			// POLICY_MEM_BUFFER is the default one
			try {
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				readOut(from, PROPS.getMaxMemBufferSize(), out);
				out.close();
				attachment = new MemoryAttachment(out.toByteArray());
			} catch (AttachmentException e) {
				attachment = new MemoryAttachment(e);
			} catch (IOException e) {
				AttachmentException ae = new AttachmentException("Reading from stream failed: " + e);
				Log.error(ae.toString());
				attachment = new MemoryAttachment(ae);
			}
		}
		attachment.setContentId(cid);
		attachment.setContentType(mimeType);
		attachment.setTransferEncoding(transferEncoding);

		StoreKey key = new StoreKey(context, cid);
		StoreKey lockKey = null;
		synchronized (attachments) {
			attachments.put(key, attachment);
			lockKey = (StoreKey)lockKeys.remove(key);
		}
		if (lockKey != null) {
			lockKey.notifyWaiters();
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.attachment.AttachmentStore#isAvailable(org.ws4d.java.
	 * communication.MIMEContextID, java.lang.String)
	 */
	public boolean isAvailable(ContextID context, String cid) {
		StoreKey key = new StoreKey(context, cid);
		synchronized (attachments) {
			return attachments.containsKey(key);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.ws4d.java.attachment.AttachmentStore#resolve(org.ws4d.java.communication
	 * .MIMEContextID, java.lang.String)
	 */
	public IncomingAttachment resolve(ContextID context, String cid) throws AttachmentException {
		IncomingAttachment attachment;
		StoreKey key = new StoreKey(context, cid);
		StoreKey lockKey;
		synchronized (attachments) {
			attachment = (IncomingAttachment) attachments.get(key);
			if (attachment == null) {
				lockKey = (StoreKey)lockKeys.get(key);
				if (lockKey == null) {
					lockKey = key;
					lockKeys.put(lockKey, lockKey);
				}
			} else {
				return attachment;
			}
		}
		
		synchronized (lockKey) {
			while (lockKey.waiting) {
				try {
					lockKey.wait();
				} catch (InterruptedException e) {
					// void
				}
			}
		}
		
		synchronized (attachments) {
			attachment = (IncomingAttachment) attachments.get(key);
		}
		if (attachment == null) {
			throw new AttachmentException("Attachment not found for " + context + " and content ID " + cid);
		}
		return attachment;
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.attachment.AttachmentStore#cleanup()
	 */
	public void cleanup() {
		if (fs != null) {
			fs.deleteFile(PROPS.getStorePath());
			synchronized (attachments) {
				attachments.clear();
				for (Iterator iter = lockKeys.values().iterator(); iter.hasNext();) {
					((StoreKey)iter.next()).notifyWaiters();
				}
				lockKeys.clear();
			}
		}
	}

	private static class StoreKey {

		final ContextID	context;

		final String	cid;

		int  hashCode;
		
		public volatile boolean waiting = true;
		
		/**
		 * @param context
		 * @param cid
		 */
		StoreKey(ContextID context, String cid) {
			this.context = context;
			this.cid = cid;
			
			final int prime = 31;
			hashCode = prime + cid.hashCode();
			hashCode = prime * hashCode + context.hashCode();
		}
		
		public int hashCode() {
			return hashCode;
		}

		/*
		 * (non-Javadoc)
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (getClass() != obj.getClass()) {
				return false;
			}
			StoreKey other = (StoreKey) obj;
			if (!cid.equals(other.cid)) {
				return false;
			}
			if (!context.equals(other.context)) {
				return false;
			}
			return true;
		}

		public synchronized void notifyWaiters() {
			waiting = false;
			notifyAll();
		}
	}
}
