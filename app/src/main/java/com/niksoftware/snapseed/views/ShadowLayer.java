package com.niksoftware.snapseed.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.GradientDrawable.Orientation;
import android.view.ViewGroup;

public class ShadowLayer extends ViewGroup {
    private ColorDrawable _background;
    private boolean _drawShadow = true;
    private GradientDrawable _gradient_b_l;
    private GradientDrawable _gradient_b_r;
    private GradientDrawable _gradient_bottom;
    private GradientDrawable _gradient_left;
    private GradientDrawable _gradient_right;
    private GradientDrawable _gradient_t_l;
    private GradientDrawable _gradient_t_r;
    private GradientDrawable _gradient_top;
    private boolean _hasInitGradients = false;
    private int _shadowSize;

    public ShadowLayer(Context context) {
        super(context);
        this._shadowSize = context.getResources().getDimensionPixelSize(R.dimen.wa_image_shadow_size);
        setWillNotDraw(false);
        setWillNotCacheDrawing(false);
        this._background = new ColorDrawable(-16777216);
        int[] colors = new int[]{2130706432, 0};
        this._gradient_top = new GradientDrawable(Orientation.BOTTOM_TOP, colors);
        this._gradient_bottom = new GradientDrawable(Orientation.TOP_BOTTOM, colors);
        this._gradient_left = new GradientDrawable(Orientation.RIGHT_LEFT, colors);
        this._gradient_right = new GradientDrawable(Orientation.LEFT_RIGHT, colors);
        this._gradient_t_l = new GradientDrawable(Orientation.TL_BR, colors);
        this._gradient_t_r = new GradientDrawable(Orientation.TR_BL, colors);
        this._gradient_b_l = new GradientDrawable(Orientation.BL_TR, colors);
        this._gradient_b_r = new GradientDrawable(Orientation.BR_TL, colors);
        this._gradient_top.setGradientType(0);
        this._gradient_top.setDither(true);
        this._gradient_bottom.setGradientType(0);
        this._gradient_bottom.setDither(true);
        this._gradient_left.setGradientType(0);
        this._gradient_left.setDither(true);
        this._gradient_right.setGradientType(0);
        this._gradient_right.setDither(true);
        this._gradient_t_l.setGradientType(1);
        this._gradient_t_l.setGradientRadius((float) this._shadowSize);
        this._gradient_t_l.setGradientCenter(1.0f, 1.0f);
        this._gradient_t_l.setDither(true);
        this._gradient_t_r.setGradientType(1);
        this._gradient_t_r.setGradientRadius((float) this._shadowSize);
        this._gradient_t_r.setGradientCenter(0.0f, 1.0f);
        this._gradient_t_r.setDither(true);
        this._gradient_b_l.setGradientType(1);
        this._gradient_b_l.setGradientRadius((float) this._shadowSize);
        this._gradient_b_l.setGradientCenter(1.0f, 0.0f);
        this._gradient_b_l.setDither(true);
        this._gradient_b_r.setGradientType(1);
        this._gradient_b_r.setGradientRadius((float) this._shadowSize);
        this._gradient_b_r.setGradientCenter(0.0f, 0.0f);
        this._gradient_b_r.setDither(true);
        setPadding(this._shadowSize, this._shadowSize, this._shadowSize, this._shadowSize);
    }

    public void setShadowVisible(boolean visible) {
        if (visible != this._drawShadow) {
            this._drawShadow = visible;
            invalidate();
        }
    }

    public Rect imageToShadowRect(Rect imageRect) {
        Rect shadowRect = new Rect(imageRect);
        shadowRect.inset(-this._shadowSize, -this._shadowSize);
        return shadowRect;
    }

    public void shadowToImageRect(Rect shadowRect) {
        shadowRect.inset(this._shadowSize, this._shadowSize);
    }

    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        layoutChildren();
    }

    public void layoutChildren() {
        int count = getChildCount();
        for (int i = 0; i < count; i++) {
            getChildAt(i).layout(this._shadowSize, this._shadowSize, getWidth() - this._shadowSize, getHeight() - this._shadowSize);
        }
        initGradients(new Rect(0, 0, getWidth(), getHeight()));
        requestLayout();
    }

    protected void onDraw(Canvas canvas) {
        if (this._hasInitGradients && this._drawShadow) {
            this._background.draw(canvas);
            this._gradient_top.draw(canvas);
            this._gradient_bottom.draw(canvas);
            this._gradient_left.draw(canvas);
            this._gradient_right.draw(canvas);
            this._gradient_t_l.draw(canvas);
            this._gradient_t_r.draw(canvas);
            this._gradient_b_l.draw(canvas);
            this._gradient_b_r.draw(canvas);
        }
    }

    private void initGradients(Rect rect) {
        this._background.setBounds(this._shadowSize, this._shadowSize, rect.right - this._shadowSize, rect.bottom - this._shadowSize);
        this._gradient_top.setBounds(rect.left + this._shadowSize, rect.top, rect.right - this._shadowSize, rect.top + this._shadowSize);
        this._gradient_bottom.setBounds(rect.left + this._shadowSize, rect.bottom - this._shadowSize, rect.right - this._shadowSize, rect.bottom);
        this._gradient_left.setBounds(rect.left, rect.top + this._shadowSize, rect.left + this._shadowSize, rect.bottom - this._shadowSize);
        this._gradient_right.setBounds(rect.right - this._shadowSize, rect.top + this._shadowSize, rect.right, rect.bottom - this._shadowSize);
        this._gradient_t_l.setBounds(rect.left, rect.top, rect.left + this._shadowSize, rect.top + this._shadowSize);
        this._gradient_t_r.setBounds(rect.right - this._shadowSize, rect.top, rect.right, rect.top + this._shadowSize);
        this._gradient_b_l.setBounds(rect.left, rect.bottom - this._shadowSize, rect.left + this._shadowSize, rect.bottom);
        this._gradient_b_r.setBounds(rect.right - this._shadowSize, rect.bottom - this._shadowSize, rect.right, rect.bottom);
        this._hasInitGradients = true;
    }
}
