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
 * Simple Queue. Thread safe!?! ;-)
 */
public class Queue {

	private LinkedList	queue;

	/**
	 * Create a new fine queue.
	 */
	public Queue() {
		queue = new LinkedList();
	}

	/**
	 * Tests that this queue has no components.
	 * 
	 * @return <code>true</code> if and only if this queue has no components,
	 *         that is, its size is zero; <code>false</code> otherwise.
	 */
	public synchronized boolean isEmpty() {
		return queue.isEmpty();
	}

	/**
	 * Enqueues element into this queue.
	 * 
	 * @param o element to enqueue.
	 */
	public void enqueue(Object o) {
		addElement(o, -1);
	}

	/**
	 * Adds elements to this queue, but the element will be set on the beginning
	 * of the queue.
	 * 
	 * @param o element to enqueue.
	 */
	public void enqueueAtBeginning(Object o) {
		addElement(o, 0);
	}

	/**
	 * Returns the first element of this queue. The first element is removed
	 * from queue, element at index 1 becomes first, etc.
	 * 
	 * @return The first element.
	 */
	public synchronized Object get() {
		Object o = getFirstElement();
		return o;
	}

	/**
	 * Returns the first element of this queue WITHOUT removing it!
	 * 
	 * @return The first element.
	 */
	public synchronized Object checkFirst() {
		Object o = checkFirstElement();
		return o;
	}

	/**
	 * Removes all entries from this queue.
	 */
	public synchronized void clear() {
		queue.clear();
		this.notifyAll();
	}

	/**
	 * Returns an iterator for this queue.
	 * <p>
	 * This iterator allows to access every item inside this queue and supports the remove method.
	 * </p>
	 * 
	 * @return the iterator for this queue.
	 */
	public synchronized Iterator iterator() {
		return queue.iterator();
	}

	/**
	 * Returns the number of components in this queue.
	 * 
	 * @return the number of components in this queue.
	 */
	public synchronized int size() {
		return queue.size();
	}

	/**
	 * Returns the first element of this queue. The first element is removed
	 * from queue, element at index 1 becomes first, etc.
	 * 
	 * @return the first element.
	 */
	protected synchronized Object getFirstElement() {
		Object o = queue.getFirst();
		queue.remove(o);
		this.notifyAll();
		return o;
	}

	/**
	 * Returns the first element of this queue.
	 * 
	 * @return the first element.
	 */
	protected synchronized Object checkFirstElement() {
		Object o = queue.getFirst();
		return o;
	}

	/**
	 * Adds element to vector at specified position. If the position is -1 the
	 * object will added to the end of the vector.
	 * 
	 * @param o object to add.
	 * @param index where to insert the object. If -1, the object will be added
	 *            at the end.
	 */
	protected synchronized void addElement(Object o, int index) {
		if (index == -1) {
			queue.add(o);
		} else {
			queue.add(index, o);
		}
		// notify about queue changes.
		this.notifyAll();
	}

}
