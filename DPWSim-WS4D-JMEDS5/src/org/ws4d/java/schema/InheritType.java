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
 * Interface for inherit types. Allows to get base type.
 */
interface InheritType extends SchemaConstants {

	static final String	TAG_EXTENSION	= SCHEMA_EXTENSION;

	static final String	TAG_RESTRICTION	= SCHEMA_RESTRICTION;

	static final String	ATTRIBUTE_BASE	= SCHEMA_BASE;

	public Type getBase();

	public void setBase(Type base);

}
