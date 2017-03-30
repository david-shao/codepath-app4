package com.david.simpletweets.network;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.codepath.oauth.OAuthBaseClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.scribe.builder.api.Api;
import org.scribe.builder.api.TwitterApi;

/*
 * 
 * This is the object responsible for communicating with a REST API. 
 * Specify the constants below to change the API being communicated with.
 * See a full list of supported API classes: 
 *   https://github.com/fernandezpablo85/scribe-java/tree/master/src/main/java/org/scribe/builder/api
 * Key and Secret are provided by the developer site for the given API i.e dev.twitter.com
 * Add methods for each relevant endpoint in the API.
 * 
 * NOTE: You may want to rename this object based on the service i.e TwitterClient or FlickrClient
 * 
 */
public class TwitterClient extends OAuthBaseClient {
	public static final Class<? extends Api> REST_API_CLASS = TwitterApi.class; // Change this
	public static final String REST_URL = "https://api.twitter.com/1.1"; // Change this, base API URL
	public static final String REST_CONSUMER_KEY = "xL5OtMqAomSSEcZRp5fJcw8lP";       // Change this
	public static final String REST_CONSUMER_SECRET = "SldqKjoSGqifyxfwhupgNyYfVieFJMsuTsN1Vt70mogdaPoppm"; // Change this
	public static final String REST_CALLBACK_URL = "oauth://cpsimpletweets"; // Change this (here and in manifest)

    public static final int COUNT = 25;

	public TwitterClient(Context context) {
		super(context, REST_API_CLASS, REST_URL, REST_CONSUMER_KEY, REST_CONSUMER_SECRET, REST_CALLBACK_URL);
	}

	/* 1. Define the endpoint URL with getApiUrl and pass a relative path to the endpoint
	 * 	  i.e getApiUrl("statuses/home_timeline.json");
	 * 2. Define the parameters to pass to the request (query or body)
	 *    i.e RequestParams params = new RequestParams("foo", "bar");
	 * 3. Define the request method and make a call to the client
	 *    i.e client.get(apiUrl, params, handler);
	 *    i.e client.post(apiUrl, params, handler);
	 */

    /**
     * Gets tweets from home timeline.
     * @param oldestId  Gets tweets older than oldestId, default to -1
     * @param newestId  Gets tweets newer than newestId, default to -1
     * @param handler   Response handler
     */
	public void getHomeTimeline(long oldestId, long newestId, AsyncHttpResponseHandler handler) {
		String apiUrl = getApiUrl("statuses/home_timeline.json");
		//specify params
		RequestParams params = new RequestParams();
		params.put("count", COUNT);
        if (oldestId > -1) {
            params.put("max_id", oldestId);
        }
        if (newestId > -1) {
            params.put("since_id", newestId);
        }
		//execute request
		client.get(apiUrl, params, handler);
	}

	public void getLoggedInUser(AsyncHttpResponseHandler handler) {
        String apiUrl = getApiUrl("account/verify_credentials.json");
        client.get(apiUrl, handler);
    }

	public void postStatusUpdate(String body, long replyTweetId, AsyncHttpResponseHandler handler) {
        String apiUrl = getApiUrl("statuses/update.json");

        RequestParams params = new RequestParams();
        params.put("status", body);
        if (replyTweetId > 0) {
            params.put("in_reply_to_status_id", replyTweetId);
        }

        client.post(apiUrl, params, handler);
    }

    public boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        Log.d("DEBUG", "network available returning: " + (activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting()));
        return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting();
    }
}
