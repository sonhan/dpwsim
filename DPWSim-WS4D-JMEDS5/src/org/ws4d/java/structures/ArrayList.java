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

public class ArrayList extends List {

	private final static int	CAPACITY_DEFAULT	= 10;

	final int					capacityIncrement;

	Object[]					elements;

	/**
	 * Constructor.
	 */
	public ArrayList() {
		this(CAPACITY_DEFAULT);
	}

	/**
	 * Constructor.
	 * 
	 * @param data
	 */
	public ArrayList(DataStructure data) {
		this(data.size(), 0);
		addAll(data);
	}

	/**
	 * Constructor.
	 * 
	 * @param initialCapacity
	 */
	public ArrayList(final int initialCapacity) {
		this(initialCapacity, 0);
	}

	/**
	 * Constructor.
	 * 
	 * @param initialCapacity
	 * @param capacityIncrement
	 */
	public ArrayList(final int initialCapacity, final int capacityIncrement) {
		this.elements = new Object[initialCapacity];
		this.capacityIncrement = capacityIncrement;
	}

	/**
	 * Constructor.
	 * 
	 * @param it
	 */
	public ArrayList(Iterator it) {
		this(CAPACITY_DEFAULT, 0);

		while (it.hasNext()) {
			this.add(it.next());
		}
	}

	// --------------------------- METHODS --------------------------

	public void add(int index, Object obj) {
		if (index < 0 || index > size) {
			throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
		}
		changes++;
		checkCapacity(size + 1);
		System.arraycopy(elements, index, elements, index + 1, size - index);
		elements[index] = obj;
		size++;
	}

	public boolean add(Object obj) {
		changes++;
		checkCapacity(size + 1);
		elements[size++] = obj;

		return true;
	}

	private void checkBounds(int index) {
		if (index >= size) {
			throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
		}
	}

	private void checkCapacity(final int minCapacity) {
		int oldCapacity = elements.length;
		if (minCapacity > oldCapacity) {
			Object[] oldElements = elements;
			int newCapacity = (capacityIncrement <= 0 ? (oldCapacity << 1) + 1 : oldCapacity + capacityIncrement);
			if (newCapacity < minCapacity) {
				newCapacity = minCapacity;
			}
			elements = new Object[newCapacity];
			System.arraycopy(oldElements, 0, elements, 0, oldElements.length);
		}
	}

	public void clear() {
		for (int i = 0; i < size; i++) {
			elements[i] = null;
		}
		size = 0;
		changes++;
	}

	public boolean contains(Object obj) {
		return indexOf(obj) >= 0;
	}

	public Object get(int index) {
		checkBounds(index);
		return elements[index];
	}

	public int indexOf(Object o) {
		if (o == null) {
			for (int i = 0; i < size; i++)
				if (elements[i] == null) return i;
		} else {
			for (int i = 0; i < size; i++)
				if (o.equals(elements[i])) return i;
		}
		return -1;
	}

	public int lastIndexOf(Object o) {
		if (o == null) {
			for (int i = size - 1; i >= 0; i--)
				if (elements[i] == null) return i;
		} else {
			for (int i = size - 1; i >= 0; i--)
				if (o.equals(elements[i])) return i;
		}
		return -1;
	}

	public Object remove(int index) {
		checkBounds(index);
		Object oldValue = elements[index];
		remove0(index);

		return oldValue;
	}

	public boolean remove(Object o) {
		if (o == null) {
			for (int index = 0; index < size; index++)
				if (elements[index] == null) {
					remove0(index);
					return true;
				}
		} else {
			for (int index = 0; index < size; index++)
				if (o.equals(elements[index])) {
					remove0(index);
					return true;
				}
		}
		return false;
	}

	private void remove0(final int index) {
		int numMoved = size - index - 1;
		if (numMoved > 0) {
			System.arraycopy(elements, index + 1, elements, index, numMoved);
		}
		elements[--size] = null;
		changes++;
	}

	/**
	 * Optimizing ArrayList.removeAll (see
	 * http://ahmadsoft.org/articles/removeall/index.html)
	 */
	public boolean removeAll(DataStructure data) {
		int oldHi = 0, newHi = 0, top = 0;
		for (int i = 0; i < size; ++i) {
			if (data.contains(elements[i])) {
				changes++;
				oldHi = newHi;
				newHi = i;

				// at the end of this loop newHi will be the non-inclusive
				// upper limit of the range to delete.
				//
				while (++newHi < size && data.contains(elements[newHi])) {
					changes++;
				}

				final int length = i - oldHi;
				System.arraycopy(elements, oldHi, elements, top, length);
				i = newHi;
				top += length;
			}
		}
		if (newHi > 0) {
			final int k = size - newHi;
			System.arraycopy(elements, newHi, elements, top, k);
			final int n = top + k;
			for (int i = n; i < size; i++) {
				elements[i] = null;
			}
			size = n;
			return true;
		} else {
			return false;
		}
	}

	public Object set(int index, Object obj) {
		checkBounds(index);
		Object oldObj = elements[index];
		elements[index] = obj;

		return oldObj;
	}

	public Object[] toArray() {
		Object[] array = new Object[size];
		System.arraycopy(elements, 0, array, 0, size);
		return array;
	}

	public Object[] toArray(Object[] array) {
		if (array.length < size) {
			return toArray();
		}
		System.arraycopy(elements, 0, array, 0, size);

		if (array.length > size) {
			for (int i = 0; i < array.length; i++) {
				array[i] = null;
			}
		}

		return array;
	}

}
