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

import org.ws4d.java.util.WS4DIllegalStateException;

/**
 * Implementation of a double linked list. Class is not synchronized.
 */
public class LinkedList extends List {

	protected final Entry	header	= new Entry(null, null, null);

	/**
	 * Constructor.
	 */
	public LinkedList() {
		super();
		header.next = header;
		header.previous = header;
	}

	/**
	 * Adds element between the previous and the old element at the specified
	 * index position.
	 * 
	 * @param index position of element to add ( 0==head, size==end of list).
	 * @param obj object to add
	 */
	public void add(int index, Object obj) throws IndexOutOfBoundsException {
		if (index < 0 || index > size) {
			throw new IndexOutOfBoundsException("index=" + index + ", size=" + size);
		}

		Entry nextEntry = getEntry(index);
		addPrevious(nextEntry, obj);
	}

	/**
	 * Adds object at the end of the linked list
	 * 
	 * @param obj object to add
	 */
	public boolean add(Object obj) {
		addPrevious(header, obj);
		return true;
	}

	/**
	 * Add all elements within the data structure at the end of the linked list.
	 * 
	 * @param c elements to add.
	 */
	public boolean addAll(DataStructure c) {
		if (c == null || c.size() == 0) {
			return false;
		}

		addPrevious(header, c);

		return true;
	}

	/**
	 * Add all elements of the given data structure into the list. The elements
	 * are added at the given index position, which will add the elements with
	 * given index 0 at the beginning of the list. Adding the data structure at
	 * index == size, will add all elements at the end of the list.
	 * 
	 * @param index Index of position to add the new elements at.
	 * @param data data structure to add.
	 * @return <code>true</code> if this list changed as a result of the call.
	 * @throws IndexOutOfBoundsException if the specified index is out of range
	 */
	public boolean addAll(int index, DataStructure data) throws IndexOutOfBoundsException {
		if (data == null || data.size() == 0) {
			return false;
		}
		if (index < 0 || index > size) {
			throw new IndexOutOfBoundsException("index=" + index + ",size=" + size);
		}

		Entry behind = getEntry(index);

		addPrevious(behind, data);
		return true;
	}

	/**
	 * Adds the given element at the beginning of this list.
	 * 
	 * @param o element to add.
	 */
	public void addFirst(Object o) {
		addPrevious(header.next, o);
	}

	/**
	 * Removes all of the elements from this list.
	 */
	public void clear() {
		changes++;
		Entry e = header.next;
		while (e != header) {
			Entry next = e.next;
			e.element = null;
			e.next = null;
			e.previous = null;
			e = next;
		}

		header.previous = header;
		header.next = header;
		size = 0;
	}

	public Object get(int index) throws IndexOutOfBoundsException {
		if (index < 0 || index >= size) {
			throw new IndexOutOfBoundsException("index=" + index + ", size=" + size);
		}

		return getEntry(index).element;
	}

	/**
	 * Returns the first element in this list.
	 * 
	 * @return first element.
	 */
	public Object getFirst() {
		return header.next.element;
	}

	/**
	 * Returns the last element in this list.
	 * 
	 * @return last element
	 */
	public Object getLast() {
		return header.previous.element;
	}

	public Iterator iterator() {
		return new IteratorImpl();
	}

	/**
	 * Returns a list iterator of the list. It starts at the given index.
	 * 
	 * @param index index to start.
	 * @throws IndexOutOfBoundsException thrown if (index < 0) or (index > size)
	 */
	public ListIterator listIterator(int index) throws IndexOutOfBoundsException {
		return new ListIteratorImpl(index);
	}

	/**
	 * Removes the element at the specified position in this list.
	 * 
	 * @param index index
	 * @throws IndexOutOfBoundsException thrown if (index < 0) or (index >=
	 *             size)
	 */
	public Object remove(int index) throws IndexOutOfBoundsException {
		if (index < 0 || index >= size) {
			throw new IndexOutOfBoundsException("index=" + index + ",size=" + size);
		}

		Entry e = getEntry(index);
		removeEntry(e);

		return e.element;
	}

	/**
	 * Removes the first element of list and returns it.
	 * 
	 * @return the removed element.
	 * @throws NoSuchElementException thrown if no element is in list
	 */
	public Object removeFirst() throws NoSuchElementException {
		if (size == 0) {
			throw new NoSuchElementException();
		}

		Entry e = header.next;
		removeEntry(e);

		return e.element;
	}

	/**
	 * Removes and returns the last element from this list.
	 * 
	 * @return the removed last element
	 * @throws NoSuchElementException
	 */
	public Object removeLast() throws NoSuchElementException {
		if (size == 0) {
			throw new NoSuchElementException();
		}

		Entry e = header.previous;
		removeEntry(e);

		return e.element;

	}

	public Object set(int index, Object element) throws IndexOutOfBoundsException {
		if (index < 0 || index >= size) {
			throw new IndexOutOfBoundsException("index=" + index + ",size=" + size);
		}

		Entry e = getEntry(index);
		return replaceEntry(element, e);
	}

	public Object[] toArray() {
		return toArray(null);
	}

	/**
	 * Fills array with the elements of this list. If the given array is too
	 * short or is null, a new array with the length of the list size will be
	 * created and filled.
	 * 
	 * @return the filled array
	 */
	public Object[] toArray(Object[] array) {
		int size = this.size;

		if (array == null || array.length < size) {
			array = new Object[size];
		}

		Iterator it = iterator();
		for (int i = 0; i < size; i++) {
			array[i] = it.next();
		}

		return array;
	}

