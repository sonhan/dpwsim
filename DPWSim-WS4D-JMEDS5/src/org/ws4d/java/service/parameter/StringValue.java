/*******************************************************************************
 * Copyright (c) 2009 MATERNA Information & Communications. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html. For further
 * project-related information visit http://www.ws4d.org. The most recent
 * version of the JMEDS framework can be obtained from
 * http://sourceforge.net/projects/ws4d-javame.
 ******************************************************************************/
package org.ws4d.java.service.parameter;

import java.io.IOException;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

public class StringValue extends ParameterDefinition {

	protected String	value	= null;

	StringValue() {

	}

	public StringValue(String value) {
		this.value = value;
	}

	/**
	 * Returns the value of this parameter value.
	 * 
	 * @return the value.
	 */
	public String get() {
		return this.value;
	}

	/**
	 * Sets the value of this parameter value.
	 * 
	 * @param value the value to set.
	 */
	public void set(String value) {
		this.value = value;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return value == null ? "" : value;
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.service.parameter.Value#getType()
	 */
	public int getValueType() {
		return TYPE_STRING;
	}

	public String serialize() {
		return value;
	}

	public void parse(String content) {
		value = content;
	}

	public void parseContent(XmlPullParser parser) throws IOException, XmlPullParserException {
		int tag = parser.getEventType();
		if (tag == XmlPullParser.START_TAG) {
			tag = parser.next(); // move to the content
		}
		if (tag == XmlPullParser.TEXT) {
			value = parser.getText();
		}
	}

	public void serializeContent(XmlSerializer serializer) throws IOException {
		if (serializer != null && value != null) {
			serializer.text(value);
		}
	}

}
