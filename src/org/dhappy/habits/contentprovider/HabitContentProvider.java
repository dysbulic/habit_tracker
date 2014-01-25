package org.dhappy.habits.contentprovider;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.Calendar;
import java.util.Random;

import org.dhappy.habits.database.DescriptorTable;
import org.dhappy.habits.database.EventTable;
import org.dhappy.habits.database.GoalTable;
import org.dhappy.habits.database.HabitDatabaseHelper;
import org.dhappy.habits.database.HabitTable;
import org.dhappy.habits.database.ReadingTable;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

public class HabitContentProvider extends ContentProvider {

  // database
  private HabitDatabaseHelper database;

  // Used for the UriMacher
  private static final int HABITS = 10;
  private static final int HABIT_ID = 20;
  private static final int HABIT_NEW = 21;
  private static final int HABIT_UPDATED = 22;
  private static final int GOALS = 30;
  private static final int GOAL_ID = 40;
  private static final int EVENTS = 50;
  private static final int EVENT_ID = 60;
  private static final int DESCRIPTORS = 70;
  private static final int DESCRIPTOR_ID = 80;
  private static final int READINGS = 90;
  private static final int READING_ID = 100;

  public static final String AUTHORITY = "org.dhappy.habits.contentprovider";

  private static final String HABITS_PATH = "habits";
  public static final Uri HABITS_URI = Uri.parse("content://" + AUTHORITY + "/" + HABITS_PATH);

  private static final String GOALS_PATH = "goals";
  public static final Uri GOALS_URI = Uri.parse("content://" + AUTHORITY + "/" + GOALS_PATH);

  private static final String EVENTS_PATH = "events";
  public static final Uri EVENTS_URI = Uri.parse("content://" + AUTHORITY + "/" + EVENTS_PATH);

  private static final String DESCRIPTORS_PATH = "descriptors";
  public static final Uri DESCRIPTORS_URI = Uri.parse("content://" + AUTHORITY + "/" + DESCRIPTORS_PATH);

  private static final String READINGS_PATH = "readings";
  public static final Uri READINGS_URI = Uri.parse("content://" + AUTHORITY + "/" + READINGS_PATH);

  public static final String HABIT_CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/habits";
  public static final String HABIT_CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/habit";
  
  public static final String GOAL_CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/goals";
  public static final String GOAL_CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/goal";

  public static final String EVENT_CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/events";
  public static final String EVENT_CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/event";

  public static final String DESCRIPTOR_CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/descriptors";
  public static final String DESCRIPTOR_CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/descriptor";

  public static final String READING_CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/readings";
  public static final String READING_CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/reading";

