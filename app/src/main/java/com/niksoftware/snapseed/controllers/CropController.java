package com.niksoftware.snapseed.controllers;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;

import com.niksoftware.snapseed.controllers.adapters.StaticStyleItemListAdapter;
import com.niksoftware.snapseed.controllers.touchhandlers.TouchHandler;
import com.niksoftware.snapseed.core.DeviceDefs;
import com.niksoftware.snapseed.core.NotificationCenter;
import com.niksoftware.snapseed.core.NotificationCenterListener;
import com.niksoftware.snapseed.core.NotificationCenterListener.ListenerType;
import com.niksoftware.snapseed.core.UndoManager;
import com.niksoftware.snapseed.core.filterparameters.FilterParameter;
import com.niksoftware.snapseed.util.TrackerData;
import com.niksoftware.snapseed.views.BaseFilterButton;
import com.niksoftware.snapseed.views.ItemSelectorView;
import com.niksoftware.snapseed.views.ItemSelectorView.OnClickListener;
import com.niksoftware.snapseed.views.ItemSelectorView.OnVisibilityChangeListener;
import com.niksoftware.snapseed.views.WorkingAreaView;

public class CropController extends EmptyFilterController {
    private static final int BORDER = 30;
    private static final int CROP_MIN_SIZE = 30;
    static int _orientation = -1;
    boolean _addUndoByTouchMove = true;
    private BaseFilterButton _aspectRatioButton;
    private int _borderX = 30;
    private int _borderY = 30;
    Edge _edge = Edge.none;
    private ItemSelectorView _itemSelectorView;
    float _moveStartX = 0.0f;
    float _moveStartY = 0.0f;
    boolean _needInit = true;
    private BaseFilterButton _orientationButton;
    private CropOverlayView _overlay;
    float _pinchCenterX = -1.0f;
    float _pinchCenterY = -1.0f;
    float _pinchSize = -1.0f;
    private ImageView _preview;
    private Rect _previewBounds = new Rect();
    private Rect _previewBounds2 = new Rect();
    private int _previewEdgeSpace;
    private Bitmap _previewImage;
    private int _previewSize;
    private StaticStyleItemListAdapter _styleListAdapter;
    float _x1 = -1.0f;
    float _x2 = -1.0f;
    float _y1 = -1.0f;
    float _y2 = -1.0f;
    private NotificationCenterListener l_DidChangeFilterParameterValue;
    private NotificationCenterListener l_UndoRedoPerformed;
    boolean rotate = false;

    private class CropOverlayView extends View {
        public static final int s_overlayBorder = 5;
        Drawable _bottom;
        Drawable _bottom_left;
        Drawable _bottom_right;
        Rect _inner;
        Drawable _left;
        Rect _outer;
        Drawable _right;
        Drawable _top;
        Drawable _top_left;
        Drawable _top_right;

        public CropOverlayView(Context context) {
            super(context);
            Resources resources = getResources();
            this._top_left = resources.getDrawable(R.drawable.edit_grid_top_left);
            this._bottom_left = resources.getDrawable(R.drawable.edit_grid_bottom_left);
            this._top_right = resources.getDrawable(R.drawable.edit_grid_top_right);
            this._bottom_right = resources.getDrawable(R.drawable.edit_grid_bottom_right);
            this._top = resources.getDrawable(R.drawable.edit_grid_bordertop);
            this._bottom = resources.getDrawable(R.drawable.edit_grid_borderbottom);
            this._right = resources.getDrawable(R.drawable.edit_grid_borderright);
            this._left = resources.getDrawable(R.drawable.edit_grid_borderleft);
        }

        public void onDraw(Canvas c) {
            Paint paint = new Paint();
            paint.setColor(-434562791);
            c.drawRect(new Rect(this._outer.left, this._outer.top, this._outer.right, this._inner.top), paint);
            c.drawRect(new Rect(this._outer.left, this._inner.top, this._inner.left, this._inner.bottom), paint);
            c.drawRect(new Rect(this._inner.right, this._inner.top, this._outer.right, this._inner.bottom), paint);
            c.drawRect(new Rect(this._outer.left, this._inner.bottom, this._outer.right, this._outer.bottom), paint);
            int y_spacing = (this._inner.bottom - this._inner.top) / 3;
            int x_spacing = (this._inner.right - this._inner.left) / 3;
            Paint gridpaint3 = new Paint();
            gridpaint3.setColor(1040187391);
            gridpaint3.setStrokeWidth(3.0f);
            c.drawLine((float) this._inner.left, (float) (this._inner.top + y_spacing), (float) this._inner.right, (float) (this._inner.top + y_spacing), gridpaint3);
            c.drawLine((float) this._inner.left, (float) (this._inner.top + (y_spacing * 2)), (float) this._inner.right, (float) (this._inner.top + (y_spacing * 2)), gridpaint3);
            c.drawLine((float) (this._inner.left + x_spacing), (float) this._inner.top, (float) (this._inner.left + x_spacing), (float) this._inner.bottom, gridpaint3);
            c.drawLine((float) (this._inner.left + (x_spacing * 2)), (float) this._inner.top, (float) (this._inner.left + (x_spacing * 2)), (float) this._inner.bottom, gridpaint3);
            Paint gridpaint1 = new Paint();
            gridpaint1.setColor(1711276032);
            gridpaint1.setStrokeWidth(1.0f);
            c.drawLine((float) this._inner.left, (float) (this._inner.top + y_spacing), (float) this._inner.right, (float) (this._inner.top + y_spacing), gridpaint1);
            c.drawLine((float) this._inner.left, (float) (this._inner.top + (y_spacing * 2)), (float) this._inner.right, (float) (this._inner.top + (y_spacing * 2)), gridpaint1);
            c.drawLine((float) (this._inner.left + x_spacing), (float) this._inner.top, (float) (this._inner.left + x_spacing), (float) this._inner.bottom, gridpaint1);
            c.drawLine((float) (this._inner.left + (x_spacing * 2)), (float) this._inner.top, (float) (this._inner.left + (x_spacing * 2)), (float) this._inner.bottom, gridpaint1);
            this._top.draw(c);
            this._bottom.draw(c);
            this._left.draw(c);
            this._right.draw(c);
            this._top_left.draw(c);
            this._bottom_left.draw(c);
            this._top_right.draw(c);
            this._bottom_right.draw(c);
        }

