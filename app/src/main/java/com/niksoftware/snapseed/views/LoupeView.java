package com.niksoftware.snapseed.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Path.Direction;
import android.graphics.Rect;
import android.view.View;
import android.view.ViewGroup;
import com.niksoftware.snapseed.MainActivity;
import com.niksoftware.snapseed.core.RenderFilterInterface.OnRenderListener;

public class LoupeView extends ViewGroup {
    private static final float FACTOR_OUTER_TO_INNER_RADIUS = 0.76f;
    private final View borderView;
    private boolean compareImageMode = false;
    private int lastX = -1;
    private int lastY = -1;
    private final LoupeImageView loupeImageView;
    private int loupeSizeOnScreen;
    private final boolean showPixelColor;
    private final boolean showRenderedImage;
    private IUpdateListener updateListener = null;

    interface IUpdateListener {
        void onUpdate(boolean z);
    }

    private class ImageAreaOnRenderListener implements OnRenderListener {
        private final boolean alignBottom;
        private final boolean alignLeft;
        private final boolean alignRight;
        private final boolean alignTop;
        private final int centerX;
        private final int centerY;

        public ImageAreaOnRenderListener(int centerX, int centerY, boolean alignLeft, boolean alignTop, boolean alignRight, boolean alignBottom) {
            this.centerX = centerX;
            this.centerY = centerY;
            this.alignLeft = alignLeft;
            this.alignTop = alignTop;
            this.alignRight = alignRight;
            this.alignBottom = alignBottom;
        }

        public void onRenderProgressUpdate(int currentStage, int stageCount) {
        }

        public void onRenderCancelled() {
        }

        public void onRenderFinished(Bitmap renderResult) {
            LoupeView.this.loupeImageView.setBitmap(renderResult, this.alignLeft, this.alignTop, this.alignRight, this.alignBottom);
            LoupeView.this.setColor(this.centerX, this.centerY);
            LoupeView.this.loupeImageView.invalidate();
        }
    }

    private static class LoupeImageView extends View {
        private static final int CROSS_STICK_SIZE = 10;
        private static final int LOUPE_BORDER_SIZE = 10;
        private int color;
        private Bitmap image;
        private boolean imageAlignBottom;
        private boolean imageAlignLeft;
        private boolean imageAlignRight;
        private boolean imageAlignTop;
        private final Paint paint = new Paint();
        private final Path path = new Path();

        public LoupeImageView(Context context) {
            super(context);
            setLayerType(1, null);
            this.paint.setColor(-65536);
        }

        public void setBitmap(Bitmap bitmap, boolean alignLeft, boolean alignTop, boolean alignRight, boolean alignBottom) {
            this.image = bitmap;
            this.image.setHasAlpha(false);
            this.imageAlignLeft = alignLeft;
            this.imageAlignTop = alignTop;
            this.imageAlignRight = alignRight;
            this.imageAlignBottom = alignBottom;
        }

        protected void onDraw(Canvas canvas) {
            if (this.image != null) {
                int midX = getWidth() / 2;
                int midY = getHeight() / 2;
                this.path.reset();
                this.path.addCircle((float) midX, (float) midY, (float) (getWidth() / 2), Direction.CCW);
                canvas.clipPath(this.path);
                canvas.drawColor(this.color != 0 ? this.color : -16777216);
                if (this.color != 0) {
                    this.path.reset();
                    this.path.addCircle((float) midX, (float) midY, (float) ((getWidth() / 2) - 10), Direction.CCW);
                    canvas.clipPath(this.path);
                }
                int x = 0;
                int y = 0;
                if (this.imageAlignLeft && this.imageAlignRight) {
                    x = (getWidth() - this.image.getWidth()) / 2;
                } else if (this.imageAlignLeft) {
                    x = 0;
                } else if (this.imageAlignRight) {
                    x = getWidth() - this.image.getWidth();
                }
                if (this.imageAlignTop && this.imageAlignBottom) {
                    y = (getHeight() - this.image.getHeight()) / 2;
                } else if (this.imageAlignTop) {
                    y = 0;
                } else if (this.imageAlignBottom) {
                    y = getHeight() - this.image.getHeight();
                }
                canvas.drawBitmap(this.image, (float) x, (float) y, null);
                if (this.color != 0) {
                    canvas.drawLine((float) (midX - 10), (float) midY, (float) (midX + 10), (float) midY, this.paint);
                    canvas.drawLine((float) midX, (float) (midY - 10), (float) midX, (float) (midY + 10), this.paint);
                }
            }
        }
    }

