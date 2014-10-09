/*******************************************************************************
 * Copyright (c) 2009 MATERNA Information & Communications. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html. For further
 * project-related information visit http://www.ws4d.org. The most recent
 * version of the JMEDS framework can be obtained from
 * http://sourceforge.net/projects/ws4d-javame.
 ******************************************************************************/

package org.ws4d.java.service.reference;

import org.ws4d.java.client.DefaultClient;
import org.ws4d.java.communication.TimeoutException;
import org.ws4d.java.configuration.DispatchingProperties;
import org.ws4d.java.dispatch.DeviceServiceRegistry;
import org.ws4d.java.service.Device;
import org.ws4d.java.structures.Iterator;
import org.ws4d.java.types.EndpointReference;
import org.ws4d.java.types.QName;
import org.ws4d.java.types.URI;
import org.ws4d.java.types.XAddressInfo;

/**
 * Reference to a device. The referenced device within can change. Get present
 * device by method getDevice(). Device references are created within the
 * discovery process and returned by the {@link DefaultClient}. All device
 * references are registered in the {@link DeviceServiceRegistry}. There should
 * only be one reference to each device.
 * <p>
 * Receiving a device reference typically means the registration as device
 * listener ({@link DeviceListener}) to the device, which informs about device
 * changes by using callbacks. The {@link DefaultClient} is such a device
 * listener. Device listening can be added manually by
 * {@link #addListener(DeviceListener)} and can be removed by
 * {@link #removeListener(DeviceListener)}.
 * </p>
 */
public interface DeviceReference extends Reference {

	/** STATE: Device reference created, but no information about device state */
	public static final int	STATE_UNKNOWN	= 0;

	/**
	 * STATE: Device has stopped is the latest known information about the
	 * referenced device
	 */
	public static final int	STATE_STOPPED	= 1;

	/**
	 * STATE: Device is running is the latest known information about the
	 * referenced device
	 */
	public static final int	STATE_RUNNING	= 2;

	/**
	 * STATE: Device is running is the latest known information about the
	 * referenced device, and device (proxy/local) was created
	 */
	public static final int	STATE_BUILD_UP	= 3;

	/**
	 * Get device. If a proxy device is not built, this will build it. Services
	 * may not be completely built.
	 * <p>
	 * A Proxy to a remote device is built up by sending resolve and get
	 * messages.
	 * </p>
	 * 
	 * @return device The device to this reference. The device can be a local
	 *         device or a proxy to a remote device.
	 * @throws TimeoutException This exception is thrown, if the remote device
	 *             doesn't answer to the messages, which tries to receive the
	 *             necessary data. The possible messages to be sent are resolve,
	 *             and get messages. The timeout value can be configured in the
	 *             {@link DispatchingProperties} via the methods
	 *             {@link DispatchingProperties#setMatchWaitTime(long)} and
	 *             {@link DispatchingProperties#setResponseWaitTime(int)}.
	 */
	public Device getDevice() throws TimeoutException;

	/**
	 * Fetches all discovery data of device reference by sending a directed
	 * probe to the remote device. The current thread will block until a
	 * response is received from the device or an exception occurs during
	 * communication. This method will not do anything, if the associated device
	 * to this reference is local sited.
	 * 
	 * @throws TimeoutException
	 */
	public void fetchCompleteDiscoveryDataSync() throws TimeoutException;

	/**
	 * Fetches all discovery data of device reference by sending a directed
	 * probe to the remote device. This method will not do anything, if the
	 * associated device to this reference is local sited.
	 * 
	 * @throws TimeoutException
	 */
	public void fetchCompleteDiscoveryDataAsync() throws TimeoutException;

	/**
	 * Gets endpoint reference of device.
	 * 
	 * @return the device's endpoint reference
	 */
	public EndpointReference getEndpointReference();

	/**
	 * Gets port types implemented by device. If discovery should be done, no
	 * port types are known and device is remote located, a resolve message will
	 * be sent to remote device to discover them.
	 * 
	 * @param doDiscovery if <code>true</code> and no port types are known and
	 *            device is remote located, a resolve message will be sent to
	 *            remote device to discover them.
	 * @return the port types of the device, object type is {@link QName}.
	 * @throws TimeoutException
	 */
	public Iterator getDevicePortTypes(boolean doDiscovery) throws TimeoutException;

	/**
	 * Gets port types implemented by device. If discovery should be done, no
	 * port types are known and device is remote located, a resolve message will
	 * be sent to remote device to discover them.
	 * 
	 * @param doDiscovery if <code>true</code> and no port types are known and
	 *            device is remote located, a resolve message will be sent to
	 *            remote device to discover them.
	 * @return Shallow copy of the port types of the device, object type is
	 *         {@link QName}.
	 * @throws TimeoutException
	 */
	public QName[] getDevicePortTypesAsArray(boolean doDiscovery) throws TimeoutException;

