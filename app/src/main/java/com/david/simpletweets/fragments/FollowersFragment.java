package com.david.simpletweets.fragments;

import android.os.Bundle;

import com.david.simpletweets.models.User;

/**
 * Created by David on 4/1/2017.
 */

public class FollowersFragment extends UsersListFragment {

    public static FollowersFragment newInstance(User currentUser, User targetUser) {
        FollowersFragment frag = new FollowersFragment();
        Bundle args = new Bundle();
        args.putParcelable("currentUser", currentUser);
        args.putParcelable("targetUser", targetUser);
        frag.setArguments(args);
        return frag;
    }

    protected void populate(final long cursor, final boolean refreshing) {
        super.populate(cursor, refreshing);

        if (client.isNetworkAvailable()) {
            client.getFollowers(targetUser.getUid(), cursor, usersHandler);
        } else {
            showNetworkUnavailableMessage();
        }
    }
}