    public LoupeView(Context context, boolean showPixelColor, boolean showRenderedImage) {
        super(context);
        setWillNotDraw(true);
        this.showPixelColor = showPixelColor;
        this.showRenderedImage = showRenderedImage;
        this.loupeImageView = new LoupeImageView(getContext());
        addView(this.loupeImageView);
        this.borderView = new View(getContext());
        this.borderView.setWillNotDraw(true);
        this.borderView.setBackgroundResource(R.drawable.loupebackground);
        addView(this.borderView);
        this.loupeSizeOnScreen = (int) context.getResources().getDimension(R.dimen.loupe_base_size);
    }

    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        int loupeSize = loupeImageViewBaseSize();
        int loupeLeft = Math.round(((float) (getWidth() - loupeSize)) / 2.0f);
        int loupeTop = Math.round(((float) (getHeight() - loupeSize)) / 2.0f);
        this.loupeImageView.layout(loupeLeft, loupeTop, loupeLeft + loupeSize, loupeTop + loupeSize);
        this.borderView.layout(0, 0, getWidth(), getHeight());
    }

    private void setColor(int x, int y) {
        if (this.showPixelColor) {
            Bitmap hundredPercentImage = MainActivity.getWorkingAreaView().getTilesProvider().getSourceImage();
            this.loupeImageView.color = hundredPercentImage.getPixel(Math.min(Math.max(0, x), hundredPercentImage.getWidth() - 1), Math.min(Math.max(0, y), hundredPercentImage.getHeight() - 1));
            return;
        }
        this.loupeImageView.color = 0;
    }

    public void setCompareImageMode(boolean mode) {
        this.compareImageMode = mode;
    }

    public void updateImage() {
        if (this.updateListener != null) {
            this.updateListener.onUpdate(showRenderedImage());
        }
    }

    public void updateImage(int x, int y) {
        updateImage(x, y, false);
    }

    public void updateImage(int x, int y, boolean forceUpdate) {
        if (forceUpdate || this.lastX != x || this.lastY != y) {
            this.lastX = x;
            this.lastY = y;
            final Bitmap hundredPercentImage = MainActivity.getWorkingAreaView().getTilesProvider().getSourceImage();
            int halfWidth = this.loupeImageView.getWidth() / 2;
            final Rect cutRect = new Rect(x - halfWidth, y - halfWidth, x + halfWidth, y + halfWidth);
            final boolean alignLeft = cutRect.right >= hundredPercentImage.getWidth();
            final boolean alignTop = cutRect.bottom >= hundredPercentImage.getHeight();
            final boolean alignBottom = cutRect.top <= 0;
            final boolean alignRight = cutRect.left <= 0;
            cutRect.intersect(0, 0, hundredPercentImage.getWidth() - 1, hundredPercentImage.getHeight() - 1);
            final int i = x;
            final int i2 = y;
            this.updateListener = new IUpdateListener() {
                public void onUpdate(boolean showRenderedImage) {
                    if (showRenderedImage) {
                        WorkingAreaView workingAreaView = MainActivity.getWorkingAreaView();
                        ((ImageViewGL) workingAreaView.getImageView()).requestRenderImage(workingAreaView.getTilesProvider(), cutRect, MainActivity.getFilterParameter(), new ImageAreaOnRenderListener(i, i2, alignLeft, alignTop, alignRight, alignBottom), true);
                        return;
                    }
                    LoupeView.this.loupeImageView.setBitmap(Bitmap.createBitmap(hundredPercentImage, cutRect.left, cutRect.top, cutRect.width(), cutRect.height()), alignLeft, alignTop, alignRight, alignBottom);
                    LoupeView.this.setColor(i, i2);
                    LoupeView.this.loupeImageView.invalidate();
                }
            };
            this.updateListener.onUpdate(showRenderedImage());
        }
    }

    public int getLoupeSize() {
        return this.loupeSizeOnScreen;
    }

    private int loupeImageViewBaseSize() {
        return (int) Math.floor((double) (((float) getWidth()) * FACTOR_OUTER_TO_INNER_RADIUS));
    }

    private boolean showRenderedImage() {
        return !this.compareImageMode && this.showRenderedImage;
    }
}
