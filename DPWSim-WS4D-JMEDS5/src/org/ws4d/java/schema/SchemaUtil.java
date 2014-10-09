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

import java.io.IOException;
import java.io.InputStream;

import org.ws4d.java.DPWSFramework;
import org.ws4d.java.communication.TimeoutException;
import org.ws4d.java.communication.monitor.ResourceLoader;
import org.ws4d.java.constants.SchemaConstants;
import org.ws4d.java.io.xml.ElementParser;
import org.ws4d.java.service.Fault;
import org.ws4d.java.service.OperationCommons;
import org.ws4d.java.service.OperationDescription;
import org.ws4d.java.service.Service;
import org.ws4d.java.structures.HashMap;
import org.ws4d.java.structures.Iterator;
import org.ws4d.java.types.QName;
import org.ws4d.java.types.URI;
import org.ws4d.java.util.Log;
import org.ws4d.java.util.StringUtil;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

/**
 * Utility class for XML Schema.
 */
public final class SchemaUtil implements SchemaConstants {

	public static final String		TYPE_ANYTYPE				= SCHEMA_STYPES[44];

	public static final String		TYPE_ANYSIMPLETYPE			= SCHEMA_STYPES[45];

	public static final String		TYPE_STRING					= SCHEMA_STYPES[0];

	public static final String		TYPE_NORMALIZED_STRING		= SCHEMA_STYPES[1];

	public static final String		TYPE_TOKEN					= SCHEMA_STYPES[2];

	public static final String		TYPE_BASE64_BINARY			= SCHEMA_STYPES[3];

	public static final String		TYPE_HEX_BINARY				= SCHEMA_STYPES[4];

	public static final String		TYPE_INTEGER				= SCHEMA_STYPES[5];

	public static final String		TYPE_POSITIVE_INTEGER		= SCHEMA_STYPES[6];

	public static final String		TYPE_NEGATIVE_INTEGER		= SCHEMA_STYPES[7];

	public static final String		TYPE_NON_NEGATIVE_INTEGER	= SCHEMA_STYPES[8];

	public static final String		TYPE_NON_POSITIVE_INTEGER	= SCHEMA_STYPES[9];

	public static final String		TYPE_LONG					= SCHEMA_STYPES[10];

	public static final String		TYPE_UNSIGNED_LONG			= SCHEMA_STYPES[11];

	public static final String		TYPE_INT					= SCHEMA_STYPES[12];

	public static final String		TYPE_UNSIGNED_INT			= SCHEMA_STYPES[13];

	public static final String		TYPE_SHORT					= SCHEMA_STYPES[14];

	public static final String		TYPE_UNSIGNED_SHORT			= SCHEMA_STYPES[15];

	public static final String		TYPE_BYTE					= SCHEMA_STYPES[16];

	public static final String		TYPE_UNSIGNED_BYTE			= SCHEMA_STYPES[17];

	public static final String		TYPE_DECIMAL				= SCHEMA_STYPES[18];

	public static final String		TYPE_FLOAT					= SCHEMA_STYPES[19];

	public static final String		TYPE_DOUBLE					= SCHEMA_STYPES[20];

	public static final String		TYPE_BOOLEAN				= SCHEMA_STYPES[21];

	public static final String		TYPE_DURATION				= SCHEMA_STYPES[22];

	public static final String		TYPE_DATE_TIME				= SCHEMA_STYPES[23];

	public static final String		TYPE_DATE					= SCHEMA_STYPES[24];

	public static final String		TYPE_TIME					= SCHEMA_STYPES[25];

	public static final String		TYPE_G_YEAR					= SCHEMA_STYPES[26];

	public static final String		TYPE_G_YEARMONTH			= SCHEMA_STYPES[27];

	public static final String		TYPE_G_MONTH				= SCHEMA_STYPES[28];

	public static final String		TYPE_G_MONTH_DAY			= SCHEMA_STYPES[29];

	public static final String		TYPE_G_DAY					= SCHEMA_STYPES[30];

	public static final String		TYPE_NAME					= SCHEMA_STYPES[31];

	public static final String		TYPE_QNAME					= SCHEMA_STYPES[32];

	public static final String		TYPE_NCNAME					= SCHEMA_STYPES[33];

	public static final String		TYPE_ANYURI					= SCHEMA_STYPES[34];

	public static final String		TYPE_LANGUAGE				= SCHEMA_STYPES[35];

	public static final String		TYPE_ID						= SCHEMA_STYPES[36];

