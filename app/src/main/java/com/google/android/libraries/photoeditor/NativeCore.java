package com.google.android.libraries.photoeditor;

import android.content.ContextWrapper;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Matrix;
import com.google.android.libraries.photoeditor.RenderFilterInterface.PreviewRenderer;
import com.google.android.libraries.photoeditor.filterparameters.FilterParameter;
import com.google.android.libraries.photoeditor.util.BitmapHelper;
import com.google.android.libraries.photoeditor.util.ContextHelper;

public enum NativeCore {
    INSTANCE;
    
    private static  Options DEFAULT_LOAD_BITMAP_OPTIONS = null;
    private ContextWrapper contextWrapper;
    private boolean isCompare;
    private NativeCoreNotificationListener notificationListener;
    private PreviewRenderer previewRenderer;
    private TilesProvider tilesProvider;

    public interface NativeCoreNotificationListener {
        void onInitializedOnScreenFilter();
    }

    public static native int activateOffScreenFilter(int i);

    private native int activateOnScreenFilter(int i, boolean z);

    public static native int allocateExportImage(int i, int i2);

    public static native int contextAction(FilterParameter filterParameter, int i);

    public static native void copyExportImageToBitmap(int i, int i2, Bitmap bitmap);

    public static native int createRGBX8TextureFromBitmap(int i, int i2, int i3, Bitmap bitmap, int i4, int i5, int i6, int i7);

    public static native void deactivateOffScreenFilter();

    public static native void deactivateOnScreenFilter();

    public static native void deallocateExportImage();

    public static native void deleteOffscreenContext();

    public static native void deleteTexture(int i);

    public static native void drawOverlayEllipse(float f, float f2, float f3, float f4, float f5, int i, int i2);

    public static native void drawOverlayLine(float f, float f2, float f3, float f4, int i, int i2);

    public static native boolean frameShouldShuffle(int i);

    public static native int getDefaultValue(int i, int i2);

    public static native int getMaxValue(int i, int i2);

    public static native int getMinValue(int i, int i2);

    public static native int initOffscreenContext();

    public static native void offscreenContextMakeCurrent();

    public static native int offscreenFilter(FilterParameter filterParameter, TilesProvider tilesProvider, Tile tile, TilesProvider tilesProvider2);

    public static native int offscreenFilterHundredPercentRegion(FilterParameter filterParameter, Bitmap bitmap, int i, int i2, int i3, int i4, Bitmap bitmap2);

    public static native int offscreenFilterPreviewToBuffer(FilterParameter filterParameter, TilesProvider tilesProvider, int i, int i2, byte[] bArr);

    public static native int offscreenPrefilter(FilterParameter filterParameter, TilesProvider tilesProvider, TilesProvider tilesProvider2);

    public static native void offscreenPrepareToApplyImage();

    public static native void onSurfaceChanged();

    public static native int onscreenFilterPreviewToScreen(FilterParameter filterParameter, TilesProvider tilesProvider, int i, int i2);

    public static native int onscreenFilterTileToScreen(FilterParameter filterParameter, TilesProvider tilesProvider, Tile tile, int i, int i2, boolean z, boolean z2);

    public static native int onscreenPrefilter(FilterParameter filterParameter, TilesProvider tilesProvider);

    public static native Bitmap renderFilterChain(Bitmap bitmap, FilterChain filterChain);

    public static native void resetRenderScaleMode();

    public static native int retroluxGetLeakType(int i);

    public static native int[] retroluxGetLeaks(int i);

    public static native int retroluxGetScratchType(int i);

    public static native int[] retroluxGetScratches(int i);

    public static native int scaleImage(Bitmap bitmap, Bitmap bitmap2);

    public static native void setDefaultRenderScaleMode(int i);

    public static native void setRenderScaleMode(int i);

    static {
        DEFAULT_LOAD_BITMAP_OPTIONS = new Options();
        DEFAULT_LOAD_BITMAP_OPTIONS.inPreferredConfig = Config.ARGB_8888;
        DEFAULT_LOAD_BITMAP_OPTIONS.inDither = false;
        DEFAULT_LOAD_BITMAP_OPTIONS.inScaled = false;
        DEFAULT_LOAD_BITMAP_OPTIONS.inPreferQualityOverSpeed = true;
        DEFAULT_LOAD_BITMAP_OPTIONS.inMutable = false;
        DEFAULT_LOAD_BITMAP_OPTIONS.inPurgeable = true;
    }

    public void initContext(ContextWrapper contextWrapper) {
        this.contextWrapper = contextWrapper;
    }

    public void cleanupContext() {
        this.contextWrapper = null;
    }

    public void setTilesProvider(TilesProvider tilesProvider) {
        this.tilesProvider = tilesProvider;
    }

    public void setPreviewRenderer(PreviewRenderer previewRenderer) {
        this.previewRenderer = previewRenderer;
    }

    public void setNotificationListener(NativeCoreNotificationListener listener) {
        this.notificationListener = listener;
    }

