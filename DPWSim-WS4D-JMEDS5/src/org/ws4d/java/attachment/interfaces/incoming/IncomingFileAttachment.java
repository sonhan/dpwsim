/*******************************************************************************
 * Copyright (c) 2009 MATERNA Information & Communications. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html. For further
 * project-related information visit http://www.ws4d.org. The most recent
 * version of the JMEDS framework can be obtained from
 * http://sourceforge.net/projects/ws4d-javame.
 ******************************************************************************/
package org.ws4d.java.attachment.interfaces.incoming;

import java.io.IOException;

import org.ws4d.java.attachment.AttachmentException;
import org.ws4d.java.attachment.IncomingAttachment;

public interface IncomingFileAttachment extends IncomingAttachment {

	/**
	 * Returns the path to the file encapsulated by this attachment. This method
	 * is only legal for attachments of {@link #getType() type}
	 * {@link #FILE_ATTACHMENT}. For any other types, it will throw an
	 * {@link AttachmentException}.
	 * 
	 * @return the path to the file containing the attachments raw data
	 */
	public String getFilePath() throws AttachmentException;

	/**
	 * Copies the attachment's raw data to the target file path (e.g.
	 * "C:/folder/file.gif"). This method can only be used for attachments of
	 * {@link #getType() type} {@link #FILE_ATTACHMENT}. In any other cases it
	 * will throw an {@link AttachmentException}.
	 * 
	 * @param targetFilePath the new path within the local file system to store
	 *            the file to
	 * @throws AttachmentException if attachment processing is not supported
	 *             within the current runtime or obtaining the attachment failed
	 *             for any reason
	 * @throws IOException in case writing to the local file system failed for
	 *             any reason
	 */
	public void save(String targetFilePath) throws AttachmentException, IOException;

	/**
	 * This method moves the file containing the attachment's raw data to the
	 * given target file path (e.g. "C:/folder/file.gif"). It can only be used
	 * for attachments of {@link #getType() type} {@link #FILE_ATTACHMENT}. In
	 * any other cases it will throw an {@link AttachmentException}.
	 * 
	 * @param newFilePath the new path within the local file system to move the
	 *            file to
	 * @return <code>true</code>, if the file was moved/renamed successfully,
	 *         <code>false</code> otherwise
	 * @throws AttachmentException if attachment processing is not supported
	 *             within the current runtime or obtaining the attachment failed
	 *             for any reason
	 */
	public boolean move(String newFilePath) throws AttachmentException;

	/**
	 * Returns <code>true</code>, if this attachment was created locally. This
	 * is synonymous to the sender/originator of this attachment instance
	 * residing within the same Java virtual machine as its receiver.
	 * <p>
	 * This method is especially important for attachments of {@link #getType()
	 * type} {@link #FILE_ATTACHMENT}, as - if it returns <code>true</code> -
	 * this denotes that the {@link #getFilePath() file} the attachment points
	 * to is the original one. That is, when attempting to {@link #move(String)}
	 * a file attachment, this allows to distinguish whether the original file
	 * would be moved to a new location or whether simply the attachment file
	 * will be moved out of the local attachment store (used for caching
	 * incoming attachments) to a place outside of it.
	 * 
	 * @return <code>true</code> only if this attachment was originally created
	 *         locally (within the same JVM)
	 */
	public boolean isLocal();

}
