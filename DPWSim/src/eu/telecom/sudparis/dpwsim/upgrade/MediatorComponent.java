package eu.telecom.sudparis.dpwsim.upgrade;

import eu.telecom.sudparis.dpwsim.view.DPWSimMainWindow;
import eu.telecom.sudparis.dpwsim.view.DeviceControlPanel;
import eu.telecom.sudparis.dpwsim.view.DeviceDialog;
import eu.telecom.sudparis.dpwsim.view.MainMenu;
import eu.telecom.sudparis.dpwsim.view.NewDevicePanel;
import eu.telecom.sudparis.dpwsim.view.NewSpacePanel;

public class MediatorComponent {
	 
	/**
	 * Singleton to mediate swing components 
	 * 
	 * @author	Son Han
	 * @date	2013/12/07
	 * @version 1.0
	 */
    private static MediatorComponent instance = new MediatorComponent();
 
    public static MediatorComponent getInstance() {
        return instance;
    }
    
    private DPWSimMainWindow mainWidow;
	private NewSpacePanel newSpacePanel;
	private NewDevicePanel newDevicePanel;
	private DeviceDialog deviceDialog;
    private MainMenu mainMenu;
    private DeviceControlPanel controlPanel;
    
    public void setMode(int mode){
    	mainWidow.setMode(mode);
    	mainMenu.setMode(mode);
    }
    
	public DPWSimMainWindow getMainWidow() {
		return mainWidow;
	}
	public void setMainWidow(DPWSimMainWindow mainWidow) {
		this.mainWidow = mainWidow;
	}
	public NewSpacePanel getNewSpacePanel() {
		return newSpacePanel;
	}
	public void setNewSpacePanel(NewSpacePanel newSpacePanel) {
		this.newSpacePanel = newSpacePanel;
	}
	public MainMenu getMainMenu() {
		return mainMenu;
	}
	public void setMainMenu(MainMenu mainMenu) {
		this.mainMenu = mainMenu;
	}
	public DeviceControlPanel getControlPanel() {
		return controlPanel;
	}
	public void setControlPanel(DeviceControlPanel controlPanel) {
		this.controlPanel = controlPanel;
	}
	public NewDevicePanel getNewDevicePanel() {
		return newDevicePanel;
	}
	public void setNewDevicePanel(NewDevicePanel newDevicePanel) {
		this.newDevicePanel = newDevicePanel;
	}
	public DeviceDialog getDeviceDialog() {
		return deviceDialog;
	}
	public void setDeviceDialog(DeviceDialog deviceDialog) {
		this.deviceDialog = deviceDialog;
	}
}