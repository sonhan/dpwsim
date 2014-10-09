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
 * 
 */
class EmptyStructure extends DataStructure {

	private static final String	CANNOT_ADD		= "Cannot add to this data structure.";

	private static final String	CANNOT_REMOVE	= "Cannot remove from this data structure.";

	EmptyStructure() {
		super();
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return "{}";
	}

	public boolean add(Object obj) {
		throw new UnsupportedOperationException(CANNOT_ADD);
	}

	public boolean addAll(DataStructure data) {
		throw new UnsupportedOperationException(CANNOT_ADD);
	}

	public void clear() {
		throw new UnsupportedOperationException(CANNOT_REMOVE);
	}

	public boolean contains(Object obj) {
		return false;
	}

	public boolean containsAll(DataStructure data) {
		return false;
	}

	public boolean isEmpty() {
		return true;
	}

	public Iterator iterator() {
		return EmptyStructures.EMPTY_ITERATOR;
	}

	public boolean remove(Object obj) {
		throw new UnsupportedOperationException(CANNOT_REMOVE);
	}

	public int size() {
		return 0;
	}

	public Object[] toArray() {
		return EmptyStructures.EMPTY_OBJECT_ARRAY;
	}

	public Object[] toArray(Object[] object) {
		return EmptyStructures.EMPTY_OBJECT_ARRAY;
	}

}
