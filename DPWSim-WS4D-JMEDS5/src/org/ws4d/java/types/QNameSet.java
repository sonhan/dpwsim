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
import org.ws4d.java.structures.ReadOnlyIterator;
import org.ws4d.java.structures.Set;
import org.ws4d.java.structures.UnsupportedOperationException;

/**
 * Type-safe QualifiedNameSet. Class is not synchronized.
 */
public class QNameSet {

	private Set		qnames		= null;

	private boolean	readOnly	= false;

	// lazy initialization, only used when readonly
	private int		hashCode	= 0;

	/**
	 * Constructor.
	 */
	public QNameSet() {
		qnames = new HashSet();
	}

	/**
	 * Constructor.
	 */
	public QNameSet(int initialCapacity) {
		qnames = new HashSet(initialCapacity);
	}

	/**
	 * Constructor, adds qualified name element.
	 * 
	 * @param qn QualifiedName element to be added to new QualifiedNameSet
	 */
	public QNameSet(QName qn) {
		qnames = new HashSet(1);
		add(qn);
	}

	/**
	 * Constructor, set must contain qualified names only. Set will be used, not
	 * cloned.
	 * 
	 * @param qnames
	 * @param readOnly
	 */
	private QNameSet(Set qnames, boolean readOnly) {
		this.qnames = qnames;
		this.readOnly = readOnly;
	}

	/**
	 * Copy Constructor. Copies the elements in the QNameSet to the new.
	 */
	public QNameSet(QNameSet set) {
		if (set == null) {
			qnames = new HashSet();
			return;
		}

		qnames = new HashSet(set.size());

		for (Iterator iter = set.iterator(); iter.hasNext();) {
			add((QName) iter.next());
		}
	}
	
	protected QNameSet(QName[] qns) {
		qnames = new HashSet(qns.length);
		for (int i = 0; i < qns.length; i++) {
			qnames.add(qns[i]);
		}
		
	}

	// ----------------- STATIC -------------------------------

	/**
	 * Creates instance of QNameSet by wrapping a set of qnames. Changes on the
	 * instance will throw a UnsupportedOperationException. Set will be used,
	 * not cloned.
	 * 
	 * @param qnames set of qnames
	 * @return wrapped set of qnames
	 */
	public static QNameSet newInstanceReadOnly(Set qnames) {
		return new QNameSet(qnames, true);
	}

	/**
	 * Creates instance of a QNameSet from a Set containing QNames. Set will be
	 * cloned.
	 * 
	 * @param qnames Set of QNames
	 * @return The constructed QNameSet
	 */
	public static QNameSet newInstance(Set qnames) {
		Set clone = new HashSet(qnames.size());

		for (Iterator iter = qnames.iterator(); iter.hasNext();) {
			clone.add(iter.next());
		}

		return new QNameSet(clone, false);
	}

	/**
	 * Constructs a QNameSet from a String containing namespaces and types.
	 * 
	 * @param nsAndTypes String with list of namespace + '/' + local part
	 *            elements divided by ' '.
	 * @return The constructed QNameSet.
	 */
	public static QNameSet construct(String nsAndTypes) {
		QNameSet result = null;
		int pos1 = 0;
		int pos2;

		if (nsAndTypes != null) {
			result = new QNameSet(5);
			QName qname;

			while (pos1 < nsAndTypes.length()) {
				pos2 = nsAndTypes.indexOf(' ', pos1);
				if (pos2 == -1) {
					pos2 = nsAndTypes.length();
				}
				qname = QName.construct(nsAndTypes.substring(pos1, pos2));
				result.add(qname);
				pos1 = pos2 + 1;
			}
		}
		return result;
	}

	// ------------------------------------------------

	/**
	 * Gets copied entries of data structure as array.
	 * 
	 * @return QualifiedName array.
	 */
	public QName[] toArray() {
		QName[] clone = new QName[qnames.size()];

		int i = 0;
		for (Iterator iter = qnames.iterator(); iter.hasNext();) {
			clone[i] = (QName) iter.next();
			i++;
		}

		return clone;
	}

	/**
	 * Gets Iterator
	 * 
	 * @see org.ws4d.java.structures.DataStructure#iterator()
	 * @return Iterator
	 */
	public Iterator iterator() {
		if (readOnly == true) {
			return new ReadOnlyIterator(qnames);
		}

		return qnames.iterator();
	}

	/**
	 * Returns true if this qualified name set contains the specified element.
	 * 
	 * @param qn QualifiedName whose presence in the qualified name set is
	 *            checked
	 * @return <code>true</code> if the argument is a component of this
	 *         QNameSet; <code>false</code> otherwise.
	 */
	public boolean contains(QName qn) {
		return qnames.contains(qn);
	}

	/**
	 * Checks if this contains the given QNameSet.
	 * 
	 * @param qnames Qualified Names which must all be included.
	 * @return <code>true</code> if all qnames are included, <code>false</code>
	 *         otherwise.
	 */
	public boolean containsAll(QNameSet qnames) {
		if (qnames == null) {
			return true;
		}

		for (Iterator it = qnames.iterator(); it.hasNext();) {
			if (!this.qnames.contains(it.next())) return false;
		}

		return true;
	}

