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

public abstract class Set extends DataStructure {

	private static final String	CLASS_SHORT_NAME	= "Set";

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.structures.DataStructure#getClassShortName()
	 */
	public String getClassShortName() {
		return CLASS_SHORT_NAME;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.ws4d.java.structures.DataStructure#containsAll(org.ws4d.java.structures
	 * .DataStructure)
	 */
	public boolean containsAll(DataStructure data) {
		if (size() > data.size()) {
			return false;
		}
		return super.containsAll(data);
	}

	// ------------------------- OVERRIDDEN OBJECT METHODS
	// ------------------------------------

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}

		if (!(obj instanceof Set)) {
			return false;
		}

		Set other = (Set) obj;
		if (other.size() != size()) {
			return false;
		}

		return other.containsAll(this);
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.structures.DataStructure#hashCode()
	 */
	public int hashCode() {
		int hashCode = 0;

		for (Iterator it = iterator(); it.hasNext();) {
			Object o = it.next();
			if (o != null) {
				/*
				 * Different sequences doesn't matter!
				 */
				hashCode += o.hashCode();
			}
		}
		return hashCode;
	}
}