    public static int createRGBX8TextureFromBitmap(int interpolationMode, int format, int wrap, Bitmap bitmap) {
        if (bitmap == null || bitmap.isRecycled()) {
            throw new IllegalArgumentException();
        }
        return createRGBX8TextureFromBitmap(interpolationMode, format, wrap, bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight());
    }

    public void activateOnScreenFilterChecked(FilterParameter filter) {
        int filterType = filter.getFilterType();
        int status = activateOnScreenFilter(filterType, false);
        if (status < 0) {
            throw new IllegalStateException();
        } else if (status == 0) {
            if (!(this.notificationListener == null || filterType == 1)) {
                this.notificationListener.onInitializedOnScreenFilter();
            }
            contextAction(filter, 6);
            contextAction(filter, 7);
        }
    }

    public synchronized boolean getCompare() {
        return this.isCompare;
    }

    public synchronized void setCompare(boolean value) {
        this.isCompare = value;
    }

    public Bitmap getPreviewSrcImage(int width, int height) {
        if (this.tilesProvider == null) {
            throw new IllegalStateException("Tiles provider hasn't been set up");
        }
        Bitmap previewSrcImage = this.tilesProvider.getPreviewSourceImage();
        if (width <= 0 || height <= 0) {
            return previewSrcImage;
        }
        return BitmapHelper.convertToRGBA8(Bitmap.createScaledBitmap(previewSrcImage, width, height, true));
    }

    public Bitmap getScaledSrcImage(int width, int height) {
        if (this.tilesProvider == null) {
            throw new IllegalStateException("Tiles provider hasn't been set up");
        }
        Bitmap fullSizeImage = this.tilesProvider.getSourceImage();
        int fullSizeWidth = fullSizeImage.getWidth();
        int fullSizeHeight = fullSizeImage.getHeight();
        if (width > fullSizeWidth || height > fullSizeHeight) {
            return fullSizeImage;
        }
        if (width == fullSizeWidth && height == fullSizeHeight) {
            return fullSizeImage;
        }
        return (width == -1 && height == -1) ? fullSizeImage : BitmapHelper.convertToRGBA8(Bitmap.createScaledBitmap(fullSizeImage, width, height, true));
    }

    public Bitmap loadBitmapResource(int resourceId) {
        if (this.contextWrapper == null) {
            throw new IllegalStateException("Context have not been initialized (use initContext())");
        }
        Bitmap image = BitmapFactory.decodeResource(this.contextWrapper.getResources(), resourceId, DEFAULT_LOAD_BITMAP_OPTIONS);
        if (image != null) {
            return BitmapHelper.convertToRGBA8(image);
        }
        throw new IllegalStateException();
    }

    public Bitmap loadBitmapResource(String resourceName) {
        return loadBitmapResource(ContextHelper.getDrawableResourceId(resourceName));
    }

    public Bitmap loadBackgroundTexture(int resourceId, int backgroundTextureOptions) {
        Bitmap image = loadBitmapResource(resourceId);
        Matrix matrix = new Matrix();
        switch (backgroundTextureOptions) {
            case 1:
                matrix.postScale(-1.0f, 1.0f);
                break;
            case 2:
                matrix.postScale(1.0f, -1.0f);
                break;
            case 3:
                matrix.postRotate(90.0f);
                break;
            case 4:
                matrix.postRotate(-90.0f);
                break;
        }
        Bitmap transformedImage = image;
        if (matrix.isIdentity()) {
            return transformedImage;
        }
        transformedImage = Bitmap.createBitmap(image, 0, 0, image.getWidth(), image.getHeight(), matrix, false);
        image.recycle();
        return transformedImage;
    }

    public Bitmap loadBackgroundTexture(String resourceName, int backgroundTextureOptions) {
        return loadBackgroundTexture(ContextHelper.getDrawableResourceId(resourceName), backgroundTextureOptions);
    }

    public Bitmap createAutorotatedTexture(String resourceName, int backgroundTextureOptions, float imageAspectRatio) {
        int i = 1;
        Bitmap bitmap = loadBackgroundTexture(ContextHelper.getDrawableResourceId(resourceName), 0);
        int imageWidth = bitmap.getWidth();
        int imageHeight = bitmap.getHeight();
        float ratio = ((float) imageWidth) / ((float) imageHeight);
        if (ratio == 1.0f || imageAspectRatio == 1.0f) {
            return bitmap;
        }
        int i2 = ratio > 1.0f ? 1 : 0;
        if (imageAspectRatio <= 1.0f) {
            i = 0;
        }
        if (i2 == i) {
            return bitmap;
        }
        Matrix matrix = new Matrix();
        matrix.postRotate(90.0f);
        Bitmap rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, imageWidth, imageHeight, matrix, false);
        bitmap.recycle();
        return rotatedBitmap;
    }

    public void recycleBitmap(Bitmap bitmap) {
        if (this.tilesProvider == null || !(bitmap == this.tilesProvider.getSourceImage() || bitmap == this.tilesProvider.getPreviewSourceImage() || bitmap == this.tilesProvider.getScreenSourceImage())) {
            bitmap.recycle();
        }
    }

    public void requestRerender() {
        if (this.previewRenderer != null) {
            this.previewRenderer.requestRenderPreview();
        }
    }
}
