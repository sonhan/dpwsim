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

import org.ws4d.java.structures.HashMap.Entry;

public class HashSet extends Set {

	HashMap	map;

	/**
	 * Constructor.
	 */
	public HashSet() {
		map = new HashMap();
	}

	/**
	 * @param data
	 */
	public HashSet(DataStructure data) {
		map = new HashMap(data.size());
		addAll(data);
	}

	public HashSet(final int initialCapacity) {
		map = new HashMap(initialCapacity);
	}

	public boolean add(Object obj) {
		return (map.put(obj, null) == null);
	}

	/**
	 * Get the object from this HashSet that is equal to the given object. 
	 * 
	 * @param obj
	 * @return the equal object from this HashSet or <code>null</code> if it does not contained an equal object
	 */
	public Object get(Object obj) {
		Entry o = map.getEntry(obj);
		return (o != null) ? o.key : null;
	}

	public Iterator iterator() {
		return map.keySet().iterator();
	}

	public int size() {
		return map.size();
	}

	public boolean isEmpty() {
		return map.isEmpty();
	}

	public void clear() {
		map.clear();
	}

	public boolean contains(Object obj) {
		return map.containsKey(obj);
	}

	public boolean remove(Object obj) {
		boolean result = contains(obj);
		if (result) {
			map.remove(obj);
		}

		return result;
	}

}
