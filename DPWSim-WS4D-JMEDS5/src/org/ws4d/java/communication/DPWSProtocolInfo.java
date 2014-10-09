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

import org.ws4d.java.configuration.DPWSProperties;
import org.ws4d.java.constants.DPWSConstants;
import org.ws4d.java.constants.DPWSConstants2006;
import org.ws4d.java.structures.HashSet;

/**
 *
 */
public class DPWSProtocolInfo extends ProtocolInfo {

	public static final int	DPWS_VERSION_NOT_SET	= -1;

	private static int		preferredVersion		= DPWS_VERSION_NOT_SET;

	private int				httpRequestChunkedMode;

	private int				httpResponseChunkedMode;

	public static int getPreferredVersion() {
		return preferredVersion;
	}

	public static void setPreferredVersion(int preferredVersion) {
		DPWSProtocolInfo.preferredVersion = preferredVersion;
	}

	public DPWSProtocolInfo() {
		if (preferredVersion == DPWS_VERSION_NOT_SET) {
			HashSet dpwsver = DPWSProperties.getInstance().getSupportedDPWSVersions();
			if (dpwsver.contains(new Integer(DPWSProperties.DEFAULT_DPWS_VERSION))) {
				version = DPWSProperties.DEFAULT_DPWS_VERSION;
			} else if (dpwsver.size() >= 1) {
				version = ((Integer) dpwsver.iterator().next()).intValue();
			}
		} else {
			version = preferredVersion;
		}
		httpRequestChunkedMode = DPWSProperties.getInstance().getHTTPRequestChunkedMode();
		httpResponseChunkedMode = DPWSProperties.getInstance().getHTTPResponseChunkedMode();
	}


	/**
	 * @param dpwsVersion
	 */
	public DPWSProtocolInfo(int dpwsVersion) {
		this.version = dpwsVersion;
		httpRequestChunkedMode = DPWSProperties.getInstance().getHTTPRequestChunkedMode();
		httpResponseChunkedMode = DPWSProperties.getInstance().getHTTPResponseChunkedMode();
	}

	
	private DPWSProtocolInfo(DPWSProtocolInfo other) {
		version = other.version;
		httpRequestChunkedMode = other.httpRequestChunkedMode;
		httpResponseChunkedMode = other.httpResponseChunkedMode;
	}
	
	
	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.communication.ProtocolInfo#getDisplayName()
	 */
	public String getDisplayName() {
		switch (version) {
			case (DPWSConstants.DPWS_VERSION2009):
				return DPWSConstants.DPWS_2009_NAME;
			case (DPWSConstants2006.DPWS_VERSION2006):
				return DPWSConstants2006.DPWS_2006_NAME;
			default:
				return "Unknown DPWS Version";
		}
	}

	public int getHttpRequestChunkedMode() {
		return httpRequestChunkedMode;
	}

	
	public void setHttpRequestChunkedMode(int httpRequestChunkedMode) {
		this.httpRequestChunkedMode = httpRequestChunkedMode;
	}

	
	public int getHttpResponseChunkedMode() {
		return httpResponseChunkedMode;
	}

	
	public void setHttpResponseChunkedMode(int httpResponseChunkedMode) {
		this.httpResponseChunkedMode = httpResponseChunkedMode;
	}

	public void merge(ProtocolInfo pInfo) {
		if (pInfo == null) return;

		if (preferredVersion != -1 && version != preferredVersion && pInfo.getVersion() == preferredVersion) {
			version = preferredVersion;
		} else if (DPWSProperties.DEFAULT_DPWS_VERSION == pInfo.getVersion()) {
			version = pInfo.getVersion();
		}
	}

	public ProtocolInfo newClone() { 
		return new DPWSProtocolInfo(this);
	}

	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + httpRequestChunkedMode;
		result = prime * result + httpResponseChunkedMode;
		return result;
	}

	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!super.equals(obj)) return false;
		if (getClass() != obj.getClass()) return false;
		DPWSProtocolInfo other = (DPWSProtocolInfo) obj;
		if (httpRequestChunkedMode != other.httpRequestChunkedMode) return false;
		if (httpResponseChunkedMode != other.httpResponseChunkedMode) return false;
		return true;
	}
}
