package com.joggingnote.util;

//the class that holds all the information of one time entry (or record)
public class JoggingRecord {
	private String date1,date2,time1,time2;
	private long hours,minutes;
	private double distance;
	private double speed;
	private String entryid;
	private boolean visible=true;
	
	public void setVisibility(boolean b){this.visible=b;}
	public boolean getVisibility() {return this.visible;}
	public JoggingRecord (){}

	public JoggingRecord(String date2, long hours, long minutes, double distance, double speed){
		this.date2=date2;
		this.hours=hours;
		this.minutes=minutes;
		this.distance=distance;
		this.speed=speed;
		this.entryid=null;
	}
	public double getDistance(){return this.distance;}
	public double getSpeed(){return this.speed;}
	public String getEntryid(){return this.entryid;}
	public long getHours(){return this.hours;}
	public long getMinutes(){return this.minutes;}	
	public String getDate(){return this.date2;}
	public String getDate1(){return this.date1;}
	public String getDate2(){return this.date2;}
	public String getTime1(){return this.time1;}
	public String getTime2(){return this.time2;}
	
}