	public static final String		TYPE_IDREF					= SCHEMA_STYPES[37];

	public static final String		TYPE_IDREFS					= SCHEMA_STYPES[38];

	public static final String		TYPE_ENTITY					= SCHEMA_STYPES[39];

	public static final String		TYPE_ENTITIES				= SCHEMA_STYPES[40];

	public static final String		TYPE_NOTATION				= SCHEMA_STYPES[41];

	public static final String		TYPE_NMTOKEN				= SCHEMA_STYPES[42];

	public static final String		TYPE_NMTOKENS				= SCHEMA_STYPES[43];

	private static final String[]	NATIVES						= { TYPE_ANYTYPE, TYPE_ANYSIMPLETYPE, TYPE_STRING, TYPE_NORMALIZED_STRING, TYPE_TOKEN, TYPE_BYTE, TYPE_UNSIGNED_BYTE, TYPE_BASE64_BINARY, TYPE_HEX_BINARY, TYPE_INTEGER, TYPE_POSITIVE_INTEGER, TYPE_NEGATIVE_INTEGER, TYPE_NON_NEGATIVE_INTEGER, TYPE_NON_POSITIVE_INTEGER, TYPE_INT, TYPE_UNSIGNED_INT, TYPE_LONG, TYPE_UNSIGNED_LONG, TYPE_SHORT, TYPE_UNSIGNED_SHORT, TYPE_DECIMAL, TYPE_FLOAT, TYPE_DOUBLE, TYPE_BOOLEAN, TYPE_TIME, TYPE_DATE_TIME, TYPE_DURATION, TYPE_DATE, TYPE_G_MONTH, TYPE_G_YEAR, TYPE_G_YEARMONTH, TYPE_G_DAY, TYPE_G_MONTH_DAY, TYPE_NAME, TYPE_QNAME, TYPE_NCNAME, TYPE_ANYURI, TYPE_LANGUAGE, TYPE_ID, TYPE_IDREF, TYPE_IDREFS, TYPE_ENTITY, TYPE_ENTITIES, TYPE_NOTATION, TYPE_NMTOKEN, TYPE_NMTOKENS };

	private static final String[]	BINARY						= { TYPE_HEX_BINARY, TYPE_BASE64_BINARY };

	public static final long		MILLIS_PER_SECOND			= 1000L;

	public static final long		MILLIS_PER_MINUTE			= 60L * MILLIS_PER_SECOND;

	public static final long		MILLIS_PER_HOUR				= 60L * MILLIS_PER_MINUTE;

	public static final long		MILLIS_PER_DAY				= 24L * MILLIS_PER_HOUR;

	public static final long		MILLIS_PER_MONTH			= 30L * MILLIS_PER_DAY;

	public static final long		MILLIS_PER_YEAR				= 365L * MILLIS_PER_DAY;

	private static HashMap			nativeTypes					= null;

	private SchemaUtil() {

	}

	/**
	 * Parses duration strings specified in schema.
	 * 
	 * @param duration Duration to parse
	 * @return Millis since 1rst of January since 1970
	 */
	public static long parseDuration(String duration) {
		if (duration == null) {
			return 0L;
		}
		// PnYnMnDTnHnMnS
		long result = 0L;
		// we don't support negative durations
		// long sign = 1L;
		long multiplier = 1L;
		boolean time = false;
		String number = "";

		int len = duration.length();
		for (int i = 0; i < len; i++) {
			char c = duration.charAt(i);
			switch (c) {
				// we don't support negative durations
				// case ('-'): {
				// sign = -1L;
				// continue;
				// }
				case ('P'): {
					continue;
				}
				case ('Y'): {
					multiplier = MILLIS_PER_YEAR;
					break;
				}
				case ('M'): {
					multiplier = time ? MILLIS_PER_MINUTE : MILLIS_PER_MONTH;
					break;
				}
				case ('D'): {
					multiplier = MILLIS_PER_DAY;
					break;
				}
				case ('T'): {
					time = true;
					continue;
				}
				case ('H'): {
					multiplier = MILLIS_PER_HOUR;
					break;
				}
				case ('S'): {
					multiplier = MILLIS_PER_SECOND;
					break;
				}
				default: {
					// must be a number
					number += c;
					continue;
				}
			}
			try {
				result += Long.parseLong(number.trim()) * multiplier;
			} catch (NumberFormatException e) {
				return -1L;
			}
			number = "";
		}
		return /* sign * */result;
	}

