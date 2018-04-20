package com.niksoftware.snapseed.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View.MeasureSpec;
import android.view.ViewGroup;
import com.niksoftware.snapseed.core.DeviceDefs;
import com.niksoftware.snapseed.core.NotificationCenter;
import com.niksoftware.snapseed.core.NotificationCenterListener;
import com.niksoftware.snapseed.core.NotificationCenterListener.ListenerType;

public class ScaleParameterDisplay extends ViewGroup {
    NotificationCenterListener _l1;
    NotificationCenterListener _l2;
    NotificationCenterListener _l3;
    NotificationCenterListener _l4;
    private ScaleParameterDisplayView _scaleParameterDisplayView = null;
    private ScaleParameterValueView _scaleParameterValueView = null;
    private int _valueHeight;
    private int _valueWidth;

    public ScaleParameterDisplay(Context context) {
        super(context);
        init();
    }

    public ScaleParameterDisplay(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ScaleParameterDisplay(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    protected void init() {
        setWillNotDraw(true);
        this._scaleParameterDisplayView = new ScaleParameterDisplayView();
        addView(this._scaleParameterDisplayView);
        if (DeviceDefs.isTablet()) {
            this._scaleParameterValueView = new ScaleParameterValueView();
            addView(this._scaleParameterValueView);
        }
        NotificationCenter instance = NotificationCenter.getInstance();
        NotificationCenterListener anonymousClass1 = new NotificationCenterListener() {
            public void performAction(Object arg) {
                if (DeviceDefs.isTablet()) {
                    ScaleParameterDisplay.this._scaleParameterDisplayView.fill_backgrounds();
                }
                ScaleParameterDisplay.this._scaleParameterDisplayView.invalidate();
            }
        };
        this._l1 = anonymousClass1;
        instance.addListener(anonymousClass1, ListenerType.DidActivateFilter);
        instance = NotificationCenter.getInstance();
        anonymousClass1 = new NotificationCenterListener() {
            public void performAction(Object arg) {
                ScaleParameterDisplay.this._scaleParameterDisplayView.invalidate();
                if (DeviceDefs.isTablet()) {
                    ScaleParameterDisplay.this._scaleParameterValueView.invalidate();
                }
            }
        };
        this._l2 = anonymousClass1;
        instance.addListener(anonymousClass1, ListenerType.DidChangeActiveFilterParameter);
        instance = NotificationCenter.getInstance();
        anonymousClass1 = new NotificationCenterListener() {
            public void performAction(Object arg) {
                if (DeviceDefs.isTablet()) {
                    ScaleParameterDisplay.this._scaleParameterValueView.invalidate();
                } else {
                    ScaleParameterDisplay.this._scaleParameterDisplayView.invalidate();
                }
            }
        };
        this._l3 = anonymousClass1;
        instance.addListener(anonymousClass1, ListenerType.DidChangeFilterParameterValue);
        instance = NotificationCenter.getInstance();
        anonymousClass1 = new NotificationCenterListener() {
            public void performAction(Object arg) {
                ScaleParameterDisplay.this._scaleParameterDisplayView.invalidate();
                if (DeviceDefs.isTablet()) {
                    ScaleParameterDisplay.this._scaleParameterValueView.invalidate();
                }
            }
        };
        this._l4 = anonymousClass1;
        instance.addListener(anonymousClass1, ListenerType.UndoRedoPerformed);
    }

    public void cleanup() {
        NotificationCenter center = NotificationCenter.getInstance();
        if (this._l1 != null) {
            center.removeListener(this._l1, ListenerType.DidActivateFilter);
        }
        if (this._l2 != null) {
            center.removeListener(this._l2, ListenerType.DidChangeActiveFilterParameter);
        }
        if (this._l3 != null) {
            center.removeListener(this._l3, ListenerType.DidChangeFilterParameterValue);
        }
        if (this._l4 != null) {
            center.removeListener(this._l4, ListenerType.UndoRedoPerformed);
        }
        this._l4 = null;
        this._l3 = null;
        this._l2 = null;
        this._l1 = null;
    }

    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int displayWidth = this._scaleParameterDisplayView.getMeasuredWidth();
        this._scaleParameterDisplayView.layout(0, 0, displayWidth, this._scaleParameterDisplayView.getMeasuredHeight());
        if (DeviceDefs.isTablet()) {
            int x = (displayWidth - this._valueWidth) / 2;
            int y = Math.round(((float) getHeight()) * 0.44f);
            this._scaleParameterValueView.layout(x, y, this._valueWidth + x, this._valueHeight + y);
        }
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        this._scaleParameterDisplayView.measure(MeasureSpec.makeMeasureSpec(0, 0), MeasureSpec.makeMeasureSpec(0, 0));
        setMeasuredDimension(MeasureSpec.makeMeasureSpec(this._scaleParameterDisplayView.getMeasuredWidth(), 1073741824), MeasureSpec.makeMeasureSpec(this._scaleParameterDisplayView.getMeasuredHeight(), 1073741824));
        if (DeviceDefs.isTablet()) {
            this._scaleParameterValueView.measure(MeasureSpec.makeMeasureSpec(0, 0), MeasureSpec.makeMeasureSpec(0, 0));
            this._valueWidth = this._scaleParameterValueView.getMeasuredWidth();
            this._valueHeight = this._scaleParameterValueView.getMeasuredHeight();
        }
    }
}
