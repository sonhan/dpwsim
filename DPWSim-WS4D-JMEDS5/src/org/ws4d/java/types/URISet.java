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
 * Implementation of a uri data structure
 * 
 * @author mspies
 */
public class URISet {

	private Set		uridentifiers	= null;

	private boolean	readOnly		= false;

	/**
	 * Constructor.
	 */
	public URISet() {
		super();
		uridentifiers = new HashSet();
	}

	/**
	 * Constructor.
	 */
	public URISet(int initialCapacity) {
		super();
		uridentifiers = new HashSet(initialCapacity);
	}

	/**
	 * Constructor, adds QualifiedName element.
	 * 
	 * @param uri QualifiedName element to be added to new URISet
	 */
	public URISet(URI uri) {
		super();
		uridentifiers = new HashSet(1);
		add(uri);
	}

	/**
	 * Copy Constructor. Copies the elements within this uri set to this new.
	 */
	public URISet(URISet set) {
		this(set == null ? 1 : set.size());
		addAll(set);
	}

	// ------------------------ STATIC -----------------------------

	// /**
	// * Create instance of URISet by wrapping a set of qnames.
	// * Changes on the instance will throw a UnsupportedOperationException.
	// * Set will be used, not cloned.
	// *
	// * @param uridentifiers set of uris
	// * @return
	// */
	// public static URISet newInstanceReadOnly( Set uridentifiers ){
	// return new URISet( uridentifiers, true );
	// }
	//
	// /**
	// * Create a new instance of URISet from a set containing qnames.
	// * Set will be cloned.
	// *
	// * @param uridentifiers set of uris
	// * @return
	// */
	// public static URISet newInstance( Set uridentifiers ){
	// Set clone = new HashSet( uridentifiers.size() );
	//
	// clone.addAll
	// for( Iterator iter = uridentifiers.iterator(); iter.hasNext(); ){
	// clone.add( (URI) iter.next() );
	// }
	//
	// return new URISet( clone, false );
	// }
	//
	// public static URISet newInstance( String urisInString ){
	// String[] uriArray = StringFormat.split(urisInString, ' ');
	// Set uris = new HashSet( uriArray.length );
	// for( int i = 0; i < uriArray.length; i++ ){
	// new
	// }
	//
	// }

	// -------------------------------------------------------------

	// /**
	// * Constructs a URISet from a String containing namespaces and
	// * types.
	// *
	// * @param nsAndTypes String with list of namespace + '/' + local part
	// * elements divided by ' '.
	// * @return The constructed UniformResourceIdentifierList.
	// */
	// public static URISet construct(String nsAndTypes) {
	// URISetImpl result = null;
	// int pos1 = 0;
	// int pos2;
	//
	// if (nsAndTypes != null) {
	// result = new URISetImpl(5);
	// URI uri;
	//
	// while (pos1 < nsAndTypes.length()) {
	// pos2 = nsAndTypes.indexOf(' ', pos1);
	// if (pos2 == -1) {
	// pos2 = nsAndTypes.length();
	// }
	// uri = URI.construct(nsAndTypes.substring(pos1, pos2));
	// result.add(uri);
	// pos1 = pos2 + 1;
	// }
	// }
	// return result;
	// }

	// /**
	// * Extracts from a XML Element with namespace prefixes and names a set of
	// * UniformResourceIdentifiers.
	// *
	// * @param element the element containing namespace prefixes and names.
	// * @return a URISet.
	// */
	// public static URISet extractUniformResourceIdentifiers(XMLElement
	// element) {
	// URISetImpl list = new URISetImpl(5);
	// String innerText = XMLElementUtil.getAllInnerText(element, true);
	// String[] typesAsString = StringUtil.split(innerText, ' ');
	// for (int j = 0; j < typesAsString.length; j++) {
	// String typeAsString = typesAsString[j];
	// String[] pairPrefixAndType = StringUtil.split(typeAsString, ':');
	// if ((pairPrefixAndType != null) && (pairPrefixAndType.length == 2)) {
	// String prefix = pairPrefixAndType[0];
	// String type = pairPrefixAndType[1];
	// String namespace = XMLElementUtil.getNamespaceNameByPrefix(element,
	// prefix);
	// list.add(new URI(type, namespace, prefix));
	// }
	// }
	// return list;
	// }

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.types.URISet#iterator()
	 */
	public Iterator iterator() {
		if (readOnly == true) {
			return new ReadOnlyIterator(uridentifiers);
		}

		return uridentifiers.iterator();
	}

