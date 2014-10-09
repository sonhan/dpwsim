/*******************************************************************************
 * Copyright (c) 2009 MATERNA Information & Communications. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html. For further
 * project-related information visit http://www.ws4d.org. The most recent
 * version of the JMEDS framework can be obtained from
 * http://sourceforge.net/projects/ws4d-javame.
 ******************************************************************************/
package org.ws4d.java.communication.protocol.soap.generator;

import java.io.IOException;
import java.io.OutputStream;

import org.ws4d.java.DPWSFramework;
import org.ws4d.java.communication.CommunicationManager;
import org.ws4d.java.communication.CommunicationUtil;
import org.ws4d.java.communication.ProtocolData;
import org.ws4d.java.configuration.FrameworkProperties;
import org.ws4d.java.constants.ConstantsHelper;
import org.ws4d.java.constants.DPWSMessageConstants;
import org.ws4d.java.constants.MEXConstants;
import org.ws4d.java.constants.SOAPConstants;
import org.ws4d.java.constants.WSAConstants;
import org.ws4d.java.constants.WSDConstants;
import org.ws4d.java.constants.WSEConstants;
import org.ws4d.java.constants.WSSecurityConstants;
import org.ws4d.java.constants.XMLConstants;
import org.ws4d.java.io.xml.XmlSerializer;
import org.ws4d.java.io.xml.XmlSerializerImplementation;
import org.ws4d.java.message.Message;
import org.ws4d.java.message.SOAPHeader;
import org.ws4d.java.security.SecurityManager;
import org.ws4d.java.structures.HashMap;
import org.ws4d.java.types.ByteArrayBuffer;
import org.ws4d.java.util.Log;
import org.ws4d.java.util.WS4DIllegalStateException;
import org.xmlpull.v1.IllegalStateException;

/**
 * Class for generating SOAP Messages out of DPWS Messages
 * 
 * @author bschierb
 */
public class DefaultMessage2SOAPGenerator implements Message2SOAPGenerator {

	protected static final HashMap	BYTE_ARRAY_POOL	= new HashMap(10);

	protected XmlSerializer			serializer		= new XmlSerializerImplementation();

	protected MessageSerializer		msgSerializer	= new DefaultMessageSerializer();

	protected ConstantsHelper		helper			= null;

	/*
	 * (non-Javadoc)
	 * @see
	 * org.ws4d.java.communication.protocol.soap.generator.IMessage2SOAPGenerator
	 * #generateSOAPMessage(java.io.OutputStream, org.ws4d.java.message.Message)
	 */
	public void generateSOAPMessage(OutputStream out, Message msg, ProtocolData pd) throws IllegalArgumentException, WS4DIllegalStateException, IOException {
		if (msg == null) {
			return;
		}
		// Message2SOAPGenerator gen = getInstance();
		this.setOutput(out);
		this.internalGenerateSOAPMessage(msg, pd);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.ws4d.java.communication.protocol.soap.generator.Message2SOAPGenerator
	 * #generateSOAPMessage(org.ws4d.java.message.Message,
	 * org.ws4d.java.communication.ProtocolData)
	 */
	public ByteArrayBuffer generateSOAPMessage(Message msg, ProtocolData pd) throws IOException {
		if (msg == null) {
			return null;
		}
		// reuse byte arrays/streams
		ReusableByteArrayOutputStream tmpOutput = getByteStream();
		// this sets the pointer to zero
		tmpOutput.reset();
		this.setOutput(tmpOutput);
		this.internalGenerateSOAPMessage(msg, pd);

		return new ByteArrayBuffer(tmpOutput.getBuffer(), tmpOutput.getCurrentSize());
	}

	protected static ReusableByteArrayOutputStream getByteStream() {
		synchronized (BYTE_ARRAY_POOL) {
			Thread t = Thread.currentThread();
			ReusableByteArrayOutputStream bout = (ReusableByteArrayOutputStream) BYTE_ARRAY_POOL.get(t);
			if (bout == null) {
				bout = new ReusableByteArrayOutputStream();
				BYTE_ARRAY_POOL.put(t, bout);
			}
			return bout;
		}
	}

	/**
	 * Constructor.
	 */
	protected DefaultMessage2SOAPGenerator() {
		super();
	}

