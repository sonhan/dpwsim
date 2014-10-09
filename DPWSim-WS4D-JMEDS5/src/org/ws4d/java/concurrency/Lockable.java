/*******************************************************************************
 * Copyright (c) 2009 MATERNA Information & Communications. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html. For further
 * project-related information visit http://www.ws4d.org. The most recent
 * version of the JMEDS framework can be obtained from
 * http://sourceforge.net/projects/ws4d-javame.
 ******************************************************************************/

package org.ws4d.java.concurrency;

public interface Lockable {

	/**
	 * Acquires a shared lock. If the lock cannot be allocated immediately, the
	 * thread is blocked until allocation is possible.
	 */
	public void sharedLock();

	/**
	 * Acquires an exclusive lock. If the lock cannot be allocated immediately,
	 * the thread is blocked until allocation is possible.
	 */
	public void exclusiveLock();

	/**
	 * Tries to get a shared lock immediately.
	 * 
	 * @return <code>true</code> if the lock has been allocated,
	 *         <code>false</code> otherwise
	 */
	public boolean trySharedLock();

	/**
	 * Try to get an exclusive lock immediately.
	 * 
	 * @return <code>true</code> if the lock has been allocated,
	 *         <code>false</code> otherwise
	 */
	public boolean tryExclusiveLock();

	/**
	 * Releases a shared lock of the current thread.
	 */
	public void releaseSharedLock();

	/**
	 * Releases an exclusive lock of the current thread.
	 * 
	 * @return true = if last exclusive lock of this thread is released.
	 */
	public boolean releaseExclusiveLock();

}
