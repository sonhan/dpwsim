/*******************************************************************************
 * Copyright (c) 2009 MATERNA Information & Communications. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html. For further
 * project-related information visit http://www.ws4d.org. The most recent
 * version of the JMEDS framework can be obtained from
 * http://sourceforge.net/projects/ws4d-javame.
 ******************************************************************************/
package org.ws4d.java.io.fs;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * 
 */
public interface FileSystem {

	public String escapeFileName(String rawFileName);

	public InputStream readFile(String filePath) throws IOException;

	public OutputStream writeFile(String filePath) throws IOException;

	public boolean deleteFile(String filePath);

	public boolean renameFile(String filePath, String newFilePath);

	public String[] listFiles(String dirPath);

	public long fileSize(String filePath);

	public String fileSeparator();

	public boolean fileExists(String filePath);
	
	public long lastModified(String filePath);

}
