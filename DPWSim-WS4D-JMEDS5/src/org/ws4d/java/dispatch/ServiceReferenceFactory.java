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

import org.ws4d.java.communication.ProtocolData;
import org.ws4d.java.communication.ResponseCallback;
import org.ws4d.java.configuration.FrameworkProperties;
import org.ws4d.java.service.reference.ServiceReference;
import org.ws4d.java.types.EndpointReference;
import org.ws4d.java.types.HostedMData;
import org.ws4d.java.types.XAddressInfo;
import org.ws4d.java.util.Log;

public class ServiceReferenceFactory {

	private static final ServiceReferenceFactory	INSTANCE;

	static {
		final String factoryClassName = FrameworkProperties.getInstance().getServiceReferenceFactoryClass();

		ServiceReferenceFactory factory = null;
		if (factoryClassName == null) {
			factory = new ServiceReferenceFactory();
		} else {
			try {
				Class classType = Class.forName(factoryClassName);
				factory = (ServiceReferenceFactory) classType.newInstance();
				Log.info("Using " + factoryClassName);
			} catch (ClassNotFoundException e) {
				Log.error("ServiceReferenceFactory: Configured ServiceReferenceFactory class [" + factoryClassName + "] not found, falling back to default implementation");
				factory = new ServiceReferenceFactory();
			} catch (Exception e) {
				Log.error("ServiceReferenceFactory: Unable to create instance of configured ServiceReferenceFactory class [" + factoryClassName + "], falling back to default implementation");
				Log.printStackTrace(e);
				factory = new ServiceReferenceFactory();
			}
		}
		INSTANCE = factory;
	}

	public static ServiceReferenceFactory getInstance() {
		return INSTANCE;
	}

	public ServiceReferenceInternal newServiceReference(HostedMData hosted, String comManId, ProtocolData protocolData) {
		return new DefaultServiceReference(hosted, comManId, protocolData);
	}

	public ServiceReferenceInternal newServiceReference(EndpointReference epr, String comManId) {
		return new DefaultServiceReference(epr, comManId);
	}

	public ResponseCallback newResponseCallbackForServiceReference(ServiceReference servRefHandler, XAddressInfo targetXAdressInfo) {
		return new DefaultServiceReferenceCallback((DefaultServiceReference) servRefHandler, targetXAdressInfo);
	}
}
