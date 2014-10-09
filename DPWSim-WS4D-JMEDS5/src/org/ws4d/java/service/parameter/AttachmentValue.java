/*******************************************************************************
 * Copyright (c) 2009 MATERNA Information & Communications. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html. For further
 * project-related information visit http://www.ws4d.org. The most recent
 * version of the JMEDS framework can be obtained from
 * http://sourceforge.net/projects/ws4d-javame.
 ******************************************************************************/
package org.ws4d.java.service.parameter;

import java.io.IOException;

import org.ws4d.java.attachment.Attachment;
import org.ws4d.java.attachment.AttachmentStub;
import org.ws4d.java.attachment.IncomingAttachment;
import org.ws4d.java.communication.ContextID;
import org.ws4d.java.constants.XOPConstants;
import org.ws4d.java.structures.List;
import org.ws4d.java.types.QName;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

public class AttachmentValue extends ParameterDefinition {

	protected Attachment	attachment	= null;

	private boolean			delegate	= true;

	public AttachmentValue() {

	}

	public AttachmentValue(String href) {
		attachment = new AttachmentStub(href);
	}

	/**
	 * Returns an attachment for this parameter value.
	 * 
	 * @return the attachment for this parameter value.
	 */
	public IncomingAttachment getAttachment() {
		return (IncomingAttachment) attachment;
	}

	/**
	 * Sets the attachment for this parameter value.
	 * 
	 * @param attachment the attachment to set.
	 */
	public void setAttachment(Attachment attachment) {
		this.attachment = attachment;
		this.delegate = false;
	}

	public void setAttachmentScope(ContextID context) {
		if (delegate) {
			((AttachmentStub) attachment).setAttachmentScope(context);

		}
	}

	public void initialize(String href) {
		attachment = new AttachmentStub(href);
	}

	public List getNamespaces() {
		List ns = super.getNamespaces();
		ns.add(new QName(XOPConstants.XOP_ELEM_INCLUDE, XOPConstants.XOP_NAMESPACE_NAME, XOPConstants.XOP_NAMESPACE_PREFIX));
		return ns;
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.service.parameter.Value#getType()
	 */
	public int getValueType() {
		return TYPE_ATTACHMENT;
	}

	public String serialize() {
		// TODO Auto-generated method stub
		return null;
	}

	public void parse(String content) {
		// TODO Auto-generated method stub

	}

	public void parseContent(XmlPullParser parser) throws IOException, XmlPullParserException {
		int tag = parser.getEventType();
		boolean xop = false;
		if (tag == XmlPullParser.START_TAG && XOPConstants.XOP_NAMESPACE_NAME.equals(parser.getNamespace()) && XOPConstants.XOP_ELEM_INCLUDE.equals(parser.getName())) {
			xop = true;
		}
		// XOP:Include start tag
		if (!xop) {
			tag = parser.nextTag();
		}
		if (tag == XmlPullParser.START_TAG && XOPConstants.XOP_NAMESPACE_NAME.equals(parser.getNamespace()) && XOPConstants.XOP_ELEM_INCLUDE.equals(parser.getName())) {
			String href = parser.getAttributeValue(null, XOPConstants.XOP_ATTRIB_HREF);
			/*
			 * Strip the cid prefix from this href ! :D
			 */
			if (href.startsWith(XOPConstants.XOP_CID_PREFIX)) {
				href = href.substring(XOPConstants.XOP_CID_PREFIX.length(), href.length());
			}
			attachment = new AttachmentStub(href);
		} else {
			throw new IOException("Cannot create attachment. Element xop:include not found.");
		}
		// XOP:Include end tag
		if (!xop) {
			tag = parser.nextTag();
		}
	}

	public void serializeContent(XmlSerializer serializer) throws IOException {
		if (attachment != null) {
			/*
			 * Serialize the XOP include element with attachment cid
			 */
			String cid = attachment.getContentId();

			serializer.startTag(XOPConstants.XOP_NAMESPACE_NAME, XOPConstants.XOP_ELEM_INCLUDE);
			serializer.attribute(null, XOPConstants.XOP_ATTRIB_HREF, XOPConstants.XOP_CID_PREFIX + cid);
			serializer.endTag(XOPConstants.XOP_NAMESPACE_NAME, XOPConstants.XOP_ELEM_INCLUDE);
		}

	}
}
