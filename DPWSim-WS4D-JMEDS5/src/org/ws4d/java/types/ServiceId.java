package org.ws4d.java.types;

import java.io.IOException;

import org.ws4d.java.constants.DPWSConstants2006;
import org.ws4d.java.io.xml.ElementHandler;
import org.ws4d.java.io.xml.ElementParser;
import org.ws4d.java.io.xml.XmlSerializer;
import org.ws4d.java.util.WS4DIllegalStateException;
import org.xmlpull.v1.XmlPullParserException;

public class ServiceId implements ElementHandler {

	public static final QName	QNAME	= new QName("ServiceId", DPWSConstants2006.DPWS_NAMESPACE_NAME);

	private URI					serviceId;

	public ServiceId() {}

	public ServiceId(URI serviceId) {
		this.serviceId = serviceId;
	}

	public URI getServiceId() {
		return serviceId;
	}

	public void setServiceId(URI serviceId) {
		this.serviceId = serviceId;
	}

	public Object handleElement(QName elementName, ElementParser parser) throws XmlPullParserException, IOException {
		ServiceId id = new ServiceId();
		if (elementName.equals(QNAME)) {
			id.setServiceId(parser.nextAttributedUri());
		}
		return id;
	}

	public void serializeElement(XmlSerializer serializer, QName qname, Object value) throws IllegalArgumentException, WS4DIllegalStateException, IOException {

		ServiceId id = (ServiceId) value;

		serializer.startTag(qname.getNamespace(), qname.getLocalPart());
		serializer.text(id.getServiceId().toString());
		serializer.endTag(qname.getNamespace(), qname.getLocalPart());
	}

}
