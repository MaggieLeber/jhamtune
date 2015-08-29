package com.k3xs.JHamTune;
/**
 * @author Margaret Leber k3xs@arrl.net
 * @version 1.0
 **/
import java.awt.*;
import java.io.*;
import javax.swing.*;
/**
 * encapsulates CTCSS (Motorola PL tones) as a typesafe enum.
 * currently butt-ugly to support bean pattern for XMLDecoder/Encoder
 * Should be converted to enum for Java 1.5 and above
 */ 
public class JHTCTCSS implements Serializable
{
	private String code;
	private int freq;
	public static final JHTCTCSS none = new JHTCTCSS("__",0);
	public static final JHTCTCSS plXZ = new JHTCTCSS("XZ",670);
	public static final JHTCTCSS plWZ = new JHTCTCSS("WZ",693);
	public static final JHTCTCSS plXA = new JHTCTCSS("XA",719);
	public static final JHTCTCSS plWA = new JHTCTCSS("WA",744);
	public static final JHTCTCSS plXB = new JHTCTCSS("XB",770);
	public static final JHTCTCSS plWB = new JHTCTCSS("WB",797);
	public static final JHTCTCSS plYZ = new JHTCTCSS("YZ",825);
	public static final JHTCTCSS plYA = new JHTCTCSS("YA",854);
	public static final JHTCTCSS plYB = new JHTCTCSS("YB",885);
	public static final JHTCTCSS plZZ = new JHTCTCSS("ZZ",915);
	public static final JHTCTCSS plZA = new JHTCTCSS("ZA",948);
	public static final JHTCTCSS plZB = new JHTCTCSS("ZB",974);
	public static final JHTCTCSS pl1Z = new JHTCTCSS("1Z",1000);
	public static final JHTCTCSS pl1A = new JHTCTCSS("1A",1035);
	public static final JHTCTCSS pl1B = new JHTCTCSS("1B",1072);
	public static final JHTCTCSS pl2Z = new JHTCTCSS("2Z",1109);
	public static final JHTCTCSS pl2A = new JHTCTCSS("2A",1148);
	public static final JHTCTCSS pl2B = new JHTCTCSS("2B",1188);
	public static final JHTCTCSS pl3Z = new JHTCTCSS("3Z",1230);
	public static final JHTCTCSS pl3A = new JHTCTCSS("3A",1273);
	public static final JHTCTCSS pl3B = new JHTCTCSS("3B",1318);
	public static final JHTCTCSS pl4Z = new JHTCTCSS("4Z",1365);
	public static final JHTCTCSS pl4A = new JHTCTCSS("4A",1413);
	public static final JHTCTCSS pl4B = new JHTCTCSS("4B",1462);
	public static final JHTCTCSS pl5Z = new JHTCTCSS("5Z",1514);
	public static final JHTCTCSS pl5A = new JHTCTCSS("5A",1567);
	public static final JHTCTCSS pl5B = new JHTCTCSS("5B",1622);
	public static final JHTCTCSS pl6Z = new JHTCTCSS("6Z",1679);
	public static final JHTCTCSS pl6A = new JHTCTCSS("6A",1738);
	public static final JHTCTCSS pl6B = new JHTCTCSS("6B",1799);
	public static final JHTCTCSS pl7Z = new JHTCTCSS("7Z",1862);
	public static final JHTCTCSS pl7A = new JHTCTCSS("7A",1928);
	public static final JHTCTCSS plM1 = new JHTCTCSS("M1",2035);
	public static final JHTCTCSS pl8Z = new JHTCTCSS("8Z",2065);
	public static final JHTCTCSS plM2 = new JHTCTCSS("M2",2107);
	public static final JHTCTCSS plM3 = new JHTCTCSS("M3",2181);
	public static final JHTCTCSS plM4 = new JHTCTCSS("M4",2257);
	public static final JHTCTCSS pl9Z = new JHTCTCSS("9Z",2291);
	public static final JHTCTCSS plM5 = new JHTCTCSS("M5",2336);
	public static final JHTCTCSS plM6 = new JHTCTCSS("M6",2148);
	public static final JHTCTCSS plM7 = new JHTCTCSS("M7",2503);
	private static JHTCTCSS [] all = {
			JHTCTCSS.none,
			JHTCTCSS.plXZ ,  
			JHTCTCSS.plWZ ,  
			JHTCTCSS.plXA ,  
			JHTCTCSS.plWA ,  
			JHTCTCSS.plXB ,  
			JHTCTCSS.plWB ,  
			JHTCTCSS.plYZ ,  
			JHTCTCSS.plYA ,  
			JHTCTCSS.plYB ,  
			JHTCTCSS.plZZ ,  
			JHTCTCSS.plZA ,  
			JHTCTCSS.plZB ,  
			JHTCTCSS.pl1Z ,  
			JHTCTCSS.pl1A ,  
			JHTCTCSS.pl1B ,  
			JHTCTCSS.pl2Z ,  
			JHTCTCSS.pl2A ,  
			JHTCTCSS.pl2B ,  
			JHTCTCSS.pl3Z ,  
			JHTCTCSS.pl3A ,  
			JHTCTCSS.pl3B ,  
			JHTCTCSS.pl4Z ,  
			JHTCTCSS.pl4A ,  
			JHTCTCSS.pl4B ,  
			JHTCTCSS.pl5Z ,  
			JHTCTCSS.pl5A ,  
			JHTCTCSS.pl5B ,  
			JHTCTCSS.pl6Z ,  
			JHTCTCSS.pl6A ,  
			JHTCTCSS.pl6B ,  
			JHTCTCSS.pl7Z ,  
			JHTCTCSS.pl7A ,  
			JHTCTCSS.plM1 ,  
			JHTCTCSS.pl8Z ,  
			JHTCTCSS.plM2 ,  
			JHTCTCSS.plM3 ,  
			JHTCTCSS.plM4 ,  
			JHTCTCSS.pl9Z ,  
			JHTCTCSS.plM5 ,  
			JHTCTCSS.plM6 ,  
			JHTCTCSS.plM7  
	 };
	

public static JComboBox getComboBox(JHTCTCSS select)
   {
    JComboBox box = new JComboBox();
    JHTCTCSS [] all = JHTCTCSS.getAll();
    for ( int i =0; i< all.length; i++) box.addItem(all[i]);
    return(box);
   } 

 private JHTCTCSS(String code, int freq){
		this.freq = freq;
		this.code = code; }
		
public JHTCTCSS() { 
	this.freq = 0;
	this.code = "__";
}

public void setFreq(int f){
	this.code = "__";
	this.freq = 0;
	for (int i = 0; i<all.length; i++)
	{
		if (all[i].getFreq() >= f) { 
		this.code = all[i].getCode();
		this.freq = all[i].getFreq();
		break; };
	}
}
public String toString()
        {
         if (this== none)
                   return("none");
              else return (freq/10.0)+"("+code+")";      
        }

public String getCode(){return code;}
public int    getFreq(){return freq;}

public static JHTCTCSS [] getAll() { 
	    return(all);   
    }
}
