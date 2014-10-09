/*******************************************************************************
 * Copyright (c) 2009 MATERNA Information & Communications. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html. For further
 * project-related information visit http://www.ws4d.org. The most recent
 * version of the JMEDS framework can be obtained from
 * http://sourceforge.net/projects/ws4d-javame.
 ******************************************************************************/
package org.ws4d.java.io.xml.cache;

import java.io.IOException;

import org.ws4d.java.io.xml.XmlSerializer;
import org.ws4d.java.io.xml.canonicalization.CanonicalSerializer;
import org.xmlpull.v1.IllegalStateException;

public interface XmlStructure {

	public static final int	XML_START_TAG	= 0x0;

	public static final int	XML_END_TAG		= 0x1;

	public static final int	XML_PREFIX		= 0x2;

	public static final int	XML_ATTRIBUTE	= 0x3;

	public static final int	XML_TEXT		= 0x4;

	public int getType();

	public String getNamespace();

	public void setNameSpace(String ns);

	public String getName();

	public String getValue();

	public void flush(CanonicalSerializer serializer) throws IllegalArgumentException, IllegalStateException, IOException;

	public void flush(XmlSerializer serializer) throws IllegalArgumentException, IllegalStateException, IOException;
}
