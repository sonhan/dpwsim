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
import org.ws4d.java.structures.List;
import org.ws4d.java.types.URI;

/**
 * Class of service properties. Sets and holds service properties.
 * 
 * @author mspies
 */
public class ServiceProperties {

	// ---------- CONST ------------------

	public static final String				PROP_CONFIGURATION_ID		= Properties.PROP_CONFIGURATION_ID;

	/** Whether the service should be secured or not (not implemented) */
	public static final String				PROP_SERVICE_SECURED		= "ServiceSecured";

	public static final String				PROP_SEC_PRIVATEKEY_ALIAS	= "PrivateKeyAlias";

	public static final String				PROP_SEC_PRIVATEKEY_PASS	= "PrivateKeyPassphrase";

	public static final String				PROP_SERVICE_ID				= "ServiceId";

	public static final String				PROP_BINDING				= Properties.PROP_BINDING;

	public static final Integer				DEFAULT_CONFIGURATION_ID	= new Integer(-1);

	// ------------ DEFAULTS -------------

	private Integer							configurationId				= DEFAULT_CONFIGURATION_ID;

	private boolean							serviceSecured				= false;

	private List							bindings					= new ArrayList(2);

	private URI								serviceId					= null;

	private String							privateKeyAlias				= null;

	private String							privateKeyPass				= null;

	// ----------------------------------------------------

	private static final BindingProperties	bindProps					= BindingProperties.getInstance();

	// ------------------------------- GETTER / SETTER
	// ---------------------------------

	/**
	 * Constructor.
	 */
	ServiceProperties() {}

	/**
	 * Copy Constructor.
	 * 
	 * @param props
	 */
	ServiceProperties(ServiceProperties props) {
		configurationId = props.configurationId;
		serviceSecured = props.serviceSecured;

		bindings = new ArrayList(props.bindings);
	}

	/**
	 * Gets configuration id.
	 * 
	 * @return Configuration id.
	 */
	public Integer getConfigurationId() {
		return configurationId;
	}

	/**
	 * Sets configuration id.
	 * 
	 * @param configurationId Configuration to set.
	 */
	public void setConfigurationId(Integer configurationId) {
		this.configurationId = configurationId;
	}

	/**
	 * Is the service secured?
	 * 
	 * @return whther the service shell be secured or not
	 */
	public boolean isServiceSecured() {
		return serviceSecured;
	}

	/**
	 * Configures service transport security.
	 * 
	 * @param serviceSecured
	 */
	public void setServiceSecured(boolean serviceSecured) {
		this.serviceSecured = serviceSecured;
	}

	/**
	 * Gets service bindings.
	 * 
	 * @return list of service bindings
	 */
	public List getBindings() {
		return bindings;
	}

	/**
	 * @return service id as uri.
	 */
	public URI getServiceId() {
		return serviceId;
	}

	public String getPrivateKeyAlias() {
		return privateKeyAlias;
	}

	public String getPrivateKeyPass() {
		return privateKeyPass;
	}

	/**
	 * Adds property to this.
	 * 
	 * @param property to add to this service's properties.
	 */
	void addProperty(Property property) {
		if (PROP_CONFIGURATION_ID.equals(property.key)) {
			configurationId = Integer.valueOf(property.value);
		}
		/*
		 * 
		 */
		else if (PROP_SERVICE_SECURED.equals(property.key)) {
			if ("true".equals(property.value)) {
				serviceSecured = true;
			} else if ("false".equals(property.value)) {
				serviceSecured = false;
			}
		} else if (PROP_SEC_PRIVATEKEY_ALIAS.equals(property.key)) {
			privateKeyAlias = property.value;
		} else if (PROP_SEC_PRIVATEKEY_PASS.equals(property.key)) {
			privateKeyPass = property.value;

		} else if (Properties.PROP_BINDING.equals(property.key)) {
			ArrayList bs = (ArrayList) bindProps.getCommunicationBinding(Integer.valueOf(property.value));
			for (int i = 0; i < bs.size(); i++) {
				bindings.add(bs.get(i));
			}
		} else if (PROP_SERVICE_ID.equals(property.key)) {
			serviceId = new URI(property.value);
		}
	}

}
