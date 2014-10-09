package eu.telecom.sudparis.dpwsim.upgrade;

import java.awt.GridBagConstraints;

import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import eu.telecom.sudparis.dpwsim.view.PanelInterface;
import eu.telecom.sudparis.dpwsim.view.tools.RequestFocusListener;
import eu.telecom.sudparis.dpwsim.view.tools.SwingUtilities;

/**
 * New Event panel
 * 
 * @author Luong Nguyen
 * @date 2013/10/17
 * @version 2.0
 */
@SuppressWarnings("serial")
public class NewEventPanel extends JPanel implements PanelInterface {

    public static final String CREATE_EVENT = "Add New Event";
	public JTextField eventName = new JTextField();
	public JTextField eventMessage = new JTextField();
	public JTextField eventFrequency = new JTextField();
// Son Han: To use the DPWSim tool of showing error message: SwingUtilities.showErrorMessage()	
	//public JTextArea errorMsg = new JTextArea();

	public NewEventPanel() {
		this.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));
		GridBagLayout gridbag = new GridBagLayout();
		GridBagConstraints c = new GridBagConstraints();
		this.setLayout(gridbag);


		c.fill = GridBagConstraints.BOTH;
		c.insets = new Insets(1, 1, 1, 1);

		c.gridy = 0;
		c.weightx = 0;
		this.add(new JLabel("Event Name"), c);

		c.weightx = 1;
		this.add(eventName, c);

		c.gridy++;
		c.weightx = 0;
		this.add(new JLabel("Event Message"), c);

		c.weightx = 1;
		this.add(eventMessage, c);

		c.gridy++;
		c.weightx = 0;
		this.add(new JLabel("Frequency (ms)"), c);

		c.weightx = 1;
		this.add(eventFrequency, c);

// Son Han: To use the DPWSim tool of showing error message: SwingUtilities.showErrorMessage()		
		
		// Set focus to the event name text field
		eventName.addAncestorListener(new RequestFocusListener());
	}

	public void reset() {
		this.eventName.setText("");
		this.eventFrequency.setText("");
		this.eventMessage.setText("");
	}

	public JTextField getEventName() {
		return eventName;
	}

	public void setEventName(JTextField eventName) {
		this.eventName = eventName;
	}

	public JTextField getEventMessage() {
		return eventMessage;
	}

	public void setEventMessage(JTextField eventMessage) {
		this.eventMessage = eventMessage;
	}

	public JTextField getEventFrequency() {
		return eventFrequency;
	}

	public void setEventFrequency(JTextField eventFrequency) {
		this.eventFrequency = eventFrequency;
	}

	@Override
	public boolean invariant() {
		boolean ok = true;
		String errorMsg = "Error:\n";
		if (this.eventName.getText().equals("")) {
			ok = false;
			errorMsg += "The name field is empty!\n";
		}
		if (this.eventMessage.getText().equals("")) {
			ok = false;
			errorMsg += "The message field is empty!\n";
		}
		if (!SwingUtilities.isInteger(this.eventFrequency.getText())) {
			ok = false;
			errorMsg += "The frequency is invalid!\n";
		}
		if (!ok) {
			// this.errorMsg.setText(errorMsg);
			// Son Han: To use the DPWSim tool of showing error message: SwingUtilities.showErrorMessage()
			SwingUtilities.showErrorMessage(this, errorMsg);
		}
		return ok;
	}

}
