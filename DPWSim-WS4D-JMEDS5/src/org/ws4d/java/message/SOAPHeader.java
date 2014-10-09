/*******************************************************************************
 * Copyright (c) 2009 MATERNA Information & Communications. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html. For further
 * project-related information visit http://www.ws4d.org. The most recent
 * version of the JMEDS framework can be obtained from
 * http://sourceforge.net/projects/ws4d-javame.
 ******************************************************************************/
package org.ws4d.java.message;

import org.ws4d.java.DPWSFramework;
import org.ws4d.java.communication.CommunicationManager;
import org.ws4d.java.communication.CommunicationUtil;
import org.ws4d.java.communication.ProtocolInfo;
import org.ws4d.java.configuration.FrameworkProperties;
import org.ws4d.java.constants.ConstantsHelper;
import org.ws4d.java.constants.DPWSMessageConstants;
import org.ws4d.java.constants.MEXConstants;
import org.ws4d.java.constants.WSAConstants;
import org.ws4d.java.constants.WSDConstants;
import org.ws4d.java.constants.WSEConstants;
import org.ws4d.java.constants.WXFConstants;
import org.ws4d.java.types.AppSequence;
import org.ws4d.java.types.AttributedURI;
import org.ws4d.java.types.EndpointReference;
import org.ws4d.java.types.ReferenceParametersMData;
import org.ws4d.java.types.UnknownDataContainer;
import org.ws4d.java.util.IDGenerator;
import org.ws4d.java.util.StringUtil;

/**
 * 
 */
public class SOAPHeader extends UnknownDataContainer {

	
	/*
	 * TODO 13.05.2011:
	 * Remove protocolInfo from SOAPHeader
	 */
	private ProtocolInfo				protocolInfo		= null;

	private AttributedURI				action;

	private AttributedURI				messageId;

	private AttributedURI				relatesTo;

	private EndpointReference			replyTo;

	private AttributedURI				to;

	private AppSequence					appSequence;

	private ReferenceParametersMData	referenceParameters;

	private byte[]						sigVal;

	private boolean						signatureValidated	= true;

	private String						comManID			= CommunicationManager.ID_NULL;

	/**
	 * Returns a new SOAP header having only set its {@link #getAction() action
	 * property} to an {@link AttributedURI} constructed from the specified
	 * String <code>action</code>. Any other fields are empty.
	 * 
	 * @param action the action to set
	 * @return the newly created SOAP header
	 */
	public static SOAPHeader createHeader(String action, String communicatonManagerID) {
		SOAPHeader header = new SOAPHeader(communicatonManagerID);
		header.setAction(new AttributedURI(action));
		return header;
	}

	/**
	 * Returns a new SOAP header having set its {@link #getAction() action
	 * property} to an {@link AttributedURI} constructed from the specified
	 * String <code>action</code>. Additionally, the header's
	 * {@link #getMessageId() message ID property} is set to a new randomly and
	 * uniquely generated UUID URN. Any other fields are empty.
	 * 
	 * @param action the action to set
	 * @return the newly created SOAP header including a message ID
	 * @see #createHeader(String)
	 */
	public static SOAPHeader createRequestHeader(String action, String communicatonManagerID) {
		SOAPHeader header = createHeader(action, communicatonManagerID);
		header.setMessageId(new AttributedURI(IDGenerator.getUUIDasURI()));
		return header;
	}

	/**
	 * 
	 */
	public SOAPHeader(String communicatonManagerID) {
		super();
		comManID = communicatonManagerID;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		StringBuffer sb = new StringBuffer(StringUtil.formatClassName(getClass()));
		sb.append(" [ action=").append(action);
		sb.append(", messageId=").append(messageId);
		sb.append(", relatesTo=").append(relatesTo);
		sb.append(", replyTo=").append(replyTo);
		sb.append(", to=").append(to);
		sb.append(", appSequence=").append(appSequence);
		sb.append(", referenceParameters=").append(referenceParameters);
		if (sigVal != null && DPWSFramework.hasModule(DPWSFramework.SECURITY_MODULE)) {
			sb.append(", Sig=").append(DPWSFramework.getSecurityManager().encode(sigVal));
			sb.append(", Signature Valid=").append(signatureValidated);
		}
		sb.append(" ]");
		return sb.toString();
	}

