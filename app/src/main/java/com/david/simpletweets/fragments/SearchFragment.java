package com.david.simpletweets.fragments;

import android.os.Bundle;
import android.util.Log;

import com.david.simpletweets.models.Tweet;
import com.david.simpletweets.models.User;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import cz.msebera.android.httpclient.Header;

/**
 * Created by David on 4/2/2017.
 */

public class SearchFragment extends TweetsListFragment {

    public static final String NAME = "search";

    public static SearchFragment newInstance(User currentUser, String query) {
        SearchFragment frag = new SearchFragment();
        Bundle args = new Bundle();
        args.putParcelable("currentUser", currentUser);
        args.putString("query", query);
        frag.setArguments(args);
        return frag;
    }

    protected void populateTimeline(final long oldestId, final boolean refreshing) {
        super.populateTimeline(oldestId, refreshing);

        String query = getArguments().getString("query");
        if (client.isNetworkAvailable()) {
            aTweets.showFooterProgressBar();
            client.getSearch(query, oldestId, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    aTweets.hideFooterProgressBar();
                    try {
                        JSONArray statuses = response.getJSONArray("statuses");
                        List<Tweet> newItems = Tweet.fromJSONArray(statuses);
                        if (refreshing) {
                            tweets.clear();
                            scrollListener.resetState();    //reset scroll listener state so it'll know to load more again
                            tweets.addAll(newItems);
                            aTweets.notifyDataSetChanged();
                            rvList.scrollToPosition(0);
                            swipeContainer.setRefreshing(false);
                        } else {
                            int curSize = aTweets.getItemCount();
                            tweets.addAll(newItems);
                            aTweets.notifyItemRangeInserted(curSize, newItems.size());
                        }
                        if (newItems.size() > 0) {
                            long newOldestId = newItems.get(newItems.size() - 1).getUid();
                            if (oldestId > newOldestId || oldestId < 0) {
                                SearchFragment.this.oldestId = newOldestId;
                                Log.d("DEBUG", "in search new oldestId set to: " + SearchFragment.this.oldestId);
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
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
                                populateTimeline(oldestId, refreshing);
                            }
                        };
                        handler.postDelayed(runnable, 30000);
                    } else {
                        aTweets.hideFooterProgressBar();
                    }
                }
            });
        } else {
            showNetworkUnavailableMessage();
        }
    }

    @Override
    public String getFragmentName() {
        return NAME;
    }
}
