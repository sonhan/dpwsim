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

import org.ws4d.java.communication.CommunicationBinding;
import org.ws4d.java.service.DefaultDevice;
import org.ws4d.java.structures.ArrayList;
import org.ws4d.java.structures.List;
import org.ws4d.java.types.DiscoveryData;
import org.ws4d.java.types.EndpointReference;
import org.ws4d.java.types.LocalizedString;
import org.ws4d.java.types.QName;
import org.ws4d.java.types.QNameSet;
import org.ws4d.java.types.ScopeSet;
import org.ws4d.java.types.ThisDeviceMData;
import org.ws4d.java.types.ThisModelMData;
import org.ws4d.java.types.URI;
import org.ws4d.java.util.StringUtil;

/**
 * Device properties.
 */
public class DeviceProperties {

	/*
	 * discovery data properties
	 */
	/**
	 * Property identifier of the configuration id, used while constructing a
	 * {@link DefaultDevice#DefaultDevice(int)}
	 */
	public static final String				PROP_CONFIGURATION_ID		= Properties.PROP_CONFIGURATION_ID;

	/**
	 * Property identifier of the stable globally-unique identifier of device.
	 * The value will be the address field within endpoint reference of the
	 * device.
	 */
	public static final String				PROP_DEVICE_UUID			= "DeviceUuid";

	/**
	 * Property identifier of the metadata version of device. If metadata is
	 * changing, version is incremented.
	 */
	public static final String				PROP_METADATA_VERSION		= "MetadataVersion";

	/**
	 * Property identifier of the device port types. Value is a list of
	 * {@link QName}s
	 */
	public static final String				PROP_TYPES					= "Types";

	/**
	 * Property identifier of the list of device scopes. Value is a list of
	 * {@link URI}
	 */
	public static final String				PROP_SCOPES					= "Scopes";

	/**
	 * Property identifier of a binding. The value refers to a binding id of a
	 * binding property
	 */
	public static final String				PROP_BINDING				= Properties.PROP_BINDING;

	public static final String				PROP_SEC					= "DeviceSecured";

	public static final String				PROP_SEC_PRIVATEKEY_ALIAS	= "PrivateKeyAlias";

	public static final String				PROP_SEC_PRIVATEKEY_PASS	= "PrivateKeyPassphrase";

	/*
	 * model data properties
	 */
	/** Property identifier of the model data property "ManufacturerUrl". */
	public static final String				PROP_MANUFACTUERE_URL		= "ManufacturerUrl";

	/** Property identifier of the model data property "ManufacturerName". */
	public static final String				PROP_MANUFACTURER_NAME		= "ManufacturerName";

	/** Property identifier of the model data property "ModelName". */
	public static final String				PROP_MODEL_NAME				= "ModelName";

	/** Property identifier of the model data property "ModelNumber". */
	public static final String				PROP_MODEL_NUMBER			= "ModelNumber";

	/** Property identifier of the model data property "ModelUrl". */
	public static final String				PROP_MODEL_URL				= "ModelUrl";

	/** Property identifier of the model data property "PresentationUrl". */
	public static final String				PROP_PRESENTATION_URL		= "PresentationUrl";

	/*
	 * device data properties
	 */
	/** Property identifier of the device data property "FriendlyName". */
	public static final String				PROP_FRIENDLY_NAME			= "FriendlyName";

	/** Property identifier of the device data property "FirmwareVersion". */
	public static final String				PROP_FIRMWARE_VERSION		= "FirmwareVersion";

	/** Property identifier of the device data property "SerialNumber". */
	public static final String				PROP_SERIAL_NUMBER			= "SerialNumber";

	/*
	 * common device properties
	 */
	public static final Integer				DEFAULT_CONFIGURATION_ID	= new Integer(-1);

	// ------------ DEFAULTS -------------

	private Integer							configurationId				= DEFAULT_CONFIGURATION_ID;

	private DiscoveryData					discoveryData				= null;

	private List							bindings					= new ArrayList(2);

	private List							discoveryBindings			= new ArrayList(2);

	private ThisModelMData					modelData					= null;

	private ThisDeviceMData					deviceData					= null;

	private static final BindingProperties	bindProps					= BindingProperties.getInstance();

	// -------------------------------------------------------

	private boolean							deviceSecured				= false;

	private String							privateKeyAlias				= "defaultPrivKey";

	private String							privateKeyPass				= "default";

	public DeviceProperties() {
		super();
	}

	/**
	 * Copy Constructor. creates a clone of the passed-in instance. Bindings are
	 * not copied.
	 */
	public DeviceProperties(DeviceProperties props) {
		super();
		discoveryData = new DiscoveryData(props.discoveryData);
		deviceData = new ThisDeviceMData(props.deviceData);
		modelData = new ThisModelMData(props.modelData);

		configurationId = props.configurationId;
		bindings = new ArrayList(props.bindings);
		discoveryBindings = new ArrayList(props.discoveryBindings);
	}

	// -------------------------------------------------------

	/**
	 * @return configuration id
	 */
	public Integer getConfigurationId() {
		return configurationId;
	}

	public List getBindings() {
		return bindings;
	}

	public List getDiscoveryBindings() {
		return discoveryBindings;
	}

