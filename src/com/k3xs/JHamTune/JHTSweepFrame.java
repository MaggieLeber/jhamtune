package com.k3xs.JHamTune;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.text.*;
/**
 * Implements a graphic representation of a band segment,
 * showing a histogram of signal strengths at various frequencies.
 * Clicking on the histogram tunes the associated radio to that frequenecy.
 * @author Margaret Leber k3xs@arrl.net
 * @version 1.0
 */
public class JHTSweepFrame extends JFrame
{
	private String name = "JHTSweepFrame";
	private JHTRadio myRadio;
	private JPanel   freqPanel;
	private JHTSweepPanel   sweepPanel;
	private JButton  sweepButton;
	private int lo;
	private int hi;
    private JHTMode mode;
	public JHTSweepFrame(JHTRadio radio, JHTBand band)

	{
        setTitle(band.getName());
		myRadio = radio;
		myRadio.setBand(band);
		lo = band.getLowFreq();
		hi = band.getHighFreq();
        mode = band.getMode(); 
        makePanel();

	}

	public JHTSweepFrame(JHTRadio radio, int freq, JHTMode m, int low, int high)
	{
        setTitle("Tuner");
		myRadio = radio;
		hi = high;
		lo = low;
        mode = m;
		myRadio.setFreq(freq);
		myRadio.setMode(mode);
        makePanel();
    }

    private void makePanel()
        {
		getContentPane().add(freqPanel= new JPanel(), BorderLayout.NORTH);
		getContentPane().add(sweepPanel= new JHTSweepPanel(lo, hi, myRadio), 
                      BorderLayout.CENTER);
		freqPanel.add(new JLabel(toMHz(lo)+"--"+toMHz(hi)+mode.getName()),
		              BorderLayout.NORTH);
		sweepButton=new JButton("sweep");
		sweepButton.setActionCommand("sweep");
		sweepButton.addActionListener(new JHTListener());
		freqPanel.add(sweepButton, BorderLayout.NORTH);
		sweepPanel.setPreferredSize(new Dimension(500,150));
		pack();
		setVisible(true);
	    }

	public void sweep() {sweepPanel.sweep();}

	public String toMHz(int freq)
	{
		DecimalFormat fmt = new DecimalFormat("###.00#### MHz");
		return(fmt.format((double)(freq)/10e5));
	}

	class JHTListener implements ActionListener
	{
		public void actionPerformed(ActionEvent e)
		{
			if (e.getActionCommand().equals("sweep")) sweep();
		}
	}

}