	/**
	 * Sets the {@link #getRelatesTo() [relationship]}, {@link #getTo() [to]}
	 * and [parameters] properties of this SOAP header to the values of the
	 * {@link #getMessageId() [message ID]} and {@link #getReplyTo() [reply to]}
	 * properties of the passed in request SOAP header.
	 * 
	 * @param requestHeader the SOAP header to extract the source properties
	 *            from
	 */
	public void setResponseTo(SOAPHeader requestHeader) {
		this.relatesTo = requestHeader.messageId;
		EndpointReference replyTo = requestHeader.replyTo;
		/*
		 * if no [reply to] specified, we don't include
		 * WSAConstants.WSA_ANONYMOUS as [to] header property of the response
		 */
		if (replyTo != null) {
			setEndpointReference(replyTo);
		}
	}

	public void setSignature(byte[] sig) {
		sigVal = sig;
	}

	public byte[] getSignature() {
		return sigVal;
	}

	public void setValidated(boolean valid) {
		this.signatureValidated = valid;
	}

	public boolean isValidated() {
		return signatureValidated;
	}

	public AttributedURI getAction() {
		return action;
	}

	public AppSequence getAppSequence() {
		return appSequence;
	}

	public AttributedURI getMessageId() {
		return messageId;
	}

	public ProtocolInfo getProtocolInfo() {
		return protocolInfo;
	}

	public AttributedURI getRelatesTo() {
		return relatesTo;
	}

	public EndpointReference getReplyTo() {
		return replyTo;
	}

	public AttributedURI getTo() {
		return to;
	}

	public String getWseIdentifier() {
		return referenceParameters == null ? null : referenceParameters.getWseIdentifier();
	}

	public int getDPWSMessageType() {
		if (action == null) {
			return DPWSMessageConstants.UNKNOWN_MESSAGE;
		}

		CommunicationManager comMan = DPWSFramework.getCommunicationManager(getCommunicationManagerID());
		CommunicationUtil comUtil = comMan.getCommunicationUtil();

		ConstantsHelper helper = comUtil.getHelper(protocolInfo.getVersion());

		String actionString = action.toString();
		if (helper.getWSDActionHello().equals(actionString)) {
			return DPWSMessageConstants.HELLO_MESSAGE;
		}
		if (helper.getWSDActionBye().equals(actionString)) {
			return DPWSMessageConstants.BYE_MESSAGE;
		}
		if (helper.getWSDActionProbe().equals(actionString)) {
			return DPWSMessageConstants.PROBE_MESSAGE;
		}
		if (helper.getWSDActionProbeMatches().equals(actionString)) {
			return DPWSMessageConstants.PROBE_MATCHES_MESSAGE;
		}
		if (helper.getWSDActionResolve().equals(actionString)) {
			return DPWSMessageConstants.RESOLVE_MESSAGE;
		}
		if (helper.getWSDActionResolveMatches().equals(actionString)) {
			return DPWSMessageConstants.RESOLVE_MATCHES_MESSAGE;
		}
		if (WSEConstants.WSE_ACTION_GETSTATUS.equals(actionString)) {
			return DPWSMessageConstants.GET_STATUS_MESSAGE;
		}
		if (WSEConstants.WSE_ACTION_GETSTATUSRESPONSE.equals(actionString)) {
			return DPWSMessageConstants.GET_STATUS_RESPONSE_MESSAGE;
		}
		if (WSEConstants.WSE_ACTION_RENEW.equals(actionString)) {
			return DPWSMessageConstants.RENEW_MESSAGE;
		}
		if (WSEConstants.WSE_ACTION_RENEWRESPONSE.equals(actionString)) {
			return DPWSMessageConstants.RENEW_RESPONSE_MESSAGE;
		}
		if (WSEConstants.WSE_ACTION_SUBSCRIBE.equals(actionString)) {
			return DPWSMessageConstants.SUBSCRIBE_MESSAGE;
		}
		if (WSEConstants.WSE_ACTION_SUBSCRIBERESPONSE.equals(actionString)) {
			return DPWSMessageConstants.SUBSCRIBE_RESPONSE_MESSAGE;
		}
		if (WSEConstants.WSE_ACTION_SUBSCRIPTIONEND.equals(actionString)) {
			return DPWSMessageConstants.SUBSCRIPTION_END_MESSAGE;
		}
		if (WSEConstants.WSE_ACTION_UNSUBSCRIBE.equals(actionString)) {
			return DPWSMessageConstants.UNSUBSCRIBE_MESSAGE;
		}
		if (WSEConstants.WSE_ACTION_UNSUBSCRIBERESPONSE.equals(actionString)) {
			return DPWSMessageConstants.UNSUBSCRIBE_RESPONSE_MESSAGE;
		}
		if (WXFConstants.WXF_ACTION_GET.equals(actionString)) {
			return DPWSMessageConstants.GET_MESSAGE;
		}
		if (WXFConstants.WXF_ACTION_GETRESPONSE.equals(actionString)) {
			return DPWSMessageConstants.GET_RESPONSE_MESSAGE;
		}
		if (MEXConstants.WSX_ACTION_GETMETADATA_REQUEST.equals(actionString)) {
			return DPWSMessageConstants.GET_METADATA_MESSAGE;
		}
		if (MEXConstants.WSX_ACTION_GETMETADATA_RESPONSE.equals(actionString)) {
			return DPWSMessageConstants.GET_METADATA_RESPONSE_MESSAGE;
		}
		if (WSAConstants.WSA_ACTION_ADDRESSING_FAULT.equals(actionString) || helper.getDPWSActionFault().equals(actionString) || WSAConstants.WSA_ACTION_SOAP_FAULT.equals(actionString) || WSDConstants.WSD_ACTION_WSD_FAULT.equals(actionString)) {
			return DPWSMessageConstants.FAULT_MESSAGE;
		}
		return DPWSMessageConstants.INVOKE_MESSAGE;
	}

