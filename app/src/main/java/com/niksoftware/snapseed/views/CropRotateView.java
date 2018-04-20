package com.niksoftware.snapseed.views;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

import com.niksoftware.snapseed.util.Geometry;
import com.niksoftware.snapseed.util.Geometry.Line;
import com.niksoftware.snapseed.util.Geometry.Vector;
import com.niksoftware.snapseed.views.FreeTransformTouchHandler.TransformListener;
import java.util.Arrays;
import java.util.List;

public class CropRotateView extends View implements TransformListener, CropAreaTransformTouchHandler.TransformListener {
    private static final int CROP_AREA_MARGIN = 100;
    private static final float SCALE_ROTATE_NO_FEEDBACK_THRESHOLD = 120.0f;
    private boolean allowCropAreaTransform = false;
    private final Drawable cropAreaBackground;
    private final RectF cropAreaScreenRect = new RectF();
    private PointF cropAreaSize;
    private CropAreaTransformTouchHandler cropTransformHandler;
    private float currentAngle;
    private float currentScale;
    private Bitmap dimmedImage;
    private boolean exceedingCropArea;
    private FreeTransformTouchHandler freeTransformHandler;
    private Bitmap image;
    private final Paint imagePaint = new Paint();
    private boolean isFreeTransform;
    private final PointF[] lastTouches = new PointF[]{new PointF(), new PointF()};
    private final Paint normalCropGridPaint;
    private final Paint secondaryCropGridPaint;
    private float startAngle;
    private final PointF startCenter = new PointF();
    private final PointF startCropAreaSize = new PointF();
    private float startSpan;
    private final PointF[] startTouches = new PointF[]{new PointF(), new PointF()};
    private final Matrix startTransformMatrix = new Matrix();
    private Matrix transformMatrix;
    private ValueAnimator transitionAnimator;
    private final Paint warningCropGridPaint;

    public CropRotateView(Context context) {
        super(context);
        this.imagePaint.setAntiAlias(true);
        Resources resources = getResources();
        this.normalCropGridPaint = new Paint();
        this.normalCropGridPaint.setAntiAlias(true);
        this.normalCropGridPaint.setColor(-1);
        this.normalCropGridPaint.setStyle(Style.STROKE);
        this.normalCropGridPaint.setStrokeWidth(2.0f);
        this.warningCropGridPaint = new Paint(this.normalCropGridPaint);
        this.warningCropGridPaint.setColor(-65536);
        this.warningCropGridPaint.setStrokeWidth(3.0f);
        this.secondaryCropGridPaint = new Paint(this.normalCropGridPaint);
        this.secondaryCropGridPaint.setColor(-3355444);
        this.secondaryCropGridPaint.setStrokeWidth(1.0f);
        this.cropAreaBackground = resources.getDrawable(R.drawable.tiled_checker);
        this.freeTransformHandler = new FreeTransformTouchHandler(this);
        this.cropTransformHandler = new CropAreaTransformTouchHandler(this);
    }

    public void setAllowCropAreaTransform(boolean allowTransform) {
        this.allowCropAreaTransform = allowTransform;
        invalidate();
    }

    public boolean getAllowCropAreaTransform() {
        return this.allowCropAreaTransform;
    }

    public void setImage(Bitmap image) {
        this.image = image;
        if (this.dimmedImage != null) {
            this.dimmedImage.recycle();
        }
        this.dimmedImage = createDimmedBitmap(image);
        this.cropAreaSize = null;
        this.transformMatrix = null;
        if (this.image != null) {
            setCropAreaSize((float) image.getWidth(), (float) image.getHeight(), false, false);
        } else {
            invalidate();
        }
    }

    public void setCropAreaSize(float width, float height, boolean animated, boolean preserveTransform) {
        this.cropAreaSize = new PointF(width, height);
        refreshTransformMatrix(width, height, animated, preserveTransform);
        if (!animated) {
            invalidate();
        }
    }

    public PointF getCropAreaSize() {
        if (this.transformMatrix == null) {
            return null;
        }
        float scale = 1.0f / this.transformMatrix.mapRadius(1.0f);
        return new PointF(this.cropAreaScreenRect.width() * scale, this.cropAreaScreenRect.height() * scale);
    }

    public void setImageTransformation(Matrix transformMatrix) {
        throw new AssertionError("Not implemented!");
    }

