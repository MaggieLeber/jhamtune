package com.k3xs.JHamTune;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
import java.text.*;
import java.util.*;
/**
 * Button widgets for the JHTScanBank frame. 
 * One click toggles between enabling and disabling this channel for scan. 
 * A double-click halts scanning and locks this channel on for receive. 
 * The label on an busy scanned channel turns bright red; each subsequent scan that 
 * finds the channel quiet causes the label to darken.
 * @author Margaret Leber k3xs@arrl.net
 * @version 1.0
 **/
class JHTScanControl extends JButton 
{
    JHTScanBank sb;
    public JHTBand b;
    
    // state constants
    public static final int INACTIVE = 1;
    public static final int ACTIVE = 2;
    public static final int BUSY = 3;
    public static final int LOCK = 4;
    public static final int LOCKBUSY = 5;
    private final Border raisedbevel = BorderFactory.createRaisedBevelBorder();
    private final Border loweredbevel = BorderFactory.createLoweredBevelBorder();

    public int state = ACTIVE; 

	public JHTScanControl(JHTScanBank sb, JHTBand b)

	{
    this.b = b;
    this.sb = sb;
    setText(b.getName());
    setBorder(loweredbevel);
    addMouseListener(new scListener());
    setPreferredSize(new Dimension(200, 30));
	}

    public JHTBand getBand()
    {
    return(b);
    } 
    
    public void inactive()
    {
    sb.scanStop();
    state = INACTIVE;
    setForeground(Color.lightGray);
    setBackground(Color.gray);
    sb.scanStart();
    }
    
    public void active()
    {
    state = ACTIVE;
    setForeground(Color.black);
    setBackground(Color.lightGray);
    }
    
    public void busy()
    {
    state = BUSY;
    setForeground(Color.red);
    setBackground(Color.lightGray);
    }
    
    public void lockOn()
    {
    state = LOCK;
    sb.clearLocks();
    sb.scanStop();
    sb.tune(b);
    setForeground(Color.black);
    setBackground(Color.white);
    }
    
    public void lockOff()
    {
    sb.clearLocks();
    active();
    sb.scanStart();
    }

    public void lockbusy()
    {
    state = LOCKBUSY;
    setForeground(Color.red);
    setBackground(Color.white);
    }
    
    public void click()

    { 
     switch (state)
     {
     case INACTIVE:	active();                 break;
     case ACTIVE:	inactive();               break;
     case BUSY:		inactive();               break;
     case LOCK:		active(); sb.scanStart(); break;
     case LOCKBUSY:	active(); sb.scanStart(); break;
     }
    }  

    public void dblclick()
    { 
     lockOn();
    }

    public void quiet() 
     {
       setForeground(getForeground().darker());
     }
     
     public void bevelUp() 
     {
       setBorder(raisedbevel);
     }

     public void bevelDown() 
     {
       setBorder(loweredbevel);
     }
      

	class scListener implements MouseListener
	{
		public void mouseClicked(MouseEvent e)
		{
        if (e.getClickCount() == 1) ((JHTScanControl)e.getComponent()).click();
        if (e.getClickCount() == 2) ((JHTScanControl)e.getComponent()).dblclick();
		}
 
        public void mousePressed(MouseEvent e)  {}
		public void mouseReleased(MouseEvent e) {}
		public void mouseEntered(MouseEvent e)	{}
		public void mouseExited(MouseEvent e)	{}
    }
}
