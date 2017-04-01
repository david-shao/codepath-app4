package com.david.simpletweets.fragments;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.david.simpletweets.R;
import com.david.simpletweets.TwitterApplication;
import com.david.simpletweets.databinding.FragmentRecyclerListBinding;
import com.david.simpletweets.decorators.CustomDividerItemDecoration;
import com.david.simpletweets.models.User;
import com.david.simpletweets.network.TwitterClient;

/**
 * Created by David on 3/31/2017.
 */

public class RecyclerListFragment extends Fragment {
    FragmentRecyclerListBinding binding;
    SwipeRefreshLayout swipeContainer;
    RecyclerView rvList;

    TwitterClient client;
    Handler handler;
    User currentUser;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_recycler_list, container, false);

        swipeContainer = binding.swipeContainer;
        rvList = binding.rvList;

        // Set layout manager to position the items
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        rvList.setLayoutManager(layoutManager);
        CustomDividerItemDecoration dividerItemDecoration = new CustomDividerItemDecoration(rvList.getContext());
        rvList.addItemDecoration(dividerItemDecoration);

        swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);

        return binding.getRoot();
    }

    //creation lifecycle event
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        currentUser = getArguments().getParcelable("currentUser");
        handler = new Handler();
        client = TwitterApplication.getRestClient();    //singleton client
    }

    protected void showNetworkUnavailableMessage() {
        Toast.makeText(getActivity(), getResources().getText(R.string.network_unavailable), Toast.LENGTH_SHORT).show();
    }
}
