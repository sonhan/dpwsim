/**********************************************************************************
 * Copyright (c) 2008 MATERNA Information & Communications and TU Dortmund, Dpt.
 * of Computer Science, Chair 4, Distributed Systems All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 **********************************************************************************/
package org.ws4d.java.attachment;

import java.io.IOException;
import java.io.OutputStream;

import org.ws4d.java.service.parameter.ParameterValue;
import org.ws4d.java.types.InternetMediaType;

/**
 * Attachment container. Provides access to metadata (content ID, content type,
 * transfer encoding) and raw data of an attachment.
 * <p>
 * There are two different ways of obtaining raw data from an
 * <code>Attachment</code>. The first one, {@link #getInputStream()}, allows the
 * reading of a stream-based representation of the attachment's bytes, whereas
 * the second one, {@link #getBytes()}, returns them within a byte array. While
 * in some cases being faster (depending on the actual <code>Attachment</code>
 * implementation), the later approach is subject to certain limitations
 * resulting from the finite amount of memory available within the current
 * runtime.
 * </p>
 * <p>
 * Most methods of this interface are indicated to throw
 * {@link AttachmentException}s if obtaining the requested attachment data or
 * property failed for any reason. This could be the result e.g. of a network
 * failure while reading the attachment or an unexpected or erroneous attachment
 * format, or it could mean that attachment support is not available within the
 * current DPWS framework configuration.
 * </p>
 * <p>
 * The actual type of attachment can be queried by means of method
 * {@link #getType()}. Depending on the value returned (see
 * {@link #STREAM_ATTACHMENT}, {@link #FILE_ATTACHMENT} and
 * {@link #MEMORY_ATTACHMENT}), some methods like {@link #getInputStream()},
 * {@link #getBytes()} and {@link #size()} may behave differently. Additionally,
 * other methods like {@link #save(String)}, {@link #move(String)} or
 * {@link #getFilePath()} may not work at all.
 * </p>
 */
public interface Attachment {

	/**
	 * This attachment will behave like a {@link InputStreamAttachment}.
	 * Moreover, it will <strong>not</strong> support the {@link #save(String)},
	 * {@link #move(String)} and {@link #getFilePath()} operations.
	 * <strong>Note</strong> that checking that an attachment is of this type is
	 * <strong>not</strong> sufficient in order to safely cast it to a
	 * {@link InputStreamAttachment}. Use an <code>instanceof</code> expression
	 * instead.
	 */
	public static final int	STREAM_ATTACHMENT		= 1;

	/**
	 * This attachment will behave like a {@link MemoryAttachment}. Moreover, it
	 * will <strong>not</strong> support the {@link #save(String)},
	 * {@link #move(String)} and {@link #getFilePath()} operations.
	 * <strong>Note</strong> that checking that an attachment is of this type is
	 * <strong>not</strong> sufficient to safely cast it to a
	 * {@link MemoryAttachment}. Use an <code>instanceof</code> expression
	 * instead.
	 */
	public static final int	MEMORY_ATTACHMENT		= 2;

	/**
	 * This attachment will behave like a {@link FileAttachment}. Moreover, it
	 * will support the {@link #save(String)}, {@link #move(String)} and
	 * {@link #getFilePath()} operations. <strong>Note</strong> that checking
	 * that an attachment is of this type is <strong>not</strong> sufficient to
	 * safely cast it to a {@link FileAttachment}. Use an
	 * <code>instanceof</code> expression instead.
	 */
	public static final int	FILE_ATTACHMENT			= 3;

	/**
	 * Mostly a {@link InputStreamAttachment}. The difference between the
	 * default {@link InputStreamAttachment} is, that this one does not accept a
	 * input stream but provides an output stream.
	 */
	public static final int	OUTPUTSTREAM_ATTACHMENT	= 4;

	/**
	 * Returns this attachment's type.
	 * <p>
	 * The value returned distinguishes between different implementations and
	 * thereby different storage models for the attachment's raw data, such as
	 * in-memory, on file system or as (opaque) input stream. It further
	 * determines which ways of obtaining the raw data are suitable/possible for
	 * this instance (e.g. by means of {@link #getBytes()} or
	 * {@link #getInputStream()}). Also, usage/availability of some operations
	 * like {@link #save(String)}, {@link #move(String)} and
	 * {@link #getFilePath()} depends on the type of attachment. On some types,
	 * these operations will not be supported at all and will always throw an
	 * {@link AttachmentException}.
	 * 
	 * @return the type of this attachment instance
	 * @throws AttachmentException if attachment processing is not supported
	 *             within the current runtime or obtaining the attachment failed
	 *             for any reason
	 * @see #STREAM_ATTACHMENT
	 * @see #FILE_ATTACHMENT
	 * @see #MEMORY_ATTACHMENT
	 */
	public int getType() throws AttachmentException;

	/**
	 * Return the content ID of this attachment. The content ID is used to
	 * distinguish the attachment within a MIME package and to enable linking to
	 * it from within {@link ParameterValue} instances.
	 * 
	 * @return this attachment's content ID
	 */
	public String getContentId();

	/**
	 * The encoding for this attachment.
	 * 
	 * @return the encoding
	 * @throws AttachmentException if attachment processing is not supported
	 *             within the current runtime or obtaining the attachment failed
	 *             for any reason
	 */
	public String getTransferEncoding() throws AttachmentException;

	/**
	 * The content type (MIME) for this attachment.
	 * 
	 * @return the content type
	 * @throws AttachmentException if attachment processing is not supported
	 *             within the current runtime or obtaining the attachment failed
	 *             for any reason
	 */
	public InternetMediaType getContentType() throws AttachmentException;

	/**
	 * Disposes of the attachment and - if possible - frees any resources such
	 * as volatile and/or non volatile memory it uses. After calling this
	 * method, access to this attachment's metadata and raw data will no longer
	 * be possible!
	 */
	public void dispose();

	public void serialize(OutputStream out) throws IOException, AttachmentException;
}
