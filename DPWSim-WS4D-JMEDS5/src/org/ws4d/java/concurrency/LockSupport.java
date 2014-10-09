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

import org.ws4d.java.structures.HashMap;
import org.ws4d.java.structures.Iterator;
import org.ws4d.java.structures.LinkedList;
import org.ws4d.java.structures.List;
import org.ws4d.java.util.WS4DIllegalStateException;

/**
 * Implementation of a multiple-readers/single-writer lock support.
 */
public class LockSupport implements Lockable {

	/**
	 * Enables or disables the visualization of lock access and release.
	 */
//	private static boolean		showLocks							= false;

	/**
	 * Contains the packages or classes which should be used to show locks.
	 */
//	private static Set			showLocksFor						= null;
//
//	private static final int	SHOW_LOCK_STATE_EXCLUSIVE_WAIT		= 0;
//
//	private static final int	SHOW_LOCK_STATE_EXCLUSIVE_GOT		= 1;
//
//	private static final int	SHOW_LOCK_STATE_EXCLUSIVE_RELEASE	= 2;
//
//	private static final int	SHOW_LOCK_STATE_SHARED_WAIT			= 3;
//
//	private static final int	SHOW_LOCK_STATE_SHARED_GOT			= 4;
//
//	private static final int	SHOW_LOCK_STATE_SHARED_RELEASE		= 5;

	/**
	 * Constant which indicates that the subject is currently unlocked.
	 */
	private static final int	UNLOCKED							= 0;

	/**
	 * Constant which indicates that the subject has only shared locks on it.
	 */
	private static final int	SHARED_LOCKED						= 1;

	/**
	 * Constant which indicates that the subject is locked exclusively.
	 */
	private static final int	EXCLUSIVE_LOCKED					= 2;

	/** Map(Thread, Lock) The locks which are currently held by the threads. */
	private final HashMap		allocatedLocks;

	private int					allocatedSharedLockNum				= 0;

	private boolean				isExclusivelyLocked					= false;

	/** List(Lock) List containing lock requests. */
	private final List			waitingForLock;

	/** a thread is waiting to get an exclusive lock */
	private boolean				firstInListAwaitsExclusive			= false;

	/** a thread who has a shared lock, demands upgrade */
	private boolean				sharedAwaitingExclusive				= false;

//	static {
//		if (showLocks) {
//			showLocksFor = new HashSet();
//			/*
//			 * Add a some packages or classes to show locks for!
//			 */
//			showLocksFor.add("org.ws4d.java.service.DefaultDevice.start");
//			showLocksFor.add("org.ws4d.java.dispatch.DeviceServiceRegistry");
//		}
//	}

	/**
	 * Show log if possible. Depends on the possibility to receive the stack
	 * trace from the given toolkit.
	 * 
	 * @param state
	 */
//	static void logLockTrace(int state, Lock lock) {
//		if (Log.isDebug() && showLocks) {
//			String[] trace = Log.getStackTrace(new Exception());
//			if (trace != null) {
//				String t = "[thread=" + Thread.currentThread() + ", trace=" + trace[2] + ", lock=" + lock + "]";
//				Iterator it = showLocksFor.iterator();
//				while (it.hasNext()) {
//					String s = (String) it.next();
//					if (trace[2].indexOf(s) >= 0) {
//						switch (state) {
//							case SHOW_LOCK_STATE_EXCLUSIVE_WAIT:
//								Log.debug("=LOCK W(exclusive): " + t + ".", Log.DEBUG_LAYER_FRAMEWORK);
//								break;
//							case SHOW_LOCK_STATE_EXCLUSIVE_GOT:
//								Log.debug("+LOCK G(exclusive): " + t + ".", Log.DEBUG_LAYER_FRAMEWORK);
//								break;
//							case SHOW_LOCK_STATE_EXCLUSIVE_RELEASE:
//								Log.debug("-LOCK R(exclusive): " + t + ".", Log.DEBUG_LAYER_FRAMEWORK);
//								break;
//							case SHOW_LOCK_STATE_SHARED_WAIT:
//								Log.debug("=LOCK W(shared): " + t + ".", Log.DEBUG_LAYER_FRAMEWORK);
//								break;
//							case SHOW_LOCK_STATE_SHARED_GOT:
//								Log.debug("+LOCK G(shared): " + t + ".", Log.DEBUG_LAYER_FRAMEWORK);
//								break;
//							case SHOW_LOCK_STATE_SHARED_RELEASE:
//								Log.debug("-LOCK R(shared): " + t + ".", Log.DEBUG_LAYER_FRAMEWORK);
//								break;
//						}
//					}
//				}
//			}
//		}
//	}

