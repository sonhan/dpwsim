package eu.telecom.sudparis.dpwsim.view.tools;

import java.io.IOException;

import java.util.HashMap;
import java.util.Iterator;

import javax.swing.JLabel;
import javax.swing.table.DefaultTableModel;

import eu.telecom.sudparis.dpws.GenericDevice;
import eu.telecom.sudparis.dpwsim.upgrade.DPWSimDevice;
import eu.telecom.sudparis.dpwsim.view.DPWSimMainWindow;

/**
 * DPWS Utilities 
 * 
 * @author	Son Han
 * @date	2013/09/20
 * @version 2.0
 */
public class DPWSUtilities {
		
	public static String addOperationsInfo(String deviceinfo, DefaultTableModel ops){
		
		if (ops != null){
			
			for (int i = 0; i < ops.getRowCount(); i++){
				String opName = (String) ops.getValueAt(i, 0);
				String param = (String) ops.getValueAt(i, 1);
				String status = (String) ops.getValueAt(i, 2);
				deviceinfo += "\nOPERATION," + opName + "," + param + "," + status;
			}
		}
		return deviceinfo;
	}
	
	public static String addEventsInfo(String deviceinfo, DefaultTableModel evts){
		
		if (evts != null){
			
			for (int i = 0; i < evts.getRowCount(); i++){
				String evtName = (String) evts.getValueAt(i, 0);
				String param = (String) evts.getValueAt(i, 1);
				String message = (String) evts.getValueAt(i, 2);
				String freq = (String) evts.getValueAt(i, 3);
				deviceinfo += "\nEVENT," + evtName + "," + param + "," + message + "," + freq;
			}
		}
		return deviceinfo;
	}
	public static void clearDevices(DPWSimMainWindow main) {
		Iterator<DPWSimDevice> itr = main.devices.iterator();
		while (itr.hasNext()) {
			DPWSimDevice dv = itr.next();
			if (dv.getDevice().isRunning()){
				try {
					dv.getDevice().stop();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		}
		main.devices.clear();

		Iterator<DPWSimDevice> itrlabel = main.devices.iterator();
		while (itrlabel.hasNext()) {
			JLabel label = itrlabel.next();
			main.remove(label);
		}
		
		main.devices.clear();

		main.revalidate();
		main.repaint();
	}
	
	public static HashMap<String, String> getDeviceInfo(GenericDevice device){
		HashMap<String, String> info = new HashMap<String, String>();
        info.put("Manufacturer", device.getManufacturer("en-US"));
        info.put("Serial Number", device.getSerialNumber());
        //info.put("Name", device.get);
        info.put("Name", device.getFriendlyName("en-US"));
		return info;
	}
}