        public void updateClipRect(float l, float t, float r, float b) {
            float x2;
            float x1;
            float y2;
            float y1;
            if (l >= r) {
                x2 = l + 5.0f;
                x1 = r + 5.0f;
            } else {
                x1 = l + 5.0f;
                x2 = r + 5.0f;
            }
            if (t >= b) {
                y2 = t + 5.0f;
                y1 = b + 5.0f;
            } else {
                y1 = t + 5.0f;
                y2 = b + 5.0f;
            }
            this._inner = new Rect((int) x1, (int) y1, (int) x2, (int) y2);
            if (!(this._outer == null || this._outer.contains(this._inner))) {
                x1 /= 0.0f;
            }
            int w = this._top_left.getIntrinsicWidth();
            int h = this._top_left.getIntrinsicHeight();
            int xx1 = ((int) x1) - 6;
            int xy1 = ((int) y1) - 6;
            int xx2 = ((int) x2) + 6;
            int xy2 = ((int) y2) + 6;
            this._top_left.setBounds(xx1, xy1, xx1 + w, xy1 + h);
            this._bottom_left.setBounds(xx1, xy2 - h, xx1 + w, xy2);
            this._top_right.setBounds(xx2 - w, xy1, xx2, xy1 + h);
            this._bottom_right.setBounds(xx2 - w, xy2 - h, xx2, xy2);
            int ix1 = ((int) x1) + 1;
            int iy1 = ((int) y1) - 2;
            int ix2 = ((int) x2) - 1;
            int iy2 = ((int) y2) + 2;
            w = this._left.getIntrinsicWidth();
            h = this._top.getIntrinsicHeight();
            this._top.setBounds(ix1, iy1, ix2, iy1 + h);
            this._bottom.setBounds(ix1, iy2 - h, ix2, iy2);
            this._left.setBounds(ix1 - w, iy1, ix1, iy2);
            this._right.setBounds(ix2, iy1, ix2 + w, iy2);
            invalidate();
        }

        public void onLayout(boolean changed, int l, int t, int r, int b) {
            Rect newouter = new Rect(5, 5, (r - l) - 5, (b - t) - 5);
            if (this._outer == null || newouter.left != this._outer.left || newouter.top != this._outer.top || newouter.right != this._outer.right || newouter.bottom != this._outer.bottom) {
                this._outer = newouter;
                this._inner = this._outer;
            }
        }

        public int width() {
            return getWidth() - 10;
        }

        public int height() {
            return getHeight() - 10;
        }
    }

    private class CropTouchListener extends TouchHandler {
        private final float touchAreaSize;

        public CropTouchListener(float touchAreaSize) {
            this.touchAreaSize = touchAreaSize;
        }

        public boolean needGestureDetector() {
            return true;
        }

        public boolean handleTouchDown(float x, float y) {
            if (CropController.this._isApplyingFilter) {
                return false;
            }
            CropController.this.checkBounds();
            if (getEdgeRect(Edge.center, this.touchAreaSize).contains(x, y)) {
                CropController.this._edge = Edge.center;
            } else if (getEdgeRect(Edge.left, this.touchAreaSize).contains(x, y)) {
                CropController.this._edge = Edge.left;
            } else if (getEdgeRect(Edge.top, this.touchAreaSize).contains(x, y)) {
                CropController.this._edge = Edge.top;
            } else if (getEdgeRect(Edge.right, this.touchAreaSize).contains(x, y)) {
                CropController.this._edge = Edge.right;
            } else if (getEdgeRect(Edge.bottom, this.touchAreaSize).contains(x, y)) {
                CropController.this._edge = Edge.bottom;
            } else if (getEdgeRect(Edge.top_left, this.touchAreaSize).contains(x, y)) {
                CropController.this._edge = Edge.top_left;
            } else if (getEdgeRect(Edge.bottom_left, this.touchAreaSize).contains(x, y)) {
                CropController.this._edge = Edge.bottom_left;
            } else if (getEdgeRect(Edge.top_right, this.touchAreaSize).contains(x, y)) {
                CropController.this._edge = Edge.top_right;
            } else if (getEdgeRect(Edge.bottom_right, this.touchAreaSize).contains(x, y)) {
                CropController.this._edge = Edge.bottom_right;
            }
            CropController.this._moveStartX = x;
            CropController.this._moveStartY = y;
            return true;
        }

