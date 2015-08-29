package com.k3xs.JHamTune;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.table.*;
import javax.help.*;
import java.text.*;
import java.util.*;
import java.util.logging.*;
import java.io.*;
import java.net.*;

/**
 * Top frame of JHamTune:
 * contains menus, manages frequency data, builds other components.
 * <img src="JHamTune.jpg">
 *
 * @author     Margaret Leber k3xs@arrl.net
 * @created    in the mists of history
 * @version    1.0
 * @todo       "Save" menu
 */

public class JHamTune extends JFrame implements ActionListener, WindowListener {
	JMenuBar menuBar;
	ClassLoader cl = ClassLoader.getSystemClassLoader();
	SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss d MMM yyyy");
	HelpSet hs;
	HelpBroker hb;
	ArrayList data; // current frequncy data as JHTBand objects
	String port;
	JHTRadio radio;
	JTable table;
	JHTuner tuner;
	JHTBandTable tableModel;
	DefaultCellEditor modeEditor, CTCSSEditor;
	protected static Logger logger = Logger.getLogger("com.k3xs.JHamTune");
	protected static Properties props;

	/**
	 * Allows running from the command line.
	 *
	 * @param  args  The command line arguments
	 * @todo         this needs to parse out a driver and driver parms
	 */
	public static void main(String[] args) {
		if (args.length == 1) {
			new JHamTune(args[0]);
		} else {
			JHamTune.logger.severe("JHamTune: Command line driver argument required");
		}
	}