	/**
	 * Gets scopes the device resides in. If discovery should be done, no port
	 * types are known and device is remote located, a resolve message will be
	 * sent to remote device to discover them.
	 * 
	 * @param doDiscovery if <code>true</code> and no port types are known and
	 *            device is remote located, a resolve message will be sent to
	 *            remote device to discover them.
	 * @return the scopes of the device, object type is {@link URI}.
	 * @throws TimeoutException
	 */
	public Iterator getScopes(boolean doDiscovery) throws TimeoutException;

	/**
	 * Gets scopes the device resides in. If discovery should be done, no port
	 * types are known and device is remote located, a resolve message will be
	 * sent to remote device to discover them.
	 * 
	 * @param doDiscovery if <code>true</code> and no port types are known and
	 *            device is remote located, a resolve message will be sent to
	 *            remote device to discover them.
	 * @return Shallow copy of the scopes of the device, object type is
	 *         {@link URI}.
	 * @throws TimeoutException
	 */
	public URI[] getScopesAsArray(boolean doDiscovery) throws TimeoutException;

	/**
	 * Returns the metadata version of the device. If discovery should be done,
	 * no port types are known and device is remote located, a resolve message
	 * will be sent to remote device to discover them.
	 * 
	 * @param doDiscovery if <code>true</code> and no port types are known and
	 *            device is remote located, a resolve message will be sent to
	 *            remote device to discover them.
	 * @return the metadata version of the device
	 * @throws TimeoutException
	 */
	public long getMetadataVersion(boolean doDiscovery) throws TimeoutException;

	/**
	 * Get the transport addresses of the device. If discovery should be done,
	 * no port types are known and device is remote located, a resolve message
	 * will be sent to remote device to discover them.
	 * 
	 * @param doDiscovery if <code>true</code> and no port types are known and
	 *            device is remote located, a resolve message will be sent to
	 *            remote device to discover them.
	 * @return the transport addresses of the device, object type is {@link URI}
	 *         .
	 * @throws TimeoutException
	 */
	public Iterator getXAddressInfos(boolean doDiscovery) throws TimeoutException;
	/**
	 * 
	 * @return the customize metadata as string 
	 */
	public String getCustomMData();

	/**
	 * Unregisters listener for this device reference. This method should be
	 * called, if holder of reference is no more interested in this reference.
	 * 
	 * @param listener listener to remove.
	 */
	public void removeListener(DeviceListener listener);

	/**
	 * Register listener (callback) for device reference changes. Listeners get
	 * get information about device changes.
	 * 
	 * @param listener listener (callback) to register.
	 */
	public void addListener(DeviceListener listener);

	/**
	 * Case remote: Did a proxy device object exist for the current version of
	 * the remote device? <BR>
	 * Case local: Always <code>true</code>.
	 * 
	 * @return Case remote: Did a proxy device object exist for the current
	 *         version of the remote device?<BR>
	 *         Case local: Always <code>true</code>.
	 */
	public boolean isDeviceObjectExisting();

	/**
	 * Case remote: Did we receive the answer to a directed probe for the
	 * current version of the device? Case local: Always true.
	 * 
	 * @return Case remote: Did we receive the answer to a directed probe for
	 *         the current version of the remote device?<BR>
	 *         Case local: Always <code>true</code>.
	 */
	public boolean isCompleteDiscovered();

	/**
	 * Case remote: Did we receive a resolve message for the current version of
	 * the device. Case local: Always true.
	 * 
	 * @return Case remote: Did we receive a resolve message for the current
	 *         version of the device?<BR>
	 *         Case local: Always <code>true</code>.
	 */
	public boolean isDiscovered();

	/**
	 * Gets state of device / device reference. The states specified for the
	 * device reference are {@link #STATE_UNKNOWN}, {@link #STATE_RUNNING},
	 * {@link #STATE_BUILD_UP} and {@link #STATE_STOPPED}.
	 * 
	 * @return the current state of the device
	 * @see #STATE_UNKNOWN
	 * @see #STATE_RUNNING
	 * @see #STATE_BUILD_UP
	 * @see #STATE_STOPPED
	 */
	public int getState();

	public boolean isSecureDevice();

	void setSecureDevice(boolean sec);

	public boolean isAutoUpdateDevice();

	/**
	 * When set to <code>true</code>, the device reference will attempt to
	 * rebuild the device proxy automatically each time a a change in the
	 * device's metadata version is detected.
	 * 
	 * @param autoUpdateDevice
	 */
	public void setAutoUpdateDevice(boolean autoUpdateDevice);

	public XAddressInfo getPreferredXAddressInfo();

}
