package com.synaptian.smoketracker.habits;

import java.util.ArrayList;
import java.util.List;

import com.synaptian.smoketracker.habits.contentprovider.MyHabitContentProvider;
import com.synaptian.smoketracker.habits.database.HabitTable;

import android.app.ListActivity;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.SimpleCursorAdapter;

public class HabitTimeListActivity extends ListActivity implements LoaderManager.LoaderCallbacks<Cursor> {
	private SimpleCursorAdapter adapter;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.event_list);

        String[] from = new String[] { HabitTable.COLUMN_NAME, HabitTable.COLUMN_TIME };
        // Fields on the UI to which we map
        int[] to = new int[] { R.id.label, R.id.timer };

        getLoaderManager().initLoader(0, null, this);
        adapter = new SimpleCursorAdapter(this, R.layout.habit_row, null, from, to, 0);

        setListAdapter(adapter);
    }
    
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = { HabitTable.COLUMN_ID, HabitTable.COLUMN_NAME };
        CursorLoader cursorLoader = new CursorLoader(this,
            MyHabitContentProvider.CONTENT_URI, projection, null, null, null);
        return cursorLoader;
    }
    
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
      adapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
      // data is not available anymore, delete reference
      adapter.swapCursor(null);
    }
}