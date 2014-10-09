package eu.telecom.sudparis.dpwsim.upgrade;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JLabel;

import eu.telecom.sudparis.dpws.OperationCommand;
import eu.telecom.sudparis.dpwsim.view.DPWSimMainWindow;
import eu.telecom.sudparis.dpwsim.view.tools.SwingUtilities;

/**
 * Real implementation of OperationCommand 
 * 
 * @author	Son Han
 * @date	2013/09/20
 * @version 2.0
 * @version	3.0, 2013/12/09
 */
public class DPWSimOperationCommand implements OperationCommand, Serializable{
	private static final long serialVersionUID = -668772726827277590L;
	private DPWSimDevice device;
	private List<OperationParam>	params = new ArrayList<>();
	
	public DPWSimOperationCommand(DPWSimDevice device){
		this.setDevice(device);
	}
	public DPWSimOperationCommand(DPWSimDevice device, List<OperationParam>	params){
		if (device != null) this.setDevice(device);
		if (params != null) this.setParams(params);
	}
	@Override
	public String execute(String paramValue) {
		//Iterator iconURL = params.iterator();
		
		String iconURL = "";
		String result = "default";
		
		if (params != null){
			Iterator<OperationParam> itr = params.iterator();
			while (itr.hasNext()){
				OperationParam param = itr.next();
				if (param.getRequest().equals(paramValue)){
					iconURL = param.getUrl();
					result = param.getResponse();
					break;
				}
			}
		}
		
		ImageIcon icon = SwingUtilities.createImageIcon(iconURL);
		if (icon == null) icon = SwingUtilities.createDefaultDeviceIcon();
				
		if (MediatorComponent.getInstance().getMainWidow().getMode() == DPWSimMainWindow.MODE_DEVICE_CREATED){
			device.setIcon(icon);
		} else {
			device.setIcon(new ImageIcon(
					SwingUtilities.resizeToHeight(icon.getImage(), 32)));
		}
		
		return result;
	}
//	
//	public List<String> listParams(){
//		List<String> result = new ArrayList<>();
//		
//		Iterator<OperationParam> itr = params.iterator();
//		while (itr.hasNext()){
//			OperationParam param = itr.next();
//			result.add(param.getRequest());
//		}
//		return result;
//	}
//	
	public DPWSimDevice getDevice() {
		return device;
	}
	public void setDevice(DPWSimDevice device) {
		this.device = device;
	}
	public List<OperationParam> getParams() {
		return params;
	}
	public void setParams(List<OperationParam> params) {
		this.params = params;
	}

}
