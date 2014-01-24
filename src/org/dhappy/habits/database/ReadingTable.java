package org.dhappy.habits.database;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class ReadingTable {

  // Database table
  public static final String TABLE_READING = "reading";
  public static final String COLUMN_ID = "_id";
  public static final String COLUMN_DESCRIPTOR_ID = "descriptor_id";
  public static final String COLUMN_WEIGHT = "weight";
  public static final String COLUMN_DESCRIPTION = "description";
  public static final String COLUMN_TIME = "time";
  public static final String COLUMN_CREATED_AT = "created_at";
  public static final String COLUMN_UPDATED_AT = "updated_at";

  // Database creation SQL statement
  private static final String DATABASE_CREATE = "create table " 
      + TABLE_READING
      + "(" 
      + COLUMN_ID + " integer primary key autoincrement, " 
      + COLUMN_DESCRIPTOR_ID + " integer not null,"
      + COLUMN_TIME + " integer,"
      + COLUMN_WEIGHT + " real,"
      + COLUMN_DESCRIPTION + " text,"
      + COLUMN_CREATED_AT + " integer,"
      + COLUMN_UPDATED_AT + " integer"
      + ");";

  public static void onCreate(SQLiteDatabase database) {
    database.execSQL(DATABASE_CREATE);
  }

  public static void onUpgrade(SQLiteDatabase database, int oldVersion,
      int newVersion) {
    Log.w(ReadingTable.class.getName(), "Upgrading database from version "
        + oldVersion + " to " + newVersion
        + ", which will destroy all old data");
    database.execSQL("DROP TABLE IF EXISTS " + TABLE_READING);
    onCreate(database);
  }
}
