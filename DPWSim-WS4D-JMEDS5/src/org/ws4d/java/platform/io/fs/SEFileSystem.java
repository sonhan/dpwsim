/*******************************************************************************
 * Copyright (c) 2009 MATERNA Information & Communications. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html. For further
 * project-related information visit http://www.ws4d.org. The most recent
 * version of the JMEDS framework can be obtained from
 * http://sourceforge.net/projects/ws4d-javame.
 ******************************************************************************/
package org.ws4d.java.platform.io.fs;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.ws4d.java.io.fs.FileSystem;

/**
 * 
 */
public class SEFileSystem implements FileSystem {

	private static boolean deleteRecursively(File dir) {
		boolean result = false;
		File[] subfiles = dir.listFiles();
		for (int i = 0; i < subfiles.length; i++) {
			File f = subfiles[i];
			if (f.isFile()) {
				result = f.delete();
			} else if (f.isDirectory()) {
				result = deleteRecursively(f);
			} else {
				result = false;
			}
			if (!result) {
				return false;
			}
		}
		return dir.delete();
	}

	/**
	 * 
	 */
	public SEFileSystem() {
		super();
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.io.fs.FileSystem#escapeFileName(java.lang.String)
	 */
	public String escapeFileName(String rawFileName) {
		if (rawFileName == null) {
			return "";
		}
		StringBuffer sb = new StringBuffer();
		int len = rawFileName.length();
		for (int i = 0; i < len; i++) {
			char c = rawFileName.charAt(i);
			switch (c) {
				case ('/'):
				case ('\\'):
				case (':'):
				case ('*'):
				case ('?'):
				case ('"'):
				case ('<'):
				case ('>'):
				case ('|'): {
					sb.append('_');
					break;
				}
				default: {
					sb.append(c);
					break;
				}
			}
		}
		return sb.toString();
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.io.fs.FileSystem#deleteFile(java.lang.String)
	 */
	public boolean deleteFile(String filePath) {
		if (filePath == null) {
			return false;
		}
		File f = new File(filePath);
		if (f.isFile()) {
			return f.delete();
		} else if (f.isDirectory()) {
			return deleteRecursively(f);
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.io.fs.FileSystem#fileSeparator()
	 */
	public String fileSeparator() {
		return File.separator;
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.io.fs.FileSystem#listFiles(java.lang.String)
	 */
	public String[] listFiles(String dirPath) {
		return dirPath == null ? null : new File(dirPath).list();
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.io.fs.FileSystem#readFile(java.lang.String)
	 */
	public InputStream readFile(String filePath) throws IOException {
		String s = System.getProperty("user.dir");
		return filePath == null ? null : new FileInputStream(filePath);
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.io.fs.FileSystem#writeFile(java.lang.String)
	 */
	public OutputStream writeFile(String filePath) throws IOException {
		if (filePath == null) {
			throw new FileNotFoundException("File name not set.");
		}
		File file = new File(filePath);
		if (!file.exists()) {
			File dir = file.getParentFile();
			if (dir != null && !(dir.exists() || dir.mkdirs())) {
				throw new IOException("unable to create parent directory " + dir);
			}
		}
		return new FileOutputStream(file);
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.io.fs.FileSystem#renameFile(java.lang.String,
	 * java.lang.String)
	 */
	public boolean renameFile(String filePath, String newFilePath) {
		if (filePath == null || newFilePath == null) {
			return false;
		}
		return new File(filePath).renameTo(new File(newFilePath));
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.io.fs.FileSystem#fileSize(java.lang.String)
	 */
	public long fileSize(String filePath) {
		return new File(filePath).length();
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.io.fs.FileSystem#fileExists(java.lang.String)
	 */
	public boolean fileExists(String filePath) {
		return new File(filePath).exists();
	}

	/* (non-Javadoc)
	 * @see org.ws4d.java.io.fs.FileSystem#lastModified(java.lang.String)
	 */
	public long lastModified(String filePath) {
		if (filePath == null) {
			return -1;
		}
		File f = new File(filePath);
		return f.lastModified();
	}

}
