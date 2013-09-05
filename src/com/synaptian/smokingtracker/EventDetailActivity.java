package com.synaptian.smokingtracker;

import android.app.Activity;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

/*
 * EventDetailActivity allows to enter a new event item 
 * or to change an existing
 */
public class EventDetailActivity extends Activity {
  private Spinner mCategory;
  private EditText mBodyText;

  private Uri eventUri;

  @Override
  protected void onCreate(Bundle bundle) {
    super.onCreate(bundle);
    setContentView(R.layout.event_edit);

    mCategory = (Spinner) findViewById(R.id.category);
    mBodyText = (EditText) findViewById(R.id.event_edit_description);
    Button confirmButton = (Button) findViewById(R.id.event_edit_button);

    Bundle extras = getIntent().getExtras();

    // Check from the saved Instance
    eventUri = (bundle == null) ? null : (Uri) bundle
        .getParcelable(EventContentProvider.CONTENT_ITEM_TYPE);

    // Or passed from the other activity
    if (extras != null) {
      eventUri = extras
          .getParcelable(EventContentProvider.CONTENT_ITEM_TYPE);

      fillData(eventUri);
    }

    confirmButton.setOnClickListener(new View.OnClickListener() {
      public void onClick(View view) {
    	  setResult(RESULT_OK);
          finish();
      }

    });
  }

  private void fillData(Uri uri) {
    String[] projection = { EventTable.COLUMN_DESCRIPTION, EventTable.COLUMN_CATEGORY };
    Cursor cursor = getContentResolver().query(uri, projection, null, null,
        null);
    if (cursor != null) {
      cursor.moveToFirst();
      String category = cursor.getString(cursor
          .getColumnIndexOrThrow(EventTable.COLUMN_CATEGORY));

      for (int i = 0; i < mCategory.getCount(); i++) {

        String s = (String) mCategory.getItemAtPosition(i);
        if (s.equalsIgnoreCase(category)) {
          mCategory.setSelection(i);
        }
      }

      mBodyText.setText(cursor.getString(cursor
          .getColumnIndexOrThrow(EventTable.COLUMN_DESCRIPTION)));

      // Always close the cursor
      cursor.close();
    }
  }

  protected void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    saveState();
    outState.putParcelable(EventContentProvider.CONTENT_ITEM_TYPE, eventUri);
  }

  @Override
  protected void onPause() {
    super.onPause();
    saveState();
  }

  private void saveState() {
    String category = (String) mCategory.getSelectedItem();
    String description = mBodyText.getText().toString();

    // Only save if either summary or description
    // is available

    if (description.length() == 0) {
      return;
    }

    ContentValues values = new ContentValues();
    values.put(EventTable.COLUMN_CATEGORY, category);
    values.put(EventTable.COLUMN_DESCRIPTION, description);

    if (eventUri == null) {
      // New event
      eventUri = getContentResolver().insert(EventContentProvider.CONTENT_URI, values);
    } else {
      // Update event
      getContentResolver().update(eventUri, values, null, null);
    }
  }

  private void makeToast() {
    Toast.makeText(EventDetailActivity.this, "Please maintain a summary",
        Toast.LENGTH_LONG).show();
  }
}