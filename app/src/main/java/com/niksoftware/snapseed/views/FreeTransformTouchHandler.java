package com.niksoftware.snapseed.views;

import android.view.MotionEvent;
import java.security.InvalidParameterException;

class FreeTransformTouchHandler {
    private static final int INVALID_TOUCH_ID = -1;
    private int activePointerId0 = -1;
    private int activePointerId1 = -1;
    private final TransformListener listener;

    public interface TransformListener {
        void onFinishedTransform();

        void onStartFreeTransform(float f, float f2, float f3, float f4);

        void onStartTranslate(float f, float f2);

        void onUpdateFreeTransform(float f, float f2, float f3, float f4);

        void onUpdateTranslate(float f, float f2);
    }

    public FreeTransformTouchHandler(TransformListener listener) {
        if (listener == null) {
            throw new InvalidParameterException();
        }
        this.listener = listener;
    }

    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction() & 255) {
            case 0:
                if (isHandlingGesture()) {
                    return false;
                }
                this.activePointerId0 = event.getPointerId(0);
                this.listener.onStartTranslate(event.getX(), event.getY());
                return true;
            case 1:
            case 3:
                finishTouchHandling();
                return true;
            case 2:
                if (!isHandlingGesture()) {
                    return false;
                }
                handleMoveAction(event);
                return true;
            case 5:
                if (isHandlingScaleRotate() || event.getPointerCount() <= 1) {
                    return false;
                }
                triggerBeginScale(event);
                return true;
            case 6:
                if (!isHandlingScaleRotate()) {
                    return false;
                }
                int pointerCount = event.getPointerCount();
                if (pointerCount < 3) {
                    this.activePointerId1 = -1;
                    int i = 0;
                    while (i < pointerCount) {
                        if (i != event.getActionIndex()) {
                            this.activePointerId0 = event.getPointerId(i);
                            this.listener.onStartTranslate(event.getX(i), event.getY(i));
                        } else {
                            i++;
                        }
                    }
                } else {
                    int index0 = event.findPointerIndex(this.activePointerId0);
                    int index1 = event.findPointerIndex(this.activePointerId1);
                    if (index0 < 0 || index1 < 0) {
                        triggerBeginScale(event);
                    }
                }
                return true;
            default:
                return false;
        }
    }

    private void handleMoveAction(MotionEvent event) {
        if (this.activePointerId1 < 0) {
            int pointerIndex = event.findPointerIndex(this.activePointerId0);
            if (pointerIndex >= 0) {
                this.listener.onUpdateTranslate(event.getX(pointerIndex), event.getY(pointerIndex));
                return;
            }
            this.activePointerId0 = event.getPointerId(0);
            this.listener.onStartTranslate(event.getX(), event.getY());
            return;
        }
        int index0 = event.findPointerIndex(this.activePointerId0);
        int index1 = event.findPointerIndex(this.activePointerId1);
        if (index0 < 0 || index1 < 0) {
            triggerBeginScale(event);
        } else {
            this.listener.onUpdateFreeTransform(event.getX(index0), event.getY(index0), event.getX(index1), event.getY(index1));
        }
    }

    private void triggerBeginScale(MotionEvent motionEvent) {
        this.activePointerId0 = motionEvent.getPointerId(0);
        this.activePointerId1 = motionEvent.getPointerId(1);
        this.listener.onStartFreeTransform(motionEvent.getX(0), motionEvent.getY(0), motionEvent.getX(1), motionEvent.getY(1));
    }

    private void finishTouchHandling() {
        this.activePointerId0 = -1;
        this.activePointerId1 = -1;
        this.listener.onFinishedTransform();
    }

    private boolean isHandlingGesture() {
        return this.activePointerId0 >= 0;
    }

    private boolean isHandlingScaleRotate() {
        return this.activePointerId0 >= 0 && this.activePointerId1 >= 0;
    }
}
