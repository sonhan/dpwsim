package eu.telecom.sudparis.dpws;


/**
 * Command pattern for passing a real execute() function into the invocation of each operation
 * Real implementation of this interface would execute the real behavior of each operation invoke()
 * 
 * @author	Son Han
 * @date	2013/09/20
 * @version 2.0
 */

public interface OperationCommand {
	public String execute(String paramValue);
}