	/**
	 * Currently this method runs a YaseuFT847 driver
	 * needs to be hooked up with properties and configuration dialog 
	 * @param  p  Serial port name
	 */
	public JHamTune(String p) {
		addWindowListener(this);
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		ImageIcon splashImg = new ImageIcon(cl.getResource("help/JHTLogo256.jpg"));
		JLabel splashLabel = new JLabel(splashImg);
		JFrame splashFrame = new JFrame();
		splashFrame.getContentPane().add(splashLabel);
		splashFrame.pack();
		splashFrame.setVisible(true);
		// create and load default properties
		Properties defaultProps = new Properties();
		try {
			InputStream in = cl.getResourceAsStream("JHamTune.properties");
			if (in != null) {
				defaultProps.load(in);
				in.close();
			} else {
				logger.warning("JHamTune: no default proprties found");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		// create program properties with default
		props = new Properties(defaultProps);

		// load properties from last invocation
		try {
			InputStream in = new FileInputStream(".JHamTune/JHamTune.properties");
			props.load(in);
			in.close();
		} catch (IOException e) {
			logger.warning("JHamTune: no old properties file found");
		}

		radio = new YaesuFT847(p);
		setTitle("JHamTune: " + radio.getName());
		tuner = new JHTuner(radio);
		tuner.start();
		splashFrame.toFront();

		menuBar = new JMenuBar();
		setJMenuBar(menuBar);

		JMenu fileMenu = new JMenu("File");
		menuAdd(fileMenu, new JMenuItem("Open..."));
		menuAdd(fileMenu, new JMenuItem("Get Bands from Radio"));
		menuAdd(fileMenu, new JMenuItem("Save List as..."));
		menuAdd(fileMenu, new JMenuItem("Exit"));
		menuBar.add(fileMenu);

		JMenu editMenu = new JMenu("Edit");
		menuAdd(editMenu, new JMenuItem("Insert"));
		menuAdd(editMenu, new JMenuItem("Delete"));
		menuAdd(editMenu, new JMenuItem("Snapshot"));
		menuAdd(editMenu, new JMenuItem("Select all"));
		menuBar.add(editMenu);

		JMenu tuneMenu = new JMenu("Tune");
		menuAdd(tuneMenu, new JMenuItem("Sweep Selected"));
		menuAdd(tuneMenu, new JMenuItem("Scan Selected"));
		menuAdd(tuneMenu, new JMenuItem("Tune Selected"));
		menuBar.add(tuneMenu);

		JMenu confMenu = new JMenu("Configure");
		String[] driverClassnames =
			props.getProperty("JHamTune.driver.classes").split(",");
		JMenuItem mi;
		for (int i = 0; i < driverClassnames.length; i++) {
			confMenu.add(mi = new JMenuItem(driverClassnames[i]));
			mi.addActionListener(this);
		}
		
		menuAdd(confMenu, new JMenuItem("General Options"));
		menuBar.add(confMenu);

		menuBar.add(Box.createHorizontalGlue());

		JMenu helpMenu = new JMenu("Help");
		menuAdd(helpMenu, new JMenuItem("About"));
		menuBar.add(helpMenu);

		// set up JavaHelp environment, add it to help menu
		String helpHS = "help/jhelpset.hs";
		try {
			URL hsURL = cl.getResource(helpHS);
			hs = new HelpSet(null, hsURL);
			hb = hs.createHelpBroker();
			JMenuItem helpLaunch = new JMenuItem("User Manual");
			helpMenu.add(helpLaunch);
			helpLaunch.addActionListener(new CSH.DisplayHelpFromSource(hb));
		} catch (Throwable e) {
			logger.warning("Error during JavaHelp initialization");
			e.printStackTrace();
			logger.warning("User manual will not be available, continuing");
		}
		testData();

		getContentPane().removeAll();
		table = new JTable(tableModel = new JHTBandTable(data));

		modeEditor = new DefaultCellEditor(JHTMode.getComboBox(JHTMode.AM));
		table.setDefaultEditor(JHTMode.class, modeEditor);

		CTCSSEditor = new DefaultCellEditor(JHTCTCSS.getComboBox(JHTCTCSS.none));
		table.setDefaultEditor(JHTCTCSS.class, CTCSSEditor);

		JScrollPane scrollPane = new JScrollPane(table);
		getContentPane().add(scrollPane);
		table.getColumnModel().getColumn(0).setPreferredWidth(400);
		table.getColumnModel().getColumn(1).setPreferredWidth(150);
		table.getColumnModel().getColumn(2).setPreferredWidth(150);
		table.getColumnModel().getColumn(3).setPreferredWidth(150);
		table.getColumnModel().getColumn(4).setPreferredWidth(150);
		pack();
		setVisible(true);
		splashFrame.setVisible(false);
		splashFrame = null;
	}
	
	private void menuAdd(JMenu menu, JMenuItem item){
		menu.add(item);
		item.addActionListener(this);
	}

	/**
	 *  Sets the radio attribute of the JHamTune object
	 *
	 * @param  r  The new radio value
	 */
	public void setRadio(JHTRadio r) {
		radio = r;
		setTitle("JHamTune: " + radio.getName());
		tuner = new JHTuner(radio);
		tuner.start();
	}

	public void actionPerformed(ActionEvent e) {
		String cmd = e.getActionCommand();

		if (cmd.equals("Open...")) {
			JFileChooser fc = new JFileChooser();
			int returnVal = fc.showOpenDialog(new JFrame("open file"));
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				tableModel.addAll(fc.getSelectedFile());
			}
			pack();
			repaint();
		}

		if (cmd.equals("Save List as...")) {
			JFileChooser fc = new JFileChooser();
			JCheckBox xml = new JCheckBox("Export in XML format");
			fc.setAccessory(xml);
			int returnVal = fc.showOpenDialog(new JFrame("Save as file"));
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				if (xml.getModel().isSelected()) {
				tableModel.XMLSave(fc.getSelectedFile()); }
				else {
				tableModel.fileSave(fc.getSelectedFile());
				}
			}
		}
		
		if (cmd.equals("Get Bands from Radio")) {
			tableModel.addAll(radio.getRxBands());
			tableModel.addAll(radio.getRxTxBands());
		}

		if (cmd.equals("Insert")) {
			JHTBand untitledBand = 
			     new JHTBand(
			          "Untitled", 
				   0, 0, 0, 0, 
				   JHTMode.AM, 
				   JHTCTCSS.none, 
				   false
				   );
			if (table.getSelectedRow() == -1) {
				tableModel.add(untitledBand);
			} else {
				tableModel.add(table.getSelectedRow(),
						untitledBand
						);
			}

		}

		if (cmd.equals("Delete")) {
			if (table.getSelectedRowCount() > 0) {
				int[] ia = table.getSelectedRows();
				Arrays.sort(ia);
				for (int i = ia.length - 1; i >= 0; i--) {
					tableModel.remove(ia[i]);
				}
			}
		}
		
		if (cmd.equals("Select all")) {
			table.selectAll();
		}
		
		if (cmd.equals("Snapshot")) {
		JHTBand snapshot =
		    new JHTBand(
			    sdf.format(new Date(),
				       new StringBuffer(),
			               new FieldPosition(0)
				       ).toString(), 
			    radio.getFreq(), 
			    0, 0, 0, 
			    radio.getMode(), 
			    null, 
			    false
		    		);

		if (table.getSelectedRow() == -1) {
			tableModel.add(snapshot);
		} else {
			tableModel.add(table.getSelectedRow(),
			snapshot);
			}

		}

		if (cmd.equals("Sweep Selected")) {
			if (table.getSelectedRowCount() > 0) {
				JHTSweepFrame sweeper =
				new JHTSweepFrame(radio, tableModel.get(table.getSelectedRow()));
			}
		}

		if (cmd.equals("Scan Selected")) {
			if (table.getSelectedRowCount() > 0) {
				int[] ia = table.getSelectedRows();
				JHTBand[] ba = new JHTBand[table.getSelectedRowCount()];

				for (int i = 0; i < ia.length; i++) {
					ba[i] = tableModel.get(ia[i]);
				}
				JHTScanBank scanner =
						new JHTScanBank(radio, ba);
			}
		}
		
		if (cmd.equals("Tune Selected")) {
			if (table.getSelectedRowCount() > 0) {
				radio.setBand(tableModel.get(table.getSelectedRow()));
			}
		}

		if (cmd.equals("About")) {
			ImageIcon splashImg = new ImageIcon(cl.getResource("help/JHTLogo256.jpg"));
			JOptionPane.showMessageDialog(null,
		"JHamTune -- Amateur Radio Station contol in Java\n"+
		"Version 1.1 -- preview edition\n"+
		"(C) 2002-2003 by Maggie Leber K3XS <k3xs@arrl.net>",
		"about JHamTune",
		JOptionPane.PLAIN_MESSAGE,
		splashImg);
		}

		if (cmd.equals("Exit")) {
			radio.disconnect();
			System.exit(0);
		}
	}

