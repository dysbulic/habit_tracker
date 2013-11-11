package com.synaptian.smoketracker.habits.database;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class EventTable {

  // Database table
  public static final String TABLE_EVENT = "event";
  public static final String COLUMN_ID = "_id";
  public static final String COLUMN_HABIT_ID = "habit_id";
  public static final String COLUMN_DESCRIPTION = "description";
  public static final String COLUMN_TIME = "time";

  // Database creation SQL statement
  private static final String DATABASE_CREATE = "create table " 
      + TABLE_EVENT
      + "(" 
      + COLUMN_ID + " integer primary key autoincrement, " 
      + COLUMN_HABIT_ID + " integer not null,"
      + COLUMN_TIME + " integer,"
      + COLUMN_DESCRIPTION + " text" 
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
