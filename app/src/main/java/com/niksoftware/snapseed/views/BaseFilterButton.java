package com.niksoftware.snapseed.views;

import android.content.Context;
import android.util.AttributeSet;

public class BaseFilterButton extends ToolButton {
    protected BaseFilterButton(Context context) {
        super(context);
    }

    public BaseFilterButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        setBackgroundDrawable(null);
    }
}
