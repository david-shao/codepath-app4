package com.david.simpletweets.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.View;

import com.codepath.oauth.OAuthLoginActionBarActivity;
import com.david.simpletweets.R;
import com.david.simpletweets.models.User;
import com.david.simpletweets.models.User_Table;
import com.david.simpletweets.network.TwitterClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.raizlabs.android.dbflow.sql.language.SQLite;

import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

//where user will sign in to twitter
public class LoginActivity extends OAuthLoginActionBarActivity<TwitterClient> {

    String preFill;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);

        processOutOfAppIntent();
	}


	// Inflate the menu; this adds items to the action bar if it is present.
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_login, menu);
		return true;
	}

	// OAuth authenticated successfully, launch primary authenticated activity
	// i.e Display application "homepage"
	@Override
	public void onLoginSuccess() {
        //get logged in user
        if (getClient().isNetworkAvailable()) {
            getClient().getLoggedInUser(new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    User user = User.fromJSON(response);
                    user.setLoggedInUser(true);
                    user.save();
                    launchTimeline(user, preFill);
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                    if (errorResponse != null) {
                        Log.d("DEBUG", "Get current user failure " + errorResponse.toString());
                    }
                }
            });
        } else {
            //check if we have logged in before, if so, go to timeline with tweets from db
            User user = SQLite.select()
                    .from(User.class)
                    .where(User_Table.isLoggedInUser.eq(true))
                    .querySingle();
            if (user != null) {
                launchTimeline(user, preFill);
            }
        }
	}

	// OAuth authentication flow failed, handle the error
	// i.e Display an error dialog or toast
	@Override
	public void onLoginFailure(Exception e) {
		e.printStackTrace();
	}

	// Click handler method for the button used to start OAuth flow
	// Uses the client to initiate OAuth authorization
	// This should be tied to a button used to menu_login
	public void loginToRest(View view) {
		getClient().connect();
	}

	private void launchTimeline(User user, String preFill) {
        Intent i = new Intent(LoginActivity.this, TimelineActivity.class);
        i.putExtra("user", user);
        if (!TextUtils.isEmpty(preFill)) {
            i.putExtra("preFill", preFill);
        }
        startActivity(i);
    }

    private void processOutOfAppIntent() {
        // Get intent, action and MIME type
        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();

        if (Intent.ACTION_SEND.equals(action) && type != null) {
            if (type.equals("text/plain")) {
                // Make sure to check whether returned data will be null.
                String titleOfPage = intent.getStringExtra(Intent.EXTRA_SUBJECT);
//                String urlOfPage = intent.getStringExtra(Intent.EXTRA_TEXT);
//                Uri imageUriOfPage = intent.getParcelableExtra(Intent.EXTRA_STREAM);
                preFill = titleOfPage;
            }
        }
    }

}
