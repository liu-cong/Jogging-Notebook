package com.joggingnote.test;

import com.joggingnote.R;
import com.joggingnote.view.EditingPanelActivity;

import android.app.Instrumentation;
import android.test.ActivityInstrumentationTestCase2;
import android.test.UiThreadTest;
import android.view.KeyEvent;
import android.widget.SeekBar;
import android.widget.SpinnerAdapter;
import android.widget.TextView;


public class EditingPanelActivityTest extends
ActivityInstrumentationTestCase2<EditingPanelActivity>{
	private EditingPanelActivity mActivity;
	private SeekBar mSeekBar;
	private int mDistance;

	public EditingPanelActivityTest() {
		super(EditingPanelActivity.class);
	} // end of SpinnerActivityTest constructor definition


	@Override
	protected void setUp() throws Exception {
		super.setUp();

		setActivityInitialTouchMode(false);

		mActivity = getActivity();

		mSeekBar=(SeekBar) mActivity.findViewById(R.id.bar_distance);

		mDistance=1000;
		//mPlanetData = mSpinner.getAdapter();

	} // end of setUp() method definition

	public void testPreConditions() {
		//assertTrue(mSeekBar.getOnFocusChangeListener()!= null);
		assertTrue(mSeekBar.getMax()==20000);
	} // end of testPreConditions() method definition


	public void testSeekBarUI() {

		mActivity.runOnUiThread(
				new Runnable() {
					public void run() {
						
					} // end of run() method definition
				} // end of anonymous Runnable object instantiation
				); // end of invocation of runOnUiThread

		mSeekBar.setProgress(mDistance);
		TextView resultView =
				(TextView) mActivity.findViewById(
						R.id.text_distance
						);

		int distance=mSeekBar.getProgress();
		assertEquals(distance, mDistance);

	} // end of testSpinnerUI() method definition




	@UiThreadTest
	public void testStatePause() {
		int progress=100;
		// Set up instrumentation. Get the instrumentation object that is controlling the application under test. This is used later to invoke the onPause() and onResume() methods:
		Instrumentation mInstr = this.getInstrumentation();
		//	Set the spinner selection to a test value:
		mSeekBar.setProgress(progress);
		//	Use instrumentation to call the Activity's onPause():
		mInstr.callActivityOnPause(mActivity);
		//	This ensures that resuming the activity actually restores the spinner's state rather than simply leaving it as it was.
		//	Use instrumentation to call the Activity's onResume():
		mInstr.callActivityOnResume(mActivity);
		//	Invoking callActivityOnResume(android.app.Activity) affects the activity in a way similar to callActivityOnPause. The activity's onResume() method is invoked instead of manipulating the activity's UI to force it to resume.
		//	Get the current state of the spinner:
		int currentPosition = mSeekBar.getProgress();
		//Test the current spinner state against the test values:
		assertEquals(progress,currentPosition);
	} // end of testStatePause() method definition
}

