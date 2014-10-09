package org.ws4d.java.types;

/**
 * This class defines a type for user added metadata
 * CustomizeMData inherit {@link UnknownDataContainer}
 * @author nneumann
 */
public final class CustomizeMData extends UnknownDataContainer {

	public static final QName			CUSTOM		= new QName("CustomMData", "http://www.ws4d.org");

	private static final CustomizeMData	INSTANCE	= new CustomizeMData();

	/**
	 * It returns a static instance of CustomizeMData
	 * 
	 * @return
	 */
	public static CustomizeMData getInstance() {
		return INSTANCE;
	}

	/**
	 * The standard consturctor
	 */
	public CustomizeMData() {
		
		super();
	}
	/**
	 * Add a new element of the user metadata
	 * @param name Name of the tag element
	 * @param value Content of the element
	 */
	public void addNewElement(QName name, Object value) {
		addUnknownElement(name, value);

	}
	/**
	 * Add a new attribute 
	 * @param name Name of the attribute
	 * @param value	Content of the attribute
	 */
	public void addNewAttribute(QName name, String value) {
		addUnknownAttribute(name, value);
	}

}
