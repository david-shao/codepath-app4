package com.david.simpletweets.fragments;

import android.util.Log;

import com.david.simpletweets.models.Tweet;
import com.david.simpletweets.models.Tweet_Table;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.raizlabs.android.dbflow.sql.language.SQLite;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

import cz.msebera.android.httpclient.Header;

/**
 * Created by David on 3/30/2017.
 */

public class MentionsTimelineFragment extends TweetsListFragment {
    protected void populateTimeline(final long oldestId, final long newestId, final boolean refreshing) {
        if (client.isNetworkAvailable()) {
            client.getMentionsTimeline(oldestId, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                    //                Log.d("DEBUG", "success! " + response.toString());
                    List<Tweet> newItems = Tweet.fromJSONArray(response);
                    if (refreshing) {
                        tweets.clear();
                        tweets.addAll(newItems);
                        aTweets.notifyDataSetChanged();
                        rvTweets.scrollToPosition(0);
                        swipeContainer.setRefreshing(false);
                    } else {
                        int curSize = aTweets.getItemCount();
                        tweets.addAll(newItems);
                        aTweets.notifyItemRangeInserted(curSize, newItems.size());
                    }
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                    if (errorResponse != null) {
                        Log.d("DEBUG", "failure code: " + statusCode + " " + errorResponse.toString());
                    }
                    //handle rate limit and try again later
                    if (statusCode == 429) {
                        Log.d("DEBUG", "rate limit reached, will try again in 30 seconds.");
                        Runnable runnable = new Runnable() {
                            @Override
                            public void run() {
                                populateTimeline(oldestId, newestId, refreshing);
                            }
                        };
                        handler.postDelayed(runnable, 30000);
                    }
                }
            });
        } else {
            showNetworkUnavailableMessage();
            //load from db if network not available
            tweets.addAll(SQLite.select()
                    .from(Tweet.class)
                    .orderBy(Tweet_Table.uid, false)
                    .queryList());
            aTweets.notifyItemRangeInserted(0, tweets.size());
        }
    }
}
