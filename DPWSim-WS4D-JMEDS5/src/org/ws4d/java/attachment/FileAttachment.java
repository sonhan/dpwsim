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
import org.ws4d.java.attachment.interfaces.incoming.IncomingFileAttachment;
import org.ws4d.java.configuration.AttachmentProperties;
import org.ws4d.java.io.fs.FileSystem;
import org.ws4d.java.service.parameter.ParameterValue;
import org.ws4d.java.types.InternetMediaType;
import org.ws4d.java.util.IDGenerator;
import org.ws4d.java.util.Log;

/**
 * This class is used when sending a file as an attachment given its path within
 * the file system. The raw attachment data can be always accessed by means of
 * {@link #getInputStream()}. On each call, this method will return a newly
 * created stream to the file in the local file system. Accordingly, the method
 * {@link #size()} will return the exact file size as reported by the file
 * system. Finally, {@link #getBytes()} will attempt to read up to
 * {@link AttachmentProperties#getMaxMemBufferSize()} bytes from the file and
 * return a new byte array from that. If the file size is however greater than
 * the configured limit, this method will throw an {@link AttachmentException}.
 * <p>
 * Beware when using the {@link #move(String)} method - it could be that this
 * attachment was received locally (i.e. from a client or service residing
 * within the same Java virtual machine) which would then result in moving the
 * original file! To prevent this, it is strongly recommended to check whether
 * the file referred to was initially local by means of the {@link #isLocal()}
 * method.
 * </p>
 * <p>
 * In order to support easy and fast type checking, method {@link #getType()}
 * will always return {@link #FILE_ATTACHMENT} for instances of this class.
 * </p>
 * <p>
 * This class may be used only on platforms including file system support (e.g.
 * Java Standard Edition).
 * </p>
 */
class FileAttachment extends AbstractAttachment implements IncomingFileAttachment, OutgoingAttachment {

	private static final FileSystem		FS;

	private static final OutputStream	NIRVANA	= new OutputStream() {

													/*
													 * (non-Javadoc)
													 * @seejava.io. OutputStream
													 * #write(int)
													 */
													public void write(int b) throws IOException {
														// void
													}

													/*
													 * (non-Javadoc)
													 * @seejava.io. OutputStream
													 * #write (byte[])
													 */
													public void write(byte[] b) throws IOException {
														// void
													}

													/*
													 * (non-Javadoc)
													 * @seejava.io. OutputStream
													 * #write (byte[], int, int)
													 */
													public void write(byte[] b, int off, int len) throws IOException {
														// void
													}

												};

	static {
		FileSystem fs;
		try {
			fs = DPWSFramework.getLocalFileSystem();
		} catch (IOException e) {
			Log.error("No local file system available, file attachments cannot be used: " + e);
			Log.printStackTrace(e);
			fs = new FileSystem() {

				/*
				 * (non-Javadoc)
				 * @see
				 * org.ws4d.java.io.fs.FileSystem#writeFile(java.lang.String)
				 */
				public OutputStream writeFile(String filePath) throws IOException {
					return NIRVANA;
				}

				/*
				 * (non-Javadoc)
				 * @see
				 * org.ws4d.java.io.fs.FileSystem#renameFile(java.lang.String,
				 * java.lang.String)
				 */
				public boolean renameFile(String filePath, String newFilePath) {
					return true;
				}

				/*
				 * (non-Javadoc)
				 * @see
				 * org.ws4d.java.io.fs.FileSystem#readFile(java.lang.String)
				 */
				public InputStream readFile(String filePath) throws IOException {
					return EMPTY_STREAM;
				}

				/*
				 * (non-Javadoc)
				 * @see
				 * org.ws4d.java.io.fs.FileSystem#listFiles(java.lang.String)
				 */
				public String[] listFiles(String dirPath) {
					return EMPTY_STRING_ARRAY;
				}

				/*
				 * (non-Javadoc)
				 * @see org.ws4d.java.io.fs.FileSystem#fileSeparator()
				 */
				public String fileSeparator() {
					return "";
				}

				/*
				 * (non-Javadoc)
				 * @see
				 * org.ws4d.java.io.fs.FileSystem#escapeFileName(java.lang.String
				 * )
				 */
				public String escapeFileName(String rawFileName) {
					return rawFileName;
				}

				/*
				 * (non-Javadoc)
				 * @see
				 * org.ws4d.java.io.fs.FileSystem#deleteFile(java.lang.String)
				 */
				public boolean deleteFile(String filePath) {
					return true;
				}

				/*
				 * (non-Javadoc)
				 * @see
				 * org.ws4d.java.io.fs.FileSystem#fileSize(java.lang.String)
				 */
				public long fileSize(String filePath) {
					return 0;
				}

				/*
				 * (non-Javadoc)
				 * @see
				 * org.ws4d.java.io.fs.FileSystem#fileExists(java.lang.String)
				 */
				public boolean fileExists(String filePath) {
					return true;
				}

				public long lastModified(String filePath) {
					return 0;
				}

			};
		}
		FS = fs;
	}

	private String						filePath;

	private boolean						local;

	/**
	 * Creates an attachment from the file with the given <code>filePath</code>
	 * within the local file system. A unique {@link #getContentId() content ID}
	 * for this attachment is automatically generated. Its
	 * {@link #getContentType() content type} will be unspecified.
	 * 
	 * @param filePath the path to the file to create an attachment from
	 */
	FileAttachment(String filePath) {
		this(filePath, (InternetMediaType) null);
	}

