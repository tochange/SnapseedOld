package com.niksoftware.snapseed.controllers.touchhandlers;

import com.niksoftware.snapseed.MainActivity;
import com.niksoftware.snapseed.core.UndoManager;
import com.niksoftware.snapseed.core.UndoObject;
import com.niksoftware.snapseed.core.filterparameters.FilterParameter;

public abstract class ParameterHandlerBase extends TouchHandler {
    private TouchMainDirection _direction = TouchMainDirection.None;
    private float _oldX;
    private float _oldY;
    private UndoObject _undo;

    protected enum TouchMainDirection {
        None,
        Undefined,
        LeftRight,
        TopBottom
    }

    public abstract boolean onChangeValue(float f);

    public abstract void showParameterView(boolean z);

    public abstract void updateParameterMenu(int i);

    public boolean handleTouchDown(float x, float y) {
        this._oldX = x;
        this._oldY = y;
        this._direction = TouchMainDirection.Undefined;
        return false;
    }

    public void handleTouchUp(float x, float y) {
        if (this._direction == TouchMainDirection.LeftRight) {
            resetTouch();
        } else if (this._direction == TouchMainDirection.TopBottom) {
            showParameterView(false);
        }
    }

    public boolean handleTouchMoved(float x, float y) {
        float deltaX = x - this._oldX;
        float deltaY = y - this._oldY;
        boolean changed = true;
        if (this._direction == TouchMainDirection.Undefined) {
            if (Math.max(Math.abs(deltaX), Math.abs(deltaY)) < 25.0f) {
                return false;
            }
            if (Math.abs(deltaX) < Math.abs(deltaY)) {
                this._direction = TouchMainDirection.TopBottom;
                showParameterView(true);
            } else {
                FilterParameter filter = MainActivity.getFilterParameter();
                this._undo = new UndoObject(filter, filter.getActiveFilterParameter(), false);
                this._direction = TouchMainDirection.LeftRight;
                onStartChangeParameter();
            }
        } else if (this._direction == TouchMainDirection.LeftRight) {
            changed = onChangeValue(deltaX);
            if (changed && this._undo != null) {
                UndoManager.getUndoManager().pushUndo(this._undo);
                this._undo = null;
            }
        } else if (this._direction == TouchMainDirection.TopBottom) {
            updateParameterMenu(Math.round(deltaY));
        }
        if (changed) {
            this._oldX = x;
            this._oldY = y;
        }
        return true;
    }

    public void handleTouchCanceled(float x, float y) {
        resetTouch();
    }

    public void handleTouchAbort(boolean pinchBegins) {
        resetTouch();
    }

    private void resetTouch() {
        showParameterView(false);
        if (this._direction == TouchMainDirection.LeftRight) {
            onEndChangeParameter();
        }
        this._direction = TouchMainDirection.None;
        this._oldX = 0.0f;
        this._oldY = 0.0f;
    }

    protected void onStartChangeParameter() {
        MainActivity.getEditingToolbar().setCompareEnabled(false);
    }

    protected void onEndChangeParameter() {
        MainActivity.getEditingToolbar().setCompareEnabled(true);
    }
}
