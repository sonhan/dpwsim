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

public abstract class DataStructure {

	private static final String	CLASS_SHORT_NAME	= "DataStructure";

	// -----------------------------------------------------

	/**
	 * Adds a new object to this data structure.
	 * 
	 * @param obj the object to add
	 * @return <code>true</code> if the object was actually added,
	 *         <code>false</code> in any other case (e.g. adding an object to a
	 *         set which already contains the same object in terms of
	 *         <code>java.lang.Object.equals(Object)</code>)
	 */
	public boolean add(Object obj) {
		throw new UnsupportedOperationException();
	}

	/**
	 * Adds all objects contained within <code>data</code> to this instance.
	 * 
	 * @param data the objects to add
	 * @return <code>true</code> if at least one object from <code>data</code>
	 *         was actually added, i.e. a modification was made to this
	 *         instance, <code>false</code> in any other case (e.g. adding
	 *         objects to a set which already contains them in terms of
	 *         <code>java.lang.Object.equals(Object)</code>)
	 */
	public boolean addAll(DataStructure data) {
		boolean changed = false;
		for (Iterator it = data.iterator(); it.hasNext();) {
			Object obj = it.next();
			if (contains(obj) == false) {
				add(obj);
				changed = true;
			}
		}
		return changed;
	}

	/**
	 * Clears this data structure by removing all content from it.
	 */
	public void clear() {
		for (Iterator it = iterator(); it.hasNext();) {
			it.next();
			it.remove();
		}
	}

	/**
	 * Checks whether <code>obj</code> (or another instance equal to
	 * <code>obj</code> in terms of <code>java.lang.Object.equals(Object)</code>
	 * ) is already present within this data structure instance and returns
	 * <code>true</code> only if this is the case.
	 * 
	 * @param obj the object to check the presence of
	 * @return <code>true</code> if <code>obj</code> is contained by this
	 *         instance, <code>false</code> if not
	 */
	public boolean contains(Object obj) {
		if (obj == null) {
			for (Iterator it = iterator(); it.hasNext();) {
				if (it.next() == null) {
					return true;
				}
			}
		} else {
			for (Iterator it = iterator(); it.hasNext();) {
				if (obj.equals(it.next())) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Checks whether all items within <code>data</code> are present within this
	 * data structure instance and returns <code>true</code> only if this is the
	 * case.
	 * 
	 * @param data the items to check the presence of
	 * @return <code>true</code> if all objects within <code>data</code> are
	 *         contained by this instance, <code>false</code> if at least one of
	 *         them is not
	 */
	public boolean containsAll(DataStructure data) {
		for (Iterator it = data.iterator(); it.hasNext();) {
			if (!contains(it.next())) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Returns the short name of the class of this <code>DataStructure</code>
	 * instance.
	 * 
	 * @return the short name of this {@link DataStructure data structure
	 *         instance's} class
	 */
	public String getClassShortName() {
		return CLASS_SHORT_NAME;
	}

	/**
	 * Checks whether this instance is empty or not.
	 * 
	 * @return <code>true</code> if this data structure is empty,
	 *         <code>false</code> otherwise
	 */
	public boolean isEmpty() {
		return size() == 0;
	}

	/**
	 * Removes <code>obj</code> from this data structure. Returns
	 * <code>true</code> if <code>obj</code> was actually present and thus it
	 * was really removed (i.e. this instance was modified by the method call).
	 * 
	 * @param obj the object to remove
	 * @return <code>true</code> only if <code>obj</code> was actually removed
	 */
	public boolean remove(Object obj) {
		if (obj == null) {
			for (Iterator it = iterator(); it.hasNext();) {
				if (it.next() == null) {
					it.remove();
					return true;
				}
			}
		} else {
			for (Iterator it = iterator(); it.hasNext();) {
				if (obj.equals(it.next())) {
					it.remove();
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Returns an array containing all items stored within this data structure.
	 * The array will be of length {@link #size()}, i.e. for an empty data
	 * structure an array of length zero is returned (rather than
	 * <code>null</code>).
	 * 
	 * @return an array of all objects contained within this data structure
	 *         instance; this method never returns <code>null</code>
	 */
	public Object[] toArray() {
		int size = size();
		if (size == 0) {
			return EmptyStructures.EMPTY_OBJECT_ARRAY;
		}

		Iterator it = iterator();
		Object[] objects = new Object[size];
		for (int i = 0; it.hasNext(); i++) {
			objects[i] = it.next();
		}
		return objects;
	}

	/**
	 * Stores all contained items into the passed-in array <code>objects</code>.
	 * Returns the same array.
	 * <p>
	 * The array is expected not to be <code>null</code> and to be of exactly
	 * {@link #size()} length. Otherwise, either a
	 * <code>java.lang.NullPointerException</code> or an
	 * <code>java.lang.IllegalArgumentException</code> will be thrown.
	 * </p>
	 * 
	 * @param objects the array to store this data structure's content to
	 * @return the same array as the one passed in (<code>objects</code>),
	 *         however after having been filled with all objects contained
	 *         within this data structure instance
	 */
	public Object[] toArray(Object[] objects) {
		if (objects.length != size()) {
			throw new IllegalArgumentException("The expected array length is " + size());
		}

		Iterator it = iterator();
		for (int i = 0; it.hasNext(); i++) {
			objects[i] = it.next();
		}
		return objects;
	}

	// ------------------------- OVERRIDDEN OBJECT METHODS
	// ------------------------------------

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		int hashCode = 1;

		for (Iterator it = iterator(); it.hasNext();) {
			Object obj = it.next();
			hashCode = 31 * hashCode + (obj == null ? 0 : obj.hashCode());
		}
		return hashCode;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		int size = size();
		if (size == 0) {
			return "{}";
		}
		StringBuffer sb = new StringBuffer(16 * size);
		sb.append("{");

		for (Iterator it = iterator(); it.hasNext();) {
			Object value = it.next();
			sb.append(value == this ? "<" + getClassShortName() + ">" : value);
			if (it.hasNext()) {
				sb.append(", ");
			}
		}
		sb.append("}");

		return sb.toString();
	}

	// ------------------------- ABSTRACT METHODS
	// ---------------------------------

	/**
	 * Returns an iterator over all items stored within this data structure
	 * instance. The iterator will walk through the items in the natural order
	 * of this data structure; i.e. for a list-like structure this will be the
	 * same order as the one defined by the list.
	 * 
	 * @return an iterator over all stored objects
	 */
	public abstract Iterator iterator();

	/**
	 * Returns the current size of the data structure.
	 * 
	 * @return the size of this data structure
	 */
	public abstract int size();

}
