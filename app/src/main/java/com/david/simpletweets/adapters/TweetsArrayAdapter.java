package com.david.simpletweets.adapters;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
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
import com.david.simpletweets.activities.ProfileActivity;
import com.david.simpletweets.activities.TimelineActivity;
import com.david.simpletweets.activities.TweetDetailsActivity;
import com.david.simpletweets.databinding.FooterProgressBinding;
import com.david.simpletweets.databinding.ItemTweetBinding;
import com.david.simpletweets.databinding.ItemTweetImageBinding;
import com.david.simpletweets.databinding.ItemTweetVideoBinding;
import com.david.simpletweets.fragments.ComposeTweetFragment;
import com.david.simpletweets.models.Tweet;
import com.david.simpletweets.models.User;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONObject;

import java.util.List;

import cz.msebera.android.httpclient.Header;

/**
 * Created by David on 3/23/2017.
 */
// taking Tweet objects and turning them into Views displayed in the list
public class TweetsArrayAdapter extends FooterArrayAdapter<RecyclerView.ViewHolder> {

    public abstract class ViewHolderBase extends RecyclerView.ViewHolder implements View.OnClickListener {
        public ImageView ivProfileImage;
        public TextView tvUserName;
        public TextView tvBody;
        public TextView tvName;
        public TextView tvDate;
        public ImageButton ibReply;
        public ImageButton ibRetweet;
        public ImageButton ibFavorite;
        public TextView tvRetweetCount;
        public TextView tvFavsCount;

