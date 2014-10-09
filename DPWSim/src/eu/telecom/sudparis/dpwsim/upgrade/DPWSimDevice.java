package eu.telecom.sudparis.dpwsim.upgrade;

import java.util.ArrayList;


import java.util.Iterator;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import eu.telecom.sudparis.dpws.GenericDevice;
import eu.telecom.sudparis.dpws.GenericEvent;
import eu.telecom.sudparis.dpws.GenericOperation;
import eu.telecom.sudparis.dpwsim.view.tools.SwingUtilities;

public class DPWSimDevice extends JLabel{

    private static final long serialVersionUID = 1194833086459282796L;
	private GenericDevice 			device;
	private List<GenericOperation>	ops = new ArrayList<>();
	private List<EventProvider>		evs = new ArrayList<>();
	//private boolean					isRunning = false; // true if the device is running
	
	public DPWSimDevice(String name,
			String portType,
			String manufacturer,
			String namespace,
			String httpBindIP, 
			int portDevice, 
			int portService,
			String iconPath) {
		super();
		device = new GenericDevice(name, portType, manufacturer, namespace, httpBindIP, portDevice, portService);
		ImageIcon icon = SwingUtilities.createImageIcon(iconPath);
		if (icon == null) icon = SwingUtilities.createDefaultDeviceIcon();
		this.setIcon(icon);
		this.setHorizontalAlignment(JLabel.CENTER);
		
	}
	
	public List<String> getOperationNames(){
		List<String> result = new ArrayList<>();
		
		Iterator<GenericOperation> itr = ops.iterator();
		while (itr.hasNext()){
			GenericOperation op = itr.next();
			result.add(op.getName());
		}
		return result;
	}
	
	public List<String> getEventNames(){
		List<String> result = new ArrayList<>();
		
		Iterator<EventProvider> itr = evs.iterator();
		while (itr.hasNext()){
			EventProvider ev = itr.next();
			result.add(ev.getEvent().getName());
		}
		return result;
	}
	
	public void addOperation(GenericOperation op) {
		device.addOperation(op);
		ops.add(op);
	}
	
	public void addEvent(String eventName, String eventMsg, int eventFreq) {
		device.addEvent(eventName);
		GenericEvent evtSource = (GenericEvent) device.getEventSource(eventName);
		EventProvider evtProvider = new EventProvider(evtSource, eventFreq, eventMsg);
		evs.add(evtProvider);		
	}
	
	public String toString(){
		return null;
	}

	public GenericDevice getDevice() {
		return device;
	}

	public void addInfo(String string) {
		
	}

	public List<GenericOperation> getOps() {
		return ops;
	}

	public void setOps(List<GenericOperation> ops) {
		this.ops = ops;
	}

	public List<EventProvider> getEvs() {
		return evs;
	}

	public void setEvs(List<EventProvider> evs) {
		this.evs = evs;
	}

	public void setDevice(GenericDevice device) {
		this.device = device;
	}

}