	/**
	 * FIXME the other way round!! Checks if <code>qNames</code> contains all
	 * QNames stored within this set.
	 * 
	 * @param qNames an iterator over Qualified Names
	 * @return <code>true</code> if <code>qNames</code> includes all of this
	 *         set's entries, <code>false</code> otherwise
	 */
	public boolean isContainedBy(Iterator qNames) {
		for (Iterator it = this.qnames.iterator(); it.hasNext();) {
			QName name = (QName) it.next();
			boolean notFound = true;
			while (qNames.hasNext()) {
				QName otherName = (QName) qNames.next();
				if (otherName.equals(name)) {
					notFound = false;
					break;
				}
			}
			if (notFound) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Returns size of list.
	 * 
	 * @see org.ws4d.java.structures.List#size()
	 * @return size of list
	 */
	public int size() {
		return qnames.size();
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		String ret = new String();

		for (Iterator it = qnames.iterator(); it.hasNext();) {
			QName qname = (QName) it.next();
			ret = ret.concat(qname.toString() + " ");
		}
		return ret.trim();
	}

	/**
	 * Compares this qualified name data structure to the specified object. The
	 * result is true if and only if the argument is not null and is a QNameSet
	 * object that represents the same set of characteristics as this qualified
	 * name data structure.
	 * 
	 * @param obj the Object to compare this qualified name data structure
	 *            against.
	 * @return true if the qualified name set are equal; false otherwise.
	 */
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}

		if (obj != null && obj instanceof QNameSet) {
			QNameSet qnc = (QNameSet) obj;

			return (qnames.equals(qnc.qnames));
		}

		return false;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		if (!readOnly) {
			return qnames.hashCode();
		}

		int hash = this.hashCode;
		if (hash == 0) {
			hash = qnames.hashCode();
			this.hashCode = hash;
		}
		return hash;
	}

	/**
	 * Checks if the QNameSet must not be changed anymore
	 * 
	 * @return <code>true</code> if it must not be changed.
	 */
	public boolean isReadOnly() {
		return readOnly;
	}

	/**
	 * Returns if data structure is empty.
	 * 
	 * @return <code>true</code> if data structure is empty, <code>false</code>
	 *         otherwise.
	 */
	public boolean isEmpty() {
		return qnames.isEmpty();
	}

	/**
	 * Adds a QualifiedName to this QualifiedNameSet. A
	 * UnsupportedOperationException is thrown if this QNL is readOnly.
	 * 
	 * @see org.ws4d.java.structures.List#add(Object)
	 * @param qn The QualifiedName to be added.
	 */
	public void add(QName qn) throws UnsupportedOperationException {
		if (readOnly) throw new UnsupportedOperationException("QNameSet status is READ ONLY!");

		if (qn != null) {
			qnames.add(qn);
		}
	}

	/**
	 * Adds a QualifiedName to this QualifiedNameSet. A
	 * UnsupportedOperationException is thrown if this QNS is readOnly.
	 * 
	 * @param qnsNewQNames
	 */
	public void addAll(QNameSet qnsNewQNames) {
		if (readOnly) throw new UnsupportedOperationException("QNameSet status is READ ONLY!");

		if (qnsNewQNames == null) {
			return;
		}

		for (Iterator it = qnsNewQNames.iterator(); it.hasNext();) {
			qnames.add(it.next());
		}
	}

	/**
	 * Removes a QualifiedName from this QualifiedNameSet. A
	 * UnsupportedOperationException is thrown if this QNL is readOnly.
	 * 
	 * @see org.ws4d.java.structures.List#remove(Object)
	 * @param qn The QualifiedName to be removed.
	 * @return <code>true</code> if the argument was a component of this
	 *         QualifiedNameSet; <code>false</code> otherwise.
	 */
	public boolean remove(QName qn) throws UnsupportedOperationException {
		if (readOnly) {
			throw new UnsupportedOperationException("QNameSet status is READ ONLY!");
		}

		return qnames.remove(qn);
	}

	/**
	 * Sets this QualifiedNameSet to readOnly.
	 */
	public void setReadOnly() {
		readOnly = true;
	}

	/**
	 * Simple bubble sort for {@link QNameSet}.
	 * <p>
	 * Sorts the qualified names by priorities.
	 * 
	 * @param qnames the set of qualified of to sort.
	 * @return a sorted array of qualified names.
	 */
	public static QName[] sortPrioritiesAsArray(QNameSet qnames) {
		if (qnames != null && qnames.size() > 0) {
			Iterator it = qnames.iterator();
			QName[] bubble = new QName[qnames.size()];
			for (int i = 0; it.hasNext(); i++) {
				QName qn = (QName) it.next();
				bubble[i] = qn;
			}
			boolean sorted = false;
			while (!sorted) {
				sorted = true;
				for (int j = 0; j < bubble.length - 1; j++) {
					QName a = bubble[j];
					QName b = bubble[j + 1];
					if (a.getPriority() < b.getPriority()) {
						QName tmp = b;
						bubble[j + 1] = a;
						bubble[j] = tmp;
						sorted = false;
					}
				}

			}
			return bubble;
		}
		return null;
	}
	
	/**
	 * Simple bubble sort for {@link QNameSet}.
	 * <p>
	 * Sorts the qualified names by priorities.
	 * 
	 * @param qnames the set of qualified of to sort.
	 * @return a sorted set of qualified names.
	 */
	public static QNameSet sortPriorities(QNameSet qnames) {
		return new QNameSet(sortPrioritiesAsArray(qnames));
	}
}
