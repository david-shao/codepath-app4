package com.david.simpletweets.fragments;

import android.os.Bundle;

import com.david.simpletweets.models.Tweet;
import com.david.simpletweets.models.Tweet_Table;
import com.david.simpletweets.models.User;
import com.raizlabs.android.dbflow.sql.language.SQLite;

/**
 * Created by David on 3/30/2017.
 */

public class HomeTimelineFragment extends TweetsListFragment {

    public static HomeTimelineFragment newInstance(User currentUser) {
        HomeTimelineFragment frag = new HomeTimelineFragment();
        Bundle args = new Bundle();
        args.putParcelable("currentUser", currentUser);
        frag.setArguments(args);
        return frag;
    }

    //send api request to get timeline json
    //fill listview by creating the tweet objects from json
    protected void populateTimeline(final long oldestId, final long newestId, final boolean refreshing) {
        super.populateTimeline(oldestId, newestId, refreshing);

        if (client.isNetworkAvailable()) {
            client.getHomeTimeline(oldestId, newestId, tweetsHandler);
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
