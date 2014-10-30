package com.joggingnote.util;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

/*
 * this class is responsible for communicating with fire base 
 * (save and delete data, retriving is done by FirebaseListAdapter class)
 * */
public class FirebaseCommunicator {

	//send data to firebase (if updating an existing entry, first delete it and add a new one)
	public static void sendData(Firebase ref, String provider, String userid, String entryid, Calendar calendar1, Calendar calendar2, double distance){
		if(provider!=null&&userid!=null)
		{				
			Firebase usersRef=ref.child(provider+"Users").child(userid);
			long seconds=(calendar2.getTimeInMillis()-calendar1.getTimeInMillis())/1000;
			long hours=(seconds/3600);
			long mins=(seconds%3600/60);
			double avgSpeed=distance/seconds;
			String date1=DateManager.getDate(calendar1);
			String date2=DateManager.getDate(calendar2);
			String time1=DateManager.getTime(calendar1);
			String time2=DateManager.getTime(calendar2);

			//put all the data into a map and then set the map to the Firebase reference
			Map<String,Object> record = new HashMap<String, Object> ();
			record.put("date1", date1);
			record.put("time1", time1);
			record.put("date2", date2);
			record.put("time2", time2);
			record.put("hours",hours);
			record.put("minutes", mins);
			record.put("distance", distance/1000);
			record.put("speed", avgSpeed);

			if(entryid==null)//this means the user wants to create a new entry rather than edit an existing one
			{
				addData(usersRef,record, date2);
			}
			else//id!=null. need to delete the entry corresponding to this id and create a new one
			{
				deleteData(usersRef.child("Times"),entryid);
				addData(usersRef,record, date2);
			}
		}
	}

	//add a new record to firebase
	private static  void addData(final Firebase ref, final Map<String, Object> record, final String date){
		final Firebase dateRef=ref.child("dateIDMapping").child(date);
		// retrieve the index associated to the date 
		//because a user may create multiple entries for one day, different indexes are used to 
		//differentiate these entries on the same day
		dateRef.addListenerForSingleValueEvent(new ValueEventListener() {
			@Override
			public void onDataChange(DataSnapshot snapshot) {
				Long index=(Long) snapshot.getValue();
				if(index==null||index==-1L){//a new date has to be added to Firebase
					index=100L;
					dateRef.setValue(index);
				}
				else{//if this date already exists (multiple times at the same day)
					index++;//create a new index by incrementing the previous index
					index=index>999L?100L:index;
					dateRef.setValue(index);
				}

				Map<String, Object> aUser=new HashMap<String,Object>();
				String newID= date.substring(0,4)+date.substring(5,7)+date.substring(8,10)+String.valueOf(index);
				record.put("entryid", newID);
				aUser.put(newID,record);
				Firebase usersRef=ref.child("Times");
				usersRef.updateChildren(aUser);
			}
			@Override
			public void onCancelled(FirebaseError firebaseError) {
			}
		});
	}


	//delete a time entry for the given entryid from Firebase
	public static void deleteData(Firebase ref, String entryid){
		Firebase child=ref.child(entryid);
		child.removeValue();
	}
}
