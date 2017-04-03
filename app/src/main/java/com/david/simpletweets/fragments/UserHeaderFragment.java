package com.david.simpletweets.fragments;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.david.simpletweets.R;
import com.david.simpletweets.TwitterApplication;
import com.david.simpletweets.activities.UsersActivity;
import com.david.simpletweets.databinding.FragmentUserHeaderBinding;
import com.david.simpletweets.models.User;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

public class UserHeaderFragment extends Fragment {

    private User user;
    private User currentUser;
    private FragmentUserHeaderBinding binding;
    private ImageView ivProfileImage;
    private TextView tvFollowersCount;
    private TextView tvFollowingsCount;
    private ImageButton ibFollow;

    public UserHeaderFragment() {
        // Required empty public constructor
    }

    public static UserHeaderFragment newInstance(User user, User currentUser) {
        UserHeaderFragment fragment = new UserHeaderFragment();
        Bundle args = new Bundle();
        args.putParcelable("user", user);
        args.putParcelable("currentUser", currentUser);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        user = getArguments().getParcelable("user");
        currentUser = getArguments().getParcelable("currentUser");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_user_header, container, false);

        binding.setUser(user);
        binding.executePendingBindings();

        ivProfileImage = binding.ivProfileImage;
        Glide.with(getContext()).load(user.getProfileImageUrl())
                .into(ivProfileImage);

        tvFollowersCount = binding.tvFollowersCount;
        tvFollowersCount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getContext(), UsersActivity.class);
                i.putExtra("currentUser", currentUser);
                i.putExtra("user", user);
                i.putExtra("type", UsersListFragment.TYPE_FOLLOWERS);
                startActivity(i);
            }
        });

        tvFollowingsCount = binding.tvFollowingsCount;
        tvFollowingsCount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getContext(), UsersActivity.class);
                i.putExtra("currentUser", currentUser);
                i.putExtra("user", user);
                i.putExtra("type", UsersListFragment.TYPE_FOLLOWINGS);
                startActivity(i);
            }
        });

        ibFollow = binding.ibFollow;
        if (user.getUid() == currentUser.getUid()) {
            ibFollow.setVisibility(View.GONE);
        } else {
            //update button images
            if (user.isFollowing()) {
                ibFollow.setImageResource(R.drawable.ic_people);
            } else {
                ibFollow.setImageResource(R.drawable.ic_person_add);
            }

            //set click listeners
            ibFollow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (user.isFollowing()) {
                        //update UI immediately for responsive UX
                        undoFollow(user);
                        TwitterApplication.getRestClient().postUnfollow(user.getUid(), new JsonHttpResponseHandler() {
                            @Override
                            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                                //we don't update the from response here because we don't want a weird UX
                            }

                            @Override
                            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                                Log.d("DEBUG", "user profile undoFollow failed: " + errorResponse.toString());
                                //revert
                                doFollow(user);
                            }
                        });
                    } else {
                        //update UI immediately for responsive UX
                        doFollow(user);
                        TwitterApplication.getRestClient().postFollow(user.getUid(), new JsonHttpResponseHandler() {
                            @Override
                            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                                //we don't update the from response here because we don't want a weird UX
                            }

                            @Override
                            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                                Log.d("DEBUG", "user profile follow failed: " + errorResponse.toString());
                                //revert
                                undoFollow(user);
                            }
                        });
                    }
                }
            });
        }

        return binding.getRoot();
    }

    protected void doFollow(User user) {
        user.setFollowersCount(user.getFollowersCount() + 1);
        user.setFollowing(true);
        user.save();
        ibFollow.setImageResource(R.drawable.ic_people);
        tvFollowersCount.setText(getString(R.string.tv_followers, user.getFollowersCount()));
    }

    protected void undoFollow(User user) {
        user.setFollowersCount(user.getFollowersCount() - 1);
        user.setFollowing(false);
        user.save();
        ibFollow.setImageResource(R.drawable.ic_person_add);
        tvFollowersCount.setText(getString(R.string.tv_followers, user.getFollowersCount()));
    }
}
