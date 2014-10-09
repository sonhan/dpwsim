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

import org.ws4d.java.configuration.AttachmentProperties;
import org.ws4d.java.service.parameter.ParameterValue;
import org.ws4d.java.types.InternetMediaType;
import org.ws4d.java.util.IDGenerator;
import org.ws4d.java.util.Log;
import org.ws4d.java.util.ObjectPool;
import org.ws4d.java.util.ObjectPool.InstanceCreator;

/**
 * This class represents an attachment which can be obtained and read as a
 * <code>java.io.InputStream</code> <em>exactly once</em>. That is, the stream
 * returned by {@link #getInputStream()} is always the same and will usually not
 * support resetting. The use of the method {@link #getBytes()} is not supported
 * on this type of attachment and will always throw an
 * {@link AttachmentException}. Moreover, the method {@link #size()} will return
 * a potentially incorrect estimate (as obtained by
 * <code>java.io.InputStream.available()</code>).
 * <p>
 * In order to support easy and fast type checking, method {@link #getType()}
 * will return always {@link #STREAM_ATTACHMENT} for instances of this class.
 * </p>
 */
class InputStreamAttachment extends AbstractAttachment implements IncomingAttachment, OutgoingAttachment {

	private static final ObjectPool	STREAM_BUFFERS	= new ObjectPool(new InstanceCreator() {

														/*
														 * (non-Javadoc )
														 * @see org .ws4d .
														 * java. util.
														 * ObjectPool .
														 * InstanceCreator #
														 * createInstance ()
														 */
														public Object createInstance() {
															return new byte[AttachmentProperties.getInstance().getStreamBufferSize()];
														}

													}, 1);

	private InputStream				in;

	/**
	 * Creates an attachment by reading its raw data from the given stream
	 * <code>in</code>. A unique {@link #getContentId() content ID} for this
	 * attachment is automatically generated. Its {@link #getContentType()
	 * content type} will be unspecified.
	 * 
	 * @param in an input stream from which to obtain the attachment's raw data
	 */
	InputStreamAttachment(InputStream in) {
		this(in, (InternetMediaType) null);
	}

	/**
	 * Creates an attachment by reading its raw data from the given stream
	 * <code>in</code>. A unique {@link #getContentId() content ID} for this
	 * attachment is automatically generated.
	 * 
	 * @param in an input stream to obtain the attachment's raw data from
	 * @param contentType the MIME content type of the attachment
	 */
	InputStreamAttachment(InputStream in, InternetMediaType contentType) {
		this(in, IDGenerator.getUUID(), contentType);
	}

	/**
	 * Creates an attachment by reading its raw data from the given stream
	 * <code>in</code>. A unique {@link #getContentId() content ID} for this
	 * attachment is automatically generated.
	 * 
	 * @param in an input stream from which to obtain the attachment's raw data
	 * @param contentType the MIME content type of the attachment
	 */
	InputStreamAttachment(InputStream in, String contentType) {
		this(in, IDGenerator.getUUID(), contentType);
	}

	/**
	 * Creates an attachment by reading its raw data from the given stream
	 * <code>in</code> and assigns the specified <code>contentId</code> to it.
	 * 
	 * @param in an input stream from which to obtain the attachment's raw data
	 * @param contentId the MIME content ID of the attachment, which should be
	 *            unique within the scope of the MIME package in which the
	 *            attachment is contained; in the case of DPWS this scope
	 *            corresponds to a single invocation message, i.e. the content
	 *            ID must be unique within the {@link ParameterValue} hierarchy
	 *            of an operations input or output parameters
	 * @param contentType the MIME content type of the attachment
	 */
	InputStreamAttachment(InputStream in, String contentId, InternetMediaType contentType) {
		super(contentId, contentType);
		this.in = in;
	}

	/**
	 * Creates an attachment by reading its raw data from the given stream
	 * <code>in</code> and assigns the specified <code>contentId</code> to it.
	 * 
	 * @param in an input stream from which to obtain the attachment's raw data
	 * @param contentId the MIME content ID of the attachment, which should be
	 *            unique within which the scope of the MIME package the
	 *            attachment is contained; in the case of DPWS this scope
	 *            corresponds to a single invocation message, i.e. the content
	 *            ID must be unique within the {@link ParameterValue} hierarchy
	 *            of an operations input or output parameters
	 * @param contentType the MIME content type of the attachment
	 */
	InputStreamAttachment(InputStream in, String contentId, String contentType) {
		this(in, contentId, maskContentType(contentType));
	}

	/**
	 * Always returns {@link #STREAM_ATTACHMENT}.
	 */
	public final int getType() throws AttachmentException {
		return STREAM_ATTACHMENT;
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.attachment.AbstractAttachment#dispose()
	 */
	public void dispose() {
		if (in == null) {
			return;
		}
		try {
			in.close();
		} catch (IOException e) {
			Log.warn("Unable to close attachment input stream on dispose: " + e);
			Log.printStackTrace(e);
		}
		in = null;
	}

	/**
	 * Returns a reference to the same input stream with which this stream
	 * attachment instance was created. Note that this stream is not required to
	 * support resetting, so reading it more than once may not be possible.
	 */
	public InputStream getInputStream() throws AttachmentException, IOException {
		if (readInException != null) {
			throw readInException;
		}
		return in;
	}

	/**
	 * Always throws an {@link AttachmentException} indicating that this class
	 * doesn't support byte access.
	 */
	public byte[] getBytes() throws AttachmentException, IOException {
		throw new AttachmentException("byte access not supported for stream attachments");
	}

	/**
	 * Returns the amount of bytes that can be obtained from the enclosed input
	 * stream as provided by method <code>java.io.InputStream.available()</code>
	 * .
	 */
	public long size() throws AttachmentException {
		if (readInException != null) {
			throw readInException;
		}
		if (in == null) {
			return 0;
		}
		try {
			return in.available();
		} catch (IOException e) {
			throw new AttachmentException("unable to access attachment input stream: " + e);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.ws4d.java.communication.protocol.mime.MIMEEntity#serialize(java.io
	 * .OutputStream)
	 */
	public void serialize(OutputStream out) throws IOException {
		if (in == null) {
			return;
		}
		DefaultAttachmentStore.readOut(in, out, (byte[]) STREAM_BUFFERS.acquire());

		/*
		 * as this method should only be called on the sender side when
		 * transmitting the attachment's data to a remote receiver, we assume no
		 * one is going to use this attachment instance after that anymore;
		 * thus, ensure input stream is closed
		 */
		dispose();
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

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.attachment.Attachment#save(java.lang.String)
	 */
	public void save(String targetFilePath) throws AttachmentException, IOException {
		throw new AttachmentException("file system operations not supported for stream attachments");
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.attachment.Attachment#move(java.lang.String)
	 */
	public boolean move(String newFilePath) throws AttachmentException {
		throw new AttachmentException("file system operations not supported for stream attachments");
	}

}
