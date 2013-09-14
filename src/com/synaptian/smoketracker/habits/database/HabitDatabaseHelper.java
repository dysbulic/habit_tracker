package com.synaptian.smoketracker.habits.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class HabitDatabaseHelper extends SQLiteOpenHelper {

  private static final String DATABASE_NAME = "habits.db";
  private static final int DATABASE_VERSION = 1;

  public HabitDatabaseHelper(Context context) {
    super(context, DATABASE_NAME, null, DATABASE_VERSION);
  }

  // Method is called during creation of the database
  @Override
  public void onCreate(SQLiteDatabase database) {
    HabitTable.onCreate(database);
  }

  // Method is called during an upgrade of the database,
  // e.g. if you increase the database version
  @Override
  public void onUpgrade(SQLiteDatabase database, int oldVersion,
      int newVersion) {
    HabitTable.onUpgrade(database, oldVersion, newVersion);
  }
}
