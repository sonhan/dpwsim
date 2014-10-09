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

import org.ws4d.java.communication.TimeoutException;
import org.ws4d.java.constants.WSDConstants;
import org.ws4d.java.dispatch.DeviceServiceRegistry;
import org.ws4d.java.service.reference.DeviceListener;
import org.ws4d.java.service.reference.DeviceReference;
import org.ws4d.java.service.reference.ServiceReference;

/**
 * Class holds dispatching properties.
 */
public class DispatchingProperties implements PropertiesHandler {

	// -------------------- CONSTS -----------------------------------

	/**
	 * Caching time in milliseconds for references without reference holder. <BR>
	 * Type: int <BR>
	 * Default: 20000
	 */
	public static final String	PROP_REFERENCE_CACHING_TIME			= "TargetReferenceCachingTime";

	/**
	 * Time to wait until a time exception is thrown, after a request was sent
	 * and no answer was received. <BR>
	 * Type: int <BR>
	 * Default: 10000
	 */
	public static final String	PROP_RESPONSE_WAIT_TIME				= "ResponseWaitTime";

	/**
	 * Time to wait until a time exception is thrown, after a request was sent
	 * and no answer was received. <BR>
	 * Type: int <BR>
	 * Default: 10000
	 */
	public static final String	PROP_MATCH_WAIT_TIME				= "MatchWaitTime";

	/**
	 * Amount of time in millis dispatcher will wait to send probe matches and
	 * hello messages. <BR>
	 * Type: long <BR>
	 * Default: 2500
	 */
	public static final String	PROP_APPLICATION_MAX_DELAY			= "ApplicationMaxDelay";

	/**
	 * Size of message id buffer. The buffer is used to ignore multiplicated udp
	 * messages. <BR>
	 * Type: int <BR>
	 * Default: 50
	 */
	public static final String	PROP_MESSAGE_ID_BUFFER_SIZE			= "MessageIdBufferSize";

	/**
	 * Size of the cache for remote service references. <BR>
	 * Type: int <BR>
	 * Default: 50
	 */
	public static final String	PROP_SERVICE_REFERENCE_CACHE_SIZE	= "ServiceReferenceCacheSize";

	/**
	 * Property to build up a device reference for each recognized remote
	 * device. <BR>
	 * Type: boolean <BR>
	 * Default: false
	 */
	public static final String	PROP_DEVICE_REFERENCE_AUTO_BUILD	= "DeviceReferenceAutoBuild";

	// --------------------------------------------------------------

	/** caching time for target references without reference holder */
	private int					targetReferenceCachingTime			= 20000;

	/**
	 * milliseconds until a response message will be handled, after that, it
	 * will be discarded (WSD Matches are excluded)
	 */
	private int					responseWaitTime					= 10000;

	/**
	 * Millis until a matching message (probe matches or resolve matches) will
	 * be handled.
	 */
	// TODO: Move into DPWS properties
	private long				matchWaitTime						= WSDConstants.WSD_MATCH_TIMEOUT;

	/**
	 * Size of message id buffer. This buffer is used to ignore multiplicated
	 * udp messages.
	 */
	private int					msgIdBufferSize						= 50;

	/**
	 * Size of the cache for remote service references.
	 */
	private int					servRefCacheSize					= 50;

	/**
	 * Property to build up a device reference for each recognized remote device
	 */
	private boolean				deviceReferenceAutoBuild			= false;

	// -----------------------------------------------------

	// private static DispatchingProperties handler = null;

	DispatchingProperties() {
		super();
		// if (handler != null) {
		// throw new
		// RuntimeException("ServicePropertiesHandler: class already instantiated!");
		// }
		// handler = this;
	}

	/**
	 * Gets instance of dispatching properties.
	 * 
	 * @return the singleton instance of the dispatching properties
	 */
	public static DispatchingProperties getInstance() {
		// if (handler == null) {
		// handler = new DispatchingProperties();
		// }
		// return handler;
		return (DispatchingProperties) Properties.forClassName(Properties.DISPATCHING_PROPERTIES_HANDLER_CLASS);
	}

	// -------------------------------------------------------

