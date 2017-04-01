package com.david.simpletweets.fragments;

import android.os.Bundle;

import com.david.simpletweets.models.Tweet;
import com.david.simpletweets.models.Tweet_Table;
import com.david.simpletweets.models.User;
import com.david.simpletweets.models.User_Table;
import com.raizlabs.android.dbflow.sql.language.Join;
import com.raizlabs.android.dbflow.sql.language.SQLite;

/**
 * Created by David on 3/31/2017.
 */

public class UserTimelineFragment extends TweetsListFragment {

    public static UserTimelineFragment newInstance(User currentUser, String screenName) {
        UserTimelineFragment userFragment = new UserTimelineFragment();
        Bundle args = new Bundle();
        args.putParcelable("currentUser", currentUser);
        args.putString("screenName", screenName);
        userFragment.setArguments(args);
        return userFragment;
    }

    protected void populateTimeline(final long oldestId, final boolean refreshing) {
        super.populateTimeline(oldestId, refreshing);

        String screenName = getArguments().getString("screenName");
        if (client.isNetworkAvailable()) {
            client.getUserTimeline(screenName, oldestId, tweetsHandler);
        } else {
            showNetworkUnavailableMessage();
            //load from db if network not available
            tweets.addAll(SQLite.select()
                    .from(Tweet.class)
                    .join(User.class, Join.JoinType.INNER)
                    .on(Tweet_Table.user_uid.eq(User_Table.uid.withTable()))
                    .where(User_Table.screenName.eq(screenName))
                    .orderBy(Tweet_Table.uid.withTable(), false)
                    .queryList());
            aTweets.notifyItemRangeInserted(0, tweets.size());
        }
    }
}
