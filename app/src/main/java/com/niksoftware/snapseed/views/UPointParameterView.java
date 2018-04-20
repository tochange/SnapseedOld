package com.niksoftware.snapseed.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.View;

import com.niksoftware.snapseed.core.FilterDefs.FilterParameterType;
import com.niksoftware.snapseed.core.filterparameters.UPointParameter;

public class UPointParameterView extends View {
    private static final int BORDER_X_INSET = 5;
    private static final int BORDER_X_INSET_PX = ParameterViewHelper.dipToPx(5);
    private static final int BORDER_Y_INSET = 3;
    private static final int BORDER_Y_INSET_PX = ParameterViewHelper.dipToPx(3);
    public static final int PARAM_GAP = ParameterViewHelper.dipToPx(10);
    private static final int TEXT_SIZE = 21;
    private static BitmapDrawable _inactivecp = ParameterViewHelper.getDrawableFromResources(R.drawable.gfx_ct_cp_disabled);
    private int _activeParameter = 0;
    private int _menuHeight;
    private int _menuWidth = 0;
    private int _nrOfParameters = 0;
    private int[] _parameters;
    private Bitmap _prerenderedViewBitmap;
    private UPointParameter _pset;
    private int[] _types;
    private Paint backgroundPaint = new Paint();

    public UPointParameterView(Context context, UPointParameter pset, int[] types) {
        super(context);
        this._pset = pset;
        this._types = types;
        this._menuWidth = getMenuWidth();
        init(types, pset.getActiveFilterParameter());
    }

    public void init(int[] parameters, int activeParameter) {
        if (parameters != null && parameters.length != 0) {
            this._parameters = parameters;
            this._nrOfParameters = parameters.length;
            this._menuWidth = getMenuWidth();
            new Paint().setTextSize(21.0f);
            this._menuHeight = getMenuHeight(this._nrOfParameters, true);
            setActiveParameterIndex(activeParameter, true);
        }
    }

    public UPointParameter getUPointParameter() {
        return this._pset;
    }

    public static int getMenuWidth() {
        return (BORDER_X_INSET_PX * 2) + _inactivecp.getIntrinsicWidth();
    }

    public static int getMenuHeight(int parameterCount, boolean withBorderInsets) {
        return (withBorderInsets ? BORDER_Y_INSET_PX * 2 : 0) + ((getMenuItemHeight(true) * parameterCount) - PARAM_GAP);
    }

    public int getMenuHeight() {
        return getMenuHeight(this._nrOfParameters, true);
    }

    public static int getMenuItemHeight(boolean withParameterGap) {
        return withParameterGap ? _inactivecp.getIntrinsicHeight() + PARAM_GAP : _inactivecp.getIntrinsicHeight();
    }

    public void setActiveParameterIndex(int activeParameter) {
        setActiveParameterIndex(activeParameter, false);
    }

    private void setActiveParameterIndex(int activeParameter, boolean forceUpdate) {
        if (forceUpdate || this._activeParameter != activeParameter) {
            this._activeParameter = activeParameter;
            this._prerenderedViewBitmap = prerenderView();
        }
    }

    public void onMeasure(int a, int b) {
        setMeasuredDimension(this._menuWidth, this._menuHeight);
    }

    protected void onDraw(Canvas canvas) {
        if (this._prerenderedViewBitmap != null) {
            canvas.drawBitmap(this._prerenderedViewBitmap, 0.0f, 0.0f, this.backgroundPaint);
        }
    }

    private Bitmap prerenderView() {
        if (this._menuWidth * this._menuHeight <= 0) {
            return null;
        }
        Bitmap bitmap = Bitmap.createBitmap(this._menuWidth, this._menuHeight, Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Drawable background = getResources().getDrawable(R.drawable.gfx_ct_cp_bg);
        background.setBounds(0, 0, this._menuWidth, this._menuHeight);
        background.draw(canvas);
        Bitmap inactiveCPBitmap = _inactivecp.getBitmap();
        Rect sourceRect = new Rect(0, 0, inactiveCPBitmap.getWidth(), inactiveCPBitmap.getHeight());
        Rect destRect = new Rect(0, 0, _inactivecp.getIntrinsicWidth(), _inactivecp.getIntrinsicHeight());
        int start_y = BORDER_Y_INSET_PX;
        int itemHeight = getMenuItemHeight(true);
        int width = canvas.getWidth();
        for (int i = 0; i < this._types.length; i++) {
            if (this._parameters[i] != this._activeParameter) {
                destRect.offsetTo(BORDER_X_INSET_PX, start_y);
                canvas.drawBitmap(inactiveCPBitmap, sourceRect, destRect, new Paint());
                String label = FilterParameterType.getParameterTitle(getContext(), this._types[i]).substring(0, 1);
                Rect textDims = new Rect();
                ParameterViewHelper.getPaint().getTextBounds(label, 0, label.length(), textDims);
                ParameterViewHelper.drawTextCentered(label, width / 2, ((_inactivecp.getIntrinsicHeight() + textDims.height()) / 2) + start_y, canvas);
            }
            start_y += itemHeight;
        }
        return bitmap;
    }
}
