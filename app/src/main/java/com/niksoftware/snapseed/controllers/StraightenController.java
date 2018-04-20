package com.niksoftware.snapseed.controllers;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Path.FillType;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.FloatMath;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import com.niksoftware.snapseed.MainActivity;
import com.niksoftware.snapseed.controllers.touchhandlers.ParameterHandler;
import com.niksoftware.snapseed.core.NotificationCenter;
import com.niksoftware.snapseed.core.NotificationCenterListener;
import com.niksoftware.snapseed.core.NotificationCenterListener.ListenerType;
import com.niksoftware.snapseed.core.SnapseedAppDelegate;
import com.niksoftware.snapseed.core.UndoManager;
import com.niksoftware.snapseed.core.filterparameters.FilterParameter;
import com.niksoftware.snapseed.core.rendering.TilesProvider;
import com.niksoftware.snapseed.util.TrackerData;
import com.niksoftware.snapseed.views.BaseFilterButton;
import com.niksoftware.snapseed.views.ImageViewSW;
import com.niksoftware.snapseed.views.ShadowLayer;
import com.niksoftware.snapseed.views.WorkingAreaView;

public class StraightenController extends FilterController {
    private static final int GRID_NUMBER = 9;
    private static final float LIMIT_FOR_ROTATION = 10.0f;
    public static RotationDirection rotationDirection = RotationDirection.RotationDirection_None;
    private float SCALE_FACTOR = 0.6f;
    private boolean _dontHandleTouchEvents = false;
    private float[] _inCoord = new float[8];
    private boolean _markUndo = true;
    private float _middleX;
    private float _middleY;
    private float[] _outCoord = new float[8];
    private StraightenOverlay _overlay;
    private ImageView _preview;
    private float _previousX = 0.0f;
    private float _previousY = 0.0f;
    private float _rotateAngle = 0.0f;
    private float _rotateAngle360 = 0.0f;
    private float _rotateAngleX90 = 0.0f;
    private BaseFilterButton _rotateLeftButton;
    private BaseFilterButton _rotateRightButton;
    private float _scaledHeight;
    private float _scaledWidth;
    private NotificationCenterListener _undoRedoPerformed;
    private float previousAngle = Float.NEGATIVE_INFINITY;
    private float rotAngle = 0.0f;

    public enum Half {
        Half_Top,
        Half_Bottom
    }

    public enum RotationDirection {
        RotationDirection_None,
        RotationDirection_CW,
        RotationDirection_CCW
    }

    private class StraightenOverlay extends View {
        private static final int ICON_SIZE = 6;
        private Drawable _bottom;
        private Drawable _bottomLeft;
        private Drawable _bottomRight;
        private Paint _gridPaint1;
        private Paint _gridPaint2;
        private Paint _imageShadePaint;
        private Rect _inner;
        private Drawable _left;
        private Path _path = new Path();
        private Drawable _right;
        private Matrix _rotationMatrix = new Matrix();
        private Drawable _top;
        private Drawable _topLeft;
        private Drawable _topRight;

        public StraightenOverlay(Context context) {
            super(context);
            Resources resources = getResources();
            this._topLeft = resources.getDrawable(R.drawable.edit_grid_top_left);
            this._bottomLeft = resources.getDrawable(R.drawable.edit_grid_bottom_left);
            this._topRight = resources.getDrawable(R.drawable.edit_grid_top_right);
            this._bottomRight = resources.getDrawable(R.drawable.edit_grid_bottom_right);
            this._top = resources.getDrawable(R.drawable.edit_grid_bordertop);
            this._bottom = resources.getDrawable(R.drawable.edit_grid_borderbottom);
            this._right = resources.getDrawable(R.drawable.edit_grid_borderright);
            this._left = resources.getDrawable(R.drawable.edit_grid_borderleft);
            this._gridPaint1 = new Paint();
            this._gridPaint1.setColor(1711276032);
            this._gridPaint1.setStrokeWidth(1.0f);
            this._gridPaint2 = new Paint();
            this._gridPaint2.setColor(1040187391);
            this._gridPaint2.setStrokeWidth(3.0f);
            this._imageShadePaint = new Paint();
            this._imageShadePaint.setAntiAlias(true);
            this._imageShadePaint.setSubpixelText(true);
            this._imageShadePaint.setStrokeWidth(1.8f);
        }

