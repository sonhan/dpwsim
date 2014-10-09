package eu.telecom.sudparis.dpwsim.view;

import java.awt.event.ActionEvent;


import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.DefaultComboBoxModel;
import javax.swing.Icon;
import javax.swing.JFileChooser;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;

import eu.telecom.sudparis.dpwsim.upgrade.DPWSimDevice;
import eu.telecom.sudparis.dpwsim.upgrade.EventProvider;
import eu.telecom.sudparis.dpwsim.upgrade.MediatorComponent;
import eu.telecom.sudparis.dpwsim.view.tools.SwingUtilities;

/**
 * Popup menu handler for devices
 *  
 * @author	Son Han
 * @date	2013/09/20
 * @version 2.0
 */
public class DevicePopupMenu extends JPopupMenu implements ActionListener{
	private static final long serialVersionUID = 7011246339317624000L;
	public static final String SAVE = "Save";
	public static final String MOVE = "Move";
	public static final String DELETE = "Delete";
	public static final String CONTROL_PANEL = "Control Panel";
	public static final String TOGGLE_ON = "Start";
	public static final String TOGGLE_OFF = "Stop";
	
	public DevicePopupMenu(int mode){
		
		JMenuItem menuItem;

		menuItem = new JMenuItem(DevicePopupMenu.TOGGLE_ON, SwingUtilities.createImageIcon("/res/icon_start.png", 16));
		menuItem.addActionListener(this);
		this.add(menuItem);
		
		this.addSeparator();
		
		menuItem = new JMenuItem(DevicePopupMenu.CONTROL_PANEL,
				SwingUtilities.createImageIcon("/res/icon_info.png", 16));
		menuItem.addActionListener(this);
		this.add(menuItem);
		
		menuItem = new JMenuItem(DevicePopupMenu.SAVE,
				SwingUtilities.createImageIcon("/res/icon_save.png", 16));
		menuItem.addActionListener(this);
		this.add(menuItem);

		if (mode == DPWSimMainWindow.MODE_SPACE_CREATED) {
			/* Popup menu item: move device */
			menuItem = new JMenuItem(DevicePopupMenu.MOVE,
					SwingUtilities
							.createImageIcon("/res/icon_move.png", 16));
			menuItem.addActionListener(this);
			this.add(menuItem);
			
			this.addSeparator();
			/* Popup menu item: delete device */
			menuItem = new JMenuItem(DevicePopupMenu.DELETE,
					SwingUtilities.createImageIcon("/res/icon_trash.png",
							16));
			menuItem.addActionListener(this);
			this.add(menuItem);
		}
	}
	@Override
	public void actionPerformed(ActionEvent e) {
		DPWSimMainWindow main_window = MediatorComponent.getInstance().getMainWidow();
		DPWSimDevice device = (DPWSimDevice) this.getInvoker();
		String command = e.getActionCommand();
		main_window.setActiveDevice(device);
		
		if (command == MOVE){
			//((DPWSimMainWindow) owner).setActiveDevice(device);
			main_window.setMode(DPWSimMainWindow.MODE_ADDING_DEVICE);
		} else if (command == DELETE){
			
			int confirm = JOptionPane.showConfirmDialog(null, "Are you sure to delete this device?", 
					"Delete Device", JOptionPane.YES_NO_OPTION);
			if (confirm == JOptionPane.YES_OPTION){
				
				if (device.getDevice().isRunning()){
					try {
						device.getDevice().stop();
					} catch (IOException e1) {
						e1.printStackTrace();
					}
				}
				main_window.devices.remove(device);
				main_window.remove(device);
				
				main_window.revalidate();
				main_window.repaint();
			}
		
		} else if (command == CONTROL_PANEL){
			DeviceDialog device_dialog = MediatorComponent.getInstance().getDeviceDialog();
			DeviceControlPanel control_panel = MediatorComponent.getInstance().getControlPanel();
			
			control_panel.loadDeviceInfo(device);
			
			device_dialog.setContentPane(control_panel);
			device_dialog.setTitle("Device Control Panel");
			device_dialog.setSize(520, 320);
			device_dialog.setVisible(true);
			
		} else if (command == TOGGLE_ON){
			try {
				device.getDevice().start();
				((JMenuItem) e.getSource()).setText(TOGGLE_OFF);
				((JMenuItem) e.getSource()).setIcon(SwingUtilities.createImageIcon("/res/icon_stop.png", 16));
								
				// Start all the automatic events
				
				Iterator<EventProvider> itr = device.getEvs().iterator();
				while (itr.hasNext()){
					EventProvider ev = itr.next();
					if (ev.getFrequency() > 0) ev.start();
				}
				
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		} else if (command == TOGGLE_OFF){
			try {
				device.getDevice().stop();
				//device.setRunning(false);
				((JMenuItem) e.getSource()).setText(TOGGLE_ON);
				((JMenuItem) e.getSource()).setIcon(SwingUtilities.createImageIcon("/res/icon_start.png", 16));
				
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		} else if (command == SAVE){
			JFileChooser fc = new JFileChooser(".");
			int returnVal = fc.showOpenDialog(null);
			if (returnVal == JFileChooser.APPROVE_OPTION){
				File file = fc.getSelectedFile();
				try{
					FileOutputStream fout = new FileOutputStream(file);
					ObjectOutputStream oos = new ObjectOutputStream(fout);   
					oos.writeObject(device);
					oos.close();
					SwingUtilities.showInformationMessage(null, "Device saved!");
			   }catch(Exception ex){
				   ex.printStackTrace();
				   SwingUtilities.showInformationMessage(null, ex.getMessage());
			   }
			}
		}
	}

}
