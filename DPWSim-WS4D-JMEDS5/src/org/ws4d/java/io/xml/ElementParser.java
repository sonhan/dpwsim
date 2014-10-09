/*******************************************************************************
 * Copyright (c) 2009 MATERNA Information & Communications. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html. For further
 * project-related information visit http://www.ws4d.org. The most recent
 * version of the JMEDS framework can be obtained from
 * http://sourceforge.net/projects/ws4d-javame.
 ******************************************************************************/
package org.ws4d.java.io.xml;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

import org.ws4d.java.constants.WSDConstants;
import org.ws4d.java.constants.XMLConstants;
import org.ws4d.java.structures.HashMap;
import org.ws4d.java.types.AttributedURI;
import org.ws4d.java.types.LocalizedString;
import org.ws4d.java.types.ProbeScopeSet;
import org.ws4d.java.types.QName;
import org.ws4d.java.types.QNameSet;
import org.ws4d.java.types.ScopeSet;
import org.ws4d.java.types.URI;
import org.ws4d.java.types.URISet;
import org.ws4d.java.types.UnknownDataContainer;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

/**
 * 
 */
public class ElementParser implements XmlPullParser {

	private static final String	ILLEGAL_TYPE		= "Wrong event type";

	private static final String	AT_END_OF_DOCUMENT	= "Already at end of document";

	private final XmlPullParser	source;

	private final int			elementDepth;

	private boolean				finished			= false;

	public static int nextNonWhiteSpace(String src, int offset) {
		if (src == null) {
			return -1;
		}
		int len = src.length();
		for (int i = offset + 1; i < len; i++) {
			char c = src.charAt(i);
			switch (c) {
				case (' '):
				case ('\t'):
				case ('\n'):
				case ('\r'): {
					break;
				}
				default: {
					return i;
				}
			}
		}
		return -1;
	}

	public static int nextWhiteSpace(String src, int offset) {
		if (src == null) {
			return -1;
		}
		int len = src.length();
		for (int i = offset + 1; i < len; i++) {
			char c = src.charAt(i);
			switch (c) {
				case (' '):
				case ('\t'):
				case ('\n'):
				case ('\r'): {
					return i;
				}
				default: {
					break;
				}
			}
		}
		return -1;
	}

	/**
	 * @param source
	 */
	public ElementParser(XmlPullParser source) {
		super();
		this.source = source;
		this.elementDepth = source.getDepth();
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.xmlpull.v1.XmlPullParser#defineEntityReplacementText(java.lang.String
	 * , java.lang.String)
	 */
	public void defineEntityReplacementText(String entityName, String replacementText) throws XmlPullParserException {
		throw new XmlPullParserException("This parser doesn't support entity replacement text", this, null);
	}

	/*
	 * (non-Javadoc)
	 * @see org.xmlpull.v1.XmlPullParser#getAttributeCount()
	 */
	public int getAttributeCount() {
		return finished ? -1 : source.getAttributeCount();
	}

	/*
	 * (non-Javadoc)
	 * @see org.xmlpull.v1.XmlPullParser#getAttributeName(int)
	 */
	public String getAttributeName(int index) {
		if (finished) {
			throw new IndexOutOfBoundsException();
		}
		return source.getAttributeName(index);
	}

	/*
	 * (non-Javadoc)
	 * @see org.xmlpull.v1.XmlPullParser#getAttributeNamespace(int)
	 */
	public String getAttributeNamespace(int index) {
		if (finished) {
			throw new IndexOutOfBoundsException();
		}
		return source.getAttributeNamespace(index);
	}

	/*
	 * (non-Javadoc)
	 * @see org.xmlpull.v1.XmlPullParser#getAttributePrefix(int)
	 */
	public String getAttributePrefix(int index) {
		if (finished) {
			throw new IndexOutOfBoundsException();
		}
		return source.getAttributePrefix(index);
	}

