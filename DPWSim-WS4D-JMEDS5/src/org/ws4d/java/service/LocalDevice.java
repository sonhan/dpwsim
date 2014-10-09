/*******************************************************************************
 * Copyright (c) 2009 MATERNA Information & Communications. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html. For further
 * project-related information visit http://www.ws4d.org. The most recent
 * version of the JMEDS framework can be obtained from
 * http://sourceforge.net/projects/ws4d-javame.
 ******************************************************************************/
package org.ws4d.java.service;

import java.io.IOException;

import org.ws4d.java.communication.Bindable;
import org.ws4d.java.communication.CommunicationBinding;
import org.ws4d.java.communication.CommunicationManager;
import org.ws4d.java.communication.protocol.http.HTTPGroup;
import org.ws4d.java.concurrency.Lockable;
import org.ws4d.java.configuration.Properties;
import org.ws4d.java.dispatch.DeviceServiceRegistry;
import org.ws4d.java.structures.HashMap;
import org.ws4d.java.structures.Iterator;
import org.ws4d.java.types.DiscoveryData;
import org.ws4d.java.types.EndpointReference;
import org.ws4d.java.types.ProbeScopeSet;
import org.ws4d.java.types.QNameSet;
import org.ws4d.java.types.ScopeSet;
import org.ws4d.java.types.ThisDeviceMData;
import org.ws4d.java.types.ThisModelMData;

/**
 * Interface of local devices.
 */
public interface LocalDevice extends Device, Bindable, Lockable {

	/**
	 * This will return the discovery data of the local device. Changes to the
	 * discovery data must be avoided outside of the local device.
	 * 
	 * @return this device's discovery data
	 */
	public DiscoveryData getDiscoveryData();

	/**
	 * Returns if the device was started and is running now.
	 * 
	 * @return <code>true</code>, if this device is running
	 */
	public boolean isRunning();

	/**
	 * Stops the device. Stopping the device will:
	 * <ul>
	 * <li>stop its services,
	 * <li>unbind each {@link CommunicationBinding} to the matching
	 * {@link CommunicationManager},
	 * <li>unregisters the device from the {@link DeviceServiceRegistry}.
	 * </ul>
	 * 
	 * @throws IOException is thrown, if a binding couldn't be unbound or if
	 *             stopping one service will throw the exception.
	 */
	public void stop() throws IOException;

	/**
	 * Starts the device. Starting the device will:
	 * <ul>
	 * <li>start its services,
	 * <li>bind each {@link CommunicationBinding} to the matching
	 * {@link CommunicationManager}, i.e. start listening on incoming messages
	 * for the specified address,
	 * <li>registers the device to the {@link DeviceServiceRegistry}.
	 * </ul>
	 * 
	 * @throws IOException is thrown, if a binding couldn't be bind to the
	 *             communication manager or if starting one service will throw
	 *             the exception.
	 */
	public void start() throws IOException;

	/**
	 * Checks if the outer device matches the searched device port types and
	 * scopes. To match the device both the port types and the scopes must be
	 * part of the device.
	 * 
	 * @param searchTypes Searched device port types to match the outer device.
	 * @param searchScopes Searched scopes to match the outer device.
	 * @return <code>true</code> - if both the given device port types and
	 *         scopes are part of the device.
	 */
	public boolean deviceMatches(QNameSet searchTypes, ProbeScopeSet searchScopes);

	/**
	 * Send hello message. Simple method to announce the device in the network.
	 * <p>
	 * <strong>Important:</strong> This method won't start the device. But
	 * starting this device will automatically send a hello message.
	 * </p>
	 */
	public void sendHello();

	/**
	 * Send Bye Message. Simple method to send a bye message to the network.
	 * <p>
	 * <strong>Important:</strong> This method won't stop the device. But
	 * stopping this device will automatically send a bye message.
	 * </p>
	 */
	public void sendBye();

	/**
	 * Set the {@link EndpointReference} of this device. The endpoint reference
	 * bears a stable globally-unique identifier of the device. This address
	 * part is typically not a physical address. <BR>
	 * If not set, the framework generates it automatically. The address part of
	 * the endpoint reference can be configured via the {@link Properties}.
	 * 
	 * @param endpoint The endpoint reference to set.
	 */
	public void setEndpointReference(EndpointReference endpoint);

