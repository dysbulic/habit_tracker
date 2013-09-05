// From: http://www.vogella.com/articles/AndroidSQLite/article.html

package com.synaptian.smokingtracker;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class EventTable {
  public static final String TABLE_EVENT = "events";
  public static final String COLUMN_ID = "_id";
  public static final String COLUMN_CATEGORY = "name";
  public static final String COLUMN_DESCRIPTION = "description";

  // Database creation SQL statement
  private static final String DATABASE_CREATE = "create table " 
      + TABLE_EVENT
      + "(" 
      + COLUMN_ID + " integer primary key autoincrement, " 
      + COLUMN_CATEGORY + " text not null, " 
      + COLUMN_DESCRIPTION + " text not null" 
      + ");";

  public static void onCreate(SQLiteDatabase database) {
	 database.execSQL(DATABASE_CREATE);
  }

  public static void onUpgrade(SQLiteDatabase database, int oldVersion,
      int newVersion) {
    Log.w(EventTable.class.getName(), "Upgrading database from version "
        + oldVersion + " to " + newVersion
        + ", which will destroy all old data");
    database.execSQL("DROP TABLE IF EXISTS " + TABLE_EVENT);
    onCreate(database);
  }
} 