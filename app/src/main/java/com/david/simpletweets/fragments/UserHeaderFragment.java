package com.david.simpletweets.fragments;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.david.simpletweets.R;
import com.david.simpletweets.databinding.FragmentUserHeaderBinding;
import com.david.simpletweets.models.User;

public class UserHeaderFragment extends Fragment {

    private User user;
    private FragmentUserHeaderBinding binding;
    private ImageView ivProfileImage;

    public UserHeaderFragment() {
        // Required empty public constructor
    }

    public static UserHeaderFragment newInstance(User user) {
        UserHeaderFragment fragment = new UserHeaderFragment();
        Bundle args = new Bundle();
        args.putParcelable("user", user);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        user = getArguments().getParcelable("user");
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

        return binding.getRoot();
    }
}
