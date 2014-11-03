package com.joggingnote.view;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.joggingnote.util.DateManager;
import com.joggingnote.util.DatePickerFragment;
import com.joggingnote.util.FirebaseCommunicator;
import com.joggingnote.util.TimePickerFragment;
import com.joggingnote.*;

import android.app.Activity;
import android.app.DialogFragment;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public class EditingPanelActivity extends Activity implements OnSeekBarChangeListener{
	private SeekBar bar; // declare seekbar object variable
	private TextView textDistance;//show the current distance corresponding to the seekbar position
	private String provider=null,userid=null,entryid=null;
	private double distance=0F;//the distance from the seekbar
	private Button button_time2,button_time1,button_date1,button_date2;//buttons to set up the date and time of the entry
	TimePickerFragment time1;
	TimePickerFragment time2;
	DatePickerFragment date1;
	DatePickerFragment date2;

	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);

		getWindow().requestFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(com.joggingnote.R.layout.activity_editingpanel);

		//retrieve data from previous activity
		Bundle extras=getIntent().getExtras();
		if(extras!=null){
			provider=extras.getString("provider");
			userid=extras.getString("userid");
			entryid=extras.getString("entryid");
		}
		
		bar = (SeekBar)findViewById(R.id.bar_distance); // make seekbar object
		bar.setOnSeekBarChangeListener(this); // set seekbar listener.
		textDistance = (TextView) findViewById(R.id.text_distance);
		
		button_date1=(Button) findViewById(R.id.button_date1);
		button_date1.setOnClickListener(new OnClickListener() {	
			@Override
			public void onClick(View v) {
				showDatePickerDialog(v, date1);
			}
		});

		button_date2=(Button) findViewById(R.id.button_date2);
		button_date2.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				showDatePickerDialog(v, date2);
			}
		});

		button_time1=(Button) findViewById(R.id.button_time1);
		button_time1.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				showTimePickerDialog(v, time1);
			}
		});

		button_time2=(Button) findViewById(R.id.button_time2);
		button_time2.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				showTimePickerDialog(v, time2);
			}
		});
		
		date1 = new DatePickerFragment();
		date2 = new DatePickerFragment();
		date1.setArgument(button_date1);
		date2.setArgument(button_date2);
		time1 = new TimePickerFragment();
		time2 = new TimePickerFragment();
		time1.setArgument(button_time1);
		time2.setArgument(button_time2);
		
		if(entryid==null){
			updateDisplay();//if adding a new entry, display the current date and time on the buttons. 
		}else{// else, display the time and date of the previous entry
			String firebaseUrl = getResources().getString(R.string.firebase_url);
			Firebase ref = new Firebase(firebaseUrl);
			Firebase usersRef=ref.child(provider+"Users").child(userid).child("Times").child(entryid);
			// retrieve the index associated to the date
			usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
				@Override
				public void onDataChange(DataSnapshot snapshot) {
					String date1=(String) snapshot.child("date1").getValue();
					String date2=(String) snapshot.child("date2").getValue();
					String time1=(String) snapshot.child("time1").getValue();
					String time2=(String) snapshot.child("time2").getValue();
					button_date1.setText(date1);
					button_date2.setText(date1);
					button_time2.setText(time1);
					button_time1.setText(time2);
				}
				@Override
				public void onCancelled(FirebaseError firebaseError) {
				}
			});

		}
	}

	//this method is associated with the "Add" button. it sends a time entry to firebase
	public void sendData(View v){
		String firebaseUrl = getResources().getString(R.string.firebase_url);
		Firebase ref = new Firebase(firebaseUrl);
		
		//First create the data according to the date,time and distance of the user input
		Calendar calendar1=time1.getCalendar();
		calendar1.set(date1.getCalendar().get(Calendar.YEAR), date1.getCalendar()
				.get(Calendar.MONTH),date1.getCalendar().get(Calendar.DAY_OF_MONTH));
		Calendar calendar2=time2.getCalendar();
		calendar2.set(date2.getCalendar().get(Calendar.YEAR), date2.getCalendar()
				.get(Calendar.MONTH),date2.getCalendar().get(Calendar.DAY_OF_MONTH));

		FirebaseCommunicator.sendData(ref, provider, userid, entryid, calendar1, calendar2, distance);
		super.onBackPressed();
		finish();
	}

	

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress,
			boolean fromUser) {
		// change progress text label with current seekbar value
		this.distance= (double)progress;
		textDistance.setText("Distance: "+String.valueOf(this.distance/1000)+" km");
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		seekBar.setSecondaryProgress(seekBar.getProgress());
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		finish();
	}

	public void showTimePickerDialog(View v, DialogFragment newFragment) {
		newFragment.show(getFragmentManager(), "timePicker");
	}

	public void showDatePickerDialog(View v, DialogFragment newFragment) {
		newFragment.show(getFragmentManager(), "datePicker");
	}
	
	//show the current date and time on each button
	public void updateDisplay(){
		if(button_date1!=null&&button_date2!=null&&button_time1!=null&&button_time2!=null)
		{
			synchronized (button_date1) {
				button_date1.setText(DateManager.getDate(date1.getCalendar()));
			}
			synchronized (button_date2) {
				button_date2.setText(DateManager.getDate(date2.getCalendar()));
			}
			synchronized (button_time1) {
				button_time1.setText(DateManager.getTime(time1.getCalendar()));
			}
			synchronized (button_time1) {
				button_time2.setText(DateManager.getTime(time2.getCalendar()));
			}
		}
	}

}
