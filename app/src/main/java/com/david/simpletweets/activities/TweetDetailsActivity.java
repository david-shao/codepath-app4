package com.david.simpletweets.activities;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
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
import com.david.simpletweets.databinding.ActivityTweetDetailsBinding;
import com.david.simpletweets.fragments.ComposeTweetFragment;
import com.david.simpletweets.models.Tweet;
import com.david.simpletweets.models.User;

import java.util.ArrayList;
import java.util.List;

import static com.raizlabs.android.dbflow.config.FlowManager.getContext;

public class TweetDetailsActivity extends AppCompatActivity implements ComposeTweetFragment.ComposeTweetListener {

    private Toolbar toolbar;

    private ImageView ivProfileImage;
    private TextView tvUserName;
    private TextView tvBody;
    private TextView tvName;
    private TextView tvDate;
    private ImageButton ibReply;
    private ImageView ivEmbedImage;
    private VideoView vvEmbedVideo;

    private Tweet tweet;
    private User currentUser;
    private int position;
    private ActivityTweetDetailsBinding binding;

    private List<Tweet> replies;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_tweet_details);

        tweet = getIntent().getParcelableExtra("tweet");
        currentUser = getIntent().getParcelableExtra("currentUser");
        position = getIntent().getIntExtra("pos", 0);
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
        ibReply = binding.ibReply;
        ivEmbedImage = binding.ivEmbedImage;
        vvEmbedVideo = binding.vvEmbedVideo;

        binding.setTweet(tweet);
        binding.executePendingBindings();

        Glide.with(getContext()).load(tweet.getUser().getProfileImageUrl())
                .into(ivProfileImage);

        if (!TextUtils.isEmpty(tweet.getMediaType())) {
            if (tweet.getMediaType().equals("photo")) {
                //set reply button to be below embeded image
                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) ibReply.getLayoutParams();
                params.addRule(RelativeLayout.BELOW, R.id.ivEmbedImage);
                ivEmbedImage.setVisibility(View.VISIBLE);
                //load image
                Glide.with(getContext()).load(tweet.getMediaUrl())
                        .into(ivEmbedImage);
            } else if (tweet.getMediaType().equals("video")) {
                //set reply button to be below embeded video
                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) ibReply.getLayoutParams();
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
                if (!replies.isEmpty()) {
                    Intent data = new Intent();
                    data.putParcelableArrayListExtra("replies", (ArrayList<Tweet>) replies);
                    setResult(RESULT_OK, data);
                }
                finish();
                break;
        }

        return true;
    }

    public void onTweetReply(View view) {
        FragmentManager fm = getSupportFragmentManager();
        ComposeTweetFragment frag = ComposeTweetFragment.newInstance(currentUser, tweet);
        frag.show(fm, "fragment_reply");
    }

    @Override
    public void onTweet(Tweet tweet) {
        //add tweet to beginning of replies list so it's always sorted most recent first
        replies.add(0, tweet);
    }
}
