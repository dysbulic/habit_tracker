package com.synaptian.smoketracker.habits;

import org.dhappy.android.widget.Timer;

import com.synaptian.smoketracker.habits.contentprovider.HabitContentProvider;
import com.synaptian.smoketracker.habits.database.HabitTable;
import com.synaptian.smoketracker.habits.database.GoalTable;

import android.app.ListFragment;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
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
import android.widget.SimpleCursorAdapter.ViewBinder;


public class GoalListFragment extends ListFragment
        implements LoaderManager.LoaderCallbacks<Cursor> {
	private static final int MENU_DELETE = Menu.FIRST + 2;

    // This is the Adapter being used to display the list's data.
    SimpleCursorAdapter mAdapter;

    @Override public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Give some text to display if there is no data.  In a real
        // application this would come from a resource.
        setEmptyText(getString(R.string.no_goals));

        // We have a menu item to show in action bar.
        setHasOptionsMenu(true);
        
        registerForContextMenu(getListView());

        String[] from = new String[] { HabitTable.COLUMN_NAME, HabitTable.COLUMN_COLOR, GoalTable.COLUMN_TIME };
        int[] to = new int[] { R.id.label, R.id.color_block, R.id.timer };

        mAdapter = new SimpleCursorAdapter(getActivity(), R.layout.goal_row, null, from, to, 0);

        mAdapter.setViewBinder(new ViewBinder() {
    		@Override
    		public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
    			if(columnIndex == 2) { // Time
    				view.setBackgroundColor(Color.parseColor(cursor.getString(columnIndex)));
    				return true;
    			}

    			if(columnIndex == 3) { // Time
                    long time = cursor.getInt(columnIndex);
                    Timer timer = (Timer) view;
                    timer.setStartingTime(time * 1000);
                    return true;
    			}

    			return false;
    		}
        });

        setListAdapter(mAdapter);

        // Start out with a progress indicator.
        setListShown(false);

        // Prepare the loader.  Either re-connect with an existing one,
        // or start a new one.
        getLoaderManager().initLoader(0, null, this);
    }

    @Override public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        MenuItem item = menu.add(R.string.menu_new);
        item.setIcon(android.R.drawable.ic_input_add);
        item.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        item.setIntent(new Intent(getActivity(), GoalDetailActivity.class));
    }

    @Override  
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {  
    	super.onCreateContextMenu(menu, v, menuInfo);  
    	menu.setHeaderTitle(R.string.goal_options_header);
    	menu.add(ContextMenu.NONE, MENU_DELETE, ContextMenu.NONE, R.string.menu_delete);
    }

    @Override  
    public boolean onContextItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case MENU_DELETE:
          AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
          Uri uri = Uri.parse(HabitContentProvider.GOALS_URI + "/" + info.id);
          getActivity().getContentResolver().delete(uri, null, null);
          return true;
        }
        return super.onContextItemSelected(item);
    }
    
    @Override public void onListItemClick(ListView l, View v, int position, long id) {
    	super.onListItemClick(l, v, position, id);
        Intent intent = new Intent(getActivity(), GoalDetailActivity.class);
        Uri goalUri = Uri.parse(HabitContentProvider.GOALS_URI + "/" + id);
        intent.putExtra(HabitContentProvider.GOAL_CONTENT_ITEM_TYPE, goalUri);

        startActivity(intent);
    }

    // These are the rows that we will retrieve.
    static final String[] GOALS_PROJECTION = new String[] {
    	GoalTable.TABLE_GOAL + "." + GoalTable.COLUMN_ID,
        HabitTable.COLUMN_NAME,
        HabitTable.COLUMN_COLOR,
        GoalTable.TABLE_GOAL + "." + GoalTable.COLUMN_TIME
    };

    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // Now create and return a CursorLoader that will take care of
        // creating a Cursor for the data being displayed.
        return new CursorLoader(getActivity(), HabitContentProvider.GOALS_URI, GOALS_PROJECTION, null, null, null);
    }

    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        // Swap the new cursor in.  (The framework will take care of closing the
        // old cursor once we return.)
        mAdapter.swapCursor(data);

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
