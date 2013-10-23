/*
 * Copyright (C) 2011 The Android Open Source Project
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
package com.synaptian.smoketracker.habits;

import com.example.android.samplesync.Constants;
import com.synaptian.smoketracker.habits.R;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerFuture;
import android.annotation.SuppressLint;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Toast;

public class StatisticsFragment extends Fragment {
    int mNum;
    private AccountManager mAccountManager;
    private final String ACCOUNT_TYPE = "com.synaptian.smoketracker.habits.sync";
    public static final String AUTHTOKEN_TYPE_FULL_ACCESS = "Full access";

    /**
     * Create a new instance of CountingFragment, providing "num"
     * as an argument.
     */
    static StatisticsFragment newInstance(int num) {
        StatisticsFragment f = new StatisticsFragment();

        // Supply num input as an argument.
        Bundle args = new Bundle();
        args.putInt("num", num);
        f.setArguments(args);

        return f;
    }

    /**
     * When creating, retrieve this instance's number from its arguments.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        mNum = getArguments() != null ? getArguments().getInt("num") : 1;
    	mAccountManager = AccountManager.get(getActivity());
    }
    
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        MenuItem item = menu.add("Sync");
        item.setIcon(android.R.drawable.ic_menu_rotate);
        item.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	Account[] accounts = mAccountManager.getAccountsByType(ACCOUNT_TYPE);
    	Toast.makeText(getActivity(), "Syncing: " + accounts.length, Toast.LENGTH_LONG).show();
    	if(accounts.length == 1) {
    		getExistingAccountAuthToken(accounts[0], Constants.AUTHTOKEN_TYPE);
    	}
		return false;
    }
    
    /**
     * The Fragment's UI is just a simple text view showing its
     * instance number.
     */
    @SuppressLint("SetJavaScriptEnabled")
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.statistics, container, false);

        WebView webView = (WebView) v.findViewById(R.id.webview);
        webView.loadUrl("http://wholcomb.github.io/smoke_tracker/stats.html");
        
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        
        return v;
    }
    
    /**
     * Get the auth token for an existing account on the AccountManager
     * @param account
     * @param authTokenType
     */
    private void getExistingAccountAuthToken(Account account, String authTokenType) {
        final AccountManagerFuture<Bundle> future = mAccountManager.getAuthToken(account, authTokenType, null, getActivity(), null, null);

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Bundle bnd = future.getResult();

                    final String authtoken = bnd.getString(AccountManager.KEY_AUTHTOKEN);
                    showMessage((authtoken != null) ? "SUCCESS!\ntoken: " + authtoken : "FAIL");
                    Log.d("udinic", "GetToken Bundle is " + bnd);
                } catch (Exception e) {
                    e.printStackTrace();
                    showMessage(e.getMessage());
                }
            }
        }).start();
    }

    private void showMessage(final String msg) {
    	if (TextUtils.isEmpty(msg))
            return;

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getActivity(), msg, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
