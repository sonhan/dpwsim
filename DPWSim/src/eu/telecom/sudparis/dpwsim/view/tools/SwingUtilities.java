package eu.telecom.sudparis.dpwsim.view.tools;

import java.awt.Component;

import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

import eu.telecom.sudparis.dpwsim.DPWSim;

/**
 * Swing Utilities 
 * 
 * @author	Son Han
 * @date	2013/09/20
 * @version 2.0
 */
public class SwingUtilities {
	
	public static String DEFAULT_IMAGE_TEXT = "Default image";
	
	/**
	 * Create an icon from an existing image file either in class path or from local file system.
	 * 
	 * @param path ((1) /res/icon.png, (2) local: E:\path\to\file, (3) invalid)
	 * @return	(1) if path exists in class, return ImageIcon
	 * 			(2) else return new ImageIcon(path) - always return a (not null) pointer
	 * 			(3) if in both cases, no image icon is created, return a null pointer
	 * 			 			
	 */
	public static ImageIcon createImageIcon(String path) {
        java.net.URL imgURL = SwingUtilities.class.getResource(path);
        ImageIcon icon;
        if (imgURL != null) {
            icon = new ImageIcon(imgURL);
        } else {
            icon = new ImageIcon(path); 
        }
        
        if (icon.getIconHeight() > 0) return icon; // if an image icon created
        else return null;
    }
	
	public static ImageIcon createImageIcon(String path, int h) {
    	ImageIcon icon = createImageIcon(path);
    	if (icon != null) icon.setImage(resizeToHeight(icon.getImage(), h));
        return icon;
    }
	
	public static void loadImage(JLabel holder, String path, int h){
		ImageIcon icon = createImageIcon(path, h);
		if (icon.getIconHeight() < 0) icon = createDefaultDeviceIcon(h);
		holder.setIcon(icon);
	}
	
	public static ImageIcon createDefaultDeviceIcon() {
		return createImageIcon("/res/device_default.png", 24);
	}
	
	public static ImageIcon createDefaultDeviceIcon(int h) {
		return createImageIcon("/res/device_default.png", h);
	}

	public static ImageIcon createAppIcon(){
		return createImageIcon("/res/favicon.png");
	}
	
	public static ImageIcon createDefaultLayout(){
		return createImageIcon("/res/default_layout.png");
	}
	
	public static ImageIcon defaultOperationStatus(){
		return createImageIcon("/res/devices/default_dev_status.jpg");
	}
	
	public static void showCreditMessage(Component frame){
		JOptionPane.showMessageDialog(frame, 
				"DPWSim version " + DPWSim.VERSION + "\n" +
				"Simulation Toolkit for DPWS \n" +
				"Institute Mines-Telecom, Telecom SudParis \n\n" +
				"Nguyen Van Luong, luongnv89@gmail.com \n" +
				"Son Han, vnhanson@gmail.com \n",
				"About" , 
				JOptionPane.INFORMATION_MESSAGE, 
				createAppIcon());
	}

	public static void showHelp(Component frame) {
		JOptionPane.showMessageDialog(frame, 
				"Simulation Toolkit for DPWS",						 
				"Help" , 
				JOptionPane.INFORMATION_MESSAGE, 
				createAppIcon());
		
	}
	
	public static boolean validateIPAddress(String ip){
		if (ip == null || ip.isEmpty()) return false;
	    ip = ip.trim();
	    if ((ip.length() < 6) & (ip.length() > 15)) return false;

	    try {
	        Pattern pattern = Pattern.compile("^(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$");
	        Matcher matcher = pattern.matcher(ip);
	        return matcher.matches();
	    } catch (PatternSyntaxException ex) {
	        return false;
	    }
	}
	
	public static void addGridBagComponent(Container container, 
			GridBagLayout gridbag,
			GridBagConstraints c,
			Component com,
			int gridx, int gridy, int gridwidth, int gridheight){
		c.gridx = gridx;
		c.gridy = gridy;
		c.gridwidth = gridwidth;
		c.gridheight = gridheight;
		gridbag.setConstraints(com, c); 
		container.add(com);
	}
	
	public static Image resizeToWidth(Image image, int w){
		int h = image.getHeight(null) * w / image.getWidth(null);
		Image newimg = image.getScaledInstance(w, h,  java.awt.Image.SCALE_SMOOTH); // scale it the smooth way  
		return newimg;
	}
	
	public static Image resizeToHeight(Image image, int h){
		int w = image.getWidth(null) * h / image.getHeight(null);
		Image newimg = image.getScaledInstance(w, h,  Image.SCALE_SMOOTH); // scale it the smooth way  
		return newimg;
	}
	public static void showErrorMessage(Component parent, Object errMsg){
		JOptionPane.showMessageDialog(parent, errMsg, "Error", JOptionPane.ERROR_MESSAGE);
	}
	public static void showInformationMessage(Component parent, String errMsg){
		JOptionPane.showMessageDialog(parent, errMsg, "Info", JOptionPane.INFORMATION_MESSAGE);
	}
	
	public static boolean isInteger(String s) {
	    try { 
	        Integer.parseInt(s); 
	    } catch(NumberFormatException e) { 
	        return false; 
	    }
	    // only got here if we didn't return false
	    return true;
	}
}
