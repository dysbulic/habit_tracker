//From: http://stackoverflow.com/questions/13590627/android-listview-headers#answer-13634801

package org.dhappy.android.widget;

import com.synaptian.smoketracker.habits.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class HeaderedListAdapter extends ArrayAdapter<ListItem> {
	private Context context;
	private List<ListItem> items;
	private LayoutInflater vi;

	public HeaderedListAdapter(Context context,List<ListItem> items) {
		super(context,0, items);
		this.context = context;
		this.items = items;
		vi = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View v = convertView;

		final ListItem i = items.get(position);
		if (i != null) {
			if(i.isHeader()){
				HeaderItem si = (HeaderItem)i;
				v = vi.inflate(R.layout.header, null);

				v.setOnClickListener(null);
				v.setOnLongClickListener(null);
				v.setLongClickable(false);
				
				final TextView sectionView = (TextView) v.findViewById(R.id.header_text);
				sectionView.setText(si.getTitle());
			} else {
				TextTimeItem ei = (TextTimeItem)i;
				v = vi.inflate(R.layout.event_row, null);
				
				final TextView color = (TextView)v.findViewById(R.id.color_block);
				color.setBackgroundColor(Color.parseColor(ei.color));

				final TextView title = (TextView)v.findViewById(R.id.name);
				final TextView time = (TextView)v.findViewById(R.id.time);
				
				if (title != null) {
					title.setText(ei.title);
				}
				if(time != null) {
	                 SimpleDateFormat timeFormat = new SimpleDateFormat("H:mm:ss");
	                 time.setText(timeFormat.format(ei.time.getTime()));
	            }
			}
		}
		return v;
	}
}