package eu.telecom.sudparis.sample;
import eu.telecom.sudparis.dpws.OperationCommand;

/**
 * Sample implementation of OperationCommand
 * 
 * @author	Son Han
 * @date	2013/12/05
 * @version 3.0
 */
public class SampleOperationCommand implements OperationCommand{
	public SampleOperationCommand(){
		
	}
	@Override
	public String execute(String paramValue) {
		System.out.println("response for " + paramValue);
		return "response for " + paramValue;
	}

}
