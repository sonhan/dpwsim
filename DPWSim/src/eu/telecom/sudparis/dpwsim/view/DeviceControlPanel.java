package eu.telecom.sudparis.dpwsim.view;

import java.awt.GridBagConstraints;


import java.awt.FlowLayout;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import eu.telecom.sudparis.dpws.GenericOperation;
import eu.telecom.sudparis.dpwsim.handler.DeviceControlHandler;
import eu.telecom.sudparis.dpwsim.upgrade.DPWSimDevice;
import eu.telecom.sudparis.dpwsim.upgrade.EventProvider;
import eu.telecom.sudparis.dpwsim.upgrade.MediatorComponent;
import eu.telecom.sudparis.dpwsim.view.tools.SwingUtilities;

/**
 * Device Control Panel
 * 
 * @author Son Han
 * @date 2013/12/07
 * @version 1.0
 */

@SuppressWarnings("serial")
public class DeviceControlPanel extends JPanel implements PanelInterface {
	public JTextField nameField = new JTextField();
	public JTextField manField = new JTextField();
	public JTextField ipField = new JTextField();
	public JTextField httpPortField = new JTextField();
	public JTextField typeField = new JTextField();
	public JTextField nsField = new JTextField();
	
	private JComboBox opCombo = new JComboBox();
	private JComboBox opParamCombo = new JComboBox();
	private JTextField paResponseField = new JTextField();
		
	private JLabel iconPreview = new JLabel();
	
	private JComboBox evCombo = new JComboBox();
	private JTextField evMessageField = new JTextField();
	private JTextField evFreqField = new JTextField();
	
	private JButton fireEvent = new JButton("Fire event");
	
	private JButton addOp = new JButton("+");
	private JButton deleteOp = new JButton("-");
	private JButton editOp = new JButton(SwingUtilities.createImageIcon("/res/icon_edit.png", 15));
	
	private JButton addPa = new JButton("+");	
	private JButton deletePa = new JButton("-");
	private JButton editPa = new JButton(SwingUtilities.createImageIcon("/res/icon_edit.png", 16));
	
	private JButton addEv = new JButton("+");
	private JButton deleteEv = new JButton("-");
	private JButton editEv = new JButton(SwingUtilities.createImageIcon("/res/icon_edit.png", 16));
	
	private JButton startDevice = new JButton("Start", SwingUtilities.createImageIcon("/res/icon_start.png", 16));
	private JButton closeButton = new JButton("Close");
	
	EventProvider activeEvent;
	

