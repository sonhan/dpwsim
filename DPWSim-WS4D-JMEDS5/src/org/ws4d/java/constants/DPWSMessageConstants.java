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
 * This interface contains constants denoting the various message types.
 */
public interface DPWSMessageConstants {

	public static final int		UNKNOWN_MESSAGE					= -1;

	// ----------------------------- DISCOVERY ---------------------------------

	public static final int		HELLO_MESSAGE					= 1;

	public static final int		BYE_MESSAGE						= 2;

	public static final int		PROBE_MESSAGE					= 3;

	public static final int		PROBE_MATCHES_MESSAGE			= 4;

	public static final int		RESOLVE_MESSAGE					= 5;

	public static final int		RESOLVE_MATCHES_MESSAGE			= 6;

	// ------------------------------ TRANSFER ---------------------------------

	public static final int		GET_MESSAGE						= 101;

	public static final int		GET_RESPONSE_MESSAGE			= 102;

	// ------------------------------ METADATA ---------------------------------

	public static final int		GET_METADATA_MESSAGE			= 201;

	public static final int		GET_METADATA_RESPONSE_MESSAGE	= 202;

	// ------------------------------ EVENTING ---------------------------------

	public static final int		SUBSCRIBE_MESSAGE				= 301;

	public static final int		SUBSCRIBE_RESPONSE_MESSAGE		= 302;

	public static final int		RENEW_MESSAGE					= 303;

	public static final int		RENEW_RESPONSE_MESSAGE			= 304;

	public static final int		UNSUBSCRIBE_MESSAGE				= 305;

	public static final int		UNSUBSCRIBE_RESPONSE_MESSAGE	= 306;

	public static final int		GET_STATUS_MESSAGE				= 307;

	public static final int		GET_STATUS_RESPONSE_MESSAGE		= 308;

	public static final int		SUBSCRIPTION_END_MESSAGE		= 309;

	// ----------------------------- INVOCATION --------------------------------

	public static final int		INVOKE_MESSAGE					= 400;

	// ------------------------------- FAULTS ----------------------------------

	// there are many possible fault types
	public static final int		FAULT_MESSAGE					= 500;

	// ------------------------------- PROPERTY --------------------------------

	public static final String	MESSAGE_PROPERTY_DATA			= "dpws.data";

}