	/**
	 * Creates duration string by millis since 1rst of January 1970.
	 * 
	 * @param millis Millis to
	 * @return Duration string specified in schema
	 */
	public static String createDuration(long millis) {
		StringBuffer sb = new StringBuffer();
		if (millis < 0L) {
			sb.append('-');
		}
		sb.append('P');
		if (millis > MILLIS_PER_YEAR) {
			sb.append(millis / MILLIS_PER_YEAR).append('Y');
			millis = millis % MILLIS_PER_YEAR;
		}
		if (millis > MILLIS_PER_MONTH) {
			sb.append(millis / MILLIS_PER_MONTH).append('M');
			millis = millis % MILLIS_PER_MONTH;
		}
		if (millis > MILLIS_PER_DAY) {
			sb.append(millis / MILLIS_PER_DAY).append('D');
			millis = millis % MILLIS_PER_DAY;
		}
		if (millis > 0L) {
			sb.append('T');
		}
		if (millis > MILLIS_PER_HOUR) {
			sb.append(millis / MILLIS_PER_HOUR).append('H');
			millis = millis % MILLIS_PER_HOUR;
		}
		if (millis > MILLIS_PER_MINUTE) {
			sb.append(millis / MILLIS_PER_MINUTE).append('M');
			millis = millis % MILLIS_PER_MINUTE;
		}
		if (millis >= MILLIS_PER_SECOND) {
			sb.append(millis / MILLIS_PER_SECOND).append('S');
			// we don't support fractions of a second
			// millis = millis % MILLIS_PER_SECOND;
		}
		return sb.toString();
	}

	/**
	 * Returns the XML schema type for the given qualified name.
	 * <p>
	 * This method will return types from the XML schema namespace (
	 * {@link Schema#XMLSCHEMA_NAMESPACE} ) only!
	 * </p>
	 * 
	 * @param name the qualified name of the XML schema type.
	 * @return the type.
	 */
	public static synchronized Type getType(QName name) {
		if (nativeTypes == null) {
			initNatives();
		}
		return (Type) nativeTypes.get(name);
	}

	/**
	 * Returns the XML schema type for the given name.
	 * <p>
	 * This method will return types from the XML schema namespace (
	 * {@link Schema#XMLSCHEMA_NAMESPACE} ) only!
	 * </p>
	 * <p>
	 * For instance this method will return a object representing the XML string
	 * (xs:string) type if the name is "string".
	 * </p>
	 * 
	 * @param name the name of the XML schema type.
	 * @return the type.
	 */
	public static Type getSchemaType(String name) {
		return getType(new QName(name, XMLSCHEMA_NAMESPACE));
	}

	/**
	 * Returns <code>true</code> if the given name matches a binary type from
	 * the XML schema, <code>false</code> otherwise.
	 * 
	 * @param name the type name to check.
	 * @return <code>true</code> if the given name matches a binary type from
	 *         the XML schema, <code>false</code> otherwise.
	 */
	public static boolean isBinaryType(String name) {
		for (int i = 0; i < BINARY.length; i++)
			if (BINARY[i].equals(name)) return true;

		return false;
	}

	public static String getPrefix(String prefixedString) {
		int p = prefixedString.indexOf(":");
		if (p == -1) return null;
		return prefixedString.substring(0, p);
	}

	public static String getPrefixedName(XmlSerializer serializer, QName name) {
		if (name == null) return "";
		String prefix = serializer.getPrefix(name.getNamespace(), false);
		return prefix + ":" + name.getLocalPart();
	}

	public static String getName(String prefixedString) {
		int p = prefixedString.indexOf(":");
		if (p == -1) return prefixedString;
		return prefixedString.substring(p + 1, prefixedString.length());
	}

	private static void addElementMap(Element elem, HashMap map) {
		if (elem != null) {
			Schema schema = null;
			String namespace = elem.getName().getNamespace();
			if (map.containsKey(namespace)) {
				schema = (Schema) map.get(namespace);
			} else {
				schema = new Schema(namespace);
				map.put(elem.getName().getNamespace(), schema);
			}
			schema.addElement(elem);
		}
	}
	
	private static void addTypeMap(Type type, HashMap map) {
		if (type != null) {
			Schema schema = null;
			String namespace = type.getName().getNamespace();
			if (map.containsKey(namespace)) {
				schema = (Schema) map.get(namespace);
			} else {
				schema = new Schema(namespace);
				map.put(type.getName().getNamespace(), schema);
			}
			schema.addType(type);
		}
	}