        public void onDraw(Canvas canvas) {
            float y_spacing = ((float) (this._inner.bottom - this._inner.top)) / 9.0f;
            float x_spacing = ((float) (this._inner.right - this._inner.left)) / 9.0f;
            for (int i = 0; i <= 9; i++) {
                Canvas canvas2 = canvas;
                canvas2.drawLine((((float) i) * x_spacing) + ((float) this._inner.left), (float) this._inner.top, (((float) i) * x_spacing) + ((float) this._inner.left), (float) this._inner.bottom, this._gridPaint2);
                canvas2 = canvas;
                canvas2.drawLine((((float) i) * x_spacing) + ((float) this._inner.left), (float) this._inner.top, (((float) i) * x_spacing) + ((float) this._inner.left), (float) this._inner.bottom, this._gridPaint1);
                canvas2 = canvas;
                canvas2.drawLine((float) this._inner.left, (((float) i) * y_spacing) + ((float) this._inner.top), (float) this._inner.right, (((float) i) * y_spacing) + ((float) this._inner.top), this._gridPaint2);
                canvas2 = canvas;
                canvas2.drawLine((float) this._inner.left, (((float) i) * y_spacing) + ((float) this._inner.top), (float) this._inner.right, (((float) i) * y_spacing) + ((float) this._inner.top), this._gridPaint1);
            }
            this._top.draw(canvas);
            this._bottom.draw(canvas);
            this._left.draw(canvas);
            this._right.draw(canvas);
            this._topLeft.draw(canvas);
            this._bottomLeft.draw(canvas);
            this._topRight.draw(canvas);
            this._bottomRight.draw(canvas);
            float angle = StraightenController.this.getWorkingAreaView().getShadowLayer().getRotation();
            this._rotationMatrix.reset();
            this._rotationMatrix.setRotate(angle, StraightenController.this._middleX, StraightenController.this._middleY);
            this._rotationMatrix.mapPoints(StraightenController.this._outCoord, StraightenController.this._inCoord);
            this._imageShadePaint.setColor(2130706432);
            this._path.reset();
            this._path.moveTo(StraightenController.this._outCoord[0], StraightenController.this._outCoord[1]);
            this._path.lineTo(StraightenController.this._outCoord[2], StraightenController.this._outCoord[3]);
            this._path.lineTo(StraightenController.this._outCoord[4], StraightenController.this._outCoord[5]);
            this._path.lineTo(StraightenController.this._outCoord[6], StraightenController.this._outCoord[7]);
            this._path.lineTo(StraightenController.this._outCoord[0], StraightenController.this._outCoord[1]);
            this._path.lineTo((float) this._inner.left, (float) this._inner.top);
            this._path.lineTo((float) this._inner.left, (float) this._inner.bottom);
            this._path.lineTo((float) this._inner.right, (float) this._inner.bottom);
            this._path.lineTo((float) this._inner.right, (float) this._inner.top);
            this._path.lineTo((float) this._inner.left, (float) this._inner.top);
            this._path.setFillType(FillType.EVEN_ODD);
            canvas.drawPath(this._path, this._imageShadePaint);
            this._imageShadePaint.setColor(-935905481);
            canvas.drawLine(StraightenController.this._outCoord[0], StraightenController.this._outCoord[1], StraightenController.this._outCoord[2], StraightenController.this._outCoord[3], this._imageShadePaint);
            canvas.drawLine(StraightenController.this._outCoord[2], StraightenController.this._outCoord[3], StraightenController.this._outCoord[4], StraightenController.this._outCoord[5], this._imageShadePaint);
            canvas.drawLine(StraightenController.this._outCoord[4], StraightenController.this._outCoord[5], StraightenController.this._outCoord[6], StraightenController.this._outCoord[7], this._imageShadePaint);
            canvas.drawLine(StraightenController.this._outCoord[6], StraightenController.this._outCoord[7], StraightenController.this._outCoord[0], StraightenController.this._outCoord[1], this._imageShadePaint);
        }

