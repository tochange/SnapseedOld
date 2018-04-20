package com.niksoftware.snapseed.views;

import android.content.Context;
import android.graphics.Canvas;
import android.view.View;

public class UPointParameterNameView extends View {
    private String _title;
    private int[] _upoint_gui_param_types;

    public UPointParameterNameView(Context context, int[] upoint_gui_param_types) {
        super(context);
        this._upoint_gui_param_types = upoint_gui_param_types;
    }

    public void setTitle(String title) {
        if (this._title == null || !this._title.equals(title)) {
            this._title = title;
            invalidate();
        }
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(ParameterViewHelper.getMaximumParameterWidth(this._upoint_gui_param_types) + 4, ParameterViewHelper.getTextHeightInPixel() + 4);
    }

    protected void onDraw(Canvas canvas) {
        if (this._title != null) {
            ParameterViewHelper.drawTextLeftGlowing(this._title, 0, (canvas.getHeight() - ParameterViewHelper.getTextHeightInPixel()) / 2, canvas);
        }
    }
}
