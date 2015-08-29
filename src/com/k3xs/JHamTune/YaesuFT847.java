package com.k3xs.JHamTune;
import java.io.*;
import java.util.*;
import javax.comm.*;

/**
 * <b>JHamTune</b> driver for the <i>Yaesu FT-847</i> radio. 
 * @author Margaret Leber k3xs@arrl.net
 * @version 1.0
 * <pre>
 * Radio specs from the FT-847 manual:
 *  GENERAL 
 * Frequency Range: Receive 100 kHz - 36.99 MHz 
 * 	37 - 76 MHz 
 * 	108 - 174 MHz 
 * 	420 - 512 MHz 
 * Transmit 160 - 6 Meters 
 * 	2 Meters 70 Centimeters (Amateur bands only) 
 * 	5.1675 MHz (Alaska Emergency Channel) 
 * Emission Modes: USB, LSB, CW, AM, FM, F1 (9600 bps Packet), F2 (1200 bps Packet), AFSK 
 * Synthesizer Steps (Min.): 0.1 Hz (CW/SSB) 10 Hz (AM/FM) 
 * </pre>
 **/
public class YaesuFT847 implements JHTRadio
{
	private InputStream CATout ;
	private OutputStream CATin ;
	private HashSet RxBands   = new HashSet();
	private HashSet RxtxBands = new HashSet();
	private final String name = "Yaesu FT-847";
	private String port;
	private int cmdDelay = 75; // ms delay to allow radio to respond 
	private boolean DEBUG = false;
	/**
	 * true:  access serial port though file system 
	 *        (probably only works on UNIX)
	 * false: access serial port through javax.comm 
	 */
	private boolean useFileSystem = false; 


	public YaesuFT847(String p)
	{
		this.port = p;
		connect(); 
		
		// initialize the band capabilities
		RxtxBands.add(new JHTBand("160m amateur band",1800000,1800000,2000000,JHTMode.LSB));
		RxtxBands.add(new JHTBand("80m CW amateur band",3500000,3500000,3750000,JHTMode.LSB));
		RxtxBands.add(new JHTBand("80m Phone amateur band",3750000,3750000,4000000,JHTMode.LSB));
		RxtxBands.add(new JHTBand("40m CW amateur band",7000000,7000000,7150000,JHTMode.LSB));
		RxtxBands.add(new JHTBand("40m phone amateur band",7150000,7150000,7300000,JHTMode.LSB));
		RxtxBands.add(new JHTBand("30m amateur band",10100000,10100000,10150000,JHTMode.LSB));
		RxtxBands.add(new JHTBand("20m CW amateur band",14000000,14000000,14150000,JHTMode.USB));
		RxtxBands.add(new JHTBand("20m phone amateur band",14150000,14150000,14350000,JHTMode.USB));
		RxtxBands.add(new JHTBand("17m CW amateur band",18068000,18068000,18110000,JHTMode.USB));
		RxtxBands.add(new JHTBand("17m phone amateur band",18110000,18110000,18168000,JHTMode.USB));
		RxtxBands.add(new JHTBand("15m CW amateur band",21008000,21000000,21200000,JHTMode.USB));
		RxtxBands.add(new JHTBand("15m phone amateur band",21200000,21200000,21450000,JHTMode.USB));
		RxtxBands.add(new JHTBand("12m CW amateur band",24890000,24890000,24930000,JHTMode.USB));
		RxtxBands.add(new JHTBand("12m phone amateur band",24930000,24930000,24990000,JHTMode.USB));
		RxtxBands.add(new JHTBand("10m CW amateur band",28000000,28000000,28300000,JHTMode.USB));
		RxtxBands.add(new JHTBand("10m phone amateur band",28300000,28300000,29700000,JHTMode.USB));
		RxtxBands.add(new JHTBand("6m FM amateur band",52020000,52020000,54000000,JHTMode.FM));
		RxBands.add(new JHTBand("AM broadcast",535000,535000,1605000,JHTMode.AM));
		RxBands.add(new JHTBand("2 MHz Marine",2000000,2000000,2500000,JHTMode.AM));
		RxBands.add(new JHTBand("3 MHz Mobile",2850000,2850000,3500000,JHTMode.AM));
		RxBands.add(new JHTBand("Public Service UHF",453000000,453000000,454000000,JHTMode.FM));
	}
	public void finalize()
	{
	disconnect();
	}
	
