package com.niksoftware.snapseed.controllers.touchhandlers;

import com.niksoftware.snapseed.MainActivity;
import com.niksoftware.snapseed.core.NotificationCenter;
import com.niksoftware.snapseed.core.NotificationCenterListener.ListenerType;
import com.niksoftware.snapseed.core.UndoManager;
import com.niksoftware.snapseed.core.filterparameters.FilterParameter;
import com.niksoftware.snapseed.util.TrackerData;

public class CircleHandler extends TouchHandler {
    public static float PINCH_FEEDBACK_RATIO = 1.7f;
    private boolean _pinchCreateUndo = false;
    private float _pinchSize = 0.0f;
    private float _startValue = 0.0f;

    private void showCircle(boolean visible) {
        if (visible) {
            MainActivity.getWorkingAreaView().setCircle(0.5f, 0.5f, (((float) Math.max(0, Math.min(MainActivity.getFilterParameter().getParameterValueOld(4), 100))) / 100.0f) + 0.4f);
            return;
        }
        MainActivity.getWorkingAreaView().clearGeometryObjects();
    }

    public boolean handlePinchBegin(int x, int y, float size, float arc) {
        MainActivity.getEditingToolbar().setCompareEnabled(false);
        this._startValue = (float) MainActivity.getFilterParameter().getParameterValueOld(4);
        this._pinchSize = size;
        this._pinchCreateUndo = true;
        showCircle(true);
        return true;
    }

    public void handlePinchEnd() {
        showCircle(false);
        MainActivity.getWorkingAreaView().requestRender();
        MainActivity.getEditingToolbar().setCompareEnabled(true);
    }

    public void handlePinchAbort() {
        handlePinchEnd();
    }

    public boolean handlePinch(int x, int y, float size, float arc) {
        int newValue = setParameterValue(this._pinchSize, this._startValue, size);
        FilterParameter filter = MainActivity.getFilterParameter();
        if (this._pinchCreateUndo && ((float) newValue) != this._startValue) {
            UndoManager.getUndoManager().createUndo(filter, 4);
            this._pinchCreateUndo = false;
        }
        filter.setParameterValueOld(4, newValue);
        TrackerData.getInstance().usingParameter(4, filter.isDefaultParameter(4));
        NotificationCenter.getInstance().performAction(ListenerType.DidChangeFilterParameterValue, Integer.valueOf(4));
        showCircle(true);
        MainActivity.getWorkingAreaView().requestRender();
        return true;
    }

    private int setParameterValue(float startPinchSize, float oldParamValue, float currentPinchSize) {
        return (int) calcUpdatedValue(0.0f, 100.0f, startPinchSize, currentPinchSize, oldParamValue, PINCH_FEEDBACK_RATIO);
    }

    public static float calcUpdatedValue(float minValue, float maxValue, float startPinchSize, float currentPinchSize, float startValue, float feedbackRatio) {
        float rangeSize = maxValue - minValue;
        return Math.max(0.0f, Math.min(rangeSize, startValue + ((((currentPinchSize - startPinchSize) * rangeSize) / ((float) MainActivity.getMainActivity().getMaxScreenEdgeSize())) * feedbackRatio))) + minValue;
    }
}
