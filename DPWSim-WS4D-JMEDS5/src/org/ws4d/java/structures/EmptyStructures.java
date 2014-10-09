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

/**
 * 
 */
public abstract class EmptyStructures {

	public static final Object[]		EMPTY_OBJECT_ARRAY	= new Object[0];

	public static final Iterator		EMPTY_ITERATOR		= new EmptyIterator();

	public static final DataStructure	EMPTY_STRUCTURE		= new EmptyStructure();

	public static final Set				EMPTY_SET			= new EmptySet();

	public static final List			EMPTY_LIST			= new EmptyList();

	public static final HashMap			EMPTY_MAP			= new EmptyMap();

}
