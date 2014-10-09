/*******************************************************************************
 * Copyright (c) 2009 MATERNA Information & Communications. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html. For further
 * project-related information visit http://www.ws4d.org. The most recent
 * version of the JMEDS framework can be obtained from
 * http://sourceforge.net/projects/ws4d-javame.
 ******************************************************************************/
package org.ws4d.java.communication.DPWS2006;

import org.ws4d.java.communication.CommunicationUtil;
import org.ws4d.java.communication.DPWS2009.DefaultConstantsHelper2009;
import org.ws4d.java.configuration.DPWSProperties;
import org.ws4d.java.constants.ConstantsHelper;
import org.ws4d.java.constants.DPWSConstants;
import org.ws4d.java.constants.DPWSConstants2006;
import org.ws4d.java.constants.DPWSMessageConstants;
import org.ws4d.java.constants.WSAConstants2006;
import org.ws4d.java.constants.WSDConstants2006;
import org.ws4d.java.constants.WXFConstants;
import org.ws4d.java.io.xml.ElementHandlerRegistry;
import org.ws4d.java.message.FaultMessage;
import org.ws4d.java.message.Message;
import org.ws4d.java.message.SOAPHeader;
import org.ws4d.java.message.discovery.ByeMessage;
import org.ws4d.java.message.discovery.HelloMessage;
import org.ws4d.java.message.discovery.ProbeMatch;
import org.ws4d.java.message.discovery.ProbeMatchesMessage;
import org.ws4d.java.message.discovery.ProbeMessage;
import org.ws4d.java.message.discovery.ResolveMatchesMessage;
import org.ws4d.java.message.discovery.ResolveMessage;
import org.ws4d.java.message.eventing.SubscribeMessage;
import org.ws4d.java.message.metadata.GetMessage;
import org.ws4d.java.message.metadata.GetMetadataMessage;
import org.ws4d.java.message.metadata.GetMetadataResponseMessage;
import org.ws4d.java.message.metadata.GetResponseMessage;
import org.ws4d.java.structures.Iterator;
import org.ws4d.java.types.AttributedURI;
import org.ws4d.java.types.DiscoveryData;
import org.ws4d.java.types.EndpointReference;
import org.ws4d.java.types.QNameSet;
import org.ws4d.java.types.ServiceId;
import org.ws4d.java.util.Log;

public class DefaultDPWSCommunicatonUtil implements CommunicationUtil {

	private static DefaultDPWSCommunicatonUtil	instance	= null;

	public ConstantsHelper getHelper(int versionInfo) {
		ConstantsHelper helper = null;
		if (versionInfo == -1) {
			versionInfo = DPWSProperties.DEFAULT_DPWS_VERSION;
		}
		switch (versionInfo) {
			case DPWSConstants.DPWS_VERSION2009:
				helper = new DefaultConstantsHelper2009();
				break;
			case DPWSConstants2006.DPWS_VERSION2006:
				helper = new DefaultConstantsHelper2006();
				break;
		}
		return helper;
	}

	public static synchronized DefaultDPWSCommunicatonUtil getInstance() {
		if (instance == null) {
			// Register the Handler for DPWS 2006 ServiceID Element
			ElementHandlerRegistry.getRegistry().registerElementHandler(ServiceId.QNAME, new ServiceId());
			instance = new DefaultDPWSCommunicatonUtil();
		}
		return instance;
	}

