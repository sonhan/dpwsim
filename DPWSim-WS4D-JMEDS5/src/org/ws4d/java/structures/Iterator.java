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
public interface Iterator {

	/**
	 * Returns <code>true</code> if the iteration has more elements.
	 * 
	 * @return <code>true</code> if the iterator has more elements.
	 */
	boolean hasNext();

	/**
	 * Returns the next element in the iteration.
	 * 
	 * @return the next element in the iteration.
	 */
	Object next();

	/**
	 * Removes from the underlying data structure the last element returned by
	 * the iterator (optional operation).
	 */
	void remove();

}