	/**
	 * Constructs a new LockSupport.
	 */
	public LockSupport() {
		super();
		this.allocatedLocks = new HashMap();
		this.waitingForLock = new LinkedList();
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public synchronized String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("LockSupport (").append(hashCode()).append("): [");
		sb.append(" ASL=").append(allocatedSharedLockNum);
		sb.append(", AEL=").append(isExclusivelyLocked);
		sb.append(", allocatedLocks=").append(allocatedLocks);
		sb.append(", waiting=").append(waitingForLock);
		sb.append(" ]");
		return sb.toString();
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.concurrency.locks.Lockable#sharedLock()
	 */
	public void sharedLock() {
		Lock lock = null;
//		logLockTrace(SHOW_LOCK_STATE_SHARED_WAIT, lock);
		synchronized (this) {
			lock = getLockForCurrentThread();
			if (lock.tryAllocateShared()) {
//				logLockTrace(SHOW_LOCK_STATE_SHARED_GOT, lock);
				return;
			}
			lock.prepareAwaitSharedAllocation();
		}
		lock.awaitSharedAllocation();
//		logLockTrace(SHOW_LOCK_STATE_SHARED_GOT, lock);
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.concurrency.locks.Lockable#exclusiveLock()
	 */
	public void exclusiveLock() {
		Lock lock = null;
//		logLockTrace(SHOW_LOCK_STATE_EXCLUSIVE_WAIT, lock);
		synchronized (this) {
			lock = getLockForCurrentThread();
			if (lock.tryAllocateExclusive()) {
//				logLockTrace(SHOW_LOCK_STATE_EXCLUSIVE_GOT, lock);
				return;
			}
			lock.prepareAwaitExclusiveAllocation();
		}
		lock.awaitExclusiveAllocation();
//		logLockTrace(SHOW_LOCK_STATE_EXCLUSIVE_GOT, lock);
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.concurrency.locks.Lockable#trySharedLock()
	 */
	public synchronized boolean trySharedLock() {
		return getLockForCurrentThread().tryAllocateShared();
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.concurrency.locks.Lockable#tryExclusiveLock()
	 */
	public synchronized boolean tryExclusiveLock() {
		return getLockForCurrentThread().tryAllocateExclusive();
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.concurrency.locks.Lockable#releaseSharedLock()
	 */
	public synchronized void releaseSharedLock() {
		Lock lock = (Lock) allocatedLocks.get(Thread.currentThread());
		if ((lock == null) || (lock.tSharedLocksNum == 0)) {
			throw new WS4DIllegalStateException("Current thread has no allocated shared lock!");
		}

		if (lock.releaseShared()) {
			checkWaitingLockRequests();
//			logLockTrace(SHOW_LOCK_STATE_SHARED_RELEASE, lock);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.concurrency.locks.Lockable#releaseExclusiveLock()
	 */
	public synchronized boolean releaseExclusiveLock() {
		Lock lock = (Lock) allocatedLocks.get(Thread.currentThread());
		if ((lock == null) || (lock.tExclusiveLocksNum == 0)) {
			throw new WS4DIllegalStateException("Current thread has no allocated exclusive lock!");
		}

		if (lock.releaseExclusive()) {
			checkWaitingLockRequests();
//			logLockTrace(SHOW_LOCK_STATE_EXCLUSIVE_RELEASE, lock);
			return true;
		}
//		logLockTrace(SHOW_LOCK_STATE_EXCLUSIVE_RELEASE, lock);
		return false;
	}

	private int getState() {
		if (isExclusivelyLocked) {
			return EXCLUSIVE_LOCKED;
		}
		if (allocatedSharedLockNum > 0) {
			return SHARED_LOCKED;
		}
		return UNLOCKED;
	}

	private Lock getLockForCurrentThread() {
		Thread currentThread = Thread.currentThread();
		Lock lock = (Lock) allocatedLocks.get(currentThread);
		if (lock == null) {
			lock = new Lock(currentThread);
		}
		return lock;
	}

	/**
	 * Determines which queued lock requests can be performed.
	 */
	private void checkWaitingLockRequests() {
		for (Iterator it = waitingForLock.iterator(); it.hasNext();) {
			Lock lock = (Lock) it.next();
			if (lock.tryAllocateAfterDelay()) {
				it.remove();
			} else {
				break;
			}
		}

		if (waitingForLock.size() == 0)
			firstInListAwaitsExclusive = false;
		else
			firstInListAwaitsExclusive = ((Lock) waitingForLock.get(0)).isWaitingForExclusiveLock();
	}

	/**
	 * 
	 */
	private class Lock {

		private final Thread		thread;

		private volatile boolean	tHasLock					= false;

		private int					tSharedLocksNum				= 0;

		private volatile int		tExclusiveLocksNum			= 0;

		private static final int	WAITING_FOR_EXCLUSIVE_LOCK	= -1;

		private volatile long		lockNumber					= 0;

		public Lock(Thread thread) {
			super();
			this.lockNumber++;
			this.thread = thread;
		}

		public boolean isWaitingForExclusiveLock() {
			return tExclusiveLocksNum == WAITING_FOR_EXCLUSIVE_LOCK;
		}

		/*
		 * (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		public String toString() {
			StringBuffer sb = new StringBuffer();
			sb.append("Lock [ n=").append(lockNumber);
			sb.append(", thread=").append(thread);
			sb.append(", hl=").append(tHasLock);
			sb.append(", SL=").append(tSharedLocksNum);
			sb.append(", EL=").append(tExclusiveLocksNum);
			sb.append(" ]");
			return sb.toString();
		}

		boolean tryAllocateShared() {
			switch (getState()) {
				case UNLOCKED:
					allocateShared();
					return true;
				case SHARED_LOCKED:
					if (!tHasLock && firstInListAwaitsExclusive) {
						return false;
					}
					allocateShared();
					return true;
				case EXCLUSIVE_LOCKED:
					if (tExclusiveLocksNum > 0) {
						allocateShared();
						return true;
					}
					return false;
				default:
					return false;
			}
		}

		boolean tryAllocateExclusive() {
			switch (getState()) {
				case UNLOCKED:
					allocateExclusive();
					return true;
				case SHARED_LOCKED:
					if (tSharedLocksNum == allocatedSharedLockNum) {
						allocateExclusive();
						return true;
					}
					if (tSharedLocksNum > 0 && sharedAwaitingExclusive) {
						/*
						 * This thread has a shared lock and tries to upgrade,
						 * an other thread demands upgrade too, this will lead
						 * to deadlock.
						 */
						throw new DeadlockException("Deadlock because two threads try to upgrade. Implement exception handling for this case.");
					}
					return false;
				case EXCLUSIVE_LOCKED:
					if (tExclusiveLocksNum > 0) {
						allocateExclusive();
						return true;
					}
					return false;
				default:
					return false;
			}
		}

		/**
		 * Checks if the lock can be allocated.
		 * 
		 * @return <code>true</code> if the lock can be allocated,
		 *         <code>false</code> otherwise.
		 */
		boolean tryAllocateAfterDelay() {
			switch (getState()) {
				case UNLOCKED:
					if (tExclusiveLocksNum == WAITING_FOR_EXCLUSIVE_LOCK) { // wants
																			// exclusive
																			// lock
						isExclusivelyLocked = true;
						tExclusiveLocksNum = 1;
					} else { // wants shared lock
						allocatedSharedLockNum = 1;
					}
					allocateAfterDelay();
					return true;
				case SHARED_LOCKED:
					if (tExclusiveLocksNum == WAITING_FOR_EXCLUSIVE_LOCK) { // wants
																			// exclusive
																			// lock
						if (tSharedLocksNum == allocatedSharedLockNum) {
							sharedAwaitingExclusive = false;
							isExclusivelyLocked = true;
							allocateAfterDelay();
							return true;
						} else {
							return false;
						}
					} else { // wants shared lock
						allocatedSharedLockNum++;
						allocateAfterDelay();
						return true;
					}
				case EXCLUSIVE_LOCKED:
					if (tExclusiveLocksNum == WAITING_FOR_EXCLUSIVE_LOCK) { // wants
																			// exclusive
																			// lock
						return false;
					} else { // wants shared lock
						if (tExclusiveLocksNum > 0) {
							allocatedSharedLockNum++;
							allocateAfterDelay();
							return true;
						} else {
							return false;
						}
					}
				default:
					return false;
			}
		}

		private void allocateAfterDelay() {
			if (tExclusiveLocksNum == WAITING_FOR_EXCLUSIVE_LOCK) {
				tExclusiveLocksNum = 1;
			}
			allocate();
			// trigger resume within delayAllocation() of target thread
			synchronized (this) {
				notify();
			}
		}

		private void allocateShared() {
			tSharedLocksNum++;
			allocatedSharedLockNum++;
			allocate();
		}

		private void allocateExclusive() {
			tExclusiveLocksNum++;
			isExclusivelyLocked = true;
			allocate();
		}

		private void allocate() {
			tHasLock = true;
			allocatedLocks.put(thread, this);
		}

		boolean releaseShared() {
			tSharedLocksNum--;
			allocatedSharedLockNum--;
			if (tSharedLocksNum == 0 && tExclusiveLocksNum == 0) {
				allocatedLocks.remove(thread);
				tHasLock = false;
				return true;
			}
			return false;
		}

		boolean releaseExclusive() {
			tExclusiveLocksNum--;
			if (tExclusiveLocksNum == 0) {
				isExclusivelyLocked = false;
				if (tSharedLocksNum == 0) {
					allocatedLocks.remove(thread);
					tHasLock = false;
				}
				return true;
			}
			return false;
		}

		void prepareAwaitSharedAllocation() {
			tSharedLocksNum = 1;
			waitingForLock.add(this);
			if (waitingForLock.size() == 1) firstInListAwaitsExclusive = false;
		}

		void prepareAwaitExclusiveAllocation() {
			tExclusiveLocksNum = WAITING_FOR_EXCLUSIVE_LOCK;
			if (tSharedLocksNum > 0) {
				/*
				 * Has shared lock but does not get exclusive now.
				 */
				sharedAwaitingExclusive = true;
				firstInListAwaitsExclusive = true;
				waitingForLock.add(0, this);
			} else {
				if (waitingForLock.size() == 0) firstInListAwaitsExclusive = true;
				waitingForLock.add(this);
			}
		}

		synchronized void awaitSharedAllocation() {
			while (!tHasLock) {
				try {
					wait();
				} catch (InterruptedException e) {
					// time to check whether we can resume
				}
			}
		}

		synchronized void awaitExclusiveAllocation() {
			while (tExclusiveLocksNum == WAITING_FOR_EXCLUSIVE_LOCK) {
				try {
					wait();
				} catch (InterruptedException e) {
					// time to check whether we can resume
				}
			}
		}

	}
}