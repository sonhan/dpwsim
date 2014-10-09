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
 * Synchronized List Class, uses lockSupport for synchronization.
 * 
 * @author mspies
 */
public class LockedList extends List implements Lockable {

	/** lock **/
	private Lockable	lock;

	/** list to be synchronized */
	private List		listToSynchronize;

	/**
	 * Constructor. A new ArrayList will be created and synchronized.
	 */
	public LockedList() {
		this(new ArrayList());
	}

	/**
	 * Constructor.
	 * 
	 * @param list List, which should be synchronized
	 */
	public LockedList(List list) {
		this(list, new LockSupport());
	}

	/**
	 * Constructor.
	 * 
	 * @param list List, which should be synchronized
	 * @param lock Lock for access synchronization.
	 */
	public LockedList(List list, Lockable lock) {
		this.lock = lock;
		listToSynchronize = list;
	}

	// ------------------------------ LOCKABLE ------------------------------

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

	// ---------------------------- OVERRIDDEN SET METHODS
	// -------------------------------

	public void add(int index, Object obj) {
		try {
			lock.exclusiveLock();
			listToSynchronize.add(index, obj);
		} finally {
			lock.releaseExclusiveLock();
		}
	}

	public boolean addAll(int index, DataStructure data) {
		try {
			lock.exclusiveLock();
			return listToSynchronize.addAll(index, data);
		} finally {
			lock.releaseExclusiveLock();
		}
	}

	public Object get(int index) {
		try {
			lock.sharedLock();
			return listToSynchronize.get(index);
		} finally {
			lock.releaseSharedLock();
		}
	}

	public int indexOf(Object obj) {
		try {
			lock.sharedLock();
			return listToSynchronize.indexOf(obj);
		} finally {
			lock.releaseSharedLock();
		}
	}

	public int lastIndexOf(Object obj) {
		try {
			lock.sharedLock();
			return listToSynchronize.lastIndexOf(obj);
		} finally {
			lock.releaseSharedLock();
		}
	}

	public ListIterator listIterator() {
		try {
			lock.sharedLock();
			return listToSynchronize.listIterator();
		} finally {
			lock.releaseSharedLock();
		}
	}

	public ListIterator listIterator(int index) {
		try {
			lock.sharedLock();
			return listToSynchronize.listIterator(index);
		} finally {
			lock.releaseSharedLock();
		}
	}

	public Object remove(int index) {
		try {
			lock.exclusiveLock();
			return listToSynchronize.remove(index);
		} finally {
			lock.releaseExclusiveLock();
		}
	}

	public Object set(int index, Object obj) {
		try {
			lock.exclusiveLock();
			return listToSynchronize.set(index, obj);
		} finally {
			lock.releaseExclusiveLock();
		}
	}

	public List subList(int fromIndex, int toIndex) {
		try {
			lock.sharedLock();
			return listToSynchronize.subList(fromIndex, toIndex);
		} finally {
			lock.releaseExclusiveLock();
		}
	}

	// ------------------------------ OVERRIDDEN DATA STRUCTURE
	// METHODS---------------------------

	/**
	 * Manual synchronization of iterator necessary.
	 */
	public Iterator iterator() {
		return listToSynchronize.iterator();
	}

	public int size() {
		lock.sharedLock();
		try {
			return listToSynchronize.size();
		} finally {
			lock.releaseSharedLock();
		}
	}

	public boolean contains(Object obj) {
		lock.sharedLock();
		try {
			return listToSynchronize.contains(obj);
		} finally {
			lock.releaseSharedLock();
		}
	}

	public boolean remove(Object obj) {
		lock.exclusiveLock();
		try {
			return listToSynchronize.remove(obj);
		} finally {
			lock.releaseExclusiveLock();
		}
	}

	public void clear() {
		lock.exclusiveLock();
		try {
			listToSynchronize.clear();
		} finally {
			lock.releaseExclusiveLock();
		}
	}

	public boolean add(Object obj) {
		lock.exclusiveLock();
		try {
			return listToSynchronize.add(obj);
		} finally {
			lock.releaseExclusiveLock();
		}
	}

	public boolean addAll(DataStructure data) {
		lock.exclusiveLock();
		try {
			return listToSynchronize.addAll(data);
		} finally {
			lock.releaseExclusiveLock();
		}
	}

	public boolean containsAll(DataStructure data) {
		lock.sharedLock();
		try {
			return listToSynchronize.containsAll(data);
		} finally {
			lock.releaseSharedLock();
		}
	}

	public boolean isEmpty() {
		lock.sharedLock();
		try {
			return listToSynchronize.isEmpty();
		} finally {
			lock.releaseSharedLock();
		}
	}

	public Object[] toArray() {
		lock.sharedLock();
		try {
			return listToSynchronize.toArray();
		} finally {
			lock.releaseSharedLock();
		}
	}

	public Object[] toArray(Object[] object) {
		lock.sharedLock();
		try {
			return listToSynchronize.toArray(object);
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
			return listToSynchronize.hashCode();
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
			return listToSynchronize.equals(o);
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
			return listToSynchronize.toString();
		} finally {
			lock.releaseSharedLock();
		}
	}
}