        public boolean handleTouchMoved(float x, float y) {
            if (CropController.this._isApplyingFilter) {
                return false;
            }
            if (!(CropController.this._edge == Edge.none || CropController.this._edge == Edge.pinch)) {
                if (CropController.this._addUndoByTouchMove) {
                    UndoManager.getUndoManager().createUndo(CropController.this.getFilterParameter(), 1000, CropController.this.getContext().getString(CropController.this._edge == Edge.center ? R.string.param_CropCoordinates : R.string.param_CropResize));
                    CropController.this._addUndoByTouchMove = false;
                }
                float overlayWidth = (float) CropController.this._overlay.width();
                float overlayHeight = (float) CropController.this._overlay.height();
                float dx = x - CropController.this._moveStartX;
                float dy = y - CropController.this._moveStartY;
                float x1 = CropController.this._x1;
                float x2 = CropController.this._x2;
                float y1 = CropController.this._y1;
                float y2 = CropController.this._y2;
                float old_value;
                switch (CropController.this._edge) {
                    case left:
                        old_value = CropController.this._x1;
                        CropController.this._x1 += dx;
                        if (CropController.this._x2 - CropController.this._x1 < 30.0f) {
                            CropController.this._x1 = old_value;
                            break;
                        }
                        break;
                    case right:
                        old_value = CropController.this._x2;
                        CropController.this._x2 += dx;
                        if (CropController.this._x2 - CropController.this._x1 < 30.0f) {
                            CropController.this._x2 = old_value;
                            break;
                        }
                        break;
                    case top:
                        old_value = CropController.this._y1;
                        CropController.this._y1 += dy;
                        if (CropController.this._y2 - CropController.this._y1 < 30.0f) {
                            CropController.this._y1 = old_value;
                            break;
                        }
                        break;
                    case bottom:
                        old_value = CropController.this._y2;
                        CropController.this._y2 += dy;
                        if (CropController.this._y2 - CropController.this._y1 < 30.0f) {
                            CropController.this._y2 = old_value;
                            break;
                        }
                        break;
                    case top_left:
                        old_value = CropController.this._x1;
                        CropController.this._x1 += dx;
                        if (CropController.this._x2 - CropController.this._x1 < 30.0f) {
                            CropController.this._x1 = old_value;
                        }
                        old_value = CropController.this._y1;
                        CropController.this._y1 += dy;
                        if (CropController.this._y2 - CropController.this._y1 < 30.0f) {
                            CropController.this._y1 = old_value;
                            break;
                        }
                        break;
                    case top_right:
                        old_value = CropController.this._x2;
                        CropController.this._x2 += dx;
                        if (CropController.this._x2 - CropController.this._x1 < 30.0f) {
                            CropController.this._x2 = old_value;
                        }
                        old_value = CropController.this._y1;
                        CropController.this._y1 += dy;
                        if (CropController.this._y2 - CropController.this._y1 < 30.0f) {
                            CropController.this._y1 = old_value;
                            break;
                        }
                        break;
                    case bottom_left:
                        old_value = CropController.this._x1;
                        CropController.this._x1 += dx;
                        if (CropController.this._x2 - CropController.this._x1 < 30.0f) {
                            CropController.this._x1 = old_value;
                        }
                        old_value = CropController.this._y2;
                        CropController.this._y2 += dy;
                        if (CropController.this._y2 - CropController.this._y1 < 30.0f) {
                            CropController.this._y2 = old_value;
                            break;
                        }
                        break;
                    case bottom_right:
                        old_value = CropController.this._x2;
                        CropController.this._x2 += dx;
                        if (CropController.this._x2 - CropController.this._x1 < 30.0f) {
                            CropController.this._x2 = old_value;
                        }
                        old_value = CropController.this._y2;
                        CropController.this._y2 += dy;
                        if (CropController.this._y2 - CropController.this._y1 < 30.0f) {
                            CropController.this._y2 = old_value;
                            break;
                        }
                        break;
                    case center:
                        CropController.this._x1 += dx;
                        CropController.this._x2 += dx;
                        CropController.this._y1 += dy;
                        CropController.this._y2 += dy;
                        CropController.this.checkBounds();
                        break;
                }
                if (CropController.this._x1 < 0.0f) {
                    CropController.this._x1 = 0.0f;
                }
                if (CropController.this._y1 < 0.0f) {
                    CropController.this._y1 = 0.0f;
                }
                if (CropController.this._x2 > overlayWidth) {
                    CropController.this._x2 = overlayWidth;
                }
                if (CropController.this._y2 > overlayHeight) {
                    CropController.this._y2 = overlayHeight;
                }
                CropController.this.checkAspectRatio();
                if (CropController.this._x1 < 0.0f || CropController.this._y1 < 0.0f || CropController.this._x2 > overlayWidth || CropController.this._y2 > overlayHeight) {
                    CropController.this._x1 = x1;
                    CropController.this._y1 = y1;
                    CropController.this._x2 = x2;
                    CropController.this._y2 = y2;
                    return true;
                }
                CropController.this._moveStartX = x;
                CropController.this._moveStartY = y;
                CropController.this._overlay.updateClipRect(CropController.this._x1, CropController.this._y1, CropController.this._x2, CropController.this._y2);
                CropController.this.updateFilterParameter();
            }
            return true;
        }

        public void handleTouchCanceled(float x, float y) {
            if (!CropController.this._isApplyingFilter) {
                CropController.this.checkBounds();
            }
        }

        public void handleTouchUp(float x, float y) {
            if (!CropController.this._isApplyingFilter) {
                CropController.this._edge = Edge.none;
                CropController.this.checkBounds();
                CropController.this._addUndoByTouchMove = true;
            }
        }

