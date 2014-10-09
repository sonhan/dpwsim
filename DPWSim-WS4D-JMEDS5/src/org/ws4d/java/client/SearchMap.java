/*******************************************************************************
 * Copyright (c) 2009 MATERNA Information & Communications. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html. For further
 * project-related information visit http://www.ws4d.org. The most recent
 * version of the JMEDS framework can be obtained from
 * http://sourceforge.net/projects/ws4d-javame.
 ******************************************************************************/
package org.ws4d.java.client;

import org.ws4d.java.structures.DataStructure;
import org.ws4d.java.structures.HashSet;
import org.ws4d.java.util.StringUtil;

/**
 * A search map is a data structure of {@link SearchPath search paths}. Each
 * search path determines one or more technologies and physical or virtual
 * interfaces of the local machine within those technologies, which should be
 * carried out over a search/discovery process.
 * 
 * @see SearchPath
 * @see SearchParameter
 */
public class SearchMap {

	private final DataStructure	paths	= new HashSet();

	// SearchPath --> e.g. DPWS, BT, ZB, ...

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		StringBuffer sb = new StringBuffer(StringUtil.formatClassName(getClass()));
		sb.append(" [ paths=").append(paths);
		sb.append(" ]");
		return sb.toString();
	}

	/**
	 * Adds the specified <code>path</code> to this search map.
	 * 
	 * @param path the search path to add
	 */
	public void addPath(SearchPath path) {
		if (path != null) {
			paths.add(path);
		}
	}

	/**
	 * Returns all {@link SearchPath search paths} contained within this search
	 * map.
	 * 
	 * @return a data structure containing {@link SearchPath} instances
	 */
	public DataStructure getPaths() {
		return paths;
	}

}
