/*******************************************************************************
 * Copyright (c) 2009 MATERNA Information & Communications. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html. For further
 * project-related information visit http://www.ws4d.org. The most recent
 * version of the JMEDS framework can be obtained from
 * http://sourceforge.net/projects/ws4d-javame.
 ******************************************************************************/
package org.ws4d.java.io.xml.canonicalization;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Writer;

import org.ws4d.java.constants.XMLConstants;
import org.ws4d.java.io.xml.ElementHandler;
import org.ws4d.java.io.xml.ElementHandlerRegistry;
import org.ws4d.java.io.xml.XmlSerializer;
import org.ws4d.java.io.xml.XmlSerializerImplementation;
import org.ws4d.java.io.xml.cache.XmlAttribute;
import org.ws4d.java.io.xml.cache.XmlPrefix;
import org.ws4d.java.io.xml.cache.XmlStructure;
import org.ws4d.java.io.xml.cache.XmlTag;
import org.ws4d.java.io.xml.cache.XmlText;
import org.ws4d.java.structures.ArrayList;
import org.ws4d.java.structures.HashMap;
import org.ws4d.java.structures.Iterator;
import org.ws4d.java.structures.List;
import org.ws4d.java.types.QName;
import org.ws4d.java.util.StringUtil;
import org.xmlpull.mxp1_serializer.MXSerializer;
import org.xmlpull.v1.IllegalStateException;

public class CanonicalSerializer extends MXSerializer implements XmlSerializer {

	public static final int			EXC_C14N_XML_SERIALIZER	= 1;

	private int						type					= EXC_C14N_XML_SERIALIZER;

	Writer							writer;

	private ArrayList				xmlCache;

	private int						currentCmd				= -1;

	// these are pointers to support faster xml processing
	private int						xmlCommandCounter		= 0;

	private int						secuInjectionStart		= -1;

	private String					id						= "";

	private String					lastStartedTag;

	boolean							canonicalize			= false;

	String							currentCanonElement		= "";

	public static final String[]	characterEntities		= { "&quot;", "&amp;", "&apos;", "&lt;", "&gt;" };

	public static final String[]	correpondingCharacters	= { "\"", "&", "'", "<", ">" };

	private ArrayList				recognNamespaces		= new ArrayList();

	private byte[]					bodyByteArray			= null;

	public CanonicalSerializer(String id) {
		this.id = id;
		xmlCache = new ArrayList();
	}

	public void setPrefix(String prefix, String namespace) {

		recognNamespaces.add(new NamespaceRecord(prefix, namespace));
		xmlCommandCounter++;
		if (currentCmd != -1) {
			currentCmd++;
			xmlCache.add(currentCmd, new XmlPrefix(prefix, namespace));
			return;
		}

		xmlCache.add(new XmlPrefix(prefix, namespace));
	}

	public org.xmlpull.v1.XmlSerializer startTag(String namespace, String name) {
		lastStartedTag = name;

		xmlCommandCounter++;
		if (currentCmd != -1) {
			currentCmd++;
			xmlCache.add(currentCmd, new XmlTag(true, namespace, name));
			return this;

			// currentCmd++;
			// xmlCache.add(currentCmd, new XmlTag(true, namespace, name));
			// return this;
		}

		if (canonicalize == true) {
			xmlCache.add(new XmlTag(true, namespace, name));
		} else {
			xmlCache.add(new XmlTag(true, namespace, name));
		}
		// xmlCache.add(new XmlTag(true, namespace, name));
		return this;
	}

	public org.xmlpull.v1.XmlSerializer endTag(String ns, String name) {

		xmlCommandCounter++;
		if (currentCmd != -1) {
			currentCmd++;
			xmlCache.add(currentCmd, new XmlTag(false, ns, name));
			return this;
		}

		if (StringUtil.equalsIgnoreCase(name, "header")) secuInjectionStart = xmlCommandCounter - 2;

		if (canonicalize)
			xmlCache.add(new XmlTag(false, ns, name));
		else
			xmlCache.add(new XmlTag(false, ns, name));
		// xmlCache.add(new XmlTag(false, ns, name));

		if ((ns + name).equals(currentCanonElement)) {
			canonicalize = false;
		}
		return this;
	}

