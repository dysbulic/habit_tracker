package com.synaptian.smoketracker.habits;

import org.dhappy.android.widget.Timer;

import com.synaptian.smoketracker.habits.contentprovider.MyHabitContentProvider;
import com.synaptian.smoketracker.habits.database.HabitTable;
import com.synaptian.smoketracker.habits.database.EventTable;

import android.app.ListActivity;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.SimpleCursorAdapter.ViewBinder;

public class HabitTimeListActivity extends ListActivity implements LoaderManager.LoaderCallbacks<Cursor> {
	private static final int DELETE_ID = Menu.FIRST + 1;
	
	private SimpleCursorAdapter adapter;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.event_list);

        registerForContextMenu(getListView());

        String[] from = new String[] { HabitTable.COLUMN_NAME, EventTable.COLUMN_TIME };
        // Fields on the UI to which we map
        int[] to = new int[] { R.id.label, R.id.timer };

        getLoaderManager().initLoader(0, null, this);

        adapter = new SimpleCursorAdapter(this, R.layout.habit_row, null, from, to, 0);

        adapter.setViewBinder(new ViewBinder() {
    		@Override
    		public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
    			if(columnIndex == 2) { // Time
                    long time = cursor.getInt(columnIndex);
                    Timer timer = (Timer) view;
                    timer.setStartingTime(time * 1000);
                    return true;
    			}

    			return false;
    		}
        });
        
        setListAdapter(adapter);
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
      MenuInflater inflater = getMenuInflater();
      inflater.inflate(R.menu.listmenu, menu);
      return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
      switch (item.getItemId()) {
      case R.id.insert:
        createHabit();
        return true;
      }
      return super.onOptionsItemSelected(item);
    }

    private void createHabit() {
    	Intent i = new Intent(this, HabitDetailActivity.class);
        startActivity(i);
    }
    
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
        ContextMenuInfo menuInfo) {
      super.onCreateContextMenu(menu, v, menuInfo);
      menu.add(0, DELETE_ID, 0, R.string.menu_delete);
    }
    
    @Override
    public boolean onContextItemSelected(MenuItem item) {
      switch (item.getItemId()) {
      case DELETE_ID:
        AdapterContextMenuInfo info = (AdapterContextMenuInfo) item
            .getMenuInfo();
        Uri uri = Uri.parse(MyHabitContentProvider.HABITS_URI + "/"
            + info.id);
        getContentResolver().delete(uri, null, null);
        //fillData();
        return true;
      }
      return super.onContextItemSelected(item);
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
      super.onListItemClick(l, v, position, id);
      Intent i = new Intent(this, HabitDetailActivity.class);
      Uri habitUri = Uri.parse(MyHabitContentProvider.HABITS_URI + "/" + id);
      i.putExtra(MyHabitContentProvider.HABIT_CONTENT_ITEM_TYPE, habitUri);

      startActivity(i);
    }

    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = { HabitTable.COLUMN_ID, HabitTable.COLUMN_NAME, EventTable.COLUMN_TIME };
        CursorLoader cursorLoader = new CursorLoader(this, MyHabitContentProvider.HABITS_URI, projection, null, null, null);
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