	public DiscoveryData getDiscoveryData() {
		return discoveryData;
	}

	public ThisModelMData getModelData() {
		return modelData;
	}

	public ThisDeviceMData getDeviceData() {
		return deviceData;
	}

	public boolean useSecurity() {
		return deviceSecured;
	}

	public String getPrivateKeyAlias() {
		return privateKeyAlias;
	}

	public String getPrivateKeyPass() {
		return privateKeyPass;
	}

	void addProperty(Property property) {
		if (discoveryData == null) {
			discoveryData = new DiscoveryData();
		}
		if (PROP_CONFIGURATION_ID.equals(property.key)) {
			configurationId = Integer.valueOf(property.value);
		}
		/*
		 * discovery data properties
		 */
		else if (PROP_DEVICE_UUID.equals(property.key)) {
			discoveryData.setEndpointReference(new EndpointReference(new URI(property.value)));
		} else if (PROP_METADATA_VERSION.equals(property.key)) {
			discoveryData.setMetadataVersion(Long.parseLong(property.value.trim()));
		} else if (PROP_TYPES.equals(property.key)) {
			discoveryData.setTypes(QNameSet.construct(property.value));
			QNameSet qnsTypes = discoveryData.getTypes();
		} else if (PROP_SCOPES.equals(property.key)) {
			discoveryData.setScopes(ScopeSet.construct(property.value));
		} else if (Properties.PROP_BINDING.equals(property.key)) {
			ArrayList bs = (ArrayList) bindProps.getCommunicationBinding(Integer.valueOf(property.value));
			if (bs != null) {
				for (int i = 0; i < bs.size(); i++) {
					bindings.add(bs.get(i));
				}
			}
			CommunicationBinding bds = (CommunicationBinding) bindProps.getDiscoveryBinding(Integer.valueOf(property.value));
			if (bds != null) {
				discoveryBindings.add(bds);
			}
		}
		/*
		 * model data properties
		 */
		else if (PROP_MANUFACTUERE_URL.equals(property.key)) {
			if (modelData == null) {
				modelData = new ThisModelMData();
			}
			modelData.setManufacturerUrl(new URI(property.value));
		} else if (PROP_MANUFACTURER_NAME.equals(property.key)) {
			if (modelData == null) {
				modelData = new ThisModelMData();
			}
			String[] parts = StringUtil.split(property.value, ';');
			LocalizedString s;
			if (parts.length == 1) {
				s = new LocalizedString(parts[0], null);
			} else if (parts.length > 1) {
				s = new LocalizedString(parts[1], parts[0]);
			} else {
				return;
			}
			modelData.addManufacturerName(s);
		} else if (PROP_MODEL_NAME.equals(property.key)) {
			if (modelData == null) {
				modelData = new ThisModelMData();
			}
			String[] parts = StringUtil.split(property.value, ';');
			LocalizedString s;
			if (parts.length == 1) {
				s = new LocalizedString(parts[0], null);
			} else if (parts.length > 1) {
				s = new LocalizedString(parts[1], parts[0]);
			} else {
				return;
			}
			modelData.addModelName(s);
		} else if (PROP_MODEL_NUMBER.equals(property.key)) {
			if (modelData == null) {
				modelData = new ThisModelMData();
			}
			modelData.setModelNumber(property.value);
		} else if (PROP_MODEL_URL.equals(property.key)) {
			if (modelData == null) {
				modelData = new ThisModelMData();
			}
			modelData.setModelUrl(new URI(property.value));
		} else if (PROP_PRESENTATION_URL.equals(property.key)) {
			if (modelData == null) {
				modelData = new ThisModelMData();
			}
			modelData.setPresentationUrl(new URI(property.value));
		}
		/*
		 * device data properties
		 */
		else if (PROP_FRIENDLY_NAME.equals(property.key)) {
			if (deviceData == null) {
				deviceData = new ThisDeviceMData();
			}
			String[] parts = StringUtil.split(property.value, ';');
			LocalizedString s;
			if (parts.length == 1) {
				s = new LocalizedString(parts[0], null);
			} else if (parts.length > 1) {
				s = new LocalizedString(parts[1], parts[0]);
			} else {
				return;
			}
			deviceData.addFriendlyName(s);
		} else if (PROP_FIRMWARE_VERSION.equals(property.key)) {
			if (deviceData == null) {
				deviceData = new ThisDeviceMData();
			}
			deviceData.setFirmwareVersion(property.value);
		} else if (PROP_SERIAL_NUMBER.equals(property.key)) {
			if (deviceData == null) {
				deviceData = new ThisDeviceMData();
			}
			deviceData.setSerialNumber(property.value);
		}

		/* Secured Services */
		else if (PROP_SEC.equals(property.key)) {
			if ("true".equals(property.value)) {
				deviceSecured = true;
			} else if ("false".equals(property.value)) {
				deviceSecured = false;
			}
		} else if (PROP_SEC_PRIVATEKEY_ALIAS.equals(property.key)) {
			privateKeyAlias = property.value;
		} else if (PROP_SEC_PRIVATEKEY_PASS.equals(property.key)) {
			privateKeyPass = property.value;
		}
		/*
		 * common device properties
		 */
	}

}