	public ReferenceParametersMData getReferenceParameters() {
		return referenceParameters;
	}

	/**
	 * Sets the {@link #getTo() to header property} to the value of the
	 * {@link EndpointReference#getAddress() address property} of the specified
	 * endpoint reference and copies any contained
	 * {@link EndpointReference#getReferenceParameters() reference parameters}
	 * into this SOAP header instance (see {@link #getReferenceParameters()}).
	 * 
	 * @param ref the endpoint reference to set
	 */
	public void setEndpointReference(EndpointReference ref) {
		to = ref.getAddress();
		referenceParameters = ref.getReferenceParameters();
	}

	/**
	 * @param action the action to set
	 */
	public void setAction(AttributedURI action) {
		this.action = action;
	}

	/**
	 * @param messageId the messageId to set
	 */
	public void setMessageId(AttributedURI messageId) {
		this.messageId = messageId;
	}

	/**
	 * @param relatesTo the relatesTo to set
	 */
	public void setRelatesTo(AttributedURI relatesTo) {
		this.relatesTo = relatesTo;
	}

	/**
	 * @param replyTo the replyTo to set
	 */
	public void setReplyTo(EndpointReference replyTo) {
		this.replyTo = replyTo;
	}

	/**
	 * @param to the to to set
	 */
	public void setTo(AttributedURI to) {
		this.to = to;
		if (!FrameworkProperties.REFERENCE_PARAM_MODE && to.getFragment() != null) {
			setWseIdentifier(to.getFragment());
		}
	}

	/**
	 * @param appSequence the appSequence to set
	 */
	public void setAppSequence(AppSequence appSequence) {
		this.appSequence = appSequence;
	}

	/**
	 * @param wseIdentifier the wseIdentifier to set
	 */
	public void setWseIdentifier(String wseIdentifier) {
		if (referenceParameters == null) {
			referenceParameters = new ReferenceParametersMData();
		}
		referenceParameters.setWseIdentifier(wseIdentifier);
	}

	public void setProtocolInfo(ProtocolInfo info) {
		this.protocolInfo = info;
	}

	public void setReferenceParameters(ReferenceParametersMData data) {
		this.referenceParameters = data;
	}

	public String getCommunicationManagerID() {
		return comManID;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((messageId == null) ? 0 : messageId.hashCode());
		result = prime * result + ((protocolInfo == null) ? 0 : protocolInfo.hashCode());
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		SOAPHeader other = (SOAPHeader) obj;
		if (messageId == null) {
			if (other.messageId != null) return false;
		} else if (!messageId.equals(other.messageId)) return false;
		if (protocolInfo == null) {
			if (other.protocolInfo != null) return false;
		} else if (!protocolInfo.equals(other.protocolInfo)) return false;
		return true;
	}
	
	

}
