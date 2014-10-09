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

import org.ws4d.java.constants.SOAPConstants;
import org.ws4d.java.constants.WS4DConstants;
import org.ws4d.java.message.FaultMessage;
import org.ws4d.java.service.parameter.ParameterValue;
import org.ws4d.java.structures.ArrayList;
import org.ws4d.java.structures.DataStructure;
import org.ws4d.java.types.LocalizedString;
import org.ws4d.java.types.QName;

/**
 * Exceptions of this class are thrown to indicate the occurrence of a
 * user-defined fault during the invocation of a {@link Operation operation} or
 * the dispatching of a {@link DefaultEventSource}. The state encapsulated
 * within such an exception allows a caller to determine the
 * {@link #getAction() type} of fault and extract any additional
 * {@link #getDetail() user-defined data} attached to it.
 */
public class InvocationException extends Exception {

	// for backward compatibility
	private static final long		serialVersionUID		= 6799847495814513813L;

	private static final String		DECLARED_FAULT_REASON	= "A declared fault occured during invocation: ";

	private final String			action;

	private final QName				code;

	private final QName				subcode;

	private final DataStructure		reason;

	private final ParameterValue	detail;

	protected static DataStructure createReasonFromString(String reason) {
		if (reason == null) {
			return null;
		}
		DataStructure result = new ArrayList(1);
		result.add(new LocalizedString(reason, null));
		return result;
	}

	/**
	 * Creates a new instance wrapping the specified <code>fault</code>.
	 * 
	 * @param fault the fault to encapsulate
	 */
	public InvocationException(Fault fault) {
		this(fault, null);
	}

	/**
	 * Creates a new instance representing the specified <code>fault</code> and
	 * carrying the given user-defined data <code>params</code> as additional
	 * fault details.
	 * 
	 * @param fault the fault this exception represents
	 * @param params user-defined additional data about the fault
	 */
	public InvocationException(Fault fault, ParameterValue params) {
		this(fault, DECLARED_FAULT_REASON + fault.getName(), params);
	}

	/**
	 * Creates a new instance representing the specified <code>fault</code> with
	 * the given reason and carrying the user-defined data <code>params</code>
	 * as additional fault details.
	 * 
	 * @param fault the fault this exception represents
	 * @param reason the fault reason
	 * @param params user-defined additional data about the fault
	 */
	public InvocationException(Fault fault, String reason, ParameterValue params) {
		this(fault.getAction(), SOAPConstants.SOAP_FAULT_SENDER, WS4DConstants.WS4D_FAULT_DECLARED, createReasonFromString(reason), params);
	}

	/**
	 * Creates a new instance wrapping the specified fault message.
	 * 
	 * @param fault the message to extract fault information from
	 */
	public InvocationException(FaultMessage fault) {
		this(fault.getAction().toString(), fault.getCode(), fault.getSubcode(), fault.getReason(), fault.getDetail());
	}

	/**
	 * Create a new instance from the specified arguments.
	 * 
	 * @param action the wsa:Action associated to the fault
	 * @param code the SOAP code of the fault
	 * @param subcode the SOAP subcode of the fault
	 * @param reason the fualt's reason, a data structure of
	 *            {@link LocalizedString} , each for a different language or
	 *            locale instances
	 * @param detail additional user-defined data further describing the fault
	 */
	protected InvocationException(String action, QName code, QName subcode, DataStructure reason, ParameterValue detail) {
		super();
		this.action = action;
		this.code = code;
		this.subcode = subcode;
		this.reason = reason;
		this.detail = detail;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		StringBuffer sb = new StringBuffer(getClass().getName());
		sb.append(": [ action=").append(action);
		sb.append(", code=").append(code);
		sb.append(", subcode=").append(subcode);
		sb.append(", reason=").append(reason);
		sb.append(", detail=").append(detail);
		sb.append(" ]");
		return sb.toString();
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Throwable#getMessage()
	 */
	public String getMessage() {
		return toString();
	}

	/**
	 * Returns the <em>wsa:Action</em> URI of the fault this exception refers
	 * to.
	 * 
	 * @return the action associated to the fault wrapped by this exception
	 */
	public String getAction() {
		return action;
	}

	/**
	 * Returns the SOAP code of the fault this exception refers to.
	 * 
	 * @return the SOAP code of the wrapped fault
	 */
	public QName getCode() {
		return code;
	}

	/**
	 * Returns the SOAP subcode of the fault this exception refers to.
	 * 
	 * @return the SOAP subcode of the wrapped fault
	 */
	public QName getSubcode() {
		return subcode;
	}

	/**
	 * Returns a data structure of {@link LocalizedString} instances, each in a
	 * different language/locale, representing the textual reason of the fault
	 * this exception refers to.
	 * 
	 * @return the localized reasons for the fault
	 */
	public DataStructure getReason() {
		return reason;
	}

	/**
	 * Returns user-defined additional data about the encapsulated fault.
	 * 
	 * @return the additional fault-related data
	 */
	public ParameterValue getDetail() {
		return detail;
	}

}
