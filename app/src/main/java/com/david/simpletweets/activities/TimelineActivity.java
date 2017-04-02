package com.david.simpletweets.activities;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.astuetz.PagerSlidingTabStrip;
import com.david.simpletweets.R;
import com.david.simpletweets.TwitterApplication;
import com.david.simpletweets.databinding.ActivityTimelineBinding;
import com.david.simpletweets.fragments.ComposeTweetFragment;
import com.david.simpletweets.fragments.HomeTimelineFragment;
import com.david.simpletweets.fragments.MentionsTimelineFragment;
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
    private ViewPager vpTabs;
    private PagerSlidingTabStrip tabStrip;
    private HomeTimelineFragment fragHome;
    private MentionsTimelineFragment fragMentions;
    private SearchView svTweets;

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

        //get the viewpager
        vpTabs = binding.vpTabs;
        //set the viewpager adapter for the pager
        vpTabs.setAdapter(new TweetsPagerAdapter(getSupportFragmentManager()));
        //find the pager sliding tabs
        tabStrip = binding.tabs;
        //attach the tabstrip to the viewpager
        tabStrip.setViewPager(vpTabs);
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

        MenuItem searchItem = menu.findItem(R.id.search_tweets);
        svTweets = (SearchView) MenuItemCompat.getActionView(searchItem);

        svTweets.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                Intent i = new Intent(TimelineActivity.this, SearchActivity.class);
                i.putExtra("currentUser", currentUser);
                i.putExtra("query", query);
                startActivity(i);

                // workaround to avoid issues with some emulators and keyboard devices firing twice if a keyboard enter is used
                // see https://code.google.com/p/android/issues/detail?id=24599
                svTweets.clearFocus();

                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch (id) {
            case R.id.miProfile:
                showProfile();
                break;
            case R.id.miMessages:
                showMessages();
                break;
        }

        return true;
    }

    private void showProfile() {
        Intent i = new Intent(this, ProfileActivity.class);
        i.putExtra("currentUser", currentUser);
        i.putExtra("user", currentUser);
        startActivity(i);
    }

    private void showMessages() {
        Intent i = new Intent(this, MessagesActivity.class);
        i.putExtra("currentUser", currentUser);
        startActivity(i);
    }

    //when tweeting/replying from compose fragment
    @Override
    public void onTweet(Tweet tweet) {
        if (fragHome != null) {
            fragHome.addTweetToHead(tweet);
        }
        //TODO: for now only add replies to home timeline
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
            if (fragmentName.equals(HomeTimelineFragment.NAME)) {
                if (fragHome != null) {
                    fragHome.updateTweet(position, updatedTweet);
                }
            } else if (fragmentName.equals(MentionsTimelineFragment.NAME)) {
                if (fragMentions != null) {
                    fragMentions.updateTweet(position, updatedTweet);
                }
            }
            //check for any tweets from replying from tweet details view
            List<Tweet> replies = data.getParcelableArrayListExtra("replies");
            if (replies != null && !replies.isEmpty()) {
                if (fragHome != null) {
                    fragHome.addTweetsToHead(replies);
                }
                //TODO: for now only add replies to home timeline
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

    //Return the order of fragments in view pager
    public class TweetsPagerAdapter extends FragmentPagerAdapter {
        private String tabTitles[] = {getResources().getString(R.string.tab_home_title), getResources().getString(R.string.tab_mentions_title)};

        //Adapter gets the manager insert or remove fragment from activity
        public TweetsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        //the order and creation of fragments within the pager
        @Override
        public Fragment getItem(int position) {
            if (position == 0) {
                fragHome = HomeTimelineFragment.newInstance(currentUser);
                return fragHome;
            } else if (position == 1) {
                fragMentions = MentionsTimelineFragment.newInstance(currentUser);
                return fragMentions;
            }
            return null;
        }

        //return the tab title
        @Override
        public CharSequence getPageTitle(int position) {
            return tabTitles[position];
        }

        //how many fragments there are to swipe between
        @Override
        public int getCount() {
            return tabTitles.length;
        }
    }
}