	/**
	 * disconnect automatic control from the radio
	 */
	 public void disconnect(){
		setCAToff();
	 	try
		{
		CATout.close();
		}
		catch (Exception e)
		{
		System.out.println("YaesuFT847 terminating: error closing serial port:"+e);
		System.exit(0);
		}
	 }
	 
	 public void connect(){	
	 	// open the radio serial control port 
		if (useFileSystem) {
		try
		{
			CATout = new FileInputStream(port);
			CATin  = new FileOutputStream(port);
		}
		catch (Exception e)
		{ 
		JHamTune.logger.severe("YaesuFT847 terminating: error opening serial port through file system:"+e);
		System.exit(0);
		}
		 }
		 else { // using javax.comm
		try
		{
			Enumeration ports = CommPortIdentifier.getPortIdentifiers();
			CommPortIdentifier cpi;
			while (ports.hasMoreElements()) {
				cpi = (CommPortIdentifier) ports.nextElement();
				JHamTune.logger.info("Type "+
					cpi.getPortType()+
					" port "+
					cpi.getName()+
					" detected."
						    );
							}
			cpi = CommPortIdentifier.getPortIdentifier(port);
			CommPort cp = cpi.open("JHamTune.FT847",5000);
			JHamTune.logger.info("Connected to type "+
					cpi.getPortType()+
					" port "+
					cpi.getName()
					     );
			CATout = cp.getInputStream();
			CATin  = cp.getOutputStream();
		}
		catch (Exception e)
		{ 
		JHamTune.logger.severe("YaesuFT847 terminating: error opening serial port "+port+" through javax.comm:"+e);
		System.exit(0);
		}
		 }
		setCATon();
	 }
	 
	public void setDebug(boolean b)
	{
		DEBUG = b;
		return;
	}


	/**
	 * unpack FT-847 BCD freq into int: 10's of Hz
	 */
	private int unpackFreq(byte input[])
	{
		int freq;
		freq =        (int)((input[0] & 0xf0) >> 4) * (int)1e7;
		freq = freq + (int)(input[0] & 0x0f)      * (int)1e6;
		freq = freq + (int)((input[1] & 0xf0) >> 4) * (int)1e5;
		freq = freq + (int)(input[1] & 0x0f)      * (int)1e4;
		freq = freq + (int)((input[2] & 0xf0) >> 4) * (int)1e3;
		freq = freq + (int)(input[2] & 0x0f)      * (int)1e2;
		freq = freq + (int)((input[3] & 0xf0) >> 4) * (int)10;
		freq = freq + (int)(input[3] & 0x0f);
		return freq;
	}

	/**
	 * setCATon -- turns CAT mode on. 
	 *
	 *  CAT command word: xx xx xx xx 00  
	 */
	private void setCATon()
	{
		byte[] cmd = { 0, 0, 0, 0, 0 };
		cmdNoResp(cmd);
		return ;
	}

	private String byteArrayDump(byte[] b)
	{
		StringBuffer sb = new StringBuffer();
		for (int i =0; i<b.length; i++)
		{
			sb.append("["+Integer.toHexString((int)b[i])+"]");
		}
		return (sb.toString());
	}
	/**
	 * setCATon -- turns CAT mode off. 
	 *
	 *  <pre>
	 *  CAT command word: xx xx xx xx 80  
	 *  </pre>
	 */
	private void setCAToff()
	{
		byte[] cmd = { 0, 0, 0, 0, (byte)0x80 };
		cmdNoResp(cmd);
		return ;
	}


