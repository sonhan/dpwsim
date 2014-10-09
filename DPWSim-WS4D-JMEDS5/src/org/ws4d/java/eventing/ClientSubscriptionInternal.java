/*******************************************************************************
 * Copyright (c) 2009 MATERNA Information & Communications. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html. For further
 * project-related information visit http://www.ws4d.org. The most recent
 * version of the JMEDS framework can be obtained from
 * http://sourceforge.net/projects/ws4d-javame.
 ******************************************************************************/
package org.ws4d.java.eventing;

/**
 *
 */
public interface ClientSubscriptionInternal extends ClientSubscription {

	/**
	 * Called after a renew on this client subscription.
	 * 
	 * @param newDuration the new duration
	 */
	public void renewInternal(long newDuration);

	/**
	 * Performs final clean-up on this client subscription instance. After a
	 * call to this method, the subscription will be invalid.
	 * <p>
	 * <strong>Warning!</strong> This method is not intended to be called from
	 * application code! It is used internally when a subscription instance is
	 * not needed anymore.
	 */
	public void dispose();

}
