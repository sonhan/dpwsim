package org.ws4d.java.communication;

import org.ws4d.java.communication.connection.ip.IPAddress;
import org.ws4d.java.constants.DPWSConstants;
import org.ws4d.java.constants.HTTPConstants;
import org.ws4d.java.types.URI;

public class DPWSDiscoveryBinding implements DiscoveryBinding {

	public static final IPAddress	DPWS_MCAST_GROUP_IPv4	= new IPAddress(DPWSConstants.DPWS_MCAST_IPv4, false, false, false);

	public static final IPAddress	DPWS_MCAST_GROUP_IPv6	= new IPAddress(DPWSConstants.DPWS_MCAST_IPv6, false, true, false);

	public static final int			IPV6					= 6;

	public static final int			IPV4					= 4;

	private String					iface;

	protected IPAddress				ipAddress				= null;

	protected int					ipVersion				= -1;

	public static final int			DISCOVERY_BINDING		= 2;

	protected int					port					= -1;

	protected URI					transportAddress		= null;

	public DPWSDiscoveryBinding(int ipVersion, String iface) {
		this.ipAddress = ipVersion == 6 ? DPWS_MCAST_GROUP_IPv6 : DPWS_MCAST_GROUP_IPv4;
		this.port = DPWSConstants.DPWS_MCAST_PORT;
		this.iface = iface;
		this.ipVersion = ipVersion;
	}

	public URI getTransportAddress() {
		if (transportAddress == null) {
			transportAddress = new URI(HTTPConstants.HTTP_SCHEMA + "://" + getHostAddress() + ":" + port);
		}
		return transportAddress;
	}

	public String getIface() {
		return iface;
	}

	public String getCommunicationManagerId() {
		return DPWSCommunicationManager.COMMUNICATION_MANAGER_ID;
	}

	public int hashCode() {
		final int prime = 31;
		int result = prime + ((iface == null) ? 0 : iface.hashCode());
		result = prime * result + ipVersion;
		return result;
	}

	public IPAddress getHostAddress() {
		return ipAddress;
	}

	public int getPort() {
		return port;
	}

	public int getType() {
		return DISCOVERY_BINDING;
	}

	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		DPWSDiscoveryBinding other = (DPWSDiscoveryBinding) obj;
		if (iface == null) {
			if (other.iface != null) {
				return false;
			}
		} else if (!iface.equals(other.iface)) {
			return false;
		}
		if (ipVersion != other.ipVersion) {
			return false;
		}
		return true;
	}

	public String toString() {
		return (ipVersion == IPV6 ? "IPv6" : "IPv4") + " - " + iface;
	}

}
