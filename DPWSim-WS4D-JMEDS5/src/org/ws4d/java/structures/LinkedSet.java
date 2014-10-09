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
 * This class is a hash set, in which the elements are linked together, ordered
 * by their adding to the set. The last added object is added to the end of the
 * sequence. Re-adding an already contained object won't change the order of the
 * set. If touch ordering is set, accessed objects in the set will be moved to
 * the end of the sequence.
 */
public class LinkedSet extends HashSet {

	/**
	 * Constructor.
	 */
	public LinkedSet() {
		map = new LinkedMap();
	}

	/**
	 * Constructor.
	 * 
	 * @param data
	 */
	public LinkedSet(DataStructure data) {
		map = new LinkedMap(data.size());
		addAll(data);
	}

	/**
	 * Constructor.
	 * 
	 * @param initialCapacity
	 */
	public LinkedSet(int initialCapacity) {
		map = new LinkedMap(initialCapacity);
	}

	/**
	 * Constructor.
	 * 
	 * @param initialCapacity
	 * @param touchOrdering access Ordering
	 */
	public LinkedSet(int initialCapacity, boolean touchOrdering) {
		map = new LinkedMap(initialCapacity, touchOrdering);
	}

	// ---------------------------------------

	/**
	 * Moves the touched object to the end of the set if touch ordering is true
	 * and the given object is an element of the set.
	 * 
	 * @param obj
	 */
	public void touch(Object obj) {
		map.get(obj);
	}

	/**
	 * Removes the first element in the set.
	 * 
	 * @return Returns oldest element. If no element is in set, it returns
	 *         <code>null</code>.
	 */
	public Object removeFirst() {
		Object eldest = null;

		Iterator it = map.entrySet().iterator();
		if (it.hasNext()) {
			eldest = it.next();
			it.remove();
		} else {
			return null;
		}

		return ((HashMap.Entry) eldest).getKey();
	}
}
