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
 * Class encapsulates data structure, method invocations are synchronized by a
 * Lockable object.
 */
public class LockedStructure extends DataStructure implements Lockable {

	/** data structure to be synchronized */
	DataStructure	dataToSynchronize;

	/** lock **/
	Lockable		lock;

	/**
	 * Constructor.
	 * 
	 * @param data data structure which will be encapsulated by this.
	 * @param lock Lock for access synchronization.
	 */
	public LockedStructure(DataStructure data, Lockable lock) {
		this.dataToSynchronize = data;
		this.lock = lock;
	}

	/**
	 * Constructor.
	 * 
	 * @param data data structure which will be encapsulated by this.
	 */
	public LockedStructure(DataStructure data) {
		this.dataToSynchronize = data;
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

	// ======================== DATA STRUCTURE ==========================

	/**
	 * Manual synchronization of iterator necessary.
	 */
	public Iterator iterator() {
		return dataToSynchronize.iterator();
	}

	public int size() {
		lock.sharedLock();
		try {
			return dataToSynchronize.size();
		} finally {
			lock.releaseSharedLock();
		}
	}

	public boolean contains(Object obj) {
		lock.sharedLock();
		try {
			return dataToSynchronize.contains(obj);
		} finally {
			lock.releaseSharedLock();
		}
	}

	public boolean remove(Object obj) {
		lock.exclusiveLock();
		try {
			return dataToSynchronize.remove(obj);
		} finally {
			lock.releaseExclusiveLock();
		}
	}

	public void clear() {
		lock.exclusiveLock();
		try {
			dataToSynchronize.clear();
		} finally {
			lock.releaseExclusiveLock();
		}
	}

	public boolean add(Object obj) {
		lock.exclusiveLock();
		try {
			return dataToSynchronize.add(obj);
		} finally {
			lock.releaseExclusiveLock();
		}
	}

	public boolean addAll(DataStructure data) {
		lock.exclusiveLock();
		try {
			return dataToSynchronize.addAll(data);
		} finally {
			lock.releaseExclusiveLock();
		}
	}

	public boolean containsAll(DataStructure data) {
		lock.sharedLock();
		try {
			return dataToSynchronize.containsAll(data);
		} finally {
			lock.releaseSharedLock();
		}
	}

	public boolean isEmpty() {
		lock.sharedLock();
		try {
			return dataToSynchronize.isEmpty();
		} finally {
			lock.releaseSharedLock();
		}
	}

	public Object[] toArray() {
		lock.sharedLock();
		try {
			return dataToSynchronize.toArray();
		} finally {
			lock.releaseSharedLock();
		}
	}

	public Object[] toArray(Object[] object) {
		lock.sharedLock();
		try {
			return dataToSynchronize.toArray(object);
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
			return dataToSynchronize.hashCode();
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
			return dataToSynchronize.equals(o);
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
			return dataToSynchronize.toString();
		} finally {
			lock.releaseSharedLock();
		}
	}

}