        public void updateClipRect(float l, float t, float r, float b) {
            int maxWidth = (int) Math.abs(r - l);
            int maxHeight = (int) Math.abs(t - b);
            int x1 = (int) Math.min(l, r);
            int x2 = (int) Math.max(l, r);
            int y1 = (int) Math.min(t, b);
            int y2 = (int) Math.max(t, b);
            this._inner = new Rect(x1, y1, x2, y2);
            int w = Math.min(this._topLeft.getIntrinsicWidth(), maxWidth);
            int h = Math.min(this._topLeft.getIntrinsicHeight(), maxHeight);
            int xx1 = x1 - 6;
            int xy1 = y1 - 6;
            int xx2 = x2 + 6;
            int xy2 = y2 + 6;
            this._topLeft.setBounds(xx1, xy1, xx1 + w, xy1 + h);
            this._bottomLeft.setBounds(xx1, xy2 - h, xx1 + w, xy2);
            this._topRight.setBounds(xx2 - w, xy1, xx2, xy1 + h);
            this._bottomRight.setBounds(xx2 - w, xy2 - h, xx2, xy2);
            w = Math.min(this._left.getIntrinsicWidth(), maxWidth);
            h = Math.min(this._top.getIntrinsicHeight(), maxHeight);
            int ix1 = x1 + 1;
            int iy1 = y1 - 2;
            int ix2 = x2 - 1;
            int iy2 = y2 + 2;
            this._top.setBounds(ix1, iy1, ix2, iy1 + h);
            this._bottom.setBounds(ix1, iy2 - h, ix2, iy2);
            this._left.setBounds(ix1 - w, iy1, ix1, iy2);
            this._right.setBounds(ix2, iy1, ix2 + w, iy2);
            invalidate();
        }

        public void onLayout(boolean changed, int l, int t, int r, int b) {
            this._inner = new Rect(0, 0, r - l, b - t);
        }
    }

    private class StraightenParameterHandler extends ParameterHandler {
        boolean angleWasChanged;

        private StraightenParameterHandler() {
        }

        public boolean handleTouchDown(float x, float y) {
            StraightenController.this._previousX = (((float) StraightenController.this.getWorkingAreaView().getImageView().getLeft()) + x) + ((float) StraightenController.this.getWorkingAreaView().getShadowLayer().getLeft());
            StraightenController.this._previousY = (((float) StraightenController.this.getWorkingAreaView().getImageView().getTop()) + y) + ((float) StraightenController.this.getWorkingAreaView().getShadowLayer().getTop());
            StraightenController.this._markUndo = true;
            StraightenController.rotationDirection = RotationDirection.RotationDirection_None;
            StraightenController.this.rotAngle = StraightenController.this._rotateAngle;
            return true;
        }

        public void handleTouchUp(float x, float y) {
            if (this.angleWasChanged) {
                TrackerData.getInstance().usingParameter(38, true);
            }
        }

        public boolean handleTouchMoved(float x, float y) {
            if (StraightenController.this._dontHandleTouchEvents) {
                return true;
            }
            WorkingAreaView workingAreaView = StraightenController.this.getWorkingAreaView();
            View imageView = workingAreaView.getImageView();
            View shadowLayer = workingAreaView.getShadowLayer();
            if (Math.abs(StraightenController.this._rotateAngle) < StraightenController.LIMIT_FOR_ROTATION || StraightenController.rotationDirection == RotationDirection.RotationDirection_None) {
                int imageViewMidX = (imageView.getLeft() + shadowLayer.getLeft()) + (imageView.getWidth() / 2);
                int imageViewMidY = (imageView.getTop() + shadowLayer.getTop()) + (imageView.getHeight() / 2);
                float xFirstVectorFormMiddlePoint = StraightenController.this._previousX - ((float) imageViewMidX);
                float yFirstVectorFormMiddlePoint = StraightenController.this._previousY - ((float) imageViewMidY);
                float xSecondVectorFormMiddlePoint = (x + ((float) (imageView.getLeft() + shadowLayer.getLeft()))) - ((float) imageViewMidX);
                float ySecondVectorFormMiddlePoint = (y + ((float) (imageView.getTop() + shadowLayer.getTop()))) - ((float) imageViewMidY);
                if (StraightenController.rotationDirection == RotationDirection.RotationDirection_None) {
                    StraightenController.rotationDirection = StraightenController.this.getFirstRotationDirectionFromStartPoint(xFirstVectorFormMiddlePoint, yFirstVectorFormMiddlePoint, xSecondVectorFormMiddlePoint, ySecondVectorFormMiddlePoint);
                }
                this.angleWasChanged |= StraightenController.this.straightenRotate((float) Math.toDegrees((double) ((float) StraightenController.this.calculateAngleFormPoint(xFirstVectorFormMiddlePoint, yFirstVectorFormMiddlePoint, xSecondVectorFormMiddlePoint, ySecondVectorFormMiddlePoint, StraightenController.rotationDirection))));
            } else {
                StraightenController.this._previousX = (((float) imageView.getLeft()) + x) + ((float) shadowLayer.getLeft());
                StraightenController.this._previousY = (((float) imageView.getTop()) + y) + ((float) shadowLayer.getTop());
                StraightenController.this.rotAngle = Math.signum(StraightenController.this._rotateAngle) * StraightenController.LIMIT_FOR_ROTATION;
                StraightenController.this._rotateAngle = StraightenController.this.rotAngle;
                StraightenController.rotationDirection = RotationDirection.RotationDirection_None;
            }
            return true;
        }
    }

