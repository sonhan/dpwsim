/*******************************************************************************
 * Copyright (c) 2009 MATERNA Information & Communications. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html. For further
 * project-related information visit http://www.ws4d.org. The most recent
 * version of the JMEDS framework can be obtained from
 * http://sourceforge.net/projects/ws4d-javame.
 ******************************************************************************/
package org.ws4d.java.eventing;

import org.ws4d.java.service.LocalService;
import org.ws4d.java.service.reference.ServiceReference;
import org.ws4d.java.structures.DataStructure;
import org.ws4d.java.types.EndpointReference;
import org.ws4d.java.types.QName;
import org.ws4d.java.wsdl.WSDLOperation;

public interface EventingFactory {

	public EventSource createDefaultEventSource(String name, QName portType);

	public EventSource createDefaultEventSource(WSDLOperation operation);

	public ClientSubscription createClientSubscription(EventSink sink, String clientSubscriptionId, EndpointReference serviceSubscriptionId, String comManId, long duration, ServiceReference servRef);

	public EventSink createEventSink(EventListener eventListener, int configurationId);

	public EventSink createEventSink(EventListener eventListener, DataStructure bindings);

	public SubscriptionManager getSubscriptionManager(LocalService service);

}
