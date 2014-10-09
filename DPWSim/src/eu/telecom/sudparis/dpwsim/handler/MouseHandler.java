package eu.telecom.sudparis.dpwsim.handler;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import eu.telecom.sudparis.dpwsim.view.DPWSimMainWindow;

/**
 * Event handler for mouse move and mouse click for main window. 
 * 
 * @author	Son Han
 * @date	2013/09/20
 * @version 2.0
 */
public class MouseHandler implements MouseListener, MouseMotionListener{
    
	private DPWSimMainWindow owner;
	public MouseHandler(DPWSimMainWindow owner){
		this.owner = owner;
	}
	

	@Override
	public void mouseMoved(MouseEvent e) {
		if (owner.getMode() == DPWSimMainWindow.MODE_ADDING_DEVICE){
			//int n = owner.getDevicesStatus().size();
			//owner.getDevicesStatus().get(n-1).setLocation(e.getX(), e.getY());
			
			//Valid the position
			int x = e.getX()- owner.activeDevice.getWidth();
			int y = e.getY()-3*owner.activeDevice.getHeight();
			//Check under Zero
			if(x< 0) x = 0;
			if(y< 0) y = 0;
			
			//TODO: Check out of range
			
			//System.out.println(x+"/"+y);
			owner.activeDevice.setLocation(x,y);
			
		}
//		System.out.println(owner.getMode() + " [" + e.getX() + e.getY() + "]");
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		if (owner.getMode() == DPWSimMainWindow.MODE_ADDING_DEVICE){
			int x = e.getX()- owner.activeDevice.getWidth();
			int y = e.getY()-3*owner.activeDevice.getHeight();
			owner.activeDevice.addInfo("\n" + x + "\n" + y);
			owner.setMode(DPWSimMainWindow.MODE_SPACE_CREATED);
		}
	}
	
	@Override
	public void mouseDragged(MouseEvent e) {}
	@Override
	public void mousePressed(MouseEvent e) {}
	@Override
	public void mouseReleased(MouseEvent e) {}
	@Override
	public void mouseEntered(MouseEvent e) {}
	@Override
	public void mouseExited(MouseEvent e) {}

}
