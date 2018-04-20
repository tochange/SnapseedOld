package com.niksoftware.snapseed.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.view.View;
import com.niksoftware.snapseed.MainActivity;
import com.niksoftware.snapseed.core.filterparameters.FilterParameter;

public class ActiveParameterView extends View {
    private int _cached_X;
    private int _cached_Y;
    private Drawable _drawable = getResources().getDrawable(R.drawable.gfx_ct_paramselect_active);
    private int _menuWidth = -1;
    private int _middleX = 0;
    private int _middleY = 0;

    public ActiveParameterView(Context context) {
        super(context);
    }

    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
    }

    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int middleX = (getLeft() + getRight()) / 2;
        int middleY = (getTop() + getBottom()) / 2;
        int menuWidth = MainActivity.getWorkingAreaView().getParameterView().getMenuWidth();
        int textHeightInPixel = ParameterViewHelper.getTextHeightInPixel();
        if (!(this._menuWidth == menuWidth && this._middleX == middleX && this._middleY == middleY)) {
            this._middleX = middleX;
            this._middleY = middleY;
            this._menuWidth = menuWidth;
            int activeLeft = this._middleX - (this._menuWidth / 2);
            int activeTop = this._middleY - (ParameterViewHelper.SINGLE_PARAM_HEIGHT() / 2);
            int activeRight = this._middleX + (this._menuWidth / 2);
            int activeBottom = this._middleY + (ParameterViewHelper.SINGLE_PARAM_HEIGHT() / 2);
            this._drawable.setBounds(activeLeft, activeTop, activeRight, activeBottom);
            this._cached_X = (activeLeft + activeRight) / 2;
            this._cached_Y = activeBottom - (((activeBottom - activeTop) - textHeightInPixel) / 2);
        }
        FilterParameter filter = MainActivity.getFilterParameter();
        int parameterId = filter.getActiveFilterParameter();
        this._drawable.draw(canvas);
        ParameterViewHelper.drawTextCentered(filter.getParameterTitle(getContext(), parameterId), this._cached_X, this._cached_Y, canvas);
    }
}
