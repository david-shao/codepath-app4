package com.david.simpletweets.adapters;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.david.simpletweets.R;
import com.david.simpletweets.activities.TimelineActivity;
import com.david.simpletweets.activities.TweetDetailsActivity;
import com.david.simpletweets.databinding.ItemTweetBinding;
import com.david.simpletweets.databinding.ItemTweetImageBinding;
import com.david.simpletweets.databinding.ItemTweetVideoBinding;
import com.david.simpletweets.fragments.ComposeTweetFragment;
import com.david.simpletweets.models.Tweet;

import java.util.List;

/**
 * Created by David on 3/23/2017.
 */
// taking Tweet objects and turning them into Views displayed in the list
public class TweetsArrayAdapter extends RecyclerView.Adapter<TweetsArrayAdapter.ViewHolderBase> {

    public abstract class ViewHolderBase extends RecyclerView.ViewHolder implements View.OnClickListener {
        public ImageView ivProfileImage;
        public TextView tvUserName;
        public TextView tvBody;
        public TextView tvName;
        public TextView tvDate;
        public ImageButton ibReply;

        public ViewHolderBase(View itemView) {
            super(itemView);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            int position = getAdapterPosition(); // gets item position
            if (position != RecyclerView.NO_POSITION) { // Check if an item was deleted, but the user clicked it before the UI removed it
                Tweet tweet = tweets.get(position);
                Intent i = new Intent(context, TweetDetailsActivity.class);
                i.putExtra("tweet", tweet);
                i.putExtra("pos", position);
                i.putExtra("currentUser", ((TimelineActivity) context).getCurrentUser());
                ((AppCompatActivity) context).startActivityForResult(i, TimelineActivity.REQUEST_CODE_DETAILS);
            }
        }

        public abstract void bindTweet(Tweet tweet);
    }
    public class ViewHolder extends ViewHolderBase {
        private ItemTweetBinding binding;