	/**
	 * This method change the namespace and some attributes according to the
	 * version given.
	 */
	public Message changeOutgoingMessage(int version, Message message) {
		switch (message.getType()) {
			case DPWSMessageConstants.HELLO_MESSAGE:
				return changeOutgoingMessage(version, (HelloMessage) message);
			case DPWSMessageConstants.BYE_MESSAGE:
				return changeOutgoingMessage(version, (ByeMessage) message);
			case DPWSMessageConstants.PROBE_MESSAGE:
				return changeOutgoingMessage(version, (ProbeMessage) message);
			case DPWSMessageConstants.PROBE_MATCHES_MESSAGE:
				return changeOutgoingMessage(version, (ProbeMatchesMessage) message);
			case DPWSMessageConstants.RESOLVE_MESSAGE:
				return changeOutgoingMessage(version, (ResolveMessage) message);
			case DPWSMessageConstants.RESOLVE_MATCHES_MESSAGE:
				return changeOutgoingMessage(version, (ResolveMatchesMessage) message);
			case DPWSMessageConstants.GET_MESSAGE:
				return changeOutgoingMessage(version, (GetMessage) message);
			case DPWSMessageConstants.GET_RESPONSE_MESSAGE:
				return changeOutgoingMessage(version, (GetResponseMessage) message);
			case DPWSMessageConstants.GET_METADATA_MESSAGE:
				return changeOutgoingMessage(version, (GetMetadataMessage) message);
			case DPWSMessageConstants.GET_METADATA_RESPONSE_MESSAGE:
				return changeOutgoingMessage(version, (GetMetadataResponseMessage) message);
			case DPWSMessageConstants.SUBSCRIBE_MESSAGE:
				return changeOutgoingMessage(version, (SubscribeMessage) message);
			case DPWSMessageConstants.FAULT_MESSAGE:
				return changeOutgoingMessage(version, (FaultMessage) message);
		}

		// message does not match any message type? :)
		return message;
	}

	private Message changeOutgoingMessage(int version, HelloMessage hello) {

		if (DPWSConstants2006.DPWS_VERSION2006 == version) {
			// Change wsa:Action
			hello.getHeader().setAction(new AttributedURI(WSDConstants2006.WSD_ACTION_HELLO));
			// Change wsa:To
			if (hello.getHeader().getTo() != null) {
				hello.getHeader().setTo(new AttributedURI(WSDConstants2006.WSD_TO));
			}

			changeTypesTo2006(hello.getTypes());
		} else if (DPWSConstants.DPWS_VERSION2009 == version) {
			changeTypesTo2009(hello.getTypes());
		}
		return hello;
	}

	private Message changeOutgoingMessage(int version, ByeMessage bye) {
		if (DPWSConstants2006.DPWS_VERSION2006 == version) {
			// Change wsa:Action
			bye.getHeader().setAction(new AttributedURI(WSDConstants2006.WSD_ACTION_BYE));
			// Change wsa:To
			if (bye.getHeader().getTo() != null) {
				bye.getHeader().setTo(new AttributedURI(WSDConstants2006.WSD_TO));
			}

			changeTypesTo2006(bye.getTypes());
		} else if (DPWSConstants.DPWS_VERSION2009 == version) {
			changeTypesTo2009(bye.getTypes());
		}
		return bye;
	}

	private Message changeOutgoingMessage(int version, ProbeMessage probe) {
		if (DPWSConstants2006.DPWS_VERSION2006 == version) {
			// Change wsa:Action
			probe.getHeader().setAction(new AttributedURI(WSDConstants2006.WSD_ACTION_PROBE));
			// Change wsa:To
			probe.getHeader().setTo(new AttributedURI(WSDConstants2006.WSD_TO));
		}
		return probe;
	}

	private Message changeOutgoingMessage(int version, ProbeMatchesMessage probeMatches) {
		if (DPWSConstants2006.DPWS_VERSION2006 == version) {
			// Change wsa:Action
			probeMatches.getHeader().setAction(new AttributedURI(WSDConstants2006.WSD_ACTION_PROBEMATCHES));
			// Change wsa:To
			if (probeMatches.getHeader().getTo() != null) {
				probeMatches.getHeader().setTo(new AttributedURI(WSAConstants2006.WSA_ANONYMOUS));
			}
			// Change DPWS Devicetype QN
			if (probeMatches.getProbeMatchCount() > 0) {
				Iterator it = probeMatches.getProbeMatches().iterator();
				while (it.hasNext()) {
					ProbeMatch probeMatch = (ProbeMatch) it.next();
					changeTypesTo2006(probeMatch.getTypes());
				}
			}
		} else if (DPWSConstants.DPWS_VERSION2009 == version) {
			if (probeMatches.getProbeMatchCount() > 0) {
				Iterator it = probeMatches.getProbeMatches().iterator();
				while (it.hasNext()) {
					ProbeMatch probeMatch = (ProbeMatch) it.next();
					changeTypesTo2009(probeMatch.getTypes());
				}
			}
		}
		return probeMatches;
	}

