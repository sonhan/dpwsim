package eu.telecom.sudparis.dpwsim.handler;

import java.awt.event.ActionEvent;

import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Iterator;

import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.table.DefaultTableModel;

import eu.telecom.sudparis.dpwsim.upgrade.DPWSimDevice;
import eu.telecom.sudparis.dpwsim.upgrade.MediatorComponent;
import eu.telecom.sudparis.dpwsim.view.DPWSimMainWindow;
import eu.telecom.sudparis.dpwsim.view.DeviceControlPanel;
import eu.telecom.sudparis.dpwsim.view.DeviceDialog;
import eu.telecom.sudparis.dpwsim.view.DevicePopupMenu;
import eu.telecom.sudparis.dpwsim.view.MainMenu;
import eu.telecom.sudparis.dpwsim.view.NewDevicePanel;
import eu.telecom.sudparis.dpwsim.view.NewSpacePanel;
import eu.telecom.sudparis.dpwsim.view.tools.DPWSUtilities;
import eu.telecom.sudparis.dpwsim.view.tools.SwingUtilities;

/**
 * Universal action handler
 * 
 * @author Son Han
 * @date 2013/09/20
 * @version 2.0
 */

public class ActionHandler implements ActionListener {

    final JFileChooser fc = new JFileChooser(".");
	private File file = null;

	MainMenu main_menu;// = ComponentMediator.getInstance().getMainMenu();
	NewSpacePanel space_panel;// =
	NewDevicePanel device_panel;// =
	DPWSimMainWindow main_window;// =
	DeviceDialog device_dialog;// = ComponentMediator.getInstance().getDialog();
	DeviceControlPanel control_panel;

	public void validate() {
		main_menu = MediatorComponent.getInstance().getMainMenu();
		main_window = MediatorComponent.getInstance().getMainWidow();
		space_panel = MediatorComponent.getInstance().getNewSpacePanel();
		device_panel = MediatorComponent.getInstance().getNewDevicePanel();
		device_dialog = MediatorComponent.getInstance().getDeviceDialog();
		control_panel = MediatorComponent.getInstance().getControlPanel();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (main_window.getMode() == DPWSimMainWindow.MODE_ADDING_DEVICE){
			SwingUtilities.showInformationMessage(null, "Please set the location of the active device (by mouse click on the space layout)!");
			return;
		}
		String command = e.getActionCommand();

		switch (command) {
		
		case MainMenu.MENU_SAVE:
			
			if (!main_window.devices.isEmpty()){
				int returnVal = fc.showOpenDialog(null);
				if (returnVal == JFileChooser.APPROVE_OPTION){
					file = fc.getSelectedFile();
					try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
						if (main_window.getMode() == DPWSimMainWindow.MODE_SPACE_CREATED){
							writer.write(MainMenu.MENU_NEW_SPACE); writer.newLine();
							writer.write(main_window.spaceinfo); writer.newLine();
						} else {
							writer.write(MainMenu.MENU_NEW_STANDALONE_DEVICE); writer.newLine();
						}
						Iterator<DPWSimDevice> itr = main_window.devices.iterator();
						while (itr.hasNext()){
							DPWSimDevice device = itr.next();
							writer.write(device.toString());
							writer.newLine();
						}
						writer.close();
						SwingUtilities.showInformationMessage(null, "Saved succesfully to " + file.getAbsolutePath());
					} catch (IOException ex) {
						SwingUtilities.showErrorMessage(null, ex.getMessage());
						break;
					}
				}
			} else {
				SwingUtilities.showErrorMessage(null, "No device!");
				break;
			}
			break;
		case MainMenu.MENU_OPEN:
			int returnVal = fc.showOpenDialog(null);
			if (returnVal == JFileChooser.APPROVE_OPTION){
				file = fc.getSelectedFile();
				try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
					if (main_window.devices != null) DPWSUtilities.clearDevices(main_window);
					//setMenuStatus(true);
					//devicesList = new ArrayList<>();
					String[] infos = null;
					String mode = reader.readLine();
					String line = null;
					if (mode.indexOf(MainMenu.MENU_NEW_SPACE) >= 0 ){
						line = reader.readLine();
						infos = line.split(",");
						
						String ipaddress = JOptionPane.showInputDialog(
								"IP Binding Address", infos[2]);
						if (ipaddress == null) break;
						main_window.IP_ADDRESS = ipaddress;	
						createSpace(infos[0], infos[1], main_window.IP_ADDRESS);
					} else if (mode.indexOf(MainMenu.MENU_NEW_STANDALONE_DEVICE) >= 0 ){
						main_window.setMode(DPWSimMainWindow.MODE_DEVICE_NEW);
						
					} else {
						SwingUtilities.showInformationMessage(null, "Not a DPWSim file!");
					}
					
					while ((line = reader.readLine()) != null) {
						if (line.indexOf("DEVICE") >= 0){
							infos = line.split(",");
							String deviceName = infos[1];
							String manufacturer = infos[2];
							String namespace = infos[3];
							String ipaddress = main_window.IP_ADDRESS;//infos[4];
							String deviceport = infos[5];
							String serviceport = infos[6];
							
							DefaultTableModel opModel = new DefaultTableModel();
							opModel.setColumnIdentifiers(new Object[] { "Name", "Parameter",
										"Status Image Location" });
							while ((line = reader.readLine()).indexOf("OPERATION") >= 0) {
								infos = line.split(",");
								opModel.addRow(new Object[]{infos[1], infos[2], infos[3]});
							}
							
							DefaultTableModel evModel = new DefaultTableModel();
							evModel.setColumnIdentifiers(new Object[] { "Name", "Parameter",
										"Event Message", "Frequency (ms)" });
							while ((line = reader.readLine()).indexOf("EVENT") >= 0) {
								infos = line.split(",");
								evModel.addRow(new Object[]{infos[1], infos[2], infos[3], infos[4]});
							}
							try{
								createNewDevice(deviceName, manufacturer, namespace, ipaddress, deviceport, serviceport, "");
							} catch (IOException e1){
								SwingUtilities.showErrorMessage(null, "Device Creation Exception!");
								break;
							}
							
							if (mode.indexOf(MainMenu.MENU_NEW_SPACE) >= 0 ){
								int x = Integer.parseInt(reader.readLine());
								int y = Integer.parseInt(reader.readLine());
								main_window.activeDevice.setLocation(x, y);
								main_window.activeDevice.addInfo("\n" + x + "\n" + y);
							}
						}
					}
					
					if (mode.indexOf(MainMenu.MENU_NEW_SPACE) >= 0 ){
						main_window.setMode(DPWSimMainWindow.MODE_SPACE_CREATED);
					} else if (mode.indexOf(MainMenu.MENU_NEW_STANDALONE_DEVICE) >= 0 ){
						main_window.setMode(DPWSimMainWindow.MODE_DEVICE_CREATED);
					}
					reader.close();
				}catch (Exception ex) {
					SwingUtilities.showErrorMessage(null, ex.getStackTrace());
					break;
				}
				//SwingUtilities.showInformationMessage(null, file.getAbsolutePath());
			}
			
