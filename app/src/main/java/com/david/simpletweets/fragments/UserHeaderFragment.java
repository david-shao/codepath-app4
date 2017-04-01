package com.david.simpletweets.fragments;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.david.simpletweets.R;
import com.david.simpletweets.activities.UsersActivity;
import com.david.simpletweets.databinding.FragmentUserHeaderBinding;
import com.david.simpletweets.models.User;

public class UserHeaderFragment extends Fragment {

    private User user;
    private User currentUser;
    private FragmentUserHeaderBinding binding;
    private ImageView ivProfileImage;
    private TextView tvFollowersCount;
    private TextView tvFollowingsCount;

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

        return binding.getRoot();
    }
}