    public RotationDirection getFirstRotationDirectionFromStartPoint(float xStart, float yStart, float xEnd, float yEnd) {
        return Math.atan2((double) yEnd, (double) xEnd) - Math.atan2((double) yStart, (double) xStart) > 0.0d ? RotationDirection.RotationDirection_CW : RotationDirection.RotationDirection_CCW;
    }

    public Half getHalfOfVector(float vectY) {
        return vectY <= 0.0f ? Half.Half_Top : Half.Half_Bottom;
    }

    public double getAngleInHalf(float xPoint, float yPoint, RotationDirection rotDirection) {
        double angle = Math.atan2((double) yPoint, (double) xPoint);
        if (getHalfOfVector(yPoint) != Half.Half_Top) {
            return rotDirection == RotationDirection.RotationDirection_CW ? angle : angle - 3.141592653589793d;
        } else if (rotDirection == RotationDirection.RotationDirection_CW) {
            return 3.141592653589793d + angle;
        } else {
            return angle;
        }
    }

    public double calculateAngleFormPoint(float xStartPoint, float yStartPoint, float xEndPoint, float yEndPoint, RotationDirection rotDir) {
        double result;
        Half startHalf = getHalfOfVector(yStartPoint);
        Half endHalf = getHalfOfVector(yEndPoint);
        double startAngle = getAngleInHalf(xStartPoint, yStartPoint, rotDir);
        double endAngle = getAngleInHalf(xEndPoint, yEndPoint, rotDir);
        if (startHalf == endHalf) {
            result = endAngle - startAngle;
        } else {
            result = rotDir == RotationDirection.RotationDirection_CW ? (3.141592653589793d - startAngle) + endAngle : (-3.141592653589793d - startAngle) + endAngle;
        }
        if (result > 3.141592653589793d) {
            result -= 6.283185307179586d;
        }
        if (result < -3.141592653589793d) {
            return result + 6.283185307179586d;
        }
        return result;
    }

    private void doSaveForUndoRotationAngle() {
        UndoManager.getUndoManager().createUndo(getFilterParameter(), 39, "");
    }

    private void doSaveForUndoStraightenAngle() {
        UndoManager.getUndoManager().createUndo(getFilterParameter(), 38, "");
    }

    private void rotateView(float angle) {
        if (angle != this.previousAngle) {
            updateFilterParameter();
            this.previousAngle = angle;
        }
        NotificationCenter.getInstance().performAction(ListenerType.DidChangeActiveFilterParameter, Integer.valueOf((int) this._rotateAngle));
        getWorkingAreaView().getShadowLayer().setRotation(angle);
        updateSize();
        reconfigureLayers();
    }

    private void limitRotateAngle360() {
        if (this._rotateAngle360 <= -360.0f) {
            this._rotateAngle360 += 360.0f;
        } else if (this._rotateAngle360 >= 360.0f) {
            this._rotateAngle360 -= 360.0f;
        }
    }

    private void limitRotateAngle90() {
        if (this._rotateAngleX90 <= -360.0f) {
            this._rotateAngleX90 += 360.0f;
        } else if (this._rotateAngleX90 >= 360.0f) {
            this._rotateAngleX90 -= 360.0f;
        }
    }

