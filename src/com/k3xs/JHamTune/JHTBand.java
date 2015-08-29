package com.k3xs.JHamTune;
import java.io.*;
/**
 * Encapsulates a frequency or frequency range and operating/modulation modes.
 * @author Margaret Leber k3xs@arrl.net
 * @version 1.0
 */ 
public class JHTBand implements Serializable
{
	private String  name;      // name of this JHTBand
	private int     initFreq;  // initial RX freq between low and high
	private int     lowFreq;   // bottom edge of RX band
	private int     highFreq;  // top edge of RX band
	private int     txFreq;

	private boolean FDX;       // duplex mode: true=full duplex
	private JHTMode mode;      // modulation mode
	private JHTCTCSS txCTCSS;  // PL tone on transmit
	private JHTCTCSS rxCTCSS;  // PL tone on receive

	public JHTBand() {}; // bean constructor -- use with caution 
	
	public JHTBand(String name,
	               int initFreq,
	               int lowFreq,
	               int highFreq,
	               JHTMode mode)
	{
		this.name = name;
		this.initFreq = initFreq;
		this.txFreq = initFreq;
		this.lowFreq = lowFreq;
		this.highFreq = highFreq;
		this.mode = mode;
		this.FDX  = false;
		this.txCTCSS = JHTCTCSS.none;
		this.rxCTCSS = JHTCTCSS.none;

	}

	public JHTBand(String name,
	               int initFreq,
	               int lowFreq,
	               int highFreq,
	               int txFreq,
	               JHTMode mode,
	               JHTCTCSS tone,
	               boolean FDX)
	{
		this.name = name;
		this.initFreq = initFreq;
		this.txFreq = txFreq;
		this.lowFreq = lowFreq;
		this.highFreq = highFreq;
		this.mode = mode;
		this.txCTCSS = tone;
		this.FDX = FDX;
	}
	public String getName()
	{ return(name); }
	public void setName(String name)
	{ this.name = name; }

	public int getInitFreq()
	{ return(initFreq); }
	public void setInitFreq(int initFreq)
	{ this.initFreq = initFreq; }

	public int getLowFreq()
	{ return(lowFreq); }
	public void setLowFreq(int lowFreq)
	{ this.lowFreq = lowFreq; }

	public int getHighFreq()
	{ return(highFreq); }
	public void setHighFreq(int highFreq)
	{ this.highFreq = highFreq; }

	public int getTxFreq()
	{ return(txFreq); }
	public void setTxFreq(int txFreq)
	{ this.txFreq = txFreq; }

	public JHTMode getMode()
	{ return(mode); }
	public void setMode(JHTMode mode)
	{ this.mode=mode; }

	public JHTCTCSS getCTCSS()
	{ return(txCTCSS); }
	public void setCTCSS(JHTCTCSS tone)
	{ this.txCTCSS = tone; }
	
	public JHTCTCSS getRxCTCSS()
	{ return(rxCTCSS); }
	public void setRxCTCSS(JHTCTCSS tone)
	{ this.rxCTCSS = tone; }

	public boolean getFDX()
	{ return(FDX); }
	public void setFDX(boolean FDX)
	{ this.FDX=FDX; }

	public JHTSweepFrame makeTuner(JHTRadio radio)
	{ return(new JHTSweepFrame(radio, this));}
}
