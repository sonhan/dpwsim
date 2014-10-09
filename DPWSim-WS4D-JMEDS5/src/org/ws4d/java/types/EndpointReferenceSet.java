/*******************************************************************************
 * Copyright (c) 2009 MATERNA Information & Communications. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html. For further
 * project-related information visit http://www.ws4d.org. The most recent
 * version of the JMEDS framework can be obtained from
 * http://sourceforge.net/projects/ws4d-javame.
 ******************************************************************************/
package org.ws4d.java.types;

import org.ws4d.java.structures.HashSet;
import org.ws4d.java.structures.Iterator;
import org.ws4d.java.structures.Set;

/**
 * A <code>Set</code> of endpoint references.
 */
public class EndpointReferenceSet {

	private Set	endpointReferences	= null;

	/**
	 * Creates an empty set of endpoint references.
	 */
	public EndpointReferenceSet() {
		this(new HashSet());
	}

	/**
	 * Creates an set of endpoint references form the given set.
	 * 
	 * @param endpointReferences the set which should be used.
	 */
	public EndpointReferenceSet(Set endpointReferences) {
		this.endpointReferences = endpointReferences;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((endpointReferences == null) ? 0 : endpointReferences.hashCode());
		return result;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		EndpointReferenceSet other = (EndpointReferenceSet) obj;
		if (endpointReferences == null) {
			if (other.endpointReferences != null) {
				return false;
			}
		} else if (!endpointReferences.equals(other.endpointReferences)) {
			return false;
		}
		return true;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return endpointReferences == null ? "" : endpointReferences.toString();
	}

	public synchronized void add(EndpointReference epr) {
		endpointReferences.add(epr);
	}

	public synchronized void addAll(EndpointReferenceSet eprSet) {
		if (eprSet != null) {
			endpointReferences.addAll(eprSet.endpointReferences);
		}
	}

	public synchronized void remove(EndpointReference epr) {
		endpointReferences.remove(epr);
	}

	public synchronized void clear() {
		endpointReferences.clear();
	}

	public synchronized Iterator iterator() {
		return endpointReferences.iterator();
	}

	public synchronized int size() {
		if (endpointReferences != null) {
			return endpointReferences.size();
		}
		return 0;
	}

	public synchronized boolean containsAll(EndpointReferenceSet other) {
		if (endpointReferences != null) {
			return endpointReferences.containsAll(other.endpointReferences);
		} else {
			return other == null;
		}
	}

	public boolean contains(EndpointReference epr) {
		return endpointReferences.contains(epr);
	}

	public synchronized URISet getAddresses() {
		URISet addresses = new URISet(endpointReferences.size());
		for (Iterator it = endpointReferences.iterator(); it.hasNext();) {
			EndpointReference epr = (EndpointReference) it.next();
			addresses.add(epr.getAddress());
		}

		return addresses;
	}

}
