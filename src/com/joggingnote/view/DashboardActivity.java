package com.joggingnote.view;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.joggingnote.R;
import com.joggingnote.util.DatePickerFragment;
import com.joggingnote.util.FirebaseCommunicator;
import com.joggingnote.util.JoggingRecord;
import com.joggingnote.util.JoggingRecordListAdapter;
import com.joggingnote.util.TimePickerFragment;

import android.app.DialogFragment;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
/*
 * This activity is the main view that shows the jogging entries. It uses a ListView to display
 * the entries. The OptionsMenu is also provided for more options.
 * */
public class DashboardActivity extends ListActivity {
	/* A reference to the firebase */
	private Firebase ref;
	private String provider;//provider of authentication (Facebook, Google, etc.)
	private String userid;//the user ID 
	private String entryid;//the id of the items that the user selects to edit

	private JoggingRecordListAdapter joggingRecordListAdapter;//the ListView to display the times

	private String[] menuItems;//the menu options of the Context menu
	ListView listView;

	int date_begin=1,date_end=0;//the begin and end date to filter the times
	boolean filterApplied;
	boolean weeklyView;

	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		menuItems=new String[]{"Edit","Delete"};
		filterApplied=false;
		weeklyView=false;

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.activity_dashboard);

		//get a reference to firebase
		String firebaseUrl = getResources().getString(R.string.firebase_url);
		Firebase.setAndroidContext(getApplicationContext());
		ref = new Firebase(firebaseUrl);


		//retrieve authentication data from MainActivity
		Bundle extras=getIntent().getExtras();
		if(extras!=null){
			provider=extras.getString("provider");
			userid=extras.getString("id");
			entryid=null;
		}
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		if (v.getId()==android.R.id.list) {
			AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)menuInfo;
			TableLayout table = (TableLayout)info.targetView;
			table.setBackgroundColor(0xFF73d49c);
			TableRow row = (TableRow)(table.getChildAt(0));
			TextView text= (TextView) row.getChildAt(0);
			TextView date=(TextView) row.getChildAt(1);
			entryid=text.getText().toString();
			String title= date.getText().toString()+" NO."+entryid.charAt(10);
			menu.setHeaderTitle(title);
			for (int i = 0; i<menuItems.length; i++) {
				menu.add(Menu.NONE, i, i, menuItems[i]);
			}
		}

	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
		TableLayout table = (TableLayout)info.targetView;
		table.setBackgroundColor(0xFFE8AC6F);
		int menuItemIndex = item.getItemId();
		String menuItemName = menuItems[menuItemIndex];
		if(menuItemName.equals("Delete")){
			//delete this entry
			Firebase timeRef=ref.child(provider+"Users").child(userid).child("Times");
			if(entryid!=null&&entryid.length()!=0)
				FirebaseCommunicator.deleteData(timeRef, entryid);
		}

		if(menuItemName.equals("Edit")){
			//edit this entry
			startEditActivity();
		}
		return true;
	}

	@Override
	public void onStart(){
		super.onStart();
		listView = getListView();//get a reference to the ListView
		registerForContextMenu(listView);//register a Context Memu for the ListView (for deleting and editing times)
		Firebase timeRef=ref.child(provider+"Users").child(userid).child("Times");
		joggingRecordListAdapter = new JoggingRecordListAdapter(timeRef, this, R.layout.jogging_record);
		listView.setAdapter(joggingRecordListAdapter);
		joggingRecordListAdapter.registerDataSetObserver(new DataSetObserver() {
			@Override
			public void onChanged(){
				super.onChanged();
				listView.setSelection(joggingRecordListAdapter.getCount()-1);
			}
		});
	}

	@Override
	public void onResume(){//refresh the ListView after returned from FilterPanelActivity
		super.onResume();
		
		
		if(weeklyView)
		{
			joggingRecordListAdapter.setWeeklyView();
		}
		
		else{
			joggingRecordListAdapter.clearWeeklyView();
		}
		
		if(filterApplied)
		{
			joggingRecordListAdapter.setFilter(date_begin, date_end);
		}
		
		else{
			joggingRecordListAdapter.clearFilter();
		}
	}


	/*
	 * Activity for adding a new time entry
	 * */
	public void startNewActivity(View v){
		entryid=null;
		startActivity(createIntent(EditingPanelActivity.class));	
	}

	/*
	 * Activity for adding a new entry or editing an existing entry 
	 * (if adding a new entry, a null entryid will be send to the EditingPanelActivity.
	 * EditingPanelActivity will decide whether to add or edit an entry based on entryid
	 * */
	public void startEditActivity(){
		startActivity(createIntent(EditingPanelActivity.class));	
	}

	/*
	 * Activity for setting up a date filter
	 * */
	public void startFilterActivity(){
		entryid=null;
		startActivityForResult(createIntent(FilterPanelActivity.class), 1);	
	}

	public Intent createIntent(Class<?> activity){
		Intent intent=new Intent(getApplicationContext(),activity);
		intent.putExtra("provider", provider);
		intent.putExtra("userid", userid);
		intent.putExtra("entryid", entryid);
		return intent;
	}
	
	public void showTimePickerDialog(View v) {
		DialogFragment newFragment = new TimePickerFragment();
		newFragment.show(getFragmentManager(), "timePicker");
	}

	public void showDatePickerDialog(View v) {
		DatePickerFragment newFragment = new DatePickerFragment();
		newFragment.show(getFragmentManager(), "datePicker");
	}


	/*
	 * Override the option menu
	 * */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_dashboard, menu);
		return true;
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_clear:
			Log.e("filter",String.valueOf(filterApplied));
			filterApplied=false;
			joggingRecordListAdapter.clearFilter();
			Log.e("filter",String.valueOf(filterApplied));
			break;
		case R.id.menu_add:
			startEditActivity();
			break;
		case R.id.menu_filter:
			startFilterActivity();
			break;
		case R.id.menu_weekly_view:
			if(weeklyView){
				weeklyView=false;
				joggingRecordListAdapter.clearWeeklyView();
			}
			else{
				weeklyView=true;
				joggingRecordListAdapter.setWeeklyView();
			}
			break;
		}
		return super.onOptionsItemSelected(item);
	}


	/*
	 * get information from the FilterPanelActivity
	 * if a valid date filter is created, the begin and end date of the filter will be sent back 
	 * */
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent settings) {//receive user input from SettingsActivity
		super.onActivityResult(requestCode, resultCode, settings);
		int duration = Toast.LENGTH_SHORT;
		CharSequence text;
		Context context = getApplicationContext();
		if(resultCode==0){
			text = "Invalid date filter, please re-create the filter.";
			Toast toast = Toast.makeText(context, text, duration);
			toast.show();
		}
		if(resultCode==1)
		{
			this.date_begin=settings.getIntExtra("date_begin", 1);
			this.date_end=settings.getIntExtra("date_end",0);
			filterApplied=true;
			text="Filter applied!";	
			Toast toast = Toast.makeText(context, text, duration);
			toast.show();
		}

	}

}
