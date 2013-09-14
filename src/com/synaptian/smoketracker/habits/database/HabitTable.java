package de.vogella.android.habits.database;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class HabitTable {

  // Database table
  public static final String TABLE_HABIT = "habit";
  public static final String COLUMN_ID = "_id";
  public static final String COLUMN_NAME = "name";
  public static final String COLUMN_DESCRIPTION = "description";
  public static final String COLUMN_TIME = "time";

  // Database creation SQL statement
  private static final String DATABASE_CREATE = "create table " 
      + TABLE_HABIT
      + "(" 
      + COLUMN_ID + " integer primary key autoincrement, " 
      + COLUMN_NAME + " text not null,"
      + COLUMN_TIME + " integer,"
      + COLUMN_DESCRIPTION + " text not null" 
      + ");";

  public static void onCreate(SQLiteDatabase database) {
    database.execSQL(DATABASE_CREATE);
  }

  public static void onUpgrade(SQLiteDatabase database, int oldVersion,
      int newVersion) {
    Log.w(HabitTable.class.getName(), "Upgrading database from version "
        + oldVersion + " to " + newVersion
        + ", which will destroy all old data");
    database.execSQL("DROP TABLE IF EXISTS " + TABLE_HABIT);
    onCreate(database);
  }
} 
