package org.dhappy.habits;

import java.util.Calendar;
import org.dhappy.habits.R;
import org.dhappy.habits.contentprovider.HabitContentProvider;
import org.dhappy.habits.database.DescriptorTable;
import org.dhappy.habits.database.HabitTable;

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

/*
 * HabitDetailActivity allows to enter a new habit item 
 * or to change an existing
 */
public class DescriptorDetailActivity extends Activity {
  private EditText mNameText;
  private EditText mColorText;

  private Uri habitUri;

  @Override
  protected void onCreate(Bundle bundle) {
    super.onCreate(bundle);
    setContentView(R.layout.descriptor_edit);

    mNameText = (EditText) findViewById(R.id.habit_edit_name);
    mColorText = (EditText) findViewById(R.id.habit_edit_color);
    Button confirmButton = (Button) findViewById(R.id.habit_edit_button);
    Button cancelButton = (Button) findViewById(R.id.habit_cancel_button);

    Bundle extras = getIntent().getExtras();

    // Check from the saved Instance
    habitUri = (bundle == null) ? null : (Uri) bundle.getParcelable(HabitContentProvider.HABIT_CONTENT_ITEM_TYPE);

    // Or passed from the other activity
    if (extras != null) {
      habitUri = extras.getParcelable(HabitContentProvider.DESCRIPTOR_CONTENT_ITEM_TYPE);

      fillData(habitUri);
    } else {
    	mColorText.setText(getRandomColor());
    }

    confirmButton.setOnClickListener(new View.OnClickListener() {
        public void onClick(View view) {
          if (TextUtils.isEmpty(mNameText.getText().toString())) {
          	Toast.makeText(DescriptorDetailActivity.this, "Please provide a name", Toast.LENGTH_LONG).show();
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
    		DescriptorTable.COLUMN_NAME,
    		DescriptorTable.COLUMN_COLOR };
    Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
    if (cursor != null) {
      cursor.moveToFirst();

      mNameText.setText(cursor.getString(cursor.getColumnIndexOrThrow(HabitTable.COLUMN_NAME)));
      mColorText.setText(cursor.getString(cursor.getColumnIndexOrThrow(HabitTable.COLUMN_COLOR)));
      
      cursor.close();
    }
  }

  protected void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    saveState();
    outState.putParcelable(HabitContentProvider.DESCRIPTOR_CONTENT_ITEM_TYPE, habitUri);
  }

  @Override
  protected void onPause() {
    super.onPause();
  }

  private void saveState() {
    String name = mNameText.getText().toString();
    String color = mColorText.getText().toString();

    // Only save if either summary or description
    // is available

    if(name.length() == 0) {
      return;
    }

    ContentValues values = new ContentValues();	
    values.put(DescriptorTable.COLUMN_NAME, name);
    values.put(DescriptorTable.COLUMN_COLOR, color);

    if (habitUri == null) {
      // New habit
      habitUri = getContentResolver().insert(HabitContentProvider.DESCRIPTORS_URI, values);
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
