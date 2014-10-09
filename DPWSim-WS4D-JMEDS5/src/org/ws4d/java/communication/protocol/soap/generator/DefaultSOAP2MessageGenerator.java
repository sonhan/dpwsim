/*******************************************************************************
 * Copyright (c) 2009 MATERNA Information & Communications. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html. For further
 * project-related information visit http://www.ws4d.org. The most recent
 * version of the JMEDS framework can be obtained from
 * http://sourceforge.net/projects/ws4d-javame.
 ******************************************************************************/

package org.ws4d.java.communication.protocol.soap.generator;

import java.io.IOException;
import java.io.InputStream;

import org.ws4d.java.DPWSFramework;
import org.ws4d.java.communication.CommunicationManager;
import org.ws4d.java.communication.CommunicationUtil;
import org.ws4d.java.communication.DPWSCommunicationManager;
import org.ws4d.java.communication.DPWSProtocolData;
import org.ws4d.java.communication.ProtocolData;
import org.ws4d.java.communication.VersionMismatchException;
import org.ws4d.java.communication.monitor.MonitorStreamFactory;
import org.ws4d.java.communication.monitor.MonitoringContext;
import org.ws4d.java.constants.ConstantsHelper;
import org.ws4d.java.constants.MEXConstants;
import org.ws4d.java.constants.SOAPConstants;
import org.ws4d.java.constants.WSAConstants;
import org.ws4d.java.constants.WSEConstants;
import org.ws4d.java.constants.WXFConstants;
import org.ws4d.java.io.xml.ElementParser;
import org.ws4d.java.io.xml.XmlPullParserSupport;
import org.ws4d.java.message.FaultMessage;
import org.ws4d.java.message.InvokeMessage;
import org.ws4d.java.message.Message;
import org.ws4d.java.message.MessageDiscarder;
import org.ws4d.java.message.SOAPHeader;
import org.ws4d.java.message.discovery.ByeMessage;
import org.ws4d.java.message.discovery.HelloMessage;
import org.ws4d.java.message.discovery.ProbeMatchesMessage;
import org.ws4d.java.message.discovery.ProbeMessage;
import org.ws4d.java.message.discovery.ResolveMatchesMessage;
import org.ws4d.java.message.discovery.ResolveMessage;
import org.ws4d.java.message.eventing.GetStatusMessage;
import org.ws4d.java.message.eventing.GetStatusResponseMessage;
import org.ws4d.java.message.eventing.RenewMessage;
import org.ws4d.java.message.eventing.RenewResponseMessage;
import org.ws4d.java.message.eventing.SubscribeMessage;
import org.ws4d.java.message.eventing.SubscribeResponseMessage;
import org.ws4d.java.message.eventing.SubscriptionEndMessage;
import org.ws4d.java.message.eventing.UnsubscribeMessage;
import org.ws4d.java.message.eventing.UnsubscribeResponseMessage;
import org.ws4d.java.message.metadata.GetMessage;
import org.ws4d.java.message.metadata.GetMetadataMessage;
import org.ws4d.java.message.metadata.GetMetadataResponseMessage;
import org.ws4d.java.message.metadata.GetResponseMessage;
import org.ws4d.java.schema.Element;
import org.ws4d.java.service.OperationDescription;
import org.ws4d.java.service.parameter.ParameterDefinition;
import org.ws4d.java.service.parameter.ParameterValue;
import org.ws4d.java.structures.ArrayList;
import org.ws4d.java.structures.HashSet;
import org.ws4d.java.structures.List;
import org.ws4d.java.types.AttributedURI;
import org.ws4d.java.types.URI;
import org.ws4d.java.util.Log;
import org.ws4d.java.wsdl.WSDLOperation;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class DefaultSOAP2MessageGenerator implements SOAP2MessageGenerator {

	protected static final DefaultMessageDiscarder	DEFAULT_DISCARDER	= new DefaultMessageDiscarder();

	/**
	 * This map holds all (statically) known message handlers.
	 */
	protected static ConstantsHelper				helper;

	private static DefaultMessageDiscarder			defaultDiscarder	= DEFAULT_DISCARDER;

	private final XmlPullParser						parser;

	private MessageParser							msgParser			= new DefaultMessageParser();

	/**
	 * Standard constructor
	 */
	public DefaultSOAP2MessageGenerator() {
		super();
		XmlPullParser parser = null;
		try {
			parser = XmlPullParserSupport.getFactory().newPullParser();
		} catch (XmlPullParserException e) {
			Log.error("Could not create XmlPullParser: " + e);
			e.printStackTrace();
			throw new RuntimeException("Could not create XmlPullParser: " + e);
		}
		this.parser = parser;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.ws4d.java.communication.protocol.soap.generator.SOAP2MessageGenerator
	 * #deliverMessage(java.io.InputStream,
	 * org.ws4d.java.communication.protocol.soap.generator.MessageReceiver,
	 * org.ws4d.java.communication.ProtocolData)
	 */
	public void deliverMessage(InputStream in, MessageReceiver to, ProtocolData protocolData) {
		deliverMessage(in, to, protocolData, getDefaultMessageDiscarder());
	}

	public static synchronized DefaultMessageDiscarder getDefaultMessageDiscarder() {
		return defaultDiscarder;
	}

	public static synchronized void setMessageDiscarder(DefaultMessageDiscarder newDiscarder) {
		defaultDiscarder = (newDiscarder == null ? DEFAULT_DISCARDER : newDiscarder);
	}

	protected void deliverBody(SOAPHeader header, ElementParser parser, MessageReceiver to, ProtocolData protocolData) throws XmlPullParserException, IOException, UnexpectedMessageException, MissingElementException, UnexpectedElementException, VersionMismatchException {
		String namespace;
		String name;
		if (header == null) {
			throw new MissingElementException(SOAPConstants.SOAP12_NAMESPACE_NAME + ":" + SOAPConstants.SOAP_ELEM_HEADER);
		}
		AttributedURI action = header.getAction();
		if (action == null) {
			throw new MissingElementException(helper.getWSANamespace() + ":" + WSAConstants.WSA_ELEM_ACTION);
		}
		String actionName = action.toString();
		protocolData.setProtocolInfo(header.getProtocolInfo());

		/*
		 * goes for the next tag inside this message. this can be a new tag, or
		 * the closing soap:Body tag. check for text inside the body tag
		 */

		int eventType = parser.next();
		if (eventType == XmlPullParser.TEXT) {
			// eat unnecessary text
			parser.getText();
			eventType = parser.next();
		}
		if (eventType == XmlPullParser.START_TAG || eventType == XmlPullParser.END_TAG) {

			CommunicationManager comMan = DPWSFramework.getCommunicationManager(protocolData.getCommunicationManagerId());
			CommunicationUtil comUtil = comMan.getCommunicationUtil();
			helper = comUtil.getHelper(header.getProtocolInfo().getVersion());

			if (handleMessage(parser, actionName, header, to, protocolData)) {
				return;
			}

			// this must be an invoke message

			/*
			 * there might be a soap:Fault within the body rather than the
			 * message stuff, check and propagate accordingly
			 */
			name = parser.getName();
			namespace = parser.getNamespace();
			if (SOAPConstants.SOAP12_NAMESPACE_NAME.equals(namespace) && SOAPConstants.SOAP_ELEM_FAULT.equals(name)) {
				// The FaultMessage parses itself.
				OperationDescription op = to.getOperation(actionName);
				FaultMessage fm = msgParser.parseFaultMessage(header, parser, protocolData, helper, actionName, op);
				to.receive(fm, protocolData);
				return;
			}

			InvokeMessage msg = new InvokeMessage(header);

			AttributedURI relatesTo = header.getRelatesTo();

			List l = new ArrayList();
			OperationDescription operation = to.getOperation(msg.getAction().toString());

			while (parser.getEventType() != XmlPullParser.END_TAG) {
				/*
				 * if this is not the closing soap:Body, get the stuff inside.
				 */
				int ot = operation.getType();
				Element element = null;
				if ((relatesTo == null && ot == WSDLOperation.TYPE_SOLICIT_RESPONSE) || (relatesTo != null && ot == WSDLOperation.TYPE_REQUEST_RESPONSE) || (relatesTo != null && ot == WSDLOperation.TYPE_ONE_WAY) || (relatesTo == null && ot == WSDLOperation.TYPE_NOTIFICATION)) {
					element = operation.getOutput();
				} else if ((relatesTo != null && ot == WSDLOperation.TYPE_SOLICIT_RESPONSE) || (relatesTo == null && ot == WSDLOperation.TYPE_REQUEST_RESPONSE) || (relatesTo == null && ot == WSDLOperation.TYPE_ONE_WAY) || (relatesTo != null && ot == WSDLOperation.TYPE_NOTIFICATION)) {
					element = operation.getInput();
				}
				l.add(ParameterDefinition.parse(parser, element, operation));
				parser.nextTag();
			}

			switch (l.size()) {
				case (0): {
					break;
				}
				case (1): {
					ParameterValue value = (ParameterValue) l.get(0);
					msg.setContent(value);
					break;
				}
				default: {
					throw new UnexpectedElementException("too much message parts: " + l.size() + "; next part=" + l.get(1).toString());
				}
			}
			to.receive(msg, protocolData);
		}
	}

	// Added 201-11-12 SSch: to ease extension
	protected boolean handleMessage(ElementParser parser, String actionName, SOAPHeader header, MessageReceiver to, ProtocolData protocolData) throws XmlPullParserException, IOException, VersionMismatchException {
		// The right Message parse itself.
		if (helper.getWSDActionHello().equals(actionName)) {
			HelloMessage hm = msgParser.parseHelloMessage(header, parser, protocolData, helper);
			to.receive(hm, protocolData);
		} else if (helper.getWSDActionBye().equals(actionName)) {
			ByeMessage bm = msgParser.parseByeMessage(header, parser, protocolData, helper);
			to.receive(bm, protocolData);
		} else if (helper.getWSDActionProbe().equals(actionName)) {
			ProbeMessage pm = msgParser.parseProbeMessage(header, parser, protocolData, helper);
			to.receive(pm, protocolData);
		} else if (helper.getWSDActionProbeMatches().equals(actionName)) {
			ProbeMatchesMessage pmm = msgParser.parseProbeMatchesMessage(header, parser, protocolData, helper);
			to.receive(pmm, protocolData);
		} else if (helper.getWSDActionResolve().equals(actionName)) {
			ResolveMessage rm = msgParser.parseResolveMessage(header, parser, protocolData, helper);
			to.receive(rm, protocolData);
		} else if (helper.getWSDActionResolveMatches().equals(actionName)) {
			ResolveMatchesMessage rmm = msgParser.parseResolveMatchesMessage(header, parser, protocolData, helper);
			to.receive(rmm, protocolData);
		} else if (WXFConstants.WXF_ACTION_GET.equals(actionName)) {
			URI transportAddress = protocolData.getTransportAddress();
			if (transportAddress != null && DPWSCommunicationManager.getRegisterForGetMetadata().contains(transportAddress)) {
				to.receive(new GetMetadataMessage(header), protocolData);
			} else {
				to.receive(new GetMessage(header), protocolData);
			}
		} else if (WXFConstants.WXF_ACTION_GETRESPONSE.equals(actionName)) {
			HashSet check = (HashSet) DPWSCommunicationManager.getMessageIDsForGetMetadataMapping();
			if (check.remove(header.getRelatesTo())) {
				GetMetadataResponseMessage gmrm = msgParser.parseGetMetadataResponseMessage(header, parser, protocolData, helper);
				to.receive(gmrm, protocolData);
			} else {
				GetResponseMessage grm = msgParser.parseGetResponseMessage(header, parser, protocolData, helper);
				to.receive(grm, protocolData);
			}
		} else if (MEXConstants.WSX_ACTION_GETMETADATA_REQUEST.equals(actionName)) {
			GetMetadataMessage gmm = msgParser.parseGetMetadataMessage(header, parser, protocolData, helper);
			to.receive(gmm, protocolData);
		} else if (MEXConstants.WSX_ACTION_GETMETADATA_RESPONSE.equals(actionName)) {
			GetMetadataResponseMessage gmrm = msgParser.parseGetMetadataResponseMessage(header, parser, protocolData, helper);
			to.receive(gmrm, protocolData);
		} else if (WSEConstants.WSE_ACTION_SUBSCRIBE.equals(actionName)) {
			SubscribeMessage sm = msgParser.parseSubscribeMessage(header, parser, protocolData, helper);
			to.receive(sm, protocolData);
		} else if (WSEConstants.WSE_ACTION_SUBSCRIBERESPONSE.equals(actionName)) {
			SubscribeResponseMessage srm = msgParser.parseSubscribeResponseMessage(header, parser, protocolData, helper);
			to.receive(srm, protocolData);
		} else if (WSEConstants.WSE_ACTION_RENEW.equals(actionName)) {
			RenewMessage rm = msgParser.parseRenewMessage(header, parser, protocolData, helper);
			to.receive(rm, protocolData);
		} else if (WSEConstants.WSE_ACTION_RENEWRESPONSE.equals(actionName)) {
			RenewResponseMessage rrm = msgParser.parseRenewResponseMessage(header, parser, protocolData, helper);
			to.receive(rrm, protocolData);
		} else if (WSEConstants.WSE_ACTION_GETSTATUS.equals(actionName)) {
			GetStatusMessage gsm = msgParser.parseGetStatusMessage(header, parser, protocolData, helper);
			to.receive(gsm, protocolData);
		} else if (WSEConstants.WSE_ACTION_GETSTATUSRESPONSE.equals(actionName)) {
			GetStatusResponseMessage gsrm = msgParser.parseGetStatusResponseMessage(header, parser, protocolData, helper);
			to.receive(gsrm, protocolData);
		} else if (WSEConstants.WSE_ACTION_UNSUBSCRIBE.equals(actionName)) {
			UnsubscribeMessage um = msgParser.parseUnsubscribeMessage(header, parser, protocolData, helper);
			to.receive(um, protocolData);
		} else if (WSEConstants.WSE_ACTION_UNSUBSCRIBERESPONSE.equals(actionName)) {
			UnsubscribeResponseMessage urm = msgParser.parseUnsubscribeResponseMessage(header, parser, protocolData, helper);
			to.receive(urm, protocolData);
		} else if (WSEConstants.WSE_ACTION_SUBSCRIPTIONEND.equals(actionName)) {
			SubscriptionEndMessage sem = msgParser.parseSubscriptionEndMessage(header, parser, protocolData, helper);
			to.receive(sem, protocolData);
		} else {
			// unrecognized action
			return false;
		}
		return true;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.ws4d.java.communication.protocol.soap.generator.SOAP2MessageGenerator
	 * #generateMessage(java.io.InputStream)
	 */
	public Message generateMessage(InputStream in) throws Exception {
		InlineMessageReceiver receiver = new InlineMessageReceiver();
		deliverMessage(in, receiver, new DPWSProtocolData("", ProtocolData.DIRECTION_IN, "", 0, "", 0, false), getDefaultMessageDiscarder());
		if (receiver.e != null) {
			throw receiver.e;
		}
		return receiver.result;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.ws4d.java.communication.protocol.soap.generator.SOAP2MessageGenerator
	 * #deliverMessage(java.io.InputStream,
	 * org.ws4d.java.communication.protocol.soap.generator.MessageReceiver,
	 * org.ws4d.java.communication.ProtocolData,
	 * org.ws4d.java.communication.protocol.soap.generator.MessageDiscarder)
	 */
	public void deliverMessage(InputStream in, MessageReceiver to, ProtocolData protocolData, DefaultMessageDiscarder discarder) {
		XmlPullParser parser = this.parser;

		if (DPWSFramework.hasModule(DPWSFramework.SECURITY_MODULE)) {
			in = DPWSFramework.getSecurityManager().wrapInputStream(in, protocolData);
		}

		try {
			parser.setInput(in, null);

			parser.nextTag(); // go to SOAP Envelope
			String namespace = parser.getNamespace();
			String name = parser.getName();

			if (SOAPConstants.SOAP12_NAMESPACE_NAME.equals(namespace)) {
				if (SOAPConstants.SOAP_ELEM_ENVELOPE.equals(name)) {
					ElementParser elementParser = new ElementParser(parser);

					elementParser.nextTag(); // go to SOAP Header
					namespace = elementParser.getNamespace();
					name = elementParser.getName();
					SOAPHeader header = null;
					if (SOAPConstants.SOAP12_NAMESPACE_NAME.equals(namespace) && SOAPConstants.SOAP_ELEM_HEADER.equals(name)) {
						// SOAPHeader is parsing itself.
						header = msgParser.parseSOAPHeader(elementParser, protocolData, helper);
						if (Log.isDebug()) {
							Log.debug("<I> Incoming SOAP message header: [ " + header + " ]", Log.DEBUG_LAYER_FRAMEWORK);
						}
						if (discarder == null) {
							discarder = getDefaultMessageDiscarder();
						}

						int reason = discarder.discardMessage(header, protocolData);

						if (reason > DefaultMessageDiscarder.NOT_DISCARDED) {
							MonitorStreamFactory msf = DPWSFramework.getMonitorStreamFactory();
							if (msf != null) {
								MonitoringContext context = msf.getMonitoringContextIn(protocolData);
								if (context != null) {
									msf.discard(protocolData, context, reason);
								} else {
									Log.warn("Cannot get correct monitoring context for message generation.");
								}
							}
							return;
						}
						name = elementParser.getName();

						elementParser.nextTag(); // go to SOAP Body
						namespace = elementParser.getNamespace();
						name = elementParser.getName();
					}
					if (SOAPConstants.SOAP12_NAMESPACE_NAME.equals(namespace) && SOAPConstants.SOAP_ELEM_BODY.equals(name)) {
						deliverBody(header, elementParser, to, protocolData);
					} else {
						// no body present
						throw new UnexpectedElementException(namespace + ":" + name + " (SOAP12:Body expected)");
					}

				} else {
					// no envelope present
					throw new UnexpectedElementException(namespace + ":" + name + " (SOAP12:Envelope expected)");
				}
			} else if (SOAPConstants.SOAP11_OLD_NAMESPACE_NAME.equals(namespace)) {
				throw new VersionMismatchException("SOAP " + SOAPConstants.SOAP11_OLD_NAMESPACE_NAME, VersionMismatchException.TYPE_WRONG_SOAP_VERSION);
			} else {
				// no envelope present
				throw new UnexpectedElementException(namespace + ":" + name + " (SOAP12:Envelope expected)");
			}
		} catch (VersionMismatchException e) {
			// only SOAP Envelope or WS-Addressing [action] can cause this
			if (Log.isDebug()) {
				Log.debug("Version mismatch: " + e.getMessage(), Log.DEBUG_LAYER_FRAMEWORK);
			}
			MonitorStreamFactory msf = DPWSFramework.getMonitorStreamFactory();
			if (msf != null) {
				MonitoringContext context = msf.getMonitoringContextIn(protocolData);
				if (context != null) {
					msf.discard(protocolData, context, MessageDiscarder.VERSION_NOT_SUPPORTED);
				} else {
					Log.warn("Cannot get correct monitoring context for message generation.");
				}
			}
			to.receiveFailed(e, protocolData);
		} catch (UnexpectedMessageException e) {
			Log.error("Unexpected message: " + e.getMessage());
			e.printStackTrace();
			to.receiveFailed(e, protocolData);
		} catch (MissingElementException e) {
			// only WS-Addressing [action] can cause this
			Log.error("Missing required element " + e.getMessage());
			to.receiveFailed(e, protocolData);
		} catch (UnexpectedElementException e) {
			Log.error("Unexpected element: " + e.getMessage());
			to.receiveFailed(e, protocolData);
		} catch (XmlPullParserException e) {
			Log.error("Parse exception during XML processing: " + e + ", caused by " + e.getDetail());
			e.printStackTrace();
			to.receiveFailed(e, protocolData);
		} catch (IOException e) {
			Log.error("IO exception during XML processing: " + e);
			e.printStackTrace();
			to.receiveFailed(e, protocolData);
		} finally {
			try {
				parser.setInput(null);
			} catch (XmlPullParserException e2) {
				// shouldn't ever occur
				Log.error("Unable to reset XML parser: " + e2);
			}
		}
	}

	protected static class InlineMessageReceiver implements MessageReceiver {

		Message		result;

		Exception	e;

		public void sendFailed(Exception e, ProtocolData protocolData) {
			this.e = e;
		}

		public void receiveFailed(Exception e, ProtocolData protocolData) {
			this.e = e;
		}

		public void receive(FaultMessage fault, ProtocolData protocolData) {
			this.result = fault;
		}

		public void receive(InvokeMessage invoke, ProtocolData protocolData) {
			this.result = invoke;
		}

		public void receive(SubscriptionEndMessage subscriptionEnd, ProtocolData protocolData) {
			this.result = subscriptionEnd;
		}

		public void receive(UnsubscribeResponseMessage unsubscribeResponse, ProtocolData protocolData) {
			this.result = unsubscribeResponse;
		}

		public void receive(UnsubscribeMessage unsubscribe, ProtocolData protocolData) {
			this.result = unsubscribe;
		}

		public void receive(RenewResponseMessage renewResponse, ProtocolData protocolData) {
			this.result = renewResponse;
		}

		public void receive(RenewMessage renew, ProtocolData protocolData) {
			this.result = renew;
		}

		public void receive(GetStatusResponseMessage getStatusResponse, ProtocolData protocolData) {
			this.result = getStatusResponse;
		}

		public void receive(GetStatusMessage getStatus, ProtocolData protocolData) {
			this.result = getStatus;
		}

		public void receive(SubscribeResponseMessage subscribeResponse, ProtocolData protocolData) {
			this.result = subscribeResponse;
		}

		public void receive(SubscribeMessage subscribe, ProtocolData protocolData) {
			this.result = subscribe;
		}

		public void receive(GetMetadataResponseMessage getMetadataResponse, ProtocolData protocolData) {
			this.result = getMetadataResponse;
		}

		public void receive(GetMetadataMessage getMetadata, ProtocolData protocolData) {
			this.result = getMetadata;
		}

		public void receive(GetResponseMessage getResponse, ProtocolData protocolData) {
			this.result = getResponse;
		}

		public void receive(GetMessage get, ProtocolData protocolData) {
			this.result = get;
		}

		public void receive(ResolveMatchesMessage resolveMatches, ProtocolData protocolData) {
			this.result = resolveMatches;
		}

		public void receive(ResolveMessage resolve, ProtocolData protocolData) {
			this.result = resolve;
		}

		public void receive(ProbeMatchesMessage probeMatches, ProtocolData protocolData) {
			this.result = probeMatches;
		}

		public void receive(ProbeMessage probe, ProtocolData protocolData) {
			this.result = probe;
		}

		public void receive(ByeMessage bye, ProtocolData protocolData) {
			this.result = bye;
		}

		public void receive(HelloMessage hello, ProtocolData protocolData) {
			this.result = hello;
		}

		public OperationDescription getOperation(String action) {
			return null;
		}
	}
}