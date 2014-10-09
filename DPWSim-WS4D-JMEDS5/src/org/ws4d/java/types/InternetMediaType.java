/*******************************************************************************
 * Copyright (c) 2009 MATERNA Information & Communications. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html. For further
 * project-related information visit http://www.ws4d.org. The most recent
 * version of the JMEDS framework can be obtained from
 * http://sourceforge.net/projects/ws4d-javame.
 ******************************************************************************/
package org.ws4d.java.types;

import org.ws4d.java.constants.MIMEConstants;
import org.ws4d.java.structures.HashMap;
import org.ws4d.java.structures.Iterator;

/**
 * Kind of MIME-Type like described at RFC 1590.
 */
public class InternetMediaType {

	private final static InternetMediaType	TYPE_SOAPXML							= new InternetMediaType(MIMEConstants.MEDIATYPE_APPLICATION, MIMEConstants.SUBTYPE_SOAPXML);			;

	private final static InternetMediaType	TYPE_XML								= new InternetMediaType(MIMEConstants.MEDIATYPE_APPLICATION, MIMEConstants.SUBTYPE_XML);

	private final static InternetMediaType	TYPE_APPLICATION_OCTET_STREAM			= new InternetMediaType(MIMEConstants.MEDIATYPE_APPLICATION, MIMEConstants.SUBTYPE_OCTETSTEAM);

	private final static InternetMediaType	TYPE_APPLICATION_XOPXML					= new InternetMediaType(MIMEConstants.MEDIATYPE_APPLICATION, MIMEConstants.SUBTYPE_XOPXML);

	private final static InternetMediaType	TYPE_MULTIPART_RELATED					= new InternetMediaType(MIMEConstants.MEDIATYPE_MULTIPART, MIMEConstants.SUBTYPE_RELATED);

	private final static InternetMediaType	TYPE_MULTIPART_FORMDATA					= new InternetMediaType(MIMEConstants.MEDIATYPE_MULTIPART, MIMEConstants.SUBTYPE_FORMDATA);

	private final static InternetMediaType	TYPE_APPLICATION_XWWW_FORM_URL_ENCODED	= new InternetMediaType(MIMEConstants.MEDIATYPE_APPLICATION, MIMEConstants.SUBTYPE_XWWWFORMURLENCODED);

	private final static InternetMediaType	TYPE_IMAGE_JPEG							= new InternetMediaType(MIMEConstants.MEDIATYPE_IMAGE, MIMEConstants.SUBTYPE_JPEG);

	private final static InternetMediaType	TYPE_TEXT_HTML							= new InternetMediaType(MIMEConstants.MEDIATYPE_TEXT, MIMEConstants.SUBTYPE_HTML);

	private final static InternetMediaType	TYPE_TEXT_PLAIN							= new InternetMediaType(MIMEConstants.MEDIATYPE_TEXT, MIMEConstants.SUBTYPE_PLAIN);

	/**
	 * Returns the application/soap+xml type.
	 * 
	 * @return the application/soap+xml type.
	 */
	public final static InternetMediaType getSOAPXML() {
		return TYPE_SOAPXML;
	}

	/**
	 * Returns the application/xml type.
	 * 
	 * @return the application/xml type.
	 */
	public final static InternetMediaType getXML() {
		return TYPE_XML;
	}

	/**
	 * Returns the application/octet-stream type.
	 * 
	 * @return the application/octet-stream type.
	 */
	public final static InternetMediaType getApplicationOctetStream() {
		return TYPE_APPLICATION_OCTET_STREAM;
	}

	/**
	 * Returns the application/xop+xml type.
	 * 
	 * @return the application/xop+xml type.
	 */
	public final static InternetMediaType getApplicationXOPXML() {
		return TYPE_APPLICATION_XOPXML;
	}

	/**
	 * Returns the multipart/related type.
	 * 
	 * @return the multipart/related type.
	 */
	public final static InternetMediaType getMultipartRelated() {
		return TYPE_MULTIPART_RELATED;
	}

	/**
	 * Returns the multipart/form-data type.
	 * 
	 * @return the multipart/form-data type.
	 */
	public final static InternetMediaType getMultipartFormdata() {
		return TYPE_MULTIPART_FORMDATA;
	}

