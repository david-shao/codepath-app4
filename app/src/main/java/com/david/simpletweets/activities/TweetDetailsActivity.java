package com.david.simpletweets.activities;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.VideoView;

import com.bumptech.glide.Glide;
import com.david.simpletweets.R;
import com.david.simpletweets.TwitterApplication;
import com.david.simpletweets.databinding.ActivityTweetDetailsBinding;
import com.david.simpletweets.fragments.ComposeTweetFragment;
import com.david.simpletweets.models.Tweet;
import com.david.simpletweets.models.User;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import cz.msebera.android.httpclient.Header;

import static com.raizlabs.android.dbflow.config.FlowManager.getContext;

public class TweetDetailsActivity extends AppCompatActivity implements ComposeTweetFragment.ComposeTweetListener {

    private Toolbar toolbar;

    private ImageView ivProfileImage;
    private TextView tvUserName;
    private TextView tvBody;
    private TextView tvName;
    private TextView tvDate;
    private ImageButton ibReply;
    public ImageButton ibRetweet;
    public ImageButton ibFavorite;
    public TextView tvRetweetCount;
    public TextView tvFavsCount;
    private ImageView ivEmbedImage;
    private VideoView vvEmbedVideo;
    private RelativeLayout rlTweetActions;

    private Tweet tweet;
    private User currentUser;
    private int position;
    private String fragmentName;
    private ActivityTweetDetailsBinding binding;

    private List<Tweet> replies;

    //profile image click listener
    protected View.OnClickListener onProfileClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            //show profile of tweet's user
            Intent i = new Intent(TweetDetailsActivity.this, ProfileActivity.class);
            i.putExtra("currentUser", currentUser);
            i.putExtra("user", tweet.getUser());
            startActivity(i);
        }
    };

    //reply listener
    protected View.OnClickListener onReply = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            FragmentManager fm = getSupportFragmentManager();
            ComposeTweetFragment frag = ComposeTweetFragment.newInstance(currentUser, tweet);
            frag.show(fm, "fragment_reply");
        }
    };

    //retweet listener
    protected View.OnClickListener onRetweet = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_tweet_details);

        tweet = getIntent().getParcelableExtra("tweet");
        currentUser = getIntent().getParcelableExtra("currentUser");
        position = getIntent().getIntExtra("pos", 0);
        fragmentName = getIntent().getStringExtra("fragmentName");
        replies = new ArrayList<>();

        setupViews();
    }

    private void setupViews() {
        toolbar = binding.toolbar;
        setSupportActionBar(toolbar);
        //turn on back button
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        ivProfileImage = binding.ivProfileImage;
        tvUserName = binding.tvUserName;
        tvBody = binding.tvBody;
        tvName = binding.tvName;
        tvDate = binding.tvDate;
        ibReply = binding.incTweetActions.ibReply;
        ibRetweet = binding.incTweetActions.ibRetweet;
        ibFavorite = binding.incTweetActions.ibFavorite;
        tvRetweetCount = binding.incTweetActions.tvRetweetCount;
        tvFavsCount = binding.incTweetActions.tvFavsCount;
        ivEmbedImage = binding.ivEmbedImage;
        vvEmbedVideo = binding.vvEmbedVideo;
        rlTweetActions = binding.incTweetActions.rlTweetActions;

        //set click listeners
        ivProfileImage.setOnClickListener(onProfileClick);
        ibReply.setOnClickListener(onReply);
        ibRetweet.setOnClickListener(onRetweet);
        ibFavorite.setOnClickListener(onFavorite);

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

        Glide.with(getContext()).load(tweet.getUser().getProfileImageUrl())
                .into(ivProfileImage);

        if (!TextUtils.isEmpty(tweet.getMediaType())) {
            if (tweet.getMediaType().equals("photo")) {
                //set reply button to be below embeded image
                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) rlTweetActions.getLayoutParams();
                params.addRule(RelativeLayout.BELOW, R.id.ivEmbedImage);
                ivEmbedImage.setVisibility(View.VISIBLE);
                //load image
                Glide.with(getContext()).load(tweet.getMediaUrl())
                        .into(ivEmbedImage);
            } else if (tweet.getMediaType().equals("video")) {
                //set reply button to be below embeded video
                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) rlTweetActions.getLayoutParams();
                params.addRule(RelativeLayout.BELOW, R.id.vvEmbedVideo);
                //load video
                Uri vidUri = Uri.parse(tweet.getVideoUrl());
                vvEmbedVideo.setVideoURI(vidUri);
                MediaController mediaController = new MediaController(TweetDetailsActivity.this);
                mediaController.setAnchorView(vvEmbedVideo);
                vvEmbedVideo.setMediaController(mediaController);
                //start video
                vvEmbedVideo.start();
                vvEmbedVideo.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_details, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {
            case android.R.id.home:
                setFinishData();
                finish();
                break;
        }

        return true;
    }

    @Override
    public void onBackPressed() {
        setFinishData();
        super.onBackPressed();
    }

    private void setFinishData() {
        Intent data = new Intent();
        data.putExtra("updatedTweet", tweet);
        data.putExtra("position", position);
        data.putExtra("fragmentName", fragmentName);
        if (!replies.isEmpty()) {
            data.putParcelableArrayListExtra("replies", (ArrayList<Tweet>) replies);
        }
        setResult(RESULT_OK, data);
    }

    @Override
    public void onTweet(Tweet tweet) {
        //add tweet to beginning of replies list so it's always sorted most recent first
        replies.add(0, tweet);
    }

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
