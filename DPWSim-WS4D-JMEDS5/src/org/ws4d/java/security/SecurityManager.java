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

import java.io.IOException;
import java.io.InputStream;

import org.ws4d.java.communication.CommunicationBinding;
import org.ws4d.java.communication.ProtocolData;
import org.ws4d.java.io.xml.XmlSerializer;
import org.ws4d.java.message.Message;
import org.ws4d.java.types.EndpointReference;
import org.ws4d.java.types.URI;

/**
 * The SecurityManager manages many aspects of the security implementation.
 */
public interface SecurityManager {

	public static String	bodyPartID	= "BID1";

	/**
	 * getBodySignature generates the signature for the xml data supplied
	 * WARNING: Do not call this method before completion of the body part
	 * 
	 * @param serial the (canonical) serializer that works with the messages to
	 *            be signed.
	 * @param msg the message to sign
	 * @return the calculated signature
	 */
	public String getBodySignature(XmlSerializer serial, Message msg);

	/**
	 * getPrivateKey
	 * 
	 * @param privKey the alias of the private key in the java keystore.
	 * @param pswd the password to access the private key
	 * @return a PrivateKey object
	 */
	public Object getPrivateKey(String privKey, String pswd);

	/**
	 * validateMessage
	 * 
	 * @param signature the signature delivered with the message
	 * @param pd the protocol data instance belonging to the message to be
	 *            validated
	 * @param uuid the EndpointReference of the source device/service
	 * @param xaddresses of the source device/Service
	 * @return True if validation was successful. False otherwise.
	 */
	public boolean validateMessage(byte[] signature, ProtocolData pd, EndpointReference epr, String[] aliasCandidates);

	/**
	 * getCertificate
	 * 
	 * @param certAlias the alias of the certificate in the java keystore.
	 * @return a Certificate object
	 */
	public Object getCertificate(String certAlias);

	/**
	 * wraps the supplied InputStream with an instance of IDawareInputStream
	 * 
	 * @param in
	 * @return a BodyBufferInputStream object wrapping the Input Stream
	 */
	public InputStream wrapInputStream(InputStream in, ProtocolData pd);

	/**
	 * getNewCanonicalSerializer
	 * 
	 * @param id the id of the part that will be signed/canonicalized
	 * @return a new CanonicalSerializer instance.
	 */
	public XmlSerializer getNewCanonicalSerializer(String id);

	/**
	 * getMD5Hash returns the MD5 hash of the given string
	 */
	public long getMD5Hash(String str);

	/**
	 * decodes a base64 encoded String
	 * 
	 * @param base64enc
	 * @return the decoded string as byte array
	 */
	public byte[] decode(String base64enc);

	/**
	 * encodes a byteArray
	 * 
	 * @param the raw byte array
	 * @return the encoded String
	 */
	public String encode(byte[] raw);

	/**
	 * @return the TrustManagers from the TrustStore specified in the properties
	 *         file.
	 * @throws IOException
	 * @throws Exception
	 */
	public Object[] getTrustManagers() throws IOException, Exception;

	/**
	 * @return the KeyManagers from the KeyStore specified in the properties
	 *         file.
	 * @throws IOException
	 * @throws Exception
	 */
	public Object[] getKeyManagers() throws IOException, Exception;

	/**
	 * This method will return the alias that belongs to the binding, if the the
	 * HTTPBinding is a HTTPSBinding
	 * 
	 * @param binding
	 * @return the alias that belongs to the binding
	 */
	public String getAliasFromBinding(CommunicationBinding binding);

	/**
	 * @param uri
	 * @return true if the URI starts with "https"
	 */
	public boolean isHTTPS(URI uri);

}
