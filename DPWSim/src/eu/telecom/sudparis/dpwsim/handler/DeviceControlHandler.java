package eu.telecom.sudparis.dpwsim.handler;

import java.awt.event.ActionEvent;


import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

import eu.telecom.sudparis.dpws.GenericEvent;
import eu.telecom.sudparis.dpws.GenericOperation;
import eu.telecom.sudparis.dpwsim.upgrade.DPWSimDevice;
import eu.telecom.sudparis.dpwsim.upgrade.DPWSimOperationCommand;
import eu.telecom.sudparis.dpwsim.upgrade.EventProvider;
import eu.telecom.sudparis.dpwsim.upgrade.MediatorComponent;
import eu.telecom.sudparis.dpwsim.upgrade.NewEventPanel;
import eu.telecom.sudparis.dpwsim.upgrade.NewParameterPanel;
import eu.telecom.sudparis.dpwsim.upgrade.OperationParam;
import eu.telecom.sudparis.dpwsim.view.DPWSimMainWindow;
import eu.telecom.sudparis.dpwsim.view.DeviceControlPanel;
import eu.telecom.sudparis.dpwsim.view.DeviceDialog;
import eu.telecom.sudparis.dpwsim.view.tools.SwingUtilities;

public class DeviceControlHandler implements ActionListener{
	
	final JFileChooser fc = new JFileChooser(".");
	private File file = null;
	
