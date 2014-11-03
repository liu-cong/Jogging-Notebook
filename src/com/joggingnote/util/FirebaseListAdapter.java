package com.joggingnote.util;

import android.app.Activity;
import android.provider.CallLog.Calls;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CalendarView;

import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.FirebaseError;
import com.firebase.client.Query;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * User: greg
 * Date: 6/21/13
 * Time: 1:47 PM
 */

/**
 * This class is a generic way of backing an Android ListView with a Firebase location.
 * It handles all of the child events at the given Firebase location. It marshals received data into the given
 * class type. Extend this class and provide an implementation of <code>populateView</code>, which will be given an
 * instance of your list item layout and an instance your class that holds your data. Simply populate the view however
 * you like and this class will handle updating the list as the data changes.
 * @param <T> The class type to use as a model for the data contained in the children of the given Firebase location
 */
public abstract class FirebaseListAdapter<T> extends BaseAdapter {

	protected int index_begin,index_end;//the start(inclusive) and end(inclusive) indexes of visible items
	protected int date_begin,date_end;//the start and end (inclusive) date of the entries

	protected boolean filterApplied;
	protected boolean weeklyView;
	private Query ref;
	private Class<T> modelClass;
	protected int layout;
	protected LayoutInflater inflater;
	protected List<T> models;
	private List<String> modelNames;
	private Map<String, T> modelNamesMapping;
	private ChildEventListener listener;

	protected List<ArrayList<ArrayList<T>>> weeklyModels;

	/**
	 * @param ref The Firebase location to watch for data changes. Can also be a slice of a location, using some
	 *            combination of <code>limit()</code>, <code>startAt()</code>, and <code>endAt()</code>,
	 * @param modelClass Firebase will marshall the data at a location into an instance of a class that you provide
	 * @param layout This is the layout used to represent a single list item. You will be responsible for populating an
	 *               instance of the corresponding view with the data from an instance of modelClass.
	 * @param activity The activity containing the ListView
	 */
	public FirebaseListAdapter(Query ref, Class<T> modelClass, int layout, Activity activity) {
		this.ref = ref;
		this.modelClass = modelClass;
		this.layout = layout;
		inflater = activity.getLayoutInflater();
		models = new ArrayList<T>();
		modelNames= new ArrayList<String> ();
		modelNamesMapping = new HashMap<String, T>();
		this.filterApplied=false;
		this.weeklyView=false;


		// Look for all child events. We will then map them to our own internal ArrayList, which backs ListView
		listener = this.ref.addChildEventListener(new ChildEventListener() {
			@Override
			public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {

				T model = dataSnapshot.getValue(FirebaseListAdapter.this.modelClass);
				String currentChildName=dataSnapshot.getName();
				modelNamesMapping.put(currentChildName, model);


				// Insert into the correct location, based on previousChildName
				int nextIndex=0;
				if (previousChildName != null) {
					T previousModel = modelNamesMapping.get(previousChildName);
					int previousIndex = models.indexOf(previousModel);
					nextIndex = previousIndex + 1;
				}
				models.add(nextIndex, model);
				modelNames.add(nextIndex, currentChildName);

				if(filterApplied){
					if(!(index_begin<=index_end&&index_end<models.size()&&index_begin>=0)){
						index_begin=findBeginIndex();
						index_end=findEndIndex();
					} else{
						int currentDate=Integer.parseInt(currentChildName.substring(0,8));
						if(currentDate>=date_begin&&currentDate<=date_end)//within the filter range
						{
							index_end++;
						}
						else if(currentDate<date_begin)
						{
							index_begin++;
							index_end++;
						}
						else {;}//do nothing
					}
				}

				notifyDataSetChanged();
			}

			@Override
			public void onChildChanged(DataSnapshot dataSnapshot, String s) {

				// One of the models changed. Replace it in our list and name mapping
				String modelName = dataSnapshot.getName();
				T oldModel = modelNamesMapping.get(modelName);
				T newModel = dataSnapshot.getValue(FirebaseListAdapter.this.modelClass);
				int index = models.indexOf(oldModel);
				models.set(index, newModel);
				modelNames.set(index, modelName);
				modelNamesMapping.put(modelName, newModel);
				notifyDataSetChanged();
			}

			@Override
			public void onChildRemoved(DataSnapshot dataSnapshot) {

				// A model was removed from the list. Remove it from our list and the name mapping
				String modelName = dataSnapshot.getName();
				T oldModel = modelNamesMapping.get(modelName);
				models.remove(oldModel);
				modelNames.remove(modelName);
				modelNamesMapping.remove(modelName);

				if(filterApplied){
					if(!(index_begin<=index_end&&index_end<models.size()&&index_begin>=0)){
						index_begin=findBeginIndex();
						index_end=findEndIndex();
					} else{
						int currentDate=Integer.parseInt(modelName.substring(0,8));
						if(currentDate>=date_begin&&currentDate<=date_end)//within the filter range
						{
							index_end--;
						}
						else if(currentDate<date_begin)
						{
							index_begin--;
							index_end--;
						}
						else {;}//do nothing
					}
				}
				notifyDataSetChanged();
			}

			@Override
			public void onChildMoved(DataSnapshot dataSnapshot, String previousChildName) {

				// A model changed position in the list. Update our list accordingly
				String modelName = dataSnapshot.getName();
				T oldModel = modelNamesMapping.get(modelName);
				T newModel = dataSnapshot.getValue(FirebaseListAdapter.this.modelClass);
				int index = models.indexOf(oldModel);
				models.remove(index);
				modelNames.remove(index);

				int nextIndex=0;
				if (previousChildName != null) {
					T previousModel = modelNamesMapping.get(previousChildName);
					int previousIndex = models.indexOf(previousModel);
					nextIndex = previousIndex + 1;
				}
				models.add(nextIndex, newModel);
				modelNames.add(nextIndex, modelName);
				notifyDataSetChanged();
			}
			@Override
			public void onCancelled(FirebaseError arg0) {
				// TODO Auto-generated method stub
				Log.e("FirebaseListAdapter", "Listen was cancelled, no more updates will occur");
			}
		});
	}