	/*
	 * (non-Javadoc)
	 * @see org.xmlpull.v1.XmlPullParser#getAttributeType(int)
	 */
	public String getAttributeType(int index) {
		return "CDATA";
	}

	/*
	 * (non-Javadoc)
	 * @see org.xmlpull.v1.XmlPullParser#getAttributeValue(int)
	 */
	public String getAttributeValue(int index) {
		if (finished) {
			throw new IndexOutOfBoundsException();
		}
		return source.getAttributeValue(index);
	}

	/*
	 * (non-Javadoc)
	 * @see org.xmlpull.v1.XmlPullParser#getAttributeValue(java.lang.String,
	 * java.lang.String)
	 */
	public String getAttributeValue(String namespace, String name) {
		if (finished) {
			throw new IndexOutOfBoundsException();
		}
		return source.getAttributeValue(namespace, name);
	}

	/*
	 * (non-Javadoc)
	 * @see org.xmlpull.v1.XmlPullParser#getColumnNumber()
	 */
	public int getColumnNumber() {
		return source.getColumnNumber();
	}

	/*
	 * (non-Javadoc)
	 * @see org.xmlpull.v1.XmlPullParser#getDepth()
	 */
	public int getDepth() {
		return source.getDepth() - elementDepth;
	}

	/*
	 * (non-Javadoc)
	 * @see org.xmlpull.v1.XmlPullParser#getEventType()
	 */
	public int getEventType() throws XmlPullParserException {
		return finished ? END_DOCUMENT : source.getEventType();
	}

