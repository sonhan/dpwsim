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

/**
 * This class represents a string for a specific language. The language tag is
 * defined by:
 * <ul>
 * <li>The syntax of the language tags is described in RFC 5646.
 * <li>All language subtags are registered to the IANA Language Subtag Registry.
 * <li>All region subtags are specified in "ISO 3166: Codes for Country Names".
 * </ul>
 */
public class LocalizedString extends UnknownDataContainer {

	/** language tag for English language in the US region */
	public static final String	LANGUAGE_EN		= "en-US";

	/** language tag for German language in the german region */
	public static final String	LANGUAGE_DE		= "de-DE";

	/** language tag for the default language (en-US) */
	public static final String	DEFAULT_LANG	= LANGUAGE_EN;

	/** string value */
	protected String			value;

	/** language of the string */
	protected String			lang;

	/** lazy initializated hash code */
	private int					hashCode		= 0;

	/**
	 * Constructor. The value holds a string in the given language. The language
	 * tag is defined by:
	 * <ul>
	 * <li>The syntax of the language tags is described in RFC 5646.
	 * <li>All language subtags are registered to the IANA Language Subtag
	 * Registry.
	 * <li>All region subtags are specified in
	 * "ISO 3166: Codes for Country Names".
	 * </ul>
	 * 
	 * @param value string value
	 * @param lang language tag of the string.
	 */
	public LocalizedString(String value, String lang) {
		super();
		this.value = value;
		this.lang = lang == null ? DEFAULT_LANG : lang;
	}

	/**
	 * Gets string value
	 * 
	 * @return value
	 */
	public String getValue() {
		return value;
	}

	/**
	 * Gets language tag as string. The language tag is defined by:
	 * <ul>
	 * <li>The syntax of the language tags is described in RFC 5646.
	 * <li>All language subtags are registered to the IANA Language Subtag
	 * Registry.
	 * <li>All region subtags are specified in
	 * "ISO 3166: Codes for Country Names".
	 * </ul>
	 * 
	 * @return The language tag.
	 */
	public String getLanguage() {
		return lang;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return lang + "=" + value;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof LocalizedString)) {
			return false;
		}

		LocalizedString lString = (LocalizedString) obj;
		if (lang == null) {
			if (lString.lang != null) {
				return false;
			}
		} else {
			if (!lang.equals(lString.lang)) {
				return false;
			}
		}
		if (value == null) {
			if (lString.value != null) {
				return false;
			}
		}
		return value.equals(lString.value);
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		int hashCode = this.hashCode;
		if (hashCode == 0) {
			hashCode = 527 + value.hashCode();
			hashCode = 31 * hashCode + lang.hashCode();
			this.hashCode = hashCode;
		}
		return hashCode;
	}

	// /* (non-Javadoc)
	// * @see java.lang.Object#hashCode()
	// */
	// public int hashCode(){
	// int hashCode = this.hashCode;
	// if( hashCode == 0){
	// hashCode = 527 + value.hashCode();
	// hashCode = (hashCode << 5) - hashCode + lang.hashCode();
	// this.hashCode = hashCode;
	// }
	// return hashCode;
	// }

	// public int hashCodeSlowest(){
	// int hashCode = this.hashCode;
	// if( hashCode == 0){
	// hashCode = 17 ;
	// hashCode = 31 * hashCode + value.hashCode();
	// hashCode = 31 * hashCode + lang.hashCode();
	// this.hashCode = hashCode;
	// }
	// return hashCode;
	// }

	// public static void main(String[] args) {
	// Random rnd;
	// System.out.println("TEST");
	// int rounds = 2000000;
	// int outer_rounds = 10;
	// // int rounds = 1;
	// int h;
	//
	// long duration;
	// long duration_absolut = 0;
	// double avg_slow;
	// double avg_slowest;
	// double avg_fast;
	// long start;
	// Date d;
	//
	//
	// for( int j = 0; j < outer_rounds; j++ ){
	// // SLOW
	// rnd = new Random(1237);
	// h = 0;
	// d = new Date();
	// start = d.getTime();
	//
	// for( int i = 0; i < rounds ; i++){
	// String v = Long.toString( rnd.nextLong() );
	// String l = Long.toString( rnd.nextLong() );
	// LocalizedString s = new LocalizedString( v, l );
	// h += s.hashCodeSlow();
	// }
	// d = new Date();
	// duration = d.getTime() - start;
	// System.out.println("slow: " + duration +" | " + h);
	// duration_absolut += duration;
	// }
	// avg_slow = duration_absolut / outer_rounds;
	// duration_absolut = 0;
	//
	// for( int j = 0; j < outer_rounds; j++ ){
	//
	// // SLOWEST
	// rnd = new Random(1237);
	// h= 0;
	// d = new Date();
	// start = d.getTime();
	//
	// for( int i = 0; i < rounds ; i++){
	// String v = Long.toString( rnd.nextLong() );
	// String l = Long.toString( rnd.nextLong() );
	// LocalizedString s = new LocalizedString( v, l );
	// h += s.hashCodeSlowest();
	// }
	// d = new Date();
	// duration = d.getTime() - start;
	// System.out.println("slowest: " + duration +" | " + h);
	// duration_absolut += duration;
	// }
	// avg_slowest = duration_absolut / outer_rounds;
	// duration_absolut = 0;
	//
	// for( int j = 0; j < outer_rounds; j++ ){
	//
	// // FAST
	// rnd = new Random(1237);
	// h= 0;
	// d = new Date();
	// start = d.getTime();
	//
	// for( int i = 0; i < rounds ; i++){
	// String v = Long.toString( rnd.nextLong() );
	// String l = Long.toString( rnd.nextLong() );
	// LocalizedString s = new LocalizedString( v, l );
	// h += s.hashCode();
	// }
	// d = new Date();
	// duration = d.getTime() - start;
	// System.out.println("fast: " + duration +" | " + h);
	// duration_absolut += duration;
	// }
	// avg_fast = duration_absolut / outer_rounds;
	// duration_absolut = 0;
	//
	//
	//
	// System.out.println("-----------------------------");
	// System.out.println("fast avg   : " + avg_fast);
	// System.out.println("slow avg   : " + avg_slow);
	// System.out.println("slowest avg: " + avg_slowest);
	// }

}
