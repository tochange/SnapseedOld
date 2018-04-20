package com.niksoftware.snapseed.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.view.View;
import com.niksoftware.snapseed.MainActivity;
import com.niksoftware.snapseed.R;
import com.niksoftware.snapseed.core.filterparameters.FilterParameter;
import java.util.ArrayList;

public class ParameterView extends View {
    private int _activeParameter = -1;
    private int _activeY = 0;
    private final Drawable _background = getResources().getDrawable(R.drawable.gfx_ct_paramselect_bg);
    private final Drawable _inactiveItemBackground = getResources().getDrawable(R.drawable.gfx_ct_paramselect_disabled);
    private int _limitBottom = 0;
    private int _limitTop = 0;
    private int _menuWidth = 0;
    private int _middleX = 0;
    private int _middleY = 0;
    private int _nrOfParameters = 0;
    private ArrayList<String> _parameterLabels = new ArrayList();
    private int[] _parameters;
    private int _startX = 0;
    private int _startY = 0;
    private int _textHeight = 0;
    private Paint paint;
    boolean toggle = true;

    ParameterView(Context context) {
        super(context);
        Paint paint = new Paint();
        paint.setTextSize((float) ParameterViewHelper.TEXT_SIZE());
        Rect textDims = new Rect();
        paint.getTextBounds("A", 0, 1, textDims);
        this._textHeight = textDims.height();
    }

    public void updateParameterMenu(int offset) {
        if (this._parameters != null && this._parameters.length != 0) {
            int old_Y = this._startY;
            this._startY += offset;
            this._startY = Math.max(this._limitTop, this._startY);
            this._startY = Math.min(this._limitBottom, this._startY);
            int menuHeight = ((this._nrOfParameters * ParameterViewHelper.SINGLE_PARAM_HEIGHT()) + 16) + ((this._nrOfParameters - 1) * 12);
            int right = (this._startX + this._menuWidth) + 12;
            Rect r1 = new Rect(this._startX, old_Y, right, old_Y + menuHeight);
            Rect r2 = new Rect(this._startX, this._startY, right, this._startY + menuHeight);
            r1.union(r2);
            invalidate(r1);
            if (isActiveParameterViewModified()) {
                MainActivity.getWorkingAreaView().getActiveParameterView().invalidate(r2);
                MainActivity.getFilterParameter().setActiveFilterParameter(this._activeParameter);
            }
        }
    }

    public int getActiveParameterIndex() {
        return this._activeParameter;
    }

    public void setActiveParameterIndex(int activeParameter) {
        if (this._activeParameter != activeParameter) {
            updateMenuPosition();
        }
    }

    public int getMenuWidth() {
        return this._menuWidth;
    }

    private boolean filterToggle(int i) {
        if (this.toggle) {
            this._activeParameter = this._parameters[i];
            this.toggle = false;
            return true;
        }
        this.toggle = true;
        return false;
    }

    private boolean clearToggle() {
        this.toggle = false;
        return false;
    }

    private boolean isActiveParameterViewModified() {
        int currentStartY = (this._startY - 8) - 6;
        int newActiveParamIndex = 0;
        while (newActiveParamIndex < this._nrOfParameters) {
            int currentEndY = (ParameterViewHelper.SINGLE_PARAM_HEIGHT() + currentStartY) + 12;
            if ((newActiveParamIndex == 0 || currentStartY < this._activeY) && (newActiveParamIndex == this._nrOfParameters - 1 || this._activeY <= currentEndY)) {
                break;
            }
            currentStartY = currentEndY;
            newActiveParamIndex++;
        }
        if (newActiveParamIndex < this._nrOfParameters && this._activeParameter != this._parameters[newActiveParamIndex]) {
            return filterToggle(newActiveParamIndex);
        }
        clearToggle();
        return false;
    }