	private Message changeOutgoingMessage(int version, ResolveMessage resolve) {
		if (DPWSConstants2006.DPWS_VERSION2006 == version) {
			// Change wsa:Action
			resolve.getHeader().setAction(new AttributedURI(WSDConstants2006.WSD_ACTION_RESOLVE));
			// Change wsa:To
			resolve.getHeader().setTo(new AttributedURI(WSDConstants2006.WSD_TO));
		}
		return resolve;
	}

	private Message changeOutgoingMessage(int version, ResolveMatchesMessage resolveMatches) {
		if (DPWSConstants2006.DPWS_VERSION2006 == version) {
			// Change wsa:Action
			resolveMatches.getHeader().setAction(new AttributedURI(WSDConstants2006.WSD_ACTION_RESOLVEMATCHES));
			// Change wsa:To
			if (resolveMatches.getHeader().getTo() != null) {
				resolveMatches.getHeader().setTo(new AttributedURI(WSAConstants2006.WSA_ANONYMOUS));
			}
			// Change DPWS Devicetype QN
			if(resolveMatches.getResolveMatch()!= null){
			changeTypesTo2006(resolveMatches.getResolveMatch().getTypes());
			}
		} else if (DPWSConstants.DPWS_VERSION2009 == version) {
			if(resolveMatches.getResolveMatch()!= null){
				changeTypesTo2009(resolveMatches.getResolveMatch().getTypes());
				}
		}
		return resolveMatches;
	}

	private Message changeOutgoingMessage(int version, GetMessage get) {
		if (DPWSConstants2006.DPWS_VERSION2006 == version) {
			// Set wsa:ReplyTo
			get.getHeader().setReplyTo(new EndpointReference(WSAConstants2006.WSA_ANONYMOUS));
			// get.getHeader().setMessageId(new
			// AttributedURI(IDGenerator.getUUIDasURI()));
		}
		return get;
	}

	private Message changeOutgoingMessage(int version, GetResponseMessage getResponse) {
		if (DPWSConstants2006.DPWS_VERSION2006 == version) {
			// Change wsa:To
			if (getResponse.getHeader().getTo() != null) {
				getResponse.getHeader().setTo(new AttributedURI(WSAConstants2006.WSA_ANONYMOUS));
			}

			// getResponse.getHeader().setMessageId(new
			// AttributedURI(IDGenerator.getUUIDasURI()));

			changeTypesTo2006(getResponse.getHost().getTypes());

			getResponse.getHost().addUnknownElement(ServiceId.QNAME, DPWSConstants2006.DPWS_DEVICE_SERVICEID);
		} else if (DPWSConstants.DPWS_VERSION2009 == version) {
			changeTypesTo2009(getResponse.getHost().getTypes());
		}
		return getResponse;
	}

	public Message changeOutgoingMessage(int version, GetMetadataMessage getMetadata) {
		if (DPWSConstants2006.DPWS_VERSION2006 == version) {
			GetMessage get = new GetMessage(getMetadata.getHeader().getCommunicationManagerID());
			copyOutgoingHeader(get.getHeader(), getMetadata);
			get.setProtocolInfo(getMetadata.getProtocolInfo());

			copyOutgoingInheritAttributes(get, getMetadata);

			// Change wsa:Action
			get.getHeader().setAction(new AttributedURI(WXFConstants.WXF_ACTION_GET));
			// Set wsa:ReplyTo
			get.getHeader().setReplyTo(new EndpointReference(WSAConstants2006.WSA_ANONYMOUS));
			return get;
		}
		return getMetadata;
	}

