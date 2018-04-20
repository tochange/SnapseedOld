package com.niksoftware.snapseed.controllers;

import android.content.Context;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Handler;
import android.os.SystemClock;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.RelativeLayout.LayoutParams;

import com.niksoftware.snapseed.R;
import com.niksoftware.snapseed.controllers.touchhandlers.TouchHandler;
import com.niksoftware.snapseed.controllers.touchhandlers.UPointCircleHandler;
import com.niksoftware.snapseed.controllers.touchhandlers.UPointParameterHandler;
import com.niksoftware.snapseed.core.NativeCore;
import com.niksoftware.snapseed.core.NotificationCenter;
import com.niksoftware.snapseed.core.NotificationCenterListener;
import com.niksoftware.snapseed.core.NotificationCenterListener.ListenerType;
import com.niksoftware.snapseed.core.UndoManager;
import com.niksoftware.snapseed.core.filterparameters.FilterParameter;
import com.niksoftware.snapseed.core.filterparameters.UPointFilterParameter;
import com.niksoftware.snapseed.core.filterparameters.UPointParameter;
import com.niksoftware.snapseed.util.TrackerData;
import com.niksoftware.snapseed.views.BaseFilterButton;
import com.niksoftware.snapseed.views.LoupeView;
import com.niksoftware.snapseed.views.PopoverWindow;
import com.niksoftware.snapseed.views.PopoverWindow.OnActionItemClickListener;
import com.niksoftware.snapseed.views.UPointView;
import com.niksoftware.snapseed.views.WorkingAreaView;
import java.util.ArrayList;
import java.util.Iterator;

public class UPointController extends EmptyFilterController {
    static final /* synthetic */ boolean $assertionsDisabled;
    private static final boolean HIDE_CP_WHILE_DRAGGING = true;
    private static final int MAGNIFIER_OFFSET = 50;
    private static final int MAX_UPOINTS = 8;
    private static final String UPOINT_ADD_ACTION = "add upoint";
    private static final String UPOINT_MENU_ACTION = "upoint_menu";
    private static final String UPOINT_MENU_COPY = "copy";
    private static final String UPOINT_MENU_CUT = "cut";
    private static final String UPOINT_MENU_DELETE = "delete";
    private static final String UPOINT_MENU_PASTE = "paste";
    private static final String UPOINT_MENU_REPLACE = "replace";
    private static final String UPOINT_MENU_RESET = "reset";
    private static final int[] upoint_param_type = new int[]{0, 1, 2};
    private BaseFilterButton _addUpointFilterButton;
    private NotificationCenterListener _didChangeCompareImageMode;
    private BaseFilterButton _hideUpointsButton;
    private boolean _isAddUPointMode;
    private boolean _isCompareMode;
    private boolean _isHideUPointsMode;
    private int _lX;
    private int _lY;
    private NotificationCenterListener _listener;
    private NotificationCenterListener _listener2;
    private LoupeView _loupe;
    private Rect _loupeRect = new Rect();
    private MakeUPointsVisible _makeUPointsVisible = null;
    private boolean _menuIsVisible = $assertionsDisabled;
    private UPointParameter _movingUPoint = null;
    private OpenLoupeRunnable _openLoupeRunnable = new OpenLoupeRunnable();
    private UPointCircleHandler _uPointCircleHandler;
    private UPointParameterHandler _uPointParameterHandler;
    private ArrayList<UPointView> _upoints = new ArrayList();
    private int[] clipboard = null;
    PopoverWindow menu;

    class MakeUPointsVisible implements Runnable {
        MakeUPointsVisible() {
        }

        public void run() {
            if (!UPointController.this._isCompareMode && !UPointController.this._isHideUPointsMode) {
                Iterator i$ = UPointController.this._upoints.iterator();
                while (i$.hasNext()) {
                    ((UPointView) i$.next()).setVisibility(0);
                }
                UPointController.this._makeUPointsVisible = null;
            }
        }
    }

    class OpenLoupeRunnable implements Runnable {
        private boolean _isScheduled = UPointController.$assertionsDisabled;
        private Point _openPosition = new Point();

        OpenLoupeRunnable() {
        }

        public void setLoupeOpenPosition(int loupeX, int loupeY) {
            this._openPosition.x = loupeX;
            this._openPosition.y = loupeY;
        }

        public void setScheduled(boolean scheduled) {
            this._isScheduled = scheduled;
        }

