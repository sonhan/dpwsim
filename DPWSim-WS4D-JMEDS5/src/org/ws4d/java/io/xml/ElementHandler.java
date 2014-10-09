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

import java.io.IOException;

import org.ws4d.java.types.QName;
import org.ws4d.java.util.WS4DIllegalStateException;
import org.xmlpull.v1.XmlPullParserException;

/**
 * Implementations of this interface are responsible for parsing a specific XML
 * element and re-creating its content as a Java object graph.
 * <strong>Warning:</strong> Each element handler instance may and
 * <strong>will</strong> be called simultaneously by different threads!
 * Therefore, element handlers <strong>must not</strong> keep any state about
 * the current parse process within local variables or similar or they always
 * must associate it with the current thread.
 */
public interface ElementHandler {

	/**
	 * Processes the content (<em>both child elements and attributes</em>) of
	 * the element with the specified <code>elementName</code> and returns its
	 * representation as a Java object. The element content can be obtained
	 * successively from the provided <code>parser</code>. It is guaranteed that
	 * this parser will be always namespace-aware. Note that the parser's
	 * current position is already at the start position of the current element,
	 * that is, the following assumptions hold immediately after entering this
	 * method:
	 * <p>
	 * <code>
	 * parser.getEventType() == XmlPullParser.START_TAG<br />
	 * <br />
	 * parser.getName() == elementName.getLocalPart()<br />
	 * <br />
	 * parser.getNamespace() == elementName.getNamespace()<br />
	 * </code>
	 * </p>
	 * <strong>Attention!</strong> The provided parser supports neither custom
	 * properties, nor any features! Also, it doesn't accept any entity
	 * replacement text.
	 * 
	 * @param elementName the qualified name of the element to process
	 * @param parser the parser to obtain element data from
	 * @return a Java object corresponding to the content of the given XML
	 *         element
	 * @throws XmlPullParserException if an error during processing of the
	 *             element content occurs
	 * @throws IOException if an error during reading the element source occurs
	 */
	public Object handleElement(QName elementName, ElementParser parser) throws XmlPullParserException, IOException;

	/**
	 * Method to serialize the UnknownElement
	 * 
	 * @param serializer ,the Serializer which is used to serialize the
	 *            UnknownElement
	 * @param qname ,the qualified name of the UnknownElement to process
	 * @param value ,the value of the UnknownElement
	 * @throws IllegalArgumentException
	 * @throws WS4DIllegalStateException
	 * @throws IOException
	 */
	public void serializeElement(XmlSerializer serializer, QName qname, Object value) throws IllegalArgumentException, WS4DIllegalStateException, IOException;

}
