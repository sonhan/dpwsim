package eu.telecom.sudparis.dpwsim.upgrade;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import eu.telecom.sudparis.dpwsim.view.PanelInterface;
import eu.telecom.sudparis.dpwsim.view.tools.RequestFocusListener;
import eu.telecom.sudparis.dpwsim.view.tools.SwingUtilities;

/**
 * New parameter panel 
 * 
 * @author	Luong Nguyen
 * @date	2013/10/17
 * @version 2.0
 * @version 3.0, 2013/12/09, Son Han
 * 
 */
@SuppressWarnings("serial")
public class NewParameterPanel extends JPanel implements PanelInterface, ActionListener{	
	
	//Button
    public static final String BROWSER_IMAGE = "Browser";
	private JButton browserButton = new JButton(BROWSER_IMAGE);
	
	public JTextField paramVal = new JTextField();
	public JTextField reponseMsg = new JTextField();
	
	private JLabel iconPreview = new JLabel();
	public String iconURL = SwingUtilities.DEFAULT_IMAGE_TEXT;
// Son Han: To use the DPWSim tool of showing error message: SwingUtilities.showErrorMessage()
	// public JTextArea errorMsg;
	
	public NewParameterPanel() {
		this.setBorder(BorderFactory.createEmptyBorder(10,10,0,10));
		GridBagLayout gridbag = new GridBagLayout();
		GridBagConstraints c = new GridBagConstraints();
		this.setLayout(gridbag);
		
		reset();
		
		browserButton.addActionListener(this);
		
		iconPreview.setBorder(BorderFactory.createEtchedBorder());
		iconPreview.setPreferredSize(new Dimension(50, 50));
		iconPreview.setHorizontalAlignment(JLabel.CENTER);
		
		c.fill = GridBagConstraints.BOTH;
		c.insets = new Insets(1, 1, 1, 1);		
		
		c.gridy = 0;
		c.weightx = 0;		this.add(new JLabel("Request Value"), c);
		
		c.gridwidth = 2;
		c.weightx = 1;		this.add(paramVal, c);
		
		c.gridy++;		
		
		c.gridwidth = 1;
		c.weightx = 0;		this.add(new JLabel("Response Message"), c);
		
		c.gridwidth = 2;
		c.weightx = 1;		this.add(reponseMsg, c);
		
		c.gridy++;
		
		c.gridwidth = 1;
		c.weightx = 0;		this.add(new JLabel("Status Image"), c);
		
		c.gridheight = 2;
		c.weightx = 1;		this.add(iconPreview, c);
		
		c.gridheight = 1;
		c.weightx = 0;		this.add(browserButton, c);
		
// Son Han: To use the DPWSim tool of showing error message: SwingUtilities.showErrorMessage()		
		//Row 5: error message
		//c.gridy = 6;
		//c.fill = 1;
		//this.add(errorMsg, c);
		
		// Set focus to the param value text field
		paramVal.addAncestorListener( new RequestFocusListener() );
	}
	
	public void reset(){
		this.paramVal.setText("");
		this.reponseMsg.setText("");
		this.iconPreview.setIcon(SwingUtilities.createDefaultDeviceIcon(24));
	}


	@Override
	public boolean invariant() {
		boolean ok=true;
		String errorMsg = "Error: \n";
		if(this.paramVal.getText().equals("")){
			ok=false;
			errorMsg+="The name field is empty!\n";
		}
		if(this.reponseMsg.getText().equals("")){
			ok=false;
			errorMsg +="The parameter filed is empty!\n";
		}
		if(!ok){
			// this.errorMsg.setText(errorMsg);
			// Son Han: To use the DPWSim tool of showing error message: SwingUtilities.showErrorMessage()
			SwingUtilities.showErrorMessage(this, errorMsg);
		}
		return ok;
	}


	@Override
	public void actionPerformed(ActionEvent e) {
		JFileChooser fc = new JFileChooser(".");
		File file = null;
		
		int returnVal = fc.showOpenDialog(null);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			file = fc.getSelectedFile();
			iconURL = file.getAbsolutePath();
			iconPreview.setIcon(SwingUtilities.createImageIcon(iconURL, 24));
		}
	}

	public JTextField getParamVal() {
		return paramVal;
	}

	public void setParamVal(JTextField paramVal) {
		this.paramVal = paramVal;
	}

	public JTextField getReponseMsg() {
		return reponseMsg;
	}

	public void setReponseMsg(JTextField reponseMsg) {
		this.reponseMsg = reponseMsg;
	}

	public String getIconURL() {
		return iconURL;
	}

	public void setIconURL(String iconURL) {
		this.iconURL = iconURL;
	}

	public JLabel getIconPreview() {
		return iconPreview;
	}

	public void setIconPreview(JLabel iconPreview) {
		this.iconPreview = iconPreview;
	}
	
}
