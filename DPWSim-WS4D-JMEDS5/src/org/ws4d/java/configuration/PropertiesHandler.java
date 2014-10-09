/*******************************************************************************
 * Copyright (c) 2009 MATERNA Information & Communications. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html. For further
 * project-related information visit http://www.ws4d.org. The most recent
 * version of the JMEDS framework can be obtained from
 * http://sourceforge.net/projects/ws4d-javame.
 ******************************************************************************/

package org.ws4d.java.configuration;

/**
 * Interface for classes that should handle properties read.
 * 
 * @author mspies
 */
public interface PropertiesHandler {

	public static final String	TRUE	= "true";

	public static final String	FALSE	= "false";

	/**
	 * @param header header of section
	 * @param property property within section
	 */
	public abstract void setProperties(PropertyHeader header, Property property);

	/**
	 * This method indicates the PropertiesHandler, that the last section with
	 * the given depth has been read completely.
	 * 
	 * @param depth depth of section.
	 */
	public abstract void finishedSection(int depth);

}
