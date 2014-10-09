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

import org.ws4d.java.service.Operation;
import org.ws4d.java.service.parameter.ParameterValue;

public interface IncomingAttachment extends Attachment {

	/**
	 * Returns the input stream which contains the data.
	 * <p>
	 * Depending on the actual attachment implementation, this method may either
	 * always return the same <code>java.io.InputStream</code> instance, or it
	 * could create a new one on each call. In the first case, it is important
	 * to note that reading the attachment data might be possible only once, as
	 * the returned stream is not guaranteed to support resetting (see
	 * {@link #getType()}.
	 * </p>
	 * 
	 * @return an input stream to this attachment's raw data
	 * @throws AttachmentException if attachment processing is not supported
	 *             within the current runtime or obtaining the attachment failed
	 *             for any reason
	 * @throws IOException if reading raw attachment data failed
	 * @see #getType()
	 * @see #size()
	 * @see #getBytes()
	 */
	public InputStream getInputStream() throws AttachmentException, IOException;

	/**
	 * Returns <code>true</code> if this attachment is ready to be processed.
	 * <p>
	 * Because the DPWS framework handles attachments asynchronously, it is
	 * possible (and will most likely occur frequently) for an attachement's raw
	 * data to still be being transmitted over the network while a caller's
	 * business logic (e.g. the {@link Operation#invoke(ParameterValue)} method)
	 * gets called providing access to the attachment by means of its
	 * {@link ParameterValue#getAttachment() parameters}. Using methods other
	 * than {@link #getContentId()} or {@link #isAvailable()} on this attachment
	 * would block the caller until the entire attachment is read out. Thus,
	 * this method allows to check whether further examination of this
	 * attachment would block it or not.
	 * </p>
	 * 
	 * @return <code>true</code>, if this attachment is already available,
	 *         <code>false</code> if it is still not received (entirely)
	 */
	public boolean isAvailable();

	/**
	 * Returns the size of the attachment in bytes.
	 * <p>
	 * Some types of attachment (e.g. {@link InputStreamAttachment}) may not be
	 * aware of their actual size; in such cases, this method will either return
	 * zero or a potentially inaccurate estimate (as provided by
	 * <code>java.io.InputStream.available()</code>).
	 * </p>
	 * 
	 * @return the size of this attachment's raw data
	 * @throws AttachmentException if attachment processing is not supported
	 *             within the current runtime or obtaining the attachment failed
	 *             for any reason
	 * @see #getType()
	 * @see #getInputStream()
	 */
	public long size() throws AttachmentException;

	/**
	 * Returns the raw data from this attachment as array of bytes.
	 * <p>
	 * <strong>WARNING:</strong> The result can potentially use a large amount
	 * of memory. Furthermore, some environments or some types of attachments
	 * (see e.g. {@link InputStreamAttachment} or {@link FileAttachment}) may
	 * not support representing the attachment's raw data as a byte array. In
	 * these cases, a call to this method will cause an
	 * {@link AttachmentException} to get thrown (see {@link #getType()}.
	 * </p>
	 * 
	 * @return the attachment data as a byte array
	 * @throws AttachmentException if attachment processing is not supported
	 *             within the current runtime or obtaining the attachment failed
	 *             for any reason or in particular, when this attachment type
	 *             doesn't support byte array access to its raw data
	 * @throws IOException if reading raw attachment data failed
	 * @see #getType()
	 * @see #getInputStream()
	 */
	public byte[] getBytes() throws AttachmentException, IOException;

}
