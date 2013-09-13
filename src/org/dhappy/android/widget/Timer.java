package org.dhappy.android.widget;

import java.util.Calendar;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

public class Timer extends TextView {
	public Timer(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	public Timer(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}

	public Timer(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
	}
	
	// From:
	public static long daysBetween(final Calendar startDate, final Calendar endDate) {  
		int MILLIS_IN_DAY = 1000 * 60 * 60 * 24;  
		long endInstant = endDate.getTimeInMillis();  
		int presumedDays = (int) ((endInstant - startDate.getTimeInMillis()) / MILLIS_IN_DAY);  
		Calendar cursor = (Calendar) startDate.clone();  
		cursor.add(Calendar.DAY_OF_YEAR, presumedDays);  
		long instant = cursor.getTimeInMillis();  
		if (instant == endInstant)  
		 return presumedDays;  
		final int step = instant < endInstant ? 1 : -1;  
		do {  
			cursor.add(Calendar.DAY_OF_MONTH, step);  
			presumedDays += step;  
		} while (cursor.getTimeInMillis() != endInstant);
		return presumedDays;  
	}
}