        public boolean handlePinch(int x, int y, float size, float arc) {
            if (CropController.this._isApplyingFilter || CropController.this._edge != Edge.pinch) {
                return true;
            }
            float overlayWidth = (float) CropController.this._overlay.width();
            float overlayHeight = (float) CropController.this._overlay.height();
            int aspectRatioValue = CropController.this.getFilterParameter().getParameterValueOld(42);
            float percent = size / CropController.this._pinchSize;
            CropController.this._pinchSize = size;
            float w = (CropController.this._x2 - CropController.this._x1) * percent;
            float h = (CropController.this._y2 - CropController.this._y1) * percent;
            float r = CropController.this.getAspectRatio(CropController.this.rotate);
            float maxWidth = overlayWidth;
            float maxHeight = maxWidth * r;
            if (maxHeight > overlayHeight) {
                maxHeight = overlayHeight;
                maxWidth = maxHeight / r;
            }
            w = Math.max(30.0f, w);
            h = Math.max(30.0f, h);
            if (w > maxWidth || h > maxHeight) {
                if (aspectRatioValue == 0) {
                    w = Math.min(maxWidth, w);
                    h = Math.min(maxHeight, h);
                } else if (h > maxHeight) {
                    h = maxHeight;
                    w = h / r;
                } else {
                    w = maxWidth;
                    h = w * r;
                }
            }
            int minSizeW = 30;
            int minSizeH = 30;
            if (aspectRatioValue == 0) {
                r = 1.0f;
            }
            if (r < 1.0f) {
                minSizeW = Math.round(((float) 30) / r);
            } else {
                minSizeH = Math.round(((float) 30) * r);
            }
            if (w < ((float) minSizeW) || h < ((float) minSizeH)) {
                return true;
            }
            CropController.this._x1 = CropController.this._pinchCenterX - (w / 2.0f);
            CropController.this._x2 = CropController.this._pinchCenterX + (w / 2.0f);
            CropController.this._y1 = CropController.this._pinchCenterY - (h / 2.0f);
            CropController.this._y2 = CropController.this._pinchCenterY + (h / 2.0f);
            CropController.this.checkBounds();
            CropController.this.checkAspectRatio();
            CropController.this._overlay.updateClipRect(CropController.this._x1, CropController.this._y1, CropController.this._x2, CropController.this._y2);
            CropController.this.updateFilterParameter();
            return true;
        }

        public boolean handlePinchBegin(int x, int y, float size, float arc) {
            if (!(CropController.this._isApplyingFilter || CropController.this._preview.getVisibility() == 0)) {
                UndoManager.getUndoManager().createUndo(CropController.this.getFilterParameter(), 1000, CropController.this.getContext().getString(R.string.param_CropResize));
                CropController.this._pinchSize = size;
                CropController.this._pinchCenterX = CropController.this._x1 + ((CropController.this._x2 - CropController.this._x1) / 2.0f);
                CropController.this._pinchCenterY = CropController.this._y1 + ((CropController.this._y2 - CropController.this._y1) / 2.0f);
                CropController.this._edge = Edge.pinch;
            }
            return true;
        }

        public void handlePinchEnd(int x, int y, float size, float arc) {
            if (!CropController.this._isApplyingFilter) {
                CropController.this._pinchSize = -1.0f;
                CropController.this._pinchCenterX = -1.0f;
                CropController.this._pinchCenterY = -1.0f;
                CropController.this._edge = Edge.none;
            }
        }

        private RectF getEdgeRect(Edge edge, float touchArea) {
            float f = 0.0f;
            switch (edge) {
                case left:
                    return new RectF(CropController.this._x1 - touchArea, CropController.this._y1 + touchArea, CropController.this._x1 + touchArea, CropController.this._y2 - touchArea);
                case right:
                    return new RectF(CropController.this._x2 - touchArea, CropController.this._y1 + touchArea, CropController.this._x2 + touchArea, CropController.this._y2 - touchArea);
                case top:
                    return new RectF(CropController.this._x1 + touchArea, CropController.this._y1 - touchArea, CropController.this._x2 - touchArea, CropController.this._y1 + touchArea);
                case bottom:
                    return new RectF(CropController.this._x1 + touchArea, CropController.this._y2 - touchArea, CropController.this._x2 - touchArea, CropController.this._y2 + touchArea);
                case top_left:
                    return new RectF(CropController.this._x1 - touchArea, CropController.this._y1 - touchArea, CropController.this._x1 + touchArea, CropController.this._y1 + touchArea);
                case top_right:
                    return new RectF(CropController.this._x2 - touchArea, CropController.this._y1 - touchArea, CropController.this._x2 + touchArea, CropController.this._y1 + touchArea);
                case bottom_left:
                    return new RectF(CropController.this._x1 - touchArea, CropController.this._y2 - touchArea, CropController.this._x1 + touchArea, CropController.this._y2 + touchArea);
                case bottom_right:
                    return new RectF(CropController.this._x2 - touchArea, CropController.this._y2 - touchArea, CropController.this._x2 + touchArea, CropController.this._y2 + touchArea);
                case center:
                    RectF edgeRect = new RectF(CropController.this._x1, CropController.this._y1, CropController.this._x2, CropController.this._y2);
                    float f2 = edgeRect.width() < 60.0f ? 0.0f : touchArea / 2.0f;
                    if (edgeRect.height() >= 60.0f) {
                        f = touchArea / 2.0f;
                    }
                    edgeRect.inset(f2, f);
                    return edgeRect;
                default:
                    return new RectF();
            }
        }
    }

    private enum Edge {
        none,
        center,
        left,
        top_left,
        top,
        top_right,
        right,
        bottom_right,
        bottom,
        bottom_left,
        pinch
    }

    public void init(ControllerContext context) {
        super.init(context);
        Resources resources = getResources();
        addTouchListener(new CropTouchListener(resources.getDimension(R.dimen.crop_touch_area_size)));
        this._previewSize = resources.getDimensionPixelSize(R.dimen.tb_subpanel_preview_size);
        this._previewEdgeSpace = resources.getDimensionPixelSize(R.dimen.tb_subpanel_preview_edge_space);
        setUpNotificationListeners();
        setUpStyleList();
        setUpItemSelector();
    }

    private void setUpNotificationListeners() {
        this.l_DidChangeFilterParameterValue = new NotificationCenterListener() {
            public void performAction(Object arg) {
                CropController.this.initDefaultSize();
            }
        };
        this.l_UndoRedoPerformed = new NotificationCenterListener() {
            public void performAction(Object arg) {
                CropController.this.updateGUIbyFilterParameter();
            }
        };
        NotificationCenter notificationCenter = NotificationCenter.getInstance();
        notificationCenter.addListener(this.l_DidChangeFilterParameterValue, ListenerType.DidChangeFilterParameterValue);
        notificationCenter.addListener(this.l_UndoRedoPerformed, ListenerType.UndoRedoPerformed);
    }

