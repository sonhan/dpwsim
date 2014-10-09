/*******************************************************************************
 * Copyright (c) 2009 MATERNA Information & Communications. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html. For further
 * project-related information visit http://www.ws4d.org. The most recent
 * version of the JMEDS framework can be obtained from
 * http://sourceforge.net/projects/ws4d-javame.
 ******************************************************************************/
package org.ws4d.java.communication;

import org.ws4d.java.constants.ConstantsHelper;
import org.ws4d.java.message.Message;
import org.ws4d.java.message.discovery.ProbeMessage;

public interface CommunicationUtil {

	public ConstantsHelper getHelper(int version);

	public Message changeOutgoingMessage(int version, Message message);

	public Message copyOutgoingMessage(Message message);

	public void changeIncomingProbe(ProbeMessage probeMessage);

}
