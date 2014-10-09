/*******************************************************************************
 * Copyright (c) 2009 MATERNA Information & Communications. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html. For further
 * project-related information visit http://www.ws4d.org. The most recent
 * version of the JMEDS framework can be obtained from
 * http://sourceforge.net/projects/ws4d-javame.
 ******************************************************************************/
package org.ws4d.java.html;

import org.ws4d.java.structures.Iterator;
import org.ws4d.java.structures.LinkedList;
import org.ws4d.java.structures.List;

/**
 * This class represents a HTML document. Can be used for text/html HTTP
 * response.
 */
public class SimpleHTML implements HTMLDocument {

	private static final String	HTML		= "html";

	private static final String	HEAD		= "head";

	private static final String	TITLE		= "title";

	private static final String	BODY		= "body";

	private static final String	HEADING1	= "h1";

	private static final String	PARAGRAPH	= "p";

	private static final String	BEGINTAG	= "<";

	private static final String	ENDTAG		= ">";

	private static final String	SEP			= "/";

	private String				title		= null;

	private List				content		= null;

	/**
	 * <code>SimpleHTML</code>.
	 * 
	 * @param title title of this HTML document.
	 */
	public SimpleHTML(String title) {
		this.title = title;
		content = new LinkedList();
	}

	/**
	 * Adds heading to this HTML document.
	 * 
	 * @param text the content.
	 */
	public void addHeading(String text) {
		if (text == null) return;
		StringBuffer paragraph = new StringBuffer();
		openTag(paragraph, HEADING1);
		paragraph.append(text);
		closeTag(paragraph, HEADING1);
		content.add(paragraph.toString());
	}

	/**
	 * Adds paragraph to this HTML document.
	 * 
	 * @param text the content.
	 */
	public void addParagraph(String text) {
		if (text == null) return;
		StringBuffer paragraph = new StringBuffer();
		openTag(paragraph, PARAGRAPH);
		paragraph.append(text);
		closeTag(paragraph, PARAGRAPH);
		content.add(paragraph.toString());
	}

	/**
	 * Adds horizontal rule to this HTML document.
	 */
	public void addHorizontalRule() {
		content.add("<hr />");
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		StringBuffer buf = new StringBuffer();
		// HTML
		openTag(buf, HTML);

		// HEAD
		openTag(buf, HEAD);
		openTag(buf, TITLE);
		buf.append(title);
		closeTag(buf, TITLE);
		closeTag(buf, HEAD);

		// BODY
		openTag(buf, BODY);
		if (content != null && content.size() > 0) {
			Iterator e = content.iterator();
			while (e.hasNext()) {
				String c = (String) e.next();
				buf.append(c);
			}

		}
		closeTag(buf, BODY);

		closeTag(buf, HTML);
		return buf.toString();
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.types.html.HTMLDocument#getData()
	 */
	public byte[] getData() {
		return toString().getBytes();
	}

	private void openTag(StringBuffer buf, String name) {
		buf.append(BEGINTAG);
		buf.append(name);
		buf.append(ENDTAG);
	}

	private void closeTag(StringBuffer buf, String name) {
		buf.append(BEGINTAG);
		buf.append(SEP);
		buf.append(name);
		buf.append(ENDTAG);
	}
}
