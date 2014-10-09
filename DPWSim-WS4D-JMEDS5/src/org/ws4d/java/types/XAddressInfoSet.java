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

import org.ws4d.java.communication.ProtocolInfo;
import org.ws4d.java.structures.HashSet;
import org.ws4d.java.structures.Iterator;
import org.ws4d.java.structures.ReadOnlyIterator;
import org.ws4d.java.structures.UnsupportedOperationException;

public class XAddressInfoSet {

	private HashSet	infosets	= null;

	private boolean	readOnly	= false;

	/**
	 * Constructor.
	 */
	public XAddressInfoSet() {
		super();
		infosets = new HashSet();
	}

	/**
	 * Constructor.
	 */
	public XAddressInfoSet(int initialCapacity) {
		super();
		infosets = new HashSet(initialCapacity);
	}

	/**
	 * Constructor, adds an {@link XAddressInfo} element to this set.
	 * 
	 * @param xAdrInfo {@link XAddressInfo} element to be added to the new set.
	 */
	public XAddressInfoSet(XAddressInfo xAdrInfo) {
		super();
		infosets = new HashSet(1);
		add(xAdrInfo);
	}

	/**
	 * Copy Constructor. Copies the elements within the given set to the new
	 * one.
	 */
	public XAddressInfoSet(XAddressInfoSet set) {
		this(set == null ? 1 : set.size());
		addAll(set);
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.types.URISet#iterator()
	 */
	public Iterator iterator() {
		if (readOnly == true) {
			return new ReadOnlyIterator(infosets);
		}

		return infosets.iterator();
	}

	/**
	 * Returns an array containing the {@link XAddressInfo} objects from this set.
	 * 
	 * @return an array containing all {@link XAddressInfo} objects from this set.
	 */
	public XAddressInfo[] toArray() {
		XAddressInfo[] a = new XAddressInfo[infosets.size()];
		Object[] o = infosets.toArray();
		for (int i = 0; i < a.length; i++) {
			a[i] = (XAddressInfo) o[i];
		}
		return a;
	}

	/**
	 * Checks whether given {@link XAddressInfo} is already present within this
	 * set.
	 * 
	 * @param infoSet the {@link XAddressInfo} to check.
	 * @return <code>true</code> if {@link XAddressInfo} is contained by this set,
	 *         <code>false</code> if not
	 */
	public boolean contains(XAddressInfo xAdrInfo) {
		return infosets.contains(xAdrInfo);
	}

	/**
	 * Checks whether all items within the given {@link XAddressInfoSet} are present
	 * within this set.
	 * 
	 * @param otherInfoSet the items to check the presence of
	 * @return <code>true</code> if all objects within <code>otherInfoSet</code> are
	 *         contained by this set, <code>false</code> if at least one of
	 *         them is not
	 */
	public boolean containsAll(XAddressInfoSet otherInfoSet) {
		if (otherInfoSet == null) {
			return true;
		}

		for (Iterator it = otherInfoSet.iterator(); it.hasNext();) {
			if (!this.infosets.contains(it.next())) return false;
		}

		return true;
	}

	/**
	 * Returns the current size of this set.
	 * 
	 * @return the size of this set.
	 */
	public int size() {
		return infosets.size();
	}


	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		StringBuffer ret = new StringBuffer();

		for (Iterator it = infosets.iterator(); it.hasNext();) {
			XAddressInfo xInfo = (XAddressInfo) it.next();
			ret.append(xInfo.getXAddress().toString());
			if (it.hasNext()) {
				ret.append(' ');
			}
		}
		return ret.toString();
	}


	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object obj) {
		boolean isEqual = false;

		if (obj != null && obj instanceof URISet) {
			isEqual = equals((XAddressInfoSet) obj);
		}

		return isEqual;
	}


	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		return infosets.hashCode();
	}

	/**
	 * A specific implementation of {@link Object#equals(Object)} for this kind
	 * of set.
	 * 
	 * @param otherInfoSet the other {@link XAddressInfoSet} to compare.
	 * @return <code>true</code> if both sets are equals, <code>false</code>
	 *         otherwise.
	 * @see Object#equals(Object)
	 */
	public boolean equals(XAddressInfoSet infosets2) {

		if (infosets2 != null) {
			// if ( uridentifiers2 != null &&
			// ((this.readOnly && uridentifiers2.isReadOnly()) ||
			// (!this.readOnly && !uridentifiers2.isReadOnly()))) {
			return (infosets.equals(infosets2));
		}

		return false;
	}

	/**
	 * Returns whether this set is read-only or not.
	 * <p>
	 * A read-only set will not allow any modification.
	 * </p>
	 * 
	 * @return <code>true</code> if the set is read-only, <code>false</code>
	 *         otherwise.
	 */
	public boolean isReadOnly() {
		return readOnly;
	}

	/**
	 * Sets this set to read-only. No further operation will be able to change
	 * this set.
	 * <p>
	 * A read-only set will not allow any modification.
	 * </p>
	 */
	public void setReadOnly() {
		readOnly = true;
	}

	/**
	 * Adds a {@link XAddressInfo} to this set.
	 * <p>
	 * A {@link UnsupportedOperationException} is thrown if this set is
	 * read-only.
	 * </p>
	 * 
	 * @param xAdrInfo The {@link XAddressInfo} to be added.
	 * @see org.ws4d.java.structures.Set#add(Object)
	 */
	public void add(XAddressInfo xAdrInfo) {
		if (readOnly) throw new UnsupportedOperationException("Set status is read-only.");

		if (xAdrInfo != null) {
			infosets.add(xAdrInfo);
		}
	}

	/**
	 * Get the xAdrInfo from this XAddressInfoSet that is equal to the given xAdrInfo. 
	 * 
	 * @param xAdrInfo
	 * @return the equal xAdrInfo from this XAddressInfoSet or <code>null</code> if it does not contained an equal xAdrInfo
	 */
	public XAddressInfo get(XAddressInfo xAdrInfo) {
		return (XAddressInfo)infosets.get(xAdrInfo);
	}
	
	
	/**
	 * Adds all {@link EprInfo} contained within <code>eprinfos</code> to this
	 * instance.
	 * <p>
	 * A {@link UnsupportedOperationException} is thrown if this set is
	 * read-only.
	 * </p>
	 * 
	 * @param xAdrInfoSet the set of {@link EprInfo} to add.
	 * @return <code>true</code> if at least one object from <code>data</code>
	 *         was actually added, i.e. a modification was made to this
	 *         instance, <code>false</code> in any other case (e.g. adding
	 *         objects to a set which already contains them in terms of
	 *         <code>java.lang.Object.equals(Object)</code>)
	 */
	public void addAll(XAddressInfoSet xAdrInfoSet) {
		if (readOnly) throw new UnsupportedOperationException("Set status is read-only.");

		if (xAdrInfoSet == null) {
			return;
		}

		infosets.addAll(xAdrInfoSet.infosets);
	}

	/**
	 * Removes a URI from this URISet. A UnsupportedOperationException is thrown
	 * if this QN data structure is readOnly.
	 * 
	 * @see org.ws4d.java.structures.List#remove(Object)
	 * @param xAdrInfo The URI to be removed.
	 * @return <code>true</code> if the argument was a component of this
	 *         UniformResourceIdentifierList; <code>false</code> otherwise.
	 */
	public boolean remove(XAddressInfo xAdrInfo) {
		if (readOnly) throw new UnsupportedOperationException("Set status is read-only.");

		return infosets.remove(xAdrInfo);
	}

	
	public void mergeProtocolInfo(ProtocolInfo protocolInfo) {
		if (protocolInfo == null) {
			return;
		}
			
		for (Iterator it = infosets.iterator(); it.hasNext();) {
			((XAddressInfo)it.next()).mergeProtocolInfo(protocolInfo);
		}
	}
}