	/**
	 * setSATon -- turns satellite mode on. 
	 *
	 *  <pre>
	 *  CAT command word: xx xx xx xx 4e  
	 *  </pre>
	 */
	private void setSATon()
	{
		byte[] cmd = { 0, 0, 0, 0, (byte)0x4e };
		cmdNoResp(cmd);
		return ;
	}


	/**
	 * setSAToff -- turns satellite mode off. 
	 *
	 *
	 *  <pre>
	 *  CAT command word: xx xx xx xx 8e  
	 *  </pre>
	 **/
	private void setSAToff()
	{
		byte[] cmd = { 0, 0, 0, 0, (byte)0x8e };
		cmdNoResp(cmd);
		return ;
	}


	/**
	 * set main VFO frequency. 
	 *
	 *
	 *  <pre>
	 *  CAT command word: Q1 Q2 Q3 Q4 01 set main VFO  
	 *  CAT command word: Q1 Q2 Q3 Q4 11 set Sat RX VFO  
	 *  CAT command word: Q1 Q2 Q3 Q4 21 set Sat TX VFO 
	 * 
	 *    Q1-Q4  are frequency to set in a goofy nybble-oriented BCD
	 *     e.g.  to set 432.2705 MHz send "43 22 70 50"
	 * </pre>
	 **/
	public synchronized void setFreq(int freq)
	{
		freq = freq/10;

		byte[] cmd = {
		     (byte)((freq/1000000%10)+(freq/10000000%10)*16),
		     (byte)((freq/10000%10)+(freq/100000%10)*16),
		     (byte)((freq/100%10)+(freq/1000%10)*16),
		     (byte)((freq%10)+(freq/10%10)*16),
		     (byte)0x01
		             };
		cmdNoResp(cmd);
		return;
	}


	/**
	* set all paramaters from a JHTBand 
	*/
	public synchronized void setBand(JHTBand band)
	{
		int offset = 0;
		if (band.getTxFreq() > 0) {
		offset = band.getTxFreq() - band.getInitFreq();
		}
		setFreq(band.getInitFreq());
		setMode(band.getMode());
		if (band.getFDX())
		{ // someday do somthing complex and funky involving SAT modes
                  // but for now:
		System.out.println("YaesuFT847.setBand():FDX not yet supported");
		}
		else{
			if (offset == 0) {setNoOffset();}
			else {
				if (offset == Math.abs(offset)) {
					setOffsetPlus();
					setOffsetFreq(offset);
				}
				else {
					setOffsetMinus();
					setOffsetFreq( 0 - offset);
				}
			}

			if (band.getCTCSS().getCode().equals("__"))
				setCTCSSoff();
			else
			{
				setCTCSSencode();
				setTone(band.getCTCSS());
			}
		}

		return;
	}


	private void setSatRxFreq(int freq)
	{
		freq = freq/10;

		byte[] cmd =   {
			       (byte)((freq/1000000%10)+(freq/10000000%10)*16),
			       (byte)((freq/10000%10)+(freq/100000%10)*16),
			       (byte)((freq/100%10)+(freq/1000%10)*16),
			       (byte)((freq%10)+(freq/10%10)*16),
			       (byte)0x11
		               };
		cmdNoResp(cmd);
		return;
	}


	private void setSatTxFreq(int freq)
	{
		freq = freq/10;

		byte[] cmd = {
			     (byte)((freq/1000000%10)+(freq/10000000%10)*16),
			     (byte)((freq/10000%10)+(freq/100000%10)*16),
			     (byte)((freq/100%10)+(freq/1000%10)*16),
			     (byte)((freq%10)+(freq/10%10)*16),
			     (byte)0x21
		             };
		cmdNoResp(cmd);
		return;
	}


