// From: http://www.codeproject.com/Articles/183608/Android-Lists-ListActivity-and-ListView-II-Custom

package com.synaptian.smoketracker.habits;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import org.dhappy.android.widget.Timer;

import com.synaptian.smoketracker.habits.database.HabitTable;

public class HabitTimeAdapter extends SimpleCursorAdapter {
	private int mSelectedPosition;
	Cursor items;
	private Context context;
	private int layout;

	public HabitTimeAdapter(Context context, int layout, Cursor c, String[] from, int[] to, int flags) {
		super(context, layout, c, from, to, flags);
		this.context = context;
		this.layout = layout;
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		Cursor c = getCursor();

		final LayoutInflater inflater = LayoutInflater.from(context);
		View view = inflater.inflate(layout, parent, false);

		TextView name = (TextView) view.findViewById(R.id.label);
		Timer timer = (Timer) view.findViewById(R.id.timer);

		name.setText(c.getString(c.getColumnIndex(HabitTable.COLUMN_NAME)));
		timer.setStartingTime(c.getInt(c.getColumnIndex(HabitTable.COLUMN_TIME)));

		return view;
	}


	@Override
	public void bindView(View view, Context context, Cursor c) {
		TextView name = (TextView) view.findViewById(R.id.label);
		Timer timer = (Timer) view.findViewById(R.id.timer);

		name.setText(c.getString(c.getColumnIndex(HabitTable.COLUMN_NAME)));
		timer.setStartingTime(c.getInt(c.getColumnIndex(HabitTable.COLUMN_TIME)));

		name.setTextColor(Color.GREEN);
		view.setBackgroundColor(Color.WHITE);
	}

	public void setSelectedPosition(int position) {
		mSelectedPosition = position;
		notifyDataSetChanged();
	}
}

/*
public class HabitTimeAdapter extends ArrayAdapter {
private final Activity activity;
private final List<HabitTime> habitTimes;

public HabitTimeAdapter(Activity activity, List<HabitTime> objects) {
	super(activity, R.layout.habit_row, objects);
	this.activity = activity;
	this.habitTimes = objects;
}

@Override
public View getView(int position, View convertView, ViewGroup parent) {
	View rowView = convertView;
	HabitTimeView htView = null;

	if (rowView == null) {
		// Get a new instance of the row layout view
		LayoutInflater inflater = activity.getLayoutInflater();
		rowView = inflater.inflate(R.layout.habit_row, null);

		// Hold the view objects in an object,
		// so they don't need to be re-fetched
		htView = new HabitTimeView();
		htView.name = (TextView) rowView.findViewById(R.id.label);
		htView.timer = (Timer) rowView.findViewById(R.id.timer);

		// Cache the view objects in the tag,
		// so they can be re-accessed later
		rowView.setTag(htView);
	} else {
		htView = (HabitTimeView) rowView.getTag();
	}

	// Transfer the stock data from the data object
	// to the view objects
	HabitTime currentTime = habitTimes.get(position);
	htView.name.setText(currentTime.getName());
	htView.timer.setStartingTime(currentTime.getTime());

	return rowView;
}

protected static class HabitTimeView {
	protected TextView name;
	protected Timer timer;
}
}
*/