package eu.telecom.sudparis.dpwsim.view;

import java.awt.Frame;

import javax.swing.JDialog;

import eu.telecom.sudparis.dpwsim.upgrade.MediatorComponent;

/**
 * New Device Dialog 
 * 
 * @author	Son Han
 * @date	2013/09/20
 * @version 2.0
 */
@SuppressWarnings("serial")
public class DeviceDialog extends JDialog{
	public DeviceDialog(Frame owner, boolean modal){
		super(owner, modal);
		MediatorComponent.getInstance().setDeviceDialog(this);
		//this.setTitle("New Device");
		//this.setSize(650,500);
		this.setLocationRelativeTo(this);
	}
	
}
