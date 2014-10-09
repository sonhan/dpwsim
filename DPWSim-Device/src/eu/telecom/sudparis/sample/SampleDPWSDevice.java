package eu.telecom.sudparis.sample;

import java.io.IOException;
import org.ws4d.java.DPWSFramework;
import org.ws4d.java.service.DefaultEventSource;

import eu.telecom.sudparis.dpws.GenericDevice;

public class SampleDPWSDevice {

	/**
	 * Test Program
	 * 
	 * @author Son Han
	 * @date 2013/12/05
	 * @version 1.0
	 */
	
	public static void main(String[] args) {
		DPWSFramework.start(null);
		
		GenericDevice device = new GenericDevice("SampleDevice", "SampleDeviceTest", 
			"Telecom SudParis", "http://telecom-sudparis.eu", 
			"127.0.0.1", 4567, 4567);
		
		SampleOperationCommand command = new SampleOperationCommand();
		
		device.addOperation("Operation1", command);
		device.addEvent("Event1");
		
		DefaultEventSource evtSource = (DefaultEventSource) device.getEventSource("Event1");
		// Event occurs every 2 second
		SampleEventProviderAutomatic evtProvider = new SampleEventProviderAutomatic(evtSource, 2000, "Event Provider");
		evtProvider.start();
		
		try {
			device.start();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