			break;
		case MainMenu.MENU_HELP:
			//SwingUtilities.showHelp(null);
			//DeviceControlPanel control_panel = new DeviceControlPanel(this);
			device_dialog.setContentPane(control_panel);
			device_dialog.setTitle("Device Control Panel");
			device_dialog.pack();
			device_dialog.setVisible(true);
			break;
		case MainMenu.MENU_ABOUT:
			SwingUtilities.showCreditMessage(null);
			break;
		case MainMenu.MENU_EXIT:
			System.exit(0);
			break;
		case MainMenu.MENU_NEW_SPACE:
			/* stop and remove all devices */
			showNewSpace();
			break;
		case MainMenu.MENU_NEW_STANDALONE_DEVICE:

			String[] options = {
					MainMenu.MENU_ADD_DEVICE,
					MainMenu.MENU_ADD_PREDEFINED + ": "
							+ MainMenu.MENU_ADD_LIGHTBULB,
					MainMenu.MENU_ADD_PREDEFINED + ": "
							+ MainMenu.MENU_ADD_COFFEE_MAKER,
					MainMenu.MENU_ADD_FILE, MainMenu.MENU_ADD_PHYSICAL };
			String type = (String) JOptionPane.showInputDialog(null,
					"All existing devices will be deleted! \n\n"
							+ "Device Type:", "New Standalone Device",
					JOptionPane.QUESTION_MESSAGE, null, options,
					MainMenu.MENU_ADD_DEVICE);
			// System.out.println(type);

