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

import org.ws4d.java.structures.LinkedList;
import org.xmlpull.v1.XmlSerializer;

/**
 * Container for complexType:sequence.
 */
public class SequenceContainer extends ElementContainer {

	public SequenceContainer() {
		super(new LinkedList());
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.types.schema.ElementContainer#toString()
	 */
	public String toString() {
		int all = getElementCount();
		return "SequenceContainer [ own=" + elementCount + ", inherit=" + (all - elementCount) + ", all=" + all + ", min=" + min + ", max=" + max + ", container=" + container + " ]";
	}

	public int getSchemaIdentifier() {
		return XSD_SEQUENCEMODEL;
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.types.schema.ElementContainer#getContainerType()
	 */
	public int getContainerType() {
		return ComplexType.CONTAINER_SEQUENCE;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.ws4d.java.types.schema.ElementContainer#serialize(org.xmlpull.v1.
	 * XmlSerializer, org.ws4d.java.types.schema.Schema)
	 */
	void serialize(XmlSerializer serializer, Schema schema) throws IOException {
		serializer.startTag(XMLSCHEMA_NAMESPACE, TAG_SEQUENCE);
		serialize0(serializer, schema);
		serializer.endTag(XMLSCHEMA_NAMESPACE, TAG_SEQUENCE);
	}

}
