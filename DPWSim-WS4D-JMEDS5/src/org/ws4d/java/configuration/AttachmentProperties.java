/*******************************************************************************
 * Copyright (c) 2009 MATERNA Information & Communications. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html. For further
 * project-related information visit http://www.ws4d.org. The most recent
 * version of the JMEDS framework can be obtained from
 * http://sourceforge.net/projects/ws4d-javame.
 ******************************************************************************/
package org.ws4d.java.configuration;

import org.ws4d.java.attachment.AttachmentStore;
import org.ws4d.java.structures.DataStructure;
import org.ws4d.java.structures.HashSet;
import org.ws4d.java.structures.Iterator;
import org.ws4d.java.structures.ReadOnlyIterator;
import org.ws4d.java.util.Log;
import org.ws4d.java.util.StringUtil;

/**
 * 
 */
public class AttachmentProperties implements PropertiesHandler {

	/**
	 * Attachment store policy in use<br />
	 * Default: {@link AttachmentStore#POLICY_MEM_BUFFER}
	 */
	public static final String	PROP_ATTACHMENT_STORE_POLICY	= "AttachmentStorePolicy";

	/**
	 * Maximum attachment size in bytes to accept<br />
	 * Default: 16777216
	 */
	public static final String	PROP_MAX_ATTACHMENT_SIZE		= "MaxAttachmentSize";

	/**
	 * Maximum size in bytes for which attachment raw data can be kept into
	 * memory when using {@link AttachmentStore#POLICY_MEM_BUFFER}.
	 * <strong>MUST</strong> be less than or equal to
	 * {@link #PROP_MAX_ATTACHMENT_SIZE}.<br />
	 * Default: 65536
	 */
	public static final String	PROP_MAX_MEM_BUFFER_SIZE		= "MaxMemBufferSize";

	/**
	 * Path to the file system location where temporary attachment files should
	 * be stored. <br />
	 * Default: attachment_store
	 */
	public static final String	PROP_STORE_PATH					= "StorePath";

	/**
	 * Size of buffer to allocate when reading in attachments from a network or
	 * file stream. <br/>
	 * Default: 8192
	 */
	public static final String	PROP_READ_BUFFER_SIZE			= "ReadBufferSize";

	/**
	 * Size of buffer to allocate when reading in attachments with streaming
	 * support, see {@link InputStreamAttachment} and
	 * {@link AttachmentStore#getStreamingMediaTypes()}. <br/>
	 * Default: 128
	 */
	public static final String	PROP_STREAM_BUFFER_SIZE			= "StreamBufferSize";

	/**
	 * MIME types to stream when sending as attachments. <br/>
	 * Default: none
	 */
	public static final String	PROP_STREAMING_MEDIA_TYPES		= "StreamingMediaTypes";

	// private static AttachmentProperties INSTANCE = null;

	private int					attachmentStorePolicy			= AttachmentStore.POLICY_EXT_STORAGE;

	private int					maxAttachmentSize				= 16777216;

	private int					maxMemBufferSize				= 65536;

	private String				storePath						= "attachment_store";

	private int					readBufferSize					= 8192;

	private int					streamBufferSize				= 128;

	private DataStructure		streamingMediaTypes				= new HashSet();

	// private static String className = null;

	// ---------------------------------------------------------------------

	AttachmentProperties() {
		super();
		// if (INSTANCE != null) {
		// throw new
		// RuntimeException("AttachmentProperties: class already instantiated!");
		// }
		// className = this.getClass().getName();
		// INSTANCE = this;
	}

	/**
	 * Returns instance of the attachment properties handler.
	 * 
	 * @return the singleton instance of the attachment properties
	 */
	public static AttachmentProperties getInstance() {
		// if (INSTANCE == null) {
		// INSTANCE = new AttachmentProperties();
		// }
		// return INSTANCE;
		return (AttachmentProperties) Properties.forClassName(Properties.ATTACHMENT_PROPERTIES_HANDLER_CLASS);
	}

	// /**
	// * Returns class name if object of this class has previously been created,
	// * else null.
	// *
	// * @return Class name if object of this class has previously been created,
	// * else null.
	// */
	// public static String getClassName() {
	// return className;
	// }

