/*******************************************************************************
 * Copyright (c) 2009 MATERNA Information & Communications. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html. For further
 * project-related information visit http://www.ws4d.org. The most recent
 * version of the JMEDS framework can be obtained from
 * http://sourceforge.net/projects/ws4d-javame.
 ******************************************************************************/

package org.ws4d.java.util;

import org.ws4d.java.DPWSFramework;
import org.ws4d.java.concurrency.LockSupport;
import org.ws4d.java.structures.ArrayList;
import org.ws4d.java.structures.Iterator;
import org.ws4d.java.structures.LinkedList;
import org.ws4d.java.structures.List;
import org.ws4d.java.structures.ListIterator;

/**
 * Class
 * 
 * @author mspies
 */
public class WatchDog implements Runnable {

	/** extra time buffer for removing callbacks */
	final static int			ADDITIONAL_TIME_BEFORE_CALLBACK_REMOVAL	= 50;					// milliseconds

	/** list<TimedEntry> */
	protected final LinkedList	listEntries								= new LinkedList();

	/** <code>true</code> if class was started */
	private boolean				running									= false;

	/** lock protects list */
	protected LockSupport		lock									= new LockSupport();

	/** this */
	private static WatchDog		watchdog								= new WatchDog();

	/**
	 * Private constructor.
	 */
	private WatchDog() {}

	public static WatchDog getInstance() {
		return watchdog;
	}

	/**
	 * Registers timed object to observe.
	 * 
	 * @param timedEntry
	 * @param timeUntilTimeout
	 */
	public void register(TimedEntry timedEntry, long timeUntilTimeout) {

		lock.exclusiveLock();
		try {
			if (timedEntry.registered) {
				update(timedEntry, timeUntilTimeout);
				return;
			}

			timedEntry.setTimer(timeUntilTimeout);

			if (listEntries.size() == 0) {
				listEntries.add(timedEntry);
			} else {
				boolean added = false;
				ListIterator it = listEntries.listIterator(listEntries.size());
				while (it.hasPrevious()) {
					TimedEntry temp = (TimedEntry) it.previous();
					if (temp.compareTo(timedEntry) < 0) {
						if (it.hasNext()) {
							it.next();
						}
						it.add(timedEntry);
						added = true;
						break;
					}
				}
				if (!added) {
					it.add(timedEntry);
				}
			}
			timedEntry.registered = true;
		} finally {
			lock.releaseExclusiveLock();
		}
		synchronized (this) {
			this.notify();
		}
	}

	/**
	 * Disables timed entry from managed objects of watchdog. Removing will
	 * occur later.
	 * 
	 * @param timedEntry
	 */
	public void unregister(TimedEntry timedEntry) {
		// lock.exclusiveLock();
		lock.sharedLock();
		try {
			// XXX: Will be faster, if we can use entries of linked list.
			// listEntries.remove(timedEntry);
			// XXX: entry will be removed at timeout
			timedEntry.disabled = true;
		} finally {
			lock.releaseSharedLock();
			// lock.releaseExclusiveLock();
		}
	}

	/**
	 * Updates timed entry with new time until timeout within managed objects of
	 * watchdog.
	 * 
	 * @param timedEntry
	 * @param timeUntilTimeout
	 */
	public void update(TimedEntry timedEntry, long timeUntilTimeout) {
		List timeoutObjects = new ArrayList(3);

		lock.exclusiveLock();
		try {
			if (!timedEntry.registered) {
				register(timedEntry, timeUntilTimeout);
				return;
			}

			timedEntry.setTimer(timeUntilTimeout);
			long currentTime = System.currentTimeMillis();

			if (listEntries.size() > 1) {
				boolean added = false;
				ListIterator it = listEntries.listIterator();
				while (it.hasNext()) {
					TimedEntry entry = (TimedEntry) it.next();

					if (entry.equals(timedEntry)) {
						it.remove();
						entry.registered = false;
					} else if (entry.timeToRemove <= currentTime) {
						it.remove();
						if (!entry.disabled) {
							// XXX previous unregistered entries won't receive
							// timeout
							timeoutObjects.add(entry);
						}
						entry.registered = false;
						entry.disabled = false;
					} else if (entry.compareTo(timedEntry) > 0 && !added) {
						// if( it.hasPrevious() ){
						// it.previous();
						// }

						it.previous();
						it.add(timedEntry);
						added = true;

						if (!entry.registered) {
							// we only break, if entry removed from old position
							break;
						}
					} else if (!entry.registered) {
						// we only break, if entry removed from old position
						break;
					}

				}
				if (!added) {
					it.add(timedEntry);
				}
			}

			timedEntry.registered = true;
			timedEntry.disabled = false;
		} finally {
			lock.releaseExclusiveLock();
		}

		/*
		 * timeout for all removed objects
		 */
		callTimeouts(timeoutObjects);

		synchronized (this) {
			this.notify();
		}
	}

