package eu.telecom.sudparis.dpwsim.view;

import java.awt.Dimension;


import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.UnknownHostException;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import eu.telecom.sudparis.dpwsim.upgrade.MediatorComponent;
import eu.telecom.sudparis.dpwsim.view.tools.SwingUtilities;

/**
 * New space panel 
 * 
 * @author	Son Han
 * @date	2013/09/20
 * @version 2.0
 */
@SuppressWarnings("serial")
public class NewSpacePanel extends JPanel implements ActionListener {

	public static final String BROWSER = "Browser";
	public static final String RESET_SPACE = "Reset Space";
	public static final String CREATE_SPACE = "Create Space";

	public static final String DEFAULT_LAYOUT = "Default layout";
	
	private JTextField nameField = new JTextField();
	private JTextField ipField = new JTextField("127.0.0.1");
	private JTextArea layoutField = new JTextArea(DEFAULT_LAYOUT);
	private JButton browserButton = new JButton(BROWSER);
	private JButton defaultButton = new JButton(RESET_SPACE);
	private JButton createButton = new JButton(CREATE_SPACE);
	private JLabel layoutPreview = new JLabel();

	public NewSpacePanel() {
		MediatorComponent.getInstance().setNewSpacePanel(this);
		this.setBorder(BorderFactory.createEmptyBorder(10,10,0,10));
		GridBagLayout gridbag = new GridBagLayout();
		GridBagConstraints c = new GridBagConstraints();
		this.setLayout(gridbag);

		// this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		// this.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
		// this.setBorder(BorderFactory.createEtchedBorder());
		// this.setBorder(BorderFactory.createTitledBorder(null,
		// "NEW SPACE PARAMETERS", TitledBorder.RIGHT, TitledBorder.TOP));
		
		//layoutField.setText(getClass().getResource("/home_dim_1024.png").getPath());
		layoutField.setEditable(false);
		layoutField.setOpaque(false);
		layoutField.setLineWrap(true);
		layoutField.setPreferredSize(new Dimension(30, 20));
		layoutField.setFont(new Font("Arial", Font.ITALIC, 10));
		
		browserButton.addActionListener(this);
		createButton.addActionListener(this);
		defaultButton.addActionListener(this);
		
		ImageIcon icon = new ImageIcon(SwingUtilities.resizeToHeight(SwingUtilities.createDefaultLayout().getImage(), 200));
		layoutPreview.setIcon(icon);
		//layoutPreview.setHorizontalTextPosition(JLabel.CENTER);
		//layoutPreview.setVerticalTextPosition(JLabel.BOTTOM);
		
		JPanel layoutHolder = new JPanel();
		layoutHolder.add(layoutPreview);
		layoutHolder.setBorder(BorderFactory.createEtchedBorder());
		layoutHolder.setPreferredSize(new Dimension(200, 220));
		
//		createButton.setEnabled(false);
		
		c.fill = GridBagConstraints.BOTH;
		c.insets = new Insets(1, 1, 1, 5);		

// Row 0
		c.weightx = 0;
		this.add(new JLabel("Space Name"), c);
		
		c.weightx = 1;
		c.gridwidth = 2;
		this.add(nameField, c);
		
// Row 1		
		c.gridy = 1;	
		c.weightx = 0;
		c.gridwidth = 1;
		this.add(new JLabel("Devices IP Address"), c);
		
		c.weightx = 1;
		c.gridwidth = 2;
		this.add(ipField, c);
		
// Row 2		
		c.gridy = 2;	
		c.weightx = 0;
		c.gridwidth = 1;
		this.add(new JLabel("Layout Image"), c);
		
		c.weightx = 1;
		c.gridheight = 3;
		this.add(layoutHolder, c);
		
		c.weightx = 0;
		c.gridheight = 1;
		this.add(browserButton, c);
		
		
// Row 3		
		c.gridy = 3; 
		this.add(defaultButton,c);
		c.gridy =4;
		this.add(layoutField, c);
		

// Row 4		
		c.gridy = 5;
		c.fill = GridBagConstraints.NONE;
		c.weighty = 1;
		c.gridwidth = 3;
		this.add(createButton, c);
		
		reset();
		
	}
		
	public JTextField getNameField() {
		return nameField;
	}

	public JTextField getIpField() {
		return ipField;
	}

	public JTextArea getLayoutField() {
		return layoutField;
	}

	public JLabel getLayoutPreview() {
		return layoutPreview;
	}

	public void reset() {
		this.nameField.setText("");
		layoutField.setText(NewSpacePanel.DEFAULT_LAYOUT);
		ImageIcon imageIcon = SwingUtilities.createDefaultLayout();
		imageIcon = new ImageIcon(SwingUtilities.resizeToHeight(
				imageIcon.getImage(), 200));

		layoutPreview.setText("");
		layoutPreview.setIcon(imageIcon);
		
		try {
			ipField.setText(Inet4Address.getLocalHost().getHostAddress().toString());
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		String command = e.getActionCommand();
		switch (command){
		case NewSpacePanel.BROWSER:
			JFileChooser fc = new JFileChooser(".");
			File file = null;
			int returnVal = fc.showOpenDialog(null);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				file = fc.getSelectedFile();
				layoutField.setText(file.getAbsolutePath());
				ImageIcon imageIcon = SwingUtilities.createImageIcon(file.getAbsolutePath(), 200);

				layoutPreview.setText("");
				layoutPreview.setIcon(imageIcon);
			}
			break;

		case NewSpacePanel.CREATE_SPACE:
			createSpace(nameField.getText(), layoutField.getText(),	ipField.getText());
			break;

		case NewSpacePanel.RESET_SPACE:
			reset();
			break;

		}
	}
	
	/**
	 * Create new space, go when user presses the Create Space button
	 */
	private void createSpace(String name, String layoutURL, String ipAddress) {
		DPWSimMainWindow main_window = MediatorComponent.getInstance().getMainWidow();
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
}