    private void setUpStyleList() {
        Resources resources = getResources();
        aspectRatioPreviews = new Bitmap[18];
        Bitmap[] stateBitmaps = getRatioItemStateImages(resources, 2);
        aspectRatioPreviews[4] = stateBitmaps[0];
        aspectRatioPreviews[5] = stateBitmaps[1];
        stateBitmaps = getRatioItemStateImages(resources, 4);
        aspectRatioPreviews[6] = stateBitmaps[0];
        aspectRatioPreviews[7] = stateBitmaps[1];
        stateBitmaps = getRatioItemStateImages(resources, 3);
        aspectRatioPreviews[8] = stateBitmaps[0];
        aspectRatioPreviews[9] = stateBitmaps[1];
        stateBitmaps = getRatioItemStateImages(resources, 5);
        aspectRatioPreviews[10] = stateBitmaps[0];
        aspectRatioPreviews[11] = stateBitmaps[1];
        stateBitmaps = getRatioItemStateImages(resources, 6);
        aspectRatioPreviews[12] = stateBitmaps[0];
        aspectRatioPreviews[13] = stateBitmaps[1];
        stateBitmaps = getRatioItemStateImages(resources, 7);
        aspectRatioPreviews[14] = stateBitmaps[0];
        aspectRatioPreviews[15] = stateBitmaps[1];
        stateBitmaps = getRatioItemStateImages(resources, 8);
        aspectRatioPreviews[16] = stateBitmaps[0];
        aspectRatioPreviews[17] = stateBitmaps[1];
        this._styleListAdapter = new StaticStyleItemListAdapter(getFilterParameter(), 42, aspectRatioPreviews);
        this._styleListAdapter.setActiveItemId(Integer.valueOf(getFilterParameter().getParameterValueOld(42)));
    }

    private void setUpItemSelector() {
        this._itemSelectorView = getItemSelectorView();
        this._itemSelectorView.reloadSelector(this._styleListAdapter);
        this._itemSelectorView.setSelectorOnClickListener(new OnClickListener() {
            public boolean onItemClick(Integer itemId) {
                if (CropController.this.changeParameter(CropController.this.getFilterParameter(), 42, itemId.intValue())) {
                    TrackerData.getInstance().usingParameter(42, false);
                    CropController.this._styleListAdapter.setActiveItemId(itemId);
                    CropController.this._itemSelectorView.refreshSelectorItems(CropController.this._styleListAdapter, true);
                }
                return true;
            }

            public boolean onContextButtonClick() {
                return false;
            }
        });
        this._itemSelectorView.setOnVisibilityChangeListener(new OnVisibilityChangeListener() {
            public void onVisibilityChanged(boolean isVisible) {
                CropController.this._aspectRatioButton.setSelected(isVisible);
            }
        });
    }

    public void cleanup() {
        hidePreview();
        WorkingAreaView workingAreaView = getWorkingAreaView();
        if (this._overlay != null) {
            workingAreaView.removeView(this._overlay);
            this._overlay = null;
        }
        if (this._preview != null) {
            workingAreaView.removeShadowedView(this._preview);
            this._preview = null;
        }
        if (!(this._previewImage == null || this._previewImage == workingAreaView.getTilesProvider().getScreenSourceImage())) {
            this._previewImage.recycle();
        }
        NotificationCenter notificationCenter = NotificationCenter.getInstance();
        notificationCenter.removeListener(this.l_DidChangeFilterParameterValue, ListenerType.DidChangeFilterParameterValue);
        notificationCenter.removeListener(this.l_UndoRedoPerformed, ListenerType.UndoRedoPerformed);
        getEditingToolbar().itemSelectorWillHide();
        this._itemSelectorView.setVisible(false, false);
        this._itemSelectorView.cleanup();
        this._itemSelectorView = null;
        super.cleanup();
    }

    public void onCancelFilter() {
        if (previewIsVisible()) {
            hidePreview();
        }
        super.onCancelFilter();
    }

    protected void applyFilter() {
        if (!this._isApplyingFilter) {
            this._isApplyingFilter = true;
            lockCurrentOrientation();
            this._overlay.setVisibility(8);
            final float scaledWidth = (float) this._overlay.width();
            final float scaledHeight = (float) this._overlay.height();
            new Thread(new Runnable() {
                public void run() {
                    Bitmap source = CropController.this.getTilesProvider().getSourceImage();
                    int originalWidth = source.getWidth();
                    int originalHeight = source.getHeight();
                    int x = Math.round((CropController.this._x1 / scaledWidth) * ((float) originalWidth));
                    int y = Math.round((CropController.this._y1 / scaledHeight) * ((float) originalHeight));
                    int width = Math.round((CropController.this._x2 / scaledWidth) * ((float) originalWidth)) - x;
                    int height = Math.round((CropController.this._y2 / scaledHeight) * ((float) originalHeight)) - y;
                    Bitmap dest = (width <= 1 || height <= 1 || (width == originalWidth && height == originalHeight)) ? null : Bitmap.createBitmap(source, x, y, width, height);
                    CropController.this.postExportResult(dest, false);
                }
            }).start();
        }
    }

