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

import org.ws4d.java.constants.PrefixRegistry;
import org.ws4d.java.constants.XMLConstants;

/**
 * Class wraps all information of a qualified name, which are:
 * <p>
 * <ul>
 * <li>port type</li>
 * <li>namespace</li>
 * <li>prefix - is the prefix fixed or only a suggestion (change is allowed
 * later on)</li>
 * <li>priority - for DPWS discovery</li>
 * </ul>
 * </p>
 * <p>
 * <h4>Notice</h4>
 * <p>
 * All created qualified names will be used for DPWS discovery. Sets the
 * priority while creating a qualified name. Set priority to
 * {@link #QNAME_WITHOUT_PRIORITY} to omit this qualified name on DPWS
 * discovery.
 * </p>
 */
public class QName {

	public static final int	QNAME_WITHOUT_PRIORITY	= -1;

	public static final int	QNAME_WITH_PRIORITY		= Integer.MAX_VALUE;

	private static int		iPrefixCounter			= 0;

	private final String	localPart;

	private final String	namespace;

	private String			prefix;

	private int				priority				= 0;

	private int				hashCode				= 0;	// default, lazy

	/**
	 * Creates a new unique prefix.
	 * 
	 * @return prefix unique prefix.
	 */
	private static synchronized String getNewPrefix() {
		return XMLConstants.XMLNS_DEFAULT_PREFIX + iPrefixCounter++;
	}

	// initialization

	/**
	 * Constructs a qualified name object with set port type.
	 * <p>
	 * This qualified name WILL be used for DPWS discovery! This qualified name
	 * has normal priority. Use {@link #QName(String, String, int)} if a
	 * priority change is necessary.
	 * </p>
	 * 
	 * @param localPart Port type.
	 * @param namespace namespace name.
	 */
	public QName(String localPart) {
		this(localPart, null, null);
	}

	/**
	 * Constructs a qualified name object with set port type, namespace name.
	 * <p>
	 * This qualified name WILL be used for DPWS discovery! This qualified name
	 * has normal priority. Use {@link #QName(String, String, int)} if a
	 * priority change is necessary.
	 * </p>
	 * 
	 * @param localPart Port type.
	 * @param namespace namespace name.
	 */
	public QName(String localPart, String namespace) {
		this(localPart, namespace, null);
	}

	/**
	 * Constructs a qualified name object with set port type, namespace name,
	 * namespace prefix and DPWS discovery priority.
	 * <p>
	 * This qualified name CAN be used for DPWS discovery! Set priority to
	 * {@link #QNAME_WITHOUT_PRIORITY} if this qualified name SHOULD NOT be used
	 * in DPWS discovery.
	 * </p>
	 * 
	 * @param localPart Port type.
	 * @param namespace namespace name.
	 * @param priority indicates whether this qualified name should be used for
	 *            DPWS discovery or not.
	 */
	public QName(String localPart, String namespace, int priority) {
		this(localPart, namespace, null, priority);
	}

	/**
	 * Constructs a qualified name object with set port type, namespace name and
	 * namespace prefix.
	 * <p>
	 * This qualified name WILL be used for DPWS discovery! This qualified name
	 * has normal priority. Use {@link #QName(String, String, String, int)} if a
	 * priority change is necessary.
	 * </p>
	 * 
	 * @param localPart Port type.
	 * @param namespace namespace name.
	 * @param prefix namespace prefix.
	 */
	public QName(String localPart, String namespace, String prefix) {
		this(localPart, namespace, prefix, 0);
	}

	/**
	 * Constructs a qualified name object with set port type, namespace name,
	 * namespace prefix and DPWS discovery priority.
	 * <p>
	 * This qualified name CAN be used for DPWS discovery! Set priority to
	 * {@link #QNAME_WITHOUT_PRIORITY} if this qualified name SHOULD NOT be used
	 * in DPWS discovery.
	 * </p>
	 * 
	 * @param localPart Port type.
	 * @param namespace namespace name.
	 * @param prefix namespace prefix.
	 * @param priority indicates whether this qualified name should be used for
	 *            DPWS discovery or not.
	 */
	public QName(String localPart, String namespace, String prefix, int priority) {
		localPart = localPart == null ? "" : localPart.trim();
		namespace = namespace == null ? "" : namespace.trim();

		/*
		 * BUGFIX 2010-08-11 SSch, Thx to Stefan Schlichting, Convert strings
		 * that are in jclark representation for a QName for the namespace into
		 * URI-representation, CLDC has no String.replace(String, String)
		 */
		if (namespace.indexOf('{') >= 0 && namespace.indexOf('}') >= 1) {
			int k = namespace.length() - 1; // Bugfix SSch 2011-01-13 see below
			int j = 0;
			char[] n = new char[k];
			for (int i = 0; i < namespace.length(); i++) {
				char c = namespace.charAt(i);
				if (c != '{') {
					// Bugfix SSch 2011-01-13 The closing curly bracket has to
					// be transformed to a '/' as described in the original
					// String.replace
					n[j] = c == '}' ? '/' : c;
					j++;
				}
			}
			if (j < k) {
				/*
				 * should NEVER happen, but trim if necessary
				 */
				char[] m = new char[j];
				System.arraycopy(n, 0, m, 0, j);
				namespace = new String(m);
			} else {
				namespace = new String(n);
			}

		}

		// ADDED Check for compliance
		String nsAndLocalPart = namespace + "/" + localPart;

		int index = nsAndLocalPart.lastIndexOf('/');
		if (index > 1) {
			namespace = nsAndLocalPart.substring(0, index);
			localPart = nsAndLocalPart.substring(index + 1);
		}

		this.localPart = localPart.trim();
		// DON'T DO THIS!!! namespaces must be compared literally!
		// int len = namespace.length();
		// if (len > 0 && namespace.charAt(len - 1) == '/') {
		// namespace = namespace.substring(0, len - 1);
		// }
		this.namespace = namespace;
		if (prefix == null || (prefix = prefix.trim()).equals("")) {
			this.prefix = PrefixRegistry.getPrefix(namespace);
			if (this.prefix == null) {
				this.prefix = getNewPrefix();
			}
		} else {
			this.prefix = prefix;
		}
		this.priority = priority;
	}

