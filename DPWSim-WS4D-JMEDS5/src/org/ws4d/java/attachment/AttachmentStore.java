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

import org.ws4d.java.DPWSFramework;
import org.ws4d.java.communication.ContextID;
import org.ws4d.java.structures.DataStructure;
import org.ws4d.java.structures.HashSet;
import org.ws4d.java.structures.Iterator;
import org.ws4d.java.structures.ReadOnlyIterator;
import org.ws4d.java.types.InternetMediaType;
import org.ws4d.java.util.Log;

/**
 * This class allows to store the attachments. Depending on the platform the
 * attachments are stored into files, or stay in memory.
 */
public abstract class AttachmentStore {

	/**
	 * This attachment store policy indicates that attachments <em>should</em>
	 * be kept within memory rather than stored on external media (file system,
	 * database, etc.).
	 */
	public static final int				POLICY_MEM_BUFFER						= 0x01;

	/**
	 * This attachment store policy indicates that attachments <em>should</em>
	 * be stored on external media (file system, database, etc.) rather than
	 * kept in memory.
	 */
	public static final int				POLICY_EXT_STORAGE						= 0x02;

	private static final String			ATTACHMENT_STORE_IMPLEMENTATION_CLASS	= "org.ws4d.java.attachment.DefaultAttachmentStore";

	private static AttachmentStore		instance;

	private static int					storePolicy								= POLICY_EXT_STORAGE;

	// contains InternetMediaType instances
	private static final DataStructure	STREAMING_MEDIA_TYPES					= new HashSet();

	static {
		try {
			AttachmentFactory aFac = DPWSFramework.getAttachmentFactory();
			Iterator it = aFac.getStreamingMediaTypes();
			while (it.hasNext()) {
				addStreamingMediaType((new InternetMediaType(it.next().toString())));
			}
		} catch (IOException e) {
			/*
			 * silent exception. we just cannot register types for streaming.
			 */
		}
	}

	public static AttachmentStore getInstance() throws AttachmentException {
		if (DPWSFramework.hasModule(DPWSFramework.ATTACHMENT_MODULE)) {
			if (instance == null) {
				try {
					Class clazz = Class.forName(ATTACHMENT_STORE_IMPLEMENTATION_CLASS);
					instance = (AttachmentStore) clazz.newInstance();
				} catch (Exception e) {
					throw new AttachmentException("Unable to create AttachmentStore instance: " + e);
				}
			}
			return instance;
		}
		throw new AttachmentException("Cannot initialize attachment store. Attachment is not supported.");
	}

	public static boolean exists() {
		return instance != null;
	}

	/**
	 * Returns the policy for storing attachment raw data used within this
	 * current runtime.
	 * 
	 * @return the attachment store policy of the current runtime/platform
	 * @see #POLICY_MEM_BUFFER
	 * @see #POLICY_EXT_STORAGE
	 */
	public static int getStorePolicy() {
		return storePolicy;
	}

	public static void setStorePolicy(int newStorePolicy) {
		if (newStorePolicy != POLICY_MEM_BUFFER && newStorePolicy != POLICY_EXT_STORAGE) {
			Log.error("Unknown attachment store policy, resetting to POLICY_MEM_BUFFER");
			newStorePolicy = POLICY_MEM_BUFFER;
		}
		storePolicy = newStorePolicy;
	}

	public static boolean addStreamingMediaType(InternetMediaType type) {
		if (type != null) {
			synchronized (STREAMING_MEDIA_TYPES) {
				return STREAMING_MEDIA_TYPES.add(type);
			}
		}
		return false;
	}

	public static boolean removeStreamingMediaType(InternetMediaType type) {
		if (type != null) {
			synchronized (STREAMING_MEDIA_TYPES) {
				return STREAMING_MEDIA_TYPES.remove(type);
			}
		}
		return false;
	}

	public static boolean isStreamingMediaType(InternetMediaType type) {
		if (type != null) {
			synchronized (STREAMING_MEDIA_TYPES) {
				return STREAMING_MEDIA_TYPES.contains(type);
			}
		}
		return false;
	}

	public static Iterator getStreamingMediaTypes() {
		DataStructure copy;
		synchronized (STREAMING_MEDIA_TYPES) {
			copy = new HashSet(STREAMING_MEDIA_TYPES);
		}
		return new ReadOnlyIterator(copy);
	}

	public static void resetStreamingMediaTypes() {
		synchronized (STREAMING_MEDIA_TYPES) {
			STREAMING_MEDIA_TYPES.clear();
		}
	}

	public abstract IncomingAttachment resolve(ContextID context, String cid) throws AttachmentException;

	public abstract void store(ContextID context, String cid, String contentType, String transferEncoding, InputStream from);

	public abstract boolean isAvailable(ContextID context, String cid);

	public abstract void cleanup();

}
