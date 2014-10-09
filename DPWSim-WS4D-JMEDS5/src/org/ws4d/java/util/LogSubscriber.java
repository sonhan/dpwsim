/*******************************************************************************
 * Copyright (c) 2009 MATERNA Information & Communications. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html. For further
 * project-related information visit http://www.ws4d.org. The most recent
 * version of the JMEDS framework can be obtained from
 * http://sourceforge.net/projects/ws4d-javame.
 ******************************************************************************/

package org.ws4d.java.util;

/**
 * Classes which want to be notified about new debug messages must implement
 * this interface and fill the notify() method.
 */
public interface LogSubscriber {

	/**
	 * This method is called by Log when you are on the debug subscription list.
	 * 
	 * @param message new debug message.
	 * @see org.ws4d.java.util.Log
	 */
	void notify(String message);
}