	/**
	 * Creates an attachment from the file with the given <code>filePath</code>
	 * within the local file system. A unique {@link #getContentId() content ID}
	 * for this attachment is automatically generated.
	 * 
	 * @param filePath the path to the file to create an attachment from
	 * @param contentType the MIME content type of the file
	 */
	FileAttachment(String filePath, InternetMediaType contentType) {
		this(filePath, IDGenerator.getUUID(), contentType);
	}

	/**
	 * Creates an attachment from the file with the given <code>filePath</code>
	 * within the local file system. A unique {@link #getContentId() content ID}
	 * for this attachment is automatically generated.
	 * 
	 * @param filePath the path to the file to create an attachment from
	 * @param contentType the MIME content type of the file
	 */
	FileAttachment(String filePath, String contentType) {
		this(filePath, IDGenerator.getUUID(), contentType);
	}

	/**
	 * Creates an attachment from the file with the given <code>filePath</code>
	 * within the local file system and assigns the specified
	 * <code>contentId</code> to it.
	 * 
	 * @param filePath the path to the file from which to create an attachment
	 * @param contentId the MIME content ID of the attachment, which should be
	 *            unique within the scope of the MIME package in which the
	 *            attachment is contained; in the case of DPWS this scope
	 *            corresponds to a single invocation message, i.e. the content
	 *            ID must be unique within the {@link ParameterValue} hierarchy
	 *            of an operations input or output parameters
	 * @param contentType the MIME content type of the file
	 */
	FileAttachment(String filePath, String contentId, InternetMediaType contentType) {
		super(contentId, contentType);
		this.filePath = filePath;
		this.local = true;
	}

	/**
	 * Creates an attachment from the file with the given <code>filePath</code>
	 * within the local file system and assigns the specified
	 * <code>contentId</code> to it.
	 * 
	 * @param filePath the path to the file from which to create an attachment
	 * @param contentId the MIME content ID of the attachment, which should be
	 *            unique within the scope of the MIME package the attachment is
	 *            contained in; in the case of DPWS this scope corresponds to a
	 *            single invocation message, i.e. the content ID must be unique
	 *            within the {@link ParameterValue} hierarchy of an operations
	 *            input or output parameters
	 * @param contentType the MIME content type of the file
	 */
	FileAttachment(String filePath, String contentId, String contentType) {
		this(filePath, contentId, maskContentType(contentType));
	}

	/*
	 * Creates an attachment from the file with the given <code>filePath</code>
	 * within the local file system. If the argument <code>local</code> is
	 * <code>false</code>, it is assumed that this file attachment was created
	 * by class DefaultAttachmentStore, so that it is safe to delete the file it
	 * points to when method dispose() is invoked.
	 */
	FileAttachment(String filePath, boolean local) {
		super((InternetMediaType) null);
		this.filePath = filePath;
		this.local = local;
	}

	/**
	 * Always returns {@link #FILE_ATTACHMENT}.
	 */
	public final int getType() throws AttachmentException {
		return FILE_ATTACHMENT;
	}

	/**
	 * Creates and returns a new input stream to the file to which this
	 * attachment refers to on each call.
	 */
	public InputStream getInputStream() throws AttachmentException, IOException {
		if (readInException != null) {
			throw readInException;
		}
		return FS.readFile(filePath);
	}

	/**
	 * Returns the size of the file constituting this attachment's raw data.
	 */
	public long size() throws AttachmentException {
		if (readInException != null) {
			throw readInException;
		}
		return FS.fileSize(filePath);
	}

	/**
	 * Returns the raw data of this attachment as a byte array, as long as its
	 * {@link #size() size} is less than or equal to the currently configured
	 * {@link AttachmentProperties#getMaxMemBufferSize() maximum in-memory
	 * attachment size}. Throws an {@link AttachmentException} otherwise.
	 */
	public byte[] getBytes() throws AttachmentException, IOException {
		if (readInException != null) {
			throw readInException;
		}
		// support this method only up to a limited amount of bytes
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		DefaultAttachmentStore.readOut(FS.readFile(filePath), AttachmentProperties.getInstance().getMaxMemBufferSize(), out);
		out.close();
		return out.toByteArray();
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.attachment.StreamAttachment#dispose()
	 */
	public void dispose() {
		if (local) {
			return;
		}
		try {
			FS.deleteFile(filePath);
		} catch (Exception e) {
			Log.warn("Unable to delete attachment file \"" + filePath + "\" on dispose: " + e);
			Log.printStackTrace(e);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.attachment.Attachment#serialize(java.io.OutputStream)
	 */
	public void serialize(OutputStream out) throws IOException {
		DefaultAttachmentStore.readOut(FS.readFile(filePath), out);
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.attachment.Attachment#isLocal()
	 */
	public boolean isLocal() {
		return local;
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.attachment.Attachment#getFilePath()
	 */
	public String getFilePath() throws AttachmentException {
		if (readInException != null) {
			throw readInException;
		}
		return filePath;
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.attachment.Attachment#move(java.lang.String)
	 */
	public boolean move(String newFilePath) throws AttachmentException {
		if (readInException != null) {
			throw readInException;
		}
		boolean result = FS.renameFile(filePath, newFilePath);
		// store new path
		this.filePath = newFilePath;
		this.local = true;
		return result;
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.attachment.Attachment#save(java.lang.String)
	 */
	public void save(String targetFilePath) throws AttachmentException, IOException {
		if (readInException != null) {
			throw readInException;
		}
		InputStream in = FS.readFile(filePath);
		OutputStream out = FS.writeFile(targetFilePath);
		DefaultAttachmentStore.readOut(in, out);
		out.flush();
		out.close();
	}

}
