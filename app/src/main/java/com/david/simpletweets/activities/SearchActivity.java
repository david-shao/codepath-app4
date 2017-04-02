package com.david.simpletweets.activities;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.david.simpletweets.R;
import com.david.simpletweets.databinding.ActivitySearchBinding;
import com.david.simpletweets.fragments.ComposeTweetFragment;
import com.david.simpletweets.fragments.SearchFragment;
import com.david.simpletweets.models.Tweet;
import com.david.simpletweets.models.User;

public class SearchActivity extends AppCompatActivity implements ComposeTweetFragment.ComposeTweetListener {

    private ActivitySearchBinding binding;
    private Toolbar toolbar;
    private User loggedInUser;
    private String query;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_search);

        toolbar = binding.toolbar;
        setSupportActionBar(toolbar);
        //turn on back button
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        loggedInUser = getIntent().getParcelableExtra("currentUser");
        query = getIntent().getStringExtra("query");

        getSupportActionBar().setTitle(getString(R.string.title_search_results, query));

        if (savedInstanceState == null) {
            SearchFragment frag = SearchFragment.newInstance(loggedInUser, query);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.flFrag, frag)
                    .commit();
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

    @Override
    public void onTweet(Tweet tweet) {
        //do nothing
    }
}