	/**
	 * Returns the application/x-www-form-urlencoded type.
	 * 
	 * @return the application/x-www-form-urlencoded type.
	 */
	public final static InternetMediaType getApplicationXWWWFormUrlEncoded() {
		return TYPE_APPLICATION_XWWW_FORM_URL_ENCODED;
	}

	/**
	 * Returns the image/jpeg type.
	 * 
	 * @return the image/jpeg type.
	 */
	public final static InternetMediaType getImageJPEG() {
		return TYPE_IMAGE_JPEG;
	}

	/**
	 * Returns the text/html type.
	 * 
	 * @return the text/html type.
	 */
	public final static InternetMediaType getTextHTML() {
		return TYPE_TEXT_HTML;
	}

	/**
	 * Returns the text/plain type.
	 * 
	 * @return the text/plain type.
	 */
	public final static InternetMediaType getTextPlain() {
		return TYPE_TEXT_PLAIN;
	}

	
	/**
	 * Clones an InternetMediaType instance and replaces the original parameters with the passed ones. 
	 * 
	 * @param original InternetMediaType instance to clone.
	 * @param parameterKeys key array for parameters.
	 * @param parameterValues value array for parameters.
	 * @return
	 */
	public static InternetMediaType cloneAndSetParameters(InternetMediaType original, String[] parameterKeys, String[] parameterValues) {
		InternetMediaType result = new InternetMediaType(original);
		if (parameterKeys == null) {
			result.parameters = new HashMap(0);
		} else {
			result.parameters = new HashMap(parameterKeys.length);
			result.addParameters(parameterKeys, parameterValues);
		}
		
		return result;
	}

	/**
	 * Clones an InternetMediaType instance and replaces the original parameters with the passed one. 
	 * 
	 * @param original InternetMediaType instance to clone.
	 * @param parameterKey key of the parameter.
	 * @param parameterValue value of the parameters.
	 * @return
	 */
	public static InternetMediaType cloneAndSetParameter(InternetMediaType original, String parameterKey, String parameterValue) {
		InternetMediaType result = new InternetMediaType(original);
		if (parameterKey == null) {
			result.parameters = new HashMap(0);
		} else {
			result.parameters = new HashMap(1);
			result.parameters.put(parameterKey.trim(), parameterValue.trim());
		}
		
		return result;
	}

	/**
	 * Clones an InternetMediaType instance and adds the passed parameters to the original ones. 
	 * 
	 * @param original InternetMediaType instance to clone.
	 * @param parameterKeys key array for parameters to add.
	 * @param parameterValues value array for parameters to add.
	 * @return
	 */	
	public static InternetMediaType cloneAndAddParameters(InternetMediaType original, String[] parameterKeys, String[] parameterValues) {
		InternetMediaType result = new InternetMediaType(original);
		result.parameters = new HashMap(original.parameters);
		if (parameterKeys != null) {
			result.addParameters(parameterKeys, parameterValues);
		}
		
		return result;		
	}
	
	/**
	 * Clones an InternetMediaType instance and adds the passed parameter to the original ones. 
	 * 
	 * @param original InternetMediaType instance to clone.
	 * @param parameterKey key of the parameter to add.
	 * @param parameterValue value of the parameter to add.
	 * @return
	 */	
	public static InternetMediaType cloneAndAddParameter(InternetMediaType original, String parameterKey, String parameterValue) {
		InternetMediaType result = new InternetMediaType(original);
		result.parameters = new HashMap(original.parameters);
		if (parameterKey != null) {
			result.parameters.put(parameterKey.trim(), parameterValue.trim());
		}
		
		return result;		
	}
		
	/** The main type. Case insensitive. */
	private String	type;

	/** The subtype. Case insensitive. */
	private String	subtype;

	/**
	 * String,String parameterName -> parameterValue. ParameterNames are case
	 * insensitive.
	 */
	private HashMap	parameters;

	private InternetMediaType(InternetMediaType original) {
		this.type = original.type;
		this.subtype = original.subtype;
	}
	
	/**
	 * Creates a new media type object from given types.
	 * 
	 * @param type media type.
	 * @param subtype media sub type.
	 */
	public InternetMediaType(String type, String subtype) {
		this(type, subtype, (String)null, null);
	}

