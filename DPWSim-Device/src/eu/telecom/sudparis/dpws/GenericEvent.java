package eu.telecom.sudparis.dpws;

import java.io.Serializable;

import org.ws4d.java.schema.Element;
import org.ws4d.java.schema.SchemaUtil;
import org.ws4d.java.schema.Type;
import org.ws4d.java.service.DefaultEventSource;
import org.ws4d.java.types.QName;

/**
 * Generic Event 
 * 
 * @author	Son Han
 * @date	2013/09/20
 * @version 2.0
 */
public class GenericEvent extends DefaultEventSource implements Serializable {

	private static final long serialVersionUID = 7746987610786222087L;

	public GenericEvent(String evtName, String namespace) {
		super(evtName, new QName("events", namespace));

		Type xsString = SchemaUtil.getSchemaType(SchemaUtil.TYPE_STRING);
		Element element = new Element(new QName("param", namespace), xsString);

		setOutput(element);
	}
}
