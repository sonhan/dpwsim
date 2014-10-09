/*******************************************************************************
 * Copyright (c) 2009 MATERNA Information & Communications. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html. For further
 * project-related information visit http://www.ws4d.org. The most recent
 * version of the JMEDS framework can be obtained from
 * http://sourceforge.net/projects/ws4d-javame.
 ******************************************************************************/

package org.ws4d.java.constants;

import org.ws4d.java.types.QName;

/**
 * Our WS4D special constants. ;-)
 */
public interface WS4DConstants {

	public static final String	WS4D_NAMESPACE_NAME			= "http://www.ws4d.org";

	public static final String	WS4D_NAMESPACE_PREFIX		= "ws4d";

	public static final String	WS4D_FRAMEWORK_NAME			= "WS4D Java Multi Edition Framework";

	public static final String	WS4D_FRAMEWORK_VERSION		= "2.0.0";

	public static final QName	WS4D_FAULT_DECLARED			= new QName("DeclaredFault", WS4D_NAMESPACE_NAME);

	public static final QName	WS4D_FAULT_TYPE_MISMATCH	= new QName("TypeMismatch", WS4D_NAMESPACE_NAME);

	public static final QName	WS4D_FAULT_NOT_IMPLEMENTED	= new QName("NotImplemented", WS4D_NAMESPACE_NAME);

}