	protected void setOutput(OutputStream out) throws IOException {
		serializer.setOutput(out, XMLConstants.ENCODING);
	}

	/**
	 * Builds the SOAP Message and sends it
	 * 
	 * @param msg
	 * @param pd TODO
	 * @return
	 * @throws IOException
	 */
	protected void internalGenerateSOAPMessage(Message msg, ProtocolData pd) throws IOException {
		CommunicationManager comMan = DPWSFramework.getCommunicationManager(pd.getCommunicationManagerId());
		CommunicationUtil comUtil = comMan.getCommunicationUtil();
		helper = comUtil.getHelper(msg.getHeader().getProtocolInfo().getVersion());

		if (Log.isDebug()) Log.debug("<O> Communicate over :" + msg.getHeader().getProtocolInfo().getDisplayName() + ", Action: " + msg.getAction() + ", Id: " + msg.getMessageId(), Log.DEBUG_LAYER_FRAMEWORK);

		if ((msg.getType() != DPWSMessageConstants.BYE_MESSAGE && msg.getType() != DPWSMessageConstants.HELLO_MESSAGE && msg.getType() != DPWSMessageConstants.PROBE_MATCHES_MESSAGE && msg.getType() != DPWSMessageConstants.RESOLVE_MATCHES_MESSAGE) || (!msg.isSecure() && serializer.getType() != XmlSerializerImplementation.XML_SERIALIZER)) {
			XmlSerializer sz = serializer;
			serializer = new XmlSerializerImplementation();
			serializer.setOutput(sz.getOutput());
			// if the message should be send secure
			// the canonical serializer is used.
		} else if (msg.isSecure() && (msg.getType() == DPWSMessageConstants.BYE_MESSAGE || msg.getType() == DPWSMessageConstants.HELLO_MESSAGE || msg.getType() == DPWSMessageConstants.PROBE_MATCHES_MESSAGE || msg.getType() == DPWSMessageConstants.RESOLVE_MATCHES_MESSAGE)) {
			try {
				XmlSerializer sz = serializer;
				if (!DPWSFramework.hasModule(DPWSFramework.SECURITY_MODULE)) {
					throw new Exception("DPWS Security Module not found. Unable to use canonical serializer.");
				}
				serializer = DPWSFramework.getSecurityManager().getNewCanonicalSerializer("BID1");
				serializer.setOutput(sz.getOutput());
			} catch (Exception e) {
				Log.printStackTrace(e);
			}
		}

		// Start the Document
		/* if (!msg.isSecure()) */
		serializer.startDocument(XMLConstants.ENCODING, null);

		// Add Standard Prefixes
		addStandardNamespaces(msg);

		// Start Envelope
		serializer.startTag(SOAPConstants.SOAP12_NAMESPACE_NAME, SOAPConstants.SOAP_ELEM_ENVELOPE);

		SOAPHeader header = msg.getHeader();
		// generate Header
		if (header != null) {
			msgSerializer.serialize(header, serializer, pd);
		} else {
			throw new WS4DIllegalStateException("No message header defined. Cannot serialize message.");
		}

		// generate Body
		msgSerializer.serialize(msg, serializer, pd);

		// Close Envelope , Document
		serializer.endTag(SOAPConstants.SOAP12_NAMESPACE_NAME, SOAPConstants.SOAP_ELEM_ENVELOPE);

		if (msg.isSecure() && (msg.getType() == DPWSMessageConstants.BYE_MESSAGE || msg.getType() == DPWSMessageConstants.HELLO_MESSAGE || msg.getType() == DPWSMessageConstants.PROBE_MATCHES_MESSAGE || msg.getType() == DPWSMessageConstants.RESOLVE_MATCHES_MESSAGE)) {
			signMessageCompact(msg);
		}

		// Send to Writer/Stream
		serializer.flushCache();
		serializer.endDocument();
	}