	public URI[] toArray() {
		URI[] a = new URI[uridentifiers.size()];
		Object[] o = uridentifiers.toArray();
		for (int i = 0; i < a.length; i++) {
			a[i] = (URI) o[i];
		}
		return a;
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.types.URISet#contains(org.ws4d.java.types.URI)
	 */
	public boolean contains(URI uri) {
		return uridentifiers.contains(uri);
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.types.URISet#containsAll(org.ws4d.java.types.URISet)
	 */
	public boolean containsAll(URISet uridentifiers) {
		if (uridentifiers == null) {
			return true;
		}

		for (Iterator it = uridentifiers.iterator(); it.hasNext();) {
			if (!this.uridentifiers.contains(it.next())) return false;
		}

		return true;
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.types.URISet#size()
	 */
	public int size() {
		return uridentifiers.size();
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.types.URISet#toString()
	 */
	public String toString() {
		StringBuffer ret = new StringBuffer();

		for (Iterator it = uridentifiers.iterator(); it.hasNext();) {
			URI uri = (URI) it.next();
			ret.append(uri.toString());
			if (it.hasNext()) {
				ret.append(' ');
			}
		}
		return ret.toString();
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.types.URISet#equals(java.lang.Object)
	 */
	public boolean equals(Object obj) {
		boolean isEqual = false;

		if (obj != null && obj instanceof URISet) {
			isEqual = equals((URISet) obj);
		}

		return isEqual;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		return uridentifiers.hashCode();
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.types.URISet#equals(org.ws4d.java.types.URISetImpl)
	 */
	public boolean equals(URISet uridentifiers2) {

		if (uridentifiers2 != null) {
			// if ( uridentifiers2 != null &&
			// ((this.readOnly && uridentifiers2.isReadOnly()) ||
			// (!this.readOnly && !uridentifiers2.isReadOnly()))) {
			return (uridentifiers.equals(uridentifiers2));
		}

		return false;
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.types.URISet#isReadOnly()
	 */
	public boolean isReadOnly() {
		return readOnly;
	}

	/**
	 * Sets this URISet to readOnly.
	 */
	public void setReadOnly() {
		readOnly = true;
	}

	// -------------------------- MODIFIERS ---------------------------------

	/**
	 * Adds a URI to this URISet. A UnsupportedOperationException is thrown if
	 * this QN data structure is readOnly.
	 * 
	 * @see org.ws4d.java.structures.List#add(Object)
	 * @param uri The URI to be added.
	 */
	public void add(URI uri) {
		if (readOnly) throw new UnsupportedOperationException("UniformResourceIdentifierList status is READ ONLY!");

		if (uri != null) {
			uridentifiers.add(uri);
		}
	}

	public void addAll(URISet uris) {
		if (readOnly) throw new UnsupportedOperationException("UniformResourceIdentifierList status is READ ONLY!");

		if (uris == null) {
			return;
		}

		uridentifiers.addAll(uris.uridentifiers);
	}

	/**
	 * Removes a URI from this URISet. A UnsupportedOperationException is thrown
	 * if this QN data structure is readOnly.
	 * 
	 * @see org.ws4d.java.structures.List#remove(Object)
	 * @param uri The URI to be removed.
	 * @return <code>true</code> if the argument was a component of this
	 *         UniformResourceIdentifierList; <code>false</code> otherwise.
	 */
	public boolean remove(URI uri) {
		if (readOnly) throw new UnsupportedOperationException("URISet status is READ ONLY!");

		return uridentifiers.remove(uri);
	}
}
