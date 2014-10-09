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

public interface SchemaConstants {

	public static final String		SCHEMA_ANNOTATION					= "annotation";

	public static final String		SCHEMA_ANY							= "any";

	public static final String		SCHEMA_ANYATTRIBUTE					= "anyAttribute";

	public static final String		SCHEMA_ATTRIBUTE					= "attribute";

	public static final String		SCHEMA_ATTRIBUTEGROUP				= "attributeGroup";

	public static final String		SCHEMA_BASE							= "base";

	public static final String		SCHEMA_COMPLEXCONTENT				= "complexContent";

	public static final String		SCHEMA_COMPLEXTYPE					= "complexType";

	public static final String		SCHEMA_ELEMENT						= "element";

	public static final String		SCHEMA_ATTRIBUTEFORMDEFAULT			= "attributeFormDefault";

	public static final String		SCHEMA_DOCUMENTATION				= "documentation";

	public static final String		SCHEMA_APP_INFO						= "appInfo";

	public static final String		SCHEMA_NOTATION						= "notation";

	public static final String		SCHEMA_PUBLIC						= "public";

	public static final String		SCHEMA_SYSTEM						= "system";

	public static final String		SCHEMA_ELEMENTFORMDEFAULT			= "elementFormDefault";

	public static final String		SCHEMA_EXTENSION					= "extension";

	public static final String		SCHEMA_FORM							= "form";

	public static final String		SCHEMA_GROUP						= "group";

	public static final String		SCHEMA_INCLUDE						= "include";

	public static final String		SCHEMA_IMPORT						= "import";

	public static final String		SCHEMA_ITEMLIST						= "itemList";

	public static final String		SCHEMA_ITEMTYPE						= "itemType";

	public static final String		SCHEMA_LIST							= "list";

	public static final String		SCHEMA_LOCATION						= "schemaLocation";

	public static final String		SCHEMA_MEMBERTYPES					= "memberTypes";

	public static final String		SCHEMA_NAME							= "name";

	public static final String		SCHEMA_NAMESPACE					= "namespace";

	public static final String		SCHEMA_NONAMESPACESCHEMALOCATION	= "noNamespaceSchemaLocation";

	public static final String		SCHEMA_QUALIFIED					= "qualified";

	public static final String		SCHEMA_UNQUALIFIED					= "unqualified";

	public static final String		SCHEMA_REDEFINE						= "redefine";

	public static final String		SCHEMA_REF							= "ref";

	public static final String		SCHEMA_RESTRICTION					= "restriction";

	public static final String		SCHEMA_SCHEMA						= "schema";

	public static final String		SCHEMA_SIMPLECONTENT				= "simpleContent";

	public static final String		SCHEMA_SIMPLETYPE					= "simpleType";

	public static final String		SCHEMA_SUBSTITUTIONGROUP			= "substitutionGroup";

	public static final String		SCHEMA_TARGETNAMESPACE				= "targetNamespace";

	public static final String		SCHEMA_TYPE							= "type";

	public static final String		SCHEMA_UNION						= "union";

	public static final String		SCHEMA_VALUE						= "value";

	public static final String		SCHEMA_VALUEVECTOR					= "valueVector";

	// ****************

	public static final String		DOCUMENTATION_LANG					= "lang";

	public static final String		ELEMENT_SEQUENCE					= "sequence";

	public static final String		ELEMENT_ALL							= "all";

	public static final String		ELEMENT_CHOICE						= "choice";

	public static final String		ELEMENT_FIXED						= "fixed";

	public static final String		ATTRIBUTE_FIXED						= "fixed";

	public static final String		ELEMENT_DEFAULT						= "default";

	public static final String		ATTRIBUTE_DEFAULT					= "default";

	public static final String		ELEMENT_PARENT						= "parent";

	public static final String		ATTRIBUTE_ABSTRACT					= "abstract";

	public static final String		LIST_ITEMTYPE						= "itemType";

	public static final String		ATTRIBUTE_USE						= "use";

	public static final String		USE_PROHIBITED						= "prohibited";

	public static final String		USE_OPTIONAL						= "optional";

