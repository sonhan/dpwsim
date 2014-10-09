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
import org.ws4d.java.constants.ConstantsHelper;
import org.ws4d.java.constants.SOAPConstants;
import org.ws4d.java.constants.WSAConstants;
import org.ws4d.java.constants.WSDConstants;
import org.ws4d.java.constants.WSSecurityConstants;
import org.ws4d.java.schema.PredefinedSchemaTypes;
import org.ws4d.java.service.parameter.ParameterValue;
import org.ws4d.java.service.parameter.StringValue;
import org.ws4d.java.structures.ArrayList;
import org.ws4d.java.structures.DataStructure;
import org.ws4d.java.types.LocalizedString;
import org.ws4d.java.types.QName;
import org.ws4d.java.util.StringUtil;

/**
 *
 */
public class FaultMessage extends Message {

	public static final String	ACTION_ADDRESSING	= WSAConstants.WSA_ACTION_ADDRESSING_FAULT;

	public static final String	ACTION_SOAP			= WSAConstants.WSA_ACTION_SOAP_FAULT;

	public static final String	ACTION_WSD			= WSDConstants.WSD_ACTION_WSD_FAULT;

	private QName				code;

	private QName				subcode;

	private QName				subsubcode;

	private DataStructure		reason;

	private ParameterValue		detail;

	public static FaultMessage createActionNotSupportedFault(Message request) {
		String communicationManagerId = request.getHeader().getCommunicationManagerID();
		CommunicationManager comMan = DPWSFramework.getCommunicationManager(request.getHeader().getCommunicationManagerID());
		CommunicationUtil comUtil = comMan.getCommunicationUtil();
		ConstantsHelper helper = comUtil.getHelper(request.getProtocolInfo().getVersion());

		String inputAction = request.getAction().toString();
		/*
		 * create a SOAP Exception with code Sender and Subcode
		 * wsa:ActionNotSupported
		 */
		FaultMessage fault = new FaultMessage(helper.getWSAActionAddressingFault(), communicationManagerId);
		fault.setResponseTo(request);
		fault.setCode(SOAPConstants.SOAP_FAULT_SENDER);
		// fill in subcode, reason and detail
		fault.setSubcode(helper.getWSAFaultActionNotSupported());
		LocalizedString reason = new LocalizedString("The endpoint at the specified address " + request.getTo() + " doesn't support the requested action " + inputAction + ".", null);
		fault.addReason(reason);

		ParameterValue detail = ParameterValue.createElementValue(PredefinedSchemaTypes.WSA_PROBLEM_ACTION);
		ParameterValue action = detail.get(WSAConstants.WSA_ELEM_ACTION);
		if (detail.getValueType() == ParameterValue.TYPE_STRING) {
			StringValue value = (StringValue) action;
			value.set(inputAction);
		}
		fault.setDetail(detail);
		return fault;
	}

	public static FaultMessage createEndpointUnavailableFault(Message message) {
		String communicationManagerId = message.getHeader().getCommunicationManagerID();
		CommunicationManager comMan = DPWSFramework.getCommunicationManager(message.getHeader().getCommunicationManagerID());
		CommunicationUtil comUtil = comMan.getCommunicationUtil();
		ConstantsHelper helper = comUtil.getHelper(message.getProtocolInfo().getVersion());

		FaultMessage fault = new FaultMessage(helper.getWSAActionAddressingFault(), communicationManagerId);
		fault.setResponseTo(message);

		// send Fault wsa:EndpointUnavailable
		fault.setCode(SOAPConstants.SOAP_FAULT_RECEIVER);
		fault.setSubcode(helper.getWSAfaultEndpointUnavailable());
		LocalizedString reason = new LocalizedString("The endpoint at the specified address " + message.getTo() + " is unable to process the message at this time.", null);
		fault.addReason(reason);
		return fault;
	}

