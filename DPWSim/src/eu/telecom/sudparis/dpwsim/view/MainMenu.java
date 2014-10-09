package eu.telecom.sudparis.dpwsim.view;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.ImageIcon;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

import eu.telecom.sudparis.dpwsim.upgrade.MediatorComponent;
import eu.telecom.sudparis.dpwsim.view.tools.SwingUtilities;

/**
 * Main Menu 
 * 
 * @author	Son Han
 * @date	2013/09/20
 * @version 2.0
 */
@SuppressWarnings("serial")
public class MainMenu extends JMenuBar {
	
	public final static String MENU_NEW = "New";
	public final static String MENU_NEW_SPACE = "Space";
	public final static String MENU_NEW_STANDALONE_DEVICE = "Standalone Device";
	public final static String MENU_OPEN = "Open";
	public final static String MENU_SAVE = "Save";
	public final static String MENU_CLEAR = "Remove All";
	public final static String MENU_START_ALL = "Start All";
	public final static String MENU_STOP_ALL = "Stop All";
	public final static String MENU_EXIT = "Quit";
	public final static String MENU_HELP = "Help Contents";
	public final static String MENU_ABOUT = "About";
	
	public final static String MENU_ADD_DEVICE = "Add New";
	public final static String MENU_ADD_FILE = "Add From File";
	public static final String MENU_ADD_PHYSICAL = "Generate from Physical Device";
	public final static String MENU_ADD_PREDEFINED = "Add Predefined";
	public final static String MENU_ADD_LIGHTBULB = "Light Bulb";
	public final static String MENU_ADD_COFFEE_MAKER = "Coffee Maker";
	
	private JMenu fileMenu = new JMenu("File");
	private JMenu deviceMenu = new JMenu("Device");
	private JMenu helpMenu = new JMenu("Help");
	
	public MainMenu(ActionListener actionListener){
		MediatorComponent.getInstance().setMainMenu(this);
		
		JMenu subMenu;
		JMenuItem menuItem;
		ImageIcon icon;
		
/* File Menu: New, Open, Save, Clear Devices, |, Quit */
		fileMenu = new JMenu("File");

// New submenu: New Space, New Device		
		icon = SwingUtilities.createImageIcon("/res/icon_new.png", 24);
		subMenu = new JMenu(MENU_NEW);
		subMenu.setIcon(icon);
		
		
		menuItem = new JMenuItem(MENU_NEW_SPACE);
		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, ActionEvent.CTRL_MASK));
		menuItem.addActionListener(actionListener);
		subMenu.add(menuItem);
		
		menuItem = new JMenuItem(MENU_NEW_STANDALONE_DEVICE);
		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D, ActionEvent.CTRL_MASK));
		menuItem.addActionListener(actionListener);
		subMenu.add(menuItem);
		
		fileMenu.add(subMenu);

// Open menu item		
		icon = SwingUtilities.createImageIcon("/res/icon_open.png", 24);
		menuItem = new JMenuItem(MENU_OPEN, icon);
		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, ActionEvent.CTRL_MASK));
		menuItem.addActionListener(actionListener);
		fileMenu.add(menuItem);

// Save menu item
		icon = SwingUtilities.createImageIcon("/res/icon_save.png", 24);
		menuItem = new JMenuItem(MENU_SAVE, icon);
		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK));
		menuItem.addActionListener(actionListener);
		fileMenu.add(menuItem);
		
		fileMenu.addSeparator();

// Quit menu item		
		icon = SwingUtilities.createImageIcon("/res/icon_exit.png", 24);
		menuItem = new JMenuItem(MENU_EXIT, icon);
		menuItem.addActionListener(actionListener);
		fileMenu.add(menuItem);
		
		this.add(fileMenu);
		
/* Device menu: Add New, Add Predefined: */
		
// Add new device
		icon = SwingUtilities.createImageIcon("/res/icon_add.png", 24);
		menuItem = new JMenuItem(MENU_ADD_DEVICE, icon);
		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, ActionEvent.CTRL_MASK));
		menuItem.addActionListener(actionListener);
		deviceMenu.add(menuItem);
		

