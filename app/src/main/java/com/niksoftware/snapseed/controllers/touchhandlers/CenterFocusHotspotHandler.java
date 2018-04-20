package com.niksoftware.snapseed.controllers.touchhandlers;

import com.niksoftware.snapseed.core.NotificationCenter;
import com.niksoftware.snapseed.core.NotificationCenterListener;
import com.niksoftware.snapseed.core.NotificationCenterListener.ListenerType;
import com.niksoftware.snapseed.core.UndoObject;

public class CenterFocusHotspotHandler extends HotspotHandler {
    private NotificationCenterListener _didChangeFilterParameterValue;
    private NotificationCenterListener _undoRedoPerformed;

    public CenterFocusHotspotHandler() {
        NotificationCenter instance = NotificationCenter.getInstance();
        NotificationCenterListener anonymousClass1 = new NotificationCenterListener() {
            public void performAction(Object arg) {
                if (arg != null && ((Integer) arg).intValue() == 4) {
                    CenterFocusHotspotHandler.this.showCircle(true);
                    CenterFocusHotspotHandler.this.showHotspot(true);
                    CenterFocusHotspotHandler.this.hideHotspotDelayed();
                }
            }
        };
        this._didChangeFilterParameterValue = anonymousClass1;
        instance.addListener(anonymousClass1, ListenerType.DidChangeFilterParameterValue);
        instance = NotificationCenter.getInstance();
        anonymousClass1 = new NotificationCenterListener() {
            public void performAction(Object arg) {
                if (arg != null && ((UndoObject) arg).getChangedParameter() == 5) {
                    CenterFocusHotspotHandler.this.updateHotspotPosition();
                    CenterFocusHotspotHandler.this.hideHotspotDelayed();
                }
            }
        };
        this._undoRedoPerformed = anonymousClass1;
        instance.addListener(anonymousClass1, ListenerType.UndoRedoPerformed);
    }

    public void cleanup() {
        NotificationCenter center = NotificationCenter.getInstance();
        center.removeListener(this._didChangeFilterParameterValue, ListenerType.DidChangeFilterParameterValue);
        center.removeListener(this._undoRedoPerformed, ListenerType.UndoRedoPerformed);
        super.cleanup();
    }

    protected float calcRadius(int radius) {
        return (((float) radius) / 100.0f) + 0.3f;
    }

    protected int setParameterValue(float startPinchSize, float oldParamValue, float currentPinchSize) {
        return (int) CircleHandler.calcUpdatedValue(0.0f, 100.0f, startPinchSize, currentPinchSize, oldParamValue, CircleHandler.PINCH_FEEDBACK_RATIO);
    }

    protected boolean circleOnWhenTouchUp() {
        return false;
    }
}