        public boolean isScheduled() {
            return this._isScheduled;
        }

        public void run() {
            this._isScheduled = UPointController.$assertionsDisabled;
            if (UPointController.this._loupe == null) {
                UPointController.this._loupe = new LoupeView(UPointController.this.getContext(), UPointController.HIDE_CP_WHILE_DRAGGING, UPointController.$assertionsDisabled);
                UPointController.this.getWorkingAreaView().addView(UPointController.this._loupe);
                UPointController.this.setMagnifierPosition(this._openPosition.x, this._openPosition.y);
            }
        }
    }

    static {
        boolean z;
        if (UPointController.class.desiredAssertionStatus()) {
            z = $assertionsDisabled;
        } else {
            z = HIDE_CP_WHILE_DRAGGING;
        }
        $assertionsDisabled = z;
    }

    public void init(ControllerContext context) {
        super.init(context);
        NotificationCenter notificationCenter = NotificationCenter.getInstance();
        NotificationCenterListener anonymousClass1 = new NotificationCenterListener() {
            public void performAction(Object arg) {
                UPointController.this._isCompareMode = ((Boolean) arg).booleanValue();
                int visibility = (UPointController.this._isCompareMode || UPointController.this._isHideUPointsMode) ? 8 : 0;
                Iterator i$ = UPointController.this._upoints.iterator();
                while (i$.hasNext()) {
                    ((UPointView) i$.next()).setVisibility(visibility);
                }
                if (UPointController.this._isCompareMode) {
                    UPointController.this._uPointCircleHandler.setOff();
                }
            }
        };
        this._didChangeCompareImageMode = anonymousClass1;
        notificationCenter.addListener(anonymousClass1, ListenerType.DidChangeCompareImageMode);
        anonymousClass1 = new NotificationCenterListener() {
            public void performAction(Object arg) {
                UPointView upointView = UPointController.this.getUPointView(UPointController.this.getUPointFilterParameter().getActiveUPoint());
                if (upointView != null) {
                    upointView.updateTitle();
                }
            }
        };
        this._listener = anonymousClass1;
        notificationCenter.addListener(anonymousClass1, ListenerType.DidChangeActiveFilterParameter);
        anonymousClass1 = new NotificationCenterListener() {
            public void performAction(Object arg) {
                UPointView upointView = UPointController.this.getUPointView(UPointController.this.getUPointFilterParameter().getActiveUPoint());
                if (upointView != null) {
                    upointView.invalidate();
                }
            }
        };
        this._listener2 = anonymousClass1;
        notificationCenter.addListener(anonymousClass1, ListenerType.DidChangeFilterParameterValue);
        TouchHandler uPointCircleHandler = new UPointCircleHandler();
        this._uPointCircleHandler = uPointCircleHandler;
        addTouchListener(uPointCircleHandler);
        addTouchListener(new TouchHandler() {
            private long _activationTime;
            private boolean _createUndo = UPointController.$assertionsDisabled;
            private int _diffX;
            private int _diffY;
            private boolean _firstMove = UPointController.$assertionsDisabled;
            private long _time;

            public boolean handleTouchDown(float x, float y) {
                UPointController.this.showMenu(null);
                if (UPointController.this.isAddUPointMode()) {
                    UPointController.this.setMagnifierOn((int) x, (int) y);
                    UPointParameter activeUPoint = UPointController.this.getUPointFilterParameter().findUPoint((int) x, (int) y);
                    if (activeUPoint == null) {
                        return UPointController.HIDE_CP_WHILE_DRAGGING;
                    }
                    UPointController.this.setActiveUPoint(activeUPoint);
                    return UPointController.HIDE_CP_WHILE_DRAGGING;
                }
                this._firstMove = UPointController.HIDE_CP_WHILE_DRAGGING;
                if (activateUPoint(x, y, UPointController.HIDE_CP_WHILE_DRAGGING)) {
                    UPointController.this.getEditingToolbar().setCompareEnabled(UPointController.$assertionsDisabled);
                    return UPointController.HIDE_CP_WHILE_DRAGGING;
                }
                UPointController.this._movingUPoint = null;
                return UPointController.$assertionsDisabled;
            }

            public void handleTouchUp(float x, float y) {
                if (UPointController.this.isAddUPointMode()) {
                    UPointController.this.createUndo(UPointController.this.getContext().getString(R.string.undo_upoint_add));
                    UPointController.this.addUPoint((int) x, (int) y);
                    TrackerData.getInstance().sendEvent(TrackerData.UI_ACTION, UPointController.UPOINT_ADD_ACTION, "", (long) UPointController.this.getUPointFilterParameter().getUPointCount());
                    UPointController.this._uPointCircleHandler.setOn();
                    UPointController.this._addUpointFilterButton.setSelected(UPointController.$assertionsDisabled);
                    UPointController.this._isAddUPointMode = UPointController.$assertionsDisabled;
                    NotificationCenter.getInstance().performAction(ListenerType.DidChangeActiveFilterParameter, null);
                } else if (UPointController.this._movingUPoint == null) {
                    UPointController.this.setActiveUPoint(null);
                    if (UPointController.this.clipboard != null) {
                        UPointController.this.showMenu(new Point((int) x, (int) y));
                    }
                } else if (SystemClock.elapsedRealtime() - this._time < 200) {
                    UPointController.this.showMenu(new Point((int) x, (int) y));
                }
                handleTouchFinish(UPointController.$assertionsDisabled);
            }

            public void handleTouchAbort(boolean pinchBegins) {
                handleTouchFinish(UPointController.HIDE_CP_WHILE_DRAGGING);
            }

            public void handleTouchCanceled(float x, float y) {
                handleTouchFinish(UPointController.$assertionsDisabled);
            }

            public void handleTouchFinish(boolean isAborted) {
                UPointView movingUPointView = UPointController.this.getUPointView(UPointController.this._movingUPoint);
                if (movingUPointView != null) {
                    Rect imageViewScreenRect = UPointController.this.getWorkingAreaView().getImageViewScreenRect();
                    UPointController.this.layoutUPoint(movingUPointView, imageViewScreenRect.left, imageViewScreenRect.top, imageViewScreenRect.right, imageViewScreenRect.bottom);
                    movingUPointView.setVisibility(0);
                }
                UPointController.this.setMagnifierOff();
                UPointController.this._movingUPoint = null;
                this._firstMove = UPointController.$assertionsDisabled;
                if (!isAborted) {
                    UPointController.this.getEditingToolbar().setCompareEnabled(UPointController.HIDE_CP_WHILE_DRAGGING);
                }
            }

            private boolean activateUPoint(float x, float y, boolean down) {
                UPointFilterParameter filter = UPointController.this.getUPointFilterParameter();
                UPointParameter moveUPoint = filter.findUPoint((int) x, (int) y);
                if (moveUPoint == null) {
                    return UPointController.$assertionsDisabled;
                }
                this._createUndo = down;
                this._activationTime = SystemClock.elapsedRealtime();
                this._time = filter.getActiveUPoint() == moveUPoint ? this._activationTime : 0;
                UPointController.this.setActiveUPoint(moveUPoint);
                this._diffX = moveUPoint.getViewX() - ((int) x);
                this._diffY = moveUPoint.getViewY() - ((int) y);
                UPointController.this.setMagnifierOn((int) x, (int) y);
                return UPointController.HIDE_CP_WHILE_DRAGGING;
            }

            public boolean handleTouchMoved(float x, float y) {
                UPointController.this._uPointCircleHandler.redraw();
                if (UPointController.this.isAddUPointMode()) {
                    UPointController.this.moveMagnifier((int) x, (int) y);
                    return UPointController.$assertionsDisabled;
                } else if (UPointController.this._movingUPoint == null) {
                    return UPointController.$assertionsDisabled;
                } else {
                    if (SystemClock.elapsedRealtime() - this._activationTime > 100) {
                        if (this._createUndo) {
                            UndoManager.getUndoManager().createUndo(UPointController.this.getFilterParameter(), 5, UPointController.this.getContext().getString(R.string.undo_upoint_move));
                            this._createUndo = UPointController.$assertionsDisabled;
                        }
                        if (this._firstMove) {
                            UPointController.this.getUPointView(UPointController.this._movingUPoint).setVisibility(4);
                            this._firstMove = UPointController.$assertionsDisabled;
                        }
                        UPointController.this._movingUPoint.setViewXY(((int) x) + this._diffX, ((int) y) + this._diffY);
                        UPointController.this.moveMagnifier(((int) x) + this._diffX, ((int) y) + this._diffY);
                        TrackerData.getInstance().usingParameter(5, UPointController.$assertionsDisabled);
                        UPointController.this.getWorkingAreaView().requestRender();
                    }
                    return UPointController.HIDE_CP_WHILE_DRAGGING;
                }
            }
        });
        this._uPointParameterHandler = new UPointParameterHandler(getUPointParametersType());
        addTouchListener(this._uPointParameterHandler);
    }

