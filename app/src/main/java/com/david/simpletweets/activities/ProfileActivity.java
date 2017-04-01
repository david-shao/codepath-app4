package com.david.simpletweets.activities;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.david.simpletweets.R;
import com.david.simpletweets.databinding.ActivityProfileBinding;
import com.david.simpletweets.fragments.UserHeaderFragment;
import com.david.simpletweets.fragments.UserTimelineFragment;
import com.david.simpletweets.models.User;

public class ProfileActivity extends AppCompatActivity {

    private ActivityProfileBinding binding;
    private Toolbar toolbar;
    private User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_profile);

        toolbar = binding.toolbar;
        setSupportActionBar(toolbar);

        user = getIntent().getParcelableExtra("user");
        getSupportActionBar().setTitle(user.getScreenName());

        if (savedInstanceState == null) {
            UserHeaderFragment userHeaderFragment = UserHeaderFragment.newInstance(user);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.flHeader, userHeaderFragment)
                    .commit();

            //create user timeline fragment
            UserTimelineFragment userFragment = UserTimelineFragment.newInstance(user.getScreenName());
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

        return true;
    }
}
