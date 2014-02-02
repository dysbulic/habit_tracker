package org.dhappy.android.widget;

import java.util.Calendar;

public class TextTimeItem implements ListItem {
	public final int id;
	public final String title;
	public final Calendar time;
	public final String color;
	public final Double weight;

	public TextTimeItem(String title, long time, String color, int id) {
		this(title, time, color, null, id);
	}
	
	public TextTimeItem(String title, long time, String color, Double weight, int id) {
		this.id = id;
		
		this.title = title;
		
		this.time = Calendar.getInstance();
		this.time.setTimeInMillis(time * 1000);
		
		this.color = color;
		
		this.weight = weight;
	}
	
	@Override
	public boolean isHeader() {
		return false;
	}
}