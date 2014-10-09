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

import java.io.IOException;

import org.ws4d.java.constants.ConstantsHelper;
import org.ws4d.java.constants.WSDConstants;
import org.ws4d.java.io.xml.ElementParser;
import org.ws4d.java.structures.HashMap;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

/**
 * 
 * 
 */
public class AppSequence extends UnknownDataContainer {

	private final long		instanceId;

	private final String	sequenceId;	// optional

	private final long		messageNumber;

	/**
	 * @param instanceId
	 * @param messageNumber
	 */
	public AppSequence(long instanceId, long messageNumber) {
		this(instanceId, null, messageNumber);
	}

	/**
	 * @param instanceId
	 * @param sequenceId
	 * @param messageNumber
	 */
	public AppSequence(long instanceId, String sequenceId, long messageNumber) {
		super();
		this.instanceId = instanceId;
		this.sequenceId = sequenceId;
		this.messageNumber = messageNumber;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("AppSequence [ instanceId=").append(instanceId);
		sb.append(", sequenceId=").append(sequenceId);
		sb.append(", messageNumber=").append(messageNumber);
		sb.append(" ]");
		return sb.toString();
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.types.AppSequence#getInstanceId()
	 */
	public long getInstanceId() {
		return instanceId;
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.types.AppSequence#getSequenceId()
	 */
	public String getSequenceId() {
		return sequenceId;
	}

	/**
	 * Get message number.
	 * 
	 * @return message number.
	 */
	public long getMessageNumber() {
		return messageNumber;
	}

	/**
	 * Checks if this application sequence is newer than the other specified.
	 * 
	 * @param other application sequence to compare with this.
	 * @return whether this instance is newer than the one passed-in
	 */
	public boolean isNewer(AppSequence other) {
		if (instanceId != other.instanceId) {
			if (instanceId > other.instanceId) {
				return true;
			}
			return false;
		} else if (messageNumber > other.messageNumber) {
			return true;
		}

		return false;
	}

	public static AppSequence parse(ElementParser parser, ConstantsHelper helper) throws XmlPullParserException, IOException {
		int attributeCount = parser.getAttributeCount();
		if (attributeCount > 0) {
			// InstanceID MUST be present and it MUST be an unsigned int
			long instanceId = -1L;
			String sequenceId = null;
			// MessageNumber MUST be present and it MUST be an unsigned int
			long messageNumber = -1L;
			HashMap attributes = new HashMap();
			for (int i = 0; i < attributeCount; i++) {
				String namespace = parser.getAttributeNamespace(i);
				String name = parser.getAttributeName(i);
				String value = parser.getAttributeValue(i);
				if ("".equals(namespace)) {
					if (WSDConstants.WSD_ATTR_INSTANCEID.equals(name)) {
						try {
							instanceId = Long.parseLong(value.trim());
						} catch (NumberFormatException e) {
							throw new XmlPullParserException("AppSequence@InstanceId is not a number: " + value.trim());
						}
					} else if (WSDConstants.WSD_ATTR_SEQUENCEID.equals(name)) {
						sequenceId = value;
					} else if (WSDConstants.WSD_ATTR_MESSAGENUMBER.equals(name)) {
						try {
							messageNumber = Long.parseLong(value.trim());
						} catch (NumberFormatException e) {
							throw new XmlPullParserException("AppSequence@MessageNumber is not a number: " + value.trim());
						}
					} else {
						attributes.put(new QName(name, namespace), value);
					}
				} else {
					attributes.put(new QName(name, namespace), value);
				}
			}
			if (instanceId == -1L) {
				throw new XmlPullParserException("AppSequence@InstanceId missing");
			}
			if (messageNumber == -1L) {
				throw new XmlPullParserException("AppSequence@MessageNumber missing");
			}
			AppSequence appSequence = new AppSequence(instanceId, sequenceId, messageNumber);
			while (parser.nextTag() == XmlPullParser.START_TAG) {
				// fill-up child elements
				String namespace = parser.getNamespace();
				String name = parser.getName();
				parser.addUnknownElement(appSequence, namespace, name);
			}
			return appSequence;
		}
		throw new XmlPullParserException("Invalid AppSequence: no attributes");
	}
}
