/*******************************************************************************
 * Copyright (c) 2009 MATERNA Information & Communications. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html. For further
 * project-related information visit http://www.ws4d.org. The most recent
 * version of the JMEDS framework can be obtained from
 * http://sourceforge.net/projects/ws4d-javame.
 ******************************************************************************/
package org.ws4d.java.client;

import org.ws4d.java.communication.CommunicationManagerRegistry;
import org.ws4d.java.communication.DefaultResponseCallback;
import org.ws4d.java.communication.ProtocolData;
import org.ws4d.java.communication.TimeoutException;
import org.ws4d.java.configuration.DispatchingProperties;
import org.ws4d.java.dispatch.OutDispatcher;
import org.ws4d.java.message.FaultMessage;
import org.ws4d.java.message.InvokeMessage;
import org.ws4d.java.message.Message;
import org.ws4d.java.service.InvocationException;
import org.ws4d.java.service.parameter.ParameterValue;
import org.ws4d.java.service.reference.DeviceReference;
import org.ws4d.java.service.reference.ServiceReference;
import org.ws4d.java.types.QName;
import org.ws4d.java.types.QNameSet;
import org.ws4d.java.types.URI;
import org.ws4d.java.types.XAddressInfo;
import org.ws4d.java.util.Log;
import org.ws4d.java.util.TimedEntry;
import org.ws4d.java.util.WatchDog;

/**
 * This utility class allows the invocation of a Web Service action without WSDL
 * parsing or discovery.
 */
public class InvokeUtil {

	/**
	 * Invokes the one-way operation of a Web Service with given input action
	 * URI.
	 * <p>
	 * This method tries to <strong>discover</strong> the Web Service. The first
	 * service which matches the port type is used. The port type is to be
	 * extracted from the input action URI.
	 * </p>
	 * 
	 * @param actionUri the input action URI of the operation
	 * @param value the request value for the operation
	 */
	public static void invokeAnyOneWay(String actionUri, ParameterValue value) {
		invokeOneWay(discover(actionUri), actionUri, value);
	}

	/**
	 * Invokes the operation of a Web Service with given input action URI.
	 * <p>
	 * This method tries to <strong>discover</strong> the Web Service. The first
	 * service which matches the port type is to be used. The port type is to be
	 * extracted from the input action URI.
	 * </p>
	 * 
	 * @param actionUri the input action URI of the operation
	 * @param value the request value for the operation
	 * @return the value of the response
	 * @throws InvocationException
	 * @throws TimeoutException
	 */
	public static ParameterValue invokeAny(String actionUri, ParameterValue value) throws InvocationException, TimeoutException {
		return invoke(discover(actionUri), actionUri, value, true);
	}

	/**
	 * Invokes the one-way operation of a Web Service with given address and
	 * input action URI.
	 * 
	 * @param actionUri the input action URI of the operation
	 * @param value the request value for the operation
	 * @throws InvocationException
	 * @throws TimeoutException
	 */
	public static void invokeOneWay(URI address, String actionUri, ParameterValue value) {
		try {
			invoke(address, actionUri, value, false);
		} catch (Exception e) {
			// void
		}
	}

	/**
	 * Invokes the operation of a Web Service with given address and input
	 * action URI.
	 * 
	 * @param actionUri the input action URI of the operation
	 * @param value the request value for the operation
	 * @return the value of the response
	 * @throws InvocationException
	 * @throws TimeoutException
	 */
	public static ParameterValue invoke(URI address, String actionUri, ParameterValue value) throws InvocationException, TimeoutException {
		return invoke(address, actionUri, value, true);
	}

	private static ParameterValue invoke(URI address, String actionUri, ParameterValue value, boolean awaitResponse) throws InvocationException, TimeoutException {
		InvokeMessage message = new InvokeMessage(actionUri, CommunicationManagerRegistry.getDefault());
		XAddressInfo targetXAddressInfo = new XAddressInfo(address, CommunicationManagerRegistry.getDefault());
		message.setTargetXAddressInfo(targetXAddressInfo);
		message.setContent(value);

		MyResponseCallBack callback = new MyResponseCallBack(targetXAddressInfo);
		OutDispatcher.getInstance().send(message, targetXAddressInfo, callback);
		if (awaitResponse) {
			return callback.waitForMe();
		}
		return null;
	}

	private static URI discover(String actionUri) {
		int i = actionUri.lastIndexOf('/');
		String portType = actionUri.substring(0, i);
		i = portType.lastIndexOf('/');
		String namespace = portType.substring(0, i);
		String localPart = portType.substring(i + 1, portType.length());

		QName pt = new QName(localPart, namespace);

		SearchParameter parameter = new SearchParameter();
		parameter.setServiceTypes(new QNameSet(pt));

		MySearchCallback callback = new MySearchCallback();
		SearchManager.searchService(parameter, callback);
		return callback.waitForMe();
	}

