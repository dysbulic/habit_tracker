package org.dhappy.habits.database;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class GoalTable {

  // Database table
  public static final String TABLE_GOAL = "goal";
  public static final String COLUMN_ID = "_id";
  public static final String COLUMN_HABIT_ID = "habit_id";
  public static final String COLUMN_DESCRIPTION = "description";
  public static final String COLUMN_START = "start";
  public static final String COLUMN_END = "end";
  public static final String COLUMN_CREATED_AT = "created_at";
  public static final String COLUMN_UPDATED_AT = "updated_at";

  // Database creation SQL statement
  private static final String DATABASE_CREATE = "create table " 
      + TABLE_GOAL
      + "(" 
      + COLUMN_ID + " integer primary key autoincrement, " 
      + COLUMN_HABIT_ID + " integer not null,"
      + COLUMN_START + " integer,"
      + COLUMN_END + " integer,"
      + COLUMN_DESCRIPTION + " text,"
      + COLUMN_CREATED_AT + " integer,"
      + COLUMN_UPDATED_AT + " integer"
      + ");";

  public static void onCreate(SQLiteDatabase database) {
    database.execSQL(DATABASE_CREATE);
  }

  public static void onUpgrade(SQLiteDatabase database, int oldVersion,
      int newVersion) {
    Log.w(GoalTable.class.getName(), "Upgrading database from version "
        + oldVersion + " to " + newVersion
        + ", which will destroy all old data");
    database.execSQL("DROP TABLE IF EXISTS " + TABLE_GOAL);
    onCreate(database);
  }
}
