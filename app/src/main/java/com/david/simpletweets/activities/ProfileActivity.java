package com.david.simpletweets.activities;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.david.simpletweets.R;
import com.david.simpletweets.databinding.ActivityProfileBinding;
import com.david.simpletweets.fragments.ComposeTweetFragment;
import com.david.simpletweets.fragments.UserHeaderFragment;
import com.david.simpletweets.fragments.UserTimelineFragment;
import com.david.simpletweets.models.Tweet;
import com.david.simpletweets.models.User;

import java.util.List;

import static com.david.simpletweets.activities.TimelineActivity.REQUEST_CODE_DETAILS;

public class ProfileActivity extends AppCompatActivity implements ComposeTweetFragment.ComposeTweetListener {

    private ActivityProfileBinding binding;
    private Toolbar toolbar;
    private User user;
    private User loggedInUser;
    private UserTimelineFragment userFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_profile);

        toolbar = binding.toolbar;
        setSupportActionBar(toolbar);
        //turn on back button
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        user = getIntent().getParcelableExtra("user");
        loggedInUser = getIntent().getParcelableExtra("currentUser");
        getSupportActionBar().setTitle(user.getScreenName());

        if (savedInstanceState == null) {
            UserHeaderFragment userHeaderFragment = UserHeaderFragment.newInstance(user, loggedInUser);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.flHeader, userHeaderFragment)
                    .commit();

            //create user timeline fragment
            userFragment = UserTimelineFragment.newInstance(loggedInUser, user.getScreenName());
            //display user timeline fragment withint this activity dynamically
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.flFrag, userFragment);
            ft.commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_profile, menu);

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
                finish();
                break;
        }

        return true;
    }

    //when tweeting/replying from compose fragment
    @Override
    public void onTweet(Tweet tweet) {
        //update profile tweets list if it's logged in user's profile
        if (user.getUid() == loggedInUser.getUid()) {
            userFragment.addTweetToHead(tweet);
        }
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
            int position = data.getIntExtra("position", 0);
            Tweet updatedTweet = data.getParcelableExtra("updatedTweet");
            String fragmentName = data.getStringExtra("fragmentName");
            if (fragmentName.equals(UserTimelineFragment.NAME)) {
                userFragment.updateTweet(position, updatedTweet);
            }
            //check for any tweets from replying from tweet details view
            List<Tweet> replies = data.getParcelableArrayListExtra("replies");
            if (replies != null && !replies.isEmpty()) {
                if (user.getUid() == loggedInUser.getUid()) {
                    userFragment.addTweetsToHead(replies);
                }
            }
        }
    }
}