        //profile image click listener
        protected View.OnClickListener onProfileClick = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int position = getAdapterPosition(); // gets item position
                if (position != RecyclerView.NO_POSITION) {
                    Tweet tweet = tweets.get(position);
                    //show profile of tweet's user
                    Intent i = new Intent(context, ProfileActivity.class);
                    i.putExtra("currentUser", currentUser);
                    i.putExtra("user", tweet.getUser());
                    context.startActivity(i);
                }
            }
        };

        //reply listener
        protected View.OnClickListener onReply = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int position = getAdapterPosition();
                Tweet tweet = tweets.get(position);
                FragmentManager fm = ((AppCompatActivity) context).getSupportFragmentManager();
                ComposeTweetFragment frag = ComposeTweetFragment.newInstance(currentUser, tweet);
                frag.show(fm, "fragment_reply");
            }
        };

        //retweet listener
        protected View.OnClickListener onRetweet = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int position = getAdapterPosition();
                final Tweet tweet = tweets.get(position);
                if (tweet.isRetweeted()) {
                    //update UI immediately for response UX
                    undoRetweet(tweet);
                    TwitterApplication.getRestClient().postUnretweet(tweet.getUid(), new JsonHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                            //we don't update the tweet from response here because we don't want a weird UX
                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                            Log.d("DEBUG", "undoRetweet failed: " + errorResponse.toString());
                            //revert
                            doRetweet(tweet);
                        }
                    });
                } else {
                    //update UI immediately for response UX
                    doRetweet(tweet);
                    TwitterApplication.getRestClient().postRetweet(tweet.getUid(), new JsonHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                            //we don't update the tweet from response here because we don't want a weird UX
                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                            Log.d("DEBUG", "retweet failed: " + errorResponse.toString());
                            //revert
                            undoRetweet(tweet);
                        }
                    });
                }
            }
        };

        //favorite listener
        protected View.OnClickListener onFavorite = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int position = getAdapterPosition();
                final Tweet tweet = tweets.get(position);
                if (tweet.isFavorited()) {
                    //update UI immediately for response UX
                    undoFavorite(tweet);
                    TwitterApplication.getRestClient().postUnfavorite(tweet.getUid(), new JsonHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                            //we don't update the tweet from response here because we don't want a weird UX
                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                            Log.d("DEBUG", "undoFavorite failed: " + errorResponse.toString());
                            //revert
                            doFavorite(tweet);
                        }
                    });
                } else {
                    //update UI immediately for response UX
                    doFavorite(tweet);
                    TwitterApplication.getRestClient().postFavorite(tweet.getUid(), new JsonHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                            //we don't update the tweet from response here because we don't want a weird UX
                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                            Log.d("DEBUG", "favorite failed: " + errorResponse.toString());
                            //revert
                            undoFavorite(tweet);
                        }
                    });
                }
            }
        };

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
                i.putExtra("currentUser", currentUser);
                i.putExtra("fragmentName", fragmentName);
                ((AppCompatActivity) context).startActivityForResult(i, TimelineActivity.REQUEST_CODE_DETAILS);
            }
        }

        public abstract void bindTweet(Tweet tweet);

        protected void doRetweet(Tweet tweet) {
            tweet.setRetweetCount(tweet.getRetweetCount() + 1);
            tweet.setRetweeted(true);
            tweet.save();
            ibRetweet.setImageResource(R.drawable.ic_repeat_on);
            tvRetweetCount.setText(String.valueOf(tweet.getRetweetCount()));
        }

        protected void undoRetweet(Tweet tweet) {
            tweet.setRetweetCount(tweet.getRetweetCount() - 1);
            tweet.setRetweeted(false);
            tweet.save();
            ibRetweet.setImageResource(R.drawable.ic_repeat);
            tvRetweetCount.setText(String.valueOf(tweet.getRetweetCount()));
        }

        protected void doFavorite(Tweet tweet) {
            tweet.setFavoritesCount(tweet.getFavoritesCount() + 1);
            tweet.setFavorited(true);
            tweet.save();
            ibFavorite.setImageResource(R.drawable.ic_star_on);
            tvFavsCount.setText(String.valueOf(tweet.getFavoritesCount()));
        }

        protected void undoFavorite(Tweet tweet) {
            tweet.setFavoritesCount(tweet.getFavoritesCount() - 1);
            tweet.setFavorited(false);
            tweet.save();
            ibFavorite.setImageResource(R.drawable.ic_star);
            tvFavsCount.setText(String.valueOf(tweet.getFavoritesCount()));
        }
    }

    public class ViewHolder extends ViewHolderBase {
        private ItemTweetBinding binding;

        public ViewHolder(ItemTweetBinding itemView) {
            super(itemView.getRoot());

            this.binding = itemView;

            //set bindings
            ivProfileImage = binding.ivProfileImage;
            tvName = binding.tvName;
            tvUserName = binding.tvUserName;
            tvBody = binding.tvBody;
            tvDate = binding.tvDate;
            ibReply = binding.incTweetActions.ibReply;
            ibRetweet = binding.incTweetActions.ibRetweet;
            ibFavorite = binding.incTweetActions.ibFavorite;
            tvRetweetCount = binding.incTweetActions.tvRetweetCount;
            tvFavsCount = binding.incTweetActions.tvFavsCount;

            //set click listeners
            ivProfileImage.setOnClickListener(onProfileClick);
            ibReply.setOnClickListener(onReply);
            ibRetweet.setOnClickListener(onRetweet);
            ibFavorite.setOnClickListener(onFavorite);
        }

        public void bindTweet(Tweet tweet) {
            binding.setTweet(tweet);
            binding.incTweetActions.setTweet(tweet);
            binding.executePendingBindings();

            //update button images
            if (tweet.isRetweeted()) {
                ibRetweet.setImageResource(R.drawable.ic_repeat_on);
            }
            if (tweet.isFavorited()) {
                ibFavorite.setImageResource(R.drawable.ic_star_on);
            }
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
            ivEmbedImage = binding.ivEmbedImage;
            ibReply = binding.incTweetActions.ibReply;
            ibRetweet = binding.incTweetActions.ibRetweet;
            ibFavorite = binding.incTweetActions.ibFavorite;
            tvRetweetCount = binding.incTweetActions.tvRetweetCount;
            tvFavsCount = binding.incTweetActions.tvFavsCount;

            //set click listeners
            ivProfileImage.setOnClickListener(onProfileClick);
            ibReply.setOnClickListener(onReply);
            ibRetweet.setOnClickListener(onRetweet);
            ibFavorite.setOnClickListener(onFavorite);
        }

        public void bindTweet(Tweet tweet) {
            binding.setTweet(tweet);
            binding.executePendingBindings();

            binding.incTweetActions.setTweet(tweet);
            binding.incTweetActions.executePendingBindings();

            //update button images
            if (tweet.isRetweeted()) {
                ibRetweet.setImageResource(R.drawable.ic_repeat_on);
            }
            if (tweet.isFavorited()) {
                ibFavorite.setImageResource(R.drawable.ic_star_on);
            }

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
            ivEmbedImage = binding.ivEmbedImage;
            ibReply = binding.incTweetActions.ibReply;
            ibRetweet = binding.incTweetActions.ibRetweet;
            ibFavorite = binding.incTweetActions.ibFavorite;
            tvRetweetCount = binding.incTweetActions.tvRetweetCount;
            tvFavsCount = binding.incTweetActions.tvFavsCount;

            //set click listeners
            ivProfileImage.setOnClickListener(onProfileClick);
            ibReply.setOnClickListener(onReply);
            ibRetweet.setOnClickListener(onRetweet);
            ibFavorite.setOnClickListener(onFavorite);
        }

        public void bindTweet(Tweet tweet) {
            binding.setTweet(tweet);
            binding.executePendingBindings();

            binding.incTweetActions.setTweet(tweet);
            binding.incTweetActions.executePendingBindings();

            //update button images
            if (tweet.isRetweeted()) {
                ibRetweet.setImageResource(R.drawable.ic_repeat_on);
            }
            if (tweet.isFavorited()) {
                ibFavorite.setImageResource(R.drawable.ic_star_on);
            }

            Glide.with(getContext()).load(tweet.getMediaUrl())
                    .into(ivEmbedImage);
        }
    }

    private final int GENERIC = 0, IMAGE = 1, VIDEO = 2;

    // Store a member variable for the tweet
    private List<Tweet> tweets;
    // Store the context for easy access
    private Context context;
    private String fragmentName;
    private User currentUser;

    public TweetsArrayAdapter(@NonNull Context context, @NonNull List<Tweet> tweets, @NonNull User currentUser, String fragmentName) {
        this.context = context;
        this.tweets = tweets;
        this.currentUser = currentUser;
        this.fragmentName = fragmentName;
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
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        // Get the data model based on position
        if (isPositionFooter(position)) {
            footerViewHolder = (FooterViewHolder) viewHolder;
        } else {
            Tweet tweet = this.tweets.get(position);
            configureViewHolder((ViewHolderBase) viewHolder, tweet);
        }
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
//        Log.d("DEBUG", "item count returning: " + this.tweets.size() + super.getItemCount());
        return this.tweets.size() + super.getItemCount(); //+1 for footer
    }

    @Override
    public int getItemViewType(int position) {
        if (isPositionFooter(position)) {
            return TYPE_FOOTER;
        }
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

    protected boolean isPositionFooter (int position) {
        return position == this.tweets.size();
    }

}
