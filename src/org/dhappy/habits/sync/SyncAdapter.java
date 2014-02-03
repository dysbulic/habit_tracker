/*
 * Copyright 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.dhappy.habits.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.Context;
import android.content.OperationApplicationException;
import android.content.SharedPreferences;
import android.content.SyncResult;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.os.Bundle;
import android.os.Environment;
import android.os.RemoteException;
import android.util.Log;

import org.dhappy.habits.R;
import org.dhappy.habits.authenticator.AuthenticationService;
import org.dhappy.habits.contentprovider.HabitContentProvider;
import org.dhappy.habits.database.EventTable;
import org.dhappy.habits.database.HabitDatabaseHelper;
import org.dhappy.habits.database.HabitTable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParserException;

import java.io.BufferedInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.channels.FileChannel;
import java.sql.Time;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.Scanner;

/**
 * Define a sync adapter for the app.
 *
 * <p>This class is instantiated in {@link SyncService}, which also 	binds SyncAdapter to the system.
 * SyncAdapter should only be initialized in SyncService, never anywhere else.
 *
 * <p>The system calls onPerformSync() via an RPC call through the IBinder object supplied by
 * SyncService.
 */
class SyncAdapter extends AbstractThreadedSyncAdapter {
    public static final String TAG = "SyncAdapter";

    /**
     * URL to fetch content from during a sync.
     */
    private static String HOST;
    private static String HABIT_WRITE_URL;
    private static String EVENT_WRITE_URL;
    private static String HABIT_READ_URL;
    private static String EVENT_READ_URL;

    private static final String PREFERENCES_KEY = "org.dhappy.habits";
    private static final String SYNC_KEY = "org.dhappy.habits.sync.last";
    
    /**
     * Content resolver, for performing database operations.
     */
    private ContentResolver mContentResolver;
    private Context mContext;

