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

import org.ws4d.java.message.Message;

/**
 * Simple message selector implementation, which matches every message
 * regardless of its concrete type or other message properties.
 */
public class AllMessageSelector implements MessageSelector {

	/**
	 * A singleton instance of this class.
	 */
	public static final MessageSelector	INSTANCE	= new AllMessageSelector();

	/**
	 * Always returns <code>true</code> regardless of the type or contents of
	 * the given message.
	 * 
	 * @param msg the message to decide upon
	 */
	public boolean matches(Message msg) {
		return true;
	}

}