	public DeviceControlPanel(DeviceControlHandler deviceControlHandler) {
		super();
		MediatorComponent.getInstance().setControlPanel(this);
		
		initComponents(deviceControlHandler);
		
		this.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));
		
		GridBagLayout gridbag = new GridBagLayout();
		this.setLayout(gridbag);
		
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		c.insets = new Insets(1, 1, 1, 1);
		c.weightx = 1;
		
		c.gridwidth = GridBagConstraints.REMAINDER;
		
		c.gridy = 0;	this.add(createInfoPanel(), c);
		c.gridy++;		this.add(createOperationPanel(), c);
		c.gridy++;		this.add(createEventPanel(), c);
		
		c.weighty = 1;
		
		JPanel controlPanel = new JPanel();
		controlPanel.add(startDevice);
		controlPanel.add(closeButton);
		
		c.gridy++;		this.add(controlPanel, c);
	}

	
	private JPanel createInfoPanel() {
		JPanel panel = new JPanel();
		panel.setLayout(new GridBagLayout());
		//panel.setBorder(BorderFactory.createEtchedBorder());

		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		c.insets = new Insets(2, 2, 2, 2);
		
		c.gridy = 0;
		
		c.weightx = 0;		panel.add(new JLabel("Device Name"), c);
		c.weightx = 0.5;	panel.add(nameField, c);
		c.weightx = 0;		panel.add(new JLabel("Manufacturer"), c);
		c.weightx = 0.5;	panel.add(manField, c);

		c.gridy++;

		c.weightx = 0;		panel.add(new JLabel("Namespace"), c);
		c.weightx = 0.5;	panel.add(nsField, c);
		c.weightx = 0;		panel.add(new JLabel("IP Address"), c);
		c.weightx = 0.5;	panel.add(ipField, c);

		c.gridy++;
		c.weightx = 0;		panel.add(new JLabel("HTTP Port"), c);
		c.weightx = 0.5;	panel.add(httpPortField, c);
		c.weightx = 0;		panel.add(new JLabel("Type"), c);
		c.weightx = 0.5;	panel.add(typeField, c);
		
		return panel;
	}

	private JPanel createOperationPanel() {
		JPanel panel = new JPanel();
		panel.setLayout(new GridBagLayout());
		panel.setBorder(BorderFactory.createTitledBorder("Operations"));

		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		c.insets = new Insets(1, 1, 1, 1);
		
		c.gridy = 0;
		
		JPanel panelTemp = new JPanel();
		panelTemp.setAlignmentX(LEFT_ALIGNMENT);
		((FlowLayout) (panelTemp.getLayout())).setVgap(0);
		((FlowLayout) (panelTemp.getLayout())).setHgap(0);
		panelTemp.add(addOp, c); panelTemp.add(deleteOp, c); panelTemp.add(editOp);
		
		c.weightx = 1;	panel.add(opCombo, c);
		c.fill = GridBagConstraints.VERTICAL;
		c.weightx = 0;	panel.add(panelTemp);
		
		
		
		c.fill = GridBagConstraints.BOTH;
		c.gridy++; 
		c.weightx = 1;
		
		panel.add(new JLabel("Request Value"), c);
		panel.add(new JLabel("Response Message"), c);
		c.weightx = 0;	panel.add(new JLabel("Status Image"), c);
		
		c.gridy++;
		
		c.weightx = 1;	panel.add(opParamCombo, c);
		c.weightx = 1;	panel.add(paResponseField, c);
//		panel.add(urlField, c);
		c.weightx = 0;	panel.add(iconPreview, c);
		panel.add(addPa, c);
		panel.add(deletePa, c);
		panel.add(editPa, c);
		
		return panel;
	}
	
	private JPanel createEventPanel() {
		JPanel panel = new JPanel();
		panel.setLayout(new GridBagLayout());
		panel.setBorder(BorderFactory.createTitledBorder("Events"));

		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		c.insets = new Insets(1, 1, 1, 1);
		
		c.weightx = 1;
		c.gridy = 0;
		
		panel.add(new JLabel(""), c);
		panel.add(new JLabel("Notification Message"), c);
		panel.add(new JLabel("Frequency"), c);
		
		c.gridy++;
		
		panel.add(evCombo, c);
		panel.add(evMessageField, c);
		panel.add(evFreqField, c);
		
		c.weightx = 0;
		panel.add(fireEvent, c);
		panel.add(addEv, c);
		panel.add(deleteEv, c);
		panel.add(editEv, c);
		
		return panel;
	}
	
	private void initComponents(DeviceControlHandler deviceControlHandler) {
		nameField.setEditable(false);
		manField.setEditable(false);
		ipField.setEditable(false);
		httpPortField.setEditable(false);
		typeField.setEditable(false);
		nsField.setEditable(false);
		
		iconPreview.setBorder(BorderFactory.createEtchedBorder());
		iconPreview.setHorizontalAlignment(JLabel.CENTER);
		
		opCombo.addActionListener(deviceControlHandler);
		addOp.addActionListener(deviceControlHandler);		addOp.setToolTipText("Add new operation");
		opParamCombo.addActionListener(deviceControlHandler);
		deletePa.addActionListener(deviceControlHandler);	deletePa.setToolTipText("Delete request value");
		editPa.addActionListener(deviceControlHandler);		editPa.setToolTipText("Edit request value");
		addPa.addActionListener(deviceControlHandler);		addPa.setToolTipText("Add request value");

		evCombo.addActionListener(deviceControlHandler);
		fireEvent.addActionListener(deviceControlHandler);
		addEv.addActionListener(deviceControlHandler);		addEv.setToolTipText("Add new event");
		editEv.addActionListener(deviceControlHandler);		editEv.setToolTipText("Edit event");
		
		startDevice.addActionListener(deviceControlHandler);
		closeButton.addActionListener(deviceControlHandler);
		
		paResponseField.setEditable(false);
		evMessageField.setEditable(false);
		evFreqField.setEditable(false);
		
		addPa.setEnabled(false);
		deletePa.setEnabled(false);
		editPa.setEnabled(false);
		
		editEv.setEnabled(false);
	}


	@Override
	public void reset() {
		// TODO Auto-generated method stub
		
	}


	@Override
	public boolean invariant() {
		// TODO Auto-generated method stub
		return false;
	}


	public void loadDeviceInfo(DPWSimDevice device) {
		nameField.setText(device.getDevice().getFriendlyName("en-US"));
		manField.setText(device.getDevice().getManufacturer("en-US"));
		nsField.setText(device.getDevice().getNamespace());
		ipField.setText(device.getDevice().getHttpBindIP());
		httpPortField.setText(device.getDevice().getPortDevice() + "");
		typeField.setText(device.getDevice().getPortType());
		
		opCombo.setModel(new DefaultComboBoxModel(device.getOperationNames().toArray()));
		
		if (device.getOps().size() > 0){
			DeviceControlHandler.updateFieldsOfOperation(device.getOps().get(0));
		}
		evCombo.setModel(new DefaultComboBoxModel(device.getEventNames().toArray()));
		
		if (device.getEvs().size() > 0){
			DeviceControlHandler.updateFieldsOfEvent(device.getEvs().get(0));
		}
		
		boolean isRunning = device.getDevice().isRunning();
		
		addOp.setEnabled(!isRunning);
		addEv.setEnabled(!isRunning);
		fireEvent.setEnabled(isRunning);
		
		if (isRunning) {
			startDevice.setText("Stop");
			startDevice.setIcon(SwingUtilities.createImageIcon("/res/icon_stop.png", 16));
		}
		else {
			startDevice.setText("Start");
			startDevice.setIcon(SwingUtilities.createImageIcon("/res/icon_start.png", 16));			
		}
	}


	public JTextField getNameField() {
		return nameField;
	}


	public void setNameField(JTextField nameField) {
		this.nameField = nameField;
	}


	public JTextField getManField() {
		return manField;
	}


	public void setManField(JTextField manField) {
		this.manField = manField;
	}


	public JTextField getIpField() {
		return ipField;
	}


	public void setIpField(JTextField ipField) {
		this.ipField = ipField;
	}


	public JTextField getHttpPortField() {
		return httpPortField;
	}


	public void setHttpPortField(JTextField httpPortField) {
		this.httpPortField = httpPortField;
	}


	public JTextField getTypeField() {
		return typeField;
	}


	public void setTypeField(JTextField typeField) {
		this.typeField = typeField;
	}


	public JTextField getNsField() {
		return nsField;
	}


	public void setNsField(JTextField nsField) {
		this.nsField = nsField;
	}


	public JComboBox getOpCombo() {
		return opCombo;
	}


	public void setOpCombo(JComboBox opCombo) {
		this.opCombo = opCombo;
	}


	public JComboBox getOpParamCombo() {
		return opParamCombo;
	}


	public void setOpParamCombo(JComboBox opParamCombo) {
		this.opParamCombo = opParamCombo;
	}


	public JTextField getOpOutputField() {
		return paResponseField;
	}


	public void setOpOutputField(JTextField opOutputField) {
		this.paResponseField = opOutputField;
	}