	public static final String		USE_REQUIRED						= "required";

	public static final String		ELEMENT_MAXOCCURS					= "maxOccurs";

	public static final String		ELEMENT_MINOCCURS					= "minOccurs";

	public static final String		MAXOCCURS_UNBOUNDED					= "unbounded";

	public static final String		ELEMENT_NILLABLE					= "nillable";

	public static final String		ELEMENT_SUBSTITUTIONS				= "substitutions";

	public static final String		ELEMENT_UNIONS						= "unions";

	public static final String		ELEMENT_RESTRICTIONS				= "restrictions";

	public static final String		XMLSCHEMA_PREFIX					= "xs";

	public static final String		XMLSCHEMA_NAMESPACE					= "http://www.w3.org/2001/XMLSchema";

	public static final String		XSI_NAMESPACE						= "http://www.w3.org/2001/XMLSchema-instance";

	// ****************

	public static final String[]	SCHEMA_STYPES						= { "string", "normalizedString", "token", "base64Binary", "hexBinary", "integer", "positiveInteger", "negativeInteger", "nonNegativeInteger", "nonPositiveInteger", "long", "unsignedLong", "int", "unsignedInt", "short", "unsignedShort", "byte", "unsignedByte", "decimal", "float", "double", "boolean", "duration", "dateTime", "date", "time", "gYear", "gYearMonth", "gMonth", "gMonthDay", "gDay", "Name", "QName", "NCName", "anyURI", "language", "ID", "IDREF", "IDREFS", "ENTITY", "ENTITIES", "NOTATION", "NMTOKEN", "NMTOKENS", "anyType", "anySimpleType" };

	public static final String[]	SCHEMA_FACETS						= { "enumeration", "fractionDigits", "length", "maxExclusive", "maxInclusive", "maxLength", "minExclusive", "minInclusive", "minLength", "pattern", "totalDigits", "whiteSpace" };

	// *****************

	public static final String		FACET_ENUMERATION					= "enumeration";

	public static final String		FACET_FRACTIONDIGITS				= "fractionDigits";

	public static final String		FACET_LENGTH						= "length";

	public static final String		FACET_MAXEXCLUSIVE					= "maxExclusive";

	public static final String		FACET_MAXINCLUSIVE					= "maxInclusive";

	public static final String		FACET_MAXLENGTH						= "maxLength";

	public static final String		FACET_MINEXCLUSIVE					= "minExclusive";

	public static final String		FACET_MININCLUSIVE					= "minInclusive";

	public static final String		FACET_MINLENGTH						= "minLength";

	public static final String		FACET_PATTERN						= "pattern";

	public static final String		FACET_TOTALDIGITS					= "totalDigits";

	public static final String		FACET_WHITESPACE					= "whiteSpace";

	public static final String		ATTRIBUTE_XSINIL					= "nil";

	public static final String		ATTRIBUTE_XSITYPE					= "type";

	// *****************

	public static final int			XSD_SCHEMA							= 0;

	public static final int			XSD_ELEMENT							= 1;

	public static final int			XSD_GROUP							= 2;

	public static final int			XSD_SIMPLETYPE						= 3;

	public static final int			XSD_COMPLEXTYPE						= 4;

	public static final int			XSD_ATTRIBUTE						= 5;

	public static final int			XSD_ATTRIBUTEGROUP					= 6;

	public static final int			XSD_EXTENDEDCOMPLEXCONTENT			= 7;

	public static final int			XSD_EXTENDEDSIMPLECONTENT			= 8;

	public static final int			XSD_RESTRICTEDSIMPLETYPE			= 9;

	public static final int			XSD_RESTRICTEDCOMPLEXCONTENT		= 10;

	public static final int			XSD_RESTRICTEDSIMPLECONTENT			= 11;

	public static final int			XSD_NOTATION						= 12;

	public static final int			XSD_ALLMODEL						= 13;

	public static final int			XSD_SEQUENCEMODEL					= 14;

	public static final int			XSD_CHOICEMODEL						= 15;

	public static final int			XSD_ANYELEMENT						= 16;

	public static final int			XSD_ANYATTRIBUTE					= 17;

}
