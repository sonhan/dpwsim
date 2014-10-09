package org.ws4d.java.attachment;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.ws4d.java.attachment.interfaces.incoming.IncomingFileAttachment;
import org.ws4d.java.communication.ContextID;
import org.ws4d.java.types.InternetMediaType;

public class AttachmentStub implements IncomingFileAttachment {

	private final String		contentId;

	private IncomingAttachment	delegate;

	private ContextID			attachmentScope;

	/**
	 * @param contentId
	 */
	public AttachmentStub(String contentId) {
		super();
		this.contentId = contentId;
	}

	public ContextID getAttachmentScope() {
		return attachmentScope;
	}

	public void setAttachmentScope(ContextID attachmentScope) {
		this.attachmentScope = attachmentScope;
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.data.Attachment#dispose()
	 */
	public void dispose() {
		if (delegate != null) {
			delegate.dispose();
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.data.Attachment#getBytes()
	 */
	public byte[] getBytes() throws AttachmentException, IOException {
		if (delegate == null) {
			resolve();
		}
		return delegate.getBytes();
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.data.Attachment#getContentId()
	 */
	public String getContentId() {
		return contentId;
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.data.Attachment#getContentType()
	 */
	public InternetMediaType getContentType() throws AttachmentException {
		if (delegate == null) {
			resolve();
		}
		return delegate.getContentType();
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.data.Attachment#getInputStream()
	 */
	public InputStream getInputStream() throws AttachmentException, IOException {
		if (delegate == null) {
			resolve();
		}
		return delegate.getInputStream();
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.data.Attachment#getTransferEncoding()
	 */
	public String getTransferEncoding() throws AttachmentException {
		if (delegate == null) {
			resolve();
		}
		return delegate.getTransferEncoding();
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.attachment.Attachment#getType()
	 */
	public int getType() throws AttachmentException {
		if (delegate == null) {
			resolve();
		}
		return delegate.getType();
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.attachment.Attachment#isAvailable()
	 */
	public boolean isAvailable() {
		try {
			return AttachmentStore.getInstance().isAvailable(attachmentScope, contentId);
		} catch (AttachmentException e) {
			return false;
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.attachment.Attachment#size()
	 */
	public long size() throws AttachmentException {
		if (delegate == null) {
			resolve();
		}
		return delegate.size();
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.attachment.Attachment#isLocal()
	 */
	public boolean isLocal() {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.attachment.Attachment#getFilePath()
	 */
	public String getFilePath() throws AttachmentException {
		if (delegate == null) {
			resolve();
		}

		return ((IncomingFileAttachment) delegate).getFilePath();
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.attachment.Attachment#save(java.lang.String)
	 */
	public void save(String targetFilePath) throws AttachmentException, IOException {
		if (delegate == null) {
			resolve();
		}
		((IncomingFileAttachment) delegate).save(targetFilePath);
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.attachment.Attachment#move(java.lang.String)
	 */
	public boolean move(String newFilePath) throws AttachmentException {
		if (delegate == null) {
			resolve();
		}
		return ((IncomingFileAttachment) delegate).move(newFilePath);
	}

	private synchronized void resolve() throws AttachmentException {
		this.delegate = AttachmentStore.getInstance().resolve(attachmentScope, contentId);
	}

	public void serialize(OutputStream out) throws IOException, AttachmentException {
		if (delegate == null) {
			resolve();
		}
		delegate.serialize(out);
	}
}
