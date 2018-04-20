package com.niksoftware.snapseed.views;

import android.graphics.PointF;
import android.graphics.RectF;
import android.view.MotionEvent;
import com.niksoftware.snapseed.util.Geometry;
import java.security.InvalidParameterException;

class CropAreaTransformTouchHandler {
    private static final float GRIP_HIT_RADIUS2 = 1600.0f;
    private Grip activeGrip = Grip.NONE;
    private int activePointerId = -1;
    private final RectF cropAreaRect = new RectF();
    private final TransformListener listener;
    private final PointF startTouch = new PointF();

    private enum Grip {
        NONE,
        TOP_LEFT,
        TOP_RIGHT,
        BOTTOM_LEFT,
        BOTTOM_RIGHT
    }

    public interface TransformListener {
        void onFinishCropAreaTransform();

        void onStartCropAreaTransform();

        void onUpdateCropAreaTransform(float f, float f2, float f3, float f4);
    }

    public CropAreaTransformTouchHandler(TransformListener listener) {
        if (listener == null) {
            throw new InvalidParameterException();
        }
        this.listener = listener;
    }

    public void setCropAreaRect(RectF cropAreaRect) {
        this.cropAreaRect.set(cropAreaRect);
    }

    public boolean onTouchEvent(MotionEvent event) {
        boolean handledTouch = isHandlingGesture();
        switch (event.getAction() & 255) {
            case 0:
                if (isHandlingGesture()) {
                    return handledTouch;
                }
                Grip grip = detectGripHit(event.getX(), event.getY());
                if (grip == Grip.NONE) {
                    return handledTouch;
                }
                startTouchHandling(event, grip);
                return true;
            case 1:
            case 3:
                if (!isHandlingGesture()) {
                    return handledTouch;
                }
                finishTouchHandling();
                return true;
            case 2:
                if (!isHandlingGesture()) {
                    return handledTouch;
                }
                int pointerIndex = event.findPointerIndex(this.activePointerId);
                if (pointerIndex < 0) {
                    finishTouchHandling();
                } else {
                    updateCropArea(event.getX(pointerIndex), event.getY(pointerIndex));
                }
                return true;
            default:
                return handledTouch;
        }
    }

    public boolean isHandlingGesture() {
        return this.activePointerId >= 0;
    }

    private Grip detectGripHit(float x, float y) {
        if (Geometry.distance2(this.cropAreaRect.left, this.cropAreaRect.top, x, y) <= GRIP_HIT_RADIUS2) {
            return Grip.TOP_LEFT;
        }
        if (Geometry.distance2(this.cropAreaRect.right, this.cropAreaRect.top, x, y) <= GRIP_HIT_RADIUS2) {
            return Grip.TOP_RIGHT;
        }
        if (Geometry.distance2(this.cropAreaRect.left, this.cropAreaRect.bottom, x, y) <= GRIP_HIT_RADIUS2) {
            return Grip.BOTTOM_LEFT;
        }
        if (Geometry.distance2(this.cropAreaRect.right, this.cropAreaRect.bottom, x, y) <= GRIP_HIT_RADIUS2) {
            return Grip.BOTTOM_RIGHT;
        }
        return Grip.NONE;
    }

    private void startTouchHandling(MotionEvent event, Grip grip) {
        this.activePointerId = event.getPointerId(0);
        this.activeGrip = grip;
        this.startTouch.x = event.getX();
        this.startTouch.y = event.getY();
        this.listener.onStartCropAreaTransform();
    }

    private void finishTouchHandling() {
        this.activePointerId = -1;
        this.activeGrip = Grip.NONE;
        this.listener.onFinishCropAreaTransform();
    }

    private void updateCropArea(float x, float y) {
        float cx = this.cropAreaRect.centerX();
        float cy = this.cropAreaRect.centerY();
        float dx = Math.abs(x - cx);
        float dy = Math.abs(y - cy);
        this.listener.onUpdateCropAreaTransform(cx - dx, cy - dy, cx + dx, cy + dy);
    }
}
