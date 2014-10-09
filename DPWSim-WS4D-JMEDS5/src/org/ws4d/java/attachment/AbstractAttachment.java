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
import java.io.InputStream;

import org.ws4d.java.constants.HTTPConstants;
import org.ws4d.java.types.InternetMediaType;
import org.ws4d.java.util.IDGenerator;

/**
 * Attachment container. Provides access to an attachment's metadata (content
 * ID, content type and transfer encoding).
 */
public abstract class AbstractAttachment implements Attachment {

	static final byte[]				EMPTY_BYTE_ARRAY	= new byte[0];

	static final InputStream		EMPTY_STREAM		= new ByteArrayInputStream(EMPTY_BYTE_ARRAY);

	static final String[]			EMPTY_STRING_ARRAY	= new String[0];

	private String					contentId;

	private InternetMediaType		contentType;

	private String					transferEncoding;

	protected AttachmentException	readInException;

	static InternetMediaType maskContentType(String contentType) {
		return contentType == null || "".equals(contentType) ? null : new InternetMediaType(contentType);
	}

	/**
	 * @param contentType
	 */
	protected AbstractAttachment(InternetMediaType contentType) {
		this(IDGenerator.getUUID(), contentType);
	}

	/**
	 * @param contentType
	 */
	protected AbstractAttachment(String contentType) {
		this(IDGenerator.getUUID(), maskContentType(contentType));
	}

	/**
	 * @param contentId
	 * @param contentType
	 */
	protected AbstractAttachment(String contentId, InternetMediaType contentType) {
		this(contentId, contentType, HTTPConstants.HTTP_HEADERVALUE_TRANSFERENCODING_BINARY);
	}

	/**
	 * @param contentId
	 * @param contentType
	 */
	protected AbstractAttachment(String contentId, String contentType) {
		this(contentId, maskContentType(contentType), HTTPConstants.HTTP_HEADERVALUE_TRANSFERENCODING_BINARY);
	}

	/**
	 * @param contentType
	 * @param contentId
	 * @param transferEncoding
	 */
	protected AbstractAttachment(String contentId, InternetMediaType contentType, String transferEncoding) {
		super();
		this.contentId = contentId;
		this.contentType = contentType;
		this.transferEncoding = transferEncoding;
	}

	/**
	 * @param contentType
	 * @param contentId
	 * @param transferEncoding
	 */
	protected AbstractAttachment(String contentId, String contentType, String transferEncoding) {
		this(contentId, maskContentType(contentType), transferEncoding);
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.attachment.Attachment#isAvailable()
	 */
	public boolean isAvailable() {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.attachment.Attachment#getContentId()
	 */
	public String getContentId() {
		return contentId;
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.attachment.Attachment#getContentType()
	 */
	public InternetMediaType getContentType() throws AttachmentException {
		if (readInException != null) {
			throw readInException;
		}
		return contentType;
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.attachment.Attachment#getTransferEncoding()
	 */
	public String getTransferEncoding() throws AttachmentException {
		if (readInException != null) {
			throw readInException;
		}
		return transferEncoding;
	}

	/**
	 * @param contentId the contentId to set
	 */
	void setContentId(String contentId) {
		this.contentId = contentId;
	}

	/**
	 * @param contentType the contentType to set
	 */
	void setContentType(InternetMediaType contentType) {
		this.contentType = contentType;
	}

	/**
	 * @param transferEncoding the transferEncoding to set
	 */
	void setTransferEncoding(String transferEncoding) {
		this.transferEncoding = transferEncoding;
	}

	/**
	 * @param readInException the readInException to set
	 */
	void setReadInException(AttachmentException readInException) {
		this.readInException = readInException;
	}

}