	private static class MySearchCallback extends TimedEntry implements SearchCallback {

		private volatile boolean	pending	= true;

		private URI					address	= null;

		/*
		 * (non-Javadoc)
		 * @see
		 * org.ws4d.java.client.SearchCallback#deviceFound(org.ws4d.java.service
		 * .reference.DeviceReference, org.ws4d.java.client.SearchParameter)
		 */
		public void deviceFound(DeviceReference devRef, SearchParameter search) {
			// void
		}

		/*
		 * (non-Javadoc)
		 * @see
		 * org.ws4d.java.client.SearchCallback#serviceFound(org.ws4d.java.service
		 * .reference.ServiceReference, org.ws4d.java.client.SearchParameter)
		 */
		public synchronized void serviceFound(ServiceReference servRef, SearchParameter search) {
			try {
				address = servRef.getPreferredXAddress();
				unsync();
			} catch (TimeoutException e) {
				Log.error("Unable to obtain transport addres of service: " + e);
			}
		}

		/*
		 * (non-Javadoc)
		 * @see org.ws4d.java.util.TimedEntry#timedOut()
		 */
		protected synchronized void timedOut() {
			Log.error("Service discovery timeout.");
			unsync();
		}

		synchronized URI waitForMe() {
			WatchDog.getInstance().register(this, DispatchingProperties.getInstance().getMatchWaitTime());
			while (pending) {
				try {
					wait();
				} catch (InterruptedException e) {
					Log.printStackTrace(e);
				}
			}
			WatchDog.getInstance().unregister(this);
			return address;
		}

		private void unsync() {
			pending = false;
			notify();
		}

	}

	private static class MyResponseCallBack extends DefaultResponseCallback {

		private volatile boolean	pending				= true;

		private ParameterValue		response			= null;

		private InvocationException	invocationException	= null;

		private TimeoutException	timeoutException	= null;
		
		MyResponseCallBack(XAddressInfo targetXAddressInfo) {
			super(targetXAddressInfo);
		}

		/*
		 * (non-Javadoc)
		 * @see
		 * org.ws4d.java.communication.DefaultResponseCallback#handle(org.ws4d
		 * .java.message.Message, org.ws4d.java.message.InvokeMessage,
		 * org.ws4d.java.communication.ProtocolData)
		 */
		public synchronized void handle(Message request, InvokeMessage invokeResponse, ProtocolData protocolData) {
			response = invokeResponse.getContent();
			unsync();
		}

		/*
		 * (non-Javadoc)
		 * @see
		 * org.ws4d.java.communication.DefaultResponseCallback#handle(org.ws4d
		 * .java.message.Message, org.ws4d.java.message.FaultMessage,
		 * org.ws4d.java.communication.ProtocolData)
		 */
		public synchronized void handle(Message request, FaultMessage fault, ProtocolData protocolData) {
			invocationException = new InvocationException(fault);
			unsync();
		}

		/*
		 * (non-Javadoc)
		 * @see org.ws4d.java.communication.DefaultResponseCallback#
		 * handleTransmissionException(org.ws4d.java.message.Message,
		 * java.lang.Exception, org.ws4d.java.communication.ProtocolData)
		 */
		public synchronized void handleTransmissionException(Message request, Exception exception, ProtocolData protocolData) {
			Log.error("Transmission error: " + exception);
			// TODO
			unsync();
		}

		/*
		 * (non-Javadoc)
		 * @see org.ws4d.java.communication.DefaultResponseCallback#
		 * handleMalformedResponseException(org.ws4d.java.message.Message,
		 * java.lang.Exception, org.ws4d.java.communication.ProtocolData)
		 */
		public synchronized void handleMalformedResponseException(Message request, Exception exception, ProtocolData protocolData) {
			Log.error("Malformed response: " + exception);
			// TODO
			unsync();
		}

		/*
		 * (non-Javadoc)
		 * @see
		 * org.ws4d.java.communication.DefaultResponseCallback#handleTimeout
		 * (org.ws4d.java.message.Message)
		 */
		public synchronized void handleTimeout(Message request) {
			Log.error("Request timeout: " + request);
			timeoutException = new TimeoutException("Invocation timeout");
			unsync();
		}

		synchronized ParameterValue waitForMe() throws InvocationException, TimeoutException {
			while (pending) {
				try {
					// FIXME this won't work for one-way operations!!!
					wait();
				} catch (InterruptedException e) {
					Log.printStackTrace(e);
				}
			}
			if (timeoutException != null) {
				throw timeoutException;
			}
			if (invocationException != null) {
				throw invocationException;
			}
			return response;
		}

		private void unsync() {
			pending = false;
			notify();
		}

	}

}
