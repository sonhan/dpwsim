/*******************************************************************************
 * Copyright (c) 2009 MATERNA Information & Communications. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html. For further
 * project-related information visit http://www.ws4d.org. The most recent
 * version of the JMEDS framework can be obtained from
 * http://sourceforge.net/projects/ws4d-javame.
 ******************************************************************************/
package org.ws4d.java.io.xml;

import org.ws4d.java.util.Log;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

/**
 *
 */
public class XmlPullParserSupport {

	private static final XmlPullParserFactory	FACTORY;

	static {
		XmlPullParserFactory factory = null;
		try {
			factory = XmlPullParserFactory.newInstance();
			factory.setNamespaceAware(true);
			// the next is the default
			// factory.setValidating(false);
		} catch (XmlPullParserException e) {
			Log.error("Could not create XmlPullParserFactory: " + e);
			e.printStackTrace();
			throw new RuntimeException("Could not create XmlPullParserFactory: " + e);
		}
		FACTORY = factory;
	}

	public static XmlPullParserFactory getFactory() {
		return FACTORY;
	}

}