    private UPointFilterParameter getUPointFilterParameter() {
        FilterParameter filter = getFilterParameter();
        if ($assertionsDisabled || (filter instanceof UPointFilterParameter)) {
            return (UPointFilterParameter) filter;
        }
        throw new AssertionError("Invalid filter parameter");
    }

    private void createUndo(String undoDescription) {
        UndoManager.getUndoManager().createUndo(getFilterParameter(), 1000, undoDescription);
    }

    public void cleanup() {
        WorkingAreaView workingAreaView = getWorkingAreaView();
        Iterator i$ = this._upoints.iterator();
        while (i$.hasNext()) {
            workingAreaView.removeView((UPointView) i$.next());
        }
        this._upoints.clear();
        NotificationCenter notificationCenter = NotificationCenter.getInstance();
        notificationCenter.removeListener(this._didChangeCompareImageMode, ListenerType.DidChangeCompareImageMode);
        notificationCenter.removeListener(this._listener, ListenerType.DidChangeActiveFilterParameter);
        notificationCenter.removeListener(this._listener2, ListenerType.DidChangeFilterParameterValue);
        this._uPointParameterHandler.cleanup();
        super.cleanup();
    }

    public void undoRedoStateChanged() {
        super.undoRedoStateChanged();
        syncUPoints();
        UPointView upointView = getUPointView(getUPointFilterParameter().getActiveUPoint());
        if (upointView != null) {
            upointView.bringToFront();
        }
        this._uPointCircleHandler.setOff();
    }