        public ViewHolder(ItemTweetBinding itemView) {
            super(itemView.getRoot());

            this.binding = itemView;

            ivProfileImage = binding.ivProfileImage;
            tvName = binding.tvName;
            tvUserName = binding.tvUserName;
            tvBody = binding.tvBody;
            tvDate = binding.tvDate;
            ibReply = binding.ibReply;

            ibReply.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = getAdapterPosition();
                    Tweet tweet = tweets.get(position);
                    FragmentManager fm = ((AppCompatActivity) context).getSupportFragmentManager();
                    ComposeTweetFragment frag = ComposeTweetFragment.newInstance(((TimelineActivity) context).getCurrentUser(), tweet);
                    frag.show(fm, "fragment_reply");
                }
            });
        }

        public void bindTweet(Tweet tweet) {
            binding.setTweet(tweet);
            binding.executePendingBindings();
        }
    }

    public class ImageViewHolder extends ViewHolderBase {
        ItemTweetImageBinding binding;
        ImageView ivEmbedImage;

        public ImageViewHolder(ItemTweetImageBinding itemView) {
            super(itemView.getRoot());
            this.binding = itemView;

            ivProfileImage = binding.ivProfileImage;
            tvName = binding.tvName;
            tvUserName = binding.tvUserName;
            tvBody = binding.tvBody;
            tvDate = binding.tvDate;
            ibReply = binding.ibReply;
            ivEmbedImage = binding.ivEmbedImage;

            ibReply.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = getAdapterPosition();
                    Tweet tweet = tweets.get(position);
                    FragmentManager fm = ((AppCompatActivity) context).getSupportFragmentManager();
                    ComposeTweetFragment frag = ComposeTweetFragment.newInstance(((TimelineActivity) context).getCurrentUser(), tweet);
                    frag.show(fm, "fragment_reply");
                }
            });
        }

        public void bindTweet(Tweet tweet) {
            binding.setTweet(tweet);
            binding.executePendingBindings();

            Glide.with(getContext()).load(tweet.getMediaUrl())
                    .into(ivEmbedImage);
        }
    }

    public class VideoViewHolder extends ViewHolderBase {
        ItemTweetVideoBinding binding;
        ImageView ivEmbedImage;

        public VideoViewHolder(ItemTweetVideoBinding itemView) {
            super(itemView.getRoot());
            this.binding = itemView;

            ivProfileImage = binding.ivProfileImage;
            tvName = binding.tvName;
            tvUserName = binding.tvUserName;
            tvBody = binding.tvBody;
            tvDate = binding.tvDate;
            ibReply = binding.ibReply;
            ivEmbedImage = binding.ivEmbedImage;

            ibReply.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = getAdapterPosition();
                    Tweet tweet = tweets.get(position);
                    FragmentManager fm = ((AppCompatActivity) context).getSupportFragmentManager();
                    ComposeTweetFragment frag = ComposeTweetFragment.newInstance(((TimelineActivity) context).getCurrentUser(), tweet);
                    frag.show(fm, "fragment_reply");
                }
            });
        }

        public void bindTweet(Tweet tweet) {
            binding.setTweet(tweet);
            binding.executePendingBindings();

            Glide.with(getContext()).load(tweet.getMediaUrl())
                    .into(ivEmbedImage);
        }
    }

    private final int GENERIC = 0, IMAGE = 1, VIDEO = 2;

    // Store a member variable for the tweet
    private List<Tweet> tweets;
    // Store the context for easy access
    private Context context;

    public TweetsArrayAdapter(@NonNull Context context, @NonNull List<Tweet> tweets) {
        this.context = context;
        this.tweets = tweets;
    }

    // Easy access to the context object in the recyclerview
    private Context getContext() {
        return this.context;
    }

    // Usually involves inflating a layout from XML and returning the holder
    @Override
    public TweetsArrayAdapter.ViewHolderBase onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        ViewHolderBase viewHolder;

        switch (viewType) {
            case VIDEO:
                ItemTweetVideoBinding bindingVideo = DataBindingUtil.inflate(inflater, R.layout.item_tweet_video, parent, false);
                viewHolder = new VideoViewHolder(bindingVideo);
                break;
            case IMAGE:
                ItemTweetImageBinding bindingImage = DataBindingUtil.inflate(inflater, R.layout.item_tweet_image, parent, false);
                viewHolder = new ImageViewHolder(bindingImage);
                break;
            case GENERIC:
            default:
                ItemTweetBinding binding = DataBindingUtil.inflate(inflater, R.layout.item_tweet, parent, false);
                viewHolder = new ViewHolder(binding);
                break;
        }

        return viewHolder;
    }

    // Involves populating data into the item through holder
    @Override
    public void onBindViewHolder(TweetsArrayAdapter.ViewHolderBase viewHolder, int position) {
        // Get the data model based on position
        Tweet tweet = this.tweets.get(position);
        configureViewHolder(viewHolder, tweet);
    }

    private void configureViewHolder(TweetsArrayAdapter.ViewHolderBase viewHolder, Tweet tweet) {
        // populate data into subviews
        viewHolder.bindTweet(tweet);

        viewHolder.ivProfileImage.setImageResource(0); //clear out old image for recycled view
        Glide.with(getContext()).load(tweet.getUser().getProfileImageUrl())
                .into(viewHolder.ivProfileImage);
    }

    // Returns the total count of items in the list
    @Override
    public int getItemCount() {
        return this.tweets.size();
    }

    @Override
    public int getItemViewType(int position) {
        Tweet tweet = this.tweets.get(position);
        if (!TextUtils.isEmpty(tweet.getMediaType())) {
            if (tweet.getMediaType().equals("photo")) {
                return IMAGE;
            } else if (tweet.getMediaType().equals("video")) {
                return VIDEO;
            }
        }
        return GENERIC;
    }

}
