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

import org.ws4d.java.constants.SchemaConstants;
import org.ws4d.java.structures.Iterator;

interface Facets extends SchemaConstants {

	public Iterator facets();

	public void addFacet(Facet f);

	// void serialize(XmlSerializer serializer) throws IOException,
	// SchemaException {
	// serializer.startTag(XMLSCHEMA_NAMESPACE, TAG_RESTRICTION);
	// QName baseName = base.getName();
	// String prefix = serializer.getPrefix(baseName.getNamespace(), false);
	// if (prefix != null) {
	// baseName.setPrefix(prefix);
	// serializer.attribute(null, ATTRIBUTE_BASE,
	// baseName.getLocalPartPrefixed());
	// } else {
	// serializer.attribute(null, ATTRIBUTE_BASE, baseName.getLocalPart());
	// }
	// for (Iterator it = facets(); it.hasNext();) {
	// Facet f = (Facet) it.next();
	// f.serialize(serializer);
	// }
	// serializer.endTag(XMLSCHEMA_NAMESPACE, TAG_RESTRICTION);
	// }

}