    private void setActiveUPoint(UPointParameter upoint) {
        UPointFilterParameter filter = getUPointFilterParameter();
        if (filter.getUPointCount() != 0) {
            UPointView upointView = getUPointView(filter.getActiveUPoint());
            if (upointView != null) {
                upointView.setActive($assertionsDisabled);
            }
            filter.activateUPoint(upoint);
            if (upoint != null) {
                getUPointView(upoint).bringToFront();
                upointView = getUPointView(upoint);
                if (upointView != null) {
                    upointView.setActive(HIDE_CP_WHILE_DRAGGING);
                }
                this._movingUPoint = upoint;
            }
            this._uPointCircleHandler.setOn();
            NotificationCenter.getInstance().performAction(ListenerType.DidChangeActiveFilterParameter, Integer.valueOf(-1));
        }
    }

    private boolean isAddUPointMode() {
        return this._isAddUPointMode;
    }

    private UPointView getActiveUPointView() {
        return getUPointView(this._movingUPoint);
    }

    private UPointView getUPointView(UPointParameter upointParameter) {
        Iterator i$ = this._upoints.iterator();
        while (i$.hasNext()) {
            UPointView upointView = (UPointView) i$.next();
            if (upointView.getParameter() == upointParameter) {
                return upointView;
            }
        }
        return null;
    }

    private void setMagnifierOn(int x, int y) {
        getWorkingAreaView().removeCallbacks(this._openLoupeRunnable);
        this._openLoupeRunnable.setScheduled(HIDE_CP_WHILE_DRAGGING);
        this._openLoupeRunnable.setLoupeOpenPosition(x, y);
        getWorkingAreaView().postDelayed(this._openLoupeRunnable, 333);
    }

    private void setMagnifierOff() {
        getWorkingAreaView().removeCallbacks(this._openLoupeRunnable);
        this._openLoupeRunnable.setScheduled($assertionsDisabled);
        if (this._loupe != null) {
            getWorkingAreaView().removeView(this._loupe);
            this._loupe = null;
        }
    }

    private void moveMagnifier(int x, int y) {
        if (this._loupe != null) {
            setMagnifierPosition(x, y);
        } else if (this._openLoupeRunnable.isScheduled()) {
            this._openLoupeRunnable.setLoupeOpenPosition(x, y);
        }
    }

    private void toWorkingAreaView(Rect r) {
        Rect imageRect = getWorkingAreaView().getImageViewScreenRect();
        r.offset(imageRect.left, imageRect.top);
    }

