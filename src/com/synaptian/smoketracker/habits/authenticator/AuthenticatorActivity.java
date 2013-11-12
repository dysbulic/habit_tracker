/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.synaptian.smoketracker.habits.authenticator;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.oltu.oauth2.client.OAuthClient;
import org.apache.oltu.oauth2.client.URLConnectionClient;
import org.apache.oltu.oauth2.client.request.OAuthClientRequest;
import org.apache.oltu.oauth2.client.response.OAuthJSONAccessTokenResponse;
import org.apache.oltu.oauth2.common.exception.OAuthProblemException;
import org.apache.oltu.oauth2.common.exception.OAuthSystemException;
import org.apache.oltu.oauth2.common.message.types.GrantType;

import com.example.android.samplesync.Constants;
import com.synaptian.smoketracker.habits.R;

import android.accounts.Account;
import android.accounts.AccountAuthenticatorActivity;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Activity which displays login screen to the user.
 */
public class AuthenticatorActivity extends AccountAuthenticatorActivity {
    /** The Intent flag to confirm credentials. */
    public static final String PARAM_CONFIRM_CREDENTIALS = "confirmCredentials";

    /** The Intent extra to store password. */
    public static final String PARAM_PASSWORD = "password";

    /** The Intent extra to store username. */
    public static final String PARAM_USERNAME = "username";

    /** The Intent extra to store username. */
    public static final String PARAM_AUTHTOKEN_TYPE = "authtokenType";

    /** The tag used to log to adb console. */
    private static final String TAG = "AuthenticatorActivity";
    private AccountManager mAccountManager;

    /** Keep track of the progress dialog so we can dismiss it */
    private ProgressDialog mProgressDialog = null;

    /**
     * If set we are just checking that the user knows their credentials; this
     * doesn't cause the user's password or authToken to be changed on the
     * device.
     */
    private Boolean mConfirmCredentials = false;

    /** for posting authentication attempts back to UI thread */
    private final Handler mHandler = new Handler();

    private TextView mMessage;

    private String mPassword;

    private EditText mPasswordEdit;

    /** Was the original caller asking for an entirely new account? */
    protected boolean mRequestNewAccount = false;

    private String mUsername;

    private EditText mUsernameEdit;

    /**
     * {@inheritDoc}
     */
    @Override
    public void onCreate(Bundle icicle) {
        Log.i(TAG, "onCreate(" + icicle + ")");
        super.onCreate(icicle);
        Log.i(TAG, "loading data from Intent");
        final Intent intent = getIntent();
        setContentView(R.layout.login_activity);

    	final String host = "http://smoke-track.herokuapp.com";
    	final String authUri = host + "/oauth/authorize";
    	final String tokenUri = host + "/oauth/token";
        final String appUri = host + "/habits";

        final String callback = getText(R.string.oauth_callback).toString();
    	
        final String clientId = getText(R.string.oauth_id).toString();
    	final String secret = getText(R.string.oauth_secret).toString();
    	
        try {
        	OAuthClientRequest request = OAuthClientRequest
				.authorizationLocation(authUri)
			    .setClientId(clientId)
			    .setRedirectURI(callback)
				.setResponseType("code")
			    .buildQueryMessage();
        	
        	final Activity parentActivity = this;

            WebView webView = (WebView) findViewById(R.id.webview);
            webView.setWebViewClient(new WebViewClient() {
                @Override
                public void onReceivedError(WebView view, int errorCode,
                        String description, String failingUrl) {
                    Log.i(TAG, "WebView Error: " + description);
                }

                @Override
                public boolean shouldOverrideUrlLoading(WebView view, String url) {
                    view.loadUrl(url);
                    return true;
                }

                @Override
                public void onLoadResource (WebView view, String url) {
                	if(url.startsWith(callback)) {
                		Pattern p = Pattern.compile("\\?code=([^&]+)");
                		Matcher m = p.matcher(url);

                		final String code = m.find() ? m.group(1) : "Not Found";
                		
            		    new AsyncTask<Void, Void, OAuthJSONAccessTokenResponse>() {

            		        protected OAuthJSONAccessTokenResponse doInBackground(Void... args) {
        						try {
        	                		OAuthClientRequest request = OAuthClientRequest
        							     .tokenLocation(tokenUri)
        							     .setGrantType(GrantType.AUTHORIZATION_CODE)
        							     .setClientId(clientId)
        							     .setClientSecret(secret)
        							     .setRedirectURI(callback)
        							     .setCode(code)
        							     .buildBodyMessage();
        	                         
        	                         OAuthClient oAuthClient = new OAuthClient(new URLConnectionClient());

        	                         return oAuthClient.accessToken(request, OAuthJSONAccessTokenResponse.class);
        						} catch (OAuthSystemException e) {
        							e.printStackTrace();
        						} catch (OAuthProblemException e) {
        							e.printStackTrace();
        						}
        						return null;
            		        }

            		        protected void onPostExecute(OAuthJSONAccessTokenResponse oAuthResponse) {
            		        	String authToken = oAuthResponse.getAccessToken();
    	                        Toast.makeText(parentActivity, authToken, Toast.LENGTH_SHORT).show();

    	                        String accountName = getText(R.string.oauth_account_name).toString();
    	                        String accountType = getText(R.string.oauth_account_type).toString();
    	                        final Account account = new Account(accountName, accountType);
    	                        AccountManager mAccountManager = AccountManager.get(getBaseContext());
	                            mAccountManager.addAccountExplicitly(account, null, null);
	                            mAccountManager.setAuthToken(account, Constants.AUTHTOKEN_TYPE, authToken);

	                            Bundle data = new Bundle();
	                            data.putString(AccountManager.KEY_ACCOUNT_NAME, accountName);
	                            data.putString(AccountManager.KEY_ACCOUNT_TYPE, accountType);
	                            data.putString(AccountManager.KEY_AUTHTOKEN, authToken);
	                            setAccountAuthenticatorResult(data);
	                            finish();
            		        }
            		    }.execute((Void) null);
                	}
                }
            });
            webView.loadUrl(request.getLocationUri());

        } catch (OAuthSystemException e) {
			e.printStackTrace();
		}
    }