    private boolean straightenRotate(float angleDelta) {
        if (this.rotAngle == LIMIT_FOR_ROTATION && angleDelta > 0.0f) {
            return false;
        }
        if (this.rotAngle == -10.0f && angleDelta < 0.0f) {
            return false;
        }
        if (this._markUndo && Math.abs(angleDelta) != 0.0f) {
            doSaveForUndoStraightenAngle();
            this._markUndo = false;
        }
        angleDelta = Math.min(Math.max(this.rotAngle + angleDelta, -10.0f), LIMIT_FOR_ROTATION) - this.rotAngle;
        this._rotateAngle = this.rotAngle + angleDelta;
        this._rotateAngle360 = this._rotateAngleX90 + this._rotateAngle;
        limitRotateAngle360();
        rotateView(this._rotateAngle360);
        if (angleDelta != 0.0f) {
            return true;
        }
        return false;
    }

    private void straightenRotateForUndo(float straightenAngle, float rotationAngle) {
        this._rotateAngleX90 = rotationAngle;
        this._rotateAngle = straightenAngle;
        this._rotateAngle360 = this._rotateAngleX90 + this._rotateAngle;
        rotateView(this._rotateAngle360);
    }

    private void straightenRotateLeft(float angleDelta) {
        this._rotateAngleX90 -= angleDelta;
        limitRotateAngle90();
        this._rotateAngle360 = this._rotateAngleX90 + this._rotateAngle;
        rotateView(this._rotateAngle360);
    }

    private void straightenRotateRight(float angleDelta) {
        this._rotateAngleX90 += angleDelta;
        limitRotateAngle90();
        this._rotateAngle360 = this._rotateAngleX90 + this._rotateAngle;
        rotateView(this._rotateAngle360);
    }

    private void updateFilterParameter() {
        FilterParameter filterParameter = getFilterParameter();
        if (filterParameter != null) {
            filterParameter.setParameterValueOld(38, (int) (this._rotateAngle * 100.0f));
            filterParameter.setParameterValueOld(39, (int) (this._rotateAngleX90 * 100.0f));
        }
    }

    private void updateGUIbyFilterParameter() {
        FilterParameter filterParameter = getFilterParameter();
        if (filterParameter != null) {
            straightenRotateForUndo(((float) filterParameter.getParameterValueOld(38)) / 100.0f, ((float) filterParameter.getParameterValueOld(39)) / 100.0f);
        }
    }

    private static double maximalNeededWH(int imageViewW, int imageViewH) {
        return 2.0d * (Math.cos((3.141592653589793d * Math.max(0.0d, ((180.0d * Math.atan(((double) imageViewW) / ((double) imageViewH))) / 3.141592653589793d) - 10.0d)) / 180.0d) * (Math.sqrt((double) ((imageViewH * imageViewH) + (imageViewW * imageViewW))) / 2.0d));
    }

    float calculateScaleFactor() {
        WorkingAreaView workingAreaView = getWorkingAreaView();
        int imageViewW = workingAreaView.getShadowLayer().getWidth();
        int imageViewH = workingAreaView.getShadowLayer().getHeight();
        Bitmap sourceImage = getTilesProvider().getSourceImage();
        boolean z = this._dontHandleTouchEvents || sourceImage.getWidth() < 32 || sourceImage.getHeight() < 32;
        this._dontHandleTouchEvents = z;
        float limit = (float) Math.min(workingAreaView.getHeight() - getEditingToolbar().getHeight(), workingAreaView.getWidth());
        float maxNeeded = (float) Math.max(maximalNeededWH(imageViewH, imageViewW), maximalNeededWH(imageViewW, imageViewH));
        if (limit > maxNeeded) {
            return 1.0f;
        }
        return limit / maxNeeded;
    }

    public static float imageScaleFactor(int shadowLayerAreaWidth, int shadowLayerAreaHeight) {
        WorkingAreaView workingAreaView = MainActivity.getWorkingAreaView();
        float limit = (float) Math.min(workingAreaView.getHeight() - MainActivity.getEditingToolbar().getHeight(), workingAreaView.getWidth());
        float maxNeeded = Math.max((float) maximalNeededWH(shadowLayerAreaWidth, shadowLayerAreaHeight), (float) maximalNeededWH(shadowLayerAreaHeight, shadowLayerAreaWidth));
        return limit > maxNeeded ? 1.0f : limit / maxNeeded;
    }