			if (type != null) {
				MediatorComponent.getInstance().setMode(DPWSimMainWindow.MODE_DEVICE_NEW);
				DPWSUtilities.clearDevices(main_window);
				
				if (type.equals(MainMenu.MENU_ADD_DEVICE)) {
					addNewDevice();
				} else if (type.equals(MainMenu.MENU_ADD_PREDEFINED + ": "
						+ MainMenu.MENU_ADD_LIGHTBULB)) {
					addPredefinedLightBulb();
				} else if (type.equals(MainMenu.MENU_ADD_PREDEFINED + ": "
						+ MainMenu.MENU_ADD_COFFEE_MAKER)) {
					addPredefinedCoffeeMaker();
				} else if (type.equals(MainMenu.MENU_ADD_FILE)) {
					addFromFile();
				} else if (type.equals(MainMenu.MENU_ADD_PHYSICAL)) {
					addFromPhysical();
				}
			} 
			break;
		case MainMenu.MENU_ADD_DEVICE:
			addNewDevice();
			break;
		case MainMenu.MENU_ADD_LIGHTBULB:
			addPredefinedLightBulb();
			break;
		case MainMenu.MENU_ADD_COFFEE_MAKER:
			addPredefinedCoffeeMaker();
			break;
		case MainMenu.MENU_ADD_FILE:
			//addFromFile();
			DPWSimDevice device;
			returnVal = fc.showOpenDialog(null);
			if (returnVal == JFileChooser.APPROVE_OPTION){
				file = fc.getSelectedFile();
				try{
				   FileInputStream fin = new FileInputStream(file);
				   ObjectInputStream ois = new ObjectInputStream(fin);
				   device = (DPWSimDevice) ois.readObject();
				   ois.close();
				   
				   device.setComponentPopupMenu(new DevicePopupMenu(main_window.getMode()));
				   
				   main_window.activeDevice = device;
				   main_window.devices.add(device);
				   main_window.add(device);

				   main_window.setLocationRelativeTo(null);
				   main_window.validate();
				   
				   SwingUtilities.showInformationMessage(null, "Device loaded!");
				   
				}catch(Exception ex){
				   ex.printStackTrace();
				   SwingUtilities.showInformationMessage(null, ex.getMessage());
				}
			}
			break;
			
