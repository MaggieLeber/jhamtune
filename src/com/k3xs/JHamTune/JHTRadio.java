package com.k3xs.JHamTune;
import java.io.*;
import java.util.*;
/**
 * an interface implemented by drivers for each <b>JHamTune</b> supported radio.
 * @author  Margaret Leber K3XS@arrl.net 
 * @version 1.0
 */
public interface JHTRadio
{
	/**
	 * connect to the radio
	 */
	void connect();

	/**
	 * disconnect automatic control from the radio
	 */
	void disconnect();

	/**
	 * @param freq tune the radio to this frequency in Hz.
	 */
	void setFreq(int freq);

	/**
	 * set mode and band limits from a JHTBand. 
	 */
	public void setBand(JHTBand band);

	/**
	 * @return     the current frequency in Hz
	 */
	int  getFreq();

	/**
	 * @param mode set the radio to this modulation mode.
	 */
	void setMode(JHTMode mode);

	/**
	 * @param tone set this CTCSS/PL tone encoding. (write only)
	 */
	void setTone(JHTCTCSS tone);

	/**
	 * @return     the current modulation mode
	 */
	JHTMode getMode();

	/**
	 * @return     the current signal strength (S-meter reading expressed 0-31)
	 */
	int  getSmeter();

	/**
	 * @return     the name for this radio
	 */
	String  getDriverName();

	/**
	 * @return     the name for the port attaching this radio, 
	 * or null if not connect()ed	 */
	String  getPortName();
	
	/**
	 * @return     the name for this instance,  
	 * usually driverName + " on " + port  */
	String  getName();
		
	/**
	 * @return     a Collection of receive-only JHTBands 
	 */
	Collection  getRxBands();

	/**
	 * @return     a Collection of transceive-capable JHTBands 
	 */
	Collection  getRxTxBands();
}
