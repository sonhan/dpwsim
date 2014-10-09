package org.ws4d.java.security;

import java.net.Socket;
import java.security.Principal;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;

import javax.net.ssl.X509KeyManager;

/**
 * The ForcedAliasKeyManager uses the next available alias from the keystore
 * with the most similarity to the given alias. E.g. if the given alias is
 * "https://example.device/test/beta" and there is no such alias in the store
 * the alias "example.device/test/beta" will be searched for instead.
 */
public class ForcedAliasKeyManager implements X509KeyManager {

	private X509KeyManager	baseKM;

	private String			alias;

	public ForcedAliasKeyManager(X509KeyManager baseKM, String alias) {
		this.baseKM = baseKM;
		this.alias = alias;
	}

	public String chooseClientAlias(String[] keyType, Principal[] issuers, Socket socket) {
		// For each keyType, call getClientAliases on the base KeyManager
		// to find valid aliases. If our requested alias is found, select it
		// for return.
		boolean aliasFound = false;
		String nearestAlias = alias;

		while (nearestAlias.length() > 1 && !aliasFound) {
			for (int i = 0; i < keyType.length && !aliasFound; i++) {
				String[] validAliases = baseKM.getClientAliases(keyType[i], issuers);
				if (validAliases != null) {
					for (int j = 0; j < validAliases.length && !aliasFound; j++) {
						if (validAliases[j].toLowerCase().equals(nearestAlias.toLowerCase())) aliasFound = true;
					}
				}
			}

			int lastIndex = -1;
			if (aliasFound) nearestAlias = (lastIndex = nearestAlias.lastIndexOf('/')) < 0 ? "" : nearestAlias.substring(lastIndex + 1);
		}
		this.alias = nearestAlias;

		if (aliasFound) {
			return nearestAlias;
		} else
			return null;
	}

	public String chooseServerAlias(String keyType, Principal[] issuers, Socket socket) {
		return baseKM.chooseServerAlias(keyType, issuers, socket);
	}

	public X509Certificate[] getCertificateChain(String arg0) {
		return baseKM.getCertificateChain(arg0);
	}

	public String[] getClientAliases(String keyType, Principal[] issuers) {
		return baseKM.getClientAliases(keyType, issuers);
	}

	public PrivateKey getPrivateKey(String arg0) {
		return baseKM.getPrivateKey(arg0);
	}

	public String[] getServerAliases(String keyType, Principal[] issuers) {
		return baseKM.getServerAliases(keyType, issuers);
	}

}