// Add predefined device
		
		subMenu = new JMenu(MENU_ADD_PREDEFINED);
		
		icon = SwingUtilities.createImageIcon("/res/icon_light_bulb.png", 24);
		menuItem = new JMenuItem(MENU_ADD_LIGHTBULB, icon);
		menuItem.addActionListener(actionListener);
		subMenu.add(menuItem);
		
		icon = SwingUtilities.createImageIcon("/res/icon_coffee_maker.png", 24);
		menuItem = new JMenuItem(MENU_ADD_COFFEE_MAKER, icon);
		menuItem.addActionListener(actionListener);
		subMenu.add(menuItem);
		
		deviceMenu.add(subMenu);

// Add from file
		icon = SwingUtilities.createImageIcon("/res/icon_open.png", 24);
		menuItem = new JMenuItem(MENU_ADD_FILE, icon);
		menuItem.addActionListener(actionListener);
		deviceMenu.add(menuItem);

// Generate from physical device
		icon = SwingUtilities.createImageIcon("/res/icon_physical.png", 24);
		menuItem = new JMenuItem(MENU_ADD_PHYSICAL, icon);
		menuItem.addActionListener(actionListener);
		deviceMenu.add(menuItem);

		deviceMenu.addSeparator();
		
// Clear devices menu item		
		icon = SwingUtilities.createImageIcon("/res/icon_clear.png", 24);
		menuItem = new JMenuItem(MENU_CLEAR, icon);
		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, ActionEvent.CTRL_MASK));
		menuItem.addActionListener(actionListener);
		deviceMenu.add(menuItem);
		
		icon = SwingUtilities.createImageIcon("/res/icon_start_all.png", 24);
		menuItem = new JMenuItem(MENU_START_ALL, icon);
		//menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, ActionEvent.CTRL_MASK));
		menuItem.addActionListener(actionListener);
		deviceMenu.add(menuItem);
		
		icon = SwingUtilities.createImageIcon("/res/icon_stop_all.png", 24);
		menuItem = new JMenuItem(MENU_STOP_ALL, icon);
		//menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, ActionEvent.CTRL_MASK));
		menuItem.addActionListener(actionListener);
		deviceMenu.add(menuItem);
		
		this.add(deviceMenu);

		
/* Help menu: Help contents, About */
		
		icon = SwingUtilities.createImageIcon("/res/icon_help.png", 24);
		menuItem = new JMenuItem(MENU_HELP, icon);
		menuItem.addActionListener(actionListener);
		helpMenu.add(menuItem);
		
		icon = SwingUtilities.createImageIcon("/res/icon_about.png", 24);
		menuItem = new JMenuItem(MENU_ABOUT);
		menuItem.addActionListener(actionListener);
		helpMenu.add(menuItem);
		
		this.add(helpMenu);
	}
	
	public void setMode(int status){
		if (status == DPWSimMainWindow.MODE_START){
			fileMenu.getItem(0).setEnabled(true);
			fileMenu.getItem(1).setEnabled(true);
			fileMenu.getItem(2).setEnabled(false);
			fileMenu.getItem(3).setEnabled(true);
			
			deviceMenu.setEnabled(false);
		} else if (status == DPWSimMainWindow.MODE_SPACE_NEW){
			fileMenu.getItem(0).setEnabled(true);
			fileMenu.getItem(1).setEnabled(true);
			fileMenu.getItem(2).setEnabled(false);
			fileMenu.getItem(4).setEnabled(true);
			
			deviceMenu.setEnabled(false);
			
		} else if (status == DPWSimMainWindow.MODE_DEVICE_CREATED){
			fileMenu.getItem(2).setEnabled(true);
			deviceMenu.setEnabled(false);
		} else if (status == DPWSimMainWindow.MODE_SPACE_CREATED){
			fileMenu.getItem(2).setEnabled(true);
			deviceMenu.setEnabled(true);
		}
		
	}
}
