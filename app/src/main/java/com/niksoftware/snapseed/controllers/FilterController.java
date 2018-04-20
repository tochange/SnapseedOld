package com.niksoftware.snapseed.controllers;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.ScaleGestureDetector.OnScaleGestureListener;
import android.view.View;
import android.view.View.OnTouchListener;
import com.niksoftware.snapseed.MainActivity;
import com.niksoftware.snapseed.R;
import com.niksoftware.snapseed.controllers.touchhandlers.CircleHandler;
import com.niksoftware.snapseed.controllers.touchhandlers.ParameterHandler;
import com.niksoftware.snapseed.controllers.touchhandlers.TouchHandler;
import com.niksoftware.snapseed.core.EditSession;
import com.niksoftware.snapseed.core.FilterDefs.FilterParameterType;
import com.niksoftware.snapseed.core.NativeCore;
import com.niksoftware.snapseed.core.NotificationCenter;
import com.niksoftware.snapseed.core.NotificationCenterListener;
import com.niksoftware.snapseed.core.NotificationCenterListener.ListenerType;
import com.niksoftware.snapseed.core.SnapseedAppDelegate;
import com.niksoftware.snapseed.core.UndoManager;
import com.niksoftware.snapseed.core.filterparameters.FilterParameter;
import com.niksoftware.snapseed.core.rendering.TilesProvider;
import com.niksoftware.snapseed.util.TrackerData;
import com.niksoftware.snapseed.views.BaseFilterButton;
import com.niksoftware.snapseed.views.EditingToolBar;
import com.niksoftware.snapseed.views.ImageViewGL;
import com.niksoftware.snapseed.views.ItemSelectorView;
import com.niksoftware.snapseed.views.RootView;
import com.niksoftware.snapseed.views.WorkingAreaView;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public abstract class FilterController {
    private TouchHandler _activeEventHandler = null;
    private DefaultTouchListener _defaultTouchListener = new DefaultTouchListener();
    private EventMode _eventMode = EventMode.emWaitingForDown;
    protected boolean _isApplyingFilter = false;
    private boolean _isBatchParameterChange;
    private List<TouchHandler> _onTouchListenerArray = new ArrayList();
    protected ParameterHandler _paramHandler;
    private ScaleGestureDetectorListener _scaleGestureDetectorListener = null;
    private boolean _startedBatchParameterChange;
    protected NotificationCenterListener _undoRedoListener;
    private EditSession editSession;
    private EditingToolBar editingToolBar;
    private ItemSelectorView itemSelectorView;
    private RootView rootView;
    private WorkingAreaView workingAreaView;

    public class DefaultTouchListener implements OnTouchListener {
        public boolean onTouch(View v, MotionEvent event) {
            int action = event.getAction();
            if (event.getPointerCount() > 1 && action == 5) {
                return false;
            }
            if (event.getPointerId(0) > 0 && action == 2) {
                return false;
            }
            action = event.getActionMasked();
            Rect imageRect = FilterController.this.getWorkingAreaView().getImageViewScreenRect();
            float x = event.getX() - ((float) imageRect.left);
            float y = event.getY() - ((float) imageRect.top);
            switch (action) {
                case 0:
                    return FilterController.this.doHandleTouchDown(x, y);
                case 1:
                    return FilterController.this.doHandleTouchUp(x, y);
                case 2:
                    return FilterController.this.doHandleTouchMoved(x, y);
                case 3:
                    return FilterController.this.doHandleTouchCanceled(x, y);
                default:
                    return false;
            }
        }

        public void onClose() {
            FilterController.this.doHandleTouchAbort();
        }
    }

    private enum EventMode {
        emWaitingForDown,
        emTouch,
        emPinch
    }

    public class ScaleGestureDetectorListener implements OnTouchListener {
        private MotionEvent _event = null;
        private boolean _handled;
        private ScaleGestureDetector _scaleGestureDetector;
        private int firstIdx;
        private int secondIdx;

         /* synthetic */ boolean access$076(ScaleGestureDetectorListener x0, boolean x1) {
            boolean z = (x0._handled || x1);
            x0._handled = z;
            return z;
        }

        private void initPinch() {
            if (this._event.getPointerCount() >= 2) {
                this.firstIdx = 0;
                this.secondIdx = 1;
                float x1 = this._event.getX(this.firstIdx);
                float y1 = this._event.getY(this.firstIdx);
                float x2 = this._event.getX(this.secondIdx);
                float y2 = this._event.getY(this.secondIdx);
                if ((y1 == y2 && x1 > x2) || y1 < y2) {
                    this.firstIdx = 1;
                    this.secondIdx = 0;
                }
            }
        }

        private float calcArc() {
            return this._event.getPointerCount() < 2 ? 0.0f : (float) Math.atan2((double) (this._event.getY(this.secondIdx) - this._event.getY(this.firstIdx)), (double) (this._event.getX(this.secondIdx) - this._event.getX(this.firstIdx)));
        }

        public ScaleGestureDetectorListener() {
            this._scaleGestureDetector = new ScaleGestureDetector(FilterController.this.getContext(), new OnScaleGestureListener(FilterController.this) {
                public void onScaleEnd(ScaleGestureDetector detector) {
                    ScaleGestureDetectorListener.this._handled = true;
                    FilterController.this.doHandlePinchEnd();
                }

                public boolean onScaleBegin(ScaleGestureDetector detector) {
                    ScaleGestureDetectorListener.this.initPinch();
                    Rect imageRect = FilterController.this.getWorkingAreaView().getImageViewScreenRect();
                    int x = (int) (detector.getFocusX() - ((float) imageRect.left));
                    int y = (int) (detector.getFocusY() - ((float) imageRect.top));
                    float arc = ScaleGestureDetectorListener.this.calcArc();
//                    ScaleGestureDetectorListener.access$076(ScaleGestureDetectorListener.this, FilterController.this.doHandlePinchBegin(x, y, detector.getCurrentSpan(), arc));
                    return ScaleGestureDetectorListener.this._handled;
                }

                public boolean onScale(ScaleGestureDetector detector) {
                    Rect imageRect = FilterController.this.getWorkingAreaView().getImageViewScreenRect();
                    int x = (int) (detector.getFocusX() - ((float) imageRect.left));
                    int y = (int) (detector.getFocusY() - ((float) imageRect.top));
                    float arc = ScaleGestureDetectorListener.this.calcArc();
//                    ScaleGestureDetectorListener.access$076(ScaleGestureDetectorListener.this, FilterController.this.doHandlePinch(x, y, detector.getCurrentSpan(), arc));
                    return ScaleGestureDetectorListener.this._handled;
                }
            });
        }

        public boolean onTouch(View v, MotionEvent event) {
            this._handled = false;
            this._event = event;
            this._scaleGestureDetector.onTouchEvent(event);
            return this._handled;
        }
    }

    public abstract int getFilterType();

    public abstract int[] getGlobalAdjustmentParameters();

    public abstract BaseFilterButton getLeftFilterButton();

    public abstract BaseFilterButton getRightFilterButton();

    public abstract boolean initLeftFilterButton(BaseFilterButton baseFilterButton);

    public abstract boolean initRightFilterButton(BaseFilterButton baseFilterButton);

    public void init(ControllerContext context) {
        this.editSession = (EditSession) context.get(ControllerContext.EDIT_SESSION);
        this.rootView = (RootView) context.get(ControllerContext.ROOT_VIEW);
        this.workingAreaView = (WorkingAreaView) context.get(ControllerContext.WORKING_AREA_VIEW);
        this.editingToolBar = (EditingToolBar) context.get(ControllerContext.EDITING_TOOL_BAR);
        this.itemSelectorView = this.rootView.getItemSelectorView();
        if (this.editingToolBar != null) {
            this.editingToolBar.cleanupFilterControls();
        }
        if (getFilterType() != 1) {
            if (!(this.editingToolBar == null || getFilterParameter().getDefaultParameter() == -1 || !showsDisplay())) {
                this.editingToolBar.addGlobalFilterControls();
            }
            NotificationCenter instance = NotificationCenter.getInstance();
            NotificationCenterListener anonymousClass1 = new NotificationCenterListener() {
                public void performAction(Object arg) {
                    FilterController.this.getWorkingAreaView().updateShadowBounds();
                    FilterController.this.undoRedoStateChanged();
                }
            };
            this._undoRedoListener = anonymousClass1;
            instance.addListener(anonymousClass1, ListenerType.UndoRedoPerformed);
            getWorkingAreaView().getShadowLayer().setShadowVisible(true);
        }
    }

    public void cleanup() {
        NotificationCenter.getInstance().removeListener(this._undoRedoListener, ListenerType.UndoRedoPerformed);
        getWorkingAreaView().clearGeometryObjects();
        for (TouchHandler handler : this._onTouchListenerArray) {
            handler.cleanup();
        }
        this._onTouchListenerArray.clear();
    }

    public FilterParameter getFilterParameter() {
        return MainActivity.getFilterParameter();
    }

    protected Context getContext() {
        return this.rootView.getContext();
    }

    protected Resources getResources() {
        return this.rootView.getResources();
    }

    protected EditingToolBar getEditingToolbar() {
        return this.editingToolBar;
    }

    protected ItemSelectorView getItemSelectorView() {
        return this.itemSelectorView;
    }

    protected TilesProvider getTilesProvider() {
        return this.workingAreaView.getTilesProvider();
    }

    protected WorkingAreaView getWorkingAreaView() {
        return this.workingAreaView;
    }

    protected RootView getRootView() {
        return this.rootView;
    }

    private void resetIgnoreEvents() {
        for (TouchHandler handler : this._onTouchListenerArray) {
            handler.resetIgnoreEvents();
        }
    }

    public boolean doHandleTouchDown(float x, float y) {
        this._eventMode = EventMode.emTouch;
        this._activeEventHandler = null;
        resetIgnoreEvents();
        Iterator<TouchHandler> iter;
        for (TouchHandler handler : this._onTouchListenerArray) {
            TouchHandler handler2;
            if (handler2.handleTouchDown(x, y)) {
                this._activeEventHandler = handler2;
                iter = this._onTouchListenerArray.iterator();
                while (true) {
                    handler2 = (TouchHandler) iter.next();
                    if (handler2 == this._activeEventHandler) {
                        break;
                    } else if (!handler2.ignoreEvents()) {
                        handler2.handleTouchAbort(false);
                    }
                }
                return true;
            }
        }
        return true;
    }

    public boolean doHandleTouchMoved(float x, float y) {
        if (this._eventMode != EventMode.emTouch) {
            return false;
        }
        if (this._activeEventHandler != null) {
            return this._activeEventHandler.handleTouchMoved(x, y);
        }
        Iterator<TouchHandler> iter;
        for (TouchHandler handler : this._onTouchListenerArray) {
            TouchHandler handler2;
            if (!handler2.ignoreEvents() && handler2.handleTouchMoved(x, y)) {
                this._activeEventHandler = handler2;
                iter = this._onTouchListenerArray.iterator();
                while (true) {
                    handler2 = (TouchHandler) iter.next();
                    if (handler2 == this._activeEventHandler) {
                        return true;
                    }
                    if (!handler2.ignoreEvents()) {
                        handler2.handleTouchAbort(false);
                    }
                }
            }
        }
        return false;
    }

    public boolean doHandleTouchUp(float x, float y) {
        if (this._eventMode != EventMode.emPinch) {
            this._eventMode = EventMode.emWaitingForDown;
            if (this._activeEventHandler != null) {
                this._activeEventHandler.handleTouchUp(x, y);
                this._activeEventHandler = null;
            } else {
                for (TouchHandler handler : this._onTouchListenerArray) {
                    if (!handler.ignoreEvents()) {
                        handler.handleTouchUp(x, y);
                    }
                }
            }
        }
        return true;
    }

    public boolean doHandleTouchCanceled(float x, float y) {
        this._eventMode = EventMode.emWaitingForDown;
        if (this._activeEventHandler != null) {
            this._activeEventHandler.handleTouchCanceled(x, y);
            this._activeEventHandler = null;
        } else {
            for (TouchHandler handler : this._onTouchListenerArray) {
                if (!handler.ignoreEvents()) {
                    handler.handleTouchCanceled(x, y);
                }
            }
        }
        return true;
    }

    public void doHandleTouchAbort() {
        if (this._activeEventHandler != null) {
            this._activeEventHandler.handleTouchAbort(true);
            this._activeEventHandler = null;
            return;
        }
        for (TouchHandler handler : this._onTouchListenerArray) {
            if (!handler.ignoreEvents()) {
                handler.handleTouchAbort(true);
            }
        }
    }

    protected boolean doHandlePinchBegin(int x, int y, float size, float arc) {
        if (this._eventMode == EventMode.emTouch) {
            doHandleTouchAbort();
        }
        resetIgnoreEvents();
        this._eventMode = EventMode.emPinch;
        Iterator<TouchHandler> iter;
        for (TouchHandler handler : this._onTouchListenerArray) {
            TouchHandler handler2;
            if (handler2.handlePinchBegin(x, y, size, arc)) {
                this._activeEventHandler = handler2;
                iter = this._onTouchListenerArray.iterator();
                while (true) {
                    handler2 = (TouchHandler) iter.next();
                    if (handler2 == this._activeEventHandler) {
                        return true;
                    }
                    if (!handler2.ignoreEvents()) {
                        handler2.handlePinchAbort();
                    }
                }
            }
        }
        return false;
    }

    protected boolean doHandlePinch(int x, int y, float size, float arc) {
        if (this._eventMode != EventMode.emPinch) {
            return false;
        }
        if (this._activeEventHandler != null) {
            return this._activeEventHandler.handlePinch(x, y, size, arc);
        }
        Iterator<TouchHandler> iter;
        for (TouchHandler handler : this._onTouchListenerArray) {
            TouchHandler handler2;
            if (!handler2.ignoreEvents() && handler2.handlePinch(x, y, size, arc)) {
                this._activeEventHandler = handler2;
                iter = this._onTouchListenerArray.iterator();
                while (true) {
                    handler2 = (TouchHandler) iter.next();
                    if (handler2 == this._activeEventHandler) {
                        return true;
                    }
                    if (!handler2.ignoreEvents()) {
                        handler2.handlePinchAbort();
                    }
                }
            }
        }
        return false;
    }

    protected void doHandlePinchEnd() {
        if (this._eventMode == EventMode.emPinch) {
            if (this._activeEventHandler != null) {
                this._activeEventHandler.handlePinchEnd();
                return;
            }
            for (TouchHandler handler : this._onTouchListenerArray) {
                if (!handler.ignoreEvents()) {
                    handler.handlePinchEnd();
                }
            }
        }
    }

    protected void addTouchListener(TouchHandler listener) {
        if (listener.needGestureDetector() && this._scaleGestureDetectorListener == null) {
            this._scaleGestureDetectorListener = new ScaleGestureDetectorListener();
        }
        this._onTouchListenerArray.add(listener);
    }

    protected void addCircleHandler() {
        addTouchListener(new CircleHandler());
    }

    protected void addParameterHandler() {
        ParameterHandler parameterHandler = new ParameterHandler();
        this._paramHandler = parameterHandler;
        addTouchListener(parameterHandler);
    }

    public boolean onTouch(View v, MotionEvent event) {
        boolean done = false;
        if (this._scaleGestureDetectorListener != null) {
            done = this._scaleGestureDetectorListener.onTouch(v, event);
        }
        return done || this._defaultTouchListener.onTouch(v, event);
    }

    public void onClose() {
        getWorkingAreaView().clearGeometryObjects();
        this._defaultTouchListener.onClose();
    }

    public void onApplyFilter() {
        onClose();
        getEditingToolbar().setEnabled(false);
        NotificationCenter notificationCenter = NotificationCenter.getInstance();
        notificationCenter.performAction(ListenerType.WillApplyFilter, null);
        System.runFinalization();
        System.gc();
        applyFilter();
        notificationCenter.performAction(ListenerType.DidApplyFilter, null);
    }

    public void onCancelFilter() {
        if (getFilterType() != 1) {
            onClose();
            NativeCore.getInstance().setCompare(false);
            MainActivity.getMainActivity().activateMainScreen(true);
        }
    }

    protected void applyFilter() {
        if (!this._isApplyingFilter) {
            try {
                ImageViewGL imageView = (ImageViewGL) getWorkingAreaView().getImageView();
                this._isApplyingFilter = true;
                lockCurrentOrientation();
                imageView.requestRenderImage(getTilesProvider(), null, getFilterParameter(), MainActivity.getMainActivity().createOnRenderExportListener(), false);
            } catch (ClassCastException e) {
            }
        }
    }

    public boolean isApplyingFilter() {
        return this._isApplyingFilter;
    }

    public void resetApplyFilter() {
        this._isApplyingFilter = false;
    }

    public void layout(boolean changed, int left, int top, int right, int bottom) {
        for (TouchHandler handler : this._onTouchListenerArray) {
            handler.layout(changed, left, top, right, bottom);
        }
    }

    public boolean showsDisplay() {
        return true;
    }

    public int getScaleBackgroundImageId(int param) {
        if (param == 0 && getFilterType() == 11) {
            return R.drawable.gfx_ct_display_100_100;
        }
        switch (param) {
//            case 3:
//                return R.drawable.gfx_ct_display_colorstyle;
//            case 38:
//                return R.drawable.gfx_ct_display_angle;
//            case 42:
//                return R.drawable.gfx_ct_display_empty;
//            case 1000:
//                return R.drawable.gfx_ct_display_empty;
            default:
                if (getFilterParameter().getMinValue(param) == 0) {
//                    return R.drawable.gfx_ct_display_0_100;
                }
//                return R.drawable.gfx_ct_display_100_100;
            return R.drawable.gfx_ct_display_empty;

        }
    }

    public String getParameterTitle(int parameter) {
        return FilterParameterType.getParameterTitle(getContext(), parameter);
    }

    public void beginChangeParameter() {
        this._isBatchParameterChange = true;
        this._startedBatchParameterChange = false;
    }

    public void endChangeParameter() {
        this._isBatchParameterChange = false;
    }

    public boolean changeParameter(FilterParameter filter, int parameterKey, int parameterValue, boolean forceApplyValue, Runnable prerenderAction) {
        if (!forceApplyValue && filter.getParameterValueOld(parameterKey) == parameterValue) {
            return false;
        }
        if (!(this._isBatchParameterChange && this._startedBatchParameterChange)) {
            UndoManager.getUndoManager().createUndo(filter, parameterKey);
            this._startedBatchParameterChange = true;
        }
        boolean succeeded = filter.setParameterValueOld(parameterKey, parameterValue);
        if (!forceApplyValue && !succeeded) {
            return succeeded;
        }
        if (prerenderAction != null) {
            prerenderAction.run();
        }
        getWorkingAreaView().requestRender();
        NotificationCenter.getInstance().performAction(ListenerType.DidChangeFilterParameterValue, Integer.valueOf(parameterKey));
        return succeeded;
    }

    public boolean changeParameter(FilterParameter filter, int parameterKey, int parameterValue) {
        return changeParameter(filter, parameterKey, parameterValue, false, null);
    }

    public void randomizeParameter(int parameterType) {
        FilterParameter filter = getFilterParameter();
        int action = 1000;
        int undoMessageId = 0;
        switch (parameterType) {
            case 3:
                action = 2;
                undoMessageId = R.string.randomize_style;
                break;
            case 101:
                action = 1;
                undoMessageId = R.string.randomize_texture;
                break;
            case 224:
                action = 1;
                undoMessageId = R.string.frame;
                break;
        }
        if (action != 1000) {
            UndoManager.getUndoManager().createUndo(filter, parameterType, getContext().getString(undoMessageId));
            if (NativeCore.contextAction(filter, action) == 1) {
                NotificationCenter.getInstance().performAction(ListenerType.DidChangeFilterParameterValue, null);
                TrackerData.getInstance().randomizeAction(filter.getFilterType(), action);
                getWorkingAreaView().requestRender();
            }
        }
    }

    public void undoRedoStateChanged() {
    }

    public void randomize() {
        randomize(true);
    }

    public void randomize(boolean pushUndo) {
        if (getFilterType() != 1) {
            FilterParameter filter = getFilterParameter();
            if (pushUndo) {
                UndoManager.getUndoManager().createUndo(filter, 1000, getContext().getString(R.string.shuffle));
                TrackerData.getInstance().randomizeAction(filter.getFilterType(), 0);
            }
            if (NativeCore.contextAction(filter, 0) == 1) {
                getWorkingAreaView().requestRender();
            }
        }
    }

    public boolean showsParameterView() {
        return false;
    }

    public void setPreviewImage(List<Bitmap> list, int parameter) {
    }

    public List<Bitmap> previewImages(int filterParameterType) {
        return null;
    }

    public boolean useActionView() {
        return true;
    }

    public boolean isHidingButtonTitles() {
        return false;
    }

    public String getButtonTitle(int resourceId) {
        return isHidingButtonTitles() ? "" : getContext().getString(resourceId);
    }

    public int[] getUPointParametersType() {
        return null;
    }

    public int getHelpResourceId() {
        return R.xml.overlay_two_gestures;
    }

    public void onPause() {
        if (this._paramHandler != null) {
            this._paramHandler.handleTouchAbort(false);
        }
    }

    public void onResume() {
    }

    public void onOnScreenFilterCreated(int filterType) {
    }

    public void updateEditingToolbarState() {
    }

    protected void lockCurrentOrientation() {
        MainActivity.getMainActivity().lockCurrentOrientation();
    }

    protected void postExportResult(final Bitmap filteredImage, final boolean endProcess) {
        getWorkingAreaView().post(new Runnable() {
            public void run() {
                MainActivity.getMainActivity().onExportFilteredImage(filteredImage);
                if (endProcess) {
                    SnapseedAppDelegate.getInstance().progressEnd();
                }
                FilterController.this._isApplyingFilter = false;
            }
        });
    }
}
