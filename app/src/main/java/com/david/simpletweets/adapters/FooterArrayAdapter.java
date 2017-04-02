package com.david.simpletweets.adapters;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import com.david.simpletweets.databinding.FooterProgressBinding;

/**
 * Created by David on 4/1/2017.
 */

public abstract class FooterArrayAdapter<T> extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    public class FooterViewHolder extends RecyclerView.ViewHolder {
        private FooterProgressBinding binding;
        private ProgressBar progressBarFooter;

        public FooterViewHolder(FooterProgressBinding itemView) {
            super(itemView.getRoot());

            this.binding = itemView;

            progressBarFooter = binding.pbFooterLoading;
        }

        // Show progress
        public void showProgressBar() {
            progressBarFooter.setVisibility(View.VISIBLE);
        }

        // Hide progress
        public void hideProgressBar() {
            progressBarFooter.setVisibility(View.GONE);
        }
    }

    protected final int TYPE_FOOTER = -1;
    protected FooterViewHolder footerViewHolder;

    @Override
    public int getItemCount() {
        return 1;
    }

    //subclasses must override and return TYPE_FOOTER if position is equal to size of items
    @Override
    public abstract int getItemViewType(int position);

    public void showFooterProgressBar() {
        if (footerViewHolder != null) {
            Log.d("DEBUG", "footer view holder exists, showing");
            footerViewHolder.showProgressBar();
        }
    }

    public void hideFooterProgressBar() {
        if (footerViewHolder != null) {
            Log.d("DEBUG", "footer view holder exists, hiding");
            footerViewHolder.hideProgressBar();
        }
    }

}
