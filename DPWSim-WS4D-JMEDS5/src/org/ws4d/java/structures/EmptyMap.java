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

class EmptyMap extends HashMap {

	private static final String	CANNOT_ADD		= "Cannot add to this map.";

	private static final String	CANNOT_REMOVE	= "Cannot remove from this map.";

	EmptyMap() {
		super(1);
	}

	public void clear() {
		throw new UnsupportedOperationException(CANNOT_REMOVE);
	}

	public boolean containsKey(Object key) {
		return false;
	}

	public boolean containsValue(Object value) {
		return false;
	}

	public Set entrySet() {
		return EmptyStructures.EMPTY_SET;
	}

	public Object get(Object key) {
		return null;
	}

	public boolean isEmpty() {
		return true;
	}

	public Set keySet() {
		return EmptyStructures.EMPTY_SET;
	}

	public Object put(Object key, Object value) {
		throw new UnsupportedOperationException(CANNOT_ADD);
	}

	public void putAll(HashMap t) {
		throw new UnsupportedOperationException(CANNOT_ADD);
	}

	public Object remove(Object key) {
		throw new UnsupportedOperationException(CANNOT_REMOVE);
	}

	public int size() {
		return 0;
	}

	public DataStructure values() {
		return EmptyStructures.EMPTY_STRUCTURE;
	}

	public String toString() {
		return "{}";
	}

}
