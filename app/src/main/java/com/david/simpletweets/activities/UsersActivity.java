package com.david.simpletweets.activities;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.david.simpletweets.R;
import com.david.simpletweets.databinding.ActivityUsersBinding;
import com.david.simpletweets.fragments.FollowersFragment;
import com.david.simpletweets.fragments.FollowingsFragment;
import com.david.simpletweets.fragments.UsersListFragment;
import com.david.simpletweets.models.User;

public class UsersActivity extends AppCompatActivity {

    private ActivityUsersBinding binding;
    private Toolbar toolbar;
    private User user;
    private User loggedInUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_users);

        toolbar = binding.toolbar;
        setSupportActionBar(toolbar);
        //turn on back button
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        user = getIntent().getParcelableExtra("user");
        loggedInUser = getIntent().getParcelableExtra("currentUser");
        String type = getIntent().getStringExtra("type");
        if (type.equals(UsersListFragment.TYPE_FOLLOWERS)) {
            getSupportActionBar().setTitle(getString(R.string.title_followers));
        } else if (type.equals(UsersListFragment.TYPE_FOLLOWINGS)) {
            getSupportActionBar().setTitle(getString(R.string.title_followings));
        }

        if (savedInstanceState == null) {
            UsersListFragment frag = null;
            if (type.equals(UsersListFragment.TYPE_FOLLOWERS)) {
                frag = FollowersFragment.newInstance(loggedInUser, user);
            } else if (type.equals(UsersListFragment.TYPE_FOLLOWINGS)) {
                frag = FollowingsFragment.newInstance(loggedInUser, user);
            }
            if (frag != null) {
                Log.d("DEBUG", "creating new fragment based on type: " + type);
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.flFrag, frag)
                        .commit();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_users, menu);

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
}
