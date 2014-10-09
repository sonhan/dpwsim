/*******************************************************************************
 * Copyright (c) 2009 MATERNA Information & Communications. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html. For further
 * project-related information visit http://www.ws4d.org. The most recent
 * version of the JMEDS framework can be obtained from
 * http://sourceforge.net/projects/ws4d-javame.
 ******************************************************************************/
package org.ws4d.java.configuration;

import org.ws4d.java.structures.ArrayList;
import org.ws4d.java.structures.DataStructure;
import org.ws4d.java.structures.HashMap;
import org.ws4d.java.structures.Iterator;

public class EventingProperties implements PropertiesHandler {

	public static final String	PROP_CONFIGURATION_ID	= Properties.PROP_CONFIGURATION_ID;

	public static final String	PROP_BINDING			= Properties.PROP_BINDING;

	private Integer				tmpConfigurationId		= null;

	private DataStructure		tmpBindings				= new ArrayList(1);

	// private static String className;
	//
	// private static EventingProperties handler = null;

	/** map: ConfigurationId<Integer> => Bindings<List of Integer> */
	private static HashMap		map_CID_2_Bindings		= new HashMap();

	/**
	 * Constructor.
	 */
	EventingProperties() {
		super();
		// if (handler != null) {
		// throw new
		// RuntimeException("DevicesPropertiesHandler: class already instantiated!");
		// }
		// className = this.getClass().getName();
		// handler = this;
	}

	/**
	 * Returns instance of the eventing properties handler.
	 * 
	 * @return the singleton instance of the eventing properties
	 */
	public static EventingProperties getInstance() {
		// if (handler == null) {
		// handler = new EventingProperties();
		// }
		// return handler;
		return (EventingProperties) Properties.forClassName(Properties.EVENTING_PROPERTIES_HANDLER_CLASS);
	}

	// /**
	// * Returns class name if object of this class had previously been created,
	// * else null.
	// *
	// * @return Class name if object of this class had previously been created,
	// * else null.
	// */
	// public static String getClassName() {
	// return className;
	// }

	// -----------------------------------------------------------

	public void finishedSection(int depth) {
		if (depth == 2) {
			if (tmpConfigurationId != null) {
				map_CID_2_Bindings.put(tmpConfigurationId, tmpBindings);
			}
			tmpBindings = new ArrayList(1);
			tmpConfigurationId = null;
		} else if (depth < 2) {
			tmpBindings = new ArrayList(1);
			tmpConfigurationId = null;
		}
	}

	public void setProperties(PropertyHeader header, Property property) {
		if (Properties.HEADER_SECTION_EVENTING.equals(header)) {
			// Properties of "Devices" Section, default for devices
		}

		else if (Properties.HEADER_SUBSECTION_EVENT_SINK.equals(header)) {
			if (PROP_BINDING.equals(property.key)) {
				tmpBindings.add(Integer.valueOf(property.value));
			} else if (PROP_CONFIGURATION_ID.equals(property.key)) {
				tmpConfigurationId = Integer.valueOf(property.value);
			}
		}

	}

	/**
	 * @param configurationId
	 * @return the bindings
	 */
	public DataStructure getBindings(Integer configurationId) {
		DataStructure bindingIds = (DataStructure) map_CID_2_Bindings.get(configurationId);
		DataStructure bindings = new ArrayList(bindingIds.size());

		for (Iterator it = bindingIds.iterator(); it.hasNext();) {
			Integer bindingId = (Integer) it.next();
			ArrayList bs = (ArrayList) BindingProperties.getInstance().getCommunicationBinding(bindingId);
			for (int i = 0; i < bs.size(); i++) {
				bindings.add(bs.get(i));
			}
		}
		return bindings;
	}

}