	/**
	 * set main VFO mode. 
	 *
	 *  <pre>     
	 *  CAT command word: MM xx xx xx 07 set main VFO mode  
	 *  CAT command word: MM xx xx xx 17 set sat RX VFO mode  
	 *  CAT command word: MM xx xx xx 27 set sat TX VFO mode 
	 *     
	 *  byte     LSB      = 0x00;
	 *  byte     USB      = 0x01;
	 *  byte     CW       = 0x02;
	 *  byte     CWlsb    = 0x03; 
	 *  byte     AM       = 0x04;
	 *  byte     FM       = 0x08;  
	 *  byte     CWnar    = (byte) 0x82; 
	 *  byte     CWnarlsb = (byte) 0x83;
	 *  byte     AMnar    = (byte) 0x84;
	 *  byte     FMnar    = (byte) 0x88;
	 * </pre>
	 */
	private void setMainMode(byte mode)
	{
		byte[] cmd ={ mode, 0, 0, 0, (byte)0x07 };
		cmdNoResp(cmd);
		return ;
	}

	public synchronized void setMode(JHTMode mode)
	{
		if (mode.getName().equals("AM"))  setMainMode((byte)0x04); else
		if (mode.getName().equals("FM"))  setMainMode((byte)0x08); else
		if (mode.getName().equals("CW"))  setMainMode((byte)0x02); else
		if (mode.getName().equals("LSB")) setMainMode((byte)0x00); else
		if (mode.getName().equals("USB")) setMainMode((byte)0x01); else
		 System.out.println("YaesuFT847.setMode():Unsupported mode:"+mode.getName());
		return;
	}

	/**
	  * set sat RX VFO mode 
	  *
	  *  CAT command word: MM xx xx xx 17 set sat RX VFO mode  
	  */
	private void setSATrxMode(byte mode)
	{
		byte [] cmd = { mode, 0, 0, 0, (byte)0x17 };
		cmdNoResp(cmd);
		return ;
	}

	/**
	  * set sat TX VFO mode.
	  *
	  *  <pre>     
	  *  CAT command word: MM xx xx xx 27 set sat TX VFO mode 
	  *  </pre>
	  */
	private void setSATtxMode(byte mode)
	{
		byte[] cmd = { mode, 0, 0, 0, (byte)0x27 };
		cmdNoResp(cmd);
		return ;
	}

	/**
	 * set VFO squelch mode. 
	 *
	 *  <pre>     
	 *  CAT command word: SS xx xx xx 0a set main VFO Squelch mode  
	 *  CAT command word: SS xx xx xx 1a set sat RX VFO Squelch mode  
	 *  CAT command word: SS xx xx xx 2a set sat TX VFO Squelch mode  
	 *    SS: 0a=DCS on 
	 *        2a=CTCSS encode and decode on
	 *        4a=CTCSS encode on 
	 *        8a=CTCSS and DCS off 
	 *  </pre>
	 **/
	private void setCTCSSencode()
	{
		setSquelchMode((byte)0x4a);
	}

	private void setCTCSSencodeDecode()
	{
		setSquelchMode((byte)0x2a);
	}

	private void setCTCSSoff()
	{
		setSquelchMode((byte)0x8a);
	}

	private void setSquelchMode(byte mode)
	{
		byte[] cmd =  { mode, 0, 0, 0, (byte)0x0a };
		cmdNoResp(cmd);
		return ;
	}

	private void setSatRxSquelchMode(byte mode)
	{
		byte[] cmd = { mode, 0, 0, 0, (byte)0x1a };
		cmdNoResp(cmd);
		return ;
	}

	private void setSatTxSquelchMode(byte mode)
	{
		byte[] cmd = { mode, 0, 0, 0, (byte)0x2a };
		cmdNoResp(cmd);
		return ;
	}