    private RectF calculateGridRect(RectF picture, float angle, float angle360) {
        double alpha = (Math.atan(Math.min((1.0d * ((double) picture.height())) / ((double) picture.width()), (1.0d * ((double) picture.width())) / ((double) picture.height()))) * 180.0d) / 3.141592653589793d;
        double newDiagonal = ((Math.sin((3.141592653589793d * alpha) / 180.0d) * (((double) FloatMath.sqrt((picture.width() * picture.width()) + (picture.height() * picture.height()))) / 2.0d)) / Math.sin((3.141592653589793d * (((double) (180.0f - Math.abs(angle))) - alpha)) / 180.0d)) * 2.0d;
        double calculatedHeight = Math.sqrt((newDiagonal * newDiagonal) / (1.0d + Math.pow((double) (picture.width() / picture.height()), 2.0d)));
        double calculatedWidth = ((double) (picture.width() / picture.height())) * calculatedHeight;
        if (Math.abs(Math.round(angle360 - angle)) % 180 != 0) {
            double t = calculatedHeight;
            calculatedHeight = calculatedWidth;
            calculatedWidth = t;
        }
        float middleX = (picture.left + picture.right) / 2.0f;
        float middleY = (picture.top + picture.bottom) / 2.0f;
        return new RectF((float) (((double) middleX) - (calculatedWidth / 2.0d)), (float) (((double) middleY) - (calculatedHeight / 2.0d)), (float) (((double) middleX) + (calculatedWidth / 2.0d)), (float) (((double) middleY) + (calculatedHeight / 2.0d)));
    }

    private void updateSize() {
        RectF rect = calculateGridRect(new RectF(this._middleX - (this._scaledWidth / 2.0f), this._middleY - (this._scaledHeight / 2.0f), this._middleX + (this._scaledWidth / 2.0f), this._middleY + (this._scaledHeight / 2.0f)), this._rotateAngle, this._rotateAngle360);
        if (this._overlay != null) {
            this._overlay.updateClipRect(rect.left, rect.top, rect.right, rect.bottom);
        }
    }

    private void initDefaultSize() {
        if (this._overlay != null) {
            this._middleX = ((float) this._overlay.getWidth()) / 2.0f;
            this._middleY = ((float) this._overlay.getHeight()) / 2.0f;
            View iv = getWorkingAreaView().getImageView();
            this._scaledWidth = ((float) (iv.getRight() - iv.getLeft())) * this.SCALE_FACTOR;
            this._scaledHeight = ((float) (iv.getBottom() - iv.getTop())) * this.SCALE_FACTOR;
            float _x1 = this._middleX - (this._scaledWidth / 2.0f);
            float _x2 = _x1 + this._scaledWidth;
            float _y1 = this._middleY - (this._scaledHeight / 2.0f);
            float _y2 = _y1 + this._scaledHeight;
            this._inCoord[0] = _x1;
            this._inCoord[1] = _y1;
            this._inCoord[2] = _x2;
            this._inCoord[3] = _y1;
            this._inCoord[4] = _x2;
            this._inCoord[5] = _y2;
            this._inCoord[6] = _x1;
            this._inCoord[7] = _y2;
            updateSize();
        }
    }

    public int getFilterType() {
        return 5;
    }

    public int[] getGlobalAdjustmentParameters() {
        return new int[]{39};
    }