    private void setMagnifierPosition(int mouseX, int mouseY) {
        WorkingAreaView workingAreaView = getWorkingAreaView();
        Rect imageRect = workingAreaView.getImageViewScreenRect();
        mouseX = Math.min(Math.max(0, mouseX), imageRect.width() - 1);
        mouseY = Math.min(Math.max(0, mouseY), imageRect.height() - 1);
        Rect aboveMouse = new Rect(mouseX - (this._loupe.getLoupeSize() / 2), (mouseY - 50) - this._loupe.getLoupeSize(), ((this._loupe.getLoupeSize() + 1) / 2) + mouseX, mouseY - 50);
        Rect belowMouse = new Rect(mouseX - (this._loupe.getLoupeSize() / 2), mouseY + MAGNIFIER_OFFSET, ((this._loupe.getLoupeSize() + 1) / 2) + mouseX, (mouseY + MAGNIFIER_OFFSET) + this._loupe.getLoupeSize());
        toWorkingAreaView(aboveMouse);
        toWorkingAreaView(belowMouse);
        if (aboveMouse.top > (-this._loupe.getLoupeSize()) / 2) {
            this._loupeRect.set(aboveMouse.left, aboveMouse.top, aboveMouse.right, aboveMouse.bottom);
        } else {
            this._loupeRect.set(belowMouse.left, belowMouse.top, belowMouse.right, belowMouse.bottom);
        }
        this._lX = (workingAreaView.getImageWidth() * mouseX) / workingAreaView.getImageView().getWidth();
        this._lY = (workingAreaView.getImageHeight() * mouseY) / workingAreaView.getImageView().getHeight();
        getRootView().forceLayoutForFilterGUI = HIDE_CP_WHILE_DRAGGING;
        workingAreaView.requestLayout();
    }

    private void syncUPoints() {
        UPointFilterParameter filter = getUPointFilterParameter();
        WorkingAreaView workingAreaView = getWorkingAreaView();
        Context context = getContext();
        int upointCount = this._upoints.size();
        while (filter.getUPointCount() > upointCount) {
            UPointView upointView = new UPointView(context, null);
            this._upoints.add(upointView);
            workingAreaView.addView(upointView);
            upointView.bringToFront();
            upointCount++;
        }
        while (filter.getUPointCount() < upointCount) {
            UPointView lastUPoint = (UPointView) this._upoints.get(upointCount - 1);
            upointCount--;
            this._upoints.remove(upointCount);
            workingAreaView.removeView(lastUPoint);
        }
        Rect imageViewRect = getWorkingAreaView().getImageViewScreenRect();
        UPointParameter activeParameter = filter.getActiveUPoint();
        for (int i = 0; i < upointCount; i++) {
            boolean z;
            upointView = (UPointView) this._upoints.get(i);
            UPointParameter oldParameter = upointView.getParameter();
            UPointParameter newParameter = filter.getUPoint(i);
            if (oldParameter != newParameter) {
                upointView.setParameter(newParameter);
                layoutUPoint(upointView, imageViewRect.left, imageViewRect.top, imageViewRect.right, imageViewRect.bottom);
            }
            if (newParameter == activeParameter) {
                z = HIDE_CP_WHILE_DRAGGING;
            } else {
                z = $assertionsDisabled;
            }
            upointView.setActive(z);
        }
        if (upointCount >= 8) {
            this._addUpointFilterButton.setSelected($assertionsDisabled);
            this._addUpointFilterButton.setEnabled($assertionsDisabled);
        } else {
            this._addUpointFilterButton.setEnabled(HIDE_CP_WHILE_DRAGGING);
        }
        this._isAddUPointMode = this._addUpointFilterButton.isSelected();
        workingAreaView.getActionView().bringToFront();
    }

    private UPointParameter addUPoint(int x, int y) {
        UPointParameter newUpoint = getUPointFilterParameter().addUPoint(x, y);
        syncUPoints();
        setActiveUPoint(newUpoint);
        getWorkingAreaView().requestRender();
        return newUpoint;
    }

    private void delUPoint(UPointParameter upoint) {
        getUPointFilterParameter().deleteUPoint(upoint);
        syncUPoints();
        getWorkingAreaView().requestRender();
    }

    public int getFilterType() {
        return 3;
    }

    public int[] getGlobalAdjustmentParameters() {
        return new int[0];
    }

    public int[] getUPointParametersType() {
        return upoint_param_type;
    }

