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

/**
 * Constants from the XML-binary Optimized Packaging spec.
 */
public interface XOPConstants {

	/** The namespace bound to the xop prefix by definition. */
	public static final String	XOP_NAMESPACE_NAME		= "http://www.w3.org/2004/08/xop/include";

	/** The default prefix for the XOP namespace. */
	public static final String	XOP_NAMESPACE_PREFIX	= "xop";

	/** "Include". */
	public static final String	XOP_ELEM_INCLUDE		= "Include";

	/** "href". */
	public static final String	XOP_ATTRIB_HREF			= "href";

	/** "cid". */
	public static final String	XOP_CID_PREFIX			= "cid:";

}
