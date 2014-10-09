package org.ws4d.java.constants;

import org.ws4d.java.types.QName;

/**
 * WS Discovery 2006 constants.
 */

public interface WSDConstants2006 {

	/** The old namespace name for WS Discovery */
	public static final String	WSD_NAMESPACE_NAME							= "http://schemas.xmlsoap.org/ws/2005/04/discovery";

	/** The PATH of the NAMESPACE for WS Discovery */
	public static final String	WSD_NAMESPACE_PATH							= "schemas.xmlsoap.org";

	/** The default To for Target Services if not set explicitly. */
	public static final String	WSD_TO										= "urn:schemas-xmlsoap-org:ws:2005:04:discovery";

	public static final String	WSD_ACTION_HELLO							= WSD_NAMESPACE_NAME + "/Hello";

	public static final String	WSD_ACTION_BYE								= WSD_NAMESPACE_NAME + "/Bye";

	public static final String	WSD_ACTION_PROBE							= WSD_NAMESPACE_NAME + "/Probe";

	public static final String	WSD_ACTION_PROBEMATCHES						= WSD_NAMESPACE_NAME + "/ProbeMatches";

	public static final String	WSD_ACTION_RESOLVE							= WSD_NAMESPACE_NAME + "/Resolve";

	public static final String	WSD_ACTION_RESOLVEMATCHES					= WSD_NAMESPACE_NAME + "/ResolveMatches";

	/** "http://docs.oasis-open.org/ws-dd/ns/discovery/2009/01/rfc2396". */
	public static final String	WSD_MATCHING_RULE_RFC2396					= WSD_NAMESPACE_NAME + "/rfc2396";

	/** "http://docs.oasis-open.org/ws-dd/ns/discovery/2009/01/uuid". */
	public static final String	WSD_MATCHING_RULE_UUID						= WSD_NAMESPACE_NAME + "/uuid";

	/** "http://docs.oasis-open.org/ws-dd/ns/discovery/2009/01/ldap". */
	public static final String	WSD_MATCHING_RULE_LDAP						= WSD_NAMESPACE_NAME + "/ldap";

	/** "http://docs.oasis-open.org/ws-dd/ns/discovery/2009/01/strcmp0". */
	public static final String	WSD_MATCHING_RULE_STRCMP0					= WSD_NAMESPACE_NAME + "/strcmp0";

	/** "http://docs.oasis-open.org/ws-dd/ns/discovery/2009/01/none". */
	public static final String	WSD_MATCHING_RULE_NONE						= WSD_NAMESPACE_NAME + "/none";

	/** The default matching rule, if not explicitly specified, is RFC3986 */
	public static final String	WSD_MATCHING_RULE_DEFAULT					= WSD_MATCHING_RULE_RFC2396;

	// If further scope matching rules are added, edit ScopeUtil.matchScopes

	/* faults */
	public static final QName	WSD_FAULT_SCOPE_MATCHING_RULE_NOT_SUPPORTED	= new QName("MatchingRuleNotSupported", WSDConstants.WSD_NAMESPACE_NAME, WSDConstants.WSD_NAMESPACE_PREFIX);

	/** The Discovery SOAP fault action. */
	public static final String	WSD_ACTION_WSD_FAULT						= WSD_NAMESPACE_NAME + "/fault";
}
