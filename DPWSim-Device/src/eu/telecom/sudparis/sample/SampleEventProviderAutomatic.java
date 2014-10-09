package eu.telecom.sudparis.sample;

import org.ws4d.java.service.DefaultEventSource;

import org.ws4d.java.service.parameter.ParameterValue;
import org.ws4d.java.util.ParameterUtil;

/**
 * Sample automatic generator of event after a frequency  
 * 
 * @author	Son Han
 * @date	2013/12/05
 * @version 3.0
 */
public class SampleEventProviderAutomatic extends Thread{

	private static int			eventCounter	= 	0;

	private DefaultEventSource	event;
	private int					frequency;
	private String				message;

	public SampleEventProviderAutomatic(DefaultEventSource event, 
			int frequency,
			String message) {
		this.event = event;
		this.frequency = frequency;
		this.message = message;
	}

	@Override
	public void run() {
		while (true) {
			try {
				Thread.sleep(frequency);
				fireEvent();
				System.out.println("fire Event");
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * This method sets the paramValue and fires the event
	 */
	public void fireEvent() {
		ParameterValue paramValue = event.createOutputValue();
		ParameterUtil.setString(paramValue, "param", message + "(" + eventCounter + ")");
		event.fire(paramValue, eventCounter++);
	}

	
}