	/**
	 * Sets the port types of the device. This port types should show clients in
	 * the network, which services the device may bear. Clients {@see
	 * DefaultClient} can search for the specific device port types. <BR>
	 * The port types are communicated via the hello, probe matches, resolve
	 * matches, get response and the get metadata response messages (the
	 * "wsdd:Types" elements and the be "dpws:Types" elements of host metadata). <BR>
	 * The "dpws:Device" port type is added by default.
	 * 
	 * @param qnsPortTypes Device port types to set.
	 */
	public void setPortTypes(QNameSet qnsPortTypes);

	/**
	 * Sets a list of scopes. Scopes are used within the discovery of devices. A
	 * client may search for devices with specific scopes. <BR>
	 * Scopes are part of the hello, probe matches, resolve matches messages.
	 * <p>
	 * Setting the scopes includes getting the exclusive lock (({@link Lockable}
	 * ) to the device.<BR>
	 * If the device is running, each change will initiate the sending of a
	 * hello message with an incremented metadata version. To combine multiple
	 * device data changes with sending only one hello, the exclusive lock has
	 * to be taken by {@link #exclusiveLock()}. After the last device data
	 * change the release of the exclusive lock by
	 * {@link #releaseExclusiveLock()} will send a single hello with an
	 * incremented metadata version.
	 * </p>
	 * 
	 * @param scopes List of scopes to set.
	 */
	public void setScopes(ScopeSet scopes);

	/**
	 * Set the url of the manufacturer. It used as value in the
	 * "dpws:ManufacturerUrl" element of the model metadata.
	 * <p>
	 * Setting the manufacturer url includes getting the exclusive lock ((
	 * {@link Lockable}) to the device.<BR>
	 * If the device is running, each change will initiate the sending of a
	 * hello message with an incremented metadata version. To combine multiple
	 * device data changes with sending only one hello, the exclusive lock has
	 * to be taken by {@link #exclusiveLock()}. After the last device data
	 * change the release of the exclusive lock by
	 * {@link #releaseExclusiveLock()} will send a single hello with an
	 * incremented metadata version.
	 * </p>
	 * 
	 * @param manufacturerUrl The url of the manufacturer to set.
	 */
	public void setManufacturerUrl(String manufacturerUrl);

	/**
	 * Sets the model number of the device. The model number is used as value of
	 * the "dpws:ModelNumber" element in the model metadata.
	 * <p>
	 * Setting the model number includes getting the exclusive lock ((
	 * {@link Lockable}) to the device.<BR>
	 * If the device is running, each change will initiate the sending of a
	 * hello message with an incremented metadata version. To combine multiple
	 * device data changes with sending only one hello, the exclusive lock has
	 * to be taken by {@link #exclusiveLock()}. After the last device data
	 * change the release of the exclusive lock by
	 * {@link #releaseExclusiveLock()} will send a single hello with an
	 * incremented metadata version.
	 * </p>
	 * 
	 * @param modelNumber The model number of the device to set.
	 */
	public void setModelNumber(String modelNumber);

	/**
	 * Sets the model url of the device. The model url is used as value of the
	 * "dpws:ModelUrl" element of the model metadata.
	 * <p>
	 * Setting the model url includes getting the exclusive lock ((
	 * {@link Lockable}) to the device.<BR>
	 * If the device is running, each change will initiate the sending of a
	 * hello message with an incremented metadata version. To combine multiple
	 * device data changes with sending only one hello, the exclusive lock has
	 * to be taken by {@link #exclusiveLock()}. After the last device data
	 * change the release of the exclusive lock by
	 * {@link #releaseExclusiveLock()} will send a single hello with an
	 * incremented metadata version.
	 * </p>
	 * 
	 * @param modelUrl The model url of the device to set.
	 */
	public void setModelUrl(String modelUrl);

	/**
	 * Sets the presentation url of the device. It is used as value of the
	 * "dpws:PresentationUrl" element of the model metadata.
	 * <p>
	 * Setting the presentation url includes getting the exclusive lock ((
	 * {@link Lockable}) to the device.<BR>
	 * If the device is running, each change will initiate the sending of a
	 * hello message with an incremented metadata version. To combine multiple
	 * device data changes with sending only one hello, the exclusive lock has
	 * to be taken by {@link #exclusiveLock()}. After the last device data
	 * change the release of the exclusive lock by
	 * {@link #releaseExclusiveLock()} will send a single hello with an
	 * incremented metadata version.
	 * </p>
	 * 
	 * @param presentationUrl The presentation url to set.
	 */
	public void setPresentationUrl(String presentationUrl);

