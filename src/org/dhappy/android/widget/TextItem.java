package org.dhappy.android.widget;

public class TextItem implements ListItem {
	public final String title;
	public final String subtitle;

	public TextItem(String title, String subtitle) {
		this.title = title;
		this.subtitle = subtitle;
	}
	
	@Override
	public boolean isHeader() {
		return false;
	}
}