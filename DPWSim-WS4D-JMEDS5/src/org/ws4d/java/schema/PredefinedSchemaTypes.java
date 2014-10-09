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

import org.ws4d.java.constants.WSAConstants;
import org.ws4d.java.constants.WSEConstants;
import org.ws4d.java.types.QName;

/**
 * 
 */
public final class PredefinedSchemaTypes {

	public static final Element	WSA_PROBLEM_ACTION;

	public static final Element	WSA_PROBLEM_HEADER_QNAME	= new Element(WSAConstants.WSA_PROBLEM_HEADER_QNAME, SchemaUtil.getSchemaType(SchemaUtil.TYPE_QNAME));

	public static final Element	WSE_SUPPORTED_DELIVERY_MODE	= new Element(WSEConstants.WSE_SUPPORTED_DELIVERY_MODE, SchemaUtil.getSchemaType(SchemaUtil.TYPE_ANYURI));

	public static final Element	WSE_SUPPORTED_DIALECT		= new Element(WSEConstants.WSE_SUPPORTED_DIALECT, SchemaUtil.getSchemaType(SchemaUtil.TYPE_ANYURI));

	static {
		ComplexType detailType = new ComplexType("WSAProblemActionType", WSAConstants.WSA_NAMESPACE_NAME, ComplexType.CONTAINER_ALL);
		Element actionElement = new Element(new QName(WSAConstants.WSA_ELEM_ACTION, WSAConstants.WSA_NAMESPACE_NAME), SchemaUtil.getSchemaType(SchemaUtil.TYPE_ANYURI));
		detailType.addElement(actionElement);
		WSA_PROBLEM_ACTION = new Element(WSAConstants.WSA_PROBLEM_ACTION, detailType);
	}

}
