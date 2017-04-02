package com.david.simpletweets.network;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.codepath.oauth.OAuthBaseClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.scribe.builder.api.Api;
import org.scribe.builder.api.TwitterApi;

import static com.david.simpletweets.models.User_Table.screenName;

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
     * @param handler   Response handler
     */
	public void getHomeTimeline(long oldestId, AsyncHttpResponseHandler handler) {
		String apiUrl = getApiUrl("statuses/home_timeline.json");
		//specify params
		RequestParams params = new RequestParams();
		params.put("count", COUNT);
//        Log.d("DEBUG", "getting home timeline with max_id: " + oldestId);
        if (oldestId > -1) {
            params.put("max_id", oldestId);
        }
		//execute request
		client.get(apiUrl, params, handler);
	}

	public void getMentionsTimeline(long oldestId, AsyncHttpResponseHandler handler) {
        String apiUrl = getApiUrl("statuses/mentions_timeline.json");
        //specify params
        RequestParams params = new RequestParams();
        params.put("count", COUNT);
//        Log.d("DEBUG", "getting mentions timeline with max_id: " + oldestId);
        if (oldestId > -1) {
            params.put("max_id", oldestId);
        }
        //execute request
        client.get(apiUrl, params, handler);
    }

    public void getUserTimeline(String screenName, long oldestId, AsyncHttpResponseHandler handler) {
        String apiUrl = getApiUrl("statuses/user_timeline.json");
        RequestParams params = new RequestParams();
        params.put("count", COUNT);
        params.put("screen_name", screenName);
//        Log.d("DEBUG", "getting user timeline with max_id: " + oldestId);
        if (oldestId > -1) {
            params.put("max_id", oldestId);
        }
        client.get(apiUrl, params, handler);
    }

	public void getLoggedInUser(AsyncHttpResponseHandler handler) {
        String apiUrl = getApiUrl("account/verify_credentials.json");
        client.get(apiUrl, handler);
    }

    public void getFollowers(long userId, long cursor, AsyncHttpResponseHandler handler) {
        String apiUrl = getApiUrl("followers/list.json");
        RequestParams params = new RequestParams();
        params.put("user_id", userId);
        params.put("count", COUNT);
        if (cursor > -1) {
            params.put("cursor", cursor);
        }
        client.get(apiUrl, params, handler);
    }

    public void getFollowings(long userId, long cursor, AsyncHttpResponseHandler handler) {
        String apiUrl = getApiUrl("friends/list.json");
        RequestParams params = new RequestParams();
        params.put("user_id", userId);
        params.put("count", COUNT);
        if (cursor > -1) {
            params.put("cursor", cursor);
        }
        client.get(apiUrl, params, handler);
    }

    public void getSearch(String query, long oldestId, AsyncHttpResponseHandler handler) {
        String apiUrl = getApiUrl("search/tweets.json");
        RequestParams params = new RequestParams();
        params.put("count", COUNT);
        params.put("q", query);
        if (oldestId > -1) {
            params.put("max_id", oldestId);
        }
        client.get(apiUrl, params, handler);
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

    public void postFavorite(long tweetId, AsyncHttpResponseHandler handler) {
        String apiUrl = getApiUrl("favorites/create.json");
        RequestParams params = new RequestParams();
        params.put("id", tweetId);
        client.post(apiUrl, params, handler);
    }

    public void postUnfavorite(long tweetId, AsyncHttpResponseHandler handler) {
        String apiUrl = getApiUrl("favorites/destroy.json");
        RequestParams params = new RequestParams();
        params.put("id", tweetId);
        client.post(apiUrl, params, handler);
    }

    public void postRetweet(long tweetId, AsyncHttpResponseHandler handler) {
        String apiUrl = getApiUrl(String.format("statuses/retweet/%1$d.json", tweetId));
        RequestParams params = new RequestParams();
        params.put("id", tweetId);
        client.post(apiUrl, params, handler);
    }

    public void postUnretweet(long tweetId, AsyncHttpResponseHandler handler) {
        String apiUrl = getApiUrl(String.format("statuses/unretweet/%1$d.json", tweetId));
        RequestParams params = new RequestParams();
        params.put("id", tweetId);
        client.post(apiUrl, params, handler);
    }

    public boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
//        Log.d("DEBUG", "network available returning: " + (activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting()));
        return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting();
    }
}
