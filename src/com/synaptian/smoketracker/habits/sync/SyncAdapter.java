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

package com.synaptian.smoketracker.habits.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
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
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;

import com.example.android.samplesync.Constants;
import com.synaptian.smoketracker.habits.contentprovider.HabitContentProvider;
import com.synaptian.smoketracker.habits.database.EventTable;
import com.synaptian.smoketracker.habits.database.HabitTable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParserException;

import java.io.BufferedInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.Scanner;

/**
 * Define a sync adapter for the app.
 *
 * <p>This class is instantiated in {@link SyncService}, which also binds SyncAdapter to the system.
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
    //private static final String HOST = "http://smoke-track.herokuapp.com";
    private static final String HOST = "http://192.168.1.113:3000";
    private static final String HABIT_WRITE_URL = HOST + "/habits/";
    private static final String EVENT_WRITE_URL = HOST + "/events/";
    private static final String HABIT_READ_URL = HOST + "/habits.json";

    private static final String PREFERENCES_KEY = "com.synaptian.habits";
    private static final String SYNC_KEY = "com.synaptian.habits.sync.last";
    
    /**
     * Content resolver, for performing database operations.
     */
    private final ContentResolver mContentResolver;

    /**
     * Constructor. Obtains handle to content resolver for later use.
     */
    public SyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        mContentResolver = context.getContentResolver();
    }

    /**
     * Constructor. Obtains handle to content resolver for later use.
     */
    public SyncAdapter(Context context, boolean autoInitialize, boolean allowParallelSyncs) {
        super(context, autoInitialize, allowParallelSyncs);
        mContentResolver = context.getContentResolver();
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
        Log.i(TAG, "Beginning network synchronization");

        try {
        	AccountManager mAccountManager = AccountManager.get(getContext());
        	String authToken = mAccountManager.blockingGetAuthToken(account, Constants.AUTHTOKEN_TYPE, true);
        	
        	Log.i(TAG, "Account / Token: " + account.name + " / " + authToken);
                    	
        	URL habitURL = new URL(HABIT_WRITE_URL);
        	URL eventURL = new URL(EVENT_WRITE_URL);

        	int currentTime = (int) (Calendar.getInstance().getTimeInMillis() / 1000);

        	SharedPreferences prefs = getContext().getSharedPreferences(PREFERENCES_KEY, Context.MODE_PRIVATE);
        	int lastSyncTime = prefs.getInt(SYNC_KEY, 0);
        	
        	// Get new habits from server
        	JSONArray habits = getJSON(new URL(HABIT_READ_URL + "?created_since=" + lastSyncTime), authToken);
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

            // Get updated habits from server
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

            mContentResolver.applyBatch(HabitContentProvider.AUTHORITY, batch);

        	String[] habitProjection = {
            		HabitTable.TABLE_HABIT + "." + HabitTable.COLUMN_ID,
            		HabitTable.COLUMN_NAME,
            		HabitTable.COLUMN_COLOR,
            		HabitTable.TABLE_HABIT + "." + HabitTable.COLUMN_DESCRIPTION };
            Cursor cursor = mContentResolver.query(HabitContentProvider.HABITS_URI, habitProjection, HabitTable.COLUMN_CREATED_AT + ">" + lastSyncTime, null, null);

            if(cursor.moveToFirst()) {
            	do {
                    JSONObject habit = new JSONObject();
                    habit.put("id", cursor.getString(cursor.getColumnIndexOrThrow(HabitTable.COLUMN_ID)));
                    habit.put("color", cursor.getString(cursor.getColumnIndexOrThrow(HabitTable.COLUMN_COLOR)));
                    habit.put("name", cursor.getString(cursor.getColumnIndexOrThrow(HabitTable.COLUMN_NAME)));
                    habit.put("description", cursor.getString(cursor.getColumnIndexOrThrow(HabitTable.COLUMN_DESCRIPTION)));

                    postJSON(habit, habitURL, authToken);
            	} while(cursor.moveToNext());
            }

        	String[] eventProjection = {
            		EventTable.TABLE_EVENT + "." + EventTable.COLUMN_ID,
            		EventTable.COLUMN_HABIT_ID,
            		EventTable.COLUMN_TIME,
            		EventTable.TABLE_EVENT + "." + EventTable.COLUMN_DESCRIPTION };
            cursor = mContentResolver.query(HabitContentProvider.EVENTS_URI, eventProjection, EventTable.COLUMN_CREATED_AT + ">" + lastSyncTime, null, null);

            if(cursor.moveToFirst()) {
            	do {
                    JSONObject event = new JSONObject();
                    event.put("id", cursor.getString(cursor.getColumnIndexOrThrow(EventTable.COLUMN_ID)));
                    event.put("habit_id", cursor.getString(cursor.getColumnIndexOrThrow(EventTable.COLUMN_HABIT_ID)));
                    event.put("time", cursor.getInt(cursor.getColumnIndexOrThrow(EventTable.COLUMN_TIME)));
                    event.put("description", cursor.getString(cursor.getColumnIndexOrThrow(EventTable.COLUMN_DESCRIPTION)));

                    postJSON(event, eventURL, authToken);
            	} while(cursor.moveToNext());
            }
        } catch (OperationCanceledException e) {
			Log.e(TAG, "OperationCanceledException: " + e.getMessage());
		} catch (AuthenticatorException e) {
			Log.e(TAG, "AuthenticatorException: " + e.getMessage());
        } catch(MalformedURLException e) {
            Log.e(TAG, "Bad URL:", e);
        } catch(IOException e) {
            Log.e(TAG, "IOException: " + e.getMessage(), e);
		} catch(JSONException e) {
            Log.e(TAG, "JSONException: " + e.getMessage());
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (OperationApplicationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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

    private JSONArray getJSON(URL url, String authToken) throws IOException, JSONException {
    	URLConnection con = url.openConnection();
    	con.setRequestProperty("Content-Type", "application/json");
        con.setRequestProperty("Authorization", "Bearer " + authToken);
    	InputStream is = new BufferedInputStream(con.getInputStream());
    	Scanner s = new Scanner(is).useDelimiter("\\A");
    	String text = s.hasNext() ? s.next() : "";
    	return new JSONArray(text);
    }
    
    /**
     * Read XML from an input stream, storing it into the content provider.
     *
     * <p>This is where incoming data is persisted, committing the results of a sync. In order to
     * minimize (expensive) disk operations, we compare incoming data with what's already in our
     * database, and compute a merge. Only changes (insert/update/delete) will result in a database
     * write.
     *
     * <p>As an additional optimization, we use a batch operation to perform all database writes at
     * once.
     *
     * <p>Merge strategy:
     * 1. Get cursor to all items in feed<br/>
     * 2. For each item, check if it's in the incoming data.<br/>
     *    a. YES: Remove from "incoming" list. Check if data has mutated, if so, perform
     *            database UPDATE.<br/>
     *    b. NO: Schedule DELETE from database.<br/>
     * (At this point, incoming database only contains missing items.)<br/>
     * 3. For any items remaining in incoming list, ADD to database.
     */
    public void updateLocalFeedData(final InputStream stream, final SyncResult syncResult)
            throws IOException, XmlPullParserException, RemoteException,
            OperationApplicationException, ParseException {
/*
    	final FeedParser feedParser = new FeedParser();
        final ContentResolver contentResolver = getContext().getContentResolver();

        Log.i(TAG, "Parsing stream as Atom feed");
        final List<FeedParser.Entry> entries = feedParser.parse(stream);
        Log.i(TAG, "Parsing complete. Found " + entries.size() + " entries");


        ArrayList<ContentProviderOperation> batch = new ArrayList<ContentProviderOperation>();

        // Build hash table of incoming entries
        HashMap<String, FeedParser.Entry> entryMap = new HashMap<String, FeedParser.Entry>();
        for (FeedParser.Entry e : entries) {
            entryMap.put(e.id, e);
        }

        // Get list of all items
        Log.i(TAG, "Fetching local entries for merge");
        Uri uri = FeedContract.Entry.CONTENT_URI; // Get all entries
        Cursor c = contentResolver.query(uri, PROJECTION, null, null, null);
        assert c != null;
        Log.i(TAG, "Found " + c.getCount() + " local entries. Computing merge solution...");

        // Find stale data
        int id;
        String entryId;
        String title;
        String link;
        long published;
        while (c.moveToNext()) {
            syncResult.stats.numEntries++;
            id = c.getInt(COLUMN_ID);
            entryId = c.getString(COLUMN_ENTRY_ID);
            title = c.getString(COLUMN_TITLE);
            link = c.getString(COLUMN_LINK);
            published = c.getLong(COLUMN_PUBLISHED);
            FeedParser.Entry match = entryMap.get(entryId);
            if (match != null) {
                // Entry exists. Remove from entry map to prevent insert later.
                entryMap.remove(entryId);
                // Check to see if the entry needs to be updated
                Uri existingUri = FeedContract.Entry.CONTENT_URI.buildUpon()
                        .appendPath(Integer.toString(id)).build();
                if ((match.title != null && !match.title.equals(title)) ||
                        (match.link != null && !match.link.equals(link)) ||
                        (match.published != published)) {
                    // Update existing record
                    Log.i(TAG, "Scheduling update: " + existingUri);
                    batch.add(ContentProviderOperation.newUpdate(existingUri)
                            .withValue(FeedContract.Entry.COLUMN_NAME_TITLE, title)
                            .withValue(FeedContract.Entry.COLUMN_NAME_LINK, link)
                            .withValue(FeedContract.Entry.COLUMN_NAME_PUBLISHED, published)
                            .build());
                    syncResult.stats.numUpdates++;
                } else {
                    Log.i(TAG, "No action: " + existingUri);
                }
            } else {
                // Entry doesn't exist. Remove it from the database.
                Uri deleteUri = FeedContract.Entry.CONTENT_URI.buildUpon()
                        .appendPath(Integer.toString(id)).build();
                Log.i(TAG, "Scheduling delete: " + deleteUri);
                batch.add(ContentProviderOperation.newDelete(deleteUri).build());
                syncResult.stats.numDeletes++;
            }
        }
        c.close();

        // Add new items
        for (FeedParser.Entry e : entryMap.values()) {
            Log.i(TAG, "Scheduling insert: entry_id=" + e.id);
            batch.add(ContentProviderOperation.newInsert(FeedContract.Entry.CONTENT_URI)
                    .withValue(FeedContract.Entry.COLUMN_NAME_ENTRY_ID, e.id)
                    .withValue(FeedContract.Entry.COLUMN_NAME_TITLE, e.title)
                    .withValue(FeedContract.Entry.COLUMN_NAME_LINK, e.link)
                    .withValue(FeedContract.Entry.COLUMN_NAME_PUBLISHED, e.published)
                    .build());
            syncResult.stats.numInserts++;
        }
        Log.i(TAG, "Merge solution ready. Applying batch update");
        mContentResolver.applyBatch(FeedContract.CONTENT_AUTHORITY, batch);
        mContentResolver.notifyChange(
                FeedContract.Entry.CONTENT_URI, // URI where data was modified
                null,                           // No local observer
                false);                         // IMPORTANT: Do not sync to network
        // This sample doesn't support uploads, but if *your* code does, make sure you set
        // syncToNetwork=false in the line above to prevent duplicate syncs.
*/
    }
}
