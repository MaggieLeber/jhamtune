package com.k3xs.JHamTune;
/**
 * A GUI widget for JHTModes
 * @author Margaret Leber k3xs@arrl.net
 * @version 1.0
 **/
import javax.swing.*;

public class JHTModeCombo extends JComboBox
{

  JHTModeCombo(JHTMode mode)

  {
  JHTMode [] all = JHTMode.getAll();
  for ( int i =0; i< all.length; i++)  addItem(all[i]);
  }

}