	// ------------------------ RUNNABLE ------------------------------

	/**
	 * Starts thread to remove timed out message requests.
	 */
	public void run() {
		running = true;

		while (running) {
			try {
				lock.sharedLock();
				//(INGO) potentially DANGEROUS because the variable size in class LinkedList is not volatile
				if (listEntries.size() == 0) {
					synchronized (this) {
						lock.releaseSharedLock();
						wait();
					}
				} else {
					TimedEntry e = (TimedEntry) listEntries.getFirst();
					long millis2Sleep = e.timeToRemove - System.currentTimeMillis();

					if (millis2Sleep > 0) {
						// some extra millis;
						millis2Sleep += ADDITIONAL_TIME_BEFORE_CALLBACK_REMOVAL;
						synchronized (this) {
							lock.releaseSharedLock();
							this.wait(millis2Sleep);
						}
					} else {
						lock.releaseSharedLock();
					}
					checkEntries();
				}
			} catch (InterruptedException e1) {
				// e1.printStackTrace();
			}
		}
	}

	/**
	 * Stops thread, which handles timed entries.
	 */
	public void stop() {
		running = false;
		synchronized (this) {
			notifyAll();
		}
		clearEntries();
	}

	// ---------------------------- PRIVATE ----------------------------

	private void clearEntries() {
		List timeoutObjects = new ArrayList(listEntries.size());

		lock.exclusiveLock();
		try {
			for (Iterator it = listEntries.iterator(); it.hasNext();) {
				final TimedEntry entry = (TimedEntry) it.next();
				it.remove();
				entry.registered = false;
				entry.disabled = false;
				timeoutObjects.add(entry);
			}
		} finally {
			lock.releaseExclusiveLock();
		}

		callTimeouts(timeoutObjects);
	}

	/**
	 * Removes all entries with expired lifetime. Sends timeout to all removed
	 * callbacks.
	 */
	private void checkEntries() {
		long currentTime = System.currentTimeMillis();
		List timeoutObjects = new ArrayList(5);

		lock.exclusiveLock();
		try {
			for (Iterator it = listEntries.iterator(); it.hasNext();) {
				TimedEntry entry = (TimedEntry) it.next();
				if (entry.timeToRemove <= currentTime) {
					it.remove();
					if (!entry.disabled) {
						// XXX previous unregistered entries won't receive
						// timeout
						timeoutObjects.add(entry);
					}
					entry.registered = false;
					entry.disabled = false;
				} else {
					break;
				}
			}
		} finally {
			lock.releaseExclusiveLock();
		}

		/*
		 * timeout for all removed objects
		 */
		callTimeouts(timeoutObjects);
	}

	/**
	 * Calls timeout callback methods of timed entries in separate threads.
	 * 
	 * @param timeoutObjects
	 */
	private void callTimeouts(List timeoutObjects) {
		for (Iterator it = timeoutObjects.iterator(); it.hasNext();) {
			final TimedEntry entry = (TimedEntry) it.next();
			DPWSFramework.getThreadPool().execute(new Runnable() {

				public void run() {
					entry.timedOut();
				}

			});
		}
	}

}