	/**
	 * Constructs a qualified name object with given namespace name and port
	 * type.
	 * 
	 * @param nsAndLocalPart namespace name and port type divided by '/'.
	 * @return Constructed QualifiedName or null.
	 */
	public static QName construct(String nsAndLocalPart) {
		QName qname = null;

		if (nsAndLocalPart == null) return null;

		// THX @Stefan Schlichting
		String namespace = null;
		String localPart = nsAndLocalPart;
		int index = localPart.lastIndexOf('/');
		if (index > 1) {
			namespace = nsAndLocalPart.substring(0, index);
			localPart = nsAndLocalPart.substring(index + 1);
		}
		qname = new QName(localPart, namespace, null);

		return qname;
	}

	/**
	 * Returns the port type without prefix.
	 * 
	 * @return Port type.
	 */
	public String getLocalPart() {
		return localPart;
	}

	/**
	 * Returns the port type with prefix.
	 * 
	 * @return Port type.
	 */
	public String getLocalPartPrefixed() {
		return prefix + ":" + localPart;
	}

	/**
	 * Returns the namespace name without prefix.
	 * 
	 * @return namespace name.
	 */
	public String getNamespace() {
		return namespace;
	}

	/**
	 * Returns the namespace name with prefix.
	 * 
	 * @return namespace name.
	 */
	public String getNamespacePrefixed() {
		return prefix + ":" + namespace;
	}

	/**
	 * Returns the prefix.
	 * 
	 * @return The prefix.
	 */
	public String getPrefix() {
		return prefix;
	}

	/**
	 * Sets the prefix.
	 * 
	 * @param prefix Prefix to set.
	 */
	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}

	/**
	 * Compares this qualified name to specified one. Based on port type and
	 * namespace name.
	 * 
	 * @param qn Qualified name to compare with.
	 * @return <code>true</code> if equal, <code>false</code> otherwise.
	 */
	public final boolean equals(QName qn) {
		if (qn == null) {
			return false;
		}
		if (this == qn) {
			return true;
		}
		if (this.toStringPlain().equals(qn.toStringPlain())) {
			return true;
		}

		return false;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public final boolean equals(Object o) {
		if (o instanceof QName) {
			return equals((QName) o);
		}
		return false;
	}

	/**
	 * Compares this qualified name to specified port type and namespace name.
	 * 
	 * @param localPart Port type.
	 * @param namespace namespace name.
	 * @return <code>true</code> if equal, <code>false</code> otherwise.
	 */
	public final boolean equals(String localPart, String namespace) {
		if (localPart == null || namespace == null) return false;

		if (!this.namespace.equals(namespace.trim())) return false;
		if (!this.localPart.equals(localPart.trim())) return false;

		return true;
	}

	/**
	 * Compares this qualified name to specified one. Based on namespace name
	 * and prefix.
	 * 
	 * @param qn Qualified name to compare with.
	 * @return <code>true</code> if equal, <code>false</code> otherwise.
	 */
	public boolean equalNamespaceAndPrefix(QName qn) {
		if (namespace.equals(qn.namespace) && prefix.equals(qn.prefix)) {
			return true;
		}

		return false;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	public final int hashCode() {
		// lazy initialization
		int hash = hashCode;
		if (hash == 0) {
			hash = toStringPlain().hashCode();
			hashCode = hash;
		}
		return hash;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public final String toString() {
		if ("".equals(namespace)) {
			return localPart;
		}
		// this is in accordance to James Clark, http://jclark.com/xml/xmlns.htm
		return '{' + namespace + '}' + localPart;
	}

	public final String toStringPlain() {
		if (namespace.equals("")) {
			return localPart;
		}
		return (namespace.endsWith("/") ? namespace : (namespace + "/")) + localPart;
	}

	public final void setPriority(int priority) {
		this.priority = priority;
	}

	public final int getPriority() {
		return priority;
	}

	public final boolean hasPriority() {
		return (priority >= 0);
	}

}