	@Override
	public void actionPerformed(ActionEvent e) {
		
		DeviceControlPanel control_panel = MediatorComponent.getInstance().getControlPanel();
		DPWSimMainWindow main_window = MediatorComponent.getInstance().getMainWidow();
		DPWSimDevice device = main_window.getActiveDevice();
		
		Object ob = e.getSource();
		
		if (ob == control_panel.getAddOp()){
			String name = JOptionPane.showInputDialog("New Operation Name");
			if (name != null) {
			    if (name.equals("")){
					SwingUtilities.showErrorMessage(null, "No operation name!");
					return;
				}
				JComboBox combo = control_panel.getOpCombo();
				DPWSimOperationCommand command = new DPWSimOperationCommand(device);
				GenericOperation op = new GenericOperation(name, device.getDevice().getNamespace(), command);
				device.addOperation(op);
				combo.setModel(new DefaultComboBoxModel(device.getOperationNames().toArray()));
				combo.setSelectedIndex(combo.getItemCount() - 1);
				
				updateFieldsOfOperation(op);
				
				System.out.println(device.getDevice().getFriendlyName("en-US"));
			}
		} else if (ob == control_panel.getAddPa()){
			GenericOperation op = (GenericOperation) device.getOps().get(control_panel.getOpCombo().getSelectedIndex());
			DPWSimOperationCommand command = (DPWSimOperationCommand) op.getCommand();
			
			NewParameterPanel panel = new NewParameterPanel();
			int confirm = JOptionPane.showConfirmDialog(null, panel, "New param value setup", JOptionPane.OK_CANCEL_OPTION);
			
			if (confirm == JOptionPane.OK_OPTION){
			
				String request = panel.getParamVal().getText();
				String response = panel.getReponseMsg().getText();
				String url = panel.getIconURL();
				
				if (request.equals("")){
					SwingUtilities.showErrorMessage(null, "No operation name provided!");
					return;
				}
				
				OperationParam param = new OperationParam(request, response, url);
				command.getParams().add(param);
				
				updateFieldsOfParam(command.getParams().size() - 1, command);
			}
		} else if (ob == control_panel.getEditPa()){
			GenericOperation op = (GenericOperation) device.getOps().get(control_panel.getOpCombo().getSelectedIndex());
			DPWSimOperationCommand command = (DPWSimOperationCommand) op.getCommand();
			int index = control_panel.getOpParamCombo().getSelectedIndex();
			OperationParam param = command.getParams().get(index);
			
			NewParameterPanel panel = new NewParameterPanel();
			
			panel.getParamVal().setText(param.getRequest());
			panel.getReponseMsg().setText(param.getResponse());
			SwingUtilities.loadImage(panel.getIconPreview(), param.getUrl(), 24);
			panel.setIconURL(param.getUrl());
			
			int confirm = JOptionPane.showConfirmDialog(null, panel, "Edit param value", JOptionPane.OK_CANCEL_OPTION);
			
			if (confirm == JOptionPane.OK_OPTION){
			
				String request = panel.getParamVal().getText();
				String response = panel.getReponseMsg().getText();
				String url = panel.getIconURL();
				
				if (request.equals("")){
					SwingUtilities.showErrorMessage(null, "No operation name provided!");
					return;
				}
				
				param.setRequest(request);
				param.setResponse(response);
				param.setUrl(url);
				
				updateFieldsOfParam(index, command);
				
				SwingUtilities.showInformationMessage(null, "Changes saved!");
			}
		} else if (ob == control_panel.getAddEv()){
			NewEventPanel panel = new NewEventPanel();
			int confirm = JOptionPane.showConfirmDialog(null, panel, "New event", JOptionPane.OK_CANCEL_OPTION);
			
			if (confirm == JOptionPane.OK_OPTION){
				String eventName = panel.getEventName().getText();
				String eventMsg = panel.getEventMessage().getText();
				int eventFreq = 0; 
				try {
					eventFreq = Integer.parseInt(panel.getEventFrequency().getText());
				} catch (Exception ee){
					
				}
				
				if (eventName.equals("")){
					SwingUtilities.showErrorMessage(null, "No event name provided!");
					return;
				}
				
				device.addEvent(eventName, eventMsg, eventFreq);
				int i = device.getEvs().size() - 1;
				
				EventProvider event = device.getEvs().get(i);
				
				control_panel.getEvCombo().setModel(new DefaultComboBoxModel(device.getEventNames().toArray()));
				control_panel.getEvCombo().setSelectedIndex(i);
				
				updateFieldsOfEvent(event);
				
			}
		} else if (ob == control_panel.getEditEv()){
			JComboBox comboEvt = control_panel.getEvCombo();
			int index = comboEvt.getSelectedIndex();
			EventProvider event = device.getEvs().get(index);
			
			NewEventPanel panel = new NewEventPanel();
			panel.getEventName().setText(event.getEvent().getName());
			panel.getEventName().setEditable(false);
			panel.getEventMessage().setText(event.getMessage());
			panel.getEventFrequency().setText(event.getFrequency() + "");
			
			int confirm = JOptionPane.showConfirmDialog(null, panel, "Edit event", JOptionPane.OK_CANCEL_OPTION);
			
			if (confirm == JOptionPane.OK_OPTION){
				String eventName = panel.getEventName().getText();
				String eventMsg = panel.getEventMessage().getText();
				int eventFreq = 0; 
				try {
					eventFreq = Integer.parseInt(panel.getEventFrequency().getText());
				} catch (Exception ee){
					
				}
				
				event.setMessage(eventMsg);
				event.setFrequency(eventFreq);
				
				updateFieldsOfEvent(event);
				
				SwingUtilities.showInformationMessage(null, "Changes saved!");
			}
		} else if (ob == control_panel.getOpCombo()){
			GenericOperation op = (GenericOperation) device.getOps().get(control_panel.getOpCombo().getSelectedIndex());
			updateFieldsOfOperation(op);
		} else if (ob == control_panel.getOpParamCombo()){
			GenericOperation op = (GenericOperation) device.getOps().get(control_panel.getOpCombo().getSelectedIndex());
			DPWSimOperationCommand command = (DPWSimOperationCommand) op.getCommand();
			updateFieldsOfParam(control_panel.getOpParamCombo().getSelectedIndex(), command);
		} else if (ob == control_panel.getEvCombo()){
			EventProvider evtProvider = (EventProvider) device.getEvs().get(control_panel.getEvCombo().getSelectedIndex());
			updateFieldsOfEvent(evtProvider);
		} else if (ob == control_panel.getFireEvent()){
			control_panel.getActiveEvent().fireEvent();
		} 
		
		
		else if (ob == control_panel.getDeletePa()){
			
			int confirm = JOptionPane.showConfirmDialog(null, "Do you really want to delete this value?");
			
			if (confirm == JOptionPane.YES_OPTION){
				
				int index = control_panel.getOpParamCombo().getSelectedIndex();
				GenericOperation op = (GenericOperation) device.getOps().get(control_panel.getOpCombo().getSelectedIndex());
				DPWSimOperationCommand command = (DPWSimOperationCommand) op.getCommand();
				command.getParams().remove(index);
				
				updateFieldsOfOperation(op);
				
				SwingUtilities.showInformationMessage(null, "Request value deleted!");

			}
		} else if (ob == control_panel.getStartDevice()){
			if (e.getActionCommand().equals("Start")){
				try {
					device.getDevice().start();
					
					((JButton) ob).setText("Stop");
					((JButton) ob).setIcon(SwingUtilities.createImageIcon("/res/icon_stop.png", 16));
					
					Iterator<EventProvider> itr = device.getEvs().iterator();
					while (itr.hasNext()){
						EventProvider ev = itr.next();
						if (ev.getFrequency() > 0) ev.start();
					}
					
				} catch (IOException e1) {
					e1.printStackTrace();
				}				
			} else if (e.getActionCommand().equals("Stop")){
				try {
					device.getDevice().stop();
					
					((JButton) ob).setText("Start");
					((JButton) ob).setIcon(SwingUtilities.createImageIcon("/res/icon_start.png", 16));
					
				} catch (IOException e1) {
					e1.printStackTrace();
				}				
			}
		}  else if (ob == control_panel.getCloseButton()){
			DeviceDialog device_dialog = MediatorComponent.getInstance().getDeviceDialog();
			device_dialog.dispose();
		}
		
		System.out.println(e.getActionCommand());
		
	}

