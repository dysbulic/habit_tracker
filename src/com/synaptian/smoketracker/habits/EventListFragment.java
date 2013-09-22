package com.synaptian.smoketracker.habits;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;

import org.dhappy.android.widget.HeaderItem;
import org.dhappy.android.widget.HeaderedListAdapter;
import org.dhappy.android.widget.ListItem;
import org.dhappy.android.widget.TextTimeItem;
import org.dhappy.android.widget.Timer;

import com.synaptian.smoketracker.habits.contentprovider.MyHabitContentProvider;
import com.synaptian.smoketracker.habits.database.HabitTable;
import com.synaptian.smoketracker.habits.database.GoalTable;
import com.synaptian.smoketracker.habits.database.EventTable;

import android.app.ListFragment;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract.Contacts;
import android.text.TextUtils;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.SearchView.OnQueryTextListener;
import android.widget.SimpleCursorAdapter.ViewBinder;
import android.widget.TextView;


public class EventListFragment extends ListFragment
        implements LoaderManager.LoaderCallbacks<Cursor> {
	private static final int MENU_DELETE = Menu.FIRST + 3;

    List<ListItem> items;
    
    // This is the Adapter being used to display the list's data.
    SimpleCursorAdapter mAdapter;

    @Override public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Give some text to display if there is no data.  In a real
        // application this would come from a resource.
        setEmptyText("No recorded events");

        // We have a menu iteArraym to show in action bar.
        setHasOptionsMenu(true);
        
        registerForContextMenu(getListView());

/*
        String[] from = new String[] { HabitTable.COLUMN_NAME, EventTable.COLUMN_TIME };
        int[] to = new int[] { R.id.name, R.id.time };

        mAdapter = new SimpleCursorAdapter(getActivity(), R.layout.event_row, null, from, to, 0);

        mAdapter.setViewBinder(new ViewBinder() {
    		@Override
    		public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
    			if(columnIndex == 2) { // Time
                    long time = cursor.getInt(columnIndex);

                    Calendar eventTime = Calendar.getInstance();
                    eventTime.setTimeInMillis(time * 1000);
                    
                    SimpleDateFormat timeFormat = new SimpleDateFormat("H:mm:ss");
                    
                    TextView timeView = (TextView) view;
                    timeView.setText(timeFormat.format(eventTime.getTime()));
                    return true;
    			}

    			return false;
    		}
        });

        setListAdapter(mAdapter);
*/
        items = new ArrayList<ListItem>();

        String[] queryCols = new String[] { EventTable.TABLE_EVENT + "." + EventTable.COLUMN_ID, HabitTable.COLUMN_NAME, HabitTable.COLUMN_COLOR, EventTable.COLUMN_TIME };
        Cursor cursor = getActivity().getContentResolver().query(MyHabitContentProvider.EVENTS_URI, queryCols, null, null, EventTable.COLUMN_TIME + " DESC");
        SimpleDateFormat dateFormat = new SimpleDateFormat("EE, d MMM y");
        
        TextTimeItem lastItem = null;
        if(cursor.moveToFirst()) {
        	do {
        		int id = cursor.getInt(cursor.getColumnIndexOrThrow(EventTable.COLUMN_ID));
        		String name = cursor.getString(cursor.getColumnIndexOrThrow(HabitTable.COLUMN_NAME));
        		long time = cursor.getInt(cursor.getColumnIndexOrThrow(EventTable.COLUMN_TIME));
        		String color = cursor.getString(cursor.getColumnIndexOrThrow(HabitTable.COLUMN_COLOR));
        		TextTimeItem nextItem = new TextTimeItem(name, time, color, id);
        		
        		if(lastItem == null || nextItem.time.get(Calendar.DAY_OF_YEAR) != lastItem.time.get(Calendar.DAY_OF_YEAR)) {
        	        items.add(new HeaderItem(dateFormat.format(nextItem.time.getTime())));
        		}
        		items.add(nextItem);
        		lastItem = nextItem;
        	} while(cursor.moveToNext());
        }

        HeaderedListAdapter adapter = new HeaderedListAdapter(getActivity(), items);
        setListAdapter(adapter);
        
        // Start out with a progress indicator.
        setListShown(false);

        // Prepare the loader.  Either re-connect with an existing one,
        // or start a new one.
        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        MenuItem item = menu.add("New");
        item.setIcon(android.R.drawable.ic_input_add);
        item.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        item.setIntent(new Intent(getActivity(), EventDetailActivity.class));
    }

    @Override  
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {  
    	super.onCreateContextMenu(menu, v, menuInfo);  
    	menu.setHeaderTitle("Event Options");
    	menu.add(ContextMenu.NONE, MENU_DELETE, ContextMenu.NONE, "Delete");
    }

    @Override  
    public boolean onContextItemSelected(MenuItem item) {
    	Toast.makeText(getActivity(), "onMenuItemSelected", Toast.LENGTH_LONG).show();
        switch (item.getItemId()) {
        case MENU_DELETE:
          AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
          int id = ((TextTimeItem) items.get((int) info.id)).id;
          Uri uri = Uri.parse(MyHabitContentProvider.EVENTS_URI + "/" + id);
          Toast.makeText(getActivity(), "Deleting: " + uri, Toast.LENGTH_LONG).show();

          getActivity().getContentResolver().delete(uri, null, null);
          return true;
        }
        return super.onContextItemSelected(item);
    }
    
    @Override public void onListItemClick(ListView l, View v, int position, long id) {
    }

    // These are the rows that we will retrieve.
    static final String[] EVENTS_PROJECTION = new String[] {
    	EventTable.TABLE_EVENT + "." + EventTable.COLUMN_ID,
        HabitTable.COLUMN_NAME,
        EventTable.TABLE_EVENT + "." + EventTable.COLUMN_TIME
    };

    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // Now create and return a CursorLoader that will take care of
        // creating a Cursor for the data being displayed.
        return new CursorLoader(getActivity(), MyHabitContentProvider.EVENTS_URI, EVENTS_PROJECTION, null, null, null);
    }

    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        // Swap the new cursor in.  (The framework will take care of closing the
        // old cursor once we return.)
        //mAdapter.swapCursor(data);

        // The list should now be shown.
        if (isResumed()) {
            setListShown(true);
        } else {
            setListShownNoAnimation(true);
        }
    }

    public void onLoaderReset(Loader<Cursor> loader) {
        // This is called when the last Cursor provided to onLoadFinished()
        // above is about to be closed.  We need to make sure we are no
        // longer using it.
        mAdapter.swapCursor(null);
    }
}