    public int getScaleBackgroundImageId(int param) {
        return param != 1000 ? R.drawable.gfx_ct_display_100_100 : R.drawable.gfx_ct_display_empty;
    }

    public String getParameterTitle(int parameter) {
        UPointFilterParameter filter = getUPointFilterParameter();
        UPointParameter activeUPoint = filter.getActiveUPoint();
        if (activeUPoint != null) {
            return filter.getParameterTitle(getContext(), activeUPoint.getActiveFilterParameter());
        }
        Context context = getContext();
        int i = (filter.getUPointCount() == 0 || this._isAddUPointMode) ? R.string.add_a_control_point : R.string.select_a_control_point;
        return context.getString(i);
    }

    public void layout(boolean changed, int left, int top, int right, int bottom) {
        super.layout(changed, left, top, right, bottom);
        Iterator i$ = this._upoints.iterator();
        while (i$.hasNext()) {
            layoutUPoint((UPointView) i$.next(), left, top, right, bottom);
        }
        if (this._loupe != null && !this._loupeRect.isEmpty()) {
            this._loupe.layout(this._loupeRect.left, this._loupeRect.top, this._loupeRect.right, this._loupeRect.bottom);
            this._loupe.updateImage(this._lX, this._lY);
        }
    }

    private void layoutUPoint(UPointView upoint, int left, int top, int right, int bottom) {
        int imageWidth = upoint.getMeasuredWidth();
        int imageHeight = upoint.getMeasuredHeight();
        UPointParameter upointParameter = upoint.getParameter();
        int x0 = (upointParameter.getViewX() + left) - (imageWidth / 2);
        int y0 = (upointParameter.getViewY() + top) - (imageHeight / 2);
        LayoutParams layoutParams = new LayoutParams(imageWidth, imageHeight);
        layoutParams.leftMargin = x0;
        layoutParams.topMargin = y0;
        upoint.setLayoutParams(layoutParams);
        upoint.layout(x0, y0, x0 + imageWidth, y0 + imageWidth);
    }