    public Matrix getImageTransform() {
        Matrix imageTransform = new Matrix(this.transformMatrix);
        imageTransform.postTranslate(-this.cropAreaScreenRect.left, -this.cropAreaScreenRect.top);
        float scale = 1.0f / this.transformMatrix.mapRadius(1.0f);
        imageTransform.postScale(scale, scale, 0.0f, 0.0f);
        return imageTransform;
    }

    private float calcOptimalScale(float cropAreaWidth, float cropAreaHeight) {
        int activeWidth = getWidth() - 200;
        int activeHeight = getHeight() - 200;
        if (activeWidth <= 0 || activeHeight <= 0) {
            return 0.0f;
        }
        return cropAreaWidth / cropAreaHeight < ((float) activeWidth) / ((float) activeHeight) ? ((float) activeHeight) / cropAreaHeight : ((float) activeWidth) / cropAreaWidth;
    }

    private RectF calcCropAreaScreenRect(float cropAreaWidth, float cropAreaHeight, float scale) {
        RectF rect = new RectF(0.0f, 0.0f, cropAreaWidth * scale, cropAreaHeight * scale);
        rect.offset((((float) getWidth()) - rect.width()) / 2.0f, (((float) getHeight()) - rect.height()) / 2.0f);
        return rect;
    }

    private Matrix calcDefaultTransformMatrix(float scale) {
        float screenMidX = (float) Math.floor((double) (((float) getWidth()) / 2.0f));
        float screenMidY = (float) Math.floor((double) (((float) getHeight()) / 2.0f));
        Matrix matrix = new Matrix();
        matrix.postTranslate(screenMidX, screenMidY);
        matrix.postScale(scale, scale, screenMidX, screenMidY);
        matrix.preTranslate(((float) (-this.image.getWidth())) / 2.0f, ((float) (-this.image.getHeight())) / 2.0f);
        return matrix;
    }

    private void refreshTransformMatrix(float cropAreaWidth, float cropAreaHeight, boolean animated, boolean preserveTransform) {
        float scale = calcOptimalScale(cropAreaWidth, cropAreaHeight);
        if (scale > 0.0f) {
            Matrix newTransformMatrix;
            RectF newCropAreaScreenRect = calcCropAreaScreenRect(cropAreaWidth, cropAreaHeight, scale);
            if (!preserveTransform || this.transformMatrix == null) {
                newTransformMatrix = calcDefaultTransformMatrix(scale);
            } else {
                newTransformMatrix = getFitMatrix(this.transformMatrix, newCropAreaScreenRect);
            }
            if (this.transformMatrix == null || !animated) {
                if (this.transformMatrix == null) {
                    this.transformMatrix = newTransformMatrix;
                } else {
                    this.transformMatrix.set(newTransformMatrix);
                }
                setCropAreaScreenRect(newCropAreaScreenRect.left, newCropAreaScreenRect.top, newCropAreaScreenRect.right, newCropAreaScreenRect.bottom);
                return;
            }
            runTransitionAnimation(newCropAreaScreenRect, newTransformMatrix);
        }
    }

    private void fitCropArea() {
        float scale = 1.0f / this.transformMatrix.mapRadius(1.0f);
        this.cropAreaSize = new PointF(this.cropAreaScreenRect.width() * scale, this.cropAreaScreenRect.height() * scale);
        refreshTransformMatrix(this.cropAreaSize.x, this.cropAreaSize.y, true, false);
    }

    private void fitImage(boolean animated) {
        Matrix transform = getFitMatrix(this.transformMatrix, this.cropAreaScreenRect);
        if (transform == null) {
            return;
        }
        if (animated) {
            runTransitionAnimation(null, transform);
            return;
        }
        this.transformMatrix.set(transform);
        invalidate();
    }

