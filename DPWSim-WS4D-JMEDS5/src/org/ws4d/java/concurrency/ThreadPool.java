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

import org.ws4d.java.structures.Iterator;
import org.ws4d.java.structures.LinkedList;
import org.ws4d.java.structures.List;
import org.ws4d.java.util.Log;

/**
 * Implements a simple thread pool which allows dynamic creation of worker
 * threads and automatic disposal of unused worker threads after the given
 * period of time.
 * <p>
 * Usage example:
 * </p>
 * <code>
 * <p>ThreadPool myThreadPool = new ThreadPool (3, 5000);<br />
 * myThreadPool.executeOrAbort(runnable1); // essential tasks, which must be started immediately or aborted<br />
 * myThreadPool.execute(runnable2); ...    // common tasks, which will be started as soon as possible <br />
 * myThreadPool.execute(runnableN);<br />
 * myThreadPool.shutdown();</p>
 * </code>
 */

public class ThreadPool {

	/**
	 * default size of the thread pool
	 */
	private static int		DEFAULT_SIZE	= 10;

	/**
	 * default time to live for idle available thread pool workers
	 */
	private static long		DEFAULT_TIMEOUT	= 10000;

	/**
	 * list of idle worker threads
	 */
	private final List		idleThreads		= new LinkedList();

	/**
	 * list of active worker threads
	 */
	private final List		activeThreads	= new LinkedList();

	/**
	 * queue with tasks waiting for any available worker thread
	 */
	private final List		waitingTasks	= new LinkedList();

	/**
	 * maximal number of threads in the pool
	 */
	private volatile int	size;

	/**
	 * life duration of idle available thread pool workers before which the idle
	 * threads will be disposed of
	 */
	private final long		timeout;

	/**
	 * Internal lock object
	 */
	private final Object	lock			= new Object();

	/**
	 * The constructor of the ThreadPool class, creating a thread pool with
	 * default size and default timeout.
	 */
	public ThreadPool() {
		this(DEFAULT_SIZE, DEFAULT_TIMEOUT);
	}

	/**
	 * The constructor of the ThreadPool class.
	 * 
	 * @param size maximal number of threads in the pool
	 * @param timeout life duration of idle thread pool worker
	 */
	public ThreadPool(int size, long timeout) {
		this.size = size;
		this.timeout = timeout;
	}

	/**
	 * The constructor of the ThreadPool class with the default life duration of
	 * idle threads
	 * 
	 * @param size maximal number of threads in the pool
	 */
	public ThreadPool(int size) {
		this(size, DEFAULT_TIMEOUT);
	}

	/**
	 * Signalizes to the thread pool that a worker thread has finished his task
	 * and is ready for further tasks. If there is no tasks waiting in the queue
	 * for execution, the worker thread is placed on the list of available
	 * thread workers. Else the thread worker gets the first waiting task
	 * assigned for the execution.
	 * 
	 * @param t worker thread which has finished its task
	 */
	private void signalAvailability(WorkerThread w) {
		synchronized (lock) {
			if (waitingTasks.size() > 0) {
				w.setTask((Runnable) waitingTasks.remove(0));
			} else {
				idleThreads.add(w);
				activeThreads.remove(w);
				w.setTask(null);
			}
		}
	}

	/**
	 * Signalizes to the thread pool that a thread worker is going to terminate
	 * itself.
	 * 
	 * @param t worker thread signaling the termination
	 */
	private void signalTermination(WorkerThread w) {
		idleThreads.remove(w);
	}

	/**
	 * Assigns tasks to the thread pool for execution. Use this method for
	 * common non-critical tasks. The tasks will be started as soon as possible.
	 * 
	 * @param task runnable which is assigned to the thread pool
	 */
	public void execute(Runnable task) {
		synchronized (lock) {
			if (!tryAllocation(task)) {
				waitingTasks.add(task);
			}
		}
	}

	/**
	 * Assigns tasks to the thread pool for execution. Use this method for
	 * essential tasks, which have to be immediately started or else aborted.
	 * 
	 * @param task runnable which is assigned to the thread pool
	 * @return boolean returns true only if the runnable was started
	 *         immediately, false if the runnable could not be started
	 *         immediately and was aborted
	 */
	public boolean executeOrAbort(Runnable task) {
		synchronized (lock) {
			if (!tryAllocation(task)) {
				return false;
			}
			return true;
		}
	}

	private boolean tryAllocation(Runnable task) {
		if (idleThreads.size() == 0) {
			if (activeThreads.size() < size) {
				WorkerThread w = new WorkerThread();
				allocate(w, task);
				w.start();

				return true;
			}
		} else {
			WorkerThread w = (WorkerThread) idleThreads.remove(0);
			allocate(w, task);

			return true;
		}
		return false;
	}

	private void allocate(WorkerThread w, Runnable task) {
		activeThreads.add(w);
		w.setTask(task);
	}

	/**
	 * Disposes of the thread pool. New tasks will be ignored. The currently
	 * running threads will be executed until the end.
	 */
	public void shutdown() {
		Thread thisThread = Thread.currentThread();

		List availableThreads = new LinkedList();
		synchronized (lock) {
			for (Iterator it = activeThreads.iterator(); it.hasNext();) {
				WorkerThread w = (WorkerThread) it.next();
				availableThreads.add(w);
				w.shutdown();
			}
			for (Iterator it = idleThreads.iterator(); it.hasNext();) {
				WorkerThread w = (WorkerThread) it.next();
				availableThreads.add(w);
				w.shutdown();
				it.remove();
			}
		}
		for (Iterator it = availableThreads.iterator(); it.hasNext();) {
			WorkerThread w = (WorkerThread) it.next();
			if (w != thisThread) {
				try {
					w.join();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * Implements a work delegation thread - worker thread class.
	 */
	class WorkerThread extends Thread {

		private volatile boolean		shutdown	= false;

		/**
		 * task that is to be executed by the worker thread
		 */
		private volatile Runnable	task;

		public void run() {
			while (true) {
				synchronized (this) {
					if (!shutdown && (task == null)) {
						try {
							wait(timeout);
						} catch (InterruptedException e) {
							// swallow
						}
						if (shutdown) {
							return;
						}
					}
				}
				synchronized (lock) {
					if (task == null) {
						shutdown = true;
						signalTermination(this);

						return;
					}
				}
				try {
					task.run();
				} catch (Exception e) {
					Log.error("Exception occurred while running thread. " + e.getMessage());
					Log.printStackTrace(e);
				} finally {
					signalAvailability(this);
				}
			}
		}

		/**
		 * Setter method for the task variable
		 * 
		 * @param task task to be assigned to the worker thread
		 */
		public synchronized void setTask(Runnable task) {
			this.task = task;
			notify();
		}

		public synchronized void shutdown() {
			shutdown = true;
			notify();
		}

	}

}