		case MainMenu.MENU_ADD_PHYSICAL:
			addFromPhysical();
			SwingUtilities.showInformationMessage(null,
						MainMenu.MENU_ADD_PHYSICAL + ": Under construction!");
			break;
		case MainMenu.MENU_CLEAR:
			menuClear();
			break;

		
		/* New Device Action */
		case NewDevicePanel.CREATE_DEVICE:
			if (device_panel.invariant()) {
				try {
					createNewDevice(device_panel.nameField.getText(),
							device_panel.manField.getText(),
							device_panel.nsField.getText(),
							device_panel.ipField.getText(),
							device_panel.httpPortField.getText(),
							device_panel.typeField.getText(),
							device_panel.defaultURL.getText());
				} catch (IOException ex) {
					SwingUtilities.showErrorMessage(main_window, "Device Creation Exception!");
				}
				device_dialog.setVisible(false);
				device_panel.reset();
				break;
			} else {
				command = MainMenu.MENU_ADD_DEVICE;
				break;
			}

		}
	}

	private void addFromPhysical() {
		// TODO Auto-generated method stub
		
	}

	private void addFromFile() {
		int returnVal = fc.showOpenDialog(null);
		if (returnVal == JFileChooser.APPROVE_OPTION){
			file = fc.getSelectedFile();
			try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
				String[] infos = null;
				String mode = reader.readLine();
				String line = null;
				if (mode.indexOf(MainMenu.MENU_NEW_STANDALONE_DEVICE) >= 0 ){
					while ((line = reader.readLine()) != null) {
						if (line.indexOf("DEVICE") >= 0){
							infos = line.split(",");
							String deviceName = infos[1];
							String manufacturer = infos[2];
							String namespace = infos[3];
							String ipaddress = infos[4];
							String deviceport = infos[5];
							String serviceport = infos[6];
							
							DefaultTableModel opModel = new DefaultTableModel();
							opModel.setColumnIdentifiers(new Object[] { "Name", "Parameter",
										"Status Image Location" });
							while ((line = reader.readLine()).indexOf("OPERATION") >= 0) {
								infos = line.split(",");
								opModel.addRow(new Object[]{infos[1], infos[2], infos[3]});
							}
							
							DefaultTableModel evModel = new DefaultTableModel();
							evModel.setColumnIdentifiers(new Object[] { "Name", "Parameter",
										"Event Message", "Frequency (ms)" });
							while ((line = reader.readLine()).indexOf("EVENT") >= 0) {
								infos = line.split(",");
								evModel.addRow(new Object[]{infos[1], infos[2], infos[3], infos[4]});
							}
							try{
								createNewDevice(deviceName, manufacturer, namespace, ipaddress, deviceport, serviceport, "");
							} catch (IOException e1){
								SwingUtilities.showErrorMessage(null, "Device Creation Exception!");
							}
							//int x = Integer.parseInt(reader.readLine());
							//int y = Integer.parseInt(reader.readLine());
							//main_window.activeStatus.setLocation(x, y);
							//main_window.activeInfo.addInfo("\n" + x + "\n" + y);
						}
					}
					
				} else {
					SwingUtilities.showInformationMessage(null, "Not a device file!");
				}
				reader.close();
				
			}catch (IOException ex) {
				SwingUtilities.showErrorMessage(null, ex.getStackTrace());
				return;
			}
			//SwingUtilities.showInformationMessage(null, file.getAbsolutePath());
		}
		
	}

	private void addNewDevice() {
		device_panel.reset();
		device_panel.ipField
				.setText(space_panel.getIpField().getText());
		//device_dialog.setContentPane(device_panel);
		//device_dialog.setTitle("New Device");
		device_dialog.setContentPane(device_panel);
		device_dialog.setSize(600, 200);
		device_dialog.setTitle("New Device");
		device_dialog.setVisible(true);
	}

	private void addPredefinedCoffeeMaker() {
		String ipAddress = JOptionPane.showInputDialog(
				"Device IP Binding Address", space_panel.getIpField().getText());

		if (ipAddress != null
				&& SwingUtilities.validateIPAddress(ipAddress)) {
			DefaultTableModel opModel = new DefaultTableModel(
					new Object[][] {
							{ "SwitchOn", "param",
									"/res/devices/coffeeon.png" },
							{ "SwitchOff", "param",
									"/res/devices/coffeeoff.png" } },
					// null,
					new Object[] { "Name", "Parameter",
							"Status Image Location" });

			DefaultTableModel evModel = new DefaultTableModel(
					new Object[][] {
							{ "Finish", "param", "Coffee ready!", "0" },
							{ "WaterOut", "param", "Out of water!", "0" },
							{ "CoffeeOut", "param", "Out of coffee!",
									"0" } }
					// null
					, new Object[] { "Name", "Parameter",
							"Event Message", "Frequency (ms)" });

			try {
				createNewDevice("CM" + System.currentTimeMillis(),
						"Telecom SudParis", "http://telecom-sudparis.eu",
						ipAddress, "4567", "CoffeeMaker", "");
			} catch (IOException ex) {
				SwingUtilities.showErrorMessage(main_window, "Device Creation Exception!");
			}
		}
	}

	private void addPredefinedLightBulb() {
		String ipAddress = JOptionPane.showInputDialog(
				"Device IP Binding Address", space_panel.getIpField().getText());
		if (ipAddress != null
				&& SwingUtilities.validateIPAddress(ipAddress)) {
			DefaultTableModel opModel = new DefaultTableModel(
					new Object[][] {
							{ "SwitchOn", "param",
									"/res/devices/lighton.png" },
							{ "SwitchOff", "param",
									"/res/devices/lightoff.png" } },
					// null,
					new Object[] { "Name", "Parameter",
							"Status Image Location" });

			DefaultTableModel evModel = new DefaultTableModel(
					new Object[][] { { "PresenceDetect", "paramOn",
							"Object detected!", "0" } }, new Object[] {
							"Name", "Parameter", "Event Message",
							"Frequency (ms)" });

			try {
				createNewDevice("L" + System.currentTimeMillis(),
						"Telecom SudParis", "http://telecom-sudparis.eu",
						ipAddress, "4567", "LightBulb", "");
			} catch (IOException ex) {
				SwingUtilities.showErrorMessage(main_window, "Device Creation Exception!");
			}
		}
	}

	

		/**
	 * Clear menu
	 */
	private void menuClear() {
		if (main_window.devices.isEmpty())
			SwingUtilities.showInformationMessage(null, "There is no device!");
		else {
			int confirm = JOptionPane.showConfirmDialog(null,
					"Are you sure to remove all the devices?",
					"Remove Devices", JOptionPane.YES_NO_OPTION);
			if (confirm == JOptionPane.YES_OPTION){
				//DPWSUtilities.clearDevices(main_window);
				Iterator<DPWSimDevice> itr = main_window.devices.iterator();
				while (itr.hasNext()) {
					DPWSimDevice device = itr.next();
					if (device.getDevice().isRunning()){
						try {
							device.getDevice().stop();
						} catch (IOException e1) {
							e1.printStackTrace();
						}
					}
					main_window.devices.remove(device);
					main_window.remove(device);
				}
				main_window.revalidate();
				main_window.repaint();
			}
		}
	}

	/**
	 * show New Space input form
	 */
	private void showNewSpace() {

		DPWSUtilities.clearDevices(main_window);

		MediatorComponent.getInstance()
				.setMode(DPWSimMainWindow.MODE_SPACE_NEW);

		main_window.setTitle(DPWSimMainWindow.DPWSIM + " - New Space");
		main_window.setDefaultSize();
		main_window.setContentPane(space_panel);
		space_panel.reset();
		main_window.setLocationRelativeTo(null);
		main_window.validate();
	}
	
	/**
	 * Create new space, go when user presses the Create Space button
	 */
	private void createSpace(String name, String layoutURL, String ipAddress) {
		if (name.equals("")) name = "Space";
		ImageIcon icon = new ImageIcon(layoutURL);
		main_window.spaceinfo = name + "," + layoutURL + "," + ipAddress;
		
		JLabel content = new JLabel();

		if ((icon == null) || (icon.getIconHeight() < 0)) {
			icon = SwingUtilities.createDefaultLayout();
		}

		content.setHorizontalAlignment(JLabel.CENTER);
		content.setIcon(icon);

		MediatorComponent.getInstance().setMode(
				DPWSimMainWindow.MODE_SPACE_CREATED);
		
		main_window.setTitle(DPWSimMainWindow.DPWSIM + " - " + name);
		main_window.setContentPane(content);
		main_window.pack();
		main_window.validate();
		main_window.setLocationRelativeTo(null);
	}

	/**
	 * Create new device. to add device to a space or create a new standalone
	 * device
	 * 
	 * @param deviceName
	 *            device name
	 * @param manufacturer
	 *            manufacturer
	 * @param namespace
	 *            namespace
	 * @param ipaddress
	 *            binding address
	 * @param deviceport
	 *            port for device binding
	 * @param serviceport
	 *            port for service binding
	 * @param opModel
	 *            table model for operations [Operation Name, Parameter, Status
	 *            Image URL]
	 * @param evModel
	 *            table model for events [Event Name, Parameter, Message,
	 *            Frequency]
	 * @throws IOException 
	 */
	private void createNewDevice(String deviceName, String manufacturer,
			String namespace, String ipaddress, String httpport,
			String devicetype,
			String iconURL) throws IOException 
	{
		// If device name is not provided, set it to a name with time stamp
		if (deviceName.equals("")) deviceName = "D" + System.currentTimeMillis();
		
		int port = Integer.parseInt(httpport);
		main_window.activeDevice = new DPWSimDevice(deviceName, devicetype, manufacturer, namespace, ipaddress, port, port, iconURL);
			
		DevicePopupMenu devicePopupHandler = new DevicePopupMenu(main_window.getMode());
		main_window.activeDevice.setComponentPopupMenu(devicePopupHandler);
		
		main_window.devices.add(main_window.activeDevice);
		device_dialog.setVisible(false);

		/*
		 * If building the space or adding device (in case users don't click
		 * to choose position)
		 */
		if (main_window.getMode() == DPWSimMainWindow.MODE_SPACE_CREATED
				|| main_window.getMode() == DPWSimMainWindow.MODE_ADDING_DEVICE) {
	
			//main_window.setActiveDevice(main_window.activeStatus);
	
			main_window.add(main_window.activeDevice);
	
			// Set all the device visual to 32 pixel
			main_window.activeDevice.setIcon(new ImageIcon(
							SwingUtilities.resizeToHeight(((ImageIcon) main_window.activeDevice.getIcon()).getImage(), 32)));
	
			// Format the device visual
			main_window.activeDevice
					.setBounds(0, 0, main_window.activeDevice.getIcon().getIconWidth(),
							main_window.activeDevice.getIcon().getIconHeight());
	
			MediatorComponent.getInstance().setMode(
					DPWSimMainWindow.MODE_ADDING_DEVICE);
		} else if (main_window.getMode() == DPWSimMainWindow.MODE_DEVICE_NEW) {
	
			main_window.setTitle(DPWSimMainWindow.DPWSIM + " - "
					+ deviceName);
	
			main_window.setContentPane(main_window.activeDevice);
			main_window.pack();
	
			if (main_window.getWidth() < 400)
				main_window.setSize(400, 420);
	
			MediatorComponent.getInstance().setMode(
					DPWSimMainWindow.MODE_DEVICE_CREATED);
	
			main_window.setLocationRelativeTo(null);
			main_window.validate();
		}

	}
}
