/*******************************************************************************
 * Copyright (c) 2009 MATERNA Information & Communications. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html. For further
 * project-related information visit http://www.ws4d.org. The most recent
 * version of the JMEDS framework can be obtained from
 * http://sourceforge.net/projects/ws4d-javame.
 ******************************************************************************/
package org.ws4d.java.schema;

import org.ws4d.java.constants.SchemaConstants;

/**
 * Just a fake class to represent xs:any ;-)
 */
public interface Any extends SchemaConstants {

	public static final String	TAG_ANY					= SCHEMA_ANY;

	public static final String	TAG_ANYATTRIBUTE		= SCHEMA_ANYATTRIBUTE;

	public static final String	ATTRIBUTE_NAME			= SCHEMA_NAME;

	public static final String	ATTRIBUTE_TYPE			= SCHEMA_TYPE;

	public static final String	ATTRIBUTE_VALUE_FALSE	= "false";

	public static final String	ATTRIBUTE_VALUE_TRUE	= "true";

	public abstract int getSchemaIdentifier();

}
