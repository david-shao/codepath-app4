package com.david.simpletweets.activities;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.david.simpletweets.R;
import com.david.simpletweets.TwitterApplication;
import com.david.simpletweets.adapters.TweetsArrayAdapter;
import com.david.simpletweets.databinding.ActivityTimelineBinding;
import com.david.simpletweets.decorators.CustomDividerItemDecoration;
import com.david.simpletweets.fragments.ComposeTweetFragment;
import com.david.simpletweets.listeners.EndlessRecyclerViewScrollListener;
import com.david.simpletweets.models.Tweet;
import com.david.simpletweets.models.Tweet_Table;
import com.david.simpletweets.models.User;
import com.david.simpletweets.network.TwitterClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.raizlabs.android.dbflow.sql.language.SQLite;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import cz.msebera.android.httpclient.Header;

public class TimelineActivity extends AppCompatActivity implements ComposeTweetFragment.ComposeTweetListener {

    public static final int REQUEST_CODE_DETAILS = 20;

    private Toolbar toolbar;
    private User currentUser;
    private TwitterClient client;
    private List<Tweet> tweets;
    private TweetsArrayAdapter aTweets;
    private EndlessRecyclerViewScrollListener scrollListener;
    private RecyclerView rvTweets;
    private SwipeRefreshLayout swipeContainer;
    private FloatingActionButton fabCompose;

    private Handler handler;

    private ActivityTimelineBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_timeline);

        toolbar = binding.toolbar;
        setSupportActionBar(toolbar);

        client = TwitterApplication.getRestClient();    //singleton client
        currentUser = getIntent().getParcelableExtra("user");
        handler = new Handler();

        processIntent();
        setupViews();
        populateTimeline(-1, -1, false);
    }

    private void setupViews() {
        swipeContainer = binding.swipeContainer;
        rvTweets = binding.rvTweets;
        tweets = new ArrayList<>();
        aTweets = new TweetsArrayAdapter(this, tweets);
        rvTweets.setAdapter(aTweets);
        fabCompose = binding.fabCompose;

        //update color
        fabCompose.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.bgColor)));

        // Set layout manager to position the items
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
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

    private void processIntent() {
        String preFill = getIntent().getStringExtra("preFill");
        if (!TextUtils.isEmpty(preFill)) {
            if (client.isNetworkAvailable()) {
                FragmentManager fm = getSupportFragmentManager();
                ComposeTweetFragment frag = ComposeTweetFragment.newInstance(currentUser, preFill);
                frag.show(fm, "fragment_compose");
            } else {
                showNetworkUnavailableMessage();
            }
        }
    }

    //send api request to get timeline json
    //fill listview by creating the tweet objects from json
    private void populateTimeline(final long oldestId, final long newestId, final boolean refreshing) {
        if (client.isNetworkAvailable()) {
            client.getHomeTimeline(oldestId, newestId, new JsonHttpResponseHandler() {
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_timeline, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        return true;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    @Override
    public void onTweet(Tweet tweet) {
        tweets.add(0, tweet);
        aTweets.notifyItemInserted(0);
        rvTweets.scrollToPosition(0);
    }

    /**
     * Callback when other activities finish.
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK && requestCode == REQUEST_CODE_DETAILS) {
            List<Tweet> replies = data.getParcelableArrayListExtra("replies");
            if (!replies.isEmpty()) {
                tweets.addAll(0, replies);
                aTweets.notifyItemRangeInserted(0, replies.size());
                rvTweets.scrollToPosition(0);
            }
        }
    }

    private void showNetworkUnavailableMessage() {
        Toast.makeText(this, getResources().getText(R.string.network_unavailable), Toast.LENGTH_SHORT).show();
    }

    public void onCompose(View view) {
        if (client.isNetworkAvailable()) {
            FragmentManager fm = getSupportFragmentManager();
            ComposeTweetFragment frag = ComposeTweetFragment.newInstance(currentUser);
//            frag.setStyle(DialogFragment.STYLE_NORMAL, R.style.Dialog_FullScreen);
            frag.show(fm, "fragment_compose");
        } else {
            showNetworkUnavailableMessage();
        }
    }
}
