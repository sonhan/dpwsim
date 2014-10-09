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
 * Class memorizes the order in which objects were inserted. Iterators of the
 * sets returned by this map will have the same sequence. Inserting an already
 * existing entry into the map won't change the order of the elements. Behavior
 * can be extended so that accessing a entry of the map will move this entry to
 * the end of the sequence.
 */
public class LinkedMap extends HashMap {

	/**
	 * Accessing a map entry will move this entry at the end of the iterator
	 * sequence
	 */
	boolean	accessOrdering	= false;

	/** head of list */
	Entry	header;

	// -------------- CONSTRUCTORS -------------------------

	/**
	 * Constructor.
	 */
	public LinkedMap() {
		super();
	}

	/**
	 * Constructor. If accessOrdering is <code>true</code>, then accessing
	 * entries by the get()-method will moved it to the end of the iterator
	 * sequences.
	 * 
	 * @param initialCapacity initial capacity to reserve
	 * @param accessOrdering
	 */
	public LinkedMap(boolean accessOrdering) {
		super();
		this.accessOrdering = accessOrdering;
	}

	/**
	 * Constructor with initial capacity to specify.
	 * 
	 * @param initialCapacity
	 */
	public LinkedMap(int initialCapacity) {
		super(initialCapacity);
	}

	/**
	 * Constructor with initial capacity to specify. If accessOrdering is
	 * <code>true</code>, then accessing entries by the get()-method will moved
	 * it to the end of the iterator sequences.
	 * 
	 * @param initialCapacity initial capacity to reserve
	 * @param accessOrdering
	 */
	public LinkedMap(int initialCapacity, boolean accessOrdering) {
		super(initialCapacity);
		this.accessOrdering = accessOrdering;
	}

	/**
	 * Constructor will create a clone of the specified map.
	 * 
	 * @param m map to clone.
	 */
	public LinkedMap(HashMap m) {
		super(m);
	}

	// --------------------------- PRIVATE ---------------------------------

	/**
	 * Initializes map, overrides and calls HashMap.init().
	 */
	void init(final int tableLength) {
		super.init(tableLength);
		header = new Entry();
	}

	protected HashMap.Entry addEntry(final int bucketIndex, final int hash, final Object key, final Object value) {
		Entry newEntry = (Entry) super.addEntry(bucketIndex, hash, key, value);
		header.addLinkedPrevious(newEntry);

		return newEntry;
	}

	protected HashMap.Entry createEntry(int hash, Object key, Object value, HashMap.Entry next) {
		return new Entry(hash, key, value, null, next);
	}

	protected void removeEntry(final int bucketIndex, final HashMap.Entry entry) {
		super.removeEntry(bucketIndex, entry);
		removeEntryFromLinking((LinkedMap.Entry) entry);
	}

	private void removeEntryFromLinking(Entry entry) {
		entry.prevLinked.nextLinked = entry.nextLinked;
		entry.nextLinked.prevLinked = entry.prevLinked;
	}

	// ------------------------------------------------------------------------

	public void clear() {
		super.clear();
		header = new Entry();
	}

	public boolean containsValue(Object value) {
		if (value != null) {
			for (Entry entry = header.nextLinked; entry != header; entry = entry.nextLinked) {
				if (value.equals(entry.value)) {
					return true;
				}
			}
		} else {
			for (Entry entry = header.nextLinked; entry != header; entry = entry.nextLinked) {
				if (entry.value == null) {
					return true;
				}
			}
		}

		return false;
	}

	public Object get(Object key) {
		int hash = 0;
		int bucketIndex = 0;

		if (key != null) {
			hash = key.hashCode();
			bucketIndex = hash & mask;
		}

		Entry entry = (Entry) getEntry(bucketIndex, hash, key);

		if (entry != null) {
			if (accessOrdering) doAccessOrdering(entry);

			return entry.value;
		}

		return null;
	}

	public Object get(int index) {
		if (index >= size) {
			throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
		}

		Entry resultEntry = getInternal(index);

		if (accessOrdering) doAccessOrdering(resultEntry);

		return resultEntry.value;
	}

