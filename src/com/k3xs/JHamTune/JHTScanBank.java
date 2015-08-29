package com.k3xs.JHamTune;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.text.*;
import java.util.*;
/**
 * Scans a bank of channels represented as JHTBand objects.
 * @author Margaret Leber k3xs@arrl.net
 * @version 1.0
 */
public class JHTScanBank extends JFrame implements WindowListener

{   private JHTRadio radio;
    private JPanel top, stack;
    int threshold;
    JSlider      sqThreshold;
    JProgressBar sMeter;  
    public Thread st;
    public JHTScanControl [] channel;
 
     JHTScanBank(JHTRadio radio, JHTBand[] bands)
     {
     this.radio = radio;
     initializeFrame();
     channel= new JHTScanControl [bands.length];
     for (int i=0; i<bands.length; i++)
      {
       channel[i] = new JHTScanControl(this, bands[i]);     
       stack.add(channel[i]);
      }
     pack();
     scanStart();  
     } 

     void initializeFrame()
     {
     setTitle("JHamTune Scanner");
     threshold=16;
     st = new Thread();
     addWindowListener(this);
     getContentPane().add(top = new JPanel(),BorderLayout.NORTH);
     getContentPane().add(stack = new JPanel(),BorderLayout.CENTER);
     top.setLayout(new BoxLayout(top,BoxLayout.Y_AXIS));
     stack.setLayout(new GridLayout(0,1));
     top.add(new JLabel("S-meter squelch"));
     top.add(sqThreshold =  new JSlider(JSlider.HORIZONTAL,0,32,16));
     top.add(sMeter =  new JProgressBar(0, 32));
     pack();
     setVisible(true);   
    }

    public void tune(JHTBand b)
    {
    radio.setBand(b);
    }

    public void scanStart()
    {
    st = new scanThread(this);
    st.start();
    }

    public void scanStop()
    {
    st = null;
    }

    public void clearLocks()

    {   // reset any locked channels to active
         for (int i=0; i < channel.length ; i++)
           { 
             if (channel[i].state == JHTScanControl.LOCK) channel[i].active();
             if (channel[i].state == JHTScanControl.LOCKBUSY) channel[i].active();
           }
    }

	public String toMHz(int freq)
	{
		DecimalFormat fmt = new DecimalFormat("###.000 MHz");
		return(fmt.format((double)(freq)/10e5));
	}

  class scanThread extends Thread
    {
    private JHTScanBank j;

     scanThread(JHTScanBank j) { this.j = j; }
     
     public void run()  // main loop for scanning
     {
      Thread t = Thread.currentThread();

      while (t == j.st)
         {
         for (int i=0; i < j.channel.length; i++)
          {

          if ( !(t == j.st)) break;   // terminate thread?

          if (j.channel[i].state > 1) // only for active channels

             { 
	       j.channel[i].bevelDown();
               tune(j.channel[i].getBand());   // listen to the channel
               j.sMeter.setValue(j.radio.getSmeter());

               while (j.radio.getSmeter() > j.sqThreshold.getValue())
                  { 
                  // channel active and busy
                  j.channel[i].busy();         // mark it busy 
                  j.sMeter.setValue(j.radio.getSmeter());
                  if (!(t == j.st)) break;     // terminate thread?
                                               // leave it tuned for one second
                  try {Thread.sleep(1000);} 
                     catch(InterruptedException e) {};
                  if (!(t == j.st)) break;     // terminate thread?
                  } 

                // quiet channel 
                j.channel[i].quiet();          // channel now quiet
		j.channel[i].bevelUp();
                if (!(t == j.st)) break; // terminate thread?
             } 
          yield();
          }
         } 
      } 
    }
    /*
     * WindowListener methods
     */
    public void windowOpened(WindowEvent e) {}
    public void windowClosing(WindowEvent e) {
	scanStop();
    }
    public void windowClosed(WindowEvent e) {
    	scanStop();}
    public void windowIconified(WindowEvent e) {}
    public void windowDeiconified(WindowEvent e) {}
    public void windowActivated(WindowEvent e) {}
    public void windowDeactivated(WindowEvent e) {}
    public void windowStateChanged(WindowEvent e) {}
    public void windowGainedFocus(WindowEvent e) {}
    public void windowLostFocus(WindowEvent e) {}
}