    private void addOverlays() {
        this._overlay = new CropOverlayView(getContext());
        WorkingAreaView workingAreaView = getWorkingAreaView();
        workingAreaView.addView(this._overlay);
        this._overlay.setVisibility(4);
        this._preview = new ImageView(getContext());
        this._preview.setVisibility(8);
        this._preview.setScaleType(ScaleType.FIT_XY);
        workingAreaView.addShadowedView(this._preview);
        workingAreaView.bringChildToFront(workingAreaView.getActionView());
        workingAreaView.bringChildToFront(workingAreaView.getHelpButton());
        if (!DeviceDefs.isTablet()) {
            workingAreaView.bringChildToFront(workingAreaView.getCompareButton());
        }
    }

    public int getFilterType() {
        return 6;
    }

    private void initDefaultSize() {
        float overlayWidth = (float) this._overlay.width();
        float overlayHeight = (float) this._overlay.height();
        float maxWidth = overlayWidth - ((float) (this._borderX * 2));
        float maxHeight = overlayHeight - ((float) (this._borderY * 2));
        float ratio = getAspectRatio(this.rotate);
        if (getFilterParameter().getParameterValueOld(42) == 0) {
            this._x1 = (float) this._borderX;
            this._y1 = (float) this._borderY;
            this._x2 = ((float) this._borderX) + maxWidth;
            this._y2 = ((float) this._borderY) + maxHeight;
        } else if (ratio != -1.0f) {
            float width;
            float height;
            if (ratio < 1.0f) {
                width = maxWidth;
                height = width * ratio;
                if (height > maxHeight) {
                    height = maxHeight;
                    width = height / ratio;
                }
            } else {
                height = maxHeight;
                width = height / ratio;
                if (width > maxWidth) {
                    width = maxWidth;
                    height = width * ratio;
                }
            }
            if (width < 30.0f) {
                width = Math.min(maxWidth, 30.0f);
            }
            if (height < 30.0f) {
                height = Math.min(maxHeight, 30.0f);
            }
            this._x1 = (overlayWidth - width) / 2.0f;
            this._y1 = (overlayHeight - height) / 2.0f;
            this._x2 = this._x1 + width;
            this._y2 = this._y1 + height;
        }
        this._overlay.updateClipRect(this._x1, this._y1, this._x2, this._y2);
        updateFilterParameter();
    }

    private float getAspectRatio(boolean r) {
        float maxWidth = (float) this._overlay.width();
        float maxHeight = (float) this._overlay.height();
        float ratio = -1.0f;
        switch (getFilterParameter().getParameterValueOld(42)) {
            case 0:
            case 1:
                ratio = maxWidth / maxHeight;
                break;
            case 2:
                ratio = 1.0f;
                break;
            case 3:
                ratio = (float) Math.sqrt(2.0d);
                break;
            case 4:
                ratio = 1.5f;
                break;
            case 5:
                ratio = 1.3333334f;
                break;
            case 6:
                ratio = 1.25f;
                break;
            case 7:
                ratio = 1.4f;
                break;
            case 8:
                ratio = 1.7777778f;
                break;
        }
        if (r) {
            return ratio;
        }
        return 1.0f / ratio;
    }

    private void checkAspectRatio() {
        if (getFilterParameter().getParameterValueOld(42) != 0) {
            float r = getAspectRatio(this.rotate);
            float width = Math.abs(this._x2 - this._x1);
            float height = Math.abs(this._y2 - this._y1);
            float overlayHeight = (float) this._overlay.height();
            float dedicatedWidth;
            switch (this._edge) {
                case left:
                case right:
                    float deltaHeight = (height - (width * r)) / 2.0f;
                    this._y2 -= deltaHeight;
                    this._y1 += deltaHeight;
                    return;
                case top:
                case bottom:
                    float deltaWidth = (width - (height / r)) / 2.0f;
                    this._x2 -= deltaWidth;
                    this._x1 += deltaWidth;
                    return;
                case top_left:
                case top_right:
                    this._y1 = this._y2 - (width * r);
                    if (this._y1 < 0.0f) {
                        this._y1 = 0.0f;
                        dedicatedWidth = (this._y2 - this._y1) / r;
                        if (this._edge == Edge.top_left) {
                            this._x1 = this._x2 - dedicatedWidth;
                            return;
                        } else {
                            this._x2 = this._x1 + dedicatedWidth;
                            return;
                        }
                    }
                    return;
                case bottom_left:
                case bottom_right:
                    this._y2 = this._y1 + (width * r);
                    if (this._y2 > overlayHeight) {
                        this._y2 = overlayHeight;
                        dedicatedWidth = (this._y2 - this._y1) / r;
                        if (this._edge == Edge.bottom_left) {
                            this._x1 = this._x2 - dedicatedWidth;
                            return;
                        } else {
                            this._x2 = this._x1 + dedicatedWidth;
                            return;
                        }
                    }
                    return;
                default:
                    return;
            }
        }
    }

    private void checkBounds() {
        if (this._x1 >= this._x2) {
            float t = this._x2;
            this._x2 = this._x1;
            this._x1 = t;
        }
        if (this._y1 >= this._y2) {
            t = this._y2;
            this._y2 = this._y1;
            this._y1 = t;
        }
        float overlayWidth = (float) this._overlay.width();
        float overlayHeight = (float) this._overlay.height();
        if (this._x1 < 0.0f) {
            float w = this._x2 - this._x1;
            this._x1 = 0.0f;
            this._x2 = Math.min(overlayWidth, w);
        }
        if (this._x2 > overlayWidth) {
            this._x1 = Math.max(0.0f, overlayWidth - (this._x2 - this._x1));
            this._x2 = overlayWidth;
        }
        if (this._y1 < 0.0f) {
            float h = this._y2 - this._y1;
            this._y1 = 0.0f;
            this._y2 = Math.min(overlayHeight, h);
        }
        if (this._y2 > overlayHeight) {
            this._y1 = Math.max(0.0f, overlayHeight - (this._y2 - this._y1));
            this._y2 = overlayHeight;
        }
    }

