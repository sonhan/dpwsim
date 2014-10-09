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

/**
 * Synchronized Queue. The enqueueing thread must wait, until someone takes the
 * object out of the queue.
 */
public class WaitingQueue extends Queue {

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.structures.Queue#get()
	 */
	protected Object getFirstElement() {
		return notifyObjectRemoved((SyncContainer)super.getFirstElement());
	}

	/* (non-Javadoc)
	 * @see org.ws4d.java.structures.Queue#addElement(java.lang.Object, int)
	 */
	protected void addElement(Object o, int index) {
		SyncContainer syncContainer = new SyncContainer(o);
		super.addElement(syncContainer, index);
		
		synchronized (syncContainer) {
			while (syncContainer.inQueue) {
				try {
					//Stops thread which enqueues the object, until someone takes it out of the queue.
					syncContainer.wait();
				} catch (InterruptedException e) {
				}
			}
		}
	}
	
	/**
	 * Returns an iterator for this queue.
	 * <p>
	 * This iterator allows to access every item inside this queue.
	 * If an element is removed using this iterator the waiting thread will be notified.
	 * </p>
	 * 
	 * @return the iterator for this queue.
	 */
	public synchronized Iterator iterator() {
		final Iterator superIter = super.iterator();
		
		return new Iterator() {

			private SyncContainer lastReturnedEntry = null;

			public void remove() {
				superIter.remove();
				notifyObjectRemoved(lastReturnedEntry);
			}
			
			public Object next() {
				lastReturnedEntry = (SyncContainer)superIter.next();
				return lastReturnedEntry.theObject;
			}
			
			public boolean hasNext() {
				return superIter.hasNext();
			}
		};
	}

	
	/**
	 * Removes all entries from this queue and notifies all waiting thread.
	 */
	public synchronized void clear() {
		Iterator iter = iterator();
		while (iter.hasNext()) {
			iter.next();
			iter.remove();
		}
		this.notifyAll();
	}

	private Object notifyObjectRemoved(SyncContainer syncContainer) {
		synchronized (syncContainer) {
			syncContainer.inQueue = false;
			syncContainer.notifyAll();
		}
		return syncContainer.theObject;		
	}

	private class SyncContainer {
		Object theObject;
		volatile boolean inQueue = true;
		
		SyncContainer(Object o) {
			theObject = o;
		}
	}
}