	public void cleanup() {
		// We're being destroyed, let go of our listener and forget about all of the models
		ref.removeEventListener(listener);
		models.clear();
		modelNames.clear();
		modelNamesMapping.clear();
	}

	/*
	 * The getCount method, along with the getItem and getView method need to be overriden. These three methods
	 * can handle different conditions (1. display all data entryies. 2. if a weekly view is required, group entries
	 * by week and 3. if a date filter is applied, only display those entry within the filter.
	 * */
	@Override
	public int getCount() {
		if(weeklyView){
			createWeeklyList();
			if(weeklyModels==null) return 0;
			int count=0;
			for(int i=0;i<weeklyModels.size();i++)
				count+=weeklyModels.get(i).size();
			return count;
		}
		
		if(filterApplied)
		{
		if(index_begin<=index_end&&index_end<models.size()&&index_begin>=0)
			return index_end-index_begin+1;
		else return 0;
		}
		else
			return models.size();
	}

	//this method is overriden in the child class JoggingRecordListAdapter as it requires more information form Class T
	@Override
	public Object getItem(int i) {
		if(filterApplied)
		{
			if(index_begin<=index_end&&index_end<models.size()&&index_begin>=0)
				return models.get(index_end-i);
			else	return null;
		}
		else 
			return models.get(models.size()-1-i);
	}


	@Override
	public long getItemId(int i) {
		return i;
	}

	@Override
	public View getView(int i, View view, ViewGroup viewGroup) {
		if (view == null) {
			view = inflater.inflate(layout, viewGroup, false);
		}

		@SuppressWarnings("unchecked")
		T model = (T) getItem(i);
		// Call out to subclass to marshall this model into the provided view
		populateView(view, model);
		return view;

	}

	//set up a date filter for the ListAdapter
	public boolean setFilter(int date_begin,int date_end){
		if(date_end<date_begin) return false;
		this.date_begin=date_begin;
		this.date_end=date_end;
		this.index_begin=findBeginIndex();
		this.index_end=findEndIndex();
		this.filterApplied=true;
		notifyDataSetChanged();
		notifyDataSetInvalidated();
		return true;
	}

	//removes the date filter
	public void clearFilter(){
		this.filterApplied=false;
		index_begin=0;
		index_end=models.size()-1;
		notifyDataSetChanged();
		notifyDataSetInvalidated();
		return;
	}

	//set up a weekly view
	public void setWeeklyView(){
		this.weeklyView=true;
		notifyDataSetChanged();
		notifyDataSetInvalidated();
	}

	//restore to daily view
	public void clearWeeklyView(){
		this.weeklyView=false;
		notifyDataSetChanged();
		notifyDataSetInvalidated();
	}

	//group the data entries by week and create a new List to hold the new weekly data
	public void createWeeklyList(){
		ArrayList<T> days=new ArrayList<T>();
		ArrayList<ArrayList<T>> weeks=new ArrayList<ArrayList<T>> ();
		List<ArrayList<ArrayList<T>>> years=new ArrayList<ArrayList<ArrayList<T>>> ();
		if(models.size()!=0) days.add(models.get(models.size()-1));
		else return;
		for(int i=modelNames.size()-2;i>=0;i--){
			//check the new entry
			Calendar pre=DateManager.getCalendar(modelNames.get(i+1));
			Calendar cur=DateManager.getCalendar(modelNames.get(i));
			if (pre.get(Calendar.YEAR)==cur.get(Calendar.YEAR)){
				if(pre.get(Calendar.WEEK_OF_YEAR)==cur.get(Calendar.WEEK_OF_YEAR)){
					days.add(models.get(i));
				}
				else{
					weeks.add(days);
					days=new ArrayList<T>();
					days.add(models.get(i));
				}
			}
			else{
				weeks.add(days);
				years.add(weeks);
				days=new ArrayList<T>();
				days.add(models.get(i));
				weeks=new ArrayList<ArrayList<T>> ();
			}
		}
		weeks.add(days);
		years.add(weeks);
		weeklyModels=years;

	}


	//find the begin Index of the entries to display once a date filter is set up
	//because the entries are sorted on date, theirfore the indexes of the entries to
	//be displayed are consecutive and only a begin and end index are required
	private int findBeginIndex(){
		int begin=Integer.MAX_VALUE;
		for(int i=0;i<modelNames.size();i++){
			if(Integer.parseInt(modelNames.get(i).substring(0,8))>=this.date_begin)
			{begin=i;break;}
		}
		return begin;
	}
	//find the end Index of the entries to display once a date filter is set up
	private int findEndIndex(){
		int end=models.size()-1;
		for(int i=0;i<modelNames.size();i++){
			if(Integer.parseInt(modelNames.get(i).substring(0,8))>this.date_end)
			{end=i-1;break;}
		}
		return end;
	}
	/**
	 * Each time the data at the given Firebase location changes, this method will be called for each item that needs
	 * to be displayed. The arguments correspond to the layout and modelClass given to the constructor of this class.
	 *
	 * Your implementation should populate the view using the data contained in the model.
	 * @param v The view to populate
	 * @param model The object containing the data used to populate the view
	 */
	protected abstract void populateView(View v, T model);
}