package eu.telecom.sudparis.dpws;



import java.io.Serializable;

import org.ws4d.java.schema.Element;
import org.ws4d.java.schema.SchemaUtil;
import org.ws4d.java.schema.Type;
import org.ws4d.java.service.InvocationException;
import org.ws4d.java.service.Operation;
import org.ws4d.java.service.parameter.ParameterValue;
import org.ws4d.java.types.QName;
import org.ws4d.java.util.ParameterUtil;

/**
 * Generic Operation 
 * 
 * @author	Son Han
 * @date	2013/12/06
 * @version 3.0
 */
public class GenericOperation extends Operation implements Serializable {

	private static final long serialVersionUID = -669454081773618772L;
	private OperationCommand command;

	/**
	 * Constructor
	 * @param namespace namespace
	 * @param opName operation name
	 * @param command real operation code
	 */
	public GenericOperation( 
			String opName, 
			String namespace,
			OperationCommand command) {
		
		super(opName, new QName("operations", namespace));
		this.command = command;
		
		// get schema type for strings
		Type xsString = SchemaUtil.getSchemaType(SchemaUtil.TYPE_STRING);

		// create new Element called "name" (just a simple one in this case)
		Element nameElem = new Element(new QName("param", namespace), xsString);

		// set the input of the operation
		setInput(nameElem);

		// create new element called "reply"
		Element reply = new Element(new QName("reply", namespace), xsString);

		// set this element as output
		setOutput(reply);
	}

	@Override
	public ParameterValue invoke(ParameterValue parameterValue) throws InvocationException {

		// get string value from input
		String paraValue = ParameterUtil.getString(parameterValue, "param");
		System.out.println("Received parameter: " + paraValue);

		// create output and set value
		ParameterValue result = createOutputValue();
		String response = command.execute(paraValue);
		ParameterUtil.setString(result, "reply", response);
		
		return result;
	}

	public OperationCommand getCommand() {
		return command;
	}

	public void setCommand(OperationCommand command) {
		this.command = command;
	}
}
