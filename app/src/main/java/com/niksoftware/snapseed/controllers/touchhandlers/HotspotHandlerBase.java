package com.niksoftware.snapseed.controllers.touchhandlers;

import android.graphics.Point;
import android.graphics.Rect;
import android.os.Handler;
import com.niksoftware.snapseed.MainActivity;
import com.niksoftware.snapseed.core.NotificationCenter;
import com.niksoftware.snapseed.core.NotificationCenterListener;
import com.niksoftware.snapseed.core.NotificationCenterListener.ListenerType;
import com.niksoftware.snapseed.core.UndoManager;
import com.niksoftware.snapseed.core.filterparameters.FilterParameter;
import com.niksoftware.snapseed.util.TrackerData;
import com.niksoftware.snapseed.views.HotspotView;
import com.niksoftware.snapseed.views.WorkingAreaView;

public abstract class HotspotHandlerBase extends TouchHandler {
    protected static final int HOTSPOT_SHORT_TIMEOUT = 50;
    protected static final int HOTSPOT_TIMEOUT = 5000;
    private NotificationCenterListener _didRenderFrame;
    private Handler _handler = MainActivity.getWorkingAreaView().getHandler();
    protected HotspotView _hotspot = null;
    protected boolean _hotspotCreateUndo = false;
    protected int _hotspotHitDistance = MainActivity.getMainActivity().getResources().getDimensionPixelSize(R.dimen.wa_hotspot_hit_distance);
    protected boolean _pinchCreateUndo = false;
    protected float _pinchSize = 0.0f;
    private Runnable _timeoutHotspot = new Runnable() {
        public void run() {
            HotspotHandlerBase.this.showHotspot(false);
        }
    };
    protected Point _touchDownDiff = null;

    protected abstract boolean circleOnWhenTouchUp();

    protected abstract void showCircle(boolean z);

    public HotspotHandlerBase() {
        if ((this instanceof TiltShiftHotspotHandler) && MainActivity.getWorkingAreaView().getActiveFilterType() == 11) {
            NotificationCenter instance = NotificationCenter.getInstance();
            NotificationCenterListener anonymousClass2 = new NotificationCenterListener() {
                public void performAction(Object arg) {
                    MainActivity.getWorkingAreaView().requestLayout();
                }
            };
            this._didRenderFrame = anonymousClass2;
            instance.addListener(anonymousClass2, ListenerType.DidRenderFrame);
        }
    }

    public void cleanup() {
        if (this._didRenderFrame != null) {
            NotificationCenter.getInstance().removeListener(this._didRenderFrame, ListenerType.DidRenderFrame);
        }
        showHotspot(false);
        super.cleanup();
    }

    protected void hideHotspotDelayed() {
        hideHotspotDelayed(HOTSPOT_TIMEOUT);
    }

    protected void hideHotspot() {
        this._timeoutHotspot.run();
    }

    protected void hideHotspotDelayed(int delay) {
        this._handler.removeCallbacks(this._timeoutHotspot);
        this._handler.postDelayed(this._timeoutHotspot, (long) delay);
    }

    protected void clearHotspotTimeout() {
        this._handler.removeCallbacks(this._timeoutHotspot);
    }

    protected static Point getHotspotPositon() {
        double x = FilterParameter.lowBitsAsDouble(MainActivity.getFilterParameter().getParameterValueOld(24));
        double y = FilterParameter.lowBitsAsDouble(MainActivity.getFilterParameter().getParameterValueOld(25));
        Rect rect = MainActivity.getWorkingAreaView().getImageViewScreenRect();
        return new Point((int) (((double) rect.width()) * x), (int) (((double) rect.height()) * y));
    }

    public void layout(boolean changed, int left, int top, int right, int bottom) {
        if (isHotspotVisible()) {
            int parentWidth = right - left;
            int parentHeight = bottom - top;
            int imageWidth = this._hotspot.getMeasuredWidth();
            int imageHeight = this._hotspot.getMeasuredHeight();
            double x = this._hotspot.getCenterX(parentWidth, parentHeight);
            int x0 = (((int) (((double) parentWidth) * x)) + left) - (imageWidth / 2);
            int y0 = (((int) (((double) parentHeight) * this._hotspot.getCenterY(parentWidth, parentHeight))) + top) - (imageHeight / 2);
            this._hotspot.layout(x0, y0, x0 + imageWidth, y0 + imageWidth);
        }
    }

    protected Point getHotspotDiff(int x, int y) {
        Point hotspot = getHotspotPositon();
        if (Math.pow((double) (hotspot.x - x), 2.0d) + Math.pow((double) (hotspot.y - y), 2.0d) > ((double) (this._hotspotHitDistance * this._hotspotHitDistance))) {
            return null;
        }
        hotspot.x -= x;
        hotspot.y -= y;
        return hotspot;
    }

