package de.vogella.android.habits;

import java.util.Calendar;

import android.app.Activity;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;
import android.util.Log;
import de.vogella.android.habits.contentprovider.MyHabitContentProvider;
import de.vogella.android.habits.database.HabitTable;

/*
 * HabitDetailActivity allows to enter a new habit item 
 * or to change an existing
 */
public class HabitDetailActivity extends Activity {
  private EditText mTitleText;
  private EditText mBodyText;
  private TimePicker mEventTime;
  private DatePicker mEventDate;

  private Uri habitUri;

  @Override
  protected void onCreate(Bundle bundle) {
    super.onCreate(bundle);
    setContentView(R.layout.habit_edit);

    mEventTime = (TimePicker) findViewById(R.id.event_time);
    mEventDate = (DatePicker) findViewById(R.id.event_date);
    mTitleText = (EditText) findViewById(R.id.habit_edit_summary);
    mBodyText = (EditText) findViewById(R.id.habit_edit_description);
    Button confirmButton = (Button) findViewById(R.id.habit_edit_button);

    Bundle extras = getIntent().getExtras();

    // Check from the saved Instance
    habitUri = (bundle == null) ? null : (Uri) bundle
        .getParcelable(MyHabitContentProvider.CONTENT_ITEM_TYPE);

    // Or passed from the other activity
    if (extras != null) {
      habitUri = extras
          .getParcelable(MyHabitContentProvider.CONTENT_ITEM_TYPE);

      fillData(habitUri);
    }

    confirmButton.setOnClickListener(new View.OnClickListener() {
      public void onClick(View view) {
        if (TextUtils.isEmpty(mTitleText.getText().toString())) {
          makeToast();
        } else {
          setResult(RESULT_OK);
          finish();
        }
      }

    });
  }

  private void fillData(Uri uri) {
    String[] projection = { HabitTable.COLUMN_NAME, HabitTable.COLUMN_TIME,
        HabitTable.COLUMN_DESCRIPTION };
    Cursor cursor = getContentResolver().query(uri, projection, null, null,
        null);
    if (cursor != null) {
      cursor.moveToFirst();

      mTitleText.setText(cursor.getString(cursor
          .getColumnIndexOrThrow(HabitTable.COLUMN_NAME)));
      mBodyText.setText(cursor.getString(cursor
          .getColumnIndexOrThrow(HabitTable.COLUMN_DESCRIPTION)));

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
    outState.putParcelable(MyHabitContentProvider.CONTENT_ITEM_TYPE, habitUri);
  }

  @Override
  protected void onPause() {
    super.onPause();
    saveState();
  }

  private void saveState() {
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
    values.put(HabitTable.COLUMN_TIME,
    		Math.floor(eventTime.getTimeInMillis() / 1000));
    values.put(HabitTable.COLUMN_DESCRIPTION, description);

    if (habitUri == null) {
      // New habit
      habitUri = getContentResolver().insert(MyHabitContentProvider.CONTENT_URI, values);
    } else {
      // Update habit
      getContentResolver().update(habitUri, values, null, null);
    }

    Log.w(HabitDetailActivity.class.getName(),
            "Event Time: " + eventTime);
  }

  private void makeToast() {
    Toast.makeText(HabitDetailActivity.this, "Please provide a name",
        Toast.LENGTH_LONG).show();
  }
}
