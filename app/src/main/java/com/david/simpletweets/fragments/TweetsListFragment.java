package com.david.simpletweets.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.david.simpletweets.adapters.TweetsArrayAdapter;
import com.david.simpletweets.listeners.EndlessRecyclerViewScrollListener;
import com.david.simpletweets.models.Tweet;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import cz.msebera.android.httpclient.Header;

/**
 * Created by David on 3/30/2017.
 */

public abstract class TweetsListFragment extends RecyclerListFragment {

    List<Tweet> tweets;
    TweetsArrayAdapter aTweets;
    EndlessRecyclerViewScrollListener scrollListener;

    long oldestId = -1;

    protected AsyncHttpResponseHandler tweetsHandler;

    //inflation logic
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        setupViews();
        populateTimeline(-1, false);

        return view;
    }

    //creation lifecycle event
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setup();
    }

    private void setup() {
        tweets = new ArrayList<>();
        aTweets = new TweetsArrayAdapter(getActivity(), tweets, currentUser);
    }

    private void setupViews() {
        rvList.setAdapter(aTweets);

        scrollListener = new EndlessRecyclerViewScrollListener((LinearLayoutManager) rvList.getLayoutManager()) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                if (client.isNetworkAvailable()) {
                    Log.d("DEBUG", "scrolling to page " + page + " with oldestId: " + oldestId);
                    populateTimeline(oldestId - 1, false);
                } else {
                    showNetworkUnavailableMessage();
                }
            }
        };
        rvList.addOnScrollListener(scrollListener);

        //setup pull down to refresh
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (client.isNetworkAvailable()) {
                    Log.d("DEBUG", "refreshing tweets!");
                    populateTimeline(-1, true);
                } else {
                    showNetworkUnavailableMessage();
                    swipeContainer.setRefreshing(false);
                }
            }
        });
    }

    public void addTweetToHead(Tweet tweet) {
        this.tweets.add(0, tweet);
        aTweets.notifyItemInserted(0);
        rvList.scrollToPosition(0);
    }

    public void addTweetsToHead(List<Tweet> tweets) {
        this.tweets.addAll(0, tweets);
        aTweets.notifyItemRangeInserted(0, tweets.size());
        rvList.scrollToPosition(0);
    }

    protected void populateTimeline(final long oldestId, final boolean refreshing) {
        tweetsHandler = new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                //                Log.d("DEBUG", "success! " + response.toString());
                aTweets.hideFooterProgressBar();
                List<Tweet> newItems = Tweet.fromJSONArray(response);
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
                        TweetsListFragment.this.oldestId = newOldestId;
                        Log.d("DEBUG", "new oldestId set to: " + TweetsListFragment.this.oldestId);
                    }
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
        };
    }
}
