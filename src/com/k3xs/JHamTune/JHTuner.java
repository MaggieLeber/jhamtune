package com.k3xs.JHamTune;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.text.*;
/**
 * GUI for tuner.
 * @author Margaret Leber k3xs@arrl.net
 * @version 1.0
 */
public class JHTuner extends JFrame implements ActionListener, Runnable
{
	private String name;
	private JHTRadio r;
	private JPanel   freqPanel;
	private JLabel   fLabel;
	private JComboBox modeBox;
	private JButton  sweepButton;
	private JHTMode mode;
	private JProgressBar sMeter;
            Thread thread;


	public  JHTuner(JHTRadio r)
	{
        Container p = getContentPane();
		this.r = r;
        setTitle(r.getName());
        fLabel =  new JLabel(toMHz(r.getFreq()));
        fLabel.setFont(fLabel.getFont().deriveFont(24f));
        p.add(fLabel);
        modeBox = JHTMode.getComboBox(r.getMode());          
        p.add(modeBox);
        p.setLayout(new BoxLayout(p,BoxLayout.X_AXIS));
        p.add(sMeter =  new JProgressBar(0, 32));
        p.setSize(new Dimension(50,200));
	    pack();
	    setVisible(true);   

	}

    public void update()
    {
    sMeter.setValue(r.getSmeter());
    fLabel.setText(toMHz(r.getFreq()));
    modeBox.setSelectedItem(r.getMode());
    }

	public String toMHz(int freq)
	{
		DecimalFormat fmt = new DecimalFormat("###.000000 MHz");
		return(fmt.format((double)(freq)/10e5));
	}

 

        public void start() 
        {
            thread = new Thread(this);
            thread.setName("Tuner updater");
            thread.start();
        }

        public void stop() 
        {
            thread = null;
        }
        
 

    public void run() 
    {
     while (thread != null)
         {
         update();
         try {Thread.sleep(500);} catch(InterruptedException e) {};
         }
    }


	public void actionPerformed(ActionEvent e)
	{			
	}


}

