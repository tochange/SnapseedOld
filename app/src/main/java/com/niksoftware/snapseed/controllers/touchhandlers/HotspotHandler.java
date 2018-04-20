package com.niksoftware.snapseed.controllers.touchhandlers;

import com.niksoftware.snapseed.MainActivity;
import com.niksoftware.snapseed.core.NotificationCenter;
import com.niksoftware.snapseed.core.NotificationCenterListener;
import com.niksoftware.snapseed.core.NotificationCenterListener.ListenerType;
import com.niksoftware.snapseed.core.UndoManager;
import com.niksoftware.snapseed.core.filterparameters.FilterParameter;
import com.niksoftware.snapseed.util.TrackerData;
import com.niksoftware.snapseed.views.WorkingAreaView;

public abstract class HotspotHandler extends HotspotHandlerBase {
    private NotificationCenterListener _didEnterEditingScreenListener;
    private int _startValue = 0;

    protected abstract float calcRadius(int i);

    protected abstract int setParameterValue(float f, float f2, float f3);

    public HotspotHandler() {
        NotificationCenter instance = NotificationCenter.getInstance();
        NotificationCenterListener anonymousClass1 = new NotificationCenterListener() {
            public void performAction(Object arg) {
                HotspotHandler.this.showHotspot(true);
                HotspotHandler.this.hideHotspotDelayed();
            }
        };
        this._didEnterEditingScreenListener = anonymousClass1;
        instance.addListener(anonymousClass1, ListenerType.DidEnterEditingScreen);
    }

    public void cleanup() {
        NotificationCenter.getInstance().removeListener(this._didEnterEditingScreenListener, ListenerType.DidEnterEditingScreen);
        super.cleanup();
    }

    protected void showCircle(boolean visible) {
        WorkingAreaView workingAreaView = MainActivity.getWorkingAreaView();
        if (visible) {
            FilterParameter params = MainActivity.getFilterParameter();
            workingAreaView.setCircle((float) FilterParameter.lowBitsAsDouble(params.getParameterValueOld(24)), (float) FilterParameter.lowBitsAsDouble(params.getParameterValueOld(25)), calcRadius(Math.max(0, Math.min(params.getParameterValueOld(4), 100))));
        } else {
            workingAreaView.clearGeometryObjects();
        }
        workingAreaView.requestRender();
    }

    public boolean handlePinchBegin(int x, int y, float size, float arc) {
        MainActivity.getEditingToolbar().setCompareEnabled(false);
        FilterParameter filterParameter = MainActivity.getFilterParameter();
        clearHotspotTimeout();
        this._startValue = filterParameter.getParameterValueOld(4);
        this._pinchSize = size;
        this._pinchCreateUndo = true;
        showHotspot(true);
        showCircle(true);
        MainActivity.getWorkingAreaView().requestRender();
        return true;
    }

    public void handlePinchEnd() {
        hideHotspotDelayed();
        showCircle(false);
        MainActivity.getWorkingAreaView().requestRender();
        MainActivity.getEditingToolbar().setCompareEnabled(true);
    }

    public void handlePinchAbort() {
        handlePinchEnd();
    }

    public boolean handlePinch(int x, int y, float size, float arc) {
        boolean z = false;
        int newValue = setParameterValue(this._pinchSize, (float) this._startValue, size);
        FilterParameter filter = MainActivity.getFilterParameter();
        if (this._pinchCreateUndo && newValue != this._startValue) {
            UndoManager.getUndoManager().createUndo(filter, 4);
            this._pinchCreateUndo = false;
        }
        filter.setParameterValueOld(4, newValue);
        TrackerData instance = TrackerData.getInstance();
        if (filter.getDefaultParameter() == 4) {
            z = true;
        }
        instance.usingParameter(4, z);
        NotificationCenter.getInstance().performAction(ListenerType.DidChangeFilterParameterValue, Integer.valueOf(4));
        showCircle(true);
        MainActivity.getWorkingAreaView().requestRender();
        return true;
    }
}
