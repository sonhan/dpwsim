package eu.telecom.sudparis.dpwsim.view;

import java.awt.GridBagConstraints;


import java.awt.FlowLayout;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

import eu.telecom.sudparis.dpwsim.handler.ActionHandler;
import eu.telecom.sudparis.dpwsim.upgrade.MediatorComponent;
import eu.telecom.sudparis.dpwsim.view.tools.SwingUtilities;

/**
 * New device panel
 * 
 * @author Son Han
 * @date 2013/09/20
 * @version 2.0
 */
@SuppressWarnings("serial")
public class NewDevicePanel extends JPanel implements PanelInterface, ActionListener {

	public static final String CREATE_DEVICE = "Create Device";
	public static final String BROWSER = "...";

	public JTextField nameField;
	public JTextField manField;
	public JTextField ipField;
	public JTextField httpPortField;
	public JTextField typeField;
	public JTextField nsField;
	public JTextField defaultURL;
	
	public JLabel		iconPreview;

// Son Han: I am using the DPWSim tool of showing error message
	//public JTextArea errorNotice;

	private JButton createButton;
	private JButton browserButton;

	public NewDevicePanel(ActionHandler actionHandler) {
		MediatorComponent.getInstance().setNewDevicePanel(this);
		this.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));
		this.setLayout(new GridBagLayout());

		initComponents(actionHandler);
		
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		c.insets = new Insets(1, 1, 1, 5);
		
		c.gridy = 0;
		
		c.weightx = 0;		this.add(new JLabel("Device Name"), c);
		c.weightx = 0.5;	this.add(nameField, c);
		c.weightx = 0;		this.add(new JLabel("Manufacturer"), c);
		c.weightx = 0.5;	this.add(manField, c);

		c.gridy++;

		c.weightx = 0;		this.add(new JLabel("Namespace"), c);
		c.weightx = 0.5;	this.add(nsField, c);
		c.weightx = 0;		this.add(new JLabel("IP Address"), c);
		c.weightx = 0.5;	this.add(ipField, c);

		c.gridy++;
		
		c.weightx = 0;		this.add(new JLabel("HTTP Port"), c);
		c.weightx = 0.5;	this.add(httpPortField, c);
		c.weightx = 0;		this.add(new JLabel("Type"), c);
		c.weightx = 0.5;	this.add(typeField, c);
		
		c.gridy++;
		c.insets = new Insets(1, 1, 1, 1);
		
		JPanel urlPanel = new JPanel();
		urlPanel.setLayout(new GridBagLayout());
		c.weightx = 1; urlPanel.add(defaultURL, c);
		c.weightx = 0; urlPanel.add(iconPreview, c); 
		c.weightx = 0; urlPanel.add(browserButton, c);
		
		c.weightx = 0;		this.add(new JLabel("Default Status"), c);
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.weightx = 0.5;	this.add(urlPanel, c);
		
		c.gridy++;
		
		c.fill = 0;
		
		c.weighty = 1;
		this.add(createButton, c);
	}

	public void actionPerformed(ActionEvent e) {
		JFileChooser fc = new JFileChooser(".");
		File file = null;
		
		int returnVal = fc.showOpenDialog(null);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			file = fc.getSelectedFile();
			defaultURL.setText(file.getAbsolutePath());
			iconPreview.setIcon(SwingUtilities.createImageIcon(file.getAbsolutePath(), 16));
		}
	}
	
	/**
	 * Reset fields: device name, operations and events Keep information about
	 * IP address, manufacturer, ...
	 * 
	 */
	public void reset() {
		this.nameField.setText("");
// Son Han: I am using the DPWSim tool of showing error message		
		// this.errorNotice.setText("");
	
	}

	/**
	 * Initialize all the Swing components
	 * 
	 * @param actionHandler
	 *            action handler
	 */
	private void initComponents(ActionHandler actionHandler) {
		nameField = new JTextField();
		manField = new JTextField("Telecom SudParis");
		nsField = new JTextField("http://telecom-sudparis.eu");
		ipField = new JTextField("127.0.0.1");
		httpPortField = new JTextField("4567");
		typeField = new JTextField("DPWSim");
		defaultURL = new JTextField();
		iconPreview = new JLabel();
		iconPreview.setBorder(BorderFactory.createEtchedBorder());
		iconPreview.setHorizontalAlignment(JLabel.CENTER);
		iconPreview.setIcon(SwingUtilities.createDefaultDeviceIcon(16));
		
		createButton = new JButton(CREATE_DEVICE);
		createButton.addActionListener(actionHandler);
		
		browserButton = new JButton(BROWSER);
		browserButton.addActionListener(this);
	}

	@Override
	public boolean invariant() {
		boolean ok = true;
		String erroMsg = "Error: \n";
		if (!SwingUtilities.validateIPAddress(ipField.getText())) {
			ok = false;
			erroMsg += "IP address is invalid!\n";
		}
		if (!SwingUtilities.isInteger(httpPortField.getText())) {
			ok = false;
			erroMsg += "Device port is invalid!\n";
		}
// Son Han: It is not required for a device to have an event		
		// if (evModel.getRowCount() < 1) {
		//	ok = false;
		//	erroMsg += "Device doesn't have any event!\n";
		// }
		
		if (!ok) {
// Son Han: I am using the DPWSim tool of showing error message
// This method doesn't require to add another field in the panel
// It has been using throughout the software
// Please compare two methods
			//this.errorNotice.setText(erroMsg);
			SwingUtilities.showErrorMessage(null, erroMsg);
		}
		return ok;
	}
}
