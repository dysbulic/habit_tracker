package com.synaptian.smoketracker.habits.database;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class HabitTable {

  // Database table
  public static final String TABLE_HABIT = "habit";
  public static final String COLUMN_ID = "_id";
  public static final String COLUMN_NAME = "name";
  public static final String COLUMN_COLOR = "color";
  public static final String COLUMN_DESCRIPTION = "description";
  public static final String COLUMN_CREATED_AT = "created_at";
  public static final String COLUMN_UPDATED_AT = "updated_at";
  public static final String COLUMN_LAST_SYNC = "last_sync";

  // Database creation SQL statement
  private static final String DATABASE_CREATE = "create table " 
      + TABLE_HABIT
      + "(" 
      + COLUMN_ID + " integer primary key autoincrement,"
      + COLUMN_NAME + " text not null,"
      + COLUMN_COLOR + " text not null,"
      + COLUMN_DESCRIPTION + " text,"
      + COLUMN_LAST_SYNC + " integer,"
      + COLUMN_CREATED_AT + " integer,"
      + COLUMN_UPDATED_AT + " integer"
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
