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
import java.io.Writer;

import org.ws4d.java.structures.Iterator;
import org.ws4d.java.structures.List;
import org.ws4d.java.types.QName;
import org.xmlpull.mxp1_serializer.MXSerializer;

/**
 * 
 */
public class XmlSerializerImplementation extends MXSerializer implements XmlSerializer {

	public static final int	XML_SERIALIZER	= 0;

	private Writer			writer			= null;

	private int				type			= XML_SERIALIZER;

	public XmlSerializerImplementation() {
		super();
	}

	/**
	 * @param writer the writer to set
	 */
	public void setOutput(Writer writer) {
		this.writer = writer;
		super.setOutput(writer);
	}

	/**
	 * Write a block of XML directly to the underlying stream, especially
	 * without escaping any special chars.
	 * 
	 * @param text the XML block to write
	 * @throws IOException
	 */
	public void plainText(String text) throws IOException {
		writer.write(text);
	}

	/**
	 * @param qname the fully qualified name of the elements to expect within
	 *            <code>list</code>
	 * @param elements the list of elements to serialize; all are expected to be
	 *            of the same type; note that this list can be empty or have
	 *            just one element
	 * @throws IOException
	 */
	public void unknownElements(QName qname, List elements) throws IOException {
		ElementHandler handler = ElementHandlerRegistry.getRegistry().getElementHandler(qname);
		if (handler != null) {
			for (Iterator at = elements.iterator(); at.hasNext();) {
				handler.serializeElement(this, qname, at.next());
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.io.xml.XmlSerializer#getType()
	 */
	public int getType() {
		return type;
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.io.xml.XmlSerializer#getOutput()
	 */
	public Writer getOutput() {
		return super.getWriter();
	}

	public void injectSecurityDone() {}

	public void injectSecurityStart() {}

	public void flushCache() {}

}
