package com.niksoftware.snapseed.views;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.view.View;

import com.niksoftware.snapseed.MainActivity;
import com.niksoftware.snapseed.core.filterparameters.FilterParameter;
import com.niksoftware.snapseed.core.filterparameters.UPointFilterParameter;

/* compiled from: ScaleParameterDisplay */
class ScaleParameterValueView extends View {
    private Bitmap _displayScaleBright;
    private Rect _drawRect;
    private Paint _paint = new Paint();
    private int _scaleImageHorzMargin;
    private int _scaleStickWidth;
    private int _valueTextBaseline;

    public ScaleParameterValueView() {
        super(MainActivity.getMainActivity());
        Resources resources = getResources();
        this._displayScaleBright = BitmapFactory.decodeResource(resources, R.drawable.gfx_ct_display_skala_bright);
        this._paint.setColor(-5197657);
        this._paint.setTextAlign(Align.CENTER);
        this._paint.setAntiAlias(true);
        this._paint.setSubpixelText(true);
        this._paint.setTypeface(Typeface.DEFAULT_BOLD);
        this._paint.setTextSize(((float) resources.getDimensionPixelSize(R.dimen.tb_scale_title_font_size)) * (resources.getConfiguration().locale.getLanguage().equals("ar") ? 0.75f : 1.0f));
        this._valueTextBaseline = Math.max(getTextBaseline(resources.getString(R.string.crop_free)), getTextBaseline(resources.getString(R.string.crop_original)));
        this._scaleStickWidth = resources.getDimensionPixelSize(R.dimen.tb_scale_stick_width);
        this._scaleImageHorzMargin = resources.getDimensionPixelSize(R.dimen.tb_scale_image_horz_margin);
        this._drawRect = new Rect();
    }

    private int getTextBaseline(String text) {
        if (this._paint == null) {
            return 0;
        }
        Rect bounds = new Rect();
        this._paint.getTextBounds(text, 0, text.length(), bounds);
        return bounds.bottom;
    }

    protected void onDraw(Canvas c) {
        int filterType = MainActivity.getFilterParameter().getFilterType();
        if (filterType == 6 || filterType == 20) {
            onDrawValue(c);
        } else {
            onDrawScale(c);
        }
    }

    private void onDrawValue(Canvas c) {
        FilterParameter filter = getFilterParameter();
        c.drawText(filter.getParameterDescription(getContext(), filter.getActiveFilterParameter()), (float) (getWidth() / 2), (float) (getHeight() - this._valueTextBaseline), this._paint);
    }

    private FilterParameter getFilterParameter() {
        FilterParameter filterParameter = MainActivity.getFilterParameter();
        return (filterParameter == null || !(filterParameter instanceof UPointFilterParameter)) ? filterParameter : ((UPointFilterParameter) filterParameter).getActiveUPoint();
    }

    private void onDrawScale(Canvas c) {
        FilterParameter f = getFilterParameter();
        if (f != null) {
            int activeParam = f.getActiveFilterParameter();
            if (activeParam != 1000) {
                int left;
                int right;
                int min = f.getMinValue(activeParam);
                int max = f.getMaxValue(activeParam);
                int value = f.getParameterValueOld(activeParam);
                int scaleImageWidth = this._displayScaleBright.getWidth();
                if (min >= 0) {
                    left = 0;
                    right = (this._scaleImageHorzMargin + this._scaleStickWidth) + ((int) Math.ceil(((double) ((scaleImageWidth - (this._scaleImageHorzMargin * 2)) * (value - min))) / ((double) (max - min))));
                } else {
                    int subscaleWidth = scaleImageWidth / 2;
                    if (value >= 0) {
                        left = subscaleWidth;
                        right = ((value > 0 ? (int) Math.ceil(((double) ((subscaleWidth - this._scaleImageHorzMargin) * value)) / ((double) max)) : this._scaleStickWidth / 2) + left) + this._scaleStickWidth;
                    } else {
                        right = subscaleWidth + ((this._scaleStickWidth * 3) / 2);
                        left = (right - ((int) Math.ceil(((double) (subscaleWidth * value)) / ((double) min)))) - (this._scaleStickWidth / 2);
                    }
                }
                this._drawRect.set(Math.max(left, 0), 0, Math.min(right, scaleImageWidth), this._displayScaleBright.getHeight());
                c.drawBitmap(this._displayScaleBright, this._drawRect, this._drawRect, null);
            }
        }
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(MeasureSpec.makeMeasureSpec(this._displayScaleBright.getWidth(), 1073741824), MeasureSpec.makeMeasureSpec(this._displayScaleBright.getHeight() + 5, 1073741824));
    }
}