    private void moveHotspot(int newX, int newY) {
        Rect rect = MainActivity.getWorkingAreaView().getImageViewScreenRect();
        newX = Math.min(Math.max(newX, 0), rect.width());
        newY = Math.min(Math.max(newY, 0), rect.height());
        int centerX = FilterParameter.packDouble2LowerInt(((double) newX) / ((double) rect.width()));
        int centerY = FilterParameter.packDouble2LowerInt(((double) newY) / ((double) rect.height()));
        MainActivity.getFilterParameter().setParameterValueOld(24, centerX);
        MainActivity.getFilterParameter().setParameterValueOld(25, centerY);
        NotificationCenter.getInstance().performAction(ListenerType.DidChangeFilterParameterValue, Integer.valueOf(5));
        TrackerData.getInstance().usingParameter(5, MainActivity.getFilterParameter().isDefaultParameter(5));
        int x = (rect.left + newX) - (this._hotspot.getMeasuredWidth() / 2);
        int y = (rect.top + newY) - (this._hotspot.getMeasuredHeight() / 2);
        this._hotspot.layout(x, y, this._hotspot.getMeasuredWidth() + x, this._hotspot.getMeasuredHeight() + y);
    }

    protected void showHotspot(boolean visible) {
        if (visible) {
            clearHotspotTimeout();
        }
        WorkingAreaView workingAreaView = MainActivity.getWorkingAreaView();
        if (visible) {
            if (this._hotspot == null) {
                this._hotspot = new HotspotView();
                workingAreaView.addView(this._hotspot);
            }
            this._hotspot.setVisibility(0);
            updateHotspotPosition();
            return;
        }
        if (this._hotspot != null) {
            workingAreaView.removeView(this._hotspot);
            this._hotspot = null;
        }
        showCircle(false);
    }

    protected void updateHotspotPosition() {
        if (this._hotspot != null) {
            updateHotspotPosition(getHotspotPositon());
        } else {
            showHotspot(true);
        }
    }

    protected void updateHotspotPosition(Point position) {
        if (this._hotspot != null) {
            Rect rect = MainActivity.getWorkingAreaView().getImageViewScreenRect();
            int x = (rect.left + position.x) - (this._hotspot.getMeasuredWidth() / 2);
            int y = (rect.top + position.y) - (this._hotspot.getMeasuredHeight() / 2);
            this._hotspot.layout(x, y, this._hotspot.getMeasuredWidth() + x, this._hotspot.getMeasuredHeight() + y);
            if (this._hotspot.getVisibility() == 0) {
                this._hotspot.setVisibility(8);
                this._hotspot.setVisibility(0);
            }
            if (this instanceof TiltShiftHotspotHandler) {
                MainActivity.getWorkingAreaView().requestLayout();
            }
        }
    }

    protected boolean isHotspotVisible() {
        return this._hotspot != null;
    }

    public boolean handleTouchDown(float x, float y) {
        if (isHotspotVisible()) {
            this._touchDownDiff = getHotspotDiff((int) x, (int) y);
            if (this._touchDownDiff != null) {
                clearHotspotTimeout();
                this._hotspotCreateUndo = true;
                MainActivity.getEditingToolbar().setCompareEnabled(false);
            }
        }
        if (this._touchDownDiff != null) {
            return true;
        }
        return false;
    }

    public void handleTouchUp(float x, float y) {
        this._touchDownDiff = null;
        showHotspot(true);
        hideHotspotDelayed();
        this._hotspotCreateUndo = false;
        this._pinchCreateUndo = false;
        showCircle(circleOnWhenTouchUp());
        MainActivity.getEditingToolbar().setCompareEnabled(true);
    }

    public void handleTouchAbort(boolean pinchBegins) {
        if (pinchBegins) {
            showCircle(circleOnWhenTouchUp());
            MainActivity.getWorkingAreaView().requestRender();
            return;
        }
        hideHotspotDelayed(HOTSPOT_SHORT_TIMEOUT);
    }

    public boolean handleTouchMoved(float x, float y) {
        if (this._touchDownDiff == null || !isHotspotVisible()) {
            return false;
        }
        if (this._hotspotCreateUndo) {
            UndoManager.getUndoManager().createUndo(MainActivity.getFilterParameter(), 5, "");
            this._hotspotCreateUndo = false;
        }
        this._hotspot.setVisibility(4);
        moveHotspot(((int) x) + this._touchDownDiff.x, ((int) y) + this._touchDownDiff.y);
        showCircle(true);
        MainActivity.getWorkingAreaView().requestRender();
        return true;
    }

    public void handleTouchCanceled(float x, float y) {
        showHotspot(false);
        MainActivity.getEditingToolbar().setCompareEnabled(true);
    }
}
