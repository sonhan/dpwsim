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
 * An iterator over a data structure.
 */
public class ReadOnlyIterator implements Iterator {

	Iterator	iterator;

	/**
	 * Constructor, wraps the iterator of the given data structure.
	 * 
	 * @param dataStructure the data structure to wrap
	 */
	public ReadOnlyIterator(DataStructure dataStructure) {
		this(dataStructure.iterator());
	}

	/**
	 * Constructor, wraps iterator.
	 * 
	 * @param it iterator to wrap
	 */
	public ReadOnlyIterator(Iterator it) {
		super();
		this.iterator = it;
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.structures.Iterator#hasNext()
	 */
	public boolean hasNext() {
		return iterator.hasNext();
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.structures.Iterator#next()
	 */
	public Object next() {
		return iterator.next();
	}

	/**
	 * Operation is unsupported by ReadOnlyIterator, throws
	 * UnsupportedOperationException.
	 */
	public void remove() {
		throw new UnsupportedOperationException("This Iterator allows only read only operations");
	}

}
