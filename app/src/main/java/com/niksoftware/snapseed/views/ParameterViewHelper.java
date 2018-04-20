package com.niksoftware.snapseed.views;

import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.util.TypedValue;
import com.niksoftware.snapseed.MainActivity;
import com.niksoftware.snapseed.core.FilterDefs.FilterParameterType;

public class ParameterViewHelper {
    public static final int PARAM_NAME_GLOW_RADIUS = 2;
    public static final int PARAM_XGAP = 6;
    public static final int PARAM_YGAP = 12;
    public static final int SINGLE_GAP = 8;
    public static final BitmapDrawable highlightcp = getDrawableFromResources(R.drawable.gfx_ct_cp_active);
    public static final ParameterViewHelper instance = new ParameterViewHelper();
    private int _textHeightInPixel;
    private Paint _textPaint = new Paint();
    private Paint _whiteTextGlow1Paint;
    private Paint _whiteTextGlow2Paint;

    public static int SINGLE_PARAM_HEIGHT() {
        return MainActivity.getMainActivity().getResources().getDimensionPixelSize(R.dimen.tmp_active_param_item_height);
    }

    public static int TEXT_SIZE() {
        return MainActivity.getMainActivity().getResources().getDimensionPixelSize(R.dimen.cp_font_size);
    }

    private ParameterViewHelper() {
        Rect textDims = new Rect();
        Paint paint = new Paint();
        paint.setTextSize((float) TEXT_SIZE());
        paint.getTextBounds("A", 0, 1, textDims);
        this._textHeightInPixel = textDims.bottom - textDims.top;
        this._textPaint.setColor(-1);
        this._textPaint.setTextAlign(Align.CENTER);
        this._textPaint.setAntiAlias(true);
        this._textPaint.setSubpixelText(true);
        this._textPaint.setTextSize((float) TEXT_SIZE());
        this._textPaint.setTypeface(Typeface.DEFAULT_BOLD);
        this._textPaint.setShadowLayer(1.0f, 0.0f, -1.0f, -788529152);
        this._whiteTextGlow1Paint = new Paint();
        this._whiteTextGlow1Paint.setColor(-1);
        this._whiteTextGlow1Paint.setTextAlign(Align.CENTER);
        this._whiteTextGlow1Paint.setAntiAlias(true);
        this._whiteTextGlow1Paint.setSubpixelText(true);
        this._whiteTextGlow1Paint.setTextSize((float) TEXT_SIZE());
        this._whiteTextGlow1Paint.setTypeface(Typeface.DEFAULT_BOLD);
        this._whiteTextGlow1Paint.setShadowLayer(2.0f, 1.0f, 1.0f, -16777216);
        this._whiteTextGlow2Paint = new Paint(this._whiteTextGlow1Paint);
        this._whiteTextGlow2Paint.setShadowLayer(2.0f, -1.0f, -1.0f, -16777216);
    }

    public static BitmapDrawable getDrawableFromResources(int res) {
        return getDrawableFromResources(res, MainActivity.getMainActivity().getResources());
    }

    public static BitmapDrawable getDrawableFromResources(int res, Resources resources) {
        BitmapDrawable drawable = new BitmapDrawable(resources, BitmapFactory.decodeResource(resources, res));
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        return drawable;
    }

    public static void drawTextCentered(String text, int x, int y, Canvas canvas) {
        canvas.drawText(text, (float) x, (float) y, instance._textPaint);
    }

    public static void drawTextLeftGlowing(String text, int x, int y, Canvas canvas) {
        int horzCenter = canvas.getWidth() / 2;
        canvas.drawText(text, (float) horzCenter, (float) ((getTextHeightInPixel() + y) + 2), instance._whiteTextGlow1Paint);
        canvas.drawText(text, (float) horzCenter, (float) ((getTextHeightInPixel() + y) + 2), instance._whiteTextGlow2Paint);
    }

    public static int getMaximumParameterWidth(int[] keys) {
        float maxWidth = 0.0f;
        for (int parameterTitle : keys) {
            maxWidth = Math.max(maxWidth, instance._textPaint.measureText(FilterParameterType.getParameterTitle(MainActivity.getMainActivity(), parameterTitle)));
        }
        return (int) Math.ceil((double) maxWidth);
    }

    static Paint getPaint() {
        return instance._textPaint;
    }

    public static int getTextHeightInPixel() {
        return instance._textHeightInPixel;
    }

    public static int dipToPx(int dip) {
        return (int) TypedValue.applyDimension(1, (float) dip, MainActivity.getMainActivity().getResources().getDisplayMetrics());
    }
}
