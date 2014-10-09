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

import org.ws4d.java.service.parameter.ParameterValue;

/**
 * Instances of this interface can be used within an {@link OperationStub} to
 * implement the actual business logic of a service's operation.
 */
public interface InvokeDelegate {

	/**
	 * Executes the business logic associated with the given
	 * <code>operation</code>. The values of any available input parameters can
	 * be obtained from the supplied <code>arguments</code>. The returned value
	 * corresponds to the result of the operation. In case any
	 * {@link Operation#getFaults() predefined faults} arise during the
	 * execution of the business logic, an {@link InvocationException} should be
	 * thrown.
	 * 
	 * @param operation the operation invoked
	 * @param arguments input arguments
	 * @return output/result of the invocation
	 * @throws InvocationException denotes failures occurring during the
	 *             execution of the business logic
	 */
	public ParameterValue invoke(Operation operation, ParameterValue arguments) throws InvocationException;

}
