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

import org.ws4d.java.structures.LinkedList;
import org.ws4d.java.structures.List;

/**
 * Class for handling HTTP user requests within the authentication. 
 *
 */
public class HTTPGroup {
	
	private String name;

	private List userList;
	
	public HTTPGroup(){
		this.name = "defaultGroup";
		this.userList = new LinkedList();
	}
	
	public HTTPGroup(String name){
		this.name = name;
		this.userList = new LinkedList();
	}

	/**
	 * Adds one HTTPUser to this group. 
	 * @param user The HTTPUser to add. 
	 */
	public void addUser(HTTPUser user){
		this.userList.add(user);
	}
	
	/**
	 * Removes one HTTPUser from this group.
	 * @param user The HTTPUser to remove. 
	 */
	public void removeUser(HTTPUser user){
		this.userList.remove(user);
	}

	/**
	 * Returns the group name.
	 * @return Group name. 
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Returns a list of all users in this group.
	 * @return List of HTTPUsers.
	 */
	public List getUser(){
		return userList;
	}
	
	/**
	 * Checks if an user belongs to this group.
	 * @param user The user to check.
	 * @return <code>true</code> if the user belongs to this group, <code>false</code> otherwise.
	 */
	public boolean inList(HTTPUser user){
		HTTPUser listUser;
		for (int i = 0; i < getUser().size(); i++){
			listUser = (HTTPUser) getUser().get(i);
			if (listUser.equals(user)) {
				return true;
			} 
		}
		return false;
	}
	
	public String toString() {
		StringBuffer sBuf = new StringBuffer();
		sBuf.append("Group [ name: ");
		sBuf.append(name);
		sBuf.append("; ");
		sBuf.append("Userlist: ");
		sBuf.append(userList);
		sBuf.append(" ]");
		return sBuf.toString();
	}

}
