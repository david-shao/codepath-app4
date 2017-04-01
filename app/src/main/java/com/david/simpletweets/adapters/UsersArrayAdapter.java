package com.david.simpletweets.adapters;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.david.simpletweets.R;
import com.david.simpletweets.activities.ProfileActivity;
import com.david.simpletweets.databinding.ItemUserBinding;
import com.david.simpletweets.models.User;

import java.util.List;

/**
 * Created by David on 3/31/2017.
 */

public class UsersArrayAdapter extends RecyclerView.Adapter<UsersArrayAdapter.ViewHolder> {

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private ItemUserBinding binding;
        private ImageView ivProfileImage;

        public ViewHolder(ItemUserBinding itemView) {
            super(itemView.getRoot());
            itemView.getRoot().setOnClickListener(this);

            this.binding = itemView;

            ivProfileImage = binding.ivProfileImage;
        }

        public void bindUser(User user) {
            binding.setUser(user);
            binding.executePendingBindings();

            Glide.with(getContext()).load(user.getProfileImageUrl())
                    .into(ivProfileImage);
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
    public UsersArrayAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        ItemUserBinding binding = DataBindingUtil.inflate(inflater, R.layout.item_user, parent, false);
        ViewHolder viewHolder = new ViewHolder(binding);

        return viewHolder;
    }

    // Involves populating data into the item through holder
    @Override
    public void onBindViewHolder(UsersArrayAdapter.ViewHolder viewHolder, int position) {
        // Get the data model based on position
        User user = this.users.get(position);
        configureViewHolder(viewHolder, user);
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
        return this.users.size();
    }
}