    private void changeOrientation() {
        boolean z;
        boolean z2 = true;
        float width = this._x2 - this._x1;
        float height = this._y2 - this._y1;
        float overlayWidth = (float) (this._overlay.width() - (this._borderX * 2));
        float overlayHeight = (float) (this._overlay.height() - (this._borderY * 2));
        float midX = this._x1 + (width / 2.0f);
        float midY = this._y1 + (height / 2.0f);
        if (this.rotate) {
            z = false;
        } else {
            z = true;
        }
        float aspectRatio = getAspectRatio(z);
        if (width > overlayHeight) {
            height = overlayHeight;
            width = Math.max(30.0f, overlayHeight / aspectRatio);
        } else if (height > overlayWidth) {
            width = overlayWidth;
            height = Math.max(30.0f, overlayWidth * aspectRatio);
        } else {
            float t = height;
            height = width;
            width = t;
        }
        if (this.rotate) {
            z2 = false;
        }
        this.rotate = z2;
        UndoManager.getUndoManager().createUndo(getFilterParameter(), 1000, getContext().getString(R.string.param_CropRotate));
        if (width < 30.0f || height < 30.0f) {
            initDefaultSize();
            return;
        }
        this._x1 = midX - (width / 2.0f);
        this._x2 = (width / 2.0f) + midX;
        this._y1 = midY - (height / 2.0f);
        this._y2 = (height / 2.0f) + midY;
        checkBounds();
        checkAspectRatio();
        this._overlay.updateClipRect(this._x1, this._y1, this._x2, this._y2);
        updateFilterParameter();
    }

    private void updateFilterParameter() {
        FilterParameter filter = getFilterParameter();
        if (filter != null) {
            float overlayWidth = (float) this._overlay.width();
            float overlayHeight = (float) this._overlay.height();
            filter.setParameterValueOld(43, Math.round((this._x1 / overlayWidth) * 2.14748365E9f));
            filter.setParameterValueOld(44, Math.round((this._x2 / overlayWidth) * 2.14748365E9f));
            filter.setParameterValueOld(45, Math.round((this._y1 / overlayHeight) * 2.14748365E9f));
            filter.setParameterValueOld(46, Math.round((this._y2 / overlayHeight) * 2.14748365E9f));
            filter.setParameterValueOld(40, this.rotate ? 1 : 0);
        }
    }

    private void updateGUIbyFilterParameter() {
        FilterParameter filter = getFilterParameter();
        if (filter != null) {
            float overlayWidth = (float) this._overlay.width();
            float overlayHeight = (float) this._overlay.height();
            int intNormX1 = filter.getParameterValueOld(43);
            int intNormX2 = filter.getParameterValueOld(44);
            int intNormY1 = filter.getParameterValueOld(45);
            int intNormY2 = filter.getParameterValueOld(46);
            this.rotate = filter.getParameterValueOld(40) == 1;
            float normX2 = ((float) intNormX2) / 2.14748365E9f;
            float normY1 = ((float) intNormY1) / 2.14748365E9f;
            float normY2 = ((float) intNormY2) / 2.14748365E9f;
            this._x1 = (((float) intNormX1) / 2.14748365E9f) * overlayWidth;
            this._x2 = normX2 * overlayWidth;
            this._y1 = normY1 * overlayHeight;
            this._y2 = normY2 * overlayHeight;
            this._overlay.updateClipRect(this._x1, this._y1, this._x2, this._y2);
        }
    }

    public void layout(boolean changed, int left, int top, int right, int bottom) {
        boolean firstInit = this._overlay == null;
        if (firstInit) {
            addOverlays();
        }
        this._overlay.layout(left - 5, top - 5, right + 5, bottom + 5);
        if (firstInit && (this._overlay.width() <= 90 || this._overlay.height() <= 90)) {
            this._borderX = 0;
            this._borderY = 0;
        }
        if (firstInit) {
            initDefaultSize();
        }
        if (this._overlay.getVisibility() != 0) {
            ObjectAnimator animator = ObjectAnimator.ofFloat(this._overlay, "alpha", new float[]{0.0f, 1.0f});
            animator.setDuration(333);
            animator.start();
            this._overlay.setVisibility(0);
        }
        WorkingAreaView workingAreaView = getWorkingAreaView();
        int isLandscape = workingAreaView.getWidth() > workingAreaView.getHeight() ? 1 : 0;
        if (!(_orientation == -1 || _orientation == isLandscape)) {
            updateGUIbyFilterParameter();
            if (this._preview.getVisibility() == 0) {
                hidePreview();
            }
        }
        _orientation = isLandscape;
        if (this._needInit) {
            initDefaultSize();
            this._needInit = false;
        }
        getEditingToolbar().setEnabled(true);
    }

    private Bitmap[] getRatioItemStateImages(Resources resources, int aspectRatioId) {
        Bitmap[] stateBitmaps = null;
        float ratio = 1.0f;
        switch (aspectRatioId) {
            case 0:
                stateBitmaps = new Bitmap[]{BitmapFactory.decodeResource(resources, R.drawable.icon_fo_free_crop_ratio_default), null};
                break;
            case 1:
                stateBitmaps = new Bitmap[]{BitmapFactory.decodeResource(resources, R.drawable.icon_fo_compare_default), null};
                break;
            case 2:
                ratio = 1.0f;
                break;
            case 3:
                ratio = (float) (1.0d / Math.sqrt(2.0d));
                break;
            case 4:
                ratio = 0.6666667f;
                break;
            case 5:
                ratio = 0.75f;
                break;
            case 6:
                ratio = 0.8f;
                break;
            case 7:
                ratio = 0.71428573f;
                break;
            case 8:
                ratio = 0.5625f;
                break;
        }
        if (stateBitmaps != null) {
            return stateBitmaps;
        }
        Paint paint = new Paint();
        paint.setAntiAlias(false);
        paint.setStrokeWidth(1.0f);
        paint.setStyle(Style.STROKE);
        Rect frameRect = new Rect(this._previewEdgeSpace, (this._previewSize - ((int) (((float) (this._previewSize - (this._previewEdgeSpace * 2))) * ratio))) - this._previewEdgeSpace, this._previewSize - (this._previewEdgeSpace * 2), this._previewSize - (this._previewEdgeSpace * 2));
        paint.setColor(resources.getColor(R.color.tb_subpanel_normal_item_title));
        new Canvas(Bitmap.createBitmap(this._previewSize, this._previewSize, Config.ARGB_8888)).drawRect(frameRect, paint);
        paint.setColor(resources.getColor(R.color.tb_subpanel_selected_item_title));
        new Canvas(Bitmap.createBitmap(this._previewSize, this._previewSize, Config.ARGB_8888)).drawRect(frameRect, paint);
        return new Bitmap[]{normalBitmap, selectedBitmap};
    }

