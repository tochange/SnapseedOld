package com.niksoftware.snapseed.controllers.touchhandlers;

import android.os.Handler;
import android.view.View;
import com.niksoftware.snapseed.MainActivity;
import com.niksoftware.snapseed.core.NotificationCenter;
import com.niksoftware.snapseed.core.NotificationCenterListener.ListenerType;
import com.niksoftware.snapseed.core.UndoManager;
import com.niksoftware.snapseed.core.UndoObject;
import com.niksoftware.snapseed.core.filterparameters.FilterParameter;
import com.niksoftware.snapseed.core.filterparameters.UPointFilterParameter;
import com.niksoftware.snapseed.core.filterparameters.UPointParameter;
import com.niksoftware.snapseed.util.TrackerData;
import com.niksoftware.snapseed.views.WorkingAreaView;

public class UPointCircleHandler extends TouchHandler {
    private static final int CIRCLE_TIMEOUT = 1000;
    private int _filterParameterType;
    private Handler _handler;
    private boolean _isVisible;
    private UndoObject _pinchCreateUndo;
    private float _pinchSize;
    private float _pinchValue;
    private Runnable _timeoutHotspot;

    public UPointCircleHandler() {
        this._pinchSize = 0.0f;
        this._pinchValue = 0.0f;
        this._pinchCreateUndo = null;
        this._isVisible = false;
        this._filterParameterType = 1000;
        this._timeoutHotspot = new Runnable() {
            public void run() {
                UPointCircleHandler.this.setOff();
            }
        };
        this._handler = MainActivity.getWorkingAreaView().getHandler();
        this._filterParameterType = 4;
    }

    private boolean isEnabled() {
        return getFilterParameter().getUPointCount() > 0;
    }

    private void showCircle(boolean visible) {
        WorkingAreaView workingAreaView = MainActivity.getWorkingAreaView();
        View imageView = workingAreaView.getImageView();
        this._isVisible = visible;
        if (!visible || imageView == null) {
            workingAreaView.clearGeometryObjects();
            return;
        }
        UPointParameter upoint = getFilterParameter().getActiveUPoint();
        workingAreaView.setCircle(((float) upoint.getViewX()) / ((float) imageView.getWidth()), ((float) upoint.getViewY()) / ((float) imageView.getHeight()), ((float) ((Math.max(1, Math.min(upoint.getParameterValueOld(this._filterParameterType), 100)) + 4) * 6)) / 600.0f);
    }

    public boolean handlePinchBegin(int x, int y, float size, float arc) {
        if (!isEnabled()) {
            return false;
        }
        UPointParameter upoint = getFilterParameter().getActiveUPoint();
        if (upoint == null) {
            return false;
        }
        MainActivity.getEditingToolbar().setCompareEnabled(false);
        WorkingAreaView workingAreaView = MainActivity.getWorkingAreaView();
        this._pinchValue = (float) upoint.getCenterSize();
        this._pinchSize = size;
        this._pinchCreateUndo = new UndoObject(workingAreaView.getFilterParameter(), this._filterParameterType, MainActivity.getMainActivity().getString(R.string.param_size));
        MainActivity.getEditingToolbar().setUndoEnabled(false);
        showCircle(true);
        upoint.setInking(true);
        workingAreaView.requestRender();
        return true;
    }

    public void handlePinchEnd() {
        MainActivity.getEditingToolbar().setUndoEnabled(true);
        showCircle(false);
        UPointParameter upoint = getFilterParameter().getActiveUPoint();
        if (upoint != null) {
            upoint.setInking(false);
        }
        MainActivity.getWorkingAreaView().requestRender();
        MainActivity.getEditingToolbar().setCompareEnabled(true);
    }

    public void handlePinchAbort() {
        handlePinchEnd();
    }

    public boolean handlePinch(int x, int y, float size, float arc) {
        if (this._pinchCreateUndo != null) {
            UndoManager.getUndoManager().pushUndo(this._pinchCreateUndo);
            this._pinchCreateUndo = null;
        }
        int newValue = Math.max(0, Math.min(((int) ((this._pinchValue + 10.0f) * ((size + 10.0f) / (this._pinchSize + 10.0f)))) - 10, 100));
        WorkingAreaView workingAreaView = MainActivity.getWorkingAreaView();
        getFilterParameter().getActiveUPoint().setCenterSize(newValue);
        TrackerData.getInstance().usingParameter(4, false);
        NotificationCenter.getInstance().performAction(ListenerType.DidChangeFilterParameterValue, Integer.valueOf(this._filterParameterType));
        showCircle(true);
        MainActivity.getEditingToolbar().setUndoEnabled(false);
        workingAreaView.requestRender();
        return true;
    }

    public void setOn(boolean moveMode) {
        if (getFilterParameter().getActiveUPoint() != null) {
            if (!moveMode) {
                this._handler.removeCallbacks(this._timeoutHotspot);
                this._handler.postDelayed(this._timeoutHotspot, 1000);
            }
            showCircle(true);
            MainActivity.getWorkingAreaView().requestRender();
        }
    }

    public void setOn() {
        setOn(false);
    }

    public void setOff() {
        UPointFilterParameter upointFilter = getFilterParameter();
        UPointParameter filter = upointFilter != null ? upointFilter.getActiveUPoint() : null;
        if (filter == null || !filter.isInking()) {
            showCircle(false);
            MainActivity.getWorkingAreaView().requestRender();
        }
    }

    public void redraw() {
        if (this._isVisible) {
            setOn(true);
        }
    }

    private UPointFilterParameter getFilterParameter() {
        FilterParameter filter = MainActivity.getFilterParameter();
        return filter instanceof UPointFilterParameter ? (UPointFilterParameter) filter : null;
    }
}
