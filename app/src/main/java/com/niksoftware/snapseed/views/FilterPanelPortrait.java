package com.niksoftware.snapseed.views;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Shader.TileMode;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.GradientDrawable.Orientation;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;

import com.niksoftware.snapseed.MainActivity;
import com.niksoftware.snapseed.MainActivity.Screen;
import com.niksoftware.snapseed.core.FilterList;

public class FilterPanelPortrait extends HorizontalScrollView implements FilterPanelInterface {
    private int _gradientSize;
    private LinearLayout _itemListContainer;
    private int _padding;

    private static class FilterButtonListener implements OnClickListener {
        private FilterButtonListener() {
        }

        public void onClick(View v) {
            MainActivity mainActivity = MainActivity.getMainActivity();
            if (mainActivity.getCurrentScreen() == Screen.MAIN && !MainActivity.getRootView().isRunningAnimation()) {
                mainActivity.activateEditScreen(((Integer) v.getTag()).intValue());
            }
        }
    }

    public FilterPanelPortrait(Context context) {
        super(context);
        setSmoothScrollingEnabled(true);
        Resources resources = getResources();
        this._gradientSize = resources.getDimensionPixelSize(R.dimen.fl_gradiend_size);
        setPadding(0, this._gradientSize + resources.getDimensionPixelSize(R.dimen.tmp_fl_port_panel_top_padding), 0, 0);
        this._itemListContainer = new LinearLayout(context);
        this._itemListContainer.setOrientation(0);
        this._padding = resources.getDimensionPixelSize(R.dimen.fl_port_edge_padding);
        int margin = resources.getDimensionPixelSize(R.dimen.fl_port_item_margin);
        for (int i = 0; i < FilterList.getItemCount(); i++) {
            int i2;
            FilterListItemView itemView = (FilterListItemView) FilterList.getItemView(context, i);
            LayoutParams layoutParams = new LayoutParams(-2, -2);
            if (i > 0) {
                i2 = margin;
            } else {
                i2 = 0;
            }
            layoutParams.setMargins(i2, 0, 0, 0);
            itemView.setLayoutParams(layoutParams);
            this._itemListContainer.addView(itemView);
        }
        this._itemListContainer.setPadding(this._padding, this._padding, this._padding, this._padding);
        addView(this._itemListContainer, new LayoutParams(-2, -2));
        measure(MeasureSpec.makeMeasureSpec(0, 0), MeasureSpec.makeMeasureSpec(0, 0));
    }

    public void setupOnClickListeners() {
        FilterButtonListener listener = new FilterButtonListener();
        for (int i = 0; i < this._itemListContainer.getChildCount(); i++) {
            View itemView = this._itemListContainer.getChildAt(i);
            if (!(!(itemView instanceof FilterListItemView) || itemView.getTag() == null || ((Integer) itemView.getTag()).intValue() == 1)) {
                ((FilterListItemView) itemView).getCoverView().setColorFilter(0);
                itemView.setOnClickListener(listener);
            }
        }
    }

    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        int backgroundWidth = r - l;
        int backgroundHeight = b - t;
        Resources resources = getResources();
        if (backgroundWidth != 0 && backgroundHeight != 0) {
            Bitmap background = Bitmap.createBitmap(backgroundWidth, backgroundHeight, Config.ARGB_8888);
            Canvas canvas = new Canvas(background);
            BitmapDrawable patternDrawable = new BitmapDrawable(resources, BitmapFactory.decodeResource(resources, R.drawable.icon_select_sidebar_bg));
            patternDrawable.setTileModeX(TileMode.REPEAT);
            patternDrawable.setTileModeY(TileMode.REPEAT);
            patternDrawable.setBounds(0, this._gradientSize, backgroundWidth, backgroundHeight);
            patternDrawable.draw(canvas);
            Drawable frameDrawable = resources.getDrawable(R.drawable.icon_select_sidebar_shadow);
            frameDrawable.setBounds(0, this._gradientSize, backgroundWidth, backgroundHeight);
            frameDrawable.draw(canvas);
            GradientDrawable shadow = new GradientDrawable(Orientation.BOTTOM_TOP, new int[]{2130706432, 0});
            shadow.setGradientType(0);
            shadow.setDither(true);
            shadow.setBounds(0, 0, backgroundWidth, this._gradientSize);
            shadow.draw(canvas);
            setBackgroundDrawable(new BitmapDrawable(resources, background));
        }
    }

    public int getFirstVisibleFilterId() {
        int x = getScrollX() + this._padding;
        for (int i = 0; i < this._itemListContainer.getChildCount(); i++) {
            View view = this._itemListContainer.getChildAt(i);
            if (view.getLeft() < x && view.getRight() > x) {
                return ((Integer) view.getTag()).intValue();
            }
        }
        return 1;
    }

    public void setFirstVisibleFilterId(int id) {
        for (int i = 0; i < this._itemListContainer.getChildCount(); i++) {
            View view = this._itemListContainer.getChildAt(i);
            if (((Integer) view.getTag()).intValue() == id) {
                scrollTo((view.getLeft() - this._padding) + 5, 0);
                return;
            }
        }
    }
}
