package com.david.simpletweets.adapters;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.david.simpletweets.R;
import com.david.simpletweets.databinding.FooterProgressBinding;
import com.david.simpletweets.databinding.ItemMessageReceivedBinding;
import com.david.simpletweets.databinding.ItemMessageSentBinding;
import com.david.simpletweets.models.Message;
import com.david.simpletweets.models.User;

import java.util.List;

/**
 * Created by David on 4/2/2017.
 */

public class MessagesArrayAdapter extends FooterArrayAdapter<RecyclerView.ViewHolder> {

    public abstract class ViewHolderBase extends RecyclerView.ViewHolder {
        protected ImageView ivProfileImage;

        public ViewHolderBase(View itemView) {
            super(itemView);
        }

        public abstract void bindMessage(Message message);
    }

    public class ViewHolderSent extends ViewHolderBase {
        private ItemMessageSentBinding binding;

        public ViewHolderSent(ItemMessageSentBinding itemView) {
            super(itemView.getRoot());

            this.binding = itemView;

            ivProfileImage = binding.ivProfileImage;
        }

        @Override
        public void bindMessage(Message message) {
            binding.setMessage(message);
            binding.executePendingBindings();

            ivProfileImage.setImageResource(0); //clear out old image for recycled view
            Glide.with(getContext()).load(message.getRecipient().getProfileImageUrl())
                    .into(ivProfileImage);
        }
    }

    public class ViewHolderReceived extends ViewHolderBase {
        private ItemMessageReceivedBinding binding;

        public ViewHolderReceived(ItemMessageReceivedBinding itemView) {
            super(itemView.getRoot());

            this.binding = itemView;

            ivProfileImage = binding.ivProfileImage;
        }

        @Override
        public void bindMessage(Message message) {
            binding.setMessage(message);
            binding.executePendingBindings();

            ivProfileImage.setImageResource(0); //clear out old image for recycled view
            Glide.with(getContext()).load(message.getSender().getProfileImageUrl())
                    .into(ivProfileImage);
        }
    }

    private final int TYPE_RECEIVED = 0, TYPE_SENT = 1;

    private List<Message> messages;
    // Store the context for easy access
    private Context context;

    private User currentUser;

    public MessagesArrayAdapter(@NonNull Context context, @NonNull List<Message> messages, @NonNull User currentUser) {
        this.context = context;
        this.messages = messages;
        this.currentUser = currentUser;
    }

    // Easy access to the context object in the recyclerview
    private Context getContext() {
        return this.context;
    }

    // Usually involves inflating a layout from XML and returning the holder
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        RecyclerView.ViewHolder viewHolder;

        switch (viewType) {
            case TYPE_FOOTER:
                FooterProgressBinding footerBinding = DataBindingUtil.inflate(inflater, R.layout.footer_progress, parent, false);
                viewHolder = new FooterViewHolder(footerBinding);
                break;
            case TYPE_RECEIVED:
                ItemMessageReceivedBinding receivedBinding = DataBindingUtil.inflate(inflater, R.layout.item_message_received, parent, false);
                viewHolder = new ViewHolderReceived(receivedBinding);
                break;
            case TYPE_SENT:
            default:
                ItemMessageSentBinding binding = DataBindingUtil.inflate(inflater, R.layout.item_message_sent, parent, false);
                viewHolder = new ViewHolderSent(binding);
                break;
        }

        return viewHolder;
    }

    // Involves populating data into the item through holder
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        // Get the data model based on position

        if (isPositionFooter(position)) {
            footerViewHolder = (FooterViewHolder) viewHolder;
        } else {
            Message msg = this.messages.get(position);
            configureViewHolder((ViewHolderBase) viewHolder, msg);
        }
    }

    private void configureViewHolder(MessagesArrayAdapter.ViewHolderBase viewHolder, Message message) {
        // populate data into subviews
        viewHolder.bindMessage(message);
    }

    // Returns the total count of items in the list
    @Override
    public int getItemCount() {
        return this.messages.size() + super.getItemCount(); //+1 for footer
    }

    @Override
    public int getItemViewType(int position) {
        if (isPositionFooter(position)) {
            return TYPE_FOOTER;
        }
        Message message = this.messages.get(position);
        if (message.getRecipient().getUid() == currentUser.getUid()) {
            return TYPE_RECEIVED;
        }
        return TYPE_SENT;
    }

    protected boolean isPositionFooter (int position) {
        return position == this.messages.size();
    }
}
