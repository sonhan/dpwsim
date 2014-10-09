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


/**
 * 
 * 
 */
public class Filter extends UnknownDataContainer {

	private URI		dialect;

	private URISet	actions;

	/**
	 * 
	 */
	public Filter() {
		super();
	}

	/**
	 * @param dialect
	 * @param actions
	 */
	public Filter(URI dialect, URISet actions) {
		super();
		this.dialect = dialect;
		this.actions = actions;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("Filter [ dialect=").append(dialect);
		sb.append(", actions=").append(actions);
		sb.append(" ]");
		return sb.toString();
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.types.Filter#getActions()
	 */
	public URISet getActions() {
		return actions;
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.types.Filter#getDialect()
	 */
	public URI getDialect() {
		return dialect;
	}

	/**
	 * @param dialect the dialect to set
	 */
	public void setDialect(URI dialect) {
		this.dialect = dialect;
	}

	/**
	 * @param actions the actions to set
	 */
	public void setActions(URISet actions) {
		this.actions = actions;
	}

}
