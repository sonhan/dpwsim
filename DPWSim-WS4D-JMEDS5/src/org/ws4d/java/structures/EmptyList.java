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

import java.util.NoSuchElementException;

class EmptyList extends List {

	private static final ListIterator	LIST_ITERATOR	= new ListIteratorImpl();

	private static final String			CANNOT_ADD		= "Cannot add to this list.";

	private static final String			CANNOT_REMOVE	= "Cannot remove from this list.";

	private static final String			CANNOT_MODIFY	= "Cannot modify this list.";

	EmptyList() {
		super();
	}

	public void add(int index, Object obj) {
		throw new UnsupportedOperationException(CANNOT_ADD);
	}

	public boolean add(Object obj) {
		throw new UnsupportedOperationException(CANNOT_REMOVE);
	}

	public boolean addAll(DataStructure data) {
		throw new UnsupportedOperationException(CANNOT_REMOVE);
	}

	public boolean addAll(int index, DataStructure data) {
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

	public Object get(int index) {
		return null;
	}

	public int indexOf(Object obj) {
		return -1;
	}

	public boolean isEmpty() {
		return true;
	}

	public Iterator iterator() {
		return EmptyStructures.EMPTY_ITERATOR;
	}

	public int lastIndexOf(Object obj) {
		return -1;
	}

	public ListIterator listIterator() {
		return LIST_ITERATOR;
	}

	public ListIterator listIterator(int index) {
		return LIST_ITERATOR;
	}

	public Object remove(int index) {
		throw new UnsupportedOperationException(CANNOT_REMOVE);
	}

	public boolean remove(Object obj) {
		throw new UnsupportedOperationException(CANNOT_REMOVE);
	}

	public Object set(int index, Object obj) {
		throw new UnsupportedOperationException(CANNOT_MODIFY);
	}

	public int size() {
		return 0;
	}

	public List subList(int fromIndex, int toIndex) {
		return this;
	}

	public Object[] toArray() {
		return EmptyStructures.EMPTY_OBJECT_ARRAY;
	}

	public Object[] toArray(Object[] object) {
		return EmptyStructures.EMPTY_OBJECT_ARRAY;
	}

	public String toString() {
		return "{}";
	}

	// ============================== INNER CLASS
	// ================================

	private static class ListIteratorImpl extends EmptyIterator implements ListIterator {

		public void add(Object obj) {
			throw new UnsupportedOperationException(CANNOT_ADD);
		}

		public boolean hasPrevious() {
			return false;
		}

		public int indexOfNext() {
			return 0;
		}

		public Object previous() {
			throw new NoSuchElementException();
		}

		public void set(Object obj) {
			throw new UnsupportedOperationException(CANNOT_MODIFY);
		}

	}
}
