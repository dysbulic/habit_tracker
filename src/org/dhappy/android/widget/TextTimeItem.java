package org.dhappy.android.widget;

import java.util.Calendar;

public class TextTimeItem implements ListItem {
	public final String title;
	public final Calendar time;
	public final String color;

	public TextTimeItem(String title, long time, String color) {
		this.title = title;
		
		this.time = Calendar.getInstance();
		this.time.setTimeInMillis(time * 1000);
		
		this.color = color;
	}
	
	@Override
	public boolean isHeader() {
		return false;
	}
}