	private void doAccessOrdering(Entry entry) {
		removeEntryFromLinking(entry);
		header.addLinkedPrevious(entry);
		changes++;
	}

	private Entry getInternal(int index) {
		Entry entry;
		if (index < (size >> 1)) {
			entry = header.nextLinked;
			for (int i = 0; i < index; i++) {
				entry = entry.nextLinked;
			}
			return entry;
		} else {
			entry = header;
			for (int i = size; i > index; i--) {
				entry = entry.prevLinked;
			}
			return entry;
		}
	}

	public Object remove(int index) {
		if (index >= size) {
			throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
		}

		return remove(getInternal(index));
	}

	public Object removeFirst() {
		if (size == 0) {
			throw new NoSuchElementException("LinkedMap is empty");
		}

		return remove(header.nextLinked.key);
	}

	public Object removeLast() {
		if (size == 0) {
			throw new NoSuchElementException("LinkedMap is empty");
		}
		return remove(header.prevLinked.key);
	}

	// ---------------------------- CREATE VIEWS -------------------------------

	public Set entrySet() {
		return (entrySet != null) ? entrySet : (entrySet = new EntrySet());
	}

	public Set keySet() {
		return (keySet != null) ? keySet : (keySet = new KeySet());
	}

	public DataStructure values() {
		return (values != null) ? values : (values = new Values());
	}

	// =============================== INNER CLASSES
	// =============================

	// ----------------------- CLASS ENTRY --------------------------------

	protected static class Entry extends HashMap.Entry {

		Entry	prevLinked	= null;

		Entry	nextLinked	= null;

		Entry(int hash, Object key, Object value, HashMap.Entry previous, HashMap.Entry next) {
			super(hash, key, value, previous, next);
		}

		Entry() {
			super(-1, null, null, null, null);
			prevLinked = this;
			nextLinked = this;
		}

		/**
		 * Adds Entry linked previous to this.
		 * 
		 * @param newEntry
		 */
		private void addLinkedPrevious(Entry newEntry) {
			newEntry.prevLinked = this.prevLinked;
			newEntry.nextLinked = this;
			this.prevLinked.nextLinked = newEntry;
			this.prevLinked = newEntry;
		}
	}

	// ----------------- CLASS ABSTRACT LINKED MAP ITERATOR
	// ---------------------

	private abstract class AbstractLinkedMapIterator implements Iterator {

		Entry	current;

		Entry	nextLinked;

		int		changesIt;

		/**
		 * Constructor.
		 */
		AbstractLinkedMapIterator() {
			changesIt = changes;
			current = header;
			nextLinked = header.nextLinked;
		}

		public void remove() {
			checkChanges();
			if (current == null || current == header) {
				throw new WS4DIllegalStateException();
			}
			nextLinked = current.nextLinked;
			LinkedMap.this.remove(current.key);
			current = null;
			changesIt = changes;
		}

		public boolean hasNext() {
			return (nextLinked != header);
		}

		/**
		 * @return
		 */
		Entry nextEntry() {
			checkChanges();
			if (nextLinked == header) {
				throw new NoSuchElementException();
			}
			current = nextLinked;
			nextLinked = current.nextLinked;

			return current;
		}

		protected final void checkChanges() {
			if (changes != changesIt) {
				throw new ConcurrentChangeException();
			}
		}
	}

	// -------------------- CLASS ENTRY SET -------------------------

	protected class EntrySet extends HashMap.EntrySet {

		public Iterator iterator() {
			return new AbstractLinkedMapIterator() {

				public Object next() {
					return nextEntry();
				}
			};
		}
	}

	// -------------------- CLASS KEY SET -------------------------

	private class KeySet extends HashMap.KeySet {

		public Iterator iterator() {
			return new AbstractLinkedMapIterator() {

				public Object next() {
					return nextEntry().key;
				}
			};
		}
	}

	// -------------------- CLASS VALUES -------------------------

	private class Values extends HashMap.Values {

		public Iterator iterator() {
			return new AbstractLinkedMapIterator() {

				public Object next() {
					return nextEntry().value;
				}
			};
		}
	}

}