	/*
	 * (non-Javadoc)
	 * @see
	 * org.ws4d.java.configuration.PropertiesHandler#setProperties(org.ws4d.
	 * java.configuration.PropertyHeader, org.ws4d.java.configuration.Property)
	 */
	public void setProperties(PropertyHeader header, Property property) {
		if (Properties.HEADER_SUBSECTION_ATTACHMENT.equals(header)) {
			if (PROP_ATTACHMENT_STORE_POLICY.equals(property.key)) {
				// encode value as string, e.g. memory / file
				int storePolicy;
				if ("file".equals(property.value)) {
					storePolicy = AttachmentStore.POLICY_EXT_STORAGE;
				} else if ("memory".equals(property.value)) {
					storePolicy = AttachmentStore.POLICY_MEM_BUFFER;
				} else {
					Log.warn("Unexpected attachment store policy: " + property.value + ", resetting to POLICY_MEM_BUFFER");
					storePolicy = AttachmentStore.POLICY_MEM_BUFFER;
				}
				attachmentStorePolicy = storePolicy;
			} else if (PROP_MAX_ATTACHMENT_SIZE.equals(property.key)) {
				maxAttachmentSize = Integer.parseInt(property.value.trim());
			} else if (PROP_MAX_MEM_BUFFER_SIZE.equals(property.key)) {
				maxMemBufferSize = Integer.parseInt(property.value.trim());
			} else if (PROP_STORE_PATH.equals(property.key)) {
				storePath = property.value;
			} else if (PROP_READ_BUFFER_SIZE.equals(property.key)) {
				readBufferSize = Integer.parseInt(property.value.trim());
			} else if (PROP_STREAM_BUFFER_SIZE.equals(property.key)) {
				streamBufferSize = Integer.parseInt(property.value.trim());
			} else if (PROP_STREAMING_MEDIA_TYPES.equals(property.key)) {
				String[] types = StringUtil.split(property.value, '|');
				for (int i = 0; i < types.length; i++) {
					streamingMediaTypes.add(types[i]);
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.configuration.PropertiesHandler#finishedSection(int)
	 */
	public void finishedSection(int depth) {
		AttachmentStore.setStorePolicy(getAttachmentStorePolicy());
	}

	/**
	 * @return the attachmentStorePolicy
	 */
	public int getAttachmentStorePolicy() {
		return attachmentStorePolicy;
	}

	/**
	 * @param attachmentStorePolicy the attachmentStorePolicy to set
	 */
	public void setAttachmentStorePolicy(int attachmentStorePolicy) {
		this.attachmentStorePolicy = attachmentStorePolicy;
	}

	/**
	 * @return the maxAttachmentSize
	 */
	public int getMaxAttachmentSize() {
		return maxAttachmentSize;
	}

	/**
	 * @param maxAttachmentSize the maxAttachmentSize to set
	 */
	public void setMaxAttachmentSize(int maxAttachmentSize) {
		this.maxAttachmentSize = maxAttachmentSize;
	}

	/**
	 * @return the maxMemBufferSize
	 */
	public int getMaxMemBufferSize() {
		return maxMemBufferSize;
	}

	/**
	 * @param maxMemBufferSize the maxMemBufferSize to set
	 */
	public void setMaxMemBufferSize(int maxMemBufferSize) {
		this.maxMemBufferSize = maxMemBufferSize;
	}

	/**
	 * @return the storePath
	 */
	public String getStorePath() {
		return storePath;
	}

	/**
	 * @param storePath the storePath to set
	 */
	public void setStorePath(String storePath) {
		this.storePath = storePath;
	}

	/**
	 * @return the readBufferSize
	 */
	public int getReadBufferSize() {
		return readBufferSize;
	}

	/**
	 * @param readBufferSize the readBufferSize to set
	 */
	public void setReadBufferSize(int readBufferSize) {
		this.readBufferSize = readBufferSize;
	}

	/**
	 * @return the streamBufferSize
	 */
	public int getStreamBufferSize() {
		return streamBufferSize;
	}

	/**
	 * @param streamBufferSize the streamBufferSize to set
	 */
	public void setStremBufferSize(int streamBufferSize) {
		this.streamBufferSize = streamBufferSize;
	}

	/**
	 * @return the streamingMediaTypes
	 */
	public Iterator getStreamingMediaTypes() {
		return new ReadOnlyIterator(new HashSet(streamingMediaTypes));
	}

	/**
	 * @param streamingMediaTypes the streamingMediaTypes to set
	 */
	public void setStreamingMediaTypes(DataStructure streamingMediaTypes) {
		this.streamingMediaTypes.clear();
		if (streamingMediaTypes != null) {
			this.streamingMediaTypes.addAll(streamingMediaTypes);
		}
	}

}