    public boolean initLeftFilterButton(BaseFilterButton button) {
        if (button == null) {
            this._rotateLeftButton = null;
            return false;
        }
        this._rotateLeftButton = button;
        this._rotateLeftButton.setStateImages((int) R.drawable.icon_tb_rotate_left_default, 0, 0);
        this._rotateLeftButton.setText(getButtonTitle(R.string.rotate_left));
        this._rotateLeftButton.setStyle(R.style.EditToolbarButtonTitle);
        this._rotateLeftButton.setBackgroundResource(R.drawable.tb_button_background);
        this._rotateLeftButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (!StraightenController.this._dontHandleTouchEvents) {
                    StraightenController.this.doSaveForUndoRotationAngle();
                    StraightenController.this.straightenRotateLeft(90.0f);
                    TrackerData.getInstance().usingParameter(39, true);
                }
            }
        });
        return true;
    }

    public boolean initRightFilterButton(BaseFilterButton button) {
        if (button == null) {
            this._rotateRightButton = null;
            return false;
        }
        this._rotateRightButton = button;
        this._rotateRightButton.setStateImages((int) R.drawable.icon_tb_rotate_right_default, 0, 0);
        this._rotateRightButton.setText(getButtonTitle(R.string.rotate_right));
        this._rotateRightButton.setStyle(R.style.EditToolbarButtonTitle);
        this._rotateRightButton.setBackgroundResource(R.drawable.tb_button_background);
        this._rotateRightButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (!StraightenController.this._dontHandleTouchEvents) {
                    StraightenController.this.doSaveForUndoRotationAngle();
                    StraightenController.this.straightenRotateRight(90.0f);
                    TrackerData.getInstance().usingParameter(39, true);
                }
            }
        });
        return true;
    }

    public BaseFilterButton getLeftFilterButton() {
        return this._rotateLeftButton;
    }

    public BaseFilterButton getRightFilterButton() {
        return this._rotateRightButton;
    }

    void reconfigureLayers() {
        this.SCALE_FACTOR = calculateScaleFactor();
        WorkingAreaView workingAreaView = getWorkingAreaView();
        View shadowLayer = workingAreaView.getShadowLayer();
        shadowLayer.setPivotX(((float) shadowLayer.getWidth()) / 2.0f);
        shadowLayer.setPivotY(((float) shadowLayer.getHeight()) / 2.0f);
        shadowLayer.setScaleX(this.SCALE_FACTOR);
        shadowLayer.setScaleY(this.SCALE_FACTOR);
        Rect visualFrame = workingAreaView.getVisualFrame();
        if (this._overlay != null) {
            this._overlay.layout(visualFrame.left, visualFrame.top, visualFrame.right, visualFrame.bottom);
        }
        if (this._preview != null && this._preview.getVisibility() == 0) {
            showPreview();
        }
        initDefaultSize();
        workingAreaView.requestLayout();
        getEditingToolbar().setEnabled(true);
    }

    public void layout(boolean changed, int l, int t, int r, int b) {
        reconfigureLayers();
    }

    public boolean useActionView() {
        return false;
    }

    public void init(ControllerContext context) {
        super.init(context);
        addTouchListener(new StraightenParameterHandler());
        this._undoRedoPerformed = new NotificationCenterListener() {
            public void performAction(Object arg) {
                StraightenController.this.updateGUIbyFilterParameter();
            }
        };
        NotificationCenter.getInstance().addListener(this._undoRedoPerformed, ListenerType.UndoRedoPerformed);
        WorkingAreaView workingAreaView = getWorkingAreaView();
        this._overlay = new StraightenOverlay(getContext());
        workingAreaView.addView(this._overlay);
        this._preview = new ImageView(getContext());
        this._preview.setVisibility(8);
        workingAreaView.addShadowedView(this._preview);
        View imageView = getWorkingAreaView().getImageView();
        if (imageView instanceof ImageViewSW) {
            ((ImageViewSW) imageView).setFillBackground(false);
        }
    }

    public void cleanup() {
        WorkingAreaView workingAreaView = getWorkingAreaView();
        hidePreview();
        super.cleanup();
        workingAreaView.getShadowLayer().setRotation(0.0f);
        workingAreaView.getShadowLayer().setScaleX(1.0f);
        workingAreaView.getShadowLayer().setScaleY(1.0f);
        if (this._overlay != null) {
            workingAreaView.removeView(this._overlay);
            this._overlay = null;
        }
        if (this._preview != null) {
            workingAreaView.removeShadowedView(this._preview);
            this._preview = null;
        }
        if (this._undoRedoPerformed != null) {
            NotificationCenter.getInstance().removeListener(this._undoRedoPerformed, ListenerType.UndoRedoPerformed);
        }
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
            cleanup();
            WorkingAreaView workingAreaView = getWorkingAreaView();
            if (Math.abs(this._rotateAngle360 % 360.0f) == 0.0f) {
                postExportResult(null, false);
                return;
            }
            hidePreview();
            if (this._overlay != null) {
                workingAreaView.removeView(this._overlay);
                this._overlay = null;
            }
            if (this._preview != null) {
                workingAreaView.removeShadowedView(this._preview);
                this._preview = null;
            }
            System.runFinalization();
            System.gc();
            SnapseedAppDelegate.getInstance().progressStart((int) R.string.processing);
            new Thread(new Runnable() {
                public void run() {
                    StraightenController.this.postExportResult(StraightenController.this.getCurrentSelectedRect(true), true);
                }
            }).start();
        }
    }

    private Bitmap getCurrentSelectedRect(boolean useHundredPercentPicture) {
        TilesProvider tilesProvider = getTilesProvider();
        Bitmap source = useHundredPercentPicture ? tilesProvider.getSourceImage() : tilesProvider.getScreenSourceImage();
        RectF gridRect = calculateGridRect(new RectF(0.0f, 0.0f, (float) source.getWidth(), (float) source.getHeight()), this._rotateAngle, this._rotateAngle);
        int width = (int) (gridRect.right - gridRect.left);
        int height = (int) (gridRect.bottom - gridRect.top);
        if (useHundredPercentPicture) {
            SnapseedAppDelegate.getInstance().progressSetValue(50);
        }
        Bitmap targetBitmap = Bitmap.createBitmap(source.getWidth(), source.getHeight(), Config.ARGB_8888);
        Canvas canvas = new Canvas(targetBitmap);
        canvas.rotate(this._rotateAngle, ((float) canvas.getWidth()) / 2.0f, ((float) canvas.getHeight()) / 2.0f);
        Paint paint = new Paint();
        paint.setFilterBitmap(true);
        Rect imageRect = new Rect(0, 0, source.getWidth(), source.getHeight());
        canvas.drawBitmap(source, imageRect, imageRect, paint);
        if (useHundredPercentPicture) {
            source.recycle();
            System.runFinalization();
            System.gc();
        }
        if (useHundredPercentPicture) {
            SnapseedAppDelegate.getInstance().progressSetValue(75);
        }
        Matrix rotationMatrix = new Matrix();
        rotationMatrix.setRotate(this._rotateAngle360 - this._rotateAngle);
        Bitmap finalBitmap = Bitmap.createBitmap(targetBitmap, (source.getWidth() - width) / 2, (source.getHeight() - height) / 2, width, height, rotationMatrix, true);
        targetBitmap.recycle();
        return finalBitmap;
    }

    public void showPreview() {
        WorkingAreaView workingAreaView = getWorkingAreaView();
        workingAreaView.hideImageView();
        this._overlay.setVisibility(8);
        Bitmap result = getCurrentSelectedRect(false);
        workingAreaView.updateShadowBounds(WorkingAreaView.getFitRect(getWorkingAreaView().getVisualFrame(), (float) result.getWidth(), (float) result.getHeight(), (float) workingAreaView.getBorder()));
        this._preview.setImageBitmap(result);
        this._preview.setScaleType(ScaleType.FIT_CENTER);
        this._preview.setVisibility(0);
        ShadowLayer shadowLayer = workingAreaView.getShadowLayer();
        shadowLayer.setScaleX(1.0f);
        shadowLayer.setScaleY(1.0f);
        shadowLayer.setRotation(0.0f);
        shadowLayer.layoutChildren();
    }

    public void onPause() {
        super.onPause();
        hidePreview();
    }

    public void hidePreview() {
        WorkingAreaView workingAreaView = getWorkingAreaView();
        ShadowLayer shadowLayer = workingAreaView.getShadowLayer();
        shadowLayer.setScaleX(this.SCALE_FACTOR);
        shadowLayer.setScaleY(this.SCALE_FACTOR);
        shadowLayer.setRotation(this._rotateAngle360);
        if (this._preview != null) {
            this._preview.setVisibility(8);
        }
        workingAreaView.showImageView();
        if (this._overlay != null) {
            this._overlay.setVisibility(0);
        }
        workingAreaView.updateShadowBounds();
        if (this._overlay != null) {
            initDefaultSize();
            this._overlay.forceLayout();
        }
        if (this._preview != null) {
            this._preview.setImageBitmap(null);
        }
    }

    public boolean previewIsVisible() {
        return this._preview.getVisibility() == 0;
    }

    public void setPreviewVisible(boolean visible) {
        if (visible) {
            showPreview();
        } else {
            hidePreview();
        }
    }

    public int getHelpResourceId() {
        return R.xml.overlay_straighten;
    }
}
