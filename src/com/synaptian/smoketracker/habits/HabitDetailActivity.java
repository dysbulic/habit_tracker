package com.synaptian.smoketracker.habits;

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
import com.synaptian.smoketracker.habits.contentprovider.MyHabitContentProvider;
import com.synaptian.smoketracker.habits.database.HabitTable;

/*
 * HabitDetailActivity allows to enter a new habit item 
 * or to change an existing
 */
public class HabitDetailActivity extends Activity {
  private EditText mNameText;
  private EditText mColorText;
  private EditText mDescriptionText;

  private Uri habitUri;

  @Override
  protected void onCreate(Bundle bundle) {
    super.onCreate(bundle);
    setContentView(R.layout.habit_edit);

    mNameText = (EditText) findViewById(R.id.habit_edit_name);
    mColorText = (EditText) findViewById(R.id.habit_edit_color);
    mDescriptionText = (EditText) findViewById(R.id.habit_edit_description);
    Button confirmButton = (Button) findViewById(R.id.habit_edit_button);
    Button cancelButton = (Button) findViewById(R.id.habit_cancel_button);

    Bundle extras = getIntent().getExtras();

    // Check from the saved Instance
    habitUri = (bundle == null) ? null : (Uri) bundle.getParcelable(MyHabitContentProvider.HABIT_CONTENT_ITEM_TYPE);

    // Or passed from the other activity
    if (extras != null) {
      habitUri = extras.getParcelable(MyHabitContentProvider.HABIT_CONTENT_ITEM_TYPE);

      fillData(habitUri);
    } else {
    	mColorText.setText(getRandomColor());
    }

    confirmButton.setOnClickListener(new View.OnClickListener() {
        public void onClick(View view) {
          if (TextUtils.isEmpty(mNameText.getText().toString())) {
          	Toast.makeText(HabitDetailActivity.this, "Please provide a name", Toast.LENGTH_LONG).show();
          } else {
            setResult(RESULT_OK);
            saveState();
            finish();
          }
        }

      });

  	cancelButton.setOnClickListener(new View.OnClickListener() {
      public void onClick(View view) {
        setResult(RESULT_CANCELED);
      	finish();
      }
    });
  }

  private void fillData(Uri uri) {
    String[] projection = {
    		HabitTable.COLUMN_NAME,
    		HabitTable.COLUMN_COLOR,
    		HabitTable.TABLE_HABIT + "." + HabitTable.COLUMN_DESCRIPTION };
    Cursor cursor = getContentResolver().query(uri, projection, null, null,
        null);
    if (cursor != null) {
      cursor.moveToFirst();

      mNameText.setText(cursor.getString(cursor.getColumnIndexOrThrow(HabitTable.COLUMN_NAME)));
      mColorText.setText(cursor.getString(cursor.getColumnIndexOrThrow(HabitTable.COLUMN_COLOR)));
      mDescriptionText.setText(cursor.getString(cursor.getColumnIndexOrThrow(HabitTable.COLUMN_DESCRIPTION)));
      
      cursor.close();
    }
  }

  protected void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    saveState();
    outState.putParcelable(MyHabitContentProvider.HABIT_CONTENT_ITEM_TYPE, habitUri);
  }

  @Override
  protected void onPause() {
    super.onPause();
  }

  private void saveState() {
    String name = mNameText.getText().toString();
    String color = mColorText.getText().toString();
    String description = mDescriptionText.getText().toString();

    // Only save if either summary or description
    // is available

    if (description.length() == 0 && name.length() == 0) {
      return;
    }

    ContentValues values = new ContentValues();	
    values.put(HabitTable.COLUMN_NAME, name);
    values.put(HabitTable.COLUMN_COLOR, color);
    values.put(HabitTable.COLUMN_DESCRIPTION, description);

    if (habitUri == null) {
      // New habit
      habitUri = getContentResolver().insert(MyHabitContentProvider.HABITS_URI, values);
    } else {
      // Update habit
      getContentResolver().update(habitUri, values, null, null);
    }
  }
  
  public static String getRandomColor() {
	 String[] letters = {"0","1","2","3","4","5","6","7","8","9","A","B","C","D","E","F"};
	 String color = "#";
	 for (int i = 0; i < 6; i++ ) {
	 	color += letters[(int) Math.round(Math.random() * (letters.length - 1))];
	 }
	 return color;
  }
}
