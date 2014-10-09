/*******************************************************************************
 * Copyright (c) 2009 MATERNA Information & Communications. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html. For further
 * project-related information visit http://www.ws4d.org. The most recent
 * version of the JMEDS framework can be obtained from
 * http://sourceforge.net/projects/ws4d-javame.
 ******************************************************************************/
package org.ws4d.java.dispatch;

import org.ws4d.java.message.Message;
import org.ws4d.java.structures.ArrayList;
import org.ws4d.java.structures.HashMap;
import org.ws4d.java.structures.HashMap.Entry;
import org.ws4d.java.structures.Iterator;
import org.ws4d.java.structures.List;
import org.ws4d.java.types.EndpointReference;
import org.ws4d.java.types.URI;

/**
 * This class allows matching messages according to their
 * {@link Message#getType() message type} or the {@link Message#getTo() address
 * of the endpoint} they are addressed to.
 * <p>
 * Selection criteria can be specified in a generic way by means of the
 * {@link #setSelectionProperty(int, List)} method. This method allows
 * declaration of expected values for each property in question. A message will
 * match those criteria only if it has matching values for all the given
 * properties.
 * </p>
 */
public class DefaultMessageSelector implements MessageSelector {

	/**
	 * Property denoting a selection based on the message type.
	 */
	public static final int	MESSAGE_TYPE		= 1;

	/**
	 * Property denoting a selection based on the target
	 * {@link EndpointReference endpoint reference} of the message (i.e. its
	 * WS-Addressing [destination] property). The [destination] property is
	 * actually an IRI consisting of the [address] property of the target's
	 * endpoint reference. Nevertheless, when specifying a selection rule by
	 * means of {@link #setSelectionProperty(int, List)} method, the provided
	 * {@link List} <code>values</code> is expected to contain
	 * {@link EndpointReference} instances rather than {@link URI}s.
	 */
	public static final int	ENDPOINT_REFERENCE	= 2;

	// key = Integer (property type), value = list of acceptable property values
	private final HashMap	selectionRules		= new HashMap();

	private static boolean isKnownPropertyType(int propertyType) {
		return MESSAGE_TYPE == propertyType || ENDPOINT_REFERENCE == propertyType;
	}

	/**
	 * Create a new default message selector.
	 */
	public DefaultMessageSelector() {
		super();
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.communication.MessageSelector#matches(org.ws4d.
	 * java.communication.message.DPWSMessage)
	 */
	public synchronized boolean matches(Message msg) {
		boolean result = true;
		for (Iterator it = selectionRules.entrySet().iterator(); it.hasNext();) {
			Entry ent = (Entry) it.next();
			int propertyType = ((Integer) ent.getKey()).intValue();
			List values = (List) ent.getValue();
			if (values == null || values.size() == 0) {
				continue;
			}
			// realize intersection semantics
			switch (propertyType) {
				case (MESSAGE_TYPE): {
					int msgType = msg.getType();
					boolean typeMatches = false;
					for (Iterator it2 = values.iterator(); it2.hasNext();) {
						Integer requestedMsgType = (Integer) it2.next();
						if (msgType == requestedMsgType.intValue()) {
							typeMatches = true;
							break;
						}
					}
					result &= typeMatches;
					break;
				}
				case (ENDPOINT_REFERENCE): {
					// FIXME get the appropriate ER!!!
					EndpointReference endpointReference = new EndpointReference(msg.getTo());
					boolean refMatches = false;
					for (Iterator it2 = values.iterator(); it2.hasNext();) {
						EndpointReference requestedEndpointReference = (EndpointReference) it2.next();
						if (endpointReference.equals(requestedEndpointReference)) {
							refMatches = true;
							break;
						}
					}
					result &= refMatches;
					break;
				}
			}
		}
		return result;
	}

	/**
	 * Adds a <em>&quot;selection rule&quot;</em> by specifying a message
	 * <code>property</code> and declaring a list of allowed <code>values</code>
	 * for this property. Each message, which has a value for this property that
	 * is contained within <code>values</code>, will be considered for a match.
	 * In case the provided list of <code>values</code> is <code>null</code> or
	 * empty, a message will always match regardless of its actual value for the
	 * property.
	 * <p>
	 * Calling this method more than once with the same <code>property</code>
	 * but different <code>values</code> overwrites (i.e. replaces) previously
	 * specified <code>values</code> for that property.
	 * </p>
	 * 
	 * @param property the property to set expected values for; allowed values
	 *            are {@link #MESSAGE_TYPE} and {@link #ENDPOINT_REFERENCE}
	 * @param values a list of values the <code>property</code> may take in
	 *            order to get matched by this selector; if empty, then any
	 *            value of the property would match
	 * @throws IllegalArgumentException if <code>propertyType</code> is neither
	 *             {@link #MESSAGE_TYPE} nor {@link #ENDPOINT_REFERENCE}
	 */
	public synchronized void setSelectionProperty(int property, List values) {
		if (!isKnownPropertyType(property)) {
			throw new IllegalArgumentException("invalid property: " + property);
		}
		selectionRules.put(new Integer(property), values == null ? null : new ArrayList(values));
	}

}
