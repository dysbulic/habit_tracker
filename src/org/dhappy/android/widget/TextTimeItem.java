package org.dhappy.android.widget;

import java.util.Calendar;

public class TextTimeItem implements ListItem {
	public final String title;
	public final Calendar time;

	public TextTimeItem(String title, long time) {
		this.title = title;
		
		this.time = Calendar.getInstance();
		this.time.setTimeInMillis(time * 1000);
	}
	
	@Override
	public boolean isHeader() {
		return false;
	}
}