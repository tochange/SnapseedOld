package com.niksoftware.snapseed.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.view.View;
import android.view.ViewGroup;
import com.niksoftware.snapseed.MainActivity;
import com.niksoftware.snapseed.R;
import com.niksoftware.snapseed.core.NotificationCenter;
import com.niksoftware.snapseed.core.NotificationCenterListener;
import com.niksoftware.snapseed.core.NotificationCenterListener.ListenerType;
import com.niksoftware.snapseed.core.filterparameters.FilterParameter;
import com.niksoftware.snapseed.core.filterparameters.UPointFilterParameter;
import com.niksoftware.snapseed.core.filterparameters.UPointParameter;

public class UPointActionView extends ViewGroup {
    static final /* synthetic */ boolean $assertionsDisabled = (!UPointActionView.class.desiredAssertionStatus());
    private NotificationCenterListener _didChangeFilterParameterValue;
    private Paint _fill = new Paint();
    private Rect _mRect = new Rect();
    private Paint _paint;
    private String _pname;
    private RectF _r = new RectF();
    private String _text;
    private TextLayer _textLayer;
    private int _upointCenterX;
    private int _upointCenterY;

    class TextLayer extends View {
        private int _genericSymbolHeight = 0;
        private Rect _textBounds = new Rect();

        public TextLayer(Context context) {
            super(context);
        }

        protected void onDraw(Canvas canvas) {
            float edges = (UPointActionView.this._r.height() / 2.0f) - 1.0f;
            canvas.drawRoundRect(UPointActionView.this._r, edges, edges, UPointActionView.this._fill);
            if (this._genericSymbolHeight <= 0) {
                UPointActionView.this._paint.getTextBounds("A", 0, 1, this._textBounds);
                this._genericSymbolHeight = this._textBounds.height();
            }
            UPointActionView.this._paint.getTextBounds(UPointActionView.this._text, 0, UPointActionView.this._text.length(), this._textBounds);
            canvas.drawText(UPointActionView.this._text, UPointActionView.this._r.left + (UPointActionView.this._r.width() / 2.0f), UPointActionView.this._r.bottom - ((UPointActionView.this._r.height() - ((float) this._genericSymbolHeight)) / 2.0f), UPointActionView.this._paint);
        }
    }

    public UPointActionView(Context context) {
        super(context);
        this._textLayer = new TextLayer(context);
        addView(this._textLayer);
        NotificationCenter instance = NotificationCenter.getInstance();
        NotificationCenterListener createNotificationCenterListener = createNotificationCenterListener();
        this._didChangeFilterParameterValue = createNotificationCenterListener;
        instance.addListener(createNotificationCenterListener, ListenerType.DidChangeFilterParameterValue);
        this._paint = new Paint();
        this._paint.setColor(-1073741825);
        this._paint.setTextAlign(Align.CENTER);
        this._paint.setAntiAlias(true);
        this._paint.setSubpixelText(true);
        this._paint.setTypeface(Typeface.DEFAULT_BOLD);
        this._paint.setTextSize((float) context.getResources().getDimensionPixelSize(R.dimen.upoint_action_font_size));
        this._fill.setColor(-1641272276);
        this._fill.setStyle(Style.FILL);
        this._fill.setAntiAlias(true);
    }

    public Rect getMeasuredRect() {
        FilterParameter filter = MainActivity.getFilterParameter();
        if ($assertionsDisabled || (filter instanceof UPointFilterParameter)) {
            UPointParameter filterParameter = ((UPointFilterParameter) filter).getActiveUPoint();
            int parameterId = filterParameter.getActiveFilterParameter();
            this._pname = filter.getParameterTitle(getContext(), parameterId);
            setValue(filterParameter.getParameterValueOld(parameterId));
            int ydiff = (ParameterViewHelper.highlightcp.getBitmap().getHeight() / 2) + 6;
            int left = this._upointCenterX - (getMeasuredWidth() / 2);
            int top = (this._upointCenterY - getMeasuredHeight()) - ydiff;
            if (top <= 0) {
                top = this._upointCenterY + ydiff;
            }
            Rect imageRect = MainActivity.getWorkingAreaView().getImageViewScreenRect();
            left += imageRect.left;
            top += imageRect.top;
            this._mRect.set(left, top, getMeasuredWidth() + left, getMeasuredHeight() + top);
            return this._mRect;
        }
        throw new AssertionError();
    }

    protected NotificationCenterListener createNotificationCenterListener() {
        return new NotificationCenterListener() {
            public void performAction(Object arg) {
                if (arg != null) {
                    MainActivity.getRootView().forceLayoutForFilterGUI = true;
                    MainActivity.getWorkingAreaView().requestLayout();
                }
            }
        };
    }

    public void cleanup() {
        NotificationCenter.getInstance().removeListener(this._didChangeFilterParameterValue, ListenerType.DidChangeFilterParameterValue);
        if (this._textLayer != null) {
            removeView(this._textLayer);
            this._textLayer = null;
        }
    }

    public void onLayout(boolean changed, int left, int top, int right, int bottom) {
        if (changed) {
            this._textLayer.layout(0, 0, right - left, bottom - top);
            this._r = new RectF(0.0f, 0.0f, (float) (right - left), (float) (bottom - top));
        }
    }

    public void setValue(int value) {
        this._text = this._pname + ((value > 0 ? " +" : " ") + Integer.valueOf(value).toString());
        setMeasuredDimension(((int) Math.ceil((double) this._paint.measureText(this._text))) + 20, ParameterViewHelper.getTextHeightInPixel() + 8);
        this._textLayer.invalidate();
    }

    public void setMiddle(int x, int y) {
        this._upointCenterX = x;
        this._upointCenterY = y;
    }
}