//	public JButton getOpSetButton() {
//		return opSetButton;
//	}
//
//
//	public void setOpSetButton(JButton opSetButton) {
//		this.opSetButton = opSetButton;
//	}
//
//
//	public JButton getOpDelButton() {
//		return opDelButton;
//	}
//
//
//	public void setOpDelButton(JButton opDelButton) {
//		this.opDelButton = opDelButton;
//	}


	public JLabel getIconPreview() {
		return iconPreview;
	}


	public void setIconPreview(JLabel urlPreview) {
		this.iconPreview = urlPreview;
	}


	public JComboBox getEvCombo() {
		return evCombo;
	}


	public void setEvCombo(JComboBox evCombo) {
		this.evCombo = evCombo;
	}


	public JTextField getEvMessageField() {
		return evMessageField;
	}


	public void setEvMessageField(JTextField evMessageField) {
		this.evMessageField = evMessageField;
	}


	public JTextField getEvFreqField() {
		return evFreqField;
	}


	public void setEvFreqField(JTextField evFreqField) {
		this.evFreqField = evFreqField;
	}


	public JButton getFireEvent() {
		return fireEvent;
	}


	public void setFireEvent(JButton fireEvent) {
		this.fireEvent = fireEvent;
	}

//	public JButton getEvSetButton() {
//		return evSetButton;
//	}
//
//
//	public void setEvSetButton(JButton evSetButton) {
//		this.evSetButton = evSetButton;
//	}
//
//
//	public JButton getEvDelButton() {
//		return evDelButton;
//	}
//
//
//	public void setEvDelButton(JButton evDelButton) {
//		this.evDelButton = evDelButton;
//	}


	public JButton getAddOp() {
		return addOp;
	}


	public void setAddOp(JButton addOp) {
		this.addOp = addOp;
	}


	public JButton getAddEv() {
		return addEv;
	}


	public void setAddEv(JButton addEv) {
		this.addEv = addEv;
	}


	public JButton getAddPa() {
		return addPa;
	}


	public void setAddPa(JButton addPa) {
		this.addPa = addPa;
	}

	public EventProvider getActiveEvent() {
		return activeEvent;
	}


	public void setActiveEvent(EventProvider activeEvent) {
		this.activeEvent = activeEvent;
	}


	public JButton getDeletePa() {
		return deletePa;
	}


	public void setDeletePa(JButton deletePa) {
		this.deletePa = deletePa;
	}


	public JButton getEditPa() {
		return editPa;
	}


	public void setEditPa(JButton editPa) {
		this.editPa = editPa;
	}


	public JButton getEditEv() {
		return editEv;
	}


	public void setEditEv(JButton editEv) {
		this.editEv = editEv;
	}


	public JButton getStartDevice() {
		return startDevice;
	}


	public void setStartDevice(JButton startDevice) {
		this.startDevice = startDevice;
	}


	public JButton getCloseButton() {
		return closeButton;
	}


	public void setCloseButton(JButton closeButton) {
		this.closeButton = closeButton;
	}
}
