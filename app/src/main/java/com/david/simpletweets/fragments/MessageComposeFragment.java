package com.david.simpletweets.fragments;


import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;

import com.david.simpletweets.R;
import com.david.simpletweets.TwitterApplication;
import com.david.simpletweets.databinding.FragmentMessageComposeBinding;
import com.david.simpletweets.models.Message;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

public class MessageComposeFragment extends DialogFragment {

    ImageButton ibCancel;
    Button btnSend;
    EditText etUserName;
    EditText etBody;
    ProgressBar pbLoading;

    FragmentMessageComposeBinding binding;

    public MessageComposeFragment() {
        // Required empty public constructor
    }

    public static MessageComposeFragment newInstance() {
        MessageComposeFragment fragment = new MessageComposeFragment();
        return fragment;
    }

    public interface ComposeMessageListener {
        void onPost(Message message);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_message_compose, container, false);

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        ibCancel = binding.ibCancel;
        btnSend = binding.btnSend;
        etUserName = binding.etUserName;
        etBody = binding.etBody;
        pbLoading = binding.pbLoading;

        setupViews();
    }

    private void setupViews() {
        ibCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //make network call to post
                String recipient = etUserName.getText().toString();
                String body = etBody.getText().toString();

                btnSend.setVisibility(View.INVISIBLE);
                pbLoading.setVisibility(View.VISIBLE);
                TwitterApplication.getRestClient().postMessage(body, recipient, new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        Message newMsg = Message.fromJSON(response);
                        ComposeMessageListener listener = (ComposeMessageListener) getActivity();
                        listener.onPost(newMsg);

                        dismiss();
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                        Log.d("DEBUG", "Sending message failed " + errorResponse.toString());
                        pbLoading.setVisibility(View.GONE);
                        btnSend.setVisibility(View.VISIBLE);
                    }
                });
            }
        });
    }

}