	public org.xmlpull.v1.XmlSerializer attribute(String namespace, String name, String value) {
		if (canonicalize) value = c14nAttributeString(value);

		if (name.equals("ID") && value.equals(id)) {
			canonicalize = true;
			currentCanonElement = lastStartedTag;
		}

		xmlCommandCounter++;
		if (currentCmd != -1) {
			currentCmd++;
			xmlCache.add(currentCmd, new XmlAttribute(namespace, name, value));
			return this;
		}
		xmlCache.add(new XmlAttribute(namespace, name, value));
		return this;
	}

	public void attributeo(String namespace, String name, String value) {
		try {
			super.attribute(namespace, name, value);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void setPrefixo(String prefix, String namespace) {
		try {
			super.setPrefix(prefix, namespace);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public XmlSerializer startTago(String namespace, String name) {
		try {
			super.startTag(namespace, name);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return this;
	}

	public XmlSerializer endTago(String ns, String name) {
		try {
			super.endTag(ns, name);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return this;
	}

	public org.xmlpull.v1.XmlSerializer texto(String text) {
		try {
			super.text(text);
		} catch (IOException e) {
			e.printStackTrace();
		}

		return this;
	}

	public void flushCache() {
		Iterator it = xmlCache.iterator();
		boolean write = true;
		while (it.hasNext()) {
			try {
				XmlStructure st = (XmlStructure) it.next();
				if ((st.getType() == XmlStructure.XML_START_TAG) && ((XmlTag) st).getName().equals("Body")) {
					writer.write(byteArrayToString(bodyByteArray == null ? (bodyByteArray = bodyPart()) : bodyByteArray));
					write = false;
				}

				if (write) {
					st.flush(this);
				}

				if ((st.getType() == XmlStructure.XML_END_TAG) && ((XmlTag) st).getName().equals("Body")) write = true;
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (IllegalStateException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		try {
			super.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private String byteArrayToString(byte[] d) {
		String g = "";
		for (int i = 0; i < d.length; i++) {
			g += (char) d[i];
		}
		return g;
	}

	/**
	 * @param writer the writer to set
	 */
	public void setOutput(Writer writer) {
		super.setOutput(writer);
		this.writer = writer;
	}

	/**
	 * Writes a block of XML directly to the underlying stream, especially
	 * without ignoring any special chars.
	 * 
	 * @param text the XML block to write
	 * @throws IOException
	 */
	public void plainText(String text) throws IOException {
		writer.write(text);
	}

	// private String exchangePrefixes(String text) {
	// int index = 0;
	// String one;
	// String two;
	//
	// while ((index = text.indexOf(":", index)) > 0) {
	// int startIndexofOldNs = text.substring(0, index).lastIndexOf(' ');
	// if (startIndexofOldNs == index - 1) {
	// index++;
	// continue;
	// }
	// String ns = text.substring(startIndexofOldNs < 0 ? 0 : startIndexofOldNs,
	// index);
	//
	// if (newNs == null) {
	// index++;
	// continue;
	// }
	//
	// one = text.substring(0, startIndexofOldNs < 0 ? 0 : startIndexofOldNs);
	// two = text.substring(index, text.length());
	//
	// text = one + newNs + two;
	//
	// index++;
	// }
	// return text;
	// }

	public org.xmlpull.v1.XmlSerializer text(String text) {
		if (canonicalize) {
			text = c14nValueString(text);
			// text = exchangePrefixes(text);
		}

		xmlCommandCounter++;
		if (currentCmd != -1) {
			currentCmd++;
			xmlCache.add(currentCmd, new XmlText(text));
		}

		xmlCache.add(new XmlText(text));
		return this;
	}

	/**
	 * Exchanges certain characters into xml character entities
	 * 
	 * @param text
	 * @return
	 */
	private String c14nValueString(String text) {
		for (int i = 0; i < correpondingCharacters.length; i++) {
			int index = -1;
			String one;
			String two;
			while ((index = text.indexOf(correpondingCharacters[i], index == -1 ? 0 : index)) > 0 && text.indexOf(characterEntities[i], index == -1 ? 0 : index) <= -1) {
				one = text.substring(0, index);
				two = text.substring(index + correpondingCharacters[i].length());
				index += characterEntities[i].length() - 1;
				text = one + characterEntities[i] + two;
			}
		}
		return text;
	}

	private String c14nAttributeString(String text) {
		for (int i = 0; i < characterEntities.length; i++) {
			int index = -1;
			String one;
			String two;
			while ((index = text.indexOf(characterEntities[i], index == -1 ? 0 : index)) > 0) {
				one = text.substring(0, index);
				two = text.substring(index + characterEntities[i].length());
				index += correpondingCharacters[i].length();
				text = one + correpondingCharacters[i] + two;
			}
		}
		return text;
	}

	/**
	 * resoluteNamespace
	 * 
	 * @param the prefix to resolute.
	 * @return the namespace.
	 */
	// public String resoluteNamespace(String prefix) {
	// for (int i = this.recognNamespaces.size() - 1; i >= 0; i--) {
	// if (((NamespaceRecord) (recognNamespaces.get(i))).prefix.equals(prefix))
	// {
	// return ((NamespaceRecord) recognNamespaces.get(i)).namespace;
	// }
	// }
	// return prefix;
	// }

	/**
	 * @param qname the fully qualified name of the elements to expect within
	 *            the <code>list</code>
	 * @param elements the list of elements to serialize; all are expected to be
	 *            of the same type; note that this list can be empty or have
	 *            just one element
	 * @throws IOException
	 */
	public void unknownElements(QName qname, List elements) throws IOException {
		ElementHandler handler = ElementHandlerRegistry.getRegistry().getElementHandler(qname);
		if (handler != null) {
			for (Iterator at = elements.iterator(); at.hasNext();) {
				handler.serializeElement(this, qname, at.next());
			}
		}
	}

	// sorting

	public void namespaceWriter() {
		boolean inSection = false;

		HashMap ns_rendered = new HashMap();

		int afterLastStartedTagPosition = -1;

		for (int iteration = 0; iteration < xmlCache.size(); iteration++) {
			XmlStructure st = (XmlStructure) xmlCache.get(iteration);

			if (st.getType() == XmlStructure.XML_START_TAG) {
				afterLastStartedTagPosition = iteration;
			}

			if (st.getType() == XmlStructure.XML_START_TAG && StringUtil.equalsIgnoreCase(((XmlTag) st).getName(), "Body")) {
				inSection = true;
				String prefix = getPrefixFor(((XmlTag) st).getNamespace());
				ns_rendered.put(prefix, ((XmlTag) st).getNamespace());
				xmlCache.add(afterLastStartedTagPosition + 1, new XmlAttribute("", "xmlns:" + prefix, ((XmlStructure) st).getNamespace()));
			} else if (inSection) {
				String ns;
				if ((ns = getPrefixFor(((XmlStructure) st).getNamespace())) != null && st.getType() != XmlStructure.XML_END_TAG) {
					if (ns_rendered.containsKey(ns)) continue;
					xmlCache.add(afterLastStartedTagPosition + 1, new XmlAttribute("", "xmlns:" + ns, ((XmlStructure) st).getNamespace()));
					ns_rendered.put(ns, ((XmlStructure) st).getNamespace());
				}
			}
		}
	}

	private void writeSortedAttributes(XmlSerializer s, ArrayList unsorted) {
		ArrayList nsDelarations = new ArrayList();
		ArrayList attributes = new ArrayList();

		Iterator it = unsorted.iterator();

		XmlStructure st;

		while (it.hasNext()) {
			st = (XmlStructure) it.next();
			if (st.getType() == XmlStructure.XML_PREFIX || st.getName().startsWith("xmlns")) {
				nsDelarations.add(st);
			} else
				attributes.add(st);
		}

		attributes = sortXmlElemets(attributes);
		nsDelarations = sortXmlElemets(nsDelarations);

		writeArrayToSerializer(s, nsDelarations);
		writeArrayToSerializer(s, attributes);
	}

	private void writeArrayToSerializer(XmlSerializer s, ArrayList l) {
		Iterator it = l.iterator();
		while (it.hasNext()) {
			try {
				((XmlStructure) it.next()).flush(s);
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (IllegalStateException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private ArrayList sortXmlElemets(ArrayList xmlStructs) {
		int i, j;
		XmlStructure newValue;
		for (i = 1; i < xmlStructs.size(); i++) {
			newValue = (XmlStructure) xmlStructs.get(i);
			j = i;
			while (j > 0 && ((XmlStructure) xmlStructs.get(j - 1)).getValue().compareTo(newValue.getValue()) > 0) {
				xmlStructs.set(j, xmlStructs.get(j - 1));
				j--;
			}
			xmlStructs.set(j, newValue);
		}

		return xmlStructs;
	}

	public byte[] bodyPart() {
		XmlSerializer serializer = new XmlSerializerImplementation();
		ByteArrayOutputStream baos = new ByteArrayOutputStream();

		try {
			serializer.setOutput(baos, XMLConstants.ENCODING);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		namespaceWriter();

		boolean write = false;

		boolean tagEnded = false;

		Iterator it = xmlCache.iterator();

		ArrayList attributes = new ArrayList();
		while (it.hasNext()) {
			XmlStructure st = (XmlStructure) it.next();
			if (write) {
				try {
					if (st.getType() == XmlStructure.XML_ATTRIBUTE || st.getType() == XmlStructure.XML_PREFIX) {
						attributes.add(st.getType() == XmlStructure.XML_PREFIX ? new XmlAttribute("", "xmlns:" + st.getName(), st.getValue()) : st);
						continue;
					} else if (st.getType() == XmlStructure.XML_START_TAG) {
						if (!attributes.isEmpty()) {
							if (!tagEnded) {
								writeSortedAttributes(serializer, attributes);
								attributes = new ArrayList();
							}
						}

						tagEnded = false;
						// if(!((XmlTag) st).getNamespace().isEmpty() &&
						// )attributes.add(new XmlAttribute("",
						// "xmlns:"+this.getPrefixFor(((XmlTag)
						// st).getNamespace()), ((XmlTag) st).getNamespace()));
						((XmlTag) st).setName(getPrefixFor(((XmlTag) st).getNamespace()) + ":" + st.getName());
						((XmlTag) st).setNamespace("");
					} else if (st.getType() == XmlStructure.XML_TEXT || st.getType() == XmlStructure.XML_END_TAG) {
						if (st.getType() == XmlStructure.XML_END_TAG && !StringUtil.equalsIgnoreCase(st.getName(), "body")) {
							tagEnded = true;
							((XmlTag) st).setName(getPrefixFor(st.getNamespace()) + ":" + st.getName());
							st.setNameSpace("");
						}
						if (!attributes.isEmpty()) {
							writeSortedAttributes(serializer, attributes);
							attributes = new ArrayList();
						}
					}

					if (st.getType() == XmlStructure.XML_END_TAG && StringUtil.equalsIgnoreCase(((XmlTag) st).getName(), "Body")) {
						st = new XmlTag(false, "", getPrefixFor(((XmlTag) st).getNamespace()) + ":" + st.getName());
						st.flush(serializer);
						break;
					}

					st.flush(serializer);
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				} catch (IllegalStateException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			} else if (st.getType() == XmlStructure.XML_START_TAG) {
				if (StringUtil.equalsIgnoreCase(((XmlTag) st).getName(), "Body")) {
					write = true;
					try {
						st = new XmlTag(true, "", getPrefixFor(((XmlTag) st).getNamespace()) + ":" + st.getName());
						st.flush(serializer);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}

		try {
			serializer.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		bodyByteArray = baos.toByteArray();
		return bodyByteArray;
	}

	public void injectSecurityStart() {
		if (this.secuInjectionStart != -1) {
			this.currentCmd = secuInjectionStart;
			return;
		}

		Iterator it = xmlCache.iterator();
		for (int pointer = 0; it.hasNext(); pointer++) {
			XmlStructure st = (XmlStructure) it.next();
			if (st.getType() == XmlStructure.XML_START_TAG) {
				if (StringUtil.equalsIgnoreCase(((XmlTag) st).getName(), "header")) {
					currentCmd = pointer + 1;
					return;
				}
			}
		}
	}

	public void injectSecurityDone() {
		currentCmd = -1;
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.io.xml.XmlSerializer#getType()
	 */
	public int getType() {
		return type;
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.io.xml.XmlSerializer#getOutput()
	 */
	public Writer getOutput() {
		return super.getWriter();
	}

	public String getPrefixFor(String namespace) {
		Iterator iter = recognNamespaces.iterator();
		while (iter.hasNext()) {
			NamespaceRecord nr = (NamespaceRecord) iter.next();
			if (nr.namespace.equals(namespace)) return nr.prefix;
		}
		return null;
	}

	public String getNamespaceFor(String prefix) {
		Iterator iter = recognNamespaces.iterator();
		while (iter.hasNext()) {
			NamespaceRecord nr = (NamespaceRecord) iter.next();
			if (nr.prefix.equals(prefix)) return nr.namespace;
		}
		return null;
	}

}

class NamespaceRecord {

	public String	prefix;

	public String	namespace;

	public NamespaceRecord(String p, String ns) {
		prefix = p;
		namespace = ns;
	}
}