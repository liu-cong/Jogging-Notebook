package com.joggingnote.util;

import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.List;
import android.app.Activity;
import android.view.View;
import android.widget.TextView;
import com.firebase.client.Query;
import com.joggingnote.R;

//this class extends the generic FirebaseListAdapter class and displays the JoggingRecord items
public class JoggingRecordListAdapter extends FirebaseListAdapter<JoggingRecord> {

	public JoggingRecordListAdapter(Query ref, Activity activity, int layout) {
		super(ref, JoggingRecord.class, layout, activity);
	}

	@Override
	public JoggingRecord getItem(int i) {
		if(weeklyView){
			createWeeklyList();
			int j=-1;
			for(int m=0;m<weeklyModels.size();m++){
				for(int n=0;n<weeklyModels.get(m).size();n++){
					j++;
					if(j==i){
						return average(weeklyModels.get(m).get(n));
					}		
				}
			}
		}

		if(filterApplied)
		{
			if(index_begin<=index_end&&index_end<models.size()&&index_begin>=0)
				return models.get(index_end-i);
			else	return null;
		}
		else 
			return models.get(models.size()-1-i);
	}


	//Returns a new JoggingRecord object with the average speed, overall time and distance of the times in one week. 
	public JoggingRecord average (List<JoggingRecord> list){
		if(list==null||list.size()==0) return null;
		long hours=0;
		long minutes=0;
		double distance=0;
		double speed=0;
		String date2= String.valueOf(DateManager.getCalendar(list.get(0).getDate()).get(Calendar.YEAR))+" Week:"
				+String.valueOf(DateManager.getCalendar(list.get(0).getDate()).get(Calendar.WEEK_OF_YEAR));

		for(int i=0;i<list.size();i++){
			JoggingRecord record=list.get(i);
			hours+=record.getHours();
			minutes+=record.getMinutes();
			distance+=record.getDistance();
		}
		hours+=minutes/60;
		minutes%=60;
		long seconds=hours*3600+minutes*60;
		speed=distance*1000/(double)seconds;
		return new JoggingRecord(date2,hours,minutes,distance,speed);
	}


	/**
	 * Bind an instance of the <code>Chat</code> class to our view. This method is called by <code>FirebaseListAdapter</code>
	 * when there is a data change, and we are given an instance of a View that corresponds to the layout that we passed
	 * to the constructor, as well as a single <code>Chat</code> instance that represents the current data to bind.
	 * @param view A view instance corresponding to the layout we passed to the constructor.
	 * @param chat An instance representing the current state of a chat message
	 */
	@Override
	protected void populateView(View view, JoggingRecord record) {
		if(record!=null){
			// Map a Chat object to an entry in our listview
			String date = record.getDate();
			double distance = record.getDistance();
			double speed = record.getSpeed();
			String duration = record.getHours()+":"+record.getMinutes();
			String entryid = record.getEntryid();

			TextView dateText = (TextView)view.findViewById(R.id.date);
			TextView durationText = (TextView)view.findViewById(R.id.duration);
			TextView speedText = (TextView)view.findViewById(R.id.speed);
			TextView distanceText = (TextView)view.findViewById(R.id.distance);
			TextView idText = (TextView)view.findViewById(R.id.entryid);

			DecimalFormat df = new DecimalFormat("#.##");
			dateText.setText(date);
			durationText.setText(duration);
			speedText.setText(df.format(speed));
			distanceText.setText(df.format(distance));
			idText.setText(entryid);
		}
	}
}