    /**
     * Called when response is received from the server for confirm credentials
     * request. See onAuthenticationResult(). Sets the
     * AccountAuthenticatorResult which is sent back to the caller.
     *
     * @param result the confirmCredentials result.
     */
    private void finishConfirmCredentials(boolean result) {
        Log.i(TAG, "finishConfirmCredentials()");
        final Account account = new Account(mUsername, Constants.ACCOUNT_TYPE);
        mAccountManager.setPassword(account, mPassword);
        final Intent intent = new Intent();
        intent.putExtra(AccountManager.KEY_BOOLEAN_RESULT, result);
        setAccountAuthenticatorResult(intent.getExtras());
        setResult(RESULT_OK, intent);
        finish();
    }

    /**
     * Called when response is received from the server for authentication
     * request. See onAuthenticationResult(). Sets the
     * AccountAuthenticatorResult which is sent back to the caller. We store the
     * authToken that's returned from the server as the 'password' for this
     * account - so we're never storing the user's actual password locally.
     *
     * @param result the confirmCredentials result.
     */
    private void finishLogin(String authToken) {

        Log.i(TAG, "finishLogin()");
        final Account account = new Account(mUsername, Constants.ACCOUNT_TYPE);
        if (mRequestNewAccount) {
            mAccountManager.addAccountExplicitly(account, mPassword, null);
            // Set contacts sync for this account.
            ContentResolver.setSyncAutomatically(account, ContactsContract.AUTHORITY, true);
        } else {
            mAccountManager.setPassword(account, mPassword);
        }
        final Intent intent = new Intent();
        intent.putExtra(AccountManager.KEY_ACCOUNT_NAME, mUsername);
        intent.putExtra(AccountManager.KEY_ACCOUNT_TYPE, Constants.ACCOUNT_TYPE);
        setAccountAuthenticatorResult(intent.getExtras());
        setResult(RESULT_OK, intent);
        finish();
    }

    /**
     * Called when the authentication process completes (see attemptLogin()).
     *
     * @param authToken the authentication token returned by the server, or NULL if
     *            authentication failed.
     */
    public void onAuthenticationResult(String authToken) {

        boolean success = ((authToken != null) && (authToken.length() > 0));
        Log.i(TAG, "onAuthenticationResult(" + success + ")");

        // Hide the progress dialog
        hideProgress();

        if (success) {
            if (!mConfirmCredentials) {
                finishLogin(authToken);
            } else {
                finishConfirmCredentials(success);
            }
        } else {
            Log.e(TAG, "onAuthenticationResult: failed to authenticate");
            if (mRequestNewAccount) {
                // "Please enter a valid username/password.
                mMessage.setText(getText(R.string.login_activity_loginfail_text_both));
            } else {
                // "Please enter a valid password." (Used when the
                // account is already in the database but the password
                // doesn't work.)
                mMessage.setText(getText(R.string.login_activity_loginfail_text_pwonly));
            }
        }
    }

    public void onAuthenticationCancel() {
        Log.i(TAG, "onAuthenticationCancel()");

        // Hide the progress dialog
        hideProgress();
    }

    /**
     * Returns the message to be displayed at the top of the login dialog box.
     */
    private CharSequence getMessage() {
        getString(R.string.label);
        if (TextUtils.isEmpty(mUsername)) {
            // If no username, then we ask the user to log in using an
            // appropriate service.
            final CharSequence msg = getText(R.string.login_activity_newaccount_text);
            return msg;
        }
        if (TextUtils.isEmpty(mPassword)) {
            // We have an account but no password
            return getText(R.string.login_activity_loginfail_text_pwmissing);
        }
        return null;
    }

    /**
     * Shows the progress UI for a lengthy operation.
     */
    private void showProgress() {
        showDialog(0);
    }

    /**
     * Hides the progress UI for a lengthy operation.
     */
    private void hideProgress() {
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
            mProgressDialog = null;
        }
    }
}
