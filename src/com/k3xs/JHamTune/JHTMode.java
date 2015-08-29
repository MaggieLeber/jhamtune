package com.k3xs.JHamTune;
import java.io.*;
import javax.swing.*;

/**
 * Encapsulates modulation modes.
 * @author Margaret Leber k3xs@arrl.net
 * @version 1.0
 **/
public class JHTMode implements Serializable
{
public static final JHTMode AM  = new JHTMode("AM");
public static final JHTMode FM  = new JHTMode("FM");
public static final JHTMode LSB = new JHTMode("LSB");
public static final JHTMode USB = new JHTMode("USB");
public static final JHTMode CW  = new JHTMode("CW");
private static final JHTMode[] allModes = { JHTMode.AM, 
                       JHTMode.FM, 
                       JHTMode.LSB, 
                       JHTMode.USB,
                       JHTMode.CW }; 
private String name;

public JHTMode()            {this.name = "AM";}

public JHTMode(String name) {this.name = validateName(name);}

public void setName(String name){
	this.name = name;
}

public String getName(){
	return this.name;
}

private String validateName(String name) {
	if (name.equals("FM") ||
	    name.equals("LSB") ||
	    name.equals("USB") ||
	    name.equals("CW") ) 
	      { return(name); }
	      else { return("AM"); }
}


public static JHTMode [] getAll()   // update this when adding new modes
   {
            return (allModes);
   }

public static JComboBox getComboBox(JHTMode s)
   {
    JComboBox box = new JComboBox();
    JHTMode [] all = JHTMode.getAll();
    for ( int i =0; i< all.length; i++) box.addItem(all[i]);
    box.setSelectedItem(s);
    return(box);
   } 

public String toString(){return this.name;}
}
