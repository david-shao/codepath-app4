package com.david.simpletweets.fragments;

import android.content.DialogInterface;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.david.simpletweets.R;
import com.david.simpletweets.TwitterApplication;
import com.david.simpletweets.databinding.FragmentComposeBinding;
import com.david.simpletweets.models.Tweet;
import com.david.simpletweets.models.User;
import com.david.simpletweets.utils.DraftUtil;
import com.david.simpletweets.utils.StyleUtils;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

/**
 * Created by David on 3/24/2017.
 */

public class ComposeTweetFragment extends DialogFragment {

    private final int CHAR_COUNT_MAX = 140;

    ImageButton ibCancel;
    Button btnTweet;
    ImageView ivProfileImage;
    TextView tvUserName;
    TextView tvName;
    EditText etBody;
    TextView tvCharCount;

    User user;
    Tweet tweet;
    FragmentComposeBinding binding;

    DraftUtil draftUtil;

    public ComposeTweetFragment() {
        //needs to be empty
    }

    public static ComposeTweetFragment newInstance(User user) {
        ComposeTweetFragment frag = new ComposeTweetFragment();
        Bundle args = new Bundle();
        args.putParcelable("user", user);
        frag.setArguments(args);
        return frag;
    }

    public static ComposeTweetFragment newInstance(User user, Tweet tweet) {
        ComposeTweetFragment frag = new ComposeTweetFragment();
        Bundle args = new Bundle();
        args.putParcelable("user", user);
        args.putParcelable("tweet", tweet);
        frag.setArguments(args);
        return frag;
    }

    public static ComposeTweetFragment newInstance(User user, String preFill) {
        ComposeTweetFragment frag = new ComposeTweetFragment();
        Bundle args = new Bundle();
        args.putParcelable("user", user);
        args.putString("preFill", preFill);
        frag.setArguments(args);
        return frag;
    }

    public interface ComposeTweetListener {
        void onTweet(Tweet tweet);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_compose, container, true);
        draftUtil = new DraftUtil(getContext());

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        ibCancel = binding.ibCancel;
        btnTweet = binding.btnTweet;
        ivProfileImage = binding.ivProfileImage;
        tvUserName = binding.tvUserName;
        tvName = binding.tvName;
        etBody = binding.etBody;
        tvCharCount = binding.tvCharCount;

        user = getArguments().getParcelable("user");
        tweet = getArguments().getParcelable("tweet");

        setupViews();
    }

    private void setupViews() {
        binding.setUser(user);
        binding.executePendingBindings();

        Glide.with(getContext()).load(user.getProfileImageUrl())
                .into(ivProfileImage);

        //do some pre-filling if necessary
        String preFill = getArguments().getString("preFill");
        if (TextUtils.isEmpty(preFill)) {
            //load any drafts if available
            preFill = draftUtil.load();
        }
        if (!TextUtils.isEmpty(preFill) || tweet != null) {
            if (tweet != null) {
                //we're replying to a tweet, so set reply string
                etBody.setText(tweet.getUser().getScreenName() + " ");
            } else if (!TextUtils.isEmpty(preFill)) {
                etBody.setText(preFill);
            }
            int length = etBody.getText().length();
            etBody.setSelection(length);
            int charCount = CHAR_COUNT_MAX - length;
            tvCharCount.setText("" + charCount);
        }

        final DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (i == DialogInterface.BUTTON_POSITIVE) {
                    //save draft
                    draftUtil.save(etBody.getText().toString());
                    dismiss();
                } else if (i == DialogInterface.BUTTON_NEGATIVE) {
                    draftUtil.discard();
                    dismiss();
                }
            }
        };

        ibCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (etBody.getText().length() > 0) {
                    //ask if user wants to save draft
                    AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getContext());
                    dialogBuilder.setMessage(getResources().getString(R.string.compose_save_draft))
                            .setPositiveButton(getResources().getString(R.string.save), dialogClickListener)
                            .setNegativeButton(getResources().getString(R.string.discard), dialogClickListener);
                    dialogBuilder.show();
                } else {
                    dismiss();
                }
            }
        });

        btnTweet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //make network call to post a tweet
                String tweetBody = etBody.getText().toString();
                long replyTweetId = -1;
                if (tweet != null) {
                    replyTweetId = tweet.getUid();
                }

                TwitterApplication.getRestClient().postStatusUpdate(tweetBody, replyTweetId, new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        Tweet newTweet = Tweet.fromJSON(response);
                        ComposeTweetListener listener = (ComposeTweetListener) getActivity();
                        listener.onTweet(newTweet);

                        draftUtil.discard();
                        dismiss();
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                        Log.d("DEBUG", "Tweeting failed " + errorResponse.toString());
                    }
                });
            }
        });

        etBody.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                int charCount = CHAR_COUNT_MAX - charSequence.length();
                if (charCount < 0) {
                    tvCharCount.setText(StyleUtils.applyColor("" + charCount, ContextCompat.getColor(getContext(), android.R.color.holo_red_light)));
                } else {
                    tvCharCount.setText("" + charCount);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
    }

    @Override
    public void onResume() {
        // Get existing layout params for the window
        ViewGroup.LayoutParams params = getDialog().getWindow().getAttributes();
        // Assign window properties to fill the parent
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.height = WindowManager.LayoutParams.MATCH_PARENT;
        getDialog().getWindow().setAttributes((android.view.WindowManager.LayoutParams) params);
        // Call super onResume after sizing
        super.onResume();
    }
}
