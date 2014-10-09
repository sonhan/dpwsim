/*******************************************************************************
 * Copyright (c) 2009 MATERNA Information & Communications. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html. For further
 * project-related information visit http://www.ws4d.org. The most recent
 * version of the JMEDS framework can be obtained from
 * http://sourceforge.net/projects/ws4d-javame.
 ******************************************************************************/
package org.ws4d.java.communication.protocol.http;

import org.ws4d.java.security.Base64Util;
import org.ws4d.java.util.StringUtil;

/**
 * Class for handling HTTP user requests within the authentication.
 *
 */
public class HTTPUser {

	private String	username;

	private String	password;

	public HTTPUser(String username, String password) {
		this.username = username;
		this.password = password;
	}

	/**
	 * Returns the user name.
	 * @return User name.
	 */
	public String getUsername () {
		return username;
	}

	/**
	 * Returns the password.
	 * @return Password.
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * Creates a HTTPUser object form a given base64 string.
	 * @param base64User The base64 data.
	 * @return A new HTTPUser object.
	 */
	public static HTTPUser createUserFromBase64String(String base64User) {
		String[] s = StringUtil.split(new String(Base64Util.decode(base64User)), ':');
		return new HTTPUser(s[0], s[1]);
	}

	/**
	 * Compares two HTTPUsers.
	 * @param user HTTPUser to compare this one to.
	 * @return <code>true</code> if it is the same user, <code>false</code> otherwise.
	 */
	public boolean equals(HTTPUser user) {
		return ((getUsername() != null && getPassword() != null) && (getUsername().equals(user.getUsername())) && (getPassword().equals(user.getPassword())));
	}

	public String toString() {
		StringBuffer sBuf = new StringBuffer();
		sBuf.append("User [ ");
		sBuf.append("username: ");
		sBuf.append(getUsername());
		sBuf.append(" ]");
		return sBuf.toString();
	}

}
