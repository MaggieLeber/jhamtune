package com.k3xs.JHamTune;
import java.io.*;
import java.util.*;

/**
 * <b>JHamTune</b> driver for a simulated radio. 
 * @author Margaret Leber k3xs@arrl.net
 * @version 1.0
 * Useful for software testing when you don't have a supported radio
 **/
public class SimulatedJHTRadio implements JHTRadio
{
	private HashSet RxBands   = new HashSet();
	private HashSet RxtxBands = new HashSet();
	private String name;
	private int freq;
	private JHTMode mode = JHTMode.AM;
	private JHTCTCSS tone = JHTCTCSS.plXZ;

	public SimulatedJHTRadio()
	{
		
		this.name="Simulated Radio";

		// initialize the band capability hashtables

		RxtxBands.add(new JHTBand("160m",1800000,1800000,2000000,JHTMode.LSB));
		RxtxBands.add(new JHTBand("80m CW",3500000,3500000,3750000,JHTMode.LSB));
		RxtxBands.add(new JHTBand("80m Phone",3750000,3750000,4000000,JHTMode.LSB));
		RxtxBands.add(new JHTBand("40m CW",7000000,7000000,7150000,JHTMode.LSB));
		RxtxBands.add(new JHTBand("40m phone",7150000,7150000,7300000,JHTMode.LSB));
		RxtxBands.add(new JHTBand("30m",10100000,10100000,10150000,JHTMode.LSB));
		RxtxBands.add(new JHTBand("20m CW",14000000,14000000,14150000,JHTMode.USB));
		RxtxBands.add(new JHTBand("20m phone",14150000,14150000,14350000,JHTMode.USB));
		RxtxBands.add(new JHTBand("17m CW",18068000,18068000,18110000,JHTMode.USB));
		RxtxBands.add(new JHTBand("17m phone",18110000,18110000,18168000,JHTMode.USB));
		RxtxBands.add(new JHTBand("15m CW",21008000,21000000,21200000,JHTMode.USB));
		RxtxBands.add(new JHTBand("15m phone",21200000,21200000,21450000,JHTMode.USB));
		RxtxBands.add(new JHTBand("12m CW",24890000,24890000,24930000,JHTMode.USB));
		RxtxBands.add(new JHTBand("12m phone",24930000,24930000,24990000,JHTMode.USB));
		RxtxBands.add(new JHTBand("10m CW",28000000,28000000,28300000,JHTMode.USB));
		RxtxBands.add(new JHTBand("10m phone",28300000,28300000,29700000,JHTMode.USB));
		RxtxBands.add(new JHTBand("6m FM",52020000,52020000,54000000,JHTMode.FM));
		RxBands.add(new JHTBand("AM broadcast",535000,535000,1605000,JHTMode.AM));
		RxBands.add(new JHTBand("2 MHz Marine",2000000,2000000,2500000,JHTMode.AM));
		RxBands.add(new JHTBand("3 MHz Mobile",2850000,2850000,3500000,JHTMode.AM));
		RxBands.add(new JHTBand("Public Service UHF",453000000,453000000,454000000,JHTMode.FM));
	}
	public void finalize()
	{
	}

	public void connect()
	{
	}	
	
	public void disconnect()
	{
	}


	public synchronized void setFreq(int freq)
	{
		this.freq = freq;
		return;
	}
	public synchronized void setBand(JHTBand band)
	{
		return;
	}

	public synchronized void setMode(JHTMode mode)
	{
                this.mode = mode;
		return;
	}

	public void setTone(JHTCTCSS tone)
	{
		this.tone = tone;
		return;
	}

	public synchronized int getSmeter()
	{
                return(32 * (int)Math.random()); 
		
	}

	public synchronized int getFreq()
	{
		return(this.freq) ;
	}

	public synchronized JHTMode getMode()
	{
	        return(this.mode);
	}

	public Collection getRxBands()
	{
		return(RxBands);
	}

	public Collection getRxTxBands()
	{
		return(RxtxBands);
	}

	public String getDriverName()
	{
		return(name);
	}
	
	public String getPortName()
	{
		return("no port");
	}

	public String getName()
	{
		return(name + " on no port");
	}
}

