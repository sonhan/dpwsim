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

import org.ws4d.java.io.xml.ElementParser;
import org.ws4d.java.structures.EmptyStructures;
import org.ws4d.java.structures.Iterator;
import org.ws4d.java.structures.LinkedList;
import org.ws4d.java.structures.List;
import org.ws4d.java.util.StringUtil;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

/**
 * Annotation class. Every schema entry can have an annotation.
 */
abstract class Annotation implements Any {

	static final String	TAG_DOCUMENTATION	= SCHEMA_DOCUMENTATION;

	static final String	TAG_APPINFO			= SCHEMA_APP_INFO;

	static final String	TAG_ANNOTATION		= SCHEMA_ANNOTATION;

	protected List		documentation		= EmptyStructures.EMPTY_LIST;

	protected List		appinfo				= EmptyStructures.EMPTY_LIST;

	static final void serialize(XmlSerializer serializer, Annotation target) throws IOException {
		if ((target.documentation == null || target.documentation.size() == 0) && ((target.appinfo == null || target.appinfo.size() == 0))) return;
		serializer.startTag(XMLSCHEMA_NAMESPACE, TAG_ANNOTATION);
		if (target.documentation != null && target.documentation.size() > 0) {
			for (Iterator it = target.documentation.iterator(); it.hasNext();) {
				String text = (String) it.next();
				serializer.startTag(XMLSCHEMA_NAMESPACE, TAG_DOCUMENTATION);
				serializer.text(text);
				serializer.endTag(XMLSCHEMA_NAMESPACE, TAG_DOCUMENTATION);
			}
		}
		if (target.appinfo != null && target.appinfo.size() > 0) {
			for (Iterator it = target.appinfo.iterator(); it.hasNext();) {
				String text = (String) it.next();
				serializer.startTag(XMLSCHEMA_NAMESPACE, TAG_APPINFO);
				serializer.text(text);
				serializer.endTag(XMLSCHEMA_NAMESPACE, TAG_APPINFO);
			}
		}
		serializer.endTag(XMLSCHEMA_NAMESPACE, TAG_ANNOTATION);
	}

	static final void handleAnnotation(ElementParser parser, Annotation target) throws XmlPullParserException, IOException {
		int i = parser.getDepth();
		while (parser.nextTag() != XmlPullParser.END_TAG && parser.getDepth() >= i + 1) {
			String name = parser.getName();
			String namespace = parser.getNamespace();
			if (XMLSCHEMA_NAMESPACE.equals(namespace)) {
				if (parser.getEventType() == XmlPullParser.START_TAG && StringUtil.equalsIgnoreCase(Annotation.TAG_DOCUMENTATION, name)) {
					int d = parser.getDepth();
					parser.next();
					while (parser.getDepth() >= d) {
						if (parser.getEventType() == XmlPullParser.END_TAG && parser.getDepth() == d) {
							break;
						}
						if (parser.getEventType() == XmlPullParser.TEXT && parser.getDepth() == d) {
							if (!parser.isWhitespace()) {
								target.addDocumentation(parser.getText());
							}
						}
						parser.next();
					}
					if (parser.getEventType() == XmlPullParser.TEXT && parser.getDepth() == d) {
						if (!parser.isWhitespace()) {
							target.addDocumentation(parser.getText());
						}
					}
				} else if (parser.getEventType() == XmlPullParser.START_TAG && StringUtil.equalsIgnoreCase(TAG_APPINFO, name)) {
					// Bugfix 2010-07-15 SSch, THX to Stefan Schlichting!
					if (parser.getDepth() == 0)
						target.addAppInfo(parser.nextText());
					else {
						// HANDLE DEPTH>0
						int d = parser.getDepth();
						parser.next();

						while (parser.getDepth() >= d) {
							// Are we at the level where we started? Then we are
							// done.
							if (parser.getEventType() == XmlPullParser.END_TAG && parser.getDepth() == d) {
								break;
							}
							// Ignore the rest
							parser.next();
						}
					}
				}
			}
		}
	}

	protected void addDocumentation(String documentation) {
		if (this.documentation == EmptyStructures.EMPTY_LIST) {
			this.documentation = new LinkedList();
		}
		this.documentation.add(documentation);
	}

	protected Iterator getDocumentations() {
		return documentation.iterator();
	}

	protected void addAppInfo(String documentation) {
		if (appinfo == EmptyStructures.EMPTY_LIST) {
			appinfo = new LinkedList();
		}
		this.appinfo.add(documentation);
	}

	protected Iterator getAppInfos() {
		return appinfo.iterator();
	}

}