   /*
     * WindowListener methods
     */
    public void windowOpened(WindowEvent e) {}
    public void windowClosing(WindowEvent e) {
	radio.disconnect();
	System.exit(0);
    }
    public void windowClosed(WindowEvent e) {}
    public void windowIconified(WindowEvent e) {}
    public void windowDeiconified(WindowEvent e) {}
    public void windowActivated(WindowEvent e) {}
    public void windowDeactivated(WindowEvent e) {}
    public void windowStateChanged(WindowEvent e) {}
    public void windowGainedFocus(WindowEvent e) {}
    public void windowLostFocus(WindowEvent e) {}

	void testData() {
		data = new ArrayList();
		data.add(new JHTBand("N3KZ Link System", 443800000, 0, 0, 000000000, JHTMode.FM, JHTCTCSS.none, false));
		data.add(new JHTBand("W3QV Philmont", 147030000, 0, 0, 147630000, JHTMode.FM, JHTCTCSS.plZZ, false));
		data.add(new JHTBand("N3ACL Mont. RACES", 146835000, 0, 0, 000000000, JHTMode.FM, JHTCTCSS.none, false));
		data.add(new JHTBand("K3DBD Pottstown", 147210000, 0, 0, 000000000, JHTMode.FM, JHTCTCSS.none, false));
		data.add(new JHTBand("K3MN Marple-Nwtn", 442250000, 0, 0, 447250000, JHTMode.FM, JHTCTCSS.pl3B, false));
		data.add(new JHTBand("K3DN Warminster", 147090000, 0, 0, 147690000, JHTMode.FM, JHTCTCSS.pl3B, false));
		data.add(new JHTBand("N3IO IRLP", 443100000, 0, 0, 448100000, JHTMode.FM, JHTCTCSS.pl3B, false));
		data.add(new JHTBand("W3OK", 146700000, 0, 0, 000000000, JHTMode.FM, JHTCTCSS.pl3B, false));
		data.add(new JHTBand("FSS 122.2", 122200000, 0, 0, 000000000, JHTMode.AM, JHTCTCSS.none, false));
		data.add(new JHTBand("KPHL appr 128.4", 128400000, 0, 0, 000000000, JHTMode.AM, JHTCTCSS.none, false));
		data.add(new JHTBand("KPHL appr 119.75", 119750000, 0, 0, 000000000, JHTMode.AM, JHTCTCSS.none, false));
		data.add(new JHTBand("KPHL appr 128.4", 128400000, 0, 0, 000000000, JHTMode.AM, JHTCTCSS.none, false));
		data.add(new JHTBand("2m call 146.52", 146520000, 0, 0, 146520000, JHTMode.FM, JHTCTCSS.none, false));
		data.add(new JHTBand("70cm call 446.0", 446000000, 0, 0, 446000000, JHTMode.FM, JHTCTCSS.none, false));
		data.add(new JHTBand("W3UER   ", 147360000, 0, 0, 000000000, JHTMode.FM, JHTCTCSS.none, false));
		data.add(new JHTBand("KIH28 NOAA wx radio", 162475000, 0, 0, 000000000, JHTMode.FM, JHTCTCSS.none, false));
		data.add(new JHTBand("WWV 10", 10000000, 0, 0, 000000000, JHTMode.AM, JHTCTCSS.none, false));
		data.add(new JHTBand("WWV 15", 15000000, 0, 0, 000000000, JHTMode.AM, JHTCTCSS.none, false));
		data.add(new JHTBand("CHU 80m", 3330000, 0, 0, 000000000, JHTMode.AM, JHTCTCSS.none, false));
		data.add(new JHTBand("CHU 75m", 7335000, 0, 0, 000000000, JHTMode.AM, JHTCTCSS.none, false));

	}
}

