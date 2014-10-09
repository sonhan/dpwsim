/*******************************************************************************
 * Copyright (c) 2009 MATERNA Information & Communications. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html. For further
 * project-related information visit http://www.ws4d.org. The most recent
 * version of the JMEDS framework can be obtained from
 * http://sourceforge.net/projects/ws4d-javame.
 ******************************************************************************/
package org.ws4d.java.communication;

import java.io.IOException;

import org.ws4d.java.concurrency.LockSupport;
import org.ws4d.java.concurrency.Lockable;
import org.ws4d.java.constants.FrameworkConstants;
import org.ws4d.java.structures.ArrayList;
import org.ws4d.java.structures.DataStructure;
import org.ws4d.java.structures.HashMap;
import org.ws4d.java.structures.Iterator;
import org.ws4d.java.structures.ReadOnlyIterator;
import org.ws4d.java.util.Log;

/**
 *
 */
public abstract class CommunicationManagerRegistry {

	public static final String		DEFAULT_CM_PACKAGE				= FrameworkConstants.DEFAULT_PACKAGENAME + ".communication";

	public static final String		DEFAULT_CM_SUFFIX				= "CommunicationManager";

	/**
	 * This array contains the communication IDs of all default communication
	 * managers. When a call to the method {@link #loadAll()} is made, the
	 * registry will attempt to instantiate and
	 * {@link CommunicationManager#start() start} each one listed herein. The
	 * first entry within the array has the special meaning of identifying the
	 * default communication technology to use when sending requests or one-way
	 * messages if none have been explicitly specified
	 */
	public static final String[]	DEFAULT_COMMUNICATION_MANAGERS	= { "DPWS" };

	private static final HashMap	COM_MANAGERS					= new HashMap(5);

	private static final Lockable	lockSupport						= new LockSupport();

	public static String getDefault() {
		return DEFAULT_COMMUNICATION_MANAGERS.length > 0 ? DEFAULT_COMMUNICATION_MANAGERS[0] : CommunicationManager.ID_NULL;
	}

	public static void loadAll() {
		for (int i = 0; i < DEFAULT_COMMUNICATION_MANAGERS.length; i++) {
			load(DEFAULT_COMMUNICATION_MANAGERS[i]);
		}
	}

	public static void load(String comManId) {
		loadInternal(comManId);
	}

	public static CommunicationManager getManager(String comManId) {
		lockSupport.sharedLock();
		try {
			return (CommunicationManager) COM_MANAGERS.get(comManId);
		} finally {
			lockSupport.releaseSharedLock();
		}
	}

	public static Iterator getLoadedManagers() {
		lockSupport.sharedLock();
		try {
			DataStructure copy = new ArrayList(COM_MANAGERS.size());
			copy.addAll(COM_MANAGERS.values());
			return new ReadOnlyIterator(copy);
		} finally {
			lockSupport.releaseSharedLock();
		}
	}

	public static void startAll() {
		lockSupport.sharedLock();
		try {
			for (Iterator it = COM_MANAGERS.values().iterator(); it.hasNext();) {
				CommunicationManager manager = (CommunicationManager) it.next();
				try {
					manager.start();
					if (Log.isDebug()) {
						Log.debug("Communication Manager " + manager.getCommunicationManagerId() + " started.", Log.DEBUG_LAYER_COMMUNICATION);
					}
				} catch (IOException e) {
					Log.error("Unable to start Communication Manager " + manager.getCommunicationManagerId() + ": " + e);
				}
			}
		} finally {
			lockSupport.releaseSharedLock();
		}
	}

	public static void stopAll() {
		lockSupport.sharedLock();
		try {
			for (Iterator it = COM_MANAGERS.values().iterator(); it.hasNext();) {
				CommunicationManager manager = (CommunicationManager) it.next();
				manager.stop();
			}
			lockSupport.exclusiveLock();
			try {
				COM_MANAGERS.clear();
			} finally {
				lockSupport.releaseExclusiveLock();
			}
		} finally {
			lockSupport.releaseSharedLock();
		}
	}

	public static void killAll() {
		lockSupport.sharedLock();
		try {
			for (Iterator it = COM_MANAGERS.values().iterator(); it.hasNext();) {
				CommunicationManager manager = (CommunicationManager) it.next();
				manager.kill();
			}
		} finally {
			lockSupport.releaseSharedLock();
		}
		lockSupport.exclusiveLock();
		try {
			COM_MANAGERS.clear();
		} finally {
			lockSupport.releaseExclusiveLock();
		}
	}

	/*
	 * This method assumes EXTERNAL synchronization on COM_MANAGERS!
	 */
	private static void loadInternal(String comManId) {
		lockSupport.exclusiveLock();
		try {
			if (comManId == CommunicationManager.ID_NULL || COM_MANAGERS.containsKey(comManId)) {
				return;
			}

			if (Log.isDebug()) {
				Log.debug("Loading Communication Manager " + comManId + "...", Log.DEBUG_LAYER_COMMUNICATION);
			}
			String className = DEFAULT_CM_PACKAGE + "." + comManId + DEFAULT_CM_SUFFIX;
			try {
				Class clazz = Class.forName(className);
				CommunicationManager manager = (CommunicationManager) clazz.newInstance();
				manager.init();
				COM_MANAGERS.put(comManId, manager);
				if (Log.isDebug()) {
					Log.debug("Communication Manager " + comManId + " initialized.", Log.DEBUG_LAYER_COMMUNICATION);
				}
			} catch (ClassNotFoundException e) {
				Log.error("Unable to find class " + className);
			} catch (IllegalAccessException e) {
				Log.error("Can not access class or default constructor of class " + className);
			} catch (InstantiationException e) {
				Log.error("Unable to create instance of class " + className);
			}
		} finally {
			lockSupport.releaseExclusiveLock();
		}
	}

	/*
	 * Disallow any instances from outside this class.
	 */
	private CommunicationManagerRegistry() {
		super();
	}

}
