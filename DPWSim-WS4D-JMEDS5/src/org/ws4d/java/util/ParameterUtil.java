/*******************************************************************************
 * Copyright (c) 2009 MATERNA Information & Communications. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html. For further
 * project-related information visit http://www.ws4d.org. The most recent
 * version of the JMEDS framework can be obtained from
 * http://sourceforge.net/projects/ws4d-javame.
 ******************************************************************************/
package org.ws4d.java.util;

import java.io.IOException;

import org.ws4d.java.DPWSFramework;
import org.ws4d.java.attachment.Attachment;
import org.ws4d.java.attachment.AttachmentException;
import org.ws4d.java.attachment.AttachmentFactory;
import org.ws4d.java.attachment.IncomingAttachment;
import org.ws4d.java.attachment.OutgoingAttachment;
import org.ws4d.java.attachment.interfaces.incoming.IncomingFileAttachment;
import org.ws4d.java.communication.ContextID;
import org.ws4d.java.schema.SchemaUtil;
import org.ws4d.java.service.parameter.AttachmentValue;
import org.ws4d.java.service.parameter.ParameterValue;
import org.ws4d.java.service.parameter.QNameValue;
import org.ws4d.java.service.parameter.StringValue;
import org.ws4d.java.structures.Iterator;
import org.ws4d.java.structures.LinkedList;
import org.ws4d.java.structures.List;
import org.ws4d.java.types.QName;

/**
 * Utility class for easier parameter handling.
 * <p>
 * This class implements methods for parameters based on the default
 * implementation inside the framework. It allows to handle
 * {@link AttachmentValue} and {@link StringValue} as before.
 * </p>
 */
public class ParameterUtil {

	/**
	 * Returns the value of the attribute.
	 * 
	 * @param wVal the parameter which should be used to get the attributes
	 *            from, or should be used as parent.
	 * @param path the path which should be used to get the child of the given
	 *            parameter.
	 * @param attribute the attribute.
	 * @return the attribute value.
	 */
	public static String getAttributeValue(ParameterValue wVal, String path, String attribute) {
		ParameterValue v = wVal.get(path);
		if (v == null) return null;
		return v.getAttributeValue(attribute);
	}

	/**
	 * @param wVal the parameter which should be used to get the attributes
	 *            from, or should be used as parent.
	 * @param path the path which should be used to get the child of the given
	 *            parameter.
	 * @param attribute the attribute.
	 * @param value the attribute value.
	 */
	public static void setAttributeValue(ParameterValue wVal, String path, String attribute, String value) {
		ParameterValue v = wVal.get(path);
		if (v == null) return;
		v.setAttributeValue(attribute, value);
	}

	/**
	 * Sets the value for a {@link StringValue} based parameter.
	 * 
	 * @param wVal the parameter from type {@link StringValue}, or the parent of
	 *            it.
	 * @param path the path which allows to address a child of the given
	 *            parameter.
	 * @param value the value.
	 */
	public static void setString(ParameterValue wVal, String path, String value) {
		ParameterValue pv = wVal.get(path);
		if (pv.getValueType() == ParameterValue.TYPE_STRING) {
			StringValue sv = (StringValue) pv;
			sv.set(value);
		} else {
			throw new RuntimeException("Cannot set string value. Parameter is not a string.");
		}
	}

	/**
	 * Returns the value for a {@link StringValue} based parameter.
	 * 
	 * @param wVal the parameter from type {@link StringValue}, or the parent of
	 *            it.
	 * @param path the path which allows to address a child of the given
	 *            parameter.
	 * @return the value.
	 */
	public static String getString(ParameterValue wVal, String path) {
		ParameterValue pv = wVal.get(path);
		if (pv.getValueType() == ParameterValue.TYPE_STRING) {
			StringValue sv = (StringValue) pv;
			return sv.get();
		}
		return null;
	}

	/**
	 * Returns <code>true</code> if any of the values (even the inner-elements)
	 * is an attachment, <code>false</code> otherwise.
	 * 
	 * @param pv the parameter which should be checked for attachments.
	 * @return <code>true</code> if any of the values (even the inner-elements)
	 *         is an attachment, <code>false</code> otherwise.
	 */
	public static boolean hasAttachment(ParameterValue pv) {
		boolean result = false;
		if (pv.getValueType() == ParameterValue.TYPE_ATTACHMENT) result |= true;
		Iterator it = pv.children();
		while (it.hasNext() && !result) {
			ParameterValue child = (ParameterValue) it.next();
			if (child.getType() == SchemaUtil.getSchemaType(SchemaUtil.TYPE_BASE64_BINARY)) {
				/*
				 * TODO: Should we return true if there is a binary type, but no
				 * attachment set?
				 */
			}
			if (child.getValueType() == ParameterValue.TYPE_ATTACHMENT) result |= true;
			result |= hasAttachment(child);
		}
		return result;
	}

	/**
	 * Sets the attachments scope for the given parameter and his children.
	 * 
	 * @param wVal the parameter which should be used to set the scope
	 * @param context the MIME context identifier.
	 */
	public static void setAttachmentScope(ParameterValue wVal, ContextID context) {
		if (wVal.getValueType() == ParameterValue.TYPE_ATTACHMENT) {
			AttachmentValue av = (AttachmentValue) wVal;
			av.setAttachmentScope(context);
		}
		Iterator it = wVal.getChildrenList();
		while (it.hasNext()) {
			ParameterValue pv = (ParameterValue) it.next();
			setAttachmentScope(pv, context);
		}
	}