	/**
	 * Adds a friendly name to the device. It is used as value of the
	 * "dpws:FriendlyName" element of the device metadata. The friendly name is
	 * language specific.
	 * <p>
	 * Adding a friendly name includes getting the exclusive lock ((
	 * {@link Lockable}) to the device.<BR>
	 * If the device is running, each change will initiate the sending of a
	 * hello message with an incremented metadata version. To combine multiple
	 * device data changes with sending only one hello, the exclusive lock has
	 * to be taken by {@link #exclusiveLock()}. After the last device data
	 * change the release of the exclusive lock by
	 * {@link #releaseExclusiveLock()} will send a single hello with an
	 * incremented metadata version.
	 * </p>
	 * 
	 * @param lang Language attribute, i. e. "en-US or "de-DE":
	 *            <ul>
	 *            <li>The syntax of the language tags is described in RFC 5646.
	 *            <li>All language subtags are registered to the IANA Language
	 *            Subtag Registry.
	 *            <li>All region subtags are specified in
	 *            "ISO 3166: Codes for Country Names".
	 *            </ul>
	 * @param friendlyName The friendly name of the device in the specified
	 *            language to be set.
	 */
	public void addFriendlyName(String lang, String friendlyName);

	/**
	 * Sets the firmware version to the device. It is used as value of the
	 * "dpws:FirmwareVersion" element of the device metadata.
	 * <p>
	 * Setting the firmware version includes getting the exclusive lock ((
	 * {@link Lockable}) to the device.<BR>
	 * If the device is running, each change will initiate the sending of a
	 * hello message with an incremented metadata version. To combine multiple
	 * device data changes with sending only one hello, the exclusive lock has
	 * to be taken by {@link #exclusiveLock()}. After the last device data
	 * change the release of the exclusive lock by
	 * {@link #releaseExclusiveLock()} will send a single hello with an
	 * incremented metadata version.
	 * </p>
	 * 
	 * @param firmware The firmware version of the device to set.
	 */
	public void setFirmwareVersion(String firmware);

	/**
	 * Sets the serial number of the device. It is used as value of the
	 * "wsdp:SerialNumber" element of the device metadata.
	 * <p>
	 * Set the serial number version includes getting the exclusive lock ((
	 * {@link Lockable}) to the device.<BR>
	 * If the device is running, each change will initiate the sending of a
	 * hello message with an incremented metadata version. To combine multiple
	 * device data changes with sending only one hello, the exclusive lock has
	 * to be taken by {@link #exclusiveLock()}. After the last device data
	 * change the release of the exclusive lock by
	 * {@link #releaseExclusiveLock()} will send a single hello with an
	 * incremented metadata version.
	 * </p>
	 * 
	 * @param serialNumber The serial number of the device to set.
	 */
	public void setSerialNumber(String serialNumber);

	/**
	 * Adds service to device.
	 * <p>
	 * NOTICE: If the device is already running, you must start the service with
	 * the start() method, or use the addService(LocalService, boolean) method.
	 * </p>
	 * <p>
	 * Adding a service to the device includes getting the exclusive lock ((
	 * {@link Lockable}) to the device.<BR>
	 * If the device is running, each change will initiate the sending of a
	 * hello message with an incremented metadata version. To combine multiple
	 * device data changes with sending only one hello, the exclusive lock has
	 * to be taken by {@link #exclusiveLock()}. After the last device data
	 * change the release of the exclusive lock by
	 * {@link #releaseExclusiveLock()} will send a single hello with an
	 * incremented metadata version.
	 * </p>
	 * 
	 * @see org.ws4d.java.service.LocalDevice#addService(org.ws4d.java.service.LocalService,
	 *      boolean)
	 * @param service service to add to this device.
	 */
	public void addService(LocalService service);

	/**
	 * Adds a service to the device.
	 * <p>
	 * Adding a service to the device includes getting the exclusive lock ((
	 * {@link Lockable}) to the device.<BR>
	 * If the device is running, each change will initiate the sending of a
	 * hello message with an incremented metadata version. To combine multiple
	 * device data changes with sending only one hello, the exclusive lock has
	 * to be taken by {@link #exclusiveLock()}. After the last device data
	 * change the release of the exclusive lock by
	 * {@link #releaseExclusiveLock()} will send a single hello with an
	 * incremented metadata version.
	 * </p>
	 * 
	 * @param service service to add to this device.
	 * @param startIfRunning <code>true</code> the service is started if the
	 *            device is already running, <code>false</code> the service is
	 *            not started, we just add it.
	 */
	public void addService(LocalService service, boolean startIfRunning) throws IOException;

