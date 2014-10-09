/*******************************************************************************
 * Copyright (c) 2009 MATERNA Information & Communications. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html. For further
 * project-related information visit http://www.ws4d.org. The most recent
 * version of the JMEDS framework can be obtained from
 * http://sourceforge.net/projects/ws4d-javame.
 ******************************************************************************/

package org.ws4d.java.types;

import org.ws4d.java.structures.DataStructure;
import org.ws4d.java.structures.HashMap;
import org.ws4d.java.util.StringUtil;

/**
 * @author mspies
 */
public class ThisDeviceMData extends UnknownDataContainer {

	// public static final URI THIS_DEVICE_METADATA_DIALECT = new
	// URI(DPWSConstants.METADATA_DIALECT_THISDEVICE);

	/** HashMap<String language, LocalizedString dpws:FriendlyName> */
	private HashMap	friendlyNames	= new HashMap();

	private String	firmwareVersion	= "1";

	private String	serialNumber	= "1";

	public ThisDeviceMData() {
		super();
	}

	/**
	 * Copy Constructor. Data structure objects will also be copied.
	 */
	public ThisDeviceMData(ThisDeviceMData metadata) {
		super(metadata);

		if (metadata == null) {
			return;
		}

		friendlyNames.putAll(metadata.friendlyNames);
		firmwareVersion = metadata.firmwareVersion;
		serialNumber = metadata.serialNumber;
	}

	// ---------------------------------------------------

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		StringBuffer sb = new StringBuffer(StringUtil.formatClassName(getClass()));
		sb.append(" [ friendlyNames=").append(friendlyNames);
		sb.append(", firmwareVersion=").append(firmwareVersion);
		sb.append(", serialNumber=").append(serialNumber);
		sb.append(" ]");
		return sb.toString();
	}

	// ------------------ GETTER -----------------------

	public String getFirmwareVersion() {
		return firmwareVersion;
	}

	public DataStructure getFriendlyNames() {
		return friendlyNames.values();
	}

	public LocalizedString getFriendlyName(String lang) {
		HashMap friendlyNames = this.friendlyNames;

		if (friendlyNames == null) {
			return null;
		} else {
			return (LocalizedString) friendlyNames.get(lang);
		}
	}

	public String getSerialNumber() {
		return serialNumber;
	}

	// -------------------- SETTER -----------------------

	/**
	 * Gets firmware version
	 * 
	 * @param firmwareVersion
	 */
	public void setFirmwareVersion(String firmwareVersion) {
		this.firmwareVersion = firmwareVersion;
	}

	/**
	 * Sets friendly names of device.
	 * 
	 * @param friendlyNames HahshMap with friendly names of device, key must be
	 *            the language string. HashMap<String language, LocalizedString
	 *            dpws:FriendlyName>
	 */
	public void setFriendlyNames(HashMap friendlyNames) {
		this.friendlyNames = friendlyNames;
	}

	/**
	 * Adds friendly name in specified language.
	 * 
	 * @param friendlyName Friendly name of device.
	 */
	public void addFriendlyName(LocalizedString friendlyName) {
		if (friendlyNames == null) {
			friendlyNames = new HashMap();
		}

		friendlyNames.put(friendlyName.getLanguage(), friendlyName);
	}

	/**
	 * Sets serial number
	 * 
	 * @param serialNumber
	 */
	public void setSerialNumber(String serialNumber) {
		this.serialNumber = serialNumber;
	}

}
