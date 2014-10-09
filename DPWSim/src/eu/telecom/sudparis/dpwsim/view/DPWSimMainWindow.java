package eu.telecom.sudparis.dpwsim.view;

import java.util.ArrayList;


import java.util.List;

import javax.swing.JFrame;
import eu.telecom.sudparis.dpwsim.upgrade.DPWSimDevice;
import eu.telecom.sudparis.dpwsim.upgrade.MediatorComponent;
import eu.telecom.sudparis.dpwsim.view.tools.SwingUtilities;

/**
 * Main window.
 * 
 * @author	Son Han
 * @date	2013/09/20
 * @version 2.0		2013/09/20
 * @version 3.0		2013/12/07
 */
@SuppressWarnings("serial")
public class DPWSimMainWindow extends JFrame{
	
	public static final String DPWSIM = "DPWSim";
	
	public static final int 	MODE_START = 0;
	public static final int 	MODE_DEVICE_NEW = 1;
	public static final int 	MODE_SPACE_NEW = 2;
	public static final int 	MODE_DEVICE_CREATED = 3;
	public static final int 	MODE_SPACE_CREATED = 4;
	public static final int 	MODE_ADDING_DEVICE = 5;
	
	private int 				mode = MODE_START;
	
	public List<DPWSimDevice> 	devices = new ArrayList<>();
	public DPWSimDevice		 	activeDevice;

	public String				spaceinfo;
	
	public String				IP_ADDRESS = "";
	
	public DPWSimMainWindow(){
		super();
		this.setIconImage(SwingUtilities.createAppIcon().getImage());
		MediatorComponent.getInstance().setMainWidow(this);	    
	}
	
	public void setDefaultSize(){
		this.setSize(650, 400); // golden ratio
		this.setResizable(false);
	}

	public int getMode() {
		return mode;
	}
	
	public void setMode(int mode) {
		this.mode = mode;
	}

	public DPWSimDevice getActiveDevice() {
		return activeDevice;
	}

	public void setActiveDevice(DPWSimDevice activeDevice) {
		this.activeDevice = activeDevice;
	}
}