	/*
	 * (non-Javadoc)
	 * @see
	 * org.ws4d.java.configuration.PropertiesHandler#setProperties(org.ws4d.
	 * java.configuration.PropertyHeader, org.ws4d.java.configuration.Property)
	 */
	public void setProperties(PropertyHeader header, Property property) {

		if (Properties.HEADER_SUBSECTION_DISPATCHING.equals(header)) {

			if (PROP_REFERENCE_CACHING_TIME.equals(property.key)) {
				targetReferenceCachingTime = Integer.parseInt(property.value.trim());
			} else if (PROP_RESPONSE_WAIT_TIME.equals(property.key)) {
				responseWaitTime = Integer.parseInt(property.value.trim());
			} else if (PROP_MATCH_WAIT_TIME.equals(property.key)) {
				matchWaitTime = Integer.parseInt(property.value.trim());
			} else if (PROP_MESSAGE_ID_BUFFER_SIZE.equals(property.key)) {
				setMessageIdBufferSize(Integer.parseInt(property.value.trim()));
			} else if (PROP_SERVICE_REFERENCE_CACHE_SIZE.equals(property.key)) {
				setServiceReferenceCacheSize(Integer.parseInt(property.value.trim()));
			} else if (PROP_DEVICE_REFERENCE_AUTO_BUILD.equals(property.key)) {
				if (property.value.equals(TRUE)) {
					setDeviceReferenceAutoBuild(true);
				} else {
					setDeviceReferenceAutoBuild(false);
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.configuration.PropertiesHandler#finishedSection(int)
	 */
	public void finishedSection(int depth) {
		// TODO Auto-generated method stub
		if (depth == 2) {
			// XXX DeviceProperties initialized
		}

	}

	/**
	 * Gets target reference caching time, i. e. the time a
	 * {@link DeviceReference} is cached in the {@link DeviceServiceRegistry}
	 * without having a registered {@link DeviceListener}.
	 * 
	 * @return Time in millis a {@link DeviceReference} is cached without having
	 *         a registered {@link DeviceListener}. Afterwards the reference is
	 *         removed from the {@link DeviceServiceRegistry}.
	 */
	public int getReferenceCachingTime() {
		return targetReferenceCachingTime;
	}

	/**
	 * Sets target reference caching time, i. e. the time a
	 * {@link DeviceReference} is cached in the {@link DeviceServiceRegistry}
	 * without having a registered {@link DeviceListener}.
	 * 
	 * @param targetReferenceCachingTime Time in millis a
	 *            {@link DeviceReference} is cached without having a registered
	 *            {@link DeviceListener}. Afterwards the reference is removed
	 *            from {@link DeviceServiceRegistry}.
	 */
	public void setReferenceCachingTime(int targetReferenceCachingTime) {
		this.targetReferenceCachingTime = targetReferenceCachingTime;
	}

	/**
	 * Gets time until request will timeout, if no response is received.
	 * 
	 * @return time for request until response must have been received, or will
	 *         timeout.
	 */
	public int getResponseWaitTime() {
		return responseWaitTime;
	}

	/**
	 * Sets time until request will timeout, if no response is received.
	 * 
	 * @param responseWaitTime time for request until response must have been
	 *            received, or will timeout
	 */
	public void setResponseWaitTime(int responseWaitTime) {
		this.responseWaitTime = responseWaitTime;
	}

	/**
	 * Gets size of message id buffer. The buffer is used to ignore multiple
	 * received udp messages.
	 * 
	 * @return the size for message ID buffers
	 */
	public int getMessageIdBufferSize() {
		return msgIdBufferSize;
	}

	/**
	 * Sets size of message id buffer. The buffer is used to ignore multiple
	 * received udp messages.
	 * 
	 * @param msgIdBufferSize
	 */
	public void setMessageIdBufferSize(int msgIdBufferSize) {
		this.msgIdBufferSize = msgIdBufferSize;
	}

	/**
	 * Gets cache size of {@link ServiceReference}s of remote services not
	 * linked to a parent device. The longest not used service reference will be
	 * disposed, if cache size is reached and an new service reference must be
	 * inserted into the cache.
	 * 
	 * @return size of cache
	 */
	public int getServiceReferenceCacheSize() {
		return servRefCacheSize;
	}

	/**
	 * Sets cache size of {@link ServiceReference}s of remote services not
	 * linked to a parent device. The longest not used service reference will be
	 * disposed, if cache size is reached and an new service reference must be
	 * inserted into the cache.
	 * 
	 * @param servRefCacheSize size of cache
	 */
	public void setServiceReferenceCacheSize(int servRefCacheSize) {
		this.servRefCacheSize = servRefCacheSize;
	}

	/**
	 * Gets time in millis for which the framework waits for a resolve matches
	 * or a probe matches message, before it throws a {@link TimeoutException}.
	 * 
	 * @return time in millis.
	 */
	public long getMatchWaitTime() {
		return matchWaitTime;
	}

	/**
	 * Sets time in millis for which the framework waits for a resolve matches
	 * or a probe matches message, before it throws a {@link TimeoutException}.
	 * 
	 * @param matchWaitTime time in millis.
	 */
	public void setMatchWaitTime(long matchWaitTime) {
		this.matchWaitTime = matchWaitTime;
	}

	/**
	 * Is a device reference created for each recognized remote device.
	 * 
	 * @return <code>true</code>, if a device reference is created for each
	 *         recognized remote device, else <code>false</code>.
	 */
	public boolean isDeviceReferenceAutoBuild() {
		return this.deviceReferenceAutoBuild;
	}

	/**
	 * Sets if a device reference should be created for each recognized remote
	 * device.
	 * 
	 * @param deviceReferenceAutoBuild <code>true</code>, if a device reference
	 *            should be created for each recognized remote device, else
	 *            <code>false</code>.
	 */
	public void setDeviceReferenceAutoBuild(boolean deviceReferenceAutoBuild) {
		this.deviceReferenceAutoBuild = deviceReferenceAutoBuild;
	}

}