	private Message changeOutgoingMessage(int version, GetMetadataResponseMessage getMetadataResponse) {
		if (DPWSConstants2006.DPWS_VERSION2006 == version) {
			// Change wsa:Action
			getMetadataResponse.getHeader().setAction(new AttributedURI(WXFConstants.WXF_ACTION_GETRESPONSE));
		}
		return getMetadataResponse;
	}

	private Message changeOutgoingMessage(int version, SubscribeMessage subscribe) {
		if (DPWSConstants2006.DPWS_VERSION2006 == version) {
			// Change wse:filter-->dialect
			subscribe.getFilter().setDialect(DPWSConstants2006.DPWS_URI_FILTER_EVENTING_ACTION);
		}
		return subscribe;
	}

	private Message changeOutgoingMessage(int version, FaultMessage fault) {
		if (DPWSConstants2006.DPWS_VERSION2006 == version) {
			fault.getHeader().setAction(new AttributedURI(DefaultConstantsHelper2006.getInstance().getWSAActionAddressingFault()));
			fault.setSubcode(DefaultConstantsHelper2006.getInstance().getWSAFaultMessageAddressingHeaderRequired());
		}
		return fault;
	}

	/**
	 * This method creates a copy of the given message an returns the copy.
	 * 
	 * @param message ,the given message
	 * @return ,the copy of the given message
	 */
	public Message copyOutgoingMessage(Message message) {
		if (message instanceof HelloMessage) {
			return copyOutgoingMessage((HelloMessage) message);
		} else if (message instanceof ByeMessage) {
			return copyOutgoingMessage((ByeMessage) message);
		} else if (message instanceof ProbeMessage) {
			return copyOutgoingMessage((ProbeMessage) message);
		} else if (message instanceof ResolveMessage) {
			return copyOutgoingMessage((ResolveMessage) message);
		} else if (message instanceof GetMessage) {
			return copyOutgoingMessage((GetMessage) message);
		}
		if (Log.isWarn()) {
			Log.warn("CopyOutgoingMessage(...) does not support messages of type " + message.getClass().getName());
		}
		return message;
	}

	private DiscoveryData copyDiscoveryData(DiscoveryData original) {
		QNameSet types = new QNameSet(original.getTypes());
		DiscoveryData d = new DiscoveryData(original);
		d.setTypes(types);
		return d;
	}

	private Message copyOutgoingMessage(HelloMessage original) {
		// generate new Hello Message within the DiscoveryData of the original
		HelloMessage hello = new HelloMessage(copyDiscoveryData(original.getDiscoveryData()), original.getHeader().getCommunicationManagerID());

		// copying Header
		copyOutgoingHeader(hello.getHeader(), original);

		// copying inherited attributs
		copyOutgoingInheritAttributes(hello, original);

		return hello;
	}

	private Message copyOutgoingMessage(ByeMessage original) {
		// generate new Hello Message within the DiscoveryData of the original
		ByeMessage bye = new ByeMessage(copyDiscoveryData(original.getDiscoveryData()), original.getHeader().getCommunicationManagerID());

		// copying Header
		copyOutgoingHeader(bye.getHeader(), original);

		// copying inherited attributs
		copyOutgoingInheritAttributes(bye, original);

		return bye;
	}

	private Message copyOutgoingMessage(ProbeMessage original) {
		// generate new Probe Message
		ProbeMessage probe = new ProbeMessage(original.getHeader().getCommunicationManagerID());

		// copying Header
		copyOutgoingHeader(probe.getHeader(), original);

		// copy Probespecific attributs
		probe.setTypes(original.getTypes());
		probe.setScopes(original.getScopes());
		probe.setDirected(original.isDirected());

		// copying inherited attributs
		copyOutgoingInheritAttributes(probe, original);

		return probe;
	}