	/**
	 * set CTCSS freq. 
	 *
	 *  <pre>     
	 *  CAT command word: QQ xx xx xx 0b set main VFO CTCSS freq  
	 *  CAT command word: QQ xx xx xx 1b set sat RX VFO CTCSS freq  
	 *  CAT command word: QQ xx xx xx 2b set sat TX VFO CTCSS freq  
	 *  QQ: freq
	        *
	 *   67.0=3f, 69.3=39, 71.9=1f, 74.4=3e, 77.0=07,
	 *   79.9=3d, 82.5=1e, 85.4=3c, 88.5=0e, 91.5=3b,
	 *   94.8=1d, 97.4=3a, 100.0=0d, 103.5=1c, 107.2=0c,
	 *   110.9=1b, 114.8=0b, 118.8=1a, 123.0=0a,
	 *   127.3=19, 131.8=09, 136.5=18, 141.3=08, 146.2=17,
	 *   151.4=07, 156.7=16, 162.2=06, 167.9=15, 173.8=05,     
	 *   179.9=14, 186.2=04, 192.8=13, 203.5=03, 210.7=12,
	 *   218.1=02, 225.7=11, 233.6=01, 214.8=10, 250.3=00
	 *  </pre>
	 **/
	private void setCTCSSFreq(byte tone)
	{
		byte[] cmd = { tone, 0, 0, 0, (byte)0x0b };
		cmdNoResp(cmd);
		return ;
	}
	private void setSatRxCTCSSFreq(byte mode)
	{
		byte[] cmd = { mode, 0, 0, 0, (byte)0x1b };
		cmdNoResp(cmd);
		return ;
	}
	private void setSatTxCTCSSFreq(byte mode)
	{
		byte[] cmd = { mode, 0, 0, 0, (byte)0x2b };
		cmdNoResp(cmd );
		return ;
	}

	private byte CTCSSSByte(JHTCTCSS pl)
	{
		if (pl.getCode().equals("XZ")) return((byte) 0x3f); // 67.0
		if (pl.getCode().equals("WZ")) return((byte) 0x39); // 69.3
		if (pl.getCode().equals("XA")) return((byte) 0x1f); // 71.9
		if (pl.getCode().equals("WA")) return((byte) 0x3e); // 74.4
		if (pl.getCode().equals("XB")) return((byte) 0x0f); // 77.0
		if (pl.getCode().equals("WB")) return((byte) 0x3d); // 79.7
		if (pl.getCode().equals("YZ")) return((byte) 0x1e); // 82.5
		if (pl.getCode().equals("YA")) return((byte) 0x3c); // 85.4
		if (pl.getCode().equals("YB")) return((byte) 0x0e); // 88.5
		if (pl.getCode().equals("ZZ")) return((byte) 0x3b); // 91.5
		if (pl.getCode().equals("ZA")) return((byte) 0x1d); // 94.8
		if (pl.getCode().equals("ZB")) return((byte) 0x3a); // 97.4
		if (pl.getCode().equals("1Z")) return((byte) 0x0d); // 100.0
		if (pl.getCode().equals("1A")) return((byte) 0x1c); // 103.5
		if (pl.getCode().equals("1B")) return((byte) 0x0c); // 107.2
		if (pl.getCode().equals("2Z")) return((byte) 0x1b); // 110.9
		if (pl.getCode().equals("2A")) return((byte) 0x0b); // 114.8
		if (pl.getCode().equals("2B")) return((byte) 0x1a); // 118.8
		if (pl.getCode().equals("3Z")) return((byte) 0x0a); // 123.0
		if (pl.getCode().equals("3A")) return((byte) 0x19); // 127.3
		if (pl.getCode().equals("3B")) return((byte) 0x09); // 131.8
		if (pl.getCode().equals("4Z")) return((byte) 0x18); // 136.5
		if (pl.getCode().equals("4A")) return((byte) 0x08); // 141.3
		if (pl.getCode().equals("4B")) return((byte) 0x17); // 146.2
		if (pl.getCode().equals("5Z")) return((byte) 0x07); // 151.4
		if (pl.getCode().equals("5A")) return((byte) 0x16); // 156.7
		if (pl.getCode().equals("5B")) return((byte) 0x06); // 162.2
		if (pl.getCode().equals("6Z")) return((byte) 0x15); // 167.9
		if (pl.getCode().equals("6A")) return((byte) 0x05); // 173.8
		if (pl.getCode().equals("6B")) return((byte) 0x14); // 179.9
		if (pl.getCode().equals("7Z")) return((byte) 0x04); // 186.2
		if (pl.getCode().equals("7A")) return((byte) 0x13); // 192.8
		if (pl.getCode().equals("M1")) return((byte) 0x03); // 203.5
	// ? 	if (pl.getCode().equals("8Z")) return((byte) 0x??); // 206.5
		if (pl.getCode().equals("M2")) return((byte) 0x12); // 210.7
		if (pl.getCode().equals("M3")) return((byte) 0x01); // 218.1
		if (pl.getCode().equals("M4")) return((byte) 0x11); // 225.7
	// ?	if (pl.getCode().equals("9Z")) return((byte) 0x??); // 229.1
		if (pl.getCode().equals("M5")) return((byte) 0x01); // 233.6
		if (pl.getCode().equals("M6")) return((byte) 0x10); // 241.8
		if (pl.getCode().equals("M7")) return((byte) 0x00); // 250.3
		return((byte) 0);
	}

