/*******************************************************************************
 * Copyright (c) 2009 MATERNA Information & Communications. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html. For further
 * project-related information visit http://www.ws4d.org. The most recent
 * version of the JMEDS framework can be obtained from
 * http://sourceforge.net/projects/ws4d-javame.
 ******************************************************************************/

package org.ws4d.java.structures;

import org.ws4d.java.concurrency.LockSupport;
import org.ws4d.java.concurrency.Lockable;

/**
 * Class synchronizes access to a map.
 */
public class LockedMap extends HashMap implements Lockable {

	private HashMap		mapToSynchronize;

	private LockSupport	lock;

	/**
	 * Constuctor. Uses new HashMap.
	 */
	public LockedMap() {
		this(new HashMap());
	}

	/**
	 * Constuctor.
	 * 
	 * @param map Map, which should be synchronized
	 */
	public LockedMap(HashMap map) {
		this.mapToSynchronize = map;
		this.lock = new LockSupport();
	}

	// ======================= LOCKABLE ================================

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.concurrency.locks.Lockable#sharedLock()
	 */
	public void sharedLock() {
		lock.sharedLock();
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.concurrency.locks.Lockable#exclusiveLock()
	 */
	public void exclusiveLock() {
		lock.exclusiveLock();
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.concurrency.locks.Lockable#releaseSharedLock()
	 */
	public void releaseSharedLock() {
		lock.releaseSharedLock();
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.concurrency.locks.Lockable#releaseExclusiveLock()
	 */
	public boolean releaseExclusiveLock() {
		return lock.releaseExclusiveLock();
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.concurrency.locks.Lockable#tryExclusiveLock()
	 */
	public boolean tryExclusiveLock() {
		return lock.tryExclusiveLock();
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.concurrency.locks.Lockable#trySharedLock()
	 */
	public boolean trySharedLock() {
		return lock.trySharedLock();
	}

	// ============================= MAP ================================

	public void clear() {
		lock.exclusiveLock();
		try {
			mapToSynchronize.clear();
		} finally {
			lock.releaseExclusiveLock();
		}
	}

	public boolean containsKey(Object key) {
		lock.sharedLock();
		try {
			return mapToSynchronize.containsKey(key);
		} finally {
			lock.releaseSharedLock();
		}
	}

	public boolean containsValue(Object value) {
		lock.sharedLock();
		try {
			return mapToSynchronize.containsKey(value);
		} finally {
			lock.releaseSharedLock();
		}
	}

	public Set entrySet() {
		lock.exclusiveLock();
		try {
			if (entrySet == null) {
				entrySet = new LockedSet(mapToSynchronize.entrySet(), lock);
			}
			return entrySet;
		} finally {
			lock.releaseExclusiveLock();
		}
	}

	public Object get(Object key) {
		lock.sharedLock();
		try {
			return mapToSynchronize.get(key);
		} finally {
			lock.releaseSharedLock();
		}
	}

	public boolean isEmpty() {
		lock.sharedLock();
		try {
			return mapToSynchronize.isEmpty();
		} finally {
			lock.releaseSharedLock();
		}
	}

	public Object put(Object key, Object value) {
		lock.exclusiveLock();
		try {
			return mapToSynchronize.put(key, value);
		} finally {
			lock.releaseExclusiveLock();
		}
	}

	public void putAll(HashMap t) {
		lock.exclusiveLock();
		try {
			mapToSynchronize.putAll(t);
		} finally {
			lock.releaseExclusiveLock();
		}
	}

	public Object remove(Object key) {
		lock.exclusiveLock();
		try {
			return mapToSynchronize.remove(key);
		} finally {
			lock.releaseExclusiveLock();
		}
	}

	public int size() {
		lock.sharedLock();
		try {
			return mapToSynchronize.size();
		} finally {
			lock.releaseSharedLock();
		}
	}

	public DataStructure values() {
		lock.exclusiveLock();
		try {
			if (values == null) {
				values = new LockedStructure(mapToSynchronize.values(), lock);
			}
			return values;
		} finally {
			lock.releaseExclusiveLock();
		}
	}

	public Set keySet() {
		lock.exclusiveLock();
		try {
			if (keySet == null) {
				keySet = new LockedSet(mapToSynchronize.keySet(), lock);
			}
			return keySet;
		} finally {
			lock.releaseExclusiveLock();
		}
	}

	public int hashCode() {
		lock.sharedLock();
		try {
			return mapToSynchronize.hashCode();
		} finally {
			lock.releaseSharedLock();
		}
	}

	public boolean equals(Object o) {
		lock.sharedLock();
		try {
			return mapToSynchronize.equals(o);
		} finally {
			lock.releaseSharedLock();
		}
	}

	public String toString() {
		lock.sharedLock();
		try {
			return mapToSynchronize.toString();
		} finally {
			lock.releaseSharedLock();
		}
	}
}