	private Message copyOutgoingMessage(ResolveMessage original) {
		// generate new Resolve Message
		ResolveMessage resolve = new ResolveMessage(original.getHeader().getCommunicationManagerID());

		// copying Header
		copyOutgoingHeader(resolve.getHeader(), original);

		// copy Resolvespecific attributs
		resolve.setEndpointReference(original.getEndpointReference());

		// copying inherited attributs
		copyOutgoingInheritAttributes(resolve, original);

		return resolve;
	}

	private Message copyOutgoingMessage(GetMessage original) {
		// generate new Resolve Message
		GetMessage get = new GetMessage(original.getHeader().getCommunicationManagerID());

		// copying Header
		copyOutgoingHeader(get.getHeader(), original);
		// necessary for DPWS 2006
		get.getHeader().setReplyTo(new EndpointReference(WSAConstants2006.WSA_ANONYMOUS));

		// copying inherited attributs
		copyOutgoingInheritAttributes(get, original);

		return get;
	}

	/**
	 * This method copies all header attributs to the new header.
	 * 
	 * @param header
	 * @param original
	 */
	private void copyOutgoingHeader(SOAPHeader header, Message original) {
		// copying Headerattributs
		header.setAction(new AttributedURI(original.getAction().toString()));
		header.setMessageId(original.getMessageId());

		AttributedURI tmp = original.getRelatesTo();
		if (tmp != null) {
			header.setRelatesTo(new AttributedURI(tmp.toString()));
		}
		EndpointReference epr = original.getReplyTo();
		if (epr != null) {
			header.setReplyTo(new EndpointReference(epr.getAddress()));
		}
		tmp = original.getTo();
		if (tmp != null) {
			header.setTo(new AttributedURI(original.getTo().toString()));
		}
		header.setAppSequence(original.getAppSequence());
		header.setReferenceParameters(original.getHeader().getReferenceParameters());
		header.setSignature(original.getHeader().getSignature());
		header.setValidated(original.getHeader().isValidated());
	}

	/**
	 * This method copies the inherit attributs of every Message (attributs
	 * extends Message.java)
	 * 
	 * @param copy
	 * @param original
	 */
	private void copyOutgoingInheritAttributes(Message copy, Message original) {
		// Change inherited Attributes (from class MESSAGE)
		copy.setInbound(original.isInbound());
		copy.setTargetXAddressInfo(original.getTargetXAddressInfo());
		copy.setSecure(original.isSecure());
		copy.setCertificate(original.getCertificate());
		copy.setPrivateKey(original.getPrivateKey());
	}

	public void changeIncomingProbe(ProbeMessage probeMessage) {
		changeTypesTo2009(probeMessage.getTypes());
	}

	/**
	 * Change the QName:DeviceType of an incoming DPWS 2006 Message to DPWS 2009
	 * 
	 * @param types
	 */
	private void changeTypesTo2009(QNameSet types) {
		// DPWS 2006 Device Type is included
		if (types.contains(DPWSConstants2006.DPWS_QN_DEVICETYPE) && !types.contains(DPWSConstants.DPWS_QN_DEVICETYPE)) {
			// Must be changed to DPWS 2009 Device Type
			types.remove(DPWSConstants2006.DPWS_QN_DEVICETYPE);
			types.add(DPWSConstants.DPWS_QN_DEVICETYPE);
		}
	}

	/**
	 * Change the QName:DeviceType of an outgoing DPWS 2009 Message to DPWS 2006
	 * 
	 * @param types
	 */
	private void changeTypesTo2006(QNameSet types) {
		// DPWS 2009 Device Type is included
		if (types != null && types.contains(DPWSConstants.DPWS_QN_DEVICETYPE) && !types.contains(DPWSConstants2006.DPWS_QN_DEVICETYPE)) {
			// Must be changed to DPWS 2006 Device Type
			types.remove(DPWSConstants.DPWS_QN_DEVICETYPE);
			types.add(DPWSConstants2006.DPWS_QN_DEVICETYPE);
		}
	}
}