    private Matrix getFitMatrix(Matrix currentTransform, RectF cropAreaScreenRect) {
        List<Line> imageEdges = getTransformedImageEdgeLoop(currentTransform);
        List<PointF> cropVertices = Arrays.asList(new PointF[]{new PointF(cropAreaScreenRect.left, cropAreaScreenRect.bottom), new PointF(cropAreaScreenRect.right, cropAreaScreenRect.bottom), new PointF(cropAreaScreenRect.right, cropAreaScreenRect.top), new PointF(cropAreaScreenRect.left, cropAreaScreenRect.top)});
        Line cropDiag0 = new Line((PointF) cropVertices.get(0), (PointF) cropVertices.get(2));
        Line cropDiag1 = new Line((PointF) cropVertices.get(1), (PointF) cropVertices.get(3));
        PointF cropVertex0 = (PointF) cropVertices.get(0);
        PointF cropVertex2 = (PointF) cropVertices.get(2);
        float scale = Geometry.distance(cropVertex0.x, cropVertex0.y, cropVertex2.x, cropVertex2.y) / Math.min(Math.min(CropRotateViewHelper.getIntersectionDistance((Line) imageEdges.get(0), (Line) imageEdges.get(2), cropDiag0), CropRotateViewHelper.getIntersectionDistance((Line) imageEdges.get(0), (Line) imageEdges.get(2), cropDiag1)), Math.min(CropRotateViewHelper.getIntersectionDistance((Line) imageEdges.get(1), (Line) imageEdges.get(3), cropDiag0), CropRotateViewHelper.getIntersectionDistance((Line) imageEdges.get(1), (Line) imageEdges.get(3), cropDiag1)));
        Matrix matrix = new Matrix(currentTransform);
        if (scale > 1.0f) {
            PointF imageCenter = getTransformedImageCenter(currentTransform);
            matrix.postTranslate(cropAreaScreenRect.centerX() - imageCenter.x, cropAreaScreenRect.centerY() - imageCenter.y);
            matrix.postScale(scale, scale, cropAreaScreenRect.centerX(), cropAreaScreenRect.centerY());
        } else {
            for (Line imageEdge : imageEdges) {
                float maxDistance = 0.0f;
                for (int i = 0; i < 4; i++) {
                    PointF cropVertex = (PointF) cropVertices.get(i);
                    if (imageEdge.apply(cropVertex.x, cropVertex.y) > 0.0f) {
                        maxDistance = Math.max(Geometry.distance(imageEdge, cropVertex.x, cropVertex.y), maxDistance);
                    }
                }
                if (maxDistance > 0.0f) {
                    Vector moveVector = Vector.buildNormalizedVector(imageEdge.a, imageEdge.b).scale(maxDistance);
                    matrix.postTranslate(moveVector.a, moveVector.b);
                }
            }
        }
        return matrix.equals(currentTransform) ? null : matrix;
    }

