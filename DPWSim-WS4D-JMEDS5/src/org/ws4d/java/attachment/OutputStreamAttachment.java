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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.ws4d.java.attachment.interfaces.outgoing.OutgoingOutputStreamAttachment;
import org.ws4d.java.constants.HTTPConstants;
import org.ws4d.java.types.InternetMediaType;
import org.ws4d.java.util.IDGenerator;
import org.ws4d.java.util.Log;

class OutputStreamAttachment extends AbstractAttachment implements IncomingAttachment, OutgoingOutputStreamAttachment {

	private StreamAttachmentOutputStream	out	= new StreamAttachmentOutputStream();

	OutputStreamAttachment(InternetMediaType contentType) {
		this(IDGenerator.getUUID(), contentType);
	}

	OutputStreamAttachment(String contentType) {
		this(IDGenerator.getUUID(), maskContentType(contentType));
	}

	OutputStreamAttachment(String contentId, InternetMediaType contentType) {
		this(contentId, contentType, HTTPConstants.HTTP_HEADERVALUE_TRANSFERENCODING_BINARY);
	}

	OutputStreamAttachment(String contentId, String contentType) {
		this(contentId, maskContentType(contentType), HTTPConstants.HTTP_HEADERVALUE_TRANSFERENCODING_BINARY);
	}

	OutputStreamAttachment(String contentId, InternetMediaType contentType, String transferEncoding) {
		super(contentId, contentType, transferEncoding);
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.attachment.Attachment#getType()
	 */
	public int getType() throws AttachmentException {
		return OUTPUTSTREAM_ATTACHMENT;
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.attachment.Attachment#getBytes()
	 */
	public byte[] getBytes() throws AttachmentException, IOException {
		throw new AttachmentException("This attachment does not allow to read bytes. No byte array available.");
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.attachment.Attachment#getInputStream()
	 */
	public InputStream getInputStream() throws AttachmentException, IOException {
		throw new AttachmentException("This attachment does not allow to read bytes. No input stream available.");
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.attachment.Attachment#size()
	 */
	public long size() throws AttachmentException {
		throw new AttachmentException("This attachment does not allow to read bytes. No size available.");
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.attachment.Attachment#isLocal()
	 */
	public boolean isLocal() {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.attachment.Attachment#getFilePath()
	 */
	public String getFilePath() throws AttachmentException {
		throw new AttachmentException("file system operations not supported for stream attachments");
	}

	public void save(String targetFilePath) throws AttachmentException, IOException {
		throw new AttachmentException("file system operations not supported for stream attachments");
	}

	public boolean move(String newFilePath) throws AttachmentException {
		throw new AttachmentException("file system operations not supported for stream attachments");
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.attachment.Attachment#dispose()
	 */
	public void dispose() {
		if (out == null) {
			return;
		}
		try {
			out.close();
		} catch (IOException e) {
			Log.warn("Unable to close attachment output stream on dispose: " + e);
			Log.printStackTrace(e);
		}
		out = null;

	}

	public void serialize(OutputStream out) throws IOException {
		if (this.out == null) {
			throw new IOException("Cannot serialize because this OutputStreamAttachment has already been disposed.");
		}
		this.out.setOutputStream(out);
		synchronized (this.out) {
			while (this.out != null && this.out.isWriteable()) {
				try {
					/*
					 * Do not serialize the rest of the other stuff until the
					 * returned output stream is closed.
					 */
					this.out.wait();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * Returns an ouput stream which allows to write the stream data.
	 * <p>
	 * The stream MUST be closed if the streaming ends and the communication
	 * should continue correctly!
	 * </p>
	 * 
	 * @return the output stream.
	 */
	public synchronized OutputStream getOutputStream() {
		return out;
	}

	public class StreamAttachmentOutputStream extends OutputStream {

		private volatile OutputStream	out		= null;

		private volatile boolean		closed	= false;

		/**
		 * Checks if this StreamAttachmentOutputStream is already connected to
		 * its underlying OutputStream.
		 * 
		 * @return true if the underlying OutputStream is available
		 */
		public synchronized boolean isWriteable() {
			return out != null;
		}

		private void waitForOut() throws IOException {
			while (out == null && !closed) {
				try {
					this.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			if (closed) throw new IOException("Cannot write because this StreamAttachmentOutputStream has already been closed.");
		}

		/*
		 * (non-Javadoc)
		 * @see java.io.OutputStream#write(int)
		 */
		public synchronized void write(int b) throws IOException {
			waitForOut();
			out.write(b);
		}

		/*
		 * (non-Javadoc)
		 * @see java.io.OutputStream#write(byte[])
		 */
		public synchronized void write(byte[] b) throws IOException {
			waitForOut();
			out.write(b);
		}

		/*
		 * (non-Javadoc)
		 * @see java.io.OutputStream#write(byte[], int, int)
		 */
		public synchronized void write(byte[] b, int off, int len) throws IOException {
			waitForOut();
			out.write(b, off, len);
		}

		/*
		 * (non-Javadoc)
		 * @see java.io.OutputStream#close()
		 */
		public synchronized void close() throws IOException {
			if (closed) {
				return;
			}
			closed = true;
			/*
			 * Just flush. We cannot decide whether we should close this output
			 * or not.
			 */
			if (out != null) {
				out.flush();
				out = null;
			}
			notifyAll();
		}

		public synchronized void flush() throws IOException {
			if (closed) throw new IOException("Cannot flush because this StreamAttachmentOutputStream has already been closed.");
			if (out == null) {
				return;
			}
			out.flush();
		}

		/**
		 * Set the real output stream and notify the waiting write thread.
		 * 
		 * @param out
		 */
		synchronized void setOutputStream(OutputStream out) {
			if (!closed) {
				this.out = out;
			}
			/*
			 * Notify the waiting thread about the stream serialization.
			 */
			notifyAll();
		}

	}

}
