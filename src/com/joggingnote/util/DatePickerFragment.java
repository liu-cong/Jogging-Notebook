package com.joggingnote.util;

import java.util.Calendar;
import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.widget.Button;
import android.widget.DatePicker;

@SuppressLint("NewApi")
public class DatePickerFragment extends DialogFragment
implements DatePickerDialog.OnDateSetListener {
	private Calendar calendar=Calendar.getInstance();
	private Button button;
	
	
	public void setArgument (Button button){
		this.button=button;
	}
	
	public  DatePickerFragment(){
		calendar=Calendar.getInstance();
	}
	@SuppressLint("NewApi")
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		// Use the current date as the default date in the picker
		final Calendar c = Calendar.getInstance();
		int year = c.get(Calendar.YEAR);
		int month = c.get(Calendar.MONTH);
		int day = c.get(Calendar.DAY_OF_MONTH);
		calendar.set(year, month, day);
		button.setText(DateManager.getDate(this.calendar));
		// Create a new instance of DatePickerDialog and return it
		return new DatePickerDialog(getActivity(), this, year, month, day);
	}

	public void onDateSet(DatePicker view, int year, int month, int day) {
		// Do something with the date chosen by the user
		synchronized (calendar) {
			calendar.set(year, month, day);
		}
		button.setText(DateManager.getDate(this.calendar));
	}

	public Calendar getCalendar(){
		synchronized (calendar) {
		return this.calendar;
		}
	}
}