    public boolean initLeftFilterButton(BaseFilterButton button) {
        if (button == null) {
            this._aspectRatioButton = null;
            return false;
        }
        this._aspectRatioButton = button;
        this._aspectRatioButton.setStateImages((int) R.drawable.icon_tb_aspect_ratio_default, (int) R.drawable.icon_tb_aspect_ratio_active, 0);
        this._aspectRatioButton.setText(getButtonTitle(R.string.btn_aspect_ratio));
        this._aspectRatioButton.setStyle(R.style.EditToolbarButtonTitle.Selectable);
        this._aspectRatioButton.setBackgroundDrawable(null);
        this._aspectRatioButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                CropController.this._styleListAdapter.setActiveItemId(Integer.valueOf(CropController.this.getFilterParameter().getParameterValueOld(42)));
                CropController.this._itemSelectorView.refreshSelectorItems(CropController.this._styleListAdapter, true);
                CropController.this._itemSelectorView.setVisible(true, true);
            }
        });
        return true;
    }

    public boolean initRightFilterButton(BaseFilterButton button) {
        if (button == null) {
            this._orientationButton = null;
            return false;
        }
        this._orientationButton = button;
        this._orientationButton.setStateImages((int) R.drawable.icon_tb_rotatecrop_default, 0, 0);
        this._orientationButton.setText(getButtonTitle(R.string.rotate));
        this._orientationButton.setStyle(R.style.EditToolbarButtonTitle);
        this._orientationButton.setBackgroundResource(R.drawable.tb_button_background);
        this._orientationButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                CropController.this.changeOrientation();
            }
        });
        return true;
    }

    public BaseFilterButton getLeftFilterButton() {
        return this._aspectRatioButton;
    }

    public BaseFilterButton getRightFilterButton() {
        return this._orientationButton;
    }

    public int[] getGlobalAdjustmentParameters() {
        return new int[]{42};
    }

    public boolean showsParameterView() {
        return false;
    }

    public void showPreview() {
        Bitmap source = getTilesProvider().getScreenSourceImage();
        int originalWidth = source.getWidth();
        int originalHeight = source.getHeight();
        float scaledWidth = (float) this._overlay.width();
        float scaledHeight = (float) this._overlay.height();
        int x = Math.round((this._x1 / scaledWidth) * ((float) originalWidth));
        int y = Math.round((this._y1 / scaledHeight) * ((float) originalHeight));
        int w = Math.round((this._x2 / scaledWidth) * ((float) originalWidth)) - x;
        int h = Math.round((this._y2 / scaledHeight) * ((float) originalHeight)) - y;
        if (w >= 1 && h >= 1) {
            Rect newPreviewBounds = new Rect(x, y, x + w, y + h);
            WorkingAreaView workingAreaView = getWorkingAreaView();
            Rect fitRect = WorkingAreaView.getFitRect(workingAreaView.getVisualFrame(), (float) newPreviewBounds.width(), (float) newPreviewBounds.height(), (float) workingAreaView.getBorder());
            if (!(this._previewBounds.left == newPreviewBounds.left && this._previewBounds.top == newPreviewBounds.top && this._previewBounds.right == newPreviewBounds.right && this._previewBounds.bottom == newPreviewBounds.bottom && fitRect.equals(this._previewBounds2))) {
                if (!(this._previewImage == null || this._previewImage == source)) {
                    this._previewImage.recycle();
                }
                this._previewImage = Bitmap.createBitmap(source, x, y, w, h);
                this._preview.setImageBitmap(this._previewImage);
                this._preview.setScaleType(ScaleType.FIT_CENTER);
                this._previewBounds2 = fitRect;
                this._previewBounds = newPreviewBounds;
            }
            this._preview.setVisibility(0);
            this._preview.bringToFront();
            workingAreaView.hideImageView();
            this._overlay.setVisibility(8);
            workingAreaView.updateShadowBounds(this._previewBounds2);
            workingAreaView.getShadowLayer().layoutChildren();
        }
    }

    public void onPause() {
        super.onPause();
        hidePreview();
    }

    public void hidePreview() {
        WorkingAreaView workingAreaView = getWorkingAreaView();
        if (workingAreaView.getImageView() != null) {
            workingAreaView.showImageView();
            if (this._preview != null) {
                this._preview.setVisibility(8);
            }
            if (this._overlay != null) {
                this._overlay.setVisibility(0);
            }
            workingAreaView.updateShadowBounds();
        }
    }

    public void setPreviewVisible(boolean visible) {
        if (visible) {
            showPreview();
        } else {
            hidePreview();
        }
    }

    public boolean previewIsVisible() {
        return this._preview.getVisibility() == 0;
    }

    public int getHelpResourceId() {
        return R.xml.overlay_crop;
    }
}
