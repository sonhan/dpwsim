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
 * Class implements a map of keys associated with values. The speed of the hash
 * map is linked to the quality of the hashCode() method of the key objects.
 */
public class HashMap {

	private static final int	MINIMAL_CAPACITY	= 1;

	private static final int	INITIAL_CAPACITY	= 16;

	transient Entry[]			buckets;

	transient int				size;

	private transient int		threshold;

	private transient final int	loadFactor			= 1;

	transient int				mask;

	transient int				changes				= 0;

	// Views
	transient Set				entrySet;

	transient Set				keySet;

	transient DataStructure		values;

	// -------------------------------------------

	public HashMap(int initialCapacity) {
		if (initialCapacity < 1) {
			initialCapacity = MINIMAL_CAPACITY;
		}
		init(initialCapacity);
	}

	public HashMap() {
		init(INITIAL_CAPACITY);
	}

	public HashMap(HashMap map) {
		int capacity = map.buckets.length;
		init(capacity);
		putAll(map);
	}

	// ---------------------------- STATIC METHODS -----------------------------

	private static int nextPowerOfTwo(final int number) {
		int powerOfTwo = 1;
		while (powerOfTwo < number) {
			powerOfTwo <<= 1;
		}
		return powerOfTwo;
	}

	// -------------------------------------------------------

	/**
	 * Init HashMap. May be overriden by extending class to do initialization.
	 * 
	 * @param tableLength
	 */
	void init(final int tableLength) {
		buckets = new Entry[tableLength];
		size = 0;
		threshold = tableLength * loadFactor;
		mask = tableLength - 1;
	}

	protected Entry addEntry(final int bucketIndex, final int hash, final Object key, final Object value) {
		Entry next = buckets[bucketIndex];
		Entry newEntry = createEntry(hash, key, value, next);
		buckets[bucketIndex] = newEntry;

		if (next != null) {
			next.previous = newEntry;
		}

		changes++;
		size++;

		return newEntry;
	}

	protected Entry createEntry(int hash, Object key, Object value, Entry next) {
		return new Entry(hash, key, value, null, next);
	}

	public void clear() {
		changes++;
		for (int i = 0; i < buckets.length; i++) {
			buckets[i] = null;
		}
		size = 0;
	}

	public boolean containsKey(Object obj) {
		return (getEntry(obj) != null);
	}

