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
 * WS Discovery constants.
 */
public interface WSDConstants {

	/** The namespace name for WS Discovery. */
	public static final String	WSD_NAMESPACE_NAME							= "http://docs.oasis-open.org/ws-dd/ns/discovery/2009/01";

	/** The PATH of the NAMESPACE for WS Discovery */
	public static final String	WSD_NAMESPACE_PATH							= "docs.oasis-open.org";

	/** The default prefix for the WSD namespace. */
	public static final String	WSD_NAMESPACE_PREFIX						= "wsd";

	/** The discovery port used to listen for multicast messages. */
	public static final int		WSD_DISCOVERY_PORT							= 3702;

	/**
	 * milliseconds until a response message will be handled, after that, it
	 * will be discarded
	 */
	public static final long	WSD_MATCH_TIMEOUT							= 10000;

	/** The default To for Target Services if not set explicitly. */
	// old one : public static final String WSD_TO =
	// "urn:schemas-xmlsoap-org:ws:2005:04:discovery";
	public static final String	WSD_TO										= "urn:docs-oasis-open-org:ws-dd:ns:discovery:2009:01";

	public static final String	WSD_ACTION_HELLO							= WSD_NAMESPACE_NAME + "/Hello";

	public static final String	WSD_ACTION_BYE								= WSD_NAMESPACE_NAME + "/Bye";

	public static final String	WSD_ACTION_PROBE							= WSD_NAMESPACE_NAME + "/Probe";

	public static final String	WSD_ACTION_PROBEMATCHES						= WSD_NAMESPACE_NAME + "/ProbeMatches";

	public static final String	WSD_ACTION_RESOLVE							= WSD_NAMESPACE_NAME + "/Resolve";

	public static final String	WSD_ACTION_RESOLVEMATCHES					= WSD_NAMESPACE_NAME + "/ResolveMatches";

	/** "Probe". */
	public static final String	WSD_ELEMENT_PROBE							= "Probe";

	public static final String	WSD_ELEMENT_PROBEMATCH						= "ProbeMatch";

	public static final String	WSD_ELEMENT_PROBEMATCHES					= "ProbeMatches";

	/** "Hello". */
	public static final String	WSD_ELEMENT_HELLO							= "Hello";

	/** "Bye". */
	public static final String	WSD_ELEMENT_BYE								= "Bye";

	/** "Resolve". */
	public static final String	WSD_ELEMENT_RESOLVE							= "Resolve";

	/** "ResolveMatch". */
	public static final String	WSD_ELEMENT_RESOLVEMATCH					= "ResolveMatch";

	/** "ResolveMatches". */
	public static final String	WSD_ELEMENT_RESOLVEMATCHES					= "ResolveMatches";

	/** "Types". */
	public static final String	WSD_ELEMENT_TYPES							= "Types";

	/** "Scopes". */
	public static final String	WSD_ELEMENT_SCOPES							= "Scopes";

	/** "XAddrs". */
	public static final String	WSD_ELEMENT_XADDRS							= "XAddrs";

	/** "ServiceId". */
	public static final String	WSD_ELEMENT_SERVICEID						= "ServiceId";

	/** "MetadataVersion". */
	public static final String	WSD_ELEMENT_METADATAVERSION					= "MetadataVersion";

	/** "AppSequence". */
	public static final String	WSD_ELEMENT_APPSEQUENCE						= "AppSequence";

	/** "MatchBy". */
	public static final String	WSD_ATTR_MATCH_BY							= "MatchBy";

	/** "InstanceId". */
	public static final String	WSD_ATTR_INSTANCEID							= "InstanceId";

	/** "SequenceId". */
	public static final String	WSD_ATTR_SEQUENCEID							= "SequenceId";

	/** "MessageNumber". */
	public static final String	WSD_ATTR_MESSAGENUMBER						= "MessageNumber";

	/** Values. */
	public static final String	WSD_VALUE_DISCOVERYPROXY					= "DiscoveryProxy";

	public static final String	WSD_VALUE_TARGETSERVICE						= "TargetService";

	/** "http://docs.oasis-open.org/ws-dd/ns/discovery/2009/01/rfc3986". */
	public static final String	WSD_MATCHING_RULE_RFC3986					= WSD_NAMESPACE_NAME + "/rfc3986";

	/** "http://docs.oasis-open.org/ws-dd/ns/discovery/2009/01/uuid". */
	public static final String	WSD_MATCHING_RULE_UUID						= WSD_NAMESPACE_NAME + "/uuid";

	/** "http://docs.oasis-open.org/ws-dd/ns/discovery/2009/01/ldap". */
	public static final String	WSD_MATCHING_RULE_LDAP						= WSD_NAMESPACE_NAME + "/ldap";

	/** "http://docs.oasis-open.org/ws-dd/ns/discovery/2009/01/strcmp0". */
	public static final String	WSD_MATCHING_RULE_STRCMP0					= WSD_NAMESPACE_NAME + "/strcmp0";

	/** "http://docs.oasis-open.org/ws-dd/ns/discovery/2009/01/none". */
	public static final String	WSD_MATCHING_RULE_NONE						= WSD_NAMESPACE_NAME + "/none";

	/** The default matching rule, if not explicitly specified, is RFC3986 */
	public static final String	WSD_MATCHING_RULE_DEFAULT					= WSD_MATCHING_RULE_RFC3986;

	// If further scope matching rules are added, edit ScopeUtil.matchScopes

	/* faults */
	public static final QName	WSD_FAULT_SCOPE_MATCHING_RULE_NOT_SUPPORTED	= new QName("MatchingRuleNotSupported", WSDConstants.WSD_NAMESPACE_NAME, WSDConstants.WSD_NAMESPACE_PREFIX);

	/** The Discovery SOAP fault action. */
	public static final String	WSD_ACTION_WSD_FAULT						= WSD_NAMESPACE_NAME + "/fault";

}
