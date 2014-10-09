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

import org.ws4d.java.service.reference.DeviceReference;
import org.ws4d.java.service.reference.ServiceReference;
import org.ws4d.java.structures.DataStructure;
import org.ws4d.java.structures.Iterator;
import org.ws4d.java.types.EndpointReference;
import org.ws4d.java.types.LocalizedString;
import org.ws4d.java.types.QName;
import org.ws4d.java.types.QNameSet;
import org.ws4d.java.types.URI;

/**
 * DPWS Device. "DPWS Hosting Service" This is the most common interface of all
 * devices.
 * <p>
 * A device in DPWS is a web service with specific functions. It can be
 * discovered via probing from clients and it supports resolving of its
 * endpoint. A device bears services.
 * </p>
 * A device is encapsulated in a {@link DeviceReference}.
 */
public interface Device {

	/**
	 * Returns the endpoint reference of this device. This endpoint reference is
	 * a unique identifier for the device.
	 * 
	 * @return endpoint reference as unique identifier.
	 */
	public EndpointReference getEndpointReference();

	/**
	 * Returns metadata version for this device. The metadata version is
	 * transmitted via discovery messages.
	 * 
	 * @return metadata version as unsigned integer.
	 */
	public long getMetadataVersion();

	/**
	 * Returns a iterator over the transport addresses of this device. The type
	 * of the addresses is {@link URI}.
	 * 
	 * @return Iterator over transport addresses of type {@link URI}.
	 */
	public Iterator getXAddressInfos();

	/**
	 * Gets the iterator over device port types of the device. The port types
	 * are of type {@link QName}.
	 * 
	 * @return Iterator over device port types of type {@link QName}.
	 */
	public Iterator getPortTypes();

	/**
	 * Gets manufacturer by language. Language consts are specified in
	 * {@link LocalizedString}.
	 * 
	 * @param lang The language of the manufacturer's name to be obtained. The
	 *            language consts are specified in {@link LocalizedString}.
	 * @return Manufacturer's name in specified language of type
	 *         {@link LocalizedString}.
	 */
	public String getManufacturer(String lang);

	/**
	 * Gets all manufacturers as iterator over manufacterer names. A single
	 * manufacturer name is of type {@link LocalizedString}.
	 * 
	 * @return Iterator over manufacturer of type {@link LocalizedString}.
	 */
	public Iterator getManufacturers();

	/**
	 * Gets the url of the manufacturer.
	 * 
	 * @return Url of manufacturer
	 */
	public String getManufacturerUrl();

	/**
	 * Gets model name by language. Language consts are specified in
	 * {@link LocalizedString}.
	 * 
	 * @param lang Language of the model name to get. The language consts are
	 *            specified in {@link LocalizedString}.
	 * @return Model name in specified language of type {@link LocalizedString}.
	 */
	public String getModelName(String lang);

	/**
	 * Gets all model names as iterator over model names. A single model name is
	 * of type {@link LocalizedString}.
	 * 
	 * @return Iterator over model names of type {@link LocalizedString}.
	 */
	public Iterator getModelNames();

	/**
	 * Gets model number of device.
	 * 
	 * @return Model number.
	 */
	public String getModelNumber();

	/**
	 * Gets model url of device.
	 * 
	 * @return Model url.
	 */
	public String getModelUrl();

	/**
	 * Gets presentation url of device.
	 * 
	 * @return Presentation url.
	 */
	public String getPresentationUrl();

	/**
	 * Gets friendly name of device.
	 * 
	 * @param lang language of friendly name
	 * @return Friendly name of type {@link LocalizedString} in specified
	 *         language
	 */
	public String getFriendlyName(String lang);

	/**
	 * Gets iterator over all friendly names of device. A single name is of type
	 * {@link LocalizedString}.
	 * 
	 * @return Iterator over friendly names of type {@link LocalizedString}.
	 */
	public Iterator getFriendlyNames();

	/**
	 * Gets firmware version.
	 * 
	 * @return firmware version
	 */
	public String getFirmwareVersion();

