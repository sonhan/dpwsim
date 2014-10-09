package org.ws4d.java.types;

import java.io.IOException;

import org.ws4d.java.io.xml.ElementHandler;
import org.ws4d.java.io.xml.ElementParser;
import org.ws4d.java.io.xml.XmlSerializer;
import org.ws4d.java.structures.HashMap;
import org.ws4d.java.structures.Iterator;
import org.ws4d.java.util.WS4DIllegalStateException;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

/**
 * This class implement a generic handler for user added customize metadata
 * 
 * @author nneumann
 */
public class CustomizeMDataHandler implements ElementHandler {

	private static String						NAMESPACE	= "http://www.ws4d.org";

	private static final CustomizeMDataHandler	INSTANCE	= new CustomizeMDataHandler();

	private CustomizeMDataHandler() {
		super();
	}

	/**
	 * @see org.ws4d.java.io.xml.ElementHandler#handleElement(QName, ElementParser)
	 */
	public Object handleElement(QName elementName, ElementParser parser) throws XmlPullParserException, IOException {
		String content = "";
		int event;
		if (parser.getNamespace().equals(NAMESPACE)) {
			String name = parser.getName();
			content = content + name + "\n[\n";
			parser.nextTag();
			do {
				content = content + parser.getName() + " = ";
				parser.next();
				content = content + parser.getText() + ";\n";
				parser.next();
				event = parser.nextTag();
			} while (event != XmlPullParser.END_TAG);
			content = content + "]";
		}
		return content;

	}
	/**
	 * @see org.ws4d.java.io.xml.ElementHandler#serializeElement(XmlSerializer, QName, Object)
	 */
	public void serializeElement(XmlSerializer serializer, QName qname, Object data) throws IllegalArgumentException, WS4DIllegalStateException, IOException {
		serializer.startTag(NAMESPACE, "CustomMData");
		HashMap mdata = (HashMap) data;
		Iterator it = mdata.keySet().iterator();
		while (it.hasNext()) {
			QName elementName = (QName) it.next();
			Object value = (Object) mdata.get(elementName);
			serializer.startTag(NAMESPACE, elementName.toString());
			serializer.text(value.toString());
			serializer.endTag(NAMESPACE, elementName.toString());
		}
		serializer.endTag(NAMESPACE, "CustomMData");

	}
	/**
	 * Give a static instance of the  CustomizeMDataHandler
	 * @return
	 */
	public static CustomizeMDataHandler getInstance() {
		return INSTANCE;
	}
}
