/*******************************************************************************
 * Copyright (c) 2009 MATERNA Information & Communications. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html. For further
 * project-related information visit http://www.ws4d.org. The most recent
 * version of the JMEDS framework can be obtained from
 * http://sourceforge.net/projects/ws4d-javame.
 ******************************************************************************/
package org.ws4d.java.configuration;

public class SecurityProperties implements PropertiesHandler {

	private static SecurityProperties	handler					= null;

	public static final String[]		SECTION_SECURITY		= { "Security" };

	public static final PropertyHeader	HEADER_SECTION_SECURITY	= new PropertyHeader(SECTION_SECURITY);

	public static final String			KEYSTORE_FILE			= "KeyStoreFile";

	public static final String			KEYSTORE_PASWD			= "KeyStorePswd";

	public static final String			TRUSTSTORE_FILE			= "TrustStoreFile";

	public static final String			TRUSTSTORE_PASWD		= "TrustStorePswd";

	public static final String			ALLOW_SECURE_ONLY		= "AllowSecureOnly";

	/**
	 * the path to the java keystore file. To create this please read +for
	 * windows:
	 * http://java.sun.com/j2se/1.4.2/docs/tooldocs/windows/keytool.html +for
	 * solaris and linux:
	 * http://java.sun.com/j2se/1.5.0/docs/tooldocs/solaris/keytool.html
	 */
	private String						keyStoreFileName		= null;

	private String						keyStorePaswd			= null;

	private String						trustStorePath			= null;

	private String						trustStorePasswd		= null;

	private boolean						allowSecureOnly			= true;

	SecurityProperties() {
		super();
		if (handler != null) {
			throw new RuntimeException("SecurityPropertiesProperties: class already instantiated!");
		}
		handler = this;
	}

	public static SecurityProperties getInstance() {
		if (handler == null) handler = new SecurityProperties();

		return handler;
	}

	public void finishedSection(int depth) {}

	public void setProperties(PropertyHeader header, Property property) {
		if (KEYSTORE_FILE.equals(property.key))
			keyStoreFileName = property.value;
		else if (KEYSTORE_PASWD.equals(property.key))
			keyStorePaswd = property.value;
		else if (TRUSTSTORE_FILE.equals(property.key))
			trustStorePath = property.value;
		else if (TRUSTSTORE_PASWD.equals(property.key))
			trustStorePasswd = property.value;
		else if (ALLOW_SECURE_ONLY.equals(property.key)) {
			allowSecureOnly = (property.value.equals("true")) ? true : false;
		}
	}

	public String getKeyStoreFilePath() {
		return keyStoreFileName;
	}

	public String getKeyStorePswd() {
		return keyStorePaswd;
	}

	public String getTrustStorePath() {
		return trustStorePath;
	}

	public String getTrustStorePasswd() {
		return trustStorePasswd;
	}

	public void setKeyStoreFileName(String keyStoreFileName) {
		this.keyStoreFileName = keyStoreFileName;
	}

	public void setKeyStorePaswd(String keyStorePaswd) {
		this.keyStorePaswd = keyStorePaswd;
	}

	public void setTrustStorePath(String trustStorePath) {
		this.trustStorePath = trustStorePath;
	}

	public void setTrustStorePasswd(String trustStorePasswd) {
		this.trustStorePasswd = trustStorePasswd;
	}
}