	public boolean containsValue(Object obj) {
		for (int i = 0; i < buckets.length; i++) {
			for (Entry entry = buckets[i]; entry != null; entry = entry.next) {

				if ((entry.value == obj) || ((entry.value != null) && (entry.value.equals(obj)))) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Get associated value to given key. A key == <code>null</code> will always
	 * return <code>null</code>;
	 * 
	 * @param key the key to lookup
	 * @return the value for the given key or <code>null</code>
	 */
	public Object get(Object key) {
		Entry entry = getEntry(key);
		if (entry != null) {
			return entry.value;
		}

		return null;
	}

	Entry getEntry(final Object key) {
		int hash = (key == null) ? 0 : key.hashCode();
		return getEntry(hash & mask, hash, key);
	}
	
	Entry getEntry(final int bucketIndex, final int hash, final Object key) {
		for (Entry entry = buckets[bucketIndex]; entry != null; entry = entry.next) {
			if ((entry.hash == hash) && ((entry.key == key) || entry.key.equals(key))) {
				return entry;
			}
		}

		return null;
	}

	public boolean isEmpty() {
		return size() == 0;
	}

	public Object put(Object key, Object value) {
		int hash = 0;
		int bucketIndex = 0;

		if (key != null) {
			hash = key.hashCode();
			bucketIndex = hash & mask;
		}

		Entry entry = getEntry(bucketIndex, hash, key);
		if (entry != null) {
			Object oldValue = entry.value;
			entry.value = value;

			return oldValue;
		}

		if (size >= threshold) {
			resize(buckets.length << 1 + 1);
			return put(key, value);
		}
		addEntry(bucketIndex, hash, key, value);

		return null;
	}

	public void putAll(HashMap map) {
		int keysToAdd = map.size();
		if (keysToAdd == 0) return;

		int targetSize = size + keysToAdd;
		if (targetSize > threshold) {
			resize(nextPowerOfTwo(targetSize / loadFactor + 1));
		}

		HashMap.Entry entry = null;
		for (Iterator iterator = map.entrySet().iterator(); iterator.hasNext();) {
			entry = (HashMap.Entry) iterator.next();
			put(entry.getKey(), entry.getValue());
		}
	}

	private void putAll0(final Entry[] entries) {
		for (int i = 0; i < entries.length; i++) {
			for (Entry entry = entries[i]; entry != null;) {
				Entry next = entry.next;

				int index = entry.hash & mask;
				entry.previous = null;
				Entry existingEntry = buckets[index];
				if (existingEntry != null) {
					existingEntry.previous = entry;
				}
				entry.next = existingEntry;
				buckets[index] = entry;

				entry = next;
			}
		}
	}

	private void resize(final int newTableLength) {
		Entry[] oldBuckets = buckets;
		buckets = new Entry[newTableLength];
		threshold = newTableLength * loadFactor;
		mask = newTableLength - 1;
		putAll0(oldBuckets);
		changes++;
	}

	public Object remove(Object key) {
		int hash = 0;
		int bucketIndex = 0;
		if (key != null) {
			hash = key.hashCode();
			bucketIndex = hash & mask;
		}
		for (Entry entry = buckets[bucketIndex]; entry != null; entry = entry.next) {
			if ((entry.hash == hash) && ((entry.key == key) || (entry.key.equals(key)))) {
				removeEntry(bucketIndex, entry);

				Object value = entry.value;
				entry.value = null;

				return value;
			}
		}
		return null;
	}

	protected void removeEntry(final int bucketIndex, final Entry entry) {
		Entry previous = entry.previous;
		Entry next = entry.next;

		if (previous != null)
			previous.next = next;
		else
			buckets[bucketIndex] = next;

		if (next != null) next.previous = previous;

		entry.previous = null;
		entry.next = null;

		changes++;
		size--;
	}

	public int size() {
		return size;
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

	// -------------------- OVERRIDDEN OBJECT METHODS --------------------

	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if ((obj == null) || !(obj instanceof HashMap)) {
			return false;
		}
		HashMap other = (HashMap) obj;
		if (other.size() != size()) {
			return false;
		}
		try {
			Iterator it = entrySet().iterator();
			while (it.hasNext()) {
				HashMap.Entry entry = (HashMap.Entry) it.next();
				Object key = entry.getKey();
				Object value = entry.getValue();
				if (!other.containsKey(key)) {
					return false;
				} else {
					Object otherValue = other.get(key);
					if (value == null) {
						if (otherValue != null) {
							return false;
						}
					} else if (!value.equals(otherValue)) {
						return false;
					}
				}
			}
		} catch (ClassCastException e) {
			return false;
		} catch (NullPointerException e) {
			return false;
		}
		return true;
	}

	public int hashCode() {
		int hashCode = 0;
		Iterator it = entrySet().iterator();
		while (it.hasNext()) {
			hashCode += it.next().hashCode();
		}
		return hashCode;
	}

	public String toString() {
		int size = size();
		if (size == 0) {
			return "{}";
		}
		StringBuffer sb = new StringBuffer(32 * size);
		sb.append("{");

		Iterator it = entrySet().iterator();
		boolean hasNext = it.hasNext();
		while (hasNext) {
			HashMap.Entry entry = (HashMap.Entry) it.next();
			Object key = entry.getKey();
			Object value = entry.getValue();
			sb.append(key == this ? "<Map>" : key).append("=").append(value == this ? "<Map>" : value);
			hasNext = it.hasNext();
			if (hasNext) {
				sb.append(", ");
			}
		}
		sb.append('}');

		return sb.toString();
	}

	// ========================= INNER CLASSES ===============================

	protected abstract class AbstractMapIterator implements Iterator {

		int				currentBucketIndex	= -1;

		private Entry	current;

		int				nextBucketIndex		= -1;

		Entry			next;

		int				changesIt;

		AbstractMapIterator() {
			changesIt = changes;
			nextBucket();
		}

		private final void nextBucket() {
			if (size > 0) {
				while (++nextBucketIndex < buckets.length && (next = buckets[nextBucketIndex]) == null) {
					// void
				}
			}
		}

		public void remove() {
			checkChanges();
			if (current == null) {
				throw new WS4DIllegalStateException();
			}
			removeEntry(currentBucketIndex, current);
			current = null;
			changesIt = changes;
		}

		public boolean hasNext() {
			return (next != null);
		}

		Entry nextEntry() {
			checkChanges();
			if (next == null) {
				throw new NoSuchElementException();
			}
			current = next;
			next = current.next;
			currentBucketIndex = nextBucketIndex;
			if (next == null) {
				nextBucket();
			}
			return current;
		}

		final void checkChanges() {
			if (changes != changesIt) {
				throw new ConcurrentChangeException();
			}
		}

	}

	// --------------------------------- CLASS ENTRY
	// ---------------------------------

	public static class Entry {

		/** The hash code of the key */
		int		hash;

		/** The key */
		Object	key;

		/** The value */
		Object	value;

		/** The previous entry in the hash chain */
		Entry	previous;

		/** The next entry in the hash chain */
		Entry	next;

		Entry(int hash, Object key, Object value, Entry previous, Entry next) {
			this.hash = hash;
			this.key = key;
			this.value = value;
			this.previous = previous;
			this.next = next;
		}

		public final Object getKey() {
			return key;
		}

		public final Object getValue() {
			return value;
		}

		public final boolean equals(Object obj) {
			if (!(obj instanceof HashMap.Entry)) {
				return false;
			}
			HashMap.Entry entry = (HashMap.Entry) obj;
			Object key = getKey();
			Object keyOther = entry.getKey();
			if (key == keyOther || (key != null && key.equals(keyOther))) {
				Object value = getValue();
				Object valueOther = entry.getValue();
				if (value == valueOther || (value != null && value.equals(valueOther))) {
					return true;
				}
			}
			return false;
		}

		public final int hashCode() {
			return key.hashCode() ^ ((value != null) ? value.hashCode() : 0);
		}

		public String toString() {
			return new StringBuffer().append(getKey()).append('=').append(getValue()).toString();
		}
	}

	// ------------------- CLASS ENTRY SET ----------------------

	protected class EntrySet extends Set {

		public Iterator iterator() {
			return new AbstractMapIterator() {

				public Object next() {
					return nextEntry();
				}
			};
		}

		public void clear() {
			HashMap.this.clear();
		}

		public boolean contains(Object obj) {
			if (obj instanceof HashMap.Entry) {
				HashMap.Entry entry = (HashMap.Entry) obj;
				Object key = entry.getKey();
				int hash = (key == null) ? 0 : key.hashCode();

				Entry foundEntry = getEntry(hash & mask, hash, key);
				return entry.equals(foundEntry);
			}
			return false;
		}

		public boolean remove(Object obj) {
			if (obj instanceof HashMap.Entry) {
				HashMap.Entry entry = (HashMap.Entry) obj;
				Object key = entry.getKey();

				int hash = 0;
				int bucketIndex = 0;

				if (key != null) {
					hash = key.hashCode();
					bucketIndex = hash & mask;
				}

				Entry foundEntry = getEntry(bucketIndex, hash, key);
				if (foundEntry == null) {
					return false;
				}

				if ((foundEntry.value == entry.getValue()) || ((foundEntry.value != null) && (foundEntry.value.equals(entry.getValue())))) {
					removeEntry(bucketIndex, foundEntry);
					return true;
				}
			}
			return false;
		}

		public int size() {
			return size;
		}
	}

	// ------------------- CLASS KEY SET ----------------------

	protected class KeySet extends Set {

		public Iterator iterator() {
			return new AbstractMapIterator() {

				public Object next() {
					return nextEntry().key;
				}
			};
		}

		public void clear() {
			HashMap.this.clear();
		}

		public boolean contains(Object obj) {
			return containsKey(obj);
		}

		public boolean remove(Object obj) {
			return (HashMap.this.remove(obj) != null);
		}

		public int size() {
			return size;
		}
	}

	// ------------------- CLASS VALUES ----------------------

	protected class Values extends DataStructure {

		public Iterator iterator() {
			return new AbstractMapIterator() {

				public Object next() {
					return nextEntry().value;
				}
			};
		}

		public void clear() {
			HashMap.this.clear();
		}

		public boolean contains(Object obj) {
			return containsValue(obj);
		}

		public int size() {
			return size;
		}
	}

}