  private static final UriMatcher sURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);
  static {
	    sURIMatcher.addURI(AUTHORITY, HABITS_PATH, HABITS);
	    sURIMatcher.addURI(AUTHORITY, HABITS_PATH + "/*", HABIT_ID);
	    sURIMatcher.addURI(AUTHORITY, HABITS_PATH + "/new_since/#", HABIT_NEW);
	    sURIMatcher.addURI(AUTHORITY, HABITS_PATH + "/updated_since/#", HABIT_UPDATED);
	    sURIMatcher.addURI(AUTHORITY, GOALS_PATH, GOALS);
	    sURIMatcher.addURI(AUTHORITY, GOALS_PATH + "/*", GOAL_ID);
	    sURIMatcher.addURI(AUTHORITY, EVENTS_PATH, EVENTS);
	    sURIMatcher.addURI(AUTHORITY, EVENTS_PATH + "/*", EVENT_ID);
	    sURIMatcher.addURI(AUTHORITY, DESCRIPTORS_PATH, DESCRIPTORS);
	    sURIMatcher.addURI(AUTHORITY, DESCRIPTORS_PATH + "/*", DESCRIPTOR_ID);
	    sURIMatcher.addURI(AUTHORITY, READINGS_PATH, READINGS);
	    sURIMatcher.addURI(AUTHORITY, READINGS_PATH + "/*", READING_ID);
  }
  
  @Override
  public boolean onCreate() {
    database = new HabitDatabaseHelper(getContext());
    return false;
  }

  @Override
  public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
  	// Using SQLiteQueryBuilder instead of query() method
    SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();

    String groupBy = null;
    
    int uriType = sURIMatcher.match(uri);
    switch (uriType) {
    case HABIT_ID:
        queryBuilder.appendWhere(HabitTable.TABLE_HABIT + "." + HabitTable.COLUMN_ID + "=" + uri.getLastPathSegment());
    case HABITS:
    	groupBy = HabitTable.TABLE_HABIT + "." + HabitTable.COLUMN_ID;
        queryBuilder.setTables(HabitTable.TABLE_HABIT + " LEFT OUTER JOIN " + EventTable.TABLE_EVENT
        					   + " ON " + HabitTable.TABLE_HABIT + "." + HabitTable.COLUMN_ID + " = " + EventTable.TABLE_EVENT + "." + EventTable.COLUMN_HABIT_ID);
        break;
    case HABIT_NEW:
        queryBuilder.appendWhere(HabitTable.COLUMN_CREATED_AT + "<" + uri.getLastPathSegment());
        queryBuilder.setTables(HabitTable.TABLE_HABIT);
        break;
    case HABIT_UPDATED:
        queryBuilder.appendWhere(HabitTable.COLUMN_CREATED_AT + "<" + uri.getLastPathSegment()
        						 + " AND " + HabitTable.COLUMN_UPDATED_AT + ">" + uri.getLastPathSegment());
        queryBuilder.setTables(HabitTable.TABLE_HABIT);
        break;
    case GOAL_ID:
        queryBuilder.appendWhere(GoalTable.TABLE_GOAL + "." + GoalTable.COLUMN_ID + "=" + uri.getLastPathSegment());
    case GOALS:
        queryBuilder.setTables(HabitTable.TABLE_HABIT + " JOIN " + GoalTable.TABLE_GOAL
        					   + " ON " + HabitTable.TABLE_HABIT + "." + HabitTable.COLUMN_ID + " = " + GoalTable.TABLE_GOAL + "." + GoalTable.COLUMN_HABIT_ID);
        break;
    case EVENT_ID:
        queryBuilder.appendWhere(EventTable.TABLE_EVENT + "." + EventTable.COLUMN_ID + "=" + uri.getLastPathSegment());
    case EVENTS:
        queryBuilder.setTables(HabitTable.TABLE_HABIT + " JOIN " + EventTable.TABLE_EVENT
				   + " ON " + HabitTable.TABLE_HABIT + "." + HabitTable.COLUMN_ID + " = " + EventTable.TABLE_EVENT + "." + EventTable.COLUMN_HABIT_ID);
        break;
    case DESCRIPTOR_ID:
        queryBuilder.appendWhere(DescriptorTable.TABLE_DESCRIPTOR + "." + DescriptorTable.COLUMN_ID + "=" + uri.getLastPathSegment());
    case DESCRIPTORS:
        queryBuilder.setTables(DescriptorTable.TABLE_DESCRIPTOR);
        break;
    case READING_ID:
        queryBuilder.appendWhere(ReadingTable.TABLE_READING + "." + ReadingTable.COLUMN_ID + "=" + uri.getLastPathSegment());
    case READINGS:
        queryBuilder.setTables(ReadingTable.TABLE_READING);
        break;
    default:
      throw new IllegalArgumentException("Unknown URI: " + uri);
    }

    SQLiteDatabase db = database.getWritableDatabase();
    Cursor cursor = queryBuilder.query(db, projection, selection, selectionArgs, groupBy, null, sortOrder);
    // Make sure that potential listeners are getting notified
    cursor.setNotificationUri(getContext().getContentResolver(), uri);

    return cursor;
  }

  @Override
  public String getType(Uri uri) {
    return null;
  }

  @Override
  public Uri insert(Uri uri, ContentValues values) {
    int uriType = sURIMatcher.match(uri);
    SQLiteDatabase sqlDB = database.getWritableDatabase();
    Uri returnUri;
    
    // Use guids to avoid server conflicts
    if(values.get(HabitTable.COLUMN_ID) == null) {
    	Random generator = new Random();
    	values.put(HabitTable.COLUMN_ID, generator.nextInt());
    }
    
	int currentTime = (int) (Calendar.getInstance().getTimeInMillis() / 1000);

	if(values.get(HabitTable.COLUMN_CREATED_AT) == null) {
    	values.put(HabitTable.COLUMN_CREATED_AT, currentTime);
    }

	if(values.get(HabitTable.COLUMN_UPDATED_AT) == null) {
    	values.put(HabitTable.COLUMN_UPDATED_AT, currentTime);
    }

    long id = 0;
    switch (uriType) {
    case HABITS:
        id = sqlDB.insert(HabitTable.TABLE_HABIT, null, values);
        returnUri = Uri.parse(HABITS_PATH + "/" + id);
        break;
    case GOALS:
        id = sqlDB.insert(GoalTable.TABLE_GOAL, null, values);
        returnUri = Uri.parse(GOALS_PATH + "/" + id);
        break;
    case EVENTS:
        id = sqlDB.insert(EventTable.TABLE_EVENT, null, values);
        returnUri = Uri.parse(EVENTS_PATH + "/" + id);
        break;
    case DESCRIPTORS:
        id = sqlDB.insert(DescriptorTable.TABLE_DESCRIPTOR, null, values);
        returnUri = Uri.parse(DESCRIPTORS_PATH + "/" + id);
        break;
    case READINGS:
        id = sqlDB.insert(ReadingTable.TABLE_READING, null, values);
        returnUri = Uri.parse(READINGS_PATH + "/" + id);
        break;
    default:
      throw new IllegalArgumentException("Unknown URI: " + uri);
    }
    getContext().getContentResolver().notifyChange(uri, null);
    return returnUri;
  }

  @Override
  public int delete(Uri uri, String selection, String[] selectionArgs) {
    int uriType = sURIMatcher.match(uri);
    SQLiteDatabase sqlDB = database.getWritableDatabase();
    int rowsDeleted = 0;
    switch (uriType) {
      case HABITS:
        rowsDeleted = sqlDB.delete(HabitTable.TABLE_HABIT, selection, selectionArgs);
        break;
      case HABIT_ID:
        String id = uri.getLastPathSegment();
        if (TextUtils.isEmpty(selection)) {
          rowsDeleted = sqlDB.delete(HabitTable.TABLE_HABIT, HabitTable.COLUMN_ID + "=" + id, null);
        } else {
          rowsDeleted = sqlDB.delete(HabitTable.TABLE_HABIT, HabitTable.COLUMN_ID + "=" + id + " and " + selection, selectionArgs);
        }
        break;
      case GOALS:
        rowsDeleted = sqlDB.delete(GoalTable.TABLE_GOAL, selection, selectionArgs);
        break;
      case GOAL_ID:
        id = uri.getLastPathSegment();
        if (TextUtils.isEmpty(selection)) {
          rowsDeleted = sqlDB.delete(GoalTable.TABLE_GOAL, GoalTable.COLUMN_ID + "=" + id, null);
        } else {
          rowsDeleted = sqlDB.delete(GoalTable.TABLE_GOAL, GoalTable.COLUMN_ID + "=" + id + " and " + selection, selectionArgs);
        }
        break;
      case EVENTS:
        rowsDeleted = sqlDB.delete(EventTable.TABLE_EVENT, selection, selectionArgs);
        break;
      case EVENT_ID:
	    id = uri.getLastPathSegment();
	    if (TextUtils.isEmpty(selection)) {
	        rowsDeleted = sqlDB.delete(EventTable.TABLE_EVENT, EventTable.COLUMN_ID + "=" + id, null);
	    } else {
	        rowsDeleted = sqlDB.delete(EventTable.TABLE_EVENT, EventTable.COLUMN_ID + "=" + id + " and " + selection, selectionArgs);
	    }
	    break;
      case DESCRIPTORS:
        rowsDeleted = sqlDB.delete(DescriptorTable.TABLE_DESCRIPTOR, selection, selectionArgs);
        break;
      case DESCRIPTOR_ID:
        id = uri.getLastPathSegment();
        if (TextUtils.isEmpty(selection)) {
            rowsDeleted = sqlDB.delete(DescriptorTable.TABLE_DESCRIPTOR, DescriptorTable.COLUMN_ID + "=" + id, null);
        } else {
            rowsDeleted = sqlDB.delete(DescriptorTable.TABLE_DESCRIPTOR, DescriptorTable.COLUMN_ID + "=" + id + " and " + selection, selectionArgs);
        }
        break;
      case READINGS:
        rowsDeleted = sqlDB.delete(ReadingTable.TABLE_READING, selection, selectionArgs);
        break;
      case READING_ID:
        id = uri.getLastPathSegment();
        if (TextUtils.isEmpty(selection)) {
            rowsDeleted = sqlDB.delete(ReadingTable.TABLE_READING, ReadingTable.COLUMN_ID + "=" + id, null);
        } else {
            rowsDeleted = sqlDB.delete(ReadingTable.TABLE_READING, ReadingTable.COLUMN_ID + "=" + id + " and " + selection, selectionArgs);
        }
        break;
    default:
        throw new IllegalArgumentException("Unknown URI: " + uri);
    }
    getContext().getContentResolver().notifyChange(uri, null);
    return rowsDeleted;
  }

  @Override
  public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
    int uriType = sURIMatcher.match(uri);
    SQLiteDatabase sqlDB = database.getWritableDatabase();
    int rowsUpdated = 0;

	int currentTime = (int) (Calendar.getInstance().getTimeInMillis() / 1000);
	values.put(HabitTable.COLUMN_UPDATED_AT, currentTime);

    switch (uriType) {
    case HABITS:
      rowsUpdated = sqlDB.update(HabitTable.TABLE_HABIT, values, selection, selectionArgs);
      break;
    case HABIT_ID:
      String id = uri.getLastPathSegment();
      if (TextUtils.isEmpty(selection)) {
        rowsUpdated = sqlDB.update(HabitTable.TABLE_HABIT, values, HabitTable.COLUMN_ID + "=" + id, null);
      } else {
        rowsUpdated = sqlDB.update(HabitTable.TABLE_HABIT, values, HabitTable.COLUMN_ID + "=" + id + " and " + selection, selectionArgs);
      }
      break;
    case GOALS:
      rowsUpdated = sqlDB.update(GoalTable.TABLE_GOAL, values, selection, selectionArgs);
      break;
    case GOAL_ID:
      id = uri.getLastPathSegment();
      if (TextUtils.isEmpty(selection)) {
        rowsUpdated = sqlDB.update(GoalTable.TABLE_GOAL, values, GoalTable.COLUMN_ID + "=" + id, null);
      } else {
    	 rowsUpdated = sqlDB.update(GoalTable.TABLE_GOAL, values, GoalTable.COLUMN_ID + "=" + id + " and " + selection, selectionArgs);
      }
      break;
    case EVENTS:
      rowsUpdated = sqlDB.update(EventTable.TABLE_EVENT, values, selection, selectionArgs);
      break;
    case EVENT_ID:
      id = uri.getLastPathSegment();
      if (TextUtils.isEmpty(selection)) {
          rowsUpdated = sqlDB.update(EventTable.TABLE_EVENT, values, EventTable.COLUMN_ID + "=" + id, null);
      } else {
          rowsUpdated = sqlDB.update(EventTable.TABLE_EVENT, values, EventTable.COLUMN_ID + "=" + id + " and " + selection, selectionArgs);
      }
      break;
    case READINGS:
      rowsUpdated = sqlDB.update(ReadingTable.TABLE_READING, values, selection, selectionArgs);
      break;
	case READING_ID:
	  id = uri.getLastPathSegment();
	  if (TextUtils.isEmpty(selection)) {
	      rowsUpdated = sqlDB.update(ReadingTable.TABLE_READING, values, ReadingTable.COLUMN_ID + "=" + id, null);
	  } else {
	      rowsUpdated = sqlDB.update(ReadingTable.TABLE_READING, values, ReadingTable.COLUMN_ID + "=" + id + " and " + selection, selectionArgs);
	  }
	  break;
    default:
      throw new IllegalArgumentException("Unknown URI: " + uri);
    }
    getContext().getContentResolver().notifyChange(uri, null);
    return rowsUpdated;
  }
}
