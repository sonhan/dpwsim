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

import java.io.InputStream;

import org.ws4d.java.attachment.interfaces.outgoing.OutgoingOutputStreamAttachment;
import org.ws4d.java.structures.Iterator;
import org.ws4d.java.types.InternetMediaType;

public interface AttachmentFactory {

	// FileAttachment
	/**
	 * @param filePath
	 * @return
	 */
	public OutgoingAttachment createFileAttachment(String filePath);

	/**
	 * @param filePath
	 * @param contentType
	 * @return
	 */
	public OutgoingAttachment createFileAttachment(String filePath, InternetMediaType contentType);

	/**
	 * @param filePath
	 * @param contentType
	 * @return
	 */
	public OutgoingAttachment createFileAttachment(String filePath, String contentType);

	/**
	 * @param filePath
	 * @param contentId
	 * @param contentType
	 * @return
	 */
	public OutgoingAttachment createFileAttachment(String filePath, String contentId, InternetMediaType contentType);

	/**
	 * @param filePath
	 * @param contentId
	 * @param contentType
	 * @return
	 */
	public OutgoingAttachment createFileAttachment(String filePath, String contentId, String contentType);

	// MemoryAttachment
	/**
	 * @param bytes
	 * @return
	 */
	public OutgoingAttachment createMemoryAttachment(byte[] bytes);

	/**
	 * @param bytes
	 * @param contentType
	 * @return
	 */
	public OutgoingAttachment createMemoryAttachment(byte[] bytes, InternetMediaType contentType);

	/**
	 * @param bytes
	 * @param contentType
	 * @return
	 */
	public OutgoingAttachment createMemoryAttachment(byte[] bytes, String contentType);

	/**
	 * @param bytes
	 * @param contentId
	 * @param contentType
	 * @return
	 */
	public OutgoingAttachment createMemoryAttachment(byte[] bytes, String contentId, InternetMediaType contentType);

	/**
	 * @param bytes
	 * @param contentId
	 * @param contentType
	 * @return
	 */
	public OutgoingAttachment createMemoryAttachment(byte[] bytes, String contentId, String contentType);

	// OutgoingInputStreamAttachment
	/**
	 * @param in
	 * @return
	 */
	public OutgoingAttachment createStreamAttachment(InputStream in);

	/**
	 * @param in
	 * @param contentType
	 * @return
	 */
	public OutgoingAttachment createStreamAttachment(InputStream in, InternetMediaType contentType);

	/**
	 * @param in
	 * @param contentType
	 * @return
	 */
	public OutgoingAttachment createStreamAttachment(InputStream in, String contentType);

	/**
	 * @param in
	 * @param contentId
	 * @param contentType
	 * @return
	 */
	public OutgoingAttachment createStreamAttachment(InputStream in, String contentId, InternetMediaType contentType);

	/**
	 * @param in
	 * @param contentId
	 * @param contentType
	 * @return
	 */
	public OutgoingAttachment createStreamAttachment(InputStream in, String contentId, String contentType);

	// OutgoingOutputStreamAttachment
	/**
	 * @param contentType
	 * @return
	 */
	public OutgoingOutputStreamAttachment createStreamAttachment(InternetMediaType contentType);

	/**
	 * @param contentType
	 * @return
	 */
	public OutgoingOutputStreamAttachment createStreamAttachment(String contentType);

	/**
	 * @param contentId
	 * @param contentType
	 * @return
	 */
	public OutgoingOutputStreamAttachment createStreamAttachment(String contentId, InternetMediaType contentType);

	/**
	 * @param contentId
	 * @param contentType
	 * @return
	 */
	public OutgoingOutputStreamAttachment createStreamAttachment(String contentId, String contentType);

	/**
	 * @param contentId
	 * @param contentType
	 * @param trasferEncoding
	 * @return
	 */
	public OutgoingOutputStreamAttachment createStreamAttachment(String contentId, InternetMediaType contentType, String transferEncoding);

	/**
	 * @return
	 */
	public Iterator getStreamingMediaTypes();

}