    private void runTransitionAnimation(RectF targetCropAreaRect, Matrix targetImageTransform) {
        if (this.transitionAnimator != null) {
            this.transitionAnimator.cancel();
        }
        if (targetCropAreaRect != null || targetImageTransform != null) {
            final RectF startCropRect = targetCropAreaRect != null ? new RectF(this.cropAreaScreenRect) : null;
            final float[] startValues = new float[9];
            final float[] endValues = new float[9];
            final float[] currentValues = new float[9];
            if (targetImageTransform != null) {
                this.transformMatrix.getValues(startValues);
                targetImageTransform.getValues(endValues);
            }
            this.transitionAnimator = ValueAnimator.ofFloat(new float[]{0.0f, 1.0f});
            this.transitionAnimator.setInterpolator(new DecelerateInterpolator());
            final RectF rectF = targetCropAreaRect;
            final Matrix matrix = targetImageTransform;
            this.transitionAnimator.addUpdateListener(new AnimatorUpdateListener() {
                public void onAnimationUpdate(ValueAnimator animator) {
                    float fraction = animator.getAnimatedFraction();
                    if (rectF != null) {
                        CropRotateView.this.setCropAreaScreenRect(CropRotateViewHelper.interpolateValue(startCropRect.left, rectF.left, fraction), CropRotateViewHelper.interpolateValue(startCropRect.top, rectF.top, fraction), CropRotateViewHelper.interpolateValue(startCropRect.right, rectF.right, fraction), CropRotateViewHelper.interpolateValue(startCropRect.bottom, rectF.bottom, fraction));
                    }
                    if (matrix != null) {
                        CropRotateViewHelper.interpolateMatrix(currentValues, startValues, endValues, fraction);
                        CropRotateView.this.transformMatrix.setValues(currentValues);
                    }
                    CropRotateView.this.invalidate();
                }
            });
            this.transitionAnimator.addListener(new AnimatorListener() {
                public void onAnimationStart(Animator animator) {
                }

                public void onAnimationEnd(Animator animator) {
                    cleanup();
                }

                public void onAnimationCancel(Animator animator) {
                    cleanup();
                }

                public void onAnimationRepeat(Animator animator) {
                }

                private void cleanup() {
                    CropRotateView.this.transitionAnimator = null;
                    CropRotateView.this.exceedingCropArea = false;
                    CropRotateView.this.invalidate();
                }
            });
            this.transitionAnimator.start();
        }
    }

    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (this.cropAreaSize != null) {
            refreshTransformMatrix(this.cropAreaSize.x, this.cropAreaSize.y, false, false);
        }
    }

    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (this.exceedingCropArea) {
            this.cropAreaBackground.draw(canvas);
        }
        canvas.save();
        canvas.drawBitmap(this.dimmedImage, this.transformMatrix, this.imagePaint);
        canvas.clipRect(this.cropAreaScreenRect);
        this.imagePaint.setColorFilter(null);
        canvas.drawBitmap(this.image, this.transformMatrix, this.imagePaint);
        canvas.restore();
        drawGrid(canvas, this.isFreeTransform, this.exceedingCropArea);
    }

    void drawGrid(Canvas canvas, boolean isFreeTransformGrid, boolean showWarning) {
        if (isFreeTransformGrid) {
            drawGrid(canvas, this.secondaryCropGridPaint, this.cropAreaScreenRect, 9, 9);
        }
        drawGrid(canvas, this.normalCropGridPaint, this.cropAreaScreenRect, 3, 3);
        if (showWarning) {
            canvas.drawRect(this.cropAreaScreenRect, this.warningCropGridPaint);
        }
    }

    private void drawGrid(Canvas canvas, Paint paint, RectF gridRect, int rowCount, int colCount) {
        if (gridRect.width() >= 1.0f && gridRect.height() >= 1.0f) {
            int i;
            if (rowCount < 1) {
                rowCount = 1;
            }
            if (colCount < 1) {
                colCount = 1;
            }
            for (i = 0; i <= rowCount; i++) {
                float y = gridRect.top + ((((float) i) * gridRect.height()) / ((float) rowCount));
                canvas.drawLine(gridRect.left, y, gridRect.right, y, paint);
            }
            for (i = 0; i <= colCount; i++) {
                float x = gridRect.left + ((((float) i) * gridRect.width()) / ((float) colCount));
                canvas.drawLine(x, gridRect.top, x, gridRect.bottom, paint);
            }
        }
    }

    public boolean onTouchEvent(MotionEvent event) {
        return (this.allowCropAreaTransform && this.cropTransformHandler.onTouchEvent(event)) || this.freeTransformHandler.onTouchEvent(event);
    }

    private void setCropAreaScreenRect(float left, float top, float right, float bottom) {
        this.cropAreaScreenRect.set(left, top, right, bottom);
        this.cropAreaBackground.setBounds((int) this.cropAreaScreenRect.left, (int) this.cropAreaScreenRect.top, (int) this.cropAreaScreenRect.right, (int) this.cropAreaScreenRect.bottom);
        this.cropTransformHandler.setCropAreaRect(this.cropAreaScreenRect);
    }

    private List<Line> getTransformedImageEdgeLoop(Matrix transformMatrix) {
        float[] imageVertices = new float[]{0.0f, 0.0f, (float) this.image.getWidth(), 0.0f, (float) this.image.getWidth(), (float) this.image.getHeight(), 0.0f, (float) this.image.getHeight()};
        transformMatrix.mapPoints(imageVertices);
        return CropRotateViewHelper.buildEdgeLoop(imageVertices);
    }

    private PointF getTransformedImageCenter(Matrix transformMatrix) {
        float[] imageCenter = new float[]{((float) this.image.getWidth()) / 2.0f, ((float) this.image.getHeight()) / 2.0f};
        transformMatrix.mapPoints(imageCenter);
        return new PointF(imageCenter[0], imageCenter[1]);
    }

    private boolean isExceedingCropArea() {
        List<Line> imageEdges = getTransformedImageEdgeLoop(this.transformMatrix);
        for (PointF cropVertex : Arrays.asList(new PointF[]{new PointF(this.cropAreaScreenRect.left, this.cropAreaScreenRect.bottom), new PointF(this.cropAreaScreenRect.right, this.cropAreaScreenRect.bottom), new PointF(this.cropAreaScreenRect.right, this.cropAreaScreenRect.top), new PointF(this.cropAreaScreenRect.left, this.cropAreaScreenRect.top)})) {
            if (!CropRotateViewHelper.isPointInsidePoly(imageEdges, cropVertex.x, cropVertex.y)) {
                return true;
            }
        }
        return false;
    }

    public void onStartTranslate(float touchX, float touchY) {
        this.isFreeTransform = false;
        this.startTransformMatrix.set(this.transformMatrix);
        this.startTouches[0].x = touchX;
        this.startTouches[0].y = touchY;
    }

    public void onUpdateTranslate(float touchX, float touchY) {
        this.transformMatrix.set(this.startTransformMatrix);
        this.transformMatrix.postTranslate(touchX - this.startTouches[0].x, touchY - this.startTouches[0].y);
        this.exceedingCropArea = isExceedingCropArea();
        invalidate();
    }

    public void onStartFreeTransform(float touchX0, float touchY0, float touchX1, float touchY1) {
        this.isFreeTransform = true;
        this.startTransformMatrix.set(this.transformMatrix);
        float spanX = touchX1 - touchX0;
        float spanY = touchY1 - touchY0;
        this.startTouches[0].x = touchX0;
        this.startTouches[0].y = touchY0;
        this.startTouches[1].x = touchX1;
        this.startTouches[1].y = touchY1;
        this.startSpan = (float) Math.sqrt((double) ((spanX * spanX) + (spanY * spanY)));
        this.startAngle = (float) Math.toDegrees(Math.atan2((double) spanY, (double) spanX));
        this.currentAngle = this.startAngle;
        this.currentScale = 1.0f;
        this.startCenter.x = (this.startTouches[0].x + this.startTouches[1].x) / 2.0f;
        this.startCenter.y = (this.startTouches[0].y + this.startTouches[1].y) / 2.0f;
        this.startCropAreaSize.set(this.cropAreaSize);
        this.lastTouches[0].x = touchX0;
        this.lastTouches[0].y = touchY0;
        this.lastTouches[1].x = touchX1;
        this.lastTouches[1].y = touchY1;
    }

    public void onUpdateFreeTransform(float touchX0, float touchY0, float touchX1, float touchY1) {
        this.transformMatrix.set(this.startTransformMatrix);
        float spanX = touchX1 - touchX0;
        float spanY = touchY1 - touchY0;
        float span = (float) Math.sqrt((double) ((spanX * spanX) + (spanY * spanY)));
        PointF center = new PointF((touchX0 + touchX1) / 2.0f, (touchY0 + touchY1) / 2.0f);
        if (span >= SCALE_ROTATE_NO_FEEDBACK_THRESHOLD) {
            this.currentScale = span / this.startSpan;
            this.currentAngle = (float) Math.toDegrees(Math.atan2((double) spanY, (double) spanX));
        }
        this.transformMatrix.postTranslate(center.x - this.startCenter.x, center.y - this.startCenter.y);
        this.transformMatrix.postRotate(this.currentAngle - this.startAngle, center.x, center.y);
        this.transformMatrix.postScale(this.currentScale, this.currentScale, center.x, center.y);
        this.lastTouches[0].x = touchX0;
        this.lastTouches[0].y = touchY0;
        this.lastTouches[1].x = touchX1;
        this.lastTouches[1].y = touchY1;
        this.exceedingCropArea = isExceedingCropArea();
        invalidate();
    }

    public void onFinishedTransform() {
        this.isFreeTransform = false;
        if (this.exceedingCropArea) {
            fitImage(true);
        } else {
            invalidate();
        }
    }

    public void onStartCropAreaTransform() {
    }

    public void onUpdateCropAreaTransform(float left, float top, float right, float bottom) {
        setCropAreaScreenRect(left, top, right, bottom);
        this.exceedingCropArea = isExceedingCropArea();
        invalidate();
    }

    public void onFinishCropAreaTransform() {
        fitCropArea();
    }

    private Bitmap createDimmedBitmap(Bitmap image) {
        if (image == null) {
            return null;
        }
        Bitmap dimmedImage = Bitmap.createBitmap(image.getWidth(), image.getHeight(), image.getConfig());
        ColorMatrix satColorMatrix = new ColorMatrix();
        satColorMatrix.setSaturation(0.0f);
        ColorMatrix dimColorMatrix = new ColorMatrix();
        dimColorMatrix.setScale(0.5f, 0.5f, 0.5f, 1.0f);
        satColorMatrix.postConcat(dimColorMatrix);
        ColorFilter filter = new ColorMatrixColorFilter(satColorMatrix);
        Canvas canvas = new Canvas(dimmedImage);
        Paint imagePaint = new Paint();
        imagePaint.setColorFilter(filter);
        canvas.drawBitmap(image, 0.0f, 0.0f, imagePaint);
        return dimmedImage;
    }
}