	/**
	 * Returns a list of attachments from the given structure. Every attachment
	 * used inside this structure will be in this list.
	 * 
	 * @param wVal the parameter which should be used to collect the
	 *            attachments.
	 * @return the list of attachments.
	 */
	public static List getAttachments(ParameterValue wVal) {
		List attachments = new LinkedList();
		getAttachments(wVal, attachments);
		return attachments;
	}

	private static void getAttachments(ParameterValue wVal, List attachments) {
		if (wVal.getValueType() == ParameterValue.TYPE_ATTACHMENT) {
			AttachmentValue av = (AttachmentValue) wVal;
			Attachment attachment = av.getAttachment();
			attachments.add(attachment);
		}
		Iterator it = wVal.getChildrenList();
		while (it.hasNext()) {
			ParameterValue pv = (ParameterValue) it.next();
			getAttachments(pv, attachments);
		}
	}

	/**
	 * Return the filename of the attachment. (Only for {@link FileAttachment})
	 * 
	 * @param wVal the parameter which should be used to determinate the
	 *            filename.
	 * @return the filename if attachment is {@link FileAttachment} else null
	 */
	public static String getAttachmentFilename(ParameterValue wVal) {
		if (wVal.getValueType() == ParameterValue.TYPE_ATTACHMENT) {
			AttachmentValue av = (AttachmentValue) wVal;
			IncomingFileAttachment attachment = (IncomingFileAttachment) av.getAttachment();
			try {
				return attachment.getFilePath();
			} catch (AttachmentException e) {
				//
			}
		}

		return null;
	}

	/**
	 * Removes attachment from a given parameter.
	 * 
	 * @param wVal the parameter which should get the attachment removed.
	 */
	public static void removeAttachment(ParameterValue wVal) {
		if (wVal.getValueType() == ParameterValue.TYPE_ATTACHMENT) {
			AttachmentValue av = (AttachmentValue) wVal;
			av.setAttachment((OutgoingAttachment) null);
		}
	}

	/**
	 * Creates new FileAttachment with the given filename.
	 * 
	 * @param wVal the parameter which should be used to set the attachment to.
	 * @param filename filename of the attachment.
	 */
	public static void setAttachment(ParameterValue wVal, String filename) {
		if (filename == null) return;
		String contentType = MIMEUtil.estimateContentType(filename);

		try {
			AttachmentFactory afac = DPWSFramework.getAttachmentFactory();
			OutgoingAttachment attachment = afac.createFileAttachment(filename, contentType);

			setAttachment(wVal, null, attachment);
		} catch (IOException e) {
			Log.error("Cannot set attachment. " + e.getMessage());
		}
	}

	/**
	 * Returns the value for a {@link AttachmentValue} based parameter.
	 * 
	 * @param wVal the parameter from type {@link AttachmentValue}, or the
	 *            parent of it.
	 * @param path the path which allows to address a child of the given
	 *            parameter.
	 * @return the value.
	 */
	public static IncomingAttachment getAttachment(ParameterValue wVal, String path) {
		ParameterValue pv = wVal.get(path);
		if (pv.getValueType() == ParameterValue.TYPE_ATTACHMENT) {
			AttachmentValue av = (AttachmentValue) pv;
			return av.getAttachment();
		}
		return null;
	}

	/**
	 * Sets the value for a {@link AttachmentValue} based parameter.
	 * 
	 * @param wVal the parameter from type {@link AttachmentValue}, or the
	 *            parent of it.
	 * @param path the path which allows to address a child of the given
	 *            parameter.
	 * @param value the value.
	 */
	public static void setAttachment(ParameterValue wVal, String path, Attachment attachment) {
		ParameterValue pv = wVal.get(path);
		if (pv.getValueType() == ParameterValue.TYPE_ATTACHMENT) {
			AttachmentValue av = (AttachmentValue) pv;
			av.setAttachment(attachment);
		} else {
			throw new RuntimeException("Cannot set attachment value. Parameter is not a attachment.");
		}
	}

	/**
	 * Sets the value for a {@link QNameValue} based parameter.
	 * 
	 * @param wVal the parameter from type {@link QNameValue}, or the parent of
	 *            it.
	 * @param path the path which allows to address a child of the given
	 *            parameter.
	 * @param value the value.
	 */
	public static void setQName(ParameterValue wVal, String path, QName value) {
		ParameterValue pv = wVal.get(path);
		if (pv.getValueType() == ParameterValue.TYPE_QNAME) {
			QNameValue qv = (QNameValue) pv;
			qv.set(value);
		} else {
			throw new RuntimeException("Cannot set qualified name value. Parameter is not a qualified name.");
		}
	}

	/**
	 * Returns the value for a {@link QNameValue} based parameter.
	 * 
	 * @param wVal the parameter from type {@link QNameValue}, or the parent of
	 *            it.
	 * @param path the path which allows to address a child of the given
	 *            parameter.
	 * @return the value.
	 */
	public static QName getQName(ParameterValue wVal, String path) {
		ParameterValue pv = wVal.get(path);
		if (pv.getValueType() == ParameterValue.TYPE_QNAME) {
			QNameValue qv = (QNameValue) pv;
			return qv.get();
		}
		return null;
	}

}