	/**
	 * Gets serial number.
	 * 
	 * @return serial number
	 */
	public String getSerialNumber();

	/**
	 * Gets iterator over the service references of all services. A service
	 * reference is of type {@link ServiceReference}.
	 * 
	 * @return Iterator over the (@link ServiceReference) of each service of the
	 *         device.
	 */
	public Iterator getServiceReferences();

	/**
	 * Gets iterator over the service reference of the services, which
	 * implements all port types specified. A service reference is of type
	 * {@link ServiceReference}.
	 * 
	 * @param servicePortTypes The service port types the services must
	 *            implement, to be returned by its reference.
	 * @return Iterator over the (@link ServiceReferences) to the services of
	 *         the device, which implements the specified service port types.
	 */
	public Iterator getServiceReferences(QNameSet servicePortTypes);

	/**
	 * Adds service references associated to this device to the data structure
	 * <code>to</code>, if they match the service port types given within
	 * <code>serviceTypes</code>.
	 * 
	 * @param to collection to add matching service references to
	 * @param serviceTypes service port types to look for
	 */
	public void addServiceReferences(DataStructure to, QNameSet serviceTypes);

	/**
	 * Gets service reference by service id of referenced service. Service id is
	 * compared as case-sensitive string.
	 * 
	 * @param serviceId
	 * @return service reference, or <code>null</code> if no service matches the
	 *         service id.
	 */
	public ServiceReference getServiceReference(URI serviceId);

	/**
	 * Get service reference by endpoint reference of referenced service.
	 * 
	 * @param serviceEpr
	 * @return service reference, or <code>null</code>, if no service matches
	 *         the service id.
	 */
	public ServiceReference getServiceReference(EndpointReference serviceEpr);

	/**
	 * Is device remote (proxy) or local?
	 * 
	 * @return whether this is a remote device (proxy) or not
	 */
	public boolean isRemote();

	/**
	 * Gets iterator over all scopes of the device. A scope is of type
	 * {@link URI}.
	 * 
	 * @return Iterator over all scopes of type {@link URI}.
	 */
	public Iterator getScopes();

	/**
	 * Gets the {@link DeviceReference} of this device.
	 * 
	 * @return The {@link DeviceReference} of this device.
	 */
	public DeviceReference getDeviceReference();

	/**
	 * This certificate is used to validate signatures. Used in the security
	 * package.
	 * 
	 * @return the java.security.cert.Certificate;
	 */
	public Object getCertificate();

	/**
	 * Sets the certificate of this device. Used only in the security package.
	 * 
	 * @param cert the java.security.cert.Certificate
	 */
	public void setCertificate(Object cert);

	/**
	 * return weather or not this services uses the security techniques.
	 * 
	 * @return
	 */
	public boolean isSecure();

	/**
	 * return the default namespace.
	 * 
	 * @return default namespace
	 */
	public String getDefaultNamespace();

	/**
	 * Makes the device secure and sets certificate and private key. The aliases
	 * are used to get the certificate and private key from the security
	 * manager.
	 * 
	 * @param certAlias Alias to get the certificate.
	 * @param privKeyAlias Alias to get the private key.
	 * @param privKeyPass Password to get the private key.
	 * @return <code>false</code>, if certificate or private key can't be
	 *         retrieved by specified aliases.
	 * @throws RuntimeException thrown, if security is not supported by the
	 *             framework.
	 */
	public boolean setSecureDevice(String certAlias, String privKeyAlias, String privKeyPass) throws RuntimeException;

	/**
	 * @return
	 */
	public Object getPrivateKey();

	/**
	 * @param privKey must be the java.security.PrivateKey of the sender device/
	 *            service
	 */
	public void setPrivateKey(Object privKey);

	public boolean isValid();

	public void invalidate();

	/**
	 * The method return the metadata that a user has added as String
	 * 
	 * @return String which contains the users metadata
	 */
	public String readCustomizeMData();

	/**
	 * Tagged if a Device has metadata which are added by a user
	 * 
	 * @return boolean 	
	 */
	public boolean hasCustomizeMData();

}
