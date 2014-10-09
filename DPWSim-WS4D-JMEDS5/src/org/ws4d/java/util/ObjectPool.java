/*******************************************************************************
 * Copyright (c) 2009 MATERNA Information & Communications. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html. For further
 * project-related information visit http://www.ws4d.org. The most recent
 * version of the JMEDS framework can be obtained from
 * http://sourceforge.net/projects/ws4d-javame.
 ******************************************************************************/
package org.ws4d.java.util;

import org.ws4d.java.structures.ArrayList;
import org.ws4d.java.structures.HashSet;
import org.ws4d.java.structures.List;
import org.ws4d.java.structures.Set;

/**
 *
 */
public class ObjectPool {

	public interface InstanceCreator {

		/**
		 * Returns a new instance of a pooled object from a certain specific
		 * type.
		 * 
		 * @return the new instance
		 */
		public Object createInstance();

	}

	private final InstanceCreator	creator;

	private final List				pooledObjects;

	private final Set				acquiredObjects;

	private final int				maxSize;

	/**
	 * Creates a new pool with no limit on the maximum number of pooled objects
	 * and an initial pool size of <code>10</code>.
	 * 
	 * @param creator the instance creator for creating new pooled objects
	 */
	public ObjectPool(InstanceCreator creator) {
		this(creator, 10);
	}

	/**
	 * Creates a new pool with no limit on the total number of pooled objects.
	 * 
	 * @param creator the instance creator for creating new pooled objects
	 * @param initialSize the initial pool size
	 */
	public ObjectPool(InstanceCreator creator, int initialSize) {
		this(creator, initialSize, -1);
	}

	/**
	 * Creates a new pool with a maximum total number of pooled objects of
	 * <code>maxSize</code>. The pool will initially have a capacity of
	 * <code>initialSize</code> instances. New instances will be created by the
	 * specified <code>creator</code>.
	 * 
	 * @param creator the instance creator for creating new pooled objects
	 * @param initialSize the initial pool size
	 * @param maxSize the maximum pool size; if <code>-1</code>, no limit on the
	 *            pool size is imposed
	 */
	public ObjectPool(InstanceCreator creator, int initialSize, int maxSize) {
		this.creator = creator;
		this.maxSize = maxSize;
		pooledObjects = new ArrayList(initialSize + 5);
		acquiredObjects = new HashSet(initialSize);
		for (int i = 0; i < initialSize; i++) {
			pooledObjects.add(creator.createInstance());
		}
	}

	public synchronized Object acquire() {
		Object o;
		if (pooledObjects.size() > 0) {
			o = pooledObjects.remove(0);
		} else if (maxSize == -1 || acquiredObjects.size() < maxSize) {
			o = creator.createInstance();
		} else {
			throw new RuntimeException("maximum pool size exceeded");
		}
		acquiredObjects.add(o);
		return o;
	}

	public synchronized void release(Object o) {
		if (!acquiredObjects.remove(o)) {
			return;
		}
		pooledObjects.add(o);
	}

}
