package com.niksoftware.snapseed.controllers.touchhandlers;

import android.util.Log;

public class DumpHandler extends TouchHandler {
    TouchHandler _base;
    lastEvent_t _lastEvent = lastEvent_t.leOther;
    boolean _logBegin = false;
    boolean _logEnd = true;
    String _msg;

    enum lastEvent_t {
        leTouchMoved,
        lePinch,
        leOther
    }

    public DumpHandler(TouchHandler base) {
        this._base = base;
        this._msg = base.getClass().getSimpleName();
    }

    public DumpHandler(String msg, TouchHandler base) {
        this._base = base;
        this._msg = msg;
    }

    public void layout(boolean changed, int left, int top, int right, int bottom) {
        this._base.layout(changed, left, top, right, bottom);
    }

    public boolean needGestureDetector() {
        return this._base.needGestureDetector();
    }

    public void cleanup() {
        this._base.cleanup();
    }

    public boolean handleTouchDown(float x, float y) {
        if (this._logBegin) {
            Log.i(this._msg, String.format("TouchDown %.0f/%.0f", new Object[]{Float.valueOf(x), Float.valueOf(y)}));
        }
        boolean ret = this._base.handleTouchDown(x, y);
        if (this._logEnd) {
            String str = this._msg;
            String str2 = "TouchDown %.0f/%.0f: returns %s";
            Object[] objArr = new Object[3];
            objArr[0] = Float.valueOf(x);
            objArr[1] = Float.valueOf(y);
            objArr[2] = ret ? "true" : "false";
            Log.i(str, String.format(str2, objArr));
        }
        this._lastEvent = lastEvent_t.leOther;
        return ret;
    }

    public boolean handleTouchMoved(float x, float y) {
        if (this._logBegin && this._lastEvent != lastEvent_t.leTouchMoved) {
            Log.i(this._msg, String.format("TouchMoved %.0f/%.0f", new Object[]{Float.valueOf(x), Float.valueOf(y)}));
        }
        boolean ret = this._base.handleTouchMoved(x, y);
        if (this._logEnd && this._lastEvent != lastEvent_t.leTouchMoved) {
            String str = this._msg;
            String str2 = "TouchMoved %.0f/%.0f: returns %s";
            Object[] objArr = new Object[3];
            objArr[0] = Float.valueOf(x);
            objArr[1] = Float.valueOf(y);
            objArr[2] = ret ? "true" : "false";
            Log.i(str, String.format(str2, objArr));
        }
        this._lastEvent = lastEvent_t.leTouchMoved;
        return ret;
    }

    public void handleTouchUp(float x, float y) {
        if (this._logBegin) {
            Log.i(this._msg, String.format("TouchUp %.0f/%.0f", new Object[]{Float.valueOf(x), Float.valueOf(y)}));
        }
        this._base.handleTouchUp(x, y);
        if (this._logEnd) {
            Log.i(this._msg, String.format("TouchUp %.0f/%.0f", new Object[]{Float.valueOf(x), Float.valueOf(y)}));
        }
        this._lastEvent = lastEvent_t.leOther;
    }

    public void handleTouchCanceled(float x, float y) {
        if (this._logBegin) {
            Log.i(this._msg, String.format("TouchCanceled %.0f/%.0f", new Object[]{Float.valueOf(x), Float.valueOf(y)}));
        }
        this._base.handleTouchCanceled(x, y);
        if (this._logEnd) {
            Log.i(this._msg, String.format("TouchCanceled %.0f/%.0f", new Object[]{Float.valueOf(x), Float.valueOf(y)}));
        }
        this._lastEvent = lastEvent_t.leOther;
    }

    public void handleTouchAbort(boolean pinchBegins) {
        if (this._logBegin) {
            String str = this._msg;
            String str2 = "TouchAbort(%s)";
            Object[] objArr = new Object[1];
            objArr[0] = pinchBegins ? "true" : "false";
            Log.i(str, String.format(str2, objArr));
        }
        this._base.handleTouchAbort(pinchBegins);
        if (this._logEnd) {
            str = this._msg;
            str2 = "TouchAbort(%s)";
            objArr = new Object[1];
            objArr[0] = pinchBegins ? "true" : "false";
            Log.i(str, String.format(str2, objArr));
        }
        this._lastEvent = lastEvent_t.leOther;
    }

    public boolean handlePinchBegin(int x, int y, float size, float arc) {
        if (this._logBegin) {
            Log.i(this._msg, String.format("PinchBegin %d/%d", new Object[]{Integer.valueOf(x), Integer.valueOf(y)}));
        }
        boolean ret = this._base.handlePinchBegin(x, y, size, arc);
        if (this._logEnd) {
            String str = this._msg;
            String str2 = "PinchBegin %d/%d: returns %s";
            Object[] objArr = new Object[3];
            objArr[0] = Integer.valueOf(x);
            objArr[1] = Integer.valueOf(y);
            objArr[2] = ret ? "true" : "false";
            Log.i(str, String.format(str2, objArr));
        }
        this._lastEvent = lastEvent_t.leOther;
        return ret;
    }

    public boolean handlePinch(int x, int y, float size, float arc) {
        if (this._logBegin && this._lastEvent != lastEvent_t.lePinch) {
            Log.i(this._msg, String.format("Pinch %d/%d", new Object[]{Integer.valueOf(x), Integer.valueOf(y)}));
        }
        boolean ret = this._base.handlePinch(x, y, size, arc);
        if (this._logEnd && this._lastEvent != lastEvent_t.lePinch) {
            String str = this._msg;
            String str2 = "Pinch %d/%d: returns %s";
            Object[] objArr = new Object[3];
            objArr[0] = Integer.valueOf(x);
            objArr[1] = Integer.valueOf(y);
            objArr[2] = ret ? "true" : "false";
            Log.i(str, String.format(str2, objArr));
        }
        this._lastEvent = lastEvent_t.lePinch;
        return ret;
    }

    public void handlePinchEnd() {
        if (this._logBegin) {
            Log.i(this._msg, "PinchEnd");
        }
        this._base.handlePinchEnd();
        if (this._logEnd) {
            Log.i(this._msg, "PinchEnd");
        }
        this._lastEvent = lastEvent_t.leOther;
    }

    public void handlePinchAbort() {
        if (this._logBegin) {
            Log.i(this._msg, "PinchAbort");
        }
        this._base.handlePinchAbort();
        if (this._logEnd) {
            Log.i(this._msg, "PinchAbort");
        }
        this._lastEvent = lastEvent_t.leOther;
    }
}
