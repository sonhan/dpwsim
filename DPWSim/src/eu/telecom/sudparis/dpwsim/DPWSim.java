package eu.telecom.sudparis.dpwsim;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.ws4d.java.DPWSFramework;

import eu.telecom.sudparis.dpwsim.handler.ActionHandler;
import eu.telecom.sudparis.dpwsim.handler.DeviceControlHandler;
import eu.telecom.sudparis.dpwsim.handler.MouseHandler;
import eu.telecom.sudparis.dpwsim.upgrade.MediatorComponent;
import eu.telecom.sudparis.dpwsim.view.DeviceControlPanel;
import eu.telecom.sudparis.dpwsim.view.DeviceDialog;
import eu.telecom.sudparis.dpwsim.view.DPWSimMainWindow;
import eu.telecom.sudparis.dpwsim.view.MainMenu;
import eu.telecom.sudparis.dpwsim.view.NewDevicePanel;
import eu.telecom.sudparis.dpwsim.view.NewSpacePanel;

public class DPWSim {

    /**
     * DPWSim Main
     * 
     * @author Son Han
     * @date 2013/09/20
     * @version 2.0
     */

    public static final String VERSION = "3.0.0.0";
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(
                    UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                ActionHandler actionHandler = new ActionHandler();
                DPWSimMainWindow window = new DPWSimMainWindow();
                MouseHandler mouseHandler = new MouseHandler(window);

                DeviceDialog dialog = new DeviceDialog(window, true);
                //dialog.setSize(600, 300);
                dialog.setLocationRelativeTo(window);

                NewDevicePanel devicePanel = new NewDevicePanel(actionHandler);
                NewSpacePanel spacePanel = new NewSpacePanel();

                MainMenu mainMenu = new MainMenu(actionHandler);

                DeviceControlPanel controlPanel = new DeviceControlPanel(new DeviceControlHandler());

                window.addMouseListener(mouseHandler);
                window.addMouseMotionListener(mouseHandler);
                window.setJMenuBar(mainMenu);

                /* Main window as new space */
                window.setContentPane(spacePanel); 
                MediatorComponent.getInstance().setMode(DPWSimMainWindow.MODE_SPACE_NEW);

                window.setTitle(DPWSimMainWindow.DPWSIM + " - New Space");

                /* Default setting and load */
                window.setDefaultSize();
                window.setVisible(true);
                window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                window.setLocationRelativeTo(null);

                dialog.setContentPane(devicePanel);
                dialog.setTitle("New Device");

                actionHandler.validate();

                /* DPWS Framework start */	    
                DPWSFramework.start(null);
            }
        });
    }

}
