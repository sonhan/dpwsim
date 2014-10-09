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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.ws4d.java.service.parameter.ParameterValue;
import org.ws4d.java.types.InternetMediaType;
import org.ws4d.java.util.IDGenerator;

/**
 * This class is used to represent an attachment as a byte array. Access to this
 * attachemnt's raw data is possible by means of method {@link #getBytes()}
 * (which returns a reference to the actual bytes of the attachment rather than
 * a copy), but also using method {@link #getInputStream()}. The latter will
 * return a new input stream instance for every call.
 * <p>
 * In order to support easy and fast type checking, method {@link #getType()}
 * will return always {@link #MEMORY_ATTACHMENT} for instances of this class.
 * </p>
 */
class MemoryAttachment extends AbstractAttachment implements IncomingAttachment, OutgoingAttachment {

	private byte[]	bytes;

	/**
	 * Creates an attachment by obtaining its raw data from the specified byte
	 * array <code>bytes</code>. A unique {@link #getContentId() content ID} for
	 * this attachment is automatically generated. Its {@link #getContentType()
	 * content type} will be unspecified.
	 * 
	 * @param bytes the array constituting this attachemnt's raw data
	 */
	MemoryAttachment(byte[] bytes) {
		this(bytes, (InternetMediaType) null);
	}

	/**
	 * Creates an attachment by obtaining its raw data from the specified byte
	 * array <code>bytes</code>. A unique {@link #getContentId() content ID} for
	 * this attachment is automatically generated.
	 * 
	 * @param bytes the array constituting this attachemnt's raw data
	 * @param contentType the MIME content type of the attachment
	 */
	MemoryAttachment(byte[] bytes, InternetMediaType contentType) {
		this(bytes, IDGenerator.getUUID(), contentType);
	}

	/**
	 * Creates an attachment by obtaining its raw data from the specified byte
	 * array <code>bytes</code>. A unique {@link #getContentId() content ID} for
	 * this attachment is automatically generated.
	 * 
	 * @param bytes the array constituting this attachemnt's raw data
	 * @param contentType the MIME content type of the attachment
	 */
	MemoryAttachment(byte[] bytes, String contentType) {
		this(bytes, IDGenerator.getUUID(), contentType);
	}

	/**
	 * Creates an attachment by obtaining its raw data from the specified byte
	 * array <code>bytes</code> and assigns the specified <code>contentId</code>
	 * to it.
	 * 
	 * @param bytes the array constituting this attachemnt's raw data
	 * @param contentId the MIME content ID of the attachment, which should be
	 *            unique within the scope of the MIME package that the
	 *            attachment is contained in; in the case of DPWS this scope
	 *            corresponds to a single invocation message, i.e. the content
	 *            ID must be unique within the {@link ParameterValue} hierarchy
	 *            of an operations input or output parameters
	 * @param contentType the MIME content type of the attachment
	 */
	MemoryAttachment(byte[] bytes, String contentId, InternetMediaType contentType) {
		super(contentId, contentType);
		this.bytes = bytes;
	}

	/**
	 * Creates an attachment by obtaining its raw data from the specified byte
	 * array <code>bytes</code> and assigns the specified <code>contentId</code>
	 * to it.
	 * 
	 * @param bytes the array constituting this attachemnt's raw data
	 * @param contentId the MIME content ID of the attachment which should be
	 *            unique within the scope of the MIME package the attachment is
	 *            contained in; in the case of DPWS this scope corresponds to a
	 *            single invocation message, i.e. the content ID must be unique
	 *            within the {@link ParameterValue} hierarchy of an operation's
	 *            input or output parameters
	 * @param contentType the MIME content type of the attachment
	 */
	MemoryAttachment(byte[] bytes, String contentId, String contentType) {
		this(bytes, contentId, maskContentType(contentType));
	}

	/*
	 * This special constructor is only used by the DefaultAttachmentStore to
	 * handle faulty attachments.
	 */
	MemoryAttachment(AttachmentException readInException) {
		this(EMPTY_BYTE_ARRAY);
		setReadInException(readInException);
	}

	/**
	 * Always returns {@link #MEMORY_ATTACHMENT}.
	 */
	public final int getType() throws AttachmentException {
		return MEMORY_ATTACHMENT;
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.attachment.Attachment#dispose()
	 */
	public void dispose() {
		bytes = null;
	}

	/**
	 * Returns a <em>reference to</em> (rather than a <em>copy of</em>) the byte
	 * array that contains the raw data of this attachment.
	 * 
	 * @return byte array containing this attachment's raw data
	 * @throws AttachmentException if attachment processing is not supported
	 *             within the current runtime or obtaining the attachment failed
	 *             for any reason
	 */
	public byte[] getBytes() throws AttachmentException {
		if (readInException != null) {
			throw readInException;
		}
		return bytes == null ? EMPTY_BYTE_ARRAY : bytes;
	}

	/**
	 * Returns the length of the byte array constituting this attachemnt's raw
	 * data.
	 */
	public long size() throws AttachmentException {
		if (readInException != null) {
			throw readInException;
		}
		if (bytes == null) {
			return 0;
		}
		return bytes.length;
	}

	/**
	 * Creates and returns a new stream wrapped around this attachment's raw
	 * data for each call.
	 */
	public InputStream getInputStream() throws AttachmentException, IOException {
		if (readInException != null) {
			throw readInException;
		}
		if (bytes == null) {
			return EMPTY_STREAM;
		}
		return new ByteArrayInputStream(bytes);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.ws4d.java.communication.protocol.mime.MIMEEntityOutput#serialize(
	 * java.io.OutputStream)
	 */
	public void serialize(OutputStream out) throws IOException {
		if (bytes != null) {
			out.write(bytes);
			dispose();
		}
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
		throw new AttachmentException("file system operations not supported for memory attachments");
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.attachment.Attachment#save(java.lang.String)
	 */
	public void save(String targetFilePath) throws AttachmentException, IOException {
		throw new AttachmentException("file system operations not supported for memory attachments");
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.attachment.Attachment#move(java.lang.String)
	 */
	public boolean move(String newFilePath) throws AttachmentException {
		throw new AttachmentException("file system operations not supported for memory attachments");
	}

}
