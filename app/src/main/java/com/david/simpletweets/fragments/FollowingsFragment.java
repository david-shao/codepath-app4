package com.david.simpletweets.fragments;

import android.os.Bundle;

import com.david.simpletweets.models.User;

/**
 * Created by David on 4/1/2017.
 */

public class FollowingsFragment extends UsersListFragment  {
    
    public static FollowingsFragment newInstance(User currentUser, User targetUser) {
        FollowingsFragment frag = new FollowingsFragment();
        Bundle args = new Bundle();
        args.putParcelable("currentUser", currentUser);
        args.putParcelable("targetUser", targetUser);
        frag.setArguments(args);
        return frag;
    }

    protected void populate(final long cursor, final boolean refreshing) {
        super.populate(cursor, refreshing);

        if (client.isNetworkAvailable()) {
            client.getFollowings(targetUser.getUid(), cursor, usersHandler);
        } else {
            showNetworkUnavailableMessage();
        }
    }
}