	public static void updateFieldsOfOperation(GenericOperation op) {
		DeviceControlPanel control_panel = MediatorComponent.getInstance().getControlPanel();
		
		JComboBox comboParam = control_panel.getOpParamCombo();
		
		DPWSimOperationCommand command = (DPWSimOperationCommand) op.getCommand();
		comboParam.setModel(new DefaultComboBoxModel(command.getParams().toArray()));
		control_panel.getAddPa().setEnabled(true);
		
		int n = comboParam.getItemCount() - 1;
		comboParam.setSelectedIndex(n);
		updateFieldsOfParam(n, command);
	}

	public static void updateFieldsOfParam(int n, DPWSimOperationCommand command) {
		DeviceControlPanel control_panel = MediatorComponent.getInstance().getControlPanel();
		control_panel.getOpParamCombo().setModel(new DefaultComboBoxModel(command.getParams().toArray()));
		control_panel.getOpParamCombo().setSelectedIndex(n);
		if (n >= 0) {
			OperationParam param = (OperationParam) command.getParams().get(n);
			control_panel.getOpOutputField().setText(param.getResponse());
			SwingUtilities.loadImage(control_panel.getIconPreview(), param.getUrl(), 20);
			control_panel.getEditPa().setEnabled(true);
			control_panel.getDeletePa().setEnabled(true);
		} else {
			control_panel.getOpOutputField().setText("");
			//control_panel.getUrlField().setText(SwingUtilities.DEFAULT_IMAGE_TEXT);
			control_panel.getIconPreview().setIcon(null);
			control_panel.getEditPa().setEnabled(false);
			control_panel.getDeletePa().setEnabled(false);
		}	
	}
	
	public static void updateFieldsOfEvent(EventProvider evtProvider) {
		DeviceControlPanel control_panel = MediatorComponent.getInstance().getControlPanel();
		DPWSimMainWindow main_window = MediatorComponent.getInstance().getMainWidow();
		DPWSimDevice device = main_window.getActiveDevice();
		
		//control_panel.getEvCombo().setModel(new DefaultComboBoxModel(device.getEventNames().toArray()));
		
		if (evtProvider != null) {
			control_panel.setActiveEvent(evtProvider);
			control_panel.getEvMessageField().setText(evtProvider.getMessage());
			control_panel.getEvFreqField().setText(evtProvider.getFrequency() + "");
			if (evtProvider.getFrequency() > 0) control_panel.getFireEvent().setEnabled(false);
			else if (device.getDevice().isRunning()) control_panel.getFireEvent().setEnabled(true);
			control_panel.getEditEv().setEnabled(true);
		} else {
			control_panel.getEvMessageField().setText("");
			control_panel.getEvFreqField().setText("");
			control_panel.getFireEvent().setEnabled(false);
			control_panel.getEditEv().setEnabled(false);
		}		
	}
	
}
