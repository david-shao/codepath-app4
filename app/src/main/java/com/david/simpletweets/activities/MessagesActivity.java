package com.david.simpletweets.activities;

import android.content.res.ColorStateList;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.astuetz.PagerSlidingTabStrip;
import com.david.simpletweets.R;
import com.david.simpletweets.TwitterApplication;
import com.david.simpletweets.databinding.ActivityMessagesBinding;
import com.david.simpletweets.fragments.MessageComposeFragment;
import com.david.simpletweets.fragments.MessagesReceivedFragment;
import com.david.simpletweets.fragments.MessagesSentFragment;
import com.david.simpletweets.models.Message;
import com.david.simpletweets.models.User;
import com.david.simpletweets.network.TwitterClient;

public class MessagesActivity extends AppCompatActivity implements MessageComposeFragment.ComposeMessageListener {

    private Toolbar toolbar;
    private User currentUser;
    private TwitterClient client;
    private FloatingActionButton fabCompose;
    private ViewPager vpTabs;
    private PagerSlidingTabStrip tabStrip;
    private MessagesReceivedFragment fragReceived;
    private MessagesSentFragment fragSent;

    private ActivityMessagesBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_messages);

        toolbar = binding.toolbar;
        setSupportActionBar(toolbar);
        //turn on back button
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(getString(R.string.title_messages));

        client = TwitterApplication.getRestClient();    //singleton client
        currentUser = getIntent().getParcelableExtra("currentUser");

        setupViews(savedInstanceState);
    }

    private void setupViews(Bundle savedInstanceState) {
        fabCompose = binding.fabCompose;

        //update color
        fabCompose.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.bgColor)));

        //get the viewpager
        vpTabs = binding.vpTabs;
        //set the viewpager adapter for the pager
        vpTabs.setAdapter(new MessagesPagerAdapter(getSupportFragmentManager()));
        //find the pager sliding tabs
        tabStrip = binding.tabs;
        //attach the tabstrip to the viewpager
        tabStrip.setViewPager(vpTabs);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_messages, menu);

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

    private void showNetworkUnavailableMessage() {
        Toast.makeText(this, getResources().getText(R.string.network_unavailable), Toast.LENGTH_SHORT).show();
    }

    public void onCompose(View view) {
        if (client.isNetworkAvailable()) {
            FragmentManager fm = getSupportFragmentManager();
            MessageComposeFragment frag = MessageComposeFragment.newInstance();
            frag.show(fm, "fragment_message_compose");
        } else {
            showNetworkUnavailableMessage();
        }
    }

    @Override
    public void onPost(Message message) {
        if (fragSent != null) {
            fragSent.addMessageToHead(message);
        }
    }

    //Return the order of fragments in view pager
    public class MessagesPagerAdapter extends FragmentPagerAdapter {
        private String tabTitles[] = {getResources().getString(R.string.tab_messages_received), getResources().getString(R.string.tab_messages_sent)};

        //Adapter gets the manager insert or remove fragment from activity
        public MessagesPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        //the order and creation of fragments within the pager
        @Override
        public Fragment getItem(int position) {
            if (position == 0) {
                fragReceived = MessagesReceivedFragment.newInstance(currentUser);
                return fragReceived;
            } else if (position == 1) {
                fragSent = MessagesSentFragment.newInstance(currentUser);
                return fragSent;
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
