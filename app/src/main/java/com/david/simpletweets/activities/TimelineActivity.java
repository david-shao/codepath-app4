package com.david.simpletweets.activities;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.david.simpletweets.R;
import com.david.simpletweets.TwitterApplication;
import com.david.simpletweets.databinding.ActivityTimelineBinding;
import com.david.simpletweets.fragments.ComposeTweetFragment;
import com.david.simpletweets.fragments.TweetsListFragment;
import com.david.simpletweets.models.Tweet;
import com.david.simpletweets.models.User;
import com.david.simpletweets.network.TwitterClient;

import java.util.List;

public class TimelineActivity extends AppCompatActivity implements ComposeTweetFragment.ComposeTweetListener {

    public static final int REQUEST_CODE_DETAILS = 20;

    private Toolbar toolbar;
    private User currentUser;
    private TwitterClient client;
    private FloatingActionButton fabCompose;
    private TweetsListFragment fragTimeline;

    private ActivityTimelineBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_timeline);

        toolbar = binding.toolbar;
        setSupportActionBar(toolbar);

        client = TwitterApplication.getRestClient();    //singleton client
        currentUser = getIntent().getParcelableExtra("user");

        processIntent();
        setupViews(savedInstanceState);
    }

    private void setupViews(Bundle savedInstanceState) {
        fabCompose = binding.fabCompose;

        //update color
        fabCompose.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.bgColor)));

        // access fragment
        if (savedInstanceState == null) {
            fragTimeline = (TweetsListFragment) getSupportFragmentManager().findFragmentById(R.id.fragTimeline);    //TODO: data bind?
        }
    }

    private void processIntent() {
        String preFill = getIntent().getStringExtra("preFill");
        if (!TextUtils.isEmpty(preFill)) {
            if (client.isNetworkAvailable()) {
                FragmentManager fm = getSupportFragmentManager();
                ComposeTweetFragment frag = ComposeTweetFragment.newInstance(currentUser, preFill);
                frag.show(fm, "fragment_compose");
            } else {
                showNetworkUnavailableMessage();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_timeline, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        return true;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    @Override
    public void onTweet(Tweet tweet) {
        fragTimeline.addTweetToHead(tweet);
    }

    /**
     * Callback when other activities finish.
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK && requestCode == REQUEST_CODE_DETAILS) {
            List<Tweet> replies = data.getParcelableArrayListExtra("replies");
            if (!replies.isEmpty()) {
                fragTimeline.addTweetsToHead(replies);
            }
        }
    }

    private void showNetworkUnavailableMessage() {
        Toast.makeText(this, getResources().getText(R.string.network_unavailable), Toast.LENGTH_SHORT).show();
    }

    public void onCompose(View view) {
        if (client.isNetworkAvailable()) {
            FragmentManager fm = getSupportFragmentManager();
            ComposeTweetFragment frag = ComposeTweetFragment.newInstance(currentUser);
//            frag.setStyle(DialogFragment.STYLE_NORMAL, R.style.Dialog_FullScreen);
            frag.show(fm, "fragment_compose");
        } else {
            showNetworkUnavailableMessage();
        }
    }
}