	/**
	 * Creates a new media type object from given types. If parameterKeys is not null 
	 * parameterValues has also not to be null and has to have the same size.
	 * 
	 * @param type media type.
	 * @param subtype media sub type.
	 * @param parameterKeys key array for parameters.
	 * @param parameterValues value array for parameters.
	 */
	public InternetMediaType(String type, String subtype, String[] parameterKeys, String[] parameterValues) {
		this.type = type;
		this.subtype = subtype;

		if (parameterKeys == null) {
			parameters = new HashMap(0);
		} else {
			parameters = new HashMap(parameterKeys.length);
			addParameters(parameterKeys, parameterValues);
		}	
	}

	/**
	 * Creates a new media type object from given types. If parameterKeys is not null 
	 * parameterValues has also not to be null and has to have the same size.
	 * 
	 * @param type media type.
	 * @param subtype media sub type.
	 * @param parameterKey key of the parameter.
	 * @param parameterValue value of the parameter.
	 */
	public InternetMediaType(String type, String subtype, String parameterKey, String parameterValue) {
		this.type = type;
		this.subtype = subtype;

		if (parameterKey == null) {
			parameters = new HashMap(0);
		} else {
			parameters = new HashMap(1);
			parameters.put(parameterKey.trim(), parameterValue.trim());
		}	
	}

	
	/**
	 * Create a new media type object from the information given.
	 * 
	 * @param mediaType the string describing the media type.
	 */
	public InternetMediaType(String mediaType) {
		if (mediaType == null) {
			invalid();
			return;
		}
		int slashPos = mediaType.indexOf(MIMEConstants.SEPARATOR);
		if ((slashPos < 1) || (slashPos == (mediaType.length() - 1))) {
			invalid();
			return;
		}

		type = mediaType.substring(0, slashPos);
		int semicolonPos = mediaType.indexOf(";", slashPos + 1);
		if (semicolonPos < 0) {
			subtype = mediaType.substring(slashPos + 1).trim();
		} else {
			subtype = mediaType.substring(slashPos + 1, semicolonPos).trim();
		}
		parameters = new HashMap();

		String tmp;
		int quotePosStart;
		int quotePosEnd;

		while (semicolonPos > 0) {
			int nextSemicolonPos = mediaType.indexOf(";", semicolonPos + 1);
			if (nextSemicolonPos > 0) {

				tmp = mediaType.substring(semicolonPos + 1, nextSemicolonPos);

				quotePosStart = checkQuotation(tmp);
				if (quotePosStart > 0) {
					quotePosEnd = searchQuotationEnd(mediaType.substring(semicolonPos + 1), quotePosStart + 1) + semicolonPos + 2;
					addParameter(mediaType.substring(semicolonPos + 1, quotePosEnd));
					semicolonPos = mediaType.indexOf(";", quotePosEnd);
				} else {
					addParameter(tmp);
					semicolonPos = nextSemicolonPos;
				}

			} else {
				addParameter(mediaType.substring(semicolonPos + 1));
				semicolonPos = -1;
			}
		}
	}

	/**
	 * Adds a parameter with its value to the parameter table.
	 * 
	 * @param parameter the parameter to add. In the form attribute=value.
	 * @return <code>true</code> if the given parameter was well-formed,
	 *         <code>false</code> otherwise.
	 */
	private boolean addParameter(String parameter) {
		int equalsPos = parameter.indexOf("=");
		if ((equalsPos < 1) || (equalsPos == parameter.length() - 1)) {
			return false;
		}
		parameters.put(parameter.substring(0, equalsPos).trim(), parameter.substring(equalsPos + 1).trim());
		return true;
	}

	
	/**
	 * Adds parameters and their associated values to the parameter table.
	 * 
	 * @param parameterKeys key array for parameters.
	 * @param parameterValues value array for parameters.
	 */
	private void addParameters(String[] parameterKeys, String[] parameterValues) {
		for (int i = 0; i < parameterKeys.length; i++) {
			parameters.put(parameterKeys[i].trim(), parameterValues[i].trim());
		}
	}
	

	/**
	 * When the constructor parameter does not conform to the standard,
	 * initialize the class variables with <code>null</code> or anything else
	 * indicating failure.
	 */
	protected void invalid() {
		type = null;
		subtype = null;
		parameters = new HashMap();
	}

