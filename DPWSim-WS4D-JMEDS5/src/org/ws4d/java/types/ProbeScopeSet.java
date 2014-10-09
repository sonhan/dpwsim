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

import org.ws4d.java.constants.WSDConstants;
import org.ws4d.java.structures.Iterator;

/**
 *  
 *
 */
public class ProbeScopeSet extends ScopeSet {

	/** Matching Rule: RFC 3986 Section 6.2.1 simple string comparison (default) */
	public static final String	SCOPE_MATCHING_RULE_RFC3986	= WSDConstants.WSD_MATCHING_RULE_RFC3986;

	/** Matching Rule: case-sensitive string comparison */
	public static final String	SCOPE_MATCHING_RULE_STRCMP0	= WSDConstants.WSD_MATCHING_RULE_STRCMP0;

	/** Matching Rule: matching true if no scope in list */
	public static final String	SCOPE_MATCHING_RULE_NONE	= WSDConstants.WSD_MATCHING_RULE_NONE;

	String						matchBy;

	/**
	 * Constructor.
	 */
	public ProbeScopeSet() {
		this(null);
	}

	/**
	 * Constructor.
	 * 
	 * @param matchBy Matching Rule
	 */
	public ProbeScopeSet(String matchBy) {
		this(matchBy, null);
	}

	/**
	 * Constructor.
	 * 
	 * @param matchBy Matching Rule
	 * @param scopes list of scopes
	 */
	public ProbeScopeSet(String matchBy, String[] scopes) {
		super(scopes);
		this.matchBy = matchBy;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("ScopeSet [ matchBy=").append(matchBy);
		sb.append(", scopes=");
		if (strScopes != null) {
			for (Iterator it = strScopes.iterator(); it.hasNext();) {
				sb.append((String) it.next());
			}
		}
		sb.append(", unknownAttributes=").append(unknownAttributes);
		sb.append(" ]");
		return sb.toString();
	}

	/**
	 * Gets matching algorithm of this scope list.
	 * 
	 * @return the matchBy algorithm
	 */
	public String getMatchBy() {
		return matchBy;
	}

	/**
	 * @param matchBy the matchBy to set
	 */
	public void setMatchBy(String matchBy) {
		this.matchBy = matchBy;
	}

	/**
	 * Checks if a given scope set is completely contained in this list. The
	 * given probe scope list defines the matching algorithm to check with.
	 * 
	 * @param others
	 * @return whether this set contains all scopes from the passed-in probe
	 *         scope set
	 */
	public synchronized boolean containsAll(ScopeSet others) {
		if (others == null) {
			return true;
		}
		if (!(others instanceof ProbeScopeSet)) {
			return false;
		}
		ProbeScopeSet o = (ProbeScopeSet) others;
		if (ProbeScopeSet.SCOPE_MATCHING_RULE_NONE.equals(o.matchBy)) {
			/*
			 * Check if no scope is in this list
			 */
			if (strScopes == null || strScopes.size() == 0) {
				return true;
			} else {
				return false;
			}
		}
		if (o.isEmpty()) {
			/*
			 * if empty, we match
			 */
			return true;
		}

		if (strScopes != null || strScopes.size() > 0) {
			if (o.matchBy == null || ProbeScopeSet.SCOPE_MATCHING_RULE_RFC3986.equals(o.matchBy)) {

				URISet otherScopes = others.getScopesAsUris();
				for (Iterator it_others = otherScopes.iterator(); it_others.hasNext();) {
					/*
					 * check each object with all objects in this scope list
					 */
					boolean contains = false;
					URI otherScope = (URI) it_others.next();
					URI thisScopeInList;

					if (uriScopes == null) {
						/*
						 * creating new uri set while checking each uri, which
						 * is added.
						 */
						uriScopes = new URISet(strScopes.size());

						for (Iterator it = strScopes.iterator(); it.hasNext();) {
							thisScopeInList = new URI((String) it.next());
							uriScopes.add(thisScopeInList);

							if (otherScope.equalsWsdRfc3986(thisScopeInList)) {
								contains = true;
								break;
							}
						}
					} else {
						for (Iterator it = strScopes.iterator(); it.hasNext();) {
							thisScopeInList = new URI((String) it.next());
							uriScopes.add(thisScopeInList);

							if (otherScope.equalsWsdRfc3986(thisScopeInList)) {
								contains = true;
								break;
							}
						}
					}

					if (!contains) {
						return false;
					}
				}
			} else if (ProbeScopeSet.SCOPE_MATCHING_RULE_STRCMP0.equals(o.matchBy)) {
				for (Iterator it_others = others.strScopes.iterator(); it_others.hasNext();) {
					/*
					 * check each object with all objects in this scope list
					 */
					boolean contains = false;
					String otherScope = (String) it_others.next();
					String thisScopeInList;

					for (Iterator it = strScopes.iterator(); it.hasNext();) {
						thisScopeInList = (String) it.next();
						if (otherScope.equals(thisScopeInList)) {
							contains = true;
							break;
						}
					}
					if (!contains) {
						return false;
					}
				}
			} else {
				/*
				 * Unknown matching algo
				 */
				return false;
			}
		} else {
			return false;
		}

		return true;
	}

}