    private void initPaints() {
        float shadowSize = getResources().getDimension(R.dimen.parameter_title_shadow_size);
        this.paint = new Paint();
        this.paint.setColor(-1711276033);
        this.paint.setTextSize((float) ParameterViewHelper.TEXT_SIZE());
        this.paint.setTextAlign(Align.CENTER);
        this.paint.setAntiAlias(true);
        this.paint.setAlpha(128);
        this.paint.setSubpixelText(true);
        this.paint.setFakeBoldText(true);
        this.paint.setShadowLayer(shadowSize, 0.0f, -shadowSize, -1728053248);
    }

    public void init(FilterParameter filterParameter, int[] parameters, int activeParameter) {
        if (parameters != null && parameters.length != 0) {
            this._parameters = parameters;
            this._activeParameter = activeParameter;
            this._nrOfParameters = parameters.length;
            this._menuWidth = 0;
            this._parameterLabels.clear();
            Paint paint = new Paint();
            paint.setTextSize((float) ParameterViewHelper.TEXT_SIZE());
            int maxTextLength = 0;
            int longestTextIndex = -1;
            for (int i = 0; i < parameters.length; i++) {
                this._parameterLabels.add(filterParameter.getParameterTitle(getContext(), parameters[i]));
                if (((String) this._parameterLabels.get(i)).length() > maxTextLength) {
                    maxTextLength = ((String) this._parameterLabels.get(i)).length();
                    longestTextIndex = i;
                }
            }
            if (longestTextIndex != -1) {
                this._menuWidth = Math.max(this._menuWidth, (int) paint.measureText((String) this._parameterLabels.get(longestTextIndex)));
            }
            this._menuWidth += ((this._menuWidth % 2) + 16) + 30;
            updateMenuPosition();
            initPaints();
        }
    }

    private void updateMenuPosition() {
        if (this._parameters != null) {
            this._activeY = this._middleY - (ParameterViewHelper.SINGLE_PARAM_HEIGHT() / 2);
            this._startX = (this._middleX - (this._menuWidth / 2)) - 6;
            boolean foundActiveParam = false;
            for (int i = 0; i < this._parameters.length; i++) {
                if (this._activeParameter == this._parameters[i]) {
                    foundActiveParam = true;
                    this._startY = (this._activeY - 8) - ((ParameterViewHelper.SINGLE_PARAM_HEIGHT() + 12) * i);
                    this._limitTop = this._startY - (((this._parameters.length - i) - 1) * (ParameterViewHelper.SINGLE_PARAM_HEIGHT() + 12));
                    this._limitBottom = this._startY + ((ParameterViewHelper.SINGLE_PARAM_HEIGHT() + 12) * i);
                    break;
                }
            }
            if (!foundActiveParam) {
                throw new IllegalStateException("Active parameter is missing");
            }
        }
    }

    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        this._middleX = (left + right) / 2;
        this._middleY = (top + bottom) / 2;
        updateMenuPosition();
    }

    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (this._parameters != null && this._parameters.length != 0) {
            this._background.setBounds(this._startX, this._startY, (this._startX + this._menuWidth) + 12, this._startY + (((this._nrOfParameters * ParameterViewHelper.SINGLE_PARAM_HEIGHT()) + 16) + ((this._nrOfParameters - 1) * 12)));
            this._background.draw(canvas);
            for (int i = 0; i < this._nrOfParameters; i++) {
                int start_x = this._startX + 6;
                int end_x = start_x + this._menuWidth;
                int start_y = (this._startY + 8) + ((ParameterViewHelper.SINGLE_PARAM_HEIGHT() + 12) * i);
                int end_y = ((this._startY + 8) + ((ParameterViewHelper.SINGLE_PARAM_HEIGHT() + 12) * i)) + ParameterViewHelper.SINGLE_PARAM_HEIGHT();
                if (this._activeParameter != this._parameters[i]) {
                    this._inactiveItemBackground.setBounds(start_x, start_y, end_x, end_y);
                    canvas.drawText((String) this._parameterLabels.get(i), (float) ((start_x + end_x) / 2), (float) (end_y - ((ParameterViewHelper.SINGLE_PARAM_HEIGHT() - this._textHeight) / 2)), this.paint);
                    this._inactiveItemBackground.draw(canvas);
                }
            }
        }
    }
}