	/*
	 * (non-Javadoc)
	 * @see org.xmlpull.v1.XmlPullParser#getFeature(java.lang.String)
	 */
	public boolean getFeature(String name) {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * @see org.xmlpull.v1.XmlPullParser#getInputEncoding()
	 */
	public String getInputEncoding() {
		return source.getInputEncoding();
	}

	/*
	 * (non-Javadoc)
	 * @see org.xmlpull.v1.XmlPullParser#getLineNumber()
	 */
	public int getLineNumber() {
		return source.getLineNumber();
	}

	/*
	 * (non-Javadoc)
	 * @see org.xmlpull.v1.XmlPullParser#getName()
	 */
	public String getName() {
		return finished ? null : source.getName();
	}

	/*
	 * (non-Javadoc)
	 * @see org.xmlpull.v1.XmlPullParser#getNamespace()
	 */
	public String getNamespace() {
		return finished ? null : source.getNamespace();
	}

	/*
	 * (non-Javadoc)
	 * @see org.xmlpull.v1.XmlPullParser#getNamespace(java.lang.String)
	 */
	public String getNamespace(String prefix) {
		return source.getNamespace(prefix);
	}

	/*
	 * (non-Javadoc)
	 * @see org.xmlpull.v1.XmlPullParser#getNamespaceCount(int)
	 */
	public int getNamespaceCount(int depth) throws XmlPullParserException {
		return source.getNamespaceCount(depth + elementDepth);
	}

	/*
	 * (non-Javadoc)
	 * @see org.xmlpull.v1.XmlPullParser#getNamespacePrefix(int)
	 */
	public String getNamespacePrefix(int pos) throws XmlPullParserException {
		return source.getNamespacePrefix(pos);
	}

	/*
	 * (non-Javadoc)
	 * @see org.xmlpull.v1.XmlPullParser#getNamespaceUri(int)
	 */
	public String getNamespaceUri(int pos) throws XmlPullParserException {
		return source.getNamespaceUri(pos);
	}

	/*
	 * (non-Javadoc)
	 * @see org.xmlpull.v1.XmlPullParser#getPositionDescription()
	 */
	public String getPositionDescription() {
		return source.getPositionDescription();
	}

	/*
	 * (non-Javadoc)
	 * @see org.xmlpull.v1.XmlPullParser#getPrefix()
	 */
	public String getPrefix() {
		return finished ? null : source.getPrefix();
	}

	/*
	 * (non-Javadoc)
	 * @see org.xmlpull.v1.XmlPullParser#getProperty(java.lang.String)
	 */
	public Object getProperty(String name) {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.xmlpull.v1.XmlPullParser#getText()
	 */
	public String getText() {
		return finished ? null : source.getText();
	}

	/*
	 * (non-Javadoc)
	 * @see org.xmlpull.v1.XmlPullParser#getTextCharacters(int[])
	 */
	public char[] getTextCharacters(int[] holderForStartAndLength) {
		return finished ? null : source.getTextCharacters(holderForStartAndLength);
	}

	/*
	 * (non-Javadoc)
	 * @see org.xmlpull.v1.XmlPullParser#isAttributeDefault(int)
	 */
	public boolean isAttributeDefault(int index) {
		return finished ? false : source.isAttributeDefault(index);
	}

	/*
	 * (non-Javadoc)
	 * @see org.xmlpull.v1.XmlPullParser#isEmptyElementTag()
	 */
	public boolean isEmptyElementTag() throws XmlPullParserException {
		if (finished) {
			throw new XmlPullParserException(ILLEGAL_TYPE, this, null);
		}
		return source.isEmptyElementTag();
	}

	/*
	 * (non-Javadoc)
	 * @see org.xmlpull.v1.XmlPullParser#isWhitespace()
	 */
	public boolean isWhitespace() throws XmlPullParserException {
		if (finished) {
			throw new XmlPullParserException(AT_END_OF_DOCUMENT, this, null);
		}
		return source.isWhitespace();
	}

	/*
	 * (non-Javadoc)
	 * @see org.xmlpull.v1.XmlPullParser#next()
	 */
	public int next() throws XmlPullParserException, IOException {
		checkFinished();
		return finished ? END_DOCUMENT : source.next();
	}

	/*
	 * (non-Javadoc)
	 * @see org.xmlpull.v1.XmlPullParser#nextTag()
	 */
	public int nextTag() throws XmlPullParserException, IOException {
		checkFinished();
		if (finished) {
			throw new XmlPullParserException(AT_END_OF_DOCUMENT, this, null);
		}
		return source.nextTag();
	}

	/*
	 * (non-Javadoc)
	 * @see org.xmlpull.v1.XmlPullParser#nextText()
	 */
	public String nextText() throws XmlPullParserException, IOException {
		checkFinished();
		if (finished) {
			throw new XmlPullParserException(AT_END_OF_DOCUMENT, this, null);
		}
		return source.nextText().trim();
	}

	/*
	 * (non-Javadoc)
	 * @see org.xmlpull.v1.XmlPullParser#nextToken()
	 */
	public int nextToken() throws XmlPullParserException, IOException {
		checkFinished();
		return finished ? END_DOCUMENT : source.nextToken();
	}

	/*
	 * (non-Javadoc)
	 * @see org.xmlpull.v1.XmlPullParser#require(int, java.lang.String,
	 * java.lang.String)
	 */
	public void require(int type, String namespace, String name) throws XmlPullParserException, IOException {
		if (finished) {
			throw new XmlPullParserException(AT_END_OF_DOCUMENT, this, null);
		}
		source.require(type, namespace, name);
	}

	/*
	 * (non-Javadoc)
	 * @see org.xmlpull.v1.XmlPullParser#setFeature(java.lang.String, boolean)
	 */
	public void setFeature(String name, boolean state) throws XmlPullParserException {
		throw new XmlPullParserException("This parser doesn't support features", this, null);
	}

	/*
	 * (non-Javadoc)
	 * @see org.xmlpull.v1.XmlPullParser#setInput(java.io.Reader)
	 */
	public void setInput(Reader in) throws XmlPullParserException {
		throw new XmlPullParserException("This parser doesn't support resetting", this, null);
	}

	/*
	 * (non-Javadoc)
	 * @see org.xmlpull.v1.XmlPullParser#setInput(java.io.InputStream,
	 * java.lang.String)
	 */
	public void setInput(InputStream inputStream, String inputEncoding) throws XmlPullParserException {
		throw new XmlPullParserException("This parser doesn't support resetting", this, null);
	}

	/*
	 * (non-Javadoc)
	 * @see org.xmlpull.v1.XmlPullParser#setProperty(java.lang.String,
	 * java.lang.Object)
	 */
	public void setProperty(String name, Object value) throws XmlPullParserException {
		throw new XmlPullParserException("This parser doesn't support properties", this, null);
	}

	public LocalizedString nextLocalizedString() throws XmlPullParserException, IOException {
		LocalizedString result;
		String lang = null;
		int attributeCount = getAttributeCount();
		HashMap attributes = null;
		for (int i = 0; i < attributeCount; i++) {
			String namespace = getAttributeNamespace(i);
			String name = getAttributeName(i);
			String value = getAttributeValue(i);
			if (XMLConstants.XML_NAMESPACE_NAME.equals(namespace) && XMLConstants.XML_ATTRIBUTE_LANGUAGE.equals(name)) {
				lang = value;
			} else {
				if (attributes == null) {
					attributes = new HashMap();
				}
				attributes.put(new QName(name, namespace), value);
			}
		}
		result = new LocalizedString(nextText().trim(), lang);
		if (attributes != null) {
			result.setUnknownAttributes(attributes);
		}
		return result;
	}

	public AttributedURI nextAttributedUri() throws XmlPullParserException, IOException {
		AttributedURI result;
		int attributeCount = getAttributeCount();
		if (attributeCount > 0) {
			HashMap attributes = new HashMap();
			for (int i = 0; i < attributeCount; i++) {
				String namespace = getAttributeNamespace(i);
				String name = getAttributeName(i);
				String value = getAttributeValue(i);
				attributes.put(new QName(name, namespace), value);
			}
			result = new AttributedURI(nextText().trim(), attributes);
		} else {
			result = new AttributedURI(nextText().trim());
		}
		return result;
	}

	/**
	 * @return the next text as {@link QName}
	 * @throws XmlPullParserException
	 * @throws IOException
	 */
	public QName nextQName() throws XmlPullParserException, IOException {
		return createQName(nextText());
	}

	/**
	 * @param i
	 * @return the value of the attribute at index <code>i</code> as
	 *         {@link QName}
	 */
	public QName getAttributeValueAsQName(int i) {
		return createQName(getAttributeValue(i));
	}

	// private ArrayList parseServiceName() throws XmlPullParserException,
	// IOException {
	// ArrayList list = new ArrayList();
	// ElementParser parser = new ElementParser(this);
	// int attributeCount = parser.getAttributeCount();
	// if (attributeCount > 0) {
	// String value = parser.getAttributeValue(0);
	// list.add(value);
	// }
	// QName serviceName = parser.nextQName();
	// list.add(serviceName);
	// return list;
	// }

	/**
	 * @return the next {@link QNameSet}
	 * @throws XmlPullParserException
	 * @throws IOException
	 */
	public QNameSet nextQNameSet() throws XmlPullParserException, IOException {
		QNameSet qNameSet = new QNameSet();
		String value = nextText();
		int pos1 = -1;
		int pos2 = pos1;
		do {
			pos1 = nextNonWhiteSpace(value, pos1);
			if (pos1 == -1) {
				break;
			}
			pos2 = nextWhiteSpace(value, pos1);
			if (pos2 == -1) {
				pos2 = value.length();
			}
			String rawQName = value.substring(pos1, pos2);
			qNameSet.add(createQName(rawQName));
			pos1 = pos2;
		} while (pos1 != -1);
		return qNameSet;
	}

	public ScopeSet nextScopeSet() throws XmlPullParserException, IOException {
		ScopeSet scopeSet = new ScopeSet();
		int attributeCount = getAttributeCount();
		for (int i = 0; i < attributeCount; i++) {
			String namespace = getAttributeNamespace(i);
			String name = getAttributeName(i);
			String value = getAttributeValue(i);
			scopeSet.addUnknownAttribute(new QName(name, namespace), value);
		}
		nextScopeSet(scopeSet);
		return scopeSet;
	}

	public ProbeScopeSet nextProbeScopeSet() throws XmlPullParserException, IOException {
		ProbeScopeSet scopeSet = new ProbeScopeSet();
		int attributeCount = getAttributeCount();
		String matchBy = WSDConstants.WSD_MATCHING_RULE_DEFAULT;
		for (int i = 0; i < attributeCount; i++) {
			String namespace = getAttributeNamespace(i);
			String name = getAttributeName(i);
			String value = getAttributeValue(i);
			if ("".equals(namespace) && WSDConstants.WSD_ATTR_MATCH_BY.equals(name)) {
				matchBy = value;
			} else {
				scopeSet.addUnknownAttribute(new QName(name, namespace), value);
			}
		}
		scopeSet.setMatchBy(matchBy);
		nextScopeSet(scopeSet);
		return scopeSet;
	}

	/**
	 * Parses scopes list and adds scope to given scope set.
	 * 
	 * @param scopes
	 * @throws XmlPullParserException
	 * @throws IOException
	 */
	public void nextScopeSet(ScopeSet scopes) throws XmlPullParserException, IOException {
		String value = nextText();
		int pos1 = -1;
		int pos2 = pos1;
		do {
			pos1 = nextNonWhiteSpace(value, pos1);
			if (pos1 == -1) {
				break;
			}
			pos2 = nextWhiteSpace(value, pos1);
			if (pos2 == -1) {
				pos2 = value.length();
			}
			String uri = value.substring(pos1, pos2);
			scopes.addScope(uri);
			pos1 = pos2;
		} while (pos1 != -1);
	}

	/**
	 * Parses uri list and returns uri set.
	 * 
	 * @return set of uris
	 * @throws XmlPullParserException
	 * @throws IOException
	 */
	public URISet nextUriSet() throws XmlPullParserException, IOException {
		URISet uriSet = new URISet();
		String value = nextText();
		int pos1 = -1;
		int pos2 = pos1;
		do {
			pos1 = nextNonWhiteSpace(value, pos1);
			if (pos1 == -1) {
				break;
			}
			pos2 = nextWhiteSpace(value, pos1);
			if (pos2 == -1) {
				pos2 = value.length();
			}
			String uri = value.substring(pos1, pos2);
			uriSet.add(new URI(uri));
			pos1 = pos2;
		} while (pos1 != -1);
		return uriSet;
	}

	public UnknownDataContainer nextGenericElement(UnknownDataContainer container) throws XmlPullParserException, IOException {
		ElementParser parser = new ElementParser(this);
		int attributeCount = parser.getAttributeCount();
		if (attributeCount > 0) {
			for (int i = 0; i < attributeCount; i++) {
				container.addUnknownAttribute(new QName(parser.getAttributeName(i), parser.getAttributeNamespace(i)), parser.getAttributeValue(i));
			}
		}
		while (nextTag() != END_TAG) {
			String namespace = parser.getNamespace();
			String name = parser.getName();
			QName elementName = new QName(name, namespace);
			Object result = chainHandler(elementName);
			if (result != null) {
				container.addUnknownElement(elementName, result);
			}
		}
		return container;
	}

	public void handleUnknownAttributes(UnknownDataContainer container) {
		int count = getAttributeCount();
		for (int i = 0; i < count; i++) {
			String namespace = getAttributeNamespace(i);
			String name = getAttributeName(i);
			String value = getAttributeValue(i);
			container.addUnknownAttribute(new QName(name, namespace), value);
		}
	}

	/**
	 * @param container
	 * @param namespace
	 * @param name
	 * @throws XmlPullParserException
	 * @throws IOException
	 */
	public void addUnknownElement(UnknownDataContainer container, String namespace, String name) throws XmlPullParserException, IOException {
		QName childName = new QName(name, namespace);
		Object value = chainHandler(childName);
		if (value != null) {
			container.addUnknownElement(childName, value);
		}
	}

	/**
	 * Equivalent to {@link #chainHandler(QName, boolean)
	 * chainHandler(elementName, true)}.
	 * 
	 * @param elementName
	 * @return the representation of the parsed element block or
	 *         <code>null</code>
	 * @throws XmlPullParserException
	 * @throws IOException
	 */
	public Object chainHandler(QName elementName) throws XmlPullParserException, IOException {
		return chainHandler(elementName, true);
	}

	/**
	 * Searches for a registered {@link ElementHandler} capable of processing
	 * the element with the specified qualified name <code>elementName</code>.
	 * If it finds one, the XML stream represented by this {@link ElementParser}
	 * instance is passed to it and this method returns the result of its
	 * {@link ElementHandler#handleElement(QName, ElementParser)} method.
	 * Otherwise, if the flag <code>consume</code> is set to <code>true</code>,
	 * the complete XML block from the current element's start tag to the
	 * corresponding end tag is silently ignored and this method returns
	 * <code>null</code>. Finally, if <code>consume</code> is <code>false</code>
	 * , this method returns <code>null</code> immediately (without advancing
	 * this parser's state).
	 * 
	 * @param elementName the name of the element to process
	 * @param consume whether to consume the entire element block in case there
	 *            is no registered {@link ElementHandler element handler} for it
	 * @return the representation of the parsed element block or
	 *         <code>null</code>
	 * @throws XmlPullParserException
	 * @throws IOException
	 */
	public Object chainHandler(QName elementName, boolean consume) throws XmlPullParserException, IOException {
		ElementParser parser = null;
		ElementHandler handler = ElementHandlerRegistry.getRegistry().getElementHandler(elementName);
		if (handler != null) {
			parser = new ElementParser(this);
			Object result = handler.handleElement(elementName, parser);
			while (!parser.finished) {
				parser.next();
			}
			return result;
		}
		if (consume) {
			parser = new ElementParser(this);
			parser.consume();
		}
		return null;
	}

	/**
	 * Advances this element parser instance to its end.
	 * 
	 * @throws XmlPullParserException
	 * @throws IOException
	 */
	public void consume() throws XmlPullParserException, IOException {
		// advance parser to corresponding closing tag
		int event = getEventType();
		while (event != END_TAG || source.getDepth() > elementDepth) {
			event = next();
		}
	}

	/**
	 * Creates a qualified name from its prefixed string representation. Takes
	 * care about resolving the namespace prefix.
	 * 
	 * @param rawQName a string representation of the qualified name in prefixed
	 *            form
	 * @return the qualified name including its correct namespace
	 */
	public QName createQName(String rawQName) {
		int idx = rawQName.indexOf(':');
		if (idx == -1) {
			idx = rawQName.lastIndexOf('/');
			if (idx == -1) {
				return new QName(rawQName, getNamespace(null));
			}
			String localPart = rawQName.substring(idx + 1);
			String namespace = rawQName.substring(0, idx);
			return new QName(localPart, namespace);
		}
		String localPart = rawQName.substring(idx + 1);
		String namespacePrefix = rawQName.substring(0, idx);
		return new QName(localPart, getNamespace(namespacePrefix));
	}

	private void checkFinished() throws XmlPullParserException {
		// intercept element end at same depth as elementDepth
		int current = source.getEventType();
		if (current == END_TAG && source.getDepth() == elementDepth) {
			finished = true;
		}
	}

}
