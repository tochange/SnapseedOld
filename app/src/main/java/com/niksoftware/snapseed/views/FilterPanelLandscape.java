package com.niksoftware.snapseed.views;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.Shader.TileMode;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.GradientDrawable.Orientation;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;
import com.niksoftware.snapseed.MainActivity;
import com.niksoftware.snapseed.MainActivity.Screen;
import com.niksoftware.snapseed.core.FilterList;

public class FilterPanelLandscape extends ViewGroup implements FilterPanelInterface {
    private static final int GRADIENT_WIDTH = 16;
    private GridLayout _filterGridLayout;
    private GradientDrawable _gradient_left;
    private int _padding;
    private BitmapDrawable _patternLayer;
    private ScrollView _scroller = new ScrollView(MainActivity.getMainActivity());
    private Drawable _shadowLayer;

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

    public FilterPanelLandscape(Context context) {
        super(context);
        Resources resources = MainActivity.getMainActivity().getResources();
        this._padding = resources.getDimensionPixelSize(R.dimen.fl_land_edge_padding);
        this._scroller.setSmoothScrollingEnabled(true);
        this._shadowLayer = resources.getDrawable(R.drawable.icon_select_sidebar_shadow);
        this._patternLayer = new BitmapDrawable(resources, BitmapFactory.decodeResource(resources, R.drawable.icon_select_sidebar_bg));
        this._patternLayer.setTileModeXY(TileMode.REPEAT, TileMode.REPEAT);
        this._filterGridLayout = new GridLayout(MainActivity.getMainActivity());
        this._filterGridLayout.setPadding(0, this._padding, 0, this._padding);
        this._filterGridLayout.setNumColumns(2, false);
        this._filterGridLayout.setNumRows((FilterList.getItemCount() + 1) / 2, false);
        for (int i = 0; i < FilterList.getItemCount(); i++) {
            this._filterGridLayout.addView(FilterList.getItemView(MainActivity.getMainActivity(), i));
        }
        setWillNotDraw(false);
        this._filterGridLayout.setCellSpacing(resources.getDimensionPixelSize(R.dimen.fl_land_cell_spacing_x), resources.getDimensionPixelSize(R.dimen.fl_land_cell_spacing_y));
        this._scroller.addView(this._filterGridLayout, new LayoutParams(-2, -2));
        addView(this._scroller);
        this._gradient_left = new GradientDrawable(Orientation.LEFT_RIGHT, new int[]{2130706432, 0});
        this._gradient_left.setGradientType(0);
        this._gradient_left.setDither(true);
    }

    public void setupOnClickListeners() {
        FilterButtonListener listener = new FilterButtonListener();
        for (int i = 0; i < this._filterGridLayout.getChildCount(); i++) {
            View itemView = this._filterGridLayout.getChildAt(i);
            if (!(!(itemView instanceof FilterListItemView) || itemView.getTag() == null || ((Integer) itemView.getTag()).intValue() == 1)) {
                ((FilterListItemView) itemView).getCoverView().setColorFilter(0);
                itemView.setOnClickListener(listener);
            }
        }
    }

    protected void onDraw(Canvas c) {
        this._patternLayer.draw(c);
        this._shadowLayer.draw(c);
        this._gradient_left.draw(c);
    }

    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if (changed) {
            int scrollerWidth = (r - l) - 16;
            this._scroller.layout(0, 0, scrollerWidth, b - t);
            this._filterGridLayout.measure(MeasureSpec.makeMeasureSpec(0, 0), MeasureSpec.makeMeasureSpec(0, 0));
            int filterGridHeight = this._filterGridLayout.getMeasuredHeight();
            int filterGridWidth = this._filterGridLayout.getMeasuredWidth();
            int filterGridLeft = (scrollerWidth - filterGridWidth) / 2;
            this._filterGridLayout.layout(filterGridLeft, 0, filterGridLeft + filterGridWidth, filterGridHeight);
            this._shadowLayer.setBounds(0, 0, (r - l) - 16, b - t);
            this._patternLayer.setBounds(0, 0, (r - l) - 16, filterGridHeight);
            this._gradient_left.setBounds(new Rect(r - 16, 0, r, b));
        }
    }

    public int getFirstVisibleFilterId() {
        int y = (this._scroller.getScrollY() + this._padding) + 10;
        for (int i = 0; i < this._filterGridLayout.getChildCount(); i++) {
            View itemView = this._filterGridLayout.getChildAt(i);
            if (itemView.getTop() < y && itemView.getBottom() > y) {
                return ((Integer) itemView.getTag()).intValue();
            }
        }
        return 1;
    }

    public void setFirstVisibleFilterId(int id) {
        for (int i = 0; i < this._filterGridLayout.getChildCount(); i++) {
            View itemView = this._filterGridLayout.getChildAt(i);
            if (((Integer) itemView.getTag()).intValue() == id) {
                this._scroller.scrollTo(0, itemView.getTop() - this._padding);
                return;
            }
        }
    }
}
