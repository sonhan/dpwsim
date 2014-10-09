package eu.telecom.sudparis.dpwsim.upgrade;

import java.io.Serializable;

import org.ws4d.java.service.parameter.ParameterValue;
import org.ws4d.java.util.ParameterUtil;

import eu.telecom.sudparis.dpws.GenericEvent;

/**
 * Automatic generator of event after a frequency  
 * 
 * @author	Son Han
 * @date	2013/12/10
 * @version 1.0
 */
public class EventProvider extends Thread implements Serializable{

    private static final long serialVersionUID = 5753180561048762526L;

	private static int			eventCounter	= 	0;

	private GenericEvent		event;
	private int					frequency;
	private String				message;

	public EventProvider(GenericEvent event, 
			int frequency,
			String message) {
		this.event = event;
		this.frequency = frequency;
		this.message = message;
	}

	@Override
	public void run() {
		if (frequency > 0){
			while (true) {
				try {
					Thread.sleep(frequency);
					fireEvent();
					//System.out.println("fire Event");
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		} else {
			fireEvent();
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

	public GenericEvent getEvent() {
		return event;
	}

	public void setEvent(GenericEvent event) {
		this.event = event;
	}

	public int getFrequency() {
		return frequency;
	}

	public void setFrequency(int frequency) {
		this.frequency = frequency;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
	
}
