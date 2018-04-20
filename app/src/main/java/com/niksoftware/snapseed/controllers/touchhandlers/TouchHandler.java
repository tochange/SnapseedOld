package com.niksoftware.snapseed.controllers.touchhandlers;

public abstract class TouchHandler {
    private boolean _ignoreEvents = false;

    public void layout(boolean changed, int left, int top, int right, int bottom) {
    }

    public boolean needGestureDetector() {
        return true;
    }

    public void cleanup() {
    }

    public boolean ignoreEvents() {
        return this._ignoreEvents;
    }

    protected void setIgnoreEvents() {
        this._ignoreEvents = true;
    }

    public void resetIgnoreEvents() {
        this._ignoreEvents = false;
    }

    public boolean handleTouchDown(float x, float y) {
        return false;
    }

    public boolean handleTouchMoved(float x, float y) {
        return false;
    }

    public void handleTouchUp(float x, float y) {
    }

    public void handleTouchCanceled(float x, float y) {
    }

    public void handleTouchAbort(boolean pinchBegins) {
    }

    public boolean handlePinchBegin(int x, int y, float size, float arc) {
        return false;
    }

    public boolean handlePinch(int x, int y, float size, float arc) {
        return false;
    }

    public void handlePinchEnd() {
    }

    public void handlePinchAbort() {
    }
}
