package com.joggingnote.test;

import java.util.Calendar;

import com.joggingnote.util.DateManager;

import junit.framework.TestCase;

public class DateManagerTest extends TestCase {

	private final int YEAR=2014;
	private final int MONTH=10;
	private final int DAY=1;

	private final int HOUR=5;
	private final int MIN=30;
	protected void setUp() throws Exception {
		super.setUp();

}

public void testGetDate() {
	Calendar c= Calendar.getInstance();
	c.set(YEAR,MONTH,DAY,HOUR,MIN);
	String actual = DateManager.getDate(c);
	String expected = "2014-11-01";
	assertEquals(expected, actual);
}

public void testGetTime() {
	Calendar c= Calendar.getInstance();
	c.set(YEAR,MONTH,DAY,HOUR,MIN);
	String actual = DateManager.getTime(c);
	String expected = String.valueOf(HOUR)+":"+String.valueOf(MIN);
	assertEquals(expected, actual);
	
}

public void testGetCalendar() {
	Calendar c= Calendar.getInstance();
	c.set(YEAR,MONTH,DAY,HOUR,MIN);
	String date=DateManager.getDate(c);
	Calendar c_test = DateManager.getCalendar(date);
	
	assertEquals(c.get(Calendar.YEAR),c_test.get(Calendar.YEAR));
	assertEquals(c.get(Calendar.MONTH),c_test.get(Calendar.MONTH));
	assertEquals(c.get(Calendar.DAY_OF_MONTH),c_test.get(Calendar.DAY_OF_MONTH));
}

}