	/**
	 * Serialize the Standardnamespaces and the specific Namespaces to the
	 * messages
	 * 
	 * @param msg
	 * @throws IOException
	 */
	protected void addStandardNamespaces(Message msg) throws IOException {
		// Standard Prefixes
		serializer.setPrefix(helper.getDPWSNamespacePrefix(), helper.getDPWSNamespace());
		serializer.setPrefix(SOAPConstants.SOAP12_NAMESPACE_PREFIX, SOAPConstants.SOAP12_NAMESPACE_NAME);
		serializer.setPrefix(WSAConstants.WSA_NAMESPACE_PREFIX, helper.getWSANamespace());

		// Discovery Namespace
		if (msg.getType() >= DPWSMessageConstants.HELLO_MESSAGE && msg.getType() <= DPWSMessageConstants.RESOLVE_MATCHES_MESSAGE) {
			serializer.setPrefix(WSDConstants.WSD_NAMESPACE_PREFIX, helper.getWSDNamespace());
		}
		// Eventing Namespace
		if (msg.getType() >= DPWSMessageConstants.SUBSCRIBE_MESSAGE && msg.getType() <= DPWSMessageConstants.SUBSCRIPTION_END_MESSAGE) {
			serializer.setPrefix(WSEConstants.WSE_NAMESPACE_PREFIX, WSEConstants.WSE_NAMESPACE_NAME);
		}
		// Metadata Namespace
		else if (msg.getType() == DPWSMessageConstants.GET_METADATA_MESSAGE || msg.getType() == DPWSMessageConstants.GET_METADATA_RESPONSE_MESSAGE || msg.getType() == DPWSMessageConstants.GET_RESPONSE_MESSAGE) {
			serializer.setPrefix(MEXConstants.WSX_NAMESPACE_PREFIX, MEXConstants.WSX_NAMESPACE_NAME);
		}
	}

	protected void signMessageCompact(Message msg) {
		SecurityManager dpwsSecMan = DPWSFramework.getSecurityManager();

		String signature = dpwsSecMan.getBodySignature(serializer, msg);

		try {
			serializer.injectSecurityStart();
			serializer.startTag(WSSecurityConstants.XML_SOAP_DISCOVERY, WSSecurityConstants.COMPACT_SECURITY);
			serializer.startTag(WSSecurityConstants.XML_SOAP_DISCOVERY, WSSecurityConstants.COMPACT_SIG);
			serializer.attribute(null, WSSecurityConstants.COMPACT_SCHEME, WSSecurityConstants.XML_SOAP_DISCOVERY + "/rsa");
			serializer.attribute(null, WSSecurityConstants.COMPACT_REFS, SecurityManager.bodyPartID);
			serializer.attribute(null, WSSecurityConstants.COMPACT_SIG, signature);
			serializer.endTag(WSSecurityConstants.XML_SOAP_DISCOVERY, WSSecurityConstants.COMPACT_SIG);
			serializer.endTag(WSSecurityConstants.XML_SOAP_DISCOVERY, WSSecurityConstants.COMPACT_SECURITY);
			serializer.injectSecurityDone();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static class ReusableByteArrayOutputStream extends OutputStream {

		private final byte[]	buf		= new byte[FrameworkProperties.getInstance().getMaxDatagramSize()];

		private int				pointer	= 0;

		ReusableByteArrayOutputStream() {
			super();
		}

		/*
		 * (non-Javadoc)
		 * @see java.io.OutputStream#write(int)
		 */
		public void write(int b) throws IOException {
			if (pointer == buf.length) {
				throw new IOException("Buffer size exceeded");
			}
			buf[pointer++] = (byte) b;
		}

		/*
		 * (non-Javadoc)
		 * @see java.io.OutputStream#write(byte[])
		 */
		public void write(byte[] b) throws IOException {
			write(b, 0, b.length);
		}

		/*
		 * (non-Javadoc)
		 * @see java.io.OutputStream#write(byte[], int, int)
		 */
		public void write(byte[] b, int off, int len) throws IOException {
			if (pointer + (len - off) >= buf.length) {
				throw new IOException("Buffer size exceeded (current=" + buf.length + ", new to store=" + (len - off));
			}
			System.arraycopy(b, off, buf, pointer, len);
			pointer += len;
		}

		/*
		 * (non-Javadoc)
		 * @see java.io.OutputStream#close()
		 */
		public void close() throws IOException {
			reset();
		}

		void reset() {
			// reset pointer
			pointer = 0;
		}

		byte[] getBuffer() {
			return buf;
		}

		int getCurrentSize() {
			return pointer;
		}
	}
}
