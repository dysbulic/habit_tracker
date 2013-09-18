package com.synaptian.smoketracker.habits;

import java.util.Calendar;

import android.app.Activity;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TimePicker;
import android.widget.Toast;
import android.util.Log;
import com.synaptian.smoketracker.habits.contentprovider.MyHabitContentProvider;
import com.synaptian.smoketracker.habits.database.HabitTable;
import com.synaptian.smoketracker.habits.database.GoalTable;

/*
 * HabitDetailActivity allows to enter a new habit item 
 * or to change an existing
 */
public class GoalDetailActivity extends Activity
	implements AdapterView.OnItemSelectedListener {
  private Spinner mHabitSelect;
  private EditText mTitleText;
  private EditText mBodyText;
  private TimePicker mEventTime;
  private DatePicker mEventDate;

  private Uri goalUri;

  @Override
  protected void onCreate(Bundle bundle) {
    super.onCreate(bundle);
    setContentView(R.layout.goal_edit);

    mHabitSelect = (Spinner) findViewById(R.id.habit);
    mEventTime = (TimePicker) findViewById(R.id.event_time);
    mEventDate = (DatePicker) findViewById(R.id.event_date);
    mTitleText = (EditText) findViewById(R.id.habit_edit_summary);
    mBodyText = (EditText) findViewById(R.id.habit_edit_description);
    Button confirmButton = (Button) findViewById(R.id.habit_edit_button);
    Button cancelButton = (Button) findViewById(R.id.habit_cancel_button);

    Bundle extras = getIntent().getExtras();

    // Check from the saved Instance
    goalUri = (bundle == null) ? null : (Uri) bundle.getParcelable(MyHabitContentProvider.HABIT_CONTENT_ITEM_TYPE);

    // Or passed from the other activity
    if (extras != null) {
      goalUri = extras.getParcelable(MyHabitContentProvider.HABIT_CONTENT_ITEM_TYPE);

      fillData(goalUri);
    }

    String[] queryCols = new String[] { HabitTable.COLUMN_ID, HabitTable.COLUMN_NAME };
    String[] from = new String[] { HabitTable.COLUMN_NAME, HabitTable.COLUMN_ID };
    int[] to = new int[] { R.id.label, R.id.id };

    Cursor cursor = getContentResolver().query(MyHabitContentProvider.HABITS_URI, queryCols, null, null, null);
    SimpleCursorAdapter mAdapter = new SimpleCursorAdapter(this, R.layout.habit_select_row, cursor, from, to, 0);
    mHabitSelect.setAdapter(mAdapter);
    
    mHabitSelect.setOnItemSelectedListener(this);
    
    confirmButton.setOnClickListener(new View.OnClickListener() {
        public void onClick(View view) {
          if (TextUtils.isEmpty(mTitleText.getText().toString())) {
          	Toast.makeText(GoalDetailActivity.this, "Please provide a name", Toast.LENGTH_LONG).show();
          } else {
            setResult(RESULT_OK);
            finish();
          }
        }

      });

  	cancelButton.setOnClickListener(new View.OnClickListener() {
      public void onClick(View view) {
      	setResult(RESULT_OK);
      	finish();
      }
    });
  }

  private void fillData(Uri uri) {
    String[] projection = { HabitTable.COLUMN_NAME, GoalTable.COLUMN_TIME, GoalTable.COLUMN_DESCRIPTION };
    Cursor cursor = getContentResolver().query(uri, projection, null, null,
        null);
    if (cursor != null) {
      cursor.moveToFirst();

      mTitleText.setText(cursor.getString(cursor.getColumnIndexOrThrow(HabitTable.COLUMN_NAME)));
      mBodyText.setText(cursor.getString(cursor.getColumnIndexOrThrow(HabitTable.COLUMN_DESCRIPTION)));

      mBodyText.setText(uri.toString());
      
      Calendar eventTime = Calendar.getInstance();
      long seconds = cursor.getInt(cursor.getColumnIndexOrThrow(HabitTable.COLUMN_TIME));
      eventTime.setTimeInMillis(seconds * 1000);
      
      mEventDate.updateDate(eventTime.get(Calendar.YEAR),
    		  eventTime.get(Calendar.MONTH),
    		  eventTime.get(Calendar.DAY_OF_MONTH));
      mEventTime.setCurrentHour(eventTime.get(Calendar.HOUR_OF_DAY));
      mEventTime.setCurrentMinute(eventTime.get(Calendar.MINUTE));
      
      // Always close the cursor
      cursor.close();
    }
  }

  protected void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    saveState();
    outState.putParcelable(MyHabitContentProvider.HABIT_CONTENT_ITEM_TYPE, goalUri);
  }

  @Override
  protected void onPause() {
    super.onPause();
    saveState();
  }

  private void saveState() {
	int habitId = mHabitSelect.getSelectedItemPosition();
    String summary = mTitleText.getText().toString();
    String description = mBodyText.getText().toString();

    Calendar eventTime = Calendar.getInstance();
    eventTime.set(mEventDate.getYear(),
    			  mEventDate.getMonth(),
    			  mEventDate.getDayOfMonth(),
    			  mEventTime.getCurrentHour(),
    			  mEventTime.getCurrentMinute());
    
    // Only save if either summary or description
    // is available

    if (description.length() == 0 && summary.length() == 0) {
      return;
    }

    ContentValues values = new ContentValues();	
    values.put(HabitTable.COLUMN_NAME, summary);
    values.put(HabitTable.COLUMN_TIME, Math.floor(eventTime.getTimeInMillis() / 1000));
    values.put(HabitTable.COLUMN_DESCRIPTION, description);

    if (goalUri == null) {
      // New habit
      goalUri = getContentResolver().insert(MyHabitContentProvider.HABITS_URI, values);
    } else {
      // Update habit
      getContentResolver().update(goalUri, values, null, null);
    }

    Log.w(GoalDetailActivity.class.getName(),
            "Event Time: " + eventTime);
  }

  @Override
  public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
	  mBodyText.setText("ID: " + id);
  }

  @Override
  public void onNothingSelected(AdapterView<?> arg0) {
  }
}
