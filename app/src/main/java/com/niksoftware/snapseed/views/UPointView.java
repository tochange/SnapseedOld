package com.niksoftware.snapseed.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.widget.TextView;

import com.niksoftware.snapseed.R;
import com.niksoftware.snapseed.core.FilterDefs.FilterParameterType;
import com.niksoftware.snapseed.core.filterparameters.UPointParameter;

public class UPointView extends TextView {
    private UPointParameter _param = null;
    private Paint _valuePaint;
    private RectF _valueRect;
    private int _valueStrokeWidth = getResources().getDimensionPixelSize(R.dimen.cp_value_indication_stroke_size);

    public UPointView(Context context, UPointParameter param) {
        super(context);
        setGravity(17);
        setActive(true);
        measure(MeasureSpec.makeMeasureSpec(0, 0), MeasureSpec.makeMeasureSpec(0, 0));
        this._param = param;
        this._valuePaint = new Paint();
        this._valuePaint.setStyle(Style.STROKE);
        this._valuePaint.setStrokeWidth((float) this._valueStrokeWidth);
        this._valuePaint.setAntiAlias(true);
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        Drawable background = getBackground();
        setMeasuredDimension(background.getIntrinsicWidth(), background.getIntrinsicHeight());
    }

    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        this._valueRect = new RectF((float) this._valueStrokeWidth, (float) this._valueStrokeWidth, (float) ((right - left) - this._valueStrokeWidth), (float) ((bottom - top) - this._valueStrokeWidth));
    }

    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (this._param.isSelected()) {
            int value = this._param.getParameterValueOld(this._param.getActiveFilterParameter());
            if (value != 0) {
                this._valuePaint.setColor(value < 0 ? -65536 : -16711936);
                canvas.drawArc(this._valueRect, -90.0f, 360.0f * (((float) value) / 100.0f), false, this._valuePaint);
            }
        }
    }

    public double getCenterX(int parentWidth, int parentHeight) {
        return ((double) this._param.getViewX()) / ((double) parentWidth);
    }

    public double getCenterY(int parentWidth, int parentHeight) {
        return ((double) this._param.getViewY()) / ((double) parentHeight);
    }

    public UPointParameter getParameter() {
        return this._param;
    }

    public void setParameter(UPointParameter param) {
        this._param = param;
        updateTitle();
    }

    public void updateTitle() {
        setText(FilterParameterType.getParameterTitle(getContext(), this._param.getParameterKeys()[this._param.getActiveFilterParameter()]).substring(0, 1));
    }

    public void setActive(boolean active) {
        if (active) {
            setBackgroundResource(R.drawable.gfx_ct_cp_active);
            setTextAppearance(getContext(), R.style.ControlPoint.Active);
            setShadowLayer(1.0f, 0.0f, -1.0f, -771751936);
            return;
        }
        setBackgroundResource(R.drawable.gfx_ct_cp_default);
        setTextAppearance(getContext(), R.style.ControlPoint.Inactive);
        setShadowLayer(0.0f, 0.0f, 0.0f, 0);
    }
}
