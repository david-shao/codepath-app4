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

import com.david.simpletweets.adapters.UsersArrayAdapter;
import com.david.simpletweets.listeners.EndlessRecyclerViewScrollListener;
import com.david.simpletweets.models.User;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import cz.msebera.android.httpclient.Header;

/**
 * Created by David on 3/31/2017.
 */

public abstract class UsersListFragment extends RecyclerListFragment {

    public static final String TYPE_FOLLOWERS = "followers";
    public static final String TYPE_FOLLOWINGS = "followings";

    List<User> users;
    UsersArrayAdapter aUsers;
    EndlessRecyclerViewScrollListener scrollListener;

    JsonHttpResponseHandler usersHandler;
    User targetUser;

    long nextCursor = -1;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        setupViews();
        populate(-1, false);

        return view;
    }

    //creation lifecycle event
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setup();
    }

    private void setup() {
        users = new ArrayList<>();
        aUsers = new UsersArrayAdapter(getActivity(), users, currentUser);
        targetUser = getArguments().getParcelable("targetUser");
    }

    private void setupViews() {
        rvList.setAdapter(aUsers);

        scrollListener = new EndlessRecyclerViewScrollListener((LinearLayoutManager) rvList.getLayoutManager()) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                if (client.isNetworkAvailable()) {
                    if (nextCursor != 0) {
                        Log.d("DEBUG", "scrolling to page " + page + " with cursor: " + nextCursor);
                        populate(nextCursor, false);
                    }
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
                    Log.d("DEBUG", "refreshing users!");
                    populate(-1, true);
                } else {
                    showNetworkUnavailableMessage();
                    swipeContainer.setRefreshing(false);
                }
            }
        });
    }

    protected void populate(final long cursor, final boolean refreshing) {
        usersHandler = new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    JSONArray usersJson = response.getJSONArray("users");
                    List<User> newItems = User.fromJSONArray(usersJson);
                    if (refreshing) {
                        users.clear();
                        scrollListener.resetState();    //reset scroll listener state so it'll know to load more again
                        users.addAll(newItems);
                        aUsers.notifyDataSetChanged();
                        rvList.scrollToPosition(0);
                        swipeContainer.setRefreshing(false);
                    } else {
                        int curSize = aUsers.getItemCount();
                        users.addAll(newItems);
                        aUsers.notifyItemRangeInserted(curSize, newItems.size());
                    }
                    if (response.has("next_cursor")) {
                        nextCursor = response.getLong("next_cursor");
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
                            populate(cursor, refreshing);
                        }
                    };
                    handler.postDelayed(runnable, 30000);
                }
            }
        };
    }
}
