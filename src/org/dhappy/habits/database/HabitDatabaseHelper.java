package org.dhappy.habits.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class HabitDatabaseHelper extends SQLiteOpenHelper {

  public static final String DATABASE_NAME = "habits.db";
  private static final int DATABASE_VERSION = 14;

  public HabitDatabaseHelper(Context context) {
    super(context, DATABASE_NAME, null, DATABASE_VERSION);
  }

  // Method is called during creation of the database
  @Override
  public void onCreate(SQLiteDatabase database) {
	    HabitTable.onCreate(database);
	    GoalTable.onCreate(database);
	    EventTable.onCreate(database);
	    DescriptorTable.onCreate(database);
	    ReadingTable.onCreate(database);
  }

  // Method is called during an upgrade of the database,
  // e.g. if you increase the database version
  @Override
  public void onUpgrade(SQLiteDatabase database, int oldVersion,
      int newVersion) {
	    HabitTable.onUpgrade(database, oldVersion, newVersion);
	    GoalTable.onUpgrade(database, oldVersion, newVersion);
	    EventTable.onUpgrade(database, oldVersion, newVersion);
	    DescriptorTable.onUpgrade(database, oldVersion, newVersion);
	    ReadingTable.onUpgrade(database, oldVersion, newVersion);
  }
}
