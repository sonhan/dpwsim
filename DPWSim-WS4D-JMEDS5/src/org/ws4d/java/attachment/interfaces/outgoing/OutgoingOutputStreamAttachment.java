/*******************************************************************************
 * Copyright (c) 2009 MATERNA Information & Communications. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html. For further
 * project-related information visit http://www.ws4d.org. The most recent
 * version of the JMEDS framework can be obtained from
 * http://sourceforge.net/projects/ws4d-javame.
 ******************************************************************************/
package org.ws4d.java.attachment.interfaces.outgoing;

import java.io.OutputStream;

import org.ws4d.java.attachment.OutgoingAttachment;

public interface OutgoingOutputStreamAttachment extends OutgoingAttachment {

	/**
	 * Returns an ouput stream which allows to write the stream data.
	 * <p>
	 * The stream MUST be closed if the streaming ends and the communication
	 * should continue correctly!
	 * </p>
	 * 
	 * @return the output stream.
	 */
	public OutputStream getOutputStream();

}
