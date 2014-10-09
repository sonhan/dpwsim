package org.ws4d.java.client;

import org.ws4d.java.dispatch.HelloData;

/**
 * Implementation of this interface is used to receive {@link HelloData} from
 * hello messages. Registration for receiving such hello message is done by the
 * {@link DefaultClient#registerHelloListening(SearchParameter, HelloListener)}.
 */
public interface HelloListener {

	/**
	 * This method is called, if matching hello was received.
	 * 
	 * @param helloData Hello data object.
	 */
	public void helloReceived(HelloData helloData);

}
