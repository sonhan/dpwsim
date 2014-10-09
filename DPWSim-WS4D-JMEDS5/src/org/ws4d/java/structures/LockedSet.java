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
 * Class encapsulates set, method invocations are synchronized by a Lockable
 * object.
 * 
 * @author mspies
 */
public class LockedSet extends Set {

	/** data structure to be synchronized */
	private Set			lockedData;

	/** lock **/
	private Lockable	lock;

	/**
	 * Constructor.
	 */
	public LockedSet() {
		this(new HashSet());

	}

	/**
	 * Constructor.
	 * 
	 * @param set Set which this will encapsulate.
	 */
	public LockedSet(Set set) {
		this(set, new LockSupport());
	}

	/**
	 * Constructor.
	 * 
	 * @param set Set which this will encapsulate.
	 * @param lock Lock for access synchronization.
	 */
	public LockedSet(Set set, Lockable lock) {
		this.lockedData = set;
		this.lock = lock;
	}

	// ----------------------------- LOCKABLE -----------------------------

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

	// ----------------------------- OVERRIDDEN DATA STRUCTURE METHODS
	// -----------------------------

	public boolean add(Object obj) {
		lock.exclusiveLock();
		try {
			return lockedData.add(obj);
		} finally {
			lock.releaseExclusiveLock();
		}
	}

	public boolean addAll(DataStructure data) {
		lock.exclusiveLock();
		try {
			return lockedData.addAll(data);
		} finally {
			lock.releaseExclusiveLock();
		}
	}

	public void clear() {
		lock.exclusiveLock();
		try {
			lockedData.clear();
		} finally {
			lock.releaseExclusiveLock();
		}
	}

	public boolean contains(Object obj) {
		lock.sharedLock();
		try {
			return lockedData.contains(obj);
		} finally {
			lock.releaseSharedLock();
		}
	}

	public boolean containsAll(DataStructure data) {
		lock.sharedLock();
		try {
			return lockedData.containsAll(data);
		} finally {
			lock.releaseSharedLock();
		}
	}

	public boolean isEmpty() {
		lock.sharedLock();
		try {
			return lockedData.isEmpty();
		} finally {
			lock.releaseSharedLock();
		}
	}

	/**
	 * Manual synchronization of iterator necessary.
	 */
	public Iterator iterator() {
		return lockedData.iterator();
	}

	public boolean remove(Object obj) {
		lock.exclusiveLock();
		try {
			return lockedData.remove(obj);
		} finally {
			lock.releaseExclusiveLock();
		}
	}

	public int size() {
		lock.sharedLock();
		try {
			return lockedData.size();
		} finally {
			lock.releaseSharedLock();
		}
	}

	public Object[] toArray() {
		lock.sharedLock();
		try {
			return lockedData.toArray();
		} finally {
			lock.releaseSharedLock();
		}
	}

	public Object[] toArray(Object[] object) {
		lock.sharedLock();
		try {
			return lockedData.toArray(object);
		} finally {
			lock.releaseSharedLock();
		}
	}

	// ------------------------- OVERRIDDEN OBJECT METHODS
	// ------------------------------------

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		lock.sharedLock();
		try {
			return lockedData.hashCode();
		} finally {
			lock.releaseSharedLock();
		}
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object o) {
		lock.sharedLock();
		try {
			return lockedData.equals(o);
		} finally {
			lock.releaseSharedLock();
		}
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		lock.sharedLock();
		try {
			return lockedData.toString();
		} finally {
			lock.releaseSharedLock();
		}
	}

}
