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

import org.ws4d.java.structures.List;
import org.ws4d.java.types.QName;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

public class QNameValue extends ParameterDefinition {

	protected QName	value	= null;

	QNameValue() {}

	public QNameValue(QName value) {
		this.value = value;
	}

	/**
	 * Returns the value of this parameter value.
	 * 
	 * @return the value.
	 */
	public QName get() {
		return this.value;
	}

	public List getNamespaces() {
		List ns = super.getNamespaces();
		if (value != null) {
			ns.add(value);
		}
		return ns;
	}

	/**
	 * Sets the value of this parameter value.
	 * 
	 * @param value the value to set.
	 */
	public void set(QName value) {
		this.value = value;
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.service.parameter.Value#getType()
	 */
	public int getValueType() {
		return TYPE_QNAME;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return value == null ? "" : value.toStringPlain();
	}

	public String serialize() {
		return value.toStringPlain();
	}

	public void parse(String content) {
		this.value = QName.construct(content);
	}

	public void parseContent(XmlPullParser parser) throws IOException, XmlPullParserException {
		if (parser.getEventType() != XmlPullParser.TEXT) {
			parser.next();
		}
		if (parser.getEventType() == XmlPullParser.TEXT) {
			String text = parser.getText();
			int index = text.lastIndexOf(':');
			if (index > 1) {
				String prefix = text.substring(0, index);
				String localPart = text.substring(index + 1);
				String namespace = parser.getNamespace(prefix);
				value = new QName(localPart, namespace, prefix);
			} else {
				value = new QName(text, null);
			}
			return;
		}
		throw new IOException("Could not parse QName form incoming data. [ Element=" + parser.getName() + " ]");
	}

	public void serializeContent(XmlSerializer serializer) throws IOException {
		if (value == null) {
			return;
		}
		String prefix = serializer.getPrefix(value.getNamespace(), false);
		serializer.text((prefix == null ? "" : prefix + ':') + value.getLocalPart());
	}

}