	public void setTone(JHTCTCSS tone)
	{
		setCTCSSFreq(CTCSSSByte(tone));
	}

	/**
	 * set DCS digital code.
	 *
	 *  <pre>     
	 *  CAT command word: S1 S2 xx xx 0C set   main VFO DCS digital code  
	 *  CAT command word: S1 S2 xx xx 1C set sat RX VFO DCS digital code  
	 *  CAT command word: S1 S2 xx xx 2C set sat TX VFO DCS digital code
	 *  S1, S2: DCS code as four BCD nybbles
	 *  </pre>
	 **/
	private void setDCSCode(int squelch)
	{
		System.out.println("YaesuFT847.setDCSCode(): not yet supported");
		return ;
	}

	/**
	 * set repeater offset direction.
	 *
	 *  <pre>     
	 *  CAT command word: 09 xx xx xx 09 set negative offset  
	 *  CAT command word: 49 xx xx xx 09 set positive offset   
	 *  CAT command word: 89 xx xx xx 2C set simplex mode-no offset
	 *  </pre>
	 **/
	private void setOffsetMinus()
	{
		byte[] cmd = { (byte)0x09, 0, 0, 0, (byte)0x09 };
		cmdNoResp(cmd);
		return ;
	}

	private void setOffsetPlus()
	{
		byte[] cmd =  { (byte)0x49, 0, 0, 0, (byte)0x09 };
		cmdNoResp(cmd);
		return ;
	}

	private void setNoOffset()
	{
		byte[] cmd =  { (byte)0x89, 0, 0, 0, (byte)0x09 };
		cmdNoResp(cmd);
		return ;
	}

	/**
	 * set repeater frequency offset.
	 *
	 *  <pre>     
	 *  CAT command word: P1 P2 P3 P4 f9 
	 *  P1-P4: difference between TX and RX freq as eight BCD nybbles
	 *    eg: 00 50 00 00 = 5MHz
	 *  </pre>
	 **/
	private void setOffsetFreq(int freq)
	{
		freq = freq / 10;
		byte[] cmd = {
		                     (byte)((freq/1000000%10)+(freq/10000000%10)*16),
		                     (byte)((freq/10000%10)+(freq/100000%10)*16),
		                     (byte)((freq/100%10)+(freq/1000%10)*16),
		                     (byte)((freq%10)+(freq/10%10)*16),
		                     (byte)0xf9
		             } ;
		cmdNoResp(cmd);
		return;
	}

	/**
	 * get current receive status.
	 *
	 *  <pre>     
	 *  CAT command word: xx xx xx xx e7 
	 *        reply data: bmdsssss
	 *             where  b:0=squelch closed  1=squelch open (BUSY)                     
	 *                    m:0=CTCSS/DCS match 1=no match
	 *                    d:0=discriminator centered 
	 *                      1=discriminator off-center
	 *                    sssss: S-meter data
	 *  </pre>
	 **/
	private byte getRxStatus()
	{
		byte[] cmd = { 0, 0, 0, 0, (byte)0xe7 };
		byte[] resp = cmdLongResp(cmd);
		return resp[0];
	}