	public static void addToSchemaMap(Iterator iterator, HashMap map) {
		while (iterator.hasNext()) {
			OperationCommons op = (OperationCommons) iterator.next();
			Element input = op.getInput();
			addElementMap(input, map);
			Element output = op.getOutput();
			addElementMap(output, map);
			for (Iterator it2 = op.getFaults(); it2.hasNext();) {
				Fault fault = (Fault) it2.next();
				Element faultElement = fault.getElement();
				addElementMap(faultElement, map);
			}
			
			for (Iterator it3 = op.getCustomComplexTypes(); it3.hasNext();) {
				ComplexType customType = (ComplexType) it3.next();
				addTypeMap(customType, map);
			}
		}
	}

	public static HashMap createSchema(Service service, String targetNamespace) {
		HashMap map = new HashMap();
		addToSchemaMap(service.getOperations(), map);
		addToSchemaMap(service.getEventSources(), map);
		return map;
	}

	public static Schema createSchema(Service service) {
		Schema schema = new Schema();
		String namespace = null;
		try {
			if (service.getParentDeviceReference() != null) {
				namespace = service.getParentDeviceReference().getDevice().getDefaultNamespace();
			}
		} catch (TimeoutException e1) {}
		addToSchema(service.getOperations(), schema, namespace);
		addToSchema(service.getEventSources(), schema, namespace);

		try {
			schema.resolveSchema();
		} catch (SchemaException e) {
			Log.printStackTrace(e);
		}
		return schema;
	}

	static Type getAnyType() {
		return getType(new QName(TYPE_ANYTYPE, XMLSCHEMA_NAMESPACE));
	}

	static Type getAnySimpleType() {
		return getType(new QName(TYPE_ANYSIMPLETYPE, XMLSCHEMA_NAMESPACE));
	}

	static Schema includeOrImportSchema(ElementParser parser, URI location, boolean loadReferencedFiles) throws XmlPullParserException, IOException, SchemaException {
		Schema s = null;
		ResourceLoader rl = DPWSFramework.getResourceAsStream(location);
		InputStream in = rl.getInputStream();
		if (in == null) throw new IOException("Cannot include. Unable to access location " + location);
		s = Schema.parse(in, location, loadReferencedFiles);
		in.close();

		int d = parser.getDepth();
		while (parser.nextTag() != XmlPullParser.END_TAG && parser.getDepth() == d + 1) {
			handleUnkownTags(parser);
		}
		return s;
	}

	static void handleUnkownTags(ElementParser parser) throws XmlPullParserException, IOException {
		/*
		 * eat every unknown tag, to move the parser to next nice one. ;)
		 */
		int i = parser.getDepth();
		int e = parser.getEventType();
		while (e != XmlPullParser.END_TAG && e != XmlPullParser.END_DOCUMENT && parser.getDepth() >= i) {
			e = parser.nextTag();
			handleUnkownTags(parser);
		}
	}

	private static void addToSchema(Iterator operationDescs, Schema schema, String defaultNamespace) {
		while (operationDescs.hasNext()) {
			OperationDescription op = (OperationDescription) operationDescs.next();
			Element input = op.getInput();
			if (input != null) {
				input.globalScope = true;
				if (input.name == null) {
					// input.name = new QName(input.getClass().getSimpleName(),
					// defaultNamespace);
					input.name = new QName(StringUtil.simpleClassName(input.getClass()), defaultNamespace);
				}
				schema.addElement(input);
			}
			Element output = op.getOutput();
			if (output != null) {
				output.globalScope = true;
				schema.addElement(output);
			}
			for (Iterator it2 = op.getFaults(); it2.hasNext();) {
				Fault fault = (Fault) it2.next();
				Element faultElement = fault.getElement();
				if (faultElement != null) {
					faultElement.globalScope = true;
					schema.addElement(faultElement);
				}
			}
		}
	}

	private static void initNatives() {
		nativeTypes = new HashMap();
		for (int i = 0; i < NATIVES.length; i++) {
			QName name = new QName(NATIVES[i], XMLSCHEMA_NAMESPACE);
			Type nativeType = new SimpleType(name);
			nativeTypes.put(name, nativeType);
		}
		// add anyType and anySimpleType
		QName typeName = new QName(TYPE_ANYTYPE, XMLSCHEMA_NAMESPACE);
		nativeTypes.put(typeName, new ComplexType(typeName));

		typeName = new QName(TYPE_ANYSIMPLETYPE, XMLSCHEMA_NAMESPACE);
		// Changed SSch 2011-01-13
		nativeTypes.put(typeName, new AnySimpleType(typeName));
	}

}
