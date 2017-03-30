package com.david.simpletweets.decorators;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.david.simpletweets.R;

/**
 * Created by David on 3/26/2017.
 */

public class CustomDividerItemDecoration extends RecyclerView.ItemDecoration {
    private Drawable divider;

    public CustomDividerItemDecoration(Context context) {
        divider = ContextCompat.getDrawable(context, R.drawable.line_divider);
    }

    @Override
    public void onDrawOver(Canvas c, RecyclerView parent, RecyclerView.State state) {
        //leave some gap on both sides
        int left = parent.getPaddingLeft() + 40;
        int right = parent.getWidth() - parent.getPaddingRight() - 40;

        int childCount = parent.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View child = parent.getChildAt(i);

            RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();

            int top = child.getBottom() + params.bottomMargin;
            int bottom = top + divider.getIntrinsicHeight();

            divider.setBounds(left, top, right, bottom);
            divider.draw(c);
        }
    }
}
