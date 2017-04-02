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

import com.david.simpletweets.adapters.MessagesArrayAdapter;
import com.david.simpletweets.listeners.EndlessRecyclerViewScrollListener;
import com.david.simpletweets.models.Message;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import cz.msebera.android.httpclient.Header;

/**
 * Created by David on 4/2/2017.
 */

public abstract class MessagesListFragment extends RecyclerListFragment {
    List<Message> messages;
    MessagesArrayAdapter aMessages;
    EndlessRecyclerViewScrollListener scrollListener;

    JsonHttpResponseHandler populateHandler;

    long oldestId = -1;

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
        messages = new ArrayList<>();
        aMessages = new MessagesArrayAdapter(getActivity(), messages, currentUser);
    }

    private void setupViews() {
        rvList.setAdapter(aMessages);

        scrollListener = new EndlessRecyclerViewScrollListener((LinearLayoutManager) rvList.getLayoutManager()) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                if (client.isNetworkAvailable()) {
                    Log.d("DEBUG", "scrolling to messages page " + page + " with oldestId: " + oldestId);
                    populate(oldestId - 1, false);
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
                    Log.d("DEBUG", "refreshing messages!");
                    populate(-1, true);
                } else {
                    showNetworkUnavailableMessage();
                    swipeContainer.setRefreshing(false);
                }
            }
        });
    }

    protected void populate(final long oldestId, final boolean refreshing) {
        populateHandler = new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                aMessages.hideFooterProgressBar();
                List<Message> newItems = Message.fromJSONArray(response);
                if (refreshing) {
                    messages.clear();
                    scrollListener.resetState();    //reset scroll listener state so it'll know to load more again
                    messages.addAll(newItems);
                    aMessages.notifyDataSetChanged();
                    rvList.scrollToPosition(0);
                    swipeContainer.setRefreshing(false);
                } else {
                    int curSize = aMessages.getItemCount();
                    messages.addAll(newItems);
                    aMessages.notifyItemRangeInserted(curSize, newItems.size());
                }
                if (newItems.size() > 0) {
                    long newOldestId = newItems.get(newItems.size() - 1).getUid();
                    if (oldestId > newOldestId || oldestId < 0) {
                        MessagesListFragment.this.oldestId = newOldestId;
                        Log.d("DEBUG", "new message oldestId set to: " + MessagesListFragment.this.oldestId);
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
                            populate(oldestId, refreshing);
                        }
                    };
                    handler.postDelayed(runnable, 30000);
                } else {
                    aMessages.hideFooterProgressBar();
                }
            }
        };
    }

    public void addMessageToHead(Message message) {
        this.messages.add(0, message);
        aMessages.notifyItemInserted(0);
        rvList.scrollToPosition(0);
    }
}
