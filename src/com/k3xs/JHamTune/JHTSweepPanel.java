package com.k3xs.JHamTune;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.text.*;
/**
 * GUI for band-sweeping.<p>
 * Displays a histogram of 300 s-meter readings
 * taken betwen the limits of a JHTBand. Mouse clicks on the panel tune the radio
 * to the associated frequency
 *
 * @author     Margaret Leber k3xs@arrl.net
 * @created    January 9, 2004
 * @version    1.0
 */
public class JHTSweepPanel extends JPanel {
	int lo;
	// low band limit
	int hi;
	// hi band limit
	int width;
	int height;
	//panel dims at last repaint
	JHTRadio myRadio;
	int slices = 300;
	int fdelta;
	// each sweep slice width in Hz
	int[] readings = new int[slices];
	// S meter readings from last sweep
	int clickFreq;
	//last frequency clicked on
	String tip;


	/**
	 *Constructor for the JHTSweepPanel object
	 *
	 * @param  low    Description of the Parameter
	 * @param  high   Description of the Parameter
	 * @param  radio  Description of the Parameter
	 */
	public JHTSweepPanel(int low, int high, JHTRadio radio) {
		myRadio = radio;
		hi = high;
		lo = low;
		fdelta = (hi - lo) / slices;
		addMouseListener(new ClickTuneListener());
		setBackground(Color.black);
	}

	public void clickTune(int x) {
		// clickFreq = lo + ((int) ((hi - lo) * ((float) x / width)));
		clickFreq = lo + ((int) ((hi - lo) * ((float) x / (float)width)));
		myRadio.setFreq(clickFreq);
		repaint();
	}

	public void sweep() {
		int saveFreq = myRadio.getFreq();
		int width = getWidth();
		int height = getHeight();
		float xdelta = ((float) width / (float) slices);
		for (int slice = 1; slice < slices; slice++) {
			myRadio.setFreq(lo + fdelta * slice);
		//      try {Thread.sleep(50);} catch(InterruptedException e) {};
			readings[slice] = myRadio.getSmeter();
			paintImmediately((int) (slice * xdelta - 10), 0, 20, height);
		}
		myRadio.setFreq(saveFreq);
		clickFreq = saveFreq;
		repaint();
	}

	public void paintComponent(Graphics g) {
		Graphics2D g2 = (Graphics2D) g;
		super.paintComponent(g2);
		width = getWidth();
		height = getHeight();
		float xdelta = ((float) width / (float) slices);
		float ydelta = (float) height / (float) 32;
		int x = 0;
		for (int slice = 1; slice < slices; slice++) {
			x = (int) (slice * xdelta);
			g2.setColor(Color.green);
			g2.drawLine(x, height, x, height - (int) (readings[slice] * ydelta));
			g2.setColor(Color.blue);
			g2.drawRect(x, height, (int) xdelta, height - (int) (readings[slice] * ydelta));
		}
		g.setColor(Color.red);
		x = (int) (((float) width) * ((float) (clickFreq - lo) / (float) (hi - lo)));
		g.drawRect(x, 0, 3, height);
		g.drawString(toMHz(clickFreq), x + 5, 10);
	}

	public String toMHz(int freq) {
		DecimalFormat fmt = new DecimalFormat("##0.0##### MHz");
		return (fmt.format(((double) (freq)) / 10e5));
	}

	public class ClickTuneListener implements MouseListener {
		public void mousePressed(MouseEvent e) { }
		public void mouseReleased(MouseEvent e) { }
		public void mouseEntered(MouseEvent e) { }
		public void mouseExited(MouseEvent e) { }

		public void mouseClicked(MouseEvent e) {
			((JHTSweepPanel) e.getComponent()).clickTune(e.getX());
		}
	}

}

