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
 * A <code>MessageSlector</code>'s purpose is to decide whether a
 * {@link MessageListener} shall be informed about a given message or not. This
 * decision is made within the {@link #matches(Message)} method.
 */
public interface MessageSelector {

	/**
	 * Decides whether the given message meets the selection criteria of this
	 * message selector instance.
	 * 
	 * @param msg the message to decide upon
	 * @return <code>true</code>, if the configured selection rules accept the
	 *         message, <code>false</code> otherwise
	 */
	public boolean matches(Message msg);

}
