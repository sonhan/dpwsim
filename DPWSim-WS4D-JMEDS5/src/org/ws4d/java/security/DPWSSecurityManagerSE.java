/*******************************************************************************
 * Copyright (c) 2009 MATERNA Information & Communications. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html. For further
 * project-related information visit http://www.ws4d.org. The most recent
 * version of the JMEDS framework can be obtained from
 * http://sourceforge.net/projects/ws4d-javame.
 ******************************************************************************/
package org.ws4d.java.security;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

import org.ws4d.java.communication.CommunicationBinding;
import org.ws4d.java.communication.HTTPBinding;
import org.ws4d.java.communication.HTTPSBinding;
import org.ws4d.java.communication.ProtocolData;
import org.ws4d.java.communication.TimeoutException;
import org.ws4d.java.communication.connection.ip.IPAddress;
import org.ws4d.java.communication.connection.tcp.SecureSocketFactorySE;
import org.ws4d.java.communication.connection.tcp.ServerSocket;
import org.ws4d.java.communication.connection.tcp.Socket;
import org.ws4d.java.configuration.SecurityProperties;
import org.ws4d.java.constants.HTTPConstants;
import org.ws4d.java.dispatch.DeviceServiceRegistry;
import org.ws4d.java.io.xml.XmlSerializer;
import org.ws4d.java.io.xml.canonicalization.CanonicalSerializer;
import org.ws4d.java.message.Message;
import org.ws4d.java.service.reference.DeviceReference;
import org.ws4d.java.service.reference.ServiceReference;
import org.ws4d.java.structures.HashMap;
import org.ws4d.java.types.EndpointReference;
import org.ws4d.java.types.URI;
import org.ws4d.java.util.Log;
import org.ws4d.java.util.StringUtil;

public class DPWSSecurityManagerSE implements DPWSSecurityManager {

	private HashMap						protocolDataToInputStream	= new HashMap();

	private static SecurityProperties	secProp;

	public DPWSSecurityManagerSE() {

	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.ws4d.java.security.DPWSSecurityManager#getBodySignature(org.ws4d.
	 * java.io.xml.XmlSerializer, org.ws4d.java.message.Message)
	 */
	public String getBodySignature(XmlSerializer serial, Message msg) {
		CanonicalSerializer cs = (CanonicalSerializer) serial;
		byte[] arry = cs.bodyPart();
		byte[] signArray = getSignature(arry, (PrivateKey) msg.getPrivateKey());

		return Base64Util.encodeBytes(signArray);
	}

