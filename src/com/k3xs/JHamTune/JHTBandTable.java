package com.k3xs.JHamTune;
import java.io.*;
import java.text.*;
import java.beans.*;
import java.util.*;
import javax.swing.*;
import javax.swing.table.*;


/**
 * Table model for frequency data used by <b>JHamTune</b>. 
 * JHamTune saves and restores frequency lists as serialized JHTBandTable objects. 
 * 
 * @author Margaret Leber k3xs@arrl.net
 * @version 1.0
 **/
class JHTBandTable
    extends AbstractTableModel {

    ArrayList data;

    JHTBandTable(Collection d) {
        data = new ArrayList(d);
        fireTableDataChanged();
    }

    public void fileSave(File f) {
        try {
            FileOutputStream os = new FileOutputStream(f);
            ObjectOutputStream s = new ObjectOutputStream(os);
            s.writeObject(data);
            s.flush();
        } catch (Exception ex) {
            JHamTune.logger.warning("Error during File Save:" + ex);
            JOptionPane.showMessageDialog(null, "Error during file save:" + ex, 
                                          "File error", 
                                          JOptionPane.ERROR_MESSAGE);
        }
    }
    
        public void XMLSave(File f) {
        try {
            FileOutputStream os = new FileOutputStream(f);
            XMLEncoder xe = new XMLEncoder(os);
	    Iterator i = data.iterator();
	    while (i.hasNext()) {
		    xe.writeObject(i.next());
	    };
            xe.flush();
        } catch (Exception ex) {
            JHamTune.logger.warning("Error during XML Save:" + ex);
            JOptionPane.showMessageDialog(null, "Error during XML save:" + ex, 
                                          "File error", 
                                          JOptionPane.ERROR_MESSAGE);
        }
    }
    
    public void addAll(File f) {

        try {
		FileReader fr = new FileReader(f);
		
		// if the first 5 chars of the file are "<?xml" 
		// read it with an XMLDecoder, 
		char[] firstFiveChars = new char[5];
		fr.read(firstFiveChars);
		String firstFive = new String(firstFiveChars);
		if (firstFive.equals("<?xml"))
		{
		fr.close();
		addAll(new XMLDecoder(new FileInputStream(f)));
		} else {
		fr.close();
		// otherwise it's a serialized Collection of JHTBands
		addAll(new ObjectInputStream(new FileInputStream(f)));
		}
		
        } catch (IOException ex) {
            JHamTune.logger.warning("Error during load:" + ex);
            JOptionPane.showMessageDialog(null, "Error during file Load:" + ex, 
                                          "File error", 
                                          JOptionPane.ERROR_MESSAGE);
        }
    }

    public void addAll(ObjectInputStream s) {

        try {
            addAll((Collection)s.readObject());
        } catch (Exception ex) {
            JHamTune.logger.warning("Error during File Load:" + ex);
            JOptionPane.showMessageDialog(null, "Error during File Load:" + ex, 
                                          "File error", 
                                          JOptionPane.ERROR_MESSAGE);
        }
    }
    
    public void addAll(XMLDecoder xd) {

        try {
	    ArrayList bands = new ArrayList(); 	
	    try {
		 while (true) bands.add(xd.readObject());
	    } catch (ArrayIndexOutOfBoundsException e) {}
	    addAll((Collection)bands);
        } catch (Exception ex) {
            JHamTune.logger.warning("Error during File Load:" + ex);
            JOptionPane.showMessageDialog(null, "Error during File Load:" + ex, 
                                          "File error", 
                                          JOptionPane.ERROR_MESSAGE);
        }
    }

    public void addAll(Collection c) {
        data.addAll(c);
        fireTableDataChanged();
    }

    public void clear() {
        data.clear();
        fireTableDataChanged();
    }

    public void remove(int i) {
        data.remove(i);
        fireTableRowsDeleted(i, i);
    }

    public void add(int i, JHTBand b) {
        data.add(i, b);
        fireTableRowsInserted(i, i);
    }

    public JHTBand get(int i) {

        return (JHTBand)data.get(i);
    }

    public void add(JHTBand b) {
        data.add(b);
        fireTableRowsInserted(data.size(), data.size());
    }

    public String getColumnName(int col) {

        switch (col) {

            case 0:
                return ("Name");
            case 1:
                return ("RxFreq");
            case 2:
                return ("LowFreq");
            case 3:
                return ("HighFreq");
            case 4:
                return ("TxFreq");
            case 5:
                return ("Mode");
            case 6:
                return ("CTCSS");
            case 7:
                return ("FDX");
        }

        return ("oops");
    }

    public int getRowCount() {

        return data.size();
    }

    public int getColumnCount() {
        return (8);
    }

    public Class getColumnClass(int c) {
        return (getValueAt(0, c).getClass());
    }

    public Object getValueAt(int row, int col) {

        JHTBand b = (JHTBand)(data.get(row));

        switch (col) {

            case 0:
                return (b.getName());
            case 1:
                return (new Double((b.getInitFreq()) / 10e5));
            case 2:
                return (new Double((b.getLowFreq()) / 10e5));
            case 3:
                return (new Double((b.getHighFreq()) / 10e5));
            case 4:
                return (new Double((b.getTxFreq()) / 10e5));
            case 5:
                return (b.getMode());
            case 6:
                return (b.getCTCSS());
            case 7:
                return (new Boolean(b.getFDX()));
        }

        return ("oops");
    }

    public boolean isCellEditable(int row, int col) {

        return true;
    }

    public void setValueAt(Object value, int row, int col) {

        JHTBand b = (JHTBand)(data.get(row));

        switch (col) {

            case 0:
             {
                b.setName(value.toString());
                break;
            }
            case 1:
             {
                b.setInitFreq((int)(((Double)value).doubleValue() * 10e5));
                break;
            }
            case 2:
             {
                b.setLowFreq((int)(((Double)value).doubleValue() * 10e5));
                break;
            }
            case 3:
             {
                b.setHighFreq((int)(((Double)value).doubleValue() * 10e5));
                break;
            }
            case 4:
             {
                b.setTxFreq((int)(((Double)value).doubleValue() * 10e5));
                break;
            }
            case 5:
             {
                b.setMode((JHTMode)value);
                break;
            }
            case 6:
             {
                b.setCTCSS((JHTCTCSS)value);
		JHamTune.logger.info("CTCSS set:"+value);
                break;
            }
            case 7:
             {
                b.setFDX(((Boolean)value).booleanValue());
                break;
            }
        }

        fireTableCellUpdated(row, col);
    }

    public String toMHz(int freq) {
	    if (freq == 0) { return ("---"); }
        else {
            DecimalFormat fmt = new DecimalFormat("##0.0##### MHz");
            return (fmt.format((double)(freq) / 10e5));
        }
    }
}
