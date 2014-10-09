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

public abstract class List extends DataStructure {

	private static final String	CLASS_SHORT_NAME	= "List";

	protected transient int		changes				= 0;

	transient int				size				= 0;

	// -----------------------------------------------------

	public String getClassShortName() {
		return CLASS_SHORT_NAME;
	}

	public void add(int index, Object obj) {
		throw new UnsupportedOperationException();
	}

	public boolean add(Object obj) {
		add(size(), obj);
		return true;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.ws4d.java.structures.DataStructure#addAll(org.ws4d.java.structures
	 * .DataStructure)
	 */
	public boolean addAll(DataStructure data) {
		boolean changed = false;
		for (Iterator it = data.iterator(); it.hasNext();) {
			Object obj = it.next();
			add(obj);
			changed = true;
		}
		return changed;
	}

	public boolean addAll(int index, DataStructure data) {
		boolean changed = false;

		for (Iterator it = data.iterator(); it.hasNext();) {
			add(index++, it.next());
			changed = true;
		}
		return changed;
	}

	public abstract Object get(int index);

	public int indexOf(Object o) {
		if (o == null) {
			for (ListIterator it = listIterator(); it.hasNext();) {
				if (it.next() == null) {
					return it.indexOfNext() - 1;
				}
			}
		} else {
			for (ListIterator it = listIterator(); it.hasNext();) {
				if (o.equals(it.next())) {
					return it.indexOfNext() - 1;
				}
			}
		}
		return -1;
	}

	public Iterator iterator() {
		return new IteratorImpl(0);
	}

	public int lastIndexOf(Object object) {
		if (object == null) {
			for (ListIterator it = listIterator(size()); it.hasPrevious();) {
				if (it.previous() == null) {
					return it.indexOfNext();
				}
			}
		} else {
			for (ListIterator it = listIterator(size()); it.hasPrevious();) {
				if (object.equals(it.previous())) {
					return it.indexOfNext();
				}
			}
		}
		return -1;
	}

	public ListIterator listIterator() {
		return listIterator(0);
	}

	public ListIterator listIterator(int index) {
		return new ListIteratorImpl(index);
	}

	public Object remove(int index) {
		throw new UnsupportedOperationException();
	}

	public Object set(int index, Object obj) {
		throw new UnsupportedOperationException();
	}

	public int size() {
		return size;
	}

	public List subList(int fromIndex, int toIndex) {
		return new SubList(fromIndex, toIndex);
	}

	// ------------------------- OVERRIDDEN OBJECT METHODS
	// ------------------------------------

	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}

		if ((obj == null) || !(obj instanceof List)) {
			return false;
		}

		List other = (List) obj;
		if (other.size() != size()) {
			return false;
		}

		ListIterator e1 = listIterator();
		ListIterator e2 = other.listIterator();
		while (e1.hasNext() && e2.hasNext()) {
			Object o1 = e1.next();
			Object o2 = e2.next();
			if (!(o1 == null ? o2 == null : o1.equals(o2))) {
				return false;
			}
		}
		return !(e1.hasNext() || e2.hasNext());
	}

	// ========================== INNER CLASS ==============================

	private class IteratorImpl implements Iterator {

		/** Index of the next element to be returned by next */
		int	nextIndex;

		int	currentIndex	= -1;

		int	changesIt		= changes;

		IteratorImpl(int nextIndex) {
			this.nextIndex = nextIndex;
			changesIt = changes;
		}

		protected final void checkChanges() {
			if (changes != changesIt) {
				throw new ConcurrentChangeException();
			}
		}

		public boolean hasNext() {
			return (nextIndex < size());
		}

		public Object next() {
			checkChanges();
			try {
				final Object obj = get(nextIndex);
				currentIndex = nextIndex++;
				return obj;
			} catch (IndexOutOfBoundsException e) {
				checkChanges();
				throw new NoSuchElementException();
			}
		}

		public void remove() {
			checkChanges();
			if (currentIndex == -1) {
				throw new WS4DIllegalStateException();
			}
			List.this.remove(currentIndex);
			if (currentIndex < nextIndex) {
				nextIndex--;
			}
			currentIndex = -1;
			changesIt = changes;
		}

	}

	private class ListIteratorImpl extends IteratorImpl implements ListIterator {

		ListIteratorImpl(final int nextIndex) {
			super(nextIndex);
		}

		public void add(Object obj) {
			checkChanges();
			List.this.add(nextIndex++, obj);
			currentIndex = -1;
			changesIt = changes;
		}

		public boolean hasPrevious() {
			return (nextIndex != 0);
		}

		public int indexOfNext() {
			return nextIndex;
		}

		public Object previous() {
			checkChanges();
			try {
				int prevIndex = nextIndex - 1;
				Object obj = get(prevIndex);
				currentIndex = nextIndex = prevIndex;
				return obj;
			} catch (IndexOutOfBoundsException e) {
				checkChanges();
				throw new NoSuchElementException();
			}
		}

		public void set(Object obj) {
			checkChanges();
			List.this.set(currentIndex, obj);
		}

	}

	private final class SubList extends List {

		// int fromIndex;

		int	offset;

		int	size;

		SubList(final int fromIndex, final int toIndex) {
			// this.fromIndex = fromIndex;
			this.offset = fromIndex;
			this.size = toIndex - fromIndex;
		}

		public void add(int index, Object obj) {
			List.this.add(index + offset, obj);
			size++;
		}

		public boolean addAll(DataStructure data) {
			return addAll(size, data);
		}

		public boolean addAll(int index, DataStructure data) {
			if (index < 0 || index > size) throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
			int dataSize = data.size();
			if (dataSize == 0) {
				return false;
			}
			final boolean result = List.this.addAll(offset + index, data);
			size += dataSize;

			return result;
		}

		public Object get(int index) {
			return List.this.get(index + offset);
		}

		public Iterator iterator() {
			return new IteratorImpl(0);
		}

		public ListIterator listIterator(final int index) {
			return new ListIteratorImpl(index) {

				public int indexOfNext() {
					return super.indexOfNext() - offset;
				}

			};
		}

		public Object remove(int index) {
			final Object obj = List.this.remove(index + offset);
			size--;
			return obj;
		}

		public Object set(int index, Object obj) {
			return List.this.set(index + offset, obj);
		}

		public int size() {
			return size;
		}

		public List subList(int fromIndex, int toIndex) {
			return new SubList(fromIndex + offset, toIndex + offset);
		}

	}
}
