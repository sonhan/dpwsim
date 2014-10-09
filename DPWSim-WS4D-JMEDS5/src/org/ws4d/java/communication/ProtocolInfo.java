/*******************************************************************************
 * Copyright (c) 2009 MATERNA Information & Communications. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html. For further
 * project-related information visit http://www.ws4d.org. The most recent
 * version of the JMEDS framework can be obtained from
 * http://sourceforge.net/projects/ws4d-javame.
 ******************************************************************************/
package org.ws4d.java.communication;

/**
 * Instances of this interface encapsulate information for a specific
 * communication protocol, such as DPWS. They are meant to be opaque for
 * everyone else but the {@link CommunicationManager communication manager}
 * instance dedicated to exactly this protocol.
 */
public abstract class ProtocolInfo {

	protected int			version;

	public abstract ProtocolInfo newClone();
	
	/**
	 * Returns a short description of the version and protocol this instance
	 * refers to, e.g. <code>DPWS 1.1</code>.
	 * 
	 * @return a short description of this instance version and protocol
	 */
	public abstract String getDisplayName();

	/**
	 * Merges the best configuration,
	 * 
	 * @param version
	 */
	public abstract void merge(ProtocolInfo version);

	public int getVersion() {
		return version;
	}

	public String toString() {
		return "ProtocolVersion: " + version;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + version;
		return result;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		ProtocolInfo other = (ProtocolInfo) obj;
		if (version != other.version) return false;
		return true;	
	}

}