    /**
     * Constructor. Obtains handle to content resolver for later use.
     */
    public SyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        init(context);
    }

    /**
     * Constructor. Obtains handle to content resolver for later use.
     */
    public SyncAdapter(Context context, boolean autoInitialize, boolean allowParallelSyncs) {
        super(context, autoInitialize, allowParallelSyncs);
        init(context);
    }

    final private void init(Context context) {
    	mContext = context;
    	mContentResolver = context.getContentResolver();
    	
    	HOST = context.getText(R.string.server_url).toString();
        HABIT_WRITE_URL = HOST + "/habits/";
        EVENT_WRITE_URL = HOST + "/events/";
        HABIT_READ_URL = HOST + "/habits.json";
        EVENT_READ_URL = HOST + "/events.json";
    }
    
    /**
     * Called by the Android system in response to a request to run the sync adapter. The work
     * required to read data from the network, parse it, and store it in the content provider is
     * done here. Extending AbstractThreadedSyncAdapter ensures that all methods within SyncAdapter
     * run on a background thread. For this reason, blocking I/O and other long-running tasks can be
     * run <em>in situ</em>, and you don't have to set up a separate thread for them.
     .
     *
     * <p>This is where we actually perform any work required to perform a sync.
     * {@link AbstractThreadedSyncAdapter} guarantees that this will be called on a non-UI thread,
     * so it is safe to perform blocking I/O here.
     *
     * <p>The syncResult argument allows you to pass information back to the method that triggered
     * the sync.
     */
    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {
    	SharedPreferences prefs = getContext().getSharedPreferences(PREFERENCES_KEY, Context.MODE_PRIVATE);
    	int lastSyncTime = prefs.getInt(SYNC_KEY, 0);
 
        Log.i(TAG, "Beginning network synchronization: " + lastSyncTime);

        try {
        	AccountManager mAccountManager = AccountManager.get(getContext());
        	AccountManagerFuture<Bundle> future = mAccountManager.getAuthToken(account, AuthenticationService.AUTHTOKEN_TYPE, null, false, null, null);
        	String authToken = mAccountManager.blockingGetAuthToken(account, AuthenticationService.AUTHTOKEN_TYPE, true);
        	
        	Log.i(TAG, "Account / Token: " + account.name + " / " + authToken);
        	
        	Log.i(TAG, "Get new habits from server");

        	JSONArray habits;
        	try {
        		habits = getJSON(new URL(HABIT_READ_URL + "?created_since=" + lastSyncTime), authToken);
        	} catch(IOException ioe) {
            	Log.i(TAG, "Expired auth token; invalidating");
        		mAccountManager.invalidateAuthToken(AuthenticationService.ACCOUNT_TYPE, authToken);
        		authToken = mAccountManager.blockingGetAuthToken(account, AuthenticationService.AUTHTOKEN_TYPE, true);
        		Log.i(TAG, "New auth token: " + authToken);
        		habits = getJSON(new URL(HABIT_READ_URL + "?created_since=" + lastSyncTime), authToken);
        	}

        	ArrayList<ContentProviderOperation> batch = new ArrayList<ContentProviderOperation>();
            for(int i = 0; i < habits.length(); i++) {
                JSONObject habit = habits.getJSONObject(i);
	            batch.add(ContentProviderOperation.newInsert(HabitContentProvider.HABITS_URI)
	                    .withValue(HabitTable.COLUMN_ID, habit.getInt("id"))
	                    .withValue(HabitTable.COLUMN_NAME, habit.getString(HabitTable.COLUMN_NAME))
	                    .withValue(HabitTable.COLUMN_COLOR, habit.getString(HabitTable.COLUMN_COLOR))
	                    .withValue(HabitTable.COLUMN_DESCRIPTION, habit.getString(HabitTable.COLUMN_DESCRIPTION))
	                    .build());
            }

        	Log.i(TAG, "Get updated habits from server");

        	habits = getJSON(new URL(HABIT_READ_URL + "?updated_since=" + lastSyncTime), authToken);
            for(int i = 0; i < habits.length(); i++) {
                JSONObject habit = habits.getJSONObject(i);
	            batch.add(ContentProviderOperation.newUpdate(HabitContentProvider.HABITS_URI)
	                    .withValue(HabitTable.COLUMN_ID, habit.getInt("id"))
	                    .withValue(HabitTable.COLUMN_NAME, habit.getString(HabitTable.COLUMN_NAME))
	                    .withValue(HabitTable.COLUMN_COLOR, habit.getString(HabitTable.COLUMN_COLOR))
	                    .withValue(HabitTable.COLUMN_DESCRIPTION, habit.getString(HabitTable.COLUMN_DESCRIPTION))
	                    .build());
            }

        	Log.i(TAG, "Posting new habits to the server");
            
        	String[] habitProjection = {
            		HabitTable.TABLE_HABIT + "." + HabitTable.COLUMN_ID,
            		HabitTable.COLUMN_NAME,
            		HabitTable.COLUMN_COLOR,
            		HabitTable.TABLE_HABIT + "." + HabitTable.COLUMN_DESCRIPTION };
            Cursor cursor = mContentResolver.query(HabitContentProvider.HABITS_URI, habitProjection, HabitTable.TABLE_HABIT + "." + HabitTable.COLUMN_CREATED_AT + ">=" + lastSyncTime, null, null);
            //Cursor cursor = mContentResolver.query(HabitContentProvider.HABITS_URI, habitProjection, null, null, null);

            if(cursor.moveToFirst()) {
            	JSONArray habitList = new JSONArray();
            	do {
                    JSONObject habit = new JSONObject();
                    habit.put("id", cursor.getString(cursor.getColumnIndexOrThrow(HabitTable.COLUMN_ID)));
                    habit.put("color", cursor.getString(cursor.getColumnIndexOrThrow(HabitTable.COLUMN_COLOR)));
                    habit.put("name", cursor.getString(cursor.getColumnIndexOrThrow(HabitTable.COLUMN_NAME)));
                    habit.put("description", cursor.getString(cursor.getColumnIndexOrThrow(HabitTable.COLUMN_DESCRIPTION)));
                    habitList.put(habit);
            	} while(cursor.moveToNext());
                postJSON(habitList, new URL(HABIT_WRITE_URL), authToken);

            	Log.i(TAG, "Sent Habits: " + habitList.length());
            }

        	Log.i(TAG, "Applying batch operation");

            try {
            	mContentResolver.applyBatch(HabitContentProvider.AUTHORITY, batch);
            } catch(SQLiteConstraintException e) {
            	Log.e(TAG, "SQLiteConstraintException: " + e.getMessage());
            }
            
        	Log.i(TAG, "Get new events from server");

        	JSONArray events;
        	int page = 1;
        	SimpleDateFormat timeFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

            do {
                batch = new ArrayList<ContentProviderOperation>();
        		events = getJSON(new URL(EVENT_READ_URL + "?created_since=" + lastSyncTime + "&page=" + (page++)), authToken);
                for(int i = 0; i < events.length(); i++) {
                	JSONObject event = events.getJSONObject(i);
                	batch.add(ContentProviderOperation.newInsert(HabitContentProvider.EVENTS_URI)
	                    .withValue(EventTable.COLUMN_ID, event.getInt("id"))
	                    .withValue(EventTable.COLUMN_HABIT_ID, event.getInt(EventTable.COLUMN_HABIT_ID))
	                    .withValue(EventTable.COLUMN_TIME, timeFormat.parse(event.getString(EventTable.COLUMN_TIME)).getTime() / 1000)
	                    .withValue(EventTable.COLUMN_DESCRIPTION, event.getString(HabitTable.COLUMN_DESCRIPTION))
	                    .build());
                }

                try {
                	mContentResolver.applyBatch(HabitContentProvider.AUTHORITY, batch);
                } catch(SQLiteConstraintException e) {
                	Log.e(TAG, "SQLiteConstraintException: " + e.getMessage());
                }
            } while(events.length() > 0);
            
        	Log.i(TAG, "Posting new events to the server");

        	String[] eventProjection = {
            		EventTable.TABLE_EVENT + "." + EventTable.COLUMN_ID,
            		EventTable.COLUMN_HABIT_ID,
            		EventTable.COLUMN_TIME,
            		EventTable.TABLE_EVENT + "." + EventTable.COLUMN_DESCRIPTION };
            cursor = mContentResolver.query(HabitContentProvider.EVENTS_URI, eventProjection, EventTable.TABLE_EVENT + "." + EventTable.COLUMN_CREATED_AT + ">" + lastSyncTime, null, null);
            //cursor = mContentResolver.query(HabitContentProvider.EVENTS_URI, eventProjection, null, null, null);

            if(cursor.moveToFirst()) {
            	while(!cursor.isAfterLast()) {
            		JSONArray eventList = new JSONArray();

            		do {
            			JSONObject event = new JSONObject();
            			event.put("id", cursor.getString(cursor.getColumnIndexOrThrow(EventTable.COLUMN_ID)));
            			event.put("habit_id", cursor.getString(cursor.getColumnIndexOrThrow(EventTable.COLUMN_HABIT_ID)));
            			event.put("time", cursor.getInt(cursor.getColumnIndexOrThrow(EventTable.COLUMN_TIME)));
            			event.put("description", cursor.getString(cursor.getColumnIndexOrThrow(EventTable.COLUMN_DESCRIPTION)));
            			eventList.put(event);
                	} while(cursor.moveToNext() && eventList.length() < 500);

                    postJSON(eventList, new URL(EVENT_WRITE_URL), authToken);

                    Log.i(TAG, "Sent Events: " + eventList.length());
            	}
            }
            
        	String state = Environment.getExternalStorageState();
        	if (state.equals(Environment.MEDIA_MOUNTED)) 						{
        	    File root = Environment.getExternalStorageDirectory();
        	    File destination = new File(root, "org.dhappy.habits.db");

	        	FileChannel source = null;
	        	FileChannel copy = null;
        	    try {
        	    	String db = (new HabitDatabaseHelper(mContext)).getWritableDatabase().getPath();
                	Log.i(TAG, "Copying Database: " + db);
        	        source = new FileInputStream(new File(db)).getChannel();
        	        copy = new FileOutputStream(destination).getChannel();
        	        copy.transferFrom(source, 0, source.size());
        	    } finally {
        	    	if(source != null) {
        	        	source.close();
        	        }
        	        if(copy != null) {
        	        	copy.close();
        	        }
        	    }
        	}
            
        	int currentTime = (int) (Calendar.getInstance().getTimeInMillis() / 1000);
        	prefs.edit().putInt(SYNC_KEY, currentTime).apply();
        } catch (OperationCanceledException e) {
			Log.e(TAG, "OperationCanceledException: " + e.getMessage());
		} catch (AuthenticatorException e) {
			Log.e(TAG, "AuthenticatorException: " + e.getMessage());
        } catch(MalformedURLException e) {
            Log.e(TAG, "Bad URL:", e);
        } catch(IOException e) {
            Log.e(TAG, "IOException: " + e.getMessage(), e);
 		} catch(JSONException e) {
            Log.e(TAG, "JSONException", e);
		} catch (RemoteException e) {
            Log.e(TAG, "RemoteException", e);
		} catch (OperationApplicationException e) {
            Log.e(TAG, "OperationApplicationException", e);
		} catch (ParseException e) {
            Log.e(TAG, "ParseException", e);
		} finally {
		}
        Log.i(TAG, "Network synchronization complete");
    }

    private int postJSON(JSONObject json, URL url, String authToken) throws IOException {
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("POST");
        con.setRequestProperty("Content-Type", "application/json");
        con.setRequestProperty("Accept", "application/json");
        con.setRequestProperty("Accept-Encoding", "gzip, deflate");
        con.setRequestProperty("Authorization", "Bearer " + authToken);

        byte[] jsonBytes = json.toString().getBytes();

        con.setRequestProperty("Content-Length", Integer.toString(jsonBytes.length));

        con.setInstanceFollowRedirects(false);
        con.setUseCaches(false);
        con.setDoInput(true);
        con.setDoOutput(true);

        DataOutputStream wr = new DataOutputStream(con.getOutputStream());
        wr.write(jsonBytes);
        wr.flush();
        wr.close();
        
        return con.getResponseCode();
    }

    private int postJSON(JSONArray json, URL url, String authToken) throws IOException {
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("POST");
        con.setRequestProperty("Content-Type", "application/json");
        con.setRequestProperty("Accept", "application/json");
        con.setRequestProperty("Accept-Encoding", "gzip, deflate");
        con.setRequestProperty("Authorization", "Bearer " + authToken);

        byte[] jsonBytes = json.toString().getBytes();

        con.setRequestProperty("Content-Length", Integer.toString(jsonBytes.length));

        con.setInstanceFollowRedirects(false);
        con.setUseCaches(false);
        con.setDoInput(true);
        con.setDoOutput(true);

        DataOutputStream wr = new DataOutputStream(con.getOutputStream());
        wr.write(jsonBytes);
        wr.flush();
        wr.close();
        
        return con.getResponseCode();
    }

    private JSONArray getJSON(URL url, String authToken) throws IOException, JSONException {
    	URLConnection con = url.openConnection();
    	con.setRequestProperty("Content-Type", "application/json");
        con.setRequestProperty("Authorization", "Bearer " + authToken);
    	InputStream is = new BufferedInputStream(con.getInputStream());
    	Scanner s = new Scanner(is).useDelimiter("\\A");
    	String text = s.hasNext() ? s.next() : "";
        Log.i(TAG, "Length: " + text.length());
    	return new JSONArray(text);
    }
}