	// ----------------------------------- PRIVATE
	// ------------------------------------

	/**
	 * Adds data structure before the specified entry.
	 * 
	 * @param nextEntry entry next to the new elements' entries.
	 * @param data Data structure to add.
	 */
	private void addPrevious(Entry nextEntry, DataStructure data) {
		changes++;
		Entry previous = nextEntry.previous;
		Entry newEntry = previous;

		for (Iterator it = data.iterator(); it.hasNext();) {
			newEntry = new Entry(it.next(), previous, nextEntry);
			previous.next = newEntry;
			previous = newEntry;
		}
		size += data.size();

		nextEntry.previous = newEntry;
	}

	/**
	 * Adds the previous object to the given entry
	 * 
	 * @param nextEntry entry next to the new element's entry
	 * @param elem element to add previous to the given entry
	 */
	private void addPrevious(Entry nextEntry, Object elem) {
		changes++;
		Entry newEntry = new Entry(elem, nextEntry.previous, nextEntry);
		nextEntry.previous.next = newEntry;
		nextEntry.previous = newEntry;
		size++;
	}

	/**
	 * Gets entry of specified index position. If the list is empty or the
	 * specified index is equal or greater than the size of list, will return
	 * header.
	 * 
	 * @param index position of entry to get
	 * @return entry at specified index position
	 */
	private Entry getEntry(int index) {
		if (size == 0 || size == index) {
			return header;
		}

		Entry entry;
		if (index < (size >> 1)) {
			entry = header.next;
			for (int i = 0; i < index; i++) {
				entry = entry.next;
			}
			return entry;
		} else {
			entry = header;
			for (int i = size; i > index; i--) {
				entry = entry.previous;
			}
			return entry;
		}
	}

	/**
	 * Removes the given entry from this list.
	 * 
	 * @param entry entry to remove.
	 */
	private void removeEntry(Entry entry) {
		entry.previous.next = entry.next;
		entry.next.previous = entry.previous;
		size--;
		changes++;
	}

	/**
	 * Replaces the element within the given entry.
	 * 
	 * @param obj replacement of element.
	 * @param entry Entry to replace the element within.
	 * @return the replaced element
	 */
	private Object replaceEntry(Object obj, Entry entry) {
		changes++;
		Object element = entry.element;
		entry.element = obj;
		return element;
	}

	// ------------------------ INNER CLASSES
	// ------------------------------------

	private static class Entry {

		protected Object	element;

		protected Entry		previous;

		protected Entry		next;

		public Entry(Object element, Entry previous, Entry next) {
			this.element = element;
			this.previous = previous;
			this.next = next;
		}
	}

	private class IteratorImpl implements Iterator {

		/** Index of the next element to be returned by next */
		int		nextIndex			= 0;

		/**
		 * Amount of changes the iterator manages. This should be equal to
		 * amount of changes of the outer data structure
		 */
		int		changesIt			= changes;

		/** Last returned entry of the iterator */
		Entry	lastReturnedEntry	= null;

		/**
		 * Next entry of the object to be returned by the call of next on this
		 * iterator
		 */
		Entry	nextEntry;

		/**
		 * Constructor.
		 */
		protected IteratorImpl() {
			nextEntry = header.next;
		}

		/**
		 * Constructor.
		 * 
		 * @param nextIndex
		 * @throws IndexOutOfBoundsException
		 */
		protected IteratorImpl(int nextIndex) throws IndexOutOfBoundsException {
			if (nextIndex < 0 || nextIndex > size) {
				throw new IndexOutOfBoundsException("nextIndex=" + nextIndex + ",size=" + size);
			}

			this.nextIndex = nextIndex;
			nextEntry = getEntry(nextIndex);
			changesIt = changes;
		}

		// -----------------------------------

		public boolean hasNext() {
			return (nextEntry != header);
		}

		public Object next() {
			checkChanges();
			if (nextEntry == header) {
				throw new NoSuchElementException();
			}

			nextIndex++;
			lastReturnedEntry = nextEntry;
			nextEntry = nextEntry.next;

			return lastReturnedEntry.element;
		}

		public void remove() {
			checkChanges();
			if (lastReturnedEntry == null) {
				throw new WS4DIllegalStateException();
			}

			removeEntry(lastReturnedEntry);
			if (nextEntry == lastReturnedEntry) {
				nextEntry = lastReturnedEntry.next;
			} else {
				nextIndex--;
			}
			lastReturnedEntry = null;
			changesIt++;
		}

		protected final void checkChanges() {
			if (changes != changesIt) {
				throw new ConcurrentChangeException();
			}
		}

	}

	private class ListIteratorImpl extends IteratorImpl implements ListIterator {

		ListIteratorImpl(final int nextIndex) {
			super(nextIndex);
		}

		public void add(Object obj) {
			checkChanges();
			LinkedList.this.addPrevious(nextEntry, obj);
			lastReturnedEntry = null;
			changesIt++;
		}

		public boolean hasPrevious() {
			return (nextEntry.previous != header);
		}

		public int indexOfNext() {
			return nextIndex;
		}

		public Object previous() {
			checkChanges();
			if (nextIndex == 0) {
				throw new NoSuchElementException();
			}
			nextIndex--;
			nextEntry = nextEntry.previous;
			lastReturnedEntry = nextEntry;
			return lastReturnedEntry.element;
		}

		public void set(Object obj) {
			checkChanges();
			if (lastReturnedEntry == null) {
				throw new WS4DIllegalStateException();
			}
			LinkedList.this.replaceEntry(obj, lastReturnedEntry);
			changesIt++;
		}

	}

}
