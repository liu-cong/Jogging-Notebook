package com.joggingnote.view;

import java.util.Calendar;
import com.joggingnote.util.DateManager;
import com.joggingnote.util.DatePickerFragment;
import com.joggingnote.*;
import android.app.DialogFragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.Button;


/*
 * This activity has two date pickers to let the users set the begin and end dates of the filter.
 * The begin and end dates are then sent back to the DashBoardActivity for it to update display.
 * */
public class FilterPanelActivity extends ActionBarActivity {
	DatePickerFragment date1;
	DatePickerFragment date2;
	Button button_date1,button_date2;//these two buttons are used to pick the begin and end dates

	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		getWindow().requestFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(com.joggingnote.R.layout.activity_filterpanel);
		
		button_date1=(Button) findViewById(R.id.button_date1_filter);
		button_date1.setOnClickListener(new OnClickListener() {	
			@Override
			public void onClick(View v) {
				showDatePickerDialog(v, date1);
			}
		});
		
		button_date2=(Button) findViewById(R.id.button_date2_filter);
		button_date2.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				showDatePickerDialog(v, date2);
			}
		});
		
		date1 = new DatePickerFragment();
		date2 = new DatePickerFragment();
		date1.setArgument(button_date1);//give DatePickerFragment a reference to the button 
		                                //so it can update the button text once a new date is set
		date2.setArgument(button_date2);
		
		updateDisplay();//show the current date on the buttons 
	}

	//send data to firebase
	public void filterData(View v){
		//First create the data according to the date,time and distance of the user input
		Calendar calendar1=date1.getCalendar();
		Calendar calendar2=date2.getCalendar();	
		String date1=DateManager.getDate(calendar1);
		String date2=DateManager.getDate(calendar2);
		date1=date1.substring(0,4)+date1.substring(5,7)+date1.substring(8,10);
		date2=date2.substring(0,4)+date2.substring(5,7)+date2.substring(8,10);
		int date_begin=Integer.parseInt(date1);
		int date_end=Integer.parseInt(date2);

		//send begin and end date back to DashBoardActivity
		Intent settings=new Intent();
		int resultcode=0;
		if(date_begin<=date_end)//if the filter is valid, set resultcode=1
			resultcode=1;
		settings.putExtra("date_begin",date_begin);
		settings.putExtra("date_end",date_end);
		setResult(resultcode,settings);
		finish();
		
		super.onBackPressed();
	}


	public void updateDisplay(){
		if(button_date1!=null&&button_date2!=null)
		{
			synchronized (button_date1) {
				button_date1.setText(DateManager.getDate(date1.getCalendar()));
			}
			synchronized (button_date2) {
				button_date2.setText(DateManager.getDate(date2.getCalendar()));
			}
		}
	}

	@Override
	public void onBackPressed() {
		Intent settings=new Intent();
		int resultcode=-1;
		setResult(resultcode,settings);
		super.onBackPressed();
		finish();
	}
	
	public void showDatePickerDialog(View v, DialogFragment newFragment) {
		newFragment.show(getFragmentManager(), "datePicker");
	}


}