    protected void showMenu(final Point point) {
        boolean visible = point != null ? HIDE_CP_WHILE_DRAGGING : $assertionsDisabled;
        if (visible != this._menuIsVisible) {
            this._menuIsVisible = visible;
            if (visible) {
                final int[] upoint_clipboard_params = new int[]{0, 1, 2, 4};
                this.menu = new PopoverWindow(getContext(), 0);
                final UPointParameter activeUpoint = getUPointFilterParameter().getActiveUPoint();
                if (activeUpoint != null) {
                    Context context = getContext();
                    this.menu.addItem(null, context.getString(R.string.menu_upoint_cut), HIDE_CP_WHILE_DRAGGING, R.string.menu_upoint_cut);
                    this.menu.addItem(null, context.getString(R.string.menu_upoint_copy), HIDE_CP_WHILE_DRAGGING, R.string.menu_upoint_copy);
                    if (this.clipboard != null) {
                        this.menu.addItem(null, context.getString(R.string.menu_upoint_paste), HIDE_CP_WHILE_DRAGGING, R.string.menu_upoint_paste);
                    }
                    this.menu.addItem(null, context.getString(R.string.menu_upoint_delete), HIDE_CP_WHILE_DRAGGING, R.string.menu_upoint_delete);
                    this.menu.addItem(null, context.getString(R.string.menu_upoint_reset), HIDE_CP_WHILE_DRAGGING, R.string.menu_upoint_reset);
                    this.menu.setOnActionItemClickListener(new OnActionItemClickListener() {
                        static final /* synthetic */ boolean $assertionsDisabled = (!UPointController.class.desiredAssertionStatus() ? UPointController.HIDE_CP_WHILE_DRAGGING : UPointController.$assertionsDisabled);

                        private String cut() {
                            copy();
                            del();
                            return UPointController.UPOINT_MENU_CUT;
                        }

                        private String copy() {
                            UPointController.this.clipboard = new int[upoint_clipboard_params.length];
                            for (int i = 0; i < upoint_clipboard_params.length; i++) {
                                UPointController.this.clipboard[i] = activeUpoint.getParameterValueOld(upoint_clipboard_params[i]);
                            }
                            return UPointController.UPOINT_MENU_COPY;
                        }

                        private String paste() {
                            UPointController.this.createUndo(UPointController.this.getContext().getString(R.string.undo_upoint_paste));
                            if ($assertionsDisabled || UPointController.this.clipboard != null) {
                                for (int i = 0; i < upoint_clipboard_params.length; i++) {
                                    activeUpoint.setParameterValueOld(upoint_clipboard_params[i], UPointController.this.clipboard[i]);
                                }
                                return UPointController.UPOINT_MENU_REPLACE;
                            }
                            throw new AssertionError();
                        }

                        private String del() {
                            UPointController.this.createUndo(UPointController.this.getContext().getString(R.string.undo_upoint_delete));
                            UPointController.this.delUPoint(activeUpoint);
                            UPointController.this._movingUPoint = null;
                            UPointController.this._addUpointFilterButton.setEnabled(UPointController.HIDE_CP_WHILE_DRAGGING);
                            return UPointController.UPOINT_MENU_DELETE;
                        }

                        private String reset() {
                            UPointController.this.createUndo(UPointController.this.getContext().getString(R.string.undo_upoint_reset));
                            for (int i = 0; i < upoint_clipboard_params.length; i++) {
                                activeUpoint.setParameterValueOld(upoint_clipboard_params[i], activeUpoint.getDefaultValue(upoint_clipboard_params[i]));
                            }
                            return UPointController.UPOINT_MENU_RESET;
                        }

                        public void onItemClick(int id) {
                            String action = "";
                            switch (id) {
                                case R.string.menu_upoint_copy:
                                    action = copy();
                                    break;
                                case R.string.menu_upoint_cut:
                                    action = cut();
                                    break;
                                case R.string.menu_upoint_delete:
                                    action = del();
                                    break;
                                case R.string.menu_upoint_paste:
                                    action = paste();
                                    break;
                                case R.string.menu_upoint_reset:
                                    action = reset();
                                    break;
                            }
                            TrackerData.getInstance().sendEvent(TrackerData.UI_ACTION, UPointController.UPOINT_MENU_ACTION, action, 0);
                            if (id != R.string.menu_upoint_copy) {
                                NotificationCenter.getInstance().performAction(ListenerType.DidChangeActiveFilterParameter, null);
                                UPointController.this.getWorkingAreaView().requestRender();
                            }
                            UPointController.this.menu.dismiss();
                            UPointController.this.menu = null;
                        }
                    });
                    this.menu.show(getActiveUPointView());
                } else if (this._upoints.size() != 8) {
                    this.menu.addItem(null, getContext().getString(R.string.menu_upoint_paste), HIDE_CP_WHILE_DRAGGING, R.string.menu_upoint_paste);
                    this.menu.setOnActionItemClickListener(new OnActionItemClickListener() {
                        static final /* synthetic */ boolean $assertionsDisabled = (!UPointController.class.desiredAssertionStatus() ? UPointController.HIDE_CP_WHILE_DRAGGING : UPointController.$assertionsDisabled);

                        public void onItemClick(int id) {
                            TrackerData.getInstance().sendEvent(TrackerData.UI_ACTION, UPointController.UPOINT_MENU_ACTION, UPointController.UPOINT_MENU_PASTE, 0);
                            if ($assertionsDisabled || id == R.string.menu_upoint_paste) {
                                UPointController.this.createUndo(UPointController.this.getContext().getString(R.string.undo_upoint_paste));
                                UPointParameter newParam = UPointController.this.addUPoint(point.x, point.y);
                                for (int i = 0; i < upoint_clipboard_params.length; i++) {
                                    newParam.setParameterValueOld(upoint_clipboard_params[i], UPointController.this.clipboard[i]);
                                }
                                NotificationCenter.getInstance().performAction(ListenerType.DidChangeActiveFilterParameter, null);
                                UPointController.this.menu.dismiss();
                                UPointController.this._uPointCircleHandler.setOn();
                                return;
                            }
                            throw new AssertionError();
                        }
                    });
                    this.menu.show(getWorkingAreaView().getImageView(), point.x, point.y);
                }
            } else if (this.menu != null) {
                this.menu.dismiss();
                this.menu = null;
            }
        }
    }

