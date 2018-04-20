package com.niksoftware.snapseed.controllers.touchhandlers;

import android.content.Context;
import android.graphics.Rect;
import com.niksoftware.snapseed.MainActivity;
import com.niksoftware.snapseed.core.NotificationCenter;
import com.niksoftware.snapseed.core.NotificationCenterListener;
import com.niksoftware.snapseed.core.NotificationCenterListener.ListenerType;
import com.niksoftware.snapseed.core.filterparameters.FilterParameter;
import com.niksoftware.snapseed.core.filterparameters.UPointFilterParameter;
import com.niksoftware.snapseed.core.filterparameters.UPointParameter;
import com.niksoftware.snapseed.util.TrackerData;
import com.niksoftware.snapseed.views.ParameterViewHelper;
import com.niksoftware.snapseed.views.UPointActionView;
import com.niksoftware.snapseed.views.UPointParameterNameView;
import com.niksoftware.snapseed.views.UPointParameterView;
import com.niksoftware.snapseed.views.WorkingAreaView;

public class UPointParameterHandler extends ParameterHandlerBase {
    static final /* synthetic */ boolean $assertionsDisabled = (!UPointParameterHandler.class.desiredAssertionStatus());
    private UPointActionView _aview;
    private int _delta;
    private NotificationCenterListener _didChangeCompareImageMode;
    private boolean _isInCompareMode = false;
    private UPointParameterNameView _paramNameView;
    private UPointParameterView _pview;
    private int[] _upointGuiParamTypes;

    public UPointParameterHandler(int[] upointGuiParamTypes) {
        this._upointGuiParamTypes = upointGuiParamTypes;
        NotificationCenter instance = NotificationCenter.getInstance();
        NotificationCenterListener anonymousClass1 = new NotificationCenterListener() {
            public void performAction(Object arg) {
                int i = 4;
                UPointParameterHandler.this._isInCompareMode = ((Boolean) arg).booleanValue();
                if (UPointParameterHandler.this._pview != null) {
                    UPointParameterHandler.this._pview.setVisibility(UPointParameterHandler.this._isInCompareMode ? 4 : 0);
                }
                if (UPointParameterHandler.this._paramNameView != null) {
                    UPointParameterNameView access$200 = UPointParameterHandler.this._paramNameView;
                    if (!UPointParameterHandler.this._isInCompareMode) {
                        i = 0;
                    }
                    access$200.setVisibility(i);
                }
            }
        };
        this._didChangeCompareImageMode = anonymousClass1;
        instance.addListener(anonymousClass1, ListenerType.DidChangeCompareImageMode);
    }

    public void cleanup() {
        NotificationCenter.getInstance().removeListener(this._didChangeCompareImageMode, ListenerType.DidChangeCompareImageMode);
    }

    public void layout(boolean changed, int left, int top, int right, int bottom) {
        if (this._aview != null) {
            Rect rect = this._aview.getMeasuredRect();
            this._aview.layout(rect.left, rect.top, rect.right, rect.bottom);
        }
        if (this._pview != null) {
            UPointParameter p = this._pview.getUPointParameter();
            int x0 = (p.getViewX() + left) - (UPointParameterView.getMenuWidth() / 2);
            int y0 = ((p.getViewY() + top) - (ParameterViewHelper.SINGLE_PARAM_HEIGHT() / 2)) + this._delta;
            this._pview.layout(x0, y0, UPointParameterView.getMenuWidth() + x0, this._pview.getMenuHeight() + y0);
            x0 += this._pview.getMeasuredWidth() + 6;
            y0 = (p.getViewY() + top) - (ParameterViewHelper.SINGLE_PARAM_HEIGHT() / 2);
            this._paramNameView.layout(x0, y0, this._paramNameView.getMeasuredWidth() + x0, ParameterViewHelper.SINGLE_PARAM_HEIGHT() + y0);
            if (this._paramNameView.getMeasuredWidth() + x0 < MainActivity.getWorkingAreaView().getRight()) {
                this._paramNameView.layout(x0, y0, this._paramNameView.getMeasuredWidth() + x0, ParameterViewHelper.SINGLE_PARAM_HEIGHT() + y0);
                return;
            }
            x0 = (x0 - (this._pview.getMeasuredWidth() + 6)) - this._paramNameView.getMeasuredWidth();
            this._paramNameView.layout(x0, y0, this._paramNameView.getMeasuredWidth() + x0, ParameterViewHelper.SINGLE_PARAM_HEIGHT() + y0);
        }
    }

    private int calcDelta() {
        return calcDelta(getFilterParameter().getActiveUPoint().getActiveFilterParameter());
    }

    private int calcDelta(int activeParameter) {
        return (-activeParameter) * UPointParameterView.getMenuItemHeight(true);
    }

