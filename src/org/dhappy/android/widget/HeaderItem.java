package org.dhappy.android.widget;

public class HeaderItem implements ListItem {
	private final String title;
	
	public HeaderItem(String title) {
		this.title = title;
	}
	
	public String getTitle(){
		return title;
	}
	
	@Override
	public boolean isHeader() {
		return true;
	}
}