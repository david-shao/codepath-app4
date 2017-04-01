package com.david.simpletweets.fragments;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.david.simpletweets.R;
import com.david.simpletweets.TwitterApplication;
import com.david.simpletweets.adapters.TweetsArrayAdapter;
import com.david.simpletweets.databinding.FragmentTweetsListBinding;
import com.david.simpletweets.decorators.CustomDividerItemDecoration;
import com.david.simpletweets.listeners.EndlessRecyclerViewScrollListener;
import com.david.simpletweets.models.Tweet;
import com.david.simpletweets.models.User;
import com.david.simpletweets.network.TwitterClient;
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

public abstract class TweetsListFragment extends Fragment {

    FragmentTweetsListBinding binding;
    SwipeRefreshLayout swipeContainer;
    RecyclerView rvTweets;
    List<Tweet> tweets;
    TweetsArrayAdapter aTweets;
    EndlessRecyclerViewScrollListener scrollListener;

    TwitterClient client;
    Handler handler;
    User currentUser;

    protected AsyncHttpResponseHandler tweetsHandler;

    public TweetsListFragment() {

    }

    //inflation logic
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_tweets_list, container, false);

        setupViews();
        populateTimeline(-1, -1, false);

        return binding.getRoot();
    }

    //creation lifecycle event
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setup();
    }

    private void setup() {
        currentUser = getArguments().getParcelable("currentUser");
        tweets = new ArrayList<>();
        aTweets = new TweetsArrayAdapter(getActivity(), tweets, currentUser);
        handler = new Handler();
        client = TwitterApplication.getRestClient();    //singleton client
    }

    private void setupViews() {
        swipeContainer = binding.swipeContainer;
        rvTweets = binding.rvTweets;
        rvTweets.setAdapter(aTweets);

        // Set layout manager to position the items
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        rvTweets.setLayoutManager(layoutManager);
        CustomDividerItemDecoration dividerItemDecoration = new CustomDividerItemDecoration(rvTweets.getContext());
        rvTweets.addItemDecoration(dividerItemDecoration);

        scrollListener = new EndlessRecyclerViewScrollListener(layoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                if (client.isNetworkAvailable()) {
                    Log.d("DEBUG", "scrolling to page " + (page));
                    populateTimeline(Tweet.getOldestId() - 1, -1, false);
                } else {
                    showNetworkUnavailableMessage();
                }
            }
        };
        rvTweets.addOnScrollListener(scrollListener);

        //setup pull down to refresh
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (client.isNetworkAvailable()) {
                    Log.d("DEBUG", "refreshing tweets!");
//                    populateTimeline(-1, Tweet.getNewestId(), true);
                    populateTimeline(-1, -1, true);
                } else {
                    showNetworkUnavailableMessage();
                    swipeContainer.setRefreshing(false);
                }
            }
        });
        swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);
    }

    protected void showNetworkUnavailableMessage() {
        Toast.makeText(getActivity(), getResources().getText(R.string.network_unavailable), Toast.LENGTH_SHORT).show();
    }

    public void addTweetToHead(Tweet tweet) {
        this.tweets.add(0, tweet);
        aTweets.notifyItemInserted(0);
        rvTweets.scrollToPosition(0);
    }

    public void addTweetsToHead(List<Tweet> tweets) {
        this.tweets.addAll(0, tweets);
        aTweets.notifyItemRangeInserted(0, tweets.size());
        rvTweets.scrollToPosition(0);
    }

    protected void populateTimeline(final long oldestId, final long newestId, final boolean refreshing) {
        tweetsHandler = new JsonHttpResponseHandler() {
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
        };
    }
}
