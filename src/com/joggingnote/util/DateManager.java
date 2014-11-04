package com.joggingnote.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

//this class is responsible for date conversions
public class DateManager {

	public static String getDate(Calendar c){
		String date="";
		String year=String.valueOf(c.get(Calendar.YEAR));
		for(int i=0;i<4-year.length();i++) year="0"+year;
		
		String month=String.valueOf(c.get(Calendar.MONTH)+1);
		for(int i=0;i<2-month.length();i++) month="0"+month;
		
		String day=String.valueOf(c.get(Calendar.DAY_OF_MONTH));
		for(int i=0;i<2-day.length();i++) day="0"+day;
		
		date=date+year+"-";
		date=date+month+"-";
		date+=day;
		return date;
	}

	public static String getTime (Calendar c){
		String time="";
		time=time+c.get(Calendar.HOUR_OF_DAY)+":";
		time=time+c.get(Calendar.MINUTE);
		return time;
	}
	
	public static Calendar getCalendar(String date){
		Calendar c= Calendar.getInstance();
		c.set(Integer.parseInt(date.substring(0,4)),Integer.parseInt(date.substring(5,7))-1,Integer.parseInt(date.substring(8,10)));
		return c;
	}
	
	public static String convert2DisplayDate(String numDate){
		return numDate.substring(0,4)+"-"+numDate.substring(4,6)+"-"+numDate.substring(6,8);
	}
}
