package com.david.simpletweets.fragments;

import android.os.Bundle;

import com.david.simpletweets.models.Tweet;
import com.david.simpletweets.models.Tweet_Table;
import com.david.simpletweets.models.User;
import com.raizlabs.android.dbflow.sql.language.SQLite;

/**
 * Created by David on 3/30/2017.
 */

public class MentionsTimelineFragment extends TweetsListFragment {

    public static MentionsTimelineFragment newInstance(User currentUser) {
        MentionsTimelineFragment frag = new MentionsTimelineFragment();
        Bundle args = new Bundle();
        args.putParcelable("currentUser", currentUser);
        frag.setArguments(args);
        return frag;
    }

    protected void populateTimeline(final long oldestId, final boolean refreshing) {
        super.populateTimeline(oldestId, refreshing);

        if (client.isNetworkAvailable()) {
            client.getMentionsTimeline(oldestId, tweetsHandler);
        } else {
            showNetworkUnavailableMessage();
            //TODO: this will load all tweets for now
            //load from db if network not available
            tweets.addAll(SQLite.select()
                    .from(Tweet.class)
                    .orderBy(Tweet_Table.uid, false)
                    .queryList());
            aTweets.notifyItemRangeInserted(0, tweets.size());
        }
    }
}