	public synchronized int getSmeter()
	{
		return (int) (getRxStatus() & 0x1f);
	}

	/**
	 * get current transmit status.
	 *
	 *  <pre>     
	 *  CAT command word: xx xx xx xx xx f7  
	 *        reply data: tXXppppp
	 *             where  t:0=PTT on  1=PTT off                     
	 *                    XX:ignore
	 *                    ppppp: power meter data
	 *  </pre>
	 **/
	private byte getTxStatus()
	{
		byte[] cmd = { 0, 0, 0, 0, (byte)0xf7 };
		return(cmdShortResp(cmd)[0]);
	}

	/**
	 * get current frequency and mode.
	 *
	 *  <pre>     
	 *  CAT command word: xx xx xx xx 03 get   main VFO freq/mode
	 *  CAT command word: xx xx xx xx 13 get sat RX VFO freq/mode
	 *  CAT command word: xx xx xx xx 23 get sat TX VFO freq/mode
	 *             reply: Q1 Q2 Q3 Q4 MM
	 *                    Q1-Q4:freq (see setFreq)
	 *                       MM:mode (see setOperMode)
	 *  </pre>
	 **/
	public synchronized int getFreq()
	{
		byte[] cmd = { 0, 0, 0, 0, (byte)0x03 };
		return(unpackFreq(cmdLongResp(cmd))*10) ;
	}
	public synchronized JHTMode getMode()
	{
		byte[] cmd = { 0, 0, 0, 0, (byte)0x03 };
		byte[] resp = cmdLongResp(cmd);
		if (resp[4] == ((byte)0x04)) return(JHTMode.AM) ;else
		if (resp[4] == ((byte)0x08)) return(JHTMode.FM) ;else
		if (resp[4] == ((byte)0x00)) return(JHTMode.LSB);else
		if (resp[4] == ((byte)0x02)) return(JHTMode.CW) ;else
		if (resp[4] == ((byte)0x01)) return(JHTMode.USB);
		else
		{
		JHamTune.logger.warning("YaesuFT847.getMode(): Unknown mode"+
				   (int)resp[4]+"returned from radio");
		return(null);
		}
	}

	private synchronized byte[] doCmd(byte[] cmd, int respLength) {
		if (DEBUG) {
			JHamTune.logger.fine(
				"FT847 DEBUG: doCmd("+
				byteArrayDump(cmd)+
				" expecting "+
				respLength+
				" byte response."
				);
		}
				
		byte[] resp = new byte[respLength];
		try {
			checkSpill(); 	
			CATin.write(cmd);
			if (resp.length > 0) {
				try{Thread.sleep(cmdDelay);}
				catch(InterruptedException e) {}; 
				CATout.read(resp);
			}
			checkSpill();
			}
		catch (Exception e)
		{
		JHamTune.logger.severe("YaesuFT847.doCmd:"+e);
		System.exit(1);
		};
		if (DEBUG) {
			JHamTune.logger.fine(
				"DEBUG: doCmd response was "+
				byteArrayDump(resp)
				);
		};
		return resp;
	}
	
	private boolean checkSpill() throws java.io.IOException {
		int spill = CATout.available();
		if (spill > 0)
		{
		JHamTune.logger.warning("FT847: checkSpill: "+spill+" spilled bytes from "+name+":");
		byte[] spillage = new byte[spill];
		CATout.read(spillage);
		System.out.println(byteArrayDump(spillage));
		return true;
		}
		else return false;
	}
	
	private byte [] cmdLongResp(byte [] cmd)
	 
	{
		return doCmd(cmd,5);
	} 

	private byte [] cmdShortResp(byte [] cmd)
	{
		return doCmd(cmd,1);	
	} 

	private void cmdNoResp(byte [] cmd)
	{
		doCmd(cmd,0);
		return;
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
		return(port);
	}
	
	public String getName()
	{
		return(name+" on "+port);
	}
	
}