	public static FaultMessage createMessageAddressingHeaderRequired(String communicationManagerId) {
		FaultMessage fault = new FaultMessage(WSAConstants.WSA_ACTION_ADDRESSING_FAULT, communicationManagerId);

		// send Fault wsa:MessageAddressingHeaderRequired
		fault.setCode(SOAPConstants.SOAP_FAULT_SENDER);
		fault.setSubcode(WSAConstants.WSA_FAULT_MESSAGE_ADDRESSING_HEADER_REQUIRED);
		LocalizedString reason = new LocalizedString("A required header representing a Message Addressing Property is not present", null);
		fault.addReason(reason);

		return fault;
	}

	public static FaultMessage createAuthenticationFault(Message message) {
		String communicationManagerId = message.getHeader().getCommunicationManagerID();

		FaultMessage fault = new FaultMessage(WSSecurityConstants.WS_SECURITY, communicationManagerId);
		fault.setResponseTo(message);

		fault.setCode(SOAPConstants.SOAP_FAULT_SENDER);
		fault.setSubcode(WSSecurityConstants.WSSE_FAULT_AUTHENTICATION);
		LocalizedString reason = new LocalizedString("The security token could not be authenticated or authorized.", null);
		fault.addReason(reason);
		return fault;
	}

	/**
	 * Crates a new fault message with the given <code>action</code>, which is
	 * expected to be a valid absolute URI.
	 * 
	 * @param action the action URI of the fault message
	 */
	public FaultMessage(String action, String communicationManagerId) {
		this(SOAPHeader.createHeader(action, communicationManagerId));
	}

	/**
	 * @param header
	 */
	public FaultMessage(SOAPHeader header) {
		this(header, null, null);
	}

	/**
	 * @param header
	 * @param code
	 * @param subcode
	 */
	public FaultMessage(SOAPHeader header, QName code, QName subcode) {
		super(header);
		this.code = code;
		this.subcode = subcode;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		StringBuffer sb = new StringBuffer(StringUtil.formatClassName(getClass()));
		sb.append(" [ header=").append(header);
		sb.append(", inbound=").append(inbound);
		sb.append(", code=").append(code);
		sb.append(", subcode=").append(subcode);
		sb.append(", subsubcode=").append(subsubcode);
		sb.append(", reason=").append(reason);
		sb.append(", detail=").append(detail);
		sb.append(" ]");
		return sb.toString();
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.message.DPWSMessage#getType()
	 */
	public int getType() {
		return FAULT_MESSAGE;
	}

	/**
	 * Returns the SOAP fault code.
	 * 
	 * @return the SOAP fault code
	 */
	public QName getCode() {
		return code;
	}

	/**
	 * Returns the SOAP fault subcode.
	 * 
	 * @return the SOAP fault subcode
	 */
	public QName getSubcode() {
		return subcode;
	}

	/**
	 * Returns the SOAP fault subsubcode.
	 * 
	 * @return the SOAP fault subsubcode
	 */
	public QName getSubsubcode() {
		return subsubcode;
	}

	/**
	 * Returns the list of reasons.
	 * 
	 * @return the list of reasons
	 */
	// list of LocalizedStrings
	public DataStructure getReason() {
		return reason;
	}

	/**
	 * Returns the SOAP fault detail.
	 * 
	 * @return the SOAP fault detail
	 */
	public ParameterValue getDetail() {
		return detail;
	}

	/**
	 * @param code the code to set
	 */
	public void setCode(QName code) {
		this.code = code;
	}

	/**
	 * @param subcode the subcode to set
	 */
	public void setSubcode(QName subcode) {
		this.subcode = subcode;
	}

	/**
	 * @param subsubcode the subsubcode to set
	 */
	public void setSubsubcode(QName subsubcode) {
		this.subsubcode = subsubcode;
	}

	/**
	 * @param reason the reason to set
	 */
	public void setReason(DataStructure reason) {
		this.reason = reason;
	}

	/**
	 * @param detail the detail to set
	 */
	public void setDetail(ParameterValue detail) {
		this.detail = detail;
	}

	public void addReason(LocalizedString reason) {
		if (this.reason == null) {
			this.reason = new ArrayList();
		}
		this.reason.add(reason);
	}
}