    public boolean initLeftFilterButton(BaseFilterButton button) {
        if (button == null) {
            this._addUpointFilterButton = null;
            return $assertionsDisabled;
        }
        this._addUpointFilterButton = button;
        this._addUpointFilterButton.setStateImages((int) R.drawable.icon_tb_add_cp_default, (int) R.drawable.icon_tb_add_cp_active, 0);
        this._addUpointFilterButton.setText(getButtonTitle(R.string.add));
        this._addUpointFilterButton.setStyle(R.style.EditToolbarButtonTitle.Selectable);
        this._addUpointFilterButton.setBackgroundDrawable(null);
        this._addUpointFilterButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (!UPointController.this._addUpointFilterButton.isSelected()) {
                    UPointController.this._addUpointFilterButton.setSelected(UPointController.HIDE_CP_WHILE_DRAGGING);
                    UPointController.this.showMenu(null);
                    NotificationCenter.getInstance().performAction(ListenerType.DidChangeActiveFilterParameter, null);
                }
                UPointController.this._isAddUPointMode = UPointController.this._addUpointFilterButton.isSelected();
            }
        });
        this._addUpointFilterButton.setSelected(this._isAddUPointMode);
        return HIDE_CP_WHILE_DRAGGING;
    }

    public boolean initRightFilterButton(BaseFilterButton button) {
        if (button == null) {
            this._hideUpointsButton = null;
            return $assertionsDisabled;
        }
        this._hideUpointsButton = button;
        this._hideUpointsButton.setStateImages((int) R.drawable.icon_tb_hide_cp_default, 0, 0);
        this._hideUpointsButton.setText(getButtonTitle(R.string.hide));
        this._hideUpointsButton.setStyle(R.style.EditToolbarButtonTitle);
        this._hideUpointsButton.setBackgroundResource(R.drawable.tb_button_background);
        this._hideUpointsButton.setOnTouchListener(new OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case 0:
                        UPointController.this._hideUpointsButton.setSelected(UPointController.HIDE_CP_WHILE_DRAGGING);
                        UPointController.this.hideUPoints();
                        return UPointController.HIDE_CP_WHILE_DRAGGING;
                    case 1:
                    case 3:
                        UPointController.this._hideUpointsButton.setSelected(UPointController.$assertionsDisabled);
                        UPointController.this.unhideUPoints();
                        return UPointController.HIDE_CP_WHILE_DRAGGING;
                    default:
                        return UPointController.$assertionsDisabled;
                }
            }
        });
        return HIDE_CP_WHILE_DRAGGING;
    }

    public BaseFilterButton getLeftFilterButton() {
        return this._addUpointFilterButton;
    }

    public BaseFilterButton getRightFilterButton() {
        return this._hideUpointsButton;
    }

    public void hideUPoints() {
        this._isHideUPointsMode = HIDE_CP_WHILE_DRAGGING;
        if (this._makeUPointsVisible != null) {
            getWorkingAreaView().getHandler().removeCallbacks(this._makeUPointsVisible);
        }
        Iterator i$ = this._upoints.iterator();
        while (i$.hasNext()) {
            ((UPointView) i$.next()).setVisibility(8);
        }
        this._uPointCircleHandler.setOff();
    }

    public void unhideUPoints() {
        this._isHideUPointsMode = $assertionsDisabled;
        Handler handler = getWorkingAreaView().getHandler();
        Runnable makeUPointsVisible = new MakeUPointsVisible();
        this._makeUPointsVisible = makeUPointsVisible;
        handler.postDelayed(makeUPointsVisible, 500);
    }

    public int getHelpResourceId() {
        return R.xml.overlay_localadjust;
    }

    public boolean useActionView() {
        return $assertionsDisabled;
    }

    public void updateEditingToolbarState() {
        if (this._upoints.size() >= 8) {
            this._addUpointFilterButton.setSelected($assertionsDisabled);
            this._addUpointFilterButton.setEnabled($assertionsDisabled);
            return;
        }
        this._addUpointFilterButton.setEnabled(HIDE_CP_WHILE_DRAGGING);
        this._isAddUPointMode = this._addUpointFilterButton.isSelected();
    }

    public void onPause() {
        super.onPause();
        showMenu(null);
        this._uPointCircleHandler.handlePinchAbort();
        setMagnifierOff();
        NativeCore.getInstance().setCompare($assertionsDisabled);
    }

    public void onResume() {
        updateEditingToolbarState();
    }

    public void onClose() {
        UPointParameter upoint = getUPointFilterParameter().getActiveUPoint();
        if (upoint != null) {
            upoint.setInking($assertionsDisabled);
        }
        super.onClose();
    }
}
