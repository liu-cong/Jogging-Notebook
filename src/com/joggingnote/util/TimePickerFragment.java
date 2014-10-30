package com.joggingnote.util;

import java.util.Calendar;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TimePicker;

@SuppressLint("NewApi")
public class TimePickerFragment extends DialogFragment
implements TimePickerDialog.OnTimeSetListener {
	private Calendar calendar=Calendar.getInstance();
	Button button;//the button on which to show the picked time
	
	public void setArgument (Button button){
		this.button=button;
	}
	
	public  TimePickerFragment(){
		calendar=Calendar.getInstance();
	}
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		// Use the current time as the default values for the picker
		final Calendar c = Calendar.getInstance();
		int hour = c.get(Calendar.HOUR_OF_DAY);
		int minute = c.get(Calendar.MINUTE);
		calendar.set(1, 1, 1, hour, minute);
		// Create a new instance of TimePickerDialog and return it
		button.setText(DateManager.getTime(this.calendar));
		return new TimePickerDialog(getActivity(), this, hour, minute,
				true);
	}

	public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
		// Do something with the time chosen by the user
		synchronized (calendar) {
		calendar.set(1, 1, 1, hourOfDay, minute);
		}
		button.setText(DateManager.getTime(this.calendar));
	}
	
	public Calendar getCalendar(){
		synchronized (calendar) {
		return this.calendar;
		}
	}
}