	/**
	 * Returns the complete media type.
	 * 
	 * @return the media type, e.g.\ "application/xml" for media type
	 *         "application/xml"
	 */
	public String getMediaType() {
		return type + MIMEConstants.SEPARATOR + subtype;
	}

	/**
	 * Returns the main type.
	 * 
	 * @return the main type, e.g.\ application for media type "application/xml"
	 */
	public String getType() {
		return type;
	}

	/**
	 * Returns the subtype.
	 * 
	 * @return the subtype, e.g.\ xml for media type "application/xml".
	 */
	public String getSubtype() {
		return subtype;
	}

	/**
	 * Checks whether this media type has the given type.
	 * 
	 * @param type the main type to check against. Case insensitive.
	 * @param subtype the subtype to check against. Case insensitive.
	 * @return <code>true</code> if the object has the given type,
	 *         <code>false</code> otherwise.
	 */
	public boolean hasType(String type, String subtype) {
		if ((this.type == null) || (this.subtype == null)) {
			return false;
		}
		return (this.type.equals(type)) && (this.subtype.equals(subtype));
	}

	/**
	 * Checks whether this media type has the given main type.
	 * 
	 * @param type the main type to check against. Case insensitive.
	 * @return <code>true</code> if the object has the given type,
	 *         <code>false</code> otherwise.
	 */
	public boolean hasMainType(String type) {
		if ((this.type == null)) {
			return false;
		}
		return (this.type.equals(type));
	}

	/**
	 * Checks whether this media type has the given sub type.
	 * 
	 * @param subtype the sub type to check against. Case insensitive.
	 * @return <code>true</code> if the object has the given type,
	 *         <code>false</code> otherwise.
	 */
	public boolean hasSubType(String subtype) {
		if ((this.subtype == null)) {
			return false;
		}
		return (this.subtype.equals(subtype));
	}

	/**
	 * Returns the value of a given attribute.
	 * 
	 * @param attributeName the name of the attribute to get the value of. Case
	 *            insensitive.
	 * @return the value of a given attribute.
	 */
	public String getParameter(String attributeName) {
		return (String) parameters.get(attributeName);
	}

	/**
	 * Checks whether this parameter exists.
	 * 
	 * @param attributeName the parameter to check for.
	 * @return <code>true</code> if the object has the given parameter,
	 *         <code>false</code> otherwise.
	 */
	public boolean hasParameter(String attributeName) {
		return parameters.containsKey(attributeName);
	}

	/**
	 * Returns a string representation of this media type.
	 */
	public String toString() {
		StringBuffer retval = new StringBuffer();
		retval.append(type);
		retval.append(MIMEConstants.SEPARATOR);
		retval.append(subtype);
		if (parameters != null) {
			Iterator en = parameters.keySet().iterator();
			while (en.hasNext()) {
				String attributeName = (String) en.next();
				String value = (String) parameters.get(attributeName);
				retval.append(";" + attributeName + "=" + value);
			}
		}
		return retval.toString();
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((subtype == null) ? 0 : subtype.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		return result;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		final InternetMediaType other = (InternetMediaType) obj;
		if (subtype == null) {
			if (other.subtype != null) return false;
		} else if (!subtype.equals(other.subtype)) return false;
		if (type == null) {
			if (other.type != null) return false;
		} else if (!type.equals(other.type)) return false;
		return true;
	}

	/**
	 * Checks if value of the parameter in the string is quoted.
	 * 
	 * @param s string to be checked.
	 * @return position of the beginning quotation mark of the parameter value.
	 *         Returns 0 if the value isn't quoted.
	 */
	private int checkQuotation(String s) {
		int equalsPos = s.indexOf("=");
		int quotePos = s.indexOf("\"");
		return (equalsPos + 1 == quotePos ? quotePos : 0);
	}

	/**
	 * Searches closing quotation mark in String.
	 * 
	 * @param s string to search in.
	 * @param index start position of search in string.
	 * @return position of the closing quotation mark of the parameter value.
	 */
	private int searchQuotationEnd(String s, int index) {
		int quotePos = s.indexOf("\"", index);

		while (s.charAt(quotePos - 1) == '\\') {
			int even = 1;
			for (int i = quotePos - 2; s.charAt(i) == '\\'; i--)
				even++;
			if (even % 2 == 0) return quotePos;

			quotePos = s.indexOf("\"", quotePos + 1);
		}

		return quotePos;
	}
}