    private int calcMaxDelta() {
        return (-UPointParameterView.getMenuHeight(this._upointGuiParamTypes.length - 1, false)) - UPointParameterView.PARAM_GAP;
    }

    public void showParameterView(boolean visible) {
        WorkingAreaView workingAreaView = MainActivity.getWorkingAreaView();
        if (visible) {
            UPointParameter upoint = getFilterParameter().getActiveUPoint();
            if (upoint != null) {
                Context context = MainActivity.getMainActivity();
                this._pview = new UPointParameterView(context, upoint, this._upointGuiParamTypes);
                workingAreaView.addView(this._pview, workingAreaView.getChildCount() - 1);
                this._pview.measure(0, 0);
                this._paramNameView = new UPointParameterNameView(context, this._upointGuiParamTypes);
                workingAreaView.addView(this._paramNameView, workingAreaView.getChildCount() - 1);
                this._paramNameView.measure(0, 0);
                this._delta = calcDelta();
                MainActivity.getRootView().forceLayoutForFilterGUI = true;
                workingAreaView.requestLayout();
                return;
            }
            return;
        }
        if (this._pview != null) {
            workingAreaView.removeView(this._pview);
            this._pview = null;
        }
        if (this._paramNameView != null) {
            workingAreaView.removeView(this._paramNameView);
            this._paramNameView = null;
        }
    }

    public boolean onChangeValue(float delta) {
        boolean z = true;
        UPointParameter upointParameter = getFilterParameter().getActiveUPoint();
        if (upointParameter == null) {
            return false;
        }
        int activeFilterParameter = upointParameter.getActiveFilterParameter();
        int d = Math.round(delta / (1280.0f / ((float) (upointParameter.getMaxValue(activeFilterParameter) - upointParameter.getMinValue(activeFilterParameter)))));
        boolean needUpdate = upointParameter.setParameterValueOld(activeFilterParameter, upointParameter.getParameterValueOld(activeFilterParameter) + d);
        TrackerData.getInstance().usingParameter(activeFilterParameter, upointParameter.getDefaultParameter() == activeFilterParameter);
        if (needUpdate) {
            WorkingAreaView workingAreaView = MainActivity.getWorkingAreaView();
            NotificationCenter.getInstance().performAction(ListenerType.DidChangeFilterParameterValue, Integer.valueOf(workingAreaView.getActiveFilterType()));
            workingAreaView.requestRender();
        }
        if (d == 0) {
            z = false;
        }
        return z;
    }

    public void updateParameterMenu(int yOffset) {
        this._delta += yOffset;
        this._delta = Math.max(calcMaxDelta(), this._delta);
        this._delta = Math.min(0, this._delta);
        MainActivity.getRootView().forceLayoutForFilterGUI = true;
        MainActivity.getWorkingAreaView().requestLayout();
        UPointParameter upoint = getFilterParameter().getActiveUPoint();
        if (upoint != null) {
            int mindiff = Integer.MAX_VALUE;
            int mindiffIndex = -1;
            for (int i = 0; i < this._upointGuiParamTypes.length; i++) {
                int diff = Math.abs(this._delta - calcDelta(i));
                if (diff < mindiff) {
                    mindiff = diff;
                    mindiffIndex = i;
                }
            }
            if (upoint.getActiveFilterParameter() != mindiffIndex) {
                upoint.setActiveFilterParameter(mindiffIndex);
                this._paramNameView.setTitle(upoint.getParameterTitle(MainActivity.getMainActivity(), mindiffIndex));
            }
            this._pview.setActiveParameterIndex(upoint.getActiveFilterParameter());
            this._pview.invalidate();
        }
    }

    protected void onStartChangeParameter() {
        MainActivity.getEditingToolbar().setCompareEnabled(false);
        UPointParameter upoint = getFilterParameter().getActiveUPoint();
        if (upoint != null) {
            this._aview = new UPointActionView(MainActivity.getWorkingAreaView().getContext());
            this._aview.setMiddle(upoint.getViewX(), upoint.getViewY());
            MainActivity.getWorkingAreaView().addView(this._aview);
        }
    }

    protected void onEndChangeParameter() {
        MainActivity.getEditingToolbar().setCompareEnabled(true);
        if (this._aview != null) {
            MainActivity.getWorkingAreaView().removeView(this._aview);
            this._aview.cleanup();
            this._aview = null;
        }
    }

    private UPointFilterParameter getFilterParameter() {
        FilterParameter filter = MainActivity.getFilterParameter();
        if ($assertionsDisabled || (filter instanceof UPointFilterParameter)) {
            return (UPointFilterParameter) filter;
        }
        throw new AssertionError("Invalid filter parameter");
    }
}
