package com.niksoftware.snapseed.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class FilterListItemView extends RelativeLayout {
    private ImageView _coverView;
    private TextView _titleView;

    public FilterListItemView(Context context) {
        super(context);
    }

    public FilterListItemView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    public static FilterListItemView createView(Context context, int coverResId, String title) {
        FilterListItemView filterListItem = (FilterListItemView) View.inflate(context, R.layout.filter_list_item, null);
        filterListItem.setLayoutParams(new LayoutParams(-2, -2));
        filterListItem.setCoverImageResource(coverResId);
        filterListItem.setTitle(title);
        return filterListItem;
    }

    public static FilterListItemView createView(Context context, int coverResId, int titleResId) {
        return createView(context, coverResId, context.getString(titleResId));
    }

    protected void init() {
        this._coverView = (ImageView) findViewById(R.id.filter_cover);
        this._titleView = (TextView) findViewById(R.id.filter_title);
    }

    public void setCoverImageResource(int resId) {
        this._coverView = (ImageView) findViewById(R.id.filter_cover);
        this._coverView.setImageResource(resId);
    }

    public void setTitle(int resId) {
        if (this._titleView != null) {
            this._titleView.setText(resId);
        }
    }

    public void setTitle(String text) {
        if (this._titleView != null) {
            this._titleView.setText(text);
        }
    }

    public void setTitleVisible(boolean visible) {
        if (this._titleView != null) {
            this._titleView.setVisibility(visible ? 0 : 8);
        }
    }

    public ImageView getCoverView() {
        return this._coverView;
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
        init();
    }

    public boolean dispatchTouchEvent(MotionEvent motionEvent) {
        switch (motionEvent.getAction()) {
            case 0:
                this._coverView.setColorFilter(1962934272);
                break;
            case 1:
            case 3:
                this._coverView.setColorFilter(0);
                break;
        }
        return super.dispatchTouchEvent(motionEvent);
    }
}