	/**
	 * Removes service from device. The service will be removed from the device,
	 * but won't be stopped.
	 * <p>
	 * Removing a service from the device includes getting the exclusive lock ((
	 * {@link Lockable}) to the device.<BR>
	 * If the device is running, each change will initiate the sending of a
	 * hello message with an incremented metadata version. To combine multiple
	 * device data changes with sending only one hello, the exclusive lock has
	 * to be taken by {@link #exclusiveLock()}. After the last device data
	 * change the release of the exclusive lock by
	 * {@link #releaseExclusiveLock()} will send a single hello with an
	 * incremented metadata version.
	 * </p>
	 * 
	 * @param service The service to remove from this device.
	 */
	public void removeService(LocalService service);

	/**
	 * Removes a service from the device. If stopIfRunning is
	 * <code>true<code> the service to remove is stopped if running, else not.
	 * <p>
	 * Removing a service from the device includes getting the exclusive lock ((
	 * {@link Lockable}) to the device.<BR>
	 * If the device is running, each change will initiate the sending of a
	 * hello message with an incremented metadata version. To combine multiple
	 * device data changes with sending only one hello, the exclusive lock has
	 * to be taken by {@link #exclusiveLock()}. After the last device data
	 * change the release of the exclusive lock by
	 * {@link #releaseExclusiveLock()} will send a single hello with an
	 * incremented metadata version.
	 * </p>
	 * 
	 * @param service The service to remove from the device.
	 * @param stopIfRunning <code>true</code> the service is stopped if the
	 *            service is running, <code>false</code> just remove.
	 */
	public void removeService(LocalService service, boolean stopIfRunning) throws IOException;

	/**
	 * Sets the device metadata of the device. It contains different device
	 * metadata and is transmitted in the "dpws:ThisDevice" metadata.
	 * <p>
	 * Setting the device metadata includes getting the exclusive lock ((
	 * {@link Lockable}) to the device.<BR>
	 * If the device is running, each change will initiate the sending of a
	 * hello message with an incremented metadata version. To combine multiple
	 * device data changes with sending only one hello, the exclusive lock has
	 * to be taken by {@link #exclusiveLock()}. After the last device data
	 * change the release of the exclusive lock by
	 * {@link #releaseExclusiveLock()} will send a single hello with an
	 * incremented metadata version.
	 * </p>
	 * 
	 * @param deviceMetadata
	 */
	public void setDeviceMetadata(ThisDeviceMData deviceMetadata);

	/**
	 * Sets the metadata version of the device. The metadata version is part of
	 * some discovery messages of the device. If it is incremented, clients
	 * receiving this new metadata version have to update the informations of
	 * the device.
	 * <p>
	 * Setting the metadata version includes getting the exclusive lock ((
	 * {@link Lockable}) to the device.<BR>
	 * If the device is running, each change will initiate the sending of a
	 * hello message with an incremented metadata version. To combine multiple
	 * device data changes with sending only one hello, the exclusive lock has
	 * to be taken by {@link #exclusiveLock()}. After the last device data
	 * change the release of the exclusive lock by
	 * {@link #releaseExclusiveLock()} will send a single hello with the new
	 * metadata version.
	 * </p>
	 * 
	 * @param metadataVersion The metadata version to set is from type unsigned
	 *            int.
	 */
	public void setMetadataVersion(long metadataVersion);

	/**
	 * Sets the model metadata of the device. It contains different model meta
	 * data and is transmitted in the "dpws:ThisModel" metadata.
	 * <p>
	 * Setting the model metadata version includes getting the exclusive lock ((
	 * {@link Lockable}) to the device.<BR>
	 * If the device is running, each change will initiate the sending of a
	 * hello message with an incremented metadata version. To combine multiple
	 * device data changes with sending only one hello, the exclusive lock has
	 * to be taken by {@link #exclusiveLock()}. After the last device data
	 * change the release of the exclusive lock by
	 * {@link #releaseExclusiveLock()} will send a single hello with an
	 * incremented metadata version.
	 * </p>
	 * 
	 * @param modelMetadata The model metadata of the device to set.
	 */
	public void setModelMetadata(ThisModelMData modelMetadata);

	/**
	 * Get iterator over all services. A service is of type {@link Service}.
	 * 
	 * @return Iterator over all services of type {@link Service}.
	 */
	public Iterator getServices();
	
	/**
	 * Add user metadata to a device 
	 * 
	 * @param metaData HashMap which contains the metadata which the user wants to add
	 */
	public void writeCustomizeMData(HashMap metaData);
	
	// Returns the user Group of the device.
	
	public HTTPGroup getGroup();

}