	private byte[] getSignature(byte[] rawxml, PrivateKey pk) {
		try {

			// Compute signature
			Signature instance = Signature.getInstance("SHA1withRSA");
			instance.initSign(pk);
			MessageDigest digest = MessageDigest.getInstance("sha1");

			// calculate digest of the one and only part
			rawxml = digest.digest(rawxml);

			// generate SignedInfo part and calculate its digest
			byte[][] digs = new byte[1][];
			digs[0] = rawxml;
			rawxml = generateSignedInfo(digs, new String[] { SecurityManager.bodyPartID });

			rawxml = digest.digest(rawxml);

			// sign the SignedInfo parts digest
			instance.update(rawxml);

			byte[] signature = instance.sign();

			return signature;
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			e.printStackTrace();
		} catch (SignatureException e) {
			e.printStackTrace();
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.security.DPWSSecurityManager#validateMessage(byte[],
	 * org.ws4d.java.communication.ProtocolData,
	 * org.ws4d.java.types.EndpointReference, java.lang.String[])
	 */
	public boolean validateMessage(byte[] signature, ProtocolData pd, EndpointReference epr, String[] aliasCanditates) {
		try {
			IDawareInputStream bbis = ((IDawareInputStream) (protocolDataToInputStream.get(pd)));

			byte[][] signedParts = bbis.getPartsByteArrays();
			if (signedParts == null) {
				Log.error("Message validation failed because the referred sections cound not be extracted!");
				return false;
			} else {
				protocolDataToInputStream.remove(pd);
			}

			Certificate cert = null;

			DeviceReference dRef = null;
			ServiceReference sRef = null;

			if ((dRef = DeviceServiceRegistry.getDeviceReference(epr, false)) != null) {
				try {
					cert = (Certificate) dRef.getDevice().getCertificate();
				} catch (TimeoutException e) {
					e.printStackTrace();
				}
			} else if ((sRef = DeviceServiceRegistry.getServiceReference(epr, pd.getCommunicationManagerId(), false)) != null) {
				try {
					cert = (Certificate) sRef.getService().getCertificate();
				} catch (TimeoutException e) {
					e.printStackTrace();
				}
			}

			for (int i = 0; cert == null && aliasCanditates != null && i < aliasCanditates.length; i++) {
				cert = (Certificate) getCertificate(aliasCanditates[i]);
			}

			if (cert == null) {
				cert = (Certificate) getCertificate(epr.getAddress().toString());
				if (dRef != null) {
					dRef.getDevice().setCertificate(cert);
				} else if (sRef != null) {
					sRef.getService().setCertificate(cert);
				}
			}

			if (cert == null) {
				Log.error("Security: device/service uuid '" + epr.getAddress() + "' not found in the specified keystore!");
				return false;
			}

			// calculating digests over all parts
			MessageDigest digest = MessageDigest.getInstance("sha1");
			byte[][] digests = new byte[signedParts.length][];
			for (int i = 0; i < signedParts.length; i++) {
				digests[i] = digest.digest(signedParts[i]);
			}

			byte[] signedInfo = generateSignedInfo(digests, bbis.getIds());

			// the digest of the signedInfo element
			signedInfo = digest.digest(signedInfo);

			// sign that digest
			PublicKey pk = cert.getPublicKey();
			Signature s = Signature.getInstance("SHA1withRSA");
			s.initVerify(pk);
			s.update(signedInfo);
			if (s.verify(signature)) {
				Log.info("Discovery-Message validated!");
				return true;
			}
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			e.printStackTrace();
		} catch (SignatureException e) {
			e.printStackTrace();
		} catch (TimeoutException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		Log.warn("Discovery-Message could not be validated!");
		return false;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.ws4d.java.security.DPWSSecurityManager#getMD5Hash(java.lang.String)
	 */
	public long getMD5Hash(String str) {
		MessageDigest m = null;
		try {
			m = MessageDigest.getInstance("SHA-256");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		m.update(str.getBytes(), 0, str.length());
		return new BigInteger(1, m.digest()).longValue();
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.ws4d.java.security.DPWSSecurityManager#getPrivateKey(java.lang.String
	 * , java.lang.String)
	 */
	public Object getPrivateKey(String privKey, String pswd) {

		try {
			if (secProp == null) {
				secProp = SecurityProperties.getInstance();
			}
			KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());

			FileInputStream fis = new FileInputStream(secProp.getKeyStoreFilePath());
			ks.load(fis, secProp.getKeyStorePswd().toCharArray());
			fis.close();

			PrivateKey pk = null;

			if (pswd == null) {
				pswd = SecurityProperties.getInstance().getKeyStorePswd();
			}
			if (pswd == null) throw new KeyStoreException("Could not fetch private key. Password not found.");

			try {
				pk = (PrivateKey) ks.getKey(privKey, pswd.toCharArray());
			} catch (UnrecoverableKeyException e) {
				// If there is no key the user should create one!
			} catch (NoSuchAlgorithmException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			return pk;
		} catch (KeyStoreException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (CertificateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.ws4d.java.security.DPWSSecurityManager#getCertificate(java.lang.String
	 * )
	 */
	public Object getCertificate(String certAlias) {
		if (secProp == null) {
			secProp = SecurityProperties.getInstance();
		}
		try {
			Certificate cert = null;
			KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
			if (SecurityProperties.getInstance().getTrustStorePath() == null && SecurityProperties.getInstance().getKeyStoreFilePath() == null) return null;

			FileInputStream fis = (secProp.getTrustStorePath() != null ? new FileInputStream(secProp.getTrustStorePath()) : new FileInputStream(secProp.getKeyStoreFilePath()));
			ks.load(fis, (secProp.getTrustStorePasswd() != null ? secProp.getTrustStorePasswd() : secProp.getKeyStorePswd()).toCharArray());
			fis.close();

			String nearestAlias = certAlias;
			int lastIndex = -1;

			while (nearestAlias.length() > 1) {
				if ((cert = ks.getCertificate(nearestAlias.toLowerCase())) != null) break;
				nearestAlias = (lastIndex = nearestAlias.indexOf('/')) < 0 ? "" : nearestAlias.substring(lastIndex + 1);
			}

			return cert;
		} catch (KeyStoreException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			Log.error("Security: Could not get keystore!");
		} catch (CertificateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.security.DPWSSecurityManager#wrapInputStream(java.io.
	 * InputStream, org.ws4d.java.communication.ProtocolData)
	 */
	public InputStream wrapInputStream(InputStream in, ProtocolData pd) {
		IDawareInputStream bbio = new IDawareInputStream(in, null);
		protocolDataToInputStream.put(pd, bbio);

		return bbio;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.ws4d.java.security.DPWSSecurityManager#getNewCanonicalSerializer()
	 */
	public XmlSerializer getNewCanonicalSerializer(String id) {
		return new CanonicalSerializer(id);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.ws4d.java.security.DPWSSecurityManager#getSecureServerSocket(java
	 * .lang.String, int, java.lang.String)
	 */
	public ServerSocket getSecureServerSocket(IPAddress adr, int port, String alias) {
		try {
			return SecureSocketFactorySE.createServerSocket(adr, port, alias);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.ws4d.java.security.DPWSSecurityManager#getSecureSocket(org.ws4d.java
	 * .types.URI)
	 */
	public Socket getSecureSocket(URI location) {
		try {
			return SecureSocketFactorySE.createSocket(new IPAddress(location.getHost()), location.getPort(), location.toString());
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.ws4d.java.security.DPWSSecurityManager#getSecureSocket(java.lang.
	 * String, int, java.lang.String)
	 */
	public Socket getSecureSocket(IPAddress host, int port, String alias) {
		try {
			return SecureSocketFactorySE.createSocket(host, port, alias);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.security.DPWSSecurityManager#decode(java.lang.String)
	 */
	public byte[] decode(String base64enc) {
		return Base64Util.decode(base64enc);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.ws4d.java.security.DPWSSecurityManager#isHTTPS(org.ws4d.java.types
	 * .URI)
	 */
	public boolean isHTTPS(URI uri) {
		// return uri.getSchema().equalsIgnoreCase(HTTPConstants.HTTPS_SCHEMA);
		return StringUtil.equalsIgnoreCase(uri.getSchema(), HTTPConstants.HTTPS_SCHEMA);
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.security.DPWSSecurityManager#getKeyManagers()
	 */
	public Object[] getKeyManagers() throws IOException, GeneralSecurityException {
		String alg = KeyManagerFactory.getDefaultAlgorithm();
		KeyManagerFactory kmFact = KeyManagerFactory.getInstance(alg);

		if (SecurityProperties.getInstance().getKeyStoreFilePath() == null) return null;
		FileInputStream fis = new FileInputStream(SecurityProperties.getInstance().getKeyStoreFilePath());
		KeyStore ks = KeyStore.getInstance("jks");
		ks.load(fis, SecurityProperties.getInstance().getKeyStorePswd().toCharArray());
		fis.close();

		kmFact.init(ks, SecurityProperties.getInstance().getKeyStorePswd().toCharArray());

		KeyManager[] kms = kmFact.getKeyManagers();
		return kms;
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.security.DPWSSecurityManager#getTrustManagers()
	 */
	public Object[] getTrustManagers() throws IOException, GeneralSecurityException {
		String alg = TrustManagerFactory.getDefaultAlgorithm();
		TrustManagerFactory tmFact = TrustManagerFactory.getInstance(alg);
		if (SecurityProperties.getInstance().getTrustStorePath() == null) return null;

		FileInputStream fis = new FileInputStream(SecurityProperties.getInstance().getTrustStorePath());
		KeyStore ks = KeyStore.getInstance("jks");
		ks.load(fis, SecurityProperties.getInstance().getTrustStorePasswd().toCharArray());
		fis.close();

		tmFact.init(ks);

		TrustManager[] tms = tmFact.getTrustManagers();
		return tms;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.ws4d.java.security.DPWSSecurityManager#getAliasFromBinding(org.ws4d
	 * .java.communication.HTTPBinding)
	 */
	public String getAliasFromBinding(CommunicationBinding binding) {
		if (binding.getType() != HTTPBinding.HTTPS_BINDING)
			return null;
		else
			return ((HTTPSBinding) binding).getAlias();
	}

	/**
	 * This element will only be used internally. It will be gennerated digested
	 * and this digest will be signed
	 * 
	 * @param digs the signatures
	 * @param refs the reference ids of the digested parts of the xml message.
	 * @return the byte array of the signed info element. Ready to be signed.
	 */
	private static byte[] generateSignedInfo(byte[][] digs, String[] refs) {
		String signatureInfo = "<SignedInfo><CanonicalizationMethod Algorithm=\"http://www.w3.org/2001/10/xml_exc-c14n#\" /> " + "<SignatureMethod Algorithm=\"htt://www.w3.org/2000/09/xmldsig#rsa-sha1\" />";
		for (int i = 0; i < digs.length; i++) {
			signatureInfo += "<Reference URI=\"#" + refs[i] + "\" ><Transforms><Transform Algorithm=\"http://www.w3.org/2001/10/xml-exc-c14n#\" />" + "</Transforms><DigestMethod Algorithm=\"http://www.w3.org/2000/09/xmldsig#sha1\" />" + "<DigestValue>" + Base64Util.encodeBytes(digs[i]) + "</DigestValue></Reference>";
		}
		signatureInfo += "</SignedInfo>";
		return signatureInfo.getBytes();
	}

	private String byteArrayToString(byte[] d) {
		String g = "";
		for (int i = 0; i < d.length; i++) {
			g += (char) d[i];
		}
		return g;
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.security.DPWSSecurityManager#encode(byte[])
	 */
	public String encode(byte[] raw) {
		return Base64Util.encodeBytes(raw);
	}

}