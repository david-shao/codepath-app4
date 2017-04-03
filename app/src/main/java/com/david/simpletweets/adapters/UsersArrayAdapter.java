package com.david.simpletweets.adapters;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.david.simpletweets.R;
import com.david.simpletweets.TwitterApplication;
import com.david.simpletweets.activities.ProfileActivity;
import com.david.simpletweets.databinding.FooterProgressBinding;
import com.david.simpletweets.databinding.ItemUserBinding;
import com.david.simpletweets.models.User;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONObject;

import java.util.List;

import cz.msebera.android.httpclient.Header;

/**
 * Created by David on 3/31/2017.
 */

public class UsersArrayAdapter extends FooterArrayAdapter<RecyclerView.ViewHolder> {

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private ItemUserBinding binding;
        private ImageView ivProfileImage;
        private ImageButton ibFollow;

        public ViewHolder(ItemUserBinding itemView) {
            super(itemView.getRoot());
            itemView.getRoot().setOnClickListener(this);

            this.binding = itemView;

            ivProfileImage = binding.ivProfileImage;
            ibFollow = binding.ibFollow;

            //set click listeners
            ibFollow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = getAdapterPosition();
                    final User user = users.get(position);
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
                                Log.d("DEBUG", "undoFollow failed: " + errorResponse.toString());
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
                                Log.d("DEBUG", "follow failed: " + errorResponse.toString());
                                //revert
                                undoFollow(user);
                            }
                        });
                    }
                }
            });
        }

        public void bindUser(User user) {
            binding.setUser(user);
            binding.executePendingBindings();

            if (user.getUid() == currentUser.getUid()) {
                ibFollow.setVisibility(View.GONE);
            } else {
                //update button images
                if (user.isFollowing()) {
                    ibFollow.setImageResource(R.drawable.ic_people);
                } else {
                    ibFollow.setImageResource(R.drawable.ic_person_add);
                }
            }
        }

        protected void doFollow(User user) {
            user.setFollowersCount(user.getFollowersCount() + 1);
            user.setFollowing(true);
            user.save();
            ibFollow.setImageResource(R.drawable.ic_people);
        }

        protected void undoFollow(User user) {
            user.setFollowersCount(user.getFollowersCount() - 1);
            user.setFollowing(false);
            user.save();
            ibFollow.setImageResource(R.drawable.ic_person_add);
        }

        @Override
        public void onClick(View view) {
            int position = getAdapterPosition(); // gets item position
            if (position != RecyclerView.NO_POSITION) {
                User user = users.get(position);
                //show user profile
                Intent i = new Intent(context, ProfileActivity.class);
                i.putExtra("currentUser", currentUser);
                i.putExtra("user", user);
                context.startActivity(i);
            }
        }
    }

    private final int TYPE_GENERIC = 0;

    private List<User> users;
    // Store the context for easy access
    private Context context;

    private User currentUser;

    public UsersArrayAdapter(@NonNull Context context, @NonNull List<User> users, @NonNull User currentUser) {
        this.context = context;
        this.users = users;
        this.currentUser = currentUser;
    }

    // Easy access to the context object in the recyclerview
    private Context getContext() {
        return this.context;
    }

    // Usually involves inflating a layout from XML and returning the holder
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        RecyclerView.ViewHolder viewHolder;

        switch (viewType) {
            case TYPE_FOOTER:
                FooterProgressBinding footerBinding = DataBindingUtil.inflate(inflater, R.layout.footer_progress, parent, false);
                viewHolder = new FooterViewHolder(footerBinding);
                break;
            case TYPE_GENERIC:
            default:
                ItemUserBinding binding = DataBindingUtil.inflate(inflater, R.layout.item_user, parent, false);
                viewHolder = new ViewHolder(binding);
                break;
        }

        return viewHolder;
    }

    // Involves populating data into the item through holder
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        // Get the data model based on position

        if (isPositionFooter(position)) {
            footerViewHolder = (FooterViewHolder) viewHolder;
        } else {
            User user = this.users.get(position);
            configureViewHolder((ViewHolder) viewHolder, user);
        }
    }

    private void configureViewHolder(UsersArrayAdapter.ViewHolder viewHolder, User user) {
        // populate data into subviews
        viewHolder.bindUser(user);

        viewHolder.ivProfileImage.setImageResource(0); //clear out old image for recycled view
        Glide.with(getContext()).load(user.getProfileImageUrl())
                .into(viewHolder.ivProfileImage);
    }

    // Returns the total count of items in the list
    @Override
    public int getItemCount() {
        return this.users.size() + super.getItemCount(); //+1 for footer
    }

    @Override
    public int getItemViewType(int position) {
        if (isPositionFooter(position)) {
            return TYPE_FOOTER;
        }
        return TYPE_GENERIC;
    }

    protected boolean isPositionFooter (int position) {
        return position == this.users.size();
    